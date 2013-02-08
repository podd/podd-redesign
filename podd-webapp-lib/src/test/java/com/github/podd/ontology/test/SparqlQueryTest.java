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
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObject;
import com.github.podd.utils.SparqlQueryHelper;

/**
 * Test for SparqlQueryHelper.java
 * 
 * @author kutila
 * 
 */
public class SparqlQueryTest extends AbstractOntologyTest
{
    
    private SparqlQueryHelper sparqlHelper;
    
    protected RepositoryConnection conn;
    
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        this.sparqlHelper = new SparqlQueryHelper();
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
    public void testGetTopObjectDetails() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-1.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        
        final Map<String, List<Value>> map =
                this.sparqlHelper.getTopObjectDetails(this.conn, contextUri, nextOntologyID.getInferredOntologyIRI()
                        .toOpenRDFURI());
        
        Assert.assertEquals("Incorrect number of statements about Top Object", 13, map.size());
        Assert.assertNotNull("Top Object's URI was null", map.get("objecturi"));
        
        Assert.assertEquals("Not the expected top object URI",
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130205/object:2966"),
                map.get("objecturi").get(0));
        Assert.assertEquals("Publication status not as expected",
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddBase#NotPublished"),
                map.get("http://purl.org/podd/ns/poddBase#hasPublicationStatus").get(0));
    }
    
    /**
     * Test retrieve all direct statements about a given object
     */
    @Test
    public void testGetAllDirectStatements() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-1.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        
        final URI objectUri =
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130205/object:2966");
        
        final Map<String, List<Value>> map =
                this.sparqlHelper.getAllDirectStatements(objectUri, this.conn, contextUri, nextOntologyID
                        .getInferredOntologyIRI().toOpenRDFURI());
        
        Assert.assertEquals("Incorrect number of statements about object", 12, map.size());
        Assert.assertEquals("Lead institution not as expected", "CSIRO HRPPC",
                ((Literal)map.get("http://purl.org/podd/ns/poddBase#hasLeadInstitution").get(0)).stringValue());
        Assert.assertEquals("Publication status not as expected",
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddBase#NotPublished"),
                map.get("http://purl.org/podd/ns/poddBase#hasPublicationStatus").get(0));
    }
    
    /**
     * Test retrieve list of direct children of the Top Object
     */
    @Test
    public void testgetContainedObjectsFromTopObject() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        
        final URI parentObjectURI =
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130205/object:2966");
        
        final List<PoddObject> childObjectList =
                this.sparqlHelper.getContainedObjects(parentObjectURI, false, this.conn, contextUri, nextOntologyID
                        .getInferredOntologyIRI().toOpenRDFURI());
        
        final String[] expectedLabels =
                { "Demo Analysis", "Demo Process 1", "Demo Process 2", "Demo Project Plan", "Demo investigation" };
        
        Assert.assertEquals("Incorrect number of direct child objects", 5, childObjectList.size());
        for(int i = 0; i < childObjectList.size(); i++)
        {
            Assert.assertEquals("Incorrect direct parent", parentObjectURI, childObjectList.get(i).getDirectParent());
            Assert.assertEquals("Incorrect object at position", expectedLabels[i], childObjectList.get(i).getLabel());
        }
    }
    
    /**
     * Test retrieve list of direct children of an inner object
     */
    @Test
    public void testgetContainedObjectsFromInnerObject() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        
        final URI parentObjectURI =
                ValueFactoryImpl.getInstance().createURI(
                        "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Investigation");
        
        final List<PoddObject> childObjectList =
                this.sparqlHelper.getContainedObjects(parentObjectURI, false, this.conn, contextUri, nextOntologyID
                        .getInferredOntologyIRI().toOpenRDFURI());
        
        final String[] expectedLabels = { "Demo material", "Squeekee material" , "my treatment 1"};
        
        Assert.assertEquals("Incorrect number of direct child objects", 3, childObjectList.size());
        for(int i = 0; i < childObjectList.size(); i++)
        {
            Assert.assertEquals("Incorrect direct parent", parentObjectURI, childObjectList.get(i).getDirectParent());
            Assert.assertEquals("Incorrect object at position", expectedLabels[i], childObjectList.get(i).getLabel());
        }
    }
    
    /**
     * Test retrieve list of direct children of the Top Object
     */
    @Test
    public void testgetContainedObjectsFromTopObjectWithRecursion() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        
        final URI parentObjectURI =
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130205/object:2966");
        
        final List<PoddObject> childObjectList =
                this.sparqlHelper.getContainedObjects(parentObjectURI, true, this.conn, contextUri, nextOntologyID
                        .getInferredOntologyIRI().toOpenRDFURI());
        
        // String[] expectedLabels = {"Demo Analysis", "Demo Process 1", "Demo Process 2",
        // "Demo Project Plan", "Demo investigation"};
        
        Assert.assertEquals("Incorrect number of direct child objects", 13, childObjectList.size());
        for(int i = 0; i < childObjectList.size(); i++)
        {
            System.out.println(childObjectList.get(i).getLabel());
            // Assert.assertEquals("Incorrect direct parent", parentObjectURI,
            // childObjectList.get(i).getDirectParent());
            // Assert.assertEquals("Incorrect object at position", expectedLabels[i],
            // childObjectList.get(i).getLabel());
        }
    }
    
    /**
     * Test retrieving all Top Objects of an artifact when the artifact has one top object.
     */
    @Test
    public void testGetTopObjectsOne() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-1.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        final List<URI> topObjects = this.sparqlHelper.getTopObjects(this.conn, contextUri);
        
        Assert.assertEquals("Expected 1 top object", 1, topObjects.size());
        Assert.assertEquals("Not the expected top object",
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130205/object:2966"),
                topObjects.get(0));
    }
    
    /**
     * Test retrieving all Top Objects of an artifact when the artifact has multiple top objects.
     * 
     * NOTE: a PODD artifact should currently have only 1 top object.
     */
    @Test
    public void testGetTopObjectsWithMultiple() throws Exception
    {
        final String testResourcePath = "/test/artifacts/3-topobjects.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        final List<URI> topObjects = this.sparqlHelper.getTopObjects(this.conn, contextUri);
        
        Assert.assertEquals("Expected 3 top objects", 3, topObjects.size());
        Assert.assertTrue(
                "Missing top object",
                topObjects.contains(ValueFactoryImpl.getInstance().createURI(
                        "http://purl.org/podd/basic-1-20130205/object:2966")));
        Assert.assertTrue(
                "Missing top object",
                topObjects.contains(ValueFactoryImpl.getInstance().createURI(
                        "http://purl.org/podd/basic-1-20130205/object:2977")));
        Assert.assertTrue(
                "Missing top object",
                topObjects.contains(ValueFactoryImpl.getInstance().createURI(
                        "http://purl.org/podd/basic-1-20130205/object:2988")));
    }
    
    /**
     * Test that direct imports are correctly identified.
     * 
     * Originally from from PoddSesameManagerImpl.java
     */
    @Test
    public void testGetDirectImports() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-1.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        Assert.assertEquals("Not the expected number of statements in Repository", 33, this.conn.size(contextUri));
        
        final Set<IRI> imports = this.sparqlHelper.getDirectImports(this.conn, contextUri);
        Assert.assertEquals("Podd-Base should have 2 imports", 2, imports.size());
        Assert.assertTrue("Missing import", imports.contains(IRI.create("http://purl.org/podd/ns/poddBase")));
        Assert.assertTrue("Missing import", imports.contains(IRI.create("http://purl.org/podd/ns/poddScience")));
    }
    
}
