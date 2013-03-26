/**
 * 
 */
package com.github.podd.api.file;

import java.util.Collection;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.URI;

import com.github.podd.api.PoddRdfProcessor;

/**
 * A factory interface that is instantiated by plugins to create file references for different types
 * of file references based on RDF triples.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface PoddFileReferenceProcessor<T extends PoddFileReference> extends PoddRdfProcessor
{
    Set<URI> getTypes();
    
    boolean canHandle(Model rdfStatements);
    
    Collection<T> createReferences(Model rdfStatements);
}
