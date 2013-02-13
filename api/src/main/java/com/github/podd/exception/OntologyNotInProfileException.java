/**
 * 
 */
package com.github.podd.exception;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.profiles.OWLProfileReport;

/**
 * An exception indicating that the OWL Ontology was not found to be in the given OWL Profile.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class OntologyNotInProfileException extends PoddException
{
    private static final long serialVersionUID = -7266174841631944910L;
    
    private final OWLOntology ontology;
    private final OWLProfileReport profileReport;
    
    /**
     * 
     * @param ontology
     *            The OWL Ontology that was not in the given profile.
     * @param profileReport
     *            The OWL Profile Report indicating the causes for the ontology not being in the
     *            given profile.
     * @param msg
     *            The message for this exception.
     */
    public OntologyNotInProfileException(final OWLOntology ontology, final OWLProfileReport profileReport,
            final String msg)
    {
        super(msg);
        this.ontology = ontology;
        this.profileReport = profileReport;
    }
    
    /**
     * @param ontology
     *            The OWL Ontology that was not in the given profile.
     * @param profileReport
     *            The OWL Profile Report indicating the causes for the ontology not being in the
     *            given profile.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public OntologyNotInProfileException(final OWLOntology ontology, final OWLProfileReport profileReport,
            final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.ontology = ontology;
        this.profileReport = profileReport;
    }
    
    /**
     * @param ontology
     *            The OWL Ontology that was not in the given profile.
     * @param profileReport
     *            The OWL Profile Report indicating the causes for the ontology not being in the
     *            given profile.
     * @param throwable
     *            The cause for this exception.
     */
    public OntologyNotInProfileException(final OWLOntology ontology, final OWLProfileReport profileReport,
            final Throwable throwable)
    {
        super(throwable);
        this.ontology = ontology;
        this.profileReport = profileReport;
    }
    
    /**
     * @return The OWL Ontology that was not in the given profile.
     */
    public OWLOntology getOntology()
    {
        return this.ontology;
    }
    
    /**
     * @return The OWL Profile report indicating what profile violations occurred.
     */
    public OWLProfileReport getProfileReport()
    {
        return this.profileReport;
    }
    
}
