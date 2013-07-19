package com.github.podd.impl.file;

import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.api.file.DataReference;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PoddRdfConstants;

public abstract class AbstractDataReferenceImpl implements DataReference
{
    
    private OWLOntologyID artifactID;
    private String label;
    private IRI objectIri;
    private IRI parentIri;
    private String repositoryAlias;
    /**
     * Defaults to http://purl.org/podd/ns/poddBase#hasDataReference
     */
    private IRI parentPredicateIRI = IRI.create(PoddRdfConstants.PODD_BASE_HAS_DATA_REFERENCE);
    
    public AbstractDataReferenceImpl()
    {
        super();
    }
    
    @Override
    public OWLOntologyID getArtifactID()
    {
        return this.artifactID;
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
    public void setRepositoryAlias(final String repositoryAlias)
    {
        this.repositoryAlias = repositoryAlias;
    }
    
    @Override
    public Model toRDF()
    {
        if(this.getParentIri() == null)
        {
            throw new IllegalStateException("Parent IRI must not be null for a data reference");
        }
        
        if(this.getRepositoryAlias() == null)
        {
            throw new IllegalStateException("Repository alias must not be null for a data reference");
        }
        
        // Setup a temporary URI if it is not setup explicitly
        if(this.getObjectIri() == null)
        {
            this.setObjectIri(IRI.create("urn:temp:uuid:datareference:" + this.getClass().getSimpleName() + ":"));
        }
        
        // Override users who set the parent predicate IRI to null using the default
        if(this.getParentPredicateIRI() == null)
        {
            this.setParentPredicateIRI(IRI.create(PoddRdfConstants.PODD_BASE_HAS_DATA_REFERENCE));
        }
        
        final Model result = new LinkedHashModel();
        
        result.add(this.getParentIri().toOpenRDFURI(), this.getParentPredicateIRI().toOpenRDFURI(), this.getObjectIri()
                .toOpenRDFURI());
        result.add(this.getObjectIri().toOpenRDFURI(), RDF.TYPE, PoddRdfConstants.PODD_BASE_DATA_REFERENCE_TYPE);
        result.add(this.getObjectIri().toOpenRDFURI(), RDF.TYPE, PoddRdfConstants.PODD_BASE_DATA_REFERENCE_TYPE_SPARQL);
        result.add(this.getObjectIri().toOpenRDFURI(), PoddRdfConstants.PODD_BASE_HAS_ALIAS,
                PoddRdfConstants.VF.createLiteral(this.getRepositoryAlias()));
        
        if(this.getArtifactID() != null)
        {
            OntologyUtils.ontologyIDToRDF(this.getArtifactID(), result, false);
        }
        
        if(this.getLabel() != null)
        {
            result.add(this.getObjectIri().toOpenRDFURI(), RDFS.LABEL,
                    PoddRdfConstants.VF.createLiteral(this.getLabel()));
        }
        
        return result;
    }
}