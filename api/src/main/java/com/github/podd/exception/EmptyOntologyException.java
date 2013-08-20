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

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * An exception indicating that the given OWL Ontology was unexpectedly empty.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class EmptyOntologyException extends PoddException
{
    private static final long serialVersionUID = 4654987743002073476L;
    
    private final OWLOntology ontology;
    
    /**
     * 
     * @param ontology
     *            The OWL Ontology which was found to be empty.
     * @param msg
     *            The message for this exception.
     */
    public EmptyOntologyException(final OWLOntology ontology, final String msg)
    {
        super(msg);
        this.ontology = ontology;
    }
    
    /**
     * @param ontology
     *            The OWL Ontology which was found to be empty.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public EmptyOntologyException(final OWLOntology ontology, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.ontology = ontology;
    }
    
    /**
     * @param ontology
     *            The OWL Ontology which was found to be empty.
     * @param throwable
     *            The cause for this exception.
     */
    public EmptyOntologyException(final OWLOntology ontology, final Throwable throwable)
    {
        super(throwable);
        this.ontology = ontology;
    }
    
    /**
     * 
     * @return The OWL Ontology which was found to be empty.
     */
    public OWLOntology getOntology()
    {
        return this.ontology;
    }
    
}
