package com.github.podd.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for working with {@link InferredOWLOntologyID}
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class OntologyUtils
{
    private static final Logger log = LoggerFactory.getLogger(OntologyUtils.class);
    
    /**
     * Extracts the {@link InferredOWLOntologyID} instances that are represented as RDF
     * {@link Statement}s in the given {@link Model}.
     * 
     * @param input
     *            The input model containing RDF statements.
     * @return A Collection of {@link InferredOWLOntologyID} instances derived from the statements
     *         in the model.
     */
    public static List<InferredOWLOntologyID> modelToOntologyIDs(final Model input)
    {
        final List<InferredOWLOntologyID> results = new ArrayList<InferredOWLOntologyID>();
        
        final Model typedOntologies = input.filter(null, RDF.TYPE, OWL.ONTOLOGY);
        
        for(final Statement nextTypeStatement : typedOntologies)
        {
            if(nextTypeStatement.getSubject() instanceof URI)
            {
                final Model versions = input.filter(nextTypeStatement.getSubject(), OWL.VERSIONIRI, null);
                
                for(final Statement nextVersion : versions)
                {
                    if(nextVersion.getObject() instanceof URI)
                    {
                        final Model inferredOntologies =
                                input.filter((URI)nextVersion.getObject(), PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                                        null);
                        
                        if(inferredOntologies.isEmpty())
                        {
                            // If there were no poddBase#inferredVersion statements, backup by
                            // trying to infer the versions using owl:imports
                            final Model importsOntologies = input.filter(null, OWL.IMPORTS, nextVersion.getObject());
                            
                            if(importsOntologies.isEmpty())
                            {
                                results.add(new InferredOWLOntologyID((URI)nextTypeStatement.getSubject(),
                                        (URI)nextVersion.getObject(), null));
                            }
                            else
                            {
                                for(final Statement nextImportOntology : importsOntologies)
                                {
                                    if(nextImportOntology.getSubject() instanceof URI)
                                    {
                                        results.add(new InferredOWLOntologyID((URI)nextTypeStatement.getSubject(),
                                                (URI)nextVersion.getObject(), (URI)nextImportOntology.getSubject()));
                                    }
                                    else
                                    {
                                        OntologyUtils.log.error("Found a non-URI import statement: {}",
                                                nextImportOntology);
                                    }
                                    
                                }
                            }
                        }
                        else
                        {
                            for(final Statement nextInferredOntology : inferredOntologies)
                            {
                                if(nextInferredOntology.getObject() instanceof URI)
                                {
                                    results.add(new InferredOWLOntologyID((URI)nextTypeStatement.getSubject(),
                                            (URI)nextVersion.getObject(), (URI)nextInferredOntology.getObject()));
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return results;
    }
    
    /**
     * Serialises the given collection of {@link InferredOWLOntologyID} objects to RDF, adding the
     * {@link Statement}s to the given {@link RDFHandler}.
     * <p>
     * This method wraps the serialisation from {@link InferredOWLOntologyID#toRDF()}.
     * 
     * @param input
     *            The collection of {@link InferredOWLOntologyID} objects to render to RDF.
     * @param handler
     *            The handler for handling the RDF statements.
     * @throws RDFHandlerException
     *             If there is an error while handling the statements.
     */
    public static void ontologyIDsToHandler(final Collection<InferredOWLOntologyID> input, final RDFHandler handler)
        throws RDFHandlerException
    {
        for(final InferredOWLOntologyID nextOntology : input)
        {
            for(final Statement nextStatement : nextOntology.toRDF())
            {
                handler.handleStatement(nextStatement);
            }
        }
    }
    
    /**
     * Serialises the given collection of {@link InferredOWLOntologyID} objects to RDF, adding the
     * {@link Statement}s to the given {@link Model}, or creating a new Model if the given model is
     * null.
     * <p>
     * This method wraps the serialisation from {@link InferredOWLOntologyID#toRDF()}.
     * 
     * @param input
     *            The collection of {@link InferredOWLOntologyID} objects to render to RDF.
     * @param result
     *            The Model to contain the resulting statements, or null to have one created
     *            internally
     * @param includeInferredOntologyStatements
     * @return A model containing the RDF statements about the given ontologies.
     * @throws RDFHandlerException
     *             If there is an error while handling the statements.
     */
    public static Model ontologyIDsToModel(final Collection<InferredOWLOntologyID> input, final Model result,
            boolean includeInferredOntologyStatements)
    {
        Model results = result;
        
        if(results == null)
        {
            results = new LinkedHashModel();
        }
        
        for(final InferredOWLOntologyID nextOntology : input)
        {
            ontologyIDToRDF(nextOntology, results, includeInferredOntologyStatements);
        }
        
        return results;
    }
    
    public static Model ontologyIDToRDF(final OWLOntologyID ontology, final Model result,
            boolean includeInferredOntologyStatements)
    {
        final ValueFactory vf = ValueFactoryImpl.getInstance();
        
        if(ontology.getOntologyIRI() != null)
        {
            result.add(vf.createStatement(ontology.getOntologyIRI().toOpenRDFURI(), RDF.TYPE, OWL.ONTOLOGY));
            if(ontology.getVersionIRI() != null)
            {
                result.add(vf.createStatement(ontology.getVersionIRI().toOpenRDFURI(), RDF.TYPE, OWL.ONTOLOGY));
                result.add(vf.createStatement(ontology.getOntologyIRI().toOpenRDFURI(), OWL.VERSIONIRI, ontology
                        .getVersionIRI().toOpenRDFURI()));
                if(includeInferredOntologyStatements && ontology instanceof InferredOWLOntologyID)
                {
                    final InferredOWLOntologyID inferredOntology = (InferredOWLOntologyID)ontology;
                    if(inferredOntology.getInferredOntologyIRI() != null)
                    {
                        result.add(vf.createStatement(inferredOntology.getInferredOntologyIRI().toOpenRDFURI(),
                                RDF.TYPE, OWL.ONTOLOGY));
                        result.add(vf.createStatement(inferredOntology.getVersionIRI().toOpenRDFURI(),
                                PoddRdfConstants.PODD_BASE_INFERRED_VERSION, inferredOntology.getInferredOntologyIRI()
                                        .toOpenRDFURI()));
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Extracts the {@link InferredOWLOntologyID} instances that are represented as RDF
     * {@link Statement}s in the given {@link String}.
     * 
     * @param string
     *            The input string containing RDF statements.
     * @param format
     *            The format of RDF statements in the string
     * @return A Collection of {@link InferredOWLOntologyID} instances derived from the statements
     *         in the string.
     * @throws OpenRDFException
     * @throws IOException
     */
    public static Collection<InferredOWLOntologyID> stringToOntologyID(final String string, final RDFFormat format)
        throws OpenRDFException, IOException
    {
        final Model model = Rio.parse(new StringReader(string), "", format);
        
        return OntologyUtils.modelToOntologyIDs(model);
    }
    
    private OntologyUtils()
    {
    }
    
    public static Model ontologyIDsToModel(List<InferredOWLOntologyID> input, Model result)
    {
        return ontologyIDsToModel(input, result, true);
    }
}
