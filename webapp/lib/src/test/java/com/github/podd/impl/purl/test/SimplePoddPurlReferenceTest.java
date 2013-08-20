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
package com.github.podd.impl.purl.test;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.github.podd.api.purl.PoddPurlReference;
import com.github.podd.api.purl.test.AbstractPoddPurlReferenceTest;
import com.github.podd.impl.purl.SimplePoddPurlReference;

/**
 * @author kutila
 */
public class SimplePoddPurlReferenceTest extends AbstractPoddPurlReferenceTest
{
    
    @Override
    protected PoddPurlReference getNewPoddPurlReference()
    {
        final URI tempUri = ValueFactoryImpl.getInstance().createURI("urn:temp:this/is/supposed/to/be/a:temporary:uri");
        final URI purl = ValueFactoryImpl.getInstance().createURI("http://purl.org/this/is/supposed/to/be/a:purl");
        return new SimplePoddPurlReference(tempUri, purl);
    }
    
}
