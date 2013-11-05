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
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

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
    void cacheSchemaOntology(InferredOWLOntologyID ontologyID, RepositoryConnection conn, URI context)
        throws OpenRDFException, OWLException, IOException, PoddException;
    
    /**
     * Creates a reasoner over the given OWLOntology
     * 
     * @param nextOntology
     * @return
     */
    OWLReasoner createReasoner(OWLOntology nextOntology);
    
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
    
    /**
     * Dump ontology to the given repository connection, using the Version IRI from the given
     * ontology as the context if a context is not given.
     * 
     * @param contextToDeduplicate
     *            If this is not null, statements available in this context are not dumped to the
     *            given repository connection.
     * @param nextOntology
     * @param nextRepositoryConnection
     * @param contexts
     *            If this is not null, it is used as the contexts, otherwise the Version IRI from
     *            the ontology is used as the context.
     * @throws IOException
     * @throws RepositoryException
     */
    void dumpOntologyToRepositoryWithoutDuplication(URI contextToDeduplicate, OWLOntology nextOntology,
            RepositoryConnection nextRepositoryConnection, URI... contexts) throws IOException, RepositoryException;
    
    /**
     * Generates a new unique IRI for inferred statements attached to the given OWLOntologyID, and
     * returns the result inside of a new InferredOWLOntologyID object.
     * 
     * @param ontologyID
     * @return
     */
    InferredOWLOntologyID generateInferredOntologyID(OWLOntologyID ontologyID);
    
    /**
     * NOTE: Restrict usage of this method, as it will always fetch the entire OWLOntology into
     * memory. Use getCurrentVersion or getVersions in most cases.
     * 
     * @param ontologyID
     *            The full OWLOntologyID, containing both Ontology IRI and Version IRI for the
     *            ontology to fetch.
     * @return An instance of OWLOntology that contains all of the OWL Axioms for the given
     *         ontology.
     * @throws IllegalArgumentException
     *             If the OWLOntologyID does not contain both Ontology IRI and Version IRI.
     * @throws OWLException
     *             If there was an error while attempting to get the Ontology.
     */
    OWLOntology getOntology(OWLOntologyID ontologyID) throws IllegalArgumentException, OWLException;
    
    /**
     * 
     * @return The {@link OWLProfile} used by the reasoner attached to this PoddOWLManager.
     */
    OWLProfile getReasonerProfile();
    
    /**
     * Returns a list of versions where one of the ontology IRI or version IRI match the ontology.
     * 
     * The most current version of the ontology is returned first in the list.
     * 
     * NOTE: The list of {@link OWLOntologyID} instances that are returned may not all contain the
     * given IRI in cases where the given IRI is one of many different Version IRIs.
     * 
     * @param ontologyIRI
     *            Either the Ontology IRI or the Version IRI of an ontology to match against.
     * @return A list of versions for all ontologies, where the version either matches the Ontology
     *         IRI or the Version IRI.
     */
    List<OWLOntologyID> getVersions(IRI ontologyIRI);
    
    /**
     * Infer statements for the given {@link OWLOntology} into the given permanent repository
     * connection.
     * 
     * TODO: Decide the behaviour if the asserted statements of the Ontology are not in the
     * Repository when this method is invoked.
     * 
     * @param ontology
     * @param permanentRepositoryConnection
     * @return The InferredOWLOntologyID representing the ontology, along with the IRI of the
     *         Inferred Ontology.
     * @throws OWLException
     * @throws OWLRuntimeException
     * @throws IOException
     * @throws OpenRDFException
     */
    InferredOWLOntologyID inferStatements(OWLOntology ontology, RepositoryConnection permanentRepositoryConnection)
        throws OWLRuntimeException, OWLException, OpenRDFException, IOException;
    
    /**
     * Loads an ontology into memory from an OWLOntologyDocumentSource.
     * 
     * @param owlSource
     * @return The OWLOntology that was loaded into memory
     * @throws OWLException
     * @throws IOException
     * @throws PoddException
     */
    OWLOntology loadOntology(OWLOntologyDocumentSource owlSource) throws OWLException, IOException, PoddException;
    
    /**
     * Parses RDF statements into an ontology, and returns the OWLOntologyID for the resulting
     * ontology.
     * 
     * NOTE: The Ontology is managed by the internal OWLOntologyManager, and will still be in memory
     * after this call.
     * 
     * @param conn
     *            Repository Connection that can be used to access the RDF statements.
     * @param contexts
     *            The Contexts inside of the Repository Connection that contain the relevant RDF
     *            statements.
     * @return The OWLOntologyID that was created by the internal OWLOntologyManager for the
     *         ontology that was parsed.
     * @throws OWLException
     * @throws IOException
     * @throws PoddException
     * @throws OpenRDFException
     */
    OWLOntologyID parseRDFStatements(RepositoryConnection conn, URI... contexts) throws OWLException, IOException,
        PoddException, OpenRDFException;
    
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
     * Sets the current version for the Ontology {@link IRI} in the given {@link OWLOntologyID} to
     * be the given version.
     * 
     * @param ontologyID
     * @throws IllegalArgumentException
     *             If the ontologyID does not have a version.
     */
    void setCurrentVersion(OWLOntologyID ontologyID);
    
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
