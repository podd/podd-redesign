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
