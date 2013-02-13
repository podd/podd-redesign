/**
 * 
 */
package com.github.podd.prototype;

/**
 * This class represents a PODD File Reference to a resource accessible via SSH.
 * 
 * @author kutila
 * @created 19/11/2012
 */
public class SshFileReference extends FileReference
{
    
    private String filename;
    private String path;
    private String description;
    
    public String getFilename()
    {
        return this.filename;
    }
    
    public void setFilename(final String filename)
    {
        this.filename = filename;
    }
    
    public String getPath()
    {
        return this.path;
    }
    
    public void setPath(final String path)
    {
        this.path = path;
    }
    
    public String getDescription()
    {
        return this.description;
    }
    
    public void setDescription(final String description)
    {
        this.description = description;
    }
    
    @Override
    public boolean isFilled()
    {
        if(this.getArtifactUri() == null || this.getObjectUri() == null || this.getServerAlias() == null
                || this.getFilename() == null || this.getPath() == null)
        {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString()
    {
        final StringBuilder b = new StringBuilder();
        b.append(super.toString());
        b.append(" path=");
        b.append(this.path);
        b.append(" filename=");
        b.append(this.filename);
        return b.toString();
    }
    
}
