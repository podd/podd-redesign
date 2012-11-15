package com.github.podd.api.file;

import java.util.Set;

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
    
    Set<PoddFileReference> getFileReferences(OWLOntologyID artifactId);
    
    Set<PoddFileReference> getFileReferences(OWLOntologyID artifactId, IRI objectIri);
    
    Set<PoddFileReference> getFileReferences(OWLOntologyID artifactId, String alias);
}
