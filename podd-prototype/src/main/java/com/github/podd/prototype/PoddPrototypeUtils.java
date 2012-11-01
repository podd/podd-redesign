/**
 * 
 */
package com.github.podd.prototype;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.fortytwo.sesametools.URITranslator;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
import org.semanticweb.owlapi.formats.RioRDFOntologyFormatFactory;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
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
import org.semanticweb.owlapi.rio.RioParserImpl;
import org.semanticweb.owlapi.rio.RioRenderer;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredDataPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentDataPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentObjectPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredInverseObjectPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredObjectPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubDataPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;
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
     * 
     * TODO: Put this in an external ontology somewhere so it isn't dependent on PODD.
     */
    public static final URI PODD_BASE_CURRENT_INFERRED_VERSION = ValueFactoryImpl.getInstance().createURI(
            "http://purl.org/podd/ns/poddBase#", "currentInferredVersion");
    
    /**
     * Creating a property for PODD to track the inferredVersion for the inferred axioms ontology of
     * a particular versioned ontology.
     * 
     * TODO: Put this in an external ontology somewhere so it isn't dependent on PODD.
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
    
    private final Logger statsLogger = LoggerFactory.getLogger("statsLogger");
    
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
     * https://github.com/ansell/protege-owl-diff/blob/master/src
     * /main/java/org/protege/owl/diff/Engine.java
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
     * @return An instance of OWLreasoner that was used to check the consistency.
     * @throws PoddException
     */
    public OWLReasoner checkConsistency(final OWLOntology nextOntology) throws PoddException
    {
        final OWLProfile nextProfile = OWLProfileRegistry.getInstance().getProfile(this.owlProfile);
        if(nextProfile == null)
        {
            throw new PoddException("Could not find profile in registry: " + this.owlProfile.toQuotedString(), null,
                    PoddException.ERR_PROFILE_NOT_FOUND);
        }
        
        final OWLProfileReport profileReport = nextProfile.checkOntology(nextOntology);
        if(!profileReport.isInProfile())
        {
            this.removeOntologyFromManager(nextOntology.getOntologyID());
            
            // TODO - could be due to incomplete imports also
            throw new PoddException("Ontology not in given profile: " + nextOntology.getOntologyID().toString(),
                    profileReport, PoddException.ERR_ONTOLOGY_NOT_IN_PROFILE);
        }
        
        // create an OWL Reasoner using the Pellet library and ensure that the reasoner thinks the
        // ontology is consistent so far
        // Use the factory that we found to create a reasoner over the ontology
        final OWLReasoner nextReasoner = this.reasonerFactory.createReasoner(nextOntology);
        
        // Test that the ontology was consistent with this reasoner
        // This ensures in the case of Pellet that it is in the OWL2-DL profile
        // if(!nextReasoner.isConsistent() || nextReasoner.getUnsatisfiableClasses().getSize() > 0)
        if(!nextReasoner.isConsistent())
        {
            this.removeOntologyFromManager(nextOntology.getOntologyID());
            
            throw new PoddException("Ontology not consistent: " + nextOntology.getOntologyID().toString(),
                    profileReport, PoddException.ERR_INCONSISTENT_ONTOLOGY);
        }
        
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
        // long startedAt = System.currentTimeMillis();
        nextReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        // this.statsLogger.info("precomputeInferences()," + (System.currentTimeMillis() -
        // startedAt));
        
        final List<InferredAxiomGenerator<? extends OWLAxiom>> axiomGenerators =
                new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
        axiomGenerators.add(new InferredClassAssertionAxiomGenerator());
        axiomGenerators.add(new InferredDataPropertyCharacteristicAxiomGenerator());
        axiomGenerators.add(new InferredEquivalentClassAxiomGenerator());
        axiomGenerators.add(new InferredEquivalentDataPropertiesAxiomGenerator());
        axiomGenerators.add(new InferredEquivalentObjectPropertyAxiomGenerator());
        axiomGenerators.add(new InferredInverseObjectPropertiesAxiomGenerator());
        axiomGenerators.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
        
        // loading time for science-3k-objects reduced from 66s to 8.8s after commenting out
        // the InferredPropertyAssertionGenerator below
        // axiomGenerators.add(new InferredPropertyAssertionGenerator());
        
        axiomGenerators.add(new InferredSubClassAxiomGenerator());
        axiomGenerators.add(new InferredSubDataPropertyAxiomGenerator());
        axiomGenerators.add(new InferredSubObjectPropertyAxiomGenerator());
        
        final InferredOntologyGenerator iog = new InferredOntologyGenerator(nextReasoner, axiomGenerators);
        final OWLOntology nextInferredAxiomsOntology = this.manager.createOntology(inferredOntologyID);
        
        // startedAt = System.currentTimeMillis();
        
        iog.fillOntology(nextInferredAxiomsOntology.getOWLOntologyManager(), nextInferredAxiomsOntology);
        // this.statsLogger.info("fillOntology()," + (System.currentTimeMillis() - startedAt));
        
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
                    new RioRenderer(nextOntology, nextOntology.getOWLOntologyManager(), repositoryHandler, null,
                            nextOntology.getOntologyID().getVersionIRI().toOpenRDFURI());
            renderer.render();
            
            // Commit the current repository connection
            nextRepositoryConnection.commit();
        }
        catch(final RepositoryException e)
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
     * <br/>
     * 
     * The given OWLOntologyID will be assigned to the ontology after it is loaded.
     * 
     * <br/>
     * 
     * <b>IMPORTANT:</b> The inferred ontology has an ontology IRI that is derived from the version
     * IRI of the loaded ontology. The version IRI in the given OWLOntologyID must be unique for
     * this process to succeed.
     * 
     * @param ontologyResourcePath
     *            The classpath resource to load the ontology from.
     * @param mimeType
     *            The MIME type of the ontology to load.
     * @param newOWLOntologyID
     *            The OWLOntologyID to be assigned to the ontology after it is loaded.
     * @param nextRepositoryConnection
     *            The repository connection to use for storing the ontology and the inferred
     *            statements.
     * @throws IOException
     * @throws RepositoryException
     * @throws ReasonerInterruptedException
     * @throws TimeOutException
     * @throws InconsistentOntologyException
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyChangeException
     */
    public InferredOWLOntologyID loadInferAndStoreSchemaOntology(final String ontologyResourcePath,
            final String mimeType, final OWLOntologyID newOWLOntologyID,
            final RepositoryConnection nextRepositoryConnection) throws OWLException, OpenRDFException, IOException,
        PoddException
    {
        
        // TODO: Create a version of this method that utilises the
        // loadOntology(RepositoryConnection...) method
        final OWLOntology nextOntology = this.loadOntology(ontologyResourcePath, mimeType);
        
        // rename the ontology
        // This step is necessary for cases where the loaded ontology either does not have an
        // owl:versionIRI statement, or the versionIRI will not be unique in the repository.
        
        // IMPORTANT NOTE:
        // The version IRI must be unique in the manager before this step or the load will fail due
        // to the ontology already existing!
        // FIXME: To get around this we would need to load the ontology into memory as RDF
        // statements and modify it before loading it out of the in-memory ontology, which is very
        // possible...
        nextOntology.getOWLOntologyManager().applyChange(new SetOntologyID(nextOntology, newOWLOntologyID));
        
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
     * <br/>
     * 
     * The ontology IRI and version IRI are taken from inside the ontology after it is loaded.
     * 
     * <br/>
     * 
     * IMPORTANT: The inferred ontology has an ontology IRI that is derived from the version IRI of
     * the loaded ontology. The version IRI of the loaded ontology must be unique for this process
     * to succeed.
     * 
     * @param ontologyResourcePath
     *            The classpath resource to load the ontology from.
     * @param mimeType
     *            The MIME type of the ontology to load.
     * @param nextRepositoryConnection
     *            The repository connection to use for storing the ontology and the inferred
     *            statements.
     */
    public InferredOWLOntologyID loadInferAndStoreSchemaOntology(final String ontologyResourcePath,
            final String mimeType, final RepositoryConnection nextRepositoryConnection) throws OWLException,
        OpenRDFException, IOException, PoddException
    {
        OWLOntology nextOntology = null;
        try
        {
            // TODO: Create a version of this method that utilises the
            // loadOntology(RepositoryConnection...) method
            nextOntology = this.loadOntology(ontologyResourcePath, mimeType);
            
            final OWLReasoner reasoner = this.checkConsistency(nextOntology);
            this.dumpOntologyToRepository(nextOntology, nextRepositoryConnection);
            final OWLOntology nextInferredOntology =
                    this.computeInferences(reasoner, this.generateInferredOntologyID(nextOntology.getOntologyID()));
            
            // TODO: Check that nextInferredOntology imports nextOntology - write a test from the
            // Repository/RDF view
            // to verify (16/10/2012)
            
            // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
            // Sesame Repository
            this.dumpOntologyToRepository(nextInferredOntology, nextRepositoryConnection);
            
            // update the link in the schema ontology management graph
            this.updateCurrentManagedSchemaOntologyVersion(nextRepositoryConnection, nextOntology.getOntologyID(),
                    nextInferredOntology.getOntologyID());
            
            return new InferredOWLOntologyID(nextOntology.getOntologyID().getOntologyIRI(), nextOntology
                    .getOntologyID().getVersionIRI(), nextInferredOntology.getOntologyID().getOntologyIRI());
        }
        catch(final Exception e)
        {
            if(nextOntology != null)
            {
                this.removeOntologyFromManager(nextOntology);
            }
            throw e;
        }
    }
    
    /**
     * Loads an ontology from a Sesame RepositoryConnection, given an optional set of contexts.
     * 
     * @param conn
     *            The Sesame RepositoryConnection object to use when loading the ontology.
     * @param mimeType
     *            The MIME type of the ontology to load.
     * @param contexts
     *            An optional varargs array of contexts specifying the contexts to use when loading
     *            the ontology. If this is missing the entire repository will be used.
     * @return An OWLOntology instance populated with the triples from the repository.
     */
    public OWLOntology loadOntology(final RepositoryConnection conn, final String mimeType, final Resource... contexts)
        throws OpenRDFException, OWLException, IOException, PoddException
    
    {
        final RioMemoryTripleSource owlSource =
                new RioMemoryTripleSource(conn.getStatements(null, null, null, true, contexts));
        owlSource.setNamespaces(conn.getNamespaces());
        
        final RioParserImpl owlParser =
                new RioParserImpl((RioRDFOntologyFormatFactory)OWLOntologyFormatFactoryRegistry.getInstance()
                        .getByMIMEType(mimeType));
        final OWLOntology nextOntology = this.manager.createOntology();
        owlParser.parse(owlSource, nextOntology);
        if(nextOntology.isEmpty())
        {
            throw new PoddException("Loaded ontology is empty", null, PoddException.ERR_EMPTY_ONTOLOGY);
        }
        
        return nextOntology;
    }
    
    /**
     * Loads an ontology from a Java Resource on the classpath. This is useful for loading test
     * resources.
     * 
     * @param ontologyResource
     *            The classpath location of the test resource to load.
     * @param mimeType
     *            The MIME type of the ontology file to load.
     * @return An OWLOntology instance populated with the triples from the classpath resource.
     * @throws Exception
     */
    public OWLOntology loadOntology(final String ontologyResource, final String mimeType)
        throws OWLOntologyCreationException, PoddException
    {
        final InputStream inputStream = this.getClass().getResourceAsStream(ontologyResource);
        
        if(inputStream == null)
        {
            this.log.error("Could not find resource: {}", ontologyResource);
            throw new NullPointerException("Could not find resource: " + ontologyResource);
        }
        
        final OWLOntology nextOntology =
                this.manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(inputStream,
                        OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(mimeType)));
        if(nextOntology.isEmpty())
        {
            throw new PoddException("Loaded ontology is empty", null, PoddException.ERR_EMPTY_ONTOLOGY);
        }
        return nextOntology;
    }
    
    /**
     * Loads a PODD Artifact from the given classpath resource into the database and into the
     * OWLOntologyManager.
     * 
     * This includes verifying that it fits with the expected profile, and verifying that it is
     * consistent with the configured reasoner.
     * 
     * @param artifactResourcePath
     * @param mimeType
     * @param nextRepositoryConnection
     * @return
     * @throws Exception
     */
    public InferredOWLOntologyID loadPoddArtifact(final String artifactResourcePath, final String mimeType,
            final RepositoryConnection nextRepositoryConnection) throws OpenRDFException, OWLException, IOException,
        PoddException
    {
        // load into temporary in memory repository to create persistent URLs
        Repository tempRepository = null;
        RepositoryConnection tempRepositoryConnection = null;
        
        try
        {
            tempRepository = new SailRepository(new MemoryStore());
            tempRepository.initialize();
            tempRepositoryConnection = tempRepository.getConnection();
            tempRepositoryConnection.setAutoCommit(false);
            
            // 1. Create permanent identifiers for any impermanent identifiers in the object...
            final URI randomURN =
                    tempRepositoryConnection.getValueFactory().createURI("urn:random:" + UUID.randomUUID().toString());
            
            final InputStream inputStream = this.getClass().getResourceAsStream(artifactResourcePath);
            
            if(inputStream == null)
            {
                this.log.error("Could not find resource: {}", artifactResourcePath);
                throw new NullPointerException("Could not find resource: " + artifactResourcePath);
            }
            
            tempRepositoryConnection.add(inputStream, "", Rio.getParserFormatForMIMEType(mimeType), randomURN);
            tempRepositoryConnection.commit();
            
            // TODO: improve the permanent URI rather than using http://example.org
            URITranslator.doTranslation(tempRepositoryConnection, "urn:temp:", "http://example.org/permanenturl/"
                    + UUID.randomUUID().toString() + "/", randomURN);
            
            // retrieve list of ontology imports in this artifact
            final URI importURI = IRI.create("http://www.w3.org/2002/07/owl#imports").toOpenRDFURI();
            final RepositoryResult<Statement> importStatements =
                    tempRepositoryConnection.getStatements(null, importURI, null, false, randomURN);
            
            while(importStatements.hasNext())
            {
                final Statement stmt = importStatements.next();
                // get current version IRI for imported ontology
                final RepositoryResult<Statement> currentVersionStatements =
                        nextRepositoryConnection.getStatements(IRI.create(stmt.getObject().stringValue())
                                .toOpenRDFURI(), IRI.create("http://omv.ontoware.org/ontology#currentVersion")
                                .toOpenRDFURI(), null, false, this.schemaGraph);
                
                // update the import statement in artifact to the "current version"
                if(currentVersionStatements.hasNext())
                {
                    final Value currentVersion = currentVersionStatements.next().getObject();
                    tempRepositoryConnection.remove(stmt, randomURN);
                    tempRepositoryConnection.add(stmt.getSubject(), importURI, currentVersion, randomURN);
                }
            }
            tempRepositoryConnection.commit();
            
            this.log.info("Loading podd artifact from repository: {}", randomURN);
            final OWLOntology nextOntology = this.loadOntology(tempRepositoryConnection, mimeType, randomURN);
            
            // regain memory after loading the ontology into OWLAPI
            tempRepositoryConnection.clear();
            
            // 2. Validate the object in terms of the OWL profile
            // 3. Validate the object using a reasoner
            long startedAt = System.currentTimeMillis();
            final OWLReasoner reasoner = this.checkConsistency(nextOntology);
            this.statsLogger.info("checkConsistency:," + (System.currentTimeMillis() - startedAt) + ",");
            
            // 4. Store the object
            this.dumpOntologyToRepository(nextOntology, nextRepositoryConnection);
            
            // 5. Infer extra statements about the object using a reasoner
            this.log.info("Computing inferences for podd artifact");
            startedAt = System.currentTimeMillis();
            final OWLOntology nextInferredOntology =
                    this.computeInferences(reasoner, this.generateInferredOntologyID(nextOntology.getOntologyID()));
            this.statsLogger.info("computeInferences:," + (System.currentTimeMillis() - startedAt) + ",");
            
            // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
            // Sesame Repository
            // 6. Store the inferred statements
            this.dumpOntologyToRepository(nextInferredOntology, nextRepositoryConnection);
            
            // 7. Update the PODD Artifact management graph to contain the latest
            // update the link in the PODD Artifact management graph
            this.updateCurrentManagedPoddArtifactOntologyVersion(nextRepositoryConnection,
                    nextOntology.getOntologyID(), nextInferredOntology.getOntologyID());
            
            return new InferredOWLOntologyID(nextOntology.getOntologyID().getOntologyIRI(), nextOntology
                    .getOntologyID().getVersionIRI(), nextInferredOntology.getOntologyID().getOntologyIRI());
        }
        catch(OpenRDFException | OWLException | IOException | PoddException e)
        {
            
            throw e;
        }
        finally
        {
            if(tempRepositoryConnection != null)
            {
                tempRepositoryConnection.rollback();
                tempRepositoryConnection.close();
            }
            if(tempRepository != null)
            {
                tempRepository.shutDown();
            }
            
        }
    }
    
    /**
     * Removes the PODD Artifact from the OWLOntologyManager.
     * 
     * NOTE: The Artifact is still in the database after this point, it is just no longer in memory.
     * 
     * @param poddArtifact
     *            The InferredOWLOntologyID of the PODD Artifact to remove
     * @return true if the manager contains the artifact and a remove attempt is made, false
     *         otherwise.
     */
    public boolean removePoddArtifactFromManager(final InferredOWLOntologyID poddArtifact)
    {
        if(this.manager.contains(poddArtifact))
        {
            return this.removePoddArtifactFromManager(poddArtifact.getBaseOWLOntologyID(),
                    poddArtifact.getInferredOWLOntologyID());
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Removes the PODD Artifact from the OWLOntologyManager using two different OWLOntologyID
     * references for the base and inferred ontologies respectively.
     * 
     * NOTE: The Artifact is still in the database after this point, it is just no longer in memory.
     * 
     * @param baseOntologyID
     *            The base OWLOntologyID of the Artifact to be removed.
     * @param inferredOntologyID
     *            The inferred OWLOntologyID of the Artifact to be removed.
     * @return true if both the Ontology and Inferred Ontology were successfully removed, false
     *         otherwise.
     */
    public boolean removePoddArtifactFromManager(final OWLOntologyID baseOntologyID,
            final OWLOntologyID inferredOntologyID)
    {
        this.manager.removeOntology(baseOntologyID);
        this.manager.removeOntology(inferredOntologyID);
        if(this.manager.contains(baseOntologyID) || this.manager.contains(inferredOntologyID))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    /**
     * If this ontology is contained in the OWLOntologyManager, removes it from the Manager.
     * 
     * @param ontologyID
     * @return True if the specified ontology was successfully removed. False is returned if the
     *         ontology was not in the manager or it could not be removed.
     */
    protected boolean removeOntologyFromManager(final OWLOntologyID ontologyID)
    {
        if(this.manager.contains(ontologyID))
        {
            this.manager.removeOntology(ontologyID);
            if(!this.manager.contains(ontologyID))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Attempts to remove the specified Ontology from its OWLOntologyManager.
     * 
     * @param nextOntology
     * @return True if the Ontology was in the Manager and it was successfully removed.
     */
    protected boolean removeOntologyFromManager(final OWLOntology nextOntology)
    {
        final OWLOntologyManager nextManager = nextOntology.getOWLOntologyManager();
        final OWLOntologyID ontologyID = nextOntology.getOntologyID();
        if(nextManager.contains(ontologyID))
        {
            nextManager.removeOntology(ontologyID);
            if(!nextManager.contains(ontologyID))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * This method adds information to the PODD artifact management graph, and updates the links for
     * the current version for both the ontology and the inferred ontology.
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
            // 1st 3 parameters represent the triple.
            // 4th (the artifactGraph) is the "context" in which the triple is stored
            nextRepositoryConnection.add(nextOntologyUri, RDF.TYPE, OWL.ONTOLOGY, this.artifactGraph);
            
            // remove previous versionIRI statements
            nextRepositoryConnection.remove(nextOntologyUri, PoddPrototypeUtils.OWL_VERSION_IRI, null,
                    this.artifactGraph);
            
            // TODO: remove the content of any contexts that are the object of versionIRI statements
            
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
            
            // remove the content for all previous inferred versions
            // NOTE: This list should not ever be very large, as we perform this step every time
            // this method is called to update the version
            final RepositoryResult<Statement> repoResults =
                    nextRepositoryConnection.getStatements(nextOntologyUri,
                            PoddPrototypeUtils.PODD_BASE_INFERRED_VERSION, null, false, this.artifactGraph);
            while(repoResults.hasNext())
            {
                final URI inferredVersionUri = IRI.create(repoResults.next().getObject().stringValue()).toOpenRDFURI();
                nextRepositoryConnection.remove(inferredVersionUri, null, null, this.artifactGraph);
            }
            
            nextRepositoryConnection.remove(nextOntologyUri, PoddPrototypeUtils.PODD_BASE_INFERRED_VERSION, null,
                    this.artifactGraph);
            
            // link from the ontology version IRI to the matching inferred axioms ontology version
            nextRepositoryConnection.add(nextOntologyUri, PoddPrototypeUtils.PODD_BASE_INFERRED_VERSION,
                    nextInferredOntologyUri, this.artifactGraph);
            
            // if everything went well commit the connection
            nextRepositoryConnection.commit();
        }
        catch(final RepositoryException e)
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
        catch(final RepositoryException e)
        {
            // if anything failed, rollback the connection before rethrowing the exception
            nextRepositoryConnection.rollback();
            throw e;
        }
        
    }
    
    public URI getSchemaGraph()
    {
        return this.schemaGraph;
    }
    
}
