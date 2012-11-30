/**
 * 
 */
package com.github.podd.impl.purl;

import org.openrdf.model.URI;

import com.github.podd.api.purl.PoddPurlReference;

/**
 * A simple PoddPurlReference implementation that is immutable.
 * 
 * @author kutila
 *
 */
public class SimplePoddPurlReference implements PoddPurlReference
{

    private URI temporaryURI;
    private URI purlURI;
    
    /**
     * 
     * @param temporaryURI The temporary URI from which this Purl was generated
     * @param purlURI The permanent URI
     */
    public SimplePoddPurlReference(URI temporaryURI, URI purlURI)
    {
        this.temporaryURI = temporaryURI;
        this.purlURI = purlURI;
    }
    
    @Override
    public URI getPurlURI()
    {
        return this.purlURI;
    }

    @Override
    public URI getTemporaryURI()
    {
        return this.temporaryURI;
    }
    
}
