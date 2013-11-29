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
import org.openrdf.model.vocabulary.OWL;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.utils.PODD;

/**
 * An exception indicating that the particular version IRI of the artifact denoted by the given IRI
 * was not managed by PODD.
 * 
 * @author Kutila
 * @since 24/07/2013
 * 
 */
public class UnmanagedArtifactVersionException extends UnmanagedSchemaException
{
    private static final long serialVersionUID = 4395800605913179651L;
    
    private final IRI artifactOntologyIRI;
    
    private final IRI artifactVersionIRI;
    
    private final IRI unmanagedVersionIRI;
    
    /**
     * @param artifactOntologyIRI
     *            The OWL Ontology IRI that is managed.
     * @param artifactVersionIRI
     *            The OWL Ontology Version IRI that is managed.
     * @param unmanagedVersionIRI
     *            The OWL Ontology Version IRI that was not managed.
     * @param msg
     *            The message for this exception.
     */
    public UnmanagedArtifactVersionException(final IRI artifactOntologyIRI, final IRI artifactVersionIRI,
            final IRI unmanagedVersionIRI, final String msg)
    {
        super(msg);
        this.artifactOntologyIRI = artifactOntologyIRI;
        this.artifactVersionIRI = artifactVersionIRI;
        this.unmanagedVersionIRI = unmanagedVersionIRI;
    }
    
    /**
     * @param artifactOntologyIRI
     *            The OWL Ontology IRI that is managed.
     * @param artifactVersionIRI
     *            The OWL Ontology Version IRI that is managed.
     * @param unmanagedVersionIRI
     *            The OWL Ontology Version IRI that was not managed.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public UnmanagedArtifactVersionException(final IRI artifactOntologyIRI, final IRI artifactVersionIRI,
            final IRI unmanagedVersionIRI, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.artifactOntologyIRI = artifactOntologyIRI;
        this.artifactVersionIRI = artifactVersionIRI;
        this.unmanagedVersionIRI = unmanagedVersionIRI;
    }
    
    /**
     * @param artifactOntologyIRI
     *            The OWL Ontology IRI that is managed.
     * @param artifactVersionIRI
     *            The OWL Ontology Version IRI that is managed.
     * @param unmanagedVersionIRI
     *            The OWL Ontology Version IRI that was not managed.
     * @param throwable
     *            The cause for this exception.
     */
    public UnmanagedArtifactVersionException(final IRI artifactOntologyIRI, final IRI artifactVersionIRI,
            final IRI unmanagedVersionIRI, final Throwable throwable)
    {
        super(throwable);
        this.artifactOntologyIRI = artifactOntologyIRI;
        this.artifactVersionIRI = artifactVersionIRI;
        this.unmanagedVersionIRI = unmanagedVersionIRI;
    }
    
    /**
     * @return The managed Ontology's current Version IRI.
     */
    public IRI getArtifactVersion()
    {
        return this.artifactVersionIRI;
    }
    
    @Override
    public Model getDetailsAsModel(final Resource errorResource)
    {
        final Model model = super.getDetailsAsModel(errorResource);
        
        if(this.getOntologyID() != null)
        {
            model.add(errorResource, PODD.ERR_SOURCE, this.getUnmanagedVersionIRI().toOpenRDFURI());
            
            model.add(errorResource, OWL.ONTOLOGY, this.getOntologyID().toOpenRDFURI());
            model.add(errorResource, PODD.OWL_VERSION_IRI, this.getArtifactVersion().toOpenRDFURI());
        }
        
        return model;
    }
    
    /**
     * @return The managed OWL Ontology IRI.
     */
    public IRI getOntologyID()
    {
        return this.artifactOntologyIRI;
    }
    
    /**
     * @return The unmanaged OWL Version IRI that caused this Exception.
     */
    public IRI getUnmanagedVersionIRI()
    {
        return this.unmanagedVersionIRI;
    }
    
}
