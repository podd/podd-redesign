/**
 * 
 */
package com.github.podd.resources.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 *
 */
public class UserPasswordResourceImplTest extends AbstractResourceImplTest
{
    
    @Test
    public void testChangeOtherUserPasswordRdf() throws Exception
    {
        final String testIdentifier = "anotherUser";
        final String newPassword = "modifiedPassword";
        final URI tempUserUri = PoddRdfConstants.VF.createURI("urn:temp:user");

        // prepare: create Model with modified password and user identifier
        final Model userInfoModel = new LinkedHashModel();
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                PoddRdfConstants.VF.createLiteral(testIdentifier));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERSECRET,
                PoddRdfConstants.VF.createLiteral(newPassword));
        
        // submit new password to Change Password Service
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final ClientResource userPasswordClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT_PWD + testIdentifier));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(userInfoModel, out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        
        final Representation modifiedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userPasswordClientResource, Method.POST, input, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // verify: response has correct identifier
        final Model model =
                this.assertRdf(new ByteArrayInputStream(modifiedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        RDFFormat.RDFXML, 1);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        
        // verify: request with old login details should fail
        final ClientResource userDetailsClientResource2 =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource2, Method.GET, null, mediaType,
                    Status.CLIENT_ERROR_UNAUTHORIZED, this.testNoAdminPrivileges);
            Assert.fail("Should have thrown a ResourceException as password should now be invalid");
        }
        catch(ResourceException e)
        {
            Assert.assertEquals("Was expecting an UNAUTHORIZED error", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }

    
    @Test
    public void testChangeOwnPasswordRdf() throws Exception
    {
        final String testIdentifier = "testAdminUser";
        final String oldPassword = "testAdminPassword";
        final String newPassword = "modifiedPassword";
        final URI tempUserUri = PoddRdfConstants.VF.createURI("urn:temp:user");

        // prepare: create Model with modified password and user identifier
        final Model userInfoModel = new LinkedHashModel();
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                PoddRdfConstants.VF.createLiteral(testIdentifier));
        userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_OLDSECRET,
                PoddRdfConstants.VF.createLiteral(oldPassword));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERSECRET,
                PoddRdfConstants.VF.createLiteral(newPassword));
        
        // submit new password to Change Password Service
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final ClientResource userPasswordClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT_PWD + testIdentifier));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(userInfoModel, out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        
        final Representation modifiedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userPasswordClientResource, Method.POST, input, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // verify: response has correct identifier
        final Model model =
                this.assertRdf(new ByteArrayInputStream(modifiedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        RDFFormat.RDFXML, 1);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        
        // verify: request with old login details should fail
        final ClientResource userDetailsClientResource2 =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource2, Method.GET, null, mediaType,
                    Status.CLIENT_ERROR_UNAUTHORIZED, this.testWithAdminPrivileges);
            Assert.fail("Should have thrown a ResourceException as password should now be invalid");
        }
        catch(ResourceException e)
        {
            Assert.assertEquals("Was expecting an UNAUTHORIZED error", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }

    @Test
    public void testPasswordChangePageHtml() throws Exception
    {
        final String testIdentifier = "testAdminUser";
        final ClientResource userPasswordClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT_PWD + testIdentifier));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userPasswordClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        //System.out.println(body);
        this.assertFreemarker(body);
        
        Assert.assertTrue("Page missing User identifier", body.contains(testIdentifier));
        Assert.assertTrue("Page missing old password", body.contains("Old Password"));
        Assert.assertTrue("Page missing confirm password", body.contains("Confirm New Password"));
        Assert.assertTrue("Page missing save button", body.contains("Save Password"));
        Assert.assertTrue("Page missing cancel button", body.contains("Cancel"));
    }
    
}
