/**
 * 
 */
package com.github.podd.api;

import org.openrdf.model.Graph;
import org.openrdf.model.URI;

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
     * Creates a query that can be used to construct triples relevant to the object with the given
     * subject URI, as long as it is relevant to this processor.
     * 
     * NOTE: This method will likely be called for each of the results from a query using
     * getSPARQLConstructWhere(), but that behaviour may not be always the case, so the type of the
     * subject still needs to be validated inside of the query.
     * 
     * @param subject
     *            The URI of a specific object to fetch results for.
     * @return
     */
    String getSPARQLConstructWhere(URI subject);
    
    /**
     * 
     * @return Any SPARQL Group By statements needed to aggregate results inside of the query.
     */
    String getSPARQLGroupBy();
    
}
