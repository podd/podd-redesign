package com.github.podd.api.purl;

import org.openrdf.model.URI;

/**
 * Encapsulates the information for a specific type of PoddPurlReference, including the URI that the
 * PURL replaces, if that information is available.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddPurlReference
{
    /**
     * 
     * @return The PURL URI for this Purl Reference object.
     */
    URI getPurlURI();
    
    /**
     * 
     * @return The temporary PURL URI, if that information is available.
     */
    URI getTemporaryURI();
}
