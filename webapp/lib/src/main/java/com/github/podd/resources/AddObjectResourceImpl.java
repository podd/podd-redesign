package com.github.podd.resources;

import java.util.Collections;
import java.util.Map;

import org.openrdf.model.URI;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * Resource to create new PODD object.
 * 
 * @author kutila
 */
public class AddObjectResourceImpl extends AbstractPoddResourceImpl
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /** Constructor */
    public AddObjectResourceImpl()
    {
        super();
    }
    
    /**
     * Serve the "Add new object" HTML page
     */
    @Get("html")
    public Representation getCreateObjectHtml(final Representation entity) throws ResourceException
    {
        this.log.info("@Get addObjectHtml Page");
        
        // - check mandatory parameter: Object Type
        final String objectType = this.getQuery().getFirstValue(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER);
        if(objectType == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Type of Object to create not specified");
        }
        
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
        
        if(artifactUri == null)
        {
            // looks like adding a new Project
            this.checkAuthentication(PoddAction.ARTIFACT_CREATE, Collections.<URI> emptySet());
        }
        else
        {
            this.checkAuthentication(PoddAction.ARTIFACT_EDIT,
                    Collections.singleton(PoddRdfConstants.VALUE_FACTORY.createURI(artifactUri)));
        }
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "add_object.html.ftl");
        dataModel.put("pageTitle", "Add Object");
        dataModel.put("objectType", objectType);
        
        dataModel.put("title", "Add Object");
        
        if(artifactUri != null)
        {
            dataModel.put("artifactUri", artifactUri);
        }
        
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    
    /**
     * Build a PODD object using the incoming RDF
     */
    @Post(":rdf|rj|ttl")
    public Representation createObjectRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        this.log.warn("Not implemented! POST with RDF data to UploadArtifactResource for new Projects and EditArtifactResource for others");
        return null;
    }
    
}
