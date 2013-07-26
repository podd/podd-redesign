/**
 * 
 */
package com.github.podd.resources.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.restlet.PoddRoles;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class UserAddResourceImplTest extends AbstractResourceImplTest
{
    
    @Test
    public void testAddUserRdfBasic() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        // prepare: create a Model of user
        final String testEmail = "testuser@restlet-test.org";
        final String testPassword = "testpassword";
        final String testFirstName = "First";
        final String testLastName = "Last";
        
        final Model userInfoModel = new LinkedHashModel();
        final URI tempUserUri = PoddRdfConstants.VF.createURI("urn:temp:user");
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                PoddRdfConstants.VF.createLiteral(testEmail));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERSECRET,
                PoddRdfConstants.VF.createLiteral(testPassword));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERFIRSTNAME,
                PoddRdfConstants.VF.createLiteral(testFirstName));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERLASTNAME,
                PoddRdfConstants.VF.createLiteral(testLastName));
        userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_HOMEPAGE,
                PoddRdfConstants.VF.createURI("http://nohomepage"));
        userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_ORGANIZATION,
                PoddRdfConstants.VF.createLiteral("n/a"));
        userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_ORCID, PoddRdfConstants.VF.createLiteral("n/a"));
        
        userInfoModel
                .add(tempUserUri, SesameRealmConstants.OAS_USEREMAIL, PoddRdfConstants.VF.createLiteral(testEmail));
        
        // prepare: add 'Authenticated User' Role
        final URI authenticatedRoleMapping =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping:", UUID.randomUUID().toString());
        userInfoModel.add(authenticatedRoleMapping, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        userInfoModel.add(authenticatedRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDUSER, tempUserUri);
        userInfoModel.add(authenticatedRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDROLE,
                PoddRoles.AUTHENTICATED.getURI());
        
        // prepare: add 'Project Observer' Role of an imaginary project
        final URI observerRoleMapping =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping:", UUID.randomUUID().toString());
        userInfoModel.add(observerRoleMapping, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        userInfoModel.add(observerRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDUSER, tempUserUri);
        userInfoModel.add(observerRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDROLE,
                PoddRoles.PROJECT_OBSERVER.getURI());
        userInfoModel.add(observerRoleMapping, PoddWebConstants.PODD_ROLEMAPPEDOBJECT,
                PoddRdfConstants.VF.createURI("urn:podd:some:project"));
        
        final ClientResource userAddClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ADD));
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(userInfoModel, out, format);
        
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userAddClientResource, Method.POST, input, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // verify: response has same correct identifier
        final Model model =
                this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)),
                        RDFFormat.RDFXML, 1);
        Assert.assertEquals("Unexpected user identifier", testEmail,
                model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        
        // TODO: verify roles successfully set by retrieving the User Details
    }
    
}
