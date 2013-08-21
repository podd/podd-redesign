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

import org.semanticweb.owlapi.model.IRI;

/**
 * An exception indicating that the Schema Ontology denoted by the given IRI was not managed by
 * PODD.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class UnmanagedSchemaIRIException extends UnmanagedSchemaException
{
    private static final long serialVersionUID = -7266174841631944910L;
    
    private final IRI schemaOntologyIRI;
    
    /**
     * 
     * @param ontology
     *            The OWL Ontology IRI that was not managed.
     * @param msg
     *            The message for this exception.
     */
    public UnmanagedSchemaIRIException(final IRI schemaOntologyIRI, final String msg)
    {
        super(msg);
        this.schemaOntologyIRI = schemaOntologyIRI;
    }
    
    /**
     * @param ontology
     *            The OWL Ontology IRI that was not managed.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public UnmanagedSchemaIRIException(final IRI schemaOntologyIRI, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.schemaOntologyIRI = schemaOntologyIRI;
    }
    
    /**
     * @param ontology
     *            The OWL Ontology IRI that was not managed.
     * @param throwable
     *            The cause for this exception.
     */
    public UnmanagedSchemaIRIException(final IRI schemaOntologyIRI, final Throwable throwable)
    {
        super(throwable);
        this.schemaOntologyIRI = schemaOntologyIRI;
    }
    
    /**
     * @return The OWL Ontology IRI that was not managed.
     */
    public IRI getOntologyID()
    {
        return this.schemaOntologyIRI;
    }
    
}
