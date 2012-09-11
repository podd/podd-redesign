/**
 * 
 */
package com.github.podd.prototype.test;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.RDFInserter;
import org.semanticweb.owlapi.formats.RDFXMLOntologyFormatFactory;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileRegistry;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.semanticweb.owlapi.rio.RioRenderer;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;

import com.clarkparsia.owlapiv3.OWL;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddPrototypeSkeletonTest extends AbstractSesameTest
{
    
    private OWLOntologyManager manager;
    private OWLClass phylomeStomatalComplex;
    private OWLClass bractStomatalComplex;
    private OWLClass plantAnatomicalEntity;
    private OWLClass phylome;
    private OWLOntology testOntology;
    private URI testContextUri;
    private URI testInferredContextUri;
    private OWLReasoner reasoner;
    private OWLReasonerFactory reasonerFactory;
    private String reasonerName;
    private OWLOntology inferredAxiomsOntology;
    
    protected OWLReasoner checkConsistency(final OWLOntology nextOntology, final IRI nextProfileIRI) throws Exception
    {
        final OWLProfile nextProfile = OWLProfileRegistry.getInstance().getProfile(nextProfileIRI);
        Assert.assertNotNull("Could not find profile in registry", nextProfile);
        final OWLProfileReport profileReport = nextProfile.checkOntology(nextOntology);
        Assert.assertTrue("Schema Ontology was not in the given profile", profileReport.isInProfile());
        
        // create an OWL Reasoner using the Pellet library and ensure that the reasoner thinks the
        // ontology is consistent so far
        // Use the factory that we found to create a reasoner over the ontology
        final OWLReasoner nextReasoner = this.reasonerFactory.createReasoner(nextOntology);
        
        // Test that the ontology was consistent with this reasoner
        // This ensures in the case of Pellet that it is in the OWL2-DL profile
        Assert.assertTrue("Ontology was not consistent", nextReasoner.isConsistent());
        
        return nextReasoner;
    }
    
    /**
     * @throws ReasonerInterruptedException
     * @throws TimeOutException
     * @throws InconsistentOntologyException
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyChangeException
     */
    protected OWLOntology computeInferences(final OWLReasoner nextReasoner, final IRI inferredOntologyUri,
            final IRI inferredOntologyVersionUri) throws ReasonerInterruptedException, TimeOutException,
        InconsistentOntologyException, OWLOntologyCreationException, OWLOntologyChangeException
    {
        nextReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        final InferredOntologyGenerator iog = new InferredOntologyGenerator(nextReasoner);
        final OWLOntology nextInferredAxiomsOntology =
                this.manager.createOntology(new OWLOntologyID(inferredOntologyUri, inferredOntologyVersionUri));
        iog.fillOntology(this.manager, nextInferredAxiomsOntology);
        
        return nextInferredAxiomsOntology;
    }
    
    /**
     * @param nextContextUri
     *            The context URI to dump the ontology triples into.
     * @param nextOntology
     *            The ontology to dump into the repository.
     * @param nextRepositoryConnection
     *            The repository connection to dump the triples into.
     * @throws IOException
     * @throws RepositoryException
     */
    protected void dumpOntologyToRepository(final URI nextContextUri, final OWLOntology nextOntology,
            final RepositoryConnection nextRepositoryConnection) throws IOException, RepositoryException
    {
        // Create an RDFHandler that will insert all triples after they are emitted from OWLAPI into
        // a single context in the Sesame Repository
        final RDFInserter repositoryHandler = new RDFInserter(nextRepositoryConnection);
        repositoryHandler.enforceContext(nextContextUri);
        
        // Render the triples out from OWLAPI into a Sesame Repository
        final RioRenderer renderer =
                new RioRenderer(nextOntology, this.manager, repositoryHandler, null, nextContextUri);
        renderer.render();
        
        // Commit the current repository connection
        nextRepositoryConnection.commit();
    }
    
    protected OWLOntology loadOntology(final String ontologyResource) throws Exception
    {
        final OWLOntology nextOntology =
                this.manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(this.getClass()
                        .getResourceAsStream(ontologyResource), new RDFXMLOntologyFormatFactory()));
        Assert.assertFalse(nextOntology.isEmpty());
        
        return nextOntology;
    }
    
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        
        // create the manager to use for the test
        this.manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        
        // TODO: Pellet should be configurable
        this.reasonerName = "Pellet";
        this.reasonerFactory = OWLReasonerFactoryRegistry.getInstance().getReasonerFactory(this.reasonerName);
        Assert.assertNotNull("Could not find reasoner", this.reasonerFactory);
    }
    
    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        
        this.manager = null;
        if(this.reasoner != null)
        {
            this.reasoner.dispose();
            this.reasoner = null;
        }
    }
    
    @Test
    public final void testBaseOntology() throws Exception
    {
        this.testOntology = this.loadOntology("/ontologies/poddBase.owl");
        this.reasoner = this.checkConsistency(this.testOntology, OWLProfile.OWL2_DL);
    }
    
    @Test
    public final void testPlantOntology() throws Exception
    {
        // create owl class objects to use in queries
        this.phylomeStomatalComplex = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025215"));
        this.bractStomatalComplex = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025216"));
        this.plantAnatomicalEntity = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0025131"));
        this.phylome = OWL.Class(IRI.create("http://purl.obolibrary.org/obo/PO_0006001"));
        
        // create URI references for the SPARQL Graph URIs for the ontology and the inferred
        // ontology statements respectively
        this.testContextUri = this.getTestValueFactory().createURI("urn:test:plantontology:context");
        this.testInferredContextUri = this.getTestValueFactory().createURI("urn:test:plantontology:inferred:context");
        
        this.testOntology = this.loadOntology("/test/ontologies/plant_ontology-v16.owl");
        
        // fix up the ontology id that was parsed using the values that we wish to use here
        final OWLOntologyID fullId =
                new OWLOntologyID(this.testOntology.getOntologyID().getOntologyIRI(),
                        IRI.create("urn:test:plantontology:version:16.0"));
        this.manager.applyChange(new SetOntologyID(this.testOntology, fullId));
        Assert.assertEquals(fullId, this.testOntology.getOntologyID());
        
        this.dumpOntologyToRepository(this.testContextUri, this.testOntology, this.getTestRepositoryConnection());
        
        // Then test to ensure that the expected number of triples were added,
        // and ensure that no other triples are in the repository at this point
        Assert.assertEquals(44333, this.getTestRepositoryConnection().size(this.testContextUri));
        Assert.assertEquals(44333, this.getTestRepositoryConnection().size());
        
        // create an OWL Reasoner using the Pellet library and ensure that the reasoner thinks the
        // ontology is consistent so far
        this.reasoner = this.checkConsistency(this.testOntology, OWLProfile.OWL2_DL);
        
        // use the reasoner to infer concrete axioms and deposit them in a new ontology
        this.inferredAxiomsOntology =
                this.computeInferences(this.reasoner, IRI.create(this.testInferredContextUri),
                        IRI.create("urn:test:inferredontology:plantontology:version:16.0"));
        
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.dumpOntologyToRepository(this.testInferredContextUri, this.inferredAxiomsOntology,
                this.getTestRepositoryConnection());
        
        // verify that the triples were inserted into the repository correctly
        Assert.assertEquals(2995, this.getTestRepositoryConnection().size(this.testInferredContextUri));
        Assert.assertEquals(44333, this.getTestRepositoryConnection().size(this.testContextUri));
        Assert.assertEquals(47328, this.getTestRepositoryConnection().size());
        
        if(this.log.isTraceEnabled())
        {
            for(final Statement nextStatement : this.getTestRepositoryConnection()
                    .getStatements(null, null, null, true, this.testInferredContextUri).asList())
            {
                this.log.trace(nextStatement.toString());
            }
        }
        
        // TODO:
        
        final OWLOntology updatedSchemaOntology =
                this.manager.createOntology(new OWLOntologyID(this.testOntology.getOntologyID().getOntologyIRI(), IRI
                        .create("urn:test:plantontology:version:16.1")));
        this.manager.addAxioms(updatedSchemaOntology, this.testOntology.getAxioms());
        
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
    
}
