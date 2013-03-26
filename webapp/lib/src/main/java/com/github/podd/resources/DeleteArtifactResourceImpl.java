/**
 * 
 */
package com.github.podd.resources;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.exception.PoddException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * TODO: Empty class with logic not implemented
 * 
 * Delete an artifact from PODD.
 * 
 * @author kutila
 * 
 */
public class DeleteArtifactResourceImpl extends AbstractPoddResourceImpl
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Get
    public Representation editArtifactPageHtml(final Representation entity) throws ResourceException
    {
        this.checkAuthentication(PoddAction.ARTIFACT_CREATE, null);
        
        this.log.info("deleteArtifactHtml");
        final User user = this.getRequest().getClientInfo().getUser();
        
        this.log.info("authenticated user: {}", user);
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "index.html.ftl");
        dataModel.put("pageTitle", "TODO: Delete Artifact");
        
        final Map<String, Object> artifactDataMap = this.getRequestedArtifact();
        dataModel.put("requestedArtifact", artifactDataMap);
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    @Delete
    public void deleteArtifact(final Representation entity) throws ResourceException
    {
        boolean result;
        try
        {
            String artifactId = this.getQueryValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
            
            if(artifactId == null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "Did not find an artifacturi parameter in the request");
            }
            InferredOWLOntologyID currentVersion =
                    this.getPoddArtifactManager().getArtifactByIRI(IRI.create(artifactId));
            
            result = this.getPoddApplication().getPoddArtifactManager().deleteArtifact(currentVersion);
            
            if(result)
            {
                this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
            }
            else
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not delete artifact");
            }
        }
        catch(PoddException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "Could not delete artifact due to an internal error", e);
        }
        
    }
    
    // FIXME: populating dummy info for test
    private Map<String, Object> getRequestedArtifact()
    {
        final Map<String, Object> testArtifactMap = new HashMap<String, Object>();
        testArtifactMap.put("TODO: ", "Implement DeleteArtifactResourceImpl");
        
        final Map<String, String> roleMap = new HashMap<String, String>();
        roleMap.put("description", "A dummy user account for testing");
        testArtifactMap.put("repositoryRole", roleMap);
        
        return testArtifactMap;
    }
    
}
