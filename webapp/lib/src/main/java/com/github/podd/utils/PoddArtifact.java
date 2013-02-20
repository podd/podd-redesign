/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.Model;
import org.openrdf.model.URI;

/**
 * Simple class to contain details about a Podd Artifact (i.e. Projects at present).
 * 
 * @author kutila
 * 
 *         TODO: Remove the need for this class.
 * 
 * @deprecated Only use PoddObject, and only use it when it is simpler than using
 *             {@link Model#filter(org.openrdf.model.Resource, URI, org.openrdf.model.Value, org.openrdf.model.Resource...)}
 *             .
 */
@Deprecated
public class PoddArtifact extends PoddObject
{
    
    private String description;
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
    
    public String getDescription()
    {
        return description;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
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
