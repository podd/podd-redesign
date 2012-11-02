package com.github.podd.prototype.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Assert;
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

public class PoddServletHelperTest
{
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    PoddServletHelper helper = null;
    
    @Before
    public void setUp() throws Exception
    {
        try
        {
            this.helper = new PoddServletHelper();
            this.helper.setUp(false, null, null);
            this.helper.loadSchemaOntologies();
        }
        catch(final Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }
    
    @After
    public void tearDown() throws Exception
    {
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
        
        // validate inferred statements
        Assert.assertNotNull(resultRDF);
        Assert.assertFalse(resultRDF.contains("urn:temp:"));
        Assert.assertTrue(resultRDF.contains(artifactUniqueIRI.toString()));
        final URI context = IRI.create("urn:context").toOpenRDFURI();
        final RepositoryConnection repoConn = this.loadDataToNewRepository(resultRDF, mimeType, context);
        Assert.assertTrue(repoConn.size(context) > 29);
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
    
    @Test
    public void testEditArtifactWithMerge() throws Exception
    {
        final boolean isReplace = false;
        // first, load an artifact using the inner-load method
        InputStream in = this.getClass().getResourceAsStream("/test/artifacts/editableProject-1.rdf");
        Assert.assertNotNull("Resource was not found", in);
        final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
        final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifactInternal(in, mimeType);
        final URI artifactUniqueIRI = addedRDF.getOntologyIRI().toOpenRDFURI();
        
        // edit it
        in = this.getClass().getResourceAsStream("/test/artifacts/editableProject-1-part.rdf");
        Assert.assertNotNull("Resource was not found", in);
        
        final String editedArtifactURI =
                this.helper.editArtifact(artifactUniqueIRI.stringValue(), in, mimeType, isReplace);
        
        // check the modifications were persisted
        final String resultRDF = this.helper.getArtifact(editedArtifactURI, mimeType, false);
        // System.out.println(resultRDF);
        
        Assert.assertNotNull(resultRDF);
        Assert.assertFalse(resultRDF.contains("urn:temp:"));
        Assert.assertTrue(resultRDF.contains(artifactUniqueIRI.toString()));
        Assert.assertTrue(resultRDF.contains("John.Doe@csiro.au"));
        final URI context = IRI.create("urn:context").toOpenRDFURI();
        final RepositoryConnection repoConn = this.loadDataToNewRepository(resultRDF, mimeType, context);
        
        // with merge, the "OWL Comment" from the ontology is retained
        Assert.assertEquals(33, repoConn.size(context));
    }
    
    @Test
    public void testEditArtifactWithReplace() throws Exception
    {
        final boolean isReplace = true;
        // first, load an artifact using the inner-load method
        InputStream in = this.getClass().getResourceAsStream("/test/artifacts/editableProject-1.rdf");
        Assert.assertNotNull("Resource was not found", in);
        final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
        final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifactInternal(in, mimeType);
        final URI artifactUniqueIRI = addedRDF.getOntologyIRI().toOpenRDFURI();
        
        // edit it
        in = this.getClass().getResourceAsStream("/test/artifacts/editableProject-1-part.rdf");
        Assert.assertNotNull("Resource was not found", in);
        
        final String editedArtifactURI =
                this.helper.editArtifact(artifactUniqueIRI.stringValue(), in, mimeType, isReplace);
        
        // check the modifications were persisted
        final String resultRDF = this.helper.getArtifact(editedArtifactURI, mimeType, false);
        // System.out.println(resultRDF);
        
        Assert.assertNotNull(resultRDF);
        Assert.assertFalse(resultRDF.contains("urn:temp:"));
        Assert.assertTrue(resultRDF.contains(artifactUniqueIRI.toString()));
        Assert.assertTrue(resultRDF.contains("John.Doe@csiro.au"));
        final URI context = IRI.create("urn:context").toOpenRDFURI();
        final RepositoryConnection repoConn = this.loadDataToNewRepository(resultRDF, mimeType, context);
        
        // with replace, the "OWL Comment" from the ontology is removed
        Assert.assertEquals(32, repoConn.size(context));
    }
    
    @Test
    public void testIncrementVersion() throws Exception
    {
        final String[] in =
                { "", "abc", "alpha:1", "http://example.org/ac-d/art%3A55", "http://example.org/ac-d/art%3A99999",
                        "http://example.org/permanenturl/431fd79a-7c8d-487c-9df5-09f4cd386249/artifact%3Aversion%3A1" };
        final String[] expected =
                { "1", "abc1", "alpha:11", "http://example.org/ac-d/art%3A56", "http://example.org/ac-d/art%3A100000",
                        "http://example.org/permanenturl/431fd79a-7c8d-487c-9df5-09f4cd386249/artifact%3Aversion%3A2" };
        
        for(int i = 0; i < in.length; i++)
        {
            final String extracted = PoddServletHelper.incrementVersion(in[i]);
            Assert.assertEquals(expected[i], extracted);
        }
        
    }
    
    @Test
    public void testGetInferredOWLOntologyIDForArtifact() throws Exception
    {
        // first, load an artifact using the inner-load method
        final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
        Assert.assertNotNull("Resource was not found", in);
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
    
    // ----- helper methods -----
    
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
    
}
