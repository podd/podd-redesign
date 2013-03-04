/**
 * 
 */
package com.github.podd.api.test;

import java.io.InputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.exception.EmptyOntologyException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;

/**
 * Abstract test to verify that the PoddSchemaManager API contract is followed by implementations.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public abstract class AbstractPoddSchemaManagerTest
{
    private PoddSchemaManager testSchemaManager;
    private PoddOWLManager testOwlManager;
    private PoddRepositoryManager testRepositoryManager;
    private OWLOntologyManager owlapiManager;
    private PoddSesameManager testSesameManager;
    
    /**
     * 
     * @return A new instance of {@link PoddOWLManager}, for each call to this method.
     */
    protected abstract PoddOWLManager getNewPoddOwlManagerInstance();
    
    /**
     * 
     * @return A new empty instance of an implementation of {@link OWLReasonerFactory}.
     */
    protected abstract OWLReasonerFactory getNewReasonerFactory();
    
    /**
     * 
     * @return A new instance of {@link OWLOntologyManager}, for each call to this method.
     */
    protected abstract OWLOntologyManager getNewOwlOntologyManagerInstance();
    
    /**
     * 
     * @return A new instance of {@link PoddRepositoryManager}, for each call to this method.
     */
    protected abstract PoddRepositoryManager getNewPoddRepositoryManagerInstance();
    
    /**
     * 
     * @return A new instance of {@link PoddSchemaManager}, for each call to this method.
     */
    protected abstract PoddSchemaManager getNewPoddSchemaManagerInstance();
    
    /**
     * 
     * @return A new instance of {@link PoddSesameManager}, for each call to this method.
     */
    protected abstract PoddSesameManager getNewPoddSesameManagerInstance();
    
    /**
     * This internal method loads schema ontologies to PODD. Should be used as a setUp() mechanism
     * where needed. 
     */
    private void loadSchemaOntologies() throws Exception
    {
        String[] schemaResourcePaths =
                { PoddRdfConstants.PATH_PODD_DCTERMS, PoddRdfConstants.PATH_PODD_FOAF, PoddRdfConstants.PATH_PODD_USER,
                        PoddRdfConstants.PATH_PODD_BASE, PoddRdfConstants.PATH_PODD_SCIENCE,
                        PoddRdfConstants.PATH_PODD_PLANT,
                // PoddRdfConstants.PATH_PODD_ANIMAL,
                };
        for (int i = 0; i < schemaResourcePaths.length; i++)
        {
            final InputStream in = this.getClass().getResourceAsStream(schemaResourcePaths[i]);
            this.testSchemaManager.uploadSchemaOntology(in, RDFFormat.RDFXML);
        }
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.testSchemaManager = this.getNewPoddSchemaManagerInstance();
        
        this.testRepositoryManager = this.getNewPoddRepositoryManagerInstance();
        this.testSchemaManager.setRepositoryManager(this.testRepositoryManager);
        
        this.testSesameManager = this.getNewPoddSesameManagerInstance();
        this.testSchemaManager.setSesameManager(this.testSesameManager);
        
        this.testOwlManager = this.getNewPoddOwlManagerInstance();
        this.testOwlManager.setReasonerFactory(this.getNewReasonerFactory());
        
        this.owlapiManager = this.getNewOwlOntologyManagerInstance();
        this.testOwlManager.setOWLOntologyManager(owlapiManager);
        this.testSchemaManager.setOwlManager(this.testOwlManager);
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        this.testSchemaManager = null;
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyMatchingOntologyID() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyNotMatchingVersion() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyNullOntologyID() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyNullOutputStream() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyOnlyOntologyIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyWithInferencesMatchingOntologyID() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyWithInferencesNoInferencesFound() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyWithInferencesNotMatchingVersion() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyWithInferencesNullOntologyID() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyWithInferencesNullOutputStream() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyWithInferencesOnlyOntologyIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     * Passes in an ontology IRI and retrieves the current version of the Ontology.
     * 
     */
    @Test
    public final void testGetCurrentSchemaOntologyVersionMatchesOntologyIRI() throws Exception
    {
        // prepare: load schema ontologies into PODD
        this.loadSchemaOntologies();
        final InputStream in = this.getClass().getResourceAsStream("/test/ontologies/poddPlantV2.owl");
        this.testSchemaManager.uploadSchemaOntology(in, RDFFormat.RDFXML);
        
        String[] testIRIs = {
                "http://purl.org/podd/ns/poddUser", 
                "http://purl.org/podd/ns/poddBase", 
                "http://purl.org/podd/ns/poddScience",
                "http://purl.org/podd/ns/poddPlant", 
                };
        
        String[] expectedVersionIRIs = {
                "http://purl.org/podd/ns/version/poddUser/1", 
                "http://purl.org/podd/ns/version/poddBase/1", 
                "http://purl.org/podd/ns/version/poddScience/1",
                "http://purl.org/podd/ns/version/poddPlant/2", 
                }; 
        
        for (int i = 0; i < testIRIs.length; i++)
        {
            final IRI testIRI = IRI.create(testIRIs[i]);
            InferredOWLOntologyID ontologyID = this.testSchemaManager.getCurrentSchemaOntologyVersion(testIRI);
            Assert.assertEquals("Input IRI does not match ontology IRI of current version", testIRI, ontologyID.getOntologyIRI());
            Assert.assertEquals("Expected Version IRI does not match current version", 
                    IRI.create(expectedVersionIRIs[i]), ontologyID.getVersionIRI());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     * Passes in a version IRI (of the current version) and retrieves the current version of the Ontology.
     */
    @Test
    public final void testGetCurrentSchemaOntologyVersionMatchesOntologyVersionIRICurrent() throws Exception
    {
        // prepare: load schema ontologies into PODD
        this.loadSchemaOntologies();
        final InputStream in = this.getClass().getResourceAsStream("/test/ontologies/poddPlantV2.owl");
        this.testSchemaManager.uploadSchemaOntology(in, RDFFormat.RDFXML);
        
        String[] testIRIs = {
                "http://purl.org/podd/ns/version/poddUser/1", 
                "http://purl.org/podd/ns/version/poddBase/1", 
                "http://purl.org/podd/ns/version/poddScience/1",
                "http://purl.org/podd/ns/version/poddPlant/2", 
                };
        
        for (int i = 0; i < testIRIs.length; i++)
        {
            final IRI testIRI = IRI.create(testIRIs[i]);
            InferredOWLOntologyID ontologyID = this.testSchemaManager.getCurrentSchemaOntologyVersion(testIRI);
            Assert.assertEquals("Input IRI does not match Version IRI of current version", testIRI, ontologyID.getVersionIRI());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     * Passes in a version IRI (of an older version) and retrieves the current version of the Ontology.
     */
    @Test
    public final void testGetCurrentSchemaOntologyVersionMatchesOntologyVersionIRINotCurrent() throws Exception
    {
        // prepare: load schema ontologies into PODD
        this.loadSchemaOntologies();
        final InputStream in = this.getClass().getResourceAsStream("/test/ontologies/poddPlantV2.owl");
        this.testSchemaManager.uploadSchemaOntology(in, RDFFormat.RDFXML);
        
        String[] testIRIs = {
                "http://purl.org/podd/ns/version/poddUser/1", 
                "http://purl.org/podd/ns/version/poddBase/1", 
                "http://purl.org/podd/ns/version/poddScience/1",
                "http://purl.org/podd/ns/version/poddPlant/1",  //an older version of PODD:Plant
                };
        
        String[] expectedVersionIRIs = {
                "http://purl.org/podd/ns/version/poddUser/1", 
                "http://purl.org/podd/ns/version/poddBase/1", 
                "http://purl.org/podd/ns/version/poddScience/1",
                "http://purl.org/podd/ns/version/poddPlant/2", // expected current Version IRI of PODD:Plant
                }; 
        
        for (int i = 0; i < testIRIs.length; i++)
        {
            final IRI testIRI = IRI.create(testIRIs[i]);
            InferredOWLOntologyID ontologyID = this.testSchemaManager.getCurrentSchemaOntologyVersion(testIRI);
            Assert.assertEquals("Expected current version IRI does not match received value", 
                    IRI.create(expectedVersionIRIs[i]), ontologyID.getVersionIRI());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Ignore
    @Test
    public final void testGetCurrentSchemaOntologyVersionNoMatches() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Test
    public final void testGetCurrentSchemaOntologyVersionNull() throws Exception
    {
        try
        {
            this.testSchemaManager.getCurrentSchemaOntologyVersion(null);
            Assert.fail("Should have thrown an UnmanagedSchemaIRIException");
        }
        catch(UnmanagedSchemaIRIException e)
        {
            Assert.assertEquals("NULL is not a managed schema ontology", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.IRI)}
     * .
     * 
     * Test that the designated current version is retrieved when an ontology IRI is given and there
     * are multiple versions available.
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyIRIMatchesOntologyIRIMultipleVersions() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyIRIMatchesOntologyIRIOneVersion() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyIRIMatchesVersionIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyIRINull() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyOWLOntologyIDNullOntologyID() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyOWLOntologyIDNullVersionIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyOWLOntologyIDOntologyExists() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyOWLOntologyIDOntologyIRIDoesNotExist() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyOWLOntologyIDOntologyVersionDoesNotExist() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#setCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testSetCurrentSchemaOntologyVersionOneVersionOfOntology() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#setCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testSetCurrentSchemaOntologyVersionTwoVersionsOfOntologyChange() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#setCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testSetCurrentSchemaOntologyVersionTwoVersionsOfOntologyNoChange() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadSchemaOntologyEmpty() throws Exception
    {
        try
        {
            InputStream testInputStream = this.getClass().getResourceAsStream("/test/ontologies/empty.owl");
            
            this.testSchemaManager.uploadSchemaOntology(testInputStream, RDFFormat.RDFXML);
            
            Assert.fail("Did not receive expected exception");
        }
        catch(EmptyOntologyException e)
        {
            Assert.assertEquals("Message was not as expected", "Loaded ontology is empty", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadSchemaOntologyIDOverrideEmpty() throws Exception
    {
        try
        {
            IRI emptyOntologyIRI = IRI.create("urn:test:empty:ontology:");
            IRI emptyVersionIRI = IRI.create("urn:test:empty:version:");
            OWLOntologyID emptyOntologyID = new OWLOntologyID(emptyOntologyIRI, emptyVersionIRI);
            this.owlapiManager.createOntology(emptyOntologyID);
            
            InputStream testInputStream = this.getClass().getResourceAsStream("/test/ontologies/empty.owl");
            
            this.testSchemaManager.uploadSchemaOntology(emptyOntologyID, testInputStream, RDFFormat.RDFXML);
            
            Assert.fail("Did not receive expected exception");
        }
        catch(EmptyOntologyException e)
        {
            Assert.assertEquals("Message was not as expected", "Loaded ontology is empty", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadSchemaOntologyIDOverrideInvalidRdfXml() throws Exception
    {
        try
        {
            IRI emptyOntologyIRI = IRI.create("urn:test:empty:ontology:");
            IRI emptyVersionIRI = IRI.create("urn:test:empty:version:");
            OWLOntologyID emptyOntologyID = new OWLOntologyID(emptyOntologyIRI, emptyVersionIRI);
            this.owlapiManager.createOntology(emptyOntologyID);
            
            InputStream testInputStream = this.getClass().getResourceAsStream("/test/ontologies/justatextfile.owl");
            
            this.testSchemaManager.uploadSchemaOntology(emptyOntologyID, testInputStream, RDFFormat.RDFXML);
            
            Assert.fail("Did not receive expected exception");
        }
        catch(UnparsableOntologyException e)
        {
            Assert.assertTrue("Message was not as expected", e.getMessage().startsWith("Problem parsing "));
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadSchemaOntologyIDOverrideInvalidTurtle() throws Exception
    {
        try
        {
            IRI emptyOntologyIRI = IRI.create("urn:test:empty:ontology:");
            IRI emptyVersionIRI = IRI.create("urn:test:empty:version:");
            OWLOntologyID emptyOntologyID = new OWLOntologyID(emptyOntologyIRI, emptyVersionIRI);
            this.owlapiManager.createOntology(emptyOntologyID);
            
            InputStream testInputStream = this.getClass().getResourceAsStream("/test/ontologies/invalidturtle.ttl");
            
            this.testSchemaManager.uploadSchemaOntology(emptyOntologyID, testInputStream, RDFFormat.TURTLE);
            
            Assert.fail("Did not receive expected exception");
        }
        catch(UnparsableOntologyException e)
        {
            Assert.assertTrue("Message was not as expected", e.getMessage().startsWith("Problem parsing "));
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyIDOverrideNoOntologyIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyIDOverrideNotConsistent() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyIDOverrideNotInProfile() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadSchemaOntologyIDOverrideNullInput() throws Exception
    {
        try
        {
            IRI emptyOntologyIRI = IRI.create("urn:test:empty:ontology:");
            IRI emptyVersionIRI = IRI.create("urn:test:empty:version:");
            OWLOntologyID emptyOntologyID = new OWLOntologyID(emptyOntologyIRI, emptyVersionIRI);
            this.owlapiManager.createOntology(emptyOntologyID);
            
            this.testSchemaManager.uploadSchemaOntology(emptyOntologyID, null, RDFFormat.RDFXML);
            
            Assert.fail("Did not receive expected exception");
        }
        catch(NullPointerException e)
        {
            Assert.assertEquals("Message was not as expected", "Schema Ontology input stream was null", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyIDOverrideOnlyOntologyIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyIDOverrideWithOntologyIRIAndVersionIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadSchemaOntologyInvalidRdfXml() throws Exception
    {
        try
        {
            InputStream testInputStream = this.getClass().getResourceAsStream("/test/ontologies/justatextfile.owl");
            
            this.testSchemaManager.uploadSchemaOntology(testInputStream, RDFFormat.RDFXML);
            
            Assert.fail("Did not receive expected exception");
        }
        catch(UnparsableOntologyException e)
        {
            Assert.assertTrue("Message was not as expected", e.getMessage().startsWith("Problem parsing "));
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadSchemaOntologyInvalidTurtle() throws Exception
    {
        try
        {
            InputStream testInputStream = this.getClass().getResourceAsStream("/test/ontologies/invalidturtle.ttl");
            
            this.testSchemaManager.uploadSchemaOntology(testInputStream, RDFFormat.RDFXML);
            
            Assert.fail("Did not receive expected exception");
        }
        catch(UnparsableOntologyException e)
        {
            Assert.assertTrue("Message was not as expected", e.getMessage().startsWith("Problem parsing "));
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyNoOntologyIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyNotConsistent() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyNotInProfile() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadSchemaOntologyNullInput() throws Exception
    {
        try
        {
            this.testSchemaManager.uploadSchemaOntology(null, RDFFormat.RDFXML);
            
            Assert.fail("Did not receive expected exception");
        }
        catch(NullPointerException e)
        {
            Assert.assertEquals("Message was not as expected", "Schema Ontology input stream was null", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyOnlyOntologyIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadSchemaOntologyWithOntologyIRIAndVersionIRI() throws Exception
    {
        String[] resourcePaths =
                { "/ontologies/dcTerms.owl", "/ontologies/foaf.owl", "/ontologies/poddUser.owl",
                        "/ontologies/poddBase.owl", };
        
        for(String path : resourcePaths)
        {
            InputStream testInputStream = this.getClass().getResourceAsStream(path);
            
            this.testSchemaManager.uploadSchemaOntology(testInputStream, RDFFormat.RDFXML);
        }
        
        // TODO: verify
    }
    
}
