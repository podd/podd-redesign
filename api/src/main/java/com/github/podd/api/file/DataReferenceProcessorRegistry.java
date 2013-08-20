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
package com.github.podd.api.file;

import java.util.ArrayList;
import java.util.List;

import com.github.ansell.abstractserviceloader.AbstractServiceLoader;
import com.github.podd.api.PoddProcessorStage;

/**
 * A registry containing dynamically loaded instances of {@link DataReferenceProcessorFactory}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class DataReferenceProcessorRegistry extends AbstractServiceLoader<String, DataReferenceProcessorFactory>
{
    private static final DataReferenceProcessorRegistry instance = new DataReferenceProcessorRegistry();
    
    /**
     * @return A static instance of this registry.
     */
    public static DataReferenceProcessorRegistry getInstance()
    {
        return DataReferenceProcessorRegistry.instance;
    }
    
    public DataReferenceProcessorRegistry()
    {
        super(DataReferenceProcessorFactory.class);
    }
    
    public final List<DataReferenceProcessorFactory> getByStage(final PoddProcessorStage nextStage)
    {
        final List<DataReferenceProcessorFactory> result = new ArrayList<DataReferenceProcessorFactory>();
        for(final DataReferenceProcessorFactory nextProcessor : this.getAll())
        {
            if(nextProcessor.canHandleStage(nextStage))
            {
                result.add(nextProcessor);
            }
        }
        
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.semanticweb.owlapi.util.AbstractServiceLoader#getKey(java.lang.Object)
     */
    @Override
    public final String getKey(final DataReferenceProcessorFactory nextFactory)
    {
        return nextFactory.getKey();
    }
    
}
