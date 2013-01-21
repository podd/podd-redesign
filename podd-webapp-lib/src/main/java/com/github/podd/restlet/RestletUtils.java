package com.github.podd.restlet;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.AppendableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilRoles;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;

/**
 * Wraps up calls to methods that relate to Restlet items, such as Representations.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 * Copied from https://github.com/ansell/oas
 */
public final class RestletUtils
{
    private static final Logger log = LoggerFactory.getLogger(RestletUtils.class);
    
    
    public static Map<String, Object> getBaseDataModel(final Request nextRequest)
    {
        final ClientInfo nextClientInfo = nextRequest.getClientInfo();
        final Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("resourceRef", nextRequest.getResourceRef());
        dataModel.put("rootRef", nextRequest.getRootRef());
        dataModel.put("keywords", "podd, ontology, phenomics");
        
        String baseUrl = nextRequest.getRootRef().toString();
        if (baseUrl.endsWith("/"))
        {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        dataModel.put("baseUrl", baseUrl);
        
        dataModel.put("clientInfo", nextClientInfo);
        dataModel.put("isAuthenticated", nextClientInfo.isAuthenticated());
        final List<Role> roles = nextClientInfo.getRoles();
        final boolean isAdmin = roles.contains(PoddRoles.SUPERUSER.getRole());
        dataModel.put("isAdmin", isAdmin);
        dataModel.put("user", nextClientInfo.getUser());
        
        final User currentUser = nextClientInfo.getUser();
        if(currentUser != null)
        {
            dataModel.put("currentUserName", currentUser.getName());
            
            RestletUtils.log.info("currentUser: {}", currentUser);
            RestletUtils.log.info("currentUser.getFirstName: {}", currentUser.getFirstName());
            RestletUtils.log.info("currentUser.getLastName: {}", currentUser.getLastName());
            RestletUtils.log.info("currentUser.getName: {}", currentUser.getName());
            RestletUtils.log.info("currentUser.getIdentifier: {}", currentUser.getIdentifier());
        }
        else
        {
            RestletUtils.log.info("No currentUser logged in");
        }
        
        return dataModel;
    }
    
    /**
     * Tests the parameter against a list of known true parameter values, before testing it against
     * a list of known false values.
     * 
     * @param nextParameter
     *            A parameter to test for its boolean value.
     * @return True if the parameter looks like a true value and false otherwise.
     */
    public static boolean getBooleanFromParameter(final Parameter nextParameter)
    {
        if(nextParameter == null)
        {
            throw new IllegalArgumentException("Cannot get a boolean from a null parameter");
        }
        
        final String paramValue = nextParameter.getValue();
        
        if(paramValue == null)
        {
            return false;
        }
        
        boolean result = false;
        
        // If that was true, return true
        if(Boolean.valueOf(paramValue))
        {
            result = true;
        }
        else if(paramValue.equalsIgnoreCase("false"))
        {
            result = false;
        }
        else if(paramValue.equalsIgnoreCase("yes"))
        {
            result = true;
        }
        else if(paramValue.equalsIgnoreCase("no"))
        {
            result = false;
        }
        else if(paramValue.equalsIgnoreCase("y"))
        {
            result = true;
        }
        else if(paramValue.equalsIgnoreCase("n"))
        {
            result = false;
        }
        
        return result;
    }
    
    /**
     * Returns a templated representation dedicated to HTML content.
     * 
     * @param templateName
     *            The name of the template.
     * @param dataModel
     *            The collection of data processed by the template engine.
     * @param mediaType
     *            The media type of the representation.
     * @param freemarkerConfiguration
     *            The FreeMarker template configuration
     * @return The representation.
     */
    public static Representation getHtmlRepresentation(final String templateName, final Map<String, Object> dataModel,
            final MediaType mediaType, final Configuration freemarkerConfiguration) throws ResourceException
    {
        // The template representation is based on Freemarker.
        return new TemplateRepresentation(templateName, freemarkerConfiguration, dataModel, mediaType);
    }
    
/*    
    
    public static Representation getJsonRepresentation(final Annotation nextAnnotation, final Variant variant,
            final Reference resourceReference) throws ResourceException, RepositoryException, RDFHandlerException
    {
        return RestletUtils.toRDFSerialisation(MediaType.APPLICATION_JSON.getName(),
                SesameUtils.toRDFRepository(nextAnnotation, null, UriConstants.ANNOTATION_MANAGEMENT_GRAPH));
    }
    
    public static Representation getJsonRepresentation(final Collection<Annotation> annotationSet,
            final Variant variant, final Reference resourceReference) throws ResourceException, RepositoryException,
        RDFHandlerException
    {
        final Repository myRepository = new SailRepository(new MemoryStore());
        myRepository.initialize();
        
        // push each of the annotations into myRepository
        for(final Annotation nextAnnotation : annotationSet)
        {
            SesameUtils.toRDFRepository(nextAnnotation, myRepository, UriConstants.ANNOTATION_MANAGEMENT_GRAPH);
        }
        
        return RestletUtils.toRDFSerialisation(MediaType.APPLICATION_JSON.getName(), myRepository);
    }
    
    public static Representation getJsonRepresentation(final Ontology nextOntology, final MediaType variant,
            final Reference resourceReference) throws ResourceException, RepositoryException, RDFHandlerException
    {
        return RestletUtils.toRDFSerialisation(MediaType.APPLICATION_JSON.getName(),
                SesameUtils.toRDFRepository(nextOntology, null));
    }
    
    public static Representation getJsonRepresentation(final ResourceCount results, final Variant variant,
            final Reference resourceRef) throws RepositoryException, RDFHandlerException
    {
        return RestletUtils.toRDFSerialisation(MediaType.APPLICATION_JSON.getName(),
                SesameUtils.toRDFRepository(results, null));
    }
    
    public static Representation getJsonRepresentationShortOntologies(final Collection<Ontology> ontologies,
            final MediaType variant, final Reference resourceReference) throws ResourceException, RepositoryException,
        RDFHandlerException
    {
        final Repository myRepository = new SailRepository(new MemoryStore());
        myRepository.initialize();
        
        for(final Ontology nextOntology : ontologies)
        {
            SesameUtils.toRDFRepository(nextOntology, myRepository);
        }
        
        return RestletUtils.toRDFSerialisation(variant.getName(), myRepository);
    }
    
    public static Representation getRdfRepresentation(final Annotation nextAnnotation, final MediaType variant,
            final Reference resourceReference) throws ResourceException, RepositoryException, RDFHandlerException
    {
        return RestletUtils.toRDFSerialisation(variant.getName(),
                SesameUtils.toRDFRepository(nextAnnotation, null, UriConstants.ANNOTATION_MANAGEMENT_GRAPH));
    }
    
    public static Representation getRdfRepresentation(final Collection<Annotation> annotationSet,
            final MediaType mediaType, final Reference resourceReference) throws ResourceException,
        RepositoryException, RDFHandlerException
    {
        final Repository myRepository = new SailRepository(new MemoryStore());
        myRepository.initialize();
        
        // push each of the annotations into myRepository
        for(final Annotation nextAnnotation : annotationSet)
        {
            SesameUtils.toRDFRepository(nextAnnotation, myRepository, UriConstants.ANNOTATION_MANAGEMENT_GRAPH);
        }
        
        return RestletUtils.toRDFSerialisation(mediaType.getName(), myRepository);
    }
    
    public static Representation getRdfRepresentation(final Ontology nextOntology, final MediaType variant,
            final Reference resourceReference) throws ResourceException, RepositoryException, RDFHandlerException
    {
        return RestletUtils.toRDFSerialisation(variant.getName(), SesameUtils.toRDFRepository(nextOntology, null));
    }
    
    public static Representation getRdfRepresentation(final ResourceCount results, final Variant variant,
            final Reference resourceRef) throws RepositoryException, RDFHandlerException
    {
        return RestletUtils.toRDFSerialisation(variant.getMediaType().getName(),
                SesameUtils.toRDFRepository(results, null));
    }
    
    public static Representation getRdfRepresentationShortOntologies(final Collection<Ontology> ontologies,
            final Map<URI, URI> latestOntologyVersionUris, final MediaType variant, final Reference resourceReference)
        throws ResourceException, RepositoryException, RDFHandlerException
    {
        final Repository myRepository = new SailRepository(new MemoryStore());
        myRepository.initialize();
        
        for(final Ontology nextOntology : ontologies)
        {
            SesameUtils.toRDFRepository(nextOntology, myRepository);
            if(latestOntologyVersionUris.containsKey(nextOntology.getOntologyUri()))
            {
                SesameUtils.addLatestVersion(nextOntology.getOntologyUri(),
                        latestOntologyVersionUris.get(nextOntology.getOntologyUri()), myRepository);
            }
        }
        
        return RestletUtils.toRDFSerialisation(variant.getName(), myRepository);
    }
  
    */
    
    /**
     * Serialises part or all of a repository into RDF, depending on which contexts are provided.
     * 
     * @param mimeType
     *            The MIME type of the serialised RDF statements.
     * @param myRepository
     *            The repository containing the RDF statements to serialise.
     * @param contexts
     *            0 or more Resources identifying contexts in the repository to serialise.
     * @return A Restlet Representation containing the
     * @throws RepositoryException
     * @throws RDFHandlerException
     */
    public static Representation toRDFSerialisation(final String mimeType, final Repository myRepository,
            final Resource... contexts) throws RepositoryException, RDFHandlerException
    {
        if(myRepository == null)
        {
            throw new IllegalArgumentException("Repository cannot be null");
        }
        
        // Attempt to find a writer format based on their requested mime type, or if that fails,
        // give them RDF/XML that every RDF library can process.
        final RDFFormat outputFormat = Rio.getWriterFormatForMIMEType(mimeType, RDFFormat.RDFXML);
        
        final StringWriter writer = new StringWriter();
        
        RepositoryConnection conn = null;
        
        conn = myRepository.getConnection();
        
        final RDFHandler output = Rio.createWriter(outputFormat, writer);
        
        conn.export(output, contexts);
        
        // TODO: find a subclass of Representation that accepts a writer directly, without having to
        // serialise it to a string, to improve performance for large results sets.
        final Representation result =
                new AppendableRepresentation(writer.toString(), MediaType.valueOf(outputFormat.getDefaultMIMEType()),
                        Language.DEFAULT, CharacterSet.UTF_8);
        
        return result;
        
    }
    
    /**
     * Private default constructor
     */
    private RestletUtils()
    {
    }
    
}