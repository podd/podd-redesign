/**
 * 
 */
package com.github.podd.impl.file.test;

import java.io.IOException;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.api.file.PoddFileRepositoryManager;
import com.github.podd.api.file.test.AbstractPoddFileRepositoryManagerTest;
import com.github.podd.exception.FileReferenceNotSupportedException;
import com.github.podd.impl.PoddRepositoryManagerImpl;
import com.github.podd.impl.file.PoddFileRepositoryManagerImpl;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author kutila
 */
public class PoddFileRepositoryManagerImplTest extends AbstractPoddFileRepositoryManagerTest
{
    
    @Override
    protected PoddFileRepository<?> buildFileRepositoryInstance(final String alias, final Model model)
    {
        // prepare: create a mock PoddFileRepository which can only return the test alias string
        return new PoddFileRepository<FileReference>()
            {
                
                @Override
                public String getAlias()
                {
                    return alias;
                }
                
                @Override
                public Set<URI> getTypes()
                {
                    return null;
                }
                
                @Override
                public boolean validate(final FileReference reference) throws FileReferenceNotSupportedException,
                    IOException
                {
                    return false;
                }
                
                @Override
                public boolean canHandle(final FileReference reference)
                {
                    return false;
                }
                
                @Override
                public Model getAsModel()
                {
                    return model;
                }
            };
    }
    
    @Override
    protected Model buildModelForFileRepository(final URI aliasUri, final String... aliases)
    {
        final Model model = new LinkedHashModel();
        for(final String alias : aliases)
        {
            model.add(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance()
                    .createLiteral(alias));
        }
        model.add(aliasUri, RDF.TYPE, PoddRdfConstants.PODD_FILE_REPOSITORY);
        
        // SSH implementation specific configurations
        model.add(aliasUri, RDF.TYPE, PoddRdfConstants.PODD_SSH_FILE_REPOSITORY);
        model.add(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_PROTOCOL, ValueFactoryImpl.getInstance()
                .createLiteral("SSH"));
        model.add(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_HOST,
                ValueFactoryImpl.getInstance().createLiteral("localhost"));
        model.add(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_PORT,
                ValueFactoryImpl.getInstance().createLiteral("9856"));
        model.add(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, ValueFactoryImpl.getInstance()
                .createLiteral("ce:a7:c1:cf:17:3f:96:49:6a:53:1a:05:0b:ba:90:db"));
        model.add(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, ValueFactoryImpl.getInstance()
                .createLiteral("salt"));
        model.add(aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET,
                ValueFactoryImpl.getInstance().createLiteral("salt"));
        
        return model;
    }
    
    @Override
    protected PoddFileRepositoryManager getNewPoddFileRepositoryManager() throws OpenRDFException
    {
        // create a Repository Manager with an internal memory Repository
        final Repository testRepository = new SailRepository(new MemoryStore());
        testRepository.initialize();
        
        final PoddRepositoryManager repositoryManager = new PoddRepositoryManagerImpl();
        repositoryManager.setRepository(testRepository);
        repositoryManager.setFileRepositoryManagementGraph(PoddRdfConstants.DEFAULT_FILE_REPOSITORY_MANAGEMENT_GRAPH);
        
        // create the PoddFileRepositoryManager for testing
        final PoddFileRepositoryManager testFileRepositoryManager = new PoddFileRepositoryManagerImpl();
        testFileRepositoryManager.setRepositoryManager(repositoryManager);
        
        return testFileRepositoryManager;
    }
    
}
