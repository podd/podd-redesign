/**
 * 
 */
package com.github.podd.exception;

import org.openrdf.model.URI;

import com.github.podd.api.PoddPurlGenerator;

/**
 * This exception indicates that a Purl Generator was not able to handle the generation of a
 * Permanent URL.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PurlGeneratorNotHandledException extends PoddException
{
    private static final long serialVersionUID = -8569720976338731517L;
    
    private final PoddPurlGenerator generator;
    private final URI inputUri;
    
    /**
     * 
     * @param generator
     *            The PURL Generator that was not able to handle the given input URI.
     * @param inputUri
     *            The input URI that failed to be handled by the given PURL Generator.
     * @param msg
     *            The message for this exception.
     */
    public PurlGeneratorNotHandledException(final PoddPurlGenerator generator, final URI inputUri, final String msg)
    {
        super(msg);
        this.generator = generator;
        this.inputUri = inputUri;
    }
    
    /**
     * @param generator
     *            The PURL Generator that was not able to handle the given input URI.
     * @param inputUri
     *            The input URI that failed to be handled by the given PURL Generator.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public PurlGeneratorNotHandledException(final PoddPurlGenerator generator, final URI inputUri, final String msg,
            final Throwable throwable)
    {
        super(msg, throwable);
        this.generator = generator;
        this.inputUri = inputUri;
    }
    
    /**
     * @param generator
     *            The PURL Generator that was not able to handle the given input URI.
     * @param inputUri
     *            The input URI that failed to be handled by the given PURL Generator.
     * @param throwable
     *            The cause for this exception.
     */
    public PurlGeneratorNotHandledException(final PoddPurlGenerator generator, final URI inputUri,
            final Throwable throwable)
    {
        super(throwable);
        this.generator = generator;
        this.inputUri = inputUri;
    }
    
    /**
     * @return The PURL Generator that was not able to handle the given input URI.
     */
    public PoddPurlGenerator getGenerator()
    {
        return this.generator;
    }
    
    /**
     * @return The input URI that failed to be handled by the given PURL Generator.
     */
    public URI getInputUri()
    {
        return this.inputUri;
    }
    
}
