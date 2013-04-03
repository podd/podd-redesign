/**
 * 
 */
package com.github.podd.exception;

/**
 * An exception that is thrown to indicate that there a file repository mapping with the given alias
 * already exists.
 * 
 * @author kutila
 */
public class FileRepositoryMappingExistsException extends PoddException
{
    
    private static final long serialVersionUID = 3224280634543226410L;
    
    private final String alias;
    
    /**
     * 
     * @param alias
     *            The file repository alias that caused the Exception.
     * @param msg
     *            The message for this exception.
     */
    public FileRepositoryMappingExistsException(final String alias, final String msg)
    {
        super(msg);
        this.alias = alias;
    }
    
    /**
     * @param alias
     *            The file repository alias that caused the Exception.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public FileRepositoryMappingExistsException(final String alias, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.alias = alias;
    }
    
    /**
     * @param alias
     *            The file repository alias that caused the Exception.
     * @param throwable
     *            The cause for this exception.
     */
    public FileRepositoryMappingExistsException(final String alias, final Throwable throwable)
    {
        super(throwable);
        this.alias = alias;
    }
    
    /**
     * @return The file repository alias that caused the Exception.
     */
    public String getAlias()
    {
        return this.alias;
    }
    
}
