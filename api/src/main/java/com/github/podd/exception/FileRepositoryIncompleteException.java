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
 * An exception that is thrown to indicate that there was insufficient information to construct a
 * file repository configuration from given data.
 * 
 * @author kutila
 */
public class FileRepositoryIncompleteException extends DataRepositoryException
{
    private static final long serialVersionUID = -689252740293644258L;
    
    /**
     * 
     * @param msg
     *            The message for this exception.
     */
    public FileRepositoryIncompleteException(final String msg)
    {
        super(msg);
    }
    
    /**
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public FileRepositoryIncompleteException(final String msg, final Throwable throwable)
    {
        super(msg, throwable);
    }
    
    /**
     * @param throwable
     *            The cause for this exception.
     */
    public FileRepositoryIncompleteException(final Throwable throwable)
    {
        super(throwable);
    }
}
