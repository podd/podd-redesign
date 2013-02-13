package com.github.podd.prototype;

/**
 * Represents a reference to a file accessible as a web resource over HTTP
 * 
 * @author kutila
 * @created 2012/11/07
 */
public class HttpFileReference extends FileReference
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
