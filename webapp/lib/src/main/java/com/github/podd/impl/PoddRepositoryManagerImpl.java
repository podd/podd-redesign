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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Graph;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
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
    private URI artifactGraph = PODD.DEFAULT_ARTIFACT_MANAGEMENT_GRAPH;
    
    private URI dataRepositoryGraph = PODD.DEFAULT_FILE_REPOSITORY_MANAGEMENT_GRAPH;
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private Repository managementRepository;
    
    private ConcurrentMap<Set<? extends OWLOntologyID>, Repository> permanentRepositories = new ConcurrentHashMap<>();
    
    private URI schemaGraph = PODD.DEFAULT_SCHEMA_MANAGEMENT_GRAPH;
    
    private String permanentRepositoryType;
    
    // /**
    // * Default constructor, which sets up an in-memory MemoryStore repository.
    // */
    // public PoddRepositoryManagerImpl()
    // {
    // this.managementRepository = new SailRepository(new MemoryStore());
    // try
    // {
    // this.managementRepository.initialize();
    // // TODO: Use a non-stub mapping here
    // this.permanentRepositories.put(Collections.<OWLOntologyID> emptySet(),
    // this.managementRepository);
    // }
    // catch(final RepositoryException e)
    // {
    // try
    // {
    // this.managementRepository.shutDown();
    // }
    // catch(final RepositoryException e1)
    // {
    // // TODO Auto-generated catch block
    // e1.printStackTrace();
    // }
    //
    // throw new
    // RuntimeException("Could not initialise PoddRepositoryManager with an in-memory repository",
    // e);
    // }
    // }
    //
    /**
     * 
     * @param managementRepository
     *            An initialized implementation of Repository.
     * @throws OpenRDFException
     * @throws IOException
     * @throws UnsupportedRDFormatException
     */
    public PoddRepositoryManagerImpl(final Repository managementRepository, final String permanentRepositoryType,
            final String permanentRepositoryConfigClasspath, final URI permanentRepositoryConfigURI)
        throws OpenRDFException, UnsupportedRDFormatException, IOException
    {
        this.managementRepository = managementRepository;
        this.permanentRepositoryType = permanentRepositoryType;
        RepositoryFactory repositoryFactory = RepositoryRegistry.getInstance().get(permanentRepositoryType);
        RepositoryImplConfig config = repositoryFactory.getConfig();
        Model configGraph =
                Rio.parse(this.getClass().getResourceAsStream(permanentRepositoryConfigClasspath), "", RDFFormat.TURTLE);
        config.parse(configGraph, permanentRepositoryConfigURI);
        // TODO: Use a non-stub mapping here
        this.permanentRepositories.put(Collections.<OWLOntologyID> emptySet(), repositoryFactory.getRepository(config));
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
        return this.managementRepository;
    }
    
    @Override
    public Repository getNewTemporaryRepository(final Set<? extends OWLOntologyID> schemaOntologies)
        throws OpenRDFException
    {
        final Repository result = new SailRepository(new MemoryStore());
        result.initialize();
        
        return result;
    }
    
    @Override
    public Repository getPermanentRepository(final Set<? extends OWLOntologyID> schemaOntologies)
        throws OpenRDFException
    {
        Collection<String> allRepositoryTypes = RepositoryRegistry.getInstance().getKeys();
        
        this.log.error("{}", allRepositoryTypes);
        
        for(Entry<Set<? extends OWLOntologyID>, Repository> nextRepository : permanentRepositories.entrySet())
        {
            // TODO: Use a non-stub mapping here
            return this.permanentRepositories.get(Collections.<OWLOntologyID> emptySet());
        }
        
        throw new RuntimeException("TODO: Complete implementation");
    }
    
    @Override
    public URI getSchemaManagementGraph()
    {
        return this.schemaGraph;
    }
    
    @Override
    public void mapPermanentRepository(final Set<? extends OWLOntologyID> schemaOntologies, final Repository repository)
    {
        // Override any previous repositories that were there
        final Repository putIfAbsent = this.permanentRepositories.putIfAbsent(schemaOntologies, repository);
        
        // TODO: Shutdown putIfAbsent
        if(putIfAbsent != null)
        {
            this.log.warn("Overriding previous repository for a set of schema ontologies: {}", schemaOntologies);
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
        this.managementRepository = repository;
    }
    
    @Override
    public void setSchemaManagementGraph(final URI schemaManagementGraph)
    {
        this.schemaGraph = schemaManagementGraph;
    }
    
    @Override
    public void shutDown() throws RepositoryException
    {
        if(this.managementRepository != null)
        {
            this.managementRepository.shutDown();
        }
    }
    
}
