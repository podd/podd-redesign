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
package com.github.podd.impl.file.test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;

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
    protected Map<Resource, Model> getIncompleteModels()
    {
        final ValueFactory vf = PoddRdfConstants.VF;
        final Map<Resource, Model> incompleteModels = new HashMap<Resource, Model>();
        
        // - no "protocol"
        final Model model1 = new LinkedHashModel();
        BNode resource1 = vf.createBNode("incompleteModel:1");
        model1.add(resource1, PoddRdfConstants.PODD_DATA_REPOSITORY_ALIAS, vf.createLiteral(this.getAliasGood()));
        model1.add(resource1, RDF.TYPE, PoddRdfConstants.PODD_DATA_REPOSITORY);
        model1.add(resource1, RDF.TYPE, PoddRdfConstants.PODD_SPARQL_DATA_REPOSITORY);
        
        model1.add(resource1, PoddRdfConstants.PODD_DATA_REPOSITORY_HOST, vf.createLiteral("localhost"));
        model1.add(resource1, PoddRdfConstants.PODD_DATA_REPOSITORY_PORT, vf.createLiteral(12345));
        
        incompleteModels.put(resource1, model1);
        
        // - no "host"
        final Model model2 = new LinkedHashModel();
        BNode resource2 = vf.createBNode("incompleteModel:2");
        model2.add(resource2, PoddRdfConstants.PODD_DATA_REPOSITORY_ALIAS, vf.createLiteral(this.getAliasGood()));
        model2.add(resource2, RDF.TYPE, PoddRdfConstants.PODD_DATA_REPOSITORY);
        model2.add(resource2, RDF.TYPE, PoddRdfConstants.PODD_SPARQL_DATA_REPOSITORY);
        
        model2.add(resource2, PoddRdfConstants.PODD_DATA_REPOSITORY_PROTOCOL,
                vf.createLiteral(PoddDataRepository.PROTOCOL_HTTP));
        model2.add(resource2, PoddRdfConstants.PODD_DATA_REPOSITORY_PORT, vf.createLiteral(12345));
        
        incompleteModels.put(resource2, model2);
        
        // - no "port"
        final Model model3 = new LinkedHashModel();
        BNode resource3 = vf.createBNode("incompleteModel:3");
        model3.add(resource3, PoddRdfConstants.PODD_DATA_REPOSITORY_ALIAS, vf.createLiteral(this.getAliasGood()));
        model3.add(resource3, RDF.TYPE, PoddRdfConstants.PODD_DATA_REPOSITORY);
        model3.add(resource3, RDF.TYPE, PoddRdfConstants.PODD_SPARQL_DATA_REPOSITORY);
        
        model3.add(resource3, PoddRdfConstants.PODD_DATA_REPOSITORY_PROTOCOL,
                vf.createLiteral(PoddDataRepository.PROTOCOL_HTTP));
        model3.add(resource3, PoddRdfConstants.PODD_DATA_REPOSITORY_HOST, vf.createLiteral("localhost"));
        
        incompleteModels.put(resource3, model3);
        
        // - no protocol, host, port
        final Model model4 = new LinkedHashModel();
        BNode resource4 = vf.createBNode("incompleteModel:4");
        model4.add(resource4, PoddRdfConstants.PODD_DATA_REPOSITORY_ALIAS, vf.createLiteral(this.getAliasGood()));
        model4.add(resource4, RDF.TYPE, PoddRdfConstants.PODD_DATA_REPOSITORY);
        model4.add(resource4, RDF.TYPE, PoddRdfConstants.PODD_SPARQL_DATA_REPOSITORY);
        
        incompleteModels.put(resource4, model4);
        return incompleteModels;
    }
    
    @Override
    protected SPARQLDataReference getNewNonValidatingDataReference()
    {
        return new SPARQLDataReferenceImpl();
    }
    
    @Override
    protected PoddDataRepository<SPARQLDataReference> getNewPoddDataRepository(final Resource nextDataRepository,
            final Model model) throws Exception
    {
        final PoddDataRepository result = new SPARQLDataRepositoryImpl(nextDataRepository, model);
        return result;
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
        final ValueFactory vf = PoddRdfConstants.VF;
        final Model model = new LinkedHashModel();
        BNode bNode = vf.createBNode();
        model.add(bNode, PoddRdfConstants.PODD_DATA_REPOSITORY_ALIAS, vf.createLiteral(this.getAliasGood()));
        model.add(bNode, RDF.TYPE, PoddRdfConstants.PODD_DATA_REPOSITORY);
        model.add(bNode, RDF.TYPE, PoddRdfConstants.PODD_SPARQL_DATA_REPOSITORY);
        
        // ssh specific attributes
        model.add(bNode, PoddRdfConstants.PODD_DATA_REPOSITORY_PROTOCOL,
                vf.createLiteral(PoddDataRepository.PROTOCOL_HTTP));
        model.add(bNode, PoddRdfConstants.PODD_DATA_REPOSITORY_HOST, vf.createLiteral(SSHService.TEST_SSH_HOST));
        model.add(bNode, PoddRdfConstants.PODD_DATA_REPOSITORY_PORT, vf.createLiteral(12345));
        model.add(bNode, PoddRdfConstants.PODD_DATA_REPOSITORY_PATH, vf.createLiteral("/path/to/sparql/endpoint"));
        
        return this.getNewPoddDataRepository(bNode, model);
    }
    
    @Override
    protected SPARQLDataReference getNewValidatingDataReference()
    {
        return new SPARQLDataReferenceImpl();
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
