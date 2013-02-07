package com.github.podd.ontology.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
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
    protected TupleQueryResult executeSparqlQuery(final String sparqlQuery, final RepositoryConnection repositoryConnection, 
            final URI... contexts) throws OpenRDFException
    {
        this.log.info("Executing SPARQL: \r\n {}", sparqlQuery);
        
        final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        
        final DatasetImpl dataset = new DatasetImpl();
        for (URI uri : contexts)
        {
            dataset.addDefaultGraph(uri);
        }
        query.setDataset(dataset);
        
        return query.evaluate();
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
     * Retrieve a list of Top Objects that are contained in the given graphs.
     * 
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    public List<String> getTopObjects(final RepositoryConnection repositoryConnection, final URI... contexts)
            throws OpenRDFException
    {
        List<String> topObjectList = new ArrayList<String>();
        final String sparqlQuery = "SELECT ?top WHERE { ?y <" + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT.stringValue() + "> ?top ." + " }";
        
        final TupleQueryResult queryResults = this.executeSparqlQuery(sparqlQuery, repositoryConnection, contexts);
        while(queryResults.hasNext())
        {
            final String pred = queryResults.next().getValue("top").stringValue();
            topObjectList.add(pred);
        }
        
        return topObjectList;
    }
            

    
    /**
     * Retrieve all objects in the given graphs that have a "contains" property or a sub-property of "contains"
     * pointed towards them.
     * 
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    public List<PoddObject> getContainedObjects(final URI parentObject, final RepositoryConnection repositoryConnection, final URI... contexts)
            throws OpenRDFException
    {
        final String sparqlQuery = "SELECT ?containsProperty ?containedObject ?label WHERE { \r\n" +
                "  <" + parentObject.stringValue() + "> ?containsProperty ?containedObject . \r\n"+
                "   ?containsProperty <" + RDFS.SUBPROPERTYOF + "> <" + PoddRdfConstants.PODDBASE_CONTAINS.stringValue() + "> . \r\n" +
                "  OPTIONAL { ?containsProperty <" + PoddRdfConstants.PODDBASE_WEIGHT.stringValue() + "> ?weight . } \r\n" +
                "   ?containedObject <" + RDFS.LABEL.stringValue() + "> ?label . \r\n" + 
                " } ORDER BY ASC(?weight) ASC(?label) ";

/*
 * Eg: use of weight to order
 * 
         SELECT * WHERE { 
                   ?y poddBase:hasTopObject ?objuri .  
                   ?objuri ?containsProperty ?x . 
                   ?containsProperty rdfs:subPropertyOf poddBase:contains .  
                   ?containsProperty poddBase:weight ?weight .  
                   ?x <" + RDFS.LABEL.stringValue() + "> ?label . 
                 } ORDER BY DESC(?weight)
*/
        
        
        final TupleQueryResult queryResults = this.executeSparqlQuery(sparqlQuery, repositoryConnection, contexts);
        
        final List<PoddObject> resultList = new ArrayList<PoddObject>();
        
        // populate HashMap with all details
        BindingSet nextResult = null;
        while(queryResults.hasNext())
        {
            nextResult = queryResults.next();
            PoddObject obj = new PoddObject((URI)nextResult.getValue("containedObject"));
            obj.setLabel(nextResult.getValue("label").stringValue());
            obj.setContainer(parentObject);
            
            resultList.add(obj);
        }
        return resultList;
    }
    
    /**
     * Retrieve statements about the "Top Object" (e.g. a Project).
     * It is expected that there is only one top object within the given graphs.
     * 
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    public Map<String, Object> getTopObjectDetails(final RepositoryConnection repositoryConnection, final URI... contexts)
            throws OpenRDFException
    {
        final String sparqlQuery = "SELECT ?top ?predicate ?obj WHERE { \r\n" +
        		"   ?y <" + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT.stringValue() + "> ?top . \r\n" + 
                "   ?top ?predicate ?obj . \r\n" + 
                " }";
        final TupleQueryResult queryResults = this.executeSparqlQuery(sparqlQuery, repositoryConnection, contexts);

        final Map<String, Object> topObjectDetailsMap = new ConcurrentHashMap<String, Object>();
        // populate HashMap with all details
        BindingSet nextResult = null;
        while(queryResults.hasNext())
        {
            nextResult = queryResults.next();
            final String pred = nextResult.getValue("predicate").stringValue();
            final String obj = nextResult.getValue("obj").stringValue();
            
            topObjectDetailsMap.put(pred, obj);
        }
        
        // finally add top object URI
        if (nextResult != null)
        {
            topObjectDetailsMap.put("objecturi", nextResult.getValue("top").stringValue());
        }
        
        return topObjectDetailsMap;
    }
    
}