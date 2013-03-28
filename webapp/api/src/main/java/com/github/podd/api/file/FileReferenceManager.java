package com.github.podd.api.file;

import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * A manager object used to maintain file references between PODD Artifacts and the various file
 * repositories that contain the actual files.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface FileReferenceManager
{
    Set<FileReference> extractFileReferences(RepositoryConnection conn, URI... contexts) throws RepositoryException;
    
    FileReferenceProcessorFactoryRegistry getFileProcessorFactoryRegistry();
    
    void setProcessorFactoryRegistry(FileReferenceProcessorFactoryRegistry registry);
    
}
