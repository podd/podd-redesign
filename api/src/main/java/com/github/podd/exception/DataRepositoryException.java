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
 * This class extends {@link PoddException} to provide an abstract exception base class for File
 * Repository related exceptions.
 *
 * @author kutila
 */
public class DataRepositoryException extends PoddException
{
    
    private static final long serialVersionUID = 6527298790638967965L;
    
    public DataRepositoryException(final String msg)
    {
        super(msg);
    }
    
    public DataRepositoryException(final String msg, final Throwable throwable)
    {
        super(msg, throwable);
    }
    
    public DataRepositoryException(final Throwable throwable)
    {
        super(throwable);
    }
    
}
