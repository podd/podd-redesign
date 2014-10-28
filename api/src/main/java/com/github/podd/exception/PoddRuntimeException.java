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

/**
 * This class extends <code>java.lang.RuntimeException</code> to provide a PODD specific Runtime
 * exception class.
 *
 * @author kutila
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class PoddRuntimeException extends RuntimeException
{
    
    private static final long serialVersionUID = -1525497322722630581L;
    
    public PoddRuntimeException(final String msg)
    {
        super(msg);
    }
    
    public PoddRuntimeException(final String msg, final Throwable throwable)
    {
        super(msg, throwable);
    }
    
    public PoddRuntimeException(final Throwable throwable)
    {
        super(throwable);
    }
    
}
