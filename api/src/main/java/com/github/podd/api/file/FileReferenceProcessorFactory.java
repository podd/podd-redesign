/**
 * 
 */
package com.github.podd.api.file;

import com.github.podd.api.PoddRdfProcessorFactory;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface FileReferenceProcessorFactory extends PoddRdfProcessorFactory<FileReferenceProcessor<FileReference>>
{
    /**
     * 
     * @return A string identifying the parent SPARQL variable that would either be bound to a
     *         parent where possible, or be used to identify all file references under a given
     *         parent object.
     */
    String getParentSPARQLVariable();
}
