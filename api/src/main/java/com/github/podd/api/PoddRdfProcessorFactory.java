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
package com.github.podd.api;

import org.openrdf.model.Model;

/**
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 * @param <T>
 *            The type of objects that this RDF Processor creates.
 */
public interface PoddRdfProcessorFactory<T extends PoddRdfProcessor> extends PoddProcessorFactory<T, Model>
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
