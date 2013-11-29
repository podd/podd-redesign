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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;

import com.github.podd.api.DanglingObjectPolicy;
import com.github.podd.api.DataReferenceVerificationPolicy;
import com.github.podd.api.UpdatePolicy;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedArtifactVersionException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * Edit an artifact from PODD.
 * 
 * @author kutila
 * 
 */
public class EditArtifactResourceImpl extends AbstractPoddResourceImpl
{
    /**
     * Handle an HTTP POST request submitting RDF data to update an existing artifact
     */
    @Post("rdf|rj|json|ttl")
    public Representation editArtifactToRdf(final Representation entity, final Variant variant)
        throws ResourceException
    {
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
        
        if(artifactUri == null)
        {
            this.log.error("Artifact ID not submitted");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact IRI not submitted");
        }
        
        // Once we find the artifact URI, check authentication for it
        // immediately
        this.checkAuthentication(PoddAction.ARTIFACT_EDIT, PODD.VF.createURI(artifactUri));
        
        final String versionUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, true);
        
        if(versionUri == null)
        {
            this.log.error("Artifact Version IRI not submitted");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact Version IRI not submitted");
        }
        
        // optional multiple parameter 'objectUri'
        final String[] objectURIStrings = this.getQuery().getValuesArray(PoddWebConstants.KEY_OBJECT_IDENTIFIER, true);
        
        // - optional parameter 'isreplace'
        UpdatePolicy updatePolicy = UpdatePolicy.REPLACE_EXISTING;
        final String isReplaceStr = this.getQuery().getFirstValue(PoddWebConstants.KEY_EDIT_WITH_REPLACE, true);
        if(isReplaceStr != null && (Boolean.valueOf(isReplaceStr) == false))
        {
            updatePolicy = UpdatePolicy.MERGE_WITH_EXISTING;
        }
        
        // - optional parameter 'isforce'
        DanglingObjectPolicy danglingObjectPolicy = DanglingObjectPolicy.REPORT;
        final String forceStr = this.getQuery().getFirstValue(PoddWebConstants.KEY_EDIT_WITH_FORCE, true);
        if(forceStr != null && Boolean.valueOf(forceStr))
        {
            danglingObjectPolicy = DanglingObjectPolicy.FORCE_CLEAN;
        }
        
        // - optional parameter 'verifyfilerefs'
        DataReferenceVerificationPolicy fileRefVerificationPolicy = DataReferenceVerificationPolicy.DO_NOT_VERIFY;
        final String fileRefVerifyStr =
                this.getQuery().getFirstValue(PoddWebConstants.KEY_EDIT_VERIFY_FILE_REFERENCES, true);
        if(fileRefVerifyStr != null && Boolean.valueOf(fileRefVerifyStr))
        {
            fileRefVerificationPolicy = DataReferenceVerificationPolicy.VERIFY;
        }
        
        final Collection<URI> objectUris = new ArrayList<URI>(objectURIStrings.length);
        for(final String nextObjectURIString : objectURIStrings)
        {
            objectUris.add(PODD.VF.createURI(nextObjectURIString));
        }
        
        this.log.debug("requesting edit artifact ({}): {}, {} with isReplace {}", variant.getMediaType().getName(),
                artifactUri, versionUri, updatePolicy);
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.debug("authenticated user: {}", user);
        
        // - get input stream with edited RDF content
        InputStream inputStream = null;
        try
        {
            inputStream = entity.getStream();
        }
        catch(final IOException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
        }
        final RDFFormat inputFormat = Rio.getParserFormatForMIMEType(entity.getMediaType().getName(), RDFFormat.RDFXML);
        
        // - prepare response
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        final RDFFormat outputFormat =
                Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
        // - do the artifact update
        try
        {
            final Model model =
                    this.getPoddArtifactManager().updateArtifact(PODD.VF.createURI(artifactUri),
                            PODD.VF.createURI(versionUri), objectUris, inputStream, inputFormat, updatePolicy,
                            danglingObjectPolicy, fileRefVerificationPolicy);
            // TODO - send detailed errors for display where possible
            
            // FIXME Change response format so that it does not resemble an
            // empty OWL Ontology
            // - write the artifact ID into response
            Rio.write(model, output, outputFormat);
        }
        catch(final UnmanagedArtifactVersionException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "Could not edit the given artifact", e);
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact", e);
        }
        catch(final PoddException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response", e);
        }
        catch(OpenRDFException | IOException | OWLException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat.getDefaultMIMEType()));
    }
    
    /**
     * View the edit artifact page in HTML
     */
    @Get("html")
    public Representation getEditArtifactHtml(final Representation entity) throws ResourceException
    {
        this.log.debug("getEditArtifactHtml");
        
        // the artifact in which editing is requested
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
        if(artifactUri == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact ID not submitted");
        }
        
        // Podd object to be edited. NULL indicates top object is to be edited.
        final String objectToEdit = this.getQuery().getFirstValue(PoddWebConstants.KEY_OBJECT_IDENTIFIER, true);
        
        this.log.debug("requesting to edit artifact (HTML): {}, {}", artifactUri, objectToEdit);
        
        this.checkAuthentication(PoddAction.ARTIFACT_EDIT, PODD.VF.createURI(artifactUri));
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.debug("authenticated user: {}", user);
        
        // validate artifact exists
        InferredOWLOntologyID ontologyID;
        try
        {
            ontologyID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact", e);
        }
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        
        dataModel.put("contentTemplate", "modify_object.html.ftl");
        dataModel.put("pageTitle", "Edit Artifact");
        
        try
        {
            URI objectUri = null;
            
            // objectUri
            if(objectToEdit == null)
            {
                // set the top object as the object URI
                final List<PoddObjectLabel> topObjectLabels =
                        this.getPoddArtifactManager().getTopObjectLabels(Arrays.asList(ontologyID));
                if(topObjectLabels.size() > 0)
                {
                    objectUri = topObjectLabels.get(0).getObjectURI();
                }
            }
            else
            {
                objectUri = PODD.VF.createURI(objectToEdit);
            }
            if(objectUri != null)
            {
                dataModel.put("objectUri", objectUri.toString());
            }
            
            // objectType
            final List<PoddObjectLabel> objectTypes =
                    this.getPoddArtifactManager().getObjectTypes(ontologyID, objectUri);
            if(objectTypes == null || objectTypes.isEmpty())
            {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not determine type of object");
            }
            // TODO: handle case where more than 1 type is found
            dataModel.put("objectType", objectTypes.get(0));
            
            if(objectToEdit == null)
            {
                dataModel.put("title", "Edit Project Object");
            }
            else
            {
                dataModel.put("title", "Edit " + objectTypes.get(0).getLabel() + " Object");
            }
            
            // Parent Details
            final Model parentDetails = this.getPoddArtifactManager().getParentDetails(ontologyID, objectUri);
            if(parentDetails.size() == 1)
            {
                final Statement statement = parentDetails.iterator().next();
                dataModel.put("parentUri", statement.getSubject().stringValue());
                dataModel.put("parentPredicateUri", statement.getPredicate().stringValue());
            }
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to populate data model");
        }
        
        dataModel.put("artifactIri", ontologyID.getOntologyIRI().toString());
        dataModel.put("versionIri", ontologyID.getVersionIRI().toString());
        
        // Defaults to false. Set to true if multiple objects are being edited
        // concurrently
        // TODO: investigate how to use this
        dataModel.put("initialized", false);
        dataModel.put("stopRefreshKey", "Stop Refresh Key");
        
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
}
