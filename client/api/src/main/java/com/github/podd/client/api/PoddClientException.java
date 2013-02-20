/**
 * 
 */
package com.github.podd.client.api;

/**
 * The base class of checked exceptions thrown by the PODD Client API.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddClientException extends Exception
{
    private static final long serialVersionUID = -2854524362938904344L;
    
    /**
     * 
     */
    public PoddClientException()
    {
        super();
    }
    
    /**
     * @param message
     */
    public PoddClientException(final String message)
    {
        super(message);
    }
    
    /**
     * @param message
     * @param cause
     */
    public PoddClientException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
    
    /**
     * @param cause
     */
    public PoddClientException(final Throwable cause)
    {
        super(cause);
    }
    
}
