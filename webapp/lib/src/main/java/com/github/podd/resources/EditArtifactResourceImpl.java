/**
 * 
 */
package com.github.podd.resources;

import java.util.Collections;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddArtifactManager;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;
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
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * View the edit artifact page in HTML
     */
    @Get("html")
    public Representation getEditArtifactHtml(final Representation entity) throws ResourceException
    {
        this.log.info("getEditArtifactHtml");
        
        // the artifact in which editing is requested
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
        if(artifactUri == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact ID not submitted");
        }
        
        // Podd object to be edited. NULL indicates top object is to be edited.
        final String objectUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_OBJECT_IDENTIFIER);
        
        this.log.info("requesting to edit artifact (HTML): {}, {}", artifactUri, objectUri);
        
        this.checkAuthentication(PoddAction.ARTIFACT_EDIT,
                Collections.singleton(PoddRdfConstants.VALUE_FACTORY.createURI(artifactUri)));
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        // validate artifact exists
        InferredOWLOntologyID ontologyID;
        try
        {
            final PoddArtifactManager artifactManager =
                    ((PoddWebServiceApplication)this.getApplication()).getPoddArtifactManager();
            ontologyID = artifactManager.getArtifactByIRI(IRI.create(artifactUri));
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact", e);
        }
        
        final Map<String, Object> dataModel = this.populateDataModelForGet(ontologyID, objectUri);
        
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    /**
     * Internal method to populate the Freemarker Data Model for Get request
     * 
     * @param ontologyID
     *            The Artifact to be edited
     * @param objectToEdit
     *            The specific PODD object to edit.
     * @return The populated data model
     */
    private Map<String, Object> populateDataModelForGet(final InferredOWLOntologyID ontologyID,
            final String objectToEdit)
    {
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "editObject.html.ftl");
        dataModel.put("pageTitle", "Edit Artifact");
        try
        {
            // TODO
            dataModel.put("initialized", true);
            
        }
        catch(final Exception e) // should be OpenRDFException
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to populate data model");
        }
        
        return dataModel;
    }
    
}
