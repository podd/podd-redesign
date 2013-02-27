/**
 * 
 */
package com.github.podd.ontology.test;

import info.aduna.iteration.Iterations;

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
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.SparqlQueryHelper;

/**
 * Test for SparqlQueryHelper.java
 * 
 * @author kutila
 * 
 */
public class SparqlQueryTest extends AbstractOntologyTest
{
    
    protected RepositoryConnection conn;
    
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
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
    public void testGetPoddObjectDetailsWithInternalObject() throws Exception
    {
        // prepare: load test artifact
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        
        this.conn = this.getConnection();
        
        final URI objectUri =
                ValueFactoryImpl.getInstance().createURI(
                        "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Investigation");
        
        // Create a list of contexts made up of the schema ontologies and the asserted artifact.
        // The inferred artifact graph is not included as we're only interested in asserted
        // properties for display purposes.
        final List<URI> allContextsToQuery = new ArrayList<URI>(super.getSchemaOntologyGraphs());
        allContextsToQuery.add(nextOntologyID.getVersionIRI().toOpenRDFURI());
        
        // invoke method under test
        final Model model =
                SparqlQueryHelper.getPoddObjectDetails(objectUri, this.conn, allContextsToQuery.toArray(new URI[0]));
        
        // verify:
        Assert.assertEquals("Incorrect number of statements about object", 8, model.size());
        
        final Model modelLabelHasMaterial =
                model.filter(ValueFactoryImpl.getInstance()
                        .createURI("http://purl.org/podd/ns/poddScience#hasMaterial"), RDFS.LABEL, null);
        Assert.assertEquals("Should be exactly 1 label for hasMaterial", 1, modelLabelHasMaterial.size());
        Assert.assertEquals("Not the expected label for hasMaterial", "has Material",
                modelLabelHasMaterial.objectString());
        
        final Model modelPropertyTriples =
                model.filter(
                        ValueFactoryImpl.getInstance().createURI(
                                "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Investigation"), null, null);
        Assert.assertEquals("Incorrect number of statements with podd object as the subject.", 3,
                modelPropertyTriples.size());
    }
    
    @Test
    public void testGetPoddObjectDetailsWithTopObject() throws Exception
    {
        // prepare: load test artifact
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        
        this.conn = this.getConnection();
        
        final URI objectUri =
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130205/object:2966");
        
        // Create a list of contexts made up of the schema ontologies and the asserted artifact.
        // The inferred artifact graph is not included as we're only interested in asserted
        // properties for display purposes.
        final List<URI> allContextsToQuery = new ArrayList<URI>(super.getSchemaOntologyGraphs());
        allContextsToQuery.add(nextOntologyID.getVersionIRI().toOpenRDFURI());
        
        // invoke method under test
        final Model model =
                SparqlQueryHelper.getPoddObjectDetails(objectUri, this.conn, allContextsToQuery.toArray(new URI[0]));
        
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
    
    @Ignore
    @Test
    public void testGetPoddObjectDetailsForEditWithInternalObject() throws Exception
    {
        // prepare: load test artifact
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        
        this.conn = this.getConnection();
        
        final URI objectUri =
                ValueFactoryImpl.getInstance().createURI(
                        "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Investigation");
        
        // Create a list of contexts made up of the schema ontologies and the asserted artifact.
        // The inferred artifact graph is not included as we're only interested in asserted
        // properties for display purposes.
        final List<URI> allContextsToQuery = new ArrayList<URI>(super.getSchemaOntologyGraphs());
        allContextsToQuery.add(nextOntologyID.getVersionIRI().toOpenRDFURI());
        
        // invoke method under test
        final Model model =
                SparqlQueryHelper.getPoddObjectDetailsForEdit(objectUri, this.conn,
                        allContextsToQuery.toArray(new URI[0]));
        
        // verify:
        Assert.assertEquals("Incorrect number of statements about object", 8, model.size());
        
        final Model modelLabelHasMaterial =
                model.filter(ValueFactoryImpl.getInstance()
                        .createURI("http://purl.org/podd/ns/poddScience#hasMaterial"), RDFS.LABEL, null);
        Assert.assertEquals("Should be exactly 1 label for hasMaterial", 1, modelLabelHasMaterial.size());
        Assert.assertEquals("Not the expected label for hasMaterial", "has Material",
                modelLabelHasMaterial.objectString());
        
        final Model modelPropertyTriples =
                model.filter(
                        ValueFactoryImpl.getInstance().createURI(
                                "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Investigation"), null, null);
        Assert.assertEquals("Incorrect number of statements with podd object as the subject.", 3,
                modelPropertyTriples.size());
    }
    
    /**
     * Test retrieve properties about a given Object
     */
    @Test
    public void testGetDirectPropertiesOfAnObject() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        
        // Create a list of contexts made up of the schema ontologies and the asserted artifact.
        // The inferred artifact graph is not included as we're only interested in asserted
        // properties for display purposes.
        final List<URI> allContextsToQuery = new ArrayList<URI>(super.getSchemaOntologyGraphs());
        allContextsToQuery.add(nextOntologyID.getVersionIRI().toOpenRDFURI());
        final URI[] contexts = allContextsToQuery.toArray(new URI[0]);
        
        this.conn = this.getConnection();
        
        // FIXME: Something is broken if we are getting a set of labels just to get the object URI.
        // Have you looked at the PoddSesameManager/PoddArtifactManager/PoddSchemaManager interfaces
        // to work on this in a cleaner manner?
        final URI topObjectUri =
                SparqlQueryHelper.getTopObjects(nextOntologyID, this.conn, contexts).get(0).getObjectURI();
        final List<URI> orderedPropertyUris = SparqlQueryHelper.getWeightedProperties(topObjectUri, this.conn, contexts);
        
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
     * Test retrieve properties about a given Object
     */
    @Test
    public void testGetDirectPropertiesOfAnInternalObject() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        
        // Create a list of contexts made up of the schema ontologies and the asserted artifact.
        // The inferred artifact graph is not included as we're only interested in asserted
        // properties for display purposes.
        final List<URI> allContextsToQuery = new ArrayList<URI>(super.getSchemaOntologyGraphs());
        allContextsToQuery.add(nextOntologyID.getVersionIRI().toOpenRDFURI());
        final URI[] contexts = allContextsToQuery.toArray(new URI[0]);
        
        this.conn = this.getConnection();
        
        // get URI for the internal Publication object
        RepositoryResult<Statement> repoResults =
                conn.getStatements(null,
                        ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddScience#hasPublication"),
                        null, false, contexts);
        List<Statement> list = Iterations.asList(repoResults);
        Assert.assertEquals("List should have 1 record", 1, list.size());
        URI publicationObjectUri = (URI)list.get(0).getObject();
        
        final List<URI> orderedPropertyUris =
                SparqlQueryHelper.getWeightedProperties(publicationObjectUri, this.conn, contexts);
        
        // verify:
        Assert.assertEquals("Incorrect number of statements about Top Object", 6, orderedPropertyUris.size());
        
        final String[] expectedUris =
                { "http://purl.org/dc/terms/creator", "http://purl.org/dc/terms/created",
                        "http://purl.org/podd/ns/poddBase#hasPURL", "http://purl.org/podd/ns/poddScience#hasAbstract",
                        "http://purl.org/podd/ns/poddScience#publishedIn",
                        "http://purl.org/podd/ns/poddScience#hasYear", };
        for(int i = 0; i < orderedPropertyUris.size(); i++)
        {
            Assert.assertEquals("Property URI not in expected position",
                    ValueFactoryImpl.getInstance().createURI(expectedUris[i]), orderedPropertyUris.get(i));
        }
        
        this.printContents(conn, super.getSchemaOntologyGraphs().get(8));
    }
    
    /**
     * Test retrieve list of direct children of the Top Object
     */
    @Test
    public void testGetContainedObjectsFromTopObject() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        
        final URI parentObjectURI =
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130205/object:2966");
        
        final List<PoddObjectLabel> childObjectList =
                SparqlQueryHelper.getContainedObjects(nextOntologyID, parentObjectURI, false, this.conn, contextUri,
                        nextOntologyID.getInferredOntologyIRI().toOpenRDFURI());
        
        final String[] expectedLabels =
                { "Demo Analysis", "Demo Process 1", "Demo Process 2", "Demo Project Plan", "Demo investigation",
                        "PODD - Towards An Extensible, Domain-agnostic Scientific Data Management System" };
        
        Assert.assertEquals("Incorrect number of direct child objects", 6, childObjectList.size());
        for(int i = 0; i < childObjectList.size(); i++)
        {
            Assert.assertEquals("Incorrect object at position", expectedLabels[i], childObjectList.get(i).getLabel());
        }
    }
    
    /**
     * Test retrieve list of direct children of an inner object
     */
    @Test
    public void testGetContainedObjectsFromInnerObject() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        
        final URI parentObjectURI =
                ValueFactoryImpl.getInstance().createURI(
                        "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Investigation");
        
        final List<PoddObjectLabel> childObjectList =
                SparqlQueryHelper.getContainedObjects(nextOntologyID, parentObjectURI, false, this.conn, contextUri,
                        nextOntologyID.getInferredOntologyIRI().toOpenRDFURI());
        
        final String[] expectedLabels = { "Demo material", "Squeekee material", "my treatment 1" };
        
        Assert.assertEquals("Incorrect number of direct child objects", 3, childObjectList.size());
        for(int i = 0; i < childObjectList.size(); i++)
        {
            Assert.assertEquals("Incorrect object at position", expectedLabels[i], childObjectList.get(i).getLabel());
        }
    }
    
    /**
     * Test retrieve list of direct children of the Top Object FIXME
     */
    @Test
    public void testGetContainedObjectsFromTopObjectWithRecursion() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        this.conn = this.getConnection();
        
        final URI parentObjectURI =
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130205/object:2966");
        
        final List<PoddObjectLabel> childObjectList =
                SparqlQueryHelper.getContainedObjects(nextOntologyID, parentObjectURI, true, this.conn, contextUri,
                        nextOntologyID.getInferredOntologyIRI().toOpenRDFURI());
        
        final String[] expectedLabels =
                { "Demo Analysis", "Demo Process 1", "Demo Process 2", "Demo Project Plan", "Demo investigation",
                        "PODD - Towards An Extensible, Domain-agnostic Scientific Data Management System",
                        "Platform 1", "Demo material", "Squeekee material", "my treatment 1", "Demo genotype",
                        "Genotype 2", "Genotype 3", "sequence a", };
        
        Assert.assertEquals("Incorrect number of direct child objects", 14, childObjectList.size());
        for(int i = 0; i < childObjectList.size(); i++)
        {
            System.out.println(childObjectList.get(i).getLabel());
            Assert.assertEquals("Incorrect object at position", expectedLabels[i], childObjectList.get(i).getLabel());
        }
    }
    
    /**
     * Test retrieve the type of a given object URI.
     */
    @Test
    public void testGetObjectType() throws Exception
    {
        final InferredOWLOntologyID ontologyID1 = this.loadArtifact("/test/artifacts/basic-2.ttl", RDFFormat.TURTLE);
        
        final List<URI> allContextsToQuery = new ArrayList<URI>(super.getSchemaOntologyGraphs());
        allContextsToQuery.add(ontologyID1.getVersionIRI().toOpenRDFURI());
        final URI[] contexts = allContextsToQuery.toArray(new URI[0]);
        
        this.conn = this.getConnection();
        final String[] objectUris =
                { "http://purl.org/podd/basic-1-20130205/object:2966",
                        "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype",
                        "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                        "http://purl.org/podd/ns/poddScience#ANZSRC_NotApplicable", };
        
        final String[] expectedTypes =
                { "http://purl.org/podd/ns/poddScience#Project", "http://purl.org/podd/ns/poddScience#Genotype",
                        "http://purl.org/podd/ns/poddScience#Material",
                        "http://purl.org/podd/ns/poddScience#ANZSRCAssertion", };
        final String[] expectedLabels = { "Project", "Genotype", "Material", "ANZSRCAssertion", };
        
        // test in a loop for multiple podd objects
        for(int i = 0; i < objectUris.length; i++)
        {
            final URI objectUri = ValueFactoryImpl.getInstance().createURI(objectUris[i]);
            
            final PoddObjectLabel poddObject =
                    SparqlQueryHelper.getObjectType(ontologyID1, objectUri, this.conn, contexts);
            
            // verify:
            Assert.assertNotNull("Type was null", poddObject);
            Assert.assertEquals("Wrong type", ValueFactoryImpl.getInstance().createURI(expectedTypes[i]),
                    poddObject.getObjectURI());
            Assert.assertEquals("Wrong label", expectedLabels[i], poddObject.getLabel());
        }
    }
    
    @Test
    public void testGetPoddArtifactList() throws Exception
    {
        final InferredOWLOntologyID ontologyID1 = this.loadArtifact("/test/artifacts/basic-1.ttl", RDFFormat.TURTLE);
        final InferredOWLOntologyID ontologyID2 = this.loadArtifact("/test/artifacts/basic-2.ttl", RDFFormat.TURTLE);
        
        this.conn = this.getConnection();
        final List<URI> artifacts = SparqlQueryHelper.getPoddArtifactList(this.conn, this.artifactGraph);
        
        Assert.assertTrue("artifact missing in list", artifacts.contains(ontologyID1.getOntologyIRI().toOpenRDFURI()));
        Assert.assertTrue("artifact missing in list", artifacts.contains(ontologyID2.getOntologyIRI().toOpenRDFURI()));
    }
    
    @Test
    public void testGetPoddObject() throws Exception
    {
        final InferredOWLOntologyID ontologyID1 = this.loadArtifact("/test/artifacts/basic-2.ttl", RDFFormat.TURTLE);
        
        final List<URI> allContextsToQuery = new ArrayList<URI>(super.getSchemaOntologyGraphs());
        allContextsToQuery.add(ontologyID1.getVersionIRI().toOpenRDFURI());
        final URI[] contexts = allContextsToQuery.toArray(new URI[0]);
        
        this.conn = this.getConnection();
        final String[] objectUris =
                { "http://purl.org/podd/basic-1-20130205/object:2966",
                        "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype",
                        "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                        "http://purl.org/podd/ns/poddScience#ANZSRC_NotApplicable", };
        
        final String[] expectedTitles =
                { "Project#2012-0006_ Cotton Leaf Morphology", "Demo genotype", "Squeekee material", "Not Applicable" };
        final String[] expectedDescriptions = { "Characterising normal and okra leaf shapes", null, null, null };
        
        // test in a loop for multiple podd objects
        for(int i = 0; i < objectUris.length; i++)
        {
            final URI objectUri = ValueFactoryImpl.getInstance().createURI(objectUris[i]);
            
            final PoddObjectLabel poddObject =
                    SparqlQueryHelper.getPoddObject(ontologyID1, objectUri, this.conn, contexts);
            
            // verify:
            Assert.assertNotNull("Podd Object was null", poddObject);
            Assert.assertEquals("Wrong title", expectedTitles[i], poddObject.getLabel());
            Assert.assertEquals("Wrong description", expectedDescriptions[i], poddObject.getDescription());
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
        final List<PoddObjectLabel> topObjects = SparqlQueryHelper.getTopObjects(nextOntologyID, this.conn, contextUri);
        
        Assert.assertEquals("Expected 1 top object", 1, topObjects.size());
        Assert.assertEquals("Not the expected top object URI",
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130205/object:2966"),
                topObjects.get(0).getObjectURI());
        Assert.assertEquals("Not the expected top object Label/title", "Project#2012-0006_ Cotton Leaf Morphology",
                topObjects.get(0).getLabel());
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
        final List<PoddObjectLabel> topObjects = SparqlQueryHelper.getTopObjects(nextOntologyID, this.conn, contextUri);
        
        Assert.assertEquals("Expected 3 top objects", 3, topObjects.size());
        
        final List<String> expectedUriList =
                Arrays.asList(new String[] { "http://purl.org/podd/basic-1-20130205/object:2966",
                        "http://purl.org/podd/basic-1-20130205/object:2977",
                        "http://purl.org/podd/basic-1-20130205/object:2988" });
        
        for(final PoddObjectLabel topObject : topObjects)
        {
            Assert.assertTrue("Unexpected top object", expectedUriList.contains(topObject.getObjectURI().toString()));
        }
    }
    
    @Test
    public void testSpikeGetCardinality() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-2.rdf";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.RDFXML);
        
        // Create a list of contexts made up of the schema ontologies and the asserted artifact.
        final List<URI> allContextsToQuery = new ArrayList<URI>(super.getSchemaOntologyGraphs());
        allContextsToQuery.add(nextOntologyID.getVersionIRI().toOpenRDFURI());
        final URI[] contexts = allContextsToQuery.toArray(new URI[0]);
        
        this.conn = this.getConnection();
        
        // this.printContents(conn, contexts[8]);
        
        URI publicationObjectUri = ValueFactoryImpl.getInstance().createURI("urn:hardcoded:purl:artifact:1#publication45");
        URI propertyUri = ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "publishedIn");
        
        int[] cardinalities = SparqlQueryHelper.spikeGetCardinality(publicationObjectUri, propertyUri, conn, contexts);
        
        // verify:
        System.out.println(cardinalities[0] + " " + cardinalities[1] + " " + cardinalities[2]);
    }
    
    /**
     * Tests retrieving all possible values for Collection types
     */
    @Ignore
    @Test
    public void testSpikeGetPossibleValues() throws Exception
    {
        // Create a list of contexts made up of the schema ontologies and the asserted artifact.
        final List<URI> allContextsToQuery = new ArrayList<URI>(super.getSchemaOntologyGraphs());
        final URI[] contexts = allContextsToQuery.toArray(new URI[0]);
        
        this.conn = this.getConnection();
        
        // types of Collections to test
        URI[] collectionURIsToTest =
                { ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "PlatformType"),
                        ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "Sex"),
                        ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "Software"),
                        ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_BASE, "PublicationStatus"),
                        ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "HasControlAssertion"),
                        ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "WildTypeAssertion"),
                        ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "ANZSRCAssertion"), };
        
        URI[][] expectedMembers =
                {
                        {
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "Software"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "Hardware"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE,
                                        "HardwareSoftware") },
                        
                        {
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE,
                                        "Sex_NotApplicable"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "Sex_Unknown"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE,
                                        "Sex_Hermaphrodite"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "Sex_Female"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "Sex_Male") },
                        
                        {}, // poddScience:Software is not a Collection
                        
                        { ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_BASE, "Published"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_BASE, "NotPublished"), },
                        
                        {
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE,
                                        "HasControl_Yes"),
                                ValueFactoryImpl.getInstance()
                                        .createURI(PoddRdfConstants.PODD_SCIENCE, "HasControl_No"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE,
                                        "HasControl_NotApplicable"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE,
                                        "HasControl_Unknown") },
                        
                        {
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "WildType_Yes"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "WildType_No"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE,
                                        "WildType_NotApplicable"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE,
                                        "WildType_Unknown") },
                        
                        {
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "ANZSRC_Yes"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE, "ANZSRC_No"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE,
                                        "ANZSRC_NotApplicable"),
                                ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_SCIENCE,
                                        "ANZSRC_Unknown") },
                
                };
        
        // iterate through test data
        for(int i = 0; i < collectionURIsToTest.length; i++)
        {
            List<PoddObjectLabel> members =
                    SparqlQueryHelper.spikeGetPossibleValues(collectionURIsToTest[i], conn, contexts);
            Assert.assertEquals("Not the expected number of members", expectedMembers[i].length, members.size());
            
            List<URI> expectedMembersList = Arrays.asList(expectedMembers[i]);
            for(PoddObjectLabel resultObject : members)
            {
                Assert.assertTrue("Unexpected member found", expectedMembersList.contains(resultObject.getObjectURI()));
            }
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
        
        final Set<IRI> imports = SparqlQueryHelper.getDirectImports(this.conn, contextUri);
        Assert.assertEquals("Podd-Base should have 4 imports", 4, imports.size());
        Assert.assertTrue("Missing import", imports.contains(IRI.create("http://purl.org/podd/ns/poddBase")));
        Assert.assertTrue("Missing import", imports.contains(IRI.create("http://purl.org/podd/ns/poddScience")));
    }
    
}
