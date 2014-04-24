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
package com.github.podd.impl.data;

import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;

import com.github.podd.api.data.PoddDataRepository;
import com.github.podd.api.data.PoddDataRepositoryFactory;
import com.github.podd.exception.DataRepositoryException;
import com.github.podd.exception.DataRepositoryIncompleteException;
import com.github.podd.utils.PODD;

/**
 * A factory to build {@link PoddDataRepository} instances from a {@link Model}.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
// Not using this as it doesn't work well with M2E in Eclipse
// @MetaInfServices(PoddDataRepositoryFactory.class)
public class SSHFileRepositoryImplFactory implements PoddDataRepositoryFactory
{
    @Override
    public boolean canCreate(final Set<URI> types)
    {
        return types.contains(PODD.PODD_SSH_FILE_REPOSITORY);
    }

    @Override
    public PoddDataRepository<?> createDataRepository(final Resource nextDataRepository, final Model statements)
            throws DataRepositoryException
            {
        if(statements.contains(null, RDF.TYPE, PODD.PODD_SSH_FILE_REPOSITORY))
        {
            return new SSHFileRepositoryImpl(nextDataRepository, statements);
        }

        throw new DataRepositoryIncompleteException("Could not create SSH file repository from this configuration");
            }

    @Override
    public String getKey()
    {
        return "datarepositoryfactory:" + PODD.PODD_SSH_FILE_REPOSITORY.stringValue();
    }
}
