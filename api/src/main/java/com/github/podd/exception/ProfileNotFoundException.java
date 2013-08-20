/*
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
package com.github.podd.exception;

import org.semanticweb.owlapi.model.IRI;

/**
 * An exception indicating that the given profile IRI was not found in the OWLAPI profile registry.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class ProfileNotFoundException extends PoddException
{
    private static final long serialVersionUID = 6247991932378540548L;
    
    private final IRI profileIRI;
    
    /**
     * 
     * @param profile
     *            The OWL profile IRI that was not found in the OWLAPI profile registry.
     * @param msg
     *            The message for this exception.
     */
    public ProfileNotFoundException(final IRI profile, final String msg)
    {
        super(msg);
        this.profileIRI = profile;
    }
    
    /**
     * @param profile
     *            The OWL profile IRI that was not found in the OWLAPI profile registry.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public ProfileNotFoundException(final IRI profile, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.profileIRI = profile;
    }
    
    /**
     * @param profile
     *            The OWL profile IRI that was not found in the OWLAPI profile registry.
     * @param throwable
     *            The cause for this exception.
     */
    public ProfileNotFoundException(final IRI profile, final Throwable throwable)
    {
        super(throwable);
        this.profileIRI = profile;
    }
    
    /**
     * 
     * @return The OWL profile IRI that was not found in the OWLAPI profile registry.
     */
    public IRI getProfile()
    {
        return this.profileIRI;
    }
    
}
