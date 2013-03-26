package com.github.podd.api.file;

import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.utils.InferredOWLOntologyID;

/**
 * A manager object used to maintain file references between PODD Artifacts and the various file
 * repositories that contain the actual files.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface PoddFileReferenceManager
{
    Set<PoddFileReference> extractFileReferences(RepositoryConnection conn, URI... contexts) throws RepositoryException;
    
    PoddFileReferenceProcessorFactoryRegistry getFileProcessorFactoryRegistry();
    
    void setProcessorFactoryRegistry(PoddFileReferenceProcessorFactoryRegistry registry);
    
}
