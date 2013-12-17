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
        final OWLOntology assertedOntology = poddOWLManager.loadOntology(null, owlSource);
        poddOWLManager.dumpOntologyToRepository(assertedOntology, testRepositoryConnection);
        
        final InferredOWLOntologyID nextInferredOntology =
                poddOWLManager.inferStatements(assertedOntology, testRepositoryConnection);
        
        return nextInferredOntology;
    }
    
}
