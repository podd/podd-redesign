/**
 * 
 */
package com.github.podd.impl.file;

import java.util.Arrays;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Value;
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
    public void setArtifactID(OWLOntologyID artifactID)
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
    public void setFilename(String filename)
    {
        this.filename = filename;
    }
    
    @Override
    public void setLabel(String label)
    {
        this.label = label;
    }
    
    @Override
    public void setObjectIri(IRI objectIri)
    {
        this.objectIri = objectIri;
    }
    
    @Override
    public void setParentIri(IRI parentIri)
    {
        this.parentIri = parentIri;
    }
    
    @Override
    public void setParentPredicateIRI(IRI parentPredicateIRI)
    {
        this.parentPredicateIRI = parentPredicateIRI;
    }
    
    @Override
    public void setPath(String path)
    {
        this.path = path;
    }
    
    @Override
    public void setRepositoryAlias(String repositoryAlias)
    {
        this.repositoryAlias = repositoryAlias;
    }
    
    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder();
        b.append("[");
        b.append(artifactID);
        b.append(" , ");
        b.append(parentIri);
        b.append(" , ");
        b.append(objectIri);
        b.append(" , ");
        b.append(label);
        b.append(" , ");
        b.append(filename);
        b.append(" , ");
        b.append(path);
        b.append(" , ");
        b.append(repositoryAlias);
        b.append("]");
        
        return b.toString();
    }
    
    @Override
    public Model toRDF()
    {
        ValueFactory vf = PoddRdfConstants.VALUE_FACTORY;
        Model result = new LinkedHashModel();
        
        if(getArtifactID() != null)
        {
            OntologyUtils.ontologyIDToRDF(getArtifactID(), result);
        }
        
        if(getFilename() != null)
        {
            result.add(this.objectIri.toOpenRDFURI(), PoddRdfConstants.PODD_BASE_HAS_FILENAME,
                    vf.createLiteral(getFilename()));
        }
        
        if(getLabel() != null)
        {
            result.add(this.objectIri.toOpenRDFURI(), RDFS.LABEL, vf.createLiteral(getLabel()));
        }
        
        if(getPath() != null)
        {
            result.add(this.objectIri.toOpenRDFURI(), PoddRdfConstants.PODD_BASE_HAS_FILE_PATH,
                    vf.createLiteral(getPath()));
        }
        
        if(getRepositoryAlias() != null)
        {
            result.add(this.objectIri.toOpenRDFURI(), PoddRdfConstants.PODD_BASE_HAS_ALIAS,
                    vf.createLiteral(getRepositoryAlias()));
        }
        
        if(getParentIri() != null && getParentPredicateIRI() != null)
        {
            result.add(getParentIri().toOpenRDFURI(), getParentPredicateIRI().toOpenRDFURI(),
                    this.objectIri.toOpenRDFURI());
        }
        
        return result;
    }
    
}
