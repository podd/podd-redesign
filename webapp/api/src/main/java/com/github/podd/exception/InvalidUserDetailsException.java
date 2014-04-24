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
 * @author kutila
 *
 */
public class InvalidUserDetailsException extends PoddException
{

    private static final long serialVersionUID = 6740075721656072394L;

    private final PoddUser user;
    private final String[] errorFields;

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
     * @return The user fields that had errors
     */
    public String[] getErrorFields()
    {
        return this.errorFields;
    }

    /**
     *
     * @return The <code>PoddUser</code> object that had errors
     */
    public PoddUser getUser()
    {
        return this.user;
    }

}
