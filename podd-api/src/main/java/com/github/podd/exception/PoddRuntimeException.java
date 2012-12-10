package com.github.podd.exception;

/**
 * This class extends <code>java.lang.RuntimeException</code> to provide a PODD specific Runtime
 * exception class.
 * 
 * @author kutila
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class PoddRuntimeException extends RuntimeException
{
    
    private static final long serialVersionUID = -1525497322722630581L;
    
    public PoddRuntimeException(final String msg)
    {
        super(msg);
    }
    
    public PoddRuntimeException(final String msg, final Throwable throwable)
    {
        super(msg, throwable);
    }
    
    public PoddRuntimeException(final Throwable throwable)
    {
        super(throwable);
    }
    
}
