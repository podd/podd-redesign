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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.util.Namespaces;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clarkparsia.owlapi.explanation.GlassBoxExplanation;
import com.github.podd.api.DanglingObjectPolicy;
import com.github.podd.api.DataReferenceVerificationPolicy;
import com.github.podd.api.MetadataPolicy;
import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.UpdatePolicy;
import com.github.podd.api.data.DataReference;
import com.github.podd.api.data.DataReferenceManager;
import com.github.podd.api.data.PoddDataRepositoryManager;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlReference;
import com.github.podd.exception.ArtifactModifyException;
import com.github.podd.exception.DataReferenceVerificationException;
import com.github.podd.exception.DeleteArtifactException;
import com.github.podd.exception.DisconnectedObjectException;
import com.github.podd.exception.DuplicateArtifactIRIException;
import com.github.podd.exception.EmptyOntologyException;
import com.github.podd.exception.InconsistentOntologyException;
import com.github.podd.exception.OntologyNotInProfileException;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.PoddRuntimeException;
import com.github.podd.exception.PublishArtifactException;
import com.github.podd.exception.PublishedArtifactModifyException;
import com.github.podd.exception.PurlProcessorNotHandledException;
import com.github.podd.exception.RepositoryNotFoundException;
import com.github.podd.exception.SchemaManifestException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedArtifactVersionException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.RdfUtility;

/**
 * Implementation of the PODD Artifact Manager API, to manage the lifecycle for PODD Artifacts.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class PoddArtifactManagerImpl implements PoddArtifactManager
{
    static
    {
        GlassBoxExplanation.setup();
    }
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private DataReferenceManager dataReferenceManager;
    private PoddDataRepositoryManager dataRepositoryManager;
    private PoddOWLManager owlManager;
    private PoddPurlManager purlManager;
    private PoddSchemaManager schemaManager;
    private PoddRepositoryManager repositoryManager;
    
    private PoddSesameManager sesameManager;
    
    /**
     *
     */
    public PoddArtifactManagerImpl()
    {
    }
    
    @Override
    public InferredOWLOntologyID attachDataReference(final InferredOWLOntologyID artifactId, final URI objectUri,
            final DataReference dataReference, final DataReferenceVerificationPolicy dataReferenceVerificationPolicy)
        throws OpenRDFException, PoddException, IOException, OWLException
    {
        return this.attachDataReferences(artifactId, dataReference.toRDF(), dataReferenceVerificationPolicy);
    }
    
    @Override
    public InferredOWLOntologyID attachDataReferences(final InferredOWLOntologyID ontologyId, final Model model,
            final DataReferenceVerificationPolicy dataReferenceVerificationPolicy) throws OpenRDFException,
        IOException, OWLException, PoddException
    {
        model.removeAll(model.filter(null, PODD.PODD_BASE_INFERRED_VERSION, null));
        
        final Set<Resource> fileReferences =
                model.filter(null, RDF.TYPE, PODD.PODD_BASE_DATA_REFERENCE_TYPE).subjects();
        final Collection<URI> fileReferenceObjects = new ArrayList<URI>(fileReferences.size());
        for(final Resource nextFileReference : fileReferences)
        {
            if(nextFileReference instanceof URI)
            {
                fileReferenceObjects.add((URI)nextFileReference);
            }
            else
            {
                this.log.warn("Will not be updating file reference for blank node reference, will instead be creating a new file reference for it.");
            }
        }
        
        final Model exportArtifact = this.exportArtifact(ontologyId, false);
        
        exportArtifact.addAll(model);
        
        final Model resultModel =
                this.updateArtifact(ontologyId.getOntologyIRI().toOpenRDFURI(), ontologyId.getVersionIRI()
                        .toOpenRDFURI(), fileReferenceObjects, model, UpdatePolicy.MERGE_WITH_EXISTING,
                        DanglingObjectPolicy.REPORT, dataReferenceVerificationPolicy);
        return OntologyUtils.modelToOntologyIDs(resultModel, true, false).get(0);
    }
    
    @Override
    public boolean deleteArtifact(final InferredOWLOntologyID artifactId) throws PoddException,
        UnsupportedRDFormatException, OpenRDFException, IOException
    {
        if(artifactId.getOntologyIRI() == null)
        {
            throw new PoddRuntimeException("Ontology IRI cannot be null");
        }
        
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        
        List<InferredOWLOntologyID> requestedArtifactIds = null;
        final Set<? extends OWLOntologyID> schemaImports = this.getSchemaImports(artifactId);
        
        try
        {
            permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
            permanentConnection.begin();
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            managementConnection.begin();
            
            if(this.getSesameManager().isPublished(artifactId, managementConnection,
                    this.getRepositoryManager().getArtifactManagementGraph()))
            {
                throw new DeleteArtifactException("Published Artifacts cannot be deleted", artifactId);
            }
            
            requestedArtifactIds =
                    this.getSesameManager().getAllOntologyVersions(artifactId.getOntologyIRI(), managementConnection,
                            this.getRepositoryManager().getArtifactManagementGraph());
            
            if(artifactId.getVersionIRI() != null)
            {
                final IRI requestedVersionIRI = artifactId.getVersionIRI();
                
                for(final InferredOWLOntologyID nextVersion : new ArrayList<InferredOWLOntologyID>(requestedArtifactIds))
                {
                    if(requestedVersionIRI.equals(nextVersion.getVersionIRI()))
                    {
                        requestedArtifactIds = Arrays.asList(nextVersion);
                    }
                }
            }
            
            this.getSesameManager().deleteOntologies(requestedArtifactIds, permanentConnection, managementConnection,
                    this.getRepositoryManager().getArtifactManagementGraph());
            permanentConnection.commit();
            managementConnection.commit();
            
            return !requestedArtifactIds.isEmpty();
        }
        catch(final Throwable e)
        {
            try
            {
                if(managementConnection != null && managementConnection.isActive())
                {
                    managementConnection.rollback();
                }
            }
            catch(final RepositoryException e1)
            {
                this.log.error("Found error rolling back repository connection", e1);
            }
            finally
            {
                try
                {
                    if(permanentConnection != null && permanentConnection.isActive())
                    {
                        permanentConnection.rollback();
                    }
                }
                catch(final RepositoryException e1)
                {
                    this.log.error("Found error rolling back repository connection", e1);
                }
            }
            
            throw new DeleteArtifactException("Exception occurred while deleting artifact", e, artifactId);
        }
        finally
        {
            try
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
            catch(final RepositoryException e)
            {
                this.log.error("Found error closing repository connection", e);
            }
            finally
            {
                try
                {
                    if(permanentConnection != null)
                    {
                        permanentConnection.close();
                    }
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Found error closing repository connection", e);
                }
                finally
                {
                    // - ensure deleted ontologies are removed from the
                    // OWLOntologyManager's cache
                    if(requestedArtifactIds != null)
                    {
                        for(final InferredOWLOntologyID deletedOntologyId : requestedArtifactIds)
                        {
                            try
                            {
                                this.getOWLManager().removeCache(deletedOntologyId.getBaseOWLOntologyID(),
                                        schemaImports);
                            }
                            catch(final OWLException e)
                            {
                                this.log.error("Found error clearing cache", e);
                            }
                            finally
                            {
                                try
                                {
                                    this.getOWLManager().removeCache(deletedOntologyId.getInferredOWLOntologyID(),
                                            schemaImports);
                                }
                                catch(final OWLException e)
                                {
                                    this.log.error("Found error clearing cache", e);
                                }
                            }
                        }
                    }
                }
            }
        }
        
    }
    
    @Override
    public InferredOWLOntologyID deleteObject(final URI artifactUri, final URI versionUri, final URI objectUri,
            final boolean cascade) throws PoddException, OpenRDFException, IOException, OWLException
    {
        // check if the specified artifact URI refers to a managed artifact
        InferredOWLOntologyID artifactID = null;
        try
        {
            artifactID = this.getArtifact(IRI.create(artifactUri));
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            this.log.error("This artifact is unmanaged. [{}]", artifactUri);
            throw e;
        }
        
        if(this.isPublished(artifactID))
        {
            throw new PublishedArtifactModifyException("Attempting to modify a Published Artifact", artifactID);
        }
        
        this.log.debug("deleteObject ({}) from artifact {} with cascade={}", objectUri, artifactUri, cascade);
        
        final URI objectToDelete = objectUri;
        
        final Collection<URI> objectsToUpdate = new ArrayList<URI>();
        objectsToUpdate.add(objectToDelete);
        final Model fragments = new LinkedHashModel();
        final Model artifactModel = this.exportArtifact(artifactID, false);
        
        // - find the objectToDelete's parent and remove parent-child link
        final Model parentDetails = this.getParentDetails(artifactID, objectToDelete);
        if(parentDetails.subjects().size() != 1)
        {
            this.log.error("Object {} cannot be deleted. (No parent) {} {}", objectUri, artifactUri, parentDetails);
            throw new ArtifactModifyException("Object cannot be deleted. (No parent)", artifactID, objectToDelete);
        }
        final Resource parent = parentDetails.subjects().iterator().next();
        fragments.addAll(artifactModel.filter(parent, null, null));
        fragments.remove(parent, null, objectToDelete);
        objectsToUpdate.add((URI)parent);
        
        // - remove any refersToLinks
        final Model referenceLinks = this.getReferenceLinks(artifactID, objectToDelete);
        final Set<Resource> referrers = referenceLinks.subjects();
        for(final Resource referrer : referrers)
        {
            final Model referrerStatements = artifactModel.filter(referrer, null, null);
            referrerStatements.remove(referrer, null, objectToDelete);
            
            fragments.addAll(referrerStatements);
            objectsToUpdate.add((URI)referrer);
        }
        
        DanglingObjectPolicy danglingObjectPolicy = DanglingObjectPolicy.REPORT;
        if(cascade)
        {
            danglingObjectPolicy = DanglingObjectPolicy.FORCE_CLEAN;
        }
        
        this.updateArtifact(artifactID.getOntologyIRI().toOpenRDFURI(), artifactID.getVersionIRI().toOpenRDFURI(),
                objectsToUpdate, fragments, UpdatePolicy.REPLACE_EXISTING, danglingObjectPolicy,
                DataReferenceVerificationPolicy.DO_NOT_VERIFY);
        
        return this.getArtifact(artifactID.getOntologyIRI());
    }
    
    @Override
    public Model exportArtifact(final InferredOWLOntologyID ontologyId, final boolean includeInferred)
        throws OpenRDFException, PoddException, IOException
    {
        if(ontologyId.getOntologyIRI() == null || ontologyId.getVersionIRI() == null)
        {
            throw new PoddRuntimeException("Ontology IRI and Version IRI cannot be null");
        }
        
        if(includeInferred && ontologyId.getInferredOntologyIRI() == null)
        {
            throw new PoddRuntimeException("Inferred Ontology IRI cannot be null");
        }
        
        List<URI> contexts;
        
        if(includeInferred)
        {
            contexts =
                    Arrays.asList(ontologyId.getVersionIRI().toOpenRDFURI(), ontologyId.getInferredOntologyIRI()
                            .toOpenRDFURI());
        }
        else
        {
            contexts = Arrays.asList(ontologyId.getVersionIRI().toOpenRDFURI());
        }
        
        RepositoryConnection conn = null;
        
        try
        {
            final Set<? extends OWLOntologyID> schemaImports = this.getSchemaImports(ontologyId);
            conn = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
            
            return this.exportArtifactInternal(includeInferred, conn, contexts.toArray(new Resource[] {}));
        }
        finally
        {
            if(conn != null)
            {
                conn.close();
            }
        }
    }
    
    private Model exportArtifactInternal(final boolean includeInferred, final RepositoryConnection permanentConnection,
            final Resource... contexts) throws OpenRDFException, PoddException, IOException
    {
        final Model model = new LinkedHashModel();
        permanentConnection
                .exportStatements(null, null, null, includeInferred, new StatementCollector(model), contexts);
        return model;
    }
    
    @Override
    public void exportArtifact(final InferredOWLOntologyID ontologyId, final OutputStream outputStream,
            final RDFFormat format, final boolean includeInferred) throws OpenRDFException, PoddException, IOException
    {
        final Model model = this.exportArtifact(ontologyId, includeInferred);
        Rio.write(model, outputStream, format);
    }
    
    @Override
    public void exportObjectMetadata(final URI objectType, final OutputStream outputStream, final RDFFormat format,
            final boolean includeDoNotDisplayProperties, final MetadataPolicy containsPropertyPolicy,
            final InferredOWLOntologyID artifactID) throws OpenRDFException, PoddException, IOException
    {
        
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        
        try
        {
            Set<? extends OWLOntologyID> schemaImports;
            
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            final URI[] contexts =
                    this.sesameManager.versionAndInferredAndSchemaContexts(artifactID, managementConnection,
                            this.repositoryManager.getSchemaManagementGraph(),
                            this.repositoryManager.getArtifactManagementGraph());
            if(artifactID != null)
            {
                schemaImports = this.getSchemaImports(artifactID);
                permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
            }
            else
            {
                // If they don't have an artifact yet, we return the set of current schema
                // ontologies
                schemaImports = this.getSchemaManager().getCurrentSchemaOntologies();
                permanentConnection = managementConnection;
            }
            
            Model model;
            if(containsPropertyPolicy == MetadataPolicy.ONLY_CONTAINS)
            {
                model = this.sesameManager.getObjectTypeContainsMetadata(objectType, permanentConnection, contexts);
            }
            else if(containsPropertyPolicy == MetadataPolicy.EXCLUDE_CONTAINS)
            {
                model =
                        this.sesameManager.getObjectTypeMetadata(objectType, includeDoNotDisplayProperties,
                                containsPropertyPolicy, permanentConnection, contexts);
            }
            else if(containsPropertyPolicy == MetadataPolicy.INCLUDE_ALL)
            {
                model =
                        this.sesameManager.getObjectTypeMetadata(objectType, includeDoNotDisplayProperties,
                                containsPropertyPolicy, permanentConnection, contexts);
            }
            else
            {
                // Will need to implement the policy separately
                throw new PoddRuntimeException("Did not recognise metadata policy: " + containsPropertyPolicy);
            }
            
            Rio.write(model, outputStream, format);
        }
        finally
        {
            try
            {
                if(managementConnection != null && managementConnection.isOpen())
                {
                    managementConnection.close();
                }
            }
            finally
            {
                if(permanentConnection != null && permanentConnection.isOpen())
                {
                    permanentConnection.close();
                }
            }
        }
    }
    
    @Override
    public Model fillMissingData(final InferredOWLOntologyID ontologyID, final Model inputModel)
        throws OpenRDFException, UnmanagedSchemaIRIException, SchemaManifestException, UnsupportedRDFormatException,
        IOException, UnmanagedArtifactIRIException, UnmanagedArtifactVersionException, RepositoryNotFoundException
    {
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        try
        {
            final Set<? extends OWLOntologyID> schemaImports = this.getSchemaImports(ontologyID);
            if(ontologyID == null)
            {
                permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports, true);
            }
            else
            {
                permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
            }
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            final URI[] contexts =
                    this.getSesameManager().versionAndInferredAndSchemaContexts(ontologyID, managementConnection,
                            this.getRepositoryManager().getSchemaManagementGraph(),
                            this.getRepositoryManager().getArtifactManagementGraph());
            
            return this.getSesameManager().fillMissingLabels(inputModel, permanentConnection, contexts);
        }
        finally
        {
            try
            {
                if(permanentConnection != null)
                {
                    permanentConnection.close();
                }
            }
            finally
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
        }
    }
    
    @Override
    public InferredOWLOntologyID getArtifact(final IRI artifactIRI) throws UnmanagedArtifactIRIException,
        UnmanagedSchemaIRIException
    {
        try
        {
            return this.getArtifact(artifactIRI, null);
        }
        catch(final UnmanagedArtifactVersionException e)
        {
            this.log.error("Null artifact version not recognised, this should not happen");
            return null;
        }
    }
    
    @Override
    public InferredOWLOntologyID getArtifact(final IRI artifactIRI, final IRI versionIRI)
        throws UnmanagedArtifactIRIException, UnmanagedArtifactVersionException, UnmanagedSchemaIRIException
    {
        RepositoryConnection managementConnection = null;
        try
        {
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            return this.getArtifactInternal(artifactIRI, versionIRI, managementConnection);
        }
        catch(final OpenRDFException e)
        {
            throw new UnmanagedArtifactIRIException(artifactIRI, e);
        }
        finally
        {
            if(managementConnection != null)
            {
                try
                {
                    managementConnection.close();
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Failed to close repository connection", e);
                }
            }
        }
    }
    
    private InferredOWLOntologyID getArtifactInternal(final IRI artifactIRI, final IRI versionIRI,
            final RepositoryConnection managementConnection) throws UnmanagedArtifactIRIException,
        UnmanagedArtifactVersionException
    {
        try
        {
            InferredOWLOntologyID result = null;
            
            if(versionIRI != null)
            {
                result =
                        this.getSesameManager().getOntologyVersion(versionIRI, managementConnection,
                                this.getRepositoryManager().getArtifactManagementGraph());
            }
            
            if(result == null)
            {
                result =
                        this.getSesameManager().getCurrentArtifactVersion(artifactIRI, managementConnection,
                                this.getRepositoryManager().getArtifactManagementGraph());
            }
            
            if(result != null)
            {
                // If the result that was returned contained a different
                // artifact IRI then throw an
                // exception early instead of returning inconsistent results
                if(versionIRI != null && !result.getVersionIRI().equals(versionIRI))
                {
                    throw new UnmanagedArtifactVersionException(artifactIRI, result.getVersionIRI(), versionIRI,
                            "Artifact IRI and Version IRI combination did not match");
                }
            }
            
            return result;
        }
        catch(final OpenRDFException e)
        {
            throw new UnmanagedArtifactIRIException(artifactIRI, e);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * Wraps PoddSesameManager.getChildObjects()
     * 
     * @see com.github.podd.api.PoddArtifactManager#getChildObjects()
     */
    @Override
    public Set<URI> getChildObjects(final InferredOWLOntologyID ontologyID, final URI objectUri)
        throws OpenRDFException, UnmanagedSchemaIRIException, SchemaManifestException, UnsupportedRDFormatException,
        IOException, UnmanagedArtifactIRIException, UnmanagedArtifactVersionException, RepositoryNotFoundException
    {
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        try
        {
            final Set<? extends OWLOntologyID> schemaImports = this.getSchemaImports(ontologyID);
            permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            
            final URI[] contexts =
                    this.getSesameManager().versionAndInferredAndSchemaContexts(ontologyID, managementConnection,
                            this.getRepositoryManager().getSchemaManagementGraph(),
                            this.getRepositoryManager().getArtifactManagementGraph());
            
            return this.getSesameManager().getChildObjects(objectUri, permanentConnection, contexts);
        }
        finally
        {
            try
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
            finally
            {
                if(permanentConnection != null)
                {
                    permanentConnection.close();
                }
            }
        }
    }
    
    @Override
    public Set<URI> getDirectSubClassOf(final URI concept, final InferredOWLOntologyID ontologyID)
        throws OpenRDFException, UnmanagedSchemaIRIException, SchemaManifestException, UnsupportedRDFormatException,
        IOException, UnmanagedArtifactIRIException, UnmanagedArtifactVersionException, RepositoryNotFoundException
    {
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        try
        {
            final Set<? extends OWLOntologyID> schemaImports = this.getSchemaImports(ontologyID);
            permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            
            final URI[] contexts =
                    this.getSesameManager().versionAndInferredAndSchemaContexts(ontologyID, managementConnection,
                            this.getRepositoryManager().getSchemaManagementGraph(),
                            this.getRepositoryManager().getArtifactManagementGraph());
            
            return this.getSesameManager().getDirectSubClassOf(concept, permanentConnection, contexts);
        }
        finally
        {
            try
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
            finally
            {
                if(permanentConnection != null)
                {
                    permanentConnection.close();
                }
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * Wraps PoddSesameManager.getEventsTopConcepts()
     */
    @Override
    public Set<URI> getEventsTopConcepts(final InferredOWLOntologyID ontologyID) throws OpenRDFException,
        UnmanagedSchemaIRIException, SchemaManifestException, UnsupportedRDFormatException, IOException,
        UnmanagedArtifactIRIException, UnmanagedArtifactVersionException, RepositoryNotFoundException
    {
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        try
        {
            final Set<? extends OWLOntologyID> schemaImports = this.getSchemaImports(ontologyID);
            permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            
            final URI[] contexts =
                    this.getSesameManager().versionAndInferredAndSchemaContexts(ontologyID, managementConnection,
                            this.getRepositoryManager().getSchemaManagementGraph(),
                            this.getRepositoryManager().getArtifactManagementGraph());
            
            return this.getSesameManager().getEventsTopConcepts(permanentConnection, contexts);
        }
        finally
        {
            try
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
            finally
            {
                if(permanentConnection != null)
                {
                    permanentConnection.close();
                }
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#getFileReferenceManager()
     */
    @Override
    public DataReferenceManager getDataReferenceManager()
    {
        return this.dataReferenceManager;
    }
    
    @Override
    public Set<DataReference> getFileReferences(final InferredOWLOntologyID artifactId)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Set<DataReference> getFileReferences(final InferredOWLOntologyID artifactId, final String alias)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Set<DataReference> getFileReferences(final InferredOWLOntologyID artifactId, final URI objectUri)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#getFileRepositoryManager()
     */
    @Override
    public PoddDataRepositoryManager getFileRepositoryManager()
    {
        return this.dataRepositoryManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * Wraps PoddSesameManager.getObjectDetailsForDisplay()
     * 
     * @see com.github.podd.api.PoddArtifactManager#getObjectDetailsForDisplay()
     */
    @Override
    public Model getObjectDetailsForDisplay(final InferredOWLOntologyID ontologyID, final URI objectUri)
        throws OpenRDFException, UnmanagedSchemaIRIException, SchemaManifestException, UnsupportedRDFormatException,
        IOException, UnmanagedArtifactIRIException, UnmanagedArtifactVersionException, RepositoryNotFoundException
    {
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        try
        {
            final Set<? extends OWLOntologyID> schemaImports = this.getSchemaImports(ontologyID);
            permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            return this.getSesameManager().getObjectDetailsForDisplay(ontologyID, objectUri, managementConnection,
                    permanentConnection, this.getRepositoryManager().getSchemaManagementGraph(),
                    this.getRepositoryManager().getArtifactManagementGraph());
        }
        finally
        {
            try
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
            finally
            {
                if(permanentConnection != null)
                {
                    permanentConnection.close();
                }
            }
        }
    }
    
    @Override
    public PoddObjectLabel getObjectLabel(final InferredOWLOntologyID ontologyID, final URI objectUri)
        throws OpenRDFException, UnmanagedSchemaIRIException, SchemaManifestException, UnsupportedRDFormatException,
        IOException, UnmanagedArtifactIRIException, UnmanagedArtifactVersionException, RepositoryNotFoundException
    {
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        try
        {
            final Set<? extends OWLOntologyID> schemaImports = this.getSchemaImports(ontologyID);
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
            
            return this.getSesameManager().getObjectLabel(ontologyID, objectUri, managementConnection,
                    permanentConnection, this.getRepositoryManager().getSchemaManagementGraph(),
                    this.getRepositoryManager().getArtifactManagementGraph());
        }
        finally
        {
            try
            {
                if(permanentConnection != null)
                {
                    permanentConnection.close();
                }
            }
            finally
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#getObjectTypes(com.github.podd .utils.
     * InferredOWLOntologyID, org.openrdf.model.URI)
     */
    @Override
    public List<PoddObjectLabel> getObjectTypes(final InferredOWLOntologyID artifactId, final URI objectUri)
        throws OpenRDFException, UnmanagedSchemaIRIException, SchemaManifestException, UnsupportedRDFormatException,
        IOException, UnmanagedArtifactIRIException, UnmanagedArtifactVersionException, RepositoryNotFoundException
    {
        final List<PoddObjectLabel> results = new ArrayList<PoddObjectLabel>();
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        
        try
        {
            final Set<? extends OWLOntologyID> schemaImports = this.getSchemaImports(artifactId);
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
            
            final List<URI> typesList =
                    this.getSesameManager().getObjectTypes(artifactId, objectUri, managementConnection,
                            permanentConnection, this.getRepositoryManager().getSchemaManagementGraph(),
                            this.getRepositoryManager().getArtifactManagementGraph());
            for(final URI objectType : typesList)
            {
                results.add(this.getSesameManager().getObjectLabel(artifactId, objectType, managementConnection,
                        permanentConnection, this.getRepositoryManager().getSchemaManagementGraph(),
                        this.getRepositoryManager().getArtifactManagementGraph()));
            }
        }
        finally
        {
            try
            {
                if(permanentConnection != null)
                {
                    permanentConnection.close();
                }
            }
            finally
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
        }
        
        return results;
    }
    
    /*
     * (non-Javadoc)
     * 
     * Wraps PoddSesameManager.getOrderedProperties()
     * 
     * @see com.github.podd.api.PoddArtifactManager#getOrderedProperties()
     */
    @Override
    public List<URI> getOrderedProperties(final InferredOWLOntologyID ontologyID, final URI objectUri,
            final boolean excludeContainsProperties) throws OpenRDFException, UnmanagedSchemaIRIException,
        SchemaManifestException, UnsupportedRDFormatException, IOException, UnmanagedArtifactIRIException,
        UnmanagedArtifactVersionException, RepositoryNotFoundException
    {
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        try
        {
            final Set<? extends OWLOntologyID> schemaImports = this.getSchemaImports(ontologyID);
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
            
            final URI[] contexts =
                    this.getSesameManager().versionAndSchemaContexts(ontologyID, managementConnection,
                            this.getRepositoryManager().getSchemaManagementGraph(),
                            this.getRepositoryManager().getArtifactManagementGraph());
            
            return this.getSesameManager().getWeightedProperties(objectUri, excludeContainsProperties,
                    permanentConnection, contexts);
        }
        finally
        {
            try
            {
                if(permanentConnection != null)
                {
                    permanentConnection.close();
                }
            }
            finally
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
        }
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
     * @see com.github.podd.api.PoddArtifactManager#getParentDetails(com.github.podd .utils.
     * InferredOWLOntologyID, org.openrdf.model.URI)
     */
    @Override
    public Model getParentDetails(final InferredOWLOntologyID ontologyID, final URI objectUri) throws OpenRDFException,
        UnmanagedSchemaIRIException, SchemaManifestException, UnsupportedRDFormatException, IOException,
        UnmanagedArtifactIRIException, UnmanagedArtifactVersionException, RepositoryNotFoundException
    {
        final Repository permanentRepository = null;
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        try
        {
            final Set<? extends OWLOntologyID> schemaImports = this.getSchemaImports(ontologyID);
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            final URI[] contexts =
                    this.getSesameManager().versionAndInferredAndSchemaContexts(ontologyID, managementConnection,
                            this.getRepositoryManager().getSchemaManagementGraph(),
                            this.getRepositoryManager().getArtifactManagementGraph());
            
            permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
            
            return this.getSesameManager().getParentDetails(objectUri, permanentConnection, contexts);
        }
        finally
        {
            try
            {
                if(permanentConnection != null)
                {
                    permanentConnection.close();
                }
            }
            finally
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
        }
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
    
    private Model getReferenceLinks(final InferredOWLOntologyID ontologyID, final URI objectUri)
        throws OpenRDFException, UnmanagedSchemaIRIException, SchemaManifestException, UnsupportedRDFormatException,
        IOException, UnmanagedArtifactIRIException, UnmanagedArtifactVersionException, RepositoryNotFoundException
    {
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        try
        {
            final Set<? extends OWLOntologyID> schemaImports = this.getSchemaImports(ontologyID);
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
            final URI[] contexts =
                    this.getSesameManager().versionAndSchemaContexts(ontologyID, managementConnection,
                            this.getRepositoryManager().getSchemaManagementGraph(),
                            this.getRepositoryManager().getArtifactManagementGraph());
            
            return this.getSesameManager().getReferringObjectDetails(objectUri, permanentConnection, contexts);
        }
        finally
        {
            try
            {
                if(permanentConnection != null)
                {
                    permanentConnection.close();
                }
            }
            finally
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
        }
    }
    
    @Override
    public PoddRepositoryManager getRepositoryManager()
    {
        return this.repositoryManager;
    }
    
    @Override
    public Set<? extends OWLOntologyID> getSchemaImports(final InferredOWLOntologyID artifactID)
        throws OpenRDFException, UnmanagedSchemaIRIException, SchemaManifestException, UnsupportedRDFormatException,
        IOException, UnmanagedArtifactIRIException, UnmanagedArtifactVersionException
    {
        Objects.requireNonNull(
                artifactID,
                "Cannot get schema imports without an artifact reference. May need to try PoddSchemaManager.getCurrentSchemaOntologies instead.");
        
        RepositoryConnection managementConnection = null;
        
        try
        {
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            
            final InferredOWLOntologyID inferredOWLOntologyID =
                    this.getArtifactInternal(artifactID.getOntologyIRI(), artifactID.getVersionIRI(),
                            managementConnection);
            
            final Model model = new LinkedHashModel();
            // NOTE: In future when we support imports to exist between artifacts, will be utilising
            // the entire artifact management graph here, so exporting it all
            managementConnection.export(new StatementCollector(model), this.getRepositoryManager()
                    .getArtifactManagementGraph());
            managementConnection.export(new StatementCollector(model), this.getRepositoryManager()
                    .getSchemaManagementGraph());
            
            final Set<OWLOntologyID> result =
                    new LinkedHashSet<>(OntologyUtils.artifactImports(inferredOWLOntologyID, model));
            
            this.log.debug("Returning from getSchemaImports: {} {}", artifactID, result);
            
            return result;
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
    public PoddSchemaManager getSchemaManager()
    {
        return this.schemaManager;
    }
    
    @Override
    public PoddSesameManager getSesameManager()
    {
        return this.sesameManager;
    }
    
    @Override
    public List<PoddObjectLabel> getTopObjectLabels(final List<InferredOWLOntologyID> artifacts)
        throws OpenRDFException, UnmanagedSchemaIRIException, SchemaManifestException, UnsupportedRDFormatException,
        IOException, UnmanagedArtifactIRIException, UnmanagedArtifactVersionException, RepositoryNotFoundException
    {
        final List<PoddObjectLabel> results = new ArrayList<PoddObjectLabel>();
        final ConcurrentMap<Set<? extends OWLOntologyID>, RepositoryConnection> cache =
                new ConcurrentHashMap<Set<? extends OWLOntologyID>, RepositoryConnection>();
        
        RepositoryConnection managementConnection = null;
        
        try
        {
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            for(final InferredOWLOntologyID artifactId : artifacts)
            {
                final Set<? extends OWLOntologyID> schemaImports = this.getSchemaImports(artifactId);
                RepositoryConnection permanentConnection = cache.get(schemaImports);
                if(permanentConnection == null)
                {
                    RepositoryConnection nextConnection =
                            this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
                    final RepositoryConnection putIfAbsent = cache.putIfAbsent(schemaImports, nextConnection);
                    if(putIfAbsent != null)
                    {
                        nextConnection.close();
                        nextConnection = putIfAbsent;
                    }
                    permanentConnection = nextConnection;
                }
                final URI objectIRI = this.getSesameManager().getTopObjectIRI(artifactId, permanentConnection);
                if(objectIRI == null)
                {
                    throw new UnmanagedArtifactIRIException(artifactId.getOntologyIRI(),
                            "Could not find the top object for an artifact");
                }
                results.add(this.getSesameManager().getObjectLabel(artifactId, objectIRI, managementConnection,
                        permanentConnection, this.getRepositoryManager().getSchemaManagementGraph(),
                        this.getRepositoryManager().getArtifactManagementGraph()));
            }
        }
        finally
        {
            try
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
            finally
            {
                for(final RepositoryConnection nextPermanentConnection : cache.values())
                {
                    try
                    {
                        if(nextPermanentConnection != null)
                        {
                            nextPermanentConnection.close();
                        }
                    }
                    catch(final Throwable e)
                    {
                        this.log.error("Found exception closing connection", e);
                    }
                }
            }
        }
        return results;
    }
    
    /**
     * Checks for dangling objects that are not linked to the artifact and deletes them if
     * <i>force</i> is true.
     *
     * @param artifactID
     * @param repositoryConnection
     * @param context
     * @param force
     *            If true, deletes any dangling objects. If false, throws a
     *            DisconnectedObjectException if any dangling objects are found.
     * @throws RepositoryException
     * @throws DisconnectedObjectException
     */
    private void handleDanglingObjects(final IRI artifactID, final RepositoryConnection repositoryConnection,
            final URI context, final DanglingObjectPolicy policy) throws RepositoryException,
        DisconnectedObjectException
    {
        // Short-circuit if they wanted to ignore dangling objects
        if(policy == DanglingObjectPolicy.IGNORE)
        {
            this.log.info("Not checking for dangling objects for artifact: {}", artifactID);
            return;
        }
        
        final Set<URI> danglingObjects =
                RdfUtility.findDisconnectedNodes(artifactID.toOpenRDFURI(), repositoryConnection, context);
        
        if(!danglingObjects.isEmpty())
        {
            if(policy.equals(DanglingObjectPolicy.REPORT))
            {
                this.log.error("Found {} dangling object(s) (reporting). \n {}", danglingObjects.size(),
                        danglingObjects);
                throw new DisconnectedObjectException(danglingObjects, "Update leads to disconnected PODD objects");
            }
            else if(policy.equals(DanglingObjectPolicy.FORCE_CLEAN))
            {
                this.log.info("Found {} dangling object(s) (force cleaning). \n {}", danglingObjects.size(),
                        danglingObjects);
                for(final URI danglingObject : danglingObjects)
                {
                    repositoryConnection.remove(danglingObject, null, null, context);
                    repositoryConnection.remove(null, null, (Value)danglingObject, context);
                }
            }
        }
    }
    
    /**
     * Helper method to handle File References in a newly loaded/updated set of statements.
     *
     * TODO: Optionally remove invalid file references or mark them as invalid using RDF
     * statements/OWL Classes
     *
     * @param repositoryConnection
     * @param context
     * @param policy
     *            If true, verifies that DataReference objects are accessible from their respective
     *            remote File Repositories
     *
     * @throws OpenRDFException
     * @throws PoddException
     */
    private void handleFileReferences(final RepositoryConnection repositoryConnection,
            final DataReferenceVerificationPolicy policy, final URI... contexts) throws OpenRDFException, PoddException
    {
        if(DataReferenceVerificationPolicy.VERIFY == policy)
        {
            if(this.getDataReferenceManager() == null)
            {
                this.log.error("Could not verify data references as the manager was not initialised.");
            }
            else
            {
                this.log.debug("Extracting data references");
                
                final Set<DataReference> fileReferenceResults =
                        this.getDataReferenceManager().extractDataReferences(repositoryConnection, contexts);
                
                this.log.debug("Handling File reference validation");
                
                try
                {
                    this.dataRepositoryManager.verifyDataReferences(fileReferenceResults);
                }
                catch(final DataReferenceVerificationException e)
                {
                    this.log.warn("From " + fileReferenceResults.size() + " file references, "
                            + e.getValidationFailures().size() + " failed validation.");
                    throw e;
                }
            }
        }
    }
    
    /**
     * Helper method to handle File References in a newly loaded/updated set of statements
     */
    private Set<PoddPurlReference> handlePurls(final RepositoryConnection repositoryConnection, final URI context)
        throws PurlProcessorNotHandledException, OpenRDFException
    {
        if(this.getPurlManager() == null)
        {
            return Collections.emptySet();
        }
        
        this.log.debug("Handling Purl generation");
        final Set<PoddPurlReference> purlResults =
                this.getPurlManager().extractPurlReferences(repositoryConnection, context);
        
        this.getPurlManager().convertTemporaryUris(purlResults, repositoryConnection, context);
        return purlResults;
    }
    
    /**
     * Helper method to check schema ontology imports and update use of ontology IRIs to version
     * IRIs.
     */
    private void useVersionsForSchemaImports(final URI ontologyIRI, final RepositoryConnection managementConnection,
            final RepositoryConnection tempRepositoryConnection, final URI tempContext) throws OpenRDFException,
        UnmanagedSchemaIRIException
    {
        final Set<URI> importedSchemas =
                this.getSesameManager().getDirectImports(tempRepositoryConnection, tempContext);
        for(final URI importedSchemaIRI : importedSchemas)
        {
            final InferredOWLOntologyID schemaOntologyID =
                    this.getSesameManager().getSchemaVersion(IRI.create(importedSchemaIRI), managementConnection,
                            this.getRepositoryManager().getSchemaManagementGraph());
            
            // Always replace with the version IRI
            if(!importedSchemaIRI.equals(schemaOntologyID.getVersionIRI().toOpenRDFURI()))
            {
                // modify import to be a specific version of the schema
                this.log.info("Updating import to version <{}>", schemaOntologyID.getVersionIRI());
                tempRepositoryConnection.remove(ontologyIRI, OWL.IMPORTS, importedSchemaIRI, tempContext);
                tempRepositoryConnection.add(ontologyIRI, OWL.IMPORTS, schemaOntologyID.getVersionIRI().toOpenRDFURI(),
                        tempContext);
            }
        }
        
    }
    
    /**
     * This helper method checks for statements with the given property and having a date-time value
     * with the year 1970 and updates their date-time with the given {@link Value}.
     *
     * @param repositoryConnection
     * @param propertyUri
     * @param newTimestamp
     * @param context
     * @throws OpenRDFException
     */
    private void handleTimestamps(final RepositoryConnection repositoryConnection, final URI propertyUri,
            final Value newTimestamp, final URI context) throws OpenRDFException
    {
        final List<Statement> statements =
                Iterations.asList(repositoryConnection.getStatements(null, propertyUri, null, false, context));
        
        for(final Statement s : statements)
        {
            final Value object = s.getObject();
            if(object instanceof Literal)
            {
                final int year = ((Literal)object).calendarValue().getYear();
                if(year == 1970)
                {
                    repositoryConnection.remove(s, context);
                    repositoryConnection.add(s.getSubject(), s.getPredicate(), newTimestamp, context);
                }
            }
        }
    }
    
    /**
     * This is not an API method. QUESTION: Should this be moved to a separate utility class?
     *
     * This method takes a String terminating with a colon (":") followed by an integer and
     * increments this integer by one. If the input String is not of the expected format, appends
     * "1" to the end of the String.
     *
     * E.g.: "http://purl.org/ab/artifact:55" is converted to "http://purl.org/ab/artifact:56"
     * "http://purl.org/ab/artifact:5A" is converted to "http://purl.org/ab/artifact:5A1"
     *
     * @param oldVersion
     * @return
     */
    public String incrementVersion(final String oldVersion)
    {
        final char versionSeparatorChar = ':';
        
        final int positionVersionSeparator = oldVersion.lastIndexOf(versionSeparatorChar);
        if(positionVersionSeparator > 1)
        {
            final String prefix = oldVersion.substring(0, positionVersionSeparator);
            final String version = oldVersion.substring(positionVersionSeparator + 1);
            try
            {
                int versionInt = Integer.parseInt(version);
                versionInt++;
                return prefix + versionSeparatorChar + versionInt;
            }
            catch(final NumberFormatException e)
            {
                return oldVersion.concat("1");
            }
        }
        return oldVersion.concat("1");
    }
    
    @Override
    public boolean isPublished(final InferredOWLOntologyID ontologyId) throws OpenRDFException
    {
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getManagementRepositoryConnection();
            
            return this.getSesameManager().isPublished(ontologyId, conn,
                    this.getRepositoryManager().getArtifactManagementGraph());
        }
        finally
        {
            if(conn != null && conn.isOpen())
            {
                conn.close();
            }
        }
    }
    
    private List<InferredOWLOntologyID> listArtifacts(final boolean published, final boolean unpublished)
        throws OpenRDFException
    {
        if(!published && !unpublished)
        {
            throw new IllegalArgumentException("Cannot choose to exclude both published and unpublished artifacts");
        }
        
        final List<InferredOWLOntologyID> results = new ArrayList<InferredOWLOntologyID>();
        
        RepositoryConnection conn = null;
        
        try
        {
            conn = this.getRepositoryManager().getManagementRepositoryConnection();
            final Collection<InferredOWLOntologyID> ontologies =
                    this.getSesameManager().getOntologies(true, conn,
                            this.getRepositoryManager().getArtifactManagementGraph());
            
            for(final InferredOWLOntologyID nextOntology : ontologies)
            {
                final boolean isPublished =
                        this.getSesameManager().isPublished(nextOntology, conn,
                                this.getRepositoryManager().getArtifactManagementGraph());
                
                if(isPublished)
                {
                    if(published)
                    {
                        results.add(nextOntology);
                    }
                }
                else if(unpublished)
                {
                    results.add(nextOntology);
                }
            }
        }
        finally
        {
            if(conn != null && conn.isOpen())
            {
                conn.close();
            }
        }
        return results;
    }
    
    @Override
    public List<InferredOWLOntologyID> listPublishedArtifacts() throws OpenRDFException
    {
        return this.listArtifacts(true, false);
    }
    
    @Override
    public List<InferredOWLOntologyID> listUnpublishedArtifacts() throws OpenRDFException
    {
        return this.listArtifacts(false, true);
    }
    
    @Override
    public InferredOWLOntologyID loadArtifact(final InputStream inputStream, final RDFFormat format)
        throws OpenRDFException, PoddException, IOException, OWLException
    {
        return this.loadArtifact(inputStream, format, DanglingObjectPolicy.REPORT,
                DataReferenceVerificationPolicy.DO_NOT_VERIFY);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream,
     * org.openrdf.rio.RDFFormat)
     */
    @Override
    public InferredOWLOntologyID loadArtifact(final InputStream inputStream, RDFFormat format,
            final DanglingObjectPolicy danglingObjectPolicy,
            final DataReferenceVerificationPolicy dataReferenceVerificationPolicy) throws OpenRDFException,
        PoddException, IOException, OWLException
    {
        
        if(inputStream == null)
        {
            throw new NullPointerException("Input stream must not be null");
        }
        
        if(format == null)
        {
            format = RDFFormat.RDFXML;
        }
        
        // FIXME: This method only works if the imports are already in a repository somewhere, need
        // to fix the Sesame manager to look for imports in Models also
        
        // connection to the temporary repository that the artifact RDF triples
        // will be stored while they are initially parsed by OWLAPI.
        Repository tempRepository = null;
        RepositoryConnection temporaryConnection = null;
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        InferredOWLOntologyID inferredOWLOntologyID = null;
        Set<OWLOntologyID> schemaImports = null;
        try
        {
            final URI randomContext = PODD.VF.createURI("urn:uuid:" + UUID.randomUUID().toString());
            Model rawModel = Rio.parse(inputStream, "", format, randomContext);
            
            final List<InferredOWLOntologyID> ontologyIDs = OntologyUtils.modelToOntologyIDs(rawModel, true, false);
            if(ontologyIDs.isEmpty())
            {
                throw new EmptyOntologyException(null, "Loaded ontology is empty");
            }
            else if(ontologyIDs.size() > 1)
            {
                this.log.warn("Found multiple ontologies when we were only expecting a single ontology: {}",
                        ontologyIDs);
            }
            else
            {
                this.log.debug("ontologyIDS : {}", ontologyIDs);
            }
            
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            managementConnection.begin();
            
            tempRepository = this.repositoryManager.getNewTemporaryRepository();
            temporaryConnection = tempRepository.getConnection();
            
            // Load the artifact RDF triples into a random context in the temp
            // repository, which may be shared between different uploads
            temporaryConnection.add(rawModel, randomContext);
            
            rawModel.clear();
            rawModel = null;
            
            // check and ensure schema ontology imports are for version IRIs
            this.useVersionsForSchemaImports(ontologyIDs.get(0).getOntologyIRI().toOpenRDFURI(), managementConnection,
                    temporaryConnection, randomContext);
            
            final Model importsModel = new LinkedHashModel();
            
            // Repopulate model so it can be used by OntologyUtils in getSchemaImportsInternal
            temporaryConnection.exportStatements(null, OWL.IMPORTS, null, true, new StatementCollector(importsModel),
                    randomContext);
            temporaryConnection.exportStatements(null, RDF.TYPE, OWL.ONTOLOGY, true, new StatementCollector(
                    importsModel), randomContext);
            temporaryConnection.exportStatements(null, OWL.VERSIONIRI, null, true,
                    new StatementCollector(importsModel), randomContext);
            managementConnection.export(new StatementCollector(importsModel), this.getRepositoryManager()
                    .getSchemaManagementGraph());
            
            // Rio.write(model, Rio.createWriter(RDFFormat.NQUADS, System.out));
            
            schemaImports = new LinkedHashSet<>(OntologyUtils.artifactImports(ontologyIDs.get(0), importsModel));
            
            // Add in all of the imports that are actually imported, but the user did not include in
            // their original list
            // This is necessary to ensure that the repository ontology lists match the artifacts,
            // so the artifact can be discovered accurately given the artifact ontology imports
            for(OWLOntologyID nextArtifactSchemaImport : schemaImports)
            {
                temporaryConnection.add(ontologyIDs.get(0).getOntologyIRI().toOpenRDFURI(), OWL.IMPORTS,
                        nextArtifactSchemaImport.getVersionIRI().toOpenRDFURI(), randomContext);
            }
            
            // Remove any assertions that the user has made about publication status, as this
            // information is a privileged operation that must be done through the designated API
            // method
            this.cleanPrivilegedAssertions(randomContext, temporaryConnection);
            
            // Replace temporary URIs with PURLs
            this.handlePurls(temporaryConnection, randomContext);
            
            // Set a Version IRI for this artifact based on the PURL
            /*
             * Version information need not be available in uploaded artifacts (any existing values
             * are ignored).
             * 
             * For a new artifact, a Version IRI is created based on the Ontology IRI while for a
             * new version of a managed artifact, the most recent version is incremented.
             */
            final IRI ontologyIRI = this.getSesameManager().getOntologyIRI(temporaryConnection, randomContext);
            if(ontologyIRI == null)
            {
                throw new EmptyOntologyException(null, "Loaded ontology is empty");
            }
            
            // check for managed version from artifact graph
            OWLOntologyID currentManagedArtifactID = null;
            
            try
            {
                currentManagedArtifactID =
                        this.getSesameManager().getCurrentArtifactVersion(ontologyIRI, managementConnection,
                                this.getRepositoryManager().getArtifactManagementGraph());
                if(currentManagedArtifactID != null)
                {
                    throw new DuplicateArtifactIRIException(ontologyIRI, "This artifact is already managed");
                }
            }
            catch(final UnmanagedArtifactIRIException e)
            {
                // ignore. indicates a new artifact is being uploaded
                this.log.info("This is an unmanaged artifact IRI {}", ontologyIRI);
            }
            
            IRI newVersionIRI = null;
            if(currentManagedArtifactID == null || currentManagedArtifactID.getVersionIRI() == null)
            {
                newVersionIRI = IRI.create(ontologyIRI.toString() + ":version:1");
            }
            
            if(newVersionIRI != null)
            {
                // set version IRI in temporary repository
                this.log.info("Setting version IRI to <{}>", newVersionIRI);
            }
            
            temporaryConnection.remove(ontologyIRI.toOpenRDFURI(), OWL.VERSIONIRI, null, randomContext);
            temporaryConnection.add(ontologyIRI.toOpenRDFURI(), OWL.VERSIONIRI, newVersionIRI.toOpenRDFURI(),
                    randomContext);
            
            // check and update statements with default timestamp values
            final Value now = PODD.VF.createLiteral(new Date());
            this.handleTimestamps(temporaryConnection, PODD.PODD_BASE_CREATED_AT, now, randomContext);
            this.handleTimestamps(temporaryConnection, PODD.PODD_BASE_LAST_MODIFIED, now, randomContext);
            
            this.handleDanglingObjects(ontologyIRI, temporaryConnection, randomContext, danglingObjectPolicy);
            
            // ensure schema ontologies are cached in memory before loading
            // statements into OWLAPI
            // this.getDirectImports(managementConnection, temporaryRepositoryConnection,
            // randomContext);
            
            permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports, true);
            permanentConnection.begin();
            
            inferredOWLOntologyID =
                    this.loadInferStoreArtifact(temporaryConnection, permanentConnection, managementConnection,
                            randomContext, dataReferenceVerificationPolicy, false, schemaImports);
            
            this.getSesameManager().updateManagedPoddArtifactVersion(inferredOWLOntologyID, true, managementConnection,
                    this.getRepositoryManager().getArtifactManagementGraph());
            
            managementConnection.remove(inferredOWLOntologyID.getOntologyIRI().toOpenRDFURI(), OWL.IMPORTS, null, this
                    .getRepositoryManager().getArtifactManagementGraph());
            
            for(final Statement nextImport : Iterations.asList(permanentConnection.getStatements(inferredOWLOntologyID
                    .getOntologyIRI().toOpenRDFURI(), OWL.IMPORTS, null, true, inferredOWLOntologyID.getVersionIRI()
                    .toOpenRDFURI())))
            {
                managementConnection.add(inferredOWLOntologyID.getOntologyIRI().toOpenRDFURI(), OWL.IMPORTS,
                        nextImport.getObject(), this.getRepositoryManager().getArtifactManagementGraph());
            }
            
            for(final OWLOntologyID nextSchemaImport : schemaImports)
            {
                managementConnection.add(inferredOWLOntologyID.getOntologyIRI().toOpenRDFURI(), OWL.IMPORTS,
                        nextSchemaImport.getVersionIRI().toOpenRDFURI(), this.getRepositoryManager()
                                .getArtifactManagementGraph());
            }
            
            permanentConnection.commit();
            managementConnection.commit();
            
            return inferredOWLOntologyID;
        }
        catch(final Throwable e)
        {
            try
            {
                if(managementConnection != null)
                {
                    managementConnection.rollback();
                }
            }
            finally
            {
                try
                {
                    if(permanentConnection != null)
                    {
                        permanentConnection.rollback();
                    }
                }
                finally
                {
                    if(temporaryConnection != null)
                    {
                        temporaryConnection.rollback();
                    }
                }
            }
            throw e;
        }
        finally
        {
            try
            {
                // release resources
                if(inferredOWLOntologyID != null && schemaImports != null)
                {
                    try
                    {
                        this.getOWLManager().removeCache(inferredOWLOntologyID.getBaseOWLOntologyID(), schemaImports);
                    }
                    finally
                    {
                        this.getOWLManager().removeCache(inferredOWLOntologyID.getInferredOWLOntologyID(),
                                schemaImports);
                    }
                }
            }
            finally
            {
                try
                {
                    if(managementConnection != null)
                    {
                        managementConnection.close();
                    }
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Found exception closing management repository connection", e);
                }
                finally
                {
                    try
                    {
                        if(permanentConnection != null)
                        {
                            permanentConnection.close();
                        }
                    }
                    catch(final RepositoryException e)
                    {
                        this.log.error("Found exception closing permanent repository connection", e);
                    }
                    finally
                    {
                        try
                        {
                            if(temporaryConnection != null)
                            {
                                temporaryConnection.close();
                            }
                        }
                        catch(final RepositoryException e)
                        {
                            this.log.error("Found exception closing temporary repository connection", e);
                        }
                        finally
                        {
                            try
                            {
                                if(tempRepository != null)
                                {
                                    tempRepository.shutDown();
                                }
                            }
                            catch(final RepositoryException e)
                            {
                                this.log.error("Found exception shutting down temporary repository", e);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * @param randomContext
     * @param temporaryRepositoryConnection
     * @throws RepositoryException
     */
    public void cleanPrivilegedAssertions(final URI randomContext,
            final RepositoryConnection temporaryRepositoryConnection) throws RepositoryException
    {
        temporaryRepositoryConnection.remove((Resource)null, PODD.PODD_BASE_HAS_PUBLICATION_STATUS, (Resource)null,
                randomContext);
    }
    
    /**
     * Helper method to load the artifact into OWLAPI from a temporary location, perform reasoning
     * and store in permanent repository.
     *
     * @param fileReferencePolicy
     * @param dependentSchemaOntologies
     */
    private InferredOWLOntologyID loadInferStoreArtifact(final RepositoryConnection tempRepositoryConnection,
            final RepositoryConnection permanentConnection, final RepositoryConnection managementConnection,
            final URI tempContext, final DataReferenceVerificationPolicy fileReferencePolicy,
            final boolean asynchronousInferences, final Set<? extends OWLOntologyID> dependentSchemaOntologies)
        throws OpenRDFException, OWLException, IOException, PoddException, OntologyNotInProfileException,
        InconsistentOntologyException
    {
        // load into OWLAPI
        this.log.debug("Loading podd artifact from temp repository: {}", tempContext);
        final List<Statement> statements =
                Iterations.asList(tempRepositoryConnection.getStatements(null, null, null, true, tempContext));
        
        final RioMemoryTripleSource owlSource =
                new RioMemoryTripleSource(statements.iterator(), Namespaces.asMap(Iterations
                        .asSet(tempRepositoryConnection.getNamespaces())));
        
        final InferredOWLOntologyID inferredOWLOntologyID =
                this.getOWLManager().loadAndInfer(owlSource, permanentConnection, null, dependentSchemaOntologies,
                        managementConnection, this.getRepositoryManager().getSchemaManagementGraph());
        
        // Check file references after inferencing to accurately identify
        // the parent object
        this.handleFileReferences(permanentConnection, fileReferencePolicy, inferredOWLOntologyID.getVersionIRI()
                .toOpenRDFURI(), inferredOWLOntologyID.getInferredOntologyIRI().toOpenRDFURI());
        
        return inferredOWLOntologyID;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#publishArtifact(org.semanticweb .owlapi.model.
     * OWLOntologyID)
     */
    @Override
    public InferredOWLOntologyID publishArtifact(final InferredOWLOntologyID ontologyId) throws OpenRDFException,
        PublishArtifactException, UnmanagedArtifactIRIException, UnmanagedSchemaIRIException
    {
        final IRI ontologyIRI = ontologyId.getOntologyIRI();
        final IRI versionIRI = ontologyId.getVersionIRI();
        
        if(versionIRI == null)
        {
            throw new PublishArtifactException("Could not publish artifact as version was not specified.", ontologyId);
        }
        
        RepositoryConnection managementConnection = null;
        try
        {
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            managementConnection.begin();
            
            if(this.getSesameManager().isPublished(ontologyId, managementConnection,
                    this.getRepositoryManager().getArtifactManagementGraph()))
            {
                // Cannot publish multiple versions of a single artifact
                throw new PublishArtifactException("Could not publish artifact as a version was already published",
                        ontologyId);
            }
            
            final InferredOWLOntologyID currentVersion =
                    this.getSesameManager().getCurrentArtifactVersion(ontologyIRI, managementConnection,
                            this.getRepositoryManager().getArtifactManagementGraph());
            
            if(!currentVersion.getVersionIRI().equals(versionIRI))
            {
                // User must make the given artifact version the current version
                // manually before
                // publishing, to ensure that work from the current version is
                // not lost accidentally
                throw new PublishArtifactException(
                        "Could not publish artifact as it was not the most current version.", ontologyId);
            }
            
            final InferredOWLOntologyID published =
                    this.getSesameManager().setPublished(true, currentVersion, managementConnection,
                            this.getRepositoryManager().getArtifactManagementGraph());
            
            managementConnection.commit();
            
            return published;
        }
        catch(final Throwable e)
        {
            if(managementConnection != null && managementConnection.isActive())
            {
                managementConnection.rollback();
            }
            
            throw e;
        }
        finally
        {
            // release resources
            try
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
            catch(final RepositoryException e)
            {
                this.log.error("Found exception closing repository connection", e);
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#searchForOntologyLabels(org.
     * semanticweb.owlapi.model. OWLOntologyID, java.lang.String, org.openrdf.model.URI[])
     */
    @Override
    public Model searchForOntologyLabels(final InferredOWLOntologyID ontologyID, final String searchTerm,
            final URI[] searchTypes) throws OpenRDFException, UnmanagedSchemaIRIException, SchemaManifestException,
        UnsupportedRDFormatException, IOException, UnmanagedArtifactIRIException, UnmanagedArtifactVersionException,
        RepositoryNotFoundException
    {
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        
        try
        {
            Set<? extends OWLOntologyID> schemaImports;
            
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            final URI[] contexts =
                    this.getSesameManager().versionAndInferredAndSchemaContexts(ontologyID, managementConnection,
                            this.getRepositoryManager().getSchemaManagementGraph(),
                            this.getRepositoryManager().getArtifactManagementGraph());
            // If they specified an artifact then we need to open a connection to that repository
            if(ontologyID != null)
            {
                schemaImports = this.getSchemaImports(ontologyID);
                permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
                return this.getSesameManager().searchOntologyLabels(searchTerm, searchTypes, 1000, 0,
                        permanentConnection, contexts);
            }
            else
            {
                // Otherwise we can use the management repository for the search
                schemaImports = this.getSchemaManager().getCurrentSchemaOntologies();
                return this.getSesameManager().searchOntologyLabels(searchTerm, searchTypes, 1000, 0,
                        managementConnection, contexts);
            }
        }
        catch(final Throwable e)
        {
            try
            {
                if(permanentConnection != null && permanentConnection.isActive())
                {
                    permanentConnection.rollback();
                }
            }
            catch(final RepositoryException e1)
            {
                this.log.error("Found error rolling back repository connection", e1);
            }
            finally
            {
                try
                {
                    if(managementConnection != null && managementConnection.isActive())
                    {
                        managementConnection.rollback();
                    }
                }
                catch(final RepositoryException e2)
                {
                    this.log.error("Found error rolling back repository connection", e2);
                }
            }
            
            throw e;
        }
        finally
        {
            try
            {
                if(managementConnection != null && managementConnection.isOpen())
                {
                    managementConnection.close();
                }
            }
            catch(final RepositoryException e)
            {
                this.log.error("Found exception closing repository connection", e);
            }
            finally
            {
                try
                {
                    if(permanentConnection != null && permanentConnection.isOpen())
                    {
                        permanentConnection.close();
                    }
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Found exception closing repository connection", e);
                }
            }
        }
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#setFileReferenceManager(com.github
     * .podd.api.file. PoddFileReferenceManager)
     */
    @Override
    public void setDataReferenceManager(final DataReferenceManager fileManager)
    {
        this.dataReferenceManager = fileManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#setFileRepositoryManager(com.
     * github.podd.api.file .PoddFileRepositoryManager)
     */
    @Override
    public void setDataRepositoryManager(final PoddDataRepositoryManager dataRepositoryManager)
    {
        this.dataRepositoryManager = dataRepositoryManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#setOwlManager(com.github.podd
     * .api.PoddOWLManager)
     */
    @Override
    public void setOwlManager(final PoddOWLManager owlManager)
    {
        this.owlManager = owlManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#setPurlManager(com.github.podd
     * .api.purl.PoddPurlManager )
     */
    @Override
    public void setPurlManager(final PoddPurlManager purlManager)
    {
        this.purlManager = purlManager;
    }
    
    @Override
    public void setRepositoryManager(final PoddRepositoryManager repositoryManager)
    {
        this.repositoryManager = repositoryManager;
    }
    
    @Override
    public void setSchemaManager(final PoddSchemaManager schemaManager)
    {
        this.schemaManager = schemaManager;
    }
    
    @Override
    public void setSesameManager(final PoddSesameManager sesameManager)
    {
        this.sesameManager = sesameManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#updateArtifact(org.openrdf.model .URI,
     * java.io.InputStream, org.openrdf.rio.RDFFormat)
     */
    @Override
    public Model updateArtifact(final URI artifactUri, final URI versionUri, final Collection<URI> objectUris,
            final InputStream inputStream, RDFFormat format, final UpdatePolicy updatePolicy,
            final DanglingObjectPolicy danglingObjectAction, final DataReferenceVerificationPolicy fileReferenceAction)
        throws OpenRDFException, IOException, OWLException, PoddException
    {
        if(inputStream == null)
        {
            throw new NullPointerException("Input stream must not be null");
        }
        
        if(format == null)
        {
            format = RDFFormat.RDFXML;
        }
        
        final Model model = Rio.parse(inputStream, "", format);
        
        return this.updateArtifact(artifactUri, versionUri, objectUris, model, updatePolicy, danglingObjectAction,
                fileReferenceAction);
    }
    
    /**
     * Internal updateArtifact() method which takes a {@link Model} containing the modified triples
     * instead of an InputStream.
     */
    protected Model updateArtifact(final URI artifactUri, final URI versionUri, final Collection<URI> objectUris,
            final Model model, final UpdatePolicy updatePolicy, final DanglingObjectPolicy danglingObjectAction,
            final DataReferenceVerificationPolicy fileReferenceAction) throws OpenRDFException, IOException,
        OWLException, PoddException
    {
        if(model == null)
        {
            throw new NullPointerException("Input Model must not be null");
        }
        
        // check if the specified artifact URI refers to a managed artifact
        InferredOWLOntologyID artifactID = null;
        try
        {
            artifactID = this.getArtifact(IRI.create(artifactUri));
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            this.log.error("This artifact is unmanaged. [{}]", artifactUri);
            throw e;
        }
        
        // check if updating from the most current version of the artifact
        try
        {
            artifactID = this.getArtifact(IRI.create(versionUri));
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            // if the version IRI is not the most current, it is unmanaged
            final String message =
                    "Attempting to update from an invalid version of an artifact [" + versionUri
                            + "]. The current version is [" + artifactID.getVersionIRI().toString() + "]";
            
            this.log.error(message);
            // TODO: UpdatePolicy.MERGE_WITH_EXISTING and UpdatePolicy.REPLACE_ALL should be fine to
            // go on in most cases
            throw new UnmanagedArtifactVersionException(artifactID.getOntologyIRI(), artifactID.getVersionIRI(),
                    IRI.create(versionUri), message, e);
            // FIXME - handle this conflict intelligently instead of rejecting the update.
        }
        
        final Repository tempRepository = this.getRepositoryManager().getNewTemporaryRepository();
        RepositoryConnection tempRepositoryConnection = null;
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        InferredOWLOntologyID inferredOWLOntologyID = null;
        Set<? extends OWLOntologyID> currentSchemaImports = null;
        
        try
        {
            // create a temporary in-memory repository
            tempRepositoryConnection = tempRepository.getConnection();
            tempRepositoryConnection.begin();
            
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            
            currentSchemaImports = this.getSchemaImports(artifactID);
            
            permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(currentSchemaImports);
            permanentConnection.begin();
            
            // load and copy the artifact's concrete statements to the temporary
            // store
            final RepositoryResult<Statement> repoResult =
                    permanentConnection.getStatements(null, null, null, false, artifactID.getVersionIRI()
                            .toOpenRDFURI());
            final URI tempContext = artifactID.getVersionIRI().toOpenRDFURI();
            tempRepositoryConnection.add(repoResult, tempContext);
            
            // update the artifact statements
            if(UpdatePolicy.REPLACE_ALL == updatePolicy)
            {
                throw new PoddRuntimeException("TODO: Implement support for UpdatePolicy.REPLACE_ALL");
            }
            else if(UpdatePolicy.REPLACE_EXISTING == updatePolicy)
            {
                // create an intermediate context and add "edit" statements to
                // it
                final URI intContext = PODD.VF.createURI("urn:intermediate:", UUID.randomUUID().toString());
                
                tempRepositoryConnection.add(model, intContext);
                
                final Collection<URI> replaceableObjects = new ArrayList<URI>(objectUris);
                
                // If they did not send a list, we create one ourselves.
                if(replaceableObjects.isEmpty())
                {
                    // get all Subjects in "edit" statements
                    final RepositoryResult<Statement> statements =
                            tempRepositoryConnection.getStatements(null, null, null, false, intContext);
                    final List<Statement> allEditStatements = Iterations.addAll(statements, new ArrayList<Statement>());
                    
                    // remove all references to these Subjects in "main" context
                    for(final Statement statement : allEditStatements)
                    {
                        if(statement.getSubject() instanceof URI)
                        {
                            replaceableObjects.add((URI)statement.getSubject());
                        }
                        else
                        {
                            // We do not support replacing objects that are not
                            // referenced using
                            // URIs, so they must stay for REPLACE_EXISTING
                            // To remove blank node subject statements, replace
                            // the entire object
                            // using REPLACE_ALL
                        }
                    }
                }
                
                for(final URI nextReplaceableObject : replaceableObjects)
                {
                    tempRepositoryConnection.remove(nextReplaceableObject, null, null, tempContext);
                }
                
                // copy the "edit" statements from intermediate context into our
                // "main" context
                tempRepositoryConnection.add(
                        tempRepositoryConnection.getStatements(null, null, null, false, intContext), tempContext);
            }
            else if(UpdatePolicy.MERGE_WITH_EXISTING == updatePolicy)
            {
                tempRepositoryConnection.add(model, tempContext);
            }
            else
            {
                throw new PoddRuntimeException("Did not recognise the UpdatePolicy: " + updatePolicy);
            }
            
            // check and update statements with default timestamp values
            final Value now = PODD.VF.createLiteral(new Date());
            this.handleTimestamps(tempRepositoryConnection, PODD.PODD_BASE_CREATED_AT, now, tempContext);
            this.handleTimestamps(tempRepositoryConnection, PODD.PODD_BASE_LAST_MODIFIED, now, tempContext);
            
            this.handleDanglingObjects(artifactID.getOntologyIRI(), tempRepositoryConnection, tempContext,
                    danglingObjectAction);
            
            this.cleanPrivilegedAssertions(tempContext, tempRepositoryConnection);
            
            final Set<PoddPurlReference> purls = this.handlePurls(tempRepositoryConnection, tempContext);
            
            final Model resultsModel = new LinkedHashModel();
            
            // add (temp-object-URI :replacedTempUriWith PURL) statements to Model
            // NOTE: Using nested loops is rather inefficient, but these collections are not
            // expected to have more than a handful of elements
            for(final URI objectUri : objectUris)
            {
                for(final PoddPurlReference purl : purls)
                {
                    final URI tempUri = purl.getTemporaryURI();
                    if(objectUri.equals(tempUri))
                    {
                        resultsModel.add(objectUri, PODD.PODD_REPLACED_TEMP_URI_WITH, purl.getPurlURI());
                        break; // out of inner loop
                    }
                }
            }
            
            // increment the version
            final OWLOntologyID currentManagedArtifactID =
                    this.getSesameManager().getCurrentArtifactVersion(IRI.create(artifactUri), managementConnection,
                            this.getRepositoryManager().getArtifactManagementGraph());
            final URI newVersionIRI =
                    PODD.VF.createURI(this.incrementVersion(currentManagedArtifactID.getVersionIRI().toString()));
            
            // set version IRI in temporary repository
            this.log.info("Setting version IRI to <{}>", newVersionIRI);
            tempRepositoryConnection.remove(artifactID.getOntologyIRI().toOpenRDFURI(), OWL.VERSIONIRI, null,
                    tempContext);
            tempRepositoryConnection.add(artifactID.getOntologyIRI().toOpenRDFURI(), OWL.VERSIONIRI, newVersionIRI,
                    tempContext);
            
            // check and ensure schema ontology imports are for version IRIs
            // WARNING: This method MUST not be used to update schema imports. If they are updated
            // silently here without a prior specific call to the updateSchemaImports method, then
            // they may land in a repository which does not contain their requisite schema
            // ontologies
            this.useVersionsForSchemaImports(artifactID.getOntologyIRI().toOpenRDFURI(), managementConnection,
                    tempRepositoryConnection, tempContext);
            
            final Model importsModel = new LinkedHashModel();
            
            // Repopulate model so it can be used by OntologyUtils in getSchemaImportsInternal
            tempRepositoryConnection.exportStatements(null, OWL.IMPORTS, null, true, new StatementCollector(
                    importsModel), tempContext);
            tempRepositoryConnection.exportStatements(null, RDF.TYPE, OWL.ONTOLOGY, true, new StatementCollector(
                    importsModel), tempContext);
            tempRepositoryConnection.exportStatements(null, OWL.VERSIONIRI, null, true, new StatementCollector(
                    importsModel), tempContext);
            managementConnection.export(new StatementCollector(importsModel), this.getRepositoryManager()
                    .getSchemaManagementGraph());
            
            // Rio.write(model, Rio.createWriter(RDFFormat.NQUADS, System.out));
            
            LinkedHashSet<OWLOntologyID> newSchemaImports =
                    new LinkedHashSet<>(OntologyUtils.artifactImports(artifactID, importsModel));
            
            // Add in all of the imports that are actually imported, but the user did not include in
            // their original list
            // This is necessary to ensure that the repository ontology lists match the artifacts,
            // so the artifact can be discovered accurately given the artifact ontology imports
            for(OWLOntologyID nextArtifactSchemaImport : newSchemaImports)
            {
                tempRepositoryConnection.add(artifactID.getOntologyIRI().toOpenRDFURI(), OWL.IMPORTS,
                        nextArtifactSchemaImport.getVersionIRI().toOpenRDFURI(), tempContext);
            }
            
            // ensure schema ontologies are cached in memory before loading
            // statements into OWLAPI
            // this.getDirectImports(managementConnection, tempRepositoryConnection,
            // tempContext);
            
            inferredOWLOntologyID =
                    this.loadInferStoreArtifact(tempRepositoryConnection, permanentConnection, managementConnection,
                            tempContext, fileReferenceAction, false, currentSchemaImports);
            
            this.getSesameManager().updateManagedPoddArtifactVersion(inferredOWLOntologyID, true, managementConnection,
                    this.getRepositoryManager().getArtifactManagementGraph());
            
            permanentConnection.commit();
            managementConnection.commit();
            tempRepositoryConnection.rollback();
            
            return OntologyUtils.ontologyIDsToModel(Arrays.asList(inferredOWLOntologyID), resultsModel);
        }
        catch(final Exception e)
        {
            try
            {
                if(managementConnection != null && managementConnection.isActive())
                {
                    managementConnection.rollback();
                }
            }
            finally
            {
                try
                {
                    if(permanentConnection != null && permanentConnection.isActive())
                    {
                        permanentConnection.rollback();
                    }
                }
                finally
                {
                    if(tempRepositoryConnection != null && tempRepositoryConnection.isActive())
                    {
                        tempRepositoryConnection.rollback();
                    }
                }
            }
            throw e;
        }
        finally
        {
            try
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
            catch(final RepositoryException e)
            {
                this.log.error("Found exception closing repository connection", e);
            }
            finally
            {
                try
                {
                    if(permanentConnection != null)
                    {
                        permanentConnection.close();
                    }
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Found exception closing repository connection", e);
                }
                finally
                {
                    try
                    {
                        // release resources
                        if(inferredOWLOntologyID != null && currentSchemaImports != null)
                        {
                            this.getOWLManager().removeCache(inferredOWLOntologyID.getBaseOWLOntologyID(),
                                    currentSchemaImports);
                            this.getOWLManager().removeCache(inferredOWLOntologyID.getInferredOWLOntologyID(),
                                    currentSchemaImports);
                        }
                    }
                    finally
                    {
                        try
                        {
                            if(tempRepositoryConnection != null)
                            {
                                tempRepositoryConnection.close();
                            }
                        }
                        catch(final RepositoryException e)
                        {
                            this.log.error("Found exception closing repository connection", e);
                        }
                        finally
                        {
                            tempRepository.shutDown();
                        }
                    }
                }
            }
            
        }
    }
    
    @Override
    public InferredOWLOntologyID updateSchemaImports(final InferredOWLOntologyID artifactId,
            final Set<? extends OWLOntologyID> oldSchemaOntologyIds,
            final Set<? extends OWLOntologyID> newSchemaOntologyIds) throws OpenRDFException, PoddException,
        IOException, OWLException
    {
        if(artifactId == null)
        {
            throw new IllegalArgumentException("Artifact was null");
        }
        
        if(OntologyUtils.ontologyVersionsMatch(oldSchemaOntologyIds, newSchemaOntologyIds))
        {
            this.log.debug(
                    "No need to update schema imports for artifact as the old and new schema sets were equal: {}",
                    artifactId);
            // No change necessary
            return artifactId;
        }
        
        RepositoryConnection managementConnection = null;
        RepositoryConnection oldPermanentConnection = null;
        RepositoryConnection newPermanentConnection = null;
        RepositoryConnection tempRepositoryConnection = null;
        Repository tempRepository = null;
        try
        {
            managementConnection = this.repositoryManager.getManagementRepositoryConnection();
            managementConnection.begin();
            
            final InferredOWLOntologyID artifactVersion =
                    this.sesameManager.getCurrentArtifactVersion(artifactId.getOntologyIRI(), managementConnection,
                            this.repositoryManager.getArtifactManagementGraph());
            if(!artifactVersion.getVersionIRI().equals(artifactId.getVersionIRI()))
            {
                throw new UnmanagedArtifactVersionException(artifactId.getOntologyIRI(),
                        artifactVersion.getVersionIRI(), artifactId.getVersionIRI(),
                        "Cannot update schema imports for artifact as the specified version was not found.");
            }
            
            this.log.info("Starting exporting artifact to RDF: {}", artifactVersion);
            
            oldPermanentConnection = this.repositoryManager.getPermanentRepositoryConnection(oldSchemaOntologyIds);
            oldPermanentConnection.begin();
            
            // Export the artifact without including the old inferred triples, and they will be
            // regenerated using the new schema ontologies
            final Model model =
                    this.exportArtifactInternal(false, oldPermanentConnection,
                            this.getSesameManager().versionContexts(artifactVersion));
            
            if(model.isEmpty())
            {
                if(this.log.isTraceEnabled())
                {
                    System.out.println("Debug why artifact was not found:");
                    // FIXME: Temporary debugging only
                    DebugUtils.printContents(oldPermanentConnection);
                }
                throw new RuntimeException("Was not able to export artifact as it did not seem to exist");
            }
            
            this.log.info("Finished exporting artifact to RDF: {}", artifactVersion);
            
            tempRepository = this.repositoryManager.getNewTemporaryRepository();
            tempRepositoryConnection = tempRepository.getConnection();
            tempRepositoryConnection.begin();
            // Bump the version identifier to a new value
            final IRI newVersionIRI = IRI.create(this.incrementVersion(artifactVersion.getVersionIRI().toString()));
            tempRepositoryConnection.add(model, newVersionIRI.toOpenRDFURI());
            
            tempRepositoryConnection.remove(artifactVersion.getOntologyIRI().toOpenRDFURI(), OWL.VERSIONIRI, null);
            tempRepositoryConnection.add(artifactVersion.getOntologyIRI().toOpenRDFURI(), OWL.VERSIONIRI,
                    newVersionIRI.toOpenRDFURI(), newVersionIRI.toOpenRDFURI());
            
            for(final OWLOntologyID nextOldSchemaOntologyID : oldSchemaOntologyIds)
            {
                // Remove both a generic import and a version specific import,
                // so this method can be
                // used to bump generic imports to version specific imports
                // after they are imported,
                // if necessary.
                tempRepositoryConnection.remove(artifactVersion.getOntologyIRI().toOpenRDFURI(), OWL.IMPORTS,
                        nextOldSchemaOntologyID.getOntologyIRI().toOpenRDFURI());
                tempRepositoryConnection.remove(artifactVersion.getOntologyIRI().toOpenRDFURI(), OWL.IMPORTS,
                        nextOldSchemaOntologyID.getVersionIRI().toOpenRDFURI());
            }
            
            this.log.info("Started caching schema ontologies for artifact migration: {}", artifactVersion);
            // Even if the old version of the artifact did not import this
            // schema, we include it now
            // as it may be required by the others
            for(final OWLOntologyID nextNewSchemaOntologyID : newSchemaOntologyIds)
            {
                // Add import to the specific version
                tempRepositoryConnection.add(artifactVersion.getOntologyIRI().toOpenRDFURI(), OWL.IMPORTS,
                        nextNewSchemaOntologyID.getVersionIRI().toOpenRDFURI(), newVersionIRI.toOpenRDFURI());
            }
            
            // this.log.info("Started caching schema ontologies: {}", newSchemaOntologyIds);
            // this.getOWLManager().cacheSchemaOntologies(newSchemaOntologyIds,
            // managementConnection,
            // this.getRepositoryManager().getSchemaManagementGraph());
            // this.log.info("Finished caching schema ontology: {}", newSchemaOntologyIds);
            //
            // this.log.info("Finished caching schema ontologies for artifact migration: {}",
            // artifactVersion);
            
            tempRepositoryConnection.commit();
            
            this.log.info("Starting reload of artifact to Repository: {}", artifactVersion);
            
            // Get a new connection, creating the new repository if it doesn't already exist
            newPermanentConnection =
                    this.repositoryManager.getPermanentRepositoryConnection(newSchemaOntologyIds, true);
            newPermanentConnection.begin();
            
            // If the following does not succeed, then it throws an exception and we rollback
            // permanentConnection
            final InferredOWLOntologyID inferredOWLOntologyID =
                    this.loadInferStoreArtifact(tempRepositoryConnection, newPermanentConnection, managementConnection,
                            newVersionIRI.toOpenRDFURI(), DataReferenceVerificationPolicy.DO_NOT_VERIFY, false,
                            newSchemaOntologyIds);
            
            this.log.info("Completed reload of artifact to Repository: {}", artifactVersion);
            
            this.getSesameManager().updateManagedPoddArtifactVersion(inferredOWLOntologyID, true, managementConnection,
                    this.getRepositoryManager().getArtifactManagementGraph());
            
            oldPermanentConnection.commit();
            newPermanentConnection.commit();
            managementConnection.commit();
            return inferredOWLOntologyID;
        }
        catch(final Throwable e)
        {
            try
            {
                if(managementConnection != null)
                {
                    managementConnection.rollback();
                }
            }
            catch(final Throwable e1)
            {
                this.log.error("Found exception while rolling back management connection", e);
            }
            finally
            {
                try
                {
                    if(newPermanentConnection != null)
                    {
                        newPermanentConnection.rollback();
                    }
                }
                catch(final Throwable e1)
                {
                    this.log.error("Found exception while rolling back permanent connection", e);
                }
                finally
                {
                    try
                    {
                        if(oldPermanentConnection != null)
                        {
                            oldPermanentConnection.rollback();
                        }
                    }
                    catch(final Throwable e1)
                    {
                        this.log.error("Found exception while rolling back permanent connection", e);
                    }
                    finally
                    {
                        try
                        {
                            if(tempRepositoryConnection != null)
                            {
                                tempRepositoryConnection.rollback();
                            }
                        }
                        catch(final Throwable e1)
                        {
                            this.log.error("Found exception while rolling back temporary connection", e);
                        }
                    }
                }
            }
            throw e;
        }
        finally
        {
            try
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
            finally
            {
                try
                {
                    if(newPermanentConnection != null)
                    {
                        newPermanentConnection.close();
                    }
                }
                finally
                {
                    try
                    {
                        if(oldPermanentConnection != null)
                        {
                            oldPermanentConnection.close();
                        }
                    }
                    finally
                    {
                        try
                        {
                            if(tempRepositoryConnection != null)
                            {
                                tempRepositoryConnection.close();
                            }
                        }
                        finally
                        {
                            if(tempRepository != null)
                            {
                                tempRepository.shutDown();
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public Model childOfList(final Set<URI> topConcepts, final InferredOWLOntologyID ontologyID)
        throws OpenRDFException, UnmanagedSchemaIRIException, SchemaManifestException, UnsupportedRDFormatException,
        IOException, UnmanagedArtifactIRIException, UnmanagedArtifactVersionException, RepositoryNotFoundException
    {
        RepositoryConnection permanentConnection = null;
        RepositoryConnection managementConnection = null;
        try
        {
            final Set<? extends OWLOntologyID> schemaImports = this.getSchemaImports(ontologyID);
            permanentConnection = this.getRepositoryManager().getPermanentRepositoryConnection(schemaImports);
            managementConnection = this.getRepositoryManager().getManagementRepositoryConnection();
            
            final URI[] contexts =
                    this.getSesameManager().versionAndInferredAndSchemaContexts(ontologyID, managementConnection,
                            this.getRepositoryManager().getSchemaManagementGraph(),
                            this.getRepositoryManager().getArtifactManagementGraph());
            
            return this.getSesameManager().ChildOfList(topConcepts, permanentConnection, contexts);
        }
        finally
        {
            try
            {
                if(managementConnection != null)
                {
                    managementConnection.close();
                }
            }
            finally
            {
                if(permanentConnection != null)
                {
                    permanentConnection.close();
                }
            }
        }
    }
    
}
