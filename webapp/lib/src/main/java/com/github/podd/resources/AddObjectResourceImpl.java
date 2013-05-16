package com.github.podd.resources;

import java.util.Collections;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddObjectLabelImpl;
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
        
        final PoddObjectLabel objectTypeLabel = this.getObjectTypeLabel(artifactUri, objectType);
        final String title = "Add new " + objectTypeLabel.getLabel();
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "add_object.html.ftl");
        dataModel.put("pageTitle", title);
        dataModel.put("title", title);
        dataModel.put("objectType", objectTypeLabel);
        
        if(artifactUri != null)
        {
            dataModel.put("artifactUri", artifactUri);
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
    {
        PoddObjectLabel objectLabel;
        try
        {
            final RepositoryConnection conn = this.getPoddRepositoryManager().getRepository().getConnection();
            conn.begin();
            try
            {
                InferredOWLOntologyID ontologyID;
                if(artifactUri == null)
                {
                    ontologyID =
                            this.getPoddSchemaManager().getCurrentSchemaOntologyVersion(
                                    IRI.create(PoddRdfConstants.PODD_SCIENCE.replace("#", "")));
                }
                else
                {
                    ontologyID = this.getPoddArtifactManager().getArtifactByIRI(IRI.create(artifactUri));
                }
                objectLabel =
                        this.getPoddSesameManager().getObjectLabel(ontologyID,
                                PoddRdfConstants.VALUE_FACTORY.createURI(objectType), conn);
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
        catch(UnmanagedArtifactIRIException | UnmanagedSchemaIRIException | OpenRDFException e)
        {
            e.printStackTrace();
            // failed to find Label
            final URI objectTypeUri = PoddRdfConstants.VALUE_FACTORY.createURI(objectType);
            objectLabel = new PoddObjectLabelImpl(null, objectTypeUri, objectType);
        }
        return objectLabel;
    }
    
}
