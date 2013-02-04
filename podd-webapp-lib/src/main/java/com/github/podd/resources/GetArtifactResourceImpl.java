/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.exception.PoddException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * Get an artifact from PODD. This resource handles requests for asserted statements as well as
 * inferred statements.
 * 
 * @author kutila
 * 
 */
public class GetArtifactResourceImpl extends AbstractPoddResourceImpl
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Get("html")
    public Representation getArtifactHtml(final Representation entity) throws ResourceException
    {
        this.log.info("getArtifactHtml");
        
        String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
        
        if(artifactUri == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact ID not submitted");
        }
        this.log.info("requesting get artifact (HTML): {}", artifactUri);
        
        this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ, 
                Collections.<URI> singleton(PoddRdfConstants.VALUE_FACTORY.createURI(artifactUri)));
        // completed checking authorization
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        // dataModel.put("contentTemplate", "index.html.ftl");
        dataModel.put("contentTemplate", "objectDetails.html.ftl");
        dataModel.put("pageTitle", "View Artifact");
        
        this.populateDataModelWithArtifactData(artifactUri, dataModel);
        
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    @Get("rdf|rj|ttl")
    public Representation getArtifactRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        
        try
        {
            String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
            
            if(artifactUri == null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact ID not submitted");
            }
            
            this.log.info("requesting get artifact (HTML): {}", artifactUri);
            
            this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ, 
                    Collections.<URI> singleton(PoddRdfConstants.VALUE_FACTORY.createURI(artifactUri)));
            // completed checking authorization

            final User user = this.getRequest().getClientInfo().getUser();
            this.log.info("authenticated user: {}", user);
            
            
            InferredOWLOntologyID ontologyID =
                    this.getPoddApplication().getPoddArtifactManager().getArtifactByIRI(IRI.create(artifactUri));
            
            // TODO: support prototype method for this
            String includeInferredString =
                    this.getRequest().getResourceRef().getQueryAsForm().getFirstValue("includeInferred", true);
            boolean includeInferred = Boolean.valueOf(includeInferredString);
            
            this.getPoddApplication()
                    .getPoddArtifactManager()
                    .exportArtifact(ontologyID, stream,
                            RDFFormat.forMIMEType(variant.getMediaType().getName(), RDFFormat.TURTLE), includeInferred);
        }
        catch(UnmanagedArtifactIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact", e);
        }
        catch(OpenRDFException | PoddException | IOException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to export artifact", e);
        }
        
        return new ByteArrayRepresentation(stream.toByteArray());
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
    private void populateDataModelWithArtifactData(String artifactUri, Map<String, Object> dataModel)
    {
//        final PoddArtifactManager artifactManager =
//                ((PoddWebServiceApplication)this.getApplication()).getPoddArtifactManager();
//        InferredOWLOntologyID ontologyID = artifactManager.getArtifactByIRI(IRI.create(artifactUri));
        
        // hard-code the required values first to display a valid html page
        //DEBUG
        dataModel.put("forbidden", false);
        dataModel.put("canEditObject", false);
        dataModel.put("pid", artifactUri);
        dataModel.put("objectType", "artifact");
        dataModel.put("creationDate", "2013-01-01");
        dataModel.put("modifiedDate", "2013-01-31");

        
//        dataModel.put("elementList", Arrays.asList("element1", "element2")); - TODO
        
        final Map<String, Object> poddObject = new HashMap<String, Object>();
        poddObject.put("pid", artifactUri);
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
