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
package com.github.podd.api.purl;

import java.util.ArrayList;
import java.util.List;

import com.github.ansell.abstractserviceloader.AbstractServiceLoader;
import com.github.podd.api.PoddProcessorStage;

/**
 * A registry for implementations of the PoddPurlProcessorFactory interface.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class PoddPurlProcessorFactoryRegistry extends AbstractServiceLoader<String, PoddPurlProcessorFactory>
{

    private static final PoddPurlProcessorFactoryRegistry instance = new PoddPurlProcessorFactoryRegistry();

    /**
     * @return A static instance of this registry.
     */
    public static PoddPurlProcessorFactoryRegistry getInstance()
    {
        return PoddPurlProcessorFactoryRegistry.instance;
    }

    public PoddPurlProcessorFactoryRegistry()
    {
        super(PoddPurlProcessorFactory.class);
    }

    /**
     * From amongst all the PODD PURL processor factories available with this registry, retrieve a
     * list of the factories that support the given <code>PoddProcessorStage</code>.
     *
     * @param nextStage
     * @return
     */
    public final List<PoddPurlProcessorFactory> getByStage(final PoddProcessorStage nextStage)
    {
        final List<PoddPurlProcessorFactory> result = new ArrayList<PoddPurlProcessorFactory>();
        for(final PoddPurlProcessorFactory nextProcessorFactory : this.getAll())
        {
            if(nextProcessorFactory.canHandleStage(nextStage))
            {
                result.add(nextProcessorFactory);
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
    public final String getKey(final PoddPurlProcessorFactory nextFactory)
    {
        return nextFactory.getKey();
    }

}
