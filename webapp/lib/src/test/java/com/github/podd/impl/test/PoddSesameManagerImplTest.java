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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.test.AbstractPoddSesameManagerTest;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddSesameManagerImpl;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PODD;

/**
 * @author kutila
 * 
 */
public class PoddSesameManagerImplTest extends AbstractPoddSesameManagerTest
{
    
    @Override
    public PoddSesameManager getNewPoddSesameManagerInstance()
    {
        return new PoddSesameManagerImpl();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.test.AbstractPoddSesameManagerTest#loadSchemaOntologies
     * (org.openrdf. repository.RepositoryConnection)
     * 
     * NOTE: This test implementation creates a PoddOWLManager instance in order to dynamically
     * generate inferred statements for schema ontologies.
     */
    @Override
    public List<InferredOWLOntologyID> loadSchemaOntologies(final RepositoryConnection conn) throws Exception
    {
        final List<InferredOWLOntologyID> schemaList = new ArrayList<InferredOWLOntologyID>();
        
        // - schema ontologies to be loaded
        final String[] schemaResourcePaths =
                { PODD.PATH_PODD_DCTERMS_V1, PODD.PATH_PODD_FOAF_V1, PODD.PATH_PODD_USER_V1, PODD.PATH_PODD_BASE_V1,
                        PODD.PATH_PODD_SCIENCE_V1, PODD.PATH_PODD_PLANT_V1,
                // PoddRdfConstants.PATH_PODD_ANIMAL,
                };
        
        // - create a PODD OWLManager instance
        final OWLOntologyManager manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        Assert.assertNotNull("Null implementation of OWLOntologymanager", manager);
        final OWLReasonerFactory reasonerFactory =
                OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet");
        Assert.assertNotNull("Null implementation of OWLReasonerFactory", reasonerFactory);
        final PoddOWLManagerImpl testPoddOWLManager = new PoddOWLManagerImpl(manager, reasonerFactory);
        
        // - load each schema ontology (and its inferred ontology) to the
        // RepositoryConnection
        for(final String schemaResourcePath : schemaResourcePaths)
        {
            this.log.debug("Next paths: {} ", schemaResourcePath);
            final InputStream resourceStream = this.getClass().getResourceAsStream(schemaResourcePath);
            final OWLOntologyDocumentSource owlSource =
                    new StreamDocumentSource(resourceStream, RDFFormat.RDFXML.getDefaultMIMEType());
            final OWLOntology assertedOntology = testPoddOWLManager.loadOntology(null, owlSource);
            testPoddOWLManager.dumpOntologyToRepository(assertedOntology, conn);
            
            final InferredOWLOntologyID nextInferredOntology =
                    testPoddOWLManager.inferStatements(assertedOntology, conn);
            final InferredOWLOntologyID loadedOntologyID = nextInferredOntology;
            schemaList.add(loadedOntologyID);
        }
        
        return schemaList;
    }
    
}
