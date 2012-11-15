/**
 * 
 */
package com.github.podd.api;

import java.util.List;

import org.openrdf.model.URI;

import com.github.podd.exception.InvalidUserDetailsException;
import com.github.podd.exception.UserAlreadyExistsException;
import com.github.podd.exception.UserNotFoundException;
import com.github.podd.utils.AbstractPoddUser;
import com.github.podd.utils.PoddUser;

/**
 * This Manager handles adding new users to the PODD system as retrieving users and updating
 * existing users.
 * 
 * @author kutila
 */
public interface PoddUserManager
{
    
    /**
     * Adds a new user to the PODD system.
     * 
     * @param user
     * @throws InvalidUserDetailsException
     *             If the provided user details have errors
     * @throws UserAlreadyExistsException
     *             If a user already exists with the provided email address
     */
    void addPoddUser(PoddUser user) throws InvalidUserDetailsException, UserAlreadyExistsException;
    
    /**
     * Retrieve the <code>PoddUser</code> with the given email address.
     * 
     * @param email
     * @return The <code>PoddUser</code> if such a user exists, NULL otherwise.
     */
    PoddUser getPoddUserByEmail(String email);
    
    /**
     * Retrieve the <code>PoddUser</code> with the given URI.
     * 
     * @param uri
     * @return The <code>PoddUser</code> if such a user exists, NULL otherwise.
     */
    PoddUser getPoddUserByURI(URI uri);
    
    /**
     * Retrieves records of all current users registered with this instance of PODD.
     * 
     * @return A <code>List</code> of the current set of PODD users or an empty list if no users
     *         exist.
     */
    List<AbstractPoddUser> listAllPoddUsers();
    
    /**
     * Updates details of an existing <code>PoddUser</code>.
     * 
     * @param user
     * @throws UserNotFoundException
     *             If the user was not found in the current set of PODD users.
     */
    void updatePoddUser(PoddUser user) throws UserNotFoundException;
    
}
