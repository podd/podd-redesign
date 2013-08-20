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
 /**
 * 
 */
package com.github.podd.impl.file.test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

import com.github.podd.api.file.PoddDataRepository;
import com.github.podd.api.file.SSHFileReference;
import com.github.podd.api.file.test.AbstractPoddFileRepositoryTest;
import com.github.podd.impl.file.SSHFileRepositoryImpl;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author kutila
 * 
 */
public class SSHFileRepositoryImplTest extends AbstractPoddFileRepositoryTest<SSHFileReference>
{
    
    @Rule
    public final TemporaryFolder tempDirectory = new TemporaryFolder();
    
    /** SSH File Repository server for tests */
    protected SSHService sshd;
    
    private Path sshDir = null;
    
    @Override
    protected Collection<URI> getExpectedTypes() throws Exception
    {
        final Collection<URI> types = new ArrayList<URI>();
        types.add(PoddRdfConstants.PODD_DATA_REPOSITORY);
        types.add(PoddRdfConstants.PODD_SSH_FILE_REPOSITORY);
        return types;
    }
    
    @Override
    protected Collection<Model> getIncompleteModels()
    {
        final Collection<Model> incompleteModels = new ArrayList<Model>();
        
        // - no "protocol"
        final Model model1 = new LinkedHashModel();
        model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(
                        this.getAliasGood())));
        model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_DATA_REPOSITORY));
        model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
        
        model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_HOST, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_HOST)));
        model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_PORT, ValueFactoryImpl.getInstance().createLiteral(
                        this.sshd.TEST_SSH_SERVICE_PORT)));
        model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_FINGERPRINT)));
        model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_USERNAME)));
        model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_SECRET)));
        
        incompleteModels.add(model1);
        
        // - no "host"
        final Model model2 = new LinkedHashModel();
        model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(
                        this.getAliasGood())));
        model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_DATA_REPOSITORY));
        model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
        
        model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_PROTOCOL, ValueFactoryImpl.getInstance().createLiteral(
                        PoddDataRepository.PROTOCOL_SSH)));
        model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_PORT, ValueFactoryImpl.getInstance().createLiteral(
                        this.sshd.TEST_SSH_SERVICE_PORT)));
        model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_FINGERPRINT)));
        model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_USERNAME)));
        model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_SECRET)));
        
        incompleteModels.add(model2);
        
        // - no "fingerprint"
        final Model model3 = new LinkedHashModel();
        model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(
                        this.getAliasGood())));
        model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_DATA_REPOSITORY));
        model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
        
        model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_PROTOCOL, ValueFactoryImpl.getInstance().createLiteral(
                        PoddDataRepository.PROTOCOL_SSH)));
        model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_HOST, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_HOST)));
        model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_PORT, ValueFactoryImpl.getInstance().createLiteral(
                        this.sshd.TEST_SSH_SERVICE_PORT)));
        model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_USERNAME)));
        model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_SECRET)));
        
        incompleteModels.add(model3);
        
        // - no protocol, host, port, fingerprint, username, secret
        final Model model4 = new LinkedHashModel();
        model4.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(
                        this.getAliasGood())));
        model4.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_DATA_REPOSITORY));
        model4.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
        
        incompleteModels.add(model4);
        return incompleteModels;
    }
    
    @Override
    protected SSHFileReference getNewValidatingDataReference() throws Exception
    {
        return SSHService.getNewValidFileReference("validatingfile",
                this.tempDirectory.newFolder("sshfilerepositoryimpltest-resources-" + UUID.randomUUID().toString())
                        .toPath());
    }
    
    @Override
    protected SSHFileReference getNewNonValidatingDataReference() throws Exception
    {
        return SSHService.getNewInvalidFileReference("non-validatingfile",
                this.tempDirectory.newFolder("sshfilerepositoryimpltest-resources-" + UUID.randomUUID().toString())
                        .toPath());
    }
    
    /*
     * Create a {@link Model} containing configuration details for an SSH File Repository.
     * 
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.file.test.AbstractPoddFileRepositoryTest#getNewPoddFileRepository()
     */
    @Override
    protected PoddDataRepository<SSHFileReference> getNewPoddFileRepository() throws Exception
    {
        final Model model = new LinkedHashModel();
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(
                        this.getAliasGood())));
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_DATA_REPOSITORY));
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
        
        // ssh specific attributes
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_PROTOCOL, ValueFactoryImpl.getInstance().createLiteral(
                        PoddDataRepository.PROTOCOL_SSH)));
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_HOST, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_HOST)));
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_PORT, ValueFactoryImpl.getInstance().createLiteral(
                        this.sshd.TEST_SSH_SERVICE_PORT)));
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_FINGERPRINT)));
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_USERNAME)));
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_SECRET)));
        
        return this.getNewPoddDataRepository(model);
    }
    
    @Override
    protected PoddDataRepository<SSHFileReference> getNewPoddDataRepository(final Model model) throws Exception
    {
        final PoddDataRepository result = new SSHFileRepositoryImpl(model);
        return result;
    }
    
    @Before
    @Override
    public void setUp() throws Exception
    {
        this.sshDir = this.tempDirectory.newFolder("podd-filerepository-manager-impl-test").toPath();
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
