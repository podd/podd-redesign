/**
 * 
 */
package com.github.podd.prototype;

import java.io.IOException;

import org.junit.Assert;
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
import org.semanticweb.owlapi.rio.RioRenderer;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;

/**
 * A selection of utilities used to create the prototype.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddPrototypeUtils
{
    
    /**
     * 
     */
    public PoddPrototypeUtils()
    {
        // TODO Auto-generated constructor stub
    }
    
    public OWLReasoner checkConsistency(final OWLOntology nextOntology, final IRI nextProfileIRI,
            final OWLReasonerFactory nextReasonerFactory) throws Exception
    {
        final OWLProfile nextProfile = OWLProfileRegistry.getInstance().getProfile(nextProfileIRI);
        Assert.assertNotNull("Could not find profile in registry", nextProfile);
        final OWLProfileReport profileReport = nextProfile.checkOntology(nextOntology);
        Assert.assertTrue("Schema Ontology was not in the given profile", profileReport.isInProfile());
        
        // create an OWL Reasoner using the Pellet library and ensure that the reasoner thinks the
        // ontology is consistent so far
        // Use the factory that we found to create a reasoner over the ontology
        final OWLReasoner nextReasoner = nextReasonerFactory.createReasoner(nextOntology);
        
        // Test that the ontology was consistent with this reasoner
        // This ensures in the case of Pellet that it is in the OWL2-DL profile
        Assert.assertTrue("Ontology was not consistent", nextReasoner.isConsistent());
        
        return nextReasoner;
    }
    
    /**
     * @param ontologyManager
     *            TODO
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
     * @param nextContextUri
     *            The context URI to dump the ontology triples into.
     * @param nextOntology
     *            The ontology to dump into the repository.
     * @param nextRepositoryConnection
     *            The repository connection to dump the triples into.
     * @param ontologyManager
     *            TODO
     * @throws IOException
     * @throws RepositoryException
     */
    public void dumpOntologyToRepository(final URI nextContextUri, final OWLOntology nextOntology,
            final RepositoryConnection nextRepositoryConnection, final OWLOntologyManager ontologyManager)
        throws IOException, RepositoryException
    {
        // Create an RDFHandler that will insert all triples after they are emitted from OWLAPI into
        // a single context in the Sesame Repository
        final RDFInserter repositoryHandler = new RDFInserter(nextRepositoryConnection);
        repositoryHandler.enforceContext(nextContextUri);
        
        // Render the triples out from OWLAPI into a Sesame Repository
        final RioRenderer renderer =
                new RioRenderer(nextOntology, ontologyManager, repositoryHandler, null, nextContextUri);
        renderer.render();
        
        // Commit the current repository connection
        nextRepositoryConnection.commit();
    }
    
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
