/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;

/**
 * Interface containing URI constants for the Ontologies needed in PODD.
 * 
 * @author kutila
 * 
 */
public interface PoddRdfConstants
{
    public static final ValueFactory VALUE_FACTORY = ValueFactoryImpl.getInstance();
    
    public static final String PODD_BASE = "http://purl.org/podd/ns/poddBase#";
    
    public static final URI HAS_PUBLICATION_STATUS = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "hasPublicationStatus");
    
    public static final URI PUBLISHED = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "Published");
    
    public static final URI HAS_TOP_OBJECT = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "artifactHasTopObject");
    
    public static final URI OWL_VERSION_IRI = PoddRdfConstants.VALUE_FACTORY.createURI(OWL.NAMESPACE, "versionIRI");
    
    /**
     * The OMV vocabulary defines a property for the current version of an ontology, so we are
     * reusing it here.
     */
    public static final URI OMV_CURRENT_VERSION = PoddRdfConstants.VALUE_FACTORY.createURI(
            "http://omv.ontoware.org/ontology#", "currentVersion");
    
    /**
     * Creating a property for PODD to track the currentInferredVersion for the inferred axioms
     * ontology when linking from the ontology IRI.
     * 
     * TODO: Put this in an external ontology somewhere so it isn't dependent on PODD.
     */
    public static final URI PODD_BASE_CURRENT_INFERRED_VERSION = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "currentInferredVersion");
    
    /**
     * Creating a property for PODD to track the inferredVersion for the inferred axioms ontology of
     * a particular versioned ontology.
     * 
     * TODO: Put this in an external ontology somewhere so it isn't dependent on PODD.
     */
    public static final URI PODD_BASE_INFERRED_VERSION = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "inferredVersion");
    
    public static final URI DEFAULT_ARTIFACT_MANAGEMENT_GRAPH = PoddRdfConstants.VALUE_FACTORY
            .createURI("urn:podd:default:artifactmanagementgraph:");
    
    public static final URI DEFAULT_SCHEMA_MANAGEMENT_GRAPH = PoddRdfConstants.VALUE_FACTORY
            .createURI("urn:podd:default:schemamanagementgraph");
    
}
