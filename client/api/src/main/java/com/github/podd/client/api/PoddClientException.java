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
package com.github.podd.client.api;

/**
 * The base class of unchecked exceptions thrown by the PODD Client API.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class PoddClientException extends RuntimeException
{
    private static final long serialVersionUID = -2854524362938904344L;

    /**
     *
     */
    public PoddClientException()
    {
        super();
    }

    /**
     * @param message
     */
    public PoddClientException(final String message)
    {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public PoddClientException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public PoddClientException(final Throwable cause)
    {
        super(cause);
    }

}
