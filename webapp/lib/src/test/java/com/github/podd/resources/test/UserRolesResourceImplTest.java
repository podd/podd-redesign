/**
 * 
 */
package com.github.podd.resources.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 */
public class UserRolesResourceImplTest extends AbstractResourceImplTest
{
    
    /**
     * Tests editing of a PODD User's Roles.  
     */
    @Test
    public void testEditOtherUserRolesRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.ADMIN.getURI(), null));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:cotton-leaf-morphology")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:tea-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:coffee-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:banana-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:coconut-leaf-study")));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        
        // prepare: modify Test User's Roles
        final Model newModel = new LinkedHashModel();
        
        // prepare: Project Observer Role for Project "cotton-leaf-23567"
        final URI testProject1Uri = PoddRdfConstants.VF.createURI("urn:podd:project-cotton-leaf-23567");
        final URI roleMapping1Uri =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping1:", UUID.randomUUID().toString());
        newModel.add(roleMapping1Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping1Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping1Uri, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, testProject1Uri);
        // NOTE: no need to specify ROLE_MAPPED_USER as User is identified from the request
        
        // prepare: Project Observer Role for Project "tea-leaf-99"
        final URI testProject2Uri = PoddRdfConstants.VF.createURI("urn:podd:project-tea-leaf-99");
        final URI roleMapping2Uri =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping2:", UUID.randomUUID().toString());
        newModel.add(roleMapping2Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping2Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping2Uri, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, testProject2Uri);
        
        
        // submit modified details to User Roles Service
        final ClientResource userRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT_ROLES + testIdentifier));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(newModel, out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        final Representation modifiedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userRolesClientResource, Method.POST, input, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        final Model model =
                this.assertRdf(new ByteArrayInputStream(modifiedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        RDFFormat.RDFXML, 1);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());

        
        // verify: Test User Roles have been correctly updated
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        final Representation updatedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        final Model resultsModel =
                this.assertRdf(new ByteArrayInputStream(updatedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        format, 20);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        Assert.assertEquals("Status was not ACTIVE", PoddUserStatus.ACTIVE.getURI(),
                resultsModel.filter(null, PoddRdfConstants.PODD_USER_STATUS, null).objectURI());
        
        Assert.assertEquals("Project Observer Role missing", 1,
                resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI())
                        .objects().size());
        final Collection<Value> objects = resultsModel.filter(null, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, null).objects();
        Assert.assertEquals("Incorrect no. of Role Mapped Objects", 2, objects.size());
    }
    
    
    
    /**
     * Tests removal of a PODD User's Roles. To remove current Roles, send an empty Request. 
     */
    @Test
    public void testRemoveRolesRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.ADMIN.getURI(), null));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:cotton-leaf-morphology")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:tea-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:coffee-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:banana-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:coconut-leaf-study")));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        
        // submit an empty Model to User Roles Service to remove all current Roles
        final ClientResource userRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT_ROLES + testIdentifier));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(new LinkedHashModel(), out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        final Representation modifiedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userRolesClientResource, Method.POST, input, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        final Model model =
                this.assertRdf(new ByteArrayInputStream(modifiedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        RDFFormat.RDFXML, 1);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());

        
        // verify: Test User's Roles have been removed
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        final Representation updatedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        final Model resultsModel =
                this.assertRdf(new ByteArrayInputStream(updatedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        format, 12);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        Assert.assertEquals("Status was not ACTIVE", PoddUserStatus.ACTIVE.getURI(),
                resultsModel.filter(null, PoddRdfConstants.PODD_USER_STATUS, null).objectURI());
        
        Assert.assertEquals("Should be no Roles", 0,
                resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null)
                        .objects().size());
    }
    
}
