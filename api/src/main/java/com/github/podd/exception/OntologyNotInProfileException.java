/**
 * 
 */
package com.github.podd.exception;

import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;

import com.github.podd.utils.PoddRdfConstants;

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
    
    @Override
    public Model getDetailsAsModel()
    {
        final Model model = super.getDetailsAsModel();
        
        // the super-class MUST set this statement
        final Resource errorUri =
                model.filter(null, PoddRdfConstants.ERR_EXCEPTION_CLASS, null).subjects().iterator().next();
        
        if(this.getOntology() != null)
        {
            model.add(errorUri, PoddRdfConstants.ERR_SOURCE, this.getOntology().getOntologyID().getOntologyIRI()
                    .toOpenRDFURI());
        }
        
        if(this.getProfileReport() != null)
        {
            for(final OWLProfileViolation violation : this.getProfileReport().getViolations())
            {
                final BNode v = PoddRdfConstants.VF.createBNode();
                model.add(errorUri, PoddRdfConstants.ERR_CONTAINS, v);
                model.add(v, RDF.TYPE, PoddRdfConstants.ERR_TYPE_ERROR);
                model.add(v, PoddRdfConstants.ERR_SOURCE,
                        PoddRdfConstants.VF.createLiteral(violation.getAxiom().toString()));
                model.add(v, RDFS.COMMENT, PoddRdfConstants.VF.createLiteral(violation.toString()));
            }
        }
        
        return model;
    }
    
}
