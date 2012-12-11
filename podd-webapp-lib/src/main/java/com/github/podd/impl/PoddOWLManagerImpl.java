/**
 * 
 */
package com.github.podd.impl;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.rio.RioMemoryTripleSource;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Implementation of PoddOWLManager interface.
 * 
 * @author kutila
 * 
 */
public class PoddOWLManagerImpl implements PoddOWLManager
{
    
    private OWLOntologyManager owlOntologyManager;
    
    private OWLReasonerFactory reasonerFactory;
    
    @Override
    public void cacheSchemaOntology(final InferredOWLOntologyID ontology, final RepositoryConnection conn)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public OWLReasoner createReasoner(final OWLOntology nextOntology)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public InferredOWLOntologyID generateInferredOntologyID(final OWLOntologyID ontologyID)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public OWLOntology getOntology(final OWLOntologyID ontologyID) throws IllegalArgumentException, OWLException
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public OWLProfile getReasonerProfile()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void inferStatements(final InferredOWLOntologyID inferredOWLOntologyID,
            final RepositoryConnection permanentRepositoryConnection)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public OWLOntology loadOntology(final RioMemoryTripleSource owlSource)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public OWLOntologyID parseRDFStatements(final RepositoryConnection conn, final URI... contexts)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public boolean removeCache(final OWLOntologyID ontologyID) throws OWLException
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public void setOWLOntologyManager(final OWLOntologyManager manager)
    {
        this.owlOntologyManager = manager;
        
    }
    
    @Override
    public void setReasonerFactory(final OWLReasonerFactory reasonerFactory)
    {
        this.reasonerFactory = reasonerFactory;
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
    
}
