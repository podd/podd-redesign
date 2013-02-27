/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.URI;
import org.semanticweb.owlapi.model.IRI;

/**
 * Simple class to contain details about a Podd Artifact (i.e. Projects at present).
 * 
 * This must only be used at the last stage before presenting results to users. For all other cases,
 * {@link InferredOWLOntologyID} should be used to represent references to specific versions of
 * artifacts, and {@link IRI} should be used to represent references to either versions or ontology
 * IRIs before they are resolved to {@link InferredOWLOntologyID} instances.
 * 
 * @author kutila
 * 
 */
public class PoddArtifact extends PoddObject
{
    
    private String leadInstitution;
    
    /**
     * Constructor
     * 
     * @param uri
     */
    public PoddArtifact(URI uri)
    {
        super(uri);
    }
    
    public String getLeadInstitution()
    {
        return leadInstitution;
    }
    
    public void setLeadInstitution(String leadInstitution)
    {
        this.leadInstitution = leadInstitution;
    }
    
}
