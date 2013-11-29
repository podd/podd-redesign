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

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.utils.PODD;

/**
 * An exception indicating that the artifact denoted by the given IRI is already managed by PODD.
 * 
 * @author Kutila
 * 
 */
public class DuplicateArtifactIRIException extends UnmanagedSchemaException
{
    private static final long serialVersionUID = -2321179998407787564L;
    
    private final IRI artifactOntologyIRI;
    
    /**
     * 
     * @param ontology
     *            The OWL Ontology IRI that is a duplicate.
     * @param msg
     *            The message for this exception.
     */
    public DuplicateArtifactIRIException(final IRI artifactOntologyIRI, final String msg)
    {
        super(msg);
        this.artifactOntologyIRI = artifactOntologyIRI;
    }
    
    /**
     * @param ontology
     *            The OWL Ontology IRI that is a duplicate.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public DuplicateArtifactIRIException(final IRI artifactOntologyIRI, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.artifactOntologyIRI = artifactOntologyIRI;
    }
    
    /**
     * @param ontology
     *            The OWL Ontology IRI that is a duplicate.
     * @param throwable
     *            The cause for this exception.
     */
    public DuplicateArtifactIRIException(final IRI artifactOntologyIRI, final Throwable throwable)
    {
        super(throwable);
        this.artifactOntologyIRI = artifactOntologyIRI;
    }
    
    @Override
    public Model getDetailsAsModel(final Resource errorResource)
    {
        final Model model = super.getDetailsAsModel(errorResource);
        
        if(this.getDuplicateOntologyIRI() != null)
        {
            model.add(errorResource, PODD.ERR_SOURCE, this.getDuplicateOntologyIRI().toOpenRDFURI());
        }
        
        return model;
    }
    
    /**
     * @return The OWL Ontology IRI that is a duplicate.
     */
    public IRI getDuplicateOntologyIRI()
    {
        return this.artifactOntologyIRI;
    }
    
}
