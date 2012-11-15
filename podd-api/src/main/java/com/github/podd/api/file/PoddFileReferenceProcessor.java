/**
 * 
 */
package com.github.podd.api.file;

import org.openrdf.model.Graph;

import com.github.podd.api.PoddRdfProcessor;

/**
 * A factory interface that is instantiated by plugins to create file references for different types
 * of file references based on RDF triples.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface PoddFileReferenceProcessor<T extends PoddFileReference> extends PoddRdfProcessor<T>
{
    /**
     * 
     * @param manager The PoddFileReferenceManager that can be used to track and update file references for objects.
     */
    void setFileReferenceManager(PoddFileReferenceManager manager);
    
    /**
     * 
     * @param rdfTriples
     *            An immutable set of RDF triples that represent the file reference in RDF triples.
     * @return An instance of PoddFileReference that represents the file reference given in the RDF triples. This file reference can be used to 
     */
    T generateFileReference(Graph rdfTriples);
}
