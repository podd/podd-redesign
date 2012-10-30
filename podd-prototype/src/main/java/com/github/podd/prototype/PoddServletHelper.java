package com.github.podd.prototype;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.UUID;

import net.fortytwo.sesametools.URITranslator;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to the PODD web service servlet containing all PODD business logic.
 * 
 * FIXME: pretty much copied from the test class and incomplete
 * 
 * @author kutila
 * @created 2012/10/25
 * 
 */
public class PoddServletHelper
{
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    private Repository nextRepository;
    
    // private RepositoryConnection nextRepositoryConnection;
    
    private ValueFactory nextValueFactory;
    
    private PoddPrototypeUtils utils;
    
    private OWLOntologyManager manager;
    private OWLReasonerFactory reasonerFactory;
    private String reasonerName;
    private URI schemaOntologyManagementGraph;
    private URI poddArtifactManagementGraph;
    private IRI pelletOwlProfile;
    private URI owlVersionIRI;
    private URI owlInferredVersionIRI;
    
    private String poddBasePath;
    private String poddSciencePath;
    
    // private String poddPlantPath;
    
    public void setUp() throws Exception
    {
        // TODO: use an on disk store
        this.nextRepository = new SailRepository(new MemoryStore());
        this.nextRepository.initialize();
        
        this.nextValueFactory = this.nextRepository.getValueFactory();
        
        // create the manager to use for the test
        this.manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        
        // We're only supporting OWL2_DL and Pellet in this prototype
        this.reasonerName = "Pellet";
        this.reasonerFactory = OWLReasonerFactoryRegistry.getInstance().getReasonerFactory(this.reasonerName);
        
        this.pelletOwlProfile = OWLProfile.OWL2_DL;
        
        this.schemaOntologyManagementGraph =
                this.nextValueFactory.createURI("urn:test:schemaOntologiesManagementGraph");
        this.poddArtifactManagementGraph = this.nextValueFactory.createURI("urn:test:poddArtifactManagementGraph");
        
        this.utils =
                new PoddPrototypeUtils(this.manager, this.pelletOwlProfile, this.reasonerFactory,
                        this.schemaOntologyManagementGraph, this.poddArtifactManagementGraph);
        
        this.poddBasePath = "/ontologies/poddBase.owl";
        this.poddSciencePath = "/ontologies/poddScience.owl";
        // this.poddPlantPath = "/ontologies/poddPlant.owl";
        
        this.owlVersionIRI = this.nextValueFactory.createURI(OWL.NAMESPACE, "versionIRI");
        this.owlInferredVersionIRI =
                this.nextValueFactory.createURI("http://purl.org/podd/ns/poddBase#inferredVersion");
        
    }
    
    /**
     * The prototype does not yet support uploading of new Schema Ontologies. Therefore, this method
     * should be called at initialization to load the schemas.
     * 
     * @throws Exception
     */
    public void loadSchemaOntologies() throws Exception
    {
        final RepositoryConnection nextRepositoryConnection = this.getRepositoryConnection();
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                nextRepositoryConnection);
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                nextRepositoryConnection);
        
        // XXXX: adding a test artifact to debug the GET process
        // final InferredOWLOntologyID poddArtifact =
        // this.utils.loadPoddArtifact("/artifacts/basicProject-1.rdf",
        // RDFFormat.RDFXML.getDefaultMIMEType(), this.getNextRepositoryConnection());
        //
        // this.log.info("Loaded a single artifact: " + poddArtifact.toString());
        this.returnRepositoryConnection(nextRepositoryConnection);
    }
    
    /**
     * Add a new artifact to PODD TODO: support different mime types
     * 
     * 
     * @param inputStream
     * @param contentType
     * @return
     * @throws Exception
     */
    public String loadPoddArtifact(final InputStream inputStream, final String contentType) throws Exception
    {
        final InferredOWLOntologyID nextOntology = this.loadPoddArtifactInternal(inputStream, contentType);
        
        return this.getArtifact(nextOntology.getOntologyIRI().toString(), contentType, false);
    }
    
    /**
     * TODO: Copied from PoddPrototypeUtils. Needs to be checked and modified for the web service
     * 
     * Loading an artifact consists of several steps. - Assign permanent URIs to any that have
     * temporary values - Fix imported ontologies to import their current versions - Check
     * consistency of ontology - Compute inferences - Store asserted and inferred ontologies in
     * Repository
     * 
     * @param inputStream
     * @param mimeType
     * @return
     * @throws Exception
     */
    public InferredOWLOntologyID loadPoddArtifactInternal(final InputStream inputStream, final String mimeType)
        throws Exception
    {
        this.log.info("ADD artifact: " + mimeType);
        
        if(inputStream == null)
        {
            final String errorMessage = "Could not read artifact from input stream";
            this.log.error(errorMessage);
            throw new NullPointerException(errorMessage);
        }
        
        // load on to a temporary in-memory repository to create persistent URLs
        Repository tempRepository = null;
        RepositoryConnection tempRepositoryConnection = null;
        final RepositoryConnection repositoryConnection = this.getRepositoryConnection();
        
        try
        {
            tempRepository = new SailRepository(new MemoryStore());
            tempRepository.initialize();
            tempRepositoryConnection = tempRepository.getConnection();
            tempRepositoryConnection.setAutoCommit(false);
            
            // 1. Create permanent identifiers for any impermanent identifiers in the object...
            final URI randomURN =
                    tempRepositoryConnection.getValueFactory().createURI("urn:random:" + UUID.randomUUID().toString());
            
            tempRepositoryConnection.add(inputStream, "", Rio.getParserFormatForMIMEType(mimeType), randomURN);
            tempRepositoryConnection.commit();
            
            // TODO: set HOST component to match current host so that URI is resolvable
            URITranslator.doTranslation(tempRepositoryConnection, "urn:temp:", "http://example.org/permanenturl/"
                    + UUID.randomUUID().toString() + "/", randomURN);
            
            // retrieve list of imported ontologies and update them to the "current version"
            final URI importURI = IRI.create("http://www.w3.org/2002/07/owl#imports").toOpenRDFURI();
            final RepositoryResult<Statement> importStatements =
                    tempRepositoryConnection.getStatements(null, importURI, null, false, randomURN);
            
            while(importStatements.hasNext())
            {
                final Statement stmt = importStatements.next();
                // get current version IRI for imported ontology
                final RepositoryResult<Statement> currentVersionStatements =
                        repositoryConnection.getStatements(IRI.create(stmt.getObject().stringValue()).toOpenRDFURI(),
                                IRI.create("http://omv.ontoware.org/ontology#currentVersion").toOpenRDFURI(), null,
                                false, this.utils.getSchemaGraph());
                
                if(currentVersionStatements.hasNext())
                {
                    final Value currentVersion = currentVersionStatements.next().getObject();
                    tempRepositoryConnection.remove(stmt, randomURN);
                    tempRepositoryConnection.add(stmt.getSubject(), importURI, currentVersion, randomURN);
                }
            }
            tempRepositoryConnection.commit();
            
            this.log.info("Loading podd artifact from repository: {}", randomURN);
            final OWLOntology nextOntology =
                    this.utils.loadOntology(tempRepositoryConnection, RDFFormat.RDFXML.getDefaultMIMEType(), randomURN);
            
            // regain memory after loading the ontology into OWLAPI
            tempRepositoryConnection.clear();
            
            // 2. Validate the object in terms of the OWL profile
            // 3. Validate the object using a reasoner
            final OWLReasoner reasoner = this.utils.checkConsistency(nextOntology);
            
            // 4. Store the object
            this.utils.dumpOntologyToRepository(nextOntology, repositoryConnection);
            
            // 5. Infer extra statements about the object using a reasoner
            this.log.info("Computing inferences for podd artifact");
            final OWLOntology nextInferredOntology =
                    this.utils.computeInferences(reasoner,
                            this.utils.generateInferredOntologyID(nextOntology.getOntologyID()));
            
            // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
            // Sesame Repository
            // 6. Store the inferred statements
            this.utils.dumpOntologyToRepository(nextInferredOntology, repositoryConnection);
            
            // 7. Update the PODD Artifact management graph to contain the latest
            // update the link in the PODD Artifact management graph
            this.utils.updateCurrentManagedPoddArtifactOntologyVersion(repositoryConnection,
                    nextOntology.getOntologyID(), nextInferredOntology.getOntologyID());
            
            return new InferredOWLOntologyID(nextOntology.getOntologyID().getOntologyIRI(), nextOntology
                    .getOntologyID().getVersionIRI(), nextInferredOntology.getOntologyID().getOntologyIRI());
        }
        finally
        {
            this.returnRepositoryConnection(repositoryConnection);
            if(tempRepositoryConnection != null)
            {
                tempRepositoryConnection.rollback();
                tempRepositoryConnection.close();
            }
            if(tempRepository != null)
            {
                tempRepository.shutDown();
            }
        }
    }
    
    /**
     * Get the specified artifact if it exists inside PODD.
     * 
     * TODO: include inferred statements if requested
     * 
     * @param artifactUri
     * @return
     * @throws Exception
     */
    public String getArtifact(final String artifactUri, final String mimeType, final boolean includeInferredStatements)
        throws Exception
    {
        this.log.info("GET artifact: " + artifactUri);
        
        final RepositoryConnection repositoryConnection = this.getRepositoryConnection();
        try
        {
            final String versionIRIString =
                    this.getArtifactVersionIRI(artifactUri, includeInferredStatements, repositoryConnection);
            final URI versionedArtifactIRI = IRI.create(versionIRIString).toOpenRDFURI();
            
            if(repositoryConnection.size(versionedArtifactIRI) < 1)
            {
                throw new Exception("Artifact <" + artifactUri + "> not found.");
            }
            
            // get InferredOwlOntologyID for this artifact
            // export 1. asserted 2. inferred statements through RDF Handler
            final URI inferredArtifactIRI = null;
            // IN PROGRESS HERE!
            
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final RDFHandler rdfWriter =
                    Rio.createWriter(Rio.getWriterFormatForMIMEType(mimeType, RDFFormat.RDFXML), out);
            
            repositoryConnection.export(rdfWriter, versionedArtifactIRI);
            
            return out.toString();
        }
        finally
        {
            this.returnRepositoryConnection(repositoryConnection);
        }
    }
    
    /**
     * Delete the specified artifact and all references to it.
     * 
     * @param artifactURI
     */
    public String deleteArtifact(final String artifactUri) throws Exception
    {
        this.log.info("DELETE artifact: " + artifactUri);
        
        final RepositoryConnection repositoryConnection = this.getRepositoryConnection();
        try
        {
            final URI artifactNormalURI = IRI.create(artifactUri).toOpenRDFURI();
            final String versionIRIString = this.getArtifactVersionIRI(artifactUri, true, repositoryConnection);
            final URI versionedArtifactIRI = IRI.create(versionIRIString).toOpenRDFURI();
            
            if(repositoryConnection.size(versionedArtifactIRI) < 1)
            {
                throw new Exception("Artifact <" + artifactUri + "> not found.");
            }
            
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final RDFHandler rdfWriter =
                    Rio.createWriter(Rio.getWriterFormatForMIMEType(PoddServlet.MIME_TYPE_RDF_XML, RDFFormat.RDFXML),
                            out);
            
            repositoryConnection.export(rdfWriter, versionedArtifactIRI);
            repositoryConnection.clear(versionedArtifactIRI);
            
            // remove references in the artifact management graph
            final RepositoryResult<Statement> repoResult =
                    repositoryConnection.getStatements(artifactNormalURI,
                            PoddPrototypeUtils.PODD_BASE_INFERRED_VERSION, null, false,
                            this.poddArtifactManagementGraph);
            while(repoResult.hasNext())
            {
                final String inferredOnto = repoResult.next().getObject().toString();
                repositoryConnection.remove(IRI.create(inferredOnto).toOpenRDFURI(), null, null,
                        this.poddArtifactManagementGraph);
            }
            repositoryConnection.remove(artifactNormalURI, null, null, this.poddArtifactManagementGraph);
            repositoryConnection.remove(versionedArtifactIRI, null, null, this.poddArtifactManagementGraph);
            
            repositoryConnection.commit();
            return out.toString();
        }
        finally
        {
            this.returnRepositoryConnection(repositoryConnection);
        }
        
    }
    
    /**
     * Allows a part of the artifact to be modified
     * 
     * @param artifactURI
     * @param in
     * @param contentType
     * @return
     */
    public String editArtifact(final String artifactURI, final InputStream in, final String contentType)
    {
        // TODO
        this.log.info("EDIT artifact: " + artifactURI);
        return artifactURI;
    }
    
    public InferredOWLOntologyID getInferredOntologyID(final String artifactUri,
            final RepositoryConnection repositoryConnection) throws Exception
    {
        final URI ontologyURI = IRI.create(artifactUri).toOpenRDFURI();
        final RepositoryResult<Statement> repoResult =
                repositoryConnection.getStatements(ontologyURI, null, null, true, this.poddArtifactManagementGraph);
        
        while(repoResult.hasNext())
        {
            System.out.println("xxx " + repoResult.next());
            // IN PROGRESS HERE!
            
            // if (this.owlInferredVersionIRI.equals(repoResult.next().getPredicate())
        }
        
        return null;
    }
    
    private String getArtifactVersionIRI(final String artifactUri, final boolean includeInferredStatements,
            final RepositoryConnection repositoryConnection) throws RepositoryException
    {
        final URI context = IRI.create(artifactUri).toOpenRDFURI();
        final RepositoryResult<Statement> repoResult =
                repositoryConnection.getStatements(context, this.owlVersionIRI, null, includeInferredStatements,
                        this.poddArtifactManagementGraph);
        
        if(repoResult.hasNext())
        {
            return repoResult.next().getObject().stringValue();
        }
        else
        {
            return artifactUri;
        }
    }
    
    /**
     * Converts incoming (URL-encoded) URI strings to a format compliant with the one used by PODD.
     * 
     * 1. The scheme is separated by a '/' only. Replace it with '://' 2. URL encode the path and
     * any fragments.
     * 
     * Examples:
     * 
     * "https/thebank.org/myaccount#55" --> "https://thebank.org/myaccount%2355".
     * 
     * "http/example.org/alpha/artifact:1:0:5:22" -->
     * "http://example.org/alpha/artifact%3A1%3A0%3A5%3A22"
     * 
     * "https/thebank.org/myaccount%2355" --> "https://thebank.org/myaccount%2355"
     * 
     * Note: Currently supports http and https protocols/schemes only.
     * 
     * @param uriPath
     *            URL encoded string of the form "http/host.com/..."
     * @return The converted URI
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException
     */
    public static String extractUri(String uriPath) throws URISyntaxException, UnsupportedEncodingException
    {
        if(uriPath == null || uriPath.length() < 6)
        {
            throw new URISyntaxException(uriPath, "Too short to be a URI in the expected format");
        }
        
        // URL encode the fragment part as SesameTools URITranslator URL encodes them when putting
        // into Sesame
        try
        {
            // URL decode it first to make sure we don't double encode
            uriPath = java.net.URLDecoder.decode(uriPath, "UTF-8");
            
            // URL encode the part after the last '/'
            final String hostPath = uriPath.substring(0, uriPath.lastIndexOf("/") + 1);
            final String toEncode = uriPath.substring(uriPath.lastIndexOf("/") + 1);
            
            final String encoded = java.net.URLEncoder.encode(toEncode, "UTF-8");
            uriPath = hostPath.concat(encoded);
        }
        catch(final UnsupportedEncodingException e)
        {
            throw e;
        }
        
        if(uriPath.startsWith("http/"))
        {
            uriPath = uriPath.replaceFirst("http/", "http://");
        }
        else if(uriPath.startsWith("https/"))
        {
            uriPath = uriPath.replaceFirst("https/", "https://");
        }
        
        return uriPath;
    }
    
    public void tearDown() throws Exception
    {
        this.nextValueFactory = null;
        
        if(this.nextRepository != null)
        {
            try
            {
                this.nextRepository.shutDown();
            }
            catch(final RepositoryException e)
            {
                this.log.info("Test repository could not be shutdown" + e);
            }
        }
        
        this.nextRepository = null;
    }
    
    /**
     * Get a new RepositoryConnection object. Remember to "return" it after you're done using it.
     * 
     * @return
     * @throws RepositoryException
     */
    public RepositoryConnection getRepositoryConnection() throws RepositoryException
    {
        final RepositoryConnection nextRepositoryConnection = this.nextRepository.getConnection();
        nextRepositoryConnection.setAutoCommit(false);
        return nextRepositoryConnection;
    }
    
    /**
     * A used RepositoryConnection should be "returned" using this method so that the system can
     * discard/reuse it as appropriate.
     * 
     * @param repositoryConnection
     */
    public void returnRepositoryConnection(final RepositoryConnection repositoryConnection)
    {
        if(repositoryConnection != null)
        {
            try
            {
                repositoryConnection.close();
            }
            catch(final RepositoryException e)
            {
                // ignore
            }
        }
    }
    
    private void printGraph(final URI context, final RepositoryConnection con)
    {
        System.out.println("====Printing Graph=====");
        org.openrdf.repository.RepositoryResult<Statement> results;
        try
        {
            results = con.getStatements(null, null, null, false, context);
            while(results.hasNext())
            {
                final Statement triple = results.next();
                final String tripleStr = java.net.URLDecoder.decode(triple.toString(), "UTF-8");
                System.out.println(" --- " + tripleStr);
            }
        }
        catch(final Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
