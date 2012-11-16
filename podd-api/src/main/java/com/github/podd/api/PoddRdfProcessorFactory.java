/**
 * 
 */
package com.github.podd.api;

import org.openrdf.model.Graph;

/**
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 * @param <T>
 *            The type of objects that this RDF Processor creates.
 */
public interface PoddRdfProcessorFactory<T extends PoddRdfProcessor> extends
        PoddProcessorFactory<T, PoddRdfProcessorEvent, Graph>
{
    /**
     * Defines the SPARQL Construct Basic Graph Pattern (BGP) that will be used to create sets of
     * triples relevant to this processor, when used in combination with the SPARQL Construct WHERE
     * clause.
     * 
     * @return A SPARQL Construct Basic Graph Pattern that can be used to generate sets of RDF
     *         Graphs that are interesting in the context of this processor.
     */
    String getSPARQLConstructBGP();
    
    String getSPARQLConstructWhere();
    
    String getSPARQLGroupBy();
    
}
