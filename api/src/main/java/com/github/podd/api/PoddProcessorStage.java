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
package com.github.podd.api;

/**
 * An enumeration of the processing stages inside of the PODD system.
 * 
 * The core functionality for each stage is fixed in the PODD system, so without any processors, the
 * system will still function, but objects for file references, purls, etc., will not be created or
 * validated.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public enum PoddProcessorStage
{
    /**
     * Processing stage : Parsing RDF document into RDF triples.
     */
    RDF_PARSING,
    
    /**
     * Processing stage : Converting RDF triples to OWL axioms.
     */
    OWL_AXIOM,
    
    /**
     * Processing stage : Checking the OWL axioms against OWL2 Profiles.
     */
    PROFILE_CHECK,
    
    /**
     * Processing stage : Checking the OWL axioms for internal consistency using a reasoner.
     */
    CONSISTENCY_CHECK,
    
    /**
     * Processing stage : Storing the concrete OWL axioms in a database.
     */
    CONCRETE_AXIOM_STORAGE,
    
    /**
     * Processing stage : Inferring new statements to complement the concrete axioms.
     * 
     * NOTE: This stage may be deferred so that a response can be returned quickly.
     */
    INFERENCE,
    
    /**
     * Processing stage : Storing inferred axioms in a database.
     * 
     * NOTE: This stage may be deferred so that a response can be returned quickly.
     */
    INFERRED_AXIOM_STORAGE,
    
    ;
}
