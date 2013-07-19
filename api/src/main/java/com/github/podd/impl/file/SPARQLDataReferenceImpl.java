/**
 * 
 */
package com.github.podd.impl.file;

import org.openrdf.model.Model;
import org.openrdf.model.vocabulary.RDF;

import com.github.podd.api.file.SPARQLDataReference;
import com.github.podd.utils.PoddRdfConstants;

/**
 * A simple implementation of a SPARQL Data Reference object for use within PODD.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class SPARQLDataReferenceImpl extends AbstractDataReferenceImpl implements SPARQLDataReference
{
    private String graph;
    
    /**
     * Constructor
     */
    public SPARQLDataReferenceImpl()
    {
        super();
    }
    
    @Override
    public String getGraph()
    {
        return this.graph;
    }
    
    @Override
    public void setGraph(final String filename)
    {
        this.graph = filename;
    }
    
    @Override
    public Model toRDF()
    {
        Model result = super.toRDF();
        
        result.add(this.getObjectIri().toOpenRDFURI(), RDF.TYPE, PoddRdfConstants.PODD_BASE_DATA_REFERENCE_TYPE_SPARQL);
        
        if(this.getGraph() != null)
        {
            result.add(this.getObjectIri().toOpenRDFURI(), PoddRdfConstants.PODD_BASE_HAS_SPARQL_GRAPH,
                    PoddRdfConstants.VF.createLiteral(this.getGraph()));
        }
        
        return result;
    }
    
    @Override
    public String toString()
    {
        final StringBuilder b = new StringBuilder();
        b.append("[");
        b.append(this.getArtifactID());
        b.append(" , ");
        b.append(this.getParentIri());
        b.append(" , ");
        b.append(this.getObjectIri());
        b.append(" , ");
        b.append(this.getLabel());
        b.append(" , ");
        b.append(this.getGraph());
        b.append(" , ");
        b.append(this.getRepositoryAlias());
        b.append("]");
        
        return b.toString();
    }
    
}
