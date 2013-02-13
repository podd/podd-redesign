/**
 * 
 */
package com.github.podd.exception;

import com.github.podd.utils.PoddUser;

/**
 * This exception indicates that another <code>PoddUser</code> already exists in the system with the
 * same email address used.
 * 
 * @author kutila
 * 
 */
public class UserAlreadyExistsException extends PoddException
{
    
    private static final long serialVersionUID = -3233046860253247896L;
    
    private PoddUser user;
    
    /**
     * 
     * @param user
     *            The PODD user that already had another entry in the system.
     * @param msg
     *            The message for this exception.
     */
    public UserAlreadyExistsException(final PoddUser user, final String msg)
    {
        super(msg);
        this.user = user;
    }
    
    /**
     * 
     * @param user
     *            The PODD user that already had another entry in the system.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public UserAlreadyExistsException(final PoddUser user, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.user = user;
    }
    
    /**
     * 
     * @param user
     *            The PODD user that already had another entry in the system.
     * @param throwable
     *            The cause for this exception.
     */
    public UserAlreadyExistsException(final PoddUser user, final Throwable throwable)
    {
        super(throwable);
        this.user = user;
    }
    
    /**
     * 
     * @return The PODD user that already had another entry in the system.
     */
    public PoddUser getUser()
    {
        return this.user;
    }
}
