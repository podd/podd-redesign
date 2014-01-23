/**
 * 
 */
package com.github.podd.resources.test;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class UserSearchResourceImplTest extends AbstractResourceImplTest
{
    
    @Test
    public void testSearchUsersRdf() throws Exception
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
        
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final ClientResource userSearchClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_SEARCH));
        try
        {
            userSearchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCHTERM, "anoth");
            
            final Representation results =
                    this.doTestAuthenticatedRequest(userSearchClientResource, Method.GET, null, mediaType,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final Model resultsModel = this.assertRdf(results, format, 2);
            
            // verify:
            Assert.assertEquals("Not the expected User", "Test User, CSIRO", resultsModel
                    .filter(null, RDFS.LABEL, null).objectString());
        }
        finally
        {
            this.releaseClient(userSearchClientResource);
        }
    }
    
}
