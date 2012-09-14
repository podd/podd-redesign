/**
 * 
 */
package com.github.podd.prototype;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * Extends the OWLOntologyID class to provide for another variable to track the inferred ontology
 * IRI that matches this ontology ID.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class InferredOWLOntologyID extends OWLOntologyID
{
    private static final long serialVersionUID = 30402L;
    
    private IRI inferredOntologyIRI;
    
    /**
     * 
     */
    public InferredOWLOntologyID(final IRI baseOntologyIRI, final IRI baseOntologyVersionIRI,
            final IRI inferredOntologyIRI)
    {
        super(baseOntologyIRI, baseOntologyVersionIRI);
        
        this.inferredOntologyIRI = inferredOntologyIRI;
        
        // Override hashcode if inferredOntologyIRI is not null, otherwise leave the hashcode to
        // match upstream
        if(inferredOntologyIRI != null)
        {
            // super uses 37, so to be distinct we need to use a different prime number here, ie, 41
            this.hashCode += 41 * inferredOntologyIRI.hashCode();
        }
    }
    
    @Override
    public boolean equals(final Object obj)
    {
        // super must be equals or we cannot be equals, which also takes into account anonymous
        // ontologies
        if(!super.equals(obj))
        {
            return false;
        }
        
        // make sure that we can still be equal to OWLOntologyID instances if we did not get an
        // InferredOntologyIRI
        if(obj instanceof InferredOWLOntologyID)
        {
            final InferredOWLOntologyID other = (InferredOWLOntologyID)obj;
            if(this.inferredOntologyIRI == null)
            {
                if(other.inferredOntologyIRI == null)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if(other.inferredOntologyIRI == null)
            {
                return false;
            }
            else
            {
                return this.inferredOntologyIRI.equals(other.inferredOntologyIRI);
            }
        }
        else
        {
            // if the other object was not an InferredOWLOntogyID then we will only be equal if we
            // do not have an inferred ontologyIRI ourselves
            return this.inferredOntologyIRI == null;
        }
        
    }
    
    /**
     * @return the inferredOntologyIRI
     */
    public IRI getInferredOntologyIRI()
    {
        return this.inferredOntologyIRI;
    }
    
    @Override
    public String toString()
    {
        if(this.inferredOntologyIRI == null)
        {
            return super.toString();
        }
        else
        {
            return super.toString() + this.inferredOntologyIRI.toQuotedString();
        }
    }
    
}
