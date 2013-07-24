/**
 * 
 */
package com.github.podd.exception;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.vocabulary.OWL;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.utils.PoddRdfConstants;

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
     * @return The managed OWL Ontology IRI.
     */
    public IRI getOntologyID()
    {
        return this.artifactOntologyIRI;
    }
    
    /**
     * @return The managed Ontology's current Version IRI.
     */
    public IRI getArtifactVersion()
    {
        return this.artifactVersionIRI;
    }
    
    /**
     * @return The unmanaged OWL Version IRI that caused this Exception.
     */
    public IRI getUnmanagedVersionIRI()
    {
        return this.unmanagedVersionIRI;
    }
    
    @Override
    public Model getDetailsAsModel(final Resource errorResource)
    {
        final Model model = super.getDetailsAsModel(errorResource);
        
        if(this.getOntologyID() != null)
        {
            model.add(errorResource, PoddRdfConstants.ERR_SOURCE, this.getUnmanagedVersionIRI().toOpenRDFURI());
            
            model.add(errorResource, OWL.ONTOLOGY, this.getOntologyID().toOpenRDFURI());
            model.add(errorResource, PoddRdfConstants.OWL_VERSION_IRI, this.getArtifactVersion().toOpenRDFURI());
        }
        
        return model;
    }
    
}
