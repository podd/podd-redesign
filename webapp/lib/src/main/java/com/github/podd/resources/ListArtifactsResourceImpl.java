/**
 * 
 */
package com.github.podd.resources;

import java.util.ArrayList;
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
import com.github.podd.utils.PoddArtifact;
import com.github.podd.utils.PoddWebConstants;
import com.github.podd.utils.SparqlQueryHelper;

/**
 * 
 * Resource which allows listing artifacts in PODD.
 * 
 * This is only a simple implementation which lists all project URIs
 * TODO: list based on authorization, group projects. list project title, description, PI and lead institution
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
    public Representation getlistArtifactsPage(final Representation entity) throws ResourceException
    {
        this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ, Collections.<URI> emptySet());
        
        this.log.info("@Get listArtifacts Page");
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "projects.html.ftl");
        dataModel.put("pageTitle", ListArtifactsResourceImpl.LIST_PAGE_TITLE_TEXT);
        
        try
        {
            this.populateDataModelWithArtifactLists(dataModel);
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to populate data model");
        }
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    private void populateDataModelWithArtifactLists(final Map<String, Object> dataModel) throws OpenRDFException
    {
        final RepositoryConnection conn =
                this.getPoddApplication().getPoddRepositoryManager().getRepository().getConnection();
        conn.begin();
        
        try
        {
            final URI artifactMgtGraph =
                    this.getPoddApplication().getPoddRepositoryManager().getArtifactManagementGraph();
            final List<URI> uris = SparqlQueryHelper.getPoddArtifactList(conn, artifactMgtGraph);
            
            final List<PoddArtifact> artifacts = new ArrayList<PoddArtifact>();
            for(final URI artifactUri : uris)
            {
                final PoddArtifact artifact = new PoddArtifact(artifactUri);
                artifact.setTitle("The title " + artifactUri);
                artifact.setDescription("The Project is really exciting. It could lead to unbelievable productivity"
                        + " in agriculture");
                artifacts.add(artifact);
            }
            dataModel.put("allProjectsList", artifacts);
            
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
    
}
