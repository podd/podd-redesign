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

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import com.github.ansell.abstractserviceloader.AbstractServiceLoader;
import com.github.podd.exception.DataRepositoryException;
import com.github.podd.utils.PoddRdfConstants;

/**
 * A registry containing dynamically loaded instances of {@link DataReferenceProcessorFactory}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddDataRepositoryRegistry extends AbstractServiceLoader<String, PoddDataRepositoryFactory>
{
    private static final PoddDataRepositoryRegistry instance = new PoddDataRepositoryRegistry();
    
    /**
     * @return A static instance of this registry.
     */
    public static PoddDataRepositoryRegistry getInstance()
    {
        return PoddDataRepositoryRegistry.instance;
    }
    
    public PoddDataRepositoryRegistry()
    {
        super(PoddDataRepositoryFactory.class);
    }
    
    public PoddDataRepository<?> createDataRepository(final Model model) throws DataRepositoryException
    {
        for(final Resource nextMatchingRepository : model.filter(null, RDF.TYPE, PoddRdfConstants.PODD_DATA_REPOSITORY)
                .subjects())
        {
            final Set<Value> types = model.filter(nextMatchingRepository, RDF.TYPE, null).objects();
            final Set<URI> uriTypes = new HashSet<URI>();
            for(final Value nextType : types)
            {
                if(nextType instanceof URI)
                {
                    uriTypes.add((URI)nextType);
                }
            }
            
            for(final PoddDataRepositoryFactory factory : PoddDataRepositoryRegistry.getInstance().getAll())
            {
                if(factory.canCreate(uriTypes))
                {
                    return factory.createDataRepository(model.filter(nextMatchingRepository, null, null));
                }
            }
        }
        
        throw new DataRepositoryException("Could not find any repositories in the given statements");
    }
    
    @Override
    public final String getKey(final PoddDataRepositoryFactory nextFactory)
    {
        return nextFactory.getKey();
    }
    
}
