package com.github.podd.exception;

/**
 * This class extends <code>java.lang.Exception</code> to provide a PODD specific checked exception
 * base class.
 * 
 * This exception class is abstract and cannot be directly instantiated.
 * 
 * @author kutila
 * @author Peter Ansell p_ansell@yahoo.com
 */
public abstract class PoddException extends Exception
{
    private static final long serialVersionUID = -6240755031638346731L;
    
    public PoddException(final String msg)
    {
        super(msg);
    }
    
    public PoddException(final String msg, final Throwable throwable)
    {
        super(msg, throwable);
    }
    
    public PoddException(final Throwable throwable)
    {
        super(throwable);
    }
    
}
