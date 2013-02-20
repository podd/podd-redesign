package com.github.podd.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains code to retrieve artifacts/objects (via SPARQL) for display purposes in the
 * HTML interface.
 * 
 * These implementations can then be copied on to PODD.
 * 
 * TODO: This class should be hidden behind the PODD API manager classes
 * 
 * @author kutila
 * 
 */
public class SparqlQueryHelper
{
    
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Helper method to execute a given SPARQL SELECT query.
     * 
     * @param sparqlQuery
     * @param repositoryConnection
     * @param contexts
     * @return The
     * @throws OpenRDFException
     */
    protected TupleQueryResult executeSparqlQuery(final String sparqlQuery,
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
    protected TupleQueryResult executeSparqlQuery(final TupleQuery sparqlQuery, final URI... contexts)
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
    protected GraphQueryResult executeGraphQuery(final GraphQuery sparqlQuery, final URI... contexts)
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
     * Retrieve all objects with which the given object has a "contains" property or a sub-property
     * of "contains".
     * 
     * NOTE on sorting of results:
     * 
     * For a non-recursive call, results are sorted based on poddBase:weight and label. Recursive
     * calls are first sorted by parent, weight and label. Parents themselves are sorted by depth
     * and their weight.
     * 
     * @param parentObject
     *            The object whose contained "children" are searched for.
     * @param recursive
     *            If false, only returns immediate contained objects. If true, this method is
     *            recursively called to obtain all descendants.
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    public List<PoddObject> getContainedObjects(final URI parentObject, final boolean recursive,
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT ?containsProperty ?containedObject ?containedObjectLabel ");
        
        sb.append(" WHERE { ");
        
        sb.append(" ?parent ?containsProperty ?containedObject . \n");
        
        sb.append(" ?containsProperty <" + RDFS.SUBPROPERTYOF + "> <"
                + PoddRdfConstants.PODDBASE_CONTAINS.stringValue() + "> . \n");
        
        sb.append(" OPTIONAL { ?containsProperty <" + PoddRdfConstants.PODDBASE_WEIGHT.stringValue()
                + "> ?weight . } \n");
        
        sb.append(" ?containedObject <" + RDFS.LABEL.stringValue() + "> ?containedObjectLabel . \n");
        
        sb.append(" } ORDER BY ASC(?weight) ASC(?containedObjectLabel) ");
        
        this.log.info("Executing SPARQL: \r\n {}", sb.toString());
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        tupleQuery.setBinding("parent", parentObject);
        
        final TupleQueryResult queryResults = this.executeSparqlQuery(tupleQuery, contexts);
        
        final List<PoddObject> children = new ArrayList<PoddObject>();
        final List<URI> childURIs = new ArrayList<URI>();
        try
        {
            while(queryResults.hasNext())
            {
                final BindingSet nextResult = queryResults.next();
                
                final PoddObject containedObject = new PoddObject((URI)nextResult.getValue("containedObject"));
                containedObject.setTitle(nextResult.getValue("containedObjectLabel").stringValue());
                
                children.add(containedObject);
                childURIs.add(containedObject.getUri());
            }
        }
        finally
        {
            queryResults.close();
        }
        
        // NOTE: recursive as SPARQL doesn't allow property paths when predicates are variables
        if(recursive)
        {
            for(final URI childUri : childURIs)
            {
                final List<PoddObject> descendantList =
                        this.getContainedObjects(childUri, true, repositoryConnection, contexts);
                children.addAll(descendantList);
            }
        }
        
        return children;
    }
    
    /**
     * Find OWL:imports statements in the given graph of the repository.
     * 
     * Copied from PoddSesameManagerImpl.java as a reference only.
     * 
     * @deprecated Not Used. Delete if unnecessary
     */
    @Deprecated
    public Set<IRI> getDirectImports(final RepositoryConnection repositoryConnection, final URI... contexts)
        throws OpenRDFException
    {
        final String sparqlQuery = "SELECT ?x WHERE { ?y <" + OWL.IMPORTS.stringValue() + "> ?x ." + " }";
        final TupleQueryResult queryResults = this.executeSparqlQuery(sparqlQuery, repositoryConnection, contexts);
        
        final Set<IRI> results = Collections.newSetFromMap(new ConcurrentHashMap<IRI, Boolean>());
        try
        {
            while(queryResults.hasNext())
            {
                final BindingSet nextResult = queryResults.next();
                final String ontologyIRI = nextResult.getValue("x").stringValue();
                results.add(IRI.create(ontologyIRI));
                
            }
        }
        finally
        {
            queryResults.close();
        }
        return results;
    }
    
    /**
     * This method retrieves a list of URIs of the artifacts currently managed by PODD.
     * 
     * @param repositoryConnection
     * @param artifactGraph
     * @return
     * @throws OpenRDFException
     */
    public List<URI> getPoddArtifactList(final RepositoryConnection repositoryConnection, final URI artifactGraph)
        throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT ?artifactUri ");
        sb.append(" WHERE { ");
        sb.append(" ?artifactUri <" + RDF.TYPE + ">  <" + OWL.ONTOLOGY + "> . ");
        sb.append(" ?artifactUri <" + PoddRdfConstants.PODD_BASE_INFERRED_VERSION + ">  ?infVersion . ");
        
        sb.append(" } ");
        
        this.log.info("Executing SPARQL: \r\n {}", sb.toString());
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        
        final TupleQueryResult queryResults = this.executeSparqlQuery(tupleQuery, artifactGraph);
        
        final List<URI> artifacts = new ArrayList<URI>();
        try
        {
            while(queryResults.hasNext())
            {
                final BindingSet nextResult = queryResults.next();
                artifacts.add((URI)nextResult.getValue("artifactUri"));
            }
        }
        finally
        {
            queryResults.close();
        }
        
        return artifacts;
    }
    
    /**
     * The result of this method is a Model containing all data required for displaying the details
     * of the object in HTML+RDFa.
     * 
     * objectUri :propertyX value, propertyX RDFS:Label "label", propertyX RDFS:Range "XSD:String"
     * 
     * @param objectUri
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    public Model getPoddObjectDetails(final URI objectUri, final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("CONSTRUCT { ?poddObject ?propertyUri ?value . ");
        
        sb.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
        
        sb.append(" ?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel . ");
        
        sb.append("} WHERE {");
        
        sb.append("?poddObject ?propertyUri ?value . ");
        
        sb.append("?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
        
        // datatype may not be available directly as Range
        sb.append("OPTIONAL {?propertyUri <" + RDFS.RANGE.stringValue() + "> ?propertyDataType } . ");
        
        // value may not have a Label
        sb.append("OPTIONAL {?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel } . ");
        
        sb.append("FILTER (?value != <" + OWL.THING.stringValue() + ">) ");
        
        sb.append("FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
        
        sb.append("FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
        
        sb.append("FILTER (?value != <" + OWL.CLASS.stringValue() + ">) ");
        
        sb.append("}");
        
        final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
        graphQuery.setBinding("poddObject", objectUri);
        
        final GraphQueryResult queryResults = this.executeGraphQuery(graphQuery, contexts);
        
        final Model model = new TreeModel();
        
        while(queryResults.hasNext())
        {
            final Statement stmt = queryResults.next();
            model.add(stmt);
        }
        
        return model;
    }
    
    /**
     * Retrieve a list of properties about the given object. The list is ordered based on property
     * weights.
     * 
     * @param objectUri
     * @param repositoryConnection
     * @param contexts
     * @return A Map containing all statements about the given object.
     * @throws OpenRDFException
     */
    public List<URI> getProperties(final URI objectUri, final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT DISTINCT ?propertyUri ");
        
        sb.append(" WHERE { ");
        
        sb.append("?poddObject ?propertyUri ?value . ");
        
        sb.append("?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
        
        sb.append("OPTIONAL {?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel } . ");
        
        // for ORDER BY
        sb.append("OPTIONAL { ?propertyUri <" + PoddRdfConstants.PODDBASE_WEIGHT.stringValue() + "> ?weight } . ");
        
        sb.append("FILTER (?value != <" + OWL.THING.stringValue() + ">) ");
        
        sb.append("FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
        
        sb.append("FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
        
        sb.append("FILTER (?value != <" + OWL.CLASS.stringValue() + ">) ");
        
        sb.append(" } ");
        
        sb.append("  ORDER BY ASC(?weight) ASC(?propertyLabel) ");
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        tupleQuery.setBinding("poddObject", objectUri);
        final TupleQueryResult queryResults = this.executeSparqlQuery(tupleQuery, contexts);
        
        final List<URI> resultList = new ArrayList<URI>();
        try
        {
            while(queryResults.hasNext())
            {
                final Value property = queryResults.next().getValue("propertyUri");
                if(property instanceof URI)
                {
                    resultList.add((URI)property);
                }
            }
        }
        finally
        {
            queryResults.close();
        }
        
        return resultList;
    }
    
    /**
     * Retrieve a list of Top Objects that are contained in the given graphs.
     * 
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    public List<PoddObject> getTopObjects(final RepositoryConnection repositoryConnection, final URI... contexts)
        throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT ?topObjectUri ?topObjectLabel ?topObjectDescription ?artifactUri ");
        
        sb.append(" WHERE { ");
        
        sb.append(" ?artifactUri <" + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT.stringValue() + "> ?topObjectUri . \n");
        
        sb.append(" OPTIONAL {  ?topObjectUri <" + RDFS.LABEL.stringValue() + "> ?topObjectLabel . } \n");
        
        sb.append(" OPTIONAL {  ?topObjectUri <" + RDFS.COMMENT.stringValue() + "> ?topObjectDescription . } \n");
        
        sb.append(" }");
        
        final TupleQueryResult queryResults = this.executeSparqlQuery(sb.toString(), repositoryConnection, contexts);
        
        final List<PoddObject> topObjectList = new ArrayList<PoddObject>();
        try
        {
            while(queryResults.hasNext())
            {
                final BindingSet next = queryResults.next();
                final URI pred = (URI)next.getValue("topObjectUri");
                final PoddObject poddObject = new PoddObject(pred);
                
                if(next.getValue("topObjectLabel") != null)
                {
                    poddObject.setTitle(next.getValue("topObjectLabel").stringValue());
                }
                
                if(next.getValue("topObjectDescription") != null)
                {
                    poddObject.setDescription(next.getValue("topObjectDescription").stringValue());
                }
                
                topObjectList.add(poddObject);
            }
        }
        finally
        {
            queryResults.close();
        }
        return topObjectList;
    }
    
}