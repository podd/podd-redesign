/**
 * 
 */
package com.github.podd.api.file;

import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddRdfProcessor;

/**
 * A factory interface that is instantiated by plugins to create file references for different types
 * of file references based on RDF triples.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface PoddFileReferenceProcessor extends PoddRdfProcessor
{
    void setArtifactManager(PoddArtifactManager testArtifactManager);
    
    /**
     * 
     * @param manager
     *            The PoddFileReferenceManager that can be used to track and update file references
     *            for objects.
     */
    void setFileReferenceManager(PoddFileReferenceManager manager);
    
}
