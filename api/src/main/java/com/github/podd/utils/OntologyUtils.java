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
package com.github.podd.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.exception.SchemaManifestException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedArtifactVersionException;

/**
 * Utilities for working with {@link InferredOWLOntologyID}
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class OntologyUtils
{
    private static final Logger log = LoggerFactory.getLogger(OntologyUtils.class);
    
    /**
     * Finds the schema imports for the given artifact.
     *
     * Must not throw an {@link UnmanagedArtifactIRIException} or
     * {@link UnmanagedArtifactVersionException} if the artifact does not exist globally, as long as
     * it is managed correctly in the given model.
     *
     * @param artifactID
     * @param results
     * @param model
     * @throws OpenRDFException
     * @throws SchemaManifestException
     * @throws IOException
     * @throws RDFParseException
     * @throws UnsupportedRDFormatException
     */
    public static List<OWLOntologyID> artifactImports(final InferredOWLOntologyID artifactID, final Model model)
        throws OpenRDFException, SchemaManifestException, IOException, RDFParseException, UnsupportedRDFormatException
    {
        Objects.requireNonNull(artifactID);
        Objects.requireNonNull(model);
        
        final Set<OWLOntologyID> results = new LinkedHashSet<OWLOntologyID>();
        
        final Set<URI> schemaOntologyUris = new LinkedHashSet<>();
        final Set<URI> schemaVersionUris = new LinkedHashSet<>();
        final ConcurrentMap<URI, Set<URI>> importsMap = new ConcurrentHashMap<>(schemaOntologyUris.size());
        
        OntologyUtils.extractOntologyAndVersions(model, schemaOntologyUris, schemaVersionUris);
        
        if(!schemaOntologyUris.contains(artifactID.getOntologyIRI().toOpenRDFURI()))
        {
            throw new SchemaManifestException(artifactID.getOntologyIRI(),
                    "Did not find the given ontology IRI in the model: " + artifactID.getOntologyIRI());
        }
        
        final List<URI> orderImports =
                OntologyUtils.orderImports(model, schemaOntologyUris, schemaVersionUris, importsMap, true);
        
        final Set<URI> artifactImports = new LinkedHashSet<>();
        
        // Be tolerant for artifacts and add imports for both the ontology and the version
        OntologyUtils.recursiveFollowImports(artifactImports, importsMap, artifactID.getOntologyIRI().toOpenRDFURI());
        
        if(artifactID.getVersionIRI() != null)
        {
            OntologyUtils
                    .recursiveFollowImports(artifactImports, importsMap, artifactID.getVersionIRI().toOpenRDFURI());
        }
        
        final List<InferredOWLOntologyID> ontologyIDs = OntologyUtils.modelToOntologyIDs(model, true, false);
        
        OntologyUtils.postSort(orderImports, importsMap);
        
        final List<OWLOntologyID> finalOrderImports =
                OntologyUtils.finalOrderImports(results, ontologyIDs, orderImports, artifactImports, importsMap);
        
        return finalOrderImports;
    }
    
    /**
     * Extracts ontology and version IRIs to separate sets, which are both given as parameters.
     *
     * Only recognises ontology IRIs which have an "ontologyIRI, rdf:type, owl:Ontology" triple.
     *
     * Only recognises version IRIs which have both "versionIRI, rdf:type, owl:Ontology" and
     * "ontologyIRI, owl:versionIRI, versionIRI"
     *
     * @param model
     *            The input statements.
     * @param schemaOntologyUris
     *            An output container for all of the ontology IRIs which are not also version IRIs.
     * @param schemaVersionUris
     *            An output container for all of the ontology IRIs which are also versionIRIs.
     */
    public static void extractOntologyAndVersions(final Model model, final Set<URI> schemaOntologyUris,
            final Set<URI> schemaVersionUris)
    {
        for(final Statement nextImport : model.filter(null, OWL.VERSIONIRI, null))
        {
            if(nextImport.getObject() instanceof URI)
            {
                if(!model.contains((URI)nextImport.getObject(), RDF.TYPE, OWL.ONTOLOGY))
                {
                    model.add((URI)nextImport.getObject(), RDF.TYPE, OWL.ONTOLOGY);
                }
            }
        }
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
                }
            }
        }
    }
    
    /**
     * Call this to reorder the results set according to the order specified in orderImports.
     *
     * @param results
     * @param ontologyIDs
     * @param orderImports
     * @param artifactImports
     * @param importsMap
     * @throws SchemaManifestException
     */
    public static List<OWLOntologyID> finalOrderImports(final Set<OWLOntologyID> results,
            final List<? extends OWLOntologyID> ontologyIDs, final List<URI> orderImports,
            final Set<URI> artifactImports, final ConcurrentMap<URI, Set<URI>> importsMap)
        throws SchemaManifestException
    {
        final Set<OWLOntologyID> finalResults = new LinkedHashSet<OWLOntologyID>();
        for(final URI nextImport : orderImports)
        {
            // Iterate through all of the imports and select those which were actually found
            if(artifactImports.contains(nextImport))
            {
                boolean found = false;
                for(final OWLOntologyID nextOntologyID : ontologyIDs)
                {
                    if(nextOntologyID.getOntologyIRI().toOpenRDFURI().equals(nextImport))
                    {
                        finalResults.add(nextOntologyID);
                        found = true;
                        break;
                    }
                    else if(nextOntologyID.getVersionIRI() != null
                            && nextOntologyID.getVersionIRI().toOpenRDFURI().equals(nextImport))
                    {
                        finalResults.add(nextOntologyID);
                        found = true;
                        break;
                    }
                }
                if(!found)
                {
                    OntologyUtils.log.error("Could not map import to an ontology ID: ", nextImport);
                }
            }
        }
        return new ArrayList<>(finalResults);
    }
    
    /**
     * @param managementConnection
     * @param schemaManagementGraph
     * @param model
     * @return
     * @throws RepositoryException
     * @throws ModelException
     * @throws IOException
     * @throws RDFParseException
     * @throws SchemaManifestException
     */
    public static List<InferredOWLOntologyID> loadSchemasFromManifest(final RepositoryConnection managementConnection,
            final URI schemaManagementGraph, final Model model) throws RepositoryException, ModelException,
        IOException, RDFParseException, SchemaManifestException
    {
        managementConnection.add(model, schemaManagementGraph);
        
        final List<InferredOWLOntologyID> ontologyIDs = OntologyUtils.modelToOntologyIDs(model, false, false);
        
        for(final InferredOWLOntologyID nextOntology : ontologyIDs)
        {
            final String classpath =
                    model.filter(nextOntology.getVersionIRI().toOpenRDFURI(), PODD.PODD_SCHEMA_CLASSPATH, null)
                            .objectString();
            if(classpath == null)
            {
                throw new SchemaManifestException(nextOntology.getVersionIRI(),
                        "Ontology was not mapped to a classpath: " + nextOntology.toString());
            }
            final InputStream nextStream = OntologyUtils.class.getResourceAsStream(classpath);
            if(nextStream == null)
            {
                throw new SchemaManifestException(nextOntology.getVersionIRI(),
                        "Ontology was not found on the classpath: " + classpath);
            }
            managementConnection.add(nextStream, "", Rio.getParserFormatForFileName(classpath, RDFFormat.RDFXML),
                    nextOntology.getVersionIRI().toOpenRDFURI());
        }
        
        final Model currentVersions = model.filter(null, PODD.OMV_CURRENT_VERSION, null);
        for(final Statement nextOntology : currentVersions)
        {
            // Ensure that there is only one current version
            managementConnection.remove(nextOntology.getSubject(), PODD.OMV_CURRENT_VERSION, null,
                    schemaManagementGraph);
            managementConnection.add(nextOntology.getSubject(), PODD.OMV_CURRENT_VERSION, nextOntology.getObject(),
                    schemaManagementGraph);
        }
        
        return ontologyIDs;
    }
    
    /**
     * @param model
     * @param currentVersionsMap
     * @param allVersionsMap
     * @param nextSchemaOntologyUri
     */
    public static void mapAllVersions(final Model model, final ConcurrentMap<URI, URI> currentVersionsMap,
            final ConcurrentMap<URI, Set<URI>> allVersionsMap, final URI nextSchemaOntologyUri)
    {
        final Set<Value> allVersions = model.filter(nextSchemaOntologyUri, OWL.VERSIONIRI, null).objects();
        Set<URI> nextAllVersions = new LinkedHashSet<>();
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
                OntologyUtils.log.error("Version was not a URI: {} {}", nextSchemaOntologyUri, nextVersionURI);
            }
        }
        
        if(nextAllVersions.isEmpty())
        {
            OntologyUtils.log.debug("Could not find any version information for schema ontology: {}",
                    nextSchemaOntologyUri);
        }
    }
    
    /**
     * @param model
     * @param currentVersionsMap
     * @param allVersionsMap
     * @param importsMap
     * @param importOrder
     * @param nextOntologyUri
     */
    private static void mapAndSortImports(final Model model, final ConcurrentMap<URI, URI> currentVersionsMap,
            final ConcurrentMap<URI, Set<URI>> allVersionsMap, final ConcurrentMap<URI, Set<URI>> importsMap,
            final List<URI> importOrder, final URI nextOntologyUri)
    {
        final Set<Value> imports = model.filter(nextOntologyUri, OWL.IMPORTS, null).objects();
        Set<URI> nextImportsSet = new LinkedHashSet<>();
        final Set<URI> putIfAbsent = importsMap.putIfAbsent(nextOntologyUri, nextImportsSet);
        if(putIfAbsent != null)
        {
            nextImportsSet = putIfAbsent;
        }
        int maxIndex = 0;
        if(imports.isEmpty())
        {
            if(!nextImportsSet.isEmpty())
            {
                OntologyUtils.log.error("Found inconsistent imports set: {} {}", nextOntologyUri, nextImportsSet);
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
                        // Replace next import with the version
                        nextImport = currentVersionsMap.get(nextImport);
                        // Map down to the current version to ensure that we can load multiple
                        // versions simultaneously (if possible with the rest of the system)
                        nextImportsSet.add((URI)nextImport);
                    }
                    else if(currentVersionsMap.containsValue(nextImport))
                    {
                        nextImportsSet.add((URI)nextImport);
                    }
                    else
                    {
                        boolean foundAllVersion = false;
                        // Attempt to verify if the version exists
                        for(final Entry<URI, Set<URI>> nextEntry : allVersionsMap.entrySet())
                        {
                            final URI nextAllVersions = nextEntry.getKey();
                            
                            if(nextAllVersions.equals(nextImport))
                            {
                                if(nextEntry.getValue().isEmpty())
                                {
                                    nextImportsSet.add((URI)nextImport);
                                }
                                else
                                {
                                    // Randomly choose one, as the ontology does not have a current
                                    // version, but it does have some version information
                                    nextImport = nextEntry.getValue().iterator().next();
                                    nextImportsSet.add((URI)nextImport);
                                }
                                foundAllVersion = true;
                                break;
                            }
                            else if(nextEntry.getValue().contains(nextImport))
                            {
                                nextImportsSet.add((URI)nextImport);
                                foundAllVersion = true;
                                break;
                            }
                        }
                        
                        if(!foundAllVersion)
                        {
                            OntologyUtils.log.warn("Could not find import: {} imports {}", nextOntologyUri, nextImport);
                        }
                        else
                        {
                            // This should not be necessary given the sequence of calls above
                            // nextImportsSet.add((URI)nextImport);
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
        OntologyUtils.log.debug("adding import for {} at {}", nextOntologyUri, maxIndex);
        // TODO: FIXME: This will not allow for multiple versions of a single schema ontology at the
        // same time if they have any shared import versions
        importOrder.add(maxIndex, nextOntologyUri);
        
    }
    
    public static ConcurrentMap<URI, OWLOntologyID> mapVersions(final Collection<? extends OWLOntologyID> ontologies)
    {
        final ConcurrentMap<URI, OWLOntologyID> result = new ConcurrentHashMap<>();
        for(final OWLOntologyID nextOntology : ontologies)
        {
            final OWLOntologyID putIfAbsent =
                    result.putIfAbsent(nextOntology.getVersionIRI().toOpenRDFURI(), nextOntology);
            if(putIfAbsent != null)
            {
                throw new RuntimeException("Found duplicated version IRI in a set of ontologies: " + nextOntology + " "
                        + ontologies);
            }
        }
        return result;
    }
    
    /**
     * @param model
     * @param currentVersionsMap
     * @param nextSchemaOntologyUri
     * @throws SchemaManifestException
     */
    public static URI mapCurrentVersion(final Model model, final ConcurrentMap<URI, URI> currentVersionsMap,
            final URI nextSchemaOntologyUri) throws SchemaManifestException
    {
        try
        {
            final URI nextCurrentVersionURI =
                    model.filter(nextSchemaOntologyUri, PODD.OMV_CURRENT_VERSION, null).objectURI();
            
            if(nextCurrentVersionURI == null)
            {
                // OntologyUtils.log
                // .error("Did not find a current version for schema ontology: {}",
                // nextSchemaOntologyUri);
                // throw new SchemaManifestException(IRI.create(nextSchemaOntologyUri),
                // "Did not find a current version for schema ontology: " +
                // nextSchemaOntologyUri.stringValue());
            }
            else
            {
                final URI putIfAbsent = currentVersionsMap.putIfAbsent(nextSchemaOntologyUri, nextCurrentVersionURI);
                if(putIfAbsent != null)
                {
                    if(!putIfAbsent.equals(nextCurrentVersionURI))
                    {
                        OntologyUtils.log.error("Found multiple version URIs for ontology: {} old={} new={}",
                                nextSchemaOntologyUri, putIfAbsent, nextCurrentVersionURI);
                        throw new SchemaManifestException(IRI.create(nextSchemaOntologyUri),
                                "Found multiple version IRIs for ontology");
                    }
                }
            }
            
            return nextCurrentVersionURI;
        }
        catch(final ModelException e)
        {
            OntologyUtils.log.error("Could not find a single unique current version for schema ontology: {}",
                    nextSchemaOntologyUri);
            throw new SchemaManifestException(IRI.create(nextSchemaOntologyUri),
                    "Could not find a single unique current version IRI for schema ontology", e);
        }
    }
    
    /**
     * Extracts the {@link InferredOWLOntologyID} instances that are represented as RDF
     * {@link Statement}s in the given {@link Model}.
     *
     * @param input
     *            The input model containing RDF statements.
     * @return A Collection of {@link InferredOWLOntologyID} instances derived from the statements
     *         in the model.
     */
    public static List<InferredOWLOntologyID> modelToOntologyIDs(final Model input)
    {
        return OntologyUtils.modelToOntologyIDs(input, false, true);
    }
    
    /**
     * Extracts the {@link InferredOWLOntologyID} instances that are represented as RDF
     * {@link Statement}s in the given {@link Model}.
     *
     * @param input
     *            The input model containing RDF statements.
     * @param allowVersionless
     *            True if the algorithm should recognise versionless ontologies, and false to ignore
     *            them.
     * @return A Collection of {@link InferredOWLOntologyID} instances derived from the statements
     *         in the model.
     */
    public static List<InferredOWLOntologyID> modelToOntologyIDs(final Model input, final boolean allowVersionless,
            final boolean includeInferred)
    {
        final List<InferredOWLOntologyID> results = new ArrayList<InferredOWLOntologyID>();
        
        final Model typedOntologies = input.filter(null, RDF.TYPE, OWL.ONTOLOGY);
        
        for(final Statement nextTypeStatement : typedOntologies)
        {
            if(nextTypeStatement.getSubject() instanceof URI)
            {
                final Model versions = input.filter(nextTypeStatement.getSubject(), OWL.VERSIONIRI, null);
                
                if(versions.isEmpty())
                {
                    if(allowVersionless)
                    {
                        results.add(new InferredOWLOntologyID(IRI.create((URI)nextTypeStatement.getSubject()), null,
                                null));
                    }
                }
                else
                {
                    for(final Statement nextVersion : versions)
                    {
                        if(nextVersion.getObject() instanceof URI)
                        {
                            final Model inferredOntologies =
                                    input.filter((URI)nextVersion.getObject(), PODD.PODD_BASE_INFERRED_VERSION, null);
                            
                            if(!includeInferred)
                            {
                                results.add(new InferredOWLOntologyID((URI)nextTypeStatement.getSubject(),
                                        (URI)nextVersion.getObject(), null));
                            }
                            else
                            {
                                if(inferredOntologies.isEmpty())
                                {
                                    // If there were no poddBase#inferredVersion statements, backup
                                    // by
                                    // trying to infer the versions using owl:imports
                                    final Model importsOntologies =
                                            input.filter(null, OWL.IMPORTS, nextVersion.getObject());
                                    
                                    if(importsOntologies.isEmpty())
                                    {
                                        results.add(new InferredOWLOntologyID((URI)nextTypeStatement.getSubject(),
                                                (URI)nextVersion.getObject(), null));
                                    }
                                    else
                                    {
                                        for(final Statement nextImportOntology : importsOntologies)
                                        {
                                            if(nextImportOntology.getSubject() instanceof URI)
                                            {
                                                results.add(new InferredOWLOntologyID((URI)nextTypeStatement
                                                        .getSubject(), (URI)nextVersion.getObject(),
                                                        (URI)nextImportOntology.getSubject()));
                                            }
                                            else
                                            {
                                                OntologyUtils.log.error("Found a non-URI import statement: {}",
                                                        nextImportOntology);
                                            }
                                            
                                        }
                                    }
                                }
                                else
                                {
                                    for(final Statement nextInferredOntology : inferredOntologies)
                                    {
                                        if(nextInferredOntology.getObject() instanceof URI)
                                        {
                                            results.add(new InferredOWLOntologyID((URI)nextTypeStatement.getSubject(),
                                                    (URI)nextVersion.getObject(), (URI)nextInferredOntology.getObject()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return results;
    }
    
    /**
     * Serialises the given collection of {@link InferredOWLOntologyID} objects to RDF, adding the
     * {@link Statement}s to the given {@link RDFHandler}.
     * <p>
     * This method wraps the serialisation from {@link InferredOWLOntologyID#toRDF()}.
     *
     * @param input
     *            The collection of {@link InferredOWLOntologyID} objects to render to RDF.
     * @param handler
     *            The handler for handling the RDF statements.
     * @throws RDFHandlerException
     *             If there is an error while handling the statements.
     */
    public static void ontologyIDsToHandler(final Collection<InferredOWLOntologyID> input, final RDFHandler handler)
        throws RDFHandlerException
    {
        for(final InferredOWLOntologyID nextOntology : input)
        {
            for(final Statement nextStatement : nextOntology.toRDF())
            {
                handler.handleStatement(nextStatement);
            }
        }
    }
    
    public static Model ontologyIDsToModel(final Collection<InferredOWLOntologyID> input, final Model result)
    {
        return OntologyUtils.ontologyIDsToModel(input, result, true);
    }
    
    /**
     * Serialises the given collection of {@link InferredOWLOntologyID} objects to RDF, adding the
     * {@link Statement}s to the given {@link Model}, or creating a new Model if the given model is
     * null.
     * <p>
     * This method wraps the serialisation from {@link InferredOWLOntologyID#toRDF()}.
     *
     * @param input
     *            The collection of {@link InferredOWLOntologyID} objects to render to RDF.
     * @param result
     *            The Model to contain the resulting statements, or null to have one created
     *            internally
     * @param includeInferredOntologyStatements
     * @return A model containing the RDF statements about the given ontologies.
     * @throws RDFHandlerException
     *             If there is an error while handling the statements.
     */
    public static Model ontologyIDsToModel(final Collection<InferredOWLOntologyID> input, final Model result,
            final boolean includeInferredOntologyStatements)
    {
        Model results = result;
        
        if(results == null)
        {
            results = new LinkedHashModel();
        }
        
        for(final InferredOWLOntologyID nextOntology : input)
        {
            OntologyUtils.ontologyIDToRDF(nextOntology, results, includeInferredOntologyStatements);
        }
        
        return results;
    }
    
    public static Model ontologyIDToRDF(final OWLOntologyID ontology, final Model result,
            final boolean includeInferredOntologyStatements)
    {
        final ValueFactory vf = ValueFactoryImpl.getInstance();
        
        if(ontology.getOntologyIRI() != null)
        {
            result.add(vf.createStatement(ontology.getOntologyIRI().toOpenRDFURI(), RDF.TYPE, OWL.ONTOLOGY));
            if(ontology.getVersionIRI() != null)
            {
                result.add(vf.createStatement(ontology.getVersionIRI().toOpenRDFURI(), RDF.TYPE, OWL.ONTOLOGY));
                result.add(vf.createStatement(ontology.getOntologyIRI().toOpenRDFURI(), OWL.VERSIONIRI, ontology
                        .getVersionIRI().toOpenRDFURI()));
                if(includeInferredOntologyStatements && ontology instanceof InferredOWLOntologyID)
                {
                    final InferredOWLOntologyID inferredOntology = (InferredOWLOntologyID)ontology;
                    if(inferredOntology.getInferredOntologyIRI() != null)
                    {
                        result.add(vf.createStatement(inferredOntology.getInferredOntologyIRI().toOpenRDFURI(),
                                RDF.TYPE, OWL.ONTOLOGY));
                        result.add(vf.createStatement(inferredOntology.getVersionIRI().toOpenRDFURI(),
                                PODD.PODD_BASE_INFERRED_VERSION, inferredOntology.getInferredOntologyIRI()
                                        .toOpenRDFURI()));
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Orders the schema ontology imports into list that can be uploaded in order to give a good
     * chance that dependencies will be uploaded first.
     *
     * @param model
     * @param schemaOntologyUris
     * @param schemaVersionUris
     * @return An ordered list of {@link URI}s that determine a useful order for uploading schema
     *         ontologies to ensure that dependencies are available internally when needed.
     * @throws SchemaManifestException
     */
    public static List<URI> orderImports(final Model model, final Set<URI> schemaOntologyUris,
            final Set<URI> schemaVersionUris, final ConcurrentMap<URI, Set<URI>> importsMap,
            final boolean allowOntologyUriImports) throws SchemaManifestException
    {
        final List<URI> importOrder = new ArrayList<>(schemaOntologyUris.size());
        
        final ConcurrentMap<URI, URI> currentVersionsMap = new ConcurrentHashMap<>(schemaOntologyUris.size());
        final ConcurrentMap<URI, Set<URI>> allVersionsMap = new ConcurrentHashMap<>(schemaOntologyUris.size());
        
        // Find current version for each schema ontology
        for(final URI nextSchemaOntologyUri : schemaOntologyUris)
        {
            OntologyUtils.mapCurrentVersion(model, currentVersionsMap, nextSchemaOntologyUri);
        }
        
        // Find all versions for each schema ontology
        for(final URI nextSchemaOntologyUri : schemaOntologyUris)
        {
            OntologyUtils.mapAllVersions(model, currentVersionsMap, allVersionsMap, nextSchemaOntologyUri);
        }
        
        // Map the actual schema ontologies to the correct order, based on
        // current versions and all versions with the imports taken into account
        for(final URI nextVersionUri : schemaVersionUris)
        {
            OntologyUtils.mapAndSortImports(model, currentVersionsMap, allVersionsMap, importsMap, importOrder,
                    nextVersionUri);
        }
        
        if(allowOntologyUriImports)
        {
            for(final URI nextOntologyUri : schemaOntologyUris)
            {
                OntologyUtils.mapAndSortImports(model, currentVersionsMap, allVersionsMap, importsMap, importOrder,
                        nextOntologyUri);
            }
        }
        
        OntologyUtils.recursiveFollowSchemaMap(importsMap);
        
        OntologyUtils.postSort(importOrder, importsMap);
        
        OntologyUtils.log.debug("importOrder: {}", importOrder);
        return importOrder;
    }
    
    public static final void recursiveFollowSchemaMap(final ConcurrentMap<URI, Set<URI>> importsMap)
        throws SchemaManifestException
    {
        // Iterate through at least as many times as the size of the importsMap to get all
        // dependencies
        for(int i = 0; i < importsMap.size(); i++)
        {
            for(final Entry<URI, Set<URI>> nextImportsMapEntry : importsMap.entrySet())
            {
                // Avoid ConcurrentModificationException by taking a copy and replacing it
                // afterwards
                final Set<URI> nextSet = new LinkedHashSet<>(nextImportsMapEntry.getValue());
                OntologyUtils.recursiveFollowSchemaMap(importsMap, nextImportsMapEntry.getKey(), nextSet);
                importsMap.put(nextImportsMapEntry.getKey(), nextSet);
            }
        }
    }
    
    public static final void recursiveFollowSchemaMap(final ConcurrentMap<URI, Set<URI>> importsMap, final URI nextUri,
            final Set<URI> nextUriImports) throws SchemaManifestException
    {
        for(final URI nextImportedUri : importsMap.get(nextUri))
        {
            nextUriImports.add(nextImportedUri);
            if(nextImportedUri.equals(nextUri))
            {
                throw new SchemaManifestException(IRI.create(nextUri), "Ontology recursively imported itself: "
                        + nextUriImports);
            }
            OntologyUtils.recursiveFollowSchemaMap(importsMap, nextImportedUri, nextUriImports);
        }
    }
    
    private static void postSort(final List<URI> importOrder, final ConcurrentMap<URI, Set<URI>> importsMap)
    {
        Collections.sort(importOrder, new OntologyImportsComparator(importsMap));
    }
    
    /**
     * Recursively follow the imports for the given URI, based on those identified in the
     * importsMap.
     *
     * @param ontologyImports
     * @param importsMap
     * @param nextURI
     */
    public static void recursiveFollowImports(final Set<URI> ontologyImports,
            final ConcurrentMap<URI, Set<URI>> importsMap, final URI nextURI)
    {
        if(importsMap.containsKey(nextURI))
        {
            final Set<URI> nextSet = importsMap.get(nextURI);
            for(final URI nextSetUri : nextSet)
            {
                if(!ontologyImports.contains(nextSetUri))
                {
                    ontologyImports.add(nextSetUri);
                    OntologyUtils.recursiveFollowImports(ontologyImports, importsMap, nextSetUri);
                }
            }
        }
    }
    
    /**
     * Retrieves imports specified using {@link OWL#IMPORTS}, based on the given version IRIs. Also
     * checks to verify that there is an {@link OWL#VERSIONIRI} statement for each of the version
     * IRIs. <br>
     * This works with the format used by both the schema manifests and the schema management graph.
     *
     * @param model
     *            The complete schema manifest as an RDF {@link Model}.
     * @param dependentSchemaOntologies
     *            The set of schema ontologies.
     * @param importsMap
     *            The map which will be populated with the mappings from ontology version IRIs to
     *            their imports. This is necessary to determine which of the other ontology version
     *            IRIs must be loaded first in order to load a given version.
     * @return An ordered List of {@link OWLOntologyID}s which specify a total ordering which can be
     *         used to load all of the ontology versions.
     * @throws SchemaManifestException
     *             If the schema manifest is not consistent.
     */
    public static List<OWLOntologyID> schemaImports(final Model model,
            final Set<? extends OWLOntologyID> dependentSchemaOntologies, final ConcurrentMap<URI, Set<URI>> importsMap)
        throws SchemaManifestException
    {
        Objects.requireNonNull(dependentSchemaOntologies);
        Objects.requireNonNull(model);
        Objects.requireNonNull(importsMap);
        
        final Set<OWLOntologyID> results = new LinkedHashSet<OWLOntologyID>();
        
        final Set<URI> schemaOntologyUris = new LinkedHashSet<>();
        final Set<URI> schemaVersionUris = new LinkedHashSet<>();
        
        OntologyUtils.extractOntologyAndVersions(model, schemaOntologyUris, schemaVersionUris);
        
        final List<URI> orderImports =
                OntologyUtils.orderImports(model, schemaOntologyUris, schemaVersionUris, importsMap, false);
        
        final Set<URI> artifactImports = new LinkedHashSet<>();
        
        for(final URI nextOrderedImport : orderImports)
        {
            for(final OWLOntologyID nextDependentSchemaOntology : dependentSchemaOntologies)
            {
                if(nextDependentSchemaOntology.getOntologyIRI().toOpenRDFURI().equals(nextOrderedImport)
                        || (nextDependentSchemaOntology.getVersionIRI() != null && nextDependentSchemaOntology
                                .getVersionIRI().toOpenRDFURI().equals(nextOrderedImport)))
                {
                    artifactImports.add(nextDependentSchemaOntology.getVersionIRI().toOpenRDFURI());
                    // Not tolerant for artifacts... Imports must be directed to version IRIs
                    // recursiveFollowImports(artifactImports, importsMap,
                    // artifactID.getOntologyIRI().toOpenRDFURI());
                    
                    OntologyUtils.recursiveFollowImports(artifactImports, importsMap, nextDependentSchemaOntology
                            .getVersionIRI().toOpenRDFURI());
                }
            }
        }
        
        OntologyUtils.postSort(orderImports, importsMap);
        
        final List<InferredOWLOntologyID> ontologyIDs = OntologyUtils.modelToOntologyIDs(model, false, true);
        
        final List<OWLOntologyID> finalOrderImports =
                OntologyUtils.finalOrderImports(results, ontologyIDs, orderImports, artifactImports, importsMap);
        
        return finalOrderImports;
    }
    
    /**
     * Extracts the {@link InferredOWLOntologyID} instances that are represented as RDF
     * {@link Statement}s in the given {@link String}.
     *
     * @param string
     *            The input string containing RDF statements.
     * @param format
     *            The format of RDF statements in the string
     * @return A Collection of {@link InferredOWLOntologyID} instances derived from the statements
     *         in the string.
     * @throws OpenRDFException
     * @throws IOException
     */
    public static Collection<InferredOWLOntologyID> stringToOntologyID(final String string, final RDFFormat format)
        throws OpenRDFException, IOException
    {
        final Model model = Rio.parse(new StringReader(string), "", format);
        
        return OntologyUtils.modelToOntologyIDs(model);
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
    public static void validateSchemaManifestImports(final Model manifestModel, final Set<URI> schemaOntologyUris,
            final Set<URI> schemaVersionUris) throws IOException, RDFParseException, UnsupportedRDFormatException,
        SchemaManifestException
    {
        for(final URI nextOntologyUri : schemaOntologyUris)
        {
            if(manifestModel.contains(nextOntologyUri, OWL.IMPORTS, null))
            {
                OntologyUtils.log.error("Schema ontology in manifest has owl:imports coming directly from it: {}",
                        nextOntologyUri);
                throw new SchemaManifestException(IRI.create(nextOntologyUri), String.format(
                        "Schema ontology in manifest has owl:imports coming directly from it {}", nextOntologyUri));
            }
            
            final Model currentVersion = manifestModel.filter(nextOntologyUri, PODD.OMV_CURRENT_VERSION, null);
            
            if(currentVersion.isEmpty())
            {
                OntologyUtils.log.error("Missing OMV current version for schema ontology: {}", nextOntologyUri);
                throw new SchemaManifestException(IRI.create(nextOntologyUri), String.format(
                        "Missing OMV current version for schema ontology : {}", nextOntologyUri));
            }
            
            if(currentVersion.size() > 1)
            {
                OntologyUtils.log.error("Multiple OMV current versions for schema ontology: {}", nextOntologyUri);
                throw new SchemaManifestException(IRI.create(nextOntologyUri), String.format(
                        "Multiple OMV current versions for schema ontology : {}", nextOntologyUri));
            }
        }
        
        for(final URI nextVersionUri : schemaVersionUris)
        {
            final Set<Value> importsInManifest = manifestModel.filter(nextVersionUri, OWL.IMPORTS, null).objects();
            
            final String classpathLocation =
                    manifestModel.filter(nextVersionUri, PODD.PODD_SCHEMA_CLASSPATH, null).objectLiteral()
                            .stringValue();
            final RDFFormat format = Rio.getParserFormatForFileName(classpathLocation, RDFFormat.RDFXML);
            try (final InputStream input = OntologyUtils.class.getResourceAsStream(classpathLocation);)
            {
                if(input == null)
                {
                    throw new SchemaManifestException(IRI.create(nextVersionUri),
                            String.format("Could not find schema at designated classpath location: {} ",
                                    nextVersionUri.stringValue()));
                }
                final Model model = Rio.parse(input, "", format);
                final Set<Value> importsInOwlFile = model.filter(null, OWL.IMPORTS, null).objects();
                
                if(!importsInManifest.equals(importsInOwlFile))
                {
                    OntologyUtils.log.warn("Comparing: \n Manifest: {} \n Owl:      {}", importsInManifest,
                            importsInOwlFile);
                    throw new SchemaManifestException(IRI.create(nextVersionUri), String.format(
                            "Schema manifest imports not consistent with actual imports : %s", nextVersionUri));
                }
            }
        }
    }
    
    private OntologyUtils()
    {
    }
    
    public static boolean ontologyVersionsMatch(final Set<? extends OWLOntologyID> set1,
            final Set<? extends OWLOntologyID> set2)
    {
        if(set2.size() == set1.size())
        {
            for(OWLOntologyID nextSchema1 : set1)
            {
                boolean foundMatch = false;
                if(nextSchema1 instanceof InferredOWLOntologyID)
                {
                    nextSchema1 = ((InferredOWLOntologyID)nextSchema1).getBaseOWLOntologyID();
                }
                for(OWLOntologyID nextSchema2 : set2)
                {
                    if(nextSchema2 instanceof InferredOWLOntologyID)
                    {
                        nextSchema2 = ((InferredOWLOntologyID)nextSchema2).getBaseOWLOntologyID();
                    }
                    if(nextSchema1.equals(nextSchema2))
                    {
                        foundMatch = true;
                        continue;
                    }
                }
                if(!foundMatch)
                {
                    return false;
                }
            }
        }
        return false;
    }
    
    public static Set<OWLOntologyID> mapFromVersions(final Set<URI> nextMinimalImportsSet,
            final List<OWLOntologyID> nextImportOrder)
    {
        final Set<OWLOntologyID> result = new LinkedHashSet<>();
        for(final OWLOntologyID nextOrderedCandidate : nextImportOrder)
        {
            for(final URI nextMinimalURI : nextMinimalImportsSet)
            {
                if(nextMinimalURI.equals(nextOrderedCandidate.getVersionIRI().toOpenRDFURI()))
                {
                    result.add(nextOrderedCandidate);
                }
            }
        }
        return result;
    }
}
