/**
 * 
 */
package com.github.podd.api.purl;

/**
 * @author kutila
 * 
 */
public enum PoddPurlProcessorPrefixes
{
    UUID("urn:temp:uuid:");
    
    private final String temporaryPrefix;
    
    PoddPurlProcessorPrefixes(final String temporaryPrefix)
    {
        this.temporaryPrefix = temporaryPrefix;
    }
    
    public String getTemporaryPrefix()
    {
        return this.temporaryPrefix;
    }
}
