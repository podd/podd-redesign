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

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.exception.SchemaManifestException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedArtifactVersionException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddObjectLabelImpl;
import com.github.podd.utils.PoddWebConstants;

/**
 * Resource to create new PODD object.
 * 
 * @author kutila
 */
public class AddObjectResourceImpl extends AbstractPoddResourceImpl
{
    /**
     * Build a PODD object using the incoming RDF
     */
    @Post(":rdf|rj|ttl")
    public Representation createObjectRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        this.log.warn("Not implemented! POST with RDF data to UploadArtifactResource for new Projects and EditArtifactResource for others");
        return null;
    }
    
    /**
     * Serve the "Add new object" HTML page
     * 
     * @throws UnmanagedSchemaIRIException
     * @throws IOException
     * @throws UnsupportedRDFormatException
     */
    @Get("html")
    public Representation getCreateObjectHtml(final Representation entity) throws ResourceException,
        UnmanagedSchemaIRIException, UnsupportedRDFormatException, IOException
    {
        this.log.info("@Get addObjectHtml Page");
        
        // - check mandatory parameter: Object Type
        final String objectType = this.getQuery().getFirstValue(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, true);
        if(objectType == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Type of Object to create not specified");
        }
        
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
        final String parentUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_PARENT_IDENTIFIER, true);
        final String parentPredicateUri =
                this.getQuery().getFirstValue(PoddWebConstants.KEY_PARENT_PREDICATE_IDENTIFIER, true);
        
        if(artifactUri == null)
        {
            // looks like adding a new Artifact (ie, a new Project)
            this.checkAuthentication(PoddAction.ARTIFACT_CREATE);
        }
        else
        {
            this.checkAuthentication(PoddAction.ARTIFACT_EDIT, PODD.VF.createURI(artifactUri));
        }
        
        final PoddObjectLabel objectTypeLabel = this.getObjectTypeLabel(artifactUri, objectType);
        final String title = "Add new " + objectTypeLabel.getLabel();
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "modify_object.html.ftl");
        dataModel.put("pageTitle", title);
        dataModel.put("title", title);
        dataModel.put("objectType", objectTypeLabel);
        // objectUri is unavailable as this is a new object
        
        if(artifactUri != null)
        {
            // adding a child object to an existing artifact
            
            if(parentUri == null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "parent URI not specified");
            }
            
            if(parentPredicateUri == null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "parent predicate URI not specified");
            }
            
            InferredOWLOntologyID ontologyID;
            try
            {
                ontologyID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
            }
            catch(final UnmanagedArtifactIRIException e)
            {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact", e);
            }
            
            dataModel.put("artifactIri", ontologyID.getOntologyIRI().toString());
            dataModel.put("versionIri", ontologyID.getVersionIRI().toString());
            
            // parentUri and parentPredicate - is any validation required?
            dataModel.put("parentUri", parentUri);
            dataModel.put("parentPredicateUri", parentPredicateUri);
        }
        
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    /*
     * Internal helper method which encapsulates the creation of a RepositoryConnection before
     * calling the SesameManager.
     * 
     * Can avoid dealing with RepositoryConnections here if this could be moved to somewhere in the
     * API.
     */
    private PoddObjectLabel getObjectTypeLabel(final String artifactUri, final String objectType)
        throws UnsupportedRDFormatException, IOException
    {
        PoddObjectLabel objectLabel;
        try
        {
            InferredOWLOntologyID ontologyID;
            if(artifactUri == null)
            {
                // FIXME: Why is there a hack here???
                ontologyID =
                        this.getPoddSchemaManager().getCurrentSchemaOntologyVersion(
                                IRI.create(PODD.PODD_SCIENCE.replace("#", "")));
            }
            else
            {
                ontologyID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
            }
            
            final Set<? extends OWLOntologyID> schemaImports =
                    this.getPoddArtifactManager().getSchemaImports(ontologyID);
            RepositoryConnection conn = null;
            try
            {
                conn = this.getPoddRepositoryManager().getPermanentRepository(schemaImports).getConnection();
                objectLabel =
                        this.getPoddSesameManager().getObjectLabel(ontologyID, PODD.VF.createURI(objectType), conn,
                                this.getPoddRepositoryManager().getSchemaManagementGraph(),
                                this.getPoddRepositoryManager().getArtifactManagementGraph());
            }
            finally
            {
                if(conn != null)
                {
                    conn.rollback(); // read only, nothing to commit
                    conn.close();
                }
            }
        }
        catch(UnmanagedArtifactIRIException | UnmanagedSchemaIRIException | OpenRDFException
                | UnmanagedArtifactVersionException | SchemaManifestException e)
        {
            e.printStackTrace();
            // failed to find Label
            final URI objectTypeUri = PODD.VF.createURI(objectType);
            objectLabel = new PoddObjectLabelImpl(null, objectTypeUri, objectType);
        }
        return objectLabel;
    }
    
}
