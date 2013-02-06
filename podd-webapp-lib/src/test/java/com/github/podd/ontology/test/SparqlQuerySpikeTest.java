/**
 * 
 */
package com.github.podd.ontology.test;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Test for SparqlQuerySpike.java
 * 
 * @author kutila
 * 
 */
public class SparqlQuerySpikeTest extends AbstractOntologyTest
{
    
    private SparqlQuerySpike testSpike;
    
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
     * Test retrieve information about Top Object
     */
    @Test
    public void testRetrieveTopObjectDetails() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-1.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.getConnection();
            this.testSpike = new SparqlQuerySpike();
            
            Map<String, Object> map = this.testSpike.getTopObjectDetails(conn, contextUri, nextOntologyID.getInferredOntologyIRI().toOpenRDFURI());
            
            Assert.assertEquals("Incorrect number of statements about Top Object", 13, map.size());
            Assert.assertNotNull("Top Object's URI was null", map.get("objecturi"));
            Assert.assertTrue("URI start not expected format", map.get("objecturi").toString().startsWith("http://purl.org/podd/"));
            Assert.assertTrue("Missing NotPublished status",map.containsValue("http://purl.org/podd/ns/poddBase#NotPublished"));
            
            // print results
//            for (Map.Entry<String, Object> entry : map.entrySet()) {
//              System.out.println("   " + entry.getKey() + " = (" + entry.getValue() + ")");
//            }            
        }
        finally
        {
            if(conn != null)
            {
                conn.rollback();
                conn.close();
            }
        }
    }
    
    /**
     * Test retrieve list of level 1 objects in a project
     */
    @Test
    public void testRetrieveTopObjectChildren() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-2.ttl";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.TURTLE);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.getConnection();
            this.testSpike = new SparqlQuerySpike();
            
            Map<String, Object> map = this.testSpike.getObjectList(conn, contextUri, nextOntologyID.getInferredOntologyIRI().toOpenRDFURI());
            
//            Assert.assertEquals("Incorrect number of statements about Top Object", 13, map.size());
//            Assert.assertNotNull("Top Object's URI was null", map.get("objecturi"));
//            Assert.assertTrue("URI start not expected format", map.get("objecturi").toString().startsWith("http://purl.org/podd/"));
//            Assert.assertTrue("Missing NotPublished status",map.containsValue("http://purl.org/podd/ns/poddBase#NotPublished"));
            
            // print results
            for (Map.Entry<String, Object> entry : map.entrySet()) {
              System.out.println("   " + entry.getKey() + " = (" + entry.getValue() + ")");
            }      
            
            this.printContents(conn, 
                    ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-2-20130206/artifact:1:version:1"));
            this.printContents(conn, 
                    ValueFactoryImpl.getInstance().createURI("urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/basic-2-20130206/artifact:1:version:1"));
            this.printContexts(conn);
            
        }
        finally
        {
            if(conn != null)
            {
                conn.rollback();
                conn.close();
            }
        }
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
     * Copied from PoddSesameManagerImpl.java as a reference only.
     */
    @Ignore
    @Test
    public void testOntologyImports() throws Exception
    {
        final String testResourcePath = "/test/artifacts/basic-1.rdf";
        final InferredOWLOntologyID nextOntologyID = this.loadArtifact(testResourcePath, RDFFormat.RDFXML);
        final URI contextUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.getConnection();
            Assert.assertEquals("Not the expected number of statements in Repository", 33, conn.size(contextUri));
            
            this.testSpike = new SparqlQuerySpike();
            final Set<IRI> imports = this.testSpike.getDirectImports(conn, contextUri);
            Assert.assertEquals("Podd-Base should have 2 imports", 2, imports.size());
            
            // DEBUG - print repository contents to console
            // this.printContexts(conn);
            this.printContents(conn, this.artifactGraph);
        }
        finally
        {
            if(conn != null)
            {
                conn.rollback();
                conn.close();
            }
        }
    }
    
}
