/**
 * 
 */
package com.github.podd.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

        this.populateDataModelWithArtifactData(artifactUri, dataModel);
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    /**
     * FIXME: populate the data model with appropriate info as required by objectDetails.html.ftl
     * 
     *  1. get artifact via the PODD API (as an OWLOntology object?)
     *  2. populate Map with required info to go into data model
     * 
     * 
     * @param artifactUri
     * @param dataModel
     */
    private void populateDataModelWithArtifactData(URI artifactUri, Map<String, Object> dataModel)
    {
        final PoddArtifactManager artifactManager =
                ((PoddWebServiceApplication)this.getApplication()).getPoddArtifactManager();
        
//        InferredOWLOntologyID ontologyID = artifactManager.getArtifactByIRI(IRI.create(artifactUri));
        
        // hard-code the required values first to display a valid html page
        //DEBUG
        dataModel.put("forbidden", false);
        dataModel.put("canEditObject", false);
        dataModel.put("pid", artifactUri.stringValue());
        dataModel.put("objectType", "artifact");
        dataModel.put("creationDate", "2013-01-01");
        dataModel.put("modifiedDate", "2013-01-31");

        
//        dataModel.put("elementList", Arrays.asList("element1", "element2")); - TODO
        
        final Map<String, Object> poddObject = new HashMap<String, Object>();
        poddObject.put("pid", artifactUri.stringValue());
        poddObject.put("localName", "Hardcoded project title");
        poddObject.put("label", "Dummy project from the resource");

        // poddObject.creator
        final Map<String, String> creator = new HashMap<String, String>();
        poddObject.put("creator", creator);
        creator.put("firstName", "Alice");
        creator.put("lastName", "Land");

        // poddObject.lastModifier
        final Map<String, String> lastModifier = new HashMap<String, String>();
        poddObject.put("lastModifier", lastModifier);
        lastModifier.put("firstName", "Bob");
        lastModifier.put("lastName", "Colt");
        
//        final Map<String, String> roleMap = new HashMap<String, String>();
//        roleMap.put("description", "A dummy user account for testing");
//        objectDetailsMap.put("repositoryRole", roleMap);
            
        dataModel.put("poddObject", poddObject);
        
        
        final List<Object> refersToList = new ArrayList<Object>();
        
        final Map<String, Object> refersToElement = new HashMap<String, Object>();
        refersToElement.put("label", "Refers To Label");
        refersToElement.put("propertyUriWithoutNamespace", "artifact89");
        refersToElement.put("availableObjects", this.getAvailableObjects());
        refersToElement.put("areSelectedObjects", true);
        
        refersToList.add(refersToElement);
        
        dataModel.put("refersToList", refersToList);        
    }

    private List<Object> getAvailableObjects()
    {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < 4; i++)
        {
            final Map<String, Object> anObject = new HashMap<String, Object>();
            anObject.put("isSelected", true);
            anObject.put("state", "A");
            anObject.put("type", "IntrnalObject");
            anObject.put("pid", "object:34343");
            anObject.put("title", "Object " + i);
            anObject.put("description", "This is a simple object within an artifact");
            
            list.add(anObject);
        }
        
        return list;
    }
    
}
