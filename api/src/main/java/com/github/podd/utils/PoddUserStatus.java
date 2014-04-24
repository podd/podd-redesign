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
package com.github.podd.utils;

import org.openrdf.model.URI;

/**
 * Represents the valid states that a <code>PoddUser</code> can be in.
 *
 * @author kutila
 *
 */
public enum PoddUserStatus
{
    /**
     * An ACTIVE user has the ability to access the PODD system.
     */
    ACTIVE("Active", "http://purl.org/podd/user/status#active"),

    /**
     * An inactive user can be referenced from within PODD artifacts. Such a user does not have the
     * ability to access the PODD system.
     */
    INACTIVE("Inactive", "http://purl.org/podd/user/status#inactive")

    ;

    /**
     * Retrieve the {@link PoddUserStatus} matching the given {@link URI}.
     *
     * @param label
     *            A String label to identify this PoddUserStatus
     * @param nextUri
     *            A URI to uniquely identify this PoddUserStatus
     * @return The Status matching the given URI or INACTIVE if no match found.
     */
    public static PoddUserStatus getUserStatusByUri(final URI nextUri)
    {
        for(final PoddUserStatus nextStatus : PoddUserStatus.values())
        {
            if(nextStatus.getURI().equals(nextUri))
            {
                return nextStatus;
            }
        }

        return INACTIVE;
    }

    private final String label;

    private final URI uri;

    private PoddUserStatus(final String label, final String uri)
    {
        this.label = label;
        this.uri = PODD.VF.createURI(uri);
    }

    public String getLabel()
    {
        return this.label;
    }

    public URI getURI()
    {
        return this.uri;
    }

}