package com.github.podd.restlet.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.restlet.Component;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.restlet.ApplicationUtils;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.PoddWebServiceApplicationImpl;
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
    /**
     * Determines the TEST_PORT number to use for the test server
     */
    protected static final int TEST_PORT = 8182;
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * A constant used to make requests that require admin privileges easier to recognise inside
     * tests.
     */
    protected final boolean testWithAdminPrivileges = true;
    
    /**
     * A constant used to make requests that do not require admin privileges easier to recognise
     * inside tests.
     */
    protected final boolean testNoAdminPrivileges = false;
    
    private Component component;
    
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
    public void assertFreemarker(final String body)
    {
        Assert.assertFalse("Freemarker error.", body.contains("Java backtrace for programmers:"));
        Assert.assertFalse("Freemarker error.", body.contains("freemarker.core."));
    }
    
    /**
     * Builds a {@link Representation} from a Resource.
     * 
     * @param resourcePath
     * @param mediaType
     * @return
     * @throws IOException
     */
    public Representation buildRepresentationFromResource(final String resourcePath, final MediaType mediaType)
        throws IOException
    {
        InputStream resourceAsStream = this.getClass().getResourceAsStream(resourcePath);
        Assert.assertNotNull("Null resource", resourceAsStream);
        final InputStream in = new BufferedInputStream(resourceAsStream);
        final String stringInput = IOUtils.toString(in);
        return new StringRepresentation(stringInput, mediaType);
    }
    
    /**
     * Returns the URI that can be used to access the given path.
     * 
     * @param path
     *            The path on the temporary test server to access. If the path does not start with a
     *            slash one will be added.
     * @return A full URI that can be used to dereference the given path on the test server.
     */
    public String getUrl(final String path)
    {
        if(!path.startsWith("/"))
        {
            return "http://localhost:" + AbstractResourceImplTest.TEST_PORT + "/podd/" + path;
        }
        else
        {
            return "http://localhost:" + AbstractResourceImplTest.TEST_PORT + "/podd" + path;
        }
    }
    
    /**
     * Loads a test artifact in RDF/XML format.
     * 
     * @param resourceName
     * @return The loaded artifact's URI
     * @throws Exception
     */
    public String loadTestArtifact(final String resourceName) throws Exception
    {
        return this.loadTestArtifact(resourceName, MediaType.APPLICATION_RDF_XML);
    }
    
    /**
     * Loads a test artifact in a given format.
     * 
     * @param resourceName
     * @param mediaType
     * @return The loaded artifact's URI
     * @throws Exception
     */
    public String loadTestArtifact(final String resourceName, MediaType mediaType) throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        final Representation input = this.buildRepresentationFromResource(resourceName, mediaType);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input,
                        MediaType.TEXT_PLAIN, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // verify: results (expecting the added artifact's ontology IRI)
        final String body = results.getText();
        Assert.assertTrue("Artifact URI should start with http", body.startsWith("http://"));
        Assert.assertFalse("Should have no references to HTML", body.contains("html"));
        Assert.assertFalse("Artifact URI should not contain newline character", body.contains("\n"));
        
        return body;
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
        
        // Add a new HTTP server listening on the given TEST_PORT.
        this.component.getServers().add(Protocol.HTTP, AbstractResourceImplTest.TEST_PORT);
        
        this.component.getClients().add(Protocol.CLAP);
        this.component.getClients().add(Protocol.HTTP);
        
        final PoddWebServiceApplication nextApplication = new PoddWebServiceApplicationImpl();
        
        // Attach the sample application.
        this.component.getDefaultHost().attach(
        // PropertyUtil.get(OasProps.PROP_WS_URI_PATH, OasProps.DEF_WS_URI_PATH),
                "/podd/", nextApplication);
        
        // The application cannot be setup properly until it is attached, as it requires
        // Application.getContext() to not return null
        ApplicationUtils.setupApplication(nextApplication, nextApplication.getContext());
        
        // Start the component.
        this.component.start();
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