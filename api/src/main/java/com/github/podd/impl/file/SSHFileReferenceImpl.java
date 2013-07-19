/**
 * 
 */
package com.github.podd.impl.file;

import org.openrdf.model.Model;

import com.github.podd.api.file.SSHFileReference;
import com.github.podd.utils.PoddRdfConstants;

/**
 * A simple implementation of an SSH File Reference object for use within PODD.
 * 
 * @author kutila
 */
public class SSHFileReferenceImpl extends AbstractDataReferenceImpl implements SSHFileReference
{
    
    private String filename;
    private String path;
    
    /**
     * Constructor
     */
    public SSHFileReferenceImpl()
    {
        super();
    }
    
    @Override
    public String getFilename()
    {
        return this.filename;
    }
    
    @Override
    public String getPath()
    {
        return this.path;
    }
    
    @Override
    public void setFilename(final String filename)
    {
        this.filename = filename;
    }
    
    @Override
    public void setPath(final String path)
    {
        this.path = path;
    }
    
    @Override
    public Model toRDF()
    {
        final Model result = super.toRDF();
        
        if(this.getFilename() != null)
        {
            result.add(this.getObjectIri().toOpenRDFURI(), PoddRdfConstants.PODD_BASE_HAS_FILENAME,
                    PoddRdfConstants.VF.createLiteral(this.getFilename()));
        }
        
        if(this.getPath() != null)
        {
            result.add(this.getObjectIri().toOpenRDFURI(), PoddRdfConstants.PODD_BASE_HAS_FILE_PATH,
                    PoddRdfConstants.VF.createLiteral(this.getPath()));
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
        b.append(this.filename);
        b.append(" , ");
        b.append(this.path);
        b.append(" , ");
        b.append(this.getRepositoryAlias());
        b.append("]");
        
        return b.toString();
    }
    
}
