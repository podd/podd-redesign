/**
 * PODD is an OWL ontology database used for scientific project management
 * 
 * Copyright (C) 2009-2013 The University Of Queensland
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedArtifactVersionException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddWebConstants;

/**
 * Service for executing SPARQL queries over specified artifacts (and their Schema ontologies) that
 * users have access to.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class SparqlResourceImpl extends AbstractPoddResourceImpl
{
    @Get(":rdf|rj|json|ttl")
    public Representation getSparqlRdf(final Variant variant) throws ResourceException
    {
        // TODO: Support an interactive HTML page that users can enter queries
        // on and see results
        
        this.log.debug("getSparqlRdf");
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        // variables for request parameters
        String sparqlQuery = null;
        boolean includeConcrete = true;
        boolean includeInferred = true;
        boolean includeSchema = true;
        String[] artifactUris;
        
        // sparql query - mandatory parameter
        sparqlQuery = this.getQuery().getFirstValue(PoddWebConstants.KEY_SPARQLQUERY, true);
        if(sparqlQuery == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "SPARQL query not submitted");
        }
        
        // artifact ids to search across
        artifactUris = this.getQuery().getValuesArray(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
        
        final String includeConcreteStatements =
                this.getQuery().getFirstValue(PoddWebConstants.KEY_INCLUDE_CONCRETE, true);
        includeConcrete = true;
        if(includeConcreteStatements != null)
        {
            includeConcrete = Boolean.valueOf(includeConcreteStatements);
        }
        
        final String includeInferredStatements =
                this.getQuery().getFirstValue(PoddWebConstants.KEY_INCLUDE_INFERRED, true);
        if(includeInferredStatements != null)
        {
            includeInferred = Boolean.valueOf(includeInferredStatements);
        }
        
        final String includeSchemaStatements = this.getQuery().getFirstValue(PoddWebConstants.KEY_INCLUDE_SCHEMA, true);
        if(includeSchemaStatements != null)
        {
            includeSchema = Boolean.valueOf(includeSchemaStatements);
        }
        
        try
        {
            return this.doSparqlInternal(sparqlQuery, includeConcrete, includeInferred, includeSchema, artifactUris,
                    variant);
        }
        catch(OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        }
    }
    
    @Post(":rdf|rj|json|ttl")
    public Representation postSparqlRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        this.log.debug("postSparqlRdf");
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        if(entity == null)
        {
            // POST request with no entity.
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not submit anything");
        }
        
        this.log.info("media-type: {}", entity.getMediaType());
        
        // variables for request parameters
        String sparqlQuery = null;
        boolean includeConcrete = true;
        boolean includeInferred = true;
        boolean includeSchema = true;
        String[] artifactUris;
        
        if(MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true))
        {
            throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED,
                    "TODO: Implement support for HTTP POST using multipart/form-data content type");
            
        }
        else
        {
            final FormDataSet form = new FormDataSet(entity);
            
            // sparql query - mandatory parameter
            sparqlQuery = form.getEntries().getFirstValue(PoddWebConstants.KEY_SPARQLQUERY, true);
            if(sparqlQuery == null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "SPARQL query not submitted");
            }
            
            // artifact ids to search across
            artifactUris = form.getEntries().getValuesArray(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
            if(artifactUris == null || artifactUris.length == 0)
            {
                // TODO: Support execution of sparql queries over all accessible
                // artifacts if they
                // did
                // not specify any artifacts
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No artifacts specified in request");
            }
            
            final String includeConcreteStatements =
                    form.getEntries().getFirstValue(PoddWebConstants.KEY_INCLUDE_CONCRETE, true);
            includeConcrete = true;
            if(includeConcreteStatements != null)
            {
                includeConcrete = Boolean.valueOf(includeConcreteStatements);
            }
            
            final String includeInferredStatements =
                    form.getEntries().getFirstValue(PoddWebConstants.KEY_INCLUDE_INFERRED, true);
            if(includeInferredStatements != null)
            {
                includeInferred = Boolean.valueOf(includeInferredStatements);
            }
            
            final String includeSchemaStatements =
                    form.getEntries().getFirstValue(PoddWebConstants.KEY_INCLUDE_SCHEMA, true);
            if(includeSchemaStatements != null)
            {
                includeSchema = Boolean.valueOf(includeSchemaStatements);
            }
        }
        
        try
        {
            return this.doSparqlInternal(sparqlQuery, includeConcrete, includeInferred, includeSchema, artifactUris,
                    variant);
        }
        catch(OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        }
    }
    
    private Representation doSparqlInternal(final String sparqlQuery, final boolean includeConcrete,
            final boolean includeInferred, final boolean includeSchema, final String[] artifactUris,
            final Variant variant) throws ResourceException, OpenRDFException
    {
        final Set<InferredOWLOntologyID> artifactIds = new LinkedHashSet<>();
        
        // If they didn't specify any artifacts, run the query across all of the artifacts that they
        // have access to
        if(artifactUris == null || artifactUris.length == 0)
        {
            List<InferredOWLOntologyID> unpublishedArtifacts = this.getPoddArtifactManager().listUnpublishedArtifacts();
            
            for(InferredOWLOntologyID unpublishedArtifact : unpublishedArtifacts)
            {
                boolean checkAuthentication =
                        this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ, unpublishedArtifact
                                .getOntologyIRI().toOpenRDFURI(), false);
                
                if(checkAuthentication)
                {
                    artifactIds.add(unpublishedArtifact);
                }
            }
            
            List<InferredOWLOntologyID> publishedArtifacts = this.getPoddArtifactManager().listPublishedArtifacts();
            
            for(InferredOWLOntologyID publishedArtifact : publishedArtifacts)
            {
                boolean checkAuthentication =
                        this.checkAuthentication(PoddAction.PUBLISHED_ARTIFACT_READ, publishedArtifact.getOntologyIRI()
                                .toOpenRDFURI(), false);
                
                if(checkAuthentication)
                {
                    artifactIds.add(publishedArtifact);
                }
            }
        }
        else
        {
            for(final String nextArtifactUri : artifactUris)
            {
                try
                {
                    final InferredOWLOntologyID ontologyID =
                            this.getPoddArtifactManager().getArtifact(IRI.create(nextArtifactUri));
                    
                    if(this.getPoddArtifactManager().isPublished(ontologyID))
                    {
                        this.checkAuthentication(PoddAction.PUBLISHED_ARTIFACT_READ, ontologyID.getOntologyIRI()
                                .toOpenRDFURI());
                    }
                    else
                    {
                        this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ, ontologyID.getOntologyIRI()
                                .toOpenRDFURI());
                    }
                    artifactIds.add(ontologyID);
                }
                catch(final UnmanagedSchemaIRIException e)
                {
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                            "Could not find a requested schema ontology", e);
                }
                catch(final UnmanagedArtifactIRIException e)
                {
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find a requested artifact",
                            e);
                }
                catch(final OpenRDFException e)
                {
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Repository exception occurred", e);
                }
            }
        }
        
        if(artifactIds.isEmpty())
        {
            throw new ResourceException(Status.CLIENT_ERROR_PRECONDITION_FAILED,
                    "Server does not contain any artifacts that you can view, so no SPARQL queries able to be performed right now.");
        }
        
        final Model results = new LinkedHashModel();
        
        for(final InferredOWLOntologyID ontologyID : artifactIds)
        {
            RepositoryConnection conn = null;
            try
            {
                final Set<InferredOWLOntologyID> schemaImports =
                        this.getPoddArtifactManager().getSchemaImports(ontologyID);
                conn = this.getPoddRepositoryManager().getPermanentRepository(schemaImports).getConnection();
                final Set<URI> contextSet = new HashSet<>();
                if(includeConcrete)
                {
                    contextSet.addAll(Arrays.asList(this.getPoddSesameManager().versionContexts(ontologyID)));
                }
                if(includeInferred)
                {
                    contextSet.addAll(Arrays.asList(this.getPoddSesameManager().inferredContexts(ontologyID)));
                }
                if(includeSchema)
                {
                    contextSet.addAll(Arrays.asList(this.getPoddSesameManager().schemaContexts(ontologyID, conn,
                            this.getPoddRepositoryManager().getSchemaManagementGraph())));
                }
                // TODO: Support cross-artifact queries if they all import the
                // same schemas
                final URI[] contexts = contextSet.toArray(new URI[0]);
                // MUST not perform queries on all contexts
                if(this.getPoddRepositoryManager().safeContexts(contexts))
                {
                    final GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, sparqlQuery);
                    
                    final DatasetImpl dataset = new DatasetImpl();
                    
                    for(final URI nextUri : contexts)
                    {
                        dataset.addDefaultGraph(nextUri);
                        dataset.addNamedGraph(nextUri);
                    }
                    
                    query.setDataset(dataset);
                    
                    query.evaluate(new StatementCollector(results));
                }
                else
                {
                    this.log.error(
                            "Could not determine contexts for artifact, or included an unsafe context: ontology=<{}> contexts=<{}>",
                            ontologyID, contextSet);
                }
            }
            catch(final UnmanagedSchemaIRIException e)
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "Could not find a requested schema ontology", e);
            }
            catch(final UnmanagedArtifactIRIException e)
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find a requested artifact", e);
            }
            catch(final UnmanagedArtifactVersionException e)
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find a requested artifact", e);
            }
            catch(final OpenRDFException e)
            {
                // TODO: May want to ignore this for stability of queries in the
                // long term
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Repository exception occurred", e);
            }
            finally
            {
                if(conn != null)
                {
                    try
                    {
                        conn.close();
                    }
                    catch(final RepositoryException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        this.log.error("Could not close repository connection: ", e);
                    }
                }
            }
            
        }
        
        // container for results
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        
        final RDFFormat resultFormat =
                Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
        // - prepare response
        try
        {
            Rio.write(results, output, resultFormat);
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Error while preparing response", e);
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(resultFormat.getDefaultMIMEType()));
    }
}
