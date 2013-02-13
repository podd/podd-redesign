/**
 * 
 */
package com.github.podd.exception;

/**
 * The superclass for exceptions that are generated because of unknown or unfound schema ontology
 * references. These include either IRI or OWLOntologyID, which are not compatible, so cannot be
 * directly referenced by this exception.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public abstract class UnmanagedSchemaException extends PoddException
{
    private static final long serialVersionUID = -2744644897176990922L;
    
    /**
     * @param msg
     */
    public UnmanagedSchemaException(final String msg)
    {
        super(msg);
    }
    
    /**
     * @param msg
     * @param throwable
     */
    public UnmanagedSchemaException(final String msg, final Throwable throwable)
    {
        super(msg, throwable);
    }
    
    /**
     * @param throwable
     */
    public UnmanagedSchemaException(final Throwable throwable)
    {
        super(throwable);
    }
    
}
