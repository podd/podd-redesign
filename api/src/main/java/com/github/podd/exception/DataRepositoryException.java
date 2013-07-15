/**
 * 
 */
package com.github.podd.exception;

/**
 * This class extends {@link PoddException} to provide an abstract exception base class for File
 * Repository related exceptions.
 * 
 * @author kutila
 */
public abstract class DataRepositoryException extends PoddException
{
    
    private static final long serialVersionUID = 6527298790638967965L;
    
    public DataRepositoryException(final String msg)
    {
        super(msg);
    }
    
    public DataRepositoryException(final String msg, final Throwable throwable)
    {
        super(msg, throwable);
    }
    
    public DataRepositoryException(final Throwable throwable)
    {
        super(throwable);
    }
    
}
