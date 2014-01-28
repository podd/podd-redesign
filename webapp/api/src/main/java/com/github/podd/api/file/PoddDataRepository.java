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

import java.io.IOException;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.URI;

import com.github.podd.api.data.DataReference;
import com.github.podd.exception.DataReferenceNotSupportedException;

/**
 * This interface represents the basic type for all data repositories. PODD uses Data Repositories
 * to represent different ways of accessing binary data that is not stored inside of PODD Artifacts.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddDataRepository<T extends DataReference>
{
    public final static String PROTOCOL_SSH = "SSH";
    
    public final static String PROTOCOL_HTTP = "HTTP";
    
    /**
     * Checks whether this {@link PoddDataRepository} instance is capable of "handling" (i.e.
     * validating) the given {@link DataReference}.
     * 
     * This decision is made by checking that the aliases and types match.
     * 
     * @param reference
     * @return
     */
    boolean canHandle(T reference);
    
    String getAlias();
    
    /**
     * Retrieve a Model representation of all this {@link PoddDataRepository}. This should contain
     * sufficient information to reconstruct this {@link PoddDataRepository} object.
     * 
     * @return A Model representation containing all configurations of this FileRepository.
     */
    Model getAsModel();
    
    /**
     * 
     * @return The set of RDF Types for {@link DataReference}s that can be stored by this
     *         repository. All of the non-OWL-built-in types should be in this set.
     */
    Set<URI> getTypes();
    
    /**
     * Validates the given DataReference instance.
     * 
     * @param reference
     *            The DataReference to be validated
     * @return True if the validation was successful, false otherwise
     * @throws DataReferenceNotSupportedException
     * @throws IOException
     */
    boolean validate(T reference) throws DataReferenceNotSupportedException, IOException;
    
}
