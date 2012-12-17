/**
 * 
 */
package com.github.podd.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.RDFInserter;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.profiles.OWLProfile;
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

import com.github.podd.api.PoddOWLManager;
import com.github.podd.exception.EmptyOntologyException;
import com.github.podd.exception.InconsistentOntologyException;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.PublishArtifactException;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;

/**
 * Implementation of PoddOWLManager interface.
 * 
 * @author kutila
 * 
 */
public class PoddOWLManagerImpl implements PoddOWLManager
{
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    private OWLOntologyManager owlOntologyManager;
    
    private OWLReasonerFactory reasonerFactory;
    
    @Override
    public void cacheSchemaOntology(final InferredOWLOntologyID ontologyID, final RepositoryConnection conn)
        throws OpenRDFException
    {
        // load ontology statements and inferred statements from the respective graphs into OWL
        // Ontology Manager
        
        /*
         * IRI schemaOntologyGraph = ontologyID.getVersionIRI(); IRI inferredSchemaOntologyGraph =
         * ontologyID.getInferredOntologyIRI();
         * 
         * RepositoryResult<Statement> baseStatements = conn.getStatements(null, null, null, true,
         * schemaOntologyGraph.toOpenRDFURI()); RepositoryResult<Statement> inferredStatements =
         * conn.getStatements(null, null, null, true, inferredSchemaOntologyGraph.toOpenRDFURI());
         */
        throw new RuntimeException("TODO: Implement cacheSchemaOntology");
    }
    
    @Override
    public OWLReasoner createReasoner(final OWLOntology nextOntology)
    {
        return this.reasonerFactory.createReasoner(nextOntology);
    }
    
    @Override
    public InferredOWLOntologyID generateInferredOntologyID(final OWLOntologyID ontologyID)
    {
        if(ontologyID == null || ontologyID.getOntologyIRI() == null || ontologyID.getVersionIRI() == null)
        {
            throw new NullPointerException("OWLOntology is incomplete");
        }
        
        final IRI inferredOntologyIRI = IRI.create(PoddRdfConstants.INFERRED_PREFIX + ontologyID.getVersionIRI());
        
        return new InferredOWLOntologyID(ontologyID.getOntologyIRI(), ontologyID.getVersionIRI(), inferredOntologyIRI);
    }
    
    @Override
    public OWLOntologyID getCurrentVersion(final IRI ontologyIRI)
    {
        throw new RuntimeException("TODO: Implement getCurrentVersion");
    }
    
    @Override
    public OWLOntology getOntology(final OWLOntologyID ontologyID) throws IllegalArgumentException, OWLException
    {
        return this.owlOntologyManager.getOntology(ontologyID);
    }
    
    @Override
    public OWLOntologyManager getOWLOntologyManager()
    {
        return this.owlOntologyManager;
    }
    
    @Override
    public OWLReasonerFactory getReasonerFactory()
    {
        return this.reasonerFactory;
    }
    
    @Override
    public OWLProfile getReasonerProfile()
    {
        final Set<OWLProfile> profiles = this.reasonerFactory.getSupportedProfiles();
        if(!profiles.isEmpty())
        {
            if(profiles.size() > 1)
            {
                this.log.info("Reasoner factory supports {} profiles. Returning one of: {}", profiles.size(), profiles);
            }
            return profiles.iterator().next();
        }
        else
        {
            this.log.warn("Could not find any supported OWL Profiles");
            return null;
        }
    }
    
    @Override
    public List<OWLOntologyID> getVersions(final IRI ontologyIRI)
    {
        throw new RuntimeException("TODO: Implement getVersions");
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
    private OWLOntology computeInferences(final OWLReasoner nextReasoner, final OWLOntologyID inferredOntologyID)
        throws ReasonerInterruptedException, TimeOutException, OWLOntologyCreationException, OWLOntologyChangeException
    {
        nextReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        
        final List<InferredAxiomGenerator<? extends OWLAxiom>> axiomGenerators =
                new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
        axiomGenerators.add(new InferredClassAssertionAxiomGenerator());
        axiomGenerators.add(new InferredDataPropertyCharacteristicAxiomGenerator());
        axiomGenerators.add(new InferredEquivalentClassAxiomGenerator());
        axiomGenerators.add(new InferredEquivalentDataPropertiesAxiomGenerator());
        axiomGenerators.add(new InferredEquivalentObjectPropertyAxiomGenerator());
        axiomGenerators.add(new InferredInverseObjectPropertiesAxiomGenerator());
        axiomGenerators.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
        
        // NOTE: InferredPropertyAssertionGenerator significantly slows down inference computation
        // axiomGenerators.add(new
        // org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator());
        
        axiomGenerators.add(new InferredSubClassAxiomGenerator());
        axiomGenerators.add(new InferredSubDataPropertyAxiomGenerator());
        axiomGenerators.add(new InferredSubObjectPropertyAxiomGenerator());
        
        final InferredOntologyGenerator iog = new InferredOntologyGenerator(nextReasoner, axiomGenerators);
        final OWLOntology nextInferredAxiomsOntology = this.owlOntologyManager.createOntology(inferredOntologyID);
        
        iog.fillOntology(nextInferredAxiomsOntology.getOWLOntologyManager(), nextInferredAxiomsOntology);
        
        return nextInferredAxiomsOntology;
    }
    
    @Override
    public InferredOWLOntologyID inferStatements(final OWLOntology nextOntology,
            final RepositoryConnection nextRepositoryConnection) throws OWLRuntimeException, OWLException,
        OpenRDFException, IOException
    {
        final InferredOWLOntologyID inferredOntologyID = this.generateInferredOntologyID(nextOntology.getOntologyID());
        final OWLReasoner nextReasoner = this.createReasoner(nextOntology);
        
        final OWLOntology nextInferredAxiomsOntology =
                this.computeInferences(nextReasoner, inferredOntologyID.getInferredOWLOntologyID());
        
        this.dumpOntologyToRepository(nextInferredAxiomsOntology, nextRepositoryConnection);
        
        return inferredOntologyID;
    }
    
    @Override
    public boolean isPublished(final IRI ontologyIRI)
    {
        throw new RuntimeException("TODO: Implement isPublished(IRI)");
    }
    
    @Override
    public boolean isPublished(final OWLOntologyID ontologyID, final RepositoryConnection repositoryConnection)
        throws OpenRDFException
    {
        if(ontologyID == null || ontologyID.getOntologyIRI() == null || ontologyID.getVersionIRI() == null)
        {
            throw new NullPointerException("OWLOntology is incomplete");
        }
        
        final OWLOntology ontology = this.owlOntologyManager.getOntology(ontologyID);
        if(ontology == null || ontology.isEmpty())
        {
            return false;
        }
        final URI artifactGraphUri = ontologyID.getVersionIRI().toOpenRDFURI();
        
        /*
         * ASK {
         * 
         * ?artifact owl:versionIRI ontology-version .
         * 
         * ?artifact poddBase:hasTopObject ?top .
         * 
         * ?top poddBase:hasPublicationStatus poddBase:Published .
         * 
         * }
         */
        final String sparqlQuery =
                "ASK { " + "?artifact <" + PoddRdfConstants.OWL_VERSION_IRI.stringValue() + "> "
                        + ontologyID.getVersionIRI().toQuotedString() + " . " + "?artifact <"
                        + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT.stringValue() + "> ?top ." + " ?top <"
                        + PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS.stringValue() + "> <"
                        + PoddRdfConstants.PODDBASE_PUBLISHED.stringValue() + ">" + " }";
        
        this.log.info("Generated SPARQL {}", sparqlQuery);
        
        final BooleanQuery booleanQuery = repositoryConnection.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlQuery);
        
        // Create a dataset to specify the contexts
        final DatasetImpl dataset = new DatasetImpl();
        dataset.addDefaultGraph(artifactGraphUri);
        dataset.addNamedGraph(artifactGraphUri);
        booleanQuery.setDataset(dataset);
        
        return booleanQuery.evaluate();
    }
    
    @Override
    public OWLOntology loadOntology(final OWLOntologyDocumentSource owlSource) throws OWLException, IOException,
        PoddException
    {
        OWLOntology nextOntology;
        if(owlSource instanceof RioMemoryTripleSource)
        {
            
            final RioParserImpl owlParser = new RioParserImpl(null);
            
            nextOntology = this.owlOntologyManager.createOntology();
            
            owlParser.parse(owlSource, nextOntology);
        }
        else
        {
            nextOntology = this.owlOntologyManager.loadOntologyFromOntologyDocument(owlSource);
        }
        
        if(nextOntology.isEmpty())
        {
            throw new EmptyOntologyException(nextOntology, "Loaded ontology is empty");
        }
        return nextOntology;
    }
    
    @Override
    public OWLOntologyID parseRDFStatements(final RepositoryConnection conn, final URI... contexts)
        throws OpenRDFException, OWLException, IOException, PoddException
    {
        final RioMemoryTripleSource owlSource =
                new RioMemoryTripleSource(conn.getStatements(null, null, null, true, contexts));
        
        final RioParserImpl owlParser = new RioParserImpl(null);
        
        final OWLOntology nextOntology = this.owlOntologyManager.createOntology();
        
        if(conn.size(contexts) == 0)
        {
            throw new EmptyOntologyException(nextOntology, "No statements to create an ontology");
        }
        
        owlParser.parse(owlSource, nextOntology);
        if(nextOntology.isEmpty())
        {
            throw new EmptyOntologyException(nextOntology, "Loaded ontology is empty");
        }
        
        return nextOntology.getOntologyID();
    }
    
    @Override
    public boolean removeCache(final OWLOntologyID ontologyID) throws OWLException
    {
        // TODO: Verify that this .contains method matches our desired semantics
        final boolean containsOntology = this.owlOntologyManager.contains(ontologyID);
        
        if(containsOntology)
        {
            this.owlOntologyManager.removeOntology(ontologyID);
            
            // return true if the ontology manager does not contain the ontology at this point
            return !this.owlOntologyManager.contains(ontologyID);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public void setCurrentVersion(final OWLOntologyID ontologyID)
    {
        throw new RuntimeException("TODO: Implement setCurrentVersion");
    }
    
    @Override
    public void setOWLOntologyManager(final OWLOntologyManager manager)
    {
        this.owlOntologyManager = manager;
        
    }
    
    @Override
    public InferredOWLOntologyID setPublished(final OWLOntologyID ontologyID) throws PublishArtifactException
    {
        throw new RuntimeException("TODO: Implement setPublished");
    }
    
    @Override
    public void setReasonerFactory(final OWLReasonerFactory reasonerFactory)
    {
        this.reasonerFactory = reasonerFactory;
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
    @Override
    public void dumpOntologyToRepository(final OWLOntology nextOntology,
            final RepositoryConnection nextRepositoryConnection, final URI... contexts) throws IOException,
        RepositoryException
    {
        if(nextOntology.getOntologyID().getVersionIRI() == null)
        {
            throw new IllegalArgumentException(
                    "Cannot dump an ontology to repository if it does not have a version IRI");
        }
        
        // Create an RDFHandler that will insert all triples after they are emitted from OWLAPI
        // into a specific context in the Sesame Repository
        final RDFInserter repositoryHandler = new RDFInserter(nextRepositoryConnection);
        RioRenderer renderer;
        
        if(contexts == null || contexts.length == 0)
        {
            repositoryHandler.enforceContext(nextOntology.getOntologyID().getVersionIRI().toOpenRDFURI());
            // Render the triples out from OWLAPI into a Sesame Repository
            renderer =
                    new RioRenderer(nextOntology, nextOntology.getOWLOntologyManager(), repositoryHandler, null,
                            nextOntology.getOntologyID().getVersionIRI().toOpenRDFURI());
        }
        else
        {
            repositoryHandler.enforceContext(contexts);
            // Render the triples out from OWLAPI into a Sesame Repository
            renderer =
                    new RioRenderer(nextOntology, nextOntology.getOWLOntologyManager(), repositoryHandler, null,
                            contexts);
        }
        renderer.render();
    }
    
}
