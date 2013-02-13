/**
 * 
 */
package com.github.podd.exception;

import org.semanticweb.owlapi.model.IRI;

/**
 * An exception indicating that the given profile IRI was not found in the OWLAPI profile registry.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class ProfileNotFoundException extends PoddException
{
    private static final long serialVersionUID = 6247991932378540548L;
    
    private final IRI profileIRI;
    
    /**
     * 
     * @param profile
     *            The OWL profile IRI that was not found in the OWLAPI profile registry.
     * @param msg
     *            The message for this exception.
     */
    public ProfileNotFoundException(final IRI profile, final String msg)
    {
        super(msg);
        this.profileIRI = profile;
    }
    
    /**
     * @param profile
     *            The OWL profile IRI that was not found in the OWLAPI profile registry.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public ProfileNotFoundException(final IRI profile, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.profileIRI = profile;
    }
    
    /**
     * @param profile
     *            The OWL profile IRI that was not found in the OWLAPI profile registry.
     * @param throwable
     *            The cause for this exception.
     */
    public ProfileNotFoundException(final IRI profile, final Throwable throwable)
    {
        super(throwable);
        this.profileIRI = profile;
    }
    
    /**
     * 
     * @return The OWL profile IRI that was not found in the OWLAPI profile registry.
     */
    public IRI getProfile()
    {
        return this.profileIRI;
    }
    
}
