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
    private InferredOWLOntologyID ontologyID;
    
    private URI objectID;
    
    private String label;
    
    private String description;
    
    /**
     * Creates a label object without a description, and without an object, meaning that the label
     * applies to the ontology itself.
     * 
     * @param ontologyID
     *            The base artifact that this label is applied to.
     * @param label
     *            The label for this ontology.
     */
    public PoddObjectLabelImpl(InferredOWLOntologyID ontologyID, String label)
    {
        this.ontologyID = ontologyID;
        this.label = label;
    }
    
    /**
     * Creates a label object without a description, and without an object, meaning that the label
     * applies to the ontology itself.
     * 
     * @param ontologyID
     *            The base artifact that this label is applied to.
     * @param label
     *            The label for this ontology.
     */
    public PoddObjectLabelImpl(InferredOWLOntologyID ontologyID, String label, String description)
    {
        this(ontologyID, label);
        this.description = description;
    }
    
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
    public PoddObjectLabelImpl(InferredOWLOntologyID ontologyID, URI object, String label)
    {
        this(ontologyID, label);
        this.objectID = object;
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
        this.description = description;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.utils.PoddObjectLabel#getParentArtifactID()
     */
    @Override
    public InferredOWLOntologyID getOntologyID()
    {
        return ontologyID;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.utils.PoddObjectLabel#getObjectID()
     */
    @Override
    public URI getObjectURI()
    {
        return objectID;
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
     * @see com.github.podd.utils.PoddObjectLabel#getDescription()
     */
    @Override
    public String getDescription()
    {
        return description;
    }
}
