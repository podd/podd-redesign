/*
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
    public Model getDetailsAsModel(final Resource errorResource)
    {
        final Model model = super.getDetailsAsModel(errorResource);
        
        final OWLReasoner reasoner = this.getReasoner();
        if(reasoner != null)
        {
            final BNode reasonerUri = PoddRdfConstants.VF.createBNode();
            model.add(errorResource, PoddRdfConstants.ERR_IDENTIFIER, reasonerUri);
            model.add(reasonerUri, RDFS.LABEL, PoddRdfConstants.VF.createLiteral(reasoner.getReasonerName()));
            model.add(reasonerUri, PoddRdfConstants.OMV_CURRENT_VERSION,
                    PoddRdfConstants.VF.createLiteral(reasoner.getReasonerVersion().toString()));
            
            model.add(errorResource, PoddRdfConstants.ERR_SOURCE, reasoner.getRootOntology().getOntologyID()
                    .getOntologyIRI().toOpenRDFURI());
            
            // TODO: can we get the causes for inconsistency?
            // use ExplanationGenerator - sample code in podd-ontologies
        }
        
        return model;
    }
    
}
