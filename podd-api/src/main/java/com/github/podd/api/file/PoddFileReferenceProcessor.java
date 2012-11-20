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
public interface PoddFileReferenceProcessor extends PoddRdfProcessor
{
    PoddFileReference createReference(Graph rdfStatements);
}
