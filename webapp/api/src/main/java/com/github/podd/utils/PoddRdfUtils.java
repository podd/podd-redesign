/*
 * PODD is an OWL ontology database used for scientific project management
 * 
 * Copyright (C) 2009-2013 The University Of Queensland
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
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
