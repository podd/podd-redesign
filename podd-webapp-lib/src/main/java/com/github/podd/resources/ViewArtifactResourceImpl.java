/**
 * 
 */
package com.github.podd.resources;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;
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
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * FIXME: incomplete class
 * 
 * View a PODD artifact in HTML format.
 * This Resource could potentially be merged with GetArtifactResourceImpl.java
 * 
 * @author kutila
 * 
 */
public class ViewArtifactResourceImpl extends AbstractPoddResourceImpl
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Get
    public Representation viewArtifactPageHtml(final Representation entity) throws ResourceException
    {
        this.log.info("viewArtifactHtml");

        String artifactId =this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);

        if (artifactId == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact ID not submitted");
        }
        this.log.info("requesting to view artifact: {}", artifactId);
        
        URI artifactUri = PoddRdfConstants.VALUE_FACTORY.createURI(artifactId);
        this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ, Collections.<URI>singleton(artifactUri));
        // completed checking authorization
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
//        dataModel.put("contentTemplate", "index.html.ftl");
        dataModel.put("contentTemplate", "objectDetails.html.ftl");
        dataModel.put("pageTitle", "View Artifact");
        
        final Map<String, Object> artifactDataMap = this.getRequestedArtifact(artifactUri);
        dataModel.put("poddObject", artifactDataMap);
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    //FIXME: populate the data model with appropriate info as required by objectDetails.html.ftl
    private Map<String, Object> getRequestedArtifact(URI artifactUri)
    {
        final PoddArtifactManager artifactManager =
                ((PoddWebServiceApplication)this.getApplication()).getPoddArtifactManager();
        
//        InferredOWLOntologyID ontologyID = artifactManager.getArtifactByIRI(IRI.create(artifactUri));
        
        
        final Map<String, Object> objectDetailsMap = new HashMap<String, Object>();
        objectDetailsMap.put("TODO: ", "Implement GetArtifactResourceImpl");
        
        objectDetailsMap.put("pid", artifactUri.stringValue());
        objectDetailsMap.put("", "");
        objectDetailsMap.put("", "");
        objectDetailsMap.put("", "");
        objectDetailsMap.put("", "");
        objectDetailsMap.put("", "");
        objectDetailsMap.put("", "");
        objectDetailsMap.put("", "");
        
        
        final Map<String, String> roleMap = new HashMap<String, String>();
        roleMap.put("description", "A dummy user account for testing");
        objectDetailsMap.put("repositoryRole", roleMap);
            
        return objectDetailsMap;
    }
    
}
