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
 * An exception indicating that the Schema-Manifest file contained an error.
 *
 * @author kutila
 *
 */
public class SchemaManifestException extends PoddException
{

    private static final long serialVersionUID = -714384727722988506L;

    private final IRI schemaOntologyIRI;

    public SchemaManifestException(final String msg)
    {
        this(null, msg);
    }

    public SchemaManifestException(final String msg, final Throwable throwable)
    {
        this(null, msg, throwable);
    }

    public SchemaManifestException(final IRI schemaOntologyIRI, final String msg)
    {
        super(msg);
        this.schemaOntologyIRI = schemaOntologyIRI;
    }

    /**
     * @param msg
     * @param throwable
     */
    public SchemaManifestException(final IRI schemaOntologyIRI, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.schemaOntologyIRI = schemaOntologyIRI;
    }

    /**
     * @param throwable
     */
    public SchemaManifestException(final IRI schemaOntologyIRI, final Throwable throwable)
    {
        super(throwable);
        this.schemaOntologyIRI = schemaOntologyIRI;
    }

    /**
     * @return The OWL Ontology IRI where the error was observed.
     */
    public IRI getSchemaOntologyIRI()
    {
        return this.schemaOntologyIRI;
    }

}
