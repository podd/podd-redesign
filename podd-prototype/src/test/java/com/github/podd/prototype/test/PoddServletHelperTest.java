package com.github.podd.prototype.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.prototype.InferredOWLOntologyID;
import com.github.podd.prototype.PoddServlet;
import com.github.podd.prototype.PoddServletHelper;

public class PoddServletHelperTest extends TestCase
{
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    PoddServletHelper helper = null;
    
    @Override
    @Before
    protected void setUp() throws Exception
    {
        super.setUp();
        try
        {
            this.helper = new PoddServletHelper();
            this.helper.setUp();
            this.helper.loadSchemaOntologies();
        }
        catch(final Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    @After
    protected void tearDown() throws Exception
    {
        super.tearDown();
        this.helper.tearDown();
    }
    
    @Test
    public void testLoadInvalidArtifact() throws Exception
    {
        final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/cookie.txt");
        try
        {
            this.helper.loadPoddArtifact(in, PoddServlet.MIME_TYPE_RDF_XML);
            Assert.fail("Should have thrown an exception here");
        }
        catch(final RDFParseException e)
        {
            Assert.assertNotNull(e);
        }
    }
    
    /**
     * Tests loading a simple PODD artifact. Response is validated based on statement count and no
     * longer having "urn:temp:".
     * 
     * @throws Exception
     */
    @Test
    public void testLoadArtifact() throws Exception
    {
        final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
        final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
        
        final String addedRdf = this.helper.loadPoddArtifact(in, mimeType);
        
        Assert.assertNotNull(addedRdf);
        Assert.assertFalse(addedRdf.contains("urn:temp:"));
        
        final URI context = IRI.create("urn:context").toOpenRDFURI();
        final RepositoryConnection repoConn = this.loadDataToNewRepository(addedRdf, mimeType, context);
        Assert.assertEquals(29, repoConn.size(context));
    }
    
    @Test
    public void testGetNonExistentArtifact() throws Exception
    {
        final String artifactUniqueIRI = "http://purl.org/nosuch/artifact:1";
        
        try
        {
            this.helper.getArtifact(artifactUniqueIRI, PoddServlet.MIME_TYPE_RDF_XML, false);
            Assert.fail("Should have thrown an exception");
        }
        catch(final Exception e)
        {
            Assert.assertNotNull(e);
            Assert.assertTrue(e.getMessage().contains("not found"));
        }
    }
    
    @Test
    public void testGetArtifactRdfXml() throws Exception
    {
        // first, load an artifact using the inner-load method
        final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
        final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
        final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifactInternal(in, mimeType);
        final URI artifactUniqueIRI = addedRDF.getOntologyIRI().toOpenRDFURI();
        
        // now retrieve it via the Helper
        final String resultRDF = this.helper.getArtifact(artifactUniqueIRI.toString(), mimeType, false);
        
        Assert.assertNotNull(resultRDF);
        Assert.assertFalse(resultRDF.contains("urn:temp:"));
        Assert.assertTrue(resultRDF.contains(artifactUniqueIRI.toString()));
        final URI context = IRI.create("urn:context").toOpenRDFURI();
        final RepositoryConnection repoConn = this.loadDataToNewRepository(resultRDF, mimeType, context);
        Assert.assertEquals(29, repoConn.size(context));
    }
    
    @Test
    public void testGetArtifactWithInferredStatements() throws Exception
    {
        // first, load an artifact using the inner-load method
        final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
        final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
        final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifactInternal(in, mimeType);
        final URI artifactUniqueIRI = addedRDF.getOntologyIRI().toOpenRDFURI();
        
        // now retrieve it via the Helper
        final String resultRDF = this.helper.getArtifact(artifactUniqueIRI.toString(), mimeType, true);
        
        // TODO - validate inferred statements. e.g. statement count should be higher than 29
        Assert.assertNotNull(resultRDF);
        Assert.assertFalse(resultRDF.contains("urn:temp:"));
        Assert.assertTrue(resultRDF.contains(artifactUniqueIRI.toString()));
        final URI context = IRI.create("urn:context").toOpenRDFURI();
        final RepositoryConnection repoConn = this.loadDataToNewRepository(resultRDF, mimeType, context);
        Assert.assertEquals(29, repoConn.size(context));
    }
    
    @Test
    public void testDeleteArtifact() throws Exception
    {
        // first, load an artifact using the inner-load method
        final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
        final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
        final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifactInternal(in, mimeType);
        final URI artifactUniqueIRI = addedRDF.getOntologyIRI().toOpenRDFURI();
        
        // get artifact to verify it exists
        final String resultRDF = this.helper.getArtifact(artifactUniqueIRI.toString(), mimeType, false);
        Assert.assertNotNull(resultRDF);
        
        // delete artifact
        this.helper.deleteArtifact(artifactUniqueIRI.toString());
        
        // get artifact should now give an error
        try
        {
            this.helper.getArtifact(artifactUniqueIRI.toString(), mimeType, false);
            Assert.fail("Should have thrown an exception");
        }
        catch(final Exception e)
        {
            Assert.assertNotNull(e);
            Assert.assertTrue(e.getMessage().contains("not found"));
        }
    }
    
    @Test
    public void testDeleteNonExistentArtifact() throws Exception
    {
        final String artifactUniqueIRI = "http://purl.org/nosuch/artifact:1";
        
        try
        {
            this.helper.deleteArtifact(artifactUniqueIRI);
            Assert.fail("Should have thrown an exception");
        }
        catch(final Exception e)
        {
            Assert.assertNotNull(e);
            Assert.assertTrue(e.getMessage().contains("not found"));
        }
    }
    
    private RepositoryConnection loadDataToNewRepository(final String data, final String mimeType, final URI context)
        throws Exception
    {
        // create a temporary Repository
        final Repository tempRepository = new SailRepository(new MemoryStore());
        tempRepository.initialize();
        final RepositoryConnection tempRepositoryConnection = tempRepository.getConnection();
        tempRepositoryConnection.setAutoCommit(false);
        
        // add data to Repository
        tempRepositoryConnection.add(new ByteArrayInputStream(data.getBytes()), "",
                Rio.getParserFormatForMIMEType(mimeType), context);
        tempRepositoryConnection.commit();
        
        return tempRepositoryConnection;
    }
    
    @Test
    public void testGetInferredOWLOntologyIDForArtifact() throws Exception
    {
        // first, load an artifact using the inner-load method
        final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
        final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
        final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifactInternal(in, mimeType);
        final URI artifactUniqueIRI = addedRDF.getOntologyIRI().toOpenRDFURI();
        
        final RepositoryConnection conn = this.helper.getRepositoryConnection();

        // test with a non-existent artifact
        InferredOWLOntologyID id = this.helper.getInferredOWLOntologyIDForArtifact("http://nosuch:ontology:1", conn);
        Assert.assertNull(id.getInferredOntologyIRI());
        Assert.assertNull(id.getVersionIRI());

        // test with the added artifact
        id = this.helper.getInferredOWLOntologyIDForArtifact(artifactUniqueIRI.stringValue(), conn);

        conn.rollback();
        this.helper.returnRepositoryConnection(conn);
    }
    
    @Test
    public void testExtractUri() throws Exception
    {
        final String[] in =
                { "http/www.google.com", "http/130.198.34.55:9090/permanenturl", "https/thebank.org/myaccount#55",
                        "https/thebank.org/myaccount%2355",
                        "http/example.org/permanenturl/34cc1c8e-0ece-49f4-ac51/artifact:1",
                        "http/example.org/permanenturl/34cc1c8e-0ece-49f4-ac51-e17aa34648e4/artifact%3A1",
                        "http/example.org/alpha/artifact:1:0:5:22" };
        final String[] expected =
                { "http://www.google.com", "http://130.198.34.55:9090/permanenturl",
                        "https://thebank.org/myaccount%2355", "https://thebank.org/myaccount%2355",
                        "http://example.org/permanenturl/34cc1c8e-0ece-49f4-ac51/artifact%3A1",
                        "http://example.org/permanenturl/34cc1c8e-0ece-49f4-ac51-e17aa34648e4/artifact%3A1",
                        "http://example.org/alpha/artifact%3A1%3A0%3A5%3A22" };
        
        for(int i = 0; i < in.length; i++)
        {
            final String extracted = PoddServletHelper.extractUri(in[i]);
            Assert.assertEquals(expected[i], extracted);
        }
        
        try
        {
            PoddServletHelper.extractUri("htp/a");
        }
        catch(final URISyntaxException e)
        {
            Assert.assertNotNull(e);
        }
    }
    
}
