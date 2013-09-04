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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.exception.PoddException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.exception.UnmanagedSchemaOntologyIDException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.FreemarkerUtil;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * Get a schema ontology from PODD.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class GetSchemaResourceImpl extends AbstractPoddResourceImpl
{
    
    @Get(":rdf|rj|json|ttl")
    public Representation getSchemaRdf(final Variant variant) throws ResourceException
    {
        this.log.info("getSchemaRdf");
        
        try
        {
            String schemaString = this.getQuery().getFirstValue(PoddWebConstants.KEY_SCHEMA_URI, true);
            
            final String versionString = this.getQuery().getFirstValue(PoddWebConstants.KEY_SCHEMA_VERSION_URI, true);
            
            final String schemaPath = (String)this.getRequest().getAttributes().get(PoddWebConstants.KEY_SCHEMA_PATH);
            
            // If they didn't specify anything in query parameters, look at the path for a backup
            if(schemaString == null && versionString == null)
            {
                if(schemaPath != null)
                {
                    schemaString = PoddWebConstants.PURL_ORG_PODD_NS + schemaPath;
                }
                
                if(schemaString == null)
                {
                    this.log.error("Schema URI not submitted");
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Schema URI not submitted");
                }
            }
            
            this.log.info("requesting get schema ({}): {} {} {}", variant.getMediaType().getName(), schemaString,
                    versionString, schemaPath);
            
            // TODO: Is authentication required for resolving schemas?
            // this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ,
            // PoddRdfConstants.VF.createURI(schemaString));
            // completed checking authorization
            
            // final User user = this.getRequest().getClientInfo().getUser();
            // this.log.info("authenticated user: {}", user);
            
            OWLOntologyID ontologyID = null;
            
            if(versionString == null)
            {
                ontologyID = this.getPoddSchemaManager().getCurrentSchemaOntologyVersion(IRI.create(schemaString));
            }
            else if(schemaString == null)
            {
                ontologyID = this.getPoddSchemaManager().getSchemaOntologyVersion(IRI.create(versionString));
            }
            else
            {
                // Fetch the exact ontology
                ontologyID =
                        this.getPoddSchemaManager().getSchemaOntologyID(
                                new OWLOntologyID(IRI.create(schemaString), IRI.create(versionString)));
            }
            
            if(ontologyID == null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given schema or version");
            }
            
            final String includeInferredString =
                    this.getRequest().getResourceRef().getQueryAsForm()
                            .getFirstValue(PoddWebConstants.KEY_INCLUDE_INFERRED, true);
            final boolean includeInferred = Boolean.valueOf(includeInferredString);
            
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            this.getPoddSchemaManager()
                    .downloadSchemaOntology(ontologyID, stream,
                            Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML),
                            includeInferred);
            return new ByteArrayRepresentation(stream.toByteArray());
        }
        catch(final UnmanagedSchemaIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given schema or version", e);
        }
        catch(final UnmanagedSchemaOntologyIDException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                    "Could not find the given schema and version combination", e);
        }
        catch(OpenRDFException | PoddException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to export schema", e);
        }
    }
    
    /**
     * This method retrieves necessary info about the object being viewed via SPARQL queries and
     * populates the data model.
     * 
     * @param ontologyID
     *            The artifact to be viewed
     * @param objectToView
     *            An optional internal object to view
     * @param ontologyGraphs
     *            The schema ontology graphs that should be part of the context for SPARQL
     * @param dataModel
     *            Freemarker data model to be populated
     * @throws OpenRDFException
     */
    private void populateDataModelWithArtifactData(final InferredOWLOntologyID ontologyID, final String objectToView,
            final Map<String, Object> dataModel) throws OpenRDFException
    {
        
        PoddObjectLabel theObject = null;
        
        if(objectToView != null && !objectToView.trim().isEmpty())
        {
            theObject =
                    this.getPoddArtifactManager().getObjectLabel(ontologyID,
                            PoddRdfConstants.VF.createURI(objectToView));
        }
        else
        {
            // find and set top-object of this artifact as the object to display
            final List<PoddObjectLabel> topObjectLabels =
                    this.getPoddArtifactManager().getTopObjectLabels(Arrays.asList(ontologyID));
            if(topObjectLabels == null || topObjectLabels.size() != 1)
            {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "There should be only 1 top object");
            }
            
            theObject = topObjectLabels.get(0);
        }
        // set title & description of object to display
        dataModel.put("poddObject", theObject);
        
        final URI objectUri = theObject.getObjectURI();
        
        this.populateParentDetails(ontologyID, objectUri, dataModel);
        
        // find the object's type
        final List<PoddObjectLabel> objectTypes = this.getPoddArtifactManager().getObjectTypes(ontologyID, objectUri);
        if(objectTypes == null || objectTypes.isEmpty())
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not determine type of object");
        }
        
        // Get label for the object type
        final PoddObjectLabel label = objectTypes.get(0);
        dataModel.put("objectType", label);
        
        // populate the properties of the object
        final List<URI> orderedProperties =
                this.getPoddArtifactManager().getOrderedProperties(ontologyID, objectUri, false);
        
        final Model allNeededStatementsForDisplay =
                this.getPoddArtifactManager().getObjectDetailsForDisplay(ontologyID, objectUri);
        
        dataModel.put("artifactUri", ontologyID.getOntologyIRI().toOpenRDFURI());
        dataModel.put("propertyList", orderedProperties);
        dataModel.put("completeModel", allNeededStatementsForDisplay);
        
        if(PoddRdfConstants.PODD_SCIENCE_PROJECT.equals(label.getObjectURI()))
        {
            dataModel.put("isProject", true);
        }
        
        // FIXME: determine based on project status and user authorization
        dataModel.put("canEditObject", true);
        
        // FIXME: should be set based on the current object and user authorization
        dataModel.put("canAddChildren", true);
        
        dataModel.put("selectedObjectCount", 0);
        dataModel.put("childHierarchyList", Collections.emptyList());
        
        dataModel.put("util", new FreemarkerUtil());
    }
    
    /**
     * Populate the data model with info about the parent of the current object. If the given object
     * does not have a parent (i.e. is a Top Object) the data model remains unchanged.
     * 
     * TODO: This method uses multiple API methods resulting in several SPARQL queries. Efficiency
     * could be improved by either adding a new API method or modifying getParentDetails() to supply
     * most of the required information.
     * 
     * @param ontologyID
     * @param objectUri
     *            The object whose parent details are required
     * @param dataModel
     * @throws OpenRDFException
     */
    private void populateParentDetails(final InferredOWLOntologyID ontologyID, final URI objectUri,
            final Map<String, Object> dataModel) throws OpenRDFException
    {
        
        final Model parentDetails = this.getPoddArtifactManager().getParentDetails(ontologyID, objectUri);
        if(parentDetails.size() == 1)
        {
            final Statement statement = parentDetails.iterator().next();
            
            final Map<String, String> parentMap = new HashMap<>();
            
            final String parentUriString = statement.getSubject().stringValue();
            parentMap.put("uri", parentUriString);
            
            final URI parentUri = PoddRdfConstants.VF.createURI(parentUriString);
            final URI parentPredicateUri = PoddRdfConstants.VF.createURI(statement.getPredicate().stringValue());
            
            // - parent's Title
            String parentLabel = "Missing Title";
            final PoddObjectLabel objectLabel = this.getPoddArtifactManager().getObjectLabel(ontologyID, parentUri);
            if(objectLabel != null)
            {
                parentLabel = objectLabel.getLabel();
            }
            parentMap.put("label", parentLabel);
            
            // - parent relationship Label
            String predicateLabel = "Missing parent relationship";
            final PoddObjectLabel predicateLabelModel =
                    this.getPoddArtifactManager().getObjectLabel(ontologyID, parentPredicateUri);
            if(predicateLabelModel != null)
            {
                predicateLabel = predicateLabelModel.getLabel();
            }
            parentMap.put("relationship", predicateLabel);
            
            // - parent's Type
            String parentType = "Unknown Type";
            final List<PoddObjectLabel> objectTypes =
                    this.getPoddArtifactManager().getObjectTypes(ontologyID, parentUri);
            if(objectTypes.size() > 0)
            {
                parentType = objectTypes.get(0).getLabel();
            }
            parentMap.put("type", parentType);
            
            dataModel.put("parentObject", parentMap);
        }
    }
    
}
