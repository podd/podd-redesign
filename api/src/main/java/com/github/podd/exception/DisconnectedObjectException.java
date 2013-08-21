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

import java.util.Set;

import org.openrdf.model.URI;

/**
 * An exception indicating that one or more PODD objects (found inside an artifact) are no longer
 * connected to an eligible Top Object.
 * 
 * @author kutila
 */
public class DisconnectedObjectException extends PoddException
{
    
    private static final long serialVersionUID = 1844520618633481527L;
    
    private final Set<URI> disconnectedObjects;
    
    public DisconnectedObjectException(final Set<URI> disconnectedObjects, final String msg)
    {
        super(msg);
        this.disconnectedObjects = disconnectedObjects;
    }
    
    public DisconnectedObjectException(final Set<URI> disconnectedObjects, final Throwable throwable)
    {
        super(throwable);
        this.disconnectedObjects = disconnectedObjects;
    }
    
    public DisconnectedObjectException(final Set<URI> disconnectedObjects, final Throwable throwable, final String msg)
    {
        super(msg, throwable);
        this.disconnectedObjects = disconnectedObjects;
    }
    
    public Set<URI> getDisconnectedObjects()
    {
        return this.disconnectedObjects;
    }
    
}
