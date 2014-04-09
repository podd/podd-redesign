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
package com.github.podd.impl.test;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryImplConfigBase;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.data.DataReferenceManager;
import com.github.podd.api.data.DataReferenceProcessorFactory;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.test.AbstractPoddArtifactManagerTest;
import com.github.podd.impl.PoddArtifactManagerImpl;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddRepositoryManagerImpl;
import com.github.podd.impl.PoddSchemaManagerImpl;
import com.github.podd.impl.PoddSesameManagerImpl;
import com.github.podd.impl.data.DataReferenceManagerImpl;
import com.github.podd.impl.data.SSHFileReferenceProcessorFactoryImpl;
import com.github.podd.impl.purl.PoddPurlManagerImpl;
import com.github.podd.impl.purl.UUIDPurlProcessorFactoryImpl;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddArtifactManagerImplTest extends AbstractPoddArtifactManagerTest
{
    @Override
    protected PoddArtifactManager getNewArtifactManager()
    {
        return new PoddArtifactManagerImpl();
    }
    
    @Override
    protected PoddPurlProcessorFactory getNewDoiPurlProcessorFactory()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected DataReferenceManager getNewFileReferenceManager()
    {
        return new DataReferenceManagerImpl();
    }
    
    @Override
    protected PoddPurlProcessorFactory getNewHandlePurlProcessorFactory()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected DataReferenceProcessorFactory getNewHttpFileReferenceProcessorFactory()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected PoddOWLManager getNewOWLManager(final OWLOntologyManagerFactory manager,
            final OWLReasonerFactory reasonerFactory)
    {
        return new PoddOWLManagerImpl(manager, reasonerFactory);
    }
    
    @Override
    protected PoddPurlManager getNewPurlManager()
    {
        return new PoddPurlManagerImpl();
    }
    
    @Override
    protected OWLReasonerFactory getNewReasonerFactory()
    {
        return OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet");
    }
    
    @Override
    protected PoddRepositoryManager getNewRepositoryManager(final Repository managementRepository, final Path testPath)
        throws Exception
    {
        
        final Model graph =
                Rio.parse(this.getClass().getResourceAsStream("/memorystoreconfig.ttl"), "", RDFFormat.TURTLE);
        final Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RepositoryConfigSchema.REPOSITORYTYPE, null);
        final RepositoryImplConfig repositoryImplConfig = RepositoryImplConfigBase.create(graph, repositoryNode);
        Assert.assertNotNull(repositoryImplConfig);
        Assert.assertNotNull(repositoryImplConfig.getType());
        return new PoddRepositoryManagerImpl(managementRepository, repositoryImplConfig, "", testPath,
                new PropertyUtil("podd"));
    }
    
    @Override
    protected PoddSchemaManager getNewSchemaManager()
    {
        return new PoddSchemaManagerImpl();
    }
    
    @Override
    protected PoddSesameManager getNewSesameManager()
    {
        return new PoddSesameManagerImpl();
    }
    
    @Override
    protected DataReferenceProcessorFactory getNewSSHFileReferenceProcessorFactory()
    {
        return new SSHFileReferenceProcessorFactoryImpl();
    }
    
    @Override
    protected PoddPurlProcessorFactory getNewUUIDPurlProcessorFactory()
    {
        return new UUIDPurlProcessorFactoryImpl();
    }
    
    /**
     * Helper method which loads version 1 for the three PODD schema ontologies (and their
     * dependencies): PODD-Base, PODD-Science and PODD-Plant.
     * 
     * This method is not called from the setUp() method since some tests require not loading all
     * schema ontologies.
     * 
     * @throws Exception
     */
    @Override
    protected List<InferredOWLOntologyID> loadVersion1SchemaOntologies() throws Exception
    {
        return this.testArtifactManager.getSchemaManager().uploadSchemaOntologies(
                Rio.parse(this.getClass().getResourceAsStream("/podd-schema-manifest-version1only.ttl"), "",
                        RDFFormat.TURTLE));
    }
    
    /**
     * Helper method which loads version 1 for the three PODD schema ontologies (and their
     * dependencies): PODD-Base, PODD-Science and PODD-Plant.
     * 
     * This method is not called from the setUp() method since some tests require not loading all
     * schema ontologies.
     * 
     * @throws Exception
     */
    @Override
    protected List<InferredOWLOntologyID> loadVersion2SchemaOntologies() throws Exception
    {
        return this.testArtifactManager.getSchemaManager().uploadSchemaOntologies(
                Rio.parse(this.getClass().getResourceAsStream("/podd-schema-manifest-version2only.ttl"), "",
                        RDFFormat.TURTLE));
    }
    
    @Test
    public void testIncrementVersion() throws Exception
    {
        final String artifactURI = "http://some/artifact:15";
        
        final PoddArtifactManagerImpl testArtifactManager = new PoddArtifactManagerImpl();
        
        // increment the version number
        final String newIncrementedVersion = testArtifactManager.incrementVersion(artifactURI + ":version:1");
        Assert.assertEquals("Version not incremented as expected", artifactURI + ":version:2", newIncrementedVersion);
        
        // append a number when version number cannot be extracted
        final String newAppendedVersion = testArtifactManager.incrementVersion(artifactURI + ":v5");
        Assert.assertEquals("Version not incremented as expected", artifactURI + ":v51", newAppendedVersion);
    }
    
    @Override
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
    
}
