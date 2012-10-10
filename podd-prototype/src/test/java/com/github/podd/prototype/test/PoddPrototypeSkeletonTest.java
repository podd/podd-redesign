/**
 * 
 */
package com.github.podd.prototype.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.semanticweb.owlapi.util.OWLObjectVisitorExAdapter;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;

import com.clarkparsia.owlapiv3.OWL;
import com.github.podd.prototype.InferredOWLOntologyID;
import com.github.podd.prototype.PoddException;
import com.github.podd.prototype.PoddPrototypeUtils;

/**
 * This class performs some basic actions that are necessary for the PODD Prototype.
 * 
 * It also validates that the Schema Ontologies we are using can be loaded in using the OWL2-DL
 * profile, which is necessary to use the Schema Ontologies with the Pellet OWLReasoner.
 * 
 * This class also tests that a valid PODD Artifact can be loaded into PODD, and that an invalid
 * PODD Artifact will fail to be loaded into PODD.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddPrototypeSkeletonTest extends AbstractSesameTest
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
    private String poddAnimalPath;
    private String poddPlantPath;
    private String poddUserPath;
    
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        
        // create the manager to use for the test
        this.manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        Assert.assertNotNull("Could not create a manager", this.manager);
        
        // TODO: Pellet should be configurable
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
        this.poddAnimalPath = "/ontologies/poddAnimal.owl";
        this.poddPlantPath = "/ontologies/poddPlant.owl";
        this.poddUserPath = "/ontologies/poddUser.owl";
        
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
    
    
    @Test
    public void testOne() throws Exception 
    {
		OWLOntologyID id = this.utils.loadInferAndStoreSchemaOntology(
				this.poddBasePath, this.getTestRepositoryConnection());
		this.getTestRepositoryConnection().commit();
		
		OWLOntology o = this.utils.loadOntology(this.poddPlantPath);
		this.getTestRepositoryConnection().commit();

		  // Create the walker
        OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(o));
        // Now ask our walker to walk over the ontology
        OWLOntologyWalkerVisitor<Object> visitor = new OWLOntologyWalkerVisitor<Object>(
                walker) {
            @Override
            public Object visit(OWLObjectSomeValuesFrom desc) {
                // Print out the restriction
                 System.out.println(desc);
                // Print out the axiom where the restriction is used
                 System.out.println("         " + getCurrentAxiom());
                 System.out.println();
                // We don't need to return anything here.
                return null;
            }
        };
        // Now ask the walker to walk over the ontology structure using our
        // visitor instance.
        walker.walkStructure(visitor);
    }

    /**
     * Not a very good test as it hard codes poddBase ontology concepts.
     * Created just to familiarize myself.
     * Query contents of poddBase ontology to ensure that it is loaded correctly
     *  - [KG]
     */
    @Test
    public void testBaseOntologyContent() 
    {
        try 
        {
			OWLOntology o = this.utils.loadOntology(this.poddBasePath);
	        assertNotNull(o);
			OWLReasoner reasoner = this.utils.checkConsistency(o);
			reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
			
			IRI poddTopObjIRI = IRI.create("http://purl.org/podd/ns/poddBase#PoddTopObject");
			IRI poddObjIRI = IRI.create("http://purl.org/podd/ns/poddBase#PoddObject");
			boolean topObjExists = false;
			boolean topObjHierarcyExists = false;
	        // These are the named classes referenced by axioms in the ontology.
	        for (OWLClass cls : o.getClassesInSignature()) 
	        {
	        	if (poddTopObjIRI.equals(cls.getIRI())) 
	        	{
	        		topObjExists = true;
	        	}
	        	
	        	if (cls.getIRI().equals(poddObjIRI))
	        	{
		            NodeSet<OWLClass> subClasses = reasoner.getSubClasses(cls, true);
		            for (OWLClass subClass : subClasses.getFlattened()) {
			        	if (poddTopObjIRI.equals(subClass.getIRI())) 
			        	{
			        		topObjHierarcyExists = true;
			        	}
		            }
	        	}
	        }			
			Assert.assertTrue("Top Object not found.", topObjExists);
			Assert.assertTrue("Top Object hierarchy not found.", topObjHierarcyExists);
        }
        catch (Exception e) 
        {
			Assert.fail("Unexpected exception: " + e.toString());
		}
    }
    
    
    /**
     * Tests the combination of the base, science, and the podd animal ontologies to verify their
     * internal consistency.
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseAndScienceAndPoddAnimalOntologies() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, this.getTestRepositoryConnection());
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, this.getTestRepositoryConnection());
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddAnimalPath, this.getTestRepositoryConnection());
    }
    
    /**
     * Tests the combination of the base, science, and the podd plant ontologies to verify their
     * internal consistency.
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseAndScienceAndPoddPlantOntologies() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, this.getTestRepositoryConnection());
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, this.getTestRepositoryConnection());
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddPlantPath, this.getTestRepositoryConnection());
    }
    
    /**
     * Tests the combination of the base and science ontologies to verify their internal
     * consistency.
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseAndScienceOntologies() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, this.getTestRepositoryConnection());
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, this.getTestRepositoryConnection());
    }
    
    /**
     * Tests the loading of the base and science ontologies followed by loading a single artifact.
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseAndScienceOntologyAndSingleArtifact() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        final InferredOWLOntologyID poddArtifact =
                this.utils.loadPoddArtifact("/test/artifacts/basicProject-1.rdf", this.getTestRepositoryConnection());
        
        // Final: Remove the PODD Artifact Ontology from the manager cache
        this.utils.removePoddArtifactFromManager(poddArtifact);
        // TODO: May eventually need to create a super-class of OWLOntologyManagerImpl that knows
        // how to fetch PODD Artifact ontologies from a repository if they are not currently in
        // memory
        // Cannot (easily?) use an IRI mapper for this process as far as I can tell
    }
    
    /**
     * Tests an inconsistent semantic ontology, containing two hasLeadInstitution statements on a
     * single Project.
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseAndScienceOntologyAndSingleArtifactInconsistent() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        try
        {
            this.utils.loadPoddArtifact("/test/artifacts/error-twoLeadInstitutions-1.rdf",
                    this.getTestRepositoryConnection());
            Assert.fail("Did not receive expected exception");
        }
        catch(final PoddException pe)
        {
            Assert.assertTrue("Exception does not have expected code",
            		pe.getCode() == PoddException.ERR_INCONSISTENT_ONTOLOGY);
        }
    }
    
    /**
     * Tests an inconsistent semantic ontology, where the artifactHasTopObject statements does not refer 
     * to a subclass of poddBase:PoddTopObject.
     * 
     * NOTE: This is difficult with OWL-DL, so temporarily ignoring this test until we have native rules 
     * to validate this constraint.
     * 
     * @throws Exception
     */
    @Ignore
    @Test
    public final void testBaseAndScienceOntologyAndSingleArtifactInconsistentNotTopObject() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        try
        {
            this.utils.loadPoddArtifact("/test/artifacts/error-badTopObjectReference-1.rdf",
                    this.getTestRepositoryConnection());
            Assert.fail("Did not receive expected exception");
        }
        catch(final PoddException pe)
        {
            Assert.assertTrue("Exception does not have expected code",
                    pe.getCode() == PoddException.ERR_INCONSISTENT_ONTOLOGY);
        }
    }
    
    /**
     * Tests the combination of the base and user ontologies to verify their internal consistency.
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseAndUserOntologies() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, this.getTestRepositoryConnection());
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddUserPath, this.getTestRepositoryConnection());
        
    }
    
    /**
     * Tests the base ontology to verify its internal consistency.
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseOntology() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, this.getTestRepositoryConnection());
    }
    
    /**
     * Tests the Open Biomedical Ontologies Plant Ontology to verify its consistency with OWL2-DL.
     * 
     * @throws Exception
     */
    @Test
    public final void testOBOPlantOntology() throws Exception
    {
        // create owl class objects to use in queries
        final OWLClass phylomeStomatalComplex = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025215"));
        final OWLClass bractStomatalComplex = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025216"));
        final OWLClass plantAnatomicalEntity = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025131"));
        final OWLClass phylome = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0006001"));
        
        final OWLOntologyID modifiedId =
                new OWLOntologyID(IRI.create("http://purl.obolibrary.org/obo/po.owl"),
                        IRI.create("urn:test:plantontology:version:16.0"));
        
        final InferredOWLOntologyID inferredOWLOntologyID =
                this.utils.loadInferAndStoreSchemaOntology("/ontologies/plant_ontology-v16.owl", modifiedId,
                        this.getTestRepositoryConnection()); 
        
        // verify that the triples were inserted into the repository correctly by testing the size
        // of different contexts and then testing the size of the complete repository to verify that
        // no other triples were inserted
        Assert.assertEquals(2995,
                this.getTestRepositoryConnection().size(inferredOWLOntologyID.getInferredOntologyIRI().toOpenRDFURI()));
        Assert.assertEquals(44333,
                this.getTestRepositoryConnection().size(inferredOWLOntologyID.getVersionIRI().toOpenRDFURI()));
        Assert.assertEquals(6, this.getTestRepositoryConnection().size(this.schemaOntologyManagementGraph));
        Assert.assertEquals(47334, this.getTestRepositoryConnection().size());
        
        if(this.log.isTraceEnabled())
        {
            for(final Statement nextStatement : this
                    .getTestRepositoryConnection()
                    .getStatements(null, null, null, true,
                            inferredOWLOntologyID.getInferredOntologyIRI().toOpenRDFURI()).asList())
            {
                this.log.trace(nextStatement.toString());
            }
        }
        
        // TODO:
        // Load a set of objects in as an ontology that imports the plant ontology into the system
        // and verify that it is consistent
        
        // TODO: Decide on a consistent strategy for linking the object with the version of the
        // ontology.
        // OWL:IMPORTS will work but we will need to know when and how to update the version, so it
        // may be useful to create another ontology annotation property to detail the current
        // version in use so we can query directly for managed ontologies
        
        // Make a change to the objects and store the resulting ontology as a new version of the
        // objects ontology that links to the first version
        
        // Update the plant ontology with a new axiom/class/property and store it and an inferred
        // ontology along with it
        
        // Make another change to the object to use the new axiom/class/property and verify that it
        // is consistent and references the new version
        
        // Verify that the original version can be loaded and references the first version of the
        // ontology schema
    }
    
    /**
     * Tests whether the file is valid RDF according to Sesame.
     * 
     * @throws Exception
     */
    @Test
    public final void testRdf() throws Exception
    {
        try
        {
            this.getTestRepositoryConnection().add(
                    this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf"), "", RDFFormat.RDFXML);
            this.getTestRepositoryConnection().commit();
            
            Assert.assertEquals(24, this.getTestRepositoryConnection().size());
        }
        catch(final Exception ex)
        {
            this.getTestRepositoryConnection().rollback();
            ex.printStackTrace();
            Assert.fail("Found unexpected exception: " + ex.getMessage());
        }
    }
    
}
