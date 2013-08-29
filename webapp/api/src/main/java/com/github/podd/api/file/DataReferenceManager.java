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

import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;

/**
 * A manager object used to maintain data references between PODD Artifacts and the various data
 * repositories that contain the actual data.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface DataReferenceManager
{
    /**
     * Extracts all data references found in the given RepositoryConnection within the given
     * contexts.
     * 
     * @param conn
     *            The {@link RepositoryConnection} in which to search for data references
     * @param contexts
     *            The contexts to be searched in
     * @return A Set of {@link DataReference} objects extracted from the repository.
     * @throws OpenRDFException
     */
    Set<DataReference> extractDataReferences(RepositoryConnection conn, URI... contexts) throws OpenRDFException;
    
    /**
     * @return A registry instance for DataReferenceProcessorFactory
     */
    DataReferenceProcessorRegistry getDataProcessorRegistry();
    
    /**
     * @param registry
     *            Set the registry for DataReferenceProcessorFactory
     */
    void setDataProcessorRegistry(DataReferenceProcessorRegistry registry);
    
}
