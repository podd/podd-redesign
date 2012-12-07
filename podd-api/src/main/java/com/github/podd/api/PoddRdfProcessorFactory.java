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
public interface PoddRdfProcessorFactory<T extends PoddRdfProcessor> extends PoddProcessorFactory<T, Graph>
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
    
    /**
     * Creates a query that can be used to construct triples for all objects that are relevant to
     * this processor.
     * 
     * @return
     */
    String getSPARQLConstructWhere();
    
    /**
     * 
     * @return Any SPARQL Group By statements needed to aggregate results inside of the query.
     */
    String getSPARQLGroupBy();
    
    /**
     * 
     * @return The name of the variable to be bound to
     */
    String getSPARQLVariable();
    
}
