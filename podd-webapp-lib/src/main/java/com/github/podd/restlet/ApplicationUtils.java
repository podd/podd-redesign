/**
 * 
 */
package com.github.podd.restlet;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.ansell.restletutils.FixedRedirectCookieAuthenticator;
import com.github.ansell.restletutils.RestletUtilRoles;
import com.github.ansell.restletutils.RestletUtilSesameRealm;
import com.github.ansell.restletutils.RestletUtilUser;
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
        
        //FIXME: read from a property
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
//                    Boolean.valueOf(PropertyUtil.getProperty(OasProperties.PROPERTY_CHALLENGE_AUTH_OPTIONAL,
//                    OasProperties.DEFAULT_CHALLENGE_AUTH_OPTIONAL)));
        }
        else if(authMethod.equalsIgnoreCase("cookie"))
        {
            ApplicationUtils.log.info("Using cookie authenticator");
            
            // FIXME: Stub implementation
            final byte[] secretKey = "s3cr3t2345667123".getBytes(StandardCharsets.UTF_8);
            
            result = new FixedRedirectCookieAuthenticator(newChildContext, nextRealm.getName(), secretKey);
            
//            ((FixedRedirectCookieAuthenticator)result).setLoginFormPath("");
//                    PropertyUtil.getProperty(
//                    OasProperties.PROPERTY_LOGIN_FORM_PATH, OasProperties.DEFAULT_LOGIN_FORM_PATH));
            
            ((FixedRedirectCookieAuthenticator)result).setLoginPath(PoddWebConstants.PATH_LOGIN_SUBMIT);
//                    PropertyUtil.getProperty(
//                    OasProperties.PROPERTY_LOGIN_PATH, OasProperties.DEFAULT_LOGIN_PATH));
//            
            ((FixedRedirectCookieAuthenticator)result).setLogoutPath(PoddWebConstants.PATH_LOGOUT);
//                    PropertyUtil.getProperty(
//                    OasProperties.PROPERTY_LOGOUT_PATH, OasProperties.DEFAULT_LOGOUT_PATH));
//            
//            ((FixedRedirectCookieAuthenticator)result).setRedirectQueryName(PropertyUtil.getProperty(
//                    OasProperties.PROPERTY_LOGIN_REDIRECT_FIELD, OasProperties.DEFAULT_LOGIN_REDIRECT_FIELD));
            
            // FIXME: Make this configurable
            ((FixedRedirectCookieAuthenticator)result).setCookieName(PoddWebConstants.COOKIE_NAME);
            // FIXME: Make this configurable
            ((FixedRedirectCookieAuthenticator)result).setIdentifierFormName("username");
            // FIXME: Make this configurable
            ((FixedRedirectCookieAuthenticator)result).setSecretFormName("password");
            ((FixedRedirectCookieAuthenticator)result).setInterceptingLogin(true);
            ((FixedRedirectCookieAuthenticator)result).setInterceptingLogout(true);
            ((FixedRedirectCookieAuthenticator)result).setFixedRedirectUri(PoddWebConstants.PATH_REDIRECT_LOGGED_IN);
//                    PropertyUtil.getProperty(
//                    OasProperties.PROPERTY_ONTOLOGY_MANAGER_PATH, OasProperties.DEFAULT_ONTOLOGY_MANAGER_PATH));
            
            result.setMultiAuthenticating(false);
            
            result.setVerifier(nextRealm.getVerifier());
            result.setEnroler(nextRealm.getEnroler());
            result.setOptional(true);
//                    Boolean.valueOf(PropertyUtil.getProperty(OasProperties.PROPERTY_CHALLENGE_AUTH_OPTIONAL,
//                    OasProperties.DEFAULT_CHALLENGE_AUTH_OPTIONAL)));
            
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
        final String repositoryUrl = ""; //PropertyUtil.getProperty(OasProperties.PROPERTY_SESAME_URL, "");
        
        // if we weren't able to find a repository URL in the configuration, we setup an
        // in
        // memory store
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
                
                ApplicationUtils.log.info("Using sesame http repository as repository for PODD");
                
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
        // FIXME: Make this configurable
        result.setTemplateLoader(new ContextTemplateLoader(newChildContext, "clap://class/templates"));
        
        final BeansWrapper myWrapper = new BeansWrapper();
        myWrapper.setSimpleMapWrapper(true);
        result.setObjectWrapper(myWrapper);
        
        return result;
    }
    
    
    public static void setupApplication(final PoddWebServiceApplication application, final Context applicationContext)
    {
        ApplicationUtils.log.info("application {}", application);
        ApplicationUtils.log.info("applicationContext {}", applicationContext);
        
        final List<Role> roles = application.getRoles();
        roles.clear();
        roles.addAll(RestletUtilRoles.getRoles());
        
        
        Repository nextRepository = null;
        try
        {
            nextRepository = application.getPoddRepositoryManager().getRepository();
        }
        catch(final OpenRDFException e)
        {
            ApplicationUtils.log.error("Could not retrieve Repository from Application", e);
            // Throw exception up ??
        } 
        
        // FIXME: Stub implementation in memory, based on the example restlet MemoryRealm class,
        // need to create a realm implementation that backs onto a database for persistence
        
        // OasMemoryRealm has extensions so that getClientInfo().getUser() will contain first name,
        // last name, and email address as necessary
        // FIXME: Restlet MemoryRealm creates a DefaultVerifier class that is not compatible with
        // DigestAuthenticator.setWrappedVerifier
        final RestletUtilSesameRealm nextRealm =
                new RestletUtilSesameRealm(nextRepository,
                                PoddWebConstants.DEF_USER_MANAGEMENT_GRAPH);
        
        // FIXME: Make this configurable
        nextRealm.setName("PODDRealm");
        
        final RestletUtilUser testUser =
                new RestletUtilUser("testUser", "testPassword", "Test", "User", "test.user@example.com");
        final URI testUserUri = nextRealm.addUser(testUser);
        nextRealm.map(testUser, RestletUtilRoles.AUTHENTICATED.getRole());
        
        final RestletUtilUser testAdminUser =
                new RestletUtilUser("testAdminUser", "testAdminPassword", "Test Admin", "User",
                        "test.admin.user@example.com");
        final URI testAdminUserUri = nextRealm.addUser(testAdminUser);
        nextRealm.map(testAdminUser, RestletUtilRoles.ADMIN.getRole());
        
        final Set<Role> testAdminUserRoles = nextRealm.findRoles(testAdminUser);
        
        ApplicationUtils.log.info("testAdminUserRoles: {}", testAdminUserRoles);
        
        final User findUser = nextRealm.findUser("testAdminUser");
        
        ApplicationUtils.log.info("findUser: {}", findUser);
        ApplicationUtils.log.info("findUser.getFirstName: {}", findUser.getFirstName());
        ApplicationUtils.log.info("findUser.getLastName: {}", findUser.getLastName());
        ApplicationUtils.log.info("findUser.getName: {}", findUser.getName());
        ApplicationUtils.log.info("findUser.getIdentifier: {}", findUser.getIdentifier());
        
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
