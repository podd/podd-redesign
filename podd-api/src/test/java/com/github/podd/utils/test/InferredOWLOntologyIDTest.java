/**
 * 
 */
package com.github.podd.utils.test;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Tests the InferredOWLOntologyID class for its assignment of IRIs to various attributes and the
 * equality conditions.
 * 
 * @author kutila
 * 
 */
public class InferredOWLOntologyIDTest
{
    
    private IRI testInferredOntologyIRI;
    private IRI testOntologyVersionIRI;
    private IRI testBaseOntologyIRI;
    
    /**
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.testBaseOntologyIRI = IRI.create("http://example.org/podd/poddBase");
        this.testOntologyVersionIRI = IRI.create("http://example.org/podd/version/poddBase/1");
        this.testInferredOntologyIRI = IRI.create("urn:inferred:prefix:http://example.org/podd/version/poddBase/1");
    }
    
    /**
     * 
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception
    {
    }
    
    @Test
    public void testEqualAnonymous() throws Exception
    {
        final InferredOWLOntologyID onto1 = new InferredOWLOntologyID(null, null, null);
        final InferredOWLOntologyID onto2 = new InferredOWLOntologyID(null, null, null);
        
        Assert.assertTrue(onto1.isAnonymous());
        Assert.assertTrue(onto2.isAnonymous());
        
        Assert.assertFalse(onto1.equals(onto2));
    }
    
    /**
     * Compare two that have different version IRIs
     * 
     * @throws Exception
     */
    @Test
    public void testEqualDifferentVersionIRI() throws Exception
    {
        final IRI differentOntologyVersionIRI = IRI.create("http://example.org/podd/version/poddBase/2");
        
        final InferredOWLOntologyID onto1 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI,
                        this.testInferredOntologyIRI);
        final InferredOWLOntologyID onto2 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, differentOntologyVersionIRI,
                        this.testInferredOntologyIRI);
        
        Assert.assertFalse(onto1.equals(onto2));
    }
    
    /**
     * Compare two that have different version IRIs and Inferred IRIs
     * 
     * @throws Exception
     */
    @Test
    public void testEqualDifferentVersionIRIAndInferredIRI() throws Exception
    {
        final IRI differentOntologyVersionIRI = IRI.create("http://example.org/podd/version/poddBase/2");
        final IRI differentInferredOntologyIRI =
                IRI.create("urn:inferred:prefix:http://example.org/podd/version/poddBase/2");
        
        final InferredOWLOntologyID onto1 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI,
                        this.testInferredOntologyIRI);
        final InferredOWLOntologyID onto2 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, differentOntologyVersionIRI,
                        differentInferredOntologyIRI);
        
        Assert.assertFalse(onto1.equals(onto2));
    }
    
    @Test
    public void testEqualInferredIRIAndNullInferredIRI() throws Exception
    {
        final InferredOWLOntologyID onto1 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI, null);
        final InferredOWLOntologyID onto2 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI,
                        this.testInferredOntologyIRI);
        
        Assert.assertFalse(onto1.equals(onto2));
        Assert.assertFalse(onto2.equals(onto1));
        
    }
    
    @Test
    public void testEqualNullInferredIRI() throws Exception
    {
        final InferredOWLOntologyID onto1 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI, null);
        final InferredOWLOntologyID onto2 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI, null);
        
        Assert.assertTrue(onto1.equals(onto2));
    }
    
    @Test
    public void testEqualNullVersionIRI() throws Exception
    {
        final InferredOWLOntologyID onto1 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, null, this.testInferredOntologyIRI);
        final InferredOWLOntologyID onto2 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, null, this.testInferredOntologyIRI);
        
        Assert.assertTrue(onto1.equals(onto2));
    }
    
    @Test
    public void testEqualNullVersionIRIAndInferredIRI() throws Exception
    {
        final InferredOWLOntologyID onto1 = new InferredOWLOntologyID(this.testBaseOntologyIRI, null, null);
        final InferredOWLOntologyID onto2 = new InferredOWLOntologyID(this.testBaseOntologyIRI, null, null);
        
        Assert.assertTrue(onto1.equals(onto2));
    }
    
    @Test
    public void testEqualWhenAllFieldsPopulated() throws Exception
    {
        final InferredOWLOntologyID onto1 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI,
                        this.testInferredOntologyIRI);
        final InferredOWLOntologyID onto2 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI,
                        this.testInferredOntologyIRI);
        
        Assert.assertTrue(onto1.equals(onto2));
    }
    
    /**
     * Compare an InferredOWLOntologyID with an OWLOntologyID
     * 
     * @throws Exception
     */
    @Test
    public void testEqualWithOWLOntologyID() throws Exception
    {
        final InferredOWLOntologyID onto1 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI,
                        this.testInferredOntologyIRI);
        final OWLOntologyID onto2 = new OWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI);
        
        Assert.assertFalse(onto1.equals(onto2));
    }
    
    /**
     * Compare an InferredOWLOntologyID which does not have an InferredIRI with an OWLOntologyID
     * 
     * @throws Exception
     */
    @Test
    public void testEqualWithOWLOntologyIDNoInferredIRI() throws Exception
    {
        final InferredOWLOntologyID onto1 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI, null);
        final OWLOntologyID onto2 = new OWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI);
        
        Assert.assertTrue(onto1.equals(onto2));
    }
    
    /**
     * Compare two InferredOWLOntologyIDs that only have base ontology IRIs
     * 
     * @throws Exception
     */
    @Test
    public void testEqualWithOWLOntologyIDNoVersionIRIAndInferredIRI() throws Exception
    {
        final InferredOWLOntologyID onto1 = new InferredOWLOntologyID(this.testBaseOntologyIRI, null, null);
        final OWLOntologyID onto2 = new OWLOntologyID(this.testBaseOntologyIRI, null);
        
        Assert.assertTrue(onto1.equals(onto2));
    }
    
    /**
     * Tests how the IRIs passed in at creation time are assigned internally.
     * 
     * @throws Exception
     */
    @Test
    public void testInternalAssignmentOfIRIs() throws Exception
    {
        
        final InferredOWLOntologyID inferredOwlOntologyID =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI,
                        this.testInferredOntologyIRI);
        
        // test the directly accessed base, version and inferred IRIs
        Assert.assertEquals(this.testBaseOntologyIRI, inferredOwlOntologyID.getOntologyIRI());
        Assert.assertEquals(this.testOntologyVersionIRI, inferredOwlOntologyID.getVersionIRI());
        Assert.assertEquals(this.testInferredOntologyIRI, inferredOwlOntologyID.getInferredOntologyIRI());
        
        // test the OWLOntologyID representing the base portion
        Assert.assertEquals(this.testBaseOntologyIRI, inferredOwlOntologyID.getBaseOWLOntologyID().getOntologyIRI());
        Assert.assertEquals(this.testOntologyVersionIRI, inferredOwlOntologyID.getBaseOWLOntologyID().getVersionIRI());
        
        // test the OWLOntologyID representing the inferred portion (here ontologyIRI and versionIRI
        // are the same)
        Assert.assertEquals(this.testInferredOntologyIRI, inferredOwlOntologyID.getInferredOWLOntologyID()
                .getOntologyIRI());
        Assert.assertEquals(this.testInferredOntologyIRI, inferredOwlOntologyID.getInferredOWLOntologyID()
                .getVersionIRI());
    }
    
    @Test
    public void testToString() throws Exception
    {
        final InferredOWLOntologyID onto1 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI,
                        this.testInferredOntologyIRI);
        final String onto1StringRepresentation = onto1.toString();
        Assert.assertNotNull("toString() returned NULL", onto1StringRepresentation);
        Assert.assertTrue(onto1StringRepresentation.contains(this.testBaseOntologyIRI.toString()));
        Assert.assertTrue(onto1StringRepresentation.contains(this.testOntologyVersionIRI.toString()));
        Assert.assertTrue(onto1StringRepresentation.contains(this.testInferredOntologyIRI.toString()));
        
        final InferredOWLOntologyID onto2 =
                new InferredOWLOntologyID(this.testBaseOntologyIRI, this.testOntologyVersionIRI, null);
        final String onto2StringRepresentation = onto2.toString();
        Assert.assertNotNull("toString() returned NULL", onto2StringRepresentation);
        Assert.assertTrue(onto2StringRepresentation.contains(this.testBaseOntologyIRI.toString()));
        Assert.assertTrue(onto2StringRepresentation.contains(this.testOntologyVersionIRI.toString()));
        Assert.assertFalse(onto2StringRepresentation.contains(this.testInferredOntologyIRI.toString()));
        
    }
    
}
