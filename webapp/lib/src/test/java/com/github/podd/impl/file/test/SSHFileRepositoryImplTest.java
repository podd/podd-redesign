/**
 * 
 */
package com.github.podd.impl.file.test;

import org.junit.Assert;
import org.junit.Test;
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
import com.github.podd.exception.IncompleteFileRepositoryException;
import com.github.podd.impl.file.SSHFileReferenceImpl;
import com.github.podd.impl.file.SSHFileRepositoryImpl;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author kutila
 * 
 */
public class SSHFileRepositoryImplTest extends AbstractPoddFileRepositoryTest<SSHFileReference>
{
    /*
     * Create a {@link Model} containing configuration details for an SSH File Repository.
     * 
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.file.test.AbstractPoddFileRepositoryTest#getNewPoddFileRepository()
     */
    @Override
    protected PoddFileRepository<SSHFileReference> getNewPoddFileRepository()
    {
        PoddFileRepository result = null;
        
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
        
        try
        {
            result = new SSHFileRepositoryImpl<SSHFileReference>(model);
        }
        catch(final IncompleteFileRepositoryException e)
        {
            Assert.fail("Failed to create an SSHFileRepositoryImpl instance: " + e.getMessage());
        }
        return result;
    }
    
    @Test
    public void testCreateFileRepositoryMissingProtocol() throws Exception
    {
        final Model model = new LinkedHashModel();
        final URI aliasUri = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/test_alias");
        model.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_BASE_ALIAS, new LiteralImpl("test_alias")));
        model.add(new StatementImpl(aliasUri, RDF.TYPE, PoddRdfConstants.PODD_FILE_REPOSITORY));
        model.add(new StatementImpl(aliasUri, RDF.TYPE, PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
        
        model.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_HOST, new LiteralImpl("localhost")));
        model.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, new LiteralImpl("9856")));
        model.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, new LiteralImpl(
                "ce:a7:c1:cf:17:3f:96:49:6a:53:1a:05:0b:ba:90:db")));
        model.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, new LiteralImpl("salt")));
        model.add(new StatementImpl(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, new LiteralImpl("salt")));
        
        try
        {
            new SSHFileRepositoryImpl<SSHFileReference>(model);
            Assert.fail("Should have thrown an IncompleteFileRepositoryException");
        }
        catch(final IncompleteFileRepositoryException e)
        {
            Assert.assertNotNull(e.getModel());
            Assert.assertEquals("SSH repository configuration incomplete", e.getMessage());
        }
    }
    
    @Test
    public void testCanHandle() throws Exception
    {
        final SSHFileReference testFileRef = new SSHFileReferenceImpl();
        testFileRef.setRepositoryAlias("test_alias");
        
        Assert.assertTrue("Repository should be able to handle this file reference",
                this.testFileRepository.canHandle(testFileRef));
    }
    
    @Test
    public void testCanHandleWithDifferentAliases() throws Exception
    {
        final SSHFileReference testFileRef = new SSHFileReferenceImpl();
        testFileRef.setRepositoryAlias("some_other_alias");
        
        Assert.assertFalse("Repository should not be able to handle this file reference",
                this.testFileRepository.canHandle(testFileRef));
    }
    
    @Test
    public void testCanHandleWithNullReference() throws Exception
    {
        Assert.assertFalse("Repository should not be able to handle NULL file reference",
                this.testFileRepository.canHandle(null));
    }
    
    @Test
    public void testValidate() throws Exception
    {
        // TODO: implement me
    }
    
}
