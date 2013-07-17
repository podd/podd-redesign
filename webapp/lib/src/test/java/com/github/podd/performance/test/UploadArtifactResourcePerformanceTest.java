/**
 * 
 */
package com.github.podd.performance.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.resources.test.AbstractResourceImplTest;
import com.github.podd.utils.PoddWebConstants;

/**
 * Parameterized class for testing the performance of loading PODD artifacts. Since this class
 * extends from AbstractResourceImplTest, the PODD Web Application is restarted for each test run.
 * 
 * @author kutila
 */
@Ignore
@RunWith(value = Parameterized.class)
public class UploadArtifactResourcePerformanceTest extends AbstractResourceImplTest
{
    
    /**
     * log4j logger which writes to the statistics file.
     */
    private final Logger statsLogger = LoggerFactory.getLogger("statsLogger");
    
    /**
     * parameter: name of test artifact file
     */
    private String filename;
    
    /**
     * parameter: type of test artifact file
     */
    private MediaType mediaType;
    
    /**
     * Parameterized constructor
     * 
     * @param number
     */
    public UploadArtifactResourcePerformanceTest(final String filename, final MediaType mediaType)
    {
        super();
        
        // increase test timeout
        super.timeout = new Timeout(30000*1000);
        
        this.filename = filename;
        this.mediaType = mediaType;
    }
    
    @Parameters
    public static Collection<Object[]> data()
    {
        final Object[][] data =
                new Object[][] {
//                        { "/test/artifacts/basic-20130206.ttl", MediaType.APPLICATION_RDF_TURTLE },
//                        { "/test/artifacts/project-temp-00010.ttl", MediaType.APPLICATION_RDF_TURTLE },
//                        { "/test/artifacts/project-temp-00100.ttl", MediaType.APPLICATION_RDF_TURTLE },
//                        { "/test/artifacts/project-temp-01000.ttl", MediaType.APPLICATION_RDF_TURTLE },
                        
                        // fails in PURL generation
                        //{ "/test/artifacts/project-temp-10000.ttl", MediaType.APPLICATION_RDF_TURTLE },
                        
                        { "/test/artifacts/project-temp-00010.rdf", MediaType.APPLICATION_RDF_XML },
                        { "/test/artifacts/project-temp-00100.rdf", MediaType.APPLICATION_RDF_XML },
                        { "/test/artifacts/project-temp-01000.rdf", MediaType.APPLICATION_RDF_XML },
                        
                        // fails in PURL generation
                        //{ "/test/artifacts/project-temp-10000.rdf", MediaType.APPLICATION_RDF_XML },
                        
//                        { "/test/artifacts/project-purl-01000.rdf", MediaType.APPLICATION_RDF_XML },
//                        { "/test/artifacts/project-purl-10000.rdf", MediaType.APPLICATION_RDF_XML },
                        
                        // fails
                        //{ "/test/artifacts/project-purl-20000.rdf", MediaType.APPLICATION_RDF_XML },
                        
                };
        return Arrays.asList(data);
    }
    
    /**
     * Test successful upload of a new artifact file while authenticated with the admin role.
     * Expects a plain text response.
     */
    @Test
    public void testUploadArtifactBasicRdf() throws Exception
    {
        final long startedAt = System.currentTimeMillis();
        
        final ClientResource uploadArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        
        final Representation input = this.buildRepresentationFromResource(this.filename, this.mediaType);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input,
                        MediaType.TEXT_PLAIN, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // verify: results (expecting the added artifact's ontology IRI)
        final String body = results.getText();
        Assert.assertTrue(body.contains("http://"));
        Assert.assertFalse(body.contains("html"));
        Assert.assertFalse(body.contains("\n"));
        
        
        // write statistics
        final StringBuilder statsMsg = new StringBuilder();
        statsMsg.append(this.filename.substring(this.filename.lastIndexOf('/') + 1) + ",");
        
        // time to load (ms)
        statsMsg.append((System.currentTimeMillis() - startedAt));
        statsMsg.append(',');
        
        // ontology asserted statement count
        final int statementCount = this.getStatementCount(body);
        statsMsg.append(statementCount);
        statsMsg.append('\n');
        
        this.statsLogger.info(statsMsg.toString());
    }
    
    public int getStatementCount(String artifactUri) throws Exception
    {
        // retrieve artifact
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                        MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // load into a Model and find statement count
        final InputStream input = new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8));
        final Model model = Rio.parse(input, "", RDFFormat.TURTLE);

        return model.size();
    }
    
}
