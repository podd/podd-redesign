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
/**
 * 
 */
package com.github.podd.api.data.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.podd.api.data.DataReference;
import com.github.podd.api.data.SPARQLDataReference;

/**
 * Simple abstract test class for SSHFileReference
 * 
 * @author kutila
 */
public abstract class AbstractSPARQLDataReferenceTest extends AbstractDataReferenceTest
{
    protected SPARQLDataReference sparqlDataReference;
    
    @Override
    protected final DataReference getNewDataReference()
    {
        return this.getNewSPARQLDataReference();
    }
    
    /**
     * 
     * @return A new SPARQLDataReference instance for use by the test
     */
    protected abstract SPARQLDataReference getNewSPARQLDataReference();
    
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        this.sparqlDataReference = this.getNewSPARQLDataReference();
    }
    
    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        this.sparqlDataReference = null;
    }
    
    @Test
    public void testGetGraph() throws Exception
    {
        this.sparqlDataReference.getGraph();
    }
    
    @Test
    public void testSetGraph() throws Exception
    {
        this.sparqlDataReference.setGraph("urn:test:graph:1");
    }
    
}
