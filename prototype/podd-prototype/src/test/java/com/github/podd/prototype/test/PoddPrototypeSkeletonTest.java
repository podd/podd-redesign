/**
 * 
 */
package com.github.podd.prototype.test;

import java.util.HashMap;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

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
    private IRI elkOwlProfile;
    
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
        
        // this.reasonerName = "Hermit";
        // this.reasonerFactory = new org.semanticweb.HermiT.Reasoner.ReasonerFactory();
        Assert.assertNotNull("Could not find reasoner", this.reasonerFactory);
        this.pelletOwlProfile = OWLProfile.OWL2_DL;
        this.elkOwlProfile = OWLProfile.OWL2_EL;
        
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
    
    /**
     * Tests that consistency checks fail when a non-existent OWL Profile is specified.
     * 
     * @throws Exception
     */
    @Test
    public final void testNonsensicalOWLProfile() throws Exception
    {
        final IRI nonsensicalOwlProfile = IRI.create("http://no.such/profile");
        final PoddException e = this.runConsistencyCheck(nonsensicalOwlProfile);
        Assert.assertNotNull(e);
        Assert.assertEquals("Unexpected error code", PoddException.ERR_PROFILE_NOT_FOUND, e.getCode());
    }
    
    /**
     * Tests that consistency checks fail when a an unsupported OWL Profile is specified.
     * 
     * @throws Exception
     */
    @Test
    public final void testUnsupportedOWLProfile() throws Exception
    {
        final IRI unsupportedOwlProfile = OWLProfile.OWL2_RL;
        final PoddException e = this.runConsistencyCheck(unsupportedOwlProfile);
        Assert.assertNotNull(e);
        Assert.assertEquals("Unexpected error code", PoddException.ERR_ONTOLOGY_NOT_IN_PROFILE, e.getCode());
    }
    
    /**
     * Helper method to check consistency based on different OWL profiles
     * 
     * @param owlProfile
     * @return The PoddException that was thrown
     * @throws Exception
     */
    private PoddException runConsistencyCheck(final IRI owlProfile) throws Exception
    {
        try
        {
            this.utils =
                    new PoddPrototypeUtils(this.manager, owlProfile, this.reasonerFactory,
                            this.schemaOntologyManagementGraph, this.poddArtifactManagementGraph);
            final OWLOntology o = this.utils.loadOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType());
            Assert.assertNotNull(o);
            this.utils.checkConsistency(o);
            this.getTestRepositoryConnection().rollback();
            Assert.fail("Consistency check should have failed.");
        }
        catch(final PoddException e)
        {
            this.getTestRepositoryConnection().rollback();
            return e;
        }
        return null; // won't get here really
    }
    
    /**
     * Tests that loading an empty OWL ontology fails with the appropriate PoddException.
     * 
     * @throws Exception
     */
    @Test
    public final void testLoadEmptyOntology() throws Exception
    {
        try
        {
            this.utils.loadOntology("/test/ontologies/empty.owl", RDFFormat.RDFXML.getDefaultMIMEType());
            this.getTestRepositoryConnection().rollback();
            Assert.fail("Should have failed to load empty ontology.");
        }
        catch(final PoddException e)
        {
            this.getTestRepositoryConnection().rollback();
            Assert.assertEquals(PoddException.ERR_EMPTY_ONTOLOGY, e.getCode());
        }
    }
    
    /**
     * Tests that the contents of an ontology (PoddBase) are correctly loaded by querying its
     * contents.
     * 
     * Test will FAIL if poddBase ontology is modified.
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseOntologyContent() throws Exception
    {
        final OWLOntology o = this.utils.loadOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType());
        Assert.assertNotNull(o);
        final OWLReasoner reasoner = this.utils.checkConsistency(o);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        
        final IRI poddTopObjIRI = IRI.create("http://purl.org/podd/ns/poddBase#PoddTopObject");
        final IRI poddObjIRI = IRI.create("http://purl.org/podd/ns/poddBase#PoddObject");
        boolean topObjExists = false;
        boolean topObjHierarcyExists = false;
        // These are the named classes referenced by axioms in the ontology.
        for(final OWLClass cls : o.getClassesInSignature())
        {
            if(poddTopObjIRI.equals(cls.getIRI()))
            {
                topObjExists = true;
            }
            
            if(cls.getIRI().equals(poddObjIRI))
            {
                final NodeSet<OWLClass> subClasses = reasoner.getSubClasses(cls, true);
                for(final OWLClass subClass : subClasses.getFlattened())
                {
                    if(poddTopObjIRI.equals(subClass.getIRI()))
                    {
                        topObjHierarcyExists = true;
                    }
                }
            }
        }
        Assert.assertTrue("Top Object not found.", topObjExists);
        Assert.assertTrue("Top Object hierarchy not found.", topObjHierarcyExists);
    }
    
    /**
     * Test that computing inferences of a very simple ontology works.
     */
    @Test
    public final void testComputeInferences() throws Exception
    {
        final OWLOntology nextOntology =
                this.utils.loadOntology("/test/ontologies/lonely.owl", RDFFormat.RDFXML.getDefaultMIMEType());
        Assert.assertEquals(2, nextOntology.getAxiomCount(AxiomType.SUBCLASS_OF));
        
        final OWLReasoner reasoner = this.utils.checkConsistency(nextOntology);
        final OWLOntology nextInferredOntology =
                this.utils.computeInferences(reasoner,
                        this.utils.generateInferredOntologyID(nextOntology.getOntologyID()));
        
        Assert.assertEquals(3, nextInferredOntology.getAxiomCount(AxiomType.SUBCLASS_OF));
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
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddAnimalPath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
    }
    
    /**
     * Tests that when required base ontologies are not loaded, consistency checks will fail.
     * 
     * NOTE: this test took 210 seconds to run on Kutila's PC. Indicates a possible performance
     * issue if attempts are made to add schema ontologies in the wrong order. Test is currently
     * skipped due to the length of time taken.
     * 
     * @throws Exception
     */
    @Ignore
    @Test
    public final void testIncompleteOntologyImportClosure() throws Exception
    {
        try
        {
            // we're not loading PoddBase which is needed by PoddScience
            this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                    this.getTestRepositoryConnection());
            this.getTestRepositoryConnection().rollback();
            
            Assert.fail("Did not fail as expected.");
        }
        catch(final PoddException e)
        {
            this.getTestRepositoryConnection().rollback();
            Assert.assertEquals(PoddException.ERR_ONTOLOGY_NOT_IN_PROFILE, e.getCode());
            // TODO: error code has to change to be made more meaningful
        }
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
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddPlantPath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
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
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
    }
    
    /**
     * Tests the loading of the base and science ontologies followed by loading a single artifact.
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseAndScienceOntologyAndSingleArtifact() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        final InferredOWLOntologyID poddArtifact =
                this.utils.loadPoddArtifact("/test/artifacts/basicProject-1.rdf",
                        RDFFormat.RDFXML.getDefaultMIMEType(), this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        // Final: Remove the PODD Artifact Ontology from the manager cache
        final boolean removed = this.utils.removePoddArtifactFromManager(poddArtifact);
        Assert.assertTrue("Could not remove artifact from Manager", removed);
        // TODO: May eventually need to create a super-class of OWLOntologyManagerImpl that knows
        // how to fetch PODD Artifact ontologies from a repository if they are not currently in
        // memory
        // Cannot (easily?) use an IRI mapper for this process as far as I can tell
    }
    
    /**
     * Tests that loading an artifact fails when schema ontologies are not available.
     * 
     * FIXME: takes too long and fails sometimes.
     * 
     * @throws Exception
     */
    @Ignore
    @Test
    public final void testSingleArtifactMissingSchemaOntologies() throws Exception
    {
        try
        {
            this.utils.loadPoddArtifact("/test/artifacts/basicProject-1.rdf", RDFFormat.RDFXML.getDefaultMIMEType(),
                    this.getTestRepositoryConnection());
            
            this.getTestRepositoryConnection().rollback();
            Assert.fail("Did not fail loading as expected");
        }
        catch(final PoddException e)
        {
            this.getTestRepositoryConnection().rollback();
            Assert.assertEquals(PoddException.ERR_ONTOLOGY_NOT_IN_PROFILE, e.getCode());
        }
    }
    
    /**
     * Tests the loading two artifacts (after loading base and science schema ontologies). This test
     * verifies that each artifact is given a unique permanent URI
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseAndScienceOntologyAndTwoArtifacts() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        // load artifact 1
        final InferredOWLOntologyID poddArtifact1 =
                this.utils.loadPoddArtifact("/test/artifacts/basicProject-1.rdf",
                        RDFFormat.RDFXML.getDefaultMIMEType(), this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        // load artifact 2 (using the same resource)
        final InferredOWLOntologyID poddArtifact2 =
                this.utils.loadPoddArtifact("/test/artifacts/basicProject-1.rdf",
                        RDFFormat.RDFXML.getDefaultMIMEType(), this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        // Final: Remove the PODD Artifacts from the manager cache
        this.utils.removePoddArtifactFromManager(poddArtifact1);
        this.utils.removePoddArtifactFromManager(poddArtifact2);
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
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        try
        {
            this.utils.loadPoddArtifact("/test/artifacts/error-twoLeadInstitutions-1.rdf",
                    RDFFormat.RDFXML.getDefaultMIMEType(), this.getTestRepositoryConnection());
            Assert.fail("Did not receive expected exception");
        }
        catch(final PoddException pe)
        {
            Assert.assertTrue("Exception does not have expected code",
                    pe.getCode() == PoddException.ERR_INCONSISTENT_ONTOLOGY);
        }
        finally
        {
            this.getTestRepositoryConnection().rollback();
        }
    }
    
    /**
     * Tests an inconsistent semantic ontology, where the artifactHasTopObject statements does not
     * refer to a subclass of poddBase:PoddTopObject.
     * 
     * NOTE: This is difficult with OWL-DL, so temporarily ignoring this test until we have native
     * rules to validate this constraint.
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseAndScienceOntologyAndSingleArtifactInconsistentNotTopObject() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        try
        {
            this.utils.loadPoddArtifact("/test/artifacts/error-badTopObjectReference-1.rdf",
                    RDFFormat.RDFXML.getDefaultMIMEType(), this.getTestRepositoryConnection());
            Assert.fail("Did not receive expected exception");
        }
        catch(final PoddException pe)
        {
            Assert.assertTrue("Exception does not have expected code",
                    pe.getCode() == PoddException.ERR_INCONSISTENT_ONTOLOGY);
            this.log.info(pe.getMessage());
        }
        finally
        {
            this.getTestRepositoryConnection().rollback();
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
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddUserPath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
    }
    
    /**
     * Tests the base ontology to verify its internal consistency.
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseOntology() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
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
                this.utils.loadInferAndStoreSchemaOntology("/ontologies/plant_ontology-v16.owl",
                        RDFFormat.RDFXML.getDefaultMIMEType(), modifiedId, this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
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
    
    /**
     * Tests loading a schema ontology which does not have a version IRI.
     * 
     * @throws Exception
     */
    @Test
    public final void testSchemaOntologyWithoutVersionIRI() throws Exception
    {
        try
        {
            this.utils.loadInferAndStoreSchemaOntology("/ontologies/plant_ontology-v16.owl",
                    RDFFormat.RDFXML.getDefaultMIMEType(), this.getTestRepositoryConnection());
            
            this.getTestRepositoryConnection().rollback();
            Assert.fail("Did not receive expected exception");
        }
        catch(final PoddException e)
        {
            this.getTestRepositoryConnection().rollback();
            Assert.assertTrue(e.getMessage().contains("Cannot load a schema ontology that does not have a version IRI"));
        }
    }
    
    /**
     * Tests loading a schema ontology which does not have a version IRI, with a hint, to make it
     * succeed.
     * 
     * @throws Exception
     */
    @Test
    public final void testSchemaOntologyWithoutVersionIRIWithHint() throws Exception
    {
        final OWLOntologyID modifiedId =
                new OWLOntologyID(IRI.create("http://purl.obolibrary.org/obo/po.owl"),
                        IRI.create("urn:test:plantontology:version:16.0"));
        
        final InferredOWLOntologyID inferredID =
                this.utils.loadInferAndStoreSchemaOntology("/ontologies/plant_ontology-v16.owl",
                        RDFFormat.RDFXML.getDefaultMIMEType(), modifiedId, this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        // verify a version IRI has been correctly assigned
        Assert.assertNotNull(inferredID.getInferredOntologyIRI());
    }
    
    /**
     * Tests loading a Science schema ontology with a non-unique version IRI set.
     * 
     * FIXME: loadInferAndStoreSchemaOntology() needs to be modified to check the ontology for
     * version information and add one if none exists, or change it if the existing one is not
     * unique
     * 
     * @throws Exception
     */
    @Ignore
    @Test
    public final void testScienceOntologyWithNonUniqueVersion() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        final OWLOntologyID modifiedId =
                new OWLOntologyID(IRI.create("http://purl.obolibrary.org/obo/po.owl"),
                        IRI.create("urn:test:plantontology:version:16.0"));
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                modifiedId, this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        // FIXME: verify version IRIs have been correctly assigned
    }
    
    /**
     * Tests loading a new version of the Science schema ontology and loading a PODD artifact that
     * is only valid against the new version.
     */
    @Test
    public final void testTwoScienceOntologyVersionsOneArtifact() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        // note down details of graph sizes
        final HashMap<String, Long> initialGraphSizes = this.calculateRepositoryGraphSizes();
        
        // load artifact with 2 lead institutions - fails
        try
        {
            this.utils.loadPoddArtifact("/test/artifacts/error-twoLeadInstitutions-1.rdf",
                    RDFFormat.RDFXML.getDefaultMIMEType(), this.getTestRepositoryConnection());
            this.getTestRepositoryConnection().rollback();
            Assert.fail("Did not receive expected exception");
        }
        catch(final PoddException e)
        {
            this.getTestRepositoryConnection().rollback();
            Assert.assertEquals(PoddException.ERR_INCONSISTENT_ONTOLOGY, e.getCode());
            
            // verify Repository is unchanged by checking that graph sizes remain the same
            final HashMap<String, Long> graphSizesAfterFailedAdd = this.calculateRepositoryGraphSizes();
            Assert.assertEquals(initialGraphSizes, graphSizesAfterFailedAdd);
        }
        
        // load new version of poddScience schema ontology which allows 2 lead institutions
        this.utils.loadInferAndStoreSchemaOntology("/test/ontologies/poddScience-2LeadInstitutions.owl",
                RDFFormat.RDFXML.getDefaultMIMEType(), this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        // load artifact with 2 lead institutions - successful
        final InferredOWLOntologyID poddArtifact1 =
                this.utils.loadPoddArtifact("/test/artifacts/error-twoLeadInstitutions-1.rdf",
                        RDFFormat.RDFXML.getDefaultMIMEType(), this.getTestRepositoryConnection());
        
        this.getTestRepositoryConnection().commit();
        
        // assert artifact exists by independently checking it has a graph with statements
        final RepositoryConnection alternateRepoConnection = this.getNewRepositoryConnection();
        Assert.assertTrue(alternateRepoConnection.size(poddArtifact1.getVersionIRI().toOpenRDFURI()) > 0);
        alternateRepoConnection.rollback();
        alternateRepoConnection.close();
        
        // remove artifact from the Manager should return true
        Assert.assertTrue(this.utils.removePoddArtifactFromManager(poddArtifact1));
        
        // a second call to remove artifact from the Manager should return false
        Assert.assertFalse(this.utils.removePoddArtifactFromManager(poddArtifact1));
    }
    
    /**
     * Loads two PODD artifacts that are validated against different versions of the Science schema
     * ontology. Test that the artifacts are modified to import the correct version of the schema
     * ontology.
     * 
     * @throws Exception
     */
    @Test
    public final void testArtifactsImportCorrectSchemaOntologyVersion() throws Exception
    {
        InferredOWLOntologyID poddArtifact1 = null;
        InferredOWLOntologyID poddArtifact2 = null;
        try
        {
            this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                    this.getTestRepositoryConnection());
            
            this.getTestRepositoryConnection().commit();
            
            // load poddScience v1 which allows 1 lead institution
            final InferredOWLOntologyID scienceV1 =
                    this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath,
                            RDFFormat.RDFXML.getDefaultMIMEType(), this.getTestRepositoryConnection());
            
            this.getTestRepositoryConnection().commit();
            
            // load artifact 1
            poddArtifact1 =
                    this.utils.loadPoddArtifact("/test/artifacts/basicProject-1.rdf",
                            RDFFormat.RDFXML.getDefaultMIMEType(), this.getTestRepositoryConnection());
            
            this.getTestRepositoryConnection().commit();
            
            this.log.info("About to load second version of ontology");
            
            // load poddScience v2 which allows 2 lead institutions
            final InferredOWLOntologyID scienceV2 =
                    this.utils.loadInferAndStoreSchemaOntology("/test/ontologies/poddScience-2LeadInstitutions.owl",
                            RDFFormat.RDFXML.getDefaultMIMEType(), this.getTestRepositoryConnection());
            
            this.getTestRepositoryConnection().commit();
            
            this.log.info("About to load second second artifact which would have been inconsistent without the second version");
            
            // load artifact 2 which has 2 lead institutions
            poddArtifact2 =
                    this.utils.loadPoddArtifact("/test/artifacts/error-twoLeadInstitutions-1.rdf",
                            RDFFormat.RDFXML.getDefaultMIMEType(), this.getTestRepositoryConnection());
            
            this.getTestRepositoryConnection().commit();
            
            // =========== verify correct schema versions are imported =================
            final OWLOntology owlScience1 = this.manager.getOntology(scienceV1.getBaseOWLOntologyID());
            final OWLOntology owlScience2 = this.manager.getOntology(scienceV2.getBaseOWLOntologyID());
            
            final Set<OWLOntology> importsClosure1 =
                    this.manager.getOntology(poddArtifact1.getBaseOWLOntologyID()).getImportsClosure();
            Assert.assertTrue("artifact1 did not import old schema version", importsClosure1.contains(owlScience1));
            Assert.assertFalse("artifact1 should not import new schema version", importsClosure1.contains(owlScience2));
            
            final Set<OWLOntology> importsClosure2 =
                    this.manager.getOntology(poddArtifact2.getBaseOWLOntologyID()).getImportsClosure();
            Assert.assertFalse("artifact2 should not import old schema version", importsClosure2.contains(owlScience1));
            Assert.assertTrue("artifact2 did not import new schema ontology", importsClosure2.contains(owlScience2));
            
            // check consistency once again
            this.utils.checkConsistency(owlScience1);
            this.utils.checkConsistency(owlScience2);
            this.utils.checkConsistency(this.manager.getOntology(poddArtifact1.getBaseOWLOntologyID()));
            this.utils.checkConsistency(this.manager.getOntology(poddArtifact2.getBaseOWLOntologyID()));
        }
        finally
        {
            if(poddArtifact1 != null)
            {
                this.utils.removePoddArtifactFromManager(poddArtifact1);
            }
            if(poddArtifact2 != null)
            {
                this.utils.removePoddArtifactFromManager(poddArtifact2);
            }
        }
    }
    
    @Test
    public void testUpdateCurrentManagedPoddArtifactOntologyVersion() throws Exception
    {
        final IRI artifact1 = IRI.create("http://purl.org/abc-def/artifact:1");
        final IRI artifactVersion1 = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
        final IRI inferredArtifactVersion1 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
        
        // first version
        OWLOntologyID nextOntologyID = new OWLOntologyID(artifact1, artifactVersion1);
        OWLOntologyID nextInferredOntologyID =
                new OWLOntologyID(inferredArtifactVersion1,
                        IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1"));
        this.utils.updateCurrentManagedPoddArtifactOntologyVersion(this.getTestRepositoryConnection(), nextOntologyID,
                nextInferredOntologyID);
        
        this.getTestRepositoryConnection().commit();
        
        // verify version statement exists
        final RepositoryResult<Statement> results4InitialVersion =
                this.getTestRepositoryConnection().getStatements(artifact1.toOpenRDFURI(),
                        PoddPrototypeUtils.OMV_CURRENT_VERSION, null, false, this.poddArtifactManagementGraph);
        while(results4InitialVersion.hasNext())
        {
            Assert.assertEquals(artifactVersion1.toString(), results4InitialVersion.next().getObject().toString());
        }
        
        // update to second version
        final IRI artifactVersion2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
        nextOntologyID = new OWLOntologyID(IRI.create("http://purl.org/abc-def/artifact:1"), artifactVersion2);
        nextInferredOntologyID =
                new OWLOntologyID(IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2"),
                        IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2"));
        
        this.utils.updateCurrentManagedPoddArtifactOntologyVersion(this.getTestRepositoryConnection(), nextOntologyID,
                nextInferredOntologyID);
        
        this.getTestRepositoryConnection().commit();
        
        // verify version statement now refers to new version
        final RepositoryResult<Statement> results4UpdatedVersion =
                this.getTestRepositoryConnection().getStatements(artifact1.toOpenRDFURI(),
                        PoddPrototypeUtils.OMV_CURRENT_VERSION, null, false, this.poddArtifactManagementGraph);
        while(results4UpdatedVersion.hasNext())
        {
            Assert.assertEquals(artifactVersion2.toString(), results4UpdatedVersion.next().getObject().toString());
        }
        
        // verify that version1 is no longer referenced
        final RepositoryResult<Statement> results4OldVersionA =
                this.getTestRepositoryConnection().getStatements(artifactVersion1.toOpenRDFURI(), null, null, false,
                        this.poddArtifactManagementGraph);
        if(results4OldVersionA.hasNext())
        {
            Assert.fail("References to old version still exist in Artifact Graph");
        }
        
        final RepositoryResult<Statement> results4OldVersionB =
                this.getTestRepositoryConnection().getStatements(inferredArtifactVersion1.toOpenRDFURI(), null, null,
                        false, this.poddArtifactManagementGraph);
        if(results4OldVersionB.hasNext())
        {
            Assert.fail("References to old version still exist in Artifact Graph");
        }
        
        final RepositoryResult<Statement> results4OldVersionC =
                this.getTestRepositoryConnection().getStatements(inferredArtifactVersion1.toOpenRDFURI(), null, null,
                        false, this.poddArtifactManagementGraph);
        if(results4OldVersionC.hasNext())
        {
            Assert.fail("References to old version still exist in Artifact Graph");
        }
        
    }
    
    public final RepositoryConnection getNewRepositoryConnection() throws Exception
    {
        final RepositoryConnection secondRepositoryConnection = this.getTestRepository().getConnection();
        secondRepositoryConnection.setAutoCommit(false);
        return secondRepositoryConnection;
    }
    
    /**
     * Utility method to calculate sizes of all graphs in Repository
     * 
     * @throws Exception
     */
    protected final HashMap<String, Long> calculateRepositoryGraphSizes() throws Exception
    {
        final HashMap<String, Long> graphInfoMap = new HashMap<String, Long>();
        final RepositoryConnection myRepositoryConnection = this.getTestRepository().getConnection();
        myRepositoryConnection.setAutoCommit(false);
        final RepositoryResult<Resource> contextIDs = myRepositoryConnection.getContextIDs();
        while(contextIDs.hasNext())
        {
            final Resource resource = contextIDs.next();
            final long graphSize = myRepositoryConnection.size(resource);
            graphInfoMap.put(resource.stringValue(), graphSize);
        }
        final long graphSize = myRepositoryConnection.size();
        graphInfoMap.put("Total", graphSize);
        
        myRepositoryConnection.rollback();
        myRepositoryConnection.close();
        
        return graphInfoMap;
    }
    
    // ///////////////////// some helper methods for debugging////////////////////////
    
    private void printSummary(final InferredOWLOntologyID ontoID) throws Exception
    {
        System.out.println("\r\n================");
        System.out.println("  Ontology IRI          : "
                + this.urldecode(ontoID.getBaseOWLOntologyID().getOntologyIRI()));
        System.out
                .println("  Ontology Version IRI  : " + this.urldecode(ontoID.getBaseOWLOntologyID().getVersionIRI()));
        System.out.println("  Inferred Ontology IRI : " + this.urldecode(ontoID.getInferredOntologyIRI()));
        System.out.println("  Imports Closure:- ");
        
        final OWLOntology owlArtifact1 = this.manager.getOntology(ontoID.getBaseOWLOntologyID());
        
        final Set<OWLOntology> ontoSet = this.manager.getImportsClosure(owlArtifact1);
        for(final Object element : ontoSet)
        {
            final OWLOntology importedOntology = (OWLOntology)element;
            System.out.println("   " + this.urldecode(importedOntology.getOntologyID().getOntologyIRI()) + " V>> "
                    + this.urldecode(importedOntology.getOntologyID().getVersionIRI()));
            
        }
        System.out.println("  axiom count = " + owlArtifact1.getAxiomCount());
        
    }
    
    private String urldecode(final Object s) throws Exception
    {
        return java.net.URLDecoder.decode(s.toString(), "UTF-8");
    }
    
    private void printGraph(final URI context)
    {
        System.out.println("===== Print Graph :" + context + "=====");
        org.openrdf.repository.RepositoryResult<Statement> results;
        try
        {
            results = this.getTestRepositoryConnection().getStatements(null, null, null, false, context);
            while(results.hasNext())
            {
                final Statement triple = results.next();
                final String tripleStr = java.net.URLDecoder.decode(triple.toString(), "UTF-8");
                System.out.println(" --- " + tripleStr);
            }
        }
        catch(final Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void printOWLProfileReport(final OWLProfileReport report)
    {
        System.out.println("==== Profile Report ======");
        System.out.println(" Profile:" + report.getProfile());
        System.out.println(" Is in Profile: " + report.isInProfile());
        System.out.println(" No. of violations: " + report.getViolations().size());
        
        for(final OWLProfileViolation violation : report.getViolations())
        {
            System.out.println(violation);
        }
    }
    
    private void countTriplesInRepository(final String msg) throws Exception
    {
        final long size = this.getTestRepositoryConnection().size();
        System.out.println(size + " triples in repository " + msg);
    }
    
}
