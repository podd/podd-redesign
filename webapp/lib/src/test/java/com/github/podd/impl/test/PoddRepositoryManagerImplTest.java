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
package com.github.podd.impl.test;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.test.AbstractPoddRepositoryManagerTest;
import com.github.podd.impl.PoddRepositoryManagerImpl;

/**
 * @author kutila
 * 
 */
public class PoddRepositoryManagerImplTest extends AbstractPoddRepositoryManagerTest
{
    
    @Override
    protected PoddRepositoryManager getNewPoddRepositoryManagerInstance() throws RepositoryException
    {
        Repository managementRepository = new SailRepository(new MemoryStore());
        managementRepository.initialize();
        Repository permanentRepository = new SailRepository(new MemoryStore());
        permanentRepository.initialize();
        
        return new PoddRepositoryManagerImpl(managementRepository, permanentRepository);
    }
    
}
