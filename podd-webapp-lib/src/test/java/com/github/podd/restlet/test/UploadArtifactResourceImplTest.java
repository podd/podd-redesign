/**
 * 
 */
package com.github.podd.restlet.test;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class UploadArtifactResourceImplTest extends AbstractResourceImplTest
{
    /**
     * Test unauthenticated access to "upload artifact" leads to error.
     */
    @Test
    public void testErrorPostWithoutAuthentication() throws Exception
    {
        final ClientResource uploadArtifactResource =
                new ClientResource(this.getUrl("/" + PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        final FormDataSet form = new FormDataSet();
        form.setMultipart(true);
        form.getEntries().add(new FormData("username", "testAdminUser"));
        form.getEntries().add(new FormData("password", "testAdminPassword"));
        
        try
        {
            uploadArtifactResource.post(form, MediaType.TEXT_HTML);
            Assert.fail("Should have thrown a ResourceException with Status Code 401");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
    
    @Test
    public void testErrorPostUploadWithoutFile() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl("/" + PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        final FormDataSet form = new FormDataSet();
        form.setMultipart(true);
        form.getEntries().add(new FormData("username", "testAdminUser"));
        form.getEntries().add(new FormData("password", "testAdminPassword"));
        
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, form,
                    MediaType.TEXT_HTML, Status.CLIENT_ERROR_BAD_REQUEST, this.testWithAdminPrivileges);
            Assert.fail("Should have thrown a ResourceException with Status Code 400");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
        }
    }
    
    /**
     * Tests that no error occurs when trying to upload a new artifact file while authenticated with
     * the admin role.
     * 
     */
    @Test
    public void testPostUploadArtifactBasic() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl("/" + PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        final FormDataSet form = new FormDataSet();
        form.setMultipart(true);
        form.getEntries().add(new FormData("username", "testAdminUser"));
        form.getEntries().add(new FormData("password", "testAdminPassword"));
        
        final URL fileUrl = this.getClass().getResource("/test/artifacts/basicProject-1-internal-object.rdf");
        
        this.log.info("The URL is {}", fileUrl);
        Assert.assertNotNull("Null artifact file", fileUrl);
        
        final File artifactDiskFile = new File(fileUrl.toURI());
        final FileRepresentation fileRep = new FileRepresentation(artifactDiskFile, MediaType.APPLICATION_RDF_XML);
        Assert.assertNotNull("Null FileRepresentation", fileRep);
        
        form.getEntries().add(new FormData("file", fileRep));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, form,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // TODO: verify results once a proper success page is incorporated.
        final String body = results.getText();
        Assert.assertTrue(body.contains("Welcome"));
    }
    
}
