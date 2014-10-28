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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.clarkparsia.owlapi.explanation.io.ExplanationRenderer;
import com.github.podd.utils.PODD;

/**
 * An exception indicating that the given ontology was inconsistent.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class InconsistentOntologyException extends PoddException
{
    private static final long serialVersionUID = 7628963882692198674L;
    private final Set<Set<OWLAxiom>> explanations;
    private final OWLOntologyID ontologyID;
    private final ExplanationRenderer renderer;
    
    /**
     *
     * @param msg
     *            The message for this exception.
     */
    public InconsistentOntologyException(final Set<Set<OWLAxiom>> inconsistencyExplanations,
            final OWLOntologyID ontologyID, final ExplanationRenderer explanationRenderer, final String msg)
    {
        super(msg);
        this.explanations = inconsistencyExplanations;
        this.ontologyID = ontologyID;
        this.renderer = explanationRenderer;
    }
    
    /**
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public InconsistentOntologyException(final Set<Set<OWLAxiom>> inconsistencyExplanations,
            final OWLOntologyID ontologyID, final ExplanationRenderer explanationRenderer, final String msg,
            final Throwable throwable)
    {
        super(msg, throwable);
        this.explanations = inconsistencyExplanations;
        this.ontologyID = ontologyID;
        this.renderer = explanationRenderer;
    }
    
    /**
     * @param reasoner
     *            The OWL Reasoner instance containing the details about why the ontology was
     *            inconsistent.
     * @param throwable
     *            The cause for this exception.
     */
    public InconsistentOntologyException(final Set<Set<OWLAxiom>> inconsistencyExplanations,
            final OWLOntologyID ontologyID, final ExplanationRenderer explanationRenderer, final Throwable throwable)
    {
        super(throwable);
        this.explanations = inconsistencyExplanations;
        this.ontologyID = ontologyID;
        this.renderer = explanationRenderer;
    }
    
    @Override
    public Model getDetailsAsModel(final Resource errorResource)
    {
        final Model model = super.getDetailsAsModel(errorResource);
        
        if(this.explanations != null)
        {
            final BNode reasonerUri = PODD.VF.createBNode();
            model.add(errorResource, PODD.ERR_IDENTIFIER, reasonerUri);
            model.add(reasonerUri, RDFS.LABEL,
                    PODD.VF.createLiteral("Explanations for inconsistencies (" + this.explanations.size() + ")"));
            
            model.add(errorResource, PODD.ERR_SOURCE, this.ontologyID.getOntologyIRI().toOpenRDFURI());
            
            String explanation;
            
            try
            {
                final StringWriter results = new StringWriter();
                
                this.renderer.startRendering(results);
                
                this.renderer.render((OWLAxiom)null, this.explanations);
                
                this.renderer.endRendering();
                
                explanation = results.toString();
            }
            catch(IOException | OWLException e)
            {
                explanation = "Failed to render inconsistency explanation";
            }
            final BNode v = PODD.VF.createBNode();
            model.add(errorResource, PODD.ERR_CONTAINS, v);
            model.add(v, RDF.TYPE, PODD.ERR_TYPE_ERROR);
            model.add(v, RDFS.LABEL, PODD.VF.createLiteral(this.getMessage()));
            model.add(v, RDFS.COMMENT, PODD.VF.createLiteral(explanation));
        }
        
        return model;
    }
    
    public OWLOntologyID getOntologyID()
    {
        return this.ontologyID;
    }
}
