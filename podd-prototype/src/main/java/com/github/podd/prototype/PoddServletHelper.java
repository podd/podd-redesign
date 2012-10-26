package com.github.podd.prototype;


import java.io.InputStream;
import java.util.UUID;

import net.fortytwo.sesametools.URITranslator;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
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
 * Helper class to the PODD web service servlet containing
 * all PODD business logic.
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
    private RepositoryConnection nextRepositoryConnection;
    private ValueFactory nextValueFactory;

    private PoddPrototypeUtils utils;
    
    private OWLOntologyManager manager;
    private OWLReasonerFactory reasonerFactory;
    private String reasonerName;
    private URI schemaOntologyManagementGraph;
    private URI poddArtifactManagementGraph;
    private IRI pelletOwlProfile;

    private String poddBasePath;
    private String poddSciencePath;
//    private String poddPlantPath;

    public void setUp() throws Exception
    {
        // TODO: use an on disk store
        this.nextRepository = new SailRepository(new MemoryStore());
        this.nextRepository.initialize();
        
        this.nextValueFactory = this.nextRepository.getValueFactory();
        
        this.nextRepositoryConnection = this.nextRepository.getConnection();
        this.nextRepositoryConnection.setAutoCommit(false);
        
        
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
//        this.poddPlantPath = "/ontologies/poddPlant.owl";
    }
    
    
    public void loadSchemaOntologies() throws Exception
    {
        this.utils.loadInferAndStoreSchemaOntology(this.poddBasePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getNextRepositoryConnection());
        
        this.utils.loadInferAndStoreSchemaOntology(this.poddSciencePath, RDFFormat.RDFXML.getDefaultMIMEType(),
                this.getNextRepositoryConnection());
        
        final InferredOWLOntologyID poddArtifact =
                this.utils.loadPoddArtifact("/artifacts/basicProject-1.rdf",
                        RDFFormat.RDFXML.getDefaultMIMEType(), this.getNextRepositoryConnection());
        
        this.log.info("Loaded a single artifact: " + poddArtifact.toString()); 
    }
    
    
    public String loadPoddArtifactPublic(final InputStream inputStream) throws Exception
    {
        return loadPoddArtifact(inputStream).toString();
    }
    
    /**
     * TODO: Copied from PoddPrototypeUtils. Needs to be checked and modified for the web service
     * 
     * @param inputStream
     * @return
     * @throws Exception
     */
    public InferredOWLOntologyID loadPoddArtifact(final InputStream inputStream) throws Exception
    {
        // TODO: shouldn't be hard coded
        String mimeType = RDFFormat.RDFXML.getDefaultMIMEType();
        
        // load into temporary in memory repository to create persistent URLs
        Repository tempRepository = null;
        RepositoryConnection tempRepositoryConnection = null;
        
        try
        {
            tempRepository = new SailRepository(new MemoryStore());
            tempRepository.initialize();
            tempRepositoryConnection = tempRepository.getConnection();
            tempRepositoryConnection.setAutoCommit(false);
            
            // 1. Create permanent identifiers for any impermanent identifiers in the object...
            final URI randomURN =
                    tempRepositoryConnection.getValueFactory().createURI("urn:random:" + UUID.randomUUID().toString());
            
//            final InputStream inputStream = this.getClass().getResourceAsStream(artifactResourcePath);
            
            if(inputStream == null)
            {
                String artifactResourcePath = "{RESOURCE_NAME_HERE}";
                this.log.error("Could not find resource: {}", artifactResourcePath);
                throw new NullPointerException("Could not find resource: " + artifactResourcePath);
            }
            
            tempRepositoryConnection.add(inputStream, "", Rio.getParserFormatForMIMEType(mimeType), randomURN);
            tempRepositoryConnection.commit();
            
            // TODO: improve the permanent URI rather than using http://example.org
            URITranslator.doTranslation(tempRepositoryConnection, "urn:temp:", "http://example.org/permanenturl/"
                    + UUID.randomUUID().toString() + "/", randomURN);
            
            // retrieve list of ontology imports in this artifact
            final URI importURI = IRI.create("http://www.w3.org/2002/07/owl#imports").toOpenRDFURI();
            final RepositoryResult<Statement> importStatements =
                    tempRepositoryConnection.getStatements(null, importURI, null, false, randomURN);
            
            while(importStatements.hasNext())
            {
                final Statement stmt = importStatements.next();
                // get current version IRI for imported ontology
                final RepositoryResult<Statement> currentVersionStatements =
                        nextRepositoryConnection.getStatements(IRI.create(stmt.getObject().stringValue())
                                .toOpenRDFURI(), IRI.create("http://omv.ontoware.org/ontology#currentVersion")
                                .toOpenRDFURI(), null, false, utils.getSchemaGraph());
                
                // update the import statement in artifact to the "current version"
                if(currentVersionStatements.hasNext())
                {
                    final Value currentVersion = currentVersionStatements.next().getObject();
                    tempRepositoryConnection.remove(stmt, randomURN);
                    tempRepositoryConnection.add(stmt.getSubject(), importURI, currentVersion, randomURN);
                }
            }
            tempRepositoryConnection.commit();
            
            this.log.info("Loading podd artifact from repository: {}", randomURN);
            final OWLOntology nextOntology = utils.loadOntology(tempRepositoryConnection, mimeType, randomURN);
            
            // regain memory after loading the ontology into OWLAPI
            tempRepositoryConnection.clear();
            
            // 2. Validate the object in terms of the OWL profile
            // 3. Validate the object using a reasoner
            long startedAt = System.currentTimeMillis();
            final OWLReasoner reasoner = utils.checkConsistency(nextOntology);
//            this.statsLogger.info("checkConsistency:," + (System.currentTimeMillis() - startedAt) + ",");
            
            // 4. Store the object
            utils.dumpOntologyToRepository(nextOntology, nextRepositoryConnection);
            
            // 5. Infer extra statements about the object using a reasoner
            this.log.info("Computing inferences for podd artifact");
            startedAt = System.currentTimeMillis();
            final OWLOntology nextInferredOntology =
                    utils.computeInferences(reasoner, utils.generateInferredOntologyID(nextOntology.getOntologyID()));
//            this.statsLogger.info("computeInferences:,"
//                    + (System.currentTimeMillis() - startedAt) + ",");

            // Dump the triples from the inferred axioms into a separate SPARQL Graph/Context in the
            // Sesame Repository
            // 6. Store the inferred statements
            utils.dumpOntologyToRepository(nextInferredOntology, nextRepositoryConnection);
            
            // 7. Update the PODD Artifact management graph to contain the latest
            // update the link in the PODD Artifact management graph
            utils.updateCurrentManagedPoddArtifactOntologyVersion(nextRepositoryConnection,
                    nextOntology.getOntologyID(), nextInferredOntology.getOntologyID());
            
            return new InferredOWLOntologyID(nextOntology.getOntologyID().getOntologyIRI(), nextOntology
                    .getOntologyID().getVersionIRI(), nextInferredOntology.getOntologyID().getOntologyIRI());
        }
        finally
        {
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
    
    
    public String getArtifact(String uri) throws Exception
    {
        URI context = IRI.create(uri).toOpenRDFURI();
        String mimeType = RDFFormat.RDFXML.getDefaultMIMEType();
        
        OWLOntology ontology = utils.loadOntology(nextRepositoryConnection, mimeType, context);
        // TODO
        
        return ontology.getOntologyID().getVersionIRI().toString();
    }
    
    public String editArtifact(String artifactURI, InputStream in)
    {
        // TODO
        this.log.info("<<<<<Requested edit artifact" + artifactURI);
        return artifactURI;
    }


    public void deleteArtifact(String artifactURI)
    {
        // TODO 
        this.log.info("<<<<<Requested delete artifact" + artifactURI);
    }
    
    
    public void tearDown() throws Exception
    {
        if(this.nextRepositoryConnection != null)
        {
            try
            {
                this.nextRepositoryConnection.close();
            }
            catch(final RepositoryException e)
            {
                this.log.info("Test repository connection could not be closed" + e);
            }
        }
        
        this.nextRepositoryConnection = null;
        
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
    
    public RepositoryConnection getNextRepositoryConnection()
    {
        return nextRepositoryConnection;
    }


    public void setNextRepositoryConnection(RepositoryConnection nextRepositoryConnection)
    {
        this.nextRepositoryConnection = nextRepositoryConnection;
    }


}
