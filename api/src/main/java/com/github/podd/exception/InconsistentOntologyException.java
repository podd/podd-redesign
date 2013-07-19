/**
 * 
 */
package com.github.podd.exception;

import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.vocabulary.RDFS;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.github.podd.utils.PoddRdfConstants;

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
    
    @Override
    public Model getDetailsAsModel()
    {
        final Model model = super.getDetailsAsModel();
        
        // the super-class MUST set this statement
        final Resource errorUri =
                model.filter(null, PoddRdfConstants.ERR_EXCEPTION_CLASS, null).subjects().iterator().next();
        
        final OWLReasoner reasoner = this.getReasoner();
        if(reasoner != null)
        {
            final BNode reasonerUri = PoddRdfConstants.VF.createBNode();
            model.add(errorUri, PoddRdfConstants.ERR_IDENTIFIER, reasonerUri);
            model.add(reasonerUri, RDFS.LABEL, PoddRdfConstants.VF.createLiteral(reasoner.getReasonerName()));
            model.add(reasonerUri, PoddRdfConstants.OMV_CURRENT_VERSION,
                    PoddRdfConstants.VF.createLiteral(reasoner.getReasonerVersion().toString()));
            
            model.add(errorUri, PoddRdfConstants.ERR_SOURCE, reasoner.getRootOntology().getOntologyID()
                    .getOntologyIRI().toOpenRDFURI());
            
            // TODO: can we get the causes for inconsistency?
        }
        
        return model;
    }
    
}
