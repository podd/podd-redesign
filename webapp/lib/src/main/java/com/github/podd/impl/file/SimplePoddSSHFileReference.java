/**
 * 
 */
package com.github.podd.impl.file;

import org.semanticweb.owlapi.model.IRI;

import com.github.podd.api.file.PoddSSHFileReference;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * A simple implementation of an SSH File Reference object for use within PODD.
 * 
 * @author kutila
 */
public class SimplePoddSSHFileReference implements PoddSSHFileReference
{

    private InferredOWLOntologyID artifactID;
    private String label;
    private IRI objectIri;
    private IRI parentIri;
    private String repositoryAlias;
    
    private String filename;
    private String path;
    
    /**
     * Constructor
     */
    public SimplePoddSSHFileReference()
    {
    }
    
    @Override
    public InferredOWLOntologyID getArtifactID()
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
    public void setArtifactID(InferredOWLOntologyID artifactID)
    {
        this.artifactID = artifactID;
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
    
}
