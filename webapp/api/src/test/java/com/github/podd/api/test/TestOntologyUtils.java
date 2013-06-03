/**
 * 
 */
package com.github.podd.api.test;

import java.io.InputStream;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Static utility class containing methods to help with loading ontologies for unit tests. 
 * 
 * @author kutila
 */
public class TestOntologyUtils
{
    
    /**
     * Given a resource containing a schema ontology, load the asserted and inferred statements into
     * the given RepositoryConnection.
     * 
     * @param resourcePath
     * @param fileFormat
     * @param poddOWLManager
     * @param testRepositoryConnection
     * @return An {@link InferredOWLOntologyID} for the loaded schema ontology
     * @throws Exception
     */
    public static InferredOWLOntologyID loadSchemaOntology(final String resourcePath, final RDFFormat fileFormat,
            final PoddOWLManager poddOWLManager, final RepositoryConnection testRepositoryConnection) throws Exception
    {
        final InputStream resourceStream = TestOntologyUtils.class.getResourceAsStream(resourcePath);
        final OWLOntologyDocumentSource owlSource =
                new StreamDocumentSource(resourceStream, fileFormat.getDefaultMIMEType());
        final OWLOntology assertedOntology = poddOWLManager.loadOntology(owlSource);
        poddOWLManager.dumpOntologyToRepository(assertedOntology, testRepositoryConnection);
        
        final InferredOWLOntologyID nextInferredOntology = poddOWLManager.inferStatements(assertedOntology, testRepositoryConnection);
        
        return nextInferredOntology;
    }
    
}
