package com.github.podd.utils;

import org.openrdf.model.URI;

/**
 * An immutable object designed solely to provide a wrapper for labels and descriptions that are
 * going to be immediately displayed.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddObjectLabel
{
    /**
     * @return the parentArtifactID
     */
    InferredOWLOntologyID getOntologyID();
    
    /**
     * @return the objectID
     */
    URI getObjectURI();
    
    /**
     * @return the label
     */
    String getLabel();
    
    /**
     * @return the description
     */
    String getDescription();
}