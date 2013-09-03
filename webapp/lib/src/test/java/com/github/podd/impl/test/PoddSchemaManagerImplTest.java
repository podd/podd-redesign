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
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.test.AbstractPoddSchemaManagerTest;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddRepositoryManagerImpl;
import com.github.podd.impl.PoddSchemaManagerImpl;
import com.github.podd.impl.PoddSesameManagerImpl;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddSchemaManagerImplTest extends AbstractPoddSchemaManagerTest
{
    @Override
    protected OWLOntologyManager getNewOwlOntologyManagerInstance()
    {
        return OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
    }
    
    @Override
    protected PoddOWLManager getNewPoddOwlManagerInstance()
    {
        return new PoddOWLManagerImpl();
    }
    
    @Override
    protected PoddRepositoryManager getNewPoddRepositoryManagerInstance()
    {
        return new PoddRepositoryManagerImpl();
    }
    
    @Override
    protected PoddSchemaManager getNewPoddSchemaManagerInstance()
    {
        return new PoddSchemaManagerImpl();
    }
    
    @Override
    protected PoddSesameManager getNewPoddSesameManagerInstance()
    {
        return new PoddSesameManagerImpl();
    }
    
    @Override
    protected OWLReasonerFactory getNewReasonerFactory()
    {
        return OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet");
    }
   
    
    @Test
    public void testUploadSchemaOntologiesInOrder() throws Exception
    {
        // prepare: Model containing schema-manifest
        final String schemaManifest = "/test/schema-manifest-a1b2c2.ttl";
        Model model = null;
        try (final InputStream schemaManifestStream = this.getClass().getResourceAsStream(schemaManifest);)
        {
            final RDFFormat format = Rio.getParserFormatForFileName(schemaManifest, RDFFormat.RDFXML);
            model = Rio.parse(schemaManifestStream, "", format);
        }
        
        // prepare: order of imports
        final String[] testImportOrderArray = {
                "http://example.org/podd/ns/version/poddA/1", 
                "http://example.org/podd/ns/version/poddB/2",
                "http://example.org/podd/ns/version/poddB/1",
                "http://example.org/podd/ns/version/poddC/2", 
                "http://example.org/podd/ns/version/poddC/1", 
            };

        //Order: A1, B2, C2, B1, C1 fails. Is it correct?
        
        final List<URI> testImportOrder = new ArrayList<>();
        for (final String s : testImportOrderArray)
        {
            testImportOrder.add(PoddRdfConstants.VF.createURI(s));
        }
        
        
        ((PoddSchemaManagerImpl)this.testSchemaManager).uploadSchemaOntologiesInOrder(model, testImportOrder);
        
        
        // verify: schemas successfully loaded
        final Set<InferredOWLOntologyID> schemaOntologies = this.testSchemaManager.getCurrentSchemaOntologies();
        Assert.assertEquals("Expected 3 schema ontologies", 3, schemaOntologies.size());
        
        // verify: iterate through to ensure all versions exist
        int versionCount = 0;
        for (final String versionIri : testImportOrderArray)
        {
            final InferredOWLOntologyID schemaOntologyVersion = this.testSchemaManager.getSchemaOntologyVersion(IRI.create(versionIri));
            Assert.assertTrue(schemaOntologyVersion != null);
            versionCount++;
        }
        Assert.assertEquals("Expected 5 distinct schema ontology versions", 5, versionCount);
    }
}
