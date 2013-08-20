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
package com.github.podd.impl.file;

import java.util.Set;

import org.kohsuke.MetaInfServices;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;

import com.github.podd.api.file.PoddDataRepository;
import com.github.podd.api.file.PoddDataRepositoryFactory;
import com.github.podd.exception.DataRepositoryException;
import com.github.podd.exception.FileRepositoryIncompleteException;
import com.github.podd.utils.PoddRdfConstants;

/**
 * A factory to build {@link PoddDataRepository} instances from a {@link Model}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
@MetaInfServices(PoddDataRepositoryFactory.class)
public class SPARQLDataRepositoryImplFactory implements PoddDataRepositoryFactory
{
    @Override
    public boolean canCreate(Set<URI> types)
    {
        return types.contains(PoddRdfConstants.PODD_SPARQL_DATA_REPOSITORY);
    }
    
    @Override
    public PoddDataRepository<?> createDataRepository(Model statements) throws DataRepositoryException
    {
        if(statements.contains(null, RDF.TYPE, PoddRdfConstants.PODD_SPARQL_DATA_REPOSITORY))
        {
            return new SPARQLDataRepositoryImpl(statements);
        }
        
        throw new FileRepositoryIncompleteException(statements,
                "Could not create SPARQL data repository from this configuration");
    }
    
    @Override
    public String getKey()
    {
        return "datarepositoryfactory:" + PoddRdfConstants.PODD_SPARQL_DATA_REPOSITORY.stringValue();
    }
}
