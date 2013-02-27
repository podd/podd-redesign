/**
 * 
 */
package com.github.podd.impl;

import info.aduna.iteration.Iterations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.resultio.helpers.QueryResultCollector;
import org.openrdf.repository.RepositoryConnection;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddSesameManager;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddObjectLabelImpl;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.SparqlQueryHelper;

/**
 * @author kutila
 * 
 */
public class PoddSesameManagerImpl implements PoddSesameManager
{
    public PoddSesameManagerImpl()
    {
    }
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Helper method to execute a given SPARQL SELECT query.
     * 
     * @param sparqlQuery
     * @param repositoryConnection
     * @param contexts
     * @return The
     * @throws OpenRDFException
     */
    private TupleQueryResult executeSparqlQuery(final String sparqlQuery,
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        this.log.info("Executing SPARQL: \r\n {}", sparqlQuery);
        
        final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        
        return this.executeSparqlQuery(query, contexts);
    }
    
    /**
     * Helper method to execute a given SPARQL Tuple query.
     * 
     * @param sparqlQuery
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    private TupleQueryResult executeSparqlQuery(final TupleQuery sparqlQuery, final URI... contexts)
        throws OpenRDFException
    {
        final DatasetImpl dataset = new DatasetImpl();
        for(final URI uri : contexts)
        {
            dataset.addDefaultGraph(uri);
        }
        sparqlQuery.setDataset(dataset);
        
        return sparqlQuery.evaluate();
    }
    
    /**
     * Helper method to execute a given SPARQL Graph query.
     * 
     * @param sparqlQuery
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    private GraphQueryResult executeGraphQuery(final GraphQuery sparqlQuery, final URI... contexts)
        throws OpenRDFException
    {
        final DatasetImpl dataset = new DatasetImpl();
        for(final URI uri : contexts)
        {
            dataset.addDefaultGraph(uri);
        }
        sparqlQuery.setDataset(dataset);
        return sparqlQuery.evaluate();
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
    public List<InferredOWLOntologyID> getAllOntologyVersions(final IRI ontologyIRI,
            final RepositoryConnection repositoryConnection, final URI ontologyManagementGraph) throws OpenRDFException
    {
        return this.getCurrentVersionsInternal(ontologyIRI, repositoryConnection, ontologyManagementGraph);
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
     * @param ontologyIRI
     * @param repositoryConnection
     * @param managementGraph
     * @return
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
        final String sparqlQuery1 =
                "SELECT ?cv ?civ WHERE { " + ontologyIRI.toQuotedString() + " <" + RDF.TYPE.stringValue() + "> <"
                        + OWL.ONTOLOGY.stringValue() + "> . " + ontologyIRI.toQuotedString() + " <"
                        + PoddRdfConstants.OMV_CURRENT_VERSION.stringValue() + "> ?cv . "
                        + ontologyIRI.toQuotedString() + " <"
                        + PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION.stringValue() + "> ?civ . " + " }";
        // SELECT ?cv ?civ WHERE { <iri> :type owl:ontology . <iri> omv:current-version ?cv . <iri>
        // :current-inferred-version ?civ . }
        this.log.info("Generated SPARQL {}", sparqlQuery1);
        
        final TupleQuery query1 = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery1);
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
        final String sparqlQuery2 =
                "SELECT ?x ?civ WHERE { " + " ?x <" + PoddRdfConstants.OMV_CURRENT_VERSION.stringValue() + "> "
                        + ontologyIRI.toQuotedString() + " . " + " ?x <" + RDF.TYPE.stringValue() + "> <"
                        + OWL.ONTOLOGY.stringValue() + "> . " + " ?x <"
                        + PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION.stringValue() + "> ?civ . " + " }";
        // SELECT ?x ?civ WHERE { ?x omv:current-version <iri> . ?x :type owl:ontology . ?x
        // :current-inferred-version ?civ . }
        this.log.info("Generated SPARQL {}", sparqlQuery2);
        
        final TupleQuery query2 = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery2);
        query2.setDataset(dataset);
        
        final TupleQueryResult queryResults2 = query2.evaluate();
        
        final QueryResultCollector nextResults2 = new QueryResultCollector();
        QueryResults.report(queryResults2, nextResults2);
        
        for(final BindingSet nextResult : nextResults2.getBindingSets())
        {
            final String nextOntologyIRI = nextResult.getValue("x").stringValue();
            final String nextInferredIRI = nextResult.getValue("civ").stringValue();
            
            returnList.add(new InferredOWLOntologyID(IRI.create(nextOntologyIRI), ontologyIRI, IRI
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
    public Set<IRI> getDirectImports(final RepositoryConnection repositoryConnection, final URI context)
        throws OpenRDFException
    {
        final String sparqlQuery = "SELECT ?x WHERE { ?y <" + OWL.IMPORTS.stringValue() + "> ?x ." + " }";
        this.log.info("Generated SPARQL {}", sparqlQuery);
        final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        
        final DatasetImpl dataset = new DatasetImpl();
        dataset.addDefaultGraph(context);
        dataset.addNamedGraph(context);
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
    
    @Override
    public Collection<InferredOWLOntologyID> getOntologies(final boolean onlyCurrentVersions,
            final RepositoryConnection repositoryConnection, final URI ontologyManagementGraph) throws OpenRDFException
    {
        final List<InferredOWLOntologyID> returnList = new ArrayList<InferredOWLOntologyID>();
        
        final DatasetImpl dataset = new DatasetImpl();
        dataset.addDefaultGraph(ontologyManagementGraph);
        dataset.addNamedGraph(ontologyManagementGraph);
        
        // 1: see if the given IRI exists as an ontology IRI
        final StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT ?ontology ?version ?inferredVersion WHERE { ?ontology <");
        sb.append(RDF.TYPE.stringValue());
        sb.append("> <");
        sb.append(OWL.ONTOLOGY.stringValue());
        sb.append("> . ");
        if(onlyCurrentVersions)
        {
            sb.append(" ?ontology <");
            sb.append(PoddRdfConstants.OMV_CURRENT_VERSION.stringValue());
            sb.append("> ?version . ");
        }
        else
        {
            sb.append(" ?ontology <");
            sb.append(OWL.VERSIONIRI.stringValue());
            sb.append("> ?version . ");
        }
        sb.append("OPTIONAL{ ?version <");
        sb.append(PoddRdfConstants.PODD_BASE_INFERRED_VERSION.stringValue());
        sb.append("> ?inferredVersion . ");
        sb.append(" }");
        sb.append("}");
        
        this.log.info("Generated SPARQL {}", sb);
        
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
                "SELECT ?x WHERE { ?x <" + RDF.TYPE + "> <" + OWL.ONTOLOGY.stringValue() + ">  . " + " ?x <"
                        + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT + "> ?y " + " }";
        this.log.info("Generated SPARQL {}", sparqlQuery);
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
            ontologyIRI = IRI.create(nextResult.getValue("x").stringValue());
        }
        return ontologyIRI;
    }
    
    /**
     * Retrieve a list of Top Objects that are contained in the given graphs.
     * 
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    @Override
    public List<PoddObjectLabel> getTopObjects(final InferredOWLOntologyID ontologyID,
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT ?topObjectUri ?topObjectLabel ?topObjectDescription ?artifactUri ");
        
        sb.append(" WHERE { ");
        
        sb.append(" ?artifactUri <" + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT.stringValue() + "> ?topObjectUri . \n");
        
        sb.append(" OPTIONAL {  ?topObjectUri <" + RDFS.LABEL.stringValue() + "> ?topObjectLabel . } \n");
        
        sb.append(" OPTIONAL {  ?topObjectUri <" + RDFS.COMMENT.stringValue() + "> ?topObjectDescription . } \n");
        
        sb.append(" }");
        
        final TupleQueryResult queryResults =
                SparqlQueryHelper.executeSparqlQuery(sb.toString(), repositoryConnection, contexts);
        
        final List<PoddObjectLabel> topObjectList = new ArrayList<PoddObjectLabel>();
        try
        {
            while(queryResults.hasNext())
            {
                final BindingSet next = queryResults.next();
                final URI pred = (URI)next.getValue("topObjectUri");
                String label = null;
                String description = null;
                
                if(next.getValue("topObjectLabel") != null)
                {
                    label = next.getValue("topObjectLabel").stringValue();
                }
                
                if(next.getValue("topObjectDescription") != null)
                {
                    description = next.getValue("topObjectDescription").stringValue();
                }
                
                final PoddObjectLabel poddObject = new PoddObjectLabelImpl(ontologyID, pred, label, description);
                
                topObjectList.add(poddObject);
            }
        }
        finally
        {
            queryResults.close();
        }
        return topObjectList;
    }
    
    /**
     * Internal helper method to retrieve the Top-Object IRI for a given ontology.
     * 
     */
    @Override
    public IRI getTopObjectIRI(final InferredOWLOntologyID ontologyIRI,
            final RepositoryConnection repositoryConnection, final URI context) throws OpenRDFException
    {
        // get ontology IRI from the RepositoryConnection using a SPARQL SELECT query
        final String sparqlQuery =
                "SELECT ?y WHERE { " + ontologyIRI.toQuotedString() + " <" + RDF.TYPE + "> <"
                        + OWL.ONTOLOGY.stringValue() + ">  . " + ontologyIRI.toQuotedString() + " <"
                        + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT + "> ?y " + " }";
        this.log.info("Generated SPARQL {}", sparqlQuery);
        final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        
        final DatasetImpl dataset = new DatasetImpl();
        dataset.addDefaultGraph(context);
        query.setDataset(dataset);
        
        IRI topObjectIRI = null;
        
        final TupleQueryResult queryResults = query.evaluate();
        if(queryResults.hasNext())
        {
            final BindingSet nextResult = queryResults.next();
            topObjectIRI = IRI.create(nextResult.getValue("y").stringValue());
        }
        return topObjectIRI;
    }
    
    /**
     * Retrieves the most specific type of the given object. The "type" itself is returned as a
     * PoddObject containing its URI, title and description.
     * 
     * Note: If multiple types are found, one is randomly returned. Including inferred statements is
     * likely to lead to multiple types being allocated.
     * 
     * @param objectUri
     *            The object whose type is to be determined
     * @param repositoryConnection
     * @param contexts
     *            The graphs in which to search for the object type.
     * @return
     * @throws OpenRDFException
     */
    @Override
    public List<URI> getObjectTypes(final InferredOWLOntologyID ontologyID, final URI objectUri,
            final RepositoryConnection repositoryConnection) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("SELECT ?poddTypeUri ?label ?description ");
        sb.append(" WHERE { ");
        sb.append(" ?objectUri <" + RDF.TYPE + "> ?poddTypeUri . ");
        sb.append(" OPTIONAL { ?poddTypeUri <" + RDFS.LABEL + "> ?label } . \n");
        sb.append(" OPTIONAL { ?poddTypeUri <" + RDFS.COMMENT + "> ?description . } \n");
        
        // filter out TYPE statements for OWL:Thing, OWL:Individual, OWL:NamedIndividual & OWL:Class
        sb.append("FILTER (?poddTypeUri != <" + OWL.THING.stringValue() + ">) ");
        sb.append("FILTER (?poddTypeUri != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
        sb.append("FILTER (?poddTypeUri != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
        sb.append("FILTER (?poddTypeUri != <" + OWL.CLASS.stringValue() + ">) ");
        
        sb.append(" }");
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        tupleQuery.setBinding("objectUri", objectUri);
        final TupleQueryResult queryResults = SparqlQueryHelper.executeSparqlQuery(tupleQuery, contexts);
        
        try
        {
            if(queryResults.hasNext())
            {
                final BindingSet next = queryResults.next();
                
                String label = null;
                String description = null;
                
                if(next.getValue("topObjectLabel") != null)
                {
                    label = next.getValue("topObjectLabel").stringValue();
                }
                
                if(next.getValue("topObjectDescription") != null)
                {
                    description = next.getValue("topObjectDescription").stringValue();
                }
                
                final PoddObjectLabel poddObject = new PoddObjectLabelImpl(ontologyID, objectUri, label, description);
                
                return poddObject;
            }
        }
        finally
        {
            queryResults.close();
        }
        return null;
    }
    
    @Override
    public boolean isPublished(final OWLOntologyID ontologyID, final RepositoryConnection repositoryConnection,
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
        final String sparqlQuery =
                "ASK { " + "?artifact <" + PoddRdfConstants.OWL_VERSION_IRI.stringValue() + "> "
                        + ontologyID.getVersionIRI().toQuotedString() + " . " + "?artifact <"
                        + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT.stringValue() + "> ?top ." + " ?top <"
                        + PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS.stringValue() + "> <"
                        + PoddRdfConstants.PODDBASE_PUBLISHED.stringValue() + ">" + " }";
        
        this.log.info("Generated SPARQL {}", sparqlQuery);
        
        final BooleanQuery booleanQuery = repositoryConnection.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlQuery);
        
        // Create a dataset to specify the contexts
        final DatasetImpl dataset = new DatasetImpl();
        dataset.addDefaultGraph(artifactGraphUri);
        dataset.addNamedGraph(artifactGraphUri);
        booleanQuery.setDataset(dataset);
        
        return booleanQuery.evaluate();
    }
    
    @Override
    public void setPublished(final IRI ontologyIRI, final RepositoryConnection repositoryConnection, final URI context)
        throws OpenRDFException
    {
        final IRI topObjectIRI = this.getTopObjectIRI(ontologyIRI, repositoryConnection, context);
        
        // remove previous value for publication status
        repositoryConnection.remove(topObjectIRI.toOpenRDFURI(), PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                null, context);
        
        // then insert the publication status as #Published
        repositoryConnection.add(topObjectIRI.toOpenRDFURI(), PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                PoddRdfConstants.PODDBASE_PUBLISHED, context);
        
        this.log.info("{} was set as Published", topObjectIRI);
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
        
        List<InferredOWLOntologyID> allOntologyVersions =
                getAllOntologyVersions(nextOntologyID.getOntologyIRI(), repositoryConnection, managementGraph);
        
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
                        List<Statement> previousInferredVersions =
                                Iterations.asList(repositoryConnection.getStatements(
                                        (URI)nextPreviousVersion.getObject(),
                                        PoddRdfConstants.PODD_BASE_INFERRED_VERSION, null, false, managementGraph));
                        
                        for(Statement nextInferredVersion : previousInferredVersions)
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
                                log.error("Found inferred version IRI that was not a URI: {}", nextInferredVersion);
                            }
                        }
                        
                        repositoryConnection.clear((URI)nextPreviousVersion.getObject());
                        repositoryConnection.remove((URI)nextPreviousVersion.getObject(), null, null, managementGraph);
                    }
                    else
                    {
                        log.error("Found version IRI that was not a URI: {}", nextPreviousVersion);
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
    
}
