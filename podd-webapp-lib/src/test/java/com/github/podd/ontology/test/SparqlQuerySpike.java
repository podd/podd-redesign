package com.github.podd.ontology.test;

import java.util.Collections;
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
     * Find OWL:imports statements in the given graph of the repository.
     * 
     * Copied from PoddSesameManagerImpl.java as a reference only.
     */
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
    
    public Map<String, Object> getObjectList(final RepositoryConnection repositoryConnection, final URI context, final URI... namedContexts)
            throws OpenRDFException
    {
        final String sparqlQuery = "SELECT * WHERE { \r\n" +
                "   ?y <" + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT.stringValue() + "> ?topobjuri . \r\n" + 
                "   ?topobjuri <" + PoddRdfConstants.PODDBASE_CONTAINS.stringValue() + "> ?x . \r\n" + 
                "   ?x <" + RDFS.LABEL.stringValue() + "> ?label . \r\n" + 
                " }";
        
        this.log.info("Generated SPARQL: {}", sparqlQuery);
        final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        
        final DatasetImpl dataset = new DatasetImpl();
        dataset.addDefaultGraph(context);
        for (URI uri : namedContexts)
        {
            dataset.addDefaultGraph(uri);
        }
        query.setDataset(dataset);
        
        final Map<String, Object> topObjectDetailsMap = new ConcurrentHashMap<String, Object>();
        
        
        final TupleQueryResult queryResults = query.evaluate();

        // populate HashMap with all details
        BindingSet nextResult = null;
        while(queryResults.hasNext())
        {
            System.out.println("####");
            nextResult = queryResults.next();
            final String pred = nextResult.getValue("x").stringValue();
            final String obj = nextResult.getValue("label").stringValue();
            
            topObjectDetailsMap.put(pred, obj);
        }
        
        return topObjectDetailsMap;
    }
    
    /**
     * Retrieve statements about the "Top Object" (e.g. a Project).
     * It is expected that there is only one top object within the given graphs.
     * 
     * @param repositoryConnection
     * @param context
     * @param namedContexts
     * @return
     * @throws OpenRDFException
     */
    public Map<String, Object> getTopObjectDetails(final RepositoryConnection repositoryConnection, final URI context, final URI... namedContexts)
            throws OpenRDFException
    {
        final String sparqlQuery = "SELECT ?top ?predicate ?obj WHERE { \r\n" +
        		"   ?y <" + PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT.stringValue() + "> ?top . \r\n" + 
                "   ?top ?predicate ?obj . \r\n" + 
                " }";
        
        this.log.info("Generated SPARQL: {}", sparqlQuery);
        final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        
        final DatasetImpl dataset = new DatasetImpl();
        dataset.addDefaultGraph(context);
        for (URI uri : namedContexts)
        {
            dataset.addNamedGraph(uri);
        }
        query.setDataset(dataset);
        
        final Map<String, Object> topObjectDetailsMap = new ConcurrentHashMap<String, Object>();
        
        
        final TupleQueryResult queryResults = query.evaluate();

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