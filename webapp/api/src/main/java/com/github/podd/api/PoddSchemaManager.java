/**
 * 
 */
package com.github.podd.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
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
     */
    void downloadSchemaOntology(OWLOntologyID schemaOntologyID, OutputStream outputStream, RDFFormat format,
            boolean includeInferences) throws UnmanagedSchemaException;
    
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
     */
    InferredOWLOntologyID getCurrentSchemaOntologyVersion(IRI schemaOntologyIRI) throws UnmanagedSchemaIRIException;
    
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
    OWLOntology getSchemaOntology(IRI schemaOntologyIRI) throws UnmanagedSchemaIRIException;
    
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
     */
    void setCurrentSchemaOntologyVersion(OWLOntologyID schemaOntologyID) throws UnmanagedSchemaOntologyIDException,
        IllegalArgumentException;
    
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
    InferredOWLOntologyID uploadSchemaOntology(InputStream inputStream, RDFFormat fileFormat) throws OpenRDFException,
        IOException, OWLException, PoddException;
    
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
            RDFFormat fileFormat) throws OpenRDFException, IOException, OWLException, PoddException;
    
}
