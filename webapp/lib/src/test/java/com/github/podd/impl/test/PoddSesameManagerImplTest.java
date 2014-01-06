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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Assert;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.test.AbstractPoddSesameManagerTest;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddSesameManagerImpl;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class PoddSesameManagerImplTest extends AbstractPoddSesameManagerTest
{
    protected OWLOntologyManagerFactory getNewOWLOntologyManagerFactory()
    {
        Collection<OWLOntologyManagerFactory> ontologyManagers =
                OWLOntologyManagerFactoryRegistry.getInstance().get(PoddWebConstants.DEFAULT_OWLAPI_MANAGER);
        
        if(ontologyManagers == null || ontologyManagers.isEmpty())
        {
            this.log.error("OWLOntologyManagerFactory was not found");
        }
        return ontologyManagers.iterator().next();
    }
    
    @Override
    public final PoddSesameManager getNewPoddSesameManagerInstance()
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
    public final List<InferredOWLOntologyID> loadSchemaOntologies(final RepositoryConnection managementConnection,
            final URI schemaManagementGraph) throws Exception
    {
        Model model =
                Rio.parse(this.getClass().getResourceAsStream("/podd-schema-manifest.ttl"), "", RDFFormat.TURTLE,
                        schemaManagementGraph);
        
        managementConnection.add(model, schemaManagementGraph);
        
        // - create a PODD OWLManager instance
        final OWLReasonerFactory reasonerFactory =
                OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet");
        Assert.assertNotNull("Null implementation of OWLReasonerFactory", reasonerFactory);
        final PoddOWLManagerImpl testPoddOWLManager =
                new PoddOWLManagerImpl(getNewOWLOntologyManagerFactory(), reasonerFactory);
        
        List<InferredOWLOntologyID> ontologyIDs = OntologyUtils.modelToOntologyIDs(model, false, false);
        
        for(InferredOWLOntologyID nextOntology : ontologyIDs)
        {
            String classpath =
                    model.filter(nextOntology.getVersionIRI().toOpenRDFURI(), PODD.PODD_SCHEMA_CLASSPATH, null)
                            .objectString();
            Assert.assertNotNull("Ontology was not mapped to a classpath: " + nextOntology, classpath);
            InputStream nextStream = this.getClass().getResourceAsStream(classpath);
            Assert.assertNotNull("Ontology classpath mapping was not valid: " + nextOntology + " " + classpath);
            
            managementConnection.add(nextStream, "", Rio.getParserFormatForFileName(classpath, RDFFormat.RDFXML),
                    nextOntology.getVersionIRI().toOpenRDFURI());
        }
        
        testPoddOWLManager.cacheSchemaOntologies(new LinkedHashSet<>(ontologyIDs), managementConnection,
                schemaManagementGraph);
        
        return ontologyIDs;
    }
    
}
