package com.github.podd.restlet;

import java.io.IOException;
import java.util.Collection;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Role;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.ansell.restletutils.CrossOriginResourceSharingFilter;
import com.github.ansell.restletutils.RestletUtilMediaType;
import com.github.ansell.restletutils.RestletUtilSesameRealm;
import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.file.PoddFileReferenceManager;
import com.github.podd.api.file.PoddFileReferenceProcessorFactoryRegistry;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
import com.github.podd.exception.PoddException;
import com.github.podd.impl.PoddArtifactManagerImpl;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddRepositoryManagerImpl;
import com.github.podd.impl.PoddSchemaManagerImpl;
import com.github.podd.impl.PoddSesameManagerImpl;
import com.github.podd.impl.file.PoddFileReferenceManagerImpl;
import com.github.podd.impl.purl.PoddPurlManagerImpl;
import com.github.podd.impl.purl.UUIDPurlProcessorFactoryImpl;
import com.github.podd.resources.AboutResourceImpl;
import com.github.podd.resources.CookieLoginResourceImpl;
import com.github.podd.resources.DeleteArtifactResourceImpl;
import com.github.podd.resources.EditArtifactResourceImpl;
import com.github.podd.resources.FileReferenceAttachResourceImpl;
import com.github.podd.resources.GetArtifactResourceImpl;
import com.github.podd.resources.IndexResourceImpl;
import com.github.podd.resources.TestResetResourceImpl;
import com.github.podd.resources.UploadArtifactResourceImpl;
import com.github.podd.resources.UserDetailsResourceImpl;
import com.github.podd.utils.PoddWebConstants;

import freemarker.template.Configuration;

/**
 * This class handles all requests from clients to the OAS Web Service.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 *         Copied from OAS project (https://github.com/ansell/oas)
 * 
 */
public class PoddWebServiceApplicationImpl extends PoddWebServiceApplication
{
    public static final URI ARTIFACT_MGT_GRAPH = ValueFactoryImpl.getInstance().createURI("urn:test:artifact-graph");
    
    public static final URI SCHEMA_MGT_GRAPH = ValueFactoryImpl.getInstance().createURI("urn:test:schema-graph");
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * The Freemarker template configuration.
     */
    private volatile Configuration freemarkerConfiguration;
    private volatile ChallengeAuthenticator auth;
    private volatile PoddSesameRealm realm;
    
    private Repository nextRepository;
    
    private PoddRepositoryManager poddRepositoryManager;
    private PoddSchemaManager poddSchemaManager;
    private PoddArtifactManager poddArtifactManager;
    
    /**
     * Default Constructor.
     * 
     * Adds the necessary file protocols and sets up the template location.
     */
    public PoddWebServiceApplicationImpl()
    {
        super();
        
        System.out.println("==============================");
        System.out.println("PODD Web Application");
        System.out.println("starting...");
        System.out.println("==============================");
        this.log.info("== Starting PODD Web Application ==");
        
        // List of protocols required by the application
        this.getConnectorService().getClientProtocols().add(Protocol.HTTP);
        this.getConnectorService().getClientProtocols().add(Protocol.CLAP);
        
        // Define extensions for RDF and Javascript
        // These extensions are also used to identify mediatypes in services
        // For example: @Get("owl") will not be processed without the declaration below
        this.getMetadataService().addExtension("rdf", MediaType.APPLICATION_RDF_XML, true);
        this.getMetadataService().addExtension("rj", RestletUtilMediaType.APPLICATION_RDF_JSON, true);
        this.getMetadataService().addExtension("owl", MediaType.APPLICATION_RDF_XML, false);
        this.getMetadataService().addExtension("json", MediaType.APPLICATION_JSON, true);
        this.getMetadataService().addExtension("ttl", MediaType.APPLICATION_RDF_TURTLE, true);
        this.getMetadataService().addExtension("n3", MediaType.TEXT_RDF_N3, true);
        this.getMetadataService().addExtension("nt", MediaType.TEXT_RDF_NTRIPLES, true);
        this.getMetadataService().addExtension("nq",
                MediaType.register("text/nquads", "The NQuads extension to the NTriples RDF serialisation"), true);
        
        this.getMetadataService().addExtension("js", MediaType.TEXT_JAVASCRIPT, true);
        this.getMetadataService().addExtension("css", MediaType.TEXT_CSS, true);
        
        this.getMetadataService().addExtension("multipart", MediaType.MULTIPART_FORM_DATA, true);
        this.getMetadataService().addExtension("form", MediaType.APPLICATION_WWW_FORM, false);
        
        // Automagically tunnel client preferences for extensions through the
        // tunnel
        this.getTunnelService().setExtensionsTunnel(true);
        
        this.nextRepository = ApplicationUtils.getNewRepository();
        this.initializePoddManagers();
    }
    
    /**
     * 
     */
    @Override
    public boolean authenticate(final PoddAction action, final Request request, final Response response,
            final Collection<URI> optionalObjectUris)
    {
        if(!action.isAuthRequired())
        {
            return true;
        }
        else if(!request.getClientInfo().isAuthenticated())
        {
            if(this.getAuthenticator() == null)
            {
                throw new RuntimeException("Could not find authentication method");
            }
            
            // add challenges to the response and set the status to HTTP 401 Unauthorized
            this.getAuthenticator().challenge(response, false);
            
            // Return false after the challenge and HTTP 401 response have been added to the
            // response
            return false;
        }
        else if (!action.isRoleRequired())
        {
            return true;
        }
        else if(!action.matchesForRoles(request.getClientInfo().getRoles()))
        {
            this.log.error("Authenticated user does not have enough privileges to execute the given action: {}", action);
            
            // FIXME: Implement auditing here
            // this.getDataHandler().addLogDetailsForRequest(message, referenceUri,
            // authenticationScope, get, currentUser, currentRole);
            
            return false;
        }
        else if(!action.requiresObjectUris())
        {
            return true;
        }
        else if(optionalObjectUris == null || optionalObjectUris.isEmpty())
        {
            this.log.error("Action requires object URIs and none were given: {}", action);
            
            return false;
        }
        else
        {
            final Collection<Role> rolesCommonAcrossGivenObjects =
                    this.getRealm().getCommonRolesForObjects(request.getClientInfo().getUser(), optionalObjectUris);
            
            if(!action.matchesForRoles(rolesCommonAcrossGivenObjects))
            {
                this.log.error("Authenticated user does not have enough privileges to execute the given action: {}" +
                		" on the given objects: {}", action, optionalObjectUris);
                return false;
            }
        }
        
        if(request.getClientInfo().isAuthenticated() && request.getClientInfo().getRoles().isEmpty())
        {
            // TODO: can this case still occur?
            this.log.warn("Authenticated user did not have any roles: user={}", request.getClientInfo().getUser());
        }
        
        return true;
    }
    
    /**
     * Create the necessary connections between the application and its handlers.
     */
    @Override
    public Restlet createInboundRoot()
    {
        final ChallengeAuthenticator authenticator = this.getAuthenticator();
        
        if(authenticator == null)
        {
            throw new RuntimeException("Could not find authentication method");
        }
        
        final Router router = new Router(this.getContext());
        
        // Add a route for Login form. Login service is handled by the authenticator
        // NOTE: This only displays the login form. All HTTP POST requests to the login path should
        // be handled by the Authenticator
        final String loginFormPath = PoddWebConstants.PATH_LOGIN_FORM;
        this.log.info("attaching login service to path={}", loginFormPath);
        router.attach(loginFormPath, CookieLoginResourceImpl.class);
        
        // Add a route for the reset service.
        final String resetPath =
                PoddWebConstants.PATH_RESET_PREFIX
                        + PropertyUtil.get(PoddWebConstants.PROPERTY_TEST_WEBSERVICE_RESET_KEY, "");
        this.log.info("attaching reset service to path={}", resetPath);
        router.attach(resetPath, TestResetResourceImpl.class);
        
        // Add a route for the About page.
        final String aboutPagePath = PoddWebConstants.PATH_ABOUT;
        this.log.info("attaching about service to path={}", aboutPagePath);
        router.attach(aboutPagePath, AboutResourceImpl.class);
        
        // Add a route for the Index page.
        final String indexPagePath = PoddWebConstants.PATH_INDEX;
        this.log.info("attaching index service to path={}", indexPagePath);
        router.attach(indexPagePath, IndexResourceImpl.class);
        
        // Add a route for the User Details page.
        final String userDetailsPath = PoddWebConstants.PATH_USER_DETAILS;
        this.log.info("attaching user details service to path={}", userDetailsPath);
        router.attach(userDetailsPath, UserDetailsResourceImpl.class);
        
        // Add a route for the Upload Artifact page.
        final String uploadArtifactPath = PoddWebConstants.PATH_ARTIFACT_UPLOAD;
        this.log.info("attaching Upload Artifact service to path={}", uploadArtifactPath);
        router.attach(uploadArtifactPath, UploadArtifactResourceImpl.class);
        
        // Add a route for the Get Artifact page.
        final String getArtifactBase = PoddWebConstants.PATH_ARTIFACT_GET_BASE;
        this.log.info("attaching Get Artifact (base) service to path={}", getArtifactBase);
        router.attach(getArtifactBase, GetArtifactResourceImpl.class);
        
        final String getArtifactInferred = PoddWebConstants.PATH_ARTIFACT_GET_INFERRED;
        this.log.info("attaching Get Artifact (inferred) service to path={}", getArtifactInferred);
        router.attach(getArtifactInferred, GetArtifactResourceImpl.class);
        
        // Add a route for the Edit Artifact page.
        final String editArtifactMerge = PoddWebConstants.PATH_ARTIFACT_EDIT_MERGE;
        this.log.info("attaching Edit Artifact (merge) service to path={}", editArtifactMerge);
        router.attach(editArtifactMerge, EditArtifactResourceImpl.class);
        
        final String editArtifactReplace = PoddWebConstants.PATH_ARTIFACT_EDIT_REPLACE;
        this.log.info("attaching Edit Artifact (replace) service to path={}", editArtifactReplace);
        router.attach(editArtifactReplace, EditArtifactResourceImpl.class);
        
        // Add a route for the Delete Artifact page.
        final String deleteArtifact = PoddWebConstants.PATH_ARTIFACT_DELETE;
        this.log.info("attaching Delete Artifact service to path={}", deleteArtifact);
        router.attach(deleteArtifact, DeleteArtifactResourceImpl.class);
        
        // Add a route for the Delete Artifact page.
        final String attachFileReference = PoddWebConstants.PATH_ATTACH_FILE_REF;
        this.log.info("attaching File Reference Attach service to path={}", attachFileReference);
        router.attach(attachFileReference, FileReferenceAttachResourceImpl.class);
        
        // Add a route for Logout service
        // final String logout = "logout";
        // PropertyUtils.getProperty(PropertyUtils.PROPERTY_LOGOUT_FORM_PATH,
        // PropertyUtils.DEFAULT_LOGOUT_FORM_PATH);
        // this.log.info("attaching logout service to path={}", logout);
        // FIXME: Switch between the logout resource implementations here based on the authenticator
        // router.attach(logout, CookieLogoutResourceImpl.class);
        
        this.log.info("routes={}", router.getRoutes().toString());
        
        // put the authenticator in front of the resource router so it can handle challenge
        // responses and forward them on to the right location after locking in the authentication
        // data. Authentication of individual methods on individual resources is handled using calls
        // to PoddWebServiceApplication.authenticate()
        authenticator.setNext(router);
        
        final CrossOriginResourceSharingFilter corsFilter = new CrossOriginResourceSharingFilter();
        corsFilter.setNext(authenticator);
        
        return corsFilter;
    }
    
    /**
     * Fetches a ChallengeAuthenticator based on the key defined in
     * PropertyUtils.PROPERTY_CHALLENGE_AUTH_METHOD.
     * 
     * Currently defaults to a DigestAuthenticator.
     * 
     * @return A ChallengeAuthenticator that can be used to challenge unauthenticated requests to
     *         resources that need authenticated access.
     */
    @Override
    public ChallengeAuthenticator getAuthenticator()
    {
        return this.auth;
    }
    
    @Override
    public PoddArtifactManager getPoddArtifactManager()
    {
        return this.poddArtifactManager;
    }
    
    @Override
    public PoddRepositoryManager getPoddRepositoryManager()
    {
        return this.poddRepositoryManager;
    }
    
    @Override
    public PoddSchemaManager getPoddSchemaManager()
    {
        return this.poddSchemaManager;
    }
    
    @Override
    public PoddSesameRealm getRealm()
    {
        return this.realm;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * net.maenad.oas.webservice.impl.OasWebServiceApplicationInterface#getTemplateConfiguration()
     */
    @Override
    public Configuration getTemplateConfiguration()
    {
        return this.freemarkerConfiguration;
    }
    
    /**
     * @param auth
     *            the auth to set
     */
    @Override
    public void setAuthenticator(final ChallengeAuthenticator auth)
    {
        this.auth = auth;
    }
    
    @Override
    public void setRealm(final RestletUtilSesameRealm nextRealm)
    {
        if(nextRealm instanceof PoddSesameRealm)
        {
            this.realm = (PoddSesameRealm)nextRealm;
        }
        else
        {
            throw new IllegalArgumentException("We only know how to handle PoddSesameRealm");
        }
    }
    
    @Override
    public void setTemplateConfiguration(final Configuration nextFreemarkerConfiguration)
    {
        this.freemarkerConfiguration = nextFreemarkerConfiguration;
    }
    
    /**
     * Initialize all Managers used by PODD.
     */
    private void initializePoddManagers()
    {
        final PoddFileReferenceProcessorFactoryRegistry nextFileRegistry =
                new PoddFileReferenceProcessorFactoryRegistry();
        // clear any automatically added entries that may come from META-INF/services entries on the
        // classpath
        nextFileRegistry.clear();
        
        final PoddPurlProcessorFactoryRegistry nextPurlRegistry = new PoddPurlProcessorFactoryRegistry();
        nextPurlRegistry.clear();
        final PoddPurlProcessorFactory nextPurlProcessorFactory = new UUIDPurlProcessorFactoryImpl();
        nextPurlRegistry.add(nextPurlProcessorFactory);
        
        final PoddFileReferenceManager nextFileReferenceManager = new PoddFileReferenceManagerImpl();
        nextFileReferenceManager.setProcessorFactoryRegistry(nextFileRegistry);
        
        final PoddPurlManager nextPurlManager = new PoddPurlManagerImpl();
        nextPurlManager.setPurlProcessorFactoryRegistry(nextPurlRegistry);
        
        final PoddOWLManager nextOWLManager = new PoddOWLManagerImpl();
        nextOWLManager.setReasonerFactory(OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet"));
        final OWLOntologyManager nextOWLOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        if(nextOWLOntologyManager == null)
        {
            this.log.error("OWLOntologyManager was null");
        }
        nextOWLManager.setOWLOntologyManager(nextOWLOntologyManager);
        
        this.poddRepositoryManager = new PoddRepositoryManagerImpl(this.nextRepository);
        this.poddRepositoryManager.setSchemaManagementGraph(PoddWebServiceApplicationImpl.SCHEMA_MGT_GRAPH);
        this.poddRepositoryManager.setArtifactManagementGraph(PoddWebServiceApplicationImpl.ARTIFACT_MGT_GRAPH);
        
        this.poddSchemaManager = new PoddSchemaManagerImpl();
        this.poddSchemaManager.setOwlManager(nextOWLManager);
        this.poddSchemaManager.setRepositoryManager(this.poddRepositoryManager);
        
        final PoddSesameManager poddSesameManager = new PoddSesameManagerImpl();
        
        this.poddArtifactManager = new PoddArtifactManagerImpl();
        this.poddArtifactManager.setRepositoryManager(this.poddRepositoryManager);
        this.poddArtifactManager.setFileReferenceManager(nextFileReferenceManager);
        this.poddArtifactManager.setPurlManager(nextPurlManager);
        this.poddArtifactManager.setOwlManager(nextOWLManager);
        this.poddArtifactManager.setSchemaManager(this.poddSchemaManager);
        this.poddArtifactManager.setSesameManager(poddSesameManager);
        
        /*
         * Since the schema ontology upload feature is not yet supported, necessary schemas are
         * uploaded here at application starts up.
         */
        try
        {
            this.getPoddSchemaManager().uploadSchemaOntology(
                    this.getClass().getResourceAsStream(PoddWebConstants.PATH_PODD_BASE), RDFFormat.RDFXML);
            this.getPoddSchemaManager().uploadSchemaOntology(
                    this.getClass().getResourceAsStream(PoddWebConstants.PATH_PODD_SCIENCE), RDFFormat.RDFXML);
            this.getPoddSchemaManager().uploadSchemaOntology(
                    this.getClass().getResourceAsStream(PoddWebConstants.PATH_PODD_PLANT), RDFFormat.RDFXML);
        }
        catch(IOException | OpenRDFException | OWLException | PoddException e)
        {
            this.log.error("Fatal Error!!! Could not load schema ontologies", e);
        }
    }
    
    /**
     * Call this method to clean up resources used by PODD. At present it shuts down the Repository.
     */
    public void cleanUpResources()
    {
        // clear all resources and shut down PODD
        if(this.nextRepository != null)
        {
            try
            {
                this.nextRepository.shutDown();
            }
            catch(final RepositoryException e)
            {
                this.log.error("Test repository could not be shutdown", e);
            }
        }
        this.nextRepository = null;
    }
    
    @Override
    public void stop() throws Exception
    {
        super.stop();
        this.cleanUpResources();
        this.log.info("== Shutting down PODD Web Application ==");
        System.out.println("===== Shutting down PODD =====");
    }
    
}
