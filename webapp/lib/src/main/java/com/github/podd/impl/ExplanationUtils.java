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

// Originally derived from PelletExplain.java which had the following copyright notice:

// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package com.github.podd.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.NullProgressMonitor;
import org.semanticweb.owlapi.util.ProgressMonitor;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.GlassBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import com.clarkparsia.owlapi.explanation.MultipleExplanationGenerator;
import com.clarkparsia.owlapi.explanation.SatisfiabilityConverter;
import com.clarkparsia.owlapi.explanation.TransactionAwareSingleExpGen;
import com.clarkparsia.owlapi.explanation.io.ExplanationRenderer;
import com.clarkparsia.owlapi.explanation.util.ExplanationProgressMonitor;
import com.clarkparsia.owlapiv3.OWL;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class ExplanationUtils
{
    private static class RendererExplanationProgressMonitor implements ExplanationProgressMonitor
    {

        private OWLAxiom axiom;
        private ExplanationRenderer rend;
        private Set<Set<OWLAxiom>> setExplanations = new HashSet<Set<OWLAxiom>>();

        private RendererExplanationProgressMonitor(final ExplanationRenderer rend, final OWLAxiom axiom)
        {
            this.axiom = axiom;
            this.rend = rend;
        }

        @Override
        public void foundAllExplanations()
        {
            // Do nothing to support multiple uses of renderer
        }

        @Override
        public void foundExplanation(final Set<OWLAxiom> axioms)
        {

            if(!this.setExplanations.contains(axioms))
            {
                this.setExplanations.add(axioms);
                try
                {
                    if(this.rend != null)
                    {
                        this.rend.render(this.axiom, Collections.singleton(axioms));
                    }
                }
                catch(final IOException e)
                {
                    System.err.println("Error rendering explanation: " + e);
                }
                catch(final OWLException e)
                {
                    System.err.println("Error rendering explanation: " + e);
                }
            }
        }

        public void foundNoExplanations()
        {
            try
            {
                if(this.rend != null)
                {
                    this.rend.render(this.axiom, Collections.<Set<OWLAxiom>> emptySet());
                }
            }
            catch(final OWLException e)
            {
                System.err.println("Error rendering explanation: " + e);
            }
            catch(final IOException e)
            {
                System.err.println("Error rendering explanation: " + e);
            }
        }

        @Override
        public boolean isCancelled()
        {
            return false;
        }
    }

    static
    {
        GlassBoxExplanation.setup();
    }

    private boolean allowInconsistency = true;
    private SatisfiabilityConverter converter;
    private int errorExpCount = 0;
    private ExplanationRenderer explanationRenderer;
    private int maxExplanations = 1;
    private ProgressMonitor monitor;
    /**
     * inferences whose explanation contains more than on axiom
     */
    private int multiAxiomExpCount = 0;

    /**
     * inferences with multiple explanations
     */
    private int multipleExpCount = 0;
    private PelletReasoner reasoner;
    private PelletReasonerFactory reasonerFactory;

    // private ExplanationProgressMonitor explanationMonitor;

    public ExplanationUtils(final PelletReasoner reasoner, final PelletReasonerFactory reasonerFactory,
            final ExplanationRenderer explanationRenderer, final ProgressMonitor monitor, final int maxExplanations)
    {
        this.maxExplanations = maxExplanations;
        this.reasoner = reasoner;
        this.reasonerFactory = reasonerFactory;
        if(monitor == null)
        {
            this.monitor = new NullProgressMonitor();
        }
        else
        {
            this.monitor = monitor;
        }

        // this.explanationMonitor = explanationMonitor;
        this.explanationRenderer = explanationRenderer;

        this.converter = new SatisfiabilityConverter(reasoner.getManager().getOWLDataFactory());
    }

    private Set<Set<OWLAxiom>> explainAxiom(final OWLAxiom axiom) throws OWLException
    {
        final MultipleExplanationGenerator expGen = new HSTExplanationGenerator(this.getSingleExplanationGenerator());
        final RendererExplanationProgressMonitor rendererMonitor =
                new RendererExplanationProgressMonitor(this.explanationRenderer, axiom);
        expGen.setProgressMonitor(rendererMonitor);

        final OWLClassExpression unsatClass = this.converter.convert(axiom);
        final Set<Set<OWLAxiom>> explanations = expGen.getExplanations(unsatClass, this.maxExplanations);

        if(explanations.isEmpty())
        {
            rendererMonitor.foundNoExplanations();
        }

        final int expSize = explanations.size();
        if(expSize == 0)
        {
            this.errorExpCount++;
        }
        else if(expSize == 1)
        {
            if(explanations.iterator().next().size() > 1)
            {
                this.multiAxiomExpCount++;
            }
        }
        else
        {
            this.multipleExpCount++;
        }

        return explanations;
    }

    public Set<Set<OWLAxiom>> explainClassHierarchy() throws OWLException
    {
        final Set<OWLClass> visited = new HashSet<OWLClass>();

        this.reasoner.flush();

        this.reasoner.getKB().classify();

        this.reasoner.getKB().realize();

        this.monitor.setMessage("Explaining");
        this.monitor.setProgress(this.reasoner.getRootOntology().getClassesInSignature().size());
        this.monitor.setStarted();

        final Set<Set<OWLAxiom>> result = new HashSet<Set<OWLAxiom>>();

        final Node<OWLClass> bottoms = this.reasoner.getEquivalentClasses(OWL.Nothing);
        result.addAll(this.explainClassHierarchy(OWL.Nothing, bottoms, visited));

        final Node<OWLClass> tops = this.reasoner.getEquivalentClasses(OWL.Thing);
        result.addAll(this.explainClassHierarchy(OWL.Thing, tops, visited));

        this.monitor.setFinished();

        return result;
    }

    private Set<Set<OWLAxiom>> explainClassHierarchy(final OWLClass cls, final Node<OWLClass> eqClasses,
            final Set<OWLClass> visited) throws OWLException
            {
        final Set<Set<OWLAxiom>> result = new HashSet<Set<OWLAxiom>>();

        if(visited.contains(cls))
        {
            return result;
        }

        visited.add(cls);
        visited.addAll(eqClasses.getEntities());

        for(final OWLClass eqClass : eqClasses)
        {
            // TODO: Support this
            // this.monitor.incrementProgress();

            result.addAll(this.explainEquivalentClass(cls, eqClass));
        }

        for(final OWLNamedIndividual ind : this.reasoner.getInstances(cls, true).getFlattened())
        {
            result.addAll(this.explainInstance(ind, cls));
        }

        final NodeSet<OWLClass> subClasses = this.reasoner.getSubClasses(cls, true);
        final Map<OWLClass, Node<OWLClass>> subClassEqs = new HashMap<OWLClass, Node<OWLClass>>();
        for(final Node<OWLClass> equivalenceSet : subClasses)
        {
            if(!equivalenceSet.isBottomNode())
            {
                final OWLClass subClass = equivalenceSet.getRepresentativeElement();
                subClassEqs.put(subClass, equivalenceSet);
                result.addAll(this.explainSubClass(subClass, cls));
            }
        }

        for(final Map.Entry<OWLClass, Node<OWLClass>> entry : subClassEqs.entrySet())
        {
            result.addAll(this.explainClassHierarchy(entry.getKey(), entry.getValue(), visited));
        }

        return result;
            }

    public Set<Set<OWLAxiom>> explainEquivalentClass(final OWLClass c1, final OWLClass c2) throws OWLException
    {
        final Set<Set<OWLAxiom>> result = new HashSet<Set<OWLAxiom>>();

        if(c1.equals(c2))
        {
            return result;
        }

        final OWLAxiom axiom = OWL.equivalentClasses(c1, c2);

        return this.explainAxiom(axiom);
    }

    public Set<Set<OWLAxiom>> explainInstance(final OWLIndividual ind, final OWLClass c) throws OWLException
    {
        final Set<Set<OWLAxiom>> result = new HashSet<Set<OWLAxiom>>();

        if(c.isOWLThing())
        {
            return result;
        }

        final OWLAxiom axiom = OWL.classAssertion(ind, c);

        return this.explainAxiom(axiom);
    }

    public Set<Set<OWLAxiom>> explainPropertyValue(final OWLIndividual s,
            @SuppressWarnings("rawtypes") final OWLProperty p, final OWLObject o) throws OWLException
            {
        if(p.isOWLObjectProperty())
        {
            return this.explainAxiom(OWL.propertyAssertion(s, (OWLObjectProperty)p, (OWLIndividual)o));
        }
        else
        {
            return this.explainAxiom(OWL.propertyAssertion(s, (OWLDataProperty)p, (OWLLiteral)o));
        }
            }

    public Set<Set<OWLAxiom>> explainSubClass(final OWLClass sub, final OWLClass sup) throws OWLException
    {
        final Set<Set<OWLAxiom>> result = new HashSet<Set<OWLAxiom>>();

        if(sub.equals(sup))
        {
            return result;
        }
        else if(sub.isOWLNothing())
        {
            return result;
        }
        else if(sup.isOWLThing())
        {
            return result;
        }

        final OWLSubClassOfAxiom axiom = OWL.subClassOf(sub, sup);
        return this.explainAxiom(axiom);
    }

    public Set<Set<OWLAxiom>> explainUnsatisfiableClass(final OWLClass cls) throws OWLException
    {
        return this.explainSubClass(cls, OWL.Nothing);
    }

    public Set<Set<OWLAxiom>> explainUnsatisfiableClasses() throws OWLException
    {
        final Set<Set<OWLAxiom>> result = new HashSet<Set<OWLAxiom>>();
        for(final OWLClass cls : this.reasoner.getEquivalentClasses(OWL.Nothing))
        {
            if(!cls.isOWLNothing())
            {
                result.addAll(this.explainUnsatisfiableClass(cls));
            }
        }
        return result;
    }

    private TransactionAwareSingleExpGen getSingleExplanationGenerator()
    {
        if(this.allowInconsistency)
        {
            return new GlassBoxExplanation(this.reasoner);
        }
        else
        {
            return new BlackBoxExplanation(this.reasoner.getRootOntology(), this.reasonerFactory, this.reasoner);
        }
    }

    public boolean getAllowInconsistency()
    {
        return this.allowInconsistency;
    }

    public void setAllowInconsistency(final boolean allowInconsistency)
    {
        this.allowInconsistency = allowInconsistency;
    }
}
