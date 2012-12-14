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
    /**
     * An arbitrary prefix to use for automatically assigning ontology IRIs to inferred ontologies.
     * There are no versions delegated to inferred ontologies, and the ontology IRI is generated
     * using the version IRI of the original ontology, which must be unique.
     */
    public static final String INFERRED_PREFIX = "urn:podd:inferred:ontologyiriprefix:";
    
    public static final URI OWL_VERSION_IRI = PoddRdfConstants.VALUE_FACTORY.createURI(OWL.NAMESPACE, "versionIRI");
    
    public static final String PODD_BASE = "http://purl.org/podd/ns/poddBase#";
    
    public static final URI PODDBASE_HAS_PUBLICATION_STATUS = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "hasPublicationStatus");
    
    public static final URI PODDBASE_HAS_TOP_OBJECT = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "artifactHasTopObject");
    
    public static final URI PODDBASE_PUBLISHED = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "Published");
    
    public static final ValueFactory VALUE_FACTORY = ValueFactoryImpl.getInstance();
}
