/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.URI;

/**
 * 
 * @author kutila
 * 
 */
public class PoddObject
{
    /** URI to identify the object */
    private URI uri;
    
    /** Title for this object */
    private String title;
    
    /** An optional description about the object */
    private String description;
    
    /**
     * Constructor
     * 
     * @param uri
     */
    public PoddObject(final URI uri)
    {
        this.uri = uri;
    }
    
    public URI getUri()
    {
        return this.uri;
    }
    
    public String getTitle()
    {
        return this.title;
    }
    
    public void setTitle(final String title)
    {
        this.title = title;
    }
    
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
    
    
}
