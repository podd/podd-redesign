/**
 * 
 */
package com.github.podd.resources;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * TODO: Empty class with logic not implemented
 * 
 * Get an artifact from PODD. This resource handles requests for
 * asserted statements as well as inferred statements.
 * 
 * @author kutila
 * 
 */
public class GetArtifactResourceImpl extends AbstractPoddResourceImpl
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Get
    public Representation getArtifactPageHtml(final Representation entity) throws ResourceException
    {
        //TODO: set requested artifact URI
        Collection<URI> objectUris = Collections.<URI>emptySet();
        this.checkAuthentication(PoddAction.ARTIFACT_CREATE, objectUris);
        
        this.log.info("getArtifactHtml");
        final User user = this.getRequest().getClientInfo().getUser();
        
        this.log.info("authenticated user: {}", user);
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "index.html.ftl");
        dataModel.put("pageTitle", "TODO: Display Artifact");
        
        final Map<String, Object> artifactDataMap = this.getRequestedArtifact();
        dataModel.put("requestedArtifact", artifactDataMap);
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    // FIXME: populating dummy info for test
    private Map<String, Object> getRequestedArtifact()
    {
        final Map<String, Object> testArtifactMap = new HashMap<String, Object>();
        testArtifactMap.put("TODO: ", "Implement GetArtifactResourceImpl");
        
        final Map<String, String> roleMap = new HashMap<String, String>();
        roleMap.put("description", "A dummy user account for testing");
        testArtifactMap.put("repositoryRole", roleMap);
            
        return testArtifactMap;
    }
    
}
