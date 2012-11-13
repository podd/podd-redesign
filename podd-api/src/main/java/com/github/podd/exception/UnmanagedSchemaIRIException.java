/**
 * 
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
