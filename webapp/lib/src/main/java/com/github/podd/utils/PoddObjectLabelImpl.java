/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.URI;
import org.semanticweb.owlapi.model.IRI;

/**
 * Encapsulates the basic label and description metadata about an object into a single object.
 * 
 * This class must only ever be used to present precompiled sets of results to users. In other
 * cases, lists of {@link InferredOWLOntologyID}, {@link IRI} or {@link URI} are the correct way to
 * process information.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class PoddObjectLabelImpl implements PoddObjectLabel
{
    private InferredOWLOntologyID parentArtifactID;
    
    private URI objectID;
    
    private String label;
    
    private String description;
    
    /**
     * Creates a label object without a description
     * 
     * @param parent
     *            The base artifact that this label is applied to.
     * @param object
     *            The object inside of the given artifact that this label is for.
     * @param label
     *            The label for this object.
     */
    public PoddObjectLabelImpl(InferredOWLOntologyID parent, URI object, String label)
    {
        this.setParentArtifactID(parent);
        this.setObjectID(object);
        this.setLabel(label);
    }
    
    /**
     * Creates a label object with a description
     * 
     * @param parent
     *            The base artifact that this label is applied to.
     * @param object
     *            The object inside of the given artifact that this label is for.
     * @param label
     *            The label for this object.
     * @param description
     *            The description for this object.
     */
    public PoddObjectLabelImpl(InferredOWLOntologyID parent, URI object, String label, String description)
    {
        this(parent, object, label);
        this.setDescription(description);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.utils.PoddObjectLabel#getParentArtifactID()
     */
    @Override
    public InferredOWLOntologyID getParentArtifactID()
    {
        return parentArtifactID;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.utils.PoddObjectLabel#setParentArtifactID(com.github.podd.utils.
     * InferredOWLOntologyID)
     */
    @Override
    public void setParentArtifactID(InferredOWLOntologyID parentArtifactID)
    {
        this.parentArtifactID = parentArtifactID;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.utils.PoddObjectLabel#getObjectID()
     */
    @Override
    public URI getObjectID()
    {
        return objectID;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.utils.PoddObjectLabel#setObjectID(org.openrdf.model.URI)
     */
    @Override
    public void setObjectID(URI objectID)
    {
        this.objectID = objectID;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.utils.PoddObjectLabel#getLabel()
     */
    @Override
    public String getLabel()
    {
        return label;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.utils.PoddObjectLabel#setLabel(java.lang.String)
     */
    @Override
    public void setLabel(String label)
    {
        this.label = label;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.utils.PoddObjectLabel#getDescription()
     */
    @Override
    public String getDescription()
    {
        return description;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.utils.PoddObjectLabel#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }
    
}
