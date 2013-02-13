/**
 * 
 */
package com.github.podd.exception;

import com.github.podd.utils.PoddUser;

/**
 * This exception indicates that an expected <code>PoddUser</code> was not present in the system.
 * 
 * @author kutila
 * 
 */
public class UserNotFoundException extends PoddException
{
    
    private static final long serialVersionUID = -2007155238930771189L;
    
    private PoddUser user;
    
    /**
     * 
     * @param user
     *            The PODD user that was not present in the system.
     * @param msg
     *            The message for this exception.
     */
    public UserNotFoundException(final PoddUser user, final String msg)
    {
        super(msg);
        this.user = user;
    }
    
    /**
     * 
     * @param user
     *            The PODD user that was not present in the system.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public UserNotFoundException(final PoddUser user, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.user = user;
    }
    
    /**
     * 
     * @param user
     *            The PODD user that was not present in the system.
     * @param throwable
     *            The cause for this exception.
     */
    public UserNotFoundException(final PoddUser user, final Throwable throwable)
    {
        super(throwable);
        this.user = user;
    }
    
    /**
     * 
     * @return The PODD user that was not present in the system.
     */
    public PoddUser getUser()
    {
        return this.user;
    }
    
}
