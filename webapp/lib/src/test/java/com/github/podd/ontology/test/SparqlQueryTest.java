/**
 * 
 */
package com.github.podd.ontology.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;
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
    
    @Test
    public void testGetPoddObjectDetails() throws Exception
    {
        // prepare: load test artifact
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        
        this.conn = this.getConnection();
        
        final URI objectUri =
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130205/object:2966");
        
        // create a list of contexts to query
        final List<URI> allContextsToQuery = new ArrayList<URI>(super.getSchemaOntologyGraphs());
        allContextsToQuery.add(nextOntologyID.getVersionIRI().toOpenRDFURI());
        allContextsToQuery.add(nextOntologyID.getInferredOntologyIRI().toOpenRDFURI());
        
        // invoke method under test
        final Model model =
                this.sparqlHelper.getPoddObjectDetails(objectUri, this.conn, allContextsToQuery.toArray(new URI[0]));
        
        // verify:
        Assert.assertEquals("Incorrect number of statements about object", 37, model.size());
        
        final Model modelLabelHasLeadInstitution =
                model.filter(
                        ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddBase#hasLeadInstitution"),
                        RDFS.LABEL, null);
        Assert.assertEquals("Should be exactly 1 label for hasLeadInstution", 1, modelLabelHasLeadInstitution.size());
        Assert.assertEquals("Not the expected label for hasLeadInstitution", "Lead Institution",
                modelLabelHasLeadInstitution.objectString());
        
        final Model modelPropertyTriples =
                model.filter(
                        ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130205/object:2966"),
                        null, null);
        Assert.assertEquals("Incorrect number of statements with podd object as the subject.", 14,
                modelPropertyTriples.size());
    }
    
    /**
     * Test retrieve properties about a given Object
     */
    @Test
    public void testGetPropertiesOfAnObject() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        
        // create a list of contexts to query
        final List<URI> allContextsToQuery = new ArrayList<URI>(super.getSchemaOntologyGraphs());
        allContextsToQuery.add(nextOntologyID.getVersionIRI().toOpenRDFURI());
        allContextsToQuery.add(nextOntologyID.getInferredOntologyIRI().toOpenRDFURI());
        final URI[] contexts = allContextsToQuery.toArray(new URI[0]);
        
        this.conn = this.getConnection();
        
        final URI topObjectUri = this.sparqlHelper.getTopObjects(this.conn, contexts).get(0).getUri();
        final List<URI> orderedPropertyUris = this.sparqlHelper.getProperties(topObjectUri, this.conn, contexts);
        
        // verify:
        Assert.assertEquals("Incorrect number of statements about Top Object", 13, orderedPropertyUris.size());
        
        final String[] expectedUris =
                { "http://purl.org/podd/ns/poddScience#hasANZSRC", "http://purl.org/podd/ns/poddBase#createdAt",
                        "http://purl.org/dc/terms/creator",
                        "http://purl.org/podd/ns/poddBase#hasPrincipalInvestigator",
                        "http://purl.org/podd/ns/poddScience#hasAnalysis",
                        "http://purl.org/podd/ns/poddScience#hasInvestigation",
                        "http://purl.org/podd/ns/poddScience#hasProcess",
                        "http://purl.org/podd/ns/poddScience#hasProjectPlan",
                        "http://purl.org/podd/ns/poddScience#hasPublication",
                        "http://purl.org/podd/ns/poddBase#hasPublicationStatus",
                        "http://purl.org/podd/ns/poddBase#hasLeadInstitution",
                        "http://purl.org/podd/ns/poddBase#hasStartDate",
                        "http://purl.org/podd/ns/poddBase#hasTopObjectStatus" };
        for(int i = 0; i < orderedPropertyUris.size(); i++)
        {
            Assert.assertEquals("Property URI not in expected position",
                    ValueFactoryImpl.getInstance().createURI(expectedUris[i]), orderedPropertyUris.get(i));
        }
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
                { "Demo Analysis", "Demo Process 1", "Demo Process 2", "Demo Project Plan", "Demo investigation",
                        "PODD - Towards An Extensible, Domain-agnostic Scientific Data Management System" };
        
        Assert.assertEquals("Incorrect number of direct child objects", 6, childObjectList.size());
        for(int i = 0; i < childObjectList.size(); i++)
        {
            Assert.assertEquals("Incorrect object at position", expectedLabels[i], childObjectList.get(i).getTitle());
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
        
        final String[] expectedLabels = { "Demo material", "Squeekee material", "my treatment 1" };
        
        Assert.assertEquals("Incorrect number of direct child objects", 3, childObjectList.size());
        for(int i = 0; i < childObjectList.size(); i++)
        {
            Assert.assertEquals("Incorrect object at position", expectedLabels[i], childObjectList.get(i).getTitle());
        }
    }
    
    /**
     * Test retrieve list of direct children of the Top Object FIXME
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
        
        Assert.assertEquals("Incorrect number of direct child objects", 14, childObjectList.size());
        for(int i = 0; i < childObjectList.size(); i++)
        {
            System.out.println(childObjectList.get(i).getTitle());
            // Assert.assertEquals("Incorrect direct parent", parentObjectURI,
            // childObjectList.get(i).getDirectParent());
            // Assert.assertEquals("Incorrect object at position", expectedLabels[i],
            // childObjectList.get(i).getLabel());
        }
    }
    
    @Test
    public void testGetPoddArtifactList() throws Exception
    {
        final InferredOWLOntologyID ontologyID1 = this.loadArtifact("/test/artifacts/basic-1.ttl", RDFFormat.TURTLE);
        final InferredOWLOntologyID ontologyID2 = this.loadArtifact("/test/artifacts/basic-2.ttl", RDFFormat.TURTLE);
        
        this.conn = this.getConnection();
        final List<URI> artifacts = this.sparqlHelper.getPoddArtifactList(this.conn, this.artifactGraph);
        
        Assert.assertTrue("artifact missing in list", artifacts.contains(ontologyID1.getOntologyIRI().toOpenRDFURI()));
        Assert.assertTrue("artifact missing in list", artifacts.contains(ontologyID2.getOntologyIRI().toOpenRDFURI()));
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
        final List<PoddObject> topObjects = this.sparqlHelper.getTopObjects(this.conn, contextUri);
        
        Assert.assertEquals("Expected 1 top object", 1, topObjects.size());
        Assert.assertEquals("Not the expected top object URI",
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130205/object:2966"),
                topObjects.get(0).getUri());
        Assert.assertEquals("Not the expected top object Label/title", "Project#2012-0006_ Cotton Leaf Morphology",
                topObjects.get(0).getTitle());
        Assert.assertEquals("Not the expected top object description", "Characterising normal and okra leaf shapes",
                topObjects.get(0).getDescription());
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
        final List<PoddObject> topObjects = this.sparqlHelper.getTopObjects(this.conn, contextUri);
        
        Assert.assertEquals("Expected 3 top objects", 3, topObjects.size());
        
        final List<String> expectedUriList =
                Arrays.asList(new String[] { "http://purl.org/podd/basic-1-20130205/object:2966",
                        "http://purl.org/podd/basic-1-20130205/object:2977",
                        "http://purl.org/podd/basic-1-20130205/object:2988" });
        
        for(final PoddObject topObject : topObjects)
        {
            Assert.assertTrue("Unexpected top object", expectedUriList.contains(topObject.getUri().toString()));
        }
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
        Assert.assertEquals("Not the expected number of statements in Repository", 32, this.conn.size(contextUri));
        
        final Set<IRI> imports = this.sparqlHelper.getDirectImports(this.conn, contextUri);
        Assert.assertEquals("Podd-Base should have 4 imports", 4, imports.size());
        Assert.assertTrue("Missing import", imports.contains(IRI.create("http://purl.org/podd/ns/poddBase")));
        Assert.assertTrue("Missing import", imports.contains(IRI.create("http://purl.org/podd/ns/poddScience")));
    }
    
}
