package com.github.podd.prototype.test;

import java.io.ByteArrayInputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.util.Series;
import org.semanticweb.owlapi.model.IRI;

/**
 * This integration test class validates the PODD-prototype web service operations.
 * 
 * @author kutila
 * 
 */
public class PoddServletIntegrationTest extends AbstractPoddIntegrationTest
{
    
    /** set of cookies containing session information */
    private Series<Cookie> cookies = null;
    
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
    
    @Override
    protected void resetWebService()
    {
        final Request resetRequest = new Request(Method.POST, this.BASE_URL + "/podd/reset");
        resetRequest.setCookies(this.cookies);
        this.getClient().handle(resetRequest);
        // Note: not asserting response code which depends on whether there was an active session
    }
    
    /**
     * Test that the home page exists to ensure the PODD web-app is deployed.
     * 
     * @throws Exception
     */
    @Test
    public void testHomePage() throws Exception
    {
        final Request request = new Request(Method.GET, this.BASE_URL + "/index.html");
        final Response response = this.getClient().handle(request);
        
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), response.getStatus().getCode());
        Assert.assertTrue(response.getEntityAsText().contains("PODD Prototype Web Service"));
    }
    
    /**
     * Tests the ability to login and logout of the web service.
     * 
     * @throws Exception
     */
    @Test
    public void testLogin() throws Exception
    {
        this.login(AbstractPoddIntegrationTest.TEST_USERNAME, AbstractPoddIntegrationTest.TEST_PASSWORD);
        
        // -- logout without sending cookie: should fail with Status 401 (UNAUTHORIZED)
        final Request incorrectLogoutRequest = new Request(Method.GET, this.BASE_URL + "/login");
        incorrectLogoutRequest.getResourceRef().addQueryParameter("logout", "true");
        final Response incorrectLogoutResponse = this.getClient().handle(incorrectLogoutRequest);
        Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED.getCode(), incorrectLogoutResponse.getStatus().getCode());
        
        // -- logout: should succeed
        final Request logoutRequest = new Request(Method.GET, this.BASE_URL + "/login");
        logoutRequest.setCookies(this.cookies);
        logoutRequest.getResourceRef().addQueryParameter("logout", "true");
        final Response logoutResponse = this.getClient().handle(logoutRequest);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), logoutResponse.getStatus().getCode());
        
        // -- try to logout again: should fail with Status 401 (UNAUTHORIZED)
        final Request secondLogoutRequest = new Request(Method.GET, this.BASE_URL + "/login");
        secondLogoutRequest.setCookies(this.cookies);
        secondLogoutRequest.getResourceRef().addQueryParameter("logout", "true");
        final Response secondLogoutResponse = this.getClient().handle(secondLogoutRequest);
        Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED.getCode(), secondLogoutResponse.getStatus().getCode());
    }
    
    /**
     * Tests that a new artifact can be added through the web service.
     * 
     * @throws Exception
     */
    @Test
    public void testAddArtifact() throws Exception
    {
        this.login(AbstractPoddIntegrationTest.TEST_USERNAME, AbstractPoddIntegrationTest.TEST_PASSWORD);
        final String path = this.getClass().getResource("/test/artifacts/basicProject-1.rdf").getFile();
        this.addArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
    }
    
    /**
     * Tests that it is possible to retrieve an artifact from the web service.
     * 
     * @throws Exception
     */
    @Test
    public void testGetArtifact() throws Exception
    {
        // -- login and add an artifact
        this.login(AbstractPoddIntegrationTest.TEST_USERNAME, AbstractPoddIntegrationTest.TEST_PASSWORD);
        final String path = this.getClass().getResource("/test/artifacts/basicProject-1.rdf").getFile();
        final Response addResponse = this.addArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
        final String artifactUri =
                this.getArtifactUriFromStringRepresentation(addResponse.getEntityAsText(), RDFFormat.RDFXML);
        
        // -- GET the "base" artifact from the web service and verify results
        final Request getBaseRequest = new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
        getBaseRequest.setCookies(this.cookies);
        final Response getBaseResponse = this.getClient().handle(getBaseRequest);
        
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), getBaseResponse.getStatus().getCode());
        final String getResult = getBaseResponse.getEntityAsText();
        final URI baseContext = IRI.create("urn:get-base:").toOpenRDFURI();
        this.getTestRepositoryConnection().add(new ByteArrayInputStream(getResult.getBytes()), "", RDFFormat.RDFXML,
                baseContext);
        Assert.assertEquals(29, this.getTestRepositoryConnection().size(baseContext));
        
        // -- GET the "inferred" artifact from the web service and verify results
        final Request getInferredRequest =
                new Request(Method.GET, this.BASE_URL + "/podd/artifact/inferred/" + artifactUri);
        getInferredRequest.setCookies(this.cookies);
        final Response getInferredResponse = this.getClient().handle(getInferredRequest);
        
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), getInferredResponse.getStatus().getCode());
        final String getInferredResult = getInferredResponse.getEntityAsText();
        final URI inferredContext = IRI.create("urn:get-inferred:").toOpenRDFURI();
        this.getTestRepositoryConnection().add(new ByteArrayInputStream(getInferredResult.getBytes()), "",
                RDFFormat.RDFXML, inferredContext);
        Assert.assertTrue(this.getTestRepositoryConnection().size(inferredContext) > 29);
        
        this.getTestRepositoryConnection().rollback();
    }
    
    /**
     * Tests that it is possible to delete an artifact through the web service.
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteArtifact() throws Exception
    {
        // -- login and add an artifact
        this.login(AbstractPoddIntegrationTest.TEST_USERNAME, AbstractPoddIntegrationTest.TEST_PASSWORD);
        final String path = this.getClass().getResource("/test/artifacts/basicProject-1.rdf").getFile();
        final Response addResponse = this.addArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
        final String artifactUri =
                this.getArtifactUriFromStringRepresentation(addResponse.getEntityAsText(), RDFFormat.RDFXML);
        
        // -- DELETE the artifact using the web service and verify results
        final Request deleteRequest = new Request(Method.DELETE, this.BASE_URL + "/podd/artifact/" + artifactUri);
        deleteRequest.setCookies(this.cookies);
        final Response deleteResponse = this.getClient().handle(deleteRequest);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), deleteResponse.getStatus().getCode());
        Assert.assertTrue(deleteResponse.getEntityAsText().contains(artifactUri.substring(6)));
        
        // -- try to retrieve the artifact from the web service and verify that it no longer exists
        final Request getBaseRequest = new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
        getBaseRequest.setCookies(this.cookies);
        final Response getBaseResponse = this.getClient().handle(getBaseRequest);
        
        Assert.assertEquals(Status.CLIENT_ERROR_NOT_FOUND.getCode(), getBaseResponse.getStatus().getCode());
    }
    
    /**
     * Tests that resetting the web service results in the repository contents being wiped.
     * 
     * @throws Exception
     */
    @Test
    public void testReset() throws Exception
    {
        // -- login and add an artifact
        this.login(AbstractPoddIntegrationTest.TEST_USERNAME, AbstractPoddIntegrationTest.TEST_PASSWORD);
        final String path = this.getClass().getResource("/test/artifacts/basicProject-1.rdf").getFile();
        final Response addResponse = this.addArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
        final String artifactUri =
                this.getArtifactUriFromStringRepresentation(addResponse.getEntityAsText(), RDFFormat.RDFXML);
        
        // -- send RESET request
        final Request resetRequest = new Request(Method.POST, this.BASE_URL + "/podd/reset");
        resetRequest.setCookies(this.cookies);
        final Response resetResponse = this.getClient().handle(resetRequest);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), resetResponse.getStatus().getCode());
        
        // -- try to retrieve the artifact from the web service and verify that it no longer exists
        final Request getBaseRequest = new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
        getBaseRequest.setCookies(this.cookies);
        final Response getBaseResponse = this.getClient().handle(getBaseRequest);
        
        Assert.assertEquals(Status.CLIENT_ERROR_NOT_FOUND.getCode(), getBaseResponse.getStatus().getCode());
    }
    
    /**
     * Tests that an artifact can be edited through the web service.
     * 
     * @throws Exception
     */
    @Test
    public void testEditArtifactWithMerge() throws Exception
    {
        final long noOfStatements = this.genericTestEditArtifact("merge");
        Assert.assertEquals(33, noOfStatements);
    }
    
    /**
     * Tests that an artifact can be edited through the web service.
     * 
     * @throws Exception
     */
    @Test
    public void testEditArtifactWithReplace() throws Exception
    {
        final long noOfStatements = this.genericTestEditArtifact("replace");
        Assert.assertEquals(32, noOfStatements);
    }
    
    public long genericTestEditArtifact(final String editType) throws Exception
    {
        // -- login and add an artifact
        this.login(AbstractPoddIntegrationTest.TEST_USERNAME, AbstractPoddIntegrationTest.TEST_PASSWORD);
        final String path = this.getClass().getResource("/test/artifacts/editableProject-1.rdf").getFile();
        final Response addResponse = this.addArtifact(path, MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK.getCode());
        final String artifactUri =
                this.getArtifactUriFromStringRepresentation(addResponse.getEntityAsText(), RDFFormat.RDFXML);
        
        // -- generate and send an edit request
        final String fragmentPath = this.getClass().getResource("/test/artifacts/fragment.rdf").getFile();
        final Request editRequest =
                new Request(Method.POST, this.BASE_URL + "/podd/artifact/edit/" + editType + "/" + artifactUri);
        editRequest.setCookies(this.cookies);
        final FileRepresentation artifactToAdd = new FileRepresentation(fragmentPath, MediaType.APPLICATION_RDF_XML);
        editRequest.setEntity(artifactToAdd);
        final Response editResponse = this.getClient().handle(editRequest);
        
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), editResponse.getStatus().getCode());
        
        // -- retrieve edited artifact and verify the modifications are present
        final Request getBaseRequest = new Request(Method.GET, this.BASE_URL + "/podd/artifact/base/" + artifactUri);
        getBaseRequest.setCookies(this.cookies);
        final Response getBaseResponse = this.getClient().handle(getBaseRequest);
        Assert.assertEquals(Status.SUCCESS_OK.getCode(), getBaseResponse.getStatus().getCode());
        final String modifiedRDFString = getBaseResponse.getEntityAsText();
        
        Assert.assertTrue(modifiedRDFString.contains("John.Doe@csiro.au"));
        final URI baseContext = IRI.create("urn:get-base:").toOpenRDFURI();
        this.getTestRepositoryConnection().add(new ByteArrayInputStream(modifiedRDFString.getBytes()), "",
                RDFFormat.RDFXML, baseContext);
        final long noOfStatements = this.getTestRepositoryConnection().size(baseContext);
        this.getTestRepositoryConnection().rollback();
        return noOfStatements;
        
    }
    
    // ----- helper methods -----
    
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
    
    /**
     * Helper method to extract an artifact's URI from a String representation of the whole
     * artifact.
     * 
     * The URI is returned with the protocol part separated using a single '/' such that it can be
     * directly used with web service requests. (E.g. "http/purl.og...")
     * 
     * @param rdfString
     * @param rdfFormat
     * @return
     * @throws Exception
     */
    protected String getArtifactUriFromStringRepresentation(final String rdfString, final RDFFormat rdfFormat)
        throws Exception
    {
        try
        {
            final URI initialContext = IRI.create("urn:initial-context:").toOpenRDFURI();
            this.getTestRepositoryConnection().add(new ByteArrayInputStream(rdfString.getBytes()), "", rdfFormat,
                    initialContext);
            final URI hasTopObject = IRI.create("http://purl.org/podd/ns/poddBase#artifactHasTopObject").toOpenRDFURI();
            
            final RepositoryResult<Statement> results =
                    this.getTestRepositoryConnection().getStatements(null, hasTopObject, null, true, initialContext);
            if(results.hasNext())
            {
                final Resource artifactResource = results.next().getSubject();
                return artifactResource.stringValue().replace("://", "/");
            }
            else
            {
                Assert.fail("Could not find URI of added artifact");
                return null; // unreachable line, added to make the compiler happy
            }
        }
        finally
        {
            this.getTestRepositoryConnection().rollback();
        }
    }
    
}
