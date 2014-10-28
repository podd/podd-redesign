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
package com.github.podd.api.purl.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.podd.api.purl.PoddPurlReference;

/**
 * Simple abstract test class for PoddPurlReference
 *
 * @author kutila
 *
 */
public abstract class AbstractPoddPurlReferenceTest
{
    
    protected PoddPurlReference purlReference;
    
    /**
     *
     * @return A new PoddPurlReference instance for use by the test
     */
    protected abstract PoddPurlReference getNewPoddPurlReference();
    
    @Before
    public void setUp() throws Exception
    {
        this.purlReference = this.getNewPoddPurlReference();
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.purlReference = null;
    }
    
    @Test
    public void testGetPurlURI() throws Exception
    {
        this.purlReference.getPurlURI();
    }
    
    @Test
    public void testGetTemporaryURI() throws Exception
    {
        this.purlReference.getTemporaryURI();
    }
}
