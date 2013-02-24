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
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
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
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObject;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;
import com.github.podd.utils.SparqlQueryHelper;

/**
 * 
 * Edit an artifact from PODD.
 * 
 * @author kutila
 * 
 */
public class EditArtifactResourceImpl extends AbstractPoddResourceImpl
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /** Constructor */
    public EditArtifactResourceImpl()
    {
        super();
    }
    
    /**
     * View the edit artifact page in HTML
     */
    @Get("html")
    public Representation getEditArtifactHtml(final Representation entity) throws ResourceException
    {
        this.log.info("getEditArtifactHtml");
        
        // the artifact in which editing is requested
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
        if(artifactUri == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact ID not submitted");
        }
        
        // Podd object to be edited. NULL indicates top object is to be edited.
        final String objectUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_OBJECT_IDENTIFIER);
        
        this.log.info("requesting to edit artifact (HTML): {}, {}", artifactUri, objectUri);
        
        this.checkAuthentication(PoddAction.ARTIFACT_EDIT,
                Collections.singleton(PoddRdfConstants.VALUE_FACTORY.createURI(artifactUri)));
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        // validate artifact exists
        InferredOWLOntologyID ontologyID;
        try
        {
            final PoddArtifactManager artifactManager =
                    ((PoddWebServiceApplication)this.getApplication()).getPoddArtifactManager();
            ontologyID = artifactManager.getArtifactByIRI(IRI.create(artifactUri));
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact", e);
        }
        
        final Map<String, Object> dataModel = this.populateDataModelForGet(ontologyID, objectUri);
        
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    /**
     * Internal method to populate the Freemarker Data Model for Get request
     * 
     * @param ontologyID
     *            The Artifact to be edited
     * @param objectToEdit
     *            The specific PODD object to edit.
     * @return The populated data model
     */
    private Map<String, Object> populateDataModelForGet(final InferredOWLOntologyID ontologyID,
            String objectToEdit)
    {
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "editObject.html.ftl");
        dataModel.put("pageTitle", "Edit Artifact");

        RepositoryConnection conn = null;
        try
        {
            conn = this.getPoddApplication().getPoddRepositoryManager().getRepository().getConnection();
            conn.begin();

            
            if (objectToEdit == null)
            {
                objectToEdit = this.getTopObject(conn, ontologyID).getUri().stringValue();
            }
            URI objectUri = ValueFactoryImpl.getInstance().createURI(objectToEdit);
            
            List<URI> contexts = new ArrayList<URI>(SparqlQueryHelper.getSchemaOntologyGraphs());
            contexts.add(ontologyID.getVersionIRI().toOpenRDFURI());
            
            PoddObject objectType = SparqlQueryHelper.getObjectType(objectUri, conn, contexts.toArray(new URI[0]));
            
            dataModel.put("objectType", objectType.getTitle());
            
            // TODO
            dataModel.put("initialized", false);
            
            /*            // *** editObject.html.ftl ***
            
            <#-- @ftlvariable name="isAdmin" type="boolean" -->
            <#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
            <#-- @ftlvariable name="title" type="java.lang.String" -->
            
            <#-- @ftlvariable name="canViewProjectParticipants" type="boolean" -->
            <#-- @ftlvariable name="initialized" type="boolean" -->
            <#-- @ftlvariable name="postUrl" type="java.lang.String" -->
            <#-- @ftlvariable name="objectPID" type="java.lang.String" -->
            <#-- @ftlvariable name="objectType" type="java.lang.String" -->
            <#-- @ftlvariable name="isProject" type="boolean" -->
            <#-- @ftlvariable name="objectName" type="java.lang.String" -->
            <#-- @ftlvariable name="objectDescription" type="java.lang.String" -->
            <#-- @ftlvariable name="elementList" type="java.util.ArrayList<podd.template.content.HTMLElementTemplate>" -->
            <#-- @ftlvariable name="refersList" type="java.util.ArrayList<podd.template.content.HTMLElementTemplate>" -->
            <#-- @ftlvariable name="fileList" type="java.util.ArrayList<podd.resources.util.view.FileElement>" -->
            <#-- @ftlvariable name="canComplete" type="boolean" -->
            <#-- @ftlvariable name="aHREF" type="java.lang.String" -->
            
            <#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
            <#-- @ftlvariable name="objectNameError" type="java.lang.String" -->
            <#-- @ftlvariable name="objectDescriptionError" type="java.lang.String" -->
            <#-- @ftlvariable name="generalErrorList" type="java.util.ArrayList<java.lang.String>" -->
            <#-- @ftlvariable name="objectErrorList" type="java.util.ArrayList<java.lang.String>" -->
             */            
            
            
            // *** attachFile.html.ftl ***
            
            
            // *** attachFile.html.ftl ***
            // stopRefreshKey - String
            // - values that don't have to be set
            //      fileDescription type="java.lang.String" -->
            //      fileErrorMessage" type="java.lang.String" -->
            //      fileDescriptionError" type="java.lang.String" -->
            //      attachedFileList" type="java.util.List<String>" -->
            dataModel.put("stopRefreshKey", "Stop Refresh Key");
            
        }
        catch(final OpenRDFException e) // should be OpenRDFException
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to populate data model");
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    // This is a Get request, therefore nothing to commit
                    conn.rollback(); 
                    conn.close();
                }
                catch (OpenRDFException e)
                {
                    this.log.error("Failed to close RepositoryConnection", e);
                    //Should we do anything other than log an error?
                }
            }
        }
        
        return dataModel;
    }
    
    protected PoddObject getTopObject(RepositoryConnection conn, InferredOWLOntologyID ontologyID) throws OpenRDFException
    {
        // get top-object of this artifact
        final List<PoddObject> topObjectList =
                SparqlQueryHelper.getTopObjects(conn, ontologyID.getVersionIRI().toOpenRDFURI(), ontologyID
                        .getInferredOntologyIRI().toOpenRDFURI());
        if(topObjectList == null || topObjectList.size() != 1)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "There should be only 1 top object");
        }
        
        return topObjectList.get(0);
    }
    
}
