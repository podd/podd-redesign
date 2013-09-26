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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.resultio.helpers.QueryResultCollector;
import org.openrdf.queryrender.RenderUtils;
import org.openrdf.repository.RepositoryConnection;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.MetadataPolicy;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddObjectLabelImpl;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.RdfUtility;

/**
 * @author kutila
 * 
 */
public class PoddSesameManagerImpl implements PoddSesameManager
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public PoddSesameManagerImpl()
    {
    }
    
    @Override
    public void deleteOntologies(final Collection<InferredOWLOntologyID> givenOntologies,
            final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException
    {
        for(final InferredOWLOntologyID nextOntologyID : givenOntologies)
        {
            final List<InferredOWLOntologyID> versionInternal =
                    this.getCurrentVersionsInternal(nextOntologyID.getOntologyIRI(), repositoryConnection,
                            managementGraph);
            boolean updateCurrentVersion = false;
            InferredOWLOntologyID newCurrentVersion = null;
            
            // If there were managed versions, and the head of the list, which is the current
            // version by convention, is the same as out version, then we need to update it,
            // otherwise we don't need to update it.
            if(!versionInternal.isEmpty()
                    && versionInternal.get(0).getVersionIRI().equals(nextOntologyID.getVersionIRI()))
            {
                updateCurrentVersion = true;
                if(versionInternal.size() > 1)
                {
                    // FIXME: Improve this version detection...
                    newCurrentVersion = versionInternal.get(1);
                }
            }
            
            // clear out the direct and inferred ontology graphs
            repositoryConnection.remove((URI)null, null, null, nextOntologyID.getInferredOntologyIRI().toOpenRDFURI());
            repositoryConnection.remove((URI)null, null, null, nextOntologyID.getVersionIRI().toOpenRDFURI());
            
            // clear out references attached to the version and inferred IRIs in the management
            // graph
            repositoryConnection.remove(nextOntologyID.getVersionIRI().toOpenRDFURI(), null, null, managementGraph);
            repositoryConnection.remove(nextOntologyID.getInferredOntologyIRI().toOpenRDFURI(), null, null,
                    managementGraph);
            
            // clear out references linked to the version and inferred IRIs in the management graph
            repositoryConnection
                    .remove((URI)null, null, nextOntologyID.getVersionIRI().toOpenRDFURI(), managementGraph);
            repositoryConnection.remove((URI)null, null, nextOntologyID.getInferredOntologyIRI().toOpenRDFURI(),
                    managementGraph);
            
            if(updateCurrentVersion)
            {
                final List<Statement> asList =
                        Iterations.asList(repositoryConnection.getStatements(nextOntologyID.getOntologyIRI()
                                .toOpenRDFURI(), PoddRdfConstants.OMV_CURRENT_VERSION, null, false, managementGraph));
                
                if(asList.size() != 1)
                {
                    this.log.error(
                            "Did not find a unique managed current version for ontology with ID: {} List was: {}",
                            nextOntologyID, asList);
                }
                
                // remove the current versions from the management graph
                repositoryConnection.remove(asList, managementGraph);
                
                // If there is no replacement available, then wipe the slate clean in the management
                // graph
                if(newCurrentVersion == null)
                {
                    repositoryConnection.remove(nextOntologyID.getOntologyIRI().toOpenRDFURI(), null, null,
                            managementGraph);
                }
                else
                {
                    // Push the next current version into the management graph
                    repositoryConnection.add(nextOntologyID.getOntologyIRI().toOpenRDFURI(),
                            PoddRdfConstants.OMV_CURRENT_VERSION, newCurrentVersion.getVersionIRI().toOpenRDFURI(),
                            managementGraph);
                }
            }
        }
    }
    
    @Override
    public Model fillMissingLabels(final Model inputModel, final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        Set<URI> missingLabelUris = new LinkedHashSet<>();
        for(final Statement statement : inputModel)
        {
            if(statement.getSubject() instanceof URI
                    && !inputModel.contains(statement.getSubject(), RDFS.LABEL, null, contexts))
            {
                missingLabelUris.add((URI)statement.getSubject());
            }
        }
        
        if(missingLabelUris.isEmpty())
        {
            return new LinkedHashModel();
        }
        
        final StringBuilder graphQuery = new StringBuilder(1024);
        
        graphQuery.append("CONSTRUCT { ");
        graphQuery.append(" ?subject ");
        graphQuery.append(RenderUtils.getSPARQLQueryString(RDFS.LABEL));
        graphQuery.append(" ?label . ");
        
        graphQuery.append("} WHERE {");
        graphQuery.append(" ?subject ");
        graphQuery.append(RenderUtils.getSPARQLQueryString(RDFS.LABEL));
        graphQuery.append(" ?label . ");
        graphQuery.append("}");
        
        graphQuery.append(" VALUES (?subject) { ");
        for(URI nextMissingLabelUri : missingLabelUris)
        {
            graphQuery.append(" ( ");
            graphQuery.append(RenderUtils.getSPARQLQueryString(nextMissingLabelUri));
            graphQuery.append(" ) ");
        }
        graphQuery.append(" } ");
        
        final GraphQuery rdfsGraphQuery =
                repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, graphQuery.toString());
        
        this.log.debug("Created SPARQL {}.", graphQuery);
        
        return RdfUtility.executeGraphQuery(rdfsGraphQuery, contexts);
    }
    
    @Override
    public List<InferredOWLOntologyID> getAllOntologyVersions(final IRI ontologyIRI,
            final RepositoryConnection repositoryConnection, final URI ontologyManagementGraph) throws OpenRDFException
    {
        // FIXME: This implementation doesn't seem correct. Verify with tests.
        return this.getCurrentVersionsInternal(ontologyIRI, repositoryConnection, ontologyManagementGraph);
    }
    
    @Override
    public Set<InferredOWLOntologyID> getAllCurrentSchemaOntologyVersions(
            final RepositoryConnection repositoryConnection, final URI schemaManagementGraph) throws OpenRDFException
    {
        final Set<InferredOWLOntologyID> returnList = new HashSet<InferredOWLOntologyID>();
        final StringBuilder sb = new StringBuilder(1024);
        
        sb.append("SELECT ?ontologyIri ?cv ?civ WHERE { ");
        
        sb.append(" ?ontologyIri <" + RDF.TYPE.stringValue() + "> <" + OWL.ONTOLOGY.stringValue() + "> . ");
        sb.append(" ?ontologyIri <" + PoddRdfConstants.OMV_CURRENT_VERSION.stringValue() + "> ?cv . ");
        sb.append(" ?ontologyIri <" + PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION.stringValue() + "> ?civ . ");
        
        sb.append(" }");
        
        this.log.debug("Generated SPARQL {} ", sb);
        
        final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        final QueryResultCollector queryResults = RdfUtility.executeTupleQuery(query, schemaManagementGraph);
        
        for(final BindingSet nextResult : queryResults.getBindingSets())
        {
            final String nextOntologyIRI = nextResult.getValue("ontologyIri").stringValue();
            final String nextVersionIRI = nextResult.getValue("cv").stringValue();
            final String nextInferredIRI = nextResult.getValue("civ").stringValue();
            
            returnList.add(new InferredOWLOntologyID(IRI.create(nextOntologyIRI), IRI.create(nextVersionIRI), IRI
                    .create(nextInferredIRI)));
        }
        return returnList;
    }
    
    @Override
    public Set<InferredOWLOntologyID> getAllSchemaOntologyVersions(final RepositoryConnection repositoryConnection,
            final URI schemaManagementGraph) throws OpenRDFException
    {
        final Set<InferredOWLOntologyID> returnList = new HashSet<InferredOWLOntologyID>();
        final StringBuilder sb = new StringBuilder(1024);
        
        sb.append("SELECT ?ontologyIri ?versionIri ?inferredVersionIri WHERE { ");
        
        sb.append(" ?ontologyIri <" + RDF.TYPE.stringValue() + "> <" + OWL.ONTOLOGY.stringValue() + "> . ");
        sb.append(" ?ontologyIri <" + OWL.VERSIONIRI.stringValue() + "> ?versionIri . ");
        sb.append(" ?versionIri <" + PoddRdfConstants.PODD_BASE_INFERRED_VERSION.stringValue()
                + "> ?inferredVersionIri . ");
        
        sb.append(" }");
        
        this.log.debug("Generated SPARQL {} ", sb);
        
        final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        final QueryResultCollector queryResults = RdfUtility.executeTupleQuery(query, schemaManagementGraph);
        
        for(final BindingSet nextResult : queryResults.getBindingSets())
        {
            final String nextOntologyIRI = nextResult.getValue("ontologyIri").stringValue();
            final String nextVersionIRI = nextResult.getValue("versionIri").stringValue();
            final String nextInferredIRI = nextResult.getValue("inferredVersionIri").stringValue();
            
            returnList.add(new InferredOWLOntologyID(IRI.create(nextOntologyIRI), IRI.create(nextVersionIRI), IRI
                    .create(nextInferredIRI)));
        }
        return returnList;
    }
    
    /**
     * Given a property URI, this method attempts to return all the valid members in the Range of
     * that property.
     * 
     * @param artifactID
     *            The Collection should either belong to this artifact or be imported by it.
     * @param propertyUri
     *            The property whose members are sought.
     * @param repositoryConnection
     * @return A List of URIs representing all valid members of the given Collection, or an empty
     *         list if the property does not have a pre-defined set of possible members.
     * @throws OpenRDFException
     * 
     * @deprecated Unused. Somewhat similar functionality is available in {@link getInstancesOf()}.
     */
    @Deprecated
    @Override
    public List<URI> getAllValidMembers(final InferredOWLOntologyID artifactID, final URI propertyUri,
            final RepositoryConnection repositoryConnection) throws OpenRDFException
    {
        /*
         * Example: Triples describing PlatformType enumeration consisting of 3 members.
         * 
         * {poddScience:PlatformType} <owl:equivalentClass> {_:genid1636663090}
         * 
         * {_:genid1636663090} <owl:oneOf> {_:genid72508669}
         * 
         * {_:genid72508669} <rdf:first> {poddScience:Software}
         * 
         * {_:genid72508669} <rdf:rest> {_:genid953844943}
         * 
         * {_:genid953844943} <rdf:first> {poddScience:HardwareSoftware}
         * 
         * {_:genid953844943} <rdf:rest> {_:genid278519207}
         * 
         * {_:genid278519207} <rdf:first> {poddScience:Hardware}
         * 
         * {_:genid278519207} <rdf:rest> {rdf:nil}
         */
        
        final List<URI> results = new ArrayList<URI>();
        
        final StringBuilder sb = new StringBuilder(1024);
        sb.append("SELECT ?member WHERE { ");
        sb.append(" ?poddProperty <" + RDFS.RANGE.stringValue() + "> ?poddConcept . ");
        sb.append(" ?poddConcept <" + OWL.EQUIVALENTCLASS.stringValue() + "> ?x . ");
        sb.append(" ?x <" + OWL.ONEOF.stringValue() + "> ?list . ");
        sb.append(" ?list <" + RDF.REST.stringValue() + ">*/<" + RDF.FIRST.stringValue() + "> ?member . ");
        sb.append(" } ");
        
        this.log.debug("Created SPARQL {} with poddProperty bound to {}", sb, propertyUri);
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        tupleQuery.setBinding("poddProperty", propertyUri);
        final QueryResultCollector queryResults =
                RdfUtility.executeTupleQuery(tupleQuery,
                        this.versionAndInferredAndSchemaContexts(artifactID, repositoryConnection));
        
        for(final BindingSet binding : queryResults.getBindingSets())
        {
            final Value member = binding.getValue("member");
            results.add((URI)member);
        }
        return results;
    }
    
    @Override
    public Map<URI, URI> getCardinalityValues(final InferredOWLOntologyID artifactID, final URI objectUri,
            final Collection<URI> propertyUris, final RepositoryConnection repositoryConnection)
        throws OpenRDFException
    {
        return this.getCardinalityValues(objectUri, propertyUris, false, repositoryConnection,
                this.versionAndInferredAndSchemaContexts(artifactID, repositoryConnection));
    }
    
    @Override
    public Map<URI, URI> getCardinalityValues(final URI objectUri, final Collection<URI> propertyUris,
            final boolean findFromType, final RepositoryConnection repositoryConnection, final URI... contexts)
        throws OpenRDFException
    {
        /*
         * Example of how a qualified cardinality statement appears in RDF triples
         * 
         * {poddBase:PoddTopObject} <rdfs:subClassOf> {_:node17l3l94qux1}
         * 
         * {_:node17l3l94qux1} <rdf:type> {owl:Restriction}
         * 
         * {_:node17l3l94qux1} <owl#onProperty> {poddBase:hasLeadInstitution}
         * 
         * {_:node17l3l94qux1} <owl:qualifiedCardinality> {"1"^^<xsd:nonNegativeInteger>}
         * 
         * {_:node17l3l94qux1} <owl:onDataRange> {xsd:string}
         */
        
        final StringBuilder sb = new StringBuilder(1024);
        
        sb.append("SELECT DISTINCT ?propertyUri ?qualifiedCardinality ?minQualifiedCardinality ?maxQualifiedCardinality ");
        sb.append(" WHERE { ");
        
        if(!findFromType)
        {
            sb.append(" ?poddObject a ?somePoddConcept . ");
        }
        
        sb.append(" ?somePoddConcept <" + RDFS.SUBCLASSOF.stringValue() + ">+ ?x . ");
        sb.append(" ?x a <" + OWL.RESTRICTION.stringValue() + "> . ");
        sb.append(" ?x <" + OWL.ONPROPERTY.stringValue() + "> ?propertyUri . ");
        sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#maxQualifiedCardinality> ?maxQualifiedCardinality } . ");
        sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#minQualifiedCardinality> ?minQualifiedCardinality } . ");
        sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#qualifiedCardinality> ?qualifiedCardinality } . ");
        
        sb.append(" } ");
        
        if(!propertyUris.isEmpty())
        {
            sb.append(" VALUES (?propertyUri) { ");
            
            for(URI nextProperty : propertyUris)
            {
                sb.append(" ( ");
                sb.append(RenderUtils.getSPARQLQueryString(nextProperty));
                sb.append(" ) ");
            }
            sb.append(" } ");
        }
        
        // this.log.debug("Created SPARQL {} with propertyUri {} and poddObject {}", sb,
        // propertyUri, objectUri);
        
        final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        // query.setBinding("propertyUri", propertyUri);
        if(findFromType)
        {
            query.setBinding("somePoddConcept", objectUri);
        }
        else
        {
            query.setBinding("poddObject", objectUri);
        }
        
        final QueryResultCollector queryResults = RdfUtility.executeTupleQuery(query, contexts);
        
        final ConcurrentMap<URI, URI> resultMap = new ConcurrentHashMap<URI, URI>();
        
        for(final BindingSet next : queryResults.getBindingSets())
        {
            Value nextProperty = next.getValue("propertyUri");
            if(nextProperty instanceof URI)
            {
                URI nextPropertyURI = (URI)nextProperty;
                URI nextCardinality = PoddRdfConstants.PODD_BASE_CARDINALITY_ZERO_OR_MANY;
                
                final Value qualifiedCardinalityValue = next.getValue("qualifiedCardinality");
                if(qualifiedCardinalityValue != null)
                {
                    nextCardinality = PoddRdfConstants.PODD_BASE_CARDINALITY_EXACTLY_ONE;
                }
                
                int minCardinality = -1;
                int maxCardinality = -1;
                
                final Value minCardinalityValue = next.getValue("minQualifiedCardinality");
                if(minCardinalityValue != null && minCardinalityValue instanceof LiteralImpl)
                {
                    minCardinality = ((LiteralImpl)minCardinalityValue).intValue();
                }
                
                final Value maxCardinalityValue = next.getValue("maxQualifiedCardinality");
                if(maxCardinalityValue != null && maxCardinalityValue instanceof LiteralImpl)
                {
                    maxCardinality = ((LiteralImpl)maxCardinalityValue).intValue();
                }
                
                if(maxCardinality == 1 && minCardinality < 1)
                {
                    nextCardinality = PoddRdfConstants.PODD_BASE_CARDINALITY_ZERO_OR_ONE;
                }
                else if(minCardinality == 1 && maxCardinality != 1)
                {
                    nextCardinality = PoddRdfConstants.PODD_BASE_CARDINALITY_ONE_OR_MANY;
                }
                
                URI putIfAbsent = resultMap.putIfAbsent(nextPropertyURI, nextCardinality);
                
                if(putIfAbsent != null && !nextCardinality.equals(putIfAbsent))
                {
                    log.warn(
                            "Found duplicate cardinality constraints for {} : original constraint : {} ignored constraint {}",
                            nextPropertyURI, putIfAbsent, nextCardinality);
                }
            }
            else
            {
                log.warn("Property was not bound to a URI: {}", nextProperty);
            }
        }
        
        return resultMap;
    }
    
    @Override
    public Set<URI> getChildObjects(final URI objectUri, final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder(1024);
        
        sb.append("SELECT DISTINCT ?childUri ");
        sb.append(" WHERE { ");
        sb.append(" ?poddObject ?propertyUri ?childUri . ");
        sb.append(" FILTER(isIRI(?childUri)) . ");
        sb.append(" ?propertyUri <" + RDFS.SUBPROPERTYOF.stringValue() + "> <" + PoddRdfConstants.PODD_BASE_CONTAINS
                + "> . ");
        sb.append(" } ");
        
        this.log.debug("Created SPARQL {} with poddObject bound to {}", sb, objectUri);
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        tupleQuery.setBinding("poddObject", objectUri);
        final QueryResultCollector queryResults = RdfUtility.executeTupleQuery(tupleQuery, contexts);
        
        final Set<URI> resultSet = new HashSet<URI>();
        for(final BindingSet next : queryResults.getBindingSets())
        {
            final Value child = next.getValue("childUri");
            if(child instanceof URI)
            {
                resultSet.add((URI)child);
            }
        }
        
        return resultSet;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.PoddSesameManager#getCurrentArtifactVersion(org.semanticweb.owlapi.model
     * .IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)
     */
    @Override
    public InferredOWLOntologyID getCurrentArtifactVersion(final IRI ontologyIRI,
            final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException,
        UnmanagedArtifactIRIException
    {
        if(ontologyIRI.toString().startsWith("_:"))
        {
            throw new UnmanagedArtifactIRIException(ontologyIRI,
                    "This IRI does not refer to a managed ontology (blank node)");
        }
        
        final InferredOWLOntologyID inferredOntologyID =
                this.getCurrentVersionInternal(ontologyIRI, repositoryConnection, managementGraph);
        
        if(inferredOntologyID != null)
        {
            return inferredOntologyID;
        }
        else
        {
            throw new UnmanagedArtifactIRIException(ontologyIRI, "This IRI does not refer to a managed ontology");
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model
     * .IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)
     */
    @Override
    public InferredOWLOntologyID getCurrentSchemaVersion(final IRI ontologyIRI,
            final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException,
        UnmanagedSchemaIRIException
    {
        final InferredOWLOntologyID inferredOntologyID =
                this.getCurrentVersionInternal(ontologyIRI, repositoryConnection, managementGraph);
        
        if(inferredOntologyID != null)
        {
            return inferredOntologyID;
        }
        else
        {
            throw new UnmanagedSchemaIRIException(ontologyIRI, "This IRI does not refer to a managed ontology");
        }
    }
    
    /**
     * Inner helper method for getCurrentArtifactVersion() and getCurrentSchemaVersion().
     * 
     * @param ontologyIRI
     * @param repositoryConnection
     * @param managementGraph
     * @return
     * @throws OpenRDFException
     */
    private InferredOWLOntologyID getCurrentVersionInternal(final IRI ontologyIRI,
            final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException
    {
        final List<InferredOWLOntologyID> list =
                this.getCurrentVersionsInternal(ontologyIRI, repositoryConnection, managementGraph);
        
        if(list.isEmpty())
        {
            return null;
        }
        else
        {
            return list.get(0);
        }
    }
    
    /**
     * Inner helper method for getCurrentArtifactVersion() and getCurrentSchemaVersion().
     * 
     * If the input ontologyIRI is either an Ontology IRI or Version IRI for a managed ontology, the
     * ID of the current version of this ontology is returned.
     * 
     * @param ontologyIRI
     *            Either an Ontology IRI or Version IRI for which the current version is requested.
     * @param repositoryConnection
     * @param managementGraph
     * @return A List of InferredOWLOntologyIDs. If the ontology is managed, the list will contain
     *         one entry for its current version. Otherwise the list will be empty.
     * @throws OpenRDFException
     */
    private List<InferredOWLOntologyID> getCurrentVersionsInternal(final IRI ontologyIRI,
            final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException
    {
        final List<InferredOWLOntologyID> returnList = new ArrayList<InferredOWLOntologyID>();
        
        final DatasetImpl dataset = new DatasetImpl();
        dataset.addDefaultGraph(managementGraph);
        dataset.addNamedGraph(managementGraph);
        
        // 1: see if the given IRI exists as an ontology IRI
        final StringBuilder sb1 = new StringBuilder(1024);
        sb1.append("SELECT ?cv ?civ WHERE { ");
        sb1.append(" ?ontologyIri <" + RDF.TYPE.stringValue() + "> <" + OWL.ONTOLOGY.stringValue() + "> . ");
        sb1.append(" ?ontologyIri <" + PoddRdfConstants.OMV_CURRENT_VERSION.stringValue() + "> ?cv . ");
        sb1.append(" ?ontologyIri <" + PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION.stringValue() + "> ?civ . ");
        
        sb1.append(" }");
        
        this.log.debug("Generated SPARQL {} with ontologyIri bound to {}", sb1, ontologyIRI);
        
        final TupleQuery query1 = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb1.toString());
        query1.setBinding("ontologyIri", ontologyIRI.toOpenRDFURI());
        query1.setDataset(dataset);
        
        final TupleQueryResult query1Results = query1.evaluate();
        
        final QueryResultCollector nextResults1 = new QueryResultCollector();
        QueryResults.report(query1Results, nextResults1);
        
        for(final BindingSet nextResult : nextResults1.getBindingSets())
        {
            final String nextVersionIRI = nextResult.getValue("cv").stringValue();
            final String nextInferredIRI = nextResult.getValue("civ").stringValue();
            
            returnList.add(new InferredOWLOntologyID(ontologyIRI, IRI.create(nextVersionIRI), IRI
                    .create(nextInferredIRI)));
        }
        
        // 2: see if the given IRI exists as a version IRI
        final StringBuilder sb2 = new StringBuilder(1024);
        sb2.append("SELECT ?x ?cv ?civ WHERE { ");
        sb2.append(" ?x <" + RDF.TYPE.stringValue() + "> <" + OWL.ONTOLOGY.stringValue() + "> . ");
        sb2.append(" ?x <" + OWL.VERSIONIRI.stringValue() + "> ?versionIri . ");
        sb2.append(" ?x <" + OWL.VERSIONIRI.stringValue() + "> ?cv . ");
        sb2.append(" ?x <" + PoddRdfConstants.OMV_CURRENT_VERSION.stringValue() + "> ?cv . ");
        sb2.append(" ?x <" + PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION.stringValue() + "> ?civ . ");
        sb2.append(" }");
        
        this.log.debug("Generated SPARQL {} with versionIri bound to {}", sb2, ontologyIRI);
        
        final TupleQuery query2 = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb2.toString());
        query2.setBinding("versionIri", ontologyIRI.toOpenRDFURI());
        query2.setDataset(dataset);
        
        final TupleQueryResult queryResults2 = query2.evaluate();
        
        final QueryResultCollector nextResults2 = new QueryResultCollector();
        QueryResults.report(queryResults2, nextResults2);
        
        for(final BindingSet nextResult : nextResults2.getBindingSets())
        {
            final String nextOntologyIRI = nextResult.getValue("x").stringValue();
            final String nextVersionIRI = nextResult.getValue("cv").stringValue();
            final String nextInferredIRI = nextResult.getValue("civ").stringValue();
            
            returnList.add(new InferredOWLOntologyID(IRI.create(nextOntologyIRI), IRI.create(nextVersionIRI), IRI
                    .create(nextInferredIRI)));
        }
        
        return returnList;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddSesameManager#getDirectImports(org.openrdf.repository.
     * RepositoryConnection, org.openrdf.model.URI)
     */
    @Override
    public Set<IRI> getDirectImports(final InferredOWLOntologyID ontologyID,
            final RepositoryConnection repositoryConnection) throws OpenRDFException
    {
        return this.getDirectImports(repositoryConnection, this.versionAndInferredContexts(ontologyID));
    }
    
    @Override
    public Set<IRI> getDirectImports(final RepositoryConnection repositoryConnection, final URI... contexts)
        throws OpenRDFException
    {
        final String sparqlQuery = "SELECT ?x WHERE { ?y <" + OWL.IMPORTS.stringValue() + "> ?x ." + " }";
        this.log.debug("Generated SPARQL {}", sparqlQuery);
        final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        
        final DatasetImpl dataset = new DatasetImpl();
        for(final URI nextContext : contexts)
        {
            dataset.addDefaultGraph(nextContext);
        }
        query.setDataset(dataset);
        
        final Set<IRI> results = Collections.newSetFromMap(new ConcurrentHashMap<IRI, Boolean>());
        
        final TupleQueryResult queryResults = query.evaluate();
        while(queryResults.hasNext())
        {
            final BindingSet nextResult = queryResults.next();
            final String ontologyIRI = nextResult.getValue("x").stringValue();
            results.add(IRI.create(ontologyIRI));
            
        }
        return results;
    }
    
    private Model getInstancesOf(final Collection<URI> nextRangeTypes, final RepositoryConnection repositoryConnection,
            final URI[] contexts) throws OpenRDFException
    {
        if(nextRangeTypes.isEmpty())
        {
            return new LinkedHashModel();
        }
        
        /*
         * This query gets the instances and their RDFS:labels for that are of the given type.
         */
        final StringBuilder instanceQuery = new StringBuilder(1024);
        
        instanceQuery.append("CONSTRUCT { ");
        instanceQuery.append(" ?instanceUri <" + RDF.TYPE.stringValue() + "> ?rangeClass . ");
        instanceQuery.append(" ?instanceUri <" + RDFS.LABEL.stringValue() + "> ?label . ");
        
        instanceQuery.append("} WHERE {");
        instanceQuery.append(" ?instanceUri <" + RDF.TYPE.stringValue() + "> ?rangeClass . ");
        instanceQuery.append(" OPTIONAL { ?instanceUri <" + RDFS.LABEL.stringValue() + "> ?label . } ");
        
        instanceQuery.append("}");
        
        instanceQuery.append(" VALUES (?rangeClass) { ");
        
        for(URI nextRangeType : nextRangeTypes)
        {
            instanceQuery.append(" ( ");
            instanceQuery.append(RenderUtils.getSPARQLQueryString(nextRangeType));
            instanceQuery.append(" ) ");
        }
        instanceQuery.append(" } ");
        
        final GraphQuery rdfsGraphQuery =
                repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, instanceQuery.toString());
        // rdfsGraphQuery.setBinding("rangeClass", nextRangeType);
        
        // this.log.debug("Created SPARQL {} \n   with nextRangeType bound to {}", instanceQuery,
        // nextRangeType);
        
        return RdfUtility.executeGraphQuery(rdfsGraphQuery, contexts);
    }
    
    @Override
    public Model getObjectData(final InferredOWLOntologyID artifactID, final URI objectUri,
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        if(objectUri == null)
        {
            return new LinkedHashModel();
        }
        
        final StringBuilder sb = new StringBuilder(1024);
        
        sb.append("CONSTRUCT { ");
        sb.append(" ?poddObject ?propertyUri ?value . ");
        sb.append(" ?parent ?somePropertyUri ?poddObject . ");
        
        sb.append("} WHERE {");
        
        sb.append(" ?poddObject ?propertyUri ?value . ");
        // TODO: somePropertyUri should be a sub property of podd:contains
        sb.append(" OPTIONAL { ?parent ?somePropertyUri ?poddObject . }");
        
        sb.append("}");
        
        final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
        graphQuery.setBinding("poddObject", objectUri);
        
        final Model queryResults = RdfUtility.executeGraphQuery(graphQuery, contexts);
        
        return queryResults;
    }
    
    /**
     * The result of this method is a Model containing all data required for displaying the details
     * of the object in HTML+RDFa.
     * 
     * The returned graph has the following structure.
     * 
     * poddObject :propertyUri :value
     * 
     * propertyUri RDFS:Label "property label"
     * 
     * value RDFS:Label "value label"
     * 
     * @param objectUri
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    @Override
    public Model getObjectDetailsForDisplay(final InferredOWLOntologyID artifactID, final URI objectUri,
            final RepositoryConnection repositoryConnection) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder(1024);
        
        sb.append("CONSTRUCT { ");
        sb.append(" ?poddObject ?propertyUri ?value . ");
        sb.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
        sb.append(" ?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel . ");
        
        sb.append("} WHERE {");
        
        sb.append(" ?poddObject ?propertyUri ?value . ");
        sb.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
        // value may not have a Label
        sb.append(" OPTIONAL {?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel } . ");
        
        sb.append(" FILTER NOT EXISTS { ?propertyUri <" + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue()
                + "> true } ");
        
        sb.append(" FILTER (?value != <" + OWL.THING.stringValue() + ">) ");
        sb.append(" FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
        sb.append(" FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
        sb.append(" FILTER (?value != <" + OWL.CLASS.stringValue() + ">) ");
        
        sb.append("}");
        
        final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
        graphQuery.setBinding("poddObject", objectUri);
        
        final Model queryResults =
                RdfUtility.executeGraphQuery(graphQuery,
                        this.versionAndInferredAndSchemaContexts(artifactID, repositoryConnection));
        
        return queryResults;
    }
    
    /**
     * Given an object URI, this method attempts to retrieve its label (rdfs:label) and description
     * (rdfs:comment) encapsulated in a <code>PoddObjectLabel</code> instance.
     * 
     * If a label is not found, the local name from the Object URI is used as the label.
     * 
     * @param ontologyID
     *            Is used to decide on the graphs in which to search for a label. This includes the
     *            given ontology as well as its imports.
     * @param objectUri
     *            The object whose label and description are sought.
     * @param repositoryConnection
     * @return
     * @throws OpenRDFException
     */
    @Override
    public PoddObjectLabel getObjectLabel(final InferredOWLOntologyID ontologyID, final URI objectUri,
            final RepositoryConnection repositoryConnection) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder(1024);
        sb.append("SELECT ?label ?description ");
        sb.append(" WHERE { ");
        sb.append(" OPTIONAL { ?objectUri <" + RDFS.LABEL + "> ?label . } ");
        sb.append(" OPTIONAL { ?objectUri <" + RDFS.COMMENT + "> ?description . } ");
        sb.append(" }");
        
        this.log.debug("Created SPARQL {} with objectUri bound to {}", sb, objectUri);
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        tupleQuery.setBinding("objectUri", objectUri);
        final QueryResultCollector queryResults =
                RdfUtility.executeTupleQuery(tupleQuery,
                        this.versionAndInferredAndSchemaContexts(ontologyID, repositoryConnection));
        
        String label = null;
        String description = null;
        
        for(final BindingSet next : queryResults.getBindingSets())
        {
            if(next.getValue("label") != null)
            {
                label = next.getValue("label").stringValue();
            }
            else
            {
                // Disabled this method as it produces worse than useless results for the typical
                // URIs that end in /UUID/object, and "object" is literally the word "object" and is
                // displayed as such.
                // FIXME: This method may be worse than showing them a URI
                // label = objectUri.getLocalName();
                label = objectUri.stringValue();
            }
            
            if(next.getValue("description") != null)
            {
                description = next.getValue("description").stringValue();
            }
        }
        
        return new PoddObjectLabelImpl(ontologyID, objectUri, label, description);
    }
    
    @Override
    public Model getObjectTypeContainsMetadata(final URI objectType, final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        final Model results = new LinkedHashModel();
        if(objectType == null)
        {
            return results;
        }
        
        /*
         * NOTE: This SPARQL query only finds properties defined as OWL restrictions in the given
         * Object Type and its ancestors.
         */
        final StringBuilder owlRestrictionQuery = new StringBuilder(1024);
        
        owlRestrictionQuery.append("CONSTRUCT { ");
        owlRestrictionQuery.append(" ?objectType <" + RDFS.SUBCLASSOF.stringValue() + "> ?x . ");
        owlRestrictionQuery.append(" ?x <" + RDF.TYPE.stringValue() + "> <" + OWL.RESTRICTION.stringValue() + "> . ");
        owlRestrictionQuery.append(" ?x <" + OWL.ONPROPERTY.stringValue() + "> ?propertyUri . ");
        owlRestrictionQuery.append(" ?x <" + OWL.ALLVALUESFROM.stringValue() + "> ?rangeClass . ");
        
        owlRestrictionQuery.append(" ?x <http://www.w3.org/2002/07/owl#onClass> ?owlClass . ");
        owlRestrictionQuery.append(" ?x <http://www.w3.org/2002/07/owl#onDataRange> ?valueRange . ");
        owlRestrictionQuery.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyUriLabel . ");
        owlRestrictionQuery.append(" ?rangeClass <" + RDFS.LABEL.stringValue() + "> ?rangeClassLabel . ");
        
        owlRestrictionQuery.append("} WHERE {");
        
        // TODO: The following seems to pick up restrictions that are put onto other types
        owlRestrictionQuery.append(" ?objectType <" + RDFS.SUBCLASSOF.stringValue() + ">+ ?x . ");
        // owlRestrictionQuery.append(" ?objectType <" + RDFS.SUBCLASSOF.stringValue() + "> ?x . ");
        owlRestrictionQuery.append(" ?x <" + RDF.TYPE.stringValue() + "> <" + OWL.RESTRICTION.stringValue() + "> . ");
        owlRestrictionQuery.append(" ?x <" + OWL.ONPROPERTY.stringValue() + "> ?propertyUri . ");
        owlRestrictionQuery.append(" OPTIONAL { ?x <" + OWL.ALLVALUESFROM.stringValue() + "> ?rangeClass } . ");
        owlRestrictionQuery.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#onClass> ?owlClass } . ");
        owlRestrictionQuery.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#onDataRange> ?valueRange } . ");
        owlRestrictionQuery
                .append(" OPTIONAL { ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyUriLabel } . ");
        owlRestrictionQuery.append(" OPTIONAL { ?rangeClass <" + RDFS.LABEL.stringValue() + "> ?rangeClassLabel } . ");
        
        // exclude doNotDisplay properties
        owlRestrictionQuery.append(" FILTER NOT EXISTS { ?propertyUri <"
                + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue() + "> true . } ");
        
        // include only contains sub-properties
        owlRestrictionQuery.append("FILTER EXISTS { ?propertyUri <" + RDFS.SUBPROPERTYOF.stringValue() + "> <"
                + PoddRdfConstants.PODD_BASE_CONTAINS.stringValue() + "> } ");
        
        owlRestrictionQuery.append("}");
        final String owlRestrictionQueryString = owlRestrictionQuery.toString();
        
        final GraphQuery rdfsGraphQuery =
                repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, owlRestrictionQueryString);
        rdfsGraphQuery.setBinding("objectType", objectType);
        
        this.log.debug("Created SPARQL {} \n   with objectType bound to {}", owlRestrictionQueryString, objectType);
        
        final Model rdfsQueryResults = RdfUtility.executeGraphQuery(rdfsGraphQuery, contexts);
        results.addAll(rdfsQueryResults);
        
        // this.log.info("{} Restrictions found", restrictions.size());
        
        /*
         * Find any sub-classes of the above ranges and include them also
         */
        if(rdfsQueryResults.contains(null, RDF.TYPE, OWL.RESTRICTION))
        {
            final StringBuilder subRangeQuery = new StringBuilder(1024);
            
            subRangeQuery.append("CONSTRUCT { ");
            subRangeQuery.append(" ?objectType <" + RDFS.SUBCLASSOF.stringValue() + "> _:x . ");
            subRangeQuery.append(" _:x <" + RDF.TYPE.stringValue() + "> <" + OWL.RESTRICTION.stringValue() + "> . ");
            subRangeQuery.append(" _:x <" + OWL.ONPROPERTY.stringValue() + "> ?propertyUri . ");
            subRangeQuery.append(" _:x <" + OWL.ALLVALUESFROM.stringValue() + "> ?subRange . ");
            subRangeQuery.append(" ?subRange <" + RDFS.LABEL.stringValue() + "> ?subRangeLabel . ");
            
            subRangeQuery.append("} WHERE {");
            
            subRangeQuery.append(" ?subRange <" + RDFS.SUBCLASSOF.stringValue() + ">+ ?rangeClass . ");
            subRangeQuery.append(" OPTIONAL { ?subRange <" + RDFS.LABEL.stringValue() + "> ?subRangeLabel } . ");
            
            subRangeQuery.append("}");
            subRangeQuery.append(" VALUES (?rangeClass ?propertyUri ?objectType) { ");
            
            for(final Value restriction : rdfsQueryResults.filter(null, RDF.TYPE, OWL.RESTRICTION).subjects())
            {
                if(restriction instanceof Resource)
                {
                    final Resource onProperty =
                            rdfsQueryResults.filter((Resource)restriction, OWL.ONPROPERTY, null).objectResource();
                    final Resource onRange =
                            rdfsQueryResults.filter((Resource)restriction, OWL.ALLVALUESFROM, null).objectResource();
                    
                    if(onProperty instanceof URI && onRange instanceof URI)
                    {
                        subRangeQuery.append(" ( ");
                        subRangeQuery.append(RenderUtils.getSPARQLQueryString(onRange));
                        subRangeQuery.append(" ");
                        subRangeQuery.append(RenderUtils.getSPARQLQueryString(onProperty));
                        subRangeQuery.append(" ");
                        subRangeQuery.append(RenderUtils.getSPARQLQueryString(objectType));
                        subRangeQuery.append(" ) ");
                    }
                    else
                    {
                        // Add warning... If we need to support blank nodes here we will need to
                        // switch to a different type of query, as SPARQL-1.1 VALUES doesn't support
                        // blank nodes
                        log.warn("FIXME: restriction pointed to a non-URI property or allvaluesfrom : {} {} {}",
                                onProperty, onRange, objectType);
                    }
                }
            }
            
            subRangeQuery.append(" } ");
            
            final String subRangeQueryString = subRangeQuery.toString();
            
            final GraphQuery subRangeGraphQuery =
                    repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, subRangeQueryString);
            
            // this.log.debug("Created SPARQL {} \n   with rangeClass bound to {}",
            // subRangeQueryString, restriction);
            
            results.addAll(RdfUtility.executeGraphQuery(subRangeGraphQuery, contexts));
        }
        
        return results;
    }
    
    @Override
    public Model getObjectTypeMetadata(final URI objectType, final boolean includeDoNotDisplayProperties,
            final MetadataPolicy containsPropertyPolicy, final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        final Model results = new LinkedHashModel();
        if(objectType == null)
        {
            return results;
        }
        
        // - find all Properties and their ranges
        
        final Set<Value> properties = new HashSet<Value>();
        
        /*
         * NOTE: This SPARQL query only finds properties defined as OWL restrictions in the given
         * Object Type and its ancestors.
         */
        final StringBuilder owlRestrictionQuery = new StringBuilder(1024);
        
        owlRestrictionQuery.append("CONSTRUCT { ");
        owlRestrictionQuery
                .append(" ?objectType <" + RDF.TYPE.stringValue() + "> <" + OWL.CLASS.stringValue() + "> . ");
        owlRestrictionQuery.append(" ?objectType <" + RDFS.SUBCLASSOF.stringValue() + "> ?x . ");
        owlRestrictionQuery.append(" ?x <" + RDF.TYPE.stringValue() + "> <" + OWL.RESTRICTION.stringValue() + "> . ");
        owlRestrictionQuery.append(" ?x <" + OWL.ONPROPERTY.stringValue() + "> ?propertyUri . ");
        owlRestrictionQuery.append(" ?x <" + OWL.ALLVALUESFROM.stringValue() + "> ?rangeClass . ");
        owlRestrictionQuery.append(" ?x <http://www.w3.org/2002/07/owl#onClass> ?owlClass . ");
        owlRestrictionQuery.append(" ?x <http://www.w3.org/2002/07/owl#onDataRange> ?valueRange . ");
        
        owlRestrictionQuery.append("} WHERE {");
        
        // TODO: The following seems to pick up restrictions that are put onto other types
        owlRestrictionQuery.append(" ?objectType <" + RDFS.SUBCLASSOF.stringValue() + ">+ ?x . ");
        // owlRestrictionQuery.append(" ?objectType <" + RDFS.SUBCLASSOF.stringValue() + "> ?x . ");
        owlRestrictionQuery.append(" ?x <" + RDF.TYPE.stringValue() + "> <" + OWL.RESTRICTION.stringValue() + "> . ");
        owlRestrictionQuery.append(" ?x <" + OWL.ONPROPERTY.stringValue() + "> ?propertyUri . ");
        owlRestrictionQuery.append(" OPTIONAL { ?x <" + OWL.ALLVALUESFROM.stringValue() + "> ?rangeClass } . ");
        owlRestrictionQuery.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#onClass> ?owlClass } . ");
        owlRestrictionQuery.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#onDataRange> ?valueRange } . ");
        
        if(!includeDoNotDisplayProperties)
        {
            owlRestrictionQuery.append(" FILTER NOT EXISTS { ?propertyUri <"
                    + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue() + "> true . } ");
        }
        
        switch(containsPropertyPolicy)
        {
            case EXCLUDE_CONTAINS:
                owlRestrictionQuery.append("FILTER NOT EXISTS { ?propertyUri <" + RDFS.SUBPROPERTYOF.stringValue()
                        + "> <" + PoddRdfConstants.PODD_BASE_CONTAINS.stringValue() + "> } ");
                break;
            
            case ONLY_CONTAINS:
                owlRestrictionQuery.append("FILTER EXISTS { ?propertyUri <" + RDFS.SUBPROPERTYOF.stringValue() + "> <"
                        + PoddRdfConstants.PODD_BASE_CONTAINS.stringValue() + "> } ");
                break;
            
            default:
                // ALL: do nothing. everything will be included
        }
        
        owlRestrictionQuery.append("}");
        final String owlRestrictionQueryString = owlRestrictionQuery.toString();
        
        final GraphQuery graphQuery =
                repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, owlRestrictionQueryString);
        graphQuery.setBinding("objectType", objectType);
        
        this.log.debug("Created SPARQL {} \n   with objectType bound to {}", owlRestrictionQueryString, objectType);
        
        final Model restrictionQueryResults = RdfUtility.executeGraphQuery(graphQuery, contexts);
        results.addAll(restrictionQueryResults);
        
        properties.addAll(restrictionQueryResults.filter(null, OWL.ONPROPERTY, null).objects());
        
        /*
         * This query maps RDFS:Domain and RDFS:Range to OWL:Restriction/SubClassOf so that we get a
         * homogeneous set of results.
         */
        final StringBuilder rdfsQuery = new StringBuilder(1024);
        
        rdfsQuery.append("CONSTRUCT { ");
        rdfsQuery.append(" ?objectType <" + RDF.TYPE.stringValue() + "> <" + OWL.CLASS.stringValue() + "> . ");
        rdfsQuery.append(" ?objectType <" + RDFS.SUBCLASSOF.stringValue() + "> _:x . ");
        rdfsQuery.append(" _:x <" + RDF.TYPE.stringValue() + "> <" + OWL.RESTRICTION.stringValue() + "> . ");
        rdfsQuery.append(" _:x <" + OWL.ONPROPERTY.stringValue() + "> ?propertyUri . ");
        rdfsQuery.append(" _:x <" + OWL.ALLVALUESFROM.stringValue() + "> ?rangeClass . ");
        
        rdfsQuery.append("} WHERE {");
        rdfsQuery.append(" ?objectType <" + RDFS.SUBCLASSOF.stringValue() + ">* ?actualObjectType . ");
        rdfsQuery.append(" ?propertyUri <" + RDFS.DOMAIN.stringValue() + "> ?actualObjectType . ");
        rdfsQuery.append(" ?propertyUri <" + RDFS.RANGE.stringValue() + "> ?rangeClass . ");
        
        if(!includeDoNotDisplayProperties)
        {
            rdfsQuery.append(" FILTER NOT EXISTS { ?propertyUri <"
                    + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue() + "> true . } ");
        }
        
        switch(containsPropertyPolicy)
        {
            case EXCLUDE_CONTAINS:
                rdfsQuery.append("FILTER NOT EXISTS { ?propertyUri <" + RDFS.SUBPROPERTYOF.stringValue() + "> <"
                        + PoddRdfConstants.PODD_BASE_CONTAINS.stringValue() + "> } ");
                break;
            
            case ONLY_CONTAINS:
                rdfsQuery.append("FILTER EXISTS { ?propertyUri <" + RDFS.SUBPROPERTYOF.stringValue() + "> <"
                        + PoddRdfConstants.PODD_BASE_CONTAINS.stringValue() + "> } ");
                break;
            
            default:
                // do nothing. everything will be included
        }
        
        rdfsQuery.append("}");
        final String rdfsQueryString = rdfsQuery.toString();
        final GraphQuery rdfsGraphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, rdfsQueryString);
        rdfsGraphQuery.setBinding("objectType", objectType);
        
        this.log.debug("Created SPARQL {} \n   with objectType bound to {}", rdfsQueryString, objectType);
        
        final Model rdfsQueryResults = RdfUtility.executeGraphQuery(rdfsGraphQuery, contexts);
        results.addAll(rdfsQueryResults);
        
        properties.addAll(rdfsQueryResults.filter(null, OWL.ONPROPERTY, null).objects());
        
        /*
         * If no properties could be found so far, return the empty Model. Continuing further
         * results in erroneously adding statements about RDFS:Label and RDFS:Comment.
         */
        if(properties.isEmpty())
        {
            return results;
        }
        
        /*
         * add statements for annotation properties RDFS:Label and RDFS:Comment
         */
        if(containsPropertyPolicy != MetadataPolicy.ONLY_CONTAINS)
        {
            final URI[] commonAnnotationProperties = { RDFS.LABEL, RDFS.COMMENT };
            
            final StringBuilder annotationQuery = new StringBuilder(1024);
            
            annotationQuery.append("CONSTRUCT { ");
            annotationQuery.append(" ?objectType <" + RDFS.SUBCLASSOF.stringValue() + "> _:x . ");
            annotationQuery.append(" _:x <" + RDF.TYPE.stringValue() + "> <" + OWL.RESTRICTION.stringValue() + "> . ");
            annotationQuery.append(" _:x <" + OWL.ONPROPERTY.stringValue() + "> ?annotationProperty . ");
            annotationQuery.append(" _:x <" + OWL.ALLVALUESFROM.stringValue() + "> ?rangeClass . ");
            
            annotationQuery.append("} WHERE {");
            annotationQuery.append(" ?annotationProperty <" + RDFS.RANGE.stringValue() + "> ?rangeClass . ");
            annotationQuery.append("}");
            
            if(commonAnnotationProperties.length > 0)
            {
                annotationQuery.append(" VALUES (?annotationProperty) { ");
                
                for(URI nextAnnotationPropertyURI : commonAnnotationProperties)
                {
                    annotationQuery.append(" ( ");
                    annotationQuery.append(RenderUtils.getSPARQLQueryString(nextAnnotationPropertyURI));
                    annotationQuery.append(" ) ");
                }
                annotationQuery.append(" } ");
            }
            
            final String annotationQueryString = annotationQuery.toString();
            
            final GraphQuery annotationGraphQuery =
                    repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, annotationQueryString);
            annotationGraphQuery.setBinding("objectType", objectType);
            
            final Model annotationQueryResults = RdfUtility.executeGraphQuery(annotationGraphQuery, contexts);
            
            results.addAll(annotationQueryResults);
            properties.addAll(annotationQueryResults.filter(null, OWL.ONPROPERTY, null).objects());
        }
        
        Set<URI> propertyUris = new LinkedHashSet<>();
        for(final Value property : properties)
        {
            if(property instanceof URI)
            {
                propertyUris.add((URI)property);
            }
        }
        // -- for each property, get meta-data about it
        
        final StringBuilder sb2 = new StringBuilder(1024);
        
        sb2.append("CONSTRUCT { ");
        sb2.append(" ?propertyUri <" + RDF.TYPE.stringValue() + "> ?propertyType . ");
        sb2.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
        sb2.append(" ?propertyUri <" + PoddRdfConstants.PODD_BASE_DISPLAY_TYPE.stringValue()
                + "> ?propertyDisplayType . ");
        sb2.append(" ?propertyUri <" + PoddRdfConstants.PODD_BASE_WEIGHT.stringValue() + "> ?propertyWeight . ");
        
        sb2.append(" ?propertyUri <" + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue()
                + "> ?propertyDoNotDisplay . ");
        
        sb2.append("} WHERE {");
        
        sb2.append(" ?propertyUri <" + RDF.TYPE.stringValue() + "> ?propertyType . ");
        
        sb2.append(" OPTIONAL {?propertyUri <" + PoddRdfConstants.PODD_BASE_DISPLAY_TYPE.stringValue()
                + "> ?propertyDisplayType . }  ");
        
        sb2.append(" OPTIONAL {?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . } ");
        
        sb2.append(" OPTIONAL {?propertyUri <" + PoddRdfConstants.PODD_BASE_WEIGHT.stringValue()
                + "> ?propertyWeight . } ");
        
        if(includeDoNotDisplayProperties)
        {
            sb2.append(" OPTIONAL { ?propertyUri <" + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue()
                    + "> ?propertyDoNotDisplay . } ");
        }
        
        sb2.append("}");
        
        if(!propertyUris.isEmpty())
        {
            sb2.append(" VALUES (?propertyUri) { ");
            
            for(URI nextProperty : propertyUris)
            {
                sb2.append(" ( ");
                sb2.append(RenderUtils.getSPARQLQueryString(nextProperty));
                sb2.append(" ) ");
            }
            sb2.append(" } ");
        }
        
        String sb2String = sb2.toString();
        
        final GraphQuery graphQuery2 = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb2String);
        final Model queryResults2 = RdfUtility.executeGraphQuery(graphQuery2, contexts);
        results.addAll(queryResults2);
        
        // - add cardinality value
        final Map<URI, URI> cardinalityValues =
                this.getCardinalityValues(objectType, propertyUris, true, repositoryConnection, contexts);
        for(URI nextProperty : propertyUris)
        {
            URI cardinalityValue = null;
            
            if(cardinalityValues.containsKey(nextProperty))
            {
                cardinalityValue = cardinalityValues.get(nextProperty);
            }
            
            if(cardinalityValue != null)
            {
                results.add((URI)nextProperty, PoddRdfConstants.PODD_BASE_HAS_CARDINALITY, cardinalityValue);
            }
            else if(nextProperty.equals(RDFS.LABEL))
            {
                results.add((URI)nextProperty, PoddRdfConstants.PODD_BASE_HAS_CARDINALITY,
                        PoddRdfConstants.PODD_BASE_CARDINALITY_EXACTLY_ONE);
            }
        }
        
        Collection<URI> nextRangeTypeURIs = new LinkedHashSet<>();
        
        for(URI property : propertyUris)
        {
            // - find property: type (e.g. object/datatype/annotation), label, display-type,
            // weight
            // graphQuery2.setBinding("propertyUri", property);
            
            // this.log.debug("Created SPARQL {} \n   with propertyUri bound to {}", sb2String,
            // property);
            
            // --- for 'drop-down' type properties, add all possible options into Model
            if(results.contains(property, PoddRdfConstants.PODD_BASE_DISPLAY_TYPE,
                    PoddRdfConstants.PODD_BASE_DISPLAY_TYPE_DROPDOWN))
            {
                for(final Resource nextRestriction : results.filter(null, OWL.ONPROPERTY, property).subjects())
                {
                    Set<Value> nextRangeTypes = results.filter(nextRestriction, OWL.ALLVALUESFROM, null).objects();
                    
                    if(nextRangeTypes.isEmpty())
                    {
                        // see if restriction exists with owl:onClass
                        // TODO: Add OWL.ONCLASS to Sesame vocabulary
                        nextRangeTypes =
                                results.filter(nextRestriction,
                                        PoddRdfConstants.VF.createURI("http://www.w3.org/2002/07/owl#onClass"), null)
                                        .objects();
                    }
                    for(final Value nextRangeType : nextRangeTypes)
                    {
                        if(nextRangeType instanceof URI)
                        {
                            nextRangeTypeURIs.add((URI)nextRangeType);
                        }
                        else
                        {
                            log.warn("Restriction was on a class that did not have a URI: property={}", property);
                        }
                    }
                }
            }
        }
        
        results.addAll(this.getInstancesOf(nextRangeTypeURIs, repositoryConnection, contexts));
        
        return results;
    }
    
    /**
     * Retrieves the most specific types of the given object as a List of URIs.
     * 
     * The contexts searched in are, the given ongology's asserted and inferred graphs as well as
     * their imported schema ontology graphs.
     * 
     * This method depends on the poddBase:doNotDisplay annotation to filter out unwanted
     * super-types.
     * 
     * @param ontologyID
     *            The artifact to which the object belongs
     * @param objectUri
     *            The object whose type is to be determined
     * @param repositoryConnection
     * @return A list of URIs for the identified object Types
     * @throws OpenRDFException
     */
    @Override
    public List<URI> getObjectTypes(final InferredOWLOntologyID ontologyID, final URI objectUri,
            final RepositoryConnection repositoryConnection) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder(1024);
        sb.append("SELECT DISTINCT ?poddTypeUri ");
        sb.append(" WHERE { ");
        sb.append(" ?objectUri <" + RDF.TYPE + "> ?poddTypeUri . ");
        
        sb.append(" FILTER NOT EXISTS { ?poddTypeUri <" + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue()
                + "> true } ");
        sb.append(" FILTER isIRI(?poddTypeUri) ");
        
        // filter out TYPE statements for OWL:Thing, OWL:Individual, OWL:NamedIndividual & OWL:Class
        sb.append("FILTER (?poddTypeUri != <" + OWL.THING.stringValue() + ">) ");
        sb.append("FILTER (?poddTypeUri != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
        sb.append("FILTER (?poddTypeUri != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
        sb.append("FILTER (?poddTypeUri != <" + OWL.CLASS.stringValue() + ">) ");
        
        sb.append(" }");
        
        this.log.debug("Created SPARQL {} with objectUri bound to {}", sb, objectUri);
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        tupleQuery.setBinding("objectUri", objectUri);
        final QueryResultCollector queryResults =
                RdfUtility.executeTupleQuery(tupleQuery,
                        this.versionAndInferredAndSchemaContexts(ontologyID, repositoryConnection));
        
        final List<URI> results = new ArrayList<URI>(queryResults.getBindingSets().size());
        
        for(final BindingSet next : queryResults.getBindingSets())
        {
            results.add((URI)next.getValue("poddTypeUri"));
        }
        
        return results;
    }
    
    @Override
    public Collection<InferredOWLOntologyID> getOntologies(final boolean onlyCurrentVersions,
            final RepositoryConnection repositoryConnection, final URI ontologyManagementGraph) throws OpenRDFException
    {
        final List<InferredOWLOntologyID> returnList = new ArrayList<InferredOWLOntologyID>();
        
        final DatasetImpl dataset = new DatasetImpl();
        dataset.addDefaultGraph(ontologyManagementGraph);
        dataset.addNamedGraph(ontologyManagementGraph);
        
        // 1: see if the given IRI exists as an ontology IRI
        final StringBuilder sb = new StringBuilder(1024);
        
        sb.append("SELECT ?ontology ?version ?inferredVersion WHERE { ?ontology ");
        sb.append(RenderUtils.getSPARQLQueryString(RDF.TYPE));
        sb.append(" ");
        sb.append(RenderUtils.getSPARQLQueryString(OWL.ONTOLOGY));
        sb.append(" . ");
        if(onlyCurrentVersions)
        {
            sb.append(" ?ontology ");
            sb.append(RenderUtils.getSPARQLQueryString(PoddRdfConstants.OMV_CURRENT_VERSION));
            sb.append(" ?version . ");
        }
        else
        {
            sb.append(" ?ontology ");
            sb.append(RenderUtils.getSPARQLQueryString(OWL.VERSIONIRI));
            sb.append(" ?version . ");
        }
        sb.append("OPTIONAL{ ?version ");
        sb.append(RenderUtils.getSPARQLQueryString(PoddRdfConstants.PODD_BASE_INFERRED_VERSION));
        sb.append(" ?inferredVersion . ");
        sb.append(" }");
        sb.append("}");
        
        this.log.debug("Generated SPARQL {}", sb);
        
        final TupleQuery query1 = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        query1.setDataset(dataset);
        
        final TupleQueryResult query1Results = query1.evaluate();
        
        final QueryResultCollector nextResults1 = new QueryResultCollector();
        QueryResults.report(query1Results, nextResults1);
        
        for(final BindingSet nextResult : nextResults1.getBindingSets())
        {
            final String nextOntologyIRI = nextResult.getValue("ontology").stringValue();
            final String nextVersionIRI = nextResult.getValue("version").stringValue();
            String nextInferredIRI = null;
            
            if(nextResult.hasBinding("inferredVersion"))
            {
                nextInferredIRI = nextResult.getValue("inferredVersion").stringValue();
                returnList.add(new InferredOWLOntologyID(IRI.create(nextOntologyIRI), IRI.create(nextVersionIRI), IRI
                        .create(nextInferredIRI)));
            }
            else
            {
                returnList
                        .add(new InferredOWLOntologyID(IRI.create(nextOntologyIRI), IRI.create(nextVersionIRI), null));
            }
        }
        
        return returnList;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.PoddSesameManager#getOntologyIRI(org.openrdf.repository.RepositoryConnection
     * , org.openrdf.model.URI)
     */
    @Override
    public IRI getOntologyIRI(final RepositoryConnection repositoryConnection, final URI context)
        throws OpenRDFException
    {
        // get ontology IRI from the RepositoryConnection using a SPARQL SELECT query
        final String sparqlQuery =
                "SELECT ?nextOntology WHERE { ?nextOntology <" + RDF.TYPE + "> <" + OWL.ONTOLOGY.stringValue()
                        + ">  . " + " ?nextOntology <" + PoddRdfConstants.PODD_BASE_HAS_TOP_OBJECT + "> ?y " + " }";
        this.log.debug("Generated SPARQL {}", sparqlQuery);
        final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        
        final DatasetImpl dataset = new DatasetImpl();
        dataset.addDefaultGraph(context);
        dataset.addNamedGraph(context);
        query.setDataset(dataset);
        
        IRI ontologyIRI = null;
        
        final TupleQueryResult queryResults = query.evaluate();
        if(queryResults.hasNext())
        {
            final BindingSet nextResult = queryResults.next();
            final Value nextOntology = nextResult.getValue("nextOntology");
            if(nextOntology instanceof URI)
            {
                ontologyIRI = IRI.create(nextOntology.stringValue());
            }
            else
            {
                ontologyIRI = IRI.create("_:" + nextOntology.stringValue());
            }
        }
        return ontologyIRI;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.PoddSesameManager#getOntologyVersion(org.semanticweb.owlapi.model.IRI,
     * org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)
     */
    @Override
    public InferredOWLOntologyID getOntologyVersion(final IRI versionIRI,
            final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException
    {
        final DatasetImpl dataset = new DatasetImpl();
        dataset.addDefaultGraph(managementGraph);
        
        // see if the given IRI exists as a version IRI
        final StringBuilder sb2 = new StringBuilder(1024);
        sb2.append("SELECT ?ontologyIri ?inferredIri WHERE { ");
        sb2.append(" ?ontologyIri <" + RDF.TYPE.stringValue() + "> <" + OWL.ONTOLOGY.stringValue() + "> . ");
        sb2.append(" ?ontologyIri <" + OWL.VERSIONIRI.stringValue() + "> ?versionIri . ");
        sb2.append(" ?versionIri <" + PoddRdfConstants.PODD_BASE_INFERRED_VERSION.stringValue() + "> ?inferredIri . ");
        sb2.append(" }");
        
        this.log.debug("Generated SPARQL {} with versionIri bound to <{}>", sb2, versionIRI);
        
        final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb2.toString());
        query.setBinding("versionIri", versionIRI.toOpenRDFURI());
        query.setDataset(dataset);
        
        final TupleQueryResult queryResults = query.evaluate();
        
        final QueryResultCollector resultsCollector = new QueryResultCollector();
        QueryResults.report(queryResults, resultsCollector);
        
        for(final BindingSet nextResult : resultsCollector.getBindingSets())
        {
            final String nextOntologyIRI = nextResult.getValue("ontologyIri").stringValue();
            final String nextInferredIRI = nextResult.getValue("inferredIri").stringValue();
            
            // return the first solution since there should only be only one result
            return new InferredOWLOntologyID(IRI.create(nextOntologyIRI), versionIRI, IRI.create(nextInferredIRI));
        }
        
        // could not find given IRI as a version IRI
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddSesameManager#getParentDetails(org.openrdf.model.URI,
     * org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI...)
     */
    @Override
    public Model getParentDetails(final URI objectUri, final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        if(objectUri == null)
        {
            return new LinkedHashModel();
        }
        
        final StringBuilder sb = new StringBuilder(1024);
        
        sb.append("CONSTRUCT { ");
        sb.append(" ?parent ?parentChildProperty ?poddObject ");
        sb.append("} WHERE {");
        sb.append(" ?parent ?parentChildProperty ?poddObject . ");
        sb.append(" ?parentChildProperty <" + RDFS.SUBPROPERTYOF.stringValue() + "> <"
                + PoddRdfConstants.PODD_BASE_CONTAINS.stringValue() + "> . ");
        sb.append("}");
        
        final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
        graphQuery.setBinding("poddObject", objectUri);
        
        this.log.debug("Created SPARQL {} \n   with poddObject bound to {}", sb, objectUri);
        
        return RdfUtility.executeGraphQuery(graphQuery, contexts);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddSesameManager#getReferringObjectDetails(org.openrdf.model.URI,
     * org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI...)
     */
    @Override
    public Model getReferringObjectDetails(final URI objectUri, final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        if(objectUri == null)
        {
            return new LinkedHashModel();
        }
        
        final StringBuilder sb = new StringBuilder(1024);
        
        sb.append("CONSTRUCT { ");
        sb.append(" ?referrer ?refersToProperty ?poddObject ");
        sb.append("} WHERE {");
        sb.append(" ?referrer ?refersToProperty ?poddObject . ");
        sb.append(" ?refersToProperty <" + RDFS.SUBPROPERTYOF.stringValue() + "> <"
                + PoddRdfConstants.PODD_BASE_REFERS_TO.stringValue() + "> . ");
        sb.append("}");
        
        final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
        graphQuery.setBinding("poddObject", objectUri);
        
        this.log.debug("Created SPARQL {} \n   with poddObject bound to {}", sb, objectUri);
        
        return RdfUtility.executeGraphQuery(graphQuery, contexts);
    }
    
    @Override
    public InferredOWLOntologyID getSchemaVersion(final IRI schemaVersionIRI,
            final RepositoryConnection repositoryConnection, final URI schemaManagementGraph) throws OpenRDFException,
        UnmanagedSchemaIRIException
    {
        final InferredOWLOntologyID ontologyID =
                this.getOntologyVersion(schemaVersionIRI, repositoryConnection, schemaManagementGraph);
        if(ontologyID != null)
        {
            return ontologyID;
        }
        else
        {
            // not a version IRI, return the current schema version
            return this.getCurrentSchemaVersion(schemaVersionIRI, repositoryConnection, schemaManagementGraph);
        }
    }
    
    /**
     * Internal helper method to retrieve the Top-Object IRI for a given ontology.
     * 
     */
    @Override
    public URI getTopObjectIRI(final InferredOWLOntologyID ontologyIRI, final RepositoryConnection repositoryConnection)
        throws OpenRDFException
    {
        final List<URI> results = this.getTopObjects(ontologyIRI, repositoryConnection);
        
        if(results.isEmpty())
        {
            return null;
        }
        else if(results.size() == 1)
        {
            return results.get(0);
        }
        else
        {
            this.log.warn("More than one top object found");
            return results.get(0);
        }
    }
    
    /**
     * Retrieve a list of Top Objects that are contained in the given ontology.
     * 
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    @Override
    public List<URI> getTopObjects(final InferredOWLOntologyID ontologyID,
            final RepositoryConnection repositoryConnection) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder(1024);
        
        sb.append("SELECT DISTINCT ?topObjectUri ");
        
        sb.append(" WHERE { ");
        
        sb.append(" ?artifactUri <" + PoddRdfConstants.PODD_BASE_HAS_TOP_OBJECT.stringValue() + "> ?topObjectUri . \n");
        
        sb.append(" }");
        
        final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        query.setBinding("artifactUri", ontologyID.getOntologyIRI().toOpenRDFURI());
        
        final QueryResultCollector queryResults =
                RdfUtility.executeTupleQuery(query, this.versionAndInferredContexts(ontologyID));
        
        final List<URI> topObjectList = new ArrayList<URI>();
        
        for(final BindingSet next : queryResults.getBindingSets())
        {
            final URI pred = (URI)next.getValue("topObjectUri");
            
            topObjectList.add(pred);
        }
        
        return topObjectList;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddSesameManager#getWeightedProperties(com.github.podd.utils.
     * InferredOWLOntologyID, org.openrdf.model.URI, boolean,
     * org.openrdf.repository.RepositoryConnection)
     */
    @Override
    public List<URI> getWeightedProperties(final URI objectUri, final boolean excludeContainsProperties,
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder(1024);
        
        sb.append("SELECT DISTINCT ?propertyUri ");
        sb.append(" WHERE { ");
        sb.append(" ?poddObject ?propertyUri ?value . ");
        
        // for ORDER BY
        sb.append(" OPTIONAL { ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel } . ");
        
        // for ORDER BY
        sb.append("OPTIONAL { ?propertyUri <" + PoddRdfConstants.PODD_BASE_WEIGHT.stringValue() + "> ?weight } . ");
        
        sb.append("FILTER (?value != <" + OWL.THING.stringValue() + ">) ");
        sb.append("FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
        sb.append("FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
        sb.append("FILTER (?value != <" + OWL.CLASS.stringValue() + ">) ");
        
        // Exclude as TYPE, Label (title) and Comment (description) are displayed separately
        sb.append("FILTER (?propertyUri != <" + RDF.TYPE.stringValue() + ">) ");
        sb.append("FILTER (?propertyUri != <" + RDFS.LABEL.stringValue() + ">) ");
        sb.append("FILTER (?propertyUri != <" + RDFS.COMMENT.stringValue() + ">) ");
        
        if(excludeContainsProperties)
        {
            sb.append("FILTER NOT EXISTS { ?propertyUri <" + RDFS.SUBPROPERTYOF.stringValue() + "> <"
                    + PoddRdfConstants.PODD_BASE_CONTAINS.stringValue() + "> } ");
        }
        
        sb.append(" FILTER NOT EXISTS { ?propertyUri <" + PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY.stringValue()
                + "> true } ");
        
        sb.append(" } ");
        sb.append("  ORDER BY ASC(xsd:integer(?weight)) ASC(?propertyLabel) ");
        
        this.log.debug("Created SPARQL {} with poddObject bound to {}", sb, objectUri);
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        tupleQuery.setBinding("poddObject", objectUri);
        final QueryResultCollector queryResults = RdfUtility.executeTupleQuery(tupleQuery, contexts);
        // this.versionAndSchemaContexts(artifactID, repositoryConnection, c));
        
        final List<URI> resultList = new ArrayList<URI>();
        for(final BindingSet next : queryResults.getBindingSets())
        {
            final Value property = next.getValue("propertyUri");
            if(property instanceof URI)
            {
                resultList.add((URI)property);
            }
        }
        
        return resultList;
    }
    
    @Override
    public boolean isPublished(final InferredOWLOntologyID ontologyID, final RepositoryConnection repositoryConnection,
            final URI managementGraph) throws OpenRDFException
    {
        if(ontologyID == null || ontologyID.getOntologyIRI() == null || ontologyID.getVersionIRI() == null)
        {
            throw new NullPointerException("OWLOntology is incomplete");
        }
        
        final URI artifactGraphUri = ontologyID.getVersionIRI().toOpenRDFURI();
        
        /*
         * ASK {
         * 
         * ?artifact owl:versionIRI ontology-version .
         * 
         * ?artifact poddBase:hasTopObject ?top .
         * 
         * ?top poddBase:hasPublicationStatus poddBase:Published .
         * 
         * }
         */
        // final String sparqlQueryString =
        // "?artifact <" + PoddRdfConstants.OWL_VERSION_IRI.stringValue() + "> "
        // + ontologyID.getVersionIRI().toQuotedString() + " . " + "?artifact <"
        // + PoddRdfConstants.PODD_BASE_HAS_TOP_OBJECT.stringValue() + "> ?top ." + " ?top <"
        // + PoddRdfConstants.PODD_BASE_HAS_PUBLICATION_STATUS.stringValue() + "> <"
        // + PoddRdfConstants.PODD_BASE_PUBLISHED.stringValue() + ">" + " }";
        
        final StringBuilder sparqlQuery = new StringBuilder(1024);
        sparqlQuery.append("ASK { ");
        sparqlQuery.append(" ?artifact ").append(RenderUtils.getSPARQLQueryString(OWL.VERSIONIRI)).append(" ");
        sparqlQuery.append(RenderUtils.getSPARQLQueryString(ontologyID.getVersionIRI().toOpenRDFURI()));
        sparqlQuery.append(" . ");
        sparqlQuery.append(" ?artifact ").append(
                RenderUtils.getSPARQLQueryString(PoddRdfConstants.PODD_BASE_HAS_PUBLICATION_STATUS));
        sparqlQuery.append(" ");
        sparqlQuery.append(RenderUtils.getSPARQLQueryString(PoddRdfConstants.PODD_BASE_PUBLISHED));
        sparqlQuery.append(" . ");
        sparqlQuery.append(" } ");
        
        this.log.debug("Generated SPARQL {}", sparqlQuery);
        
        final BooleanQuery booleanQuery =
                repositoryConnection.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlQuery.toString());
        
        // Create a dataset to specify the contexts
        final DatasetImpl dataset = new DatasetImpl();
        dataset.addDefaultGraph(managementGraph);
        dataset.addNamedGraph(managementGraph);
        booleanQuery.setDataset(dataset);
        
        return booleanQuery.evaluate();
    }
    
    @Override
    public Model searchOntologyLabels(final String searchTerm, final URI[] searchTypes, final int limit,
            final int offset, final RepositoryConnection repositoryConnection, final URI... contexts)
        throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder(1024);
        
        sb.append("CONSTRUCT { ");
        sb.append(" ?uri <" + RDFS.LABEL.stringValue() + "> ?label ");
        sb.append(" } WHERE { ");
        
        // limit the "types" of objects to search for
        if(searchTypes != null)
        {
            for(final URI type : searchTypes)
            {
                sb.append(" ?uri a <" + type.stringValue() + "> . ");
                // sb.append(" ?uri a ?type . ");
                // sb.append(" ?type rdfs:subClassOf+ <" + type.stringValue() + "> . ");
            }
        }
        
        sb.append(" ?uri <" + RDFS.LABEL.stringValue() + "> ?label . ");
        
        // filter for "searchTerm" in label
        sb.append(" FILTER(CONTAINS( LCASE(?label) , LCASE(?searchTerm) )) ");
        
        sb.append(" } LIMIT ");
        sb.append(limit);
        
        sb.append(" OFFSET ");
        sb.append(offset);
        
        final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
        graphQuery.setBinding("searchTerm", PoddRdfConstants.VF.createLiteral(searchTerm));
        
        this.log.debug("Created SPARQL {} with searchTerm bound to '{}' ", sb, searchTerm);
        
        final Model queryResults = RdfUtility.executeGraphQuery(graphQuery, contexts);
        
        return queryResults;
    }
    
    @Override
    public InferredOWLOntologyID setPublished(final boolean wantToPublish, final InferredOWLOntologyID ontologyID,
            final RepositoryConnection repositoryConnection, final URI artifactManagementGraph) throws OpenRDFException
    {
        boolean changeRequired = false;
        
        if(wantToPublish)
        {
            if(!repositoryConnection.hasStatement(ontologyID.getOntologyIRI().toOpenRDFURI(),
                    PoddRdfConstants.PODD_BASE_HAS_PUBLICATION_STATUS, PoddRdfConstants.PODD_BASE_PUBLISHED, false,
                    artifactManagementGraph))
            {
                changeRequired = true;
            }
        }
        else
        {
            if(!repositoryConnection.hasStatement(ontologyID.getOntologyIRI().toOpenRDFURI(),
                    PoddRdfConstants.PODD_BASE_HAS_PUBLICATION_STATUS, PoddRdfConstants.PODD_BASE_NOT_PUBLISHED, false,
                    artifactManagementGraph))
            {
                changeRequired = true;
            }
        }
        
        if(!changeRequired)
        {
            return ontologyID;
        }
        else if(wantToPublish)
        {
            // remove previous value for publication status
            repositoryConnection.remove(ontologyID.getOntologyIRI().toOpenRDFURI(),
                    PoddRdfConstants.PODD_BASE_HAS_PUBLICATION_STATUS, null, artifactManagementGraph);
            
            // then insert the publication status as #Published
            repositoryConnection.add(ontologyID.getOntologyIRI().toOpenRDFURI(),
                    PoddRdfConstants.PODD_BASE_HAS_PUBLICATION_STATUS, PoddRdfConstants.PODD_BASE_PUBLISHED,
                    artifactManagementGraph);
            
            this.log.info("{} was set as Published", ontologyID.getOntologyIRI().toOpenRDFURI());
        }
        else
        {
            // remove previous value for publication status
            repositoryConnection.remove(ontologyID.getOntologyIRI().toOpenRDFURI(),
                    PoddRdfConstants.PODD_BASE_HAS_PUBLICATION_STATUS, null, artifactManagementGraph);
            
            this.updateManagedPoddArtifactVersion(ontologyID, true, repositoryConnection, artifactManagementGraph);
            
            // then insert the publication status as #NotPublished
            repositoryConnection.add(ontologyID.getOntologyIRI().toOpenRDFURI(),
                    PoddRdfConstants.PODD_BASE_HAS_PUBLICATION_STATUS, PoddRdfConstants.PODD_BASE_NOT_PUBLISHED,
                    artifactManagementGraph);
            
            this.log.info("{} was set as Unpublished", ontologyID.getOntologyIRI().toOpenRDFURI());
        }
        
        return ontologyID;
    }
    
    @Override
    public void updateCurrentManagedSchemaOntologyVersion(final InferredOWLOntologyID nextOntologyID,
            final boolean updateCurrent, final RepositoryConnection repositoryConnection, final URI context)
        throws OpenRDFException
    {
        final URI nextOntologyUri = nextOntologyID.getOntologyIRI().toOpenRDFURI();
        final URI nextVersionUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        // NOTE: The version is not used for the inferred ontology ID. A new ontology URI must
        // be generated for each new inferred ontology generation. For reference though, the
        // version is equal to the ontology IRI in the prototype code. See
        // generateInferredOntologyID method for the corresponding code.
        final URI nextInferredOntologyUri = nextOntologyID.getInferredOntologyIRI().toOpenRDFURI();
        
        // type the ontology
        repositoryConnection.add(nextOntologyUri, RDF.TYPE, OWL.ONTOLOGY, context);
        // setup a version number link for this version
        repositoryConnection.add(nextOntologyUri, PoddRdfConstants.OWL_VERSION_IRI, nextVersionUri, context);
        
        final List<Statement> currentVersions =
                Iterations.asList(repositoryConnection.getStatements(nextOntologyUri,
                        PoddRdfConstants.OMV_CURRENT_VERSION, null, false, context));
        
        // If there are no current versions, or we must update the current version, then do it
        // here
        if(currentVersions.isEmpty() || updateCurrent)
        {
            // remove whatever was previously there for the current version marker
            repositoryConnection.remove(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, null, context);
            
            // then insert the new current version marker
            repositoryConnection.add(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, nextVersionUri, context);
        }
        
        // then do a similar process with the inferred axioms ontology
        repositoryConnection.add(nextInferredOntologyUri, RDF.TYPE, OWL.ONTOLOGY, context);
        
        // remove whatever was previously there for the current inferred version marker
        repositoryConnection
                .remove(nextOntologyUri, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION, null, context);
        
        // link from the ontology IRI to the current inferred axioms ontology version
        repositoryConnection.add(nextOntologyUri, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                nextInferredOntologyUri, context);
        
        // link from the ontology version IRI to the matching inferred axioms ontology version
        repositoryConnection.add(nextVersionUri, PoddRdfConstants.PODD_BASE_INFERRED_VERSION, nextInferredOntologyUri,
                context);
        
    }
    
    @Override
    public void updateManagedPoddArtifactVersion(final InferredOWLOntologyID nextOntologyID,
            final boolean updateCurrentAndDeletePrevious, final RepositoryConnection repositoryConnection,
            final URI managementGraph) throws OpenRDFException
    {
        final URI nextOntologyUri = nextOntologyID.getOntologyIRI().toOpenRDFURI();
        final URI nextVersionUri = nextOntologyID.getVersionIRI().toOpenRDFURI();
        // NOTE: The version is not used for the inferred ontology ID. A new ontology URI must
        // be generated for each new inferred ontology generation. For reference though, the
        // version is equal to the ontology IRI in the prototype code. See
        // generateInferredOntologyID method for the corresponding code.
        final URI nextInferredOntologyUri = nextOntologyID.getInferredOntologyIRI().toOpenRDFURI();
        
        final List<InferredOWLOntologyID> allOntologyVersions =
                this.getAllOntologyVersions(nextOntologyID.getOntologyIRI(), repositoryConnection, managementGraph);
        
        // If there are no current versions then the steps are relatively simple
        if(allOntologyVersions.isEmpty())
        {
            // type the ontology
            repositoryConnection.add(nextOntologyUri, RDF.TYPE, OWL.ONTOLOGY, managementGraph);
            // type the version of the ontology
            repositoryConnection.add(nextVersionUri, RDF.TYPE, OWL.ONTOLOGY, managementGraph);
            // type the inferred ontology
            repositoryConnection.add(nextInferredOntologyUri, RDF.TYPE, OWL.ONTOLOGY, managementGraph);
            
            // then insert the new current version marker
            repositoryConnection.add(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, nextVersionUri,
                    managementGraph);
            // link from the ontology IRI to the current inferred axioms ontology version
            repositoryConnection.add(nextOntologyUri, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                    nextInferredOntologyUri, managementGraph);
            
            // setup a version number link for this version
            repositoryConnection
                    .add(nextOntologyUri, PoddRdfConstants.OWL_VERSION_IRI, nextVersionUri, managementGraph);
            // link from the ontology version IRI to the matching inferred axioms ontology version
            repositoryConnection.add(nextVersionUri, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                    nextInferredOntologyUri, managementGraph);
        }
        else
        {
            // else, do find and replace to add the version into the system
            
            // type the ontology
            repositoryConnection.add(nextOntologyUri, RDF.TYPE, OWL.ONTOLOGY, managementGraph);
            // type the version of the ontology
            repositoryConnection.add(nextVersionUri, RDF.TYPE, OWL.ONTOLOGY, managementGraph);
            // type the inferred ontology
            repositoryConnection.add(nextInferredOntologyUri, RDF.TYPE, OWL.ONTOLOGY, managementGraph);
            
            // Update the current version and cleanup previous versions
            if(updateCurrentAndDeletePrevious)
            {
                // remove the content of any contexts that are the object of versionIRI statements
                final List<Statement> previousVersions =
                        Iterations.asList(repositoryConnection.getStatements(nextOntologyUri,
                                PoddRdfConstants.OWL_VERSION_IRI, null, true, managementGraph));
                
                for(final Statement nextPreviousVersion : previousVersions)
                {
                    if(nextPreviousVersion.getObject() instanceof URI)
                    {
                        final List<Statement> previousInferredVersions =
                                Iterations.asList(repositoryConnection.getStatements(
                                        (URI)nextPreviousVersion.getObject(),
                                        PoddRdfConstants.PODD_BASE_INFERRED_VERSION, null, false, managementGraph));
                        
                        for(final Statement nextInferredVersion : previousInferredVersions)
                        {
                            if(nextInferredVersion.getObject() instanceof URI)
                            {
                                // clear inferred statements for previous inferred version
                                repositoryConnection.clear((URI)nextInferredVersion.getObject());
                                
                                // remove all references from artifact management graph
                                repositoryConnection.remove((URI)nextInferredVersion.getObject(), null, null,
                                        managementGraph);
                            }
                            else
                            {
                                this.log.error("Found inferred version IRI that was not a URI: {}", nextInferredVersion);
                            }
                        }
                        
                        repositoryConnection.clear((URI)nextPreviousVersion.getObject());
                        repositoryConnection.remove((URI)nextPreviousVersion.getObject(), null, null, managementGraph);
                    }
                    else
                    {
                        this.log.error("Found version IRI that was not a URI: {}", nextPreviousVersion);
                    }
                }
                
                // remove whatever was previously there for the current version marker
                repositoryConnection.remove(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, null,
                        managementGraph);
                
                // then insert the new current version marker
                repositoryConnection.add(nextOntologyUri, PoddRdfConstants.OMV_CURRENT_VERSION, nextVersionUri,
                        managementGraph);
                
                // remove whatever was previously there for the current inferred version marker
                repositoryConnection.remove(nextOntologyUri, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION, null,
                        managementGraph);
                
                // link from the ontology IRI to the current inferred axioms ontology version
                repositoryConnection.add(nextOntologyUri, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                        nextInferredOntologyUri, managementGraph);
                
                // remove previous versionIRI statements if they are no longer needed, before adding
                // the new version below
                repositoryConnection.remove(nextOntologyUri, PoddRdfConstants.OWL_VERSION_IRI, null, managementGraph);
            }
            
            // always setup a version number link for this version
            repositoryConnection
                    .add(nextOntologyUri, PoddRdfConstants.OWL_VERSION_IRI, nextVersionUri, managementGraph);
            
            // always setup an inferred axioms ontology version for this version
            repositoryConnection.add(nextVersionUri, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                    nextInferredOntologyUri, managementGraph);
        }
    }
    
    /**
     * Return an array of URIs representing contexts that can be used to access the version and
     * inferred contexts for the given {@link InferredOWLOntologyID}, along with all of its imported
     * schema ontologies, which are derived using
     * {@link #getDirectImports(InferredOWLOntologyID, RepositoryConnection)}.
     * 
     * @param ontologyID
     * @param repositoryConnection
     * @return
     * @throws OpenRDFException
     */
    @Override
    public URI[] versionAndInferredAndSchemaContexts(final InferredOWLOntologyID ontologyID,
            final RepositoryConnection repositoryConnection) throws OpenRDFException
    {
        // FIXME: Change this to use versionAndSchemaContexts as its basis, and then just add the
        // inferred if available at the end.
        final Set<IRI> directImports = this.getDirectImports(ontologyID, repositoryConnection);
        
        final List<URI> results = new ArrayList<URI>(directImports.size() + 2);
        
        results.addAll(Arrays.asList(this.versionAndInferredContexts(ontologyID)));
        
        for(final IRI nextDirectImport : directImports)
        {
            results.add(nextDirectImport.toOpenRDFURI());
        }
        
        return results.toArray(new URI[0]);
    }
    
    /**
     * Return an array of URIs representing contexts that can be used to access the version and
     * inferred contexts for the given {@link InferredOWLOntologyID}.
     * 
     * @param ontologyID
     * @return
     */
    @Override
    public URI[] versionAndInferredContexts(final InferredOWLOntologyID ontologyID)
    {
        if(ontologyID.getInferredOntologyIRI() != null)
        {
            return new URI[] { ontologyID.getVersionIRI().toOpenRDFURI(),
                    ontologyID.getInferredOntologyIRI().toOpenRDFURI() };
        }
        else
        {
            return new URI[] { ontologyID.getVersionIRI().toOpenRDFURI() };
        }
    }
    
    /**
     * Return an array of URIs representing contexts that can be used to access the version and
     * inferred contexts for the given {@link InferredOWLOntologyID}, along with all of its imported
     * schema ontologies, which are derived using
     * {@link #getDirectImports(InferredOWLOntologyID, RepositoryConnection)}.
     * <p>
     * NOTE: This method intentionally does not include the inferred ontology IRI here so that we
     * can search for concrete triples specifically.
     * 
     * @param ontologyID
     *            An InferredOWLOntologyID which can be used to identify which schemas are relevant.
     *            If it is null all of the current schema contexts will be returned.
     * @param repositoryConnection
     *            The repository connection which is to be used to source the contexts from.
     * @param schemaManagementGraph
     *            If ontologyID is null, this parameter is used instead to source all of the current
     *            schema ontology versions.
     * @return An array of {@link URI}s that can be passed into the {@link RepositoryConnection}
     *         varargs methods to define which contexts are relevant to queries, or used to define
     *         the default graphs for SPARQL queries.
     * @throws OpenRDFException
     */
    @Override
    public URI[] versionAndSchemaContexts(final InferredOWLOntologyID ontologyID,
            final RepositoryConnection repositoryConnection, final URI schemaManagementGraph) throws OpenRDFException
    {
        final Set<URI> contexts = new LinkedHashSet<URI>();
        if(ontologyID != null)
        {
            contexts.add(ontologyID.getVersionIRI().toOpenRDFURI());
            
            final Set<IRI> directImports = this.getDirectImports(ontologyID, repositoryConnection);
            for(final IRI directImport : directImports)
            {
                contexts.add(directImport.toOpenRDFURI());
            }
        }
        else
        {
            final Set<InferredOWLOntologyID> allSchemaOntologyVersions =
                    this.getAllCurrentSchemaOntologyVersions(repositoryConnection, schemaManagementGraph);
            for(final InferredOWLOntologyID schemaOntology : allSchemaOntologyVersions)
            {
                contexts.add(schemaOntology.getVersionIRI().toOpenRDFURI());
            }
        }
        return contexts.toArray(new URI[0]);
    }
    
}
