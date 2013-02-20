/**
 * 
 */
package com.github.podd.exception;

import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * An exception that is thrown when an artifact cannot be deleted for any reason.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DeleteArtifactException extends PoddException
{
    private static final long serialVersionUID = 7736119378934264560L;
    
    private final OWLOntologyID artifact;
    
    /**
     * @param msg
     */
    public DeleteArtifactException(final String msg, final OWLOntologyID artifact)
    {
        super(msg);
        this.artifact = artifact;
    }
    
    /**
     * @param msg
     * @param throwable
     */
    public DeleteArtifactException(final String msg, final Throwable throwable, final OWLOntologyID artifact)
    {
        super(msg, throwable);
        this.artifact = artifact;
    }
    
    /**
     * @param throwable
     */
    public DeleteArtifactException(final Throwable throwable, final OWLOntologyID artifact)
    {
        super(throwable);
        this.artifact = artifact;
    }
    
    /**
     * @return the artifact
     */
    public OWLOntologyID getArtifact()
    {
        return this.artifact;
    }
    
}
