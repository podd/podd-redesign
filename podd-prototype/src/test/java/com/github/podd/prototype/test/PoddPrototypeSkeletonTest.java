/**
 * 
 */
package com.github.podd.prototype.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

import com.clarkparsia.owlapiv3.OWL;
import com.github.podd.prototype.PoddPrototypeUtils;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddPrototypeSkeletonTest extends AbstractSesameTest
{
    
    private OWLOntologyManager manager;
    
    private OWLReasonerFactory reasonerFactory;
    
    private String reasonerName;
    
    private PoddPrototypeUtils utils;
    
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
        
        this.utils = new PoddPrototypeUtils();
        
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
     * Tests the combination of the base, science, and the podd animal ontologies to verify their
     * internal consistency.
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseAndScienceAndPoddAnimalOntologies() throws Exception
    {
        final URI testBaseContextUri = this.getTestValueFactory().createURI("urn:test:poddBase:context");
        final URI testBaseInferredContextUri =
                this.getTestValueFactory().createURI("urn:test:poddBase:inferred:context");
        final IRI testBaseInferredOntologyVersionUri = IRI.create("urn:test:poddBase:inferred:axioms:version-0.0.1");
        
        final OWLOntology testBaseOntology = this.utils.loadOntology("/ontologies/poddBase.owl", this.manager);
        final OWLReasoner reasoner =
                this.utils.checkConsistency(testBaseOntology, OWLProfile.OWL2_DL, this.reasonerFactory);
        this.utils.dumpOntologyToRepository(testBaseContextUri, testBaseOntology, this.getTestRepositoryConnection(),
                this.manager);
        final OWLOntology testBaseInferredOntology =
                this.utils.computeInferences(reasoner, IRI.create(testBaseInferredContextUri),
                        testBaseInferredOntologyVersionUri, this.manager);
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.utils.dumpOntologyToRepository(testBaseInferredContextUri, testBaseInferredOntology,
                this.getTestRepositoryConnection(), this.manager);
        
        final URI testScienceContextUri = this.getTestValueFactory().createURI("urn:test:poddScience:context");
        final URI testScienceInferredContextUri =
                this.getTestValueFactory().createURI("urn:test:poddScience:inferred:context");
        final IRI testScienceInferredOntologyVersionUri =
                IRI.create("urn:test:poddScience:inferred:axioms:version-0.0.1");
        
        this.log.info("About to load science ontology");
        final OWLOntology testScienceOntology = this.utils.loadOntology("/ontologies/poddScience.owl", this.manager);
        this.log.info("Loaded science ontology");
        final OWLReasoner scienceOntologyReasoner =
                this.utils.checkConsistency(testScienceOntology, OWLProfile.OWL2_DL, this.reasonerFactory);
        this.log.info("Completed reasoning consistency for science ontology");
        this.utils.dumpOntologyToRepository(testScienceContextUri, testScienceOntology,
                this.getTestRepositoryConnection(), this.manager);
        
        final OWLOntology testScienceInferredOntology =
                this.utils.computeInferences(scienceOntologyReasoner, IRI.create(testScienceInferredContextUri),
                        testScienceInferredOntologyVersionUri, this.manager);
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.utils.dumpOntologyToRepository(testScienceInferredContextUri, testScienceInferredOntology,
                this.getTestRepositoryConnection(), this.manager);
        
        final URI testPoddAnimalContextUri = this.getTestValueFactory().createURI("urn:test:poddAnimal:context");
        final URI testPoddAnimalInferredContextUri =
                this.getTestValueFactory().createURI("urn:test:poddAnimal:inferred:context");
        final IRI testPoddAnimalInferredOntologyVersionUri =
                IRI.create("urn:test:poddAnimal:inferred:axioms:version-0.0.1");
        
        this.log.info("About to load PODD animal ontology");
        final OWLOntology testPoddAnimalOntology = this.utils.loadOntology("/ontologies/poddAnimal.owl", this.manager);
        this.log.info("Loaded PODD animal ontology");
        final OWLReasoner poddAnimalOntologyReasoner =
                this.utils.checkConsistency(testPoddAnimalOntology, OWLProfile.OWL2_DL, this.reasonerFactory);
        this.log.info("Completed reasoning consistency for PODD animal ontology");
        this.utils.dumpOntologyToRepository(testPoddAnimalContextUri, testPoddAnimalOntology,
                this.getTestRepositoryConnection(), this.manager);
        
        final OWLOntology testPoddAnimalInferredOntology =
                this.utils.computeInferences(poddAnimalOntologyReasoner, IRI.create(testPoddAnimalInferredContextUri),
                        testPoddAnimalInferredOntologyVersionUri, this.manager);
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.utils.dumpOntologyToRepository(testPoddAnimalInferredContextUri, testPoddAnimalInferredOntology,
                this.getTestRepositoryConnection(), this.manager);
        
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
        final URI testBaseContextUri = this.getTestValueFactory().createURI("urn:test:poddBase:context");
        final URI testBaseInferredContextUri =
                this.getTestValueFactory().createURI("urn:test:poddBase:inferred:context");
        final IRI testBaseInferredOntologyVersionUri = IRI.create("urn:test:poddBase:inferred:axioms:version-0.0.1");
        
        final OWLOntology testBaseOntology = this.utils.loadOntology("/ontologies/poddBase.owl", this.manager);
        final OWLReasoner reasoner =
                this.utils.checkConsistency(testBaseOntology, OWLProfile.OWL2_DL, this.reasonerFactory);
        this.utils.dumpOntologyToRepository(testBaseContextUri, testBaseOntology, this.getTestRepositoryConnection(),
                this.manager);
        final OWLOntology testBaseInferredOntology =
                this.utils.computeInferences(reasoner, IRI.create(testBaseInferredContextUri),
                        testBaseInferredOntologyVersionUri, this.manager);
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.utils.dumpOntologyToRepository(testBaseInferredContextUri, testBaseInferredOntology,
                this.getTestRepositoryConnection(), this.manager);
        
        final URI testScienceContextUri = this.getTestValueFactory().createURI("urn:test:poddScience:context");
        final URI testScienceInferredContextUri =
                this.getTestValueFactory().createURI("urn:test:poddScience:inferred:context");
        final IRI testScienceInferredOntologyVersionUri =
                IRI.create("urn:test:poddScience:inferred:axioms:version-0.0.1");
        
        this.log.info("About to load science ontology");
        final OWLOntology testScienceOntology = this.utils.loadOntology("/ontologies/poddScience.owl", this.manager);
        this.log.info("Loaded science ontology");
        final OWLReasoner scienceOntologyReasoner =
                this.utils.checkConsistency(testScienceOntology, OWLProfile.OWL2_DL, this.reasonerFactory);
        this.log.info("Completed reasoning consistency for science ontology");
        this.utils.dumpOntologyToRepository(testScienceContextUri, testScienceOntology,
                this.getTestRepositoryConnection(), this.manager);
        
        final OWLOntology testScienceInferredOntology =
                this.utils.computeInferences(scienceOntologyReasoner, IRI.create(testScienceInferredContextUri),
                        testScienceInferredOntologyVersionUri, this.manager);
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.utils.dumpOntologyToRepository(testScienceInferredContextUri, testScienceInferredOntology,
                this.getTestRepositoryConnection(), this.manager);
        
        final URI testPoddPlantContextUri = this.getTestValueFactory().createURI("urn:test:poddPlant:context");
        final URI testPoddPlantInferredContextUri =
                this.getTestValueFactory().createURI("urn:test:poddPlant:inferred:context");
        final IRI testPoddPlantInferredOntologyVersionUri =
                IRI.create("urn:test:poddPlant:inferred:axioms:version-0.0.1");
        
        this.log.info("About to load PODD plant ontology");
        final OWLOntology testPoddPlantOntology = this.utils.loadOntology("/ontologies/poddPlant.owl", this.manager);
        this.log.info("Loaded PODD plant ontology");
        final OWLReasoner poddPlantOntologyReasoner =
                this.utils.checkConsistency(testPoddPlantOntology, OWLProfile.OWL2_DL, this.reasonerFactory);
        this.log.info("Completed reasoning consistency for PODD plant ontology");
        this.utils.dumpOntologyToRepository(testPoddPlantContextUri, testPoddPlantOntology,
                this.getTestRepositoryConnection(), this.manager);
        
        final OWLOntology testPoddPlantInferredOntology =
                this.utils.computeInferences(poddPlantOntologyReasoner, IRI.create(testPoddPlantInferredContextUri),
                        testPoddPlantInferredOntologyVersionUri, this.manager);
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.utils.dumpOntologyToRepository(testPoddPlantInferredContextUri, testPoddPlantInferredOntology,
                this.getTestRepositoryConnection(), this.manager);
        
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
        final URI testBaseContextUri = this.getTestValueFactory().createURI("urn:test:poddBase:context");
        final URI testBaseInferredContextUri =
                this.getTestValueFactory().createURI("urn:test:poddBase:inferred:context");
        final IRI testBaseInferredOntologyVersionUri = IRI.create("urn:test:poddBase:inferred:axioms:version-0.0.1");
        
        final OWLOntology testBaseOntology = this.utils.loadOntology("/ontologies/poddBase.owl", this.manager);
        final OWLReasoner reasoner =
                this.utils.checkConsistency(testBaseOntology, OWLProfile.OWL2_DL, this.reasonerFactory);
        this.utils.dumpOntologyToRepository(testBaseContextUri, testBaseOntology, this.getTestRepositoryConnection(),
                this.manager);
        final OWLOntology testBaseInferredOntology =
                this.utils.computeInferences(reasoner, IRI.create(testBaseInferredContextUri),
                        testBaseInferredOntologyVersionUri, this.manager);
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.utils.dumpOntologyToRepository(testBaseInferredContextUri, testBaseInferredOntology,
                this.getTestRepositoryConnection(), this.manager);
        
        final URI testScienceContextUri = this.getTestValueFactory().createURI("urn:test:poddScience:context");
        final URI testScienceInferredContextUri =
                this.getTestValueFactory().createURI("urn:test:poddScience:inferred:context");
        final IRI testScienceInferredOntologyVersionUri =
                IRI.create("urn:test:poddScience:inferred:axioms:version-0.0.1");
        
        this.log.info("About to load science ontology");
        final OWLOntology testScienceOntology = this.utils.loadOntology("/ontologies/poddScience.owl", this.manager);
        this.log.info("Loaded science ontology");
        final OWLReasoner scienceOntologyReasoner =
                this.utils.checkConsistency(testScienceOntology, OWLProfile.OWL2_DL, this.reasonerFactory);
        this.log.info("Completed reasoning consistency for science ontology");
        this.utils.dumpOntologyToRepository(testScienceContextUri, testScienceOntology,
                this.getTestRepositoryConnection(), this.manager);
        
        final OWLOntology testScienceInferredOntology =
                this.utils.computeInferences(scienceOntologyReasoner, IRI.create(testScienceInferredContextUri),
                        testScienceInferredOntologyVersionUri, this.manager);
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.utils.dumpOntologyToRepository(testScienceInferredContextUri, testScienceInferredOntology,
                this.getTestRepositoryConnection(), this.manager);
        
    }
    
    /**
     * Tests the combination of the base and user ontologies to verify their internal consistency.
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseAndUserOntologies() throws Exception
    {
        final URI testBaseContextUri = this.getTestValueFactory().createURI("urn:test:poddBase:context");
        final URI testBaseInferredContextUri =
                this.getTestValueFactory().createURI("urn:test:poddBase:inferred:context");
        final IRI testBaseInferredOntologyVersionUri = IRI.create("urn:test:poddBase:inferred:axioms:version-0.0.1");
        
        final OWLOntology testBaseOntology = this.utils.loadOntology("/ontologies/poddBase.owl", this.manager);
        final OWLReasoner reasoner =
                this.utils.checkConsistency(testBaseOntology, OWLProfile.OWL2_DL, this.reasonerFactory);
        this.utils.dumpOntologyToRepository(testBaseContextUri, testBaseOntology, this.getTestRepositoryConnection(),
                this.manager);
        final OWLOntology testBaseInferredOntology =
                this.utils.computeInferences(reasoner, IRI.create(testBaseInferredContextUri),
                        testBaseInferredOntologyVersionUri, this.manager);
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.utils.dumpOntologyToRepository(testBaseInferredContextUri, testBaseInferredOntology,
                this.getTestRepositoryConnection(), this.manager);
        
        final URI testUserContextUri = this.getTestValueFactory().createURI("urn:test:poddUser:context");
        final URI testUserInferredContextUri =
                this.getTestValueFactory().createURI("urn:test:poddUser:inferred:context");
        final IRI testUserInferredOntologyVersionUri = IRI.create("urn:test:poddUser:inferred:axioms:version-0.0.1");
        
        this.log.info("About to load user ontology");
        final OWLOntology testUserOntology = this.utils.loadOntology("/ontologies/poddUser.owl", this.manager);
        this.log.info("Loaded user ontology");
        final OWLReasoner userOntologyReasoner =
                this.utils.checkConsistency(testUserOntology, OWLProfile.OWL2_DL, this.reasonerFactory);
        this.log.info("Completed reasoning consistency for user ontology");
        this.utils.dumpOntologyToRepository(testUserContextUri, testUserOntology, this.getTestRepositoryConnection(),
                this.manager);
        
        final OWLOntology testUserInferredOntology =
                this.utils.computeInferences(userOntologyReasoner, IRI.create(testUserInferredContextUri),
                        testUserInferredOntologyVersionUri, this.manager);
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.utils.dumpOntologyToRepository(testUserInferredContextUri, testUserInferredOntology,
                this.getTestRepositoryConnection(), this.manager);
        
    }
    
    /**
     * Tests the base ontology to verify its internal consistency.
     * 
     * @throws Exception
     */
    @Test
    public final void testBaseOntology() throws Exception
    {
        final URI testContextUri = this.getTestValueFactory().createURI("urn:test:poddBase:context");
        final URI testInferredContextUri = this.getTestValueFactory().createURI("urn:test:poddBase:inferred:context");
        final IRI testInferredOntologyVersionUri = IRI.create("urn:test:poddBase:inferred:axioms:version-0.0.1");
        
        final OWLOntology testBaseOntology = this.utils.loadOntology("/ontologies/poddBase.owl", this.manager);
        final OWLReasoner reasoner =
                this.utils.checkConsistency(testBaseOntology, OWLProfile.OWL2_DL, this.reasonerFactory);
        this.utils.dumpOntologyToRepository(testContextUri, testBaseOntology, this.getTestRepositoryConnection(),
                this.manager);
        final OWLOntology testBaseInferredOntology =
                this.utils.computeInferences(reasoner, IRI.create(testInferredContextUri),
                        testInferredOntologyVersionUri, this.manager);
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.utils.dumpOntologyToRepository(testInferredContextUri, testBaseInferredOntology,
                this.getTestRepositoryConnection(), this.manager);
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
        
        // create URI references for the SPARQL Graph URIs for the ontology and the inferred
        // ontology statements respectively
        final URI testContextUri = this.getTestValueFactory().createURI("urn:test:plantontology:context");
        final URI testInferredContextUri =
                this.getTestValueFactory().createURI("urn:test:plantontology:inferred:context");
        
        final OWLOntology testOntology =
                this.utils.loadOntology("/ontologies/plant_ontology-v16.owl", this.manager);
        
        // fix up the ontology id that was parsed using the values that we wish to use here
        final OWLOntologyID fullId =
                new OWLOntologyID(testOntology.getOntologyID().getOntologyIRI(),
                        IRI.create("urn:test:plantontology:version:16.0"));
        this.manager.applyChange(new SetOntologyID(testOntology, fullId));
        Assert.assertEquals(fullId, testOntology.getOntologyID());
        
        this.utils.dumpOntologyToRepository(testContextUri, testOntology, this.getTestRepositoryConnection(),
                this.manager);
        
        // Then test to ensure that the expected number of triples were added,
        // and ensure that no other triples are in the repository at this point
        Assert.assertEquals(44333, this.getTestRepositoryConnection().size(testContextUri));
        Assert.assertEquals(44333, this.getTestRepositoryConnection().size());
        
        // create an OWL Reasoner using the Pellet library and ensure that the reasoner thinks the
        // ontology is consistent so far
        final OWLReasoner reasoner =
                this.utils.checkConsistency(testOntology, OWLProfile.OWL2_DL, this.reasonerFactory);
        
        // use the reasoner to infer concrete axioms and deposit them in a new ontology
        final OWLOntology inferredAxiomsOntology =
                this.utils.computeInferences(reasoner, IRI.create(testInferredContextUri),
                        IRI.create("urn:test:inferredontology:plantontology:version:16.0"), this.manager);
        
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.utils.dumpOntologyToRepository(testInferredContextUri, inferredAxiomsOntology,
                this.getTestRepositoryConnection(), this.manager);
        
        // verify that the triples were inserted into the repository correctly
        Assert.assertEquals(2995, this.getTestRepositoryConnection().size(testInferredContextUri));
        Assert.assertEquals(44333, this.getTestRepositoryConnection().size(testContextUri));
        Assert.assertEquals(47328, this.getTestRepositoryConnection().size());
        
        if(this.log.isTraceEnabled())
        {
            for(final Statement nextStatement : this.getTestRepositoryConnection()
                    .getStatements(null, null, null, true, testInferredContextUri).asList())
            {
                this.log.trace(nextStatement.toString());
            }
        }
        
        // TODO:
        
        final OWLOntology updatedSchemaOntology =
                this.manager.createOntology(new OWLOntologyID(testOntology.getOntologyID().getOntologyIRI(), IRI
                        .create("urn:test:plantontology:version:16.1")));
        this.manager.addAxioms(updatedSchemaOntology, testOntology.getAxioms());
        
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
    
    @Test
    public final void testUserOntology() throws Exception
    {
        final URI testContextUri = this.getTestValueFactory().createURI("urn:test:poddUser:context");
        final URI testInferredContextUri = this.getTestValueFactory().createURI("urn:test:poddUser:inferred:context");
        final IRI testInferredOntologyVersionUri = IRI.create("urn:test:poddUser:inferred:axioms:version-0.0.1");
        
        this.log.info("About to load base ontology");
        final OWLOntology testBaseOntology = this.utils.loadOntology("/ontologies/poddBase.owl", this.manager);
        this.log.info("About to load user ontology");
        final OWLOntology testUserOntology = this.utils.loadOntology("/ontologies/poddUser.owl", this.manager);
        this.log.info("Loaded user ontology");
        final OWLReasoner userOntologyReasoner =
                this.utils.checkConsistency(testUserOntology, OWLProfile.OWL2_DL, this.reasonerFactory);
        this.log.info("Completed reasoning consistency for user ontology");
        this.utils.dumpOntologyToRepository(testContextUri, testUserOntology, this.getTestRepositoryConnection(),
                this.manager);
        
        final OWLOntology testUserInferredOntology =
                this.utils.computeInferences(userOntologyReasoner, IRI.create(testInferredContextUri),
                        testInferredOntologyVersionUri, this.manager);
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.utils.dumpOntologyToRepository(testInferredContextUri, testUserInferredOntology,
                this.getTestRepositoryConnection(), this.manager);
        
    }
    
}
