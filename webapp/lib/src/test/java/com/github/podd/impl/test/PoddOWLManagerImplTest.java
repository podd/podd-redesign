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
package com.github.podd.impl.test;

import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.test.AbstractPoddOWLManagerTest;
import com.github.podd.impl.PoddOWLManagerImpl;

/**
 * @author kutila
 * 
 */
public class PoddOWLManagerImplTest extends AbstractPoddOWLManagerTest
{
    private String reasonerName = "Pellet";
    
    @Override
    protected OWLReasonerFactory getNewOWLReasonerFactoryInstance()
    {
        return OWLReasonerFactoryRegistry.getInstance().getReasonerFactory(this.reasonerName);
    }
    
    @Override
    protected PoddOWLManager getNewPoddOWLManagerInstance(OWLOntologyManager manager, OWLReasonerFactory reasonerFactory)
    {
        return new PoddOWLManagerImpl(manager, reasonerFactory);
    }
    
}
