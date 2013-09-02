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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
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
    public void downloadSchemaOntology(final OWLOntologyID schemaOntologyID, final OutputStream outputStream,
            final RDFFormat format, final boolean includeInferences) throws UnmanagedSchemaException
    {
        throw new RuntimeException("TODO: Implement downloadSchemaOntology");
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
        this.log.info("adding import for {} at {}", nextVersionUri, maxIndex);
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
            // Check to see if this is actually a version, in which case ignore it for now
            if(nextOntology instanceof URI)
            {
                if(model.contains(null, OWL.VERSIONIRI, nextOntology))
                {
                    schemaVersionUris.add((URI)nextOntology);
                }
                else
                {
                    schemaOntologyUris.add((URI)nextOntology);
                }
            }
        }
        final ConcurrentMap<URI, URI> currentVersionsMap = new ConcurrentHashMap<>(schemaOntologyUris.size());
        final ConcurrentMap<URI, Set<URI>> allVersionsMap = new ConcurrentHashMap<>(schemaOntologyUris.size());
        final ConcurrentMap<URI, Set<URI>> importsMap = new ConcurrentHashMap<>(schemaOntologyUris.size());
        
        final List<URI> importOrder = new ArrayList<>(schemaOntologyUris.size());
        
        for(final URI nextSchemaOntologyUri : schemaOntologyUris)
        {
            this.mapCurrentVersion(model, currentVersionsMap, nextSchemaOntologyUri);
        }
        
        for(final URI nextSchemaOntologyUri : schemaOntologyUris)
        {
            this.mapAllVersions(model, currentVersionsMap, allVersionsMap, nextSchemaOntologyUri);
        }
        
        for(final URI nextVersionUri : schemaVersionUris)
        {
            this.mapAndSortImports(model, currentVersionsMap, allVersionsMap, importsMap, importOrder,
                    nextVersionUri);
        }
        
        this.log.info("importOrder: {}", importOrder);
        
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
}
