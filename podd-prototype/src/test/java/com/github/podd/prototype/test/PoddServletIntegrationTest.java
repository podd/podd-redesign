package com.github.podd.prototype.test;

import java.io.ByteArrayInputStream;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.util.Series;

import com.github.podd.prototype.PoddServletContextListener;

/**
 * Integration test for PODD Servlet.
 * 
 * @author kutila
 * 
 */
public class PoddServletIntegrationTest extends AbstractPoddIntegrationTest
{
    // set of cookies containing session information
    private Series<Cookie> cookies = null;
    
    // Restlet Client to access the PODD web service
    private Client client = null;
    
    /**
     * Mimic a typical browser accept header, which will have * / * and we need to be able to cope
     * with that.
     */
    @Override
    protected String getTestAcceptHeader()
    {
        return "text/html, text/javascript, application/javascript, text/css, */*";
    }
    
    @Override
    protected void login(final String username, final String password)
    {
        final Request request = new Request(Method.POST, this.BASE_URL + "/login");
        final Form form = new Form();
        form.add("username", username);
        form.add("password", password);
        request.setEntity(form.getWebRepresentation(CharacterSet.UTF_8));
        
        final Response response = this.getClient().handle(request);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), response.getStatus().getCode());
        
        // -- session cookie for subsequent requests
        this.cookies = new Series<Cookie>(Cookie.class);
        final Series<CookieSetting> cookieSettingSeries = response.getCookieSettings();
        for(final CookieSetting cookieSetting : cookieSettingSeries)
        {
            this.cookies.add(cookieSetting);
        }
        this.stopClient();
    }
    
    @Override
    protected void logout()
    {
        final Request logoutRequest = new Request(Method.GET, this.BASE_URL + "/login");
        logoutRequest.setCookies(this.cookies);
        logoutRequest.getResourceRef().addQueryParameter("logout", "true");
        this.getClient().handle(logoutRequest);
        // Note: not asserting response code which depends on whether there was an active session
    }
    
    /**
     * Test the home page exists to ensure the web-app is deployed
     */
    @Test
    public void testHomePage() throws Exception
    {
        final Request request = new Request(Method.GET, this.BASE_URL + "/index.html");
        final Response response = this.getClient().handle(request);
        
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), response.getStatus().getCode());
        Assert.assertTrue(response.getEntityAsText().contains("PODD Prototype Web Service"));
    }
    
    @Test
    public void testLogin() throws Exception
    {
        this.login(AbstractPoddIntegrationTest.TEST_USERNAME, AbstractPoddIntegrationTest.TEST_PASSWORD);
        
        // -- logout without sending cookie: fails with Status 401 (UNAUTHORIZED)
        final Request incorrectLogoutRequest = new Request(Method.GET, this.BASE_URL + "/login");
        incorrectLogoutRequest.getResourceRef().addQueryParameter("logout", "true");
        final Response incorrectLogoutResponse = this.getClient().handle(incorrectLogoutRequest);
        Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED.getCode(), incorrectLogoutResponse.getStatus().getCode());
        
        // -- logout: succeeds
        final Request logoutRequest = new Request(Method.GET, this.BASE_URL + "/login");
        logoutRequest.setCookies(this.cookies);
        logoutRequest.getResourceRef().addQueryParameter("logout", "true");
        final Response logoutResponse = this.getClient().handle(logoutRequest);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), logoutResponse.getStatus().getCode());
    }
    
    @Test
    public void testSimpleAddArtifact() throws Exception
    {
        this.login(AbstractPoddIntegrationTest.TEST_USERNAME, AbstractPoddIntegrationTest.TEST_PASSWORD);
        final String path =
                System.getProperty(PoddServletContextListener.PODD_HOME)
                        + "/../test-classes/test/artifacts/basicProject-1.rdf";
        
        this.addArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
    }
    
    @Test
    public void testGetArtifact() throws Exception
    {
        this.login(AbstractPoddIntegrationTest.TEST_USERNAME, AbstractPoddIntegrationTest.TEST_PASSWORD);
        final String path =
                System.getProperty(PoddServletContextListener.PODD_HOME)
                        + "/../test-classes/test/artifacts/basicProject-1.rdf";
        
        final Response response = this.addArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
        
        final RepositoryConnection testRepoConnection = this.getMemoryRepositoryConnection();
        testRepoConnection.add(new ByteArrayInputStream(response.getEntityAsText().getBytes()), "", RDFFormat.RDFXML);
        Assert.assertEquals(29, testRepoConnection.size());
        
        // INCOMPLETE HERE. IN PROGRESS
        
        final String artifactUri = "http/purl.org/example/artifact:11";
        
        final Request getRequest = new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
        getRequest.setCookies(this.cookies);
        final Response getResponse = this.getClient().handle(getRequest);
        System.out.println(getResponse.getStatus().getCode());
        
    }
    
    @Ignore
    @Test
    public void testEditArtifact() throws Exception
    {
        // TODO
    }
    
    @Ignore
    @Test
    public void testDeleteArtifact() throws Exception
    {
        // TODO
    }
    
    @Test
    public void testReset() throws Exception
    {
        this.login(AbstractPoddIntegrationTest.TEST_USERNAME, AbstractPoddIntegrationTest.TEST_PASSWORD);
        
        // -- send RESET request
        final Request logoutRequest = new Request(Method.POST, this.BASE_URL + "/podd/reset");
        logoutRequest.setCookies(this.cookies);
        final Response logoutResponse = this.getClient().handle(logoutRequest);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), logoutResponse.getStatus().getCode());
        
        // -- TODO: verify RDF store is empty
    }
    
    // ----- helper methods -----
    
    protected RepositoryConnection getMemoryRepositoryConnection() throws Exception
    {
        final Repository repository = new SailRepository(new MemoryStore());
        repository.initialize();
        
        final RepositoryConnection repositoryConnection = repository.getConnection();
        repositoryConnection.setAutoCommit(false);
        return repositoryConnection;
    }
    
    /**
     * Helper method to add an artifact. This method expects that a valid session exists.
     * 
     * @param path
     *            Location of the artifact file
     * @param mediaType
     * @param expectedStatusCode
     * @return
     * @throws Exception
     */
    protected Response addArtifact(final String path, final MediaType mediaType, final int expectedStatusCode)
        throws Exception
    {
        final Request addRequest = new Request(Method.POST, this.BASE_URL + "/podd/artifact/new");
        addRequest.setCookies(this.cookies);
        final FileRepresentation artifactToAdd = new FileRepresentation(path, mediaType);
        addRequest.setEntity(artifactToAdd);
        final Response addResponse = this.getClient().handle(addRequest);
        Assert.assertEquals(expectedStatusCode, addResponse.getStatus().getCode());
        return addResponse;
    }
    
    protected Client getClient()
    {
        if(this.client == null)
        {
            this.client = new Client(Protocol.HTTP);
        }
        return this.client;
    }
    
    protected void stopClient()
    {
        try
        {
            this.client.stop();
        }
        catch(final Exception e)
        {
            e.printStackTrace();
        }
        
    }
    
}
