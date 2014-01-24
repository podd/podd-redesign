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
package com.github.podd.utils.test;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryImplConfigBase;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

/**
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class RepositoryConfigTest
{
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    
    private File testDir;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        testDir = tempDir.newFolder("repositoryconfigtest");
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        testDir = null;
    }
    
    @Test
    public final void testParsingMemoryStore() throws Exception
    {
        final Model graph =
                Rio.parse(this.getClass().getResourceAsStream("/memorystoreconfig.ttl"), "", RDFFormat.TURTLE);
        final Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RepositoryConfigSchema.REPOSITORYTYPE, null);
        // RepositoryConfig repositoryConfig = RepositoryConfig.create(graph, repositoryNode);
        // RepositoryImplConfig repositoryImplConfig = repositoryConfig.getRepositoryImplConfig();
        final RepositoryImplConfig repositoryImplConfig = RepositoryImplConfigBase.create(graph, repositoryNode);
        Assert.assertNotNull(repositoryImplConfig);
        Assert.assertNotNull(repositoryImplConfig.getType());
        final RepositoryFactory repositoryFactory =
                RepositoryRegistry.getInstance().get(repositoryImplConfig.getType());
        
        final Repository repository = repositoryFactory.getRepository(repositoryImplConfig);
        Assert.assertNotNull(repository);
        repository.setDataDir(testDir);
        repository.initialize();
    }
    
    @Test
    public final void testParsingNativeStore() throws Exception
    {
        final Model graph =
                Rio.parse(this.getClass().getResourceAsStream("/nativestoreconfig.ttl"), "", RDFFormat.TURTLE);
        final Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RepositoryConfigSchema.REPOSITORYTYPE, null);
        final RepositoryImplConfig repositoryImplConfig = RepositoryImplConfigBase.create(graph, repositoryNode);
        Assert.assertNotNull(repositoryImplConfig);
        Assert.assertNotNull(repositoryImplConfig.getType());
        final RepositoryFactory repositoryFactory =
                RepositoryRegistry.getInstance().get(repositoryImplConfig.getType());
        
        final Repository repository = repositoryFactory.getRepository(repositoryImplConfig);
        Assert.assertNotNull(repository);
        repository.setDataDir(testDir);
        repository.initialize();
    }
}
