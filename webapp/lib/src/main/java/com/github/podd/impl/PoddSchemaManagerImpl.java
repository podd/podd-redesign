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
package com.github.podd.impl;

import info.aduna.iteration.Iterations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.exception.EmptyOntologyException;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.PoddRuntimeException;
import com.github.podd.exception.SchemaManifestException;
import com.github.podd.exception.UnmanagedSchemaException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.exception.UnmanagedSchemaOntologyIDException;
import com.github.podd.restlet.ApplicationUtils;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PODD;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class PoddSchemaManagerImpl implements PoddSchemaManager
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private PoddRepositoryManager repositoryManager;
    private PoddSesameManager sesameManager;
    private PoddOWLManager owlManager;
    
    /**
     *
     */
    public PoddSchemaManagerImpl()
    {
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void downloadSchemaOntology(final InferredOWLOntologyID ontologyId, final OutputStream outputStream,
            final RDFFormat format, final boolean includeInferred) throws UnmanagedSchemaException,
        RepositoryException, OpenRDFException
    {
        if(ontologyId.getOntologyIRI() == null || ontologyId.getVersionIRI() == null)
        {
            throw new PoddRuntimeException("Ontology IRI and Version IRI cannot be null");
        }
        
        final InferredOWLOntologyID schemaOntologyID = this.getSchemaOntologyID(ontologyId);
        
        if(includeInferred && schemaOntologyID.getInferredOntologyIRI() == null)
        {
            throw new PoddRuntimeException("Inferred Ontology IRI cannot be null");
        }
        
        List<URI> contexts;
        
        if(includeInferred)
        {
            contexts =
                    Arrays.asList(schemaOntologyID.getVersionIRI().toOpenRDFURI(), schemaOntologyID
                            .getInferredOntologyIRI().toOpenRDFURI());
        }
        else
        {
            contexts = Arrays.asList(schemaOntologyID.getVersionIRI().toOpenRDFURI());
        }
        
        RepositoryConnection managementConnection = null;
        
        try
        {
            managementConnection = this.repositoryManager.getManagementRepositoryConnection();
            
            final RepositoryResult<Statement> statements =
                    managementConnection.getStatements(null, null, null, includeInferred,
                            contexts.toArray(new Resource[] {}));
            final Model model = new LinkedHashModel(Iterations.asList(statements));
            final RepositoryResult<Namespace> namespaces = managementConnection.getNamespaces();
            for(final Namespace nextNs : Iterations.asSet(namespaces))
            {
                model.setNamespace(nextNs);
            }
            Rio.write(model, Rio.createWriter(format, outputStream));
        }
        finally
        {
            if(managementConnection != null)
            {
                managementConnection.close();
            }
        }
    }
    
    @Override
    public Set<InferredOWLOntologyID> getCurrentSchemaOntologies() throws OpenRDFException
    {
        RepositoryConnection managementConnection = null;
        
        try
        {
            managementConnection = this.repositoryManager.getManagementRepositoryConnection();
            
            return this.sesameManager.getAllCurrentSchemaOntologyVersions(managementConnection,
                    this.repositoryManager.getSchemaManagementGraph());
        }
        finally
        {
            if(managementConnection != null)
            {
                managementConnection.close();
            }
        }
    }
    
    @Override
    public InferredOWLOntologyID getCurrentSchemaOntologyVersion(final IRI schemaOntologyIRI)
        throws UnmanagedSchemaIRIException, OpenRDFException
    {
        if(schemaOntologyIRI == null)
        {
            throw new UnmanagedSchemaIRIException(null, "NULL is not a managed schema ontology");
        }
        
        RepositoryConnection managementConnection = null;
        try
        {
            managementConnection = this.repositoryManager.getManagementRepositoryConnection();
            
            return this.sesameManager.getCurrentSchemaVersion(schemaOntologyIRI, managementConnection,
                    this.repositoryManager.getSchemaManagementGraph());
        }
        finally
        {
            if(managementConnection != null)
            {
                managementConnection.close();
            }
        }
    }
    
    @Override
    public Set<InferredOWLOntologyID> getSchemaOntologies() throws OpenRDFException
    {
        RepositoryConnection managementConnection = null;
        
        try
        {
            managementConnection = this.repositoryManager.getManagementRepositoryConnection();
            
            return this.sesameManager.getAllSchemaOntologyVersions(managementConnection,
                    this.repositoryManager.getSchemaManagementGraph());
        }
        finally
        {
            if(managementConnection != null)
            {
                managementConnection.close();
            }
        }
    }
    
    @Override
    public OWLOntology getSchemaOntology(final URI schemaOntologyIRI) throws UnmanagedSchemaIRIException
    {
        throw new RuntimeException("TODO: Implement getSchemaOntology(IRI)");
    }
    
    @Override
    public OWLOntology getSchemaOntology(final OWLOntologyID schemaOntologyID)
        throws UnmanagedSchemaOntologyIDException
    {
        throw new RuntimeException("TODO: Implement getSchemaOntology(OWLOntologyID)");
    }
    
    @Override
    public InferredOWLOntologyID getSchemaOntologyID(final OWLOntologyID owlOntologyID)
        throws UnmanagedSchemaOntologyIDException, OpenRDFException
    {
        if(owlOntologyID == null)
        {
            throw new UnmanagedSchemaOntologyIDException(owlOntologyID, "NULL is not a managed schema ontology");
        }
        
        RepositoryConnection managementConnection = null;
        try
        {
            managementConnection = this.repositoryManager.getManagementRepositoryConnection();
            
            final InferredOWLOntologyID version =
                    this.sesameManager.getSchemaVersion(owlOntologyID.getVersionIRI(), managementConnection,
                            this.repositoryManager.getSchemaManagementGraph());
            
            // Check that the ontology IRI matches or return an error
            if(version.getOntologyIRI().equals(owlOntologyID.getOntologyIRI()))
            {
                return version;
            }
            else
            {
                throw new UnmanagedSchemaOntologyIDException(owlOntologyID,
                        "Version did not match the ontology IRI specified for the ontology.");
            }
        }
        catch(final UnmanagedSchemaIRIException e)
        {
            throw new UnmanagedSchemaOntologyIDException(owlOntologyID,
                    "Could not find the version specified for the ontology.", e);
        }
        finally
        {
            if(managementConnection != null)
            {
                managementConnection.close();
            }
        }
    }
    
    @Override
    public InferredOWLOntologyID getSchemaOntologyVersion(final IRI schemaVersionIRI)
        throws UnmanagedSchemaIRIException, OpenRDFException
    {
        if(schemaVersionIRI == null)
        {
            throw new UnmanagedSchemaIRIException(null, "NULL is not a managed schema ontology");
        }
        
        RepositoryConnection managementConnection = null;
        try
        {
            managementConnection = this.repositoryManager.getManagementRepositoryConnection();
            
            return this.sesameManager.getSchemaVersion(schemaVersionIRI, managementConnection,
                    this.repositoryManager.getSchemaManagementGraph());
        }
        finally
        {
            if(managementConnection != null)
            {
                managementConnection.close();
            }
        }
    }
    
    @Override
    public void setCurrentSchemaOntologyVersion(final OWLOntologyID schemaOntologyID)
        throws UnmanagedSchemaOntologyIDException, IllegalArgumentException, OpenRDFException
    {
        RepositoryConnection managementConnection = null;
        try
        {
            managementConnection = this.repositoryManager.getManagementRepositoryConnection();
            
            this.setUpdateManagedSchemaOntologyVersionInternal(schemaOntologyID, true, managementConnection,
                    this.repositoryManager.getSchemaManagementGraph());
        }
        finally
        {
            if(managementConnection != null)
            {
                managementConnection.close();
            }
        }
    }
    
    private void setUpdateManagedSchemaOntologyVersionInternal(final OWLOntologyID schemaOntologyID,
            final boolean updateCurrent, final RepositoryConnection managementConnection,
            final URI schemaManagementContext) throws UnmanagedSchemaOntologyIDException, OpenRDFException
    {
        this.sesameManager.updateManagedSchemaOntologyVersion(schemaOntologyID, updateCurrent, managementConnection,
                schemaManagementContext);
    }
    
    @Override
    public void setOwlManager(final PoddOWLManager owlManager)
    {
        this.owlManager = owlManager;
    }
    
    @Override
    public void setRepositoryManager(final PoddRepositoryManager repositoryManager)
    {
        this.repositoryManager = repositoryManager;
    }
    
    @Override
    public void setSesameManager(final PoddSesameManager sesameManager)
    {
        this.sesameManager = sesameManager;
    }
    
    @Override
    public List<InferredOWLOntologyID> uploadSchemaOntologies(final Model model) throws OpenRDFException, IOException,
        OWLException, PoddException
    {
        final Set<URI> schemaOntologyUris = new HashSet<>();
        final Set<URI> schemaVersionUris = new HashSet<>();
        OntologyUtils.extractOntologyAndVersions(model, schemaOntologyUris, schemaVersionUris);
        OntologyUtils.validateSchemaManifestImports(model, schemaOntologyUris, schemaVersionUris);
        final ConcurrentMap<URI, URI> currentVersionsMap = new ConcurrentHashMap<URI, URI>();
        // Find current version for each schema ontology
        for(final URI nextSchemaOntologyUri : schemaOntologyUris)
        {
            OntologyUtils.mapCurrentVersion(model, currentVersionsMap, nextSchemaOntologyUri);
        }
        // Map<URI, Set<OWLOntologyID>> allImports =
        // OntologyUtils.schemaManifestImports(model, schemaOntologyUris, schemaVersionUris);
        
        final List<InferredOWLOntologyID> dependentSchemaOntologies =
                OntologyUtils.modelToOntologyIDs(model, false, false);
        ConcurrentMap<URI, Set<URI>> importsMap = new ConcurrentHashMap<>();
        final List<OWLOntologyID> manifestImports =
                OntologyUtils.schemaImports(model, new LinkedHashSet<>(dependentSchemaOntologies), importsMap);
        
        this.log.debug("Uploading schema ontologies: {}", manifestImports);
        // OntologyUtils.recursiveFollowImports(ontologyImports, importsMap, nextURI);
        return this.uploadSchemaOntologiesInOrder(model, manifestImports, currentVersionsMap, importsMap);
    }
    
    /**
     * Given the manifest {@link Model} and the overall order of imports based on ontology version
     * IRIs, import all of the ontologies which have new versions.
     *
     * @param model
     *            The complete schema ontology information.
     * @param nextImportOrder
     *            The order of the schema imports.
     * @param currentVersionsMap
     *            A map specifying what the current versions for each schema ontology are to be
     *            after the uploads complete.
     * @param importsMap
     *            The map from all ontology version IRIs to the set of version IRIs that they have
     *            dependencies on. This enables loading of nextImportOrder consistently.
     * @return The IDs for the schema ontologies that were successfully uploaded.
     * @throws ModelException
     * @throws OpenRDFException
     * @throws IOException
     * @throws OWLException
     * @throws PoddException
     */
    private List<InferredOWLOntologyID> uploadSchemaOntologiesInOrder(final Model model,
            final List<OWLOntologyID> nextImportOrder, final ConcurrentMap<URI, URI> currentVersionsMap,
            ConcurrentMap<URI, Set<URI>> importsMap) throws ModelException, OpenRDFException, IOException,
        OWLException, PoddException
    {
        Objects.requireNonNull(model, "Schema Ontology model was null");
        Objects.requireNonNull(nextImportOrder, "Schema Ontology import order was null");
        
        final Map<OWLOntologyID, Boolean> loadingOrder = new LinkedHashMap<>();
        
        RepositoryConnection managementConnection = null;
        
        try
        {
            
            managementConnection = this.repositoryManager.getManagementRepositoryConnection();
            
            if(this.log.isDebugEnabled())
            {
                DebugUtils.printContents(managementConnection, this.repositoryManager.getSchemaManagementGraph());
            }
            
            final Set<InferredOWLOntologyID> existingSchemaOntologies =
                    this.sesameManager.getAllSchemaOntologyVersions(managementConnection,
                            this.repositoryManager.getSchemaManagementGraph());
            this.log.debug("currentVersionsMap : {}", currentVersionsMap);
            this.log.debug("nextImportOrder : {}", nextImportOrder);
            this.log.debug("Existing schema ontologies at this point: {}", existingSchemaOntologies);
            
            for(final OWLOntologyID nextImport : nextImportOrder)
            {
                boolean alreadyLoaded = false;
                for(final InferredOWLOntologyID nextCurrentSchemaOntology : existingSchemaOntologies)
                {
                    if(nextImport.equals(nextCurrentSchemaOntology))
                    {
                        // FIXME: Must do it this way to preserve inferred ontology information
                        // which may not be present in nextImport
                        alreadyLoaded = true;
                        break;
                    }
                }
                loadingOrder.put(nextImport, alreadyLoaded);
            }
            
            final List<InferredOWLOntologyID> results = new ArrayList<>();
            
            this.log.debug("About to load ontologies in order: {}", loadingOrder);
            for(final Entry<OWLOntologyID, Boolean> loadEntry : loadingOrder.entrySet())
            {
                this.log.debug("Ontologies loaded so far: {}", results);
                if(loadEntry.getValue())
                {
                    this.log.debug("Not loading ontology from scratch as it was already available: {}",
                            loadEntry.getKey());
                    if(loadEntry.getKey() instanceof InferredOWLOntologyID)
                    {
                        results.add((InferredOWLOntologyID)loadEntry.getKey());
                    }
                    else
                    {
                        this.log.error("Found an already loaded ontology without an inferred IRI: {}",
                                loadEntry.getKey());
                        results.add(new InferredOWLOntologyID(loadEntry.getKey().getOntologyIRI(), loadEntry.getKey()
                                .getVersionIRI(), null));
                    }
                }
                else
                {
                    this.log.debug("Need to load ontology that is not already available in management repository: {}",
                            loadEntry.getKey());
                    final OWLOntologyID loadEntryID = loadEntry.getKey();
                    final String classpathLocation =
                            model.filter(loadEntryID.getVersionIRI().toOpenRDFURI(), PODD.PODD_SCHEMA_CLASSPATH, null)
                                    .objectLiteral().stringValue();
                    this.log.debug("Loading from classpath: {}", classpathLocation);
                    final RDFFormat fileFormat = Rio.getParserFormatForFileName(classpathLocation, RDFFormat.RDFXML);
                    try (final InputStream inputStream = ApplicationUtils.class.getResourceAsStream(classpathLocation);)
                    {
                        if(inputStream == null)
                        {
                            throw new SchemaManifestException(loadEntryID.getVersionIRI(),
                                    "Could not find schema at designated classpath location: " + classpathLocation);
                        }
                        
                        managementConnection.begin();
                        final OWLOntologyID schemaOntologyID = null;
                        // Need to get the minimal set of imports for the next schema ontology and
                        // load exactly those ontologies
                        Set<URI> nextMinimalImportsSet = importsMap.get(loadEntryID.getVersionIRI().toOpenRDFURI());
                        Set<? extends OWLOntologyID> nextMinimalOntologyIDs =
                                OntologyUtils.mapFromVersions(nextMinimalImportsSet, nextImportOrder);
                        this.log.debug("nextMinimalImportsSet: {}", nextMinimalImportsSet);
                        this.log.debug("nextMinimalOntologyIDs: {}", nextMinimalOntologyIDs);
                        final InferredOWLOntologyID nextResult =
                                this.uploadSchemaOntologyInternal(schemaOntologyID, inputStream, fileFormat,
                                        managementConnection, this.repositoryManager.getSchemaManagementGraph(),
                                        nextMinimalOntologyIDs);
                        
                        boolean updateCurrent = true;
                        if(currentVersionsMap.containsKey(nextResult.getOntologyIRI().toOpenRDFURI()))
                        {
                            if(!currentVersionsMap.get(nextResult.getOntologyIRI().toOpenRDFURI()).equals(
                                    nextResult.getVersionIRI().toOpenRDFURI()))
                            {
                                updateCurrent = false;
                            }
                        }
                        
                        this.log.debug("Current version update required : {} {}", nextResult, updateCurrent);
                        
                        this.setUpdateManagedSchemaOntologyVersionInternal(nextResult, updateCurrent,
                                managementConnection, this.repositoryManager.getSchemaManagementGraph());
                        
                        final List<Statement> importStatements =
                                Iterations.asList(managementConnection.getStatements(nextResult.getOntologyIRI()
                                        .toOpenRDFURI(), OWL.IMPORTS, null, true, nextResult.getVersionIRI()
                                        .toOpenRDFURI()));
                        
                        this.log.debug("Imports to copy for ontology: {} {}", nextResult, importStatements);
                        
                        for(final Statement nextImportStatement : importStatements)
                        {
                            managementConnection.add(nextResult.getVersionIRI().toOpenRDFURI(), OWL.IMPORTS,
                                    nextImportStatement.getObject(), this.repositoryManager.getSchemaManagementGraph());
                        }
                        
                        for(URI nextMinimalImport : nextMinimalImportsSet)
                        {
                            // Add from the analysed schema manifest which includes the full
                            // heirarchy
                            managementConnection.add(nextResult.getVersionIRI().toOpenRDFURI(), OWL.IMPORTS,
                                    nextMinimalImport, this.repositoryManager.getSchemaManagementGraph());
                        }
                        managementConnection.commit();
                        
                        results.add(nextResult);
                    }
                    
                }
            }
            
            this.log.debug("Completed loading schema ontologies");
            
            return results;
        }
        catch(final Throwable e)
        {
            if(managementConnection != null)
            {
                managementConnection.rollback();
            }
            
            throw e;
        }
        finally
        {
            if(managementConnection != null)
            {
                managementConnection.close();
            }
        }
    }
    
    @Override
    public InferredOWLOntologyID uploadSchemaOntology(final InputStream inputStream, final RDFFormat fileFormat,
            final Set<? extends OWLOntologyID> dependentSchemaOntologies) throws OpenRDFException, IOException,
        OWLException, PoddException
    {
        return this.uploadSchemaOntology(null, inputStream, fileFormat, dependentSchemaOntologies);
    }
    
    /*
     * FIXME: check if the version exists in the repository before upload
     */
    @Override
    public InferredOWLOntologyID uploadSchemaOntology(final OWLOntologyID schemaOntologyID,
            final InputStream inputStream, final RDFFormat fileFormat,
            final Set<? extends OWLOntologyID> dependentSchemaOntologies) throws OpenRDFException, IOException,
        OWLException, PoddException
    {
        Objects.requireNonNull(inputStream, "Schema Ontology input stream was null");
        
        RepositoryConnection managementConnection = null;
        
        try
        {
            // TODO: Should we store these copies in a separate repository
            // again, to reduce bloat in
            // the management repository??
            managementConnection = this.repositoryManager.getManagementRepositoryConnection();
            managementConnection.begin();
            
            // TODO: Call this method directly from other methods so that the whole transaction can
            // be rolled back if there are any failures!
            final InferredOWLOntologyID nextResult =
                    this.uploadSchemaOntologyInternal(schemaOntologyID, inputStream, fileFormat, managementConnection,
                            this.repositoryManager.getSchemaManagementGraph(), dependentSchemaOntologies);
            
            this.setUpdateManagedSchemaOntologyVersionInternal(nextResult, true, managementConnection,
                    this.repositoryManager.getSchemaManagementGraph());
            
            final List<Statement> importStatements =
                    Iterations.asList(managementConnection.getStatements(nextResult.getOntologyIRI().toOpenRDFURI(),
                            OWL.IMPORTS, null, true, nextResult.getVersionIRI().toOpenRDFURI()));
            for(final Statement nextImportStatement : importStatements)
            {
                managementConnection.add(nextResult.getVersionIRI().toOpenRDFURI(), OWL.IMPORTS,
                        nextImportStatement.getObject(), this.repositoryManager.getSchemaManagementGraph());
            }
            
            managementConnection.commit();
            
            return nextResult;
        }
        catch(final Throwable e)
        {
            if(managementConnection != null)
            {
                managementConnection.rollback();
            }
            
            throw e;
        }
        finally
        {
            if(managementConnection != null)
            {
                managementConnection.close();
            }
        }
        
    }
    
    /**
     * @param schemaOntologyID
     * @param inputStream
     * @param fileFormat
     * @param managementConnection
     * @param dependentSchemaOntologies
     * @return
     * @throws OWLException
     * @throws IOException
     * @throws PoddException
     * @throws EmptyOntologyException
     * @throws RepositoryException
     * @throws OWLRuntimeException
     * @throws OpenRDFException
     */
    private InferredOWLOntologyID uploadSchemaOntologyInternal(final OWLOntologyID schemaOntologyID,
            final InputStream inputStream, final RDFFormat fileFormat, final RepositoryConnection managementConnection,
            final URI schemaManagementGraph, final Set<? extends OWLOntologyID> dependentSchemaOntologies)
        throws OWLException, IOException, PoddException, EmptyOntologyException, RepositoryException,
        OWLRuntimeException, OpenRDFException
    {
        this.log.info("Dependent ontologies for next schema upload: {}", dependentSchemaOntologies);
        
        final OWLOntologyDocumentSource owlSource =
                new StreamDocumentSource(inputStream, fileFormat.getDefaultMIMEType());
        final InferredOWLOntologyID nextInferredOntology =
                this.owlManager.loadAndInfer(owlSource, managementConnection, schemaOntologyID,
                        dependentSchemaOntologies, managementConnection, schemaManagementGraph);
        
        // update the link in the schema ontology management graph
        // TODO: This may not be the right method for this purpose
        // this.sesameManager.updateManagedSchemaOntologyVersion(nextInferredOntology, true,
        // managementConnection,
        // schemaManagementGraph);
        
        return nextInferredOntology;
        // TODO: Why are we not able to return nextInferredOntology here
        // final InferredOWLOntologyID result =
        // new InferredOWLOntologyID(nextInferredOntology.getOntologyIRI(),
        // nextInferredOntology.getVersionIRI(),
        // nextInferredOntology.getOntologyIRI());
        // return result;
    }
}
