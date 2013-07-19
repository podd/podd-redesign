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
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

import com.github.podd.api.file.DataReference;
import com.github.podd.api.file.PoddDataRepository;
import com.github.podd.api.file.SPARQLDataReference;
import com.github.podd.api.file.test.AbstractPoddFileRepositoryTest;
import com.github.podd.impl.file.SPARQLDataReferenceImpl;
import com.github.podd.impl.file.SPARQLDataRepositoryImpl;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author kutila
 * 
 */
public class SPARQLDataRepositoryImplTest extends AbstractPoddFileRepositoryTest<SPARQLDataReference>
{
    
    @Rule
    public final TemporaryFolder tempDirectory = new TemporaryFolder();
    
    private Path sshDir = null;
    
    @Override
    protected Collection<URI> getExpectedTypes() throws Exception
    {
        final Collection<URI> types = new ArrayList<URI>();
        types.add(PoddRdfConstants.PODD_DATA_REPOSITORY);
        types.add(PoddRdfConstants.PODD_SPARQL_DATA_REPOSITORY);
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
                PoddRdfConstants.PODD_SPARQL_DATA_REPOSITORY));
        
        model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_HOST, ValueFactoryImpl.getInstance().createLiteral("localhost")));
        model1.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_PORT, ValueFactoryImpl.getInstance().createLiteral(12345)));
        
        incompleteModels.add(model1);
        
        // - no "host"
        final Model model2 = new LinkedHashModel();
        model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(
                        this.getAliasGood())));
        model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_DATA_REPOSITORY));
        model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_SPARQL_DATA_REPOSITORY));
        
        model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_PROTOCOL, ValueFactoryImpl.getInstance().createLiteral(
                        PoddDataRepository.PROTOCOL_HTTP)));
        model2.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_PORT, ValueFactoryImpl.getInstance().createLiteral(12345)));
        
        incompleteModels.add(model2);
        
        // - no "port"
        final Model model3 = new LinkedHashModel();
        model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(
                        this.getAliasGood())));
        model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_DATA_REPOSITORY));
        model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_SPARQL_DATA_REPOSITORY));
        
        model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_PROTOCOL, ValueFactoryImpl.getInstance().createLiteral(
                        PoddDataRepository.PROTOCOL_HTTP)));
        model3.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_HOST, ValueFactoryImpl.getInstance().createLiteral("localhost")));
        
        incompleteModels.add(model3);
        
        // - no protocol, host, port
        final Model model4 = new LinkedHashModel();
        model4.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(
                        this.getAliasGood())));
        model4.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_DATA_REPOSITORY));
        model4.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_SPARQL_DATA_REPOSITORY));
        
        incompleteModels.add(model4);
        return incompleteModels;
    }
    
    @Override
    protected SPARQLDataReference getNewValidatingDataReference()
    {
        return new SPARQLDataReferenceImpl();
    }
    
    @Override
    protected SPARQLDataReference getNewNonValidatingDataReference()
    {
        return new SPARQLDataReferenceImpl();
    }
    
    /*
     * Create a {@link Model} containing configuration details for an SSH File Repository.
     * 
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.file.test.AbstractPoddFileRepositoryTest#getNewPoddFileRepository()
     */
    @Override
    protected PoddDataRepository<SPARQLDataReference> getNewPoddFileRepository() throws Exception
    {
        final Model model = new LinkedHashModel();
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance().createLiteral(
                        this.getAliasGood())));
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_DATA_REPOSITORY));
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI, RDF.TYPE,
                PoddRdfConstants.PODD_SPARQL_DATA_REPOSITORY));
        
        // ssh specific attributes
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_PROTOCOL, ValueFactoryImpl.getInstance().createLiteral(
                        PoddDataRepository.PROTOCOL_HTTP)));
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_HOST, ValueFactoryImpl.getInstance().createLiteral(
                        SSHService.TEST_SSH_HOST)));
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_PORT, ValueFactoryImpl.getInstance().createLiteral(12345)));
        model.add(new StatementImpl(AbstractPoddFileRepositoryTest.TEST_ALIAS_URI,
                PoddRdfConstants.PODD_DATA_REPOSITORY_PATH, ValueFactoryImpl.getInstance().createLiteral(
                        "/path/to/sparql/endpoint")));
        
        return this.getNewPoddDataRepository(model);
    }
    
    @Override
    protected PoddDataRepository<SPARQLDataReference> getNewPoddDataRepository(final Model model) throws Exception
    {
        final PoddDataRepository result = new SPARQLDataRepositoryImpl(model);
        return result;
    }
    
    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
    }
    
    @Override
    protected void startRepositorySource() throws Exception
    {
    }
    
    @Override
    protected void stopRepositorySource() throws Exception
    {
    }
    
    @Ignore("TODO: Implement validation")
    @Test
    @Override
    public void testValidateWithNonExistentFile() throws Exception
    {
    }
    
}
