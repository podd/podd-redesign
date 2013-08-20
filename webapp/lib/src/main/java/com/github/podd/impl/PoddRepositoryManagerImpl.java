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
package com.github.podd.impl;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddRepositoryManagerImpl implements PoddRepositoryManager
{
    
    private Repository repository;
    
    private URI artifactGraph = PoddRdfConstants.DEFAULT_ARTIFACT_MANAGEMENT_GRAPH;
    
    private URI schemaGraph = PoddRdfConstants.DEFAULT_SCHEMA_MANAGEMENT_GRAPH;
    
    private URI dataRepositoryGraph = PoddRdfConstants.DEFAULT_FILE_REPOSITORY_MANAGEMENT_GRAPH;
    
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
            throw new RuntimeException("Could not initialise PoddRepositoryManager with an in-memory repository");
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
    public Repository getNewTemporaryRepository() throws OpenRDFException
    {
        final Repository result = new SailRepository(new MemoryStore());
        result.initialize();
        
        return result;
    }
    
    @Override
    public Repository getRepository() throws OpenRDFException
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
    public void setRepository(final Repository repository) throws OpenRDFException
    {
        this.repository = repository;
    }
    
    @Override
    public void setSchemaManagementGraph(final URI schemaManagementGraph)
    {
        this.schemaGraph = schemaManagementGraph;
    }
    
}
