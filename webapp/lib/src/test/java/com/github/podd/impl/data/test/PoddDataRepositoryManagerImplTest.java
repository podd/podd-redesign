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
package com.github.podd.impl.data.test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryImplConfigBase;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.data.DataReference;
import com.github.podd.api.data.test.AbstractPoddDataRepositoryManagerTest;
import com.github.podd.api.file.PoddDataRepository;
import com.github.podd.api.file.PoddDataRepositoryManager;
import com.github.podd.exception.DataReferenceNotSupportedException;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddRepositoryManagerImpl;
import com.github.podd.impl.data.PoddFileRepositoryManagerImpl;
import com.github.podd.impl.file.test.SSHService;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddWebConstants;

/**
 * This concrete test class uses SSH File References and a test SSH file repository to run through
 * the abstract PoddDataRepositoryManager tests.
 * 
 * @author kutila
 */
public class PoddDataRepositoryManagerImplTest extends AbstractPoddDataRepositoryManagerTest
{
    @Rule
    public Timeout timeout = new Timeout(30000);
    
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    
    /** SSH File Repository server for tests */
    protected SSHService sshd;
    
    private Path sshDir = null;
    
    @Override
    protected PoddDataRepository<?> buildDataRepositoryInstance(final String alias, final Model model)
    {
        // prepare: create a mock PoddDataRepository which can only return the
        // test alias string
        return new PoddDataRepository<DataReference>()
            {
                
                @Override
                public boolean canHandle(final DataReference reference)
                {
                    return false;
                }
                
                @Override
                public String getAlias()
                {
                    return alias;
                }
                
                @Override
                public Model getAsModel()
                {
                    return model;
                }
                
                @Override
                public Set<URI> getTypes()
                {
                    return null;
                }
                
                @Override
                public boolean validate(final DataReference reference) throws DataReferenceNotSupportedException,
                    IOException
                {
                    return false;
                }
            };
    }
    
    @Override
    protected Model buildModelForDataRepository(final URI aliasUri, final String... aliases)
    {
        final Model model = new LinkedHashModel();
        for(final String alias : aliases)
        {
            model.add(aliasUri, PODD.PODD_DATA_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(alias));
        }
        model.add(aliasUri, RDF.TYPE, PODD.PODD_DATA_REPOSITORY);
        
        // SSH implementation specific configurations
        model.add(aliasUri, RDF.TYPE, PODD.PODD_SSH_FILE_REPOSITORY);
        model.add(aliasUri, PODD.PODD_DATA_REPOSITORY_PROTOCOL,
                ValueFactoryImpl.getInstance().createLiteral(PoddDataRepository.PROTOCOL_SSH));
        model.add(aliasUri, PODD.PODD_DATA_REPOSITORY_HOST,
                ValueFactoryImpl.getInstance().createLiteral(SSHService.TEST_SSH_HOST));
        model.add(aliasUri, PODD.PODD_DATA_REPOSITORY_PORT,
                ValueFactoryImpl.getInstance().createLiteral(this.sshd.TEST_SSH_SERVICE_PORT));
        model.add(aliasUri, PODD.PODD_FILE_REPOSITORY_FINGERPRINT,
                ValueFactoryImpl.getInstance().createLiteral(SSHService.TEST_SSH_FINGERPRINT));
        model.add(aliasUri, PODD.PODD_FILE_REPOSITORY_USERNAME,
                ValueFactoryImpl.getInstance().createLiteral(SSHService.TEST_SSH_USERNAME));
        model.add(aliasUri, PODD.PODD_FILE_REPOSITORY_SECRET,
                ValueFactoryImpl.getInstance().createLiteral(SSHService.TEST_SSH_SECRET));
        
        return model;
    }
    
    @Override
    protected DataReference getNewInvalidDataReference() throws Exception
    {
        return SSHService
                .getNewInvalidFileReference("invalid-file",
                        this.tempDir.newFolder("poddfilerepositoryimpltest-resources-" + UUID.randomUUID().toString())
                                .toPath());
    }
    
    @Override
    protected PoddDataRepositoryManager getNewPoddDataRepositoryManager() throws Exception
    {
        // create a Repository Manager with an internal memory Repository
        final Repository managementRepository = new SailRepository(new MemoryStore());
        managementRepository.initialize();
        
        final Model graph =
                Rio.parse(this.getClass().getResourceAsStream("/memorystoreconfig.ttl"), "", RDFFormat.TURTLE);
        final Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RepositoryConfigSchema.REPOSITORYTYPE, null);
        final RepositoryImplConfig repositoryImplConfig = RepositoryImplConfigBase.create(graph, repositoryNode);
        Assert.assertNotNull(repositoryImplConfig);
        Assert.assertNotNull(repositoryImplConfig.getType());
        final PoddRepositoryManagerImpl repositoryManagerImpl =
                new PoddRepositoryManagerImpl(managementRepository, repositoryImplConfig, "", this.tempDir.newFolder(
                        "test-podd-repository-manager").toPath(), new PropertyUtil("podd"));
        
        final PoddOWLManager owlManager =
                new PoddOWLManagerImpl(this.getNewOWLOntologyManagerFactory(), this.getNewReasonerFactory());
        
        // create the PoddDataRepositoryManager for testing
        final PoddDataRepositoryManager testFileRepositoryManager = new PoddFileRepositoryManagerImpl();
        testFileRepositoryManager.setRepositoryManager(repositoryManagerImpl);
        testFileRepositoryManager.setOWLManager(owlManager);
        
        return testFileRepositoryManager;
    }
    
    protected OWLOntologyManagerFactory getNewOWLOntologyManagerFactory()
    {
        final Collection<OWLOntologyManagerFactory> ontologyManagers =
                OWLOntologyManagerFactoryRegistry.getInstance().get(PoddWebConstants.DEFAULT_OWLAPI_MANAGER);
        
        if(ontologyManagers == null || ontologyManagers.isEmpty())
        {
            this.log.error("OWLOntologyManagerFactory was not found");
        }
        return ontologyManagers.iterator().next();
    }
    
    protected OWLReasonerFactory getNewReasonerFactory()
    {
        return OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet");
    }
    
    @Override
    protected DataReference getNewValidDataReference() throws Exception
    {
        return SSHService
                .getNewValidFileReference("valid-file",
                        this.tempDir.newFolder("poddfilerepositoryimpltest-resources-" + UUID.randomUUID().toString())
                                .toPath());
    }
    
    @Before
    @Override
    public void setUp() throws Exception
    {
        this.sshDir = this.tempDir.newFolder("podd-filerepository-manager-impl-test").toPath();
        this.sshd = new SSHService();
        this.sshd.startTestSSHServer(this.sshDir);
        super.setUp();
    }
    
    @Override
    protected void startRepositorySource() throws Exception
    {
    }
    
    @Override
    protected void stopRepositorySource() throws Exception
    {
        if(this.sshd != null)
        {
            this.sshd.stopTestSSHServer(this.sshDir);
        }
    }
    
}
