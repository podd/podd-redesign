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
 * An exception that is thrown when an artifact cannot be deleted for any reason.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DeleteArtifactException extends PoddException
{
    private static final long serialVersionUID = 7736119378934264560L;
    
    private final OWLOntologyID artifact;
    
    /**
     * @param msg
     */
    public DeleteArtifactException(final String msg, final OWLOntologyID artifact)
    {
        super(msg);
        this.artifact = artifact;
    }
    
    /**
     * @param msg
     * @param throwable
     */
    public DeleteArtifactException(final String msg, final Throwable throwable, final OWLOntologyID artifact)
    {
        super(msg, throwable);
        this.artifact = artifact;
    }
    
    /**
     * @param throwable
     */
    public DeleteArtifactException(final Throwable throwable, final OWLOntologyID artifact)
    {
        super(throwable);
        this.artifact = artifact;
    }
    
    /**
     * @return the artifact
     */
    public OWLOntologyID getArtifact()
    {
        return this.artifact;
    }
    
}
