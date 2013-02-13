/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.URI;

import com.github.podd.api.PoddRdfProcessor;
import com.github.podd.api.PoddRdfProcessorFactory;

/**
 * Utility class containing helper methods for handling RDF content.
 * 
 * @author kutila
 * 
 */
public class PoddRdfUtils
{
    /**
     * Builds a SPARQL "construct" query using the given <code>PoddRdfProcessorFactory</code>.
     * 
     * @param rdfProcessorFactory
     *            The Factory from which SPARQL query parts are obtained
     * @return A SPARQL query String
     */
    public static String buildSparqlConstructQuery(
            final PoddRdfProcessorFactory<? extends PoddRdfProcessor> rdfProcessorFactory)
    {
        return PoddRdfUtils.buildSparqlConstructQuery(rdfProcessorFactory, null);
    }
    
    /**
     * Builds a SPARQL "construct" query using the given <code>PoddRdfProcessorFactory</code> and
     * Subject URI.
     * 
     * @param rdfProcessorFactory
     *            The Factory from which SPARQL query parts are obtained
     * @param subject
     *            A URI of interest that should be used to build the SPARQL query
     * @return A SPARQL query String
     */
    public static String buildSparqlConstructQuery(
            final PoddRdfProcessorFactory<? extends PoddRdfProcessor> rdfProcessorFactory, final URI subject)
    {
        final StringBuilder sparqlBuilder = new StringBuilder();
        sparqlBuilder.append("CONSTRUCT { ");
        sparqlBuilder.append(rdfProcessorFactory.getSPARQLConstructBGP());
        sparqlBuilder.append(" } WHERE { ");
        
        sparqlBuilder.append(rdfProcessorFactory.getSPARQLConstructWhere());
        
        sparqlBuilder.append(" }");
        
        if(subject != null)
        {
            sparqlBuilder.append(" VALUES (?");
            sparqlBuilder.append(rdfProcessorFactory.getSPARQLVariable());
            sparqlBuilder.append(") { (<");
            sparqlBuilder.append(subject.stringValue());
            sparqlBuilder.append("> ) }");
        }
        
        if(!rdfProcessorFactory.getSPARQLGroupBy().isEmpty())
        {
            sparqlBuilder.append(" GROUP BY ");
            sparqlBuilder.append(rdfProcessorFactory.getSPARQLGroupBy());
        }
        
        return sparqlBuilder.toString();
    }
}
