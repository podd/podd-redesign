/**
 * 
 */
package com.github.podd.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
import org.semanticweb.owlapi.formats.RioRDFOntologyFormatFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
import org.semanticweb.owlapi.rio.RioParser;
import org.semanticweb.owlapi.rio.RioParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.file.PoddFileReference;
import com.github.podd.api.file.PoddFileReferenceManager;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlReference;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.PublishArtifactException;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Implementation of the PODD Artifact Manager API, to manage the lifecycle for PODD Artifacts.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddArtifactManagerImpl implements PoddArtifactManager
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private PoddFileReferenceManager fileReferenceManager;
    private PoddOWLManager owlManager;
    private PoddPurlManager purlManager;
    private PoddSchemaManager schemaManager;
    
    /**
     * 
     */
    public PoddArtifactManagerImpl()
    {
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void exportArtifact(final OWLOntologyID ontologyId, final OutputStream outputStream, final RDFFormat format,
            final boolean includeInferred) throws OpenRDFException, PoddException, IOException
    {
        throw new RuntimeException("TODO: Implement exportArtifact");
    }
    
    @Override
    public InferredOWLOntologyID getArtifactByIRI(final IRI artifactIRI)
    {
        throw new RuntimeException("TODO: Implement getArtifactByIRI");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#getFileReferenceManager()
     */
    @Override
    public PoddFileReferenceManager getFileReferenceManager()
    {
        return this.fileReferenceManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#getOWLManager()
     */
    @Override
    public PoddOWLManager getOWLManager()
    {
        return this.owlManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#getPurlManager()
     */
    @Override
    public PoddPurlManager getPurlManager()
    {
        return this.purlManager;
    }
    
    @Override
    public PoddSchemaManager getSchemaManager()
    {
        return this.schemaManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream,
     * org.openrdf.rio.RDFFormat)
     */
    @Override
    public InferredOWLOntologyID loadArtifact(final InputStream inputStream, final RDFFormat format)
        throws OpenRDFException, PoddException, IOException, OWLException
    {
        // connection to the temporary repository that the artifact RDF triples will be stored while
        // they are initially parsed by OWLAPI.
        final RepositoryConnection temporaryRepositoryConnection = null;
        
        final URI randomContext = ValueFactoryImpl.getInstance().createURI(UUID.randomUUID().toString());
        
        // Load the artifact RDF triples into a random context in the temp repository, which may be
        // shared between different uploads
        temporaryRepositoryConnection.add(inputStream, "", format, randomContext);
        
        // TODO: SPARQL query to extract the Ontology IRI and Version IRI from the
        // temporaryRepositoryConnection
        
        // Create an initial OWLOntologyID for the uploaded ontology, after checking that the
        // Version IRI is distinct for the given Ontology IRI
        final OWLOntologyID tempArtifactId = null;
        
        // TODO If the OWLOntologyID is not distinct, then modify it to be distinct
        
        // return the results, setting the results variable to be the same as the internalResults
        // variable from inside of extractFileReferences
        // Ie, return internalResults; results = internalResults;
        
        final Set<PoddPurlReference> purlResults =
                this.getPurlManager().extractPurlReferences(temporaryRepositoryConnection,
                        tempArtifactId.getVersionIRI().toOpenRDFURI());
        
        // perform the conversion of the URIs, possibly in bulk, as they are all given to this
        // method together
        this.getPurlManager().convertTemporaryUris(purlResults, temporaryRepositoryConnection,
                tempArtifactId.getVersionIRI().toOpenRDFURI());
        
        // Then work on the file references
        
        // calls, to setup the results collection
        final Set<PoddFileReference> fileReferenceResults =
                this.getFileReferenceManager().extractFileReferences(temporaryRepositoryConnection,
                        tempArtifactId.getVersionIRI().toOpenRDFURI());
        
        // optionally verify the file references
        this.getFileReferenceManager().verifyFileReferences(fileReferenceResults, temporaryRepositoryConnection,
                tempArtifactId.getVersionIRI().toOpenRDFURI());
        
        // TODO: Optionally remove invalid file references or mark them as invalid using RDF
        // statements/OWL Classes
        
        // Before loading the statements into OWLAPI, ensure that the schema ontologies are cached
        // in memory
        
        // TODO: For each OWL:IMPORTS statement, call the following
        final IRI schemaOntologyIRI = null;
        // Get the current version
        final InferredOWLOntologyID ontologyVersion =
                this.getSchemaManager().getCurrentSchemaOntologyVersion(schemaOntologyIRI);
        // Make sure it is cached in memory. This will not attempt to load the ontology again if it
        // is already cached or already being loaded
        this.getOWLManager().cacheSchemaOntology(ontologyVersion, temporaryRepositoryConnection);
        
        // Load the statements into an OWLAPI OWLOntology
        final RioMemoryTripleSource owlSource =
                new RioMemoryTripleSource(temporaryRepositoryConnection.getStatements(null, null, null, true,
                        tempArtifactId.getVersionIRI().toOpenRDFURI()));
        owlSource.setNamespaces(temporaryRepositoryConnection.getNamespaces());
        
        final OWLOntology nextOntology = this.getOWLManager().loadOntology(owlSource);
        
        // Inside of PoddOWLManager.loadOntology().....
        final RioParser owlParser =
                new RioParserImpl((RioRDFOntologyFormatFactory)OWLOntologyFormatFactoryRegistry.getInstance()
                        .getByMIMEType(format.getDefaultMIMEType()));
        // nextOntology = this.manager.createOntology();
        owlParser.parse(owlSource, nextOntology);
        
        // Check the OWLAPI OWLOntology against an OWLProfile to make sure it is in profile
        final OWLProfileReport profileReport = this.getOWLManager().getReasonerProfile().checkOntology(nextOntology);
        if(!profileReport.isInProfile())
        {
            this.getOWLManager().removeCache(nextOntology.getOntologyID());
        }
        
        // create an OWL Reasoner using the Pellet library and ensure that the reasoner thinks the
        // ontology is consistent so far
        // Use the factory that we found to create a reasoner over the ontology
        final OWLReasoner nextReasoner = this.getOWLManager().createReasoner(nextOntology);
        
        // Test that the ontology was consistent with this reasoner
        // This ensures in the case of Pellet that it is in the OWL2-DL profile
        // if(!nextReasoner.isConsistent() || nextReasoner.getUnsatisfiableClasses().getSize() > 0)
        // Check the OWLAPI OWLOntology using a reasoner for .isConsistent
        if(!nextReasoner.isConsistent())
        {
            this.getOWLManager().removeCache(nextOntology.getOntologyID());
        }
        
        // Once the reasoner determines that the ontology is consistent, copy the statements to a
        // permanent repository connection
        final RepositoryConnection permanentRepositoryConnection = null;
        
        // TODO: Copy the statements to permanentRepositoryConnection
        
        // NOTE: At this stage, a client could be notified, and the artifact could be streamed back
        // to them from permanentRepositoryConnection
        
        final InferredOWLOntologyID inferredOWLOntologyID =
                this.getOWLManager().generateInferredOntologyID(nextOntology.getOntologyID());
        
        // Use an OWLAPI InferredAxiomGenerator together with the reasoner to create inferred
        // axioms to store in the database
        // Serialise the inferred statements back to a different context in the permanent
        // repository connection
        // The contexts to use within the permanent repository connection are all encapsulated in
        // the InferredOWLOntologyID object
        this.getOWLManager().inferStatements(inferredOWLOntologyID, permanentRepositoryConnection);
        
        // make sure in a finally block to remove the cache for the ontology
        this.getOWLManager().removeCache(inferredOWLOntologyID);
        
        // return InferredOWLOntologyID() with the context of the inferred statements
        
        return inferredOWLOntologyID;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#publishArtifact(org.semanticweb.owlapi.model.
     * OWLOntologyID)
     */
    @Override
    public InferredOWLOntologyID publishArtifact(final OWLOntologyID ontologyId) throws PublishArtifactException
    {
        if(ontologyId.getVersionIRI() == null)
        {
            throw new PublishArtifactException("Could not publish artifact as version was not specified.", ontologyId);
        }
        
        if(this.getOWLManager().isPublished(ontologyId.getOntologyIRI()))
        {
            // Cannot publish multiple versions of a single artifact
            throw new PublishArtifactException("Could not publish artifact as a version was already published",
                    ontologyId);
        }
        
        final OWLOntologyID currentVersion = this.getOWLManager().getCurrentVersion(ontologyId.getOntologyIRI());
        
        if(!currentVersion.getVersionIRI().equals(ontologyId.getVersionIRI()))
        {
            // User must make the given artifact version the current version manually before
            // publishing, to ensure that work from the current version is not lost accidentally
            throw new PublishArtifactException("Could not publish artifact as it was not the most current version.",
                    ontologyId);
        }
        
        final InferredOWLOntologyID published = this.getOWLManager().setPublished(ontologyId);
        
        return published;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.PoddArtifactManager#setFileReferenceManager(com.github.podd.api.file.
     * PoddFileReferenceManager)
     */
    @Override
    public void setFileReferenceManager(final PoddFileReferenceManager fileManager)
    {
        this.fileReferenceManager = fileManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.PoddArtifactManager#setOwlManager(com.github.podd.api.PoddOWLManager)
     */
    @Override
    public void setOwlManager(final PoddOWLManager owlManager)
    {
        this.owlManager = owlManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.PoddArtifactManager#setPurlManager(com.github.podd.api.purl.PoddPurlManager
     * )
     */
    @Override
    public void setPurlManager(final PoddPurlManager purlManager)
    {
        this.purlManager = purlManager;
    }
    
    @Override
    public void setSchemaManager(final PoddSchemaManager schemaManager)
    {
        this.schemaManager = schemaManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#updateSchemaImport(org.semanticweb.owlapi.model.
     * OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID)
     */
    @Override
    public void updateSchemaImport(final OWLOntologyID artifactId, final OWLOntologyID schemaOntologyId)
    {
        throw new RuntimeException("TODO: Implement updateSchemaImport");
    }
    
}
