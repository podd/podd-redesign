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
package com.github.podd.impl;

import java.util.Arrays;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.utils.PODD;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddRepositoryManagerImpl implements PoddRepositoryManager
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private Repository repository;
    
    private URI artifactGraph = PODD.DEFAULT_ARTIFACT_MANAGEMENT_GRAPH;
    
    private URI schemaGraph = PODD.DEFAULT_SCHEMA_MANAGEMENT_GRAPH;
    
    private URI dataRepositoryGraph = PODD.DEFAULT_FILE_REPOSITORY_MANAGEMENT_GRAPH;
    
    /**
     * Default constructor, which sets up an in-memory MemoryStore repository.
     */
    public PoddRepositoryManagerImpl()
    {
        this.repository = new SailRepository(new MemoryStore());
        try
        {
            this.repository.initialize();
        }
        catch(final RepositoryException e)
        {
            try
            {
                this.repository.shutDown();
            }
            catch(final RepositoryException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
            throw new RuntimeException("Could not initialise PoddRepositoryManager with an in-memory repository", e);
        }
    }
    
    /**
     * 
     * @param repository
     *            An initialized implementation of Repository.
     */
    public PoddRepositoryManagerImpl(final Repository repository)
    {
        this.repository = repository;
    }
    
    @Override
    public URI getArtifactManagementGraph()
    {
        return this.artifactGraph;
    }
    
    @Override
    public URI getFileRepositoryManagementGraph()
    {
        return this.dataRepositoryGraph;
    }
    
    @Override
    public Repository getManagementRepository() throws OpenRDFException
    {
        return this.repository;
    }
    
    @Override
    public Repository getNewTemporaryRepository(final Set<? extends OWLOntologyID> ontologies) throws OpenRDFException
    {
        final Repository result = new SailRepository(new MemoryStore());
        result.initialize();
        
        return result;
    }
    
    @Override
    public Repository getPermanentRepository(final Set<? extends OWLOntologyID> ontologies) throws OpenRDFException
    {
        return this.repository;
    }
    
    @Override
    public URI getSchemaManagementGraph()
    {
        return this.schemaGraph;
    }
    
    @Override
    public void setArtifactManagementGraph(final URI artifactManagementGraph)
    {
        this.artifactGraph = artifactManagementGraph;
    }
    
    @Override
    public void setFileRepositoryManagementGraph(final URI dataRepositoryManagementGraph)
    {
        this.dataRepositoryGraph = dataRepositoryManagementGraph;
    }
    
    @Override
    public void setManagementRepository(final Repository repository) throws OpenRDFException
    {
        this.repository = repository;
    }
    
    @Override
    public void setSchemaManagementGraph(final URI schemaManagementGraph)
    {
        this.schemaGraph = schemaManagementGraph;
    }
    
    @Override
    public void shutDown() throws RepositoryException
    {
        if(this.repository != null)
        {
            this.repository.shutDown();
        }
    }
    
    @Override
    public boolean safeContexts(final URI... contexts)
    {
        boolean returnValue = true;
        if(contexts == null)
        {
            returnValue = false;
        }
        else if(contexts.length == 0)
        {
            returnValue = false;
        }
        else
        {
            for(final URI nextContext : contexts)
            {
                if(nextContext == null)
                {
                    returnValue = false;
                }
                else if(nextContext.equals(SESAME.NIL))
                {
                    returnValue = false;
                }
                else if(nextContext.equals(this.getArtifactManagementGraph()))
                {
                    returnValue = false;
                }
                else if(nextContext.equals(this.getSchemaManagementGraph()))
                {
                    returnValue = false;
                }
                else if(nextContext.equals(this.getFileRepositoryManagementGraph()))
                {
                    returnValue = false;
                }
            }
        }
        
        if(!returnValue)
        {
            this.log.warn("Found unsafe URI contexts: <{}>", Arrays.asList(contexts));
        }
        
        return returnValue;
    }
    
}
