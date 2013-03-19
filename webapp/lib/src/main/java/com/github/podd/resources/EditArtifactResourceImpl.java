/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.exception.PoddException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.FreemarkerUtil;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

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
     * Handle an HTTP POST request submitting RDF data to update an existing artifact
     */
    @Post("rdf|rj|ttl")
    public Representation editArtifactToRdf(final Representation entity, final Variant variant)
        throws ResourceException
    {
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
        
        if(artifactUri == null)
        {
            this.log.error("Artifact ID not submitted");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact ID not submitted");
        }

        boolean isReplace = true;
        final String isReplaceStr = this.getQuery().getFirstValue(PoddWebConstants.KEY_EDIT_WITH_REPLACE);
        if (isReplaceStr != null)
        {
            isReplace = Boolean.valueOf(isReplaceStr);
        }
        
        boolean force = false;
        final String forceStr = this.getQuery().getFirstValue(PoddWebConstants.KEY_EDIT_WITH_FORCE);
        if (forceStr != null)
        {
            force = Boolean.valueOf(forceStr);
        }
        
        
        this.log.info("requesting edit artifact ({}): {}, with isReplace {}", variant.getMediaType().getName(),
                artifactUri, isReplace);
        
        this.checkAuthentication(PoddAction.ARTIFACT_EDIT,
                Collections.<URI> singleton(PoddRdfConstants.VALUE_FACTORY.createURI(artifactUri)));
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        // - get input stream with edited RDF content
        InputStream inputStream = null;
        try
        {
            inputStream = entity.getStream();
        }
        catch(IOException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
        }
        RDFFormat inputFormat = Rio.getParserFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
        
        // - prepare response
        ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        RDFWriter writer =
                Rio.createWriter(Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML),
                        output);

        // - do the artifact update 
        try
        {
            final InferredOWLOntologyID ontologyID =
                    this.getPoddArtifactManager().updateArtifact(ValueFactoryImpl.getInstance().createURI(artifactUri),
                            inputStream, inputFormat, isReplace, force);
            //TODO - send detailed errors for display where possible
            
            // - write the artifact ID into response
            writer.startRDF();
            OntologyUtils.ontologyIDsToHandler(Arrays.asList(ontologyID), writer);
            writer.endRDF();
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact", e);
        }
        catch(final PoddException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response", e);
        }
        catch(OpenRDFException | IOException | OWLException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(writer.getRDFFormat()
                .getDefaultMIMEType()));
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
            ontologyID = this.getPoddArtifactManager().getArtifactByIRI(IRI.create(artifactUri));
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
    private Map<String, Object> populateDataModelForGet(final InferredOWLOntologyID ontologyID, String objectToEdit)
    {
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "editObject.html.ftl");
        dataModel.put("pageTitle", "Edit Artifact");
        
        // add required constants and methods to data model
        dataModel.put("RDFS_LABEL", RDFS.LABEL);
        dataModel.put("RDFS_RANGE", RDFS.RANGE);
        dataModel.put("RDF_TYPE", RDF.TYPE);
        dataModel.put("OWL_OBJECT_PROPERTY", OWL.OBJECTPROPERTY);
        dataModel.put("OWL_DATA_PROPERTY", OWL.DATATYPEPROPERTY);
        dataModel.put("OWL_ANNOTATION_PROPERTY", OWL.ANNOTATIONPROPERTY);
        dataModel.put("util", new FreemarkerUtil());
        
        // Defaults to false. Set to true if multiple objects are being edited concurrently
        // TODO: investigate how to use this
        boolean initialized = false;
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.getPoddRepositoryManager().getRepository().getConnection();
            conn.begin();
            
            URI objectUri;
            
            if(objectToEdit == null)
            {
                objectUri = this.getPoddSesameManager().getTopObjectIRI(ontologyID, conn);
            }
            else
            {
                objectUri = ValueFactoryImpl.getInstance().createURI(objectToEdit);
            }
            
            List<URI> objectTypes = this.getPoddSesameManager().getObjectTypes(ontologyID, objectUri, conn);
            if(objectTypes == null || objectTypes.isEmpty())
            {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not determine type of object");
            }
            
            // Get label for the object type
            PoddObjectLabel objectType =
                    this.getPoddSesameManager().getObjectLabel(ontologyID, objectTypes.get(0), conn);
            if (objectType == null || objectType.getLabel() == null)
            {
                dataModel.put("objectType", objectTypes.get(0));
            }
            else
            {
                dataModel.put("objectType", objectType.getLabel());
            }

            PoddObjectLabel theObject = this.getPoddSesameManager().getObjectLabel(ontologyID, objectUri, conn);
            dataModel.put("poddObject", theObject);
            
            // an ordered-list of the properties about the object
            final List<URI> orderedProperties =
                    this.getPoddSesameManager().getWeightedProperties(ontologyID, objectUri, conn);
            this.log.info("Found {} properties about object {}", orderedProperties.size(), objectUri);
            dataModel.put("orderedPropertyList", orderedProperties);
            
            // all statements which are needed to display these properties in HTML
            final Model allNeededStatementsForEdit =
                    this.getPoddSesameManager().getObjectDetailsForEdit(ontologyID, objectUri, conn);
            dataModel.put("completeModel", allNeededStatementsForEdit);
            
            /*
             * *** editObject.html.ftl ***
             * 
             * @ftlvariable name="isAdmin" type="boolean"
             * 
             * @ftlvariable name="canViewProjectParticipants" type="boolean" 
             * 
             * @ftlvariable name="postUrl" type="java.lang.String"
             * 
             * @ftlvariable name="objectPID" type="java.lang.String"
             * 
             * @ftlvariable name="objectType" type="java.lang.String"
             *  
             * @ftlvariable name="isProject" type="boolean" 
             * 
             * @ftlvariable name="elementList" type="java.util.ArrayList<podd.template.content.HTMLElementTemplate>"
             * 
             * @ftlvariable name="refersList" type="java.util.ArrayList<podd.template.content.HTMLElementTemplate>"
             * 
             * @ftlvariable name="fileList" type="java.util.ArrayList<podd.resources.util.view.FileElement>"
             * 
             * @ftlvariable name="canComplete" type="boolean"
             * 
             * @ftlvariable name="aHREF" type="java.lang.String"
             * 
             * @ftlvariable name="errorMessage" type="java.lang.String"
             * 
             * @ftlvariable name="objectNameError" type="java.lang.String"
             *  
             * @ftlvariable name="objectDescriptionError" type="java.lang.String"
             * 
             * @ftlvariable name="generalErrorList" type="java.util.ArrayList<java.lang.String>"
             * 
             * @ftlvariable name="objectErrorList" type="java.util.ArrayList<java.lang.String>"
             */
            
            // *** attachFile.html.ftl ***
            // stopRefreshKey - String
            // - values that don't have to be set
            // fileDescription type="java.lang.String" -->
            // fileErrorMessage" type="java.lang.String" -->
            // fileDescriptionError" type="java.lang.String" -->
            // attachedFileList" type="java.util.List<String>" -->
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
                catch(OpenRDFException e)
                {
                    this.log.error("Failed to close RepositoryConnection", e);
                    // Should we do anything other than log an error?
                }
            }
        }
        
        dataModel.put("initialized", initialized);
        return dataModel;
    }
}
