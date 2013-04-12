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

    /** Path to default alias file */
    public static final String PATH_DEFAULT_ALIASES_FILE = "/alias.ttl";
    
    /** Path to dcTerms.owl */
    public static final String PATH_PODD_DCTERMS = "/ontologies/dcTerms.owl";

    /** Path to foaf.owl */
    public static final String PATH_PODD_FOAF = "/ontologies/foaf.owl";

    /** Path to poddUser.owl */
    public static final String PATH_PODD_USER = "/ontologies/poddUser.owl";

    /** Path to poddBase.owl */
    public static final String PATH_PODD_BASE = "/ontologies/poddBase.owl";

    /** Path to poddScience.owl */
    public static final String PATH_PODD_SCIENCE = "/ontologies/poddScience.owl";

    /** Path to poddPlant.owl */
    public static final String PATH_PODD_PLANT = "/ontologies/poddPlant.owl";

    /** Path to poddAnimal.owl */
    public static final String PATH_PODD_ANIMAL = "/ontologies/poddAnimal.owl";

    
    public static final String PODD_DCTERMS = "http://purl.org/podd/ns/dcTerms#";

    public static final String PODD_FOAF = "http://purl.org/podd/ns/foaf#";

    public static final String PODD_USER = "http://purl.org/podd/ns/poddUser#";

    public static final String PODD_BASE = "http://purl.org/podd/ns/poddBase#";
    
    public static final String PODD_SCIENCE = "http://purl.org/podd/ns/poddScience#";

    public static final String PODD_PLANT = "http://purl.org/podd/ns/poddPlant#";

    
    /**
     * An arbitrary prefix to use for automatically assigning ontology IRIs to inferred ontologies.
     * There are no versions delegated to inferred ontologies, and the ontology IRI is generated
     * using the version IRI of the original ontology, which must be unique.
     */
    public static final String INFERRED_PREFIX = "urn:podd:inferred:ontologyiriprefix:";

    
    
    /** Default value is urn:podd:default:artifactmanagementgraph:  */
    public static final URI DEFAULT_ARTIFACT_MANAGEMENT_GRAPH = PoddRdfConstants.VALUE_FACTORY
            .createURI("urn:podd:default:artifactmanagementgraph:");
    
    /** Default value is urn:podd:default:schemamanagementgraph  */
    public static final URI DEFAULT_SCHEMA_MANAGEMENT_GRAPH = PoddRdfConstants.VALUE_FACTORY
            .createURI("urn:podd:default:schemamanagementgraph");

    /** Default value is urn:podd:default:usermanagementgraph:  */
    public static final URI DEF_USER_MANAGEMENT_GRAPH = VALUE_FACTORY
            .createURI("urn:podd:default:usermanagementgraph:");

    public static final URI DEFAULT_FILE_REPOSITORY_MANAGEMENT_GRAPH = VALUE_FACTORY
            .createURI("urn:podd:default:filerepositorymanagementgraph:");

    
    
    public static final URI OWL_MAX_QUALIFIED_CARDINALITY = PoddRdfConstants.VALUE_FACTORY
            .createURI("http://www.w3.org/2002/07/owl#maxQualifiedCardinality");

    public static final URI OWL_MIN_QUALIFIED_CARDINALITY = PoddRdfConstants.VALUE_FACTORY
            .createURI("http://www.w3.org/2002/07/owl#minQualifiedCardinality");

    public static final URI OWL_QUALIFIED_CARDINALITY = PoddRdfConstants.VALUE_FACTORY
            .createURI("http://www.w3.org/2002/07/owl#qualifiedCardinality");

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
     */
    public static final URI PODD_BASE_CURRENT_INFERRED_VERSION = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "currentInferredVersion");
    
    
    public static final String HTTP = "http://www.w3.org/2011/http#";
    
    /** http://www.w3.org/2011/http#statusCodeValue */
    public static final URI HTTP_STATUS_CODE_VALUE = ValueFactoryImpl.getInstance().createURI(
            PoddRdfConstants.HTTP, "statusCodeValue");

    /** http://www.w3.org/2011/http#reasonPhrase */
    public static final URI HTTP_REASON_PHRASE = ValueFactoryImpl.getInstance().createURI(
            PoddRdfConstants.HTTP, "reasonPhrase");

    
    /**
     * Creating a property for PODD to track the inferredVersion for the inferred axioms ontology of
     * a particular versioned ontology.
     */
    public static final URI PODD_BASE_INFERRED_VERSION = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "inferredVersion");

    
    public static final URI PODD_BASE_HAS_PUBLICATION_STATUS = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "hasPublicationStatus");
    
    public static final URI PODD_BASE_HAS_TOP_OBJECT = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "artifactHasTopObject");
    
    public static final URI PODD_BASE_PUBLISHED = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "Published");

    public static final URI PODD_BASE_NOT_PUBLISHED = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "NotPublished");
    
    
    
    public static final URI PODD_BASE_WEIGHT = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "weight");

    public static final URI PODD_BASE_DO_NOT_DISPLAY = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "doNotDisplay");
    
    public static final URI PODD_BASE_CONTAINS = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "contains");

    /** http://purl.org/podd/ns/poddBase#hasDisplayType */
    public static final URI PODD_BASE_DISPLAY_TYPE = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_BASE,
            "hasDisplayType");

    /** http://purl.org/podd/ns/poddBase#DisplayType_ShortText */
    public static final URI PODD_BASE_DISPLAY_TYPE_SHORTTEXT = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "DisplayType_ShortText");
    
    /** http://purl.org/podd/ns/poddBase#DisplayType_LongText */
    public static final URI PODD_BASE_DISPLAY_TYPE_LONGTEXT = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "DisplayType_LongText");
    
    /** http://purl.org/podd/ns/poddBase#DisplayType_DropDownList */
    public static final URI PODD_BASE_DISPLAY_TYPE_DROPDOWN = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "DisplayType_DropDownList");
    
    /** http://purl.org/podd/ns/poddBase#DisplayType_CheckBox */
    public static final URI PODD_BASE_DISPLAY_TYPE_CHECKBOX = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "DisplayType_CheckBox");
    
    /** http://purl.org/podd/ns/poddBase#DisplayType_Table */
    public static final URI PODD_BASE_DISPLAY_TYPE_TABLE = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "DisplayType_Table");
    
    /** http://purl.org/podd/ns/poddBase#hasFileReference */
    public static final URI PODD_BASE_FILE_REFERENCE = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "hasFileReference");

    /** http://purl.org/podd/ns/poddBase#hasFileName */
    public static final URI PODD_BASE_FILENAME = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "hasFileName");

    /** http://purl.org/podd/ns/poddBase#hasPath */
    public static final URI PODD_BASE_FILE_PATH = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "hasPath");

    /** http://purl.org/podd/ns/poddBase#hasAlias */
    public static final URI PODD_BASE_ALIAS = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "hasAlias");


    /** http://purl.org/podd/ns/poddBase#FileReference */
    public static final URI PODD_BASE_FILE_REFERENCE_TYPE = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "FileReference");
    
    /** http://purl.org/podd/ns/poddBase#SSHFileReference */
    public static final URI PODD_BASE_FILE_REFERENCE_TYPE_SSH = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_BASE, "SSHFileReference");
    
    /** http://purl.org/podd/ns/poddBase#FileRepository */
    public static final URI PODD_FILE_REPOSITORY = ValueFactoryImpl.getInstance().createURI(PoddRdfConstants.PODD_BASE,
            "FileRepository");
    
    /** http://purl.org/podd/ns/poddBase#SSHFileRepository */
    public static final URI PODD_SSH_FILE_REPOSITORY = ValueFactoryImpl.getInstance().createURI(
            PoddRdfConstants.PODD_BASE, "SSHFileRepository");
    
    public static final URI PODD_HTTP_FILE_REPOSITORY = ValueFactoryImpl.getInstance().createURI(
            PoddRdfConstants.PODD_BASE, "HTTPFileRepository");
    
    /** http://purl.org/podd/ns/poddBase#hasAlias */
    public static final URI PODD_FILE_REPOSITORY_ALIAS = ValueFactoryImpl.getInstance().createURI(
            PoddRdfConstants.PODD_BASE, "hasAlias");
    
    /** http://purl.org/podd/ns/poddBase#hasFileRepositoryProtocol */
    public static final URI PODD_FILE_REPOSITORY_PROTOCOL = ValueFactoryImpl.getInstance().createURI(
            PoddRdfConstants.PODD_BASE, "hasFileRepositoryProtocol");
    
    /** http://purl.org/podd/ns/poddBase#hasFileRepositoryHost */
    public static final URI PODD_FILE_REPOSITORY_HOST = ValueFactoryImpl.getInstance().createURI(
            PoddRdfConstants.PODD_BASE, "hasFileRepositoryHost");
    
    /** http://purl.org/podd/ns/poddBase#hasFileRepositoryPort */
    public static final URI PODD_FILE_REPOSITORY_PORT = ValueFactoryImpl.getInstance().createURI(
            PoddRdfConstants.PODD_BASE, "hasFileRepositoryPort");
    
    /** http://purl.org/podd/ns/poddBase#hasFileRepositoryFingerprint */
    public static final URI PODD_FILE_REPOSITORY_FINGERPRINT = ValueFactoryImpl.getInstance().createURI(
            PoddRdfConstants.PODD_BASE, "hasFileRepositoryFingerprint");
    
    /** http://purl.org/podd/ns/poddBase#hasFileRepositoryUsername */
    public static final URI PODD_FILE_REPOSITORY_USERNAME = ValueFactoryImpl.getInstance().createURI(
            PoddRdfConstants.PODD_BASE, "hasFileRepositoryUsername");
    
    /** http://purl.org/podd/ns/poddBase#hasFileRepositorySecret */
    public static final URI PODD_FILE_REPOSITORY_SECRET = ValueFactoryImpl.getInstance().createURI(
            PoddRdfConstants.PODD_BASE, "hasFileRepositorySecret");

    
}
