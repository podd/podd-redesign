/**
 * 
 */
package com.github.podd.impl.file.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.api.file.SSHFileReference;
import com.github.podd.api.file.test.AbstractPoddFileRepositoryTest;
import com.github.podd.impl.file.SSHFileReferenceImpl;
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
    
    /*
     * Create a {@link Model} containing configuration details for an SSH File Repository.
     * 
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.file.test.AbstractPoddFileRepositoryTest#getNewPoddFileRepository()
     */
    @Override
    protected PoddFileRepository<SSHFileReference> getNewPoddFileRepository() throws Exception
    {
        final Model model = new LinkedHashModel();
        final URI aliasUri = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/test_alias");
        model.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_BASE_ALIAS, new LiteralImpl("test_alias")));
        model.add(new StatementImpl(aliasUri, RDF.TYPE, PoddRdfConstants.PODD_FILE_REPOSITORY));
        model.add(new StatementImpl(aliasUri, RDF.TYPE, PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
        
        // ssh specific attributes
        model.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_PROTOCOL, new LiteralImpl(
                SSHFileRepositoryImpl.PROTOCOL_SSH)));
        model.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_HOST, new LiteralImpl("localhost")));
        model.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, new LiteralImpl("9856")));
        model.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, new LiteralImpl(
                "ce:a7:c1:cf:17:3f:96:49:6a:53:1a:05:0b:ba:90:db")));
        model.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, new LiteralImpl("salt")));
        model.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, new LiteralImpl("salt")));
        
        return this.getNewPoddFileRepository(model);
    }
    
    @Override
    protected PoddFileRepository<SSHFileReference> getNewPoddFileRepository(final Model model) throws Exception
    {
        final PoddFileRepository result = new SSHFileRepositoryImpl(model);
        return result;
    }
    
    @Override
    protected List<Model> getIncompleteModels()
    {
        final List<Model> incompleteModels = new ArrayList<Model>();
        
        // - no "protocol"
        final Model model1 = new LinkedHashModel();
        final URI aliasUri = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/test_alias");
        model1.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_BASE_ALIAS, new LiteralImpl("test_alias")));
        model1.add(new StatementImpl(aliasUri, RDF.TYPE, PoddRdfConstants.PODD_FILE_REPOSITORY));
        model1.add(new StatementImpl(aliasUri, RDF.TYPE, PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
        
        model1.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_HOST, new LiteralImpl("localhost")));
        model1.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, new LiteralImpl("9856")));
        model1.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, new LiteralImpl(
                "ce:a7:c1:cf:17:3f:96:49:6a:53:1a:05:0b:ba:90:db")));
        model1.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, new LiteralImpl("salt")));
        model1.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, new LiteralImpl("salt")));
        
        incompleteModels.add(model1);
        
        // - no "host"
        final Model model2 = new LinkedHashModel();
        model2.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_BASE_ALIAS, new LiteralImpl("test_alias")));
        model2.add(new StatementImpl(aliasUri, RDF.TYPE, PoddRdfConstants.PODD_FILE_REPOSITORY));
        model2.add(new StatementImpl(aliasUri, RDF.TYPE, PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
        
        model2.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_PROTOCOL, new LiteralImpl("ssh")));
        model2.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, new LiteralImpl("9856")));
        model2.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, new LiteralImpl(
                "ce:a7:c1:cf:17:3f:96:49:6a:53:1a:05:0b:ba:90:db")));
        model2.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, new LiteralImpl("salt")));
        model2.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, new LiteralImpl("salt")));
        
        incompleteModels.add(model2);
        
        // - no "fingerprint"
        final Model model3 = new LinkedHashModel();
        model3.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_BASE_ALIAS, new LiteralImpl("test_alias")));
        model3.add(new StatementImpl(aliasUri, RDF.TYPE, PoddRdfConstants.PODD_FILE_REPOSITORY));
        model3.add(new StatementImpl(aliasUri, RDF.TYPE, PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
        
        model3.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_PROTOCOL, new LiteralImpl("ssh")));
        model3.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_HOST, new LiteralImpl("localhost")));
        model3.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, new LiteralImpl("9856")));
        model3.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, new LiteralImpl("salt")));
        model3.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, new LiteralImpl("salt")));
        
        incompleteModels.add(model3);
        
        // - no protocol, host, port, fingerprint, username, secret
        final Model model4 = new LinkedHashModel();
        model4.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_BASE_ALIAS, new LiteralImpl("test_alias")));
        model4.add(new StatementImpl(aliasUri, RDF.TYPE, PoddRdfConstants.PODD_FILE_REPOSITORY));
        model4.add(new StatementImpl(aliasUri, RDF.TYPE, PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
        
        incompleteModels.add(model4);
        return incompleteModels;
    }
    
    @Override
    protected SSHFileReference getNewFileReference(final String alias)
    {
        final SSHFileReference testFileRef = new SSHFileReferenceImpl();
        testFileRef.setRepositoryAlias(alias);
        return testFileRef;
    }
    
    /**
     * This test starts up an internal SSH server. If the specified port is unavailable, the test
     * will fail.
     * 
     * TODO - in progress
     * 
     * @throws Exception
     */
    @Test
    public void testValidateWithExistingFile() throws Exception
    {
        final SSHService sshd = new SSHService();
        try
        {
            sshd.startTestSSHServer(9856, this.tempDirectory.newFolder());
            
            final SSHFileReference fileReference = new SSHFileReferenceImpl();
            fileReference.setRepositoryAlias("test_alias");
            fileReference.setFilename("basic-1.rdf");
            fileReference.setPath("src/test/resources/test/artifacts"); // XXX: switch to use a
                                                                        // relative path
            
            Assert.assertTrue("File Reference should have been valid", this.testFileRepository.validate(fileReference));
        }
        finally
        {
            sshd.stopTestSSHServer();
        }
    }
    
}
