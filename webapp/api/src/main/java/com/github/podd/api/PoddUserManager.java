/**
 * PODD is an OWL ontology database used for scientific project management
 *
 * Copyright (C) 2009-2013 The University Of Queensland
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
/**
 *
 */
package com.github.podd.api;

import java.util.List;

import org.openrdf.model.URI;

import com.github.podd.exception.InvalidUserDetailsException;
import com.github.podd.exception.UserAlreadyExistsException;
import com.github.podd.exception.UserNotFoundException;
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
    List<PoddUser> listAllPoddUsers();

    /**
     * Updates details of an existing <code>PoddUser</code>.
     *
     * @param user
     * @throws UserNotFoundException
     *             If the user was not found in the current set of PODD users.
     */
    void updatePoddUser(PoddUser user) throws UserNotFoundException;

}
