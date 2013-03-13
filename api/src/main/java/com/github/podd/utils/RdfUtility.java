/**
 * 
 */
package com.github.podd.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.resultio.helpers.QueryResultCollector;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kutila
 * 
 */
public class RdfUtility
{
    
    private final static Logger log = LoggerFactory.getLogger(RdfUtility.class);

    /*    
    private static final String[] SCHEMA_RESOURCE_PATHS = { PoddRdfConstants.PATH_PODD_DCTERMS,
            PoddRdfConstants.PATH_PODD_FOAF, PoddRdfConstants.PATH_PODD_USER, PoddRdfConstants.PATH_PODD_BASE,
            PoddRdfConstants.PATH_PODD_SCIENCE, PoddRdfConstants.PATH_PODD_PLANT,
            };
    
    private static final String[] SCHEMA_INFERRED_RESOURCE_PATHS = { "/test/ontologies/dcTermsInferred.rdf",
            "/test/ontologies/foafInferred.rdf", "/test/ontologies/poddUserInferred.rdf",
            "/test/ontologies/poddBaseInferred.rdf", "/test/ontologies/poddScienceInferred.rdf",
            "/test/ontologies/poddPlantInferred.rdf", };
     */    
    
    
    /**
     * Given an artifact, this method attempts to validate that all objects are connected in a
     * hierarchy to the top object.
     * 
     * @param inputStream
     *            Input stream containing the artifact statements
     * @param format
     *            The RDF format in which the statements are provided
     * @return True if the artifact is structurally valid, false otherwise
     */
    public static boolean validateArtifactStructure(final InputStream inputStream, RDFFormat format)
    {
        if(inputStream == null)
        {
            throw new NullPointerException("Input stream must not be null");
        }
        
        if(format == null)
        {
            format = RDFFormat.RDFXML;
        }
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:concrete:random");
        
        Repository tempRepository = null;
        RepositoryConnection connection = null;
        
        try
        {
            // create a temporary in-memory repository
            tempRepository = new SailRepository(new MemoryStore());
            tempRepository.initialize();
            connection = tempRepository.getConnection();
            connection.begin();
            
            // load artifact statements into repository
            connection.add(inputStream, "", format, context);
            //DebugUtils.printContents(connection, context);

            
            // - find artifact and top object URIs
            final StringBuilder sb1 = new StringBuilder();
            sb1.append("SELECT ?artifact ?topObject WHERE { ");
            sb1.append(" ?artifact <" + RDF.TYPE.stringValue() + "> <" + OWL.ONTOLOGY.stringValue() + "> .");
            sb1.append(" ?artifact <" + PoddRdfConstants.PODD_BASE + "artifactHasTopObject> ?topObject .");
            sb1.append(" } ");
            
            RdfUtility.log.info("Created SPARQL {} ", sb1.toString());
            
            final TupleQuery tupleQuery1 = connection.prepareTupleQuery(QueryLanguage.SPARQL, sb1.toString());
            final QueryResultCollector queryResults1 = executeSparqlQuery(tupleQuery1, context);
            
            int noOfResults = 0;
            URI topObject = null;
            URI artifactUri = null;
            for(final BindingSet binding : queryResults1.getBindingSets())
            {
                topObject = (URI)binding.getValue("topObject");
                artifactUri = (URI)binding.getValue("artifact");
                noOfResults++;
            }
            if(noOfResults != 1)
            {
                // should have exactly 1 top object
                return false;
            }
            
            // - get a list of podd objects
            final StringBuilder sb2 = new StringBuilder();
            sb2.append("SELECT ?poddObject WHERE { ");
            sb2.append(" OPTIONAL { ?poddObject <" + RDF.TYPE.stringValue() + "> <http://www.w3.org/2002/07/owl#NamedIndividual> . } ");
            sb2.append(" OPTIONAL { ?poddObject <" + RDF.TYPE.stringValue() + "> <" + OWL.INDIVIDUAL.stringValue() + "> . } ");
            sb2.append(" OPTIONAL { ?poddObject <" + RDF.TYPE.stringValue() + "> <" + OWL.THING.stringValue() + "> . } ");
            sb2.append("FILTER (?poddObject != ?artifact) ");
            sb2.append("FILTER (?poddObject != ?topObject) ");
            sb2.append(" } ");

            final TupleQuery tupleQuery2 = connection.prepareTupleQuery(QueryLanguage.SPARQL, sb2.toString());
            tupleQuery2.setBinding("artifact", artifactUri);
            tupleQuery2.setBinding("topObject", topObject);
            
            final QueryResultCollector queryResults2 = executeSparqlQuery(tupleQuery2, context);
            
            final List<URI> poddObjects = new ArrayList<URI>();
            for(final BindingSet binding : queryResults2.getBindingSets())
            {
                poddObjects.add((URI)binding.getValue("poddObject"));
            }

            log.info("{} poddObjects found.", poddObjects.size());

            
            //----------------- build contexts including schema ontologies 
            List<URI> contexts = new ArrayList<URI>();
            
            /*            
             //load schema ontologies and their pre-computed inferred statements into repository
            for(int i = 0; i < RdfUtility.SCHEMA_RESOURCE_PATHS.length; i++)
            {
                InferredOWLOntologyID ontologyID =
                        RdfUtility.loadOntologyFromResource(RdfUtility.SCHEMA_RESOURCE_PATHS[i],
                                RdfUtility.SCHEMA_INFERRED_RESOURCE_PATHS[i], RDFFormat.RDFXML, connection);
                contexts.add(ontologyID.getVersionIRI().toOpenRDFURI());
                contexts.add(ontologyID.getInferredOntologyIRI().toOpenRDFURI());
            }
            contexts.add(context);
             */
            
            for (URI poddObject : poddObjects)
            {
                // is there a path from the Top Object to this PODD Object?
                
                final StringBuilder sb3 = new StringBuilder();
                sb3.append("ASK { ");
                sb3.append(" ?topObject (");
                
                sb3.append("<" + PoddRdfConstants.PODDBASE_CONTAINS + ">|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasAnalysis>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasContainer>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasData>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasEnvironment>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasGene>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasGenotype>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasInvestigation>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasMaterial>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasPhenotype>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasPlatform>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasProcess>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasProjectPlan>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasProtocol>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasPublication>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasSequence>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasTreatment>|");
                sb3.append("<http://purl.org/podd/ns/poddScience#hasTreatmentMaterial>");
                
                sb3.append(")+ ?poddObject .");
                sb3.append(" } ");

                final BooleanQuery booleanQuery3 = connection.prepareBooleanQuery(QueryLanguage.SPARQL, sb3.toString());
                booleanQuery3.setBinding("poddObject", poddObject);
                booleanQuery3.setBinding("topObject", topObject);
                
                final DatasetImpl dataset = new DatasetImpl();
                for(final URI uri : contexts)
                {
                    dataset.addDefaultGraph(uri);
                }
                booleanQuery3.setDataset(dataset);
                boolean result = booleanQuery3.evaluate();
                if (result)
                {
                    log.info(" Connected <{}>", poddObject);
                }
                else
                {
                    log.info(" NOT connected <{}>", poddObject);
                }
            }
        }
        catch(final Exception e)
        {
            // better to throw an exception containing error details
            RdfUtility.log.error("An exception in validateArtifactConnectedness() ", e);
            return false;
        }
        finally
        {
            try
            {
                if(connection != null && connection.isOpen())
                {
                    connection.rollback();
                    connection.close();
                }
                tempRepository.shutDown();
            }
            catch(final Exception e)
            {
                RdfUtility.log.error("Exception while releasing resources", e);
            }
        }
        
        return true;
    }
    
    /**
     * NOTE: Copied from PoddSesameManagerImpl.java
     * 
     * Helper method to execute a given SPARQL Tuple query, which may have had bindings attached.
     * 
     * @param sparqlQuery
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    private static QueryResultCollector executeSparqlQuery(final TupleQuery sparqlQuery, final URI... contexts)
        throws OpenRDFException
    {
        final DatasetImpl dataset = new DatasetImpl();
        for(final URI uri : contexts)
        {
            dataset.addDefaultGraph(uri);
        }
        sparqlQuery.setDataset(dataset);
        
        QueryResultCollector results = new QueryResultCollector();
        QueryResults.report(sparqlQuery.evaluate(), results);
        
        return results;
    }
    
    
    
    /**
     * NOTE: Copied from AbstractPoddSesameManagerTest.java
     * 
     * Loads the statements in the specified resource paths as the asserted and inferred statements
     * of an ontology. The contexts to load into are identified from the <i>OWL:VersionIRI</i>
     * values in both files.
     * 
     * @param resourcePath
     *            Points to a resource containing asserted statements
     * @param inferredResourcePath
     *            Points to a resource containing the inferred statements
     * @param format
     *            The Format of both resources
     * @return An InferredOWLOntologyID for the loaded ontology
     * @throws Exception
     */
    private static InferredOWLOntologyID loadOntologyFromResource(final String resourcePath,
            final String inferredResourcePath, final RDFFormat format, final RepositoryConnection connection)
        throws Exception
    {
        final InputStream resourceStream = RdfUtility.class.getResourceAsStream(resourcePath);
        Assert.assertNotNull("Resource was null", resourceStream);
        
        // load statements into a Model
        final Model concreteModel = new LinkedHashModel();
        final RDFParser parser = Rio.createParser(format);
        parser.setRDFHandler(new StatementCollector(concreteModel));
        parser.parse(resourceStream, "");
        
        final Model inferredModel = new LinkedHashModel();
        if(inferredResourcePath != null)
        {
            final InputStream inferredResourceStream = RdfUtility.class.getResourceAsStream(inferredResourcePath);
            Assert.assertNotNull("Inferred resource was null", inferredResourceStream);
            
            // load inferred statements into a Model
            final RDFParser inferredParser = Rio.createParser(format);
            inferredParser.setRDFHandler(new StatementCollector(inferredModel));
            inferredParser.parse(inferredResourceStream, "");
            
            // extract version IRI which is also the inferred IRI
            connection.add(inferredModel, GraphUtil.getUniqueSubjectURI(inferredModel, RDF.TYPE, OWL.ONTOLOGY));
        }
        
        final URI ontologyURI = GraphUtil.getUniqueSubjectURI(concreteModel, RDF.TYPE, OWL.ONTOLOGY);
        RdfUtility.log.info("ontology URI: {}", ontologyURI);
        // dump the statements into the correct context of the Repository
        connection.add(concreteModel, GraphUtil.getUniqueObjectURI(concreteModel, ontologyURI, OWL.VERSIONIRI));
        
        final Model totalModel = new LinkedHashModel(concreteModel);
        totalModel.addAll(inferredModel);
        
        final Collection<InferredOWLOntologyID> results = OntologyUtils.modelToOntologyIDs(totalModel);
        Assert.assertEquals(1, results.size());
        
        return results.iterator().next();
    }
    
}
