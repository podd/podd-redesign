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
 * An interface that is instantiated by plugins to create file references for different types of
 * file references based on RDF triples.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface FileReferenceProcessor<T extends FileReference> extends PoddRdfProcessor
{
    /**
     * Decides whether this File Reference Processor is able to extract references from the given
     * input Model. <br>
     * Returning true does not indicate that this processor is able to handle ALL file references
     * found in the Model, simply that it is able to examine the given Model and extract any file
     * references that matches types it knows of.
     * 
     * @param rdfStatements
     *            A Model from which File references need to be extracted.
     * @return True if this processor should be able to create File References from statements
     *         contained in the given Model, and false if it is not known whether this will be
     *         possible, or if a NULL value is passed in.
     */
    boolean canHandle(Model rdfStatements);
    
    /**
     * Extracts from the given Model, file references of the types supported by this processor.
     * 
     * @param rdfStatements
     *            A Model from which File references need to be extracted.
     * @return A Collection of File References that were extracted from the given Model.
     */
    Collection<T> createReferences(Model rdfStatements);
    
    /**
     * 
     * @return A Set of File Reference types that this Processor can handle.
     */
    Set<URI> getTypes();
}
