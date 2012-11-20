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
        return filename;
    }
    public void setFilename(String filename)
    {
        this.filename = filename;
    }
    public String getPath()
    {
        return path;
    }
    public void setPath(String path)
    {
        this.path = path;
    }
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
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
        StringBuilder b = new StringBuilder();
        b.append(super.toString());
        b.append(" path=");
        b.append(path);
        b.append(" filename=");
        b.append(filename);
        return b.toString();
    }
    
}
