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

    PoddPurlProcessorPrefixes(String temporaryPrefix)
    {
        this.temporaryPrefix = temporaryPrefix;
    }

    public String getTemporaryPrefix()
    {
        return temporaryPrefix;
    }
}
