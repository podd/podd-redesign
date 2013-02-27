/**
 * 
 */
package com.github.podd.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.exception.EmptyOntologyException;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.UnmanagedSchemaException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.exception.UnmanagedSchemaOntologyIDException;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddObjectLabelImpl;
import com.github.podd.utils.SparqlQueryHelper;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddSchemaManagerImpl implements PoddSchemaManager
{
    
    private PoddRepositoryManager repositoryManager;
    private PoddSesameManager sesameManager;
    private PoddOWLManager owlManager;
    
    /**
     * 
     */
    public PoddSchemaManagerImpl()
    {
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void downloadSchemaOntology(final OWLOntologyID schemaOntologyID, final OutputStream outputStream,
            final RDFFormat format, final boolean includeInferences) throws UnmanagedSchemaException
    {
        throw new RuntimeException("TODO: Implement downloadSchemaOntology");
    }
    
    @Override
    public InferredOWLOntologyID getCurrentSchemaOntologyVersion(final IRI schemaOntologyIRI)
        throws UnmanagedSchemaIRIException
    {
        if(schemaOntologyIRI == null)
        {
            throw new UnmanagedSchemaIRIException(schemaOntologyIRI, "NULL is not a managed schema ontology");
        }
        throw new RuntimeException("TODO: Implement getCurrentSchemaOntologyVersion(IRI)");
    }
    
    @Override
    public OWLOntology getSchemaOntology(final IRI schemaOntologyIRI) throws UnmanagedSchemaIRIException
    {
        throw new RuntimeException("TODO: Implement getSchemaOntology(IRI)");
    }
    
    @Override
    public OWLOntology getSchemaOntology(final OWLOntologyID schemaOntologyID)
        throws UnmanagedSchemaOntologyIDException
    {
        throw new RuntimeException("TODO: Implement getSchemaOntology(OWLOntologyID)");
    }
    
    @Override
    public void setCurrentSchemaOntologyVersion(final OWLOntologyID schemaOntologyID)
        throws UnmanagedSchemaOntologyIDException, IllegalArgumentException
    {
        throw new RuntimeException("TODO: Implement setCurrentSchemaOntologyVersion");
    }
    
    @Override
    public void setOwlManager(final PoddOWLManager owlManager)
    {
        this.owlManager = owlManager;
    }
    
    @Override
    public void setRepositoryManager(final PoddRepositoryManager repositoryManager)
    {
        this.repositoryManager = repositoryManager;
    }
    
    @Override
    public void setSesameManager(final PoddSesameManager sesameManager)
    {
        this.sesameManager = sesameManager;
    }
    
    @Override
    public InferredOWLOntologyID uploadSchemaOntology(final InputStream inputStream, final RDFFormat fileFormat)
        throws OpenRDFException, IOException, OWLException, PoddException
    {
        return this.uploadSchemaOntology(null, inputStream, fileFormat);
    }
    
    @Override
    public InferredOWLOntologyID uploadSchemaOntology(final OWLOntologyID schemaOntologyID,
            final InputStream inputStream, final RDFFormat fileFormat) throws OpenRDFException, IOException,
        OWLException, PoddException
    {
        if(inputStream == null)
        {
            throw new NullPointerException("Schema Ontology input stream was null");
        }
        
        OWLOntologyDocumentSource owlSource = new StreamDocumentSource(inputStream, fileFormat.getDefaultMIMEType());
        OWLOntology ontology = this.owlManager.loadOntology(owlSource);
        
        if(ontology.isEmpty())
        {
            throw new EmptyOntologyException(ontology, "Schema Ontology contained no axioms");
        }
        
        if(schemaOntologyID != null)
        {
            // FIXME: Change OWLOntologyID to schemaOntologyID in this case
        }
        
        RepositoryConnection conn = null;
        
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            this.owlManager.dumpOntologyToRepository(ontology, conn);
            
            final InferredOWLOntologyID nextInferredOntology = this.owlManager.inferStatements(ontology, conn);
            
            conn.commit();
            
            // update the link in the schema ontology management graph
            this.sesameManager.updateCurrentManagedSchemaOntologyVersion(nextInferredOntology, true, conn,
                    this.repositoryManager.getSchemaManagementGraph());
            
            // update the link in the schema ontology management graph
            // TODO: This is probably not the right method for this purpose
            this.sesameManager.updateCurrentManagedSchemaOntologyVersion(nextInferredOntology, true, conn,
                    this.repositoryManager.getSchemaManagementGraph());
            
            return new InferredOWLOntologyID(ontology.getOntologyID().getOntologyIRI(), ontology.getOntologyID()
                    .getVersionIRI(), nextInferredOntology.getOntologyIRI());
        }
        catch(OpenRDFException | IOException e)
        {
            if(conn != null && conn.isActive())
            {
                conn.rollback();
            }
            
            throw e;
        }
        finally
        {
            if(conn != null && conn.isOpen())
            {
                conn.close();
            }
        }
        
    }
    
    /**
     * Spike method only.
     * 
     * Retrieves the cardinalities from the schema ontologies, for the given concept and property.
     * 
     * NOTE: does not work on "unqualified" cardinality statements yet.
     * 
     * @param objectUri
     * @param propertyUri
     * @param repositoryConnection
     * @param contexts
     * @return an integer array of size 3.
     * @throws OpenRDFException
     */
    @Override
    public Model getCardinality(final URI objectUri, final URI propertyUri, 
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        final int[] cardinalities = { -1, -1, -1 };
        
        final StringBuilder sb = new StringBuilder();
        
        sb.append("SELECT ?maxCardinality ?minCardinality ?qualifiedCardinality ");
        sb.append(" WHERE { ");
        
        sb.append(" ?poddObject <" + RDF.TYPE.stringValue() + "> ?poddConcept . ");
        sb.append(" ?poddConcept <" + RDFS.SUBCLASSOF.stringValue() + "> ?x . ");
        sb.append(" ?x <" + OWL.ONPROPERTY.stringValue() + "> ?propertyUri . ");
        sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#maxQualifiedCardinality> ?maxCardinality } . ");
        sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#minQualifiedCardinality> ?minCardinality } . ");
        sb.append(" OPTIONAL { ?x <http://www.w3.org/2002/07/owl#qualifiedCardinality> ?qualifiedCardinality } . ");
        
        sb.append(" } ");
        
        SparqlQueryHelper.log.info("Created SPARQL {}", sb.toString());
        
        final TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
        tupleQuery.setBinding("poddConcept", objectUri);
        tupleQuery.setBinding("propertyUri", propertyUri);
        final TupleQueryResult queryResults = SparqlQueryHelper.executeSparqlQuery(tupleQuery, contexts);
        
        try
        {
            if(queryResults.hasNext())
            {
                final BindingSet binding = queryResults.next();
                
                Value minCardinality = binding.getValue("minCardinality");
                if(minCardinality != null && minCardinality instanceof Literal)
                {
                    cardinalities[0] = ((Literal)minCardinality).intValue();
                }
                
                Value qualifiedCardinality = binding.getValue("qualifiedCardinality");
                if(qualifiedCardinality != null && qualifiedCardinality instanceof Literal)
                {
                    cardinalities[1] = ((Literal)qualifiedCardinality).intValue();
                }
                
                Value maxCardinality = binding.getValue("maxCardinality");
                if(maxCardinality != null && maxCardinality instanceof Literal)
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
     * Spike method.
     * 
     * {http://purl.org/podd/ns/poddScience#PlatformType}
     * <http://www.w3.org/2002/07/owl#equivalentClass> {_:genid1636663090} {_:genid1636663090}
     * <http://www.w3.org/2002/07/owl#oneOf> {_:genid72508669}
     * 
     * {_:genid72508669} <http://www.w3.org/1999/02/22-rdf-syntax-ns#first>
     * {http://purl.org/podd/ns/poddScience#Software} {_:genid72508669}
     * <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> {_:genid953844943}
     * 
     * {_:genid953844943} <http://www.w3.org/1999/02/22-rdf-syntax-ns#first>
     * {http://purl.org/podd/ns/poddScience#HardwareSoftware} {_:genid953844943}
     * <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> {_:genid278519207}
     * 
     * {_:genid278519207} <http://www.w3.org/1999/02/22-rdf-syntax-ns#first>
     * {http://purl.org/podd/ns/poddScience#Hardware} {_:genid278519207}
     * <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>
     * {http://www.w3.org/1999/02/22-rdf-syntax-ns#nil}
     * 
     * SELECT ?member WHERE { ?conceptUri :equivalentClass ?b0 . ?b0 :oneOf ?b1 . ?b1 rdf:rest * /
     * rdf:first ?member . }
     */
    @Override
    public List<URI> getAllValidMembers(final URI conceptUri,
            final RepositoryConnection repositoryConnection, final URI... contexts) throws OpenRDFException
    {
        List<PoddObjectLabel> results = new ArrayList<PoddObjectLabel>();
        
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
                
                String label = null;
                String description = null;
                
                if(memberLabel != null)
                {
                    label = memberLabel.stringValue();
                }
                if(memberDescription != null)
                {
                    description = memberDescription.stringValue();
                }
                
                PoddObjectLabel memberObject = new PoddObjectLabelImpl(null, (URI)member, label, description);
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
