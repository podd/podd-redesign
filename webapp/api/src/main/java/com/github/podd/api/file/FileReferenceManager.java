package com.github.podd.api.file;

import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;

/**
 * A manager object used to maintain file references between PODD Artifacts and the various file
 * repositories that contain the actual files.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface FileReferenceManager
{
    /**
     * Extracts all file references found in the given RepositoryConnection within the given contexts.
     * 
     * @param conn
     *            The {@link RepositoryConnection} in which to search for file references
     * @param contexts
     *            The contexts to be searched in
     * @return A Set of {@link FileReference} objects extracted from the repository.
     * @throws OpenRDFException
     */
    Set<FileReference> extractFileReferences(RepositoryConnection conn, URI... contexts) throws OpenRDFException;
    
    /**
     * @return A registry instance for FileReferenceProcessorFactory
     */
    FileReferenceProcessorFactoryRegistry getFileProcessorFactoryRegistry();
    
    /**
     * @param registry
     *            Set the registry for FileReferenceProcessorFactory
     */
    void setProcessorFactoryRegistry(FileReferenceProcessorFactoryRegistry registry);
    
}
