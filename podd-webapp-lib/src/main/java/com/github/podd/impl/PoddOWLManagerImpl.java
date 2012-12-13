/**
 * 
 */
package com.github.podd.impl;

import java.io.IOException;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
import org.semanticweb.owlapi.rio.RioParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.exception.EmptyOntologyException;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.PublishArtifactException;
import com.github.podd.utils.InferredOWLOntologyID;

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
        throw new RuntimeException("TODO: Implement generateInferredOntologyID");
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
        throw new RuntimeException("TODO: Implement getReasonerProfile");
    }
    
    @Override
    public List<OWLOntologyID> getVersions(final IRI ontologyIRI)
    {
        throw new RuntimeException("TODO: Implement getVersions");
    }
    
    @Override
    public InferredOWLOntologyID inferStatements(final OWLOntologyID inferredOWLOntologyID,
            final RepositoryConnection permanentRepositoryConnection)
    {
        throw new RuntimeException("TODO: Implement inferStatements");
    }
    
    @Override
    public boolean isPublished(final IRI ontologyIRI)
    {
        throw new RuntimeException("TODO: Implement isPublished(IRI)");
    }
    
    @Override
    public boolean isPublished(final OWLOntologyID ontologyId)
    {
        throw new RuntimeException("TODO: Implement isPublished(OWLOntologyID)");
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
    public void setCurrentVersion(final OWLOntologyID ontologyId)
    {
        throw new RuntimeException("TODO: Implement setCurrentVersion");
    }
    
    @Override
    public void setOWLOntologyManager(final OWLOntologyManager manager)
    {
        this.owlOntologyManager = manager;
        
    }
    
    @Override
    public InferredOWLOntologyID setPublished(final OWLOntologyID ontologyId) throws PublishArtifactException
    {
        throw new RuntimeException("TODO: Implement setPublished");
    }
    
    @Override
    public void setReasonerFactory(final OWLReasonerFactory reasonerFactory)
    {
        this.reasonerFactory = reasonerFactory;
    }
    
}
