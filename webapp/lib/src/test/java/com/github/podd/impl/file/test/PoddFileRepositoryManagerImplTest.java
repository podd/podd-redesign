/**
 * 
 */
package com.github.podd.impl.file.test;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.file.PoddFileRepositoryManager;
import com.github.podd.api.file.test.AbstractPoddFileRepositoryManagerTest;
import com.github.podd.impl.PoddRepositoryManagerImpl;
import com.github.podd.impl.file.PoddFileRepositoryManagerImpl;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author kutila
 */
public class PoddFileRepositoryManagerImplTest extends AbstractPoddFileRepositoryManagerTest
{
    
    @Override
    protected PoddFileRepositoryManager getNewPoddFileRepositoryManager() throws OpenRDFException
    {
        // create a Repository Manager with an internal memory Repository
        Repository testRepository = new SailRepository(new MemoryStore());
        testRepository.initialize();

        final PoddRepositoryManager repositoryManager = new PoddRepositoryManagerImpl();
        repositoryManager.setRepository(testRepository);
        repositoryManager.setFileRepositoryManagementGraph(PoddRdfConstants.DEFAULT_FILE_REPOSITORY_MANAGEMENT_GRAPH);

        
        // create the PoddFileRepositoryManager for testing
        PoddFileRepositoryManager testFileRepositoryManager = new PoddFileRepositoryManagerImpl();
        testFileRepositoryManager.setRepositoryManager(repositoryManager);
        
        return testFileRepositoryManager;
    }
    
    @Override
    protected void populateFileRepositoryManagementGraph() throws OpenRDFException
    {
        RepositoryConnection conn = super.testRepositoryManager.getRepository().getConnection();
        try
        {
            conn.begin();
            
            URI context = super.testRepositoryManager.getFileRepositoryManagementGraph();
            
            // alias 1
            final URI alias1Uri = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/alias/1-alpha");
            conn.add(alias1Uri, RDF.TYPE, PoddRdfConstants.PODD_FILE_REPOSITORY, context);
            conn.add(alias1Uri, RDF.TYPE, PoddRdfConstants.PODD_SSH_FILE_REPOSITORY, context);
            
            conn.add(alias1Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance()
                    .createLiteral(TEST_ALIAS_1A), context);
            conn.add(alias1Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance()
                    .createLiteral(TEST_ALIAS_1B), context);
            
            conn.add(alias1Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_PROTOCOL, ValueFactoryImpl.getInstance()
                    .createLiteral("SSH"), context);
            conn.add(alias1Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_HOST, ValueFactoryImpl.getInstance()
                    .createLiteral("localhost"), context);
            conn.add(alias1Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, ValueFactoryImpl.getInstance()
                    .createLiteral("9856"), context);
            conn.add(alias1Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, ValueFactoryImpl.getInstance()
                    .createLiteral("ce:a7:c1:cf:17:3f:96:49:6a:53:1a:05:0b:ba:90:db"), context);
            conn.add(alias1Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, ValueFactoryImpl.getInstance()
                    .createLiteral("salt"), context);
            conn.add(alias1Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, ValueFactoryImpl.getInstance()
                    .createLiteral("salt"), context);            
            
            
            // alias 2
            final URI alias2Uri = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/alias/2-beta");
            conn.add(alias2Uri, RDF.TYPE, PoddRdfConstants.PODD_FILE_REPOSITORY, context);
            conn.add(alias2Uri, RDF.TYPE, PoddRdfConstants.PODD_SSH_FILE_REPOSITORY, context);
            
            conn.add(alias2Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance()
                    .createLiteral(TEST_ALIAS_2A), context);
            conn.add(alias2Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance()
                    .createLiteral(TEST_ALIAS_2B), context);
            
            conn.add(alias2Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_PROTOCOL, ValueFactoryImpl.getInstance()
                    .createLiteral("SSH"), context);
            conn.add(alias2Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_HOST, ValueFactoryImpl.getInstance()
                    .createLiteral("localhost"), context);
            conn.add(alias2Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, ValueFactoryImpl.getInstance()
                    .createLiteral("9856"), context);
            conn.add(alias2Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, ValueFactoryImpl.getInstance()
                    .createLiteral("ce:a7:c1:cf:17:3f:96:49:6a:53:1a:05:0b:ba:90:db"), context);
            conn.add(alias2Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, ValueFactoryImpl.getInstance()
                    .createLiteral("salt"), context);
            conn.add(alias2Uri, PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, ValueFactoryImpl.getInstance()
                    .createLiteral("salt"), context);            
            
            
            
            
            conn.commit();
        }
        finally
        {
            conn.close();
        }
    }
    
}
