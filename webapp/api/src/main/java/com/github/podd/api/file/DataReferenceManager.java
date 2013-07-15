package com.github.podd.api.file;

import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;

/**
 * A manager object used to maintain data references between PODD Artifacts and the various data
 * repositories that contain the actual data.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface DataReferenceManager
{
    /**
     * Extracts all data references found in the given RepositoryConnection within the given
     * contexts.
     * 
     * @param conn
     *            The {@link RepositoryConnection} in which to search for data references
     * @param contexts
     *            The contexts to be searched in
     * @return A Set of {@link DataReference} objects extracted from the repository.
     * @throws OpenRDFException
     */
    Set<DataReference> extractDataReferences(RepositoryConnection conn, URI... contexts) throws OpenRDFException;
    
    /**
     * @return A registry instance for DataReferenceProcessorFactory
     */
    DataReferenceProcessorRegistry getDataProcessorRegistry();
    
    /**
     * @param registry
     *            Set the registry for DataReferenceProcessorFactory
     */
    void setDataProcessorRegistry(DataReferenceProcessorRegistry registry);
    
}
