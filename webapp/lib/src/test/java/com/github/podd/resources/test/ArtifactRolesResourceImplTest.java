/**
 *
 */
package com.github.podd.resources.test;

import java.io.StringReader;
import java.util.Collection;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.github.ansell.restletutils.RestletUtilMediaType;
import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 *
 */
public class ArtifactRolesResourceImplTest extends AbstractResourceImplTest
{

    @Test
    public void testGetArtifactRolesBasicHtml() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);

        final ClientResource getArtifactRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_ROLES));

        try
        {
            getArtifactRolesClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);

            final Representation results =
                    this.doTestAuthenticatedRequest(getArtifactRolesClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);

            final String body = this.getText(results);

            // verify:
            // System.out.println(body);
            Assert.assertTrue("Page does not identify Administrator", body.contains("Administrator"));
            Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));

            this.assertFreemarker(body);
        }
        finally
        {
            this.releaseClient(getArtifactRolesClientResource);
        }
    }

    @Test
    public void testGetArtifactRolesBasicRdf() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);

        final ClientResource getArtifactRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_ROLES));

        try
        {
            getArtifactRolesClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);

            final Representation results =
                    this.doTestAuthenticatedRequest(getArtifactRolesClientResource, Method.GET, null,
                            RestletUtilMediaType.APPLICATION_RDF_JSON, Status.SUCCESS_OK,
                            AbstractResourceImplTest.WITH_ADMIN);

            final String body = this.getText(results);

            // verify:
            // System.out.println(body);
            Assert.assertTrue("Page does not identify Administrator", body.contains("Administrator"));
            Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));

            this.assertFreemarker(body);

            final Model model = this.assertRdf(new StringReader(body), RDFFormat.RDFJSON, 6);

            final Map<RestletUtilRole, Collection<String>> roles = PoddRoles.extractRoleMappingsArtifact(model);

            Assert.assertEquals(2, roles.size());
            Assert.assertTrue(roles.containsKey(PoddRoles.PROJECT_ADMIN));
            Assert.assertEquals(1, roles.get(PoddRoles.PROJECT_ADMIN).size());
            Assert.assertEquals(RestletTestUtils.TEST_ADMIN_USERNAME, roles.get(PoddRoles.PROJECT_ADMIN).iterator()
                    .next());
            Assert.assertTrue(roles.containsKey(PoddRoles.PROJECT_PRINCIPAL_INVESTIGATOR));
            Assert.assertEquals(1, roles.get(PoddRoles.PROJECT_PRINCIPAL_INVESTIGATOR).size());
            Assert.assertEquals(RestletTestUtils.TEST_ADMIN_USERNAME,
                    roles.get(PoddRoles.PROJECT_PRINCIPAL_INVESTIGATOR).iterator().next());
        }
        finally
        {
            this.releaseClient(getArtifactRolesClientResource);
        }
    }

}
