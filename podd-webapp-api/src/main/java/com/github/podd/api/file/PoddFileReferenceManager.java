package com.github.podd.api.file;

import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * A manager object used to maintain file references between PODD Artifacts and the various file
 * repositories that contain the actual files.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface PoddFileReferenceManager
{
    void addFileReference(PoddFileReference reference);
    
    void deleteFileReference(PoddFileReference reference);
    
    Set<PoddFileReference> extractFileReferences(RepositoryConnection conn, URI... contexts) throws RepositoryException;
    
    PoddFileReferenceProcessorFactoryRegistry getFileProcessorFactoryRegistry();
    
    Set<PoddFileReference> getFileReferences(OWLOntologyID artifactId);
    
    Set<PoddFileReference> getFileReferences(OWLOntologyID artifactId, IRI objectIri);
    
    Set<PoddFileReference> getFileReferences(OWLOntologyID artifactId, String alias);
    
    void setProcessorFactoryRegistry(PoddFileReferenceProcessorFactoryRegistry registry);
    
    void verifyFileReferences(Set<PoddFileReference> fileReferenceResults, RepositoryConnection tempConn, URI openRDFURI)
        throws RepositoryException;
}
