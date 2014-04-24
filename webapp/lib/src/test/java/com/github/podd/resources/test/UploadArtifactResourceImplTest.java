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

import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.RestletUtilMediaType;
import com.github.podd.api.test.TestConstants;
import com.github.podd.exception.OntologyNotInProfileException;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 *
 */
public class UploadArtifactResourceImplTest extends AbstractResourceImplTest
{
    /**
     * Test Upload attempt with an artifact that is inconsistent. Results in an HTTP 500 Internal
     * Server Error with detailed error causes in the RDF body.
     */
    @Test
    public void testErrorUploadWithInconsistentArtifactRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat responseFormat = RDFFormat.forMIMEType(mediaType.getName(), RDFFormat.RDFXML);

        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));

        try
        {
            final Representation input =
                    this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_BAD_2_LEAD_INSTITUTES,
                            MediaType.APPLICATION_RDF_XML);

            this.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input, mediaType,
                    Status.SERVER_ERROR_INTERNAL, AbstractResourceImplTest.WITH_ADMIN);
        }
        catch(final ResourceException e)
        {
            // verify: error details
            Assert.assertEquals("Not the expected HTTP status code", Status.SERVER_ERROR_INTERNAL, e.getStatus());

            final Model model = this.assertRdf(uploadArtifactClientResource.getResponseEntity(), responseFormat, 16);

            final Set<Resource> errors = model.filter(null, RDF.TYPE, PODD.ERR_TYPE_TOP_ERROR).subjects();
            Assert.assertEquals("Not the expected number of Errors", 1, errors.size());
            final Resource topError = errors.iterator().next();

            // Resource level error details
            Assert.assertEquals("Not the expected HTTP Status Code", "500",
                    model.filter(topError, PODD.HTTP_STATUS_CODE_VALUE, null).objectString());
            Assert.assertEquals("Not the expected Reason Phrase", "Internal Server Error",
                    model.filter(topError, PODD.HTTP_REASON_PHRASE, null).objectString());
            Assert.assertEquals("Not the expected RDFS:comment", "Error loading artifact to PODD",
                    model.filter(topError, RDFS.COMMENT, null).objectString());

            Assert.assertEquals("Expected 1 child error node", 1, model.filter(topError, PODD.ERR_CONTAINS, null)
                    .size());
            final Resource errorNode = model.filter(topError, PODD.ERR_CONTAINS, null).objectResource();

            // Error cause details
            Assert.assertEquals("Not the expected Exception class",
                    "com.github.podd.exception.InconsistentOntologyException",
                    model.filter(errorNode, PODD.ERR_EXCEPTION_CLASS, null).objectString());

            Assert.assertEquals("Not the expected error source", "urn:temp:inconsistentArtifact:1",
                    model.filter(errorNode, PODD.ERR_SOURCE, null).objectString());

            Assert.assertTrue(
                    "Not the expected inconsistency explanation",
                    model.filter(errorNode, RDFS.COMMENT, null)
                    .objectString()
                    .contains(
                            "Individual urn:temp:object:1960 has more than 1 values for property http://purl.org/podd/ns/poddBase#hasLeadInstitution"));
        }
        finally
        {
            this.releaseClient(uploadArtifactClientResource);
        }
    }

    /**
     * Test Upload attempt with an artifact that is inconsistent. Results in an HTTP 500 Internal
     * Server Error with detailed error causes in the RDF body.
     */
    @Test
    public void testErrorUploadWithNotInOwlDlProfileArtifactRdf() throws Exception
    {
        final Representation input =
                this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_BAD_NOT_OWL_DL,
                        MediaType.APPLICATION_RDF_XML);

        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat responseFormat = RDFFormat.forMIMEType(mediaType.getName(), RDFFormat.RDFXML);

        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));

        try
        {
            this.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input, mediaType,
                    Status.SERVER_ERROR_INTERNAL, AbstractResourceImplTest.WITH_ADMIN);
        }
        catch(final ResourceException e)
        {
            // verify: error details
            Assert.assertEquals("Not the expected HTTP status code", Status.SERVER_ERROR_INTERNAL, e.getStatus());

            final Model model = this.assertRdf(uploadArtifactClientResource.getResponseEntity(), responseFormat, 18);

            final Set<Resource> errors = model.filter(null, RDF.TYPE, PODD.ERR_TYPE_TOP_ERROR).subjects();
            Assert.assertEquals("Not the expected number of Errors", 1, errors.size());
            final Resource topError = errors.iterator().next();

            // Resource level error details
            Assert.assertEquals("Not the expected HTTP Status Code", "500",
                    model.filter(topError, PODD.HTTP_STATUS_CODE_VALUE, null).objectString());
            Assert.assertEquals("Not the expected Reason Phrase", "Internal Server Error",
                    model.filter(topError, PODD.HTTP_REASON_PHRASE, null).objectString());
            Assert.assertEquals("Not the expected RDFS:comment", "Error loading artifact to PODD",
                    model.filter(topError, RDFS.COMMENT, null).objectString());

            // Error cause details
            Assert.assertEquals("Not the expected Exception class", OntologyNotInProfileException.class.getName(),
                    model.filter(null, PODD.ERR_EXCEPTION_CLASS, null).objectString());

            Assert.assertEquals(
                    "Expected error sources not found",
                    2,
                    model.filter(null, PODD.ERR_SOURCE,
                            PODD.VF.createLiteral("ClassAssertion(owl:Individual <mailto:helen.daily@csiro.au>)"))
                            .size());
        }
        finally
        {
            this.releaseClient(uploadArtifactClientResource);
        }
    }

    /**
     * Test unauthenticated access to "upload artifact" leads to an UNAUTHORIZED error.
     */
    @Test
    public void testErrorUploadWithoutAuthentication() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));

        try
        {
            final Representation input =
                    this.buildRepresentationFromResource("/test/artifacts/basicProject-1-internal-object.rdf",
                            MediaType.APPLICATION_RDF_XML);

            final FormDataSet form = new FormDataSet();
            form.setMultipart(true);
            form.getEntries().add(new FormData("file", input));

            uploadArtifactClientResource.post(form, MediaType.TEXT_HTML);
            Assert.fail("Should have thrown a ResourceException with Status Code 401");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
        finally
        {
            this.releaseClient(uploadArtifactClientResource);
        }
    }

    /**
     * Test upload attempt without actual file leads to a BAD_REQUEST error
     */
    @Test
    public void testErrorUploadWithoutFile() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));

        try
        {
            final FormDataSet form = new FormDataSet();
            form.setMultipart(true);

            this.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, form, MediaType.TEXT_HTML,
                    Status.CLIENT_ERROR_BAD_REQUEST, AbstractResourceImplTest.WITH_ADMIN);
            Assert.fail("Should have thrown a ResourceException with Status Code 400");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
        }
        finally
        {
            this.releaseClient(uploadArtifactClientResource);
        }
    }

    /**
     * Test authenticated access to the upload Artifact page in HTML
     */
    @Test
    public void testGetUploadArtifactPageBasicHtml() throws Exception
    {
        // prepare: add an artifact
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));

        try
        {
            final Representation results =
                    this.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null, MediaType.TEXT_HTML,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);

            final String body = this.getText(results);
            Assert.assertTrue(body.contains("Upload new artifact"));
            Assert.assertTrue(body.contains("type=\"file\""));

            this.assertFreemarker(body);
        }
        finally
        {
            this.releaseClient(getArtifactClientResource);
        }
    }

    // @Ignore("When this test is active, it seems to slow down other tests so far that they don't complete normally")
    @Test
    public final void testLoadArtifactConcurrency() throws Exception
    {
        // load test artifact
        final InputStream inputStream4Artifact =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1);

        Assert.assertNotNull("Could not find test resource: " + TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1,
                inputStream4Artifact);

        final String nextTestArtifact = IOUtils.toString(inputStream4Artifact);

        final AtomicInteger threadSuccessCount = new AtomicInteger(0);
        final AtomicInteger perThreadSuccessCount = new AtomicInteger(0);
        final AtomicInteger threadStartCount = new AtomicInteger(0);
        final AtomicInteger perThreadStartCount = new AtomicInteger(0);
        final CountDownLatch openLatch = new CountDownLatch(1);
        // Changing this from 8 to 9 on my machine may be triggering a restlet
        // bug
        final int threadCount = 9;
        final int perThreadCount = 2;
        final CountDownLatch closeLatch = new CountDownLatch(threadCount);
        for(int i = 0; i < threadCount; i++)
        {
            final int number = i;
            final Runnable runner = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        openLatch.await(55000, TimeUnit.MILLISECONDS);
                        threadStartCount.incrementAndGet();
                        for(int j = 0; j < perThreadCount; j++)
                        {
                            perThreadStartCount.incrementAndGet();
                            ClientResource uploadArtifactClientResource = null;

                            try
                            {
                                uploadArtifactClientResource =
                                        new ClientResource(
                                                UploadArtifactResourceImplTest.this
                                                .getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));

                                AbstractResourceImplTest.setupThreading(uploadArtifactClientResource.getContext());

                                final Representation input =
                                        UploadArtifactResourceImplTest.this.buildRepresentationFromResource(
                                                TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1,
                                                MediaType.APPLICATION_RDF_XML);

                                final Representation results =
                                        UploadArtifactResourceImplTest.this.doTestAuthenticatedRequest(
                                                uploadArtifactClientResource, Method.POST, input,
                                                MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK,
                                                AbstractResourceImplTest.WITH_ADMIN);

                                // verify: results (expecting the added
                                        // artifact's ontology IRI)
                                final String body = UploadArtifactResourceImplTest.this.getText(results);

                                final Collection<InferredOWLOntologyID> ontologyIDs =
                                        OntologyUtils.stringToOntologyID(body, RDFFormat.RDFXML);

                                Assert.assertNotNull("No ontology IDs in response", ontologyIDs);
                                Assert.assertEquals("More than 1 ontology ID in response", 1, ontologyIDs.size());
                                Assert.assertTrue("Ontology ID not of expected format", ontologyIDs.iterator()
                                        .next().toString().contains("artifact:1:version:1"));
                                perThreadSuccessCount.incrementAndGet();
                            }
                            finally
                            {
                                UploadArtifactResourceImplTest.this.releaseClient(uploadArtifactClientResource);
                            }
                        }
                        threadSuccessCount.incrementAndGet();
                    }
                    catch(final Throwable e)
                    {
                        e.printStackTrace();
                        Assert.fail("Failed in test: " + number);
                    }
                    finally
                    {
                        closeLatch.countDown();
                    }
                }
            };
            new Thread(runner, "TestThread" + number).start();
        }
        // all threads are waiting on the latch.
        openLatch.countDown(); // release the latch
        // all threads are now running concurrently.
        closeLatch.await(50000, TimeUnit.MILLISECONDS);
        // closeLatch.await();
        // Verify that there were no startup failures
        Assert.assertEquals("Some threads did not all start successfully", threadCount, threadStartCount.get());
        Assert.assertEquals("Some thread loops did not start successfully", perThreadCount * threadCount,
                perThreadStartCount.get());
        // Verify that there were no failures, as the count is only incremented
        // for successes, where
        // the closeLatch must always be called, even for failures
        Assert.assertEquals("Some thread loops did not complete successfully", perThreadCount * threadCount,
                perThreadSuccessCount.get());
        Assert.assertEquals("Some threads did not complete successfully", threadCount, threadSuccessCount.get());
    }

    /**
     * Test successful upload of a new artifact file while authenticated with the admin role.
     * Expects an HTML response.
     */
    @Test
    public void testUploadArtifactBasicHtml() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        try
        {
            final Representation input =
                    this.buildRepresentationFromResource("/test/artifacts/basicProject-1-internal-object.rdf",
                            MediaType.APPLICATION_RDF_XML);

            final FormDataSet form = new FormDataSet();
            form.setMultipart(true);
            form.getEntries().add(new FormData("file", input));

            final Representation results =
                    this.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, form,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);

            // TODO: verify results once a proper success page is incorporated.
            final String body = this.getText(results);
            Assert.assertTrue(body.contains("Project successfully uploaded"));
            this.assertFreemarker(body);
        }
        finally
        {
            this.releaseClient(uploadArtifactClientResource);
        }
    }

    /**
     * Test successful upload of a new artifact file while authenticated with the admin role.
     * Expects a plain text response.
     */
    @Test
    public void testUploadArtifactBasicRdf() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        try
        {
            final Representation input =
                    this.buildRepresentationFromResource("/test/artifacts/basicProject-1-internal-object.rdf",
                            MediaType.APPLICATION_RDF_XML);

            final Representation results =
                    this.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input,
                            MediaType.TEXT_PLAIN, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);

            // verify: results (expecting the added artifact's ontology IRI)
            final String body = this.getText(results);
            Assert.assertTrue(body.contains("http://"));
            Assert.assertFalse(body.contains("html"));
            Assert.assertFalse(body.contains("\n"));
        }
        finally
        {
            this.releaseClient(uploadArtifactClientResource);
        }
    }

    /**
     * Test successful upload of a new artifact file while authenticated with the admin role.
     * Expects a plain text response.
     */
    @Test
    public void testUploadArtifactBasicRdfWithFormData() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));

        try
        {
            final Representation input =
                    this.buildRepresentationFromResource("/test/artifacts/basicProject-1-internal-object.rdf",
                            MediaType.APPLICATION_RDF_XML);

            final FormDataSet form = new FormDataSet();
            form.setMultipart(true);
            form.getEntries().add(new FormData("file", input));

            final Representation results =
                    this.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, form,
                            MediaType.TEXT_PLAIN, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);

            // verify: results (expecting the added artifact's ontology IRI)
            final String body = this.getText(results);
            Assert.assertTrue(body.contains("http://"));
            Assert.assertFalse(body.contains("html"));
            Assert.assertFalse(body.contains("\n"));
        }
        finally
        {
            this.releaseClient(uploadArtifactClientResource);
        }
    }

    /**
     * Test successful upload of a new artifact file while authenticated with the admin role.
     * Expects a plain text response.
     */
    @Test
    public void testUploadArtifactBasicTurtle() throws Exception
    {
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));

        try
        {
            final Representation input =
                    this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_TTL_1_INTERNAL_OBJECT,
                            MediaType.APPLICATION_RDF_TURTLE);

            final Representation results =
                    this.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input,
                            MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);

            // verify: results (expecting the added artifact's ontology IRI)
            final String body = this.getText(results);

            final Collection<InferredOWLOntologyID> ontologyIDs =
                    OntologyUtils.stringToOntologyID(body, RDFFormat.TURTLE);

            Assert.assertNotNull("No ontology IDs in response", ontologyIDs);
            Assert.assertEquals("More than 1 ontology ID in response", 1, ontologyIDs.size());
            Assert.assertTrue("Ontology ID not of expected format",
                    ontologyIDs.iterator().next().toString().contains("artifact:1:version:1"));
        }
        finally
        {
            this.releaseClient(uploadArtifactClientResource);
        }
    }

    @Test
    public void testUploadNewProjectTemplate() throws Exception
    {
        // Rio.write(Rio.parse(this.getClass().getResourceAsStream("/test/artifacts/new-project-template.rj"),
        // "", RDFFormat.RDFJSON), System.out, RDFFormat.RDFJSON);

        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        try
        {
            final Representation input =
                    this.buildRepresentationFromResource("/test/artifacts/new-project-template.rj",
                            RestletUtilMediaType.APPLICATION_RDF_JSON);

            final Representation results =
                    this.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input,
                            RestletUtilMediaType.APPLICATION_RDF_JSON, Status.SUCCESS_OK,
                            AbstractResourceImplTest.WITH_ADMIN);

            // verify: results (expecting the added artifact's ontology IRI)
            final String body = this.getText(results);

            final Model model = this.assertRdf(new StringReader(body), RDFFormat.RDFJSON, 6);

            Assert.assertEquals(3, model.subjects().size());
            Assert.assertEquals(3, model.predicates().size());
            Assert.assertEquals(5, model.objects().size());
        }
        finally
        {
            this.releaseClient(uploadArtifactClientResource);
        }
    }

}
