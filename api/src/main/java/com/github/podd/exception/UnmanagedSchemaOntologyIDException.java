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

import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * An exception indicating that the Schema Ontology denoted by the given OWLOntologyID was not
 * managed by PODD.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class UnmanagedSchemaOntologyIDException extends UnmanagedSchemaException
{
    private static final long serialVersionUID = -7266174841631944910L;

    private final OWLOntologyID schemaOntologyID;

    /**
     *
     * @param ontology
     *            The OWL Ontology ID that was not managed.
     * @param msg
     *            The message for this exception.
     */
    public UnmanagedSchemaOntologyIDException(final OWLOntologyID schemaOntologyID, final String msg)
    {
        super(msg);
        this.schemaOntologyID = schemaOntologyID;
    }

    /**
     * @param ontology
     *            The OWL Ontology ID that was not managed.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public UnmanagedSchemaOntologyIDException(final OWLOntologyID schemaOntologyID, final String msg,
            final Throwable throwable)
    {
        super(msg, throwable);
        this.schemaOntologyID = schemaOntologyID;
    }

    /**
     * @param ontology
     *            The OWL Ontology ID that was not managed.
     * @param throwable
     *            The cause for this exception.
     */
    public UnmanagedSchemaOntologyIDException(final OWLOntologyID schemaOntologyID, final Throwable throwable)
    {
        super(throwable);
        this.schemaOntologyID = schemaOntologyID;
    }

    /**
     * @return The OWL Ontology ID that was not managed.
     */
    public OWLOntologyID getOntologyID()
    {
        return this.schemaOntologyID;
    }

}
