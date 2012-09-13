/**
 * 
 */
package com.github.podd.prototype;

import java.io.IOException;

import org.junit.Assert;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
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
    private final Logger log = LoggerFactory.getLogger(PoddPrototypeUtils.class);
    
    /**
     * 
     */
    public PoddPrototypeUtils()
    {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Checks the consistency of the ontology and returns the instance of OWLReasoner that was used
     * to check the consistency.
     * 
     * @param nextOntology
     *            The ontology to check for consistency.
     * @param nextProfileIRI
     *            The IRI of the OWL Profile to use for checking consistency.
     * @param nextReasonerFactory
     *            The OWLReasonerFactory that is able to create instances of an OWLReasoner that are
     *            compatible with the given OWL Profile.
     * @return An instance of OWLreasoner that was used to check the consistency.s
     * @throws Exception
     */
    public OWLReasoner checkConsistency(final OWLOntology nextOntology, final IRI nextProfileIRI,
            final OWLReasonerFactory nextReasonerFactory) throws Exception
    {
        final OWLProfile nextProfile = OWLProfileRegistry.getInstance().getProfile(nextProfileIRI);
        Assert.assertNotNull("Could not find profile in registry: " + nextProfileIRI.toQuotedString(), nextProfile);
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
        final OWLReasoner nextReasoner = nextReasonerFactory.createReasoner(nextOntology);
        
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
     * @param inferredOntologyUri
     *            The IRI to use for the resulting OWLOntology containing the inferred statements.
     * @param inferredOntologyVersionUri
     *            The IRI to use for the version of the resulting OWLOntology containing the
     *            inferred statements.
     * @param ontologyManager
     *            The OWLOntologyManager to use for the process.
     * @return An OWLOntology instance containing the axioms that were inferred from the original
     *         ontology.
     * @throws ReasonerInterruptedException
     * @throws TimeOutException
     * @throws InconsistentOntologyException
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyChangeException
     */
    public OWLOntology computeInferences(final OWLReasoner nextReasoner, final IRI inferredOntologyUri,
            final IRI inferredOntologyVersionUri, final OWLOntologyManager ontologyManager)
        throws ReasonerInterruptedException, TimeOutException, InconsistentOntologyException,
        OWLOntologyCreationException, OWLOntologyChangeException
    {
        nextReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        final InferredOntologyGenerator iog = new InferredOntologyGenerator(nextReasoner);
        final OWLOntology nextInferredAxiomsOntology =
                ontologyManager.createOntology(new OWLOntologyID(inferredOntologyUri, inferredOntologyVersionUri));
        iog.fillOntology(ontologyManager, nextInferredAxiomsOntology);
        
        return nextInferredAxiomsOntology;
    }
    
    /**
     * Dump the triples representing a given ontology into a Sesame Repository.
     * 
     * @param nextOntologyContextUri
     *            The context URI to dump the ontology triples into.
     * @param nextOntology
     *            The ontology to dump into the repository.
     * @param nextRepositoryConnection
     *            The repository connection to dump the triples into.
     * @param ontologyManager
     *            The ontology manager containing the given ontology.
     * @param managementContextUri
     *            The URI of a context in the repository that is used to track Schema Ontologies and
     *            their current versions
     * @throws IOException
     * @throws RepositoryException
     */
    public void dumpSchemaOntologyToRepository(final URI nextOntologyContextUri, final OWLOntology nextOntology,
            final RepositoryConnection nextRepositoryConnection, final OWLOntologyManager ontologyManager,
            final URI managementContextUri) throws IOException, RepositoryException
    {
        try
        {
            // Create an RDFHandler that will insert all triples after they are emitted from OWLAPI
            // into a single context in the Sesame Repository
            final RDFInserter repositoryHandler = new RDFInserter(nextRepositoryConnection);
            repositoryHandler.enforceContext(nextOntologyContextUri);
            
            // Render the triples out from OWLAPI into a Sesame Repository
            final RioRenderer renderer =
                    new RioRenderer(nextOntology, ontologyManager, repositoryHandler, null, nextOntologyContextUri);
            renderer.render();
            
            // TODO: Store the ontology IRI and ontology version IRI into managementContextUri
            // context and overwrite any previous currentVersion links
            
            // Commit the current repository connection
            nextRepositoryConnection.commit();
        }
        catch(final Exception e)
        {
            nextRepositoryConnection.rollback();
            throw e;
        }
    }
    
    /**
     * Loads an ontology from a Sesame RepositoryConnection, given an optional set of contexts.
     * 
     * @param conn
     *            The Sesame RepositoryConnection object to use when loading the ontology.
     * @param ontologyManager
     *            The ontology manager to use when loading the ontology.
     * @param contexts
     *            An optional varargs array of contexts specifying the contexts to use when loading
     *            the ontology. If this is missing the entire repository will be used.
     * @return An OWLOntology instance populated with the triples from the repository.
     * @throws Exception
     */
    public OWLOntology loadOntology(final RepositoryConnection conn, final OWLOntologyManager ontologyManager,
            final Resource... contexts) throws Exception
    {
        final RioMemoryTripleSource tripleSource =
                new RioMemoryTripleSource(conn.getStatements(null, null, null, true, contexts));
        tripleSource.setNamespaces(conn.getNamespaces());
        
        final OWLOntology nextOntology = ontologyManager.loadOntologyFromOntologyDocument(tripleSource);
        
        Assert.assertFalse(nextOntology.isEmpty());
        
        return nextOntology;
    }
    
    /**
     * Loads an ontology from a Java Resource on the classpath. This is useful for loading test
     * resources.
     * 
     * @param ontologyResource
     *            The classpath location of the test resource to load.
     * @param ontologyManager
     *            The ontology manager to use when loading the ontology.
     * @return An OWLOntology instance populated with the triples from the classpath resource.
     * @throws Exception
     */
    public OWLOntology loadOntology(final String ontologyResource, final OWLOntologyManager ontologyManager)
        throws Exception
    {
        final OWLOntology nextOntology =
                ontologyManager.loadOntologyFromOntologyDocument(new StreamDocumentSource(this.getClass()
                        .getResourceAsStream(ontologyResource), new RDFXMLOntologyFormatFactory()));
        Assert.assertFalse(nextOntology.isEmpty());
        
        return nextOntology;
    }
    
}
