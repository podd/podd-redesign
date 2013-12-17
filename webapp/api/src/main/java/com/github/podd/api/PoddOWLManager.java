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
/**
 * 
 */
package com.github.podd.api;

import java.io.IOException;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.exception.OntologyNotInProfileException;
import com.github.podd.exception.PoddException;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Manages interactions with OWLAPI for PODD.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddOWLManager
{
    /**
     * Loads and caches the given schema ontology in memory from a Repository. Silently returns if
     * the ontology is already cached.
     * 
     * @param ontologyID
     * @param conn
     * @param context
     * @throws OpenRDFException
     * @throws PoddException
     * @throws IOException
     * @throws OWLException
     */
    void cacheSchemaOntology(OWLOntologyID ontologyID, RepositoryConnection conn, URI context) throws OpenRDFException,
        OWLException, IOException, PoddException;
    
    /**
     * Dump ontology to the given repository connection, using the Version IRI from the given
     * ontology as the context if a context is not given.
     * 
     * @param nextOntology
     * @param nextRepositoryConnection
     * @param contexts
     *            If this is not null, it is used as the contexts, otherwise the Version IRI from
     *            the ontology is used as the context.
     * @throws IOException
     * @throws RepositoryException
     */
    void dumpOntologyToRepository(OWLOntology nextOntology, RepositoryConnection nextRepositoryConnection,
            URI... contexts) throws IOException, RepositoryException;
    
    InferredOWLOntologyID loadAndInfer(RepositoryConnection permanentRepositoryConnection, OWLOntologyID ontologyID,
            OWLOntologyDocumentSource owlSource) throws OWLException, PoddException, OpenRDFException, IOException;
    
    /**
     * Attempts to regain memory in the underlying OWLOntologyManager by removing the ontology from
     * the in-memory cache.
     * 
     * @param ontologyID
     *            The full OWLOntologyID, containing both Ontology IRI and Version IRI for the
     *            ontology to remove from the cache.
     * @return True if the ontology was in memory and it was successfully removed, and false if the
     *         ontology was not found in memory.
     * @throws IllegalArgumentException
     *             If the OWLOntologyID does not contain both Ontology IRI and Version IRI.
     * @throws OWLException
     *             If there was an error while attempting to retrieve the memory.
     */
    boolean removeCache(OWLOntologyID ontologyID) throws OWLException;
    
    /**
     * Helper method to verify that a given {@link Model} represents an ontology which complies with
     * the given schema OWL Ontology.
     * 
     * @param model
     * @param schemaModel
     * @throws OntologyNotInProfileException
     */
    void verifyAgainstSchema(Model model, Model schemaModel) throws OntologyNotInProfileException;
    
}
