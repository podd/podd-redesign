/**
 * 
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
