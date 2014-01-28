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
package com.github.podd.api.data;

import com.github.podd.api.PoddRdfProcessorFactory;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface DataReferenceProcessorFactory extends PoddRdfProcessorFactory<DataReferenceProcessor<DataReference>>
{
    /**
     * 
     * @return A string identifying the parent SPARQL variable that would either be bound to a
     *         parent where possible, or be used to identify all file references under a given
     *         parent object.
     */
    String getParentSPARQLVariable();
}
