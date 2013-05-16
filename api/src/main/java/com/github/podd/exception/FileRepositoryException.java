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
public abstract class FileRepositoryException extends PoddException
{
    
    private static final long serialVersionUID = 6527298790638967965L;
    
    public FileRepositoryException(final String msg)
    {
        super(msg);
    }
    
    public FileRepositoryException(final String msg, final Throwable throwable)
    {
        super(msg, throwable);
    }
    
    public FileRepositoryException(final Throwable throwable)
    {
        super(throwable);
    }
    
}
