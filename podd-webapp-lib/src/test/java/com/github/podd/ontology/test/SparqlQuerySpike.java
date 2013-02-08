package com.github.podd.ontology.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.utils.PoddObject;
import com.github.podd.utils.PoddRdfConstants;

/**
 * This class contains code to retrieve artifacts/objects (via SPARQL) for display purposes in the
 * HTML interface.
 * 
 * These implementations can then be copied on to PODD.
 * 
 * @author kutila
 * 
 */
public class SparqlQuerySpike
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
     * Helper method to execute a given SPARQL SELECT query.
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
     * Retrieve all objects in the given graphs that have a "contains" property or a sub-property of
     * "contains" pointed towards them.
     * 
     * @param parentObject
     * @param recursive
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    public List<PoddObject> getContainedObjects(final URI parentObject, final boolean recursive,
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        if(recursive)
        {
            throw new RuntimeException("Recursion not yet supported.");
        }
        
        final String sparqlQuery =
                "SELECT ?containsProperty ?containedObject ?containedObjectLabel WHERE { \r\n"
                        + "  ?parent ?containsProperty ?containedObject . \r\n" + "   ?containsProperty <"
                        + RDFS.SUBPROPERTYOF + "> <" + PoddRdfConstants.PODDBASE_CONTAINS.stringValue() + "> . \r\n"
                        + "  OPTIONAL { ?containsProperty <" + PoddRdfConstants.PODDBASE_WEIGHT.stringValue()
                        + "> ?weight . } \r\n" + "   ?containedObject <" + RDFS.LABEL.stringValue()
                        + "> ?containedObjectLabel . \r\n" + " } ORDER BY ASC(?weight) ASC(?containedObjectLabel) ";
        
        this.log.info("Executing SPARQL: \r\n {}", sparqlQuery);
        
        // TODO: 1. For recursing into descendant contained objects, use property paths - ?containsProperty+
        // NOTE: Cannot use property paths with variables
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        tupleQuery.setBinding("parent", parentObject);
        
        final TupleQueryResult queryResults = this.executeSparqlQuery(tupleQuery, contexts);
        
        final List<PoddObject> resultList = new ArrayList<PoddObject>();
        
        // populate HashMap with all details
        BindingSet nextResult = null;
        while(queryResults.hasNext())
        {
            nextResult = queryResults.next();
            final PoddObject obj = new PoddObject((URI)nextResult.getValue("containedObject"));
            obj.setLabel(nextResult.getValue("containedObjectLabel").stringValue());
            obj.setDirectParent(parentObject);
            obj.setRelationshipFromDirectParent(nextResult.getValue("containsProperty").stringValue());
            
            resultList.add(obj);
        }
        return resultList;
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
        while(queryResults.hasNext())
        {
            final BindingSet nextResult = queryResults.next();
            final String ontologyIRI = nextResult.getValue("x").stringValue();
            results.add(IRI.create(ontologyIRI));
            
        }
        return results;
    }
    
    /**
     * Retrieve statements about the "Top Object" (e.g. a Project).
     * 
     * This method expects that there is only one top object is present in the given graphs.
     * 
     * @param repositoryConnection
     * @param contexts
     * @return A Map containing all statements about the Top Object.
     * @throws OpenRDFException
     */
    public Map<String, Object> getTopObjectDetailsOld(final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        final String sparqlQuery =
                "SELECT ?top ?predicate ?value WHERE { \r\n" + "   ?artifact <"
                        + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT.stringValue() + "> ?top . \r\n"
                        + "   ?top ?predicate ?value . \r\n" + " }";
        final TupleQueryResult queryResults = this.executeSparqlQuery(sparqlQuery, repositoryConnection, contexts);
        
        final Map<String, Object> resultMap = new ConcurrentHashMap<String, Object>();
        
        BindingSet nextResult = null;
        while(queryResults.hasNext())
        {
            nextResult = queryResults.next();
            final String pred = nextResult.getValue("predicate").stringValue();
            final String obj = nextResult.getValue("value").stringValue();
            
            resultMap.put(pred, obj);
        }
        
        // finally add top object URI
        if(nextResult != null)
        {
            resultMap.put("objecturi", nextResult.getValue("top").stringValue());
        }
        return resultMap;
    }
    
    public Map<String, List<Object>> getTopObjectDetails(final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        final List<URI> topObject = this.getTopObjects(repositoryConnection, contexts);
        if(topObject.size() != 1)
        {
            throw new RuntimeException("Exactly 1 top object is required");
        }
        
        final Map<String, List<Object>> allStatements =
                this.getAllDirectStatements(topObject.get(0), repositoryConnection, contexts);
        final List<Object> list = new ArrayList<Object>();
        list.add(topObject.get(0));
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
    public Map<String, List<Object>> getAllDirectStatements(final URI objectUri,
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        final String sparqlQuery = "SELECT ?predicate ?value WHERE { ?poddObject ?predicate ?value .  }";
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        tupleQuery.setBinding("poddObject", objectUri);
        final TupleQueryResult queryResults = this.executeSparqlQuery(tupleQuery, contexts);
        
        final Map<String, List<Object>> resultMap = new ConcurrentHashMap<String, List<Object>>();
        
        while(queryResults.hasNext())
        {
            final BindingSet nextResult = queryResults.next();
            final String pred = nextResult.getValue("predicate").stringValue();
            if(!resultMap.containsKey(pred))
            {
                final List<Object> list = new ArrayList<Object>();
                list.add(nextResult.getValue("value"));
                resultMap.put(pred, list);
            }
            else
            {
                resultMap.get(pred).add(nextResult.getValue("value"));
            }
            
            System.out.println(pred + " = " + resultMap.get(pred));
        }
        
        return resultMap;
    }
    
    /**
     * Retrieve a list of Top Objects that are contained in the given graphs.
     * 
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    public List<URI> getTopObjects(final RepositoryConnection repositoryConnection, final URI... contexts)
        throws OpenRDFException
    {
        final List<URI> topObjectList = new ArrayList<URI>();
        final String sparqlQuery =
                "SELECT ?top WHERE { ?y <" + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT.stringValue() + "> ?top ." + " }";
        
        final TupleQueryResult queryResults = this.executeSparqlQuery(sparqlQuery, repositoryConnection, contexts);
        while(queryResults.hasNext())
        {
            final URI pred = (URI)queryResults.next().getValue("top");
            topObjectList.add(pred);
        }
        
        return topObjectList;
    }
    
}