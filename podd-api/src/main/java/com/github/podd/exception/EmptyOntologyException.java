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
