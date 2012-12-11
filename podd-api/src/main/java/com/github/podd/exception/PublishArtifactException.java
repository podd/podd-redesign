/**
 * 
 */
package com.github.podd.exception;

import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PublishArtifactException extends PoddException
{
    private static final long serialVersionUID = -2281384497326519780L;
    
    private OWLOntologyID artifactId;
    
    /**
     * @param msg
     */
    public PublishArtifactException(final String msg, final OWLOntologyID artifactId)
    {
        super(msg);
        this.artifactId = artifactId;
    }
    
    /**
     * @param msg
     * @param throwable
     */
    public PublishArtifactException(final String msg, final Throwable throwable, final OWLOntologyID artifactId)
    {
        super(msg, throwable);
        this.artifactId = artifactId;
    }
    
    /**
     * @param throwable
     */
    public PublishArtifactException(final Throwable throwable, final OWLOntologyID artifactId)
    {
        super(throwable);
        this.artifactId = artifactId;
    }
    
    /**
     * 
     * @return The {@link OWLOntologyID} of the artifact that was not able to be published.
     */
    public OWLOntologyID getArtifactID()
    {
        return this.artifactId;
    }
}
