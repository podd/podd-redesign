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
package com.github.podd.api.test;

import info.aduna.iteration.Iterations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.DanglingObjectPolicy;
import com.github.podd.api.DataReferenceVerificationPolicy;
import com.github.podd.api.MetadataPolicy;
import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.UpdatePolicy;
import com.github.podd.api.file.DataReferenceManager;
import com.github.podd.api.file.DataReferenceProcessorFactory;
import com.github.podd.api.file.DataReferenceProcessorRegistry;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
import com.github.podd.exception.ArtifactModifyException;
import com.github.podd.exception.DeleteArtifactException;
import com.github.podd.exception.DisconnectedObjectException;
import com.github.podd.exception.EmptyOntologyException;
import com.github.podd.exception.InconsistentOntologyException;
import com.github.podd.exception.OntologyNotInProfileException;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.PublishedArtifactModifyException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedArtifactVersionException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public abstract class AbstractPoddArtifactManagerTest
{
    /**
     * All of the unit tests individually timeout after 60 seconds.
     */
    @Rule
    public Timeout timeout = new Timeout(60000);
    
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    private PoddArtifactManager testArtifactManager;
    private PoddRepositoryManager testRepositoryManager;
    private PoddSchemaManager testSchemaManager;
    private PoddSesameManager testSesameManager;
    
    private RepositoryConnection testRepositoryConnection;
    
    private URI schemaGraph;
    
    private URI artifactGraph;
    
    /**
     * Write contents of specified context to a file
     * 
     * @param context
     * @param filename
     * @param writeFormat
     * @throws IOException
     * @throws OpenRDFException
     */
    public void dumpRdfToFile(final URI context, final String filename, final RDFFormat writeFormat)
        throws IOException, OpenRDFException
    {
        final String outFilename = filename + "." + writeFormat.getFileExtensions().get(0);
        
        final RDFWriter writer = Rio.createWriter(writeFormat, new FileOutputStream(filename));
        writer.handleNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        writer.handleNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
        writer.handleNamespace("owl", "http://www.w3.org/2002/07/owl#");
        writer.handleNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        writer.handleNamespace("xml", "http://www.w3.org/XML/1998/namespace");
        
        writer.handleNamespace("dc", "http://purl.org/podd/ns/dcTerms#");
        
        writer.startRDF();
        
        final List<Statement> inferredList =
                Iterations.asList(this.testRepositoryConnection.getStatements(null, null, null, false, context));
        for(final Statement s : inferredList)
        {
            writer.handleStatement(s);
        }
        writer.endRDF();
        this.log.info("Wrote {} statements to file {}", inferredList.size(), outFilename);
    }
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of PoddArtifactManager
     * for each invocation.
     * 
     * @return A new empty instance of an implementation of PoddArtifactManager.
     */
    protected abstract PoddArtifactManager getNewArtifactManager();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * PoddPurlProcessorFactory that can process DOI references for each invocation.
     * 
     * @return A new empty instance of an implementation of PoddPurlProcessorFactory that can
     *         process DOI references.
     */
    protected abstract PoddPurlProcessorFactory getNewDoiPurlProcessorFactory();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * PoddFileReferenceManager.
     * 
     * @return A new empty instance of an implementation of PoddFileReferenceManager.
     */
    protected abstract DataReferenceManager getNewFileReferenceManager();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * PoddPurlProcessorFactory that can process Handle references for each invocation.
     * 
     * @return A new empty instance of an implementation of PoddPurlProcessorFactory that can
     *         process Handle references.
     */
    protected abstract PoddPurlProcessorFactory getNewHandlePurlProcessorFactory();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * PoddFileReferenceProcessorFactory that can process HTTP-based file references for each
     * invocation.
     * 
     * @return A new empty instance of an implementation of PoddFileReferenceProcessorFactory that
     *         can process HTTP-based file references.
     */
    protected abstract DataReferenceProcessorFactory getNewHttpFileReferenceProcessorFactory();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of {@link PoddOWLManager}
     * .
     * 
     * @return A new empty instance of an implementation of PoddOWLManager.
     */
    protected abstract PoddOWLManager getNewOWLManager();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * {@link PoddPurlManager}.
     * 
     * @return A new empty instance of an implementation of PoddPurlManager.
     */
    protected abstract PoddPurlManager getNewPurlManager();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * {@link OWLReasonerFactory} that can be used with the {@link PoddOWLManager}.
     * 
     * @return A new empty instance of an implementation of OWLReasonerFactory.
     */
    protected abstract OWLReasonerFactory getNewReasonerFactory();
    
    /**
     * Concrete tests must override this to provide a new, initialised, instance of
     * {@link PoddRepositoryManager} with the desired {@link Repository} for this test.
     * 
     * @return A new, initialised. instance of {@link PoddRepositoryManager}
     * @throws OpenRDFException
     *             If there were problems creating or initialising the Repository.
     */
    protected abstract PoddRepositoryManager getNewRepositoryManager() throws OpenRDFException;
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * {@link PoddSchemaManager}.
     * 
     * @return A new empty instance of an implementation of PoddSchemaManager.
     */
    protected abstract PoddSchemaManager getNewSchemaManager();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * {@link PoddSesameManager}.
     * 
     * @return
     */
    protected abstract PoddSesameManager getNewSesameManager();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * {@link DataReferenceProcessorFactory} that can process SSH-based file references for each
     * invocation.
     * 
     * @return A new empty instance of an implementation of PoddFileReferenceProcessorFactory that
     *         can process SSH-based file references.
     */
    protected abstract DataReferenceProcessorFactory getNewSSHFileReferenceProcessorFactory();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * {@link PoddPurlProcessorFactory} that can process UUID references for each invocation.
     * 
     * @return A new empty instance of an implementation of PoddPurlProcessorFactory that can
     *         process UUID references.
     */
    protected abstract PoddPurlProcessorFactory getNewUUIDPurlProcessorFactory();
    
    private final void internalTestExportObjectmetadata(final InferredOWLOntologyID artifactID) throws Exception
    {
        
        // Format: Object Type, includeDoNotDisplayProperties, includeContainsSubProperties,
        // expected model size, expected property count, do-not-display statement count
        final Object[][] testData =
                {
                        { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_BASE, "NoSuchObjectType"), false,
                                MetadataPolicy.INCLUDE_ALL, 0, 0, 0 },
                        
                        { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Project"), false,
                                MetadataPolicy.INCLUDE_ALL, 139, 17, 0 },
                        { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Project"), false,
                                MetadataPolicy.EXCLUDE_CONTAINS, 83, 10, 0 },
                        { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Project"), true,
                                MetadataPolicy.INCLUDE_ALL, 283, 34, 14 },
                        { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Project"), false,
                                MetadataPolicy.ONLY_CONTAINS, 42, 7, 0 },
                        
                        { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Publication"), false,
                                MetadataPolicy.INCLUDE_ALL, 82, 10, 0 },
                        { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Publication"), true,
                                MetadataPolicy.INCLUDE_ALL, 115, 15, 4 },
                        { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Publication"), false,
                                MetadataPolicy.ONLY_CONTAINS, 0, 0, 0 },
                        
                        { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Environment"), false,
                                MetadataPolicy.INCLUDE_ALL, 61, 8, 0 },
                        { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Environment"), true,
                                MetadataPolicy.INCLUDE_ALL, 94, 13, 4 },
                        { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Environment"), false,
                                MetadataPolicy.ONLY_CONTAINS, 12, 2, 0 },
                        
                        { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_PLANT, "FieldConditions"), false,
                                MetadataPolicy.INCLUDE_ALL, 77, 10, 0 },
                        { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_PLANT, "FieldConditions"), true,
                                MetadataPolicy.INCLUDE_ALL, 110, 15, 4 },
                        { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_PLANT, "FieldConditions"), false,
                                MetadataPolicy.ONLY_CONTAINS, 12, 2, 0 }, };
        
        for(final Object[] element : testData)
        {
            final URI objectType = (URI)element[0];
            final boolean includeDoNotDisplayProperties = (Boolean)element[1];
            
            final MetadataPolicy containsPropertyPolicy = (MetadataPolicy)element[2];
            final int expectedTripleCount = (int)element[3];
            final int expectedPropertyCount = (int)element[4];
            final int expectedNonDisplayablePropertyCount = (int)element[5];
            
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            
            this.testArtifactManager.exportObjectMetadata(objectType, output, RDFFormat.TURTLE,
                    includeDoNotDisplayProperties, containsPropertyPolicy, artifactID);
            
            // parse output into a Model
            final ByteArrayInputStream bin = new ByteArrayInputStream(output.toByteArray());
            final RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
            final Model model = new LinkedHashModel();
            rdfParser.setRDFHandler(new StatementCollector(model));
            rdfParser.parse(bin, "");
            
            if(expectedTripleCount != model.size())
            {
                DebugUtils.printContents(model);
            }
            
            // verify:
            Assert.assertEquals("Not the expected statement count in Model", expectedTripleCount, model.size());
            Assert.assertEquals("Not the expected no. of properties", expectedPropertyCount,
                    model.filter(objectType, null, null).size());
            Assert.assertEquals("Not the expected no. of non-displayable properties",
                    expectedNonDisplayablePropertyCount,
                    model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY, null).size());
        }
    }
    
    /**
     * Internal helper method to carry out invoking updateArtifact()
     * 
     * @param resourcePath
     * @param resourceFormat
     * @param mgtGraphSize
     * @param assertedStatementCount
     * @param inferredStatementCount
     * @param isPublished
     * @param fragmentPath
     * @param fragmentFormat
     * @param updateObjectUris
     * @return
     * @throws Exception
     */
    private InferredOWLOntologyID internalTestUpdateArtifact(final String resourcePath, final RDFFormat resourceFormat,
            final int mgtGraphSize, final long assertedStatementCount, final long inferredStatementCount,
            final boolean isPublished, final String fragmentPath, final RDFFormat fragmentFormat,
            final UpdatePolicy updatePolicy, final DanglingObjectPolicy danglingObjectPolicy,
            final DataReferenceVerificationPolicy verifyFileReferences, final Collection<URI> updateObjectUris)
        throws Exception
    {
        this.loadSchemaOntologies();
        
        final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
        
        final InferredOWLOntologyID artifactId = this.testArtifactManager.loadArtifact(inputStream, resourceFormat);
        this.verifyLoadedArtifact(artifactId, mgtGraphSize, assertedStatementCount, inferredStatementCount, isPublished);
        
        final InputStream editInputStream = this.getClass().getResourceAsStream(fragmentPath);
        final Model model =
                this.testArtifactManager.updateArtifact(artifactId.getOntologyIRI().toOpenRDFURI(), artifactId
                        .getVersionIRI().toOpenRDFURI(), updateObjectUris, editInputStream, fragmentFormat,
                        updatePolicy, danglingObjectPolicy, verifyFileReferences);
        return OntologyUtils.modelToOntologyIDs(model).get(0);
    }
    
    /**
     * Helper method which loads, infers and stores a given ontology using the PoddOWLManager.
     * 
     * @param inputStream
     * @param format
     * @param assertedStatementCount
     * @param inferredStatementCount
     * @param repositoryConnection
     *            TODO
     * @return
     * @throws Exception
     */
    private InferredOWLOntologyID loadInferStoreOntology(final InputStream inputStream, final RDFFormat format,
            final long assertedStatementCount, final long inferredStatementCount,
            final RepositoryConnection repositoryConnection) throws Exception
    {
        // load ontology to OWLManager
        final OWLOntologyDocumentSource owlSource =
                new StreamDocumentSource(inputStream, OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                        format.getDefaultMIMEType()));
        
        final OWLOntology loadedBaseOntology = this.testArtifactManager.getOWLManager().loadOntology(owlSource);
        
        repositoryConnection.begin();
        
        this.testArtifactManager.getOWLManager().dumpOntologyToRepository(loadedBaseOntology, repositoryConnection);
        
        // infer statements and dump to repository
        final InferredOWLOntologyID inferredOntologyID =
                this.testArtifactManager.getOWLManager().inferStatements(loadedBaseOntology, repositoryConnection);
        
        // verify statement counts
        final URI versionURI = loadedBaseOntology.getOntologyID().getVersionIRI().toOpenRDFURI();
        Assert.assertEquals("Wrong statement count", assertedStatementCount, repositoryConnection.size(versionURI));
        
        final URI inferredOntologyURI = inferredOntologyID.getInferredOntologyIRI().toOpenRDFURI();
        Assert.assertEquals("Wrong inferred statement count", inferredStatementCount,
                repositoryConnection.size(inferredOntologyURI));
        
        repositoryConnection.commit();
        
        return inferredOntologyID;
    }
    
    /**
     * Helper method which loads, infers and stores a given ontology using the PoddOWLManager.
     * 
     * @param resourcePath
     * @param format
     * @param assertedStatementCount
     * @param inferredStatementCount
     * @param repositoryConnection
     *            TODO
     * @return
     * @throws Exception
     */
    private InferredOWLOntologyID loadInferStoreOntology(final String resourcePath, final RDFFormat format,
            final long assertedStatementCount, final long inferredStatementCount,
            final RepositoryConnection repositoryConnection) throws Exception
    {
        // load ontology to OWLManager
        final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
        Assert.assertNotNull("Could not find resource: " + resourcePath, inputStream);
        
        return this.loadInferStoreOntology(inputStream, format, assertedStatementCount, inferredStatementCount,
                repositoryConnection);
    }
    
    /**
     * Helper method which loads the three PODD schema ontologies (and their dependencies):
     * PODD-Base, PODD-Science and PODD-Plant.
     * 
     * This method is not called from the setUp() method since some tests require not loading all
     * schema ontologies.
     * 
     * @throws Exception
     */
    private List<InferredOWLOntologyID> loadSchemaOntologies() throws Exception
    {
        // prepare: load schema ontologies
        final InferredOWLOntologyID inferredDctermsOntologyID =
                this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_DCTERMS, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_INFERRED, this.testRepositoryConnection);
        final InferredOWLOntologyID inferredFoafOntologyID =
                this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_FOAF, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_FOAF_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_FOAF_INFERRED, this.testRepositoryConnection);
        final InferredOWLOntologyID inferredPUserOntologyID =
                this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_USER, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_USER_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_USER_INFERRED, this.testRepositoryConnection);
        final InferredOWLOntologyID inferredPBaseOntologyID =
                this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_BASE, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED, this.testRepositoryConnection);
        final InferredOWLOntologyID inferredPScienceOntologyID =
                this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_SCIENCE, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_INFERRED, this.testRepositoryConnection);
        final InferredOWLOntologyID inferredPPlantOntologyID =
                this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_PLANT, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_PLANT_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_PLANT_INFERRED, this.testRepositoryConnection);
        
        // prepare: update schema management graph
        this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredDctermsOntologyID, false,
                this.testRepositoryConnection, this.schemaGraph);
        this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredFoafOntologyID, false,
                this.testRepositoryConnection, this.schemaGraph);
        this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredPUserOntologyID, false,
                this.testRepositoryConnection, this.schemaGraph);
        this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredPBaseOntologyID, false,
                this.testRepositoryConnection, this.schemaGraph);
        this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredPScienceOntologyID, false,
                this.testRepositoryConnection, this.schemaGraph);
        this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredPPlantOntologyID, false,
                this.testRepositoryConnection, this.schemaGraph);
        
        return Arrays.asList(inferredDctermsOntologyID, inferredFoafOntologyID, inferredPUserOntologyID,
                inferredPBaseOntologyID, inferredPScienceOntologyID, inferredPPlantOntologyID);
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.schemaGraph = ValueFactoryImpl.getInstance().createURI("urn:test:schema-graph");
        this.artifactGraph = ValueFactoryImpl.getInstance().createURI("urn:test:artifact-graph");
        
        this.testRepositoryManager = this.getNewRepositoryManager();
        this.testRepositoryManager.setSchemaManagementGraph(this.schemaGraph);
        this.testRepositoryManager.setArtifactManagementGraph(this.artifactGraph);
        
        this.testRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
        
        final DataReferenceProcessorRegistry testFileRegistry = new DataReferenceProcessorRegistry();
        // clear any automatically added entries that may come from META-INF/services entries on the
        // classpath
        testFileRegistry.clear();
        
        final PoddPurlProcessorFactoryRegistry testPurlRegistry = new PoddPurlProcessorFactoryRegistry();
        testPurlRegistry.clear();
        final PoddPurlProcessorFactory uuidFactory = this.getNewUUIDPurlProcessorFactory();
        Assert.assertNotNull("UUID factory was null", uuidFactory);
        testPurlRegistry.add(uuidFactory);
        
        /**
         * // In practice, the following factories would be automatically added to the registry, //
         * however for testing we want to explicitly add the ones we want to support for each test
         * PoddFileReferenceProcessorFactory sshFactory =
         * this.getNewSSHFileReferenceProcessorFactory();
         * Assert.assertNotNull("SSH factory was null", sshFactory);
         * testFileRegistry.add(sshFactory);
         * 
         * PoddFileReferenceProcessorFactory httpFactory =
         * this.getNewHttpFileReferenceProcessorFactory();
         * Assert.assertNotNull("HTTP factory was null", httpFactory);
         * testFileRegistry.add(httpFactory);
         */
        
        final DataReferenceManager testFileReferenceManager = this.getNewFileReferenceManager();
        testFileReferenceManager.setDataProcessorRegistry(testFileRegistry);
        
        /**
         * // FIXME: Implement these purl processor factories PoddPurlProcessorFactory doiFactory =
         * this.getNewDoiPurlProcessorFactory(); testPurlRegistry.add(doiFactory);
         * Assert.assertNotNull("DOI factory was null", httpFactory);
         * 
         * PoddPurlProcessorFactory handleFactory = this.getNewHandlePurlProcessorFactory();
         * testPurlRegistry.add(handleFactory); Assert.assertNotNull("Handle factory was null",
         * handleFactory);
         **/
        
        final PoddPurlManager testPurlManager = this.getNewPurlManager();
        testPurlManager.setPurlProcessorFactoryRegistry(testPurlRegistry);
        
        final PoddOWLManager testOWLManager = this.getNewOWLManager();
        testOWLManager.setReasonerFactory(this.getNewReasonerFactory());
        final OWLOntologyManager manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        Assert.assertNotNull("Null implementation of OWLOntologymanager", manager);
        testOWLManager.setOWLOntologyManager(manager);
        
        this.testSchemaManager = this.getNewSchemaManager();
        this.testSchemaManager.setOwlManager(testOWLManager);
        this.testSchemaManager.setRepositoryManager(this.testRepositoryManager);
        
        this.testSesameManager = this.getNewSesameManager();
        
        this.testArtifactManager = this.getNewArtifactManager();
        this.testArtifactManager.setRepositoryManager(this.testRepositoryManager);
        this.testArtifactManager.setDataReferenceManager(testFileReferenceManager);
        this.testArtifactManager.setPurlManager(testPurlManager);
        this.testArtifactManager.setOwlManager(testOWLManager);
        this.testArtifactManager.setSchemaManager(this.testSchemaManager);
        this.testArtifactManager.setSesameManager(this.testSesameManager);
        
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        this.testArtifactManager = null;
        
        try
        {
            if(this.testRepositoryConnection.isActive())
            {
                this.log.warn("Found active transaction after test");
                // this.testRepositoryConnection.rollback();
            }
        }
        finally
        {
            try
            {
                if(this.testRepositoryConnection.isOpen())
                {
                    this.testRepositoryConnection.close();
                }
            }
            finally
            {
                this.testRepositoryManager.getRepository().shutDown();
                this.testRepositoryManager = null;
                this.testRepositoryConnection = null;
            }
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#attachDataReferences(URI, URI, InputStream, RDFFormat, DataReferenceVerificationPolicy)}
     * .
     */
    @Test
    public final void testAttachFileReferencesWithoutVerification() throws Exception
    {
        this.loadSchemaOntologies();
        
        final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        
        final InferredOWLOntologyID artifactId = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactId, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        final InputStream editInputStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT);
        
        final InferredOWLOntologyID updatedArtifact =
                this.testArtifactManager.attachDataReferences(artifactId.getOntologyIRI().toOpenRDFURI(), artifactId
                        .getVersionIRI().toOpenRDFURI(), editInputStream, RDFFormat.RDFXML,
                        DataReferenceVerificationPolicy.DO_NOT_VERIFY);
        
        // verify:
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
            nextRepositoryConnection.begin();
            
            this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES + 8, nextRepositoryConnection);
            
            // verify: file reference object
            final List<Statement> fileRefList =
                    Iterations.asList(nextRepositoryConnection.getStatements(null, ValueFactoryImpl.getInstance()
                            .createURI(PoddRdfConstants.PODD_BASE, "hasDataReference"), null, false, updatedArtifact
                            .getVersionIRI().toOpenRDFURI()));
            Assert.assertEquals("Graph should have 1 file reference", 1, fileRefList.size());
            
            Assert.assertTrue("File reference value incorrect",
                    fileRefList.get(0).getObject().stringValue().endsWith("object-rice-scan-34343-a"));
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
            nextRepositoryConnection = null;
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#deleteArtifact(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     * Tests that the artifact manager cannot delete a published artifact.
     */
    @Test
    public final void testDeleteArtifactWhenPublished() throws Exception
    {
        this.loadSchemaOntologies();
        
        final InputStream inputStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        final String mimeType = "application/rdf+xml";
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
        
        // invoke test method
        InferredOWLOntologyID resultArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
        
        resultArtifactId = this.testArtifactManager.publishArtifact(resultArtifactId);
        
        // verify:
        this.verifyLoadedArtifact(resultArtifactId, 8,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, true);
        
        try
        {
            this.testArtifactManager.deleteArtifact(resultArtifactId);
            Assert.fail("Should have failed to delete Published artifact");
        }
        catch(final DeleteArtifactException e)
        {
            Assert.assertNotNull("Exception did not contain the artifact ID", e.getArtifact());
            Assert.assertEquals("IRI on the exception did not match our expected IRI",
                    resultArtifactId.getOntologyIRI(), e.getArtifact().getOntologyIRI());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#deleteArtifact(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     * Tests that the artifact manager cannot delete a published artifact.
     */
    @Test
    public final void testDeleteArtifactWhenUnmanaged() throws Exception
    {
        this.loadSchemaOntologies();
        
        final InputStream inputStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        final String mimeType = "application/rdf+xml";
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
        
        InferredOWLOntologyID resultArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
        
        // verify:
        this.verifyLoadedArtifact(resultArtifactId, 7,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
        
        boolean deleted = this.testArtifactManager.deleteArtifact(resultArtifactId);
        Assert.assertTrue("Should have deleted artifact successfully", deleted);
        
        deleted = this.testArtifactManager.deleteArtifact(resultArtifactId);
        Assert.assertFalse("Should fail as artifact no longer exists", deleted);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#deleteArtifact(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     * Tests that the artifact manager can delete an artifact when there was a single version
     * loaded, and the version is given to the deleteArtifact method.
     */
    @Test
    public final void testDeleteArtifactWithVersionSingle() throws Exception
    {
        this.loadSchemaOntologies();
        
        final InputStream inputStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        // MIME type should be either given by the user, detected from the content type on the
        // request, or autodetected using the Any23 Mime Detector
        final String mimeType = "application/rdf+xml";
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
        
        // invoke test method
        final InferredOWLOntologyID resultArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
        
        // verify:
        this.verifyLoadedArtifact(resultArtifactId, 7,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
        
        Assert.assertTrue("Could not delete artifact", this.testArtifactManager.deleteArtifact(resultArtifactId));
        
        try
        {
            this.testArtifactManager.getArtifact(resultArtifactId.getOntologyIRI());
            
            Assert.fail("Current contract is to throw an exception when someone tries to get an artifact that does not exist");
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            Assert.assertNotNull("Exception did not contain the requested artifact IRI", e.getUnmanagedOntologyIRI());
            
            Assert.assertEquals("IRI on the exception did not match our expected IRI",
                    resultArtifactId.getOntologyIRI(), e.getUnmanagedOntologyIRI());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#deleteObject(String, String, String, boolean)}
     * .
     * 
     * Tests that the artifact manager can delete a PODD Object.
     */
    @Test
    public final void testDeleteObjectSuccess() throws Exception
    {
        // prepare: load schema ontologies and test artifact
        this.loadSchemaOntologies();
        final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactID = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactID, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        final Object[][] testData =
                { { "http://purl.org/podd/basic-2-20130206/artifact:1#My_Treatment1", 86, false },
                        { "http://purl.org/podd/basic-2-20130206/artifact:1#publication45", 76, false },
                        { "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial", 64, true }, };
        
        for(final Object[] element : testData)
        {
            final String objectToDelete = (String)element[0];
            final int expectedArtifactSize = (int)element[1];
            final boolean cascade = (boolean)element[2];
            
            // perform test action: delete object
            final InferredOWLOntologyID modifiedArtifactId =
                    this.testArtifactManager.deleteObject(artifactID.getOntologyIRI().toString(), artifactID
                            .getVersionIRI().toString(), objectToDelete, cascade);
            
            // verify:
            final Model artifactModel = this.testArtifactManager.exportArtifact(modifiedArtifactId, false);
            Assert.assertEquals("Reduction in artifact size incorrect", expectedArtifactSize, artifactModel.size());
            
            Assert.assertTrue("Object was not deleted",
                    artifactModel.filter(PoddRdfConstants.VF.createURI(objectToDelete), null, null).isEmpty());
            Assert.assertTrue("Object was not deleted",
                    artifactModel.filter(null, null, PoddRdfConstants.VF.createURI(objectToDelete)).isEmpty());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#deleteObject(String, String, String, boolean)}
     * .
     * 
     * Tests that deleting an object which has child objects without setting the cascade option is
     * not allowed.
     */
    @Test
    public final void testDeleteObjectWithChildrenNoCascade() throws Exception
    {
        // prepare: load schema ontologies and test artifact
        this.loadSchemaOntologies();
        final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactID = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactID, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        final String objectToDelete = "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial";
        final boolean cascade = false;
        
        // perform test action: delete object
        try
        {
            this.testArtifactManager.deleteObject(artifactID.getOntologyIRI().toString(), artifactID.getVersionIRI()
                    .toString(), objectToDelete, cascade);
            Assert.fail("Should not have allowed deletion");
        }
        catch(final DisconnectedObjectException e)
        {
            final Set<URI> disconnectedObjects = e.getDisconnectedObjects();
            System.out.println(disconnectedObjects);
            
            final Model artifactModel = this.testArtifactManager.exportArtifact(artifactID, false);
            Assert.assertEquals("Reduction in artifact size incorrect",
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES, artifactModel.size());
            Assert.assertFalse("Object was deleted",
                    artifactModel.filter(PoddRdfConstants.VF.createURI(objectToDelete), null, null).isEmpty());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#deleteObject(String, String, String, boolean)}
     * .
     * 
     * Tests deleting an object which belongs to a Published artifact is not allowed.
     */
    @Test
    public final void testDeleteObjectWithPublishedArtifact() throws Exception
    {
        // prepare: load schema ontologies and test artifact
        this.loadSchemaOntologies();
        final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactID = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactID, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        final InferredOWLOntologyID publishedArtifact = this.testArtifactManager.publishArtifact(artifactID);
        
        final String objectToDelete = "http://purl.org/podd/basic-2-20130206/artifact:1#publication45";
        
        // perform test action: delete object
        try
        {
            this.testArtifactManager.deleteObject(publishedArtifact.getOntologyIRI().toString(), publishedArtifact
                    .getVersionIRI().toString(), objectToDelete, false);
            Assert.fail("Should not have allowed deletion");
        }
        catch(final PublishedArtifactModifyException e)
        {
            Assert.assertEquals("Failure not due to published Artifact", publishedArtifact, e.getArtifactID());
            final Model artifactModel = this.testArtifactManager.exportArtifact(artifactID, false);
            Assert.assertEquals("Reduction in artifact size incorrect",
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES, artifactModel.size());
            Assert.assertFalse("Object was deleted",
                    artifactModel.filter(PoddRdfConstants.VF.createURI(objectToDelete), null, null).isEmpty());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#deleteObject(String, String, String, boolean)}
     * .
     * 
     * Tests deleting an object which is connected to another object via a refersToXXX link.
     */
    @Test
    public final void testDeleteObjectWithReferredToLinks() throws Exception
    {
        // prepare: load schema ontologies and test artifact
        this.loadSchemaOntologies();
        final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_WITH_REFERSTO);
        final InferredOWLOntologyID artifactID = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactID, 7, TestConstants.TEST_ARTIFACT_WITH_REFERSTO_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_WITH_REFERSTO_INFERRED_TRIPLES, false);
        
        final String objectToDelete = "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_genotype_3";
        final boolean cascade = true;
        
        // perform test action: delete object
        final InferredOWLOntologyID modifiedArtifactId =
                this.testArtifactManager.deleteObject(artifactID.getOntologyIRI().toString(), artifactID
                        .getVersionIRI().toString(), objectToDelete, cascade);
        
        // verify:
        final Model artifactModel = this.testArtifactManager.exportArtifact(modifiedArtifactId, false);
        Assert.assertEquals("Reduction in artifact size incorrect", 72, artifactModel.size());
        Assert.assertTrue("Object still exists as an object of some statement",
                artifactModel.filter(null, null, PoddRdfConstants.VF.createURI(objectToDelete)).isEmpty());
        Assert.assertTrue("Object exists as a subject of some statement",
                artifactModel.filter(PoddRdfConstants.VF.createURI(objectToDelete), null, null).isEmpty());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#deleteObject(String, String, String, boolean)}
     * .
     * 
     * Tests that deleting a Top Object (i.e. a Project object) is not allowed with the
     * deleteObject() method.
     */
    @Test
    public final void testDeleteObjectWithTopObject() throws Exception
    {
        // prepare: load schema ontologies and test artifact
        this.loadSchemaOntologies();
        final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactID = this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactID, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        final String objectToDelete = "http://purl.org/podd/basic-1-20130206/object:2966";
        final boolean cascade = true;
        
        // perform test action: delete object
        try
        {
            this.testArtifactManager.deleteObject(artifactID.getOntologyIRI().toString(), artifactID.getVersionIRI()
                    .toString(), objectToDelete, cascade);
            Assert.fail("Should not have allowed deletion");
        }
        catch(final ArtifactModifyException e)
        {
            Assert.assertEquals("Failure not due to object to delete", objectToDelete, e.getObjectUri().toString());
            
            final Model artifactModel = this.testArtifactManager.exportArtifact(artifactID, false);
            Assert.assertEquals("Reduction in artifact size incorrect",
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES, artifactModel.size());
            Assert.assertFalse("Object was deleted",
                    artifactModel.filter(PoddRdfConstants.VF.createURI(objectToDelete), null, null).isEmpty());
        }
    }
    
    @Test
    public final void testExportArtifact() throws Exception
    {
        this.loadSchemaOntologies();
        
        // prepare: upload a test artifact
        final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactIDv1 =
                this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactIDv1, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        final Model model = this.testArtifactManager.exportArtifact(artifactIDv1, false);
        
        Assert.assertFalse("Exported artifact was empty", model.isEmpty());
        Assert.assertEquals("Incorrect statement count in exported artifact",
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES, model.size());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#exportObjectMetadata(URI, java.io.OutputStream, RDFFormat, boolean, MetadataPolicy, InferredOWLOntologyID)}
     * .
     */
    @Test
    public final void testExportObjectMetadataWithArtifact() throws Exception
    {
        this.loadSchemaOntologies();
        
        // prepare: upload a test artifact
        final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactIDv1 =
                this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactIDv1, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        this.internalTestExportObjectmetadata(artifactIDv1);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#exportObjectMetadata(URI, java.io.OutputStream, RDFFormat, boolean, MetadataPolicy, InferredOWLOntologyID)}
     * .
     */
    @Test
    public final void testExportObjectMetadataWithArtifactOther() throws Exception
    {
        this.loadSchemaOntologies();
        
        // prepare: upload a test artifact
        final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactIDv1 =
                this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactIDv1, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        this.internalTestExportObjectmetadata(artifactIDv1);
        
        final InputStream inputStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        final InferredOWLOntologyID unpublishedArtifactId =
                this.testArtifactManager.loadArtifact(inputStream, RDFFormat.RDFXML);
        this.verifyLoadedArtifact(unpublishedArtifactId, 14,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
        
        // Test that after the second artifact is added the numbers do not change
        this.internalTestExportObjectmetadata(artifactIDv1);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#exportObjectMetadata(URI, java.io.OutputStream, RDFFormat, boolean, MetadataPolicy, InferredOWLOntologyID)}
     * .
     */
    @Test
    public final void testExportObjectMetadataWithoutArtifact() throws Exception
    {
        this.loadSchemaOntologies();
        
        this.internalTestExportObjectmetadata(null);
    }
    
    @Test
    public final void testFillMissingData() throws Exception
    {
        this.loadSchemaOntologies();
        
        // prepare: upload a test artifact
        final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactIDv1 =
                this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactIDv1, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        final String[] objectUris =
                { "http://purl.org/podd/basic-1-20130206/object:2966",
                        "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype",
                        "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                        "http://purl.org/podd/ns/poddScience#WildType_NotApplicable",
                        "http://purl.org/podd/ns/poddPlant#DeltaTporometer-63",
                        "http://purl.org/podd/ns/poddBase#DisplayType_LongText" };
        
        final String[] expectedLabels =
                { "Project#2012-0006_ Cotton Leaf Morphology", "Demo genotype", "Squeekee material", "Not Applicable",
                        "Delta-T porometer", null };
        
        // prepare: Model with test data
        final Model testModel = new LinkedHashModel();
        for(final String s : objectUris)
        {
            testModel.add(PoddRdfConstants.VF.createURI(s), RDFS.LABEL, PoddRdfConstants.VF.createLiteral("?blank"));
        }
        
        final Model resultModel = this.testArtifactManager.fillMissingData(artifactIDv1, testModel);
        
        // verify: each URI has the expected label
        for(int i = 0; i < objectUris.length; i++)
        {
            final String objectString =
                    resultModel.filter(PoddRdfConstants.VF.createURI(objectUris[i]), RDFS.LABEL, null).objectString();
            Assert.assertEquals("Not the expected label", expectedLabels[i], objectString);
        }
    }
    
    @Test
    public final void testGetChildObjects() throws Exception
    {
        this.loadSchemaOntologies();
        
        // prepare: upload a test artifact
        final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID ontologyID = this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(ontologyID, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        final Object[][] testData =
                { { "http://purl.org/podd/basic-1-20130206/object:2966", 6 },
                        { "http://purl.org/podd/basic-2-20130206/artifact:1#publication45", 0 },
                        { "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype", 0 },
                        { "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Investigation", 3 },
                        { "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial", 1 },
                        { "http://purl.org/podd/ns/poddScience#WildType_NotApplicable", 0 }, };
        
        for(final Object[] element : testData)
        {
            final URI objectUri = PoddRdfConstants.VF.createURI(element[0].toString());
            final int expectedChildObjectCount = (int)element[1];
            
            final Set<URI> childObjects = this.testArtifactManager.getChildObjects(ontologyID, objectUri);
            Assert.assertEquals("Not the expected number of child objects", expectedChildObjectCount,
                    childObjects.size());
        }
    }
    
    @Test
    public final void testGetFileReferenceManager() throws Exception
    {
        Assert.assertNotNull("File Reference Manager was null", this.testArtifactManager.getFileReferenceManager());
    }
    
    @Test
    public final void testGetObjectTypes() throws Exception
    {
        this.loadSchemaOntologies();
        
        // prepare: upload a test artifact
        final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactIDv1 =
                this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactIDv1, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        final Object[][] testData =
                {
                        { "http://purl.org/podd/basic-1-20130206/object:2966", 1,
                                "http://purl.org/podd/ns/poddScience#Project" },
                        { "http://purl.org/podd/basic-2-20130206/artifact:1#publication45", 1,
                                "http://purl.org/podd/ns/poddScience#Publication" },
                        { "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype", 1,
                                "http://purl.org/podd/ns/poddScience#Genotype" },
                        { "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial", 1,
                                "http://purl.org/podd/ns/poddScience#Material" },
                        { "http://purl.org/podd/ns/poddScience#WildType_NotApplicable", 1,
                                "http://purl.org/podd/ns/poddScience#WildTypeAssertion" }, };
        
        for(final Object[] element : testData)
        {
            final URI objectUri = ValueFactoryImpl.getInstance().createURI(element[0].toString());
            final int expectedStatementCount = (int)element[1];
            
            final List<PoddObjectLabel> list = this.testArtifactManager.getObjectTypes(artifactIDv1, objectUri);
            
            this.log.info("Results for object <{}> are {}", objectUri, list.get(0).toString());
            
            Assert.assertEquals("Unexpected no. of statements", expectedStatementCount, list.size());
            if(expectedStatementCount == 1)
            {
                final URI expectedType = ValueFactoryImpl.getInstance().createURI(element[2].toString());
                Assert.assertEquals("Not the expected type", expectedType, list.get(0).getObjectURI());
            }
        }
    }
    
    @Test
    public final void testGetOWLManager() throws Exception
    {
        Assert.assertNotNull("OWL Manager was null", this.testArtifactManager.getOWLManager());
    }
    
    @Test
    public final void testGetParentDetails() throws Exception
    {
        this.loadSchemaOntologies();
        
        // prepare: upload a test artifact
        final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactIDv1 =
                this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactIDv1, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        final Object[][] testData =
                {
                        { "http://purl.org/podd/basic-1-20130206/object:2966", 0, "" },
                        { "http://purl.org/podd/basic-2-20130206/artifact:1#publication45", 1,
                                "http://purl.org/podd/basic-1-20130206/object:2966" },
                        { "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype", 1,
                                "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Material" },
                        { "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial", 1,
                                "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Investigation" },
                        { "http://purl.org/podd/ns/poddScience#ANZSRC_NotApplicable", 0, "" }, };
        
        for(final Object[] element : testData)
        {
            final URI objectUri = ValueFactoryImpl.getInstance().createURI(element[0].toString());
            final int expectedStatementCount = (int)element[1];
            
            final Model model = this.testArtifactManager.getParentDetails(artifactIDv1, objectUri);
            
            Assert.assertEquals("Unexpected no. of statements", expectedStatementCount, model.size());
            if(expectedStatementCount == 1)
            {
                final URI expectedParent = ValueFactoryImpl.getInstance().createURI(element[2].toString());
                Assert.assertTrue("Not the expected parent", model.subjects().contains(expectedParent));
            }
        }
    }
    
    @Test
    public final void testGetPurlManager() throws Exception
    {
        Assert.assertNotNull("Purl Manager was null", this.testArtifactManager.getPurlManager());
    }
    
    @Test
    public final void testGetRepositoryManager() throws Exception
    {
        Assert.assertNotNull("Repository Manager was null", this.testArtifactManager.getRepositoryManager());
    }
    
    @Test
    public final void testGetSchemaManager() throws Exception
    {
        Assert.assertNotNull("Schema Manager was null", this.testArtifactManager.getSchemaManager());
    }
    
    /**
     * Test method for {@link com.github.podd.api.PoddArtifactManager#listPublishedArtifacts()}. .
     */
    @Test
    public final void testListPublishedArtifacts() throws Exception
    {
        this.loadSchemaOntologies();
        
        final InputStream inputStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        final InferredOWLOntologyID unpublishedArtifactId =
                this.testArtifactManager.loadArtifact(inputStream, RDFFormat.RDFXML);
        this.verifyLoadedArtifact(unpublishedArtifactId, 7,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
        
        // invoke method under test
        final InferredOWLOntologyID publishedArtifactId =
                this.testArtifactManager.publishArtifact(unpublishedArtifactId);
        
        Assert.assertNotNull(publishedArtifactId);
        
        final Collection<InferredOWLOntologyID> listPublishedArtifacts =
                this.testArtifactManager.listPublishedArtifacts();
        
        this.log.info("published artifacts: {}", listPublishedArtifacts);
        
        Assert.assertNotNull(listPublishedArtifacts);
        Assert.assertEquals(1, listPublishedArtifacts.size());
        
        final InferredOWLOntologyID nextArtifact = listPublishedArtifacts.iterator().next();
        Assert.assertEquals(unpublishedArtifactId.getOntologyIRI(), nextArtifact.getOntologyIRI());
        
        final Collection<InferredOWLOntologyID> listUnpublishedArtifacts =
                this.testArtifactManager.listUnpublishedArtifacts();
        
        Assert.assertNotNull(listUnpublishedArtifacts);
        Assert.assertTrue(listUnpublishedArtifacts.isEmpty());
    }
    
    /**
     * Test method for {@link com.github.podd.api.PoddArtifactManager#listPublishedArtifacts()}. .
     */
    @Test
    public final void testListUnpublishedArtifacts() throws Exception
    {
        this.loadSchemaOntologies();
        
        final InputStream inputStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        // MIME type should be either given by the user, detected from the content type on the
        // request, or autodetected using the Any23 Mime Detector
        final String mimeType = "application/rdf+xml";
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
        
        final InferredOWLOntologyID unpublishedArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
        this.verifyLoadedArtifact(unpublishedArtifactId, 7,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
        
        final Collection<InferredOWLOntologyID> listPublishedArtifacts =
                this.testArtifactManager.listPublishedArtifacts();
        
        Assert.assertNotNull(listPublishedArtifacts);
        Assert.assertTrue(listPublishedArtifacts.isEmpty());
        
        final Collection<InferredOWLOntologyID> listUnpublishedArtifacts =
                this.testArtifactManager.listUnpublishedArtifacts();
        
        Assert.assertNotNull(listUnpublishedArtifacts);
        Assert.assertEquals(1, listUnpublishedArtifacts.size());
        
        final InferredOWLOntologyID nextArtifact = listUnpublishedArtifacts.iterator().next();
        Assert.assertEquals(unpublishedArtifactId.getOntologyIRI(), nextArtifact.getOntologyIRI());
        Assert.assertEquals(unpublishedArtifactId, nextArtifact);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testLoadArtifactBasicSuccess() throws Exception
    {
        this.loadSchemaOntologies();
        
        final InputStream inputStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        // MIME type should be either given by the user, detected from the content type on the
        // request, or autodetected using the Any23 Mime Detector
        final String mimeType = "application/rdf+xml";
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
        
        // invoke test method
        final InferredOWLOntologyID resultArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
        
        // verify:
        this.verifyLoadedArtifact(resultArtifactId, 7,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
    }
    
    @Ignore("TODO: Enable periodically to debug concurrency issues")
    @Test
    public final void testLoadArtifactConcurrency() throws Exception
    {
        // prepare:
        this.loadSchemaOntologies();
        
        // load test artifact
        final InputStream inputStream4Artifact =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1);
        
        Assert.assertNotNull("Could not find test resource: " + TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1,
                inputStream4Artifact);
        
        final String nextTestArtifact = IOUtils.toString(inputStream4Artifact);
        
        final AtomicInteger count = new AtomicInteger(0);
        final CountDownLatch openLatch = new CountDownLatch(1);
        final int threadCount = 15;
        final CountDownLatch closeLatch = new CountDownLatch(threadCount);
        for(int i = 0; i < threadCount; i++)
        {
            final int number = i;
            final Runnable runner = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            openLatch.await();
                            for(int j = 0; j < 5; j++)
                            {
                                final ByteArrayInputStream inputStream =
                                        new ByteArrayInputStream(nextTestArtifact.getBytes(StandardCharsets.UTF_8));
                                final InferredOWLOntologyID artifactId =
                                        AbstractPoddArtifactManagerTest.this.testArtifactManager.loadArtifact(
                                                inputStream, RDFFormat.RDFXML);
                            }
                            count.incrementAndGet();
                        }
                        catch(OpenRDFException | PoddException | IOException | OWLException e)
                        {
                            e.printStackTrace();
                            Assert.fail("Failed in test: " + number);
                        }
                        catch(final InterruptedException ie)
                        {
                            ie.printStackTrace();
                            Assert.fail("Failed in test: " + number);
                        }
                        finally
                        {
                            closeLatch.countDown();
                        }
                    }
                };
            new Thread(runner, "TestThread" + number).start();
        }
        // all threads are waiting on the latch.
        openLatch.countDown(); // release the latch
        // all threads are now running concurrently.
        closeLatch.await();
        // Verify that there were no failures, as the count is only incremented for successes, where
        // the closeLatch must always be called, even for failures
        Assert.assertEquals(threadCount, count.get());
        
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testLoadArtifactWithEmptyOntology() throws Exception
    {
        final InputStream inputStream = this.getClass().getResourceAsStream(TestConstants.TEST_INVALID_ARTIFACT_EMPTY);
        final RDFFormat format = Rio.getParserFormatForMIMEType("application/rdf+xml", RDFFormat.RDFXML);
        
        try
        {
            // invoke test method
            this.testArtifactManager.loadArtifact(inputStream, format);
            Assert.fail("Should have thrown an EmptyOntologyException");
        }
        catch(final EmptyOntologyException e)
        {
            Assert.assertEquals("Exception does not have expected message.", "Loaded ontology is empty", e.getMessage());
            Assert.assertTrue("The ontology is not empty", (e.getOntology() == null || e.getOntology().isEmpty()));
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testLoadArtifactWithInconsistency() throws Exception
    {
        this.loadSchemaOntologies();
        
        final InputStream inputStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BAD_2_LEAD_INSTITUTES);
        // MIME type should be either given by the user, detected from the content type on the
        // request, or autodetected using the Any23 Mime Detector
        final String mimeType = "application/rdf+xml";
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
        
        try
        {
            // invoke test method
            this.testArtifactManager.loadArtifact(inputStream, format);
            Assert.fail("Should have thrown an InconsistentOntologyException");
        }
        catch(final InconsistentOntologyException e)
        {
            final OWLReasoner reasoner = e.getReasoner();
            Assert.assertFalse("Reasoner says ontology is consistent", reasoner.isConsistent());
            Assert.assertEquals("Not the expected Root Ontology", "urn:temp:inconsistentArtifact:1", reasoner
                    .getRootOntology().getOntologyID().getOntologyIRI().toString());
            Assert.assertEquals("Not the expected error message", "Ontology is inconsistent", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     * 
     * This test attempts to load an RDF/XML serialized artifact after wrongly specifying the MIME
     * type as turtle. The exception thrown depends on the expected and actual MIME type
     * combination.
     */
    @Test
    public final void testLoadArtifactWithIncorrectFormat() throws Exception
    {
        final InputStream inputStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        try
        {
            // invoke test method with the invalid RDF Format of TURTLE
            this.testArtifactManager.loadArtifact(inputStream, RDFFormat.TURTLE);
            Assert.fail("Should have thrown an RDFParseException");
        }
        catch(final RDFParseException e)
        {
            Assert.assertTrue(e.getMessage().startsWith("Not a valid"));
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testLoadArtifactWithMissingSchemaOntologiesInRepository() throws Exception
    {
        // prepare: load schema ontologies
        final InferredOWLOntologyID inferredDctermsOntologyID =
                this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_DCTERMS, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_INFERRED, this.testRepositoryConnection);
        final InferredOWLOntologyID inferredFoafOntologyID =
                this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_FOAF, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_FOAF_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_FOAF_INFERRED, this.testRepositoryConnection);
        final InferredOWLOntologyID inferredPUserOntologyID =
                this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_USER, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_USER_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_USER_INFERRED, this.testRepositoryConnection);
        final InferredOWLOntologyID inferredPBaseOntologyID =
                this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_BASE, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED, this.testRepositoryConnection);
        final InferredOWLOntologyID inferredPScienceOntologyID =
                this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_SCIENCE, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_INFERRED, this.testRepositoryConnection);
        
        // prepare: update schema management graph
        this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredDctermsOntologyID, false,
                this.testRepositoryConnection, this.schemaGraph);
        this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredFoafOntologyID, false,
                this.testRepositoryConnection, this.schemaGraph);
        this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredPUserOntologyID, false,
                this.testRepositoryConnection, this.schemaGraph);
        this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredPBaseOntologyID, false,
                this.testRepositoryConnection, this.schemaGraph);
        // PODD-Science ontology is not added to schema management graph
        
        final InputStream inputStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        final RDFFormat format = Rio.getParserFormatForMIMEType("application/rdf+xml", RDFFormat.RDFXML);
        
        try
        {
            // invoke test method
            this.testArtifactManager.loadArtifact(inputStream, format);
            Assert.fail("Should have thrown an UnmanagedSchemaIRIException");
        }
        catch(final UnmanagedSchemaIRIException e)
        {
            Assert.assertEquals("The cause should have been the missing PODD Science ontology",
                    inferredPScienceOntologyID.getBaseOWLOntologyID().getOntologyIRI(), e.getOntologyID());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     * 
     * Tests loading an artifact which imports a previous version of a schema ontology (i.e.
     * poddScience v1)
     */
    @Test
    public final void testLoadArtifactWithNonCurrentSchemaVersionImport() throws Exception
    {
        // prepare:
        this.loadSchemaOntologies();
        
        // prepare: load poddScience v2
        final InferredOWLOntologyID inferredPScienceOntologyID =
                this.loadInferStoreOntology("/test/ontologies/poddScienceV2.owl", RDFFormat.RDFXML, 1265, 218,
                        this.testRepositoryConnection);
        this.testSesameManager.updateCurrentManagedSchemaOntologyVersion(inferredPScienceOntologyID, true,
                this.testRepositoryConnection, this.schemaGraph);
        
        // load test artifact
        final InputStream inputStream4Artifact =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1);
        final InferredOWLOntologyID artifactId =
                this.testArtifactManager.loadArtifact(inputStream4Artifact, RDFFormat.RDFXML);
        
        this.verifyLoadedArtifact(artifactId, 7, TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1_INFERRED_TRIPLES, false);
        
        // verify:
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
            nextRepositoryConnection.begin();
            
            final String[] expectedImports =
                    { "http://purl.org/podd/ns/version/dcTerms/1", "http://purl.org/podd/ns/version/poddUser/1",
                            "http://purl.org/podd/ns/version/poddBase/1",
                            "http://purl.org/podd/ns/version/poddScience/1", // an older version
                    };
            
            // verify: no. of import statements
            final int importStatementCount =
                    Iterations.asList(
                            nextRepositoryConnection.getStatements(null, OWL.IMPORTS, null, false, artifactId
                                    .getVersionIRI().toOpenRDFURI())).size();
            Assert.assertEquals("Graph should have 4 import statements", 4, importStatementCount);
            
            for(final String expectedImport : expectedImports)
            {
                final List<Statement> importStatements =
                        Iterations.asList(nextRepositoryConnection.getStatements(null, OWL.IMPORTS, ValueFactoryImpl
                                .getInstance().createURI(expectedImport), false, artifactId.getVersionIRI()
                                .toOpenRDFURI()));
                
                Assert.assertEquals("Expected 1 import statement per schema", 1, importStatements.size());
            }
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
            nextRepositoryConnection = null;
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     * 
     * Tests loading an artifact where the source RDF statements do not contain a version IRI.
     * 
     */
    @Test
    public final void testLoadArtifactWithNoVersionIRIInSource() throws Exception
    {
        this.loadSchemaOntologies();
        
        // load artifact
        final InputStream inputStream4FirstArtifact =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_NO_VERSION_INFO);
        final InferredOWLOntologyID firstArtifactId =
                this.testArtifactManager.loadArtifact(inputStream4FirstArtifact, RDFFormat.RDFXML);
        
        // verify
        this.verifyLoadedArtifact(firstArtifactId, 7, TestConstants.TEST_ARTIFACT_NO_VERSION_INFO_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_NO_VERSION_INFO_INFERRED_TRIPLES, false);
        Assert.assertEquals("Version IRI of loaded ontology not expected value", firstArtifactId.getOntologyIRI()
                .toString().concat(":version:1"), firstArtifactId.getVersionIRI().toString());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     * 
     * Tests loading two artifacts one after the other.
     * 
     */
    @Test
    public final void testLoadArtifactWithTwoDistinctArtifacts() throws Exception
    {
        this.loadSchemaOntologies();
        
        // load 1st artifact
        final InputStream inputStream4FirstArtifact =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        final InferredOWLOntologyID firstArtifactId =
                this.testArtifactManager.loadArtifact(inputStream4FirstArtifact, RDFFormat.RDFXML);
        
        this.verifyLoadedArtifact(firstArtifactId, 7,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
        
        // load 2nd artifact
        final InputStream inputStream4SecondArtifact =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_PROJECT_2);
        final InferredOWLOntologyID secondArtifactId =
                this.testArtifactManager.loadArtifact(inputStream4SecondArtifact, RDFFormat.RDFXML);
        
        this.verifyLoadedArtifact(firstArtifactId, 14,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
        this.verifyLoadedArtifact(secondArtifactId, 14, TestConstants.TEST_ARTIFACT_BASIC_PROJECT_2_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_PROJECT_2_INFERRED_TRIPLES, true);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     * 
     * Tests loading two versions of the same artifact one after the other.
     * 
     * The two source RDF files have PURLs instead of temporary URIs since they both need to be
     * identified as the same artifact.
     */
    @Test
    public final void testLoadArtifactWithTwoVersionsOfSameArtifact() throws Exception
    {
        this.loadSchemaOntologies();
        
        // load 1st artifact
        final InputStream inputStream4FirstArtifact =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_PURLS_v1);
        final InferredOWLOntologyID firstArtifactId =
                this.testArtifactManager.loadArtifact(inputStream4FirstArtifact, RDFFormat.RDFXML);
        
        this.verifyLoadedArtifact(firstArtifactId, 7, TestConstants.TEST_ARTIFACT_PURLS_v1_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_PURLS_v1_INFERRED_TRIPLES, false);
        
        // load 2nd artifact
        final InputStream inputStream4SecondArtifact =
                this.getClass().getResourceAsStream("/test/artifacts/project-with-purls-v2.rdf");
        final InferredOWLOntologyID secondArtifactId =
                this.testArtifactManager.loadArtifact(inputStream4SecondArtifact, RDFFormat.RDFXML);
        
        Assert.assertEquals("Both versions should have the same artifact ID", firstArtifactId.getOntologyIRI(),
                secondArtifactId.getOntologyIRI());
        
        Assert.assertFalse("Two versions should NOT have the same Version IRI", firstArtifactId.getVersionIRI()
                .toString().equals(secondArtifactId.getVersionIRI().toString()));
        
        this.verifyLoadedArtifact(secondArtifactId, 7, TestConstants.TEST_ARTIFACT_PURLS_v1_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_PURLS_v1_INFERRED_TRIPLES, false);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     * 
     * Tests that the version IRI in the source file is ignored.
     * 
     */
    @Test
    public final void testLoadArtifactWithVersionIRIInSourceIgnored() throws Exception
    {
        this.loadSchemaOntologies();
        
        // load 1st artifact
        final InputStream inputStream4Artifact =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_PROJECT_PUBLISHED);
        final InferredOWLOntologyID artifactId =
                this.testArtifactManager.loadArtifact(inputStream4Artifact, RDFFormat.RDFXML);
        
        this.verifyLoadedArtifact(artifactId, 7, TestConstants.TEST_ARTIFACT_BASIC_PROJECT_PUBLISHED_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_PROJECT_PUBLISHED_INFERRED_TRIPLES, true);
        
        Assert.assertFalse("Version IRI in source should have been ignored", artifactId.getVersionIRI().toString()
                .endsWith(":55"));
        Assert.assertTrue("New generated Version IRI should start from 1", artifactId.getVersionIRI().toString()
                .endsWith(":1"));
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#publishArtifact(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Test
    public final void testPublishArtifactBasicSuccess() throws Exception
    {
        this.loadSchemaOntologies();
        
        final InputStream inputStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        // MIME type should be either given by the user, detected from the content type on the
        // request, or autodetected using the Any23 Mime Detector
        final String mimeType = "application/rdf+xml";
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
        
        final InferredOWLOntologyID unpublishedArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
        this.verifyLoadedArtifact(unpublishedArtifactId, 7,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT_INFERRED_TRIPLES, false);
        
        // invoke method under test
        final InferredOWLOntologyID publishedArtifactId =
                this.testArtifactManager.publishArtifact(unpublishedArtifactId);
        
        // verify: publication status is correctly updated
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
            nextRepositoryConnection.begin();
            
            // verify: a single PUBLICATION_STATUS in asserted ontology
            final List<Statement> publicationStatusStatementList =
                    Iterations.asList(nextRepositoryConnection.getStatements(null,
                            PoddRdfConstants.PODD_BASE_HAS_PUBLICATION_STATUS, null, false,
                            this.testRepositoryManager.getArtifactManagementGraph()));
            Assert.assertEquals("Graph should have one HAS_PUBLICATION_STATUS statement.", 1,
                    publicationStatusStatementList.size());
            
            // verify: artifact is PUBLISHED
            Assert.assertEquals("Wrong publication status", PoddRdfConstants.PODD_BASE_PUBLISHED.toString(),
                    publicationStatusStatementList.get(0).getObject().toString());
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
            nextRepositoryConnection = null;
        }
        
        // FIXME: How do we get information about whether an artifact is published and other
        // metadata like who can access the artifact?
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#searchForOntologyLabels(InferredOWLOntologyID, String, URI[])}
     */
    @Test
    public final void testSearchForOntologyLabelsWithPlatforms() throws Exception
    {
        this.loadSchemaOntologies();
        
        // prepare: upload a test artifact
        final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactIDv1 =
                this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactIDv1, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        final String searchTerm = "lat";
        final URI[] searchTypes =
                { PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Platform"),
                        PoddRdfConstants.VF.createURI(OWL.NAMESPACE, "NamedIndividual") };
        
        final Model result = this.testArtifactManager.searchForOntologyLabels(artifactIDv1, searchTerm, searchTypes);
        
        // verify:
        Assert.assertNotNull("NULL result", result);
        Assert.assertEquals("Not the expected number of search results", 1, result.size());
        Assert.assertEquals("Expected custom Platform 1 not found", 1,
                result.filter(null, null, PoddRdfConstants.VF.createLiteral("Platform 1")).size());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
     * . Tests adding a Podd Object inconsistent with the schema ontologies fails.
     */
    @Test
    public final void testUpdateArtifactAddInconsistentObject() throws Exception
    {
        try
        {
            this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 7,
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                    TestConstants.TEST_ARTIFACT_FRAGMENT_INCONSISTENT_OBJECT, RDFFormat.TURTLE,
                    UpdatePolicy.MERGE_WITH_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                    DataReferenceVerificationPolicy.DO_NOT_VERIFY, Collections.<URI> emptyList());
            Assert.fail("Should have thrown an InconsistentOntologyException");
        }
        catch(final InconsistentOntologyException e)
        {
            Assert.assertEquals("Not the expected error message", "Ontology is inconsistent", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
     * .
     * 
     * Tests adding multiple new PODD objects to an artifact.
     */
    @Test
    public final void testUpdateArtifactAddNewPoddObjectsWithMerge() throws Exception
    {
        final List<URI> objectUriList =
                Arrays.asList(PoddRdfConstants.VF.createURI("urn:temp:uuid:object-rice-scan-34343-a"),
                        PoddRdfConstants.VF.createURI("urn:temp:uuid:publication35"),
                        PoddRdfConstants.VF.createURI("urn:temp:uuid:publication46"));
        
        final InferredOWLOntologyID updatedArtifact =
                this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 7,
                        TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                        TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                        TestConstants.TEST_ARTIFACT_FRAGMENT_MULTIPLE_OBJECTS_TTL, RDFFormat.TURTLE,
                        UpdatePolicy.MERGE_WITH_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                        DataReferenceVerificationPolicy.DO_NOT_VERIFY, objectUriList);
        
        // verify:
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
            nextRepositoryConnection.begin();
            
            this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES + 28, nextRepositoryConnection);
            
            // verify: 2 publications exist
            final List<Statement> testList =
                    Iterations.asList(nextRepositoryConnection.getStatements(null, ValueFactoryImpl.getInstance()
                            .createURI(PoddRdfConstants.PODD_SCIENCE, "hasPublication"), null, false, updatedArtifact
                            .getVersionIRI().toOpenRDFURI()));
            Assert.assertEquals("Graph should have 2 publications", 3, testList.size());
            
            // verify: newly added publication exists
            Assert.assertTrue("New publication is missing",
                    testList.get(0).getObject().toString().endsWith("publication46")
                            || testList.get(1).getObject().toString().endsWith("publication46")
                            || testList.get(2).getObject().toString().endsWith("publication46"));
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
            nextRepositoryConnection = null;
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
     * .
     * 
     * Edit an artifact by adding a Platform pre-defined in a schema ontology and a new custom
     * Platform.
     */
    @Test
    public final void testUpdateArtifactAddNewPoddObjectsWithPlatforms() throws Exception
    {
        final List<URI> objectUriList = Arrays.asList(
        // a temporary URI for a Platform being newly added
                PoddRdfConstants.VF.createURI("urn:temp:uuid:object-rice-scanner-platform"),
                // a Platform that is pre-defined in PODD Plant Ontology
                PoddRdfConstants.VF.createURI("http://purl.org/podd/ns/poddPlant#PlantScan-6e"));
        
        final InferredOWLOntologyID updatedArtifact =
                this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 7,
                        TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                        TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                        TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_PLATFORM_OBJECTS, RDFFormat.TURTLE,
                        UpdatePolicy.MERGE_WITH_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                        DataReferenceVerificationPolicy.DO_NOT_VERIFY, objectUriList);
        
        // verify:
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
            nextRepositoryConnection.begin();
            
            this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES + 8, nextRepositoryConnection);
            
            // verify: platform object
            final List<Statement> platformList =
                    Iterations.asList(nextRepositoryConnection.getStatements(null, ValueFactoryImpl.getInstance()
                            .createURI(PoddRdfConstants.PODD_SCIENCE, "hasPlatform"), null, false, updatedArtifact
                            .getVersionIRI().toOpenRDFURI()));
            
            // 2 added in the test plus a platform that was defined in the initially uploaded
            // artifact
            Assert.assertEquals("Not the expected number of Platforms", 3, platformList.size());
            
            // verify: correct set of platforms
            Assert.assertTrue(
                    "PlantScan Platform is missing",
                    platformList.get(0).getObject().toString().endsWith("PlantScan-6e")
                            || platformList.get(1).getObject().toString().endsWith("PlantScan-6e")
                            || platformList.get(2).getObject().toString().endsWith("PlantScan-6e"));
            
            Assert.assertTrue("Rice-Scanner Platform is missing",
                    platformList.get(0).getObject().toString().endsWith("object-rice-scanner-platform")
                            || platformList.get(1).getObject().toString().endsWith("object-rice-scanner-platform")
                            || platformList.get(2).getObject().toString().endsWith("object-rice-scanner-platform"));
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
            nextRepositoryConnection = null;
        }
        
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
     * .
     * 
     * NOTE: Once file reference validation is implemented in the DataReferenceManager this test
     * will fail. The referred file will have to be created for validation to pass.
     */
    @Test
    public final void testUpdateArtifactAddNewPoddObjectWithFileReferences() throws Exception
    {
        final InferredOWLOntologyID updatedArtifact =
                this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 7,
                        TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                        TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                        TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT, RDFFormat.RDFXML,
                        UpdatePolicy.MERGE_WITH_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                        DataReferenceVerificationPolicy.DO_NOT_VERIFY, Collections.<URI> emptyList());
        
        // verify:
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
            nextRepositoryConnection.begin();
            
            this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES + 8, nextRepositoryConnection);
            
            // verify: file reference object
            final List<Statement> fileRefList =
                    Iterations.asList(nextRepositoryConnection.getStatements(null, ValueFactoryImpl.getInstance()
                            .createURI(PoddRdfConstants.PODD_BASE, "hasDataReference"), null, false, updatedArtifact
                            .getVersionIRI().toOpenRDFURI()));
            Assert.assertEquals("Graph should have 1 file reference", 1, fileRefList.size());
            
            Assert.assertTrue("File reference value incorrect",
                    fileRefList.get(0).getObject().stringValue().endsWith("object-rice-scan-34343-a"));
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
            nextRepositoryConnection = null;
        }
        
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
     * .
     */
    @Test
    public final void testUpdateArtifactAddNewPoddObjectWithMerge() throws Exception
    {
        final InferredOWLOntologyID updatedArtifact =
                this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 7,
                        TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                        TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                        TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_PUBLICATION_OBJECT, RDFFormat.TURTLE,
                        UpdatePolicy.MERGE_WITH_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                        DataReferenceVerificationPolicy.DO_NOT_VERIFY, Collections.<URI> emptyList());
        
        // verify:
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
            nextRepositoryConnection.begin();
            
            this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES + 10, nextRepositoryConnection);
            
            // verify: 2 publications exist
            final List<Statement> testList =
                    Iterations.asList(nextRepositoryConnection.getStatements(null, ValueFactoryImpl.getInstance()
                            .createURI(PoddRdfConstants.PODD_SCIENCE, "hasPublication"), null, false, updatedArtifact
                            .getVersionIRI().toOpenRDFURI()));
            Assert.assertEquals("Graph should have 2 publications", 2, testList.size());
            
            // verify: newly added publication exists
            Assert.assertTrue("New publication is missing",
                    testList.get(0).getObject().toString().endsWith("#publication46")
                            || testList.get(1).getObject().toString().endsWith("#publication46"));
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
            nextRepositoryConnection = null;
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
     * .
     */
    @Test
    public final void testUpdateArtifactAddToNonExistentArtifact() throws Exception
    {
        this.loadSchemaOntologies();
        
        final URI nonExistentArtifactURI =
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-3-no-such-artifact");
        
        final InputStream editInputStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFIED_PUBLICATION_OBJECT);
        
        try
        {
            this.testArtifactManager.updateArtifact(nonExistentArtifactURI, nonExistentArtifactURI,
                    Collections.<URI> emptyList(), editInputStream, RDFFormat.TURTLE, UpdatePolicy.REPLACE_EXISTING,
                    DanglingObjectPolicy.FORCE_CLEAN, DataReferenceVerificationPolicy.DO_NOT_VERIFY);
            Assert.fail("Should have thrown an UnmanagedArtifactIRIException");
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            Assert.assertEquals("Exception not due to expected IRI", nonExistentArtifactURI, e
                    .getUnmanagedOntologyIRI().toOpenRDFURI());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
     * .
     */
    @Test
    public final void testUpdateArtifactModifyPoddObjectWithReplace() throws Exception
    {
        final InferredOWLOntologyID updatedArtifact =
                this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 7,
                        TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                        TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                        TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFIED_PUBLICATION_OBJECT, RDFFormat.TURTLE,
                        UpdatePolicy.REPLACE_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                        DataReferenceVerificationPolicy.DO_NOT_VERIFY, Collections.<URI> emptyList());
        
        // verify:
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
            nextRepositoryConnection.begin();
            
            this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES, nextRepositoryConnection);
            
            // verify: still only 1 publication
            final List<Statement> testList =
                    Iterations.asList(nextRepositoryConnection.getStatements(null, ValueFactoryImpl.getInstance()
                            .createURI(PoddRdfConstants.PODD_SCIENCE, "hasPublication"), null, false, updatedArtifact
                            .getVersionIRI().toOpenRDFURI()));
            Assert.assertEquals("Incorrect no. of hasPublication statements", 1, testList.size());
            
            // verify: publication info has been updated
            final List<Statement> testList2 =
                    Iterations.asList(nextRepositoryConnection.getStatements(null, ValueFactoryImpl.getInstance()
                            .createURI(PoddRdfConstants.PODD_SCIENCE, "hasYear"), null, false, updatedArtifact
                            .getVersionIRI().toOpenRDFURI()));
            Assert.assertEquals("Incorrect no. of hasYear statements", 1, testList2.size());
            Assert.assertEquals("Publication Year has not bee updated", "2011", testList2.get(0).getObject()
                    .stringValue());
            
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
            nextRepositoryConnection = null;
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
     * .
     */
    @Test
    public final void testUpdateArtifactMovePoddObject() throws Exception
    {
        final InferredOWLOntologyID updatedArtifact =
                this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 7,
                        TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                        TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                        TestConstants.TEST_ARTIFACT_FRAGMENT_MOVE_DEMO_INVESTIGATION, RDFFormat.TURTLE,
                        UpdatePolicy.REPLACE_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                        DataReferenceVerificationPolicy.DO_NOT_VERIFY, Collections.<URI> emptyList());
        
        // verify:
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
            nextRepositoryConnection.begin();
            
            this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES, nextRepositoryConnection);
            
            if(this.log.isDebugEnabled())
            {
                DebugUtils.printContents(nextRepositoryConnection, updatedArtifact.getVersionIRI().toOpenRDFURI());
            }
            
            // verify: SqueekeMaterial is now under My_Treatment1
            Assert.assertEquals(
                    "Graph should have 1 statement",
                    1,
                    Iterations.asList(
                            nextRepositoryConnection.getStatements(
                                    ValueFactoryImpl.getInstance().createURI(
                                            "http://purl.org/podd/basic-2-20130206/artifact:1#My_Treatment1"),
                                    null,
                                    ValueFactoryImpl.getInstance().createURI(
                                            "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial"),
                                    false, updatedArtifact.getVersionIRI().toOpenRDFURI())).size());
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
            nextRepositoryConnection = null;
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
     * .
     */
    @Test
    public final void testUpdateArtifactWithDanglingObjects() throws Exception
    {
        final InferredOWLOntologyID updatedArtifact =
                this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 7,
                        TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                        TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                        TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFY_DEMO_INVESTIGATION, RDFFormat.TURTLE,
                        UpdatePolicy.REPLACE_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                        DataReferenceVerificationPolicy.DO_NOT_VERIFY, Collections.<URI> emptyList());
        
        // verify:
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
            nextRepositoryConnection.begin();
            
            this.verifyUpdatedArtifact(updatedArtifact, "http://purl.org/podd/basic-2-20130206/artifact:1:version:2",
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES - 12, nextRepositoryConnection);
            
            // verify: dangling objects are no longer in the updated artifact
            final String[] danglingObjects =
                    { "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                            "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_genotype_3",
                            "http://purl.org/podd/basic-2-20130206/artifact:1#Sequence_A", };
            for(final String deletedObject : danglingObjects)
            {
                final URI deletedObjURI = ValueFactoryImpl.getInstance().createURI(deletedObject);
                Assert.assertEquals(
                        "Dangling object should not exist",
                        0,
                        Iterations.asList(
                                nextRepositoryConnection.getStatements(deletedObjURI, null, null, false,
                                        updatedArtifact.getVersionIRI().toOpenRDFURI())).size());
            }
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
            nextRepositoryConnection = null;
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
     * .
     */
    @Test
    public final void testUpdateArtifactWithDanglingObjectsWithoutForce() throws Exception
    {
        try
        {
            this.internalTestUpdateArtifact(TestConstants.TEST_ARTIFACT_20130206, RDFFormat.TURTLE, 7,
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                    TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false,
                    TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFY_DEMO_INVESTIGATION, RDFFormat.TURTLE,
                    UpdatePolicy.REPLACE_EXISTING, DanglingObjectPolicy.REPORT,
                    DataReferenceVerificationPolicy.DO_NOT_VERIFY, Collections.<URI> emptyList());
            Assert.fail("Should have thrown an Exception to indicate that dangling objects will result");
        }
        catch(final DisconnectedObjectException e)
        {
            Assert.assertEquals("Update leads to disconnected PODD objects", e.getMessage());
            Assert.assertEquals(4, e.getDisconnectedObjects().size());
            
            final String[] danglingObjects =
                    { "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                            "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_genotype_3",
                            "http://purl.org/podd/basic-2-20130206/artifact:1#Sequence_A", };
            for(final String danglingObject : danglingObjects)
            {
                final URI danglingObjectURI = ValueFactoryImpl.getInstance().createURI(danglingObject);
                Assert.assertTrue("Expected dangling object not present",
                        e.getDisconnectedObjects().contains(danglingObjectURI));
            }
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateArtifact(URI, InputStream, RDFFormat, boolean)}
     * . Tests attempting to update an artifact when an old version of the artifact has been used as
     * the basis of the update. This could occur due to concurrent updates.
     * 
     * Currently verifies that a failure occurs.
     */
    @Test
    public final void testUpdateArtifactWithOldVersion() throws Exception
    {
        this.loadSchemaOntologies();
        
        // upload artifact
        final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactIDv1 =
                this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactIDv1, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        // upload another version of artifact
        final InputStream inputStream2 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactIDv2 =
                this.testArtifactManager.loadArtifact(inputStream2, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactIDv2, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        System.out.println(artifactIDv1);
        System.out.println(artifactIDv2);
        
        final InputStream editInputStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFY_DEMO_INVESTIGATION);
        try
        {
            this.testArtifactManager.updateArtifact(artifactIDv1.getOntologyIRI().toOpenRDFURI(), artifactIDv1
                    .getVersionIRI().toOpenRDFURI(), Collections.<URI> emptyList(), editInputStream, RDFFormat.TURTLE,
                    UpdatePolicy.REPLACE_EXISTING, DanglingObjectPolicy.FORCE_CLEAN,
                    DataReferenceVerificationPolicy.DO_NOT_VERIFY);
            Assert.fail("Should have thrown an UnmanagedArtifactIRIException");
        }
        catch(final UnmanagedArtifactVersionException e)
        {
            Assert.assertEquals("Exception not due to the expected artifact version", artifactIDv1.getVersionIRI(),
                    e.getUnmanagedVersionIRI());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
     * .
     */
    @Test
    public final void testUpdateSchemaImportsNull() throws Exception
    {
        try
        {
            this.testArtifactManager.updateSchemaImports(null, new HashSet<OWLOntologyID>(),
                    new HashSet<OWLOntologyID>());
            Assert.fail("Should have thrown an IllegalArgumentException");
        }
        catch(final IllegalArgumentException e)
        {
            Assert.assertTrue(e.getMessage().contains("Artifact was null"));
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
     * .
     */
    @Test
    public final void testUpdateSchemaImportsUnmanagedArtifact() throws Exception
    {
        try
        {
            this.testArtifactManager.updateSchemaImports(
                    new InferredOWLOntologyID(IRI.create("urn:test:ontology:nonexistent"), IRI
                            .create("urn:test:ontology:version:nonexistent"), IRI
                            .create("urn:test:ontology:inferredversion:nonexistent")), new HashSet<OWLOntologyID>(),
                    new HashSet<OWLOntologyID>());
            Assert.fail("Should have thrown an UnmanagedArtifactIRIException");
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            Assert.assertEquals("Exception not due to the expected artifact IRI",
                    IRI.create("urn:test:ontology:nonexistent"), e.getUnmanagedOntologyIRI());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
     * .
     */
    @Test
    public final void testUpdateSchemaImportsUnmanagedVersion() throws Exception
    {
        this.loadSchemaOntologies();
        
        // upload artifact
        final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactIDv1 =
                this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactIDv1, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        try
        {
            this.testArtifactManager.updateSchemaImports(
                    new InferredOWLOntologyID(artifactIDv1.getOntologyIRI(), IRI
                            .create("urn:test:ontology:version:nonexistent"), artifactIDv1.getInferredOntologyIRI()),
                    new HashSet<OWLOntologyID>(), new HashSet<OWLOntologyID>());
            Assert.fail("Should have thrown an UnmanagedArtifactVersionException");
        }
        catch(final UnmanagedArtifactVersionException e)
        {
            Assert.assertEquals("Exception not due to the expected artifact version",
                    IRI.create("urn:test:ontology:version:nonexistent"), e.getUnmanagedVersionIRI());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
     * .
     */
    @Test
    public final void testUpdateSchemaImportsEmptySchemas() throws Exception
    {
        final List<InferredOWLOntologyID> schemaOntologies = this.loadSchemaOntologies();
        
        // upload artifact
        final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactIDv1 =
                this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactIDv1, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        final InferredOWLOntologyID updateSchemaImports =
                this.testArtifactManager.updateSchemaImports(new InferredOWLOntologyID(artifactIDv1.getOntologyIRI(),
                        artifactIDv1.getVersionIRI(), artifactIDv1.getInferredOntologyIRI()),
                        new HashSet<OWLOntologyID>(), new HashSet<OWLOntologyID>());
        
        Assert.assertEquals(updateSchemaImports.getOntologyIRI(), artifactIDv1.getOntologyIRI());
        Assert.assertNotEquals(updateSchemaImports.getVersionIRI(), artifactIDv1.getVersionIRI());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
     * .
     */
    @Test
    public final void testUpdateSchemaImportsEmptyNewSchemas() throws Exception
    {
        final List<InferredOWLOntologyID> schemaOntologies = this.loadSchemaOntologies();
        
        // upload artifact
        final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactIDv1 =
                this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactIDv1, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        try
        {
            this.testArtifactManager.updateSchemaImports(new InferredOWLOntologyID(artifactIDv1.getOntologyIRI(),
                    artifactIDv1.getVersionIRI(), artifactIDv1.getInferredOntologyIRI()),
                    new LinkedHashSet<OWLOntologyID>(schemaOntologies), new HashSet<OWLOntologyID>());
            Assert.fail("Removing essential ontologies should generate an OWL-related exception");
        }
        catch(final OntologyNotInProfileException e)
        {
            Assert.assertEquals(artifactIDv1.getOntologyIRI(), e.getOntology().getOntologyID().getOntologyIRI());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImports(InferredOWLOntologyID, Set, Set)}
     * .
     */
    @Test
    public final void testUpdateSchemaImportsEmptyOldSchemas() throws Exception
    {
        final List<InferredOWLOntologyID> schemaOntologies = this.loadSchemaOntologies();
        
        // upload artifact
        final InputStream inputStream1 = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        final InferredOWLOntologyID artifactIDv1 =
                this.testArtifactManager.loadArtifact(inputStream1, RDFFormat.TURTLE);
        this.verifyLoadedArtifact(artifactIDv1, 7, TestConstants.TEST_ARTIFACT_BASIC_1_20130206_CONCRETE_TRIPLES,
                TestConstants.TEST_ARTIFACT_BASIC_1_20130206_INFERRED_TRIPLES, false);
        
        final InferredOWLOntologyID updateSchemaImports =
                this.testArtifactManager.updateSchemaImports(new InferredOWLOntologyID(artifactIDv1.getOntologyIRI(),
                        artifactIDv1.getVersionIRI(), artifactIDv1.getInferredOntologyIRI()),
                        new HashSet<OWLOntologyID>(), new LinkedHashSet<OWLOntologyID>(schemaOntologies));
        
        Assert.assertEquals(updateSchemaImports.getOntologyIRI(), artifactIDv1.getOntologyIRI());
        Assert.assertNotEquals(updateSchemaImports.getVersionIRI(), artifactIDv1.getVersionIRI());
    }
    
    /**
     * Helper method to write Repository graphs to files when required.
     * 
     * NOTE 1: This test is to be regularly ignored as it does not test any functionality.
     * 
     * NOTE 2: Schemas and artifacts are loaded through the standard PODD manager API classes. One
     * effect of this is that any existing version IRI in the source file of an artifact is ignored.
     * Therefore, the inferred triples will import the internally generated version.
     * 
     * @since 06/03/2013
     */
    @Ignore
    @Test
    public final void testWriteInferredOntologyToFile() throws Exception
    {
        this.loadSchemaOntologies();
        final InputStream inputStream = this.getClass().getResourceAsStream("/test/artifacts/basic-20130206.ttl");
        final RDFFormat readFormat = RDFFormat.TURTLE;
        final InferredOWLOntologyID resultArtifactId = this.testArtifactManager.loadArtifact(inputStream, readFormat);
        
        this.dumpRdfToFile(resultArtifactId.getInferredOntologyIRI().toOpenRDFURI(),
                "/home/kutila/basic-20130206-inferred.ttl", RDFFormat.TURTLE);
        
        /*
         * String[] contexts = { // "http://purl.org/podd/ns/version/dcTerms/1",
         * "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/dcTerms/1", //
         * "http://purl.org/podd/ns/version/foaf/1",
         * "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/foaf/1", //
         * "http://purl.org/podd/ns/version/poddUser/1",
         * "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddUser/1", //
         * "http://purl.org/podd/ns/version/poddBase/1",
         * "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddBase/1", //
         * "http://purl.org/podd/ns/version/poddScience/1",
         * "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddScience/1", //
         * "http://purl.org/podd/ns/version/poddPlant/1",
         * "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddPlant/1", };
         * 
         * String[] fileNames = { // "dcTerms", "dcTermsInferred", // "foaf", "foafInferred", //
         * "poddUser", "poddUserInferred", // "poddBase", "poddBaseInferred", // "poddScience",
         * "poddScienceInferred", // "poddPlant", "poddPlantInferred", };
         * 
         * for(int i = 0; i < contexts.length; i++) { URI context =
         * ValueFactoryImpl.getInstance().createURI(contexts[i]); final RDFFormat writeFormat =
         * RDFFormat.RDFXML; String path = "/home/kutila/";
         * 
         * dumpRdfToFile(context, (path + fileNames[i]), writeFormat); }
         */
    }
    
    /**
     * Helper method to verify the contents of artifact management graph
     * 
     * @param repositoryConnection
     * @param graphSize
     *            Expected size of the graph
     * @param testGraph
     *            The Graph/context to be tested
     * @param ontologyIRI
     *            The ontology/artifact
     * @param versionIRI
     *            Version IRI of the ontology/artifact
     * @param inferredVersionIRI
     *            Inferred version of the ontology/artifact
     * @throws RepositoryException
     * @throws Exception
     */
    private void verifyArtifactManagementGraphContents(final RepositoryConnection repositoryConnection,
            final int graphSize, final URI testGraph, final IRI ontologyIRI, final IRI versionIRI,
            final IRI inferredVersionIRI) throws RepositoryException
    {
        Assert.assertEquals("Graph not of expected size", graphSize, repositoryConnection.size(testGraph));
        
        // verify: OWL_VERSION
        final List<Statement> stmtList =
                Iterations.asList(repositoryConnection.getStatements(ontologyIRI.toOpenRDFURI(),
                        PoddRdfConstants.OWL_VERSION_IRI, null, false, testGraph));
        Assert.assertEquals("Graph should have one OWL_VERSION statement", 1, stmtList.size());
        Assert.assertEquals("Wrong OWL_VERSION in Object", versionIRI.toString(), stmtList.get(0).getObject()
                .toString());
        
        // verify: OMV_CURRENT_VERSION
        final List<Statement> currentVersionStatementList =
                Iterations.asList(repositoryConnection.getStatements(ontologyIRI.toOpenRDFURI(),
                        PoddRdfConstants.OMV_CURRENT_VERSION, null, false, testGraph));
        Assert.assertEquals("Graph should have one OMV_CURRENT_VERSION statement", 1,
                currentVersionStatementList.size());
        Assert.assertEquals("Wrong OMV_CURRENT_VERSION in Object", versionIRI.toString(), currentVersionStatementList
                .get(0).getObject().toString());
        
        // verify: INFERRED_VERSION
        final List<Statement> inferredVersionStatementList =
                Iterations.asList(repositoryConnection.getStatements(versionIRI.toOpenRDFURI(),
                        PoddRdfConstants.PODD_BASE_INFERRED_VERSION, null, false, testGraph));
        Assert.assertEquals("Graph should have one INFERRED_VERSION statement", 1, inferredVersionStatementList.size());
        Assert.assertEquals("Wrong INFERRED_VERSION in Object", inferredVersionIRI.toString(),
                inferredVersionStatementList.get(0).getObject().toString());
        
        // verify: CURRENT_INFERRED_VERSION
        final List<Statement> currentInferredVersionStatementList =
                Iterations.asList(repositoryConnection.getStatements(ontologyIRI.toOpenRDFURI(),
                        PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION, null, false, testGraph));
        Assert.assertEquals("Graph should have one CURRENT_INFERRED_VERSION statement", 1,
                currentInferredVersionStatementList.size());
        Assert.assertEquals("Wrong CURRENT_INFERRED_VERSION in Object", inferredVersionIRI.toString(),
                currentInferredVersionStatementList.get(0).getObject().toString());
    }
    
    /**
     * Helper method to verify that the given InferredOWLOntologyID represents an artifact that has
     * been successfully loaded.
     * 
     * @param inferredOntologyId
     *            Identifies the loaded ontology
     * @param mgtGraphSize
     *            Expected size of the artifact management graph
     * @param assertedStatementCount
     *            Number of asserted statements in repository for this artifact
     * @param inferredStatementCount
     *            Number of inferred statements in repository for this artifact
     * @param isPublished
     *            True if the artifact is Published, false otherwise
     * @throws OpenRDFException
     * @throws RepositoryException
     */
    private void verifyLoadedArtifact(final InferredOWLOntologyID inferredOntologyId, final int mgtGraphSize,
            final long assertedStatementCount, final long inferredStatementCount, final boolean isPublished)
        throws RepositoryException, OpenRDFException
    
    {
        // verify: ontology ID has all details
        Assert.assertNotNull("Null ontology ID", inferredOntologyId);
        Assert.assertNotNull("Null ontology IRI", inferredOntologyId.getOntologyIRI());
        Assert.assertNotNull("Null ontology version IRI", inferredOntologyId.getVersionIRI());
        Assert.assertNotNull("Null inferred ontology IRI", inferredOntologyId.getInferredOntologyIRI());
        
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
            nextRepositoryConnection.begin();
            
            if(nextRepositoryConnection.size(inferredOntologyId.getVersionIRI().toOpenRDFURI()) != assertedStatementCount)
            {
                DebugUtils.printContents(nextRepositoryConnection, inferredOntologyId.getVersionIRI().toOpenRDFURI());
            }
            // verify: size of asserted graph
            Assert.assertEquals("Incorrect number of asserted statements for artifact", assertedStatementCount,
                    nextRepositoryConnection.size(inferredOntologyId.getVersionIRI().toOpenRDFURI()));
            
            // verify: size of inferred graph
            Assert.assertEquals("Incorrect number of inferred statements for artifact", inferredStatementCount,
                    nextRepositoryConnection.size(inferredOntologyId.getInferredOntologyIRI().toOpenRDFURI()));
            
            // verify: artifact management graph contents
            this.verifyArtifactManagementGraphContents(nextRepositoryConnection, mgtGraphSize,
                    this.testRepositoryManager.getArtifactManagementGraph(), inferredOntologyId.getOntologyIRI(),
                    inferredOntologyId.getVersionIRI(), inferredOntologyId.getInferredOntologyIRI());
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
            nextRepositoryConnection = null;
        }
    }
    
    /**
     * Helper method to verify that an updated artifact has expected version IRI etc.
     * 
     * @param updatedArtifact
     * @param expectedVersionIri
     * @param expectedConcreteStatementCount
     * @param nextRepositoryConnection
     * @throws Exception
     */
    private void verifyUpdatedArtifact(final InferredOWLOntologyID updatedArtifact, final String expectedVersionIri,
            final long expectedConcreteStatementCount, final RepositoryConnection nextRepositoryConnection)
        throws Exception
    {
        Assert.assertEquals("Unexpected concrete statement count", expectedConcreteStatementCount,
                nextRepositoryConnection.size(updatedArtifact.getVersionIRI().toOpenRDFURI()));
        
        // verify: owl:versionIRI incremented in graph
        final List<Statement> versionIRIs =
                Iterations.asList(nextRepositoryConnection.getStatements(null, PoddRdfConstants.OWL_VERSION_IRI, null,
                        false, updatedArtifact.getVersionIRI().toOpenRDFURI()));
        Assert.assertEquals("Should have only 1 version IRI", 1, versionIRIs.size());
        Assert.assertEquals("Version IRI not expected value", expectedVersionIri, versionIRIs.get(0).getObject()
                .stringValue());
        
        // verify: current version updated in management graph
        final InferredOWLOntologyID currentArtifactVersion =
                this.testArtifactManager.getSesameManager().getCurrentArtifactVersion(updatedArtifact.getOntologyIRI(),
                        nextRepositoryConnection, this.artifactGraph);
        Assert.assertEquals("Unexpected Version IRI in management graph", expectedVersionIri, currentArtifactVersion
                .getVersionIRI().toString());
    }
    
}
