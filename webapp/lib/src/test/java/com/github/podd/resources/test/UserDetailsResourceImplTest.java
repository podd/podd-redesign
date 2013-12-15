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

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

/**
 * Test UserDetails resource at "user/details?userIdentifier={identifier}"
 * 
 * @author kutila
 * 
 */
public class UserDetailsResourceImplTest extends AbstractResourceImplTest
{
    
    /**
     * Test requesting details of a non-existent user results in a 404 response
     */
    @Test
    public void testErrorGetUserDetailsOfNonExistentUser() throws Exception
    {
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        try
        {
            userDetailsClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, "noSuchUser");
            
            RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null,
                    MediaType.TEXT_HTML, Status.CLIENT_ERROR_NOT_FOUND, this.WITH_ADMIN);
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_NOT_FOUND, e.getStatus());
        }
        finally
        {
            this.releaseClient(userDetailsClientResource);
        }
    }
    
    /**
     * Test authenticated user requesting details of another user is not allowed
     */
    @Test
    public void testErrorGetUserDetailsOfOtherUserByNonAdmin() throws Exception
    {
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        try
        {
            userDetailsClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER,
                    RestletTestUtils.TEST_ADMIN_USERNAME);
            
            RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null,
                    MediaType.TEXT_HTML, Status.CLIENT_ERROR_UNAUTHORIZED, this.NO_ADMIN);
            Assert.fail("Should have thrown a ResourceException with Status Code 401");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
        finally
        {
            this.releaseClient(userDetailsClientResource);
        }
    }
    
    /**
     * Test unauthenticated access gives an UNAUTHORIZED error.
     */
    @Test
    public void testErrorGetUserDetailsWithoutAuthentication() throws Exception
    {
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        
        try
        {
            userDetailsClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER,
                    RestletTestUtils.TEST_ADMIN_USERNAME);
            
            userDetailsClientResource.get(MediaType.TEXT_HTML);
            Assert.fail("Should have thrown a ResourceException with Status Code 401");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
        finally
        {
            this.releaseClient(userDetailsClientResource);
        }
    }
    
    /**
     * Test authenticated Admin user requesting details of another user
     */
    @Test
    public void testGetUserDetailsOfOtherUserByAdministrator() throws Exception
    {
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.ADMIN.getURI(), null));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PODD.VF
                .createURI("urn:podd:some-project")));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        try
        {
            userDetailsClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, this.WITH_ADMIN);
            
            final String body = this.getText(results);
            Assert.assertTrue(body.contains("User Name: "));
            Assert.assertTrue(body.contains("testuser@podd.com"));
            this.assertFreemarker(body);
        }
        finally
        {
            this.releaseClient(userDetailsClientResource);
        }
    }
    
    /**
     * Test authenticated access to user details of current user
     */
    @Test
    public void testGetUserDetailsWithAuthentication() throws Exception
    {
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        try
        {
            userDetailsClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER,
                    RestletTestUtils.TEST_ADMIN_USERNAME);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, this.WITH_ADMIN);
            
            final String body = this.getText(results);
            Assert.assertTrue(body.contains("Personal Details"));
            Assert.assertTrue(body.contains("User Name: "));
            Assert.assertTrue(body.contains("initial.admin.user@example.com"));
            this.assertFreemarker(body);
        }
        finally
        {
            this.releaseClient(userDetailsClientResource);
        }
    }
    
    /**
     * Test authenticated access to user details of current user
     */
    @Test
    public void testGetUserRdfBasic() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        try
        {
            userDetailsClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER,
                    RestletTestUtils.TEST_ADMIN_USERNAME);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                            Status.SUCCESS_OK, this.WITH_ADMIN);
            
            final Model resultsModel = this.assertRdf(results, format, 11);
            
            // DebugUtils.printContents(resultsModel);
            Assert.assertEquals("Not the expected identifier", "testAdminUser",
                    resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
            
            // verify: Roles are valid PoddRoles
            final Set<Value> roleSet =
                    resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objects();
            Assert.assertEquals("Not expected number of Roles", 1, roleSet.size());
            final Iterator<Value> iterator = roleSet.iterator();
            while(iterator.hasNext())
            {
                final Value next = iterator.next();
                final RestletUtilRole roleByUri = PoddRoles.getRoleByUri((URI)next);
                Assert.assertNotNull("Role is not a PoddRole", roleByUri);
            }
        }
        finally
        {
            this.releaseClient(userDetailsClientResource);
        }
    }
    
    @Test
    public void testGetUserRolesWithOptionalUrisRdf() throws Exception
    {
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final URI testObjectUri = PODD.VF.createURI("urn:podd:some-project");
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.ADMIN.getURI(), null));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), testObjectUri));
        final String testUserUri =
                this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier, null, null, null,
                        null, null, null, null, roles, PoddUserStatus.ACTIVE);
        
        // retrieve user details:
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        try
        {
            userDetailsClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                            Status.SUCCESS_OK, this.WITH_ADMIN);
            
            final Model resultsModel = this.assertRdf(results, format, 12);
            
            // verify:
            Assert.assertEquals("Not the expected User URI", testUserUri,
                    resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects().iterator()
                            .next().stringValue());
            Assert.assertEquals("Not the expected object URI", testObjectUri,
                    resultsModel.filter(null, PODD.PODD_ROLEMAPPEDOBJECT, null).objectURI());
            Assert.assertEquals("Not the expected User Status", PoddUserStatus.ACTIVE.getURI(),
                    resultsModel.filter(null, PODD.PODD_USER_STATUS, null).objectURI());
        }
        finally
        {
            this.releaseClient(userDetailsClientResource);
        }
    }
    
}
