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
    public PoddClientException(String message)
    {
        super(message);
    }
    
    /**
     * @param cause
     */
    public PoddClientException(Throwable cause)
    {
        super(cause);
    }
    
    /**
     * @param message
     * @param cause
     */
    public PoddClientException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
}
