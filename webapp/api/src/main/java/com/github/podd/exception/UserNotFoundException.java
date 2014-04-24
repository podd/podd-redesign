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
