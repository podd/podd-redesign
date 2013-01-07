/**
 * 
 */
package com.github.podd.impl;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddSesameManager;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author kutila
 * 
 */
public class PoddSesameManagerImpl implements PoddSesameManager
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
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
        if(query1Results.hasNext())
        {
            final BindingSet nextResult = query1Results.next();
            final String nextVersionIRI = nextResult.getValue("cv").stringValue();
            final String nextInferredIRI = nextResult.getValue("civ").stringValue();
            
            return new InferredOWLOntologyID(ontologyIRI, IRI.create(nextVersionIRI), IRI.create(nextInferredIRI));
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
        if(queryResults2.hasNext())
        {
            final BindingSet nextResult = queryResults2.next();
            final String nextOntologyIRI = nextResult.getValue("x").stringValue();
            final String nextInferredIRI = nextResult.getValue("civ").stringValue();
            
            return new InferredOWLOntologyID(IRI.create(nextOntologyIRI), ontologyIRI, IRI.create(nextInferredIRI));
        }
        return null;
    }
    
}
