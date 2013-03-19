/**
 * 
 */
package com.github.podd.exception;

import java.util.Set;

import org.openrdf.model.URI;

/**
 * An exception indicating that one or more PODD objects (found inside an artifact) are no longer
 * connected to an eligible Top Object. 
 * 
 * @author kutila
 */
public class DisconnectedObjectException extends PoddException
{
    
    private static final long serialVersionUID = 1844520618633481527L;
    
    private final Set<URI> disconnectedObjects;
    
    public DisconnectedObjectException(final Set<URI> disconnectedObjects, final String msg)
    {
        super(msg);
        this.disconnectedObjects = disconnectedObjects;
    }
    
    public DisconnectedObjectException(final Set<URI> disconnectedObjects, final Throwable throwable)
    {
        super(throwable);
        this.disconnectedObjects = disconnectedObjects;
    }
    
    public DisconnectedObjectException(final Set<URI> disconnectedObjects, final Throwable throwable,
            final String msg)
    {
        super(msg, throwable);
        this.disconnectedObjects = disconnectedObjects;
    }
    
    public Set<URI> getDisconnectedObjects()
    {
        return this.disconnectedObjects;
    }
    
}
