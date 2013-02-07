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
    
    /** Label for this object */
    private String label;
    
    /** The parent of this object */
    private URI container;
    
    /**
     * Constructor
     * 
     * @param uri
     */
    public PoddObject(URI uri)
    {
        this.uri = uri;
    }
    
    public URI getUri()
    {
        return uri;
    }
    
    public String getLabel()
    {
        return label;
    }
    
    public URI getContainer()
    {
        return container;
    }
    
    public void setLabel(String label)
    {
        this.label = label;
    }
    
    public void setContainer(URI container)
    {
        this.container = container;
    }
    
    public boolean hasContainer()
    {
        return (this.container != null);
    }
}
