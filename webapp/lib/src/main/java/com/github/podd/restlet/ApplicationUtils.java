/**
 * 
 */
package com.github.podd.restlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.restlet.Context;
import org.restlet.ext.crypto.DigestAuthenticator;
import org.restlet.ext.crypto.DigestVerifier;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.LocalVerifier;
import org.restlet.security.Realm;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.ansell.restletutils.FixedRedirectCookieAuthenticator;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.file.FileReferenceManager;
import com.github.podd.api.file.FileReferenceProcessorFactory;
import com.github.podd.api.file.FileReferenceProcessorFactoryRegistry;
import com.github.podd.api.file.PoddFileRepositoryManager;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
import com.github.podd.exception.PoddException;
import com.github.podd.impl.PoddArtifactManagerImpl;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddRepositoryManagerImpl;
import com.github.podd.impl.PoddSchemaManagerImpl;
import com.github.podd.impl.PoddSesameManagerImpl;
import com.github.podd.impl.file.FileReferenceManagerImpl;
import com.github.podd.impl.file.PoddFileRepositoryManagerImpl;
import com.github.podd.impl.file.SSHFileReferenceProcessorFactoryImpl;
import com.github.podd.impl.purl.PoddPurlManagerImpl;
import com.github.podd.impl.purl.UUIDPurlProcessorFactoryImpl;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class ApplicationUtils
{
    private static final Logger log = LoggerFactory.getLogger(ApplicationUtils.class);
    
    public static ChallengeAuthenticator getNewAuthenticator(final Realm nextRealm, final Context newChildContext)
    {
        ChallengeAuthenticator result = null;
        
        // FIXME: read from a property
        final String authMethod =
                PropertyUtil.get(PoddWebConstants.PROPERTY_CHALLENGE_AUTH_METHOD,
                        PoddWebConstants.DEF_CHALLENGE_AUTH_METHOD);
        
        if(authMethod.equalsIgnoreCase("digest"))
        {
            ApplicationUtils.log.info("Using digest authenticator");
            // FIXME: Stub implementation
            result = new DigestAuthenticator(newChildContext, nextRealm.getName(), "s3cret");
            
            if(nextRealm.getVerifier() instanceof DigestVerifier)
            {
                // NOTE: The verifier in this case must support digest verification by being an
                // instance of DigestVerifier
                result.setVerifier(nextRealm.getVerifier());
            }
            else if(nextRealm.getVerifier() instanceof LocalVerifier)
            {
                // else we need to map the verifier in
                ((DigestAuthenticator)result).setWrappedVerifier((LocalVerifier)nextRealm.getVerifier());
            }
            else
            {
                throw new RuntimeException("Verifier was not valid for use with DigestAuthenticator verifier="
                        + nextRealm.getVerifier().toString());
            }
            
            result.setEnroler(nextRealm.getEnroler());
            
            result.setOptional(true);
            // Boolean.valueOf(PropertyUtil.getProperty(OasProperties.PROPERTY_CHALLENGE_AUTH_OPTIONAL,
            // OasProperties.DEFAULT_CHALLENGE_AUTH_OPTIONAL)));
        }
        else if(authMethod.equalsIgnoreCase("cookie"))
        {
            ApplicationUtils.log.info("Using cookie authenticator");
            
            // FIXME: Stub implementation
            final byte[] secretKey = "s3cr3t2345667123".getBytes(StandardCharsets.UTF_8);
            
            result = new FixedRedirectCookieAuthenticator(newChildContext, nextRealm.getName(), secretKey);
            
            // ((FixedRedirectCookieAuthenticator)result).setLoginFormPath("");
            // PropertyUtil.getProperty(
            // OasProperties.PROPERTY_LOGIN_FORM_PATH, OasProperties.DEFAULT_LOGIN_FORM_PATH));
            
            ((FixedRedirectCookieAuthenticator)result).setLoginPath(PoddWebConstants.PATH_LOGIN_SUBMIT);
            // PropertyUtil.getProperty(
            // OasProperties.PROPERTY_LOGIN_PATH, OasProperties.DEFAULT_LOGIN_PATH));
            //
            ((FixedRedirectCookieAuthenticator)result).setLogoutPath(PoddWebConstants.PATH_LOGOUT);
            // PropertyUtil.getProperty(
            // OasProperties.PROPERTY_LOGOUT_PATH, OasProperties.DEFAULT_LOGOUT_PATH));
            //
            // ((FixedRedirectCookieAuthenticator)result).setRedirectQueryName(PropertyUtil.getProperty(
            // OasProperties.PROPERTY_LOGIN_REDIRECT_FIELD,
            // OasProperties.DEFAULT_LOGIN_REDIRECT_FIELD));
            
            // FIXME: Make this configurable
            ((FixedRedirectCookieAuthenticator)result).setCookieName(PoddWebConstants.COOKIE_NAME);
            // FIXME: Make this configurable
            ((FixedRedirectCookieAuthenticator)result).setIdentifierFormName("username");
            // FIXME: Make this configurable
            ((FixedRedirectCookieAuthenticator)result).setSecretFormName("password");
            ((FixedRedirectCookieAuthenticator)result).setInterceptingLogin(true);
            ((FixedRedirectCookieAuthenticator)result).setInterceptingLogout(true);
            ((FixedRedirectCookieAuthenticator)result).setFixedRedirectUri(PoddWebConstants.PATH_REDIRECT_LOGGED_IN);
            // PropertyUtil.getProperty(
            // OasProperties.PROPERTY_ONTOLOGY_MANAGER_PATH,
            // OasProperties.DEFAULT_ONTOLOGY_MANAGER_PATH));
            
            result.setMultiAuthenticating(false);
            
            result.setVerifier(nextRealm.getVerifier());
            result.setEnroler(nextRealm.getEnroler());
            result.setOptional(true);
            // Boolean.valueOf(PropertyUtil.getProperty(OasProperties.PROPERTY_CHALLENGE_AUTH_OPTIONAL,
            // OasProperties.DEFAULT_CHALLENGE_AUTH_OPTIONAL)));
            
        }
        else if(authMethod.equalsIgnoreCase("http"))
        {
            // FIXME: Implement a stub here
            ApplicationUtils.log.error("FIXME: Implement HTTP ChallengeAuthenticator authMethod={}", authMethod);
            throw new RuntimeException("FIXME: Implement HTTP ChallengeAuthenticator");
        }
        else
        {
            ApplicationUtils.log.error("Did not recognise ChallengeAuthenticator method authMethod={}", authMethod);
            throw new RuntimeException("Did not recognise ChallengeAuthenticator method");
        }
        
        return result;
    }
    
    public static Repository getNewRepository()
    {
        // FIXME: Enable this before deploying
        final String repositoryUrl = ""; // PropertyUtil.getProperty(OasProperties.PROPERTY_SESAME_URL,
                                         // "");
        
        // if we weren't able to find a repository URL in the configuration, we setup an
        // in-memory store
        if(repositoryUrl.trim().isEmpty())
        {
            final Repository repository = new SailRepository(new MemoryStore());
            
            try
            {
                repository.initialize();
                
                ApplicationUtils.log.info("Created an in memory store as repository for PODD");
                
                return repository;
            }
            catch(final RepositoryException ex)
            {
                throw new RuntimeException("Could not initialise Sesame In Memory repository");
            }
        }
        else
        {
            final Repository repository = new HTTPRepository(repositoryUrl);
            
            try
            {
                repository.initialize();
                
                ApplicationUtils.log.info("Using sesame http repository as repository for PODD: {}", repositoryUrl);
                
                return repository;
            }
            catch(final RepositoryException ex)
            {
                throw new RuntimeException("Could not initialise Sesame HTTP repository with URL=" + repositoryUrl);
            }
        }
    }
    
    public static Configuration getNewTemplateConfiguration(final Context newChildContext)
    {
        final Configuration result = new Configuration();
        result.setDefaultEncoding("UTF-8");
        result.setURLEscapingCharset("UTF-8");
        
        // FIXME: Make this configurable
        result.setTemplateLoader(new ContextTemplateLoader(newChildContext, "clap://class/templates"));
        
        final BeansWrapper myWrapper = new BeansWrapper();
        myWrapper.setSimpleMapWrapper(true);
        result.setObjectWrapper(myWrapper);
        
        return result;
    }
    
    public static void setupApplication(final PoddWebServiceApplication application, final Context applicationContext)
        throws OpenRDFException
    {
        ApplicationUtils.log.debug("application {}", application);
        ApplicationUtils.log.debug("applicationContext {}", applicationContext);
        
        final List<Role> roles = application.getRoles();
        roles.clear();
        roles.addAll(PoddRoles.getRoles());
        
        final Repository nextRepository = ApplicationUtils.getNewRepository();
        
        application.setPoddRepositoryManager(new PoddRepositoryManagerImpl(nextRepository));
        application.getPoddRepositoryManager().setSchemaManagementGraph(PoddWebServiceApplicationImpl.SCHEMA_MGT_GRAPH);
        application.getPoddRepositoryManager().setArtifactManagementGraph(
                PoddWebServiceApplicationImpl.ARTIFACT_MGT_GRAPH);
        
        // File Reference manager
        final FileReferenceProcessorFactoryRegistry nextFileRegistry = new FileReferenceProcessorFactoryRegistry();
        // clear any automatically added entries that may come from META-INF/services entries on the
        // classpath
        nextFileRegistry.clear();
        final FileReferenceProcessorFactory nextFileProcessorFactory = new SSHFileReferenceProcessorFactoryImpl();
        nextFileRegistry.add(nextFileProcessorFactory);
        
        // File Reference Manager
        final FileReferenceManager nextFileReferenceManager = new FileReferenceManagerImpl();
        nextFileReferenceManager.setProcessorFactoryRegistry(nextFileRegistry);
        
        // PURL manager
        final PoddPurlProcessorFactoryRegistry nextPurlRegistry = new PoddPurlProcessorFactoryRegistry();
        nextPurlRegistry.clear();
        final PoddPurlProcessorFactory nextPurlProcessorFactory = new UUIDPurlProcessorFactoryImpl();
        
        final String purlPrefix = PropertyUtil.get(PoddWebConstants.PROPERTY_PURL_PREFIX, null);
        ((UUIDPurlProcessorFactoryImpl)nextPurlProcessorFactory).setPrefix(purlPrefix);
        
        nextPurlRegistry.add(nextPurlProcessorFactory);
        
        final PoddPurlManager nextPurlManager = new PoddPurlManagerImpl();
        nextPurlManager.setPurlProcessorFactoryRegistry(nextPurlRegistry);
        
        final PoddOWLManager nextOWLManager = new PoddOWLManagerImpl();
        nextOWLManager.setReasonerFactory(OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet"));
        final OWLOntologyManager nextOWLOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        if(nextOWLOntologyManager == null)
        {
            ApplicationUtils.log.error("OWLOntologyManager was null");
        }
        nextOWLManager.setOWLOntologyManager(nextOWLOntologyManager);
        
        // File Repository Manager
        final PoddFileRepositoryManager nextFileRepositoryManager = new PoddFileRepositoryManagerImpl();
        nextFileRepositoryManager.setRepositoryManager(application.getPoddRepositoryManager());
        nextFileRepositoryManager.setOWLManager(nextOWLManager);
        try
        {
            final Model aliasConfiguration = application.getAliasesConfiguration();
            nextFileRepositoryManager.init(application.getAliasesConfiguration());
        }
        catch(PoddException | IOException e)
        {
            ApplicationUtils.log.error("Fatal Error!!! Could not initialize File Repository Manager", e);
        }
        
        final PoddSesameManager poddSesameManager = new PoddSesameManagerImpl();
        
        application.setPoddSchemaManager(new PoddSchemaManagerImpl());
        application.getPoddSchemaManager().setOwlManager(nextOWLManager);
        application.getPoddSchemaManager().setRepositoryManager(application.getPoddRepositoryManager());
        application.getPoddSchemaManager().setSesameManager(poddSesameManager);
        
        application.setPoddArtifactManager(new PoddArtifactManagerImpl());
        application.getPoddArtifactManager().setRepositoryManager(application.getPoddRepositoryManager());
        application.getPoddArtifactManager().setFileReferenceManager(nextFileReferenceManager);
        application.getPoddArtifactManager().setFileRepositoryManager(nextFileRepositoryManager);
        application.getPoddArtifactManager().setPurlManager(nextPurlManager);
        application.getPoddArtifactManager().setOwlManager(nextOWLManager);
        application.getPoddArtifactManager().setSchemaManager(application.getPoddSchemaManager());
        application.getPoddArtifactManager().setSesameManager(poddSesameManager);
        
        /*
         * Since the schema ontology upload feature is not yet supported, necessary schemas are
         * uploaded here at application starts up.
         */
        try
        {
            application.getPoddSchemaManager().uploadSchemaOntology(
                    ApplicationUtils.class.getResourceAsStream(PoddRdfConstants.PATH_PODD_DCTERMS), RDFFormat.RDFXML);
            application.getPoddSchemaManager().uploadSchemaOntology(
                    ApplicationUtils.class.getResourceAsStream(PoddRdfConstants.PATH_PODD_FOAF), RDFFormat.RDFXML);
            application.getPoddSchemaManager().uploadSchemaOntology(
                    ApplicationUtils.class.getResourceAsStream(PoddRdfConstants.PATH_PODD_USER), RDFFormat.RDFXML);
            application.getPoddSchemaManager().uploadSchemaOntology(
                    ApplicationUtils.class.getResourceAsStream(PoddRdfConstants.PATH_PODD_BASE), RDFFormat.RDFXML);
            application.getPoddSchemaManager().uploadSchemaOntology(
                    ApplicationUtils.class.getResourceAsStream(PoddRdfConstants.PATH_PODD_SCIENCE), RDFFormat.RDFXML);
            application.getPoddSchemaManager().uploadSchemaOntology(
                    ApplicationUtils.class.getResourceAsStream(PoddRdfConstants.PATH_PODD_PLANT), RDFFormat.RDFXML);
        }
        catch(IOException | OpenRDFException | OWLException | PoddException e)
        {
            ApplicationUtils.log.error("Fatal Error!!! Could not load schema ontologies", e);
        }
        
        // FIXME: Stub implementation in memory, based on the example restlet MemoryRealm class,
        // need to create a realm implementation that backs onto a database for persistence
        
        // OasMemoryRealm has extensions so that getClientInfo().getUser() will contain first name,
        // last name, and email address as necessary
        // FIXME: Restlet MemoryRealm creates a DefaultVerifier class that is not compatible with
        // DigestAuthenticator.setWrappedVerifier
        final PoddSesameRealmImpl nextRealm =
                new PoddSesameRealmImpl(nextRepository, PoddRdfConstants.DEF_USER_MANAGEMENT_GRAPH);
        
        // FIXME: Make this configurable
        nextRealm.setName("PODDRealm");
        
        final URI testUserHomePage = PoddRdfConstants.VF.createURI("http://www.example.com/testUser");
        final PoddUser testUser =
                new PoddUser("testUser", "testPassword".toCharArray(), "Test", "User", "test.user@example.com",
                        PoddUserStatus.ACTIVE, testUserHomePage, "CSIRO", "Orcid-Test-User");
        final URI testUserUri = nextRealm.addUser(testUser);
        nextRealm.map(testUser, PoddRoles.AUTHENTICATED.getRole());
        
        final URI testAdminUserHomePage = PoddRdfConstants.VF.createURI("http://www.example.com/testAdmin");
        final PoddUser testAdminUser =
                new PoddUser("testAdminUser", "testAdminPassword".toCharArray(), "Test Admin", "User",
                        "test.admin.user@example.com", PoddUserStatus.ACTIVE, testAdminUserHomePage, "UQ",
                        "Orcid-Test-Admin");
        final URI testAdminUserUri = nextRealm.addUser(testAdminUser);
        nextRealm.map(testAdminUser, PoddRoles.ADMIN.getRole());
        nextRealm.map(testAdminUser, PoddRoles.AUTHENTICATED.getRole());
        
        final URI testArtifactUri =
                PoddRdfConstants.VF.createURI("http://purl.org/podd/ns/artifact/artifact89");
        nextRealm.map(testAdminUser, PoddRoles.PROJECT_ADMIN.getRole(), testArtifactUri);
        
        final Set<Role> testAdminUserRoles = nextRealm.findRoles(testAdminUser);
        
        ApplicationUtils.log.debug("testAdminUserRoles: {}, {}", testAdminUserRoles, testAdminUserRoles.size());
        
        final User findUser = nextRealm.findUser("testAdminUser");
        
        ApplicationUtils.log.debug("findUser: {}", findUser);
        ApplicationUtils.log.debug("findUser.getFirstName: {}", findUser.getFirstName());
        ApplicationUtils.log.debug("findUser.getLastName: {}", findUser.getLastName());
        ApplicationUtils.log.debug("findUser.getName: {}", findUser.getName());
        ApplicationUtils.log.debug("findUser.getIdentifier: {}", findUser.getIdentifier());
        
        // TODO: Define groups here also
        
        // final MapVerifier verifier = new MapVerifier();
        // final ConcurrentHashMap<String, char[]> hardcodedLocalSecrets = new
        // ConcurrentHashMap<String, char[]>();
        // hardcodedLocalSecrets.put("testUser", "testPassword".toCharArray());
        // verifier.setLocalSecrets(hardcodedLocalSecrets);
        
        // final Context authenticatorChildContext = applicationContext.createChildContext();
        final ChallengeAuthenticator newAuthenticator =
                ApplicationUtils.getNewAuthenticator(nextRealm, applicationContext);
        application.setAuthenticator(newAuthenticator);
        
        application.setRealm(nextRealm);
        
        // TODO: Is this necessary?
        // FIXME: Is this safe?
        // applicationContext.setDefaultVerifier(newAuthenticator.getVerifier());
        // applicationContext.setDefaultEnroler(newAuthenticator.getEnroler());
        
        // applicationContext.setDefaultVerifier(nextRealm.getVerifier());
        // applicationContext.setDefaultEnroler(nextRealm.getEnroler());
        
        // final Context templateChildContext = applicationContext.createChildContext();
        final Configuration newTemplateConfiguration = ApplicationUtils.getNewTemplateConfiguration(applicationContext);
        application.setTemplateConfiguration(newTemplateConfiguration);
        
        // create a custom error handler using our overridden PoddStatusService together with the
        // freemarker configuration
        final PoddStatusService statusService = new PoddStatusService(newTemplateConfiguration);
        application.setStatusService(statusService);
    }
    
    private ApplicationUtils()
    {
    }
    
}
