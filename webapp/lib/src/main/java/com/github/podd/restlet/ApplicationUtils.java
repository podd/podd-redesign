/**
 * PODD is an OWL ontology database used for scientific project management
 * 
 * Copyright (C) 2009-2013 The University Of Queensland
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.podd.restlet;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import info.aduna.iteration.Iterations;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryImplConfigBase;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.sail.memory.MemoryStore;
import org.restlet.Context;
import org.restlet.ext.crypto.DigestAuthenticator;
import org.restlet.ext.crypto.DigestVerifier;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.LocalVerifier;
import org.restlet.security.Realm;
import org.restlet.security.Role;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.ansell.restletutils.FixedRedirectCookieAuthenticator;
import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.file.DataReferenceManager;
import com.github.podd.api.file.DataReferenceProcessorFactory;
import com.github.podd.api.file.DataReferenceProcessorRegistry;
import com.github.podd.api.file.PoddDataRepositoryManager;
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
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class ApplicationUtils
{
    private static final Logger log = LoggerFactory.getLogger(ApplicationUtils.class);
    
    /**
     * @param application
     * @param nextRepository
     * @throws RepositoryException
     * @throws RDFHandlerException
     */
    private static void dumpSchemaGraph(final PoddWebServiceApplication application, final Repository nextRepository)
        throws RepositoryException, RDFHandlerException
    {
        RepositoryConnection conn = null;
        
        try
        {
            conn = nextRepository.getConnection();
            
            final Model model =
                    new LinkedHashModel(Iterations.asList(conn.getStatements(null, null, null, true, application
                            .getPoddRepositoryManager().getSchemaManagementGraph())));
            for(final Namespace nextNamespace : Iterations.asSet(conn.getNamespaces()))
            {
                model.setNamespace(nextNamespace);
            }
            Rio.write(model, System.out, RDFFormat.TURTLE);
        }
        finally
        {
            if(conn != null)
            {
                conn.close();
            }
        }
    }
    
    public static ChallengeAuthenticator getNewAuthenticator(final Realm nextRealm, final Context newChildContext,
            final PropertyUtil propertyUtil)
    {
        ChallengeAuthenticator result = null;
        
        // FIXME: read from a property
        final String authMethod =
                propertyUtil.get(PoddWebConstants.PROPERTY_CHALLENGE_AUTH_METHOD,
                        PoddWebConstants.DEF_CHALLENGE_AUTH_METHOD);
        
        if(authMethod.equalsIgnoreCase("digest"))
        {
            ApplicationUtils.log.info("Using digest authenticator");
            // FIXME: Stub implementation
            result = new DigestAuthenticator(newChildContext, nextRealm.getName(), "s3cret");
            
            if(nextRealm.getVerifier() instanceof DigestVerifier)
            {
                // NOTE: The verifier in this case must support digest
                // verification by being an
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
            
            ((FixedRedirectCookieAuthenticator)result).setLoginPath(PoddWebConstants.PATH_LOGIN_SUBMIT);
            ((FixedRedirectCookieAuthenticator)result).setLogoutPath(PoddWebConstants.PATH_LOGOUT);
            
            // FIXME: Make this configurable
            ((FixedRedirectCookieAuthenticator)result).setCookieName(PoddWebConstants.COOKIE_NAME);
            // FIXME: Make this configurable
            ((FixedRedirectCookieAuthenticator)result).setIdentifierFormName("username");
            // FIXME: Make this configurable
            ((FixedRedirectCookieAuthenticator)result).setSecretFormName("password");
            ((FixedRedirectCookieAuthenticator)result).setInterceptingLogin(true);
            ((FixedRedirectCookieAuthenticator)result).setInterceptingLogout(true);
            ((FixedRedirectCookieAuthenticator)result).setFixedRedirectUri(PoddWebConstants.PATH_REDIRECT_LOGGED_IN);
            
            result.setMultiAuthenticating(false);
            
            result.setVerifier(nextRealm.getVerifier());
            result.setEnroler(nextRealm.getEnroler());
            result.setOptional(true);
            
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
    
    public static Repository getNewManagementRepository(final PropertyUtil props) throws RepositoryException
    {
        final String repositoryUrl =
                props.get(PoddWebConstants.PROPERTY_MANAGEMENT_SESAME_LOCATION,
                        PoddWebConstants.DEFAULT_MANAGEMENT_SESAME_LOCATION);
        
        return ApplicationUtils.getNewRepositoryInternal(repositoryUrl);
    }
    
    private static Repository getNewRepositoryInternal(final String repositoryUrl) throws RepositoryException
    {
        // if we weren't able to find a repository URL in the configuration, we
        // setup an in-memory store
        if(repositoryUrl == null || repositoryUrl.trim().isEmpty())
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
                repository.shutDown();
                throw new RuntimeException("Could not initialise Sesame In Memory repository");
            }
        }
        else
        {
            final Repository repository = new HTTPRepository(repositoryUrl.trim());
            
            try
            {
                repository.initialize();
                
                ApplicationUtils.log.info("Using sesame http repository as repository for PODD: {}", repositoryUrl);
                
                return repository;
            }
            catch(final RepositoryException ex)
            {
                repository.shutDown();
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
        throws OpenRDFException, UnsupportedRDFormatException, IOException
    {
        final PropertyUtil props = application.getPropertyUtil();
        
        ApplicationUtils.log.debug("application {}", application);
        ApplicationUtils.log.debug("applicationContext {}", applicationContext);
        
        final List<Role> roles = application.getRoles();
        // FIXME: Why does the list need to be cleared here?
        roles.clear();
        roles.addAll(PoddRoles.getRoles());
        
        final Repository nextManagementRepository = ApplicationUtils.getNewManagementRepository(props);
        
        final String permanentRepositoryConfigPath =
                props.get(PoddWebConstants.PROPERTY_PERMANENT_SESAME_REPOSITORY_CONFIG,
                        PoddWebConstants.DEFAULT_PERMANENT_SESAME_REPOSITORY_CONFIG);
        final InputStream repositoryImplConfigStream =
                ApplicationUtils.class.getResourceAsStream(permanentRepositoryConfigPath);
        if(repositoryImplConfigStream == null)
        {
            ApplicationUtils.log.error("Could not find repository config");
        }
        final Model graph = Rio.parse(repositoryImplConfigStream, "", RDFFormat.TURTLE);
        final Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RepositoryConfigSchema.REPOSITORYTYPE, null);
        final RepositoryImplConfig repositoryImplConfig = RepositoryImplConfigBase.create(graph, repositoryNode);
        
        final String poddHome = props.get(PoddWebConstants.PROPERTY_PODD_HOME, "");
        final Path poddHomePath = Paths.get(poddHome);
        
        application.setPoddRepositoryManager(new PoddRepositoryManagerImpl(nextManagementRepository,
                repositoryImplConfig, props.get(PoddWebConstants.PROPERTY_PERMANENT_SESAME_REPOSITORY_SERVER,
                        PoddWebConstants.DEFAULT_PERMANENT_SESAME_REPOSITORY_SERVER), poddHomePath, props));
        
        // File Reference manager
        final DataReferenceProcessorRegistry nextFileRegistry = new DataReferenceProcessorRegistry();
        // FIXME: This clears any automatically added entries that come from META-INF/services
        // entries on the classpath
        nextFileRegistry.clear();
        final DataReferenceProcessorFactory nextFileProcessorFactory = new SSHFileReferenceProcessorFactoryImpl();
        nextFileRegistry.add(nextFileProcessorFactory);
        
        // File Reference Manager
        final DataReferenceManager nextDataReferenceManager = new FileReferenceManagerImpl();
        nextDataReferenceManager.setDataProcessorRegistry(nextFileRegistry);
        
        // TODO: Use the automatically loaded registry
        // PURL manager
        final PoddPurlProcessorFactoryRegistry nextPurlRegistry = new PoddPurlProcessorFactoryRegistry();
        nextPurlRegistry.clear();
        final PoddPurlProcessorFactory nextPurlProcessorFactory = new UUIDPurlProcessorFactoryImpl();
        
        final String purlPrefix = props.get(PoddWebConstants.PROPERTY_PURL_PREFIX, null);
        ((UUIDPurlProcessorFactoryImpl)nextPurlProcessorFactory).setPrefix(purlPrefix);
        
        nextPurlRegistry.add(nextPurlProcessorFactory);
        
        final PoddPurlManager nextPurlManager = new PoddPurlManagerImpl();
        nextPurlManager.setPurlProcessorFactoryRegistry(nextPurlRegistry);
        
        final Collection<OWLOntologyManagerFactory> ontologyManagers =
                OWLOntologyManagerFactoryRegistry.getInstance().get(
                        props.get(PoddWebConstants.PROPERTY_OWLAPI_MANAGER, PoddWebConstants.DEFAULT_OWLAPI_MANAGER));
        
        if(ontologyManagers == null || ontologyManagers.isEmpty())
        {
            ApplicationUtils.log.error("OWLOntologyManagerFactory was not found");
        }
        
        final OWLReasonerFactory reasonerFactory =
                OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet");
        if(reasonerFactory == null)
        {
            ApplicationUtils.log.error("OWLReasonerFactory was null");
        }
        
        final PoddOWLManager nextOWLManager =
                new PoddOWLManagerImpl(ontologyManagers.iterator().next(), reasonerFactory);
        
        // File Repository Manager
        final PoddDataRepositoryManager nextDataRepositoryManager = new PoddFileRepositoryManagerImpl();
        nextDataRepositoryManager.setRepositoryManager(application.getPoddRepositoryManager());
        nextDataRepositoryManager.setOWLManager(nextOWLManager);
        try
        {
            // TODO: Configure data repositories in a cleaner manner than this
            final Model aliasConfiguration = application.getDataRepositoryConfig();
            nextDataRepositoryManager.initialise(aliasConfiguration);
        }
        catch(PoddException | IOException e)
        {
            ApplicationUtils.log.error("Fatal Error!!! Could not initialize File Repository Manager", e);
        }
        
        application.setPoddDataRepositoryManager(nextDataRepositoryManager);
        
        final PoddSesameManager poddSesameManager = new PoddSesameManagerImpl();
        
        application.setPoddSchemaManager(new PoddSchemaManagerImpl());
        application.getPoddSchemaManager().setOwlManager(nextOWLManager);
        application.getPoddSchemaManager().setRepositoryManager(application.getPoddRepositoryManager());
        application.getPoddSchemaManager().setSesameManager(poddSesameManager);
        
        application.setPoddArtifactManager(new PoddArtifactManagerImpl());
        application.getPoddArtifactManager().setRepositoryManager(application.getPoddRepositoryManager());
        application.getPoddArtifactManager().setDataReferenceManager(nextDataReferenceManager);
        application.getPoddArtifactManager().setDataRepositoryManager(nextDataRepositoryManager);
        application.getPoddArtifactManager().setPurlManager(nextPurlManager);
        application.getPoddArtifactManager().setOwlManager(nextOWLManager);
        application.getPoddArtifactManager().setSchemaManager(application.getPoddSchemaManager());
        application.getPoddArtifactManager().setSesameManager(poddSesameManager);
        
        ApplicationUtils.setupSchemas(application);
        
        final PoddSesameRealm nextRealm =
                new PoddSesameRealm(nextManagementRepository, PODD.VF.createURI(props.get(
                        PODD.PROPERTY_USER_MANAGEMENT_GRAPH, PODD.DEFAULT_USER_MANAGEMENT_GRAPH.stringValue())));
        
        // FIXME: Make this configurable
        nextRealm.setName("PODDRealm");
        
        // Check if there is a current admin, and only add our test admin user if there is no admin
        // in the system
        boolean foundCurrentAdmin = false;
        for(final PoddUser nextUser : nextRealm.getUsers())
        {
            if(nextRealm.findRoles(nextUser).contains(PoddRoles.ADMIN.getRole()))
            {
                foundCurrentAdmin = true;
                break;
            }
        }
        
        if(!foundCurrentAdmin)
        {
            final URI testAdminUserHomePage = PODD.VF.createURI("http://www.example.com/testAdmin");
            final String username =
                    props.get(PoddWebConstants.PROPERTY_INITIAL_ADMIN_USERNAME,
                            PoddWebConstants.DEFAULT_INITIAL_ADMIN_USERNAME);
            final char[] password =
                    props.get(PoddWebConstants.PROPERTY_INITIAL_ADMIN_PASSWORD,
                            PoddWebConstants.DEFAULT_INITIAL_ADMIN_PASSWORD).toCharArray();
            final PoddUser testAdminUser =
                    new PoddUser(username, password, "Initial Admin", "User", "initial.admin.user@example.com",
                            PoddUserStatus.ACTIVE, testAdminUserHomePage, "Local Organisation", "Dummy-ORCID");
            final URI testAdminUserUri = nextRealm.addUser(testAdminUser);
            nextRealm.map(testAdminUser, PoddRoles.ADMIN.getRole());
            
            final Set<Role> testAdminUserRoles = nextRealm.findRoles(testAdminUser);
            
            ApplicationUtils.log.debug("testAdminUserRoles: {}, {}", testAdminUserRoles, testAdminUserRoles.size());
            
            // FIXME: Should put the application in maintenance mode at this point (when that is
            // supported), to require password/username change before opening up to other users
            
        }
        final ChallengeAuthenticator newAuthenticator =
                ApplicationUtils.getNewAuthenticator(nextRealm, applicationContext, props);
        application.setAuthenticator(newAuthenticator);
        
        application.setRealm(nextRealm);
        
        // Setup the Freemarker configuration
        final Configuration newTemplateConfiguration = ApplicationUtils.getNewTemplateConfiguration(applicationContext);
        application.setTemplateConfiguration(newTemplateConfiguration);
        
        // Create a custom error handler using our overridden PoddStatusService together with the
        // Freemarker configuration
        final PoddStatusService statusService = new PoddStatusService(newTemplateConfiguration);
        application.setStatusService(statusService);
    }
    
    /**
     * @param application
     * @param props
     * @throws RuntimeException
     * @throws UnsupportedRDFormatException
     */
    public static void setupSchemas(final PoddWebServiceApplication application) throws RuntimeException,
        UnsupportedRDFormatException
    {
        final PropertyUtil props = application.getPropertyUtil();
        final PoddSchemaManager poddSchemaManager = application.getPoddSchemaManager();
        final PoddArtifactManager poddArtifactManager = application.getPoddArtifactManager();
        
        /*
         * Since the schema ontology upload feature is not yet supported, necessary schemas are
         * uploaded here at application start up.
         */
        try
        {
            final String schemaManifest = props.get(PODD.KEY_SCHEMAS, PODD.PATH_DEFAULT_SCHEMAS);
            final RDFFormat format = Rio.getParserFormatForFileName(schemaManifest, RDFFormat.RDFXML);
            Model model = null;
            try (final InputStream schemaManifestStream = application.getClass().getResourceAsStream(schemaManifest);)
            {
                if(schemaManifestStream == null)
                {
                    throw new RuntimeException("Could not find the schema ontology manifest: " + schemaManifest);
                }
                model = Rio.parse(schemaManifestStream, "", format);
            }
            
            if(ApplicationUtils.log.isInfoEnabled())
            {
                ApplicationUtils.log.info("Schema manifest contents");
                DebugUtils.printContents(model);
            }
            ApplicationUtils.log.info("About to upload schema ontologies");
            
            // Returns an ordered list of the schema ontologies that were uploaded
            final List<InferredOWLOntologyID> schemaOntologies = poddSchemaManager.uploadSchemaOntologies(model);
            
            if(!schemaOntologies.isEmpty())
            {
                ApplicationUtils.log.info("Uploaded new schema ontologies: {}", schemaOntologies);
            }
            else
            {
                ApplicationUtils.log.info("No new schema ontologies uploaded this time");
            }
            
            // NOTE: The following is not ordered at this point in time
            // TODO: Do we gain anything from ordering this collection
            final Set<InferredOWLOntologyID> currentSchemaOntologies = poddSchemaManager.getCurrentSchemaOntologies();
            
            if(!currentSchemaOntologies.isEmpty())
            {
                ApplicationUtils.log.info("Existing current schema ontologies: {}", currentSchemaOntologies);
            }
            else
            {
                ApplicationUtils.log.info("Found no existing current schema ontologies");
            }
            
            final List<InferredOWLOntologyID> updatedCurrentSchemaOntologies = new ArrayList<>();
            
            for(final InferredOWLOntologyID nextSchemaOntology : schemaOntologies)
            {
                if(currentSchemaOntologies.contains(nextSchemaOntology))
                {
                    updatedCurrentSchemaOntologies.add(nextSchemaOntology);
                }
            }
            
            if(!updatedCurrentSchemaOntologies.isEmpty())
            {
                ApplicationUtils.log.info("Found new versions of existing schema ontologies: {}",
                        updatedCurrentSchemaOntologies);
            }
            
            // TODO: Offer one-time migration based on updatedCurrentSchemaOntologies
            // For now, we always attempt to update all artifacts to the current schema ontologies
            // Once the upgrade process is well developed, may want to streamline application
            // startup to avoid attempting to do this every time
            
            final ConcurrentMap<InferredOWLOntologyID, Set<? extends OWLOntologyID>> currentArtifactImports =
                    new ConcurrentHashMap<>();
            
            final ConcurrentMap<InferredOWLOntologyID, Set<InferredOWLOntologyID>> artifactsToUpdate =
                    new ConcurrentHashMap<>();
            
            final List<InferredOWLOntologyID> unpublishedArtifacts = poddArtifactManager.listUnpublishedArtifacts();
            
            ApplicationUtils.log.info("Existing unpublished artifacts: \n{}", unpublishedArtifacts);
            
            for(final InferredOWLOntologyID nextArtifact : unpublishedArtifacts)
            {
                if(poddArtifactManager.isPublished(nextArtifact))
                {
                    ApplicationUtils.log.info("Not attempting to update ontologies for published artifact: {}",
                            nextArtifact.getOntologyIRI());
                    // Must not update the schema imports for published artifacts
                    continue;
                }
                
                ApplicationUtils.log.info("Fetching schema imports for unpublished artifact: {}",
                        nextArtifact.getOntologyIRI());
                
                final Set<? extends OWLOntologyID> schemaImports = poddArtifactManager.getSchemaImports(nextArtifact);
                
                // Cache the current artifact imports so they are easily accessible without calling
                // the above method again if they need to be updated
                currentArtifactImports.put(nextArtifact, schemaImports);
                
                for(final InferredOWLOntologyID nextUpdatedSchemaImport : currentSchemaOntologies)
                {
                    boolean foundNonCurrentVersion = false;
                    OWLOntologyID matchingSchema = null;
                    
                    for(final OWLOntologyID nextSchemaImport : schemaImports)
                    {
                        // If the ontology IRI of the artifacts schema import was in the updated
                        // list, then signal it for updating
                        if(nextUpdatedSchemaImport.getOntologyIRI().equals(nextSchemaImport.getOntologyIRI())
                                && !nextUpdatedSchemaImport.getVersionIRI().equals(nextSchemaImport.getVersionIRI()))
                        {
                            foundNonCurrentVersion = true;
                        }
                        
                        if(nextUpdatedSchemaImport.getOntologyIRI().equals(nextSchemaImport.getOntologyIRI()))
                        {
                            matchingSchema = nextSchemaImport;
                        }
                    }
                    
                    // If there is a new schema, or they import an old version of one of the
                    // schemas, then import all of the current schemas
                    // FIXME: Naive strategy to ensure that we get all of the imports that
                    // users expect is to add all of the current schema ontologies here
                    // Need to customise strategies for users here, or in the GUI to select the
                    // schema ontologies that they wish to use for each artifact
                    if(foundNonCurrentVersion || matchingSchema == null)
                    {
                        ApplicationUtils.log.info("Found out of date or missing schema version: {}",
                                nextUpdatedSchemaImport);
                        Set<InferredOWLOntologyID> set = new HashSet<>();
                        final Set<InferredOWLOntologyID> putIfAbsent = artifactsToUpdate.putIfAbsent(nextArtifact, set);
                        if(putIfAbsent != null)
                        {
                            set = putIfAbsent;
                        }
                        set.addAll(currentSchemaOntologies);
                        // Do not continue this loop in this naive strategy
                        break;
                    }
                }
                
            }
            
            ApplicationUtils.log.info("Unpublished artifacts requiring schema updates: \n{}", artifactsToUpdate);
            
            for(final Entry<InferredOWLOntologyID, Set<InferredOWLOntologyID>> nextEntry : artifactsToUpdate.entrySet())
            {
                // FIXME: Naive strategy is to fail for each import
                // If/When we support linked artifacts, this logic will need to be improved to
                // ensure that both parents and dependencies are not updated if any of them are not
                // consistent with the new schema versions
                try
                {
                    final InferredOWLOntologyID nextArtifactToUpdate = nextEntry.getKey();
                    ApplicationUtils.log.info("About to update schema imports for: {}", nextArtifactToUpdate);
                    poddArtifactManager.updateSchemaImports(nextArtifactToUpdate,
                            currentArtifactImports.get(nextArtifactToUpdate), nextEntry.getValue());
                    ApplicationUtils.log.info("Completed updating schema imports for: {}", nextArtifactToUpdate);
                }
                catch(final Throwable e)
                {
                    ApplicationUtils.log.error("Could not update schema imports automatically due to exception: ", e);
                }
            }
            // Enable the following for debugging
            // dumpSchemaGraph(application, nextRepository);
            
        }
        catch(IOException | OpenRDFException | OWLException | PoddException e)
        {
            ApplicationUtils.log.error("Fatal Error!!! Could not load schema ontologies", e);
        }
    }
    
    private ApplicationUtils()
    {
    }
    
}
