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
    
    @Override
    public Model getDetailsAsModel(final Resource errorResource)
    {
        final Model model = super.getDetailsAsModel(errorResource);
        
        if(this.getOntology() != null)
        {
            model.add(errorResource, PoddRdfConstants.ERR_SOURCE, this.getOntology().getOntologyID().getOntologyIRI()
                    .toOpenRDFURI());
        }
        
        if(this.getProfileReport() != null)
        {
            for(final OWLProfileViolation violation : this.getProfileReport().getViolations())
            {
                final BNode v = PoddRdfConstants.VF.createBNode();
                model.add(errorResource, PoddRdfConstants.ERR_CONTAINS, v);
                model.add(v, RDF.TYPE, PoddRdfConstants.ERR_TYPE_ERROR);
                model.add(v, PoddRdfConstants.ERR_SOURCE,
                        PoddRdfConstants.VF.createLiteral(violation.getAxiom().toString()));
                model.add(v, RDFS.COMMENT, PoddRdfConstants.VF.createLiteral(violation.toString()));
            }
        }
        
        return model;
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
