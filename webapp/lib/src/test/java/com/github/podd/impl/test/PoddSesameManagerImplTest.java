/**
 * 
 */
package com.github.podd.impl.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.test.AbstractPoddSesameManagerTest;
import com.github.podd.api.test.TestOntologyUtils;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddSesameManagerImpl;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;

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
     * @see com.github.podd.api.test.AbstractPoddSesameManagerTest#loadSchemaOntologies(org.openrdf.
     * repository.RepositoryConnection)
     * 
     * NOTE: This test implementation creates a PoddOWLManager instance in order to dynamically
     * generate inferred statements for schema ontologies.
     */
    @Override
    public List<InferredOWLOntologyID> loadSchemaOntologies(RepositoryConnection conn) throws Exception
    {
        final List<InferredOWLOntologyID> schemaList = new ArrayList<InferredOWLOntologyID>();
        
        // - schema ontologies to be loaded
        final String[] schemaResourcePaths =
                { PoddRdfConstants.PATH_PODD_DCTERMS, PoddRdfConstants.PATH_PODD_FOAF, PoddRdfConstants.PATH_PODD_USER,
                        PoddRdfConstants.PATH_PODD_BASE, PoddRdfConstants.PATH_PODD_SCIENCE,
                        PoddRdfConstants.PATH_PODD_PLANT,
                // PoddRdfConstants.PATH_PODD_ANIMAL,
                };
        
        // - create a PODD OWLManager instance
        final PoddOWLManagerImpl testPoddOWLManager = new PoddOWLManagerImpl();
        
        final OWLOntologyManager manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        Assert.assertNotNull("Null implementation of OWLOntologymanager", manager);
        testPoddOWLManager.setOWLOntologyManager(manager);
        
        final OWLReasonerFactory reasonerFactory =
                OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet");
        Assert.assertNotNull("Null implementation of OWLReasonerFactory", reasonerFactory);
        testPoddOWLManager.setReasonerFactory(reasonerFactory);
        
        // - load each schema ontology (and its inferred ontology) to the RepositoryConnection
        for(int i = 0; i < schemaResourcePaths.length; i++)
        {
            this.log.debug("Next paths: {} ", schemaResourcePaths[i]);
            InferredOWLOntologyID loadedOntologyID =
                    TestOntologyUtils.loadSchemaOntology(schemaResourcePaths[i], RDFFormat.RDFXML, testPoddOWLManager,
                            conn);
            schemaList.add(loadedOntologyID);
        }
        
        return schemaList;
    }
    
}
