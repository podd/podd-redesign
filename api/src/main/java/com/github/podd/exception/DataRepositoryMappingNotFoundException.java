/**
 * 
 */
package com.github.podd.exception;

/**
 * An exception that is thrown to indicate that there was no file repository mapping found for the
 * given alias.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DataRepositoryMappingNotFoundException extends DataRepositoryException
{
    private static final long serialVersionUID = -6834156313829093766L;
    
    private final String alias;
    
    /**
     * 
     * @param alias
     *            The file repository alias that was not found in the PoddFileRepositoryManager.
     * @param msg
     *            The message for this exception.
     */
    public DataRepositoryMappingNotFoundException(final String alias, final String msg)
    {
        super(msg);
        this.alias = alias;
    }
    
    /**
     * @param alias
     *            The file repository alias that was not found in the PoddFileRepositoryManager.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public DataRepositoryMappingNotFoundException(final String alias, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.alias = alias;
    }
    
    /**
     * @param alias
     *            The file repository alias that was not found in the PoddFileRepositoryManager.
     * @param throwable
     *            The cause for this exception.
     */
    public DataRepositoryMappingNotFoundException(final String alias, final Throwable throwable)
    {
        super(throwable);
        this.alias = alias;
    }
    
    /**
     * @return The alias that was not found in the set of mappings in the PoddFileRepositoryManager.
     */
    public String getAlias()
    {
        return this.alias;
    }
    
}
