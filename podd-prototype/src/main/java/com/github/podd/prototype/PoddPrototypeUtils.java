/**
 * 
 */
package com.github.podd.prototype;

import java.io.IOException;

import org.junit.Assert;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.RDFInserter;
import org.semanticweb.owlapi.formats.RDFXMLOntologyFormatFactory;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileRegistry;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
import org.semanticweb.owlapi.rio.RioRenderer;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A selection of utilities used to create the prototype.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddPrototypeUtils
{
    /**
     * This URI is not currently in the Sesame OWL namespace, so we create a constant here as it is
     * vital to our strategy.
     */
    public static final URI OWL_VERSION_IRI = ValueFactoryImpl.getInstance().createURI(OWL.NAMESPACE, "versionIRI");
    
    /**
     * The OMV vocabulary defines a property for the current version of an ontology, so we are
     * reusing it here.
     */
    public static final URI OMV_CURRENT_VERSION = ValueFactoryImpl.getInstance().createURI(
            "http://omv.ontoware.org/ontology#", "currentVersion");
    
    /**
     * Creating a property for PODD to track the currentInferredVersion for the inferred axioms
     * ontology when linking from the ontology IRI.
     */
    public static final URI PODD_BASE_CURRENT_INFERRED_VERSION = ValueFactoryImpl.getInstance().createURI(
            "http://purl.org/podd/ns/poddBase#", "currentInferredVersion");
    
    /**
     * Creating a property for PODD to track the inferredVersion for the inferred axioms ontology of
     * a particular versioned ontology.
     */
    public static final URI PODD_BASE_INFERRED_VERSION = ValueFactoryImpl.getInstance().createURI(
            "http://purl.org/podd/ns/poddBase#", "inferredVersion");
    
    /**
     * An arbitrary prefix to use for automatically assigning ontology IRIs to inferred ontologies.
     * There are no versions delegated to inferred ontologies, and the ontology IRI is generated
     * using the version IRI of the original ontology, which must be unique.
     */
    private static final String INFERRED_PREFIX = "urn:podd:inferred:ontologyiriprefix:";
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * The manager that will be used to manage the Schema Ontologies.
     * 
     * TODO: Decide how to manage PODD Artifact ontologies that will typically have smaller
     * lifetimes than Schema Ontologies which will stay in the manager for the lifetime of the
     * manager, until the next shutdown or maintenance period when they may be changed. PODD
     * Artifact ontologies may import Schema Ontologies so their manager needs to be able to access
     * the list of Schema Ontologies. However, PODD Artifact ontologies must not stay in the manager
     * for long periods of time or they will cause unsustainable memory growth, particularly for
     * large ontologies with many versions.
     * 
     * TODO: How does an edit conflict resolution strategy work in relation to this manager.
     * Ideally, we should also only be storing the latest version of a PODD Artifact in the manager.
     * If we are performing edit conflict resolution based solely on RDF triples and not OWL Axioms,
     * then we may be able to perform the diff externally. See the Protege OWL Diff code for
     * examples of how to diff ontologies.
     */
    private OWLOntologyManager manager;
    
    /**
     * A factory for producing reasoners that are suitable according to owlProfile.
     */
    private OWLReasonerFactory reasonerFactory;
    
    /**
     * The graph used to manage Schema Ontologies. This is distinct from the graph used to manage
     * PODD Artifacts.
     */
    private URI schemaGraph;
    
    /**
     * The graph used to manage PODD Artifacts. This is distinct from the graph used to manage
     * Schema Ontologies.
     */
    private URI artifactGraph;
    
    /**
     * The OWLProfile that matches the reasoners produced by the reasonerFactory. This enables a
     * faster pre-reasoner check to very that the ontology is in the profile before attempting to
     * reason over the entire ontology.
     */
    private IRI owlProfile;
    
    /**
     * 
     * @param nextManager
     *            The OWLOntologyManager instance that will be used to store ontologies in memory.
     * @param nextOwlProfile
     *            The IRI of the OWL Profile that matches the reasoner factory, and will be used to
     *            check for basic consistency before using the reasoner.
     * @param nextReasonerFactory
     *            The reasoner factory that will be used to create reasoners for consistency checks
     *            and for inferring extra triples.
     * @param nextSchemaGraph
     *            The Graph URI that will be used for storing the schema ontology management
     *            statements.
     * @param nextPoddArtifactGraph
     *            TODO
     * 
     */
    public PoddPrototypeUtils(final OWLOntologyManager nextManager, final IRI nextOwlProfile,
            final OWLReasonerFactory nextReasonerFactory, final URI nextSchemaGraph, final URI nextPoddArtifactGraph)
    {
        this.manager = nextManager;
        this.owlProfile = nextOwlProfile;
        this.reasonerFactory = nextReasonerFactory;
        this.schemaGraph = nextSchemaGraph;
        this.artifactGraph = nextPoddArtifactGraph;
    }
    
    /**
     * Checks the consistency of the ontology and returns the instance of OWLReasoner that was used
     * to check the consistency.
     * 
     * @param nextOntology
     *            The ontology to check for consistency.
     * @return An instance of OWLreasoner that was used to check the consistency.s
     * @throws Exception
     */
    public OWLReasoner checkConsistency(final OWLOntology nextOntology) throws Exception
    {
        final OWLProfile nextProfile = OWLProfileRegistry.getInstance().getProfile(this.owlProfile);
        Assert.assertNotNull("Could not find profile in registry: " + this.owlProfile.toQuotedString(), nextProfile);
        final OWLProfileReport profileReport = nextProfile.checkOntology(nextOntology);
        if(!profileReport.isInProfile())
        {
            this.log.error("Bad profile report count: {}", profileReport.getViolations().size());
            this.log.error("Bad profile report: {}", profileReport);
        }
        Assert.assertTrue("Schema Ontology was not in the given profile: " + nextOntology.getOntologyID().toString(),
                profileReport.isInProfile());
        
        // create an OWL Reasoner using the Pellet library and ensure that the reasoner thinks the
        // ontology is consistent so far
        // Use the factory that we found to create a reasoner over the ontology
        final OWLReasoner nextReasoner = this.reasonerFactory.createReasoner(nextOntology);
        
        // Test that the ontology was consistent with this reasoner
        // This ensures in the case of Pellet that it is in the OWL2-DL profile
        Assert.assertTrue("Ontology was not consistent: " + nextOntology.getOntologyID().toString(),
                nextReasoner.isConsistent());
        
        return nextReasoner;
    }
    
    /**
     * Computes the inferences using the given reasoner, which has previously been setup based on an
     * ontology.
     * 
     * @param nextReasoner
     *            The reasoner to use to compute the inferences.
     * @param inferredOntologyID
     *            The OWLOntologyID to use for the inferred ontology. This must be unique and not
     *            previously used in either the repository or the OWLOntologyManager
     * @return An OWLOntology instance containing the axioms that were inferred from the original
     *         ontology.
     * @throws ReasonerInterruptedException
     * @throws TimeOutException
     * @throws InconsistentOntologyException
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyChangeException
     */
    public OWLOntology computeInferences(final OWLReasoner nextReasoner, final OWLOntologyID inferredOntologyID)
        throws ReasonerInterruptedException, TimeOutException, InconsistentOntologyException,
        OWLOntologyCreationException, OWLOntologyChangeException
    {
        nextReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        final InferredOntologyGenerator iog = new InferredOntologyGenerator(nextReasoner);
        final OWLOntology nextInferredAxiomsOntology = this.manager.createOntology(inferredOntologyID);
        iog.fillOntology(this.manager, nextInferredAxiomsOntology);
        
        return nextInferredAxiomsOntology;
    }
    
    /**
     * Dump the triples representing a given ontology into a Sesame Repository.
     * 
     * @param nextOntology
     *            The ontology to dump into the repository.
     * @param nextRepositoryConnection
     *            The repository connection to dump the triples into.
     * @throws IOException
     * @throws RepositoryException
     */
    public void dumpOntologyToRepository(final OWLOntology nextOntology,
            final RepositoryConnection nextRepositoryConnection) throws IOException, RepositoryException
    {
        try
        {
            // Create an RDFHandler that will insert all triples after they are emitted from OWLAPI
            // into a single context in the Sesame Repository
            final RDFInserter repositoryHandler = new RDFInserter(nextRepositoryConnection);
            repositoryHandler.enforceContext(nextOntology.getOntologyID().getVersionIRI().toOpenRDFURI());
            
            // Render the triples out from OWLAPI into a Sesame Repository
            final RioRenderer renderer =
                    new RioRenderer(nextOntology, this.manager, repositoryHandler, null, nextOntology.getOntologyID()
                            .getVersionIRI().toOpenRDFURI());
            renderer.render();
            
            // Commit the current repository connection
            nextRepositoryConnection.commit();
        }
        catch(final Exception e)
        {
            // if anything failed, rollback the connection before rethrowing the exception
            nextRepositoryConnection.rollback();
            throw e;
        }
    }
    
    /**
     * Generates a unique inferred ontology ID based on the original ontology ID version IRI.
     * 
     * Both ontology IRI and version IRI for the resulting ontology ID are the same to ensure
     * consistency.
     * 
     * @param originalOntologyID
     *            The original ontology ID to use for naming the inferred ontology.
     * @return An instance of OWLOntologyID that can be used to name an inferred ontology.
     */
    public OWLOntologyID generateInferredOntologyID(final OWLOntologyID originalOntologyID)
    {
        return new OWLOntologyID(IRI.create(PoddPrototypeUtils.INFERRED_PREFIX + originalOntologyID.getVersionIRI()),
                IRI.create(PoddPrototypeUtils.INFERRED_PREFIX + originalOntologyID.getVersionIRI()));
    }
    
    /**
     * Loads an ontology from a classpath resource, renames the ontology using the given
     * OWLOntologyID, checks the consistency of the ontology, infers statements from the ontology,
     * and stores the inferred statements.
     * 
     * <br />
     * 
     * The given OWLOntologyID will be assigned to the ontology after it is loaded.
     * 
     * <br />
     * 
     * IMPORTANT: The inferred ontology has an ontology IRI that is derived from the version IRI of
     * the loaded ontology. The version IRI in the given OWLOntologyID must be unique for this
     * process to succeed.
     * 
     * @param ontologyResourcePath
     *            The classpath resource to load the ontology from.
     * @param nextRepositoryConnection
     *            The repository connection to use for storing the ontology and the inferred
     *            statements.
     * @throws Exception
     * @throws IOException
     * @throws RepositoryException
     * @throws ReasonerInterruptedException
     * @throws TimeOutException
     * @throws InconsistentOntologyException
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyChangeException
     */
    public InferredOWLOntologyID loadInferAndStoreSchemaOntology(final String ontologyResourcePath,
            final OWLOntologyID newOWLOntologyID, final RepositoryConnection nextRepositoryConnection)
        throws Exception, IOException, RepositoryException, ReasonerInterruptedException, TimeOutException,
        InconsistentOntologyException, OWLOntologyCreationException, OWLOntologyChangeException
    {
        // TODO: Create a version of this method that utilises the
        // loadOntology(RepositoryConnection...) method
        final OWLOntology nextOntology = this.loadOntology(ontologyResourcePath);
        
        // rename the ontology
        // This step is necessary for cases where the loaded ontology either does not have an
        // owl:versionIRI statement, or the versionIRI will not be unique in the repository.
        
        // IMPORTANT NOTE:
        // The version IRI must be unique in the manager before this step or the load will fail due
        // to the ontology already existing!
        // FIXME: To get around this we would need to load the ontology into memory as RDF
        // statements and modify it before loading it out of the in-memory ontology, which is very
        // possible...
        this.manager.applyChange(new SetOntologyID(nextOntology, newOWLOntologyID));
        
        final OWLReasoner reasoner = this.checkConsistency(nextOntology);
        this.dumpOntologyToRepository(nextOntology, nextRepositoryConnection);
        final OWLOntology nextInferredOntology =
                this.computeInferences(reasoner, this.generateInferredOntologyID(nextOntology.getOntologyID()));
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.dumpOntologyToRepository(nextInferredOntology, nextRepositoryConnection);
        
        // update the link in the schema ontology management graph
        this.updateCurrentManagedSchemaOntologyVersion(nextRepositoryConnection, nextOntology.getOntologyID(),
                nextInferredOntology.getOntologyID());
        
        return new InferredOWLOntologyID(nextOntology.getOntologyID().getOntologyIRI(), nextOntology.getOntologyID()
                .getVersionIRI(), nextInferredOntology.getOntologyID().getOntologyIRI());
    }
    
    /**
     * Loads an ontology from a classpath resource, checks the consistency of the ontology, infers
     * statements from the ontology, and stores the inferred statements.
     * 
     * <br />
     * 
     * The ontology IRI and version IRI are taken from inside the ontology after it is loaded.
     * 
     * <br />
     * 
     * IMPORTANT: The inferred ontology has an ontology IRI that is derived from the version IRI of
     * the loaded ontology. The version IRI of the loaded ontology must be unique for this process
     * to succeed.
     * 
     * @param ontologyResourcePath
     *            The classpath resource to load the ontology from.
     * @param nextRepositoryConnection
     *            The repository connection to use for storing the ontology and the inferred
     *            statements.
     * @throws Exception
     * @throws IOException
     * @throws RepositoryException
     * @throws ReasonerInterruptedException
     * @throws TimeOutException
     * @throws InconsistentOntologyException
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyChangeException
     */
    public InferredOWLOntologyID loadInferAndStoreSchemaOntology(final String ontologyResourcePath,
            final RepositoryConnection nextRepositoryConnection) throws Exception, IOException, RepositoryException,
        ReasonerInterruptedException, TimeOutException, InconsistentOntologyException, OWLOntologyCreationException,
        OWLOntologyChangeException
    {
        // TODO: Create a version of this method that utilises the
        // loadOntology(RepositoryConnection...) method
        final OWLOntology nextOntology = this.loadOntology(ontologyResourcePath);
        
        final OWLReasoner reasoner = this.checkConsistency(nextOntology);
        this.dumpOntologyToRepository(nextOntology, nextRepositoryConnection);
        final OWLOntology nextInferredOntology =
                this.computeInferences(reasoner, this.generateInferredOntologyID(nextOntology.getOntologyID()));
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        this.dumpOntologyToRepository(nextInferredOntology, nextRepositoryConnection);
        
        // update the link in the schema ontology management graph
        this.updateCurrentManagedSchemaOntologyVersion(nextRepositoryConnection, nextOntology.getOntologyID(),
                nextInferredOntology.getOntologyID());
        
        return new InferredOWLOntologyID(nextOntology.getOntologyID().getOntologyIRI(), nextOntology.getOntologyID()
                .getVersionIRI(), nextInferredOntology.getOntologyID().getOntologyIRI());
    }
    
    /**
     * Loads an ontology from a Sesame RepositoryConnection, given an optional set of contexts.
     * 
     * @param conn
     *            The Sesame RepositoryConnection object to use when loading the ontology.
     * @param contexts
     *            An optional varargs array of contexts specifying the contexts to use when loading
     *            the ontology. If this is missing the entire repository will be used.
     * @return An OWLOntology instance populated with the triples from the repository.
     * @throws Exception
     */
    public OWLOntology loadOntology(final RepositoryConnection conn, final Resource... contexts) throws Exception
    {
        final RioMemoryTripleSource tripleSource =
                new RioMemoryTripleSource(conn.getStatements(null, null, null, true, contexts));
        tripleSource.setNamespaces(conn.getNamespaces());
        
        final OWLOntology nextOntology = this.manager.loadOntologyFromOntologyDocument(tripleSource);
        
        Assert.assertFalse(nextOntology.isEmpty());
        
        return nextOntology;
    }
    
    /**
     * Loads an ontology from a Java Resource on the classpath. This is useful for loading test
     * resources.
     * 
     * NOTE: We currently assume that the ontology will be in RDF/XML. Outside of the prototype we
     * cannot make this assumption as any RDF or OWL format may be used.
     * 
     * @param ontologyResource
     *            The classpath location of the test resource to load.
     * @return An OWLOntology instance populated with the triples from the classpath resource.
     * @throws Exception
     */
    public OWLOntology loadOntology(final String ontologyResource) throws Exception
    {
        final OWLOntology nextOntology =
                this.manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(this.getClass()
                        .getResourceAsStream(ontologyResource), new RDFXMLOntologyFormatFactory()));
        Assert.assertFalse(nextOntology.isEmpty());
        
        return nextOntology;
    }
    
    public InferredOWLOntologyID loadPoddArtifact(final String artifactResourcePath,
            final RepositoryConnection nextRepositoryConnection) throws Exception
    {
        // 1. Create permanent identifiers for any impermanent identifiers in the object...
        this.log.info("Loading podd artifact from: {}", artifactResourcePath);
        final OWLOntology nextOntology = this.loadOntology(artifactResourcePath);
        
        // 2. Validate the object in terms of the OWL profile
        // 3. Validate the object using a reasoner
        this.log.info("Checking consistency of podd artifact");
        final OWLReasoner reasoner = this.checkConsistency(nextOntology);
        
        // 4. Store the object
        this.dumpOntologyToRepository(nextOntology, nextRepositoryConnection);
        
        // 5. Infer extra statements about the object using a reasoner
        this.log.info("Computing inferences for podd artifact");
        final OWLOntology nextInferredOntology =
                this.computeInferences(reasoner, this.generateInferredOntologyID(nextOntology.getOntologyID()));
        
        // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
        // Sesame Repository
        // 6. Store the inferred statements
        this.dumpOntologyToRepository(nextInferredOntology, nextRepositoryConnection);
        
        // 7. Update the PODD Artifact management graph to contain the latest
        // update the link in the PODD Artifact management graph
        this.updateCurrentManagedPoddArtifactOntologyVersion(nextRepositoryConnection, nextOntology.getOntologyID(),
                nextInferredOntology.getOntologyID());
        
        return new InferredOWLOntologyID(nextOntology.getOntologyID().getOntologyIRI(), nextOntology.getOntologyID()
                .getVersionIRI(), nextInferredOntology.getOntologyID().getOntologyIRI());
    }
    
    /**
     * This method adds information to the Schema Ontology management graph, and updates the links
     * for the current version for both the ontology and the inferred ontology.
     * 
     * @param nextRepositoryConnection
     *            The repository connection to use for updating the code. The schema graph/context
     *            to use is setup as a member variable.
     * @param nextOntologyID
     *            The ontology ID that contains the information about the original ontology.
     * @param nextInferredOntologyID
     *            The ontology ID that contains the information about the inferred ontology.
     * @throws RepositoryException
     */
    public void updateCurrentManagedPoddArtifactOntologyVersion(final RepositoryConnection nextRepositoryConnection,
            final OWLOntologyID nextOntologyID, final OWLOntologyID nextInferredOntologyID) throws RepositoryException
    {
        final URI nextOntologyUri = nextOntologyID.getOntologyIRI().toOpenRDFURI();
        final URI nextVersionUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        // NOTE: The version is not used for the inferred ontology ID. A new ontology URI must be
        // generated for each new inferred ontology generation. For reference though, the version is
        // equal to the ontology IRI in the prototype code. See generateInferredOntologyID method
        // for the corresponding code.
        final URI nextInferredOntologyUri = nextInferredOntologyID.getOntologyIRI().toOpenRDFURI();
        
        try
        {
            // type the ontology
            nextRepositoryConnection.add(nextOntologyUri, RDF.TYPE, OWL.ONTOLOGY, this.artifactGraph);
            // setup a version number link for this version
            nextRepositoryConnection.add(nextOntologyUri, PoddPrototypeUtils.OWL_VERSION_IRI, nextVersionUri,
                    this.artifactGraph);
            
            // remove whatever was previously there for the current version marker
            nextRepositoryConnection.remove(nextOntologyUri, PoddPrototypeUtils.OMV_CURRENT_VERSION, null,
                    this.artifactGraph);
            
            // then insert the new current version marker
            nextRepositoryConnection.add(nextOntologyUri, PoddPrototypeUtils.OMV_CURRENT_VERSION, nextVersionUri,
                    this.artifactGraph);
            
            // then do a similar process with the inferred axioms ontology
            nextRepositoryConnection.add(nextInferredOntologyUri, RDF.TYPE, OWL.ONTOLOGY, this.artifactGraph);
            
            // remove whatever was previously there for the current inferred version marker
            nextRepositoryConnection.remove(nextOntologyUri, PoddPrototypeUtils.PODD_BASE_CURRENT_INFERRED_VERSION,
                    null, this.artifactGraph);
            
            // link from the ontology IRI to the current inferred axioms ontology version
            nextRepositoryConnection.add(nextOntologyUri, PoddPrototypeUtils.PODD_BASE_CURRENT_INFERRED_VERSION,
                    nextInferredOntologyUri, this.artifactGraph);
            
            // link from the ontology version IRI to the matching inferred axioms ontology version
            nextRepositoryConnection.add(nextOntologyUri, PoddPrototypeUtils.PODD_BASE_INFERRED_VERSION,
                    nextInferredOntologyUri, this.artifactGraph);
            
            // if everything went well commit the connection
            nextRepositoryConnection.commit();
        }
        catch(final Exception e)
        {
            // if anything failed, rollback the connection before rethrowing the exception
            nextRepositoryConnection.rollback();
            throw e;
        }
        
    }
    
    /**
     * This method adds information to the Schema Ontology management graph, and updates the links
     * for the current version for both the ontology and the inferred ontology.
     * 
     * @param nextRepositoryConnection
     *            The repository connection to use for updating the code. The schema graph/context
     *            to use is setup as a member variable.
     * @param nextOntologyID
     *            The ontology ID that contains the information about the original ontology.
     * @param nextInferredOntologyID
     *            The ontology ID that contains the information about the inferred ontology.
     * @throws RepositoryException
     */
    public void updateCurrentManagedSchemaOntologyVersion(final RepositoryConnection nextRepositoryConnection,
            final OWLOntologyID nextOntologyID, final OWLOntologyID nextInferredOntologyID) throws RepositoryException
    {
        final URI nextOntologyUri = nextOntologyID.getOntologyIRI().toOpenRDFURI();
        final URI nextVersionUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        // NOTE: The version is not used for the inferred ontology ID. A new ontology URI must be
        // generated for each new inferred ontology generation. For reference though, the version is
        // equal to the ontology IRI in the prototype code. See generateInferredOntologyID method
        // for the corresponding code.
        final URI nextInferredOntologyUri = nextInferredOntologyID.getOntologyIRI().toOpenRDFURI();
        
        try
        {
            // type the ontology
            nextRepositoryConnection.add(nextOntologyUri, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
            // setup a version number link for this version
            nextRepositoryConnection.add(nextOntologyUri, PoddPrototypeUtils.OWL_VERSION_IRI, nextVersionUri,
                    this.schemaGraph);
            
            // remove whatever was previously there for the current version marker
            nextRepositoryConnection.remove(nextOntologyUri, PoddPrototypeUtils.OMV_CURRENT_VERSION, null,
                    this.schemaGraph);
            
            // then insert the new current version marker
            nextRepositoryConnection.add(nextOntologyUri, PoddPrototypeUtils.OMV_CURRENT_VERSION, nextVersionUri,
                    this.schemaGraph);
            
            // then do a similar process with the inferred axioms ontology
            nextRepositoryConnection.add(nextInferredOntologyUri, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
            
            // remove whatever was previously there for the current inferred version marker
            nextRepositoryConnection.remove(nextOntologyUri, PoddPrototypeUtils.PODD_BASE_CURRENT_INFERRED_VERSION,
                    null, this.schemaGraph);
            
            // link from the ontology IRI to the current inferred axioms ontology version
            nextRepositoryConnection.add(nextOntologyUri, PoddPrototypeUtils.PODD_BASE_CURRENT_INFERRED_VERSION,
                    nextInferredOntologyUri, this.schemaGraph);
            
            // link from the ontology version IRI to the matching inferred axioms ontology version
            nextRepositoryConnection.add(nextOntologyUri, PoddPrototypeUtils.PODD_BASE_INFERRED_VERSION,
                    nextInferredOntologyUri, this.schemaGraph);
            
            // if everything went well commit the connection
            nextRepositoryConnection.commit();
        }
        catch(final Exception e)
        {
            // if anything failed, rollback the connection before rethrowing the exception
            nextRepositoryConnection.rollback();
            throw e;
        }
        
    }
    
}
