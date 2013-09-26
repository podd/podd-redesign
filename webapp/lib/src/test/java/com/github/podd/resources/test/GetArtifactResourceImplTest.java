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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.RestletUtilMediaType;
import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddWebConstants;

/**
 * Test various forms of GetArtifact
 * 
 * @author kutila
 * 
 */
public class GetArtifactResourceImplTest extends AbstractResourceImplTest
{
    
    /**
     * Test unauthenticated access to a managed artifact gives an UNAUTHORIZED error and not a 404.
     * <p>
     * This distinction ensures that users must be logged in to see 404 exceptions for artifact
     * retrieval, providing a level of obscurity for systems that do not have user self-signup
     * enabled.
     */
    @Test
    public void testErrorGetArtifactManagedWithoutAuthentication() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER,
                    "http://purl.org/podd/ns/artifact/artifact89");
            
            getArtifactClientResource.get(MediaType.TEXT_HTML);
            Assert.fail("Should have thrown a ResourceException with Status Code 401");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Test authenticated access attempts to an unmanaged artifact gives a 404 error.
     */
    @Test
    public void testErrorGetArtifactUnmanagedWithAuthenticationAdmin() throws Exception
    {
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER,
                    "http://purl.org/podd/ns/artifact/artifact89");
            
            RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                    MediaType.APPLICATION_RDF_XML, Status.CLIENT_ERROR_NOT_FOUND, this.testWithAdminPrivileges);
            Assert.fail("Should have thrown a ResourceException with Status Code 404");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_NOT_FOUND, e.getStatus());
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Test authenticated access attempts to an unmanaged artifact gives a 404 error.
     */
    @Test
    public void testErrorGetArtifactUnmanagedWithAuthenticationNonAdmin() throws Exception
    {
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER,
                    "http://purl.org/podd/ns/artifact/artifact89");
            
            RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                    MediaType.APPLICATION_RDF_XML, Status.CLIENT_ERROR_NOT_FOUND, this.testNoAdminPrivileges);
            Assert.fail("Should have thrown a ResourceException with Status Code 404");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_NOT_FOUND, e.getStatus());
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Test unauthenticated access to an unmanaged artifact gives an UNAUTHORIZED error.
     */
    @Test
    public void testErrorGetArtifactUnmanagedWithoutAuthentication() throws Exception
    {
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER,
                    "http://purl.org/podd/ns/artifact/artifact89");
            
            getArtifactClientResource.get(MediaType.TEXT_HTML);
            Assert.fail("Should have thrown a ResourceException with Status Code 401");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Test access without artifactID parameter gives a BAD_REQUEST error.
     */
    @Test
    public void testErrorGetArtifactWithoutArtifactId() throws Exception
    {
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.get(MediaType.TEXT_HTML);
            Assert.fail("Should have thrown a ResourceException with Status Code 400");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Test authenticated access to get Artifact in HTML
     */
    @Test
    public void testGetArtifactBasicHtml() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String body = getText(results);
            
            // verify:
            Assert.assertTrue("Page does not identify Administrator", body.contains("Administrator"));
            Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));
            
            Assert.assertTrue("Missing: Project Details", body.contains("Project Details"));
            Assert.assertTrue("Missng: ANZSRC FOR Code", body.contains("ANZSRC FOR Code:"));
            Assert.assertTrue("Missng: Project#2012...", body.contains("Project#2012-0006_ Cotton Leaf Morphology"));
            
            this.assertFreemarker(body);
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Test authenticated access to get Artifact in HTML with a check on the RDFa
     */
    @Ignore("Temporarily ignore RDFa/XHTML parse error")
    @Test
    public void testGetArtifactBasicHtmlRDFa() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String body = getText(results);
            
            // verify:
            Assert.assertTrue("Page does not identify Administrator", body.contains("Administrator"));
            Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));
            
            Assert.assertTrue("Missing: Project Details", body.contains("Project Details"));
            Assert.assertTrue("Missng: ANZSRC FOR Code", body.contains("ANZSRC FOR Code:"));
            Assert.assertTrue("Missng: Project#2012...", body.contains("Project#2012-0006_ Cotton Leaf Morphology"));
            
            this.assertFreemarker(body);
            
            final Model model = this.assertRdf(new StringReader(body), RDFFormat.RDFA, 12);
            
            // RDFa generates spurious triples, use at your own risk
            // Only rely on numbers from actual RDF serialisations
            Assert.assertEquals(4, model.subjects().size());
            Assert.assertEquals(9, model.predicates().size());
            Assert.assertEquals(12, model.objects().size());
            
            if(this.log.isDebugEnabled())
            {
                DebugUtils.printContents(model);
            }
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
        
    }
    
    /**
     * Test authenticated access to get Artifact in RDF/JSON
     */
    @Test
    public void testGetArtifactBasicJson() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                            RestletUtilMediaType.APPLICATION_RDF_JSON, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String body = getText(results);
            
            // verify: received contents are in RDF/JSON
            // Assert.assertTrue("Result does not have @prefix", body.contains("@prefix"));
            
            // verify: received contents have artifact's ontology and version IRIs
            Assert.assertTrue("Result does not contain artifact URI", body.contains(artifactUri));
            
            final Model model = this.assertRdf(new StringReader(body), RDFFormat.RDFJSON, 28);
            
            Assert.assertEquals(6, model.subjects().size());
            Assert.assertEquals(14, model.predicates().size());
            Assert.assertEquals(23, model.objects().size());
            
            if(this.log.isDebugEnabled())
            {
                DebugUtils.printContents(model);
            }
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Test authenticated access to get Artifact in RDF/XML
     */
    @Test
    public void testGetArtifactBasicRdf() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                            MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String body = getText(results);
            
            // verify: received contents are in RDF
            Assert.assertTrue("Result does not have RDF", body.contains("<rdf:RDF"));
            Assert.assertTrue("Result does not have RDF", body.endsWith("</rdf:RDF>"));
            
            // verify: received contents have artifact URI
            Assert.assertTrue("Result does not contain artifact URI", body.contains(artifactUri));
            
            final Model model = this.assertRdf(new StringReader(body), RDFFormat.RDFXML, 28);
            
            Assert.assertEquals(6, model.subjects().size());
            Assert.assertEquals(14, model.predicates().size());
            Assert.assertEquals(23, model.objects().size());
            
            if(this.log.isDebugEnabled())
            {
                DebugUtils.printContents(model);
            }
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Test authenticated access to get Artifact in RDF/Turtle
     */
    @Test
    public void testGetArtifactBasicTurtle() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                            MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String body = getText(results).trim();
            
            // verify: received contents are in Turtle
            Assert.assertTrue("Turtle result did not have namespaces", body.contains("@prefix"));
            Assert.assertTrue("Turtle result should end with a period", body.endsWith(" ."));
            
            // verify: received contents have artifact's ontology and version IRIs
            Assert.assertTrue("Result does not contain artifact URI", body.contains(artifactUri));
            
            final Model model = this.assertRdf(new StringReader(body), RDFFormat.TURTLE, 28);
            
            Assert.assertEquals(6, model.subjects().size());
            Assert.assertEquals(14, model.predicates().size());
            Assert.assertEquals(23, model.objects().size());
            
            if(this.log.isDebugEnabled())
            {
                DebugUtils.printContents(model);
            }
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Test authenticated access to get an internal podd object in HTML
     */
    @Test
    public void testGetArtifactHtmlAnalysisObject() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        final String objectUri = "urn:poddinternal:7616392e-802b-4c5d-953d-bf81da5a98f4:0";
        
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_IDENTIFIER, objectUri);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String body = getText(results);
            
            // verify:
            Assert.assertTrue("Page does not identify Administrator", body.contains("Administrator"));
            Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));
            
            Assert.assertTrue("Missing: Analysis Details", body.contains("Analysis Details"));
            Assert.assertTrue("Missng title: poddScience#Analysis 0", body.contains("poddScience#Analysis 0"));
            
            this.assertFreemarker(body);
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Test authenticated access to get an internal podd object in HTML
     */
    @Test
    public void testGetArtifactHtmlPublicationObject() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact("/test/artifacts/basic-2.rdf");
        
        final String objectUri = "urn:hardcoded:purl:artifact:1#publication45";
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_IDENTIFIER, objectUri);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String body = getText(results);
            
            // verify:
            Assert.assertTrue("Page does not identify Administrator", body.contains("Administrator"));
            Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));
            
            Assert.assertTrue("Publication title is missing",
                    body.contains("Towards An Extensible, Domain-agnostic Scientific Data Management System"));
            Assert.assertTrue("#publishedIn value is missing", body.contains("Proceedings of the IEEE eScience 2010"));
            // Assert.assertTrue("Publicatin's PURL value is missing",
            // body.contains("http://dx.doi.org/10.1109/eScience.2010.44"));
            
            this.assertFreemarker(body);
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Test authenticated access to get Artifact in HTML by a non Repository Admin User
     */
    @Test
    public void testGetArtifactWithProjectAdminUserHtml() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        this.mapUserToRole("anotherUser", PoddRoles.PROJECT_ADMIN, artifactUri);
        
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testNoAdminPrivileges);
            
            final String body = getText(results);
            
            // verify:
            Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));
            
            Assert.assertTrue("Missing: Project Details", body.contains("Project Details"));
            Assert.assertTrue("Missng: ANZSRC FOR Code", body.contains("ANZSRC FOR Code:"));
            Assert.assertTrue("Missng: Project#2012...", body.contains("Project#2012-0006_ Cotton Leaf Morphology"));
            Assert.assertTrue("Missing: Edit Participants button", body.contains("Edit Participants"));
            Assert.assertTrue("Missing: Add Child Object button", body.contains("Add Child Object"));
            Assert.assertTrue("Missing: Delete Project button", body.contains("id=\"deleteProject\""));
            
            this.assertFreemarker(body);
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Test authenticated access to get Artifact in HTML by a non Repository Admin User
     */
    @Test
    public void testGetArtifactWithProjectObserverUserHtml() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        this.mapUserToRole("anotherUser", PoddRoles.PROJECT_OBSERVER, artifactUri);
        
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testNoAdminPrivileges);
            
            final String body = getText(results);
            
            // verify:
            Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));
            
            Assert.assertTrue("Missing: Project Details", body.contains("Project Details"));
            Assert.assertTrue("Missng: ANZSRC FOR Code", body.contains("ANZSRC FOR Code:"));
            Assert.assertTrue("Missng: Project#2012...", body.contains("Project#2012-0006_ Cotton Leaf Morphology"));
            
            Assert.assertFalse("Edit Participants button should NOT be present", body.contains("Edit Participants"));
            Assert.assertFalse("Add Child Object button should NOT be present", body.contains("Add Child Object"));
            Assert.assertFalse("Delete button should NOT be present", body.contains("id=\"deleteObject\""));
            
            this.assertFreemarker(body);
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Test authenticated access to get Artifact in RDF/XML by a non Repository Admin User.
     */
    @Test
    public void testGetArtifactWithNonAdminUserRdf() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        this.mapUserToRole("anotherUser", PoddRoles.PROJECT_ADMIN, artifactUri);
        
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                            MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testNoAdminPrivileges);
            
            final String body = getText(results);
            
            // verify: received contents are in RDF
            Assert.assertTrue("Result does not have RDF", body.contains("<rdf:RDF"));
            Assert.assertTrue("Result does not have RDF", body.endsWith("</rdf:RDF>"));
            
            // verify: received contents have artifact URI
            Assert.assertTrue("Result does not contain artifact URI", body.contains(artifactUri));
            
            final Model model = this.assertRdf(new StringReader(body), RDFFormat.RDFXML, 28);
            
            Assert.assertEquals(6, model.subjects().size());
            Assert.assertEquals(14, model.predicates().size());
            Assert.assertEquals(23, model.objects().size());
            
            if(this.log.isDebugEnabled())
            {
                DebugUtils.printContents(model);
            }
        }
        finally
        {
            releaseClient(getArtifactClientResource);
        }
    }
    
    /**
     * Test parsing a simple RDFa document
     */
    @Test
    public void testSimpleRDFaParse() throws Exception
    {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\" \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\"");
        sb.append(" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
        sb.append(" version=\"XHTML+RDFa 1.0\">");
        sb.append("<head>");
        sb.append(" <link rel=\"stylesheet\" href=\"http://localhost:8080/test/styles/mystyle.css\" media=\"screen\" type=\"text/css\" />");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("</body>");
        sb.append("</html>");
        this.assertRdf(new StringReader(sb.toString()), RDFFormat.RDFA, 1);
    }
    
}
