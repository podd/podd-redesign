/**
 * 
 */
package com.github.podd.api.purl;

import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.github.podd.exception.PurlProcessorNotHandledException;

/**
 * A manager object used to manage conversion of temporary URIs to Permanent URLs as required by
 * PODD.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface PoddPurlManager
{
    /**
     * Convert the temporary URIs from the RepositoryConnection in the given contexts to PURLs.
     * 
     * @param purlResults
     *            Set of PurlReferences containing temporary URIs and their corresponding PURLs
     * @param repositoryConnection
     *            The RepositoryConnection in which temporary URIs need to be replaced
     * @param contexts
     *            The contexts in the Repository to be considered
     * @throws RepositoryException
     * @throws UpdateExecutionException
     */
    void convertTemporaryUris(Set<PoddPurlReference> purlResults, RepositoryConnection repositoryConnection,
            URI... contexts) throws RepositoryException, UpdateExecutionException;
    
    /**
     * Identify temporary URIs from the RepositoryConnection (in the given contexts) and generate
     * PURLs for them.
     * 
     * @param repositoryConnection
     * @param contexts
     *            The contexts in the Repository to be considered
     * @return A set of PoddPurlReferences containing the extracted temporary URIs and the PURLs
     *         generated for them
     * @throws PurlProcessorNotHandledException
     * @throws RepositoryException
     */
    Set<PoddPurlReference> extractPurlReferences(RepositoryConnection repositoryConnection, URI... contexts)
        throws PurlProcessorNotHandledException, RepositoryException;
    
    /**
     * Identify temporary URIs from the RepositoryConnection (in the given contexts) and generate
     * PURLs for them.
     * 
     * @param repositoryConnection
     * @param parentUri
     *            An optional parent URI that the {@link PoddPurlProcessor} may use when creating
     *            Purls using {@link PoddPurlProcessor#handleTranslation(URI, URI)}.
     * @param contexts
     *            The contexts in the Repository to be considered
     * @return A set of PoddPurlReferences containing the extracted temporary URIs and the PURLs
     *         generated for them
     * @throws PurlProcessorNotHandledException
     * @throws RepositoryException
     */
    Set<PoddPurlReference> extractPurlReferences(URI parentUri, RepositoryConnection repositoryConnection,
            URI... contexts) throws PurlProcessorNotHandledException, RepositoryException;
    
    /**
     * Retrieve the <code>PodPurlProcessorFactoryRegistry</code> assigned to this Manager.
     * 
     * @return
     */
    PoddPurlProcessorFactoryRegistry getPurlProcessorFactoryRegistry();
    
    /**
     * Set the the <code>PodPurlProcessorFactoryRegistry</code> for this Manager.
     * 
     * @param purlProcessorFactoryRegistry
     */
    void setPurlProcessorFactoryRegistry(PoddPurlProcessorFactoryRegistry purlProcessorFactoryRegistry);
    
}
