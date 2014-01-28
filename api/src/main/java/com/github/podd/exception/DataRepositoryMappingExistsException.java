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
 * An exception that is thrown to indicate that there a file repository mapping with the given alias
 * already exists.
 * 
 * @author kutila
 */
public class DataRepositoryMappingExistsException extends DataRepositoryException
{
    
    private static final long serialVersionUID = 3224280634543226410L;
    
    private final String alias;
    
    /**
     * 
     * @param alias
     *            The file repository alias that caused the Exception.
     * @param msg
     *            The message for this exception.
     */
    public DataRepositoryMappingExistsException(final String alias, final String msg)
    {
        super(msg);
        this.alias = alias;
    }
    
    /**
     * @param alias
     *            The file repository alias that caused the Exception.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public DataRepositoryMappingExistsException(final String alias, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.alias = alias;
    }
    
    /**
     * @param alias
     *            The file repository alias that caused the Exception.
     * @param throwable
     *            The cause for this exception.
     */
    public DataRepositoryMappingExistsException(final String alias, final Throwable throwable)
    {
        super(throwable);
        this.alias = alias;
    }
    
    /**
     * @return The file repository alias that caused the Exception.
     */
    public String getAlias()
    {
        return this.alias;
    }
    
}
