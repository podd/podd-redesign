/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
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

import com.github.podd.api.DanglingObjectPolicy;
import com.github.podd.api.FileReferenceVerificationPolicy;
import com.github.podd.api.UpdatePolicy;
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
    @Post("rdf|rj|json|ttl")
    public Representation editArtifactToRdf(final Representation entity, final Variant variant)
        throws ResourceException
    {
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
        
        if(artifactUri == null)
        {
            this.log.error("Artifact ID not submitted");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact IRI not submitted");
        }
        
        // Once we find the artifact URI, check authentication for it immediately
        this.checkAuthentication(PoddAction.ARTIFACT_EDIT,
                Collections.<URI> singleton(PoddRdfConstants.VF.createURI(artifactUri)));
        
        final String versionUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, true);
        
        if(versionUri == null)
        {
            this.log.error("Artifact Version IRI not submitted");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact Version IRI not submitted");
        }
        
        // optional multiple parameter 'objectUri'
        String[] objectURIStrings = this.getQuery().getValuesArray(PoddWebConstants.KEY_OBJECT_IDENTIFIER, true);
        
        // - optional parameter 'isreplace'
        UpdatePolicy updatePolicy = UpdatePolicy.REPLACE_EXISTING;
        final String isReplaceStr = this.getQuery().getFirstValue(PoddWebConstants.KEY_EDIT_WITH_REPLACE, true);
        if(isReplaceStr != null && (Boolean.valueOf(isReplaceStr) == false))
        {
            updatePolicy = UpdatePolicy.MERGE_WITH_EXISTING;
        }
        
        // - optional parameter 'isforce'
        DanglingObjectPolicy danglingObjectPolicy = DanglingObjectPolicy.REPORT;
        final String forceStr = this.getQuery().getFirstValue(PoddWebConstants.KEY_EDIT_WITH_FORCE, true);
        if(forceStr != null && Boolean.valueOf(forceStr))
        {
            danglingObjectPolicy = DanglingObjectPolicy.FORCE_CLEAN;
        }
        
        // - optional parameter 'verifyfilerefs'
        FileReferenceVerificationPolicy fileRefVerificationPolicy = FileReferenceVerificationPolicy.DO_NOT_VERIFY;
        final String fileRefVerifyStr =
                this.getQuery().getFirstValue(PoddWebConstants.KEY_EDIT_VERIFY_FILE_REFERENCES, true);
        if(fileRefVerifyStr != null && Boolean.valueOf(fileRefVerifyStr))
        {
            fileRefVerificationPolicy = FileReferenceVerificationPolicy.VERIFY;
        }
        
        Collection<URI> objectUris = new ArrayList<URI>(objectURIStrings.length);
        for(String nextObjectURIString : objectURIStrings)
        {
            objectUris.add(PoddRdfConstants.VF.createURI(nextObjectURIString));
        }
        
        this.log.info("requesting edit artifact ({}): {}, with isReplace {}", variant.getMediaType().getName(),
                artifactUri, updatePolicy);
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        // - get input stream with edited RDF content
        InputStream inputStream = null;
        try
        {
            inputStream = entity.getStream();
        }
        catch(final IOException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
        }
        final RDFFormat inputFormat = Rio.getParserFormatForMIMEType(entity.getMediaType().getName(), RDFFormat.RDFXML);
        
        // - prepare response
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        final RDFWriter writer =
                Rio.createWriter(Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML),
                        output);
        
        // - do the artifact update
        try
        {
            final InferredOWLOntologyID ontologyID =
                    this.getPoddArtifactManager().updateArtifact(PoddRdfConstants.VF.createURI(artifactUri),
                            PoddRdfConstants.VF.createURI(versionUri), objectUris, inputStream, inputFormat,
                            updatePolicy, danglingObjectPolicy, fileRefVerificationPolicy);
            // TODO - send detailed errors for display where possible
            
            // FIXME Change response format so that it does not resemble an empty OWL Ontology
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
                Collections.singleton(PoddRdfConstants.VF.createURI(artifactUri)));
        
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
     * Request for RDF data for building the "edit object" page.
     */
    @Get("rdf|rj|json|ttl")
    public Representation getEditArtifactRdf(final Representation entity, final Variant variant)
        throws ResourceException
    {
        // the artifact in which editing is requested
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
        if(artifactUri == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact ID not submitted");
        }
        
        // Podd object to be edited. NULL indicates top object is to be edited.
        final String objectUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_OBJECT_IDENTIFIER);
        
        this.log.info("requesting to populate edit artifact ({}): {}, ", variant.getMediaType().getName(), artifactUri);
        
        this.checkAuthentication(PoddAction.ARTIFACT_EDIT,
                Collections.singleton(PoddRdfConstants.VF.createURI(artifactUri)));
        
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
        
        final Model modelForEdit = this.getModelForEdit(ontologyID, objectUri);
        
        // - prepare response
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        final RDFWriter writer =
                Rio.createWriter(Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML),
                        output);
        try
        {
            writer.startRDF();
            for(final Statement st : modelForEdit)
            {
                writer.handleStatement(st);
            }
            writer.endRDF();
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response", e);
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(writer.getRDFFormat()
                .getDefaultMIMEType()));
    }
    
    /**
     * Get a {@link Model} containing all data and meta-data necessary to display the "edit object"
     * page.
     * 
     * @param ontologyID
     * @param objectToEdit
     * @return A Model containing all necessary statements
     */
    private Model getModelForEdit(final InferredOWLOntologyID ontologyID, final String objectToEdit)
    {
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
            
            final List<URI> objectTypes = this.getPoddSesameManager().getObjectTypes(ontologyID, objectUri, conn);
            if(objectTypes == null || objectTypes.isEmpty())
            {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not determine type of object");
            }
            
            return this.getPoddSesameManager().getObjectDetailsForEdit(ontologyID, objectUri, conn);
        }
        catch(final OpenRDFException e)
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
                catch(final OpenRDFException e)
                {
                    this.log.error("Failed to close RepositoryConnection", e);
                    // Should we do anything other than log an error?
                }
            }
        }
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
            final String objectToEdit)
    {
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "modify_object.html.ftl");
        dataModel.put("pageTitle", "Edit Artifact");
        
        // add required constants and methods to data model
        dataModel.put("RDFS_LABEL", RDFS.LABEL);
        dataModel.put("RDFS_RANGE", RDFS.RANGE);
        dataModel.put("RDF_TYPE", RDF.TYPE);
        dataModel.put("OWL_OBJECT_PROPERTY", OWL.OBJECTPROPERTY);
        dataModel.put("OWL_DATA_PROPERTY", OWL.DATATYPEPROPERTY);
        dataModel.put("OWL_ANNOTATION_PROPERTY", OWL.ANNOTATIONPROPERTY);
        // dataModel.put("OWL_MAX_CARDINALITY", PoddRdfConstants.OWL_MAX_QUALIFIED_CARDINALITY);
        // dataModel.put("OWL_MIN_CARDINALITY", PoddRdfConstants.OWL_MIN_QUALIFIED_CARDINALITY);
        // dataModel.put("OWL_CARDINALITY", PoddRdfConstants.OWL_QUALIFIED_CARDINALITY);
        dataModel.put("PODD_BASE_HAS_CARDINALITY", PoddRdfConstants.PODD_BASE_HAS_CARDINALITY);
        dataModel.put("PODD_BASE_DISPLAY_TYPE", PoddRdfConstants.PODD_BASE_DISPLAY_TYPE);
        dataModel.put("PODD_BASE_HAS_WEIGHT", PoddRdfConstants.PODD_BASE_WEIGHT);
        
        dataModel.put("util", new FreemarkerUtil());
        
        // Defaults to false. Set to true if multiple objects are being edited concurrently
        // TODO: investigate how to use this
        final boolean initialized = false;
        
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
            
            final List<URI> objectTypes = this.getPoddSesameManager().getObjectTypes(ontologyID, objectUri, conn);
            if(objectTypes == null || objectTypes.isEmpty())
            {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not determine type of object");
            }
            
            // Get label for the object type
            final PoddObjectLabel objectType =
                    this.getPoddSesameManager().getObjectLabel(ontologyID, objectTypes.get(0), conn);
            if(objectType == null || objectType.getLabel() == null)
            {
                dataModel.put("objectType", objectTypes.get(0));
            }
            else
            {
                dataModel.put("objectType", objectType.getLabel());
            }
            
            final PoddObjectLabel theObject = this.getPoddSesameManager().getObjectLabel(ontologyID, objectUri, conn);
            dataModel.put("poddObject", theObject);
            
            // an ordered-list of the properties about the object
            final List<URI> orderedProperties =
                    this.getPoddSesameManager().getWeightedProperties(ontologyID, objectUri, true, conn);
            this.log.info("Found {} properties about object {}", orderedProperties.size(), objectUri);
            dataModel.put("orderedPropertyList", orderedProperties);
            
            // all statements which are needed to display these properties in HTML
            final Model allNeededStatementsForEdit =
                    this.getPoddSesameManager().getObjectDetailsForEdit(ontologyID, objectUri, conn);
            
            // identify Collection elements and add all their possible values to the data model
            // TODO: figure out a better way of doing this
            final Map<String, List<PoddObjectLabel>> membersMap = new HashMap<String, List<PoddObjectLabel>>();
            for(final URI prop : orderedProperties)
            {
                final String displayTypeValue =
                        allNeededStatementsForEdit.filter(prop, PoddRdfConstants.PODD_BASE_DISPLAY_TYPE, null)
                                .objectString();
                
                if(PoddRdfConstants.PODD_BASE_DISPLAY_TYPE_DROPDOWN.stringValue().equals(displayTypeValue))
                {
                    final List<PoddObjectLabel> memberList = new ArrayList<PoddObjectLabel>();
                    final List<URI> allValidMembers =
                            this.getPoddSesameManager().getAllValidMembers(ontologyID, prop, conn);
                    for(final URI member : allValidMembers)
                    {
                        memberList.add(this.getPoddSesameManager().getObjectLabel(ontologyID, member, conn));
                    }
                    membersMap.put(prop.toString(), memberList);
                    System.out.println(prop + " has valid members: " + allValidMembers);
                }
            }
            dataModel.put("members", membersMap);
            
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
             * @ftlvariable name="elementList"
             * type="java.util.ArrayList<podd.template.content.HTMLElementTemplate>"
             * 
             * @ftlvariable name="refersList"
             * type="java.util.ArrayList<podd.template.content.HTMLElementTemplate>"
             * 
             * @ftlvariable name="fileList"
             * type="java.util.ArrayList<podd.resources.util.view.FileElement>"
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
                catch(final OpenRDFException e)
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
