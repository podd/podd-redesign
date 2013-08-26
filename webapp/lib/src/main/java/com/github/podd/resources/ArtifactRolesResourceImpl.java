/**
 * 
 */
package com.github.podd.resources;

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

import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * @author kutila
 */
public class ArtifactRolesResourceImpl extends AbstractPoddResourceImpl
{
 
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * View the Edit Project Participants page in HTML
     */
    @Get("html")
    public Representation getEditArtifactHtml(final Representation entity) throws ResourceException
    {
        this.log.info("getArtifactRolesHtml");
        
        // the artifact in which editing is requested
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
        if(artifactUri == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact ID not submitted");
        }
        
        this.log.info("requesting to edit Artifact Participants (HTML): {}", artifactUri);
        
        this.checkAuthentication(PoddAction.PROJECT_ROLE_EDIT, PoddRdfConstants.VF.createURI(artifactUri));
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
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
        
        dataModel.put("contentTemplate", "projectParticipants.html.ftl");
        dataModel.put("pageTitle", "Edit Project Participants");
        
        try
        {
            // FIXME: get current project roles and populate them in here
            dataModel.put("pi", "Prof P Investigator");
            dataModel.put("admin", "Dr ECA Admin");
            
        }
        catch(final Exception e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to populate data model");
        }
        
        dataModel.put("artifactIri", ontologyID.getOntologyIRI().toString());
        dataModel.put("versionIri", ontologyID.getVersionIRI().toString());
        
        // Defaults to false. Set to true if multiple objects are being edited concurrently
        // TODO: investigate how to use this
        dataModel.put("initialized", false);
        dataModel.put("stopRefreshKey", "Stop Refresh Key");
        
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }

}
