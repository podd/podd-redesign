package com.github.podd.utils;

import org.openrdf.model.URI;

/**
 * Represents the valid states that a <code>PoddUser</code> can be in.
 * 
 * @author kutila
 * 
 */
public enum PoddUserStatus
{
    /**
     * An ACTIVE user has the ability to access the PODD system.
     */
    ACTIVE("Active", "http://purl.org/podd/user/status#active"),
    
    /**
     * An inactive user can be referenced from within PODD artifacts. Such a user does not have the
     * ability to access the PODD system.
     */
    INACTIVE("Inactive", "http://purl.org/podd/user/status#inactive")
    
    ;
    
    /**
     * Retrieve the {@link PoddUserStatus} matching the given {@link URI}.
     * 
     * @param label
     *            A String label to identify this PoddUserStatus
     * @param nextUri
     *            A URI to uniquely identify this PoddUserStatus
     * @return The Status matching the given URI or INACTIVE if no match found.
     */
    public static PoddUserStatus getUserStatusByUri(final URI nextUri)
    {
        for(final PoddUserStatus nextStatus : PoddUserStatus.values())
        {
            if(nextStatus.getURI().equals(nextUri))
            {
                return nextStatus;
            }
        }
        
        return INACTIVE;
    }
    
    private final String label;
    
    private final URI uri;
    
    private PoddUserStatus(final String label, final String uri)
    {
        this.label = label;
        this.uri = PoddRdfConstants.VF.createURI(uri);
    }
    
    public String getLabel()
    {
        return this.label;
    }
    
    public URI getURI()
    {
        return this.uri;
    }
    
}