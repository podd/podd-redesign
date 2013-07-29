/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
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
import com.github.podd.utils.FreemarkerUtil;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;
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
    
    @Get(":html")
    public Representation getArtifactHtml(final Representation entity) throws ResourceException
    {
        this.log.info("getArtifactHtml");
        
        final String artifactString = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
        
        if(artifactString == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact ID not submitted");
        }
        
        final String versionString =
                this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, true);
        
        // optional parameter for inner objects
        final String objectToView = this.getQuery().getFirstValue(PoddWebConstants.KEY_OBJECT_IDENTIFIER, true);
        
        this.log.info("requesting get artifact (HTML): {}, {}, {}", artifactString, versionString, objectToView);
        
        // FIXME: The artifact may be published here
        this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ, PoddRdfConstants.VF.createURI(artifactString));
        // completed checking authorization
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        InferredOWLOntologyID ontologyID = null;
        try
        {
            if(versionString == null)
            {
                ontologyID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactString));
            }
            else
            {
                ontologyID =
                        this.getPoddArtifactManager()
                                .getArtifact(IRI.create(artifactString), IRI.create(versionString));
            }
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact", e);
        }
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "objectDetails.html.ftl");
        dataModel.put("pageTitle", "View Artifact");
        
        try
        {
            this.populateDataModelWithArtifactData(ontologyID, objectToView, dataModel);
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to populate data model", e);
        }
        
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    @Get(":rdf|rj|ttl")
    public Representation getArtifactRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        // FIXME: Some Firefox requests get sent with Accept: */*, and Restlet chooses this method
        // instead of HTML as default
        // To fix this we need to check whether Accept is exactly "*/*" and response with HTML
        // variant instead of an RDF variant
        
        this.log.info("getArtifactRdf");
        
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        
        try
        {
            final String artifactString = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
            
            if(artifactString == null)
            {
                this.log.error("Artifact ID not submitted");
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact ID not submitted");
            }
            
            final String versionString =
                    this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, true);
            
            this.log.info("requesting get artifact ({}): {}", variant.getMediaType().getName(), artifactString);
            
            // FIXME: The artifact may be published here
            this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ,
                    PoddRdfConstants.VF.createURI(artifactString));
            // completed checking authorization
            
            final User user = this.getRequest().getClientInfo().getUser();
            this.log.info("authenticated user: {}", user);
            
            InferredOWLOntologyID ontologyID = null;
            if(versionString == null)
            {
                ontologyID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactString));
            }
            else
            {
                ontologyID =
                        this.getPoddArtifactManager()
                                .getArtifact(IRI.create(artifactString), IRI.create(versionString));
            }
            
            final String includeInferredString =
                    this.getRequest().getResourceRef().getQueryAsForm()
                            .getFirstValue(PoddWebConstants.KEY_INCLUDE_INFERRED, true);
            final boolean includeInferred = Boolean.valueOf(includeInferredString);
            
            this.getPoddApplication()
                    .getPoddArtifactManager()
                    .exportArtifact(ontologyID, stream,
                            RDFFormat.forMIMEType(variant.getMediaType().getName(), RDFFormat.TURTLE), includeInferred);
        }
        catch(final UnmanagedArtifactIRIException e)
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
     * This method retrieves necessary info about the object being viewed via SPARQL queries and
     * populates the data model.
     * 
     * @param ontologyID
     *            The artifact to be viewed
     * @param objectToView
     *            An optional internal object to view
     * @param ontologyGraphs
     *            The schema ontology graphs that should be part of the context for SPARQL
     * @param dataModel
     *            Freemarker data model to be populated
     * @throws OpenRDFException
     */
    private void populateDataModelWithArtifactData(final InferredOWLOntologyID ontologyID, final String objectToView,
            final Map<String, Object> dataModel) throws OpenRDFException
    {
        
        PoddObjectLabel theObject = null;
        
        if(objectToView != null && !objectToView.trim().isEmpty())
        {
            theObject =
                    this.getPoddArtifactManager().getObjectLabel(ontologyID,
                            PoddRdfConstants.VF.createURI(objectToView));
        }
        else
        {
            // find and set top-object of this artifact as the object to display
            final List<PoddObjectLabel> topObjectLabels =
                    this.getPoddArtifactManager().getTopObjectLabels(Arrays.asList(ontologyID));
            if(topObjectLabels == null || topObjectLabels.size() != 1)
            {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "There should be only 1 top object");
            }
            
            theObject = topObjectLabels.get(0);
        }
        // set title & description of object to display
        dataModel.put("poddObject", theObject);
        
        final URI objectUri = theObject.getObjectURI();
        
        this.populateParentDetails(ontologyID, objectUri, dataModel);
        
        // find the object's type
        final List<PoddObjectLabel> objectTypes = this.getPoddArtifactManager().getObjectTypes(ontologyID, objectUri);
        if(objectTypes == null || objectTypes.isEmpty())
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not determine type of object");
        }
        
        // Get label for the object type
        final PoddObjectLabel label = objectTypes.get(0);
        dataModel.put("objectType", label);
        
        // populate the properties of the object
        final List<URI> orderedProperties =
                this.getPoddArtifactManager().getOrderedProperties(ontologyID, objectUri, false);
        
        final Model allNeededStatementsForDisplay =
                this.getPoddArtifactManager().getObjectDetailsForDisplay(ontologyID, objectUri);
        
        dataModel.put("artifactUri", ontologyID.getOntologyIRI().toOpenRDFURI());
        dataModel.put("propertyList", orderedProperties);
        dataModel.put("completeModel", allNeededStatementsForDisplay);
        
        // FIXME: determine based on project status and user authorization
        dataModel.put("canEditObject", true);
        
        // FIXME: should be set based on the current object and user authorization
        dataModel.put("canAddChildren", true);
        
        dataModel.put("selectedObjectCount", 0);
        dataModel.put("childHierarchyList", Collections.emptyList());
        
        dataModel.put("util", new FreemarkerUtil());
    }
    
    /**
     * Populate the data model with info about the parent of the current object. If the given object
     * does not have a parent (i.e. is a Top Object) the data model remains unchanged.
     * 
     * TODO: This method uses multiple API methods resulting in several SPARQL queries. Efficiency
     * could be improved by either adding a new API method or modifying getParentDetails() to supply
     * most of the required information.
     * 
     * @param ontologyID
     * @param objectUri
     *            The object whose parent details are required
     * @param dataModel
     * @throws OpenRDFException
     */
    private void populateParentDetails(final InferredOWLOntologyID ontologyID, final URI objectUri,
            final Map<String, Object> dataModel) throws OpenRDFException
    {
        
        final Model parentDetails = this.getPoddArtifactManager().getParentDetails(ontologyID, objectUri);
        if(parentDetails.size() == 1)
        {
            final Statement statement = parentDetails.iterator().next();
            
            final Map<String, String> parentMap = new HashMap<>();
            
            final String parentUriString = statement.getSubject().stringValue();
            parentMap.put("uri", parentUriString);
            
            final URI parentUri = PoddRdfConstants.VF.createURI(parentUriString);
            final URI parentPredicateUri = PoddRdfConstants.VF.createURI(statement.getPredicate().stringValue());
            
            // - parent's Title
            String parentLabel = "Missing Title";
            final PoddObjectLabel objectLabel = this.getPoddArtifactManager().getObjectLabel(ontologyID, parentUri);
            if(objectLabel != null)
            {
                parentLabel = objectLabel.getLabel();
            }
            parentMap.put("label", parentLabel);
            
            // - parent relationship Label
            String predicateLabel = "Missing parent relationship";
            final PoddObjectLabel predicateLabelModel =
                    this.getPoddArtifactManager().getObjectLabel(ontologyID, parentPredicateUri);
            if(predicateLabelModel != null)
            {
                predicateLabel = predicateLabelModel.getLabel();
            }
            parentMap.put("relationship", predicateLabel);
            
            // - parent's Type
            String parentType = "Unknown Type";
            final List<PoddObjectLabel> objectTypes =
                    this.getPoddArtifactManager().getObjectTypes(ontologyID, parentUri);
            if(objectTypes.size() > 0)
            {
                parentType = objectTypes.get(0).getLabel();
            }
            parentMap.put("type", parentType);
            
            dataModel.put("parentObject", parentMap);
        }
    }
    
}
