/**
 * 
 */
package com.github.podd.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddObjectLabelImpl;
import com.github.podd.utils.PoddWebConstants;
import com.github.podd.utils.SparqlQueryHelper;

/**
 * 
 * Resource which allows listing artifacts in PODD.
 * 
 * This is only a simple implementation which lists all project URIs TODO: list based on
 * authorization, group projects. list project title, description, PI and lead institution
 * 
 * @author kutila
 * 
 */
public class ListArtifactsResourceImpl extends AbstractPoddResourceImpl
{
    
    private static final String LIST_PAGE_TITLE_TEXT = "PODD Project Listing";
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Handle http GET request to serve the list artifacts page.
     */
    @Get("html")
    public Representation getListArtifactsPage(final Representation entity) throws ResourceException
    {
        this.log.info("@Get listArtifacts Page");
        
        Collection<InferredOWLOntologyID> artifacts = getArtifactsInternal();
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "projects.html.ftl");
        dataModel.put("pageTitle", ListArtifactsResourceImpl.LIST_PAGE_TITLE_TEXT);
        
        this.populateDataModelWithArtifactLists(dataModel, artifacts);
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    private Collection<InferredOWLOntologyID> getArtifactsInternal() throws ResourceException
    {
        Collection<InferredOWLOntologyID> results = new ArrayList<InferredOWLOntologyID>();
        
        final String publishedString = this.getQuery().getFirstValue(PoddWebConstants.KEY_PUBLISHED);
        final String unpublishedString = this.getQuery().getFirstValue(PoddWebConstants.KEY_UNPUBLISHED);
        
        // default to both published and unpublished to start with
        boolean published = true;
        boolean unpublished = true;
        
        if(publishedString != null)
        {
            published = Boolean.parseBoolean(publishedString);
        }
        
        if(unpublishedString != null)
        {
            unpublished = Boolean.parseBoolean(unpublishedString);
        }
        
        // If they are not authenticated always set unpublished to false to avoid listing
        // unpublished artifacts to them, even if the public has access to specific unpublished
        // artifacts using direct links
        if(!this.getClientInfo().isAuthenticated())
        {
            unpublished = false;
        }
        
        try
        {
            if(published)
            {
                final Collection<InferredOWLOntologyID> publishedArtifacts =
                        this.getPoddApplication().getPoddArtifactManager().listPublishedArtifacts();
                
                for(final InferredOWLOntologyID nextPublishedArtifact : publishedArtifacts)
                {
                    if(this.checkAuthentication(PoddAction.PUBLISHED_ARTIFACT_READ,
                            Arrays.asList(nextPublishedArtifact.getOntologyIRI().toOpenRDFURI()), false))
                    {
                        // If the authentication succeeded add the artifact
                        results.add(nextPublishedArtifact);
                    }
                }
            }
            
            if(unpublished)
            {
                final Collection<InferredOWLOntologyID> unpublishedArtifacts =
                        this.getPoddApplication().getPoddArtifactManager().listUnpublishedArtifacts();
                
                for(final InferredOWLOntologyID nextUnpublishedArtifact : unpublishedArtifacts)
                {
                    if(this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ,
                            Arrays.asList(nextUnpublishedArtifact.getOntologyIRI().toOpenRDFURI()), false))
                    {
                        // If the authentication succeeded add the artifact
                        results.add(nextUnpublishedArtifact);
                    }
                }
            }
        }
        catch(OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Database exception", e);
        }
        
        return results;
    }
    
    @Get("rdf|rj|ttl")
    public Representation getListArtifactsRdf(final Representation entity) throws ResourceException
    {
        Collection<InferredOWLOntologyID> artifactsInternal = getArtifactsInternal();
        
        throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED, "TODO: Implement listing of artifacts");
    }
    
    private void populateDataModelWithArtifactLists(final Map<String, Object> dataModel,
            Collection<InferredOWLOntologyID> artifacts)
    {
        final List<PoddObjectLabel> results = new ArrayList<PoddObjectLabel>();
        for(final InferredOWLOntologyID artifactUri : artifacts)
        {
            final PoddObjectLabel artifact = new PoddObjectLabelImpl(artifactUri);
            artifact.setTitle("The title " + artifactUri);
            artifact.setDescription("The Project is really exciting. It could lead to unbelievable productivity"
                    + " in agriculture");
            results.add(artifact);
        }
        dataModel.put("allProjectsList", artifacts);
        
    }
    
}
