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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
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
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;

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
        
        InferredOWLOntologyID schemaOntologyID = this.getSchemaOntologyID(ontologyId);
        
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
        
        RepositoryConnection connection = null;
        
        try
        {
            connection = this.repositoryManager.getRepository().getConnection();
            
            RepositoryResult<Statement> statements =
                    connection.getStatements(null, null, null, includeInferred, contexts.toArray(new Resource[] {}));
            Model model = new LinkedHashModel(Iterations.asList(statements));
            RepositoryResult<Namespace> namespaces = connection.getNamespaces();
            for(Namespace nextNs : Iterations.asSet(namespaces))
            {
                model.setNamespace(nextNs);
            }
            Rio.write(model, Rio.createWriter(format, outputStream));
        }
        finally
        {
            if(connection != null)
            {
                connection.close();
            }
        }
    }
    
    @Override
    public Set<InferredOWLOntologyID> getCurrentSchemaOntologies() throws OpenRDFException
    {
        RepositoryConnection conn = null;
        
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            
            return this.sesameManager.getAllCurrentSchemaOntologyVersions(conn,
                    this.repositoryManager.getSchemaManagementGraph());
        }
        finally
        {
            if(conn != null && conn.isOpen())
            {
                conn.close();
            }
        }
    }
    
    @Override
    public InferredOWLOntologyID getCurrentSchemaOntologyVersion(final IRI schemaOntologyIRI)
        throws UnmanagedSchemaIRIException, OpenRDFException
    {
        if(schemaOntologyIRI == null)
        {
            throw new UnmanagedSchemaIRIException(schemaOntologyIRI, "NULL is not a managed schema ontology");
        }
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            return this.sesameManager.getCurrentSchemaVersion(schemaOntologyIRI, conn,
                    this.repositoryManager.getSchemaManagementGraph());
        }
        finally
        {
            if(conn != null && conn.isActive())
            {
                conn.rollback();
            }
            if(conn != null && conn.isOpen())
            {
                conn.close();
            }
        }
    }
    
    @Override
    public Set<InferredOWLOntologyID> getSchemaOntologies() throws OpenRDFException
    {
        RepositoryConnection conn = null;
        
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            
            return this.sesameManager.getAllSchemaOntologyVersions(conn,
                    this.repositoryManager.getSchemaManagementGraph());
        }
        finally
        {
            if(conn != null && conn.isOpen())
            {
                conn.close();
            }
        }
    }
    
    @Override
    public OWLOntology getSchemaOntology(final IRI schemaOntologyIRI) throws UnmanagedSchemaIRIException
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
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            final InferredOWLOntologyID version =
                    this.sesameManager.getSchemaVersion(owlOntologyID.getVersionIRI(), conn,
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
            if(conn != null && conn.isActive())
            {
                conn.rollback();
            }
            if(conn != null && conn.isOpen())
            {
                conn.close();
            }
        }
    }
    
    @Override
    public InferredOWLOntologyID getSchemaOntologyVersion(final IRI schemaVersionIRI)
        throws UnmanagedSchemaIRIException, OpenRDFException
    {
        if(schemaVersionIRI == null)
        {
            throw new UnmanagedSchemaIRIException(schemaVersionIRI, "NULL is not a managed schema ontology");
        }
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            return this.sesameManager.getSchemaVersion(schemaVersionIRI, conn,
                    this.repositoryManager.getSchemaManagementGraph());
        }
        finally
        {
            if(conn != null && conn.isActive())
            {
                conn.rollback();
            }
            if(conn != null && conn.isOpen())
            {
                conn.close();
            }
        }
    }
    
    /**
     * @param model
     * @param currentVersionsMap
     * @param allVersionsMap
     * @param nextSchemaOntologyUri
     */
    public void mapAllVersions(final Model model, final ConcurrentMap<URI, URI> currentVersionsMap,
            final ConcurrentMap<URI, Set<URI>> allVersionsMap, final URI nextSchemaOntologyUri)
    {
        final Set<Value> allVersions = model.filter(nextSchemaOntologyUri, OWL.VERSIONIRI, null).objects();
        Set<URI> nextAllVersions = new HashSet<>();
        final Set<URI> putIfAbsent = allVersionsMap.putIfAbsent(nextSchemaOntologyUri, nextAllVersions);
        if(putIfAbsent != null)
        {
            nextAllVersions = putIfAbsent;
        }
        // If they specified a current version add it to the set
        if(currentVersionsMap.containsKey(nextSchemaOntologyUri))
        {
            nextAllVersions.add(currentVersionsMap.get(nextSchemaOntologyUri));
        }
        for(final Value nextVersionURI : allVersions)
        {
            if(nextVersionURI instanceof URI)
            {
                nextAllVersions.add((URI)nextVersionURI);
            }
            else
            {
                this.log.error("Version was not a URI: {} {}", nextSchemaOntologyUri, nextVersionURI);
            }
        }
        
        if(nextAllVersions.isEmpty())
        {
            this.log.error("Could not find any version information for schema ontology: {}", nextSchemaOntologyUri);
        }
    }
    
    /**
     * @param model
     * @param currentVersionsMap
     * @param allVersionsMap
     * @param importsMap
     * @param importOrder
     * @param nextVersionUri
     */
    public void mapAndSortImports(final Model model, final ConcurrentMap<URI, URI> currentVersionsMap,
            final ConcurrentMap<URI, Set<URI>> allVersionsMap, final ConcurrentMap<URI, Set<URI>> importsMap,
            final List<URI> importOrder, final URI nextVersionUri)
    {
        final Set<Value> imports = model.filter(nextVersionUri, OWL.IMPORTS, null).objects();
        Set<URI> nextImportsSet = new LinkedHashSet<>();
        final Set<URI> putIfAbsent = importsMap.putIfAbsent(nextVersionUri, nextImportsSet);
        if(putIfAbsent != null)
        {
            nextImportsSet = putIfAbsent;
        }
        int maxIndex = 0;
        if(imports.isEmpty())
        {
            if(!nextImportsSet.isEmpty())
            {
                this.log.error("Found inconsistent imports set: {} {}", nextVersionUri, nextImportsSet);
            }
        }
        else
        {
            for(Value nextImport : imports)
            {
                if(nextImport instanceof URI)
                {
                    if(nextImportsSet.contains(nextImport))
                    {
                        // avoid duplicates
                        continue;
                    }
                    
                    if(currentVersionsMap.containsKey(nextImport))
                    {
                        // Map down to the current version to ensure that we can load
                        // multiple versions simultaneously (if possible with the rest of
                        // the system)
                        nextImportsSet.add(currentVersionsMap.get(nextImport));
                    }
                    else if(currentVersionsMap.containsValue(nextImport))
                    {
                        nextImportsSet.add((URI)nextImport);
                    }
                    else
                    {
                        boolean foundAllVersion = false;
                        // Attempt to verify if the version exists
                        for(final URI nextAllVersions : allVersionsMap.keySet())
                        {
                            if(nextAllVersions.equals(nextImport))
                            {
                                foundAllVersion = true;
                                // this should not normally occur, as the current versions
                                // map should also contain this key
                                nextImport = currentVersionsMap.get(nextAllVersions);
                                nextImportsSet.add((URI)nextImport);
                            }
                            else if(allVersionsMap.get(nextAllVersions).contains(nextImport))
                            {
                                nextImportsSet.add((URI)nextImport);
                                foundAllVersion = true;
                            }
                        }
                        
                        if(!foundAllVersion)
                        {
                            this.log.warn("Could not find import: {} imports {}", nextVersionUri, nextImport);
                        }
                        else
                        {
                            nextImportsSet.add((URI)nextImport);
                        }
                    }
                    final int nextIndex = importOrder.indexOf(nextImport);
                    if(nextIndex >= maxIndex)
                    {
                        maxIndex = nextIndex + 1;
                    }
                }
            }
        }
        this.log.debug("adding import for {} at {}", nextVersionUri, maxIndex);
        // TODO: FIXME: This will not allow for multiple versions of a single schema
        // ontology at the same time
        importOrder.add(maxIndex, nextVersionUri);
    }
    
    /**
     * @param model
     * @param currentVersionsMap
     * @param nextSchemaOntologyUri
     * @throws SchemaManifestException
     */
    public void mapCurrentVersion(final Model model, final ConcurrentMap<URI, URI> currentVersionsMap,
            final URI nextSchemaOntologyUri) throws SchemaManifestException
    {
        try
        {
            final URI nextCurrentVersionURI =
                    model.filter(nextSchemaOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, null).objectURI();
            
            if(nextCurrentVersionURI == null)
            {
                this.log.error("Did not find a current version for schema ontology: {}", nextSchemaOntologyUri);
                throw new SchemaManifestException(IRI.create(nextSchemaOntologyUri),
                        "Did not find a current version for schema ontology");
            }
            else
            {
                final URI putIfAbsent = currentVersionsMap.putIfAbsent(nextSchemaOntologyUri, nextCurrentVersionURI);
                if(putIfAbsent != null)
                {
                    this.log.error("Found multiple version URIs for schema ontology: {} old={} new={}",
                            nextSchemaOntologyUri, putIfAbsent, nextCurrentVersionURI);
                    throw new SchemaManifestException(IRI.create(nextSchemaOntologyUri),
                            "Found multiple version IRIs for schema ontology");
                }
            }
        }
        catch(final ModelException e)
        {
            this.log.error("Could not find a single unique current version for schema ontology: {}",
                    nextSchemaOntologyUri);
            throw new SchemaManifestException(IRI.create(nextSchemaOntologyUri),
                    "Could not find a single unique current version IRI for schema ontology");
        }
    }
    
    @Override
    public void setCurrentSchemaOntologyVersion(final OWLOntologyID schemaOntologyID)
        throws UnmanagedSchemaOntologyIDException, IllegalArgumentException
    {
        throw new RuntimeException("TODO: Implement setCurrentSchemaOntologyVersion");
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
    public void uploadSchemaOntologies(final Model model) throws OpenRDFException, IOException, OWLException,
        PoddException
    {
        final Set<URI> schemaOntologyUris = new HashSet<>();
        final Set<URI> schemaVersionUris = new HashSet<>();
        for(final Resource nextOntology : model.filter(null, RDF.TYPE, OWL.ONTOLOGY).subjects())
        {
            if(nextOntology instanceof URI)
            {
                if(model.contains(null, OWL.VERSIONIRI, nextOntology))
                {
                    schemaVersionUris.add((URI)nextOntology);
                }
                else
                {
                    schemaOntologyUris.add((URI)nextOntology);
                    // check ontology IRI does not have any associated owl:imports
                    if(model.filter(nextOntology, OWL.IMPORTS, null).objects().size() > 0)
                    {
                        throw new SchemaManifestException(IRI.create((URI)nextOntology),
                                "Imports should be associated with version IRI");
                    }
                }
            }
        }
        
        this.validateSchemaManifestImports(model, schemaVersionUris);
        
        final ConcurrentMap<URI, URI> currentVersionsMap = new ConcurrentHashMap<>(schemaOntologyUris.size());
        final ConcurrentMap<URI, Set<URI>> allVersionsMap = new ConcurrentHashMap<>(schemaOntologyUris.size());
        final ConcurrentMap<URI, Set<URI>> importsMap = new ConcurrentHashMap<>(schemaOntologyUris.size());
        
        final List<URI> importOrder = new ArrayList<>(schemaOntologyUris.size());
        
        // Find current version for each schema ontology
        for(final URI nextSchemaOntologyUri : schemaOntologyUris)
        {
            this.mapCurrentVersion(model, currentVersionsMap, nextSchemaOntologyUri);
        }
        
        // Find all versions for each schema ontology
        for(final URI nextSchemaOntologyUri : schemaOntologyUris)
        {
            this.mapAllVersions(model, currentVersionsMap, allVersionsMap, nextSchemaOntologyUri);
        }
        
        // Map the actual schema ontologies to the correct order, based on current versions and all
        // versions with the imports taken into account
        for(final URI nextVersionUri : schemaVersionUris)
        {
            this.mapAndSortImports(model, currentVersionsMap, allVersionsMap, importsMap, importOrder, nextVersionUri);
        }
        
        this.log.debug("importOrder: {}", importOrder);
        
        this.uploadSchemaOntologiesInOrder(model, importOrder);
    }
    
    /**
     * @param model
     * @param importOrder
     * @throws ModelException
     * @throws OpenRDFException
     * @throws IOException
     * @throws OWLException
     * @throws PoddException
     */
    public void uploadSchemaOntologiesInOrder(final Model model, final List<URI> importOrder) throws ModelException,
        OpenRDFException, IOException, OWLException, PoddException
    {
        for(final URI nextOrderedImport : importOrder)
        {
            final String classpathLocation =
                    model.filter(nextOrderedImport, PoddRdfConstants.PODD_SCHEMA_CLASSPATH, null).objectLiteral()
                            .stringValue();
            final RDFFormat format = Rio.getParserFormatForFileName(classpathLocation, RDFFormat.RDFXML);
            try (final InputStream input = ApplicationUtils.class.getResourceAsStream(classpathLocation);)
            {
                this.uploadSchemaOntology(input, format);
            }
        }
    }
    
    @Override
    public InferredOWLOntologyID uploadSchemaOntology(final InputStream inputStream, final RDFFormat fileFormat)
        throws OpenRDFException, IOException, OWLException, PoddException
    {
        return this.uploadSchemaOntology(null, inputStream, fileFormat);
    }
    
    /*
     * FIXME: check if the version exists in the repository before upload
     */
    @Override
    public InferredOWLOntologyID uploadSchemaOntology(final OWLOntologyID schemaOntologyID,
            final InputStream inputStream, final RDFFormat fileFormat) throws OpenRDFException, IOException,
        OWLException, PoddException
    {
        if(inputStream == null)
        {
            throw new NullPointerException("Schema Ontology input stream was null");
        }
        
        final OWLOntologyDocumentSource owlSource =
                new StreamDocumentSource(inputStream, fileFormat.getDefaultMIMEType());
        final OWLOntology ontology = this.owlManager.loadOntology(owlSource);
        
        if(ontology.isEmpty())
        {
            throw new EmptyOntologyException(ontology, "Schema Ontology contained no axioms");
        }
        
        if(schemaOntologyID != null)
        {
            // FIXME: Change OWLOntologyID to schemaOntologyID in this case
        }
        
        RepositoryConnection conn = null;
        
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            this.owlManager.dumpOntologyToRepository(ontology, conn);
            
            final InferredOWLOntologyID nextInferredOntology = this.owlManager.inferStatements(ontology, conn);
            
            // update the link in the schema ontology management graph
            this.sesameManager.updateCurrentManagedSchemaOntologyVersion(nextInferredOntology, true, conn,
                    this.repositoryManager.getSchemaManagementGraph());
            
            // update the link in the schema ontology management graph
            // TODO: This is probably not the right method for this purpose
            this.sesameManager.updateCurrentManagedSchemaOntologyVersion(nextInferredOntology, true, conn,
                    this.repositoryManager.getSchemaManagementGraph());
            
            conn.commit();
            
            return new InferredOWLOntologyID(ontology.getOntologyID().getOntologyIRI(), ontology.getOntologyID()
                    .getVersionIRI(), nextInferredOntology.getOntologyIRI());
        }
        catch(OpenRDFException | IOException e)
        {
            if(conn != null && conn.isActive())
            {
                conn.rollback();
            }
            
            throw e;
        }
        finally
        {
            if(conn != null && conn.isOpen())
            {
                conn.close();
            }
        }
        
    }
    
    /**
     * This method performs a consistency check between the ontology imports specified in the
     * schema-manifest with the actual imports specified within the ontology.
     * 
     * If the two sets of imports are not equal, a {@link SchemaManifestException} is thrown.
     * 
     * @param manifestModel
     *            Contents of the schema-manifest
     * @param schemaVersionUris
     *            Ontology Version IRIs to be loaded
     * @throws IOException
     * @throws RDFParseException
     * @throws UnsupportedRDFormatException
     * @throws SchemaManifestException
     *             If the imports from the two locations are not consistent
     */
    public void validateSchemaManifestImports(final Model manifestModel, final Set<URI> schemaVersionUris)
        throws IOException, RDFParseException, UnsupportedRDFormatException, SchemaManifestException
    {
        for(final URI nextVersionUri : schemaVersionUris)
        {
            final Set<Value> importsInManifest = manifestModel.filter(nextVersionUri, OWL.IMPORTS, null).objects();
            
            final String classpathLocation =
                    manifestModel.filter(nextVersionUri, PoddRdfConstants.PODD_SCHEMA_CLASSPATH, null).objectLiteral()
                            .stringValue();
            final RDFFormat format = Rio.getParserFormatForFileName(classpathLocation, RDFFormat.RDFXML);
            try (final InputStream input = ApplicationUtils.class.getResourceAsStream(classpathLocation);)
            {
                final Model model = Rio.parse(input, "", format);
                final Set<Value> importsInOwlFile = model.filter(null, OWL.IMPORTS, null).objects();
                
                this.log.debug("Comparing: \n Manifest: {} \n Owl:      {}", importsInManifest, importsInOwlFile);
                if(!importsInManifest.equals(importsInOwlFile))
                {
                    throw new SchemaManifestException(IRI.create(nextVersionUri),
                            "Schema manifest imports not consistent with actual imports");
                }
            }
        }
    }
    
}
