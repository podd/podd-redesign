/**
 * 
 */
package com.github.podd.integration.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import net.sourceforge.jwebunit.api.IElement;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

/**
 * Tests the PODD web applicaiton with text/html requests, which should mimic those from browsers. Other
 * tests can be setup to allow other Accept headers.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class PoddHtmlIntegrationTest extends AbstractPoddHtmlUnitIntegrationTest
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    private File testDataFolder;
    
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
        this.getWebTester().beginAt("/loginpage");
        
        final IElement userElement = this.getWebTester().getElementById("user");
        userElement.setAttribute("value", username);
        
        final IElement passwordElement = this.getWebTester().getElementById("password");
        passwordElement.setAttribute("value", password);
        
        this.getWebTester().clickButtonWithText("login");
    }
    
    @Override
    protected void logout()
    {
        this.getWebTester().gotoPage("/logout");
    }
    
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        
        this.testDataFolder = this.folder.newFolder();
    }
    
    @Ignore
    @Test
    public void testCreateDeleteCreateSameOntologyUri() throws IOException
    {
        this.login("testAdminUser", "testAdminPassword");
        
        this.getWebTester().gotoPage("/service/testontologies/ontologymanager/upload");
        
        // Create temp file using the new Java-7 NIO API
        final Path tempOntologyFile =
                Files.createTempFile(Paths.get(this.testDataFolder.toURI()), "testontologydata-", ".rdf");
        
        Assert.assertTrue(Files.isWritable(tempOntologyFile));
        
        // java-7 try-with-resources block so that the outputStream is automatically closed
        // try (OutputStream outputStream = Files.newOutputStream(tempOntologyFile,
        // StandardOpenOption.WRITE))
        // {
        // IOUtils.write(TestUtils.createTestOntologyDocument("application/rdf+xml",
        // "http://example.org/ontology/me/", "http://versions.other.example.org/ontology/me/1"),
        // outputStream);
        // }
        
        final String filename = tempOntologyFile.toAbsolutePath().toString();
        this.log.info("filename={}", filename);
        this.getWebTester().setWorkingForm("ontologyuploadform");
        this.getWebTester().setTextField("ontologyUri", "http://example.org/ontology/me/");
        this.getWebTester().setTextField("ontologyFile", filename);
        
        // submit the page to /oas-test/services/testontologies/ontologymanager/upload where it
        // should be handled using the multipart/form-data request content type handler
        this.getWebTester().submit("submit");
        
        Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                .endsWith("/service/testontologies/ontologymanager/"));
        
        this.getWebTester().assertHeaderEquals("Content-Type", "text/html; charset=UTF-8");
        
        this.getWebTester().assertResponseCode(200);
        
        this.getWebTester().assertElementPresent("ontologyList");
        
        final IElement table = this.getWebTester().getElementById("ontologyList");
        
        Assert.assertNotNull(table.getChildren());
        
        Assert.assertEquals(1, table.getChildren().size());
        
        this.getWebTester().clickButtonWithText("Delete");
        
        this.log.info("delete URL={}", this.getWebTester().getTestingEngine().getPageURL().toExternalForm());
        
        Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                .contains("/service/testontologies/ontologymanager/delete"));
        
        this.getWebTester().assertHeaderEquals("Content-Type", "text/html; charset=UTF-8");
        
        this.getWebTester().assertResponseCode(200);
        
        // This click is to confirm deletion, as the first delete click sends the user to a
        // confirmation page to prevent mistakes
        this.getWebTester().clickButtonWithText("Delete");
        
        Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                .endsWith("/service/testontologies/ontologymanager/"));
        
        this.getWebTester().assertHeaderEquals("Content-Type", "text/html; charset=UTF-8");
        
        this.getWebTester().assertResponseCode(200);
        
        // verify that the list of ontologies is now empty
        this.getWebTester().assertElementNotPresent("ontologyList");
        
        this.getWebTester().gotoPage("/service/testontologies/ontologymanager/upload");
        
        this.getWebTester().setWorkingForm("ontologyuploadform");
        this.getWebTester().setTextField("ontologyUri", "http://example.org/ontology/me/");
        this.getWebTester().setTextField("ontologyFile", filename);
        
        // submit the page to /oas-test/services/testontologies/ontologymanager/upload where it
        // should be handled using the multipart/form-data request content type handler
        this.getWebTester().submit("submit");
        
        Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                .endsWith("/service/testontologies/ontologymanager/"));
        
        this.getWebTester().assertHeaderEquals("Content-Type", "text/html; charset=UTF-8");
        
        this.getWebTester().assertResponseCode(200);
        
        this.getWebTester().assertElementPresent("ontologyList");
        
        final IElement tableAfterRecreation = this.getWebTester().getElementById("ontologyList");
        
        Assert.assertNotNull(tableAfterRecreation.getChildren());
        
        Assert.assertEquals(1, tableAfterRecreation.getChildren().size());
        
        // FIXME: Test the resulting page
    }
    
    @Ignore
    @Test
    public void testCreateDeleteCreateSameOntologyUriMultipleTimes() throws IOException
    {
        this.login("testAdminUser", "testAdminPassword");
        
        // Create temp file using the new Java-7 NIO API
        final Path tempOntologyFile =
                Files.createTempFile(Paths.get(this.testDataFolder.toURI()), "testontologydata-", ".rdf");
        
        Assert.assertTrue(Files.isWritable(tempOntologyFile));
        
        // java-7 try-with-resources block so that the outputStream is automatically closed
        // try (OutputStream outputStream = Files.newOutputStream(tempOntologyFile,
        // StandardOpenOption.WRITE))
        // {
        // IOUtils.write(TestUtils.createTestOntologyDocument("application/rdf+xml",
        // "http://example.org/ontology/me/", "http://versions.other.example.org/ontology/me/1"),
        // outputStream);
        // }
        final String filename = tempOntologyFile.toAbsolutePath().toString();
        this.log.info("filename={}", filename);
        
        for(int i = 0; i < 10; i++)
        {
            this.getWebTester().gotoPage("/service/testontologies/ontologymanager/upload");
            
            this.getWebTester().setWorkingForm("ontologyuploadform");
            this.getWebTester().setTextField("ontologyUri", "http://example.org/ontology/me/");
            this.getWebTester().setTextField("ontologyFile", filename);
            
            // submit the page to /oas-test/services/testontologies/ontologymanager/upload where it
            // should be handled using the multipart/form-data request content type handler
            this.getWebTester().submit("submit");
            
            Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                    .endsWith("/service/testontologies/ontologymanager/"));
            
            this.getWebTester().assertHeaderEquals("Content-Type", "text/html; charset=UTF-8");
            
            this.getWebTester().assertResponseCode(200);
            
            this.getWebTester().assertElementPresent("ontologyList");
            
            final IElement table = this.getWebTester().getElementById("ontologyList");
            
            Assert.assertNotNull(table.getChildren());
            
            Assert.assertEquals(1, table.getChildren().size());
            
            this.getWebTester().clickButtonWithText("Delete");
            
            this.log.info("delete URL={}", this.getWebTester().getTestingEngine().getPageURL().toExternalForm());
            
            Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                    .contains("/service/testontologies/ontologymanager/delete"));
            
            this.getWebTester().assertHeaderEquals("Content-Type", "text/html; charset=UTF-8");
            
            this.getWebTester().assertResponseCode(200);
            
            // This click is to confirm deletion, as the first delete click sends the user to a
            // confirmation page to prevent mistakes
            this.getWebTester().clickButtonWithText("Delete");
            
            Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                    .endsWith("/service/testontologies/ontologymanager/"));
            
            this.getWebTester().assertHeaderEquals("Content-Type", "text/html; charset=UTF-8");
            
            this.getWebTester().assertResponseCode(200);
            
            // verify that the list of ontologies is now empty
            this.getWebTester().assertElementNotPresent("ontologyList");
            
            // FIXME: Test the resulting page
            
        }
    }
    
    @Ignore
    @Test
    public void testCreateOntologyPostRdfWebService() throws IOException
    {
        this.login("testAdminUser", "testAdminPassword");
        
        this.getWebTester().gotoPage("/service/testontologies/ontologymanager/upload");
        
        // Create temp file using the new Java-7 NIO API
        final Path temp = Files.createTempFile(Paths.get(this.testDataFolder.toURI()), "testontologydata-", ".rdf");
        
        Assert.assertTrue(Files.isWritable(temp));
        
        // java-7 try-with-resources block so that the outputStream is automatically closed
        // try (OutputStream outputStream = Files.newOutputStream(temp, StandardOpenOption.WRITE))
        // {
        // IOUtils.write(TestUtils.createTestOntologyDocument("application/rdf+xml",
        // "http://example.org/ontology/me/", "http://versions.other.example.org/ontology/me/1"),
        // outputStream);
        // }
        
        final String filename = temp.toAbsolutePath().toString();
        this.log.info("filename={}", filename);
        this.getWebTester().setWorkingForm("ontologyuploadform");
        this.getWebTester().setTextField("ontologyFile", filename);
        
        // submit the page to /oas-test/services/testontologies/ontologymanager/upload where it
        // should be handled using the multipart/form-data request content type handler
        this.getWebTester().submit("submit");
        
        Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                .endsWith("/service/testontologies/ontologymanager/"));
        
        this.getWebTester().assertHeaderEquals("Content-Type", "text/html; charset=UTF-8");
        
        this.getWebTester().assertResponseCode(200);
        
        this.getWebTester().assertElementPresent("ontologyList");
        
        final IElement table = this.getWebTester().getElementById("ontologyList");
        
        Assert.assertNotNull(table.getChildren());
        
        Assert.assertEquals(1, table.getChildren().size());
        
        this.getWebTester().clickButtonWithText("Delete");
        
        this.log.info("delete URL={}", this.getWebTester().getTestingEngine().getPageURL().toExternalForm());
        
        Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                .contains("/service/testontologies/ontologymanager/delete"));
        
        this.getWebTester().assertHeaderEquals("Content-Type", "text/html; charset=UTF-8");
        
        this.getWebTester().assertResponseCode(200);
        
        // FIXME: Test the resulting page
    }
    
    @Ignore
    @Test
    public void testCreateOntologyPostRdfWebServiceMediumSize() throws IOException, URISyntaxException
    {
        this.login("testAdminUser", "testAdminPassword");
        
        this.getWebTester().gotoPage("/service/testontologies/ontologymanager/upload");
        
        // Create temp file using the new Java-7 NIO API
        final Path temp = Files.createTempFile(Paths.get(this.testDataFolder.toURI()), "testontologydata-", ".rdf");
        Assert.assertTrue(Files.isWritable(temp));
        
        // copy the resource to the temporary file so it can be uploaded, as the resource may be
        // inside a jar file and not-locatable by the web browsing test engine
        // Use Java-7 try-with-resource block
        try (InputStream input = this.getClass().getResourceAsStream("/ontologies/plant_ontology-v16.owl"))
        {
            final long copy = Files.copy(input, temp, StandardCopyOption.REPLACE_EXISTING);
            
            Assert.assertTrue(copy > 0);
        }
        
        final String filename = temp.toAbsolutePath().toString();
        this.log.info("filename={}", filename);
        this.getWebTester().setWorkingForm("ontologyuploadform");
        this.getWebTester().setTextField("ontologyFile", filename);
        this.getWebTester().setTextField("ontologyUri", "http://my.example.org/ontologies/plantontology/version/16");
        
        // submit the page to /oas-test/services/testontologies/ontologymanager/upload where it
        // should be handled using the multipart/form-data request content type handler
        this.getWebTester().submit("submit");
        
        Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                .endsWith("/service/testontologies/ontologymanager/"));
        
        this.getWebTester().assertHeaderEquals("Content-Type", "text/html; charset=UTF-8");
        
        this.getWebTester().assertResponseCode(200);
        
        this.getWebTester().assertElementPresent("ontologyList");
        
        final IElement table = this.getWebTester().getElementById("ontologyList");
        Assert.assertEquals("table", table.getName());
        Assert.assertNotNull(table.getChildren());
        Assert.assertEquals(1, table.getChildren().size());
        
        final IElement tbody = table.getChildren().get(0);
        Assert.assertEquals("tbody", tbody.getName());
        Assert.assertNotNull(tbody.getChildren());
        Assert.assertEquals(1, tbody.getChildren().size());
        
        final IElement tr = tbody.getChildren().get(0);
        Assert.assertEquals("tr", tr.getName());
        Assert.assertNotNull(tr.getChildren());
        Assert.assertEquals(1, tr.getChildren().size());
        
        final IElement td = tr.getChildren().get(0);
        
        this.log.info("td.getChildren()=" + td.getChildren());
        
        Assert.assertEquals("td", td.getName());
        Assert.assertNotNull(td.getChildren());
        Assert.assertEquals(5, td.getChildren().size());
        
        final IElement ontologyAnchor = td.getChildren().get(0);
        Assert.assertEquals("a", ontologyAnchor.getName());
        Assert.assertNotNull(ontologyAnchor.getChildren());
        Assert.assertEquals(0, ontologyAnchor.getChildren().size());
        
        Assert.assertEquals("ontology", ontologyAnchor.getAttribute("class"));
        Assert.assertTrue(ontologyAnchor
                .getAttribute("href")
                .contains(
                        "service/testontologies/ontologymanager/?ontologyUri=http://my.example.org/ontologies/plantontology/version/16"));
        
        final IElement ontologyVersionAnchor = td.getChildren().get(1);
        Assert.assertEquals("a", ontologyVersionAnchor.getName());
        Assert.assertNotNull(ontologyVersionAnchor.getChildren());
        Assert.assertEquals(0, ontologyVersionAnchor.getChildren().size());
        
        Assert.assertEquals("ontology_version", ontologyVersionAnchor.getAttribute("class"));
        Assert.assertTrue(ontologyVersionAnchor
                .getAttribute("href")
                .contains(
                        "service/testontologies/ontologymanager/?ontologyUri=http://my.example.org/ontologies/plantontology/version/16&ontologyVersionUri="));
        
        final IElement browseOntologyVersionAnchor = td.getChildren().get(2);
        Assert.assertEquals("a", browseOntologyVersionAnchor.getName());
        Assert.assertNotNull(browseOntologyVersionAnchor.getChildren());
        Assert.assertEquals(0, browseOntologyVersionAnchor.getChildren().size());
        
        this.log.info("browseOntologyVersionAnchor={}", browseOntologyVersionAnchor);
        
        Assert.assertEquals("ontology_version", browseOntologyVersionAnchor.getAttribute("class"));
        Assert.assertTrue(browseOntologyVersionAnchor
                .getAttribute("href")
                .contains(
                        "service/testontologies/ontologybrowser/?ontologyUri=http://my.example.org/ontologies/plantontology/version/16&ontologyVersionUri="));
        
        final IElement profileHeader = td.getChildren().get(3);
        Assert.assertEquals("div", profileHeader.getName());
        Assert.assertNotNull(profileHeader.getChildren());
        Assert.assertEquals(2, profileHeader.getChildren().size());
        
        final IElement profileSpan = profileHeader.getChildren().get(0);
        Assert.assertEquals("span", profileSpan.getName());
        Assert.assertNotNull(profileSpan.getChildren());
        Assert.assertEquals(0, profileSpan.getChildren().size());
        
        final IElement profileList = profileHeader.getChildren().get(1);
        Assert.assertEquals("ul", profileList.getName());
        Assert.assertNotNull(profileList.getChildren());
        // 2 here indicates that there are two profiles suitable for this ontology, currently OWL_DL
        // and OWL_FULL
        Assert.assertEquals(2, profileList.getChildren().size());
        // FIXME: Check contents of Profile ul list here
        
        final IElement form = td.getChildren().get(4);
        Assert.assertEquals("form", form.getName());
        Assert.assertNotNull(form.getChildren());
        Assert.assertEquals(4, form.getChildren().size());
        
        this.log.info("form.getChildren()=" + form.getChildren());
        
        Assert.assertEquals("delete_ontology", form.getAttribute("class"));
        Assert.assertTrue(form.getAttribute("action").contains("service/testontologies/ontologymanager/delete"));
        Assert.assertEquals("get", form.getAttribute("method"));
        
        this.getWebTester().clickButtonWithText("Delete");
        
        this.log.info("delete URL={}", this.getWebTester().getTestingEngine().getPageURL().toExternalForm());
        
        Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                .contains("/service/testontologies/ontologymanager/delete"));
        
        this.getWebTester().assertHeaderEquals("Content-Type", "text/html; charset=UTF-8");
        
        this.getWebTester().assertResponseCode(200);
        
        // FIXME: Check the contents of the form
        
        this.getWebTester().clickButtonWithText("Delete");
        
        Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                .endsWith("/service/testontologies/ontologymanager/"));
        
        this.getWebTester().assertHeaderEquals("Content-Type", "text/html; charset=UTF-8");
        
        this.getWebTester().assertResponseCode(200);
        
    }
    
    /**
     * Tests whether a call to the Search for Annotations by Object Type service generates an HTTP
     * 400 response.
     */
    @Ignore
    @Test
    public void testFailEmptyAnnotationByObjectType()
    {
        try
        {
            this.getWebTester().beginAt("/service/testannotations/byobjecttype");
            Assert.fail("Did not find expected exception");
        }
        catch(final net.sourceforge.jwebunit.exception.TestingEngineResponseException ex)
        {
            Assert.assertNotNull(ex.getCause());
            
            Assert.assertTrue(ex.getCause() instanceof FailingHttpStatusCodeException);
            
            final FailingHttpStatusCodeException fhsce = (FailingHttpStatusCodeException)ex.getCause();
            
            this.log.info("Found expected exception: " + fhsce.getMessage());
            
            Assert.assertEquals(400, fhsce.getStatusCode());
        }
    }
    
    /**
     * Tests whether a call to the Search for Annotations by Object URI service generates an HTTP
     * 400 response.
     */
    @Ignore
    @Test
    public void testFailEmptyAnnotationByObjectUri()
    {
        try
        {
            this.getWebTester().beginAt("/service/testannotations/byobjecturi");
            Assert.fail("Did not find expected exception");
        }
        catch(final net.sourceforge.jwebunit.exception.TestingEngineResponseException ex)
        {
            Assert.assertNotNull(ex.getCause());
            
            Assert.assertTrue(ex.getCause() instanceof FailingHttpStatusCodeException);
            
            final FailingHttpStatusCodeException fhsce = (FailingHttpStatusCodeException)ex.getCause();
            
            this.log.info("Found expected exception: " + fhsce.getMessage());
            
            Assert.assertEquals(400, fhsce.getStatusCode());
        }
    }
    
    /**
     * Tests whether a call to the Search for Annotations by Ontology Term URI service generates an
     * HTTP 400 response.
     */
    @Ignore
    @Test
    public void testFailEmptyAnnotationByOntologyTermUri()
    {
        try
        {
            this.getWebTester().beginAt("/service/testannotations/byontologytermuri");
            Assert.fail("Did not find expected exception");
        }
        catch(final net.sourceforge.jwebunit.exception.TestingEngineResponseException ex)
        {
            Assert.assertNotNull(ex.getCause());
            
            Assert.assertTrue(ex.getCause() instanceof FailingHttpStatusCodeException);
            
            final FailingHttpStatusCodeException fhsce = (FailingHttpStatusCodeException)ex.getCause();
            
            this.log.info("Found expected exception: " + fhsce.getMessage());
            
            Assert.assertEquals(400, fhsce.getStatusCode());
        }
    }
    
    /**
     * Tests whether a call to the Count Annotations by Ontology Term URI service generates an HTTP
     * 400 response.
     */
    @Ignore
    @Test
    public void testFailEmptyAnnotationCountByOntologyTermUri()
    {
        try
        {
            this.getWebTester().beginAt("/service/testannotations/countbyontologytermuri");
            Assert.fail("Did not find expected exception");
        }
        catch(final net.sourceforge.jwebunit.exception.TestingEngineResponseException ex)
        {
            Assert.assertNotNull(ex.getCause());
            
            Assert.assertTrue(ex.getCause() instanceof FailingHttpStatusCodeException);
            
            final FailingHttpStatusCodeException fhsce = (FailingHttpStatusCodeException)ex.getCause();
            
            this.log.info("Found expected exception: " + fhsce.getMessage());
            
            Assert.assertEquals(400, fhsce.getStatusCode());
        }
    }
    
    /**
     * Tests whether a call to the annotation create webservice with an empty query generates an
     * HTTP 400 error.
     */
    @Ignore
    @Test
    public void testFailEmptyCreateAnnotation()
    {
        try
        {
            this.getWebTester().beginAt("/service/testannotations/create");
            Assert.fail("Did not find expected exception");
        }
        catch(final net.sourceforge.jwebunit.exception.TestingEngineResponseException ex)
        {
            Assert.assertNotNull(ex.getCause());
            
            Assert.assertTrue(ex.getCause() instanceof FailingHttpStatusCodeException);
            
            final FailingHttpStatusCodeException fhsce = (FailingHttpStatusCodeException)ex.getCause();
            
            this.log.info("Found expected exception: " + fhsce.getMessage());
            
            Assert.assertEquals(400, fhsce.getStatusCode());
        }
    }
    
    /**
     * Tests whether a call to an unknown annotation generates a HTTP 400 response.
     */
    @Ignore
    @Test
    public void testFailEmptyFetchUnknownAnnotation()
    {
        try
        {
            this.getWebTester().beginAt("/service/testannotations/id/0123456");
            Assert.fail("Did not find expected exception");
        }
        catch(final net.sourceforge.jwebunit.exception.TestingEngineResponseException ex)
        {
            Assert.assertNotNull(ex.getCause());
            
            Assert.assertTrue(ex.getCause() instanceof FailingHttpStatusCodeException);
            
            final FailingHttpStatusCodeException fhsce = (FailingHttpStatusCodeException)ex.getCause();
            
            this.log.info("Found expected exception: " + fhsce.getMessage());
            
            Assert.assertEquals(400, fhsce.getStatusCode());
        }
        
    }
    
    /**
     * Tests whether an HTTP GET request to the Ontology Manager Delete service generates an HTTP
     * 405 Method Not Allowed response, as HTTP GET is not allowed by this service
     */
    @Ignore
    @Test
    public void testFailEmptyOntologyManagerDelete()
    {
        this.login("testAdminUser", "testAdminPassword");
        
        try
        {
            this.getWebTester().gotoPage("/service/testontologies/ontologymanager/delete");
            Assert.fail("Did not find expected exception");
        }
        catch(final net.sourceforge.jwebunit.exception.TestingEngineResponseException ex)
        {
            Assert.assertNotNull(ex.getCause());
            
            Assert.assertTrue(ex.getCause() instanceof FailingHttpStatusCodeException);
            
            final FailingHttpStatusCodeException fhsce = (FailingHttpStatusCodeException)ex.getCause();
            
            this.log.info("Found expected exception: " + fhsce.getMessage());
            
            // HTTP 400 client error because we did not specify either the ontologyUri or the
            // ontologyVersionUri
            Assert.assertEquals(400, fhsce.getStatusCode());
        }
    }
    
    /**
     * Tests whether an HTTP GET request to the Ontology Manager Delete service generates an HTTP
     * 405 Method Not Allowed response, as HTTP GET is not allowed by this service
     */
    @Ignore
    @Test
    public void testFailEmptyOntologyManagerDeleteNonAdmin()
    {
        this.login("testUser", "testPassword");
        
        try
        {
            this.getWebTester().gotoPage("/service/testontologies/ontologymanager/delete");
            Assert.fail("Did not find expected exception");
        }
        catch(final net.sourceforge.jwebunit.exception.TestingEngineResponseException ex)
        {
            Assert.assertNotNull(ex.getCause());
            
            Assert.assertTrue(ex.getCause() instanceof FailingHttpStatusCodeException);
            
            final FailingHttpStatusCodeException fhsce = (FailingHttpStatusCodeException)ex.getCause();
            
            this.log.info("Found expected exception: " + fhsce.getMessage());
            
            // HTTP 400 client error because we did not specify either the ontologyUri or the
            // ontologyVersionUri
            Assert.assertEquals(401, fhsce.getStatusCode());
        }
    }
    
    /**
     * Tests whether an HTTP GET request to the Ontology Manager Delete service generates an HTTP
     * 401 Unauthorised response if not logged in.
     */
    @Ignore
    @Test
    public void testFailEmptyOntologyManagerDeleteUnauthorised()
    {
        try
        {
            this.getWebTester().beginAt("/service/testontologies/ontologymanager/delete");
            Assert.fail("Did not find expected exception");
        }
        catch(final net.sourceforge.jwebunit.exception.TestingEngineResponseException ex)
        {
            Assert.assertNotNull(ex.getCause());
            
            Assert.assertTrue(ex.getCause() instanceof FailingHttpStatusCodeException);
            
            final FailingHttpStatusCodeException fhsce = (FailingHttpStatusCodeException)ex.getCause();
            
            this.log.info("Found expected exception: " + fhsce.getMessage());
            
            // HTTP 400 client error because we did not specify either the ontologyUri or the
            // ontologyVersionUri
            Assert.assertEquals(401, fhsce.getStatusCode());
        }
    }
    
    /**
     * Tests whether an HTTP GET request to the Ontology Manager Upload service generates an HTTP
     * 405 Method Not Allowed response, as HTTP GET is not allowed by this service
     * 
     * FIXME: Convert this to a test of the upload HTML interface. A HTTP GET request to this
     * resource now returns a form that can be used to upload ontologies, where the submission goes
     * using POST to this resource.
     */
    @Ignore
    @Test
    public void testFailEmptyOntologyManagerUpload()
    {
        try
        {
            this.getWebTester().beginAt("/service/testontologies/ontologymanager/upload");
            Assert.fail("Did not find expected exception");
        }
        catch(final net.sourceforge.jwebunit.exception.TestingEngineResponseException ex)
        {
            Assert.assertNotNull(ex.getCause());
            
            Assert.assertTrue(ex.getCause() instanceof FailingHttpStatusCodeException);
            
            final FailingHttpStatusCodeException fhsce = (FailingHttpStatusCodeException)ex.getCause();
            
            this.log.info("Found expected exception: " + fhsce.getMessage());
            
            Assert.assertEquals(405, fhsce.getStatusCode());
        }
    }
    
    /**
     * Tests whether an HTTP GET request to the Ontology Manager Upload service generates an HTTP
     * 401 Unauthorized response if not logged in.
     * 
     */
    @Ignore
    @Test
    public void testFailEmptyOntologyManagerUploadUnauthenticated()
    {
        try
        {
            this.getWebTester().beginAt("/service/testontologies/ontologymanager/upload");
            Assert.fail("Did not find expected exception");
        }
        catch(final net.sourceforge.jwebunit.exception.TestingEngineResponseException ex)
        {
            Assert.assertNotNull(ex.getCause());
            
            Assert.assertTrue(ex.getCause() instanceof FailingHttpStatusCodeException);
            
            final FailingHttpStatusCodeException fhsce = (FailingHttpStatusCodeException)ex.getCause();
            
            this.log.info("Found expected exception: " + fhsce.getMessage());
            
            Assert.assertEquals(401, fhsce.getStatusCode());
        }
    }
    
    /**
     * Verify that the admin user in testing has the admin role.
     */
    @Ignore
    @Test
    public void testGetRolesAdmin()
    {
        this.login("testAdminUser", "testAdminPassword");
        
        // we should be at the ontology manager page with a 200 HTTP status after login
        this.getWebTester().assertResponseCode(200);
        Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                .endsWith("/service/testontologies/ontologymanager/"));
        
        // verify that the Login link has disappeared
        this.getWebTester().assertTextNotPresent("Login");
        
        // verify the correct user was logged in and their name now appears
        this.getWebTester().assertTextPresent("testAdminUser");
        
        // verify the Logout link is available
        this.getWebTester().assertTextPresent("Logout");
        
        // verify the Administrator-only Upload link is available
        this.getWebTester().assertTextPresent("Upload");
        
        this.getWebTester().gotoPage("/user");
    }
    
    /**
     * Verify that the login process using the HTML login form does not fall over.
     */
    @Test
    public void testHtmlLogin()
    {
        this.login("testUser", "testPassword");
        
        // we should be at the index page with a 200 HTTP status after login
        this.getWebTester().assertResponseCode(200);
        Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                .endsWith("/index"));
        
        // verify that the Login link has disappeared
        this.getWebTester().assertTextNotPresent("Login");
        
        // verify the correct user was logged in and their name now appears
        this.getWebTester().assertTextPresent("Test User");
        
        // verify the Logout link is available
        this.getWebTester().assertTextPresent("Logout");
        
        // verify the Administrator-only Upload link is NOT available
        this.getWebTester().assertTextNotPresent("Upload");
    }
    
    /**
     * Verify that the login process using the HTML login form does not fall over.
     */
    @Test
    public void testHtmlLoginAdmin()
    {
        this.login("testAdminUser", "testAdminPassword");
        
        // we should be at the index page with a 200 HTTP status after login
        this.getWebTester().assertResponseCode(200);
        Assert.assertTrue(this.getWebTester().getTestingEngine().getPageURL().toExternalForm()
                .endsWith("/index"));
        
        // verify that the Login link has disappeared
        this.getWebTester().assertTextNotPresent("Login");
        
        // verify the correct user was logged in and their name now appears
        this.getWebTester().assertTextPresent("Test Admin User");
        
        // verify the Logout link is available
        this.getWebTester().assertTextPresent("Logout");
        
        // verify the Administrator-only Upload link is available
        this.getWebTester().assertTextNotPresent("Upload");
    }
    
    /**
     * Quick test for resource loading and the clicking of a button on a page to generate a dialog
     * using Javascript.
     */
    @Ignore
    @Test
    public void testResourceLoading()
    {
        this.getWebTester().beginAt("/resources/static/scripts/oas-rdf.js");
        
        this.getWebTester().gotoPage("/resources/static/oas-test.html");
        
        this.getWebTester().clickElementByXPath("//div[@id='science_thing']");
        
        this.getWebTester().clickButtonWithText("Create annotation");
    }
    
    /**
     * Tests whether an HTTP GET request to the Ontology Manager main page using text/html generates
     * an HTTP 200 response.
     * 
     */
    @Ignore
    @Test
    public void testSuccessEmptyOntologyManagerMainPageHtml()
    {
        // setupTestAcceptHeader();
        
        this.getWebTester().beginAt("/service/testontologies/ontologymanager/");
        
        this.getWebTester().assertHeaderEquals("Content-Type", "text/html; charset=UTF-8");
        
        this.getWebTester().assertResponseCode(200);
    }
    
}
