package com.github.podd.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.OWL;
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

import com.github.podd.exception.PoddRuntimeException;

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
     * For a non-recursive call, results are sorted based on poddBase:weight and label.
     * Recursive calls are first sorted by parent, weight and label. Parents themselves are sorted
     * by depth and their weight.
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
        
        final String sparqlQuery =
                "SELECT ?containsProperty ?containedObject ?containedObjectLabel WHERE { \r\n"
                        + "  ?parent ?containsProperty ?containedObject . \r\n" + "   ?containsProperty <"
                        + RDFS.SUBPROPERTYOF + "> <" + PoddRdfConstants.PODDBASE_CONTAINS.stringValue() + "> . \r\n"
                        + "  OPTIONAL { ?containsProperty <" + PoddRdfConstants.PODDBASE_WEIGHT.stringValue() + "> ?weight . } \r\n" 
                        + "   ?containedObject <" + RDFS.LABEL.stringValue() + "> ?containedObjectLabel . \r\n" 
                        + " } ORDER BY ASC(?weight) ASC(?containedObjectLabel) ";
        
        this.log.info("Executing SPARQL: \r\n {}", sparqlQuery);
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
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
                containedObject.setDirectParent(parentObject);
                containedObject.setRelationshipFromDirectParent(nextResult.getValue("containsProperty").stringValue());
                
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
     */
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
     * Retrieve statements about the "Top Object" (e.g. a PoddBase:Project).
     * 
     * This method expects that there is only one top object present in the given contexts.
     * 
     * @param repositoryConnection
     * @param contexts
     * @return A Map containing all statements about the Top Object.
     * @throws OpenRDFException
     */
    public Map<String, List<Value>> getTopObjectDetails(final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        final List<PoddObject> topObject = this.getTopObjects(repositoryConnection, contexts);
        if(topObject.size() != 1)
        {
            //this should never happen (unless PODD allows multiple top objects at some stage)
            throw new PoddRuntimeException("More than 1 top object found. There should be only 1.");
        }
        
        final Map<String, List<Value>> allStatements =
                this.getAllDirectStatements(topObject.get(0).getUri(), repositoryConnection, contexts);
        final List<Value> list = new ArrayList<Value>();
        list.add(topObject.get(0).getUri());
        allStatements.put("objecturi", list);
        return allStatements;
    }
    
    /**
     * Retrieve all direct statements about the given object.
     * 
     * @param objectUri
     * @param repositoryConnection
     * @param contexts
     * @return A Map containing all statements about the given object.
     * @throws OpenRDFException
     */
    public Map<String, List<Value>> getAllDirectStatements(final URI objectUri,
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        final String sparqlQuery = "SELECT ?predicate ?value WHERE { ?poddObject ?predicate ?value .  }";
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        tupleQuery.setBinding("poddObject", objectUri);
        final TupleQueryResult queryResults = this.executeSparqlQuery(tupleQuery, contexts);
        
        final Map<String, List<Value>> resultMap = new ConcurrentHashMap<String, List<Value>>();
        
        try
        {
            while(queryResults.hasNext())
            {
                final BindingSet nextResult = queryResults.next();
                final String pred = nextResult.getValue("predicate").stringValue();
                if(!resultMap.containsKey(pred))
                {
                    final List<Value> list = new ArrayList<Value>();
                    list.add(nextResult.getValue("value"));
                    resultMap.put(pred, list);
                }
                else
                {
                    resultMap.get(pred).add(nextResult.getValue("value"));
                }
            }
        }
        finally
        {
            queryResults.close();
        }
        
        return resultMap;
    }
    
    /**
     * topObjUri:   hasTopObjectStatus  ?status
     *              type                ?topObjectType
     *              label               ?title
     *              comment             ?description
     *              creator             ?creatorUri
     *              
     * 
     * 
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    public Map<String, String> getTopObjectDetailsAsModel(final RepositoryConnection repositoryConnection, final URI... contexts) 
            throws OpenRDFException
    {
        final List<PoddObject> topObject = this.getTopObjects(repositoryConnection, contexts);
        if(topObject.size() != 1)
        {
            //this should never happen (unless PODD allows multiple top objects at some stage)
            throw new PoddRuntimeException("More than 1 top object found. There should be only 1.");
        }
        URI objectUri = topObject.get(0).getUri();
        
        
        final String sparqlQuery = "CONSTRUCT { ?poddObject ?predicate ?value } WHERE { ?poddObject ?predicate ?value " +
        		"FILTER (?value != <" + OWL.THING.stringValue() + ">) " +
                "FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) " +
                "FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) " +
                "FILTER (?value != <" + OWL.CLASS.stringValue() + ">) " +
        		"}";
        
        final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sparqlQuery);
        graphQuery.setBinding("poddObject", objectUri);
        
        final GraphQueryResult queryResults = this.executeGraphQuery(graphQuery, contexts);

        Map<String, String> map = new HashMap<String, String>();
        LinkedHashModel result = new LinkedHashModel();
        while (queryResults.hasNext())
        {
            Statement stmt = queryResults.next();
            result.add(stmt);
            map.put(stmt.getPredicate().stringValue(), stmt.getObject().stringValue());
        }
        
        return map;
    }
    
    //FIXME: in progress here
    public LinkedHashModel getPoddObjectDetails(final URI objectUri, 
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        
        final String sparqlQuery = "CONSTRUCT { ?poddObject ?propertyUri ?value . " +
                " ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . " +
                " ?propertyUri <" + RDFS.RANGE.stringValue() + "> ?propertyDataType . " +
        	"} WHERE { " +
        		"?poddObject ?propertyUri ?value . " +
                "?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . " +
                "?propertyUri <" + RDFS.RANGE.stringValue() + "> ?propertyDataType . " +
                
                "FILTER (?value != <" + OWL.THING.stringValue() + ">) " +
                "FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) " +
                "FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) " +
                "FILTER (?value != <" + OWL.CLASS.stringValue() + ">) " +
                "}";
        
        final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sparqlQuery);
        graphQuery.setBinding("poddObject", objectUri);
        
        final GraphQueryResult queryResults = this.executeGraphQuery(graphQuery, contexts);

        LinkedHashModel result = new LinkedHashModel();
        while (queryResults.hasNext())
        {
            Statement stmt = queryResults.next();
            result.add(stmt);
        }

        return result;
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
        final List<PoddObject> topObjectList = new ArrayList<PoddObject>();
        final String sparqlQuery =
                "SELECT ?topObjectUri ?topObjectLabel ?topObjectDescription ?artifactUri WHERE {" +
                " ?artifactUri <" + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT.stringValue() + "> ?topObjectUri . \r\n"
                        + " OPTIONAL {  ?topObjectUri <" + RDFS.LABEL.stringValue() + "> ?topObjectLabel . } \r\n" 
                        + " OPTIONAL {  ?topObjectUri <" + RDFS.COMMENT.stringValue() + "> ?topObjectDescription . } \r\n" 
        
                        + " }";
        
        final TupleQueryResult queryResults = this.executeSparqlQuery(sparqlQuery, repositoryConnection, contexts);
        try
        {
            while(queryResults.hasNext())
            {
                BindingSet next = queryResults.next();
                final URI pred = (URI)next.getValue("topObjectUri");
                PoddObject poddObject = new PoddObject(pred);
                
                if (next.getValue("topObjectLabel") != null)
                {
                    poddObject.setTitle(next.getValue("topObjectLabel").stringValue());
                }
                
                if (next.getValue("topObjectDescription") != null)
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