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
package com.github.podd.integration.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public abstract class AbstractSesameTest
{
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    private Repository testRepository;
    
    private ValueFactory testValueFactory;
    
    private RepositoryConnection testRepositoryConnection;
    
    /**
     * @return the testRepository
     */
    public Repository getTestRepository()
    {
        return this.testRepository;
    }
    
    /**
     * @return the testRepositoryConnection
     */
    public RepositoryConnection getTestRepositoryConnection()
    {
        return this.testRepositoryConnection;
    }
    
    /**
     * @return the testValueFactory
     */
    public ValueFactory getTestValueFactory()
    {
        return this.testValueFactory;
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.testRepository = new SailRepository(new MemoryStore());
        this.testRepository.initialize();
        
        this.testValueFactory = this.testRepository.getValueFactory();
        
        this.testRepositoryConnection = this.testRepository.getConnection();
        this.testRepositoryConnection.begin();
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        boolean errorOccurred = false;
        
        if(this.testRepositoryConnection != null)
        {
            try
            {
                this.testRepositoryConnection.rollback();
                this.testRepositoryConnection.close();
            }
            catch(final RepositoryException e)
            {
                errorOccurred = true;
                this.log.error("Test repository connection could not be closed", e);
            }
        }
        
        this.testRepositoryConnection = null;
        
        this.testValueFactory = null;
        
        if(this.testRepository != null)
        {
            try
            {
                this.testRepository.shutDown();
            }
            catch(final RepositoryException e)
            {
                errorOccurred = true;
                this.log.error("Test repository could not be shutdown", e);
            }
        }
        
        this.testRepository = null;
        
        Assert.assertFalse("Error occurred during tearDown", errorOccurred);
    }
    
}
