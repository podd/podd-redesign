/**
 * 
 */
package com.github.podd.exception;

import com.github.podd.utils.PoddUser;

/**
 * @author kutila
 * 
 */
public class InvalidUserDetailsException extends PoddException
{
    
    private static final long serialVersionUID = 6740075721656072394L;
    
    private PoddUser user;
    private String[] errorFields;
    
    /**
     * 
     * @param user
     *            The <code>PoddUser</code> object that had errors
     * @param errorFields
     *            The user fields that had errors
     * @param msg
     *            The message for this exception.
     */
    public InvalidUserDetailsException(final PoddUser user, final String[] errorFields, final String msg)
    {
        super(msg);
        this.user = user;
        this.errorFields = errorFields;
    }
    
    /**
     * 
     * @param user
     *            The <code>PoddUser</code> object that had errors
     * @param errorFields
     *            The user fields that had errors
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public InvalidUserDetailsException(final PoddUser user, final String[] errorFields, final String msg,
            final Throwable throwable)
    {
        super(msg, throwable);
        this.user = user;
        this.errorFields = errorFields;
    }
    
    /**
     * 
     * @param user
     *            The <code>PoddUser</code> object that had errors
     * @param errorFields
     *            The user fields that had errors
     * @param throwable
     *            The cause for this exception.
     */
    public InvalidUserDetailsException(final PoddUser user, final String[] errorFields, final Throwable throwable)
    {
        super(throwable);
        this.user = user;
        this.errorFields = errorFields;
    }
    
    /**
     * 
     * @return The <code>PoddUser</code> object that had errors
     */
    public PoddUser getUser()
    {
        return this.user;
    }
    
    /**
     * @return The user fields that had errors
     */
    public String[] getErrorFields()
    {
        return this.errorFields;
    }
    
}
