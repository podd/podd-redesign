package com.github.podd.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
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
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
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
    
    private static Logger log = LoggerFactory.getLogger(SparqlQueryHelper.class.getName());
    
    /**
     * Helper method to execute a given SPARQL SELECT query.
     * 
     * @param sparqlQuery
     * @param repositoryConnection
     * @param contexts
     * @return The
     * @throws OpenRDFException
     */
    protected static TupleQueryResult executeSparqlQuery(final String sparqlQuery,
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        SparqlQueryHelper.log.info("Executing SPARQL: \r\n {}", sparqlQuery);
        
        final TupleQuery query = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        
        return SparqlQueryHelper.executeSparqlQuery(query, contexts);
    }
    
    /**
     * Helper method to execute a given SPARQL Tuple query.
     * 
     * @param sparqlQuery
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    protected static TupleQueryResult executeSparqlQuery(final TupleQuery sparqlQuery, final URI... contexts)
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
    protected static GraphQueryResult executeGraphQuery(final GraphQuery sparqlQuery, final URI... contexts)
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
    public static List<PoddObject> getContainedObjects(final URI parentObject, final boolean recursive,
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT DISTINCT ?containsProperty ?containedObject ?containedObjectLabel ");
        
        sb.append(" WHERE { ");
        
        sb.append(" ?parent ?containsProperty ?containedObject . \n");
        
        sb.append(" ?containsProperty <" + RDFS.SUBPROPERTYOF + "> <"
                + PoddRdfConstants.PODDBASE_CONTAINS.stringValue() + "> . \n");
        
        sb.append(" OPTIONAL { ?containsProperty <" + PoddRdfConstants.PODDBASE_WEIGHT.stringValue()
                + "> ?weight . } \n");
        
        sb.append(" ?containedObject <" + RDFS.LABEL.stringValue() + "> ?containedObjectLabel . \n");
        
        sb.append(" } ORDER BY ASC(?weight) ASC(?containedObjectLabel) ");
        
        SparqlQueryHelper.log.info("Executing SPARQL: \r\n {}", sb.toString());
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        tupleQuery.setBinding("parent", parentObject);
        
        final TupleQueryResult queryResults = SparqlQueryHelper.executeSparqlQuery(tupleQuery, contexts);
        
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
                        SparqlQueryHelper.getContainedObjects(childUri, true, repositoryConnection, contexts);
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
    public static Set<IRI> getDirectImports(final RepositoryConnection repositoryConnection, final URI... contexts)
        throws OpenRDFException
    {
        final String sparqlQuery = "SELECT ?x WHERE { ?y <" + OWL.IMPORTS.stringValue() + "> ?x ." + " }";
        final TupleQueryResult queryResults =
                SparqlQueryHelper.executeSparqlQuery(sparqlQuery, repositoryConnection, contexts);
        
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
    public static List<URI> getPoddArtifactList(final RepositoryConnection repositoryConnection, final URI artifactGraph)
        throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT ?artifactUri ");
        sb.append(" WHERE { ");
        sb.append(" ?artifactUri <" + RDF.TYPE + ">  <" + OWL.ONTOLOGY + "> . ");
        sb.append(" ?artifactUri <" + PoddRdfConstants.PODD_BASE_INFERRED_VERSION + ">  ?infVersion . ");
        
        sb.append(" } ");
        
        SparqlQueryHelper.log.info("Executing SPARQL: \r\n {}", sb.toString());
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        
        final TupleQueryResult queryResults = SparqlQueryHelper.executeSparqlQuery(tupleQuery, artifactGraph);
        
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
    public static Model getPoddObjectDetails(final URI objectUri, final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("CONSTRUCT { ");
        sb.append(" ?poddObject ?propertyUri ?value . ");
        sb.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
        sb.append(" ?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel . ");
        
        sb.append("} WHERE {");
        
        sb.append(" ?poddObject ?propertyUri ?value . ");
        sb.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
        // value may not have a Label
        sb.append(" OPTIONAL {?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel } . ");
        
        sb.append(" FILTER (?value != <" + OWL.THING.stringValue() + ">) ");
        sb.append(" FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
        sb.append(" FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
        sb.append(" FILTER (?value != <" + OWL.CLASS.stringValue() + ">) ");
        
        sb.append("}");
        
        final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
        graphQuery.setBinding("poddObject", objectUri);
        
        final GraphQueryResult queryResults = SparqlQueryHelper.executeGraphQuery(graphQuery, contexts);
        
        final Model model = new TreeModel();
        
        while(queryResults.hasNext())
        {
            final Statement stmt = queryResults.next();
            model.add(stmt);
        }
        
        return model;
    }

    /**
     * Work in progress [25/02/2013]
     * 
     * Attempting to retrieve sufficient triples to display the object_edit page
     * 
     * @param objectUri
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    public static Model getPoddObjectDetailsForEdit(final URI objectUri, final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("CONSTRUCT { ");
        sb.append(" ?poddObject ?propertyUri ?value . ");
        sb.append(" ?propertyUri <" + RDF.TYPE.stringValue() + "> ?propertyType . ");
        sb.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
        sb.append(" ?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel . ");
        
        sb.append("} WHERE {");
        
        sb.append(" ?poddObject ?propertyUri ?value . ");
        sb.append(" ?propertyUri <" + RDF.TYPE.stringValue() + "> ?propertyType . ");
        sb.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
        // value may not have a Label
        sb.append(" OPTIONAL {?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel } . ");
        
        sb.append(" FILTER (?value != <" + OWL.THING.stringValue() + ">) ");
        sb.append(" FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
        sb.append(" FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
        sb.append(" FILTER (?value != <" + OWL.CLASS.stringValue() + ">) ");
        
        sb.append("}");
        
        SparqlQueryHelper.log.info("Created SPARQL {}", sb.toString());
        
        final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
        graphQuery.setBinding("poddObject", objectUri);
        
        final GraphQueryResult queryResults = SparqlQueryHelper.executeGraphQuery(graphQuery, contexts);
        
        final Model model = new TreeModel();
        
        while(queryResults.hasNext())
        {
            final Statement stmt = queryResults.next();
            model.add(stmt);
            System.out.println(stmt.getSubject() + "   [" + stmt.getPredicate() + "]   " + stmt.getObject());
        }
        
        return model;
    }
    
    
    /**
     * Retrieve a list of properties about the given object. The list is ordered based on property
     * weights.
     * 
     * RDF:TYPE, RDFS:COMMENT and RDFS:LABEL statements are omitted from the results. 
     * 
     * Note: If only asserted properties are required, the inferred ontology graph should not be
     * included in the <i>contexts</i> passed into this method.
     * 
     * @param objectUri
     * @param repositoryConnection
     * @param contexts
     * @return A Map containing all statements about the given object.
     * @throws OpenRDFException
     */
    public static List<URI> getDirectProperties(final URI objectUri, final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT DISTINCT ?propertyUri ");
        sb.append(" WHERE { ");
        sb.append(" ?poddObject ?propertyUri ?value . ");
        
        // for ORDER BY
        sb.append(" ?propertyUri <" + RDFS.LABEL.stringValue() + "> ?propertyLabel . ");
        
        // value may not have a Label
        sb.append(" OPTIONAL {?value <" + RDFS.LABEL.stringValue() + "> ?valueLabel } . ");
        
        // for ORDER BY
        sb.append("OPTIONAL { ?propertyUri <" + PoddRdfConstants.PODDBASE_WEIGHT.stringValue() + "> ?weight } . ");
        
        sb.append("FILTER (?value != <" + OWL.THING.stringValue() + ">) ");
        sb.append("FILTER (?value != <" + OWL.INDIVIDUAL.stringValue() + ">) ");
        sb.append("FILTER (?value != <http://www.w3.org/2002/07/owl#NamedIndividual>) ");
        sb.append("FILTER (?value != <" + OWL.CLASS.stringValue() + ">) ");

        // Exclude as TYPE, Label (title) and Comment (description) are displayed separately
        sb.append("FILTER (?propertyUri != <" + RDF.TYPE.stringValue() + ">) ");
        sb.append("FILTER (?propertyUri != <" + RDFS.LABEL.stringValue() + ">) ");
        sb.append("FILTER (?propertyUri != <" + RDFS.COMMENT.stringValue() + ">) ");
        
        sb.append(" } ");
        sb.append("  ORDER BY ASC(?weight) ASC(?propertyLabel) ");
        
        SparqlQueryHelper.log.info("Created SPARQL {}", sb.toString());
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        tupleQuery.setBinding("poddObject", objectUri);
        final TupleQueryResult queryResults = SparqlQueryHelper.executeSparqlQuery(tupleQuery, contexts);
        
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
    public static List<PoddObject> getTopObjects(final RepositoryConnection repositoryConnection, final URI... contexts)
        throws OpenRDFException
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
    
    /**
     * Retrieve a <code>PoddObject</code> containing the "title" and "description" of the specified
     * object if they exist.
     * 
     * If multiple titles/descriptions exist, the first pair of values returned by SPARQL will be
     * used.
     * 
     * @param objectUri
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    public static PoddObject getPoddObject(final URI objectUri, final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("SELECT ?label ?description ");
        sb.append(" WHERE { ");
        sb.append(" OPTIONAL { ?objectUri <" + RDFS.LABEL + "> ?label } . \n");
        sb.append(" OPTIONAL { ?objectUri <" + RDFS.COMMENT + "> ?description . } \n");
        sb.append(" }");
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        tupleQuery.setBinding("objectUri", objectUri);
        final TupleQueryResult queryResults = SparqlQueryHelper.executeSparqlQuery(tupleQuery, contexts);
        
        final PoddObject poddObject = new PoddObject(objectUri);
        try
        {
            if(queryResults.hasNext())
            {
                final BindingSet next = queryResults.next();
                
                if(next.getValue("label") != null)
                {
                    poddObject.setTitle(next.getValue("label").stringValue());
                }
                
                if(next.getValue("description") != null)
                {
                    poddObject.setDescription(next.getValue("description").stringValue());
                }
            }
        }
        finally
        {
            queryResults.close();
        }
        return poddObject;
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
    public static PoddObject getObjectType(final URI objectUri, final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
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
        
        PoddObject poddObject = new PoddObject(objectUri);
        try
        {
            if(queryResults.hasNext())
            {
                final BindingSet next = queryResults.next();
                
                poddObject = new PoddObject((URI)next.getValue("poddTypeUri"));
                
                if(next.getValue("label") != null)
                {
                    poddObject.setTitle(next.getValue("label").stringValue());
                }
                else
                {
                    poddObject.setTitle(poddObject.getUri().getLocalName());
                }
                
                if(next.getValue("description") != null)
                {
                    poddObject.setDescription(next.getValue("description").stringValue());
                }
            }
        }
        finally
        {
            queryResults.close();
        }
        return poddObject;
    }
    
    /**
     * Retrieves the list of contexts in which all PODD schema ontologies are stored.
     * 
     */
    public static List<URI> getSchemaOntologyGraphs()
    {
        return Arrays.asList(SparqlQueryHelper.tempSchemaGraphs);
    }
    
    private static URI[] tempSchemaGraphs = {
            ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/dcTerms/1"),
            ValueFactoryImpl.getInstance().createURI(
                    "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/dcTerms/1"),
            ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/foaf/1"),
            ValueFactoryImpl.getInstance().createURI(
                    "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/foaf/1"),
            ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddUser/1"),
            ValueFactoryImpl.getInstance().createURI(
                    "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddUser/1"),
            ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddBase/1"),
            ValueFactoryImpl.getInstance().createURI(
                    "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddBase/1"),
            ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddScience/1"),
            ValueFactoryImpl.getInstance().createURI(
                    "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/ns/version/poddScience/1"), };
    
    
    /**
     * Spike method only. 
     * 
     * Retrieves the cardinalities from the schema ontologies, for the given concept and property. 
     * 
     * NOTE: does not work on "unqualified" cardinality statements yet.
     * 
     * @param conceptUri
     * @param propertyUri
     * @param repositoryConnection
     * @param contexts
     * @return an integer array of size 3. 
     * @throws OpenRDFException
     */
    public static int[] spikeGetCardinality(final URI conceptUri, final URI propertyUri, 
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        final int[] cardinalities = {-1, -1, -1};
        
        final StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT ?maxCardinality ?minCardinality ?qualifiedCardinality ");
        sb.append(" WHERE { ");
        
        sb.append(" ?poddConcept <" + RDFS.SUBCLASSOF.stringValue() + "> ?x . ");
        sb.append(" ?x <" + OWL.ONPROPERTY.stringValue() + "> ?propertyUri . ");
        sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#maxQualifiedCardinality> ?maxCardinality } . ");
        sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#minQualifiedCardinality> ?minCardinality } . ");
        sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#qualifiedCardinality> ?qualifiedCardinality } . ");
        
        sb.append(" } ");
        
        SparqlQueryHelper.log.info("Created SPARQL {}", sb.toString());
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        tupleQuery.setBinding("poddConcept", conceptUri);
        tupleQuery.setBinding("propertyUri", propertyUri);
        final TupleQueryResult queryResults = SparqlQueryHelper.executeSparqlQuery(tupleQuery, contexts);
        
        try
        {
            if(queryResults.hasNext())
            {
                final BindingSet binding = queryResults.next();
                
                Value minCardinality = binding.getValue("minCardinality");
                if (minCardinality != null && minCardinality instanceof Literal)
                {
                    cardinalities[0] = ((Literal)minCardinality).intValue();
                }

                Value qualifiedCardinality = binding.getValue("qualifiedCardinality");
                if (qualifiedCardinality != null && qualifiedCardinality instanceof Literal)
                {
                    cardinalities[1] = ((Literal)qualifiedCardinality).intValue();
                }

                Value maxCardinality = binding.getValue("maxCardinality");
                if (maxCardinality != null && maxCardinality instanceof Literal)
                {
                    cardinalities[2] = ((Literal)maxCardinality).intValue();
                }
            }
        }
        finally
        {
            queryResults.close();
        }
        
        return cardinalities;
    }
    
    /*
     Spike method.  

   {http://purl.org/podd/ns/poddScience#PlatformType}   <http://www.w3.org/2002/07/owl#equivalentClass>  {_:genid1636663090}
   {_:genid1636663090}   <http://www.w3.org/2002/07/owl#oneOf>  {_:genid72508669}
   
   {_:genid72508669}   <http://www.w3.org/1999/02/22-rdf-syntax-ns#first>  {http://purl.org/podd/ns/poddScience#Software}
   {_:genid72508669}   <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>  {_:genid953844943}
 
   {_:genid953844943}   <http://www.w3.org/1999/02/22-rdf-syntax-ns#first>  {http://purl.org/podd/ns/poddScience#HardwareSoftware}
   {_:genid953844943}   <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>  {_:genid278519207}
   
   {_:genid278519207}   <http://www.w3.org/1999/02/22-rdf-syntax-ns#first>  {http://purl.org/podd/ns/poddScience#Hardware}
   {_:genid278519207}   <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>  {http://www.w3.org/1999/02/22-rdf-syntax-ns#nil}
 
 SELECT ?member WHERE
 {
     ?conceptUri :equivalentClass ?b0 .
     ?b0         :oneOf             ?b1 .
     ?b1    rdf:rest * / rdf:first ?member .     
 }
     */
    public static List<PoddObject> spikeGetPossibleValues(final URI conceptUri,  
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        List<PoddObject> results = new ArrayList<PoddObject>();
        
        final StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT ?member ?memberLabel ?memberDescription ");
        sb.append(" WHERE { ");
        
        sb.append(" ?poddConcept <" + OWL.EQUIVALENTCLASS.stringValue() + "> ?x . ");
        sb.append(" ?x <" + OWL.ONEOF.stringValue() + "> ?list . ");
        sb.append(" ?list <" + RDF.REST.stringValue() + ">*/<" + RDF.FIRST.stringValue() + "> ?member . ");
        sb.append(" OPTIONAL { ?member <" + RDFS.LABEL.stringValue() + "> ?memberLabel } . ");
        sb.append(" OPTIONAL { ?member <" + RDFS.COMMENT.stringValue() + "> ?memberDescription } . ");
        sb.append(" } ");
        
        SparqlQueryHelper.log.info("Created SPARQL {}", sb.toString());
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        tupleQuery.setBinding("poddConcept", conceptUri);
        final TupleQueryResult queryResults = SparqlQueryHelper.executeSparqlQuery(tupleQuery, contexts);

        try
        {
            while(queryResults.hasNext())
            {
                final BindingSet binding = queryResults.next();
                
                Value member = binding.getValue("member");
                Value memberLabel = binding.getValue("memberLabel");
                Value memberDescription = binding.getValue("memberDescription");
                
                PoddObject memberObject = new PoddObject((URI)member);
                if (memberLabel != null)
                {
                    memberObject.setTitle(memberLabel.stringValue());
                }
                if (memberDescription != null)
                {
                    memberObject.setDescription(memberDescription.stringValue());
                }
                results.add(memberObject);
            }
        }
        finally
        {
            queryResults.close();
        }
        
        return results;
    }
    
    
}