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
    private URI directParent = null;
    
    private String relationshipFromDirectParent = null;
    
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
    
    public String getLabel()
    {
        return this.label;
    }
    
    public void setLabel(final String label)
    {
        this.label = label;
    }
    
    public URI getDirectParent()
    {
        return this.directParent;
    }
    
    public void setDirectParent(final URI container)
    {
        this.directParent = container;
    }
    
    public boolean hasDirectParent()
    {
        return (this.directParent != null);
    }
    
    public void setRelationshipFromDirectParent(final String relationshipFromDirectParent)
    {
        this.relationshipFromDirectParent = relationshipFromDirectParent;
    }
    
    public String getRelationshipFromDirectParent()
    {
        return this.relationshipFromDirectParent;
    }
}
