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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.exception.PoddException;
import com.github.podd.exception.UnmanagedSchemaException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.exception.UnmanagedSchemaOntologyIDException;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Manages PODD Schema Ontologies, including loading and retrieving ontologies in various RDF
 * formats, along with the computation of inferences for ontologies.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public interface PoddSchemaManager
{
    /**
     * Downloads a PODD Schema Ontology to the given outputStream, in the given format, without
     * inferences.
     *
     * @param schemaOntologyID
     *            The OWL Ontology ID for the schema ontology.
     * @param outputStream
     *            The stream that will be sent the schema ontology.
     * @param format
     *            The format for the downloaded Schema Ontology.
     * @param includeInferences
     *            If true, inferred statements are also returned. If false, only the concrete RDF
     *            triples are returned.
     * @throws UnmanagedSchemaException
     *             If the schema is not managed
     * @throws OpenRDFException
     * @throws RepositoryException
     */
    void downloadSchemaOntology(InferredOWLOntologyID schemaOntologyID, OutputStream outputStream, RDFFormat format,
            boolean includeInferences) throws UnmanagedSchemaException, RepositoryException, OpenRDFException;
    
    /**
     * Gets the complete set of current PODD Schema Ontologies. That is, for each schema ontology,
     * its current version is included.
     *
     * @return The set of current schema ontologies.
     * @throws OpenRDFException
     */
    Set<InferredOWLOntologyID> getCurrentSchemaOntologies() throws OpenRDFException;
    
    /**
     * Get the most current version of a managed Schema Ontology that contains the given
     * schemaOntologyIRI as either the ontology IRI or the version IRI.
     *
     * NOTE: This method must be able to take an ontology version IRI, and return the most current
     * version of that ontology, if the IRI does not match a schema ontology IRI.
     *
     * @param schemaOntologyIRI
     *            The Ontology IRI, or if no ontology IRIs match, the version IRI of a Schema
     *            Ontology.
     * @return The current managed Schema Ontology with either the given IRI as the schema ontology
     *         IRI, or the given IRI as the schema ontology version IRI.
     * @throws UnmanagedSchemaIRIException
     *             If the given schemaOntologyIRI is not recognised or managed.
     * @throws OpenRDFException
     */
    InferredOWLOntologyID getCurrentSchemaOntologyVersion(IRI schemaOntologyIRI) throws UnmanagedSchemaIRIException,
        OpenRDFException;
    
    /**
     * Gets the complete set of PODD Schema Ontologies, including previous versions.
     *
     * @return The set of all schema ontologies.
     * @throws OpenRDFException
     */
    Set<InferredOWLOntologyID> getSchemaOntologies() throws OpenRDFException;
    
    /**
     * Gets a PODD Schema Ontology based on the given IRI. If the IRI matches a managed Schema
     * Ontology IRI then the current version of that Schema Ontology is returned. If the IRI does
     * not match a Schema Ontology IRI, then it is checked against all of the version IRIs for the
     * managed Schema Ontologies and the matching Schema Ontology is returned.
     *
     * If no managed Schema Ontologies match the given IRI, then an UnmanagedSchemaIRIException is
     * thrown.
     *
     * @param schemaOntologyIRI
     *            The Ontology IRI, or if no ontology IRIs match, the version IRI of a Schema
     *            Ontology.
     * @return The OWL Ontology represented by the schemaOntologyIRI. If the schemaOntologyIRI
     *         matches an ontology IRI, then the current version of the schema Ontology is returned,
     *         otherwise the schemaOntologyIRI is matched as a version against ontologies.
     * @throws UnmanagedSchemaIRIException
     *             If the given schemaOntologyIRI is not recognised or managed.
     */
    OWLOntology getSchemaOntology(URI schemaOntologyIRI) throws UnmanagedSchemaIRIException;
    
    /**
     * Gets the PODD Schema Ontology based on the schemaOntologyID. If the schemaOntologyID does not
     * contain a version IRI, then the current version of the Schema Ontology
     *
     * @param schemaOntologyID
     *            The OWL Ontology ID for the schema ontology.
     * @return The OWLOntology instance containing the ontology identified by schemaOntologyID. If
     *         the schemaOntologyID does not match any managed schema ontologies.
     * @throws UnmanagedSchemaOntologyIDException
     *             If the given schemaOntologyID is not managed.
     */
    OWLOntology getSchemaOntology(OWLOntologyID schemaOntologyID) throws UnmanagedSchemaOntologyIDException;
    
    /**
     * Gets the exact schema ontology version specified by the parameter if it exists, or throws an
     * exception.
     *
     * @param owlOntologyID
     *            The schema ontology to fetch.
     * @return An {@link InferredOWLOntologyID} representing the ontology IRI and version IRI for
     *         the schema ontology as managed by the system.
     * @throws UnmanagedSchemaIRIException
     *             If the schema identified by the parameter is not currently managed by the system.
     * @throws OpenRDFException
     *             If there was a problem with resolving or parsing the schema.
     */
    InferredOWLOntologyID getSchemaOntologyID(OWLOntologyID owlOntologyID) throws UnmanagedSchemaOntologyIDException,
        OpenRDFException;
    
    /**
     * If the given IRI is the version IRI of a managed Schema Ontology, return its Ontology ID. If
     * the given IRI is an ontology IRI of a managed Schema Ontology, return the Ontology ID of its
     * most current version.
     *
     * @param schemaVersionIRI
     *            The Version IRI, or if no version IRIs match, the ontology IRI of a Schema
     *            Ontology.
     * @return
     * @throws UnmanagedSchemaIRIException
     * @throws OpenRDFException
     */
    InferredOWLOntologyID getSchemaOntologyVersion(IRI schemaVersionIRI) throws UnmanagedSchemaIRIException,
        OpenRDFException;
    
    /**
     * Sets the given schemaOntologyID to be the most current version for all managed Schema
     * Ontologies with the schema ontology IRI.
     *
     * If the schemaOntologyID does not contain a version IRI, then an IllegalArgumentException is
     * thrown.
     *
     * @param schemaOntologyID
     *            The most current OWL Ontology ID for the schema ontology.
     * @throws UnmanagedSchemaOntologyIDException
     *             If the given schemaOntologyID is not managed.
     * @throws IllegalArgumentException
     *             If the given schemaOntologyID does not contain both an ontology IRI and a version
     *             IRI.
     * @throws OpenRDFException
     */
    void setCurrentSchemaOntologyVersion(OWLOntologyID schemaOntologyID) throws UnmanagedSchemaOntologyIDException,
        IllegalArgumentException, OpenRDFException;
    
    /**
     * Sets the shared PoddOWLManager to use for Schema Ontologies and Artifact Ontologies.
     *
     * @param owlManager
     *            The PoddOWLManager
     */
    void setOwlManager(PoddOWLManager owlManager);
    
    /**
     *
     * @param repositoryManager
     *            The PoddRepositoryManager
     */
    void setRepositoryManager(PoddRepositoryManager repositoryManager);
    
    /**
     *
     * @param sesameManager
     *            The PoddSesameManager
     */
    void setSesameManager(PoddSesameManager sesameManager);
    
    /**
     * Uploads schema ontologies to the schema manager using the RDF statements in the manifest
     * {@link Model}. This method must do its best to arrange for the schema ontologies to be
     * uploaded in order of their imports.
     *
     * @param manifest
     *            A Model containing RDF statements describing the schema ontologies to upload.
     * @return The ordered list of schema ontologies that were uploaded.
     * @throws PoddException
     * @throws OWLException
     * @throws IOException
     * @throws OpenRDFException
     */
    List<InferredOWLOntologyID> uploadSchemaOntologies(Model manifest) throws OpenRDFException, IOException,
        OWLException, PoddException;
    
    /**
     * Loads a Schema Ontology into the internal repository, computes inferences on the schema
     * ontology, and stores the original schema ontology and the inferences in separate RDF Graphs
     * inside of the internal repository.
     *
     * If there is no version IRI in the incoming schema ontology, PODD assigns a new version.
     * However, if the incoming content has a version, and it is unique, it is preserved.
     *
     * @param inputStream
     * @param fileFormat
     * @return An InferredOWLOntologyID containing the ontology IRI, version IRI, along with the
     *         ontology IRI that was used for the inferences.
     * @throws OpenRDFException
     *             If an RDF parsing or RDF repository error occurs.
     * @throws IOException
     *             If the file cannot be processed due to an Input/Output error.
     * @throws OWLException
     *             If the file cannot be interpreted as an OWL Ontology or is inconsistent in some
     *             way.
     * @throws PoddException
     *             If an error occurs due to a violation of the PODD constraints.
     */
    InferredOWLOntologyID uploadSchemaOntology(InputStream inputStream, RDFFormat fileFormat,
            Set<? extends OWLOntologyID> dependentSchemaOntologies) throws OpenRDFException, IOException, OWLException,
        PoddException;
    
    /**
     * Loads a Schema Ontology into the internal repository, computes inferences on the schema
     * ontology, and stores the original schema ontology and the inferences in separate RDF Graphs
     * inside of the internal repository.
     *
     * The version information in the passed in OWLOntologyID will override any version or ontology
     * IRI information contained in the inputstream.
     *
     * @param schemaOntologyID
     * @param inputStream
     * @param fileFormat
     * @return An InferredOWLOntologyID containing the ontology IRI, version IRI, along with the
     *         ontology IRI that was used for the inferences.
     * @throws OpenRDFException
     *             If an RDF parsing or RDF repository error occurs.
     * @throws IOException
     *             If the file cannot be processed due to an Input/Output error.
     * @throws OWLException
     *             If the file cannot be interpreted as an OWL Ontology or is inconsistent in some
     *             way.
     * @throws PoddException
     *             If an error occurs due to a violation of the PODD constraints.
     */
    InferredOWLOntologyID uploadSchemaOntology(OWLOntologyID schemaOntologyID, InputStream inputStream,
            RDFFormat fileFormat, Set<? extends OWLOntologyID> dependentSchemaOntologies) throws OpenRDFException,
        IOException, OWLException, PoddException;
    
}
