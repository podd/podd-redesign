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
 * Exception to indicate an attempt to modify a Published PODD Artifact.
 * 
 * @author kutila
 * 
 */
public class PublishedArtifactModifyException extends PoddException
{
    private static final long serialVersionUID = -3273512190148407834L;
    
    private OWLOntologyID artifactId;
    
    /**
     * @param msg
     */
    public PublishedArtifactModifyException(final String msg, final OWLOntologyID artifactId)
    {
        super(msg);
        this.artifactId = artifactId;
    }
    
    /**
     * @param msg
     * @param throwable
     */
    public PublishedArtifactModifyException(final String msg, final Throwable throwable, final OWLOntologyID artifactId)
    {
        super(msg, throwable);
        this.artifactId = artifactId;
    }
    
    /**
     * @param throwable
     */
    public PublishedArtifactModifyException(final Throwable throwable, final OWLOntologyID artifactId)
    {
        super(throwable);
        this.artifactId = artifactId;
    }
    
    /**
     * 
     * @return The {@link OWLOntologyID} of the artifact that was not able to be published.
     */
    public OWLOntologyID getArtifactID()
    {
        return this.artifactId;
    }
}
