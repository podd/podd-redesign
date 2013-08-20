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
package com.github.podd.impl.purl;

import org.openrdf.model.URI;

import com.github.podd.api.purl.PoddPurlReference;

/**
 * A simple PoddPurlReference implementation that is immutable.
 * 
 * @author kutila
 * 
 */
public class SimplePoddPurlReference implements PoddPurlReference
{
    
    private URI temporaryURI;
    private URI purlURI;
    
    /**
     * 
     * @param temporaryURI
     *            The temporary URI from which this Purl was generated
     * @param purlURI
     *            The permanent URI
     */
    public SimplePoddPurlReference(final URI temporaryURI, final URI purlURI)
    {
        this.temporaryURI = temporaryURI;
        this.purlURI = purlURI;
    }
    
    @Override
    public URI getPurlURI()
    {
        return this.purlURI;
    }
    
    @Override
    public URI getTemporaryURI()
    {
        return this.temporaryURI;
    }
    
}
