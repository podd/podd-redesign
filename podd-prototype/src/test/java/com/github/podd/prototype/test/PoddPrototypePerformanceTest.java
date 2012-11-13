package com.github.podd.prototype.test;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.semanticweb.owlapi.io.OWLParserFactoryRegistry;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.model.OWLOntologyStorerFactoryRegistry;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.prototype.InferredOWLOntologyID;
import com.github.podd.prototype.PoddPrototypeUtils;

/**
 * Parameterized class for testing the performance of loading PODD artifacts.
 * 
 * @author kutila
 * @created 2012/10/23
 */
@RunWith(value = Parameterized.class)
public class PoddPrototypePerformanceTest extends AbstractSesameTest
{
    
    private OWLOntologyManager manager;
    private OWLReasonerFactory reasonerFactory;
    private String reasonerName;
    private URI schemaOntologyManagementGraph;
    private URI poddArtifactManagementGraph;
    private IRI pelletOwlProfile;
    
    private PoddPrototypeUtils utils;
    
    private String poddBasePath;
    private String poddSciencePath;

    /**
     * log4j logger which writes to the statistics file.
     */
    private final Logger statsLogger = LoggerFactory.getLogger("statsLogger");
    
    @BeforeClass
    public static void beforeClass() throws Exception
    {
        for(int i = 0; i < 15000; i++)
        {
            OWLOntologyManagerFactoryRegistry.getInstance().getAll();
            OWLParserFactoryRegistry.getInstance().getAll();
            OWLOntologyStorerFactoryRegistry.getInstance().getAll();
            OWLProfileRegistry.getInstance().getAll();
            OWLReasonerFactoryRegistry.getInstance().getAll();        
        }
    }
    
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        
        // create the manager to use for the test
        this.manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        Assert.assertNotNull("Could not create a manager", this.manager);
        
        this.reasonerName = "Pellet";
        this.reasonerFactory = OWLReasonerFactoryRegistry.getInstance().getReasonerFactory(this.reasonerName);
        
        Assert.assertNotNull("Could not find reasoner", this.reasonerFactory);
        this.pelletOwlProfile = OWLProfile.OWL2_DL;
        
        this.schemaOntologyManagementGraph =
                this.getTestValueFactory().createURI("urn:test:schemaOntologiesManagementGraph");
        this.poddArtifactManagementGraph = this.getTestValueFactory().createURI("urn:test:poddArtifactManagementGraph");
        
        this.utils =
                new PoddPrototypeUtils(this.manager, this.pelletOwlProfile, this.reasonerFactory,
                        this.schemaOntologyManagementGraph, this.poddArtifactManagementGraph);
        
        this.poddBasePath = "/ontologies/poddBase.owl";
        this.poddSciencePath = "/ontologies/poddScience.owl";
    }
    
    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        
        this.manager = null;
        this.reasonerFactory = null;
        this.utils = null;
    }
    
    // ----------- parameters for junit test----------------
    private boolean isPlant;
    private String filename;
    
    /**
     * Parameterized constructor
     * 
     * @param number
     */
    public PoddPrototypePerformanceTest(final String filename, final boolean isPlant)
    {
        this.filename = filename;
        this.isPlant = isPlant;
    }
    
    @Parameters
    public static Collection<Object[]> data()
    {
        final Object[][] data =
                new Object[][] {
                        { "/test/artifacts/plant-1k-objects.rdf", true }, //take care of the cold start
                        { "/test/artifacts/plant-1k-objects.rdf", true },
                        { "/test/artifacts/plant-3k-objects.rdf", true },
                        { "/test/artifacts/plant-10k-objects.rdf", true },
                        { "/test/artifacts/science-1k-objects-deep.rdf", false },
                        { "/test/artifacts/science-3k-objects-deep.rdf", false },
                        { "/test/artifacts/science-10k-objects-deep.rdf", false },
                        { "/test/artifacts/science-1k-objects-shallow.rdf", false },
                        { "/test/artifacts/science-3k-objects-shallow.rdf", false },
                        { "/test/artifacts/science-10k-objects-shallow.rdf", false },
                        { "/test/artifacts/plant-1k-objects.nt", true },
                        { "/test/artifacts/plant-3k-objects.nt", true },
                        { "/test/artifacts/plant-10k-objects.nt", true },
                        { "/test/artifacts/science-1k-objects.nt", false },
                        { "/test/artifacts/science-3k-objects.nt", false },
                        { "/test/artifacts/science-10k-objects.nt", false } };
        return Arrays.asList(data);
    }
    
    @Test
    public void testLoadArtifactPerformance() throws Exception
    {
        if(this.isPlant)
        {
            this.loadPlantImports(this.filename);
        }
        else
        {
            this.loadScienceImports(this.filename);
        }
        
        final String mimeType = Rio.getParserFormatForFileName(this.filename, RDFFormat.RDFXML).getDefaultMIMEType();
        
        this.statsLogger.info(this.filename.substring(this.filename.lastIndexOf('/') + 1) + ",");
        
        final long startedAt = System.currentTimeMillis();
        InferredOWLOntologyID inferred = this.utils.loadPoddArtifact(
                this.filename, mimeType, this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        // write statistics
        StringBuilder statsMsg = new StringBuilder();
        
        // time to load (ms)
        statsMsg.append((System.currentTimeMillis() - startedAt));
        statsMsg.append(',');
        
        // ontology statement count
        statsMsg.append(this.getTestRepositoryConnection().size(inferred.getVersionIRI().toOpenRDFURI())); 
        statsMsg.append(',');
        
        // inferred statement count
        statsMsg.append(this.getTestRepositoryConnection().size(inferred.getInferredOntologyIRI().toOpenRDFURI()));
        statsMsg.append("\n");
        this.statsLogger.info(statsMsg.toString());
    }
    
    private void loadPlantImports(final String filename) throws Exception
    {
        final OWLOntologyID modifiedId =
                new OWLOntologyID(IRI.create("http://purl.obolibrary.org/obo/po.owl"),
                        IRI.create("urn:test:plantontology:version:16.0"));
        
        this.utils.loadInferAndStoreSchemaOntology("/ontologies/plant_ontology-v16.owl",
                RDFFormat.RDFXML.getDefaultMIMEType(), modifiedId, this.getTestRepositoryConnection());
    }
    
    private void loadScienceImports(final String filename) throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
    }
    
}
