/**
 * 
 */
package com.github.podd.api;

/**
 * An enumeration of the processing stages inside of the PODD system. Events are generated
 * immediately before and after each stage to allow processors to modify the input or results of
 * each stage.
 * 
 * The core functionality for each stage is fixed in the PODD system, so without any processors, the
 * system will still function.
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
