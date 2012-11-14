/**
 * 
 */
package com.github.podd.exception;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * An exception indicating that the given ontology was inconsistent.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class InconsistentOntologyException extends PoddException
{
    private static final long serialVersionUID = 7628963882692198674L;
    
    private final OWLReasoner reasoner;
    
    /**
     * 
     * @param reasoner
     *            The OWL Reasoner instance containing the details about why the ontology was
     *            inconsistent.
     * @param msg
     *            The message for this exception.
     */
    public InconsistentOntologyException(final OWLReasoner reasoner, final String msg)
    {
        super(msg);
        this.reasoner = reasoner;
    }
    
    /**
     * @param reasoner
     *            The OWL Reasoner instance containing the details about why the ontology was
     *            inconsistent.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public InconsistentOntologyException(final OWLReasoner reasoner, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.reasoner = reasoner;
    }
    
    /**
     * @param reasoner
     *            The OWL Reasoner instance containing the details about why the ontology was
     *            inconsistent.
     * @param throwable
     *            The cause for this exception.
     */
    public InconsistentOntologyException(final OWLReasoner reasoner, final Throwable throwable)
    {
        super(throwable);
        this.reasoner = reasoner;
    }
    
    /**
     * @return The OWL Reasoner instance containing the details about why the ontology was
     *         inconsistent.
     */
    public OWLReasoner getReasoner()
    {
        return this.reasoner;
    }
    
}
