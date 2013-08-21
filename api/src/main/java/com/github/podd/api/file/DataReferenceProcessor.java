/**
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
public interface DataReferenceProcessor<T extends DataReference> extends PoddRdfProcessor
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
