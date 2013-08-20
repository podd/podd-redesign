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
package com.github.podd.api.file;

import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.URI;

import com.github.podd.exception.DataRepositoryException;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface PoddDataRepositoryFactory
{
    /**
     * 
     * @param types
     *            The type URIs for the given repository that needs to be created.
     * @return True if this factory can create a repository to access all of the given type URIs.
     */
    boolean canCreate(Set<URI> types);
    
    /**
     * 
     * @param statements
     *            The RDF statements defining the repository configuration.
     * @return A new instance of the repository.
     * @throws DataRepositoryException
     *             If there was an error creating the repository.
     */
    PoddDataRepository<?> createDataRepository(Model statements) throws DataRepositoryException;
    
    /**
     * @return A Unique identifying string for this instance of this factory.
     */
    String getKey();
}
