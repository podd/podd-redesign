/**
 * 
 */
package com.github.podd.ontology.test;


public class SparqlQueryTest extends AbstractOntologyTest
{
/***
 * FIXME
 *
    
    /**
     * Test the performance of above queries. Move this to a separate test class.
    @Ignore
    @Test
    public void testPerformance() throws Exception
    {
        Assert.fail("TODO");
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
     * Test retrieve list of direct children of the Top Object
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
    
**/
}
