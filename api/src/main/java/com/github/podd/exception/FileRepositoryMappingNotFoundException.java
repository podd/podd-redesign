/**
 * 
 */
package com.github.podd.exception;

import com.github.podd.api.file.PoddFileRepositoryManager;

/**
 * An exception that is thrown to indicate that there was no file repository mapping found for the
 * given alias.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class FileRepositoryMappingNotFoundException extends PoddException
{
    private static final long serialVersionUID = -6834156313829093766L;
    
    private final String alias;
    
    /**
     * 
     * @param alias
     *            The file repository alias that was not found in the
     *            {@link PoddFileRepositoryManager}.
     * @param msg
     *            The message for this exception.
     */
    public FileRepositoryMappingNotFoundException(final String alias, final String msg)
    {
        super(msg);
        this.alias = alias;
    }
    
    /**
     * @param alias
     *            The file repository alias that was not found in the
     *            {@link PoddFileRepositoryManager}.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public FileRepositoryMappingNotFoundException(final String alias, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.alias = alias;
    }
    
    /**
     * @param alias
     *            The file repository alias that was not found in the
     *            {@link PoddFileRepositoryManager}.
     * @param throwable
     *            The cause for this exception.
     */
    public FileRepositoryMappingNotFoundException(final String alias, final Throwable throwable)
    {
        super(throwable);
        this.alias = alias;
    }
    
    /**
     * @return The alias that was not found in the set of mappings in the
     *         {@link PoddFileRepositoryManager}.
     */
    public String getAlias()
    {
        return this.alias;
    }
    
}
