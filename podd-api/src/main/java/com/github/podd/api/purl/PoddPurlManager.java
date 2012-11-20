/**
 * 
 */
package com.github.podd.api.purl;

import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface PoddPurlManager
{
    
    Set<PoddPurlReference> extractPurlReferences(RepositoryConnection tempConn, URI... contexts);
    
    PoddPurlProcessorFactoryRegistry getProcessorFactoryRegistry();
    
    void setPurlFactoryRegistry(PoddPurlProcessorFactoryRegistry testPurlRegistry);
    
}
