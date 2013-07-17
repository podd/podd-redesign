/**
 * 
 */
package com.github.podd.impl.file;

import org.openrdf.model.Model;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDFS;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.api.file.SPARQLDataReference;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PoddRdfConstants;

/**
 * A simple implementation of a SPARQL Data Reference object for use within PODD.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class SPARQLDataReferenceImpl implements SPARQLDataReference
{
    
    private OWLOntologyID artifactID;
    private String label;
    private IRI objectIri;
    private IRI parentIri;
    private String repositoryAlias;
    
    private String graph;
    private String endpointURL;
    private IRI parentPredicateIRI;
    
    /**
     * Constructor
     */
    public SPARQLDataReferenceImpl()
    {
    }
    
    @Override
    public OWLOntologyID getArtifactID()
    {
        return this.artifactID;
    }
    
    @Override
    public String getGraph()
    {
        return this.graph;
    }
    
    @Override
    public String getLabel()
    {
        return this.label;
    }
    
    @Override
    public IRI getObjectIri()
    {
        return this.objectIri;
    }
    
    @Override
    public IRI getParentIri()
    {
        return this.parentIri;
    }
    
    @Override
    public IRI getParentPredicateIRI()
    {
        return this.parentPredicateIRI;
    }
    
    @Override
    public String getEndpointURL()
    {
        return this.endpointURL;
    }
    
    @Override
    public String getRepositoryAlias()
    {
        return this.repositoryAlias;
    }
    
    @Override
    public void setArtifactID(final OWLOntologyID artifactID)
    {
        if(artifactID instanceof InferredOWLOntologyID)
        {
            this.artifactID = ((InferredOWLOntologyID)artifactID).getBaseOWLOntologyID();
        }
        else
        {
            this.artifactID = artifactID;
        }
    }
    
    @Override
    public void setGraph(final String filename)
    {
        this.graph = filename;
    }
    
    @Override
    public void setLabel(final String label)
    {
        this.label = label;
    }
    
    @Override
    public void setObjectIri(final IRI objectIri)
    {
        this.objectIri = objectIri;
    }
    
    @Override
    public void setParentIri(final IRI parentIri)
    {
        this.parentIri = parentIri;
    }
    
    @Override
    public void setParentPredicateIRI(final IRI parentPredicateIRI)
    {
        this.parentPredicateIRI = parentPredicateIRI;
    }
    
    @Override
    public void setEndpointURL(final String path)
    {
        this.endpointURL = path;
    }
    
    @Override
    public void setRepositoryAlias(final String repositoryAlias)
    {
        this.repositoryAlias = repositoryAlias;
    }
    
    @Override
    public Model toRDF()
    {
        final ValueFactory vf = PoddRdfConstants.VF;
        final Model result = new LinkedHashModel();
        
        if(this.getArtifactID() != null)
        {
            OntologyUtils.ontologyIDToRDF(this.getArtifactID(), result, false);
        }
        
        if(this.getGraph() != null)
        {
            result.add(this.objectIri.toOpenRDFURI(), PoddRdfConstants.PODD_BASE_HAS_SPARQL_GRAPH,
                    vf.createLiteral(this.getGraph()));
        }
        
        if(this.getLabel() != null)
        {
            result.add(this.objectIri.toOpenRDFURI(), RDFS.LABEL, vf.createLiteral(this.getLabel()));
        }
        
        if(this.getEndpointURL() != null)
        {
            result.add(this.objectIri.toOpenRDFURI(), PoddRdfConstants.PODD_BASE_HAS_SPARQL_ENDPOINT,
                    vf.createLiteral(this.getEndpointURL()));
        }
        
        if(this.getRepositoryAlias() != null)
        {
            result.add(this.objectIri.toOpenRDFURI(), PoddRdfConstants.PODD_BASE_HAS_ALIAS,
                    vf.createLiteral(this.getRepositoryAlias()));
        }
        
        if(this.getParentIri() != null && this.getParentPredicateIRI() != null)
        {
            result.add(this.getParentIri().toOpenRDFURI(), this.getParentPredicateIRI().toOpenRDFURI(),
                    this.objectIri.toOpenRDFURI());
        }
        
        return result;
    }
    
    @Override
    public String toString()
    {
        final StringBuilder b = new StringBuilder();
        b.append("[");
        b.append(this.artifactID);
        b.append(" , ");
        b.append(this.parentIri);
        b.append(" , ");
        b.append(this.objectIri);
        b.append(" , ");
        b.append(this.label);
        b.append(" , ");
        b.append(this.graph);
        b.append(" , ");
        b.append(this.endpointURL);
        b.append(" , ");
        b.append(this.repositoryAlias);
        b.append("]");
        
        return b.toString();
    }
    
}
