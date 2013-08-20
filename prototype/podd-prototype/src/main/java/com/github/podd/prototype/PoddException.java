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
 package com.github.podd.prototype;

/**
 * This class extends <code>java.lang.Exception</code> to provide a PODD specific exception class.
 * 
 * @author kutila
 * 
 */
public class PoddException extends Exception
{
    
    /** Unsupported OWL Profile. */
    public static final int ERR_PROFILE_NOT_FOUND = 03000;
    
    /** Ontology not in required OWL Profile. */
    public static final int ERR_ONTOLOGY_NOT_IN_PROFILE = 03001;
    
    /** Ontology not consistent against OWL Profile. */
    public static final int ERR_INCONSISTENT_ONTOLOGY = 03002;
    
    /** Ontology not consistent against OWL Profile. */
    public static final int ERR_EMPTY_ONTOLOGY = 03003;
    
    private static final long serialVersionUID = 804321L;
    
    private final Object details;
    private final int code;
    
    /**
     * Create a new <code>PoddException</code> with details.
     * 
     * @param msg
     *            A message describing the Exception.
     * @param details
     *            An object containing details of the underlying cause.
     * @param code
     *            A numeric code describing the Exception.
     */
    public PoddException(final String msg, final Object details, final int code)
    {
        super(msg);
        this.details = details;
        this.code = code;
    }
    
    /**
     * Create a new <code>PoddException</code> with details.
     * 
     * @param msg
     *            A message describing the Exception.
     * @param details
     *            An object containing details of the underlying cause.
     * @param code
     *            A numeric code describing the Exception.
     */
    public PoddException(final String msg, final Throwable cause, final Object details, final int code)
    {
        super(msg, cause);
        this.details = details;
        this.code = code;
    }
    
    public Object getDetails()
    {
        return this.details;
    }
    
    public int getCode()
    {
        return this.code;
    }
    
}
