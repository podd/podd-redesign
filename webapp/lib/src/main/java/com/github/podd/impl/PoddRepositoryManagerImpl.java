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

import info.aduna.iteration.Iterations;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Graph;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.manager.RepositoryManager;
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
    
    private RepositoryImplConfig permanentRepositoryConfig;
    
    private RepositoryManager sesameRepositoryManager;
    
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
     * @param permanentRepository
     */
    public PoddRepositoryManagerImpl(final Repository managementRepository, final Repository permanentRepository)
    {
        this.managementRepository = managementRepository;
        this.permanentRepositories.put(Collections.<OWLOntologyID> emptySet(), permanentRepository);
    }
    
    /**
     * 
     * @param managementRepository
     * @param permanentRepository
     * @throws RepositoryConfigException
     */
    public PoddRepositoryManagerImpl(final Repository managementRepository, final RepositoryManager repositoryManager,
            final RepositoryImplConfig permanentRepositoryConfig) throws RepositoryConfigException
    {
        this.managementRepository = managementRepository;
        this.sesameRepositoryManager = repositoryManager;
        this.permanentRepositoryConfig = permanentRepositoryConfig;
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
        Objects.requireNonNull(schemaOntologies, "Schema ontologies must not be null");
        
        Repository repository = this.permanentRepositories.get(schemaOntologies);
        // This synchronisation should not inhibit most operations, but is necessary to prevent
        // multiple repositories with the same schema ontologies, given that there is a relatively
        // large latency in the new repository create process
        // ConcurrentMap.putIfAbsent is not applicable to the initial situation as it is very costly
        // to create a repository if it is not needed
        if(repository == null)
        {
            synchronized(this.permanentRepositories)
            {
                repository = this.permanentRepositories.get(schemaOntologies);
                if(repository == null)
                {
                    // Create a new one
                    // Get a new repository ID using our base name as the starting point
                    String newRepositoryID = this.sesameRepositoryManager.getNewRepositoryID("poddredesignrepository");
                    RepositoryConfig config =
                            new RepositoryConfig(newRepositoryID, "PODD Redesign Repository (Automatically created)",
                                    permanentRepositoryConfig);
                    this.sesameRepositoryManager.addRepositoryConfig(config);
                    
                    Repository nextRepository = this.sesameRepositoryManager.getRepository(newRepositoryID);
                    // If we somehow created a new repository since we entered this section, we need
                    // to remove the new repository to cleanup
                    Repository putIfAbsent = this.permanentRepositories.putIfAbsent(schemaOntologies, nextRepository);
                    if(putIfAbsent != null)
                    {
                        this.log.error("Created a duplicate repository that must now be removed: {}", newRepositoryID);
                        boolean removeRepository = this.sesameRepositoryManager.removeRepository(newRepositoryID);
                        if(!removeRepository)
                        {
                            this.log.warn("Could not remove repository");
                        }
                        repository = putIfAbsent;
                    }
                    else
                    {
                        repository = nextRepository;
                        
                        // In this case, we need to copy the relevant schema ontologies over to the
                        // new repository
                        
                        Repository managementRepository = getManagementRepository();
                        RepositoryConnection managementConnection = null;
                        RepositoryConnection repositoryConnection = null;
                        try
                        {
                            repositoryConnection = repository.getConnection();
                            managementConnection = managementRepository.getConnection();
                            for(OWLOntologyID nextSchemaOntology : schemaOntologies)
                            {
                                if(!repositoryConnection.hasStatement(null, null, null, false, nextSchemaOntology
                                        .getVersionIRI().toOpenRDFURI()))
                                {
                                    repositoryConnection.add(managementConnection.getStatements(null, null, null,
                                            false, nextSchemaOntology.getVersionIRI().toOpenRDFURI()));
                                }
                                
                                RepositoryResult<Statement> statements =
                                        managementConnection.getStatements(nextSchemaOntology.getVersionIRI()
                                                .toOpenRDFURI(), PODD.PODD_BASE_INFERRED_VERSION, null, false, this
                                                .getSchemaManagementGraph());
                                
                                for(Statement nextInferredStatement : Iterations.asList(statements))
                                {
                                    if(nextInferredStatement.getObject() instanceof URI)
                                    {
                                        if(!repositoryConnection.hasStatement(null, null, null, false,
                                                (URI)nextInferredStatement.getObject()))
                                        {
                                            repositoryConnection.add(managementConnection.getStatements(null, null,
                                                    null, false, (URI)nextInferredStatement.getObject()));
                                        }
                                    }
                                }
                            }
                            repositoryConnection.commit();
                        }
                        finally
                        {
                            if(managementConnection != null)
                            {
                                managementConnection.close();
                            }
                            if(repositoryConnection != null)
                            {
                                repositoryConnection.close();
                            }
                        }
                    }
                }
            }
        }
        return repository;
    }
    
    @Override
    public URI getSchemaManagementGraph()
    {
        return this.schemaGraph;
    }
    
    @Override
    public void mapPermanentRepository(final Set<? extends OWLOntologyID> schemaOntologies, final Repository repository)
    {
        synchronized(this.permanentRepositories)
        {
            // Override any previous repositories that were there
            final Repository putIfAbsent = this.permanentRepositories.putIfAbsent(schemaOntologies, repository);
            
            // TODO: Shutdown putIfAbsent
            if(putIfAbsent != null)
            {
                this.log.warn("Overriding previous repository for a set of schema ontologies: {}", schemaOntologies);
            }
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
