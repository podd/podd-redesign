/**
 * 
 */
package com.github.podd.ontology.test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObject;

/**
 * Test for SparqlQuerySpike.java
 * 
 * @author kutila
 * 
 */
public class SparqlQueryTest extends AbstractOntologyTest
{
    
    private SparqlQuerySpike testSpike;
    
    protected RepositoryConnection conn;
    
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        this.testSpike = new SparqlQuerySpike();
    }
    
    @Override
    @After
    public void tearDown() throws Exception
    {
        if(this.conn != null)
        {
            this.conn.rollback();
            this.conn.close();
        }
        
        super.tearDown();
    }
    
    /**
     * Test that all objects are linked to "PoddObject".
     */
    @Ignore
    @Test
    public void testAllObjectsAreLinkedToPoddObject() throws Exception
    {
        Assert.fail("TODO");
    }

    /**
     * Test retrieving objects sorted by "weight" allocated in the schema ontologies.
     */
    @Ignore
    @Test
    public void testRetrieveObjectsSortedByWeight() throws Exception
    {
        Assert.fail("TODO");
    }
    
    /**
     * Test the performance of above queries. Move this to a separate test class.
     */
    @Ignore
    @Test
    public void testPerformance() throws Exception
    {
        Assert.fail("TODO");
    }
    
    /**
     * Test retrieve information about Top Object
     */
    @Test
    public void testRetrieveTopObjectDetails() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-1.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        
        final Map<String, Object> map =
                this.testSpike.getTopObjectDetails(this.conn, contextUri, nextOntologyID.getInferredOntologyIRI()
                        .toOpenRDFURI());
        
        Assert.assertEquals("Incorrect number of statements about Top Object", 13, map.size());
        Assert.assertNotNull("Top Object's URI was null", map.get("objecturi"));
        Assert.assertTrue("URI start not expected format",
                map.get("objecturi").toString().startsWith("http://purl.org/podd/"));
        Assert.assertTrue("Missing NotPublished status",
                map.containsValue("http://purl.org/podd/ns/poddBase#NotPublished"));
    }
    
    /**
     * Test retrieve list of level 1 objects in a project
     */
    @Test
    public void testRetrieveDirectChildren() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        
        URI parentObjectURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130205/object:2966");
        
        final List<PoddObject> map =
                this.testSpike.getContainedObjects(parentObjectURI, this.conn, contextUri, nextOntologyID.getInferredOntologyIRI()
                        .toOpenRDFURI());

        // print results
        for (PoddObject poddObj : map)
        {
            System.out.println("   " + poddObj.getLabel() + " : (" + poddObj.getUri().stringValue() +
                    ") <contained-by> " + poddObj.getContainer().stringValue());
        }
        
        
        Assert.assertEquals("Incorrect number of statements about Top Object", 5, map.size());
        // Assert.assertNotNull("Top Object's URI was null", map.get("objecturi"));
        // Assert.assertTrue("URI start not expected format",
        // map.get("objecturi").toString().startsWith("http://purl.org/podd/"));
        // Assert.assertTrue("Missing NotPublished status",map.containsValue("http://purl.org/podd/ns/poddBase#NotPublished"));
        
        
//        this.printContents(this.conn, ValueFactoryImpl.getInstance().createURI(
//                "http://purl.org/podd/basic-2-20130206/artifact:1:version:1"));
//        this.printContents(this.conn, ValueFactoryImpl.getInstance().createURI(
//                "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/basic-2-20130206/artifact:1:version:1"));
//        this.printContexts(this.conn);
    }
    
    /**
     * Test retrieving all Top Objects of an artifact. 
     */
    @Test
    public void testGetTopObjectsWithOnlyOne() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-1.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        List<String> topObjects = this.testSpike.getTopObjects(conn, contextUri);
        
        Assert.assertEquals("Should be only 1 top object", 1, topObjects.size());
        Assert.assertEquals("Not the expected top object", "http://purl.org/podd/basic-1-20130205/object:2966", 
                topObjects.get(0));
    }

    /**
     * Test retrieving all Top Objects of an artifact. 
     */
    @Test
    public void testGetTopObjectsWithMultiple() throws Exception
    {
        final String testResourcePath = "/test/artifacts/3-topobjects.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        List<String> topObjects = this.testSpike.getTopObjects(conn, contextUri);
        
        Assert.assertEquals("Should be only 3 top object", 3, topObjects.size());
        Assert.assertTrue("Missing top object", topObjects.contains("http://purl.org/podd/basic-1-20130205/object:2966"));
        Assert.assertTrue("Missing top object", topObjects.contains("http://purl.org/podd/basic-1-20130205/object:2977"));
        Assert.assertTrue("Missing top object", topObjects.contains("http://purl.org/podd/basic-1-20130205/object:2988"));
    }
    
    
    /**
     * Test that direct imports are correctly identified.
     * 
     * Originally from from PoddSesameManagerImpl.java
     */
    @Test
    public void testOntologyImports() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-1.rdf";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.RDFXML);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        Assert.assertEquals("Not the expected number of statements in Repository", 33, this.conn.size(contextUri));
        
        final Set<IRI> imports = this.testSpike.getDirectImports(this.conn, contextUri);
        Assert.assertEquals("Podd-Base should have 2 imports", 2, imports.size());
        Assert.assertTrue("Missing import", imports.contains(IRI.create("http://purl.org/podd/ns/poddBase")));
        Assert.assertTrue("Missing import", imports.contains(IRI.create("http://purl.org/podd/ns/poddScience")));
    }
    
}
