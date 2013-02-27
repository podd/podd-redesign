package com.github.podd.utils;

import org.openrdf.model.URI;

public interface PoddObjectLabel
{
    
    /**
     * @return the parentArtifactID
     */
    public abstract InferredOWLOntologyID getParentArtifactID();
    
    /**
     * @param parentArtifactID
     *            the parentArtifactID to set
     */
    public abstract void setParentArtifactID(InferredOWLOntologyID parentArtifactID);
    
    /**
     * @return the objectID
     */
    public abstract URI getObjectID();
    
    /**
     * @param objectID
     *            the objectID to set
     */
    public abstract void setObjectID(URI objectID);
    
    /**
     * @return the label
     */
    public abstract String getLabel();
    
    /**
     * @param label
     *            the label to set
     */
    public abstract void setLabel(String label);
    
    /**
     * @return the description
     */
    public abstract String getDescription();
    
    /**
     * @param description
     *            the description to set
     */
    public abstract void setDescription(String description);
    
}