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

import com.github.podd.api.file.SSHFileReference;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PoddRdfConstants;

/**
 * A simple implementation of an SSH File Reference object for use within PODD.
 * 
 * @author kutila
 */
public class SSHFileReferenceImpl implements SSHFileReference
{
    
    private OWLOntologyID artifactID;
    private String label;
    private IRI objectIri;
    private IRI parentIri;
    private String repositoryAlias;
    
    private String filename;
    private String path;
    private IRI parentPredicateIRI;
    
    /**
     * Constructor
     */
    public SSHFileReferenceImpl()
    {
    }
    
    @Override
    public OWLOntologyID getArtifactID()
    {
        return this.artifactID;
    }
    
    @Override
    public String getFilename()
    {
        return this.filename;
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
    public String getPath()
    {
        return this.path;
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
    public void setFilename(final String filename)
    {
        this.filename = filename;
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
    public void setPath(final String path)
    {
        this.path = path;
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
            OntologyUtils.ontologyIDToRDF(this.getArtifactID(), result);
        }
        
        if(this.getFilename() != null)
        {
            result.add(this.objectIri.toOpenRDFURI(), PoddRdfConstants.PODD_BASE_HAS_FILENAME,
                    vf.createLiteral(this.getFilename()));
        }
        
        if(this.getLabel() != null)
        {
            result.add(this.objectIri.toOpenRDFURI(), RDFS.LABEL, vf.createLiteral(this.getLabel()));
        }
        
        if(this.getPath() != null)
        {
            result.add(this.objectIri.toOpenRDFURI(), PoddRdfConstants.PODD_BASE_HAS_FILE_PATH,
                    vf.createLiteral(this.getPath()));
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
        b.append(this.filename);
        b.append(" , ");
        b.append(this.path);
        b.append(" , ");
        b.append(this.repositoryAlias);
        b.append("]");
        
        return b.toString();
    }
    
}
