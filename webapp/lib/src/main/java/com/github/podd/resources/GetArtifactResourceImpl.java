/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
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

import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
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
        
        InferredOWLOntologyID ontologyID;
        try
        {
            final PoddArtifactManager artifactManager =
                    ((PoddWebServiceApplication)this.getApplication()).getPoddArtifactManager();
            ontologyID = artifactManager.getArtifactByIRI(IRI.create(artifactUri));
        }
        catch(UnmanagedArtifactIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact", e);
        }
        
        List<URI> schemaOntologyGraphs = new ArrayList<URI>(Arrays.asList(this.tempSchemaGraphs));//this.getSchemaOntologyGraphs();
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "objectDetails.html.ftl");
        dataModel.put("pageTitle", "View Artifact");
        
        try
        {
            this.populateDataModelWithArtifactData(ontologyID, schemaOntologyGraphs, dataModel);
        }
        catch(OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to populate data model");
        }
        
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
            
            this.log.info("requesting get artifact ({}): {}",variant.getMediaType().getName(), artifactUri);
            
            this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ, 
                    Collections.<URI> singleton(PoddRdfConstants.VALUE_FACTORY.createURI(artifactUri)));
            // completed checking authorization

            final User user = this.getRequest().getClientInfo().getUser();
            this.log.info("authenticated user: {}", user);
            
            
            InferredOWLOntologyID ontologyID =
                    this.getPoddApplication().getPoddArtifactManager().getArtifactByIRI(IRI.create(artifactUri));
            
            // FIXME: support prototype method for this
            // use this instead of ../base/ ../inferred/.. in the Prototype. Change documentation too.
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
     * @param ontologyID
     * @param dataModel
     * @throws OpenRDFException 
     */
    private void populateDataModelWithArtifactData(InferredOWLOntologyID ontologyID, List<URI> schemaOntologyGraphs,  
            Map<String, Object> dataModel) throws OpenRDFException
    {
        
        RepositoryConnection conn = this.getPoddApplication().getPoddRepositoryManager().getRepository().getConnection();
        conn.begin();
        try
        {
            SparqlQueryHelper spike = new SparqlQueryHelper();
            
            //FIXME: put in a Model to freemarker instead of Maps
            
            List<PoddObject> topObjectList = spike.getTopObjects(conn, 
                    ontologyID.getVersionIRI().toOpenRDFURI(), ontologyID.getInferredOntologyIRI().toOpenRDFURI());
            if (topObjectList == null || topObjectList.size() != 1)
            {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Found more than 1 top object");
            }
            dataModel.put("poddObject", topObjectList.get(0));
            
            
            Map<String, String> m = spike.getTopObjectDetailsAsModel(conn, 
                    ontologyID.getVersionIRI().toOpenRDFURI(), ontologyID.getInferredOntologyIRI().toOpenRDFURI());            
            
            dataModel.put("topObject", m);

            // hack together the list of contexts to query in
            schemaOntologyGraphs.add(ontologyID.getVersionIRI().toOpenRDFURI());
            schemaOntologyGraphs.add(ontologyID.getInferredOntologyIRI().toOpenRDFURI());
            
            // object to display
            URI objectUri = ontologyID.getOntologyIRI().toOpenRDFURI();
            
            Model model = spike.getPoddObjectDetails(objectUri, conn, schemaOntologyGraphs.toArray(new URI[0]));
            dataModel.put("objectModel", model);
            
            //TODO: populate top object creator details
        }
        finally
        {
            if (conn != null)
            {
                conn.rollback(); //read only, nothing to commit
                conn.close();
            }
        }
        // hard-code the required values first to display a valid html page
        //DEBUG
        dataModel.put("canEditObject", true);
        dataModel.put("uri", ontologyID.getOntologyIRI().toString());
        dataModel.put("objectType", "artifact");
        dataModel.put("creationDate", "2013-01-01");
        dataModel.put("modifiedDate", "2013-01-31");

        
//        dataModel.put("elementList", Arrays.asList("element1", "element2")); - TODO
        
        final Map<String, Object> poddObject = new HashMap<String, Object>();
        poddObject.put("uri", ontologyID.getOntologyIRI().toString());
        poddObject.put("localName", "Hardcoded project title");
        poddObject.put("description", "Dummy project from the resource");

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
        
        poddObject.put("members", new HashMap<String, String>());
        
//        final Map<String, String> roleMap = new HashMap<String, String>();
//        roleMap.put("description", "A dummy user account for testing");
//        objectDetailsMap.put("repositoryRole", roleMap);
            
//        dataModel.put("poddObject", poddObject);
        
        
        // -populate refers to list
        final List<Object> refersToList = new ArrayList<Object>();
        
        final Map<String, Object> refersToElement = new HashMap<String, Object>();
        refersToElement.put("label", "Refers To Label");
        // DESIGN FIXME: Figure out a way of doing this without removing characters. It is not an option to remove characters or split URIs. 
        refersToElement.put("propertyUriWithoutNamespace", "artifact89");
        refersToElement.put("availableObjects", this.getAvailableObjects());
        refersToElement.put("areSelectedObjects", true);
        
        refersToList.add(refersToElement);
        
        dataModel.put("refersToList", refersToList);        

        dataModel.put("selectedObjectCount", 0);
        dataModel.put("childHierarchyList", Collections.emptyList());
        
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
            anObject.put("uri", "object:34343");
            anObject.put("title", "Object " + i);
            anObject.put("description", "This is a simple object within an artifact");
            
            list.add(anObject);
        }
        
        return list;
    }
    
    //FIXME: hard coded until Schema Manager is implemented
    URI[] tempSchemaGraphs = {
            ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/dcTerms/1"),
            ValueFactoryImpl.getInstance().createURI(
                    "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/dcTerms/1"),
            ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/foaf/1"),
            ValueFactoryImpl.getInstance().createURI(
                    "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/foaf/1"),
            ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddUser/1"),
            ValueFactoryImpl.getInstance().createURI(
                    "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddUser/1"),
            ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddBase/1"),
            ValueFactoryImpl.getInstance().createURI(
                    "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddBase/1"),
            ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddScience/1"),
            ValueFactoryImpl.getInstance().createURI(
                    "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddScience/1"), };
    
    private List<URI> getSchemaOntologyGraphs()
    {
        List<URI> schemaOntologyGraphs = new ArrayList<URI>();
        
        final PoddSchemaManager schemaManager =
                ((PoddWebServiceApplication)this.getApplication()).getPoddSchemaManager();
        String[] schemaPaths = {PoddRdfConstants.PATH_PODD_DCTERMS, PoddRdfConstants.PATH_PODD_FOAF,
                PoddRdfConstants.PATH_PODD_USER, PoddRdfConstants.PATH_PODD_BASE,
                PoddRdfConstants.PATH_PODD_SCIENCE, PoddRdfConstants.PATH_PODD_PLANT};
        
        for(String schemaPath : schemaPaths)
        {
            try
            {   
                InferredOWLOntologyID inferredID = schemaManager.getCurrentSchemaOntologyVersion(IRI.create(schemaPath));
                //add graph names
                schemaOntologyGraphs.add(inferredID.getVersionIRI().toOpenRDFURI());
                schemaOntologyGraphs.add(inferredID.getInferredOntologyIRI().toOpenRDFURI());
            }
            catch(UnmanagedSchemaIRIException e1)
            {
                log.error("Could not locate schema ontology {}", schemaPath);
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not locate schema ontology"); 
            }
        }
        return schemaOntologyGraphs;
    }
    
}
