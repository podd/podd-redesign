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
package com.github.podd.resources.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CharacterSet;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilMediaType;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.junit.ext.TimeoutWithStackTraces;
import com.github.podd.restlet.ApplicationUtils;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.PoddWebServiceApplicationImpl;
import com.github.podd.test.TestUtils;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

/**
 * Abstract test implementation that contains common components required by resource implementation
 * tests, including setting up the application and component, along with the TEST_PORT number to use
 * in the tests.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class AbstractResourceImplTest
{
    private static synchronized int getFreePort()
    {
        int result = -1;
        while(result <= 0)
        {
            try (ServerSocket ss = new ServerSocket(0))
            {
                ss.setReuseAddress(true);
                result = ss.getLocalPort();
                if(AbstractResourceImplTest.usedPorts.contains(result))
                {
                    result = -1;
                }
                else
                {
                    AbstractResourceImplTest.usedPorts.add(result);
                    try (DatagramSocket ds = new DatagramSocket(result);)
                    {
                        ds.setReuseAddress(true);
                    }
                }
            }
            catch(final IOException e)
            {
                result = -1;
            }
        }
        return result;
    }
    
    protected static void setupThreading(final Context nextContext)
    {
        if(nextContext != null)
        {
            nextContext.getParameters().add("maxThreads", "256");
            nextContext.getParameters().add("minThreads", "4");
            nextContext.getParameters().add("lowThreads", "145");
            nextContext.getParameters().add("maxQueued", "100");
            nextContext.getParameters().add("maxTotalConnections", "100");
            // nextContext.getParameters().add("maxIoIdleTimeMs", "10000");
        }
    }
    
    @Rule
    public TemporaryFolder tempDirectory = new TemporaryFolder();
    
    /**
     * Timeout tests after 60 seconds.
     */
    @Rule
    public TimeoutWithStackTraces timeout = new TimeoutWithStackTraces(60000);
    
    /**
     * The set of ports that have been used in tests so far in this virtual machine.
     */
    private static final Set<Integer> usedPorts = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
    
    /**
     * Determines the TEST_PORT number to use for the test server
     */
    protected int testPort;
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    protected final ValueFactory vf = PODD.VF;
    
    /**
     * A constant used to make requests that require admin privileges easier to recognise inside
     * tests.
     */
    protected static final boolean WITH_ADMIN = true;
    
    /**
     * A constant used to make requests that do not require admin privileges easier to recognise
     * inside tests.
     */
    protected static final boolean NO_ADMIN = false;
    
    private Component component;
    
    protected Path testDir;
    
    /**
     * Store this for testing restarting the application for consistency and correctness.
     */
    private PoddWebServiceApplication poddApplication;
    
    private Series<CookieSetting> currentCookies = new Series<CookieSetting>(CookieSetting.class);
    
    public AbstractResourceImplTest()
    {
        super();
    }
    
    /**
     * Utility method to verify that freemarker has not encountered errors when generating output.
     * 
     * @param body
     *            Generated output in which to look for possible freemarker errors.
     */
    protected void assertFreemarker(final String body)
    {
        Assert.assertFalse("Freemarker error.", body.contains("Java backtrace for programmers:"));
        Assert.assertFalse("Freemarker error.", body.contains("freemarker.core."));
        Assert.assertFalse("Freemarker error.", body.contains("Could not generate page"));
    }
    
    /**
     * Utility method to verify that RDF documents can be parsed and the resulting number of
     * statements is as expected.
     * 
     * @param inputStream
     * @param format
     * @param expectedStatements
     * @return
     * @throws RDFParseException
     * @throws RDFHandlerException
     * @throws IOException
     */
    protected Model assertRdf(final InputStream inputStream, final RDFFormat format, final int expectedStatements)
        throws RDFParseException, RDFHandlerException, IOException
    {
        return this.assertRdf(new InputStreamReader(inputStream), format, expectedStatements);
    }
    
    /**
     * Utility method to verify that RDF documents can be parsed and the resulting number of
     * statements is as expected.
     * 
     * @param reader
     * @param format
     * @param expectedStatements
     * @return
     * @throws RDFParseException
     * @throws RDFHandlerException
     * @throws IOException
     */
    protected Model assertRdf(final Reader reader, final RDFFormat format, final int expectedStatements)
        throws RDFParseException, RDFHandlerException, IOException
    {
        final Model model = Rio.parse(reader, "http://test.podd.example.org/should/not/occur/in/a/real/graph/", format);
        
        if(expectedStatements != model.size())
        {
            System.out.println("Number of statements was not as expected found:" + model.size() + " expected:"
                    + expectedStatements);
            DebugUtils.printContents(model);
        }
        
        Assert.assertEquals("Unexpected number of statements", expectedStatements, model.size());
        
        return model;
    }
    
    protected Model assertRdf(final Representation representation, final RDFFormat format, final int expectedStatements)
        throws RDFParseException, RDFHandlerException, IOException
    {
        return this.assertRdf(new StringReader(this.getText(representation)), format, expectedStatements);
    }
    
    /**
     * Builds a {@link Representation} from a Resource.
     * 
     * @param resourcePath
     * @param mediaType
     * @return
     * @throws IOException
     */
    protected FileRepresentation buildRepresentationFromResource(final String resourcePath, final MediaType mediaType)
        throws IOException
    {
        final Path target =
                this.testDir.resolve(UUID.randomUUID().toString() + "." + Paths.get(resourcePath).getFileName());
        
        try (final InputStream input = this.getClass().getResourceAsStream(resourcePath))
        {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
        
        final FileRepresentation fileRep = new FileRepresentation(target.toFile(), mediaType);
        return fileRep;
    }
    
    public Representation doTestAuthenticatedRequest(final ClientResource clientResource, final Method requestMethod,
            final Representation inputRepresentation, final MediaType requestMediaType,
            final Status expectedResponseStatus, final boolean requiresAdminPrivileges) throws Exception
    {
        try
        {
            if(!this.isLoggedIn())
            {
                if(requiresAdminPrivileges)
                {
                    if(!this.login(RestletTestUtils.TEST_ADMIN_USERNAME, RestletTestUtils.TEST_ADMIN_PASSWORD))
                    {
                        Assert.fail("Failed to login as admin");
                    }
                }
                else
                {
                    if(!this.login(RestletTestUtils.TEST_USERNAME, RestletTestUtils.TEST_PASSWORD))
                    {
                        Assert.fail("Failed to login as normal user");
                    }
                }
            }
            
            Representation result = null;
            
            clientResource.getCookies().addAll(this.currentCookies);
            
            if(requestMethod.equals(Method.DELETE))
            {
                result = clientResource.delete(requestMediaType);
            }
            else if(requestMethod.equals(Method.PUT))
            {
                result = clientResource.put(inputRepresentation, requestMediaType);
            }
            else if(requestMethod.equals(Method.GET))
            {
                result = clientResource.get(requestMediaType);
            }
            else if(requestMethod.equals(Method.POST))
            {
                result = clientResource.post(inputRepresentation, requestMediaType);
            }
            else
            {
                throw new RuntimeException("Did not recognise request method: " + requestMethod.toString());
            }
            
            Assert.assertEquals(expectedResponseStatus.getCode(), clientResource.getResponse().getStatus().getCode());
            
            return result;
        }
        finally
        {
            this.logout();
        }
    }
    
    /**
     * Retrieves the asserted statements of a given artifact from the Server as a String.
     * 
     * @param artifactUri
     *            The URI of the artifact requested
     * @param mediaType
     *            The format in which statements should be retrieved
     * @return The artifact's asserted statements represented as a String
     * @throws Exception
     */
    protected Model getArtifactAsModel(final String artifactUri) throws Exception
    {
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            
            final Representation results =
                    this.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                            RestletUtilMediaType.APPLICATION_RDF_JSON, Status.SUCCESS_OK,
                            AbstractResourceImplTest.WITH_ADMIN);
            
            return this.getModel(results);
        }
        finally
        {
            this.releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Retrieves the asserted statements of a given artifact from the Server as a String.
     * 
     * @param artifactUri
     *            The URI of the artifact requested
     * @param mediaType
     *            The format in which statements should be retrieved
     * @return The artifact's asserted statements represented as a String
     * @throws Exception
     */
    protected String getArtifactAsString(final String artifactUri, final MediaType mediaType) throws Exception
    {
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            
            final Representation results =
                    this.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null, mediaType,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            return this.getText(results);
        }
        finally
        {
            this.releaseClient(getArtifactClientResource);
        }
    }
    
    protected Model getModel(final Representation representation) throws OpenRDFException,
        UnsupportedRDFormatException, IOException
    {
        try
        {
            return Rio.parse(representation.getStream(), "",
                    Rio.getParserFormatForMIMEType(representation.getMediaType().getName(), RDFFormat.RDFXML));
        }
        finally
        {
            try
            {
                representation.exhaust();
            }
            finally
            {
                representation.release();
            }
        }
    }
    
    protected final PoddWebServiceApplication getPoddApplication()
    {
        return this.poddApplication;
    }
    
    /**
     * Override this to change the test aliases for a given test.
     * 
     * @return A {@link Model} containing the statements relevant to test aliases.
     * @throws IOException
     * @throws UnsupportedRDFormatException
     * @throws RDFParseException
     */
    protected Model getTestAliases() throws RDFParseException, UnsupportedRDFormatException, IOException
    {
        final String configuration =
                IOUtils.toString(this.getClass().getResourceAsStream("/test/test-alias.ttl"), StandardCharsets.UTF_8);
        
        return Rio.parse(new StringReader(configuration), "", RDFFormat.TURTLE);
    }
    
    protected String getText(final Representation representation) throws IOException
    {
        final StringWriter result = new StringWriter();
        try
        {
            InputStream stream = representation.getStream();
            if(stream != null)
            {
                IOUtils.copy(stream, result, StandardCharsets.UTF_8);
            }
        }
        finally
        {
            try
            {
                representation.exhaust();
            }
            finally
            {
                representation.release();
            }
        }
        return result.toString();
    }
    
    /**
     * Returns the URI that can be used to access the given path.
     * 
     * @param path
     *            The path on the temporary test server to access. If the path does not start with a
     *            slash one will be added.
     * @return A full URI that can be used to dereference the given path on the test server.
     */
    protected String getUrl(final String path)
    {
        if(!path.startsWith("/"))
        {
            return "http://localhost:" + this.testPort + "/podd/" + path;
        }
        else
        {
            return "http://localhost:" + this.testPort + "/podd" + path;
        }
    }
    
    protected boolean isLoggedIn()
    {
        return !this.currentCookies.isEmpty();
    }
    
    /**
     * Loads a test artifact in RDF/XML format.
     * 
     * @param resourceName
     * @return The loaded artifact's URI
     * @throws Exception
     */
    protected String loadTestArtifact(final String resourceName) throws Exception
    {
        return this.loadTestArtifact(resourceName, MediaType.APPLICATION_RDF_XML).getOntologyIRI().toString();
    }
    
    /**
     * Loads a test artifact in a given format.
     * 
     * @param resourceName
     * @param mediaType
     *            Specifies the media type of the resource to load (e.g. RDF/XML, Turtle)
     * @return An InferredOWLOntologyID identifying the loaded artifact
     * @throws Exception
     */
    protected InferredOWLOntologyID loadTestArtifact(final String resourceName, final MediaType mediaType)
        throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        try
        {
            final Representation input = this.buildRepresentationFromResource(resourceName, mediaType);
            
            final Representation results =
                    this.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input,
                            MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            final String body = this.getText(results);
            // this.log.info(body);
            this.assertFreemarker(body);
            
            final Collection<InferredOWLOntologyID> ontologyIDs =
                    OntologyUtils.stringToOntologyID(body, RDFFormat.TURTLE);
            
            Assert.assertEquals("Should have got only 1 Ontology ID", 1, ontologyIDs.size());
            return ontologyIDs.iterator().next();
        }
        finally
        {
            this.releaseClient(uploadArtifactClientResource);
            
            this.logout();
        }
    }
    
    /**
     * Load a new test PoddUser. Does not check for presence of mandatory attributes.
     * 
     * @return A String representation of the unique URI assigned to the new User
     */
    protected String loadTestUser(final String testIdentifier, final String testPassword, final String testFirstName,
            final String testLastName, final String testEmail, final String testHomePage,
            final String testOrganization, final String testOrcid, final String testTitle, final String testPhone,
            final String testAddress, final String testPosition, final List<Map.Entry<URI, URI>> roles,
            final PoddUserStatus testStatus) throws Exception
    {
        // - create a Model of user
        final Model userInfoModel = new LinkedHashModel();
        final URI tempUserUri = PODD.VF.createURI("urn:temp:user");
        if(testIdentifier != null)
        {
            userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                    PODD.VF.createLiteral(testIdentifier));
        }
        if(testPassword != null)
        {
            userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERSECRET, PODD.VF.createLiteral(testPassword));
        }
        if(testFirstName != null)
        {
            userInfoModel
                    .add(tempUserUri, SesameRealmConstants.OAS_USERFIRSTNAME, PODD.VF.createLiteral(testFirstName));
        }
        if(testLastName != null)
        {
            userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERLASTNAME, PODD.VF.createLiteral(testLastName));
        }
        if(testHomePage != null)
        {
            userInfoModel.add(tempUserUri, PODD.PODD_USER_HOMEPAGE, PODD.VF.createURI(testHomePage));
        }
        if(testOrganization != null)
        {
            userInfoModel.add(tempUserUri, PODD.PODD_USER_ORGANIZATION, PODD.VF.createLiteral(testOrganization));
        }
        if(testOrcid != null)
        {
            userInfoModel.add(tempUserUri, PODD.PODD_USER_ORCID, PODD.VF.createLiteral(testOrcid));
        }
        if(testEmail != null)
        {
            userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USEREMAIL, PODD.VF.createLiteral(testEmail));
        }
        if(testTitle != null)
        {
            userInfoModel.add(tempUserUri, PODD.PODD_USER_TITLE, PODD.VF.createLiteral(testTitle));
        }
        if(testPhone != null)
        {
            userInfoModel.add(tempUserUri, PODD.PODD_USER_PHONE, PODD.VF.createLiteral(testPhone));
        }
        if(testAddress != null)
        {
            userInfoModel.add(tempUserUri, PODD.PODD_USER_ADDRESS, PODD.VF.createLiteral(testAddress));
        }
        if(testPosition != null)
        {
            userInfoModel.add(tempUserUri, PODD.PODD_USER_POSITION, PODD.VF.createLiteral(testPosition));
        }
        if(testStatus != null)
        {
            userInfoModel.add(tempUserUri, PODD.PODD_USER_STATUS, testStatus.getURI());
        }
        else
        {
            userInfoModel.add(tempUserUri, PODD.PODD_USER_STATUS, PoddUserStatus.INACTIVE.getURI());
        }
        
        // prepare: add Role Mappings
        for(final Map.Entry<URI, URI> entry : roles)
        {
            final URI role = entry.getKey();
            final URI mappedObject = entry.getValue();
            
            final URI roleMapping = PODD.VF.createURI("urn:podd:rolemapping:", UUID.randomUUID().toString());
            userInfoModel.add(roleMapping, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
            userInfoModel.add(roleMapping, SesameRealmConstants.OAS_ROLEMAPPEDUSER, tempUserUri);
            userInfoModel.add(roleMapping, SesameRealmConstants.OAS_ROLEMAPPEDROLE, role);
            if(mappedObject != null)
            {
                userInfoModel.add(roleMapping, PODD.PODD_ROLEMAPPEDOBJECT, mappedObject);
            }
        }
        
        // - request new user creation from User Add RDF Service
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final ClientResource userAddClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ADD));
        
        try
        {
            final StringWriter out = new StringWriter();
            Rio.write(userInfoModel, out, format);
            final Representation input = new StringRepresentation(out.toString(), mediaType);
            
            final Representation results =
                    this.doTestAuthenticatedRequest(userAddClientResource, Method.POST, input, mediaType,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            // verify: response has 1 statement and identifier is correct
            final Model model = this.assertRdf(results, RDFFormat.RDFXML, 1);
            Assert.assertEquals("Unexpected user identifier", testIdentifier,
                    model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
            
            // return the unique URI assigned to this User
            final Resource next =
                    model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects().iterator().next();
            return next.stringValue();
        }
        finally
        {
            this.releaseClient(userAddClientResource);
        }
    }
    
    protected boolean login(final String username, final char[] testAdminPassword) throws Exception
    {
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_LOGIN_SUBMIT));
        
        try
        {
            resource.getCookies().addAll(this.currentCookies);
            
            // TODO: when Cookies natively supported by Client Resource, or another method remove
            // this
            // Until then, this is necessary to manually attach the cookies after login to the
            // redirected address.
            // GitHub issue for this: https://github.com/restlet/restlet-framework-java/issues/21
            resource.setFollowingRedirects(false);
            
            final Form form = new Form();
            form.add("username", username);
            form.add("password", new String(testAdminPassword));
            
            final Representation rep = resource.post(form.getWebRepresentation(CharacterSet.UTF_8));
            
            try
            {
                this.log.info("login result status: {}", resource.getStatus());
                if(rep != null)
                {
                    // FIXME: Representation.getText may be implemented badly, so avoid calling it
                    // this.log.info("login result: {}", rep.getText());
                }
                else
                {
                    this.log.info("login result was null");
                }
                
                // HACK
                if(resource.getStatus().equals(Status.REDIRECTION_SEE_OTHER) || resource.getStatus().isSuccess())
                {
                    this.currentCookies = resource.getCookieSettings();
                }
                else
                {
                    this.log.error("Found unrecognised status after login: {}", resource.getStatus());
                }
                
                this.log.info("cookies: {}", this.currentCookies);
                
                boolean result = !this.currentCookies.isEmpty();
                
                this.log.info("Logged in=" + result);
                
                return result;
            }
            catch(final Throwable e)
            {
                this.currentCookies.clear();
                this.log.warn("Error with request", e);
                throw e;
            }
            finally
            {
                this.getText(rep);
            }
        }
        finally
        {
            this.releaseClient(resource);
        }
    }
    
    protected boolean logout() throws Exception
    {
        this.log.info("cookies: {}", this.currentCookies);
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_LOGOUT));
        try
        {
            // add the cookie settings so that the server knows who to logout
            resource.getCookies().addAll(this.currentCookies);
            
            // TODO: when Cookies natively supported by Client Resource, or another method remove
            // this
            // Until then, this is necessary to manually attach the cookies after login to the
            // redirected address.
            // GitHub issue for this: https://github.com/restlet/restlet-framework-java/issues/21
            resource.setFollowingRedirects(false);
            
            final Representation rep = resource.get();
            try
            {
                this.currentCookies = resource.getCookieSettings();
                
                this.log.info("logout result status: {}", resource.getStatus());
                
                if(rep != null)
                {
                    // FIXME: Representation.getText may be implemented badly, so avoid calling it
                    // this.log.info("logout result: {}", rep.getText());
                }
                else
                {
                    this.log.info("logout result was null");
                }
                
                this.log.info("cookies: {}", this.currentCookies);
                
                this.currentCookies.clear();
                
                return true;
            }
            catch(final Throwable e)
            {
                this.currentCookies.clear();
                this.log.warn("Error with request", e);
                throw e;
            }
            finally
            {
                this.getText(rep);
            }
        }
        finally
        {
            this.releaseClient(resource);
        }
    }
    
    /**
     * Maps the given User and Role with an optional object URI.
     * 
     * @param userIdentifier
     * @param poddRole
     * @param mappedObjectUri
     * @throws Exception
     */
    protected void mapUserToRole(final String userIdentifier, final PoddRoles poddRole, final String mappedObjectUri)
        throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final Model newModel = new LinkedHashModel();
        
        // prepare Model with additional Role mapping
        final URI roleMapping1Uri = PODD.VF.createURI("urn:podd:rolemapping1:", UUID.randomUUID().toString());
        newModel.add(roleMapping1Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping1Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, poddRole.getURI());
        if(mappedObjectUri != null)
        {
            newModel.add(roleMapping1Uri, PODD.PODD_ROLEMAPPEDOBJECT, PODD.VF.createURI(mappedObjectUri));
        }
        
        // submit modified details to User Roles Service
        final ClientResource userRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ROLES));
        try
        {
            userRolesClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, userIdentifier);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            Rio.write(newModel, out, format);
            final Representation input = new StringRepresentation(out.toString(), mediaType);
            final Representation modifiedResults =
                    this.doTestAuthenticatedRequest(userRolesClientResource, Method.POST, input, mediaType,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            final Model model = this.assertRdf(modifiedResults, RDFFormat.RDFXML, 1);
            Assert.assertEquals("Unexpected user identifier", userIdentifier,
                    model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        }
        finally
        {
            this.releaseClient(userRolesClientResource);
        }
    }
    
    protected void releaseClient(final ClientResource clientResource) throws Exception
    {
        if(clientResource != null && clientResource.getNext() != null && clientResource.getNext() instanceof Client)
        {
            final Client c = (Client)clientResource.getNext();
            c.stop();
        }
    }
    
    /**
     * Create a new server for each test.
     * 
     * State will only be shared when they use a common database.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.component = new Component();
        
        this.testPort = AbstractResourceImplTest.getFreePort();
        
        final Server httpServer =
                new Server(this.component.getContext().createChildContext(), Protocol.HTTP, this.testPort);
        AbstractResourceImplTest.setupThreading(httpServer.getContext());
        // Add a new HTTP server listening on the given TEST_PORT.
        this.component.getServers().add(httpServer);
        
        this.component.getClients().add(Protocol.CLAP);
        this.component.getClients().add(Protocol.HTTP);
        
        this.poddApplication = new PoddWebServiceApplicationImpl();
        
        // Attach the sample application.
        this.component.getDefaultHost().attach("/podd/", this.poddApplication);
        
        this.poddApplication.setDataRepositoryConfig(this.getTestAliases());
        
        // The application cannot be setup properly until it is attached, as it
        // requires Application.getContext() to not return null
        ApplicationUtils.setupApplication(this.poddApplication, this.poddApplication.getContext());
        TestUtils.setupTestUser(this.poddApplication);
        
        // Start the component.
        this.component.start();
        
        AbstractResourceImplTest.setupThreading(this.poddApplication.getContext());
        AbstractResourceImplTest.setupThreading(this.component.getContext());
        for(final Client nextClient : this.component.getClients())
        {
            AbstractResourceImplTest.setupThreading(nextClient.getContext());
        }
        
        this.testDir = this.tempDirectory.newFolder(this.getClass().getSimpleName()).toPath();
    }
    
    /**
     * Stop and nullify the test server object after each test.
     * 
     * NOTE: Does not clear any databases.
     * 
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception
    {
        // Stop the component
        if(this.component != null)
        {
            this.component.stop();
        }
        
        // nullify the reference to the component
        this.component = null;
    }
    
}