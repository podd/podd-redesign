/**
 *
 */
package com.github.podd.client.api;

import java.util.Objects;

import org.openrdf.model.URI;

import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Placeholder for top level information about artifacts, including the artifact ID, the top object
 * URI and the barcode for the top object.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public final class PoddArtifact
{
    public final InferredOWLOntologyID artifactID;
    public final URI topObjectUri;
    public final String topObjectBarcode;
    
    private PoddArtifact(final InferredOWLOntologyID artifactID, final URI projectUri, final String projectBarcode)
    {
        this.artifactID = Objects.requireNonNull(artifactID, "Artifact ID must not be null");
        this.topObjectUri = Objects.requireNonNull(projectUri, "Project URI must not be null");
        this.topObjectBarcode = Objects.requireNonNull(projectBarcode, "Project Barcode must not be null");
    }
    
    public static PoddArtifact from(final InferredOWLOntologyID artifactID, final URI projectUri,
            final String projectBarcode)
    {
        return new PoddArtifact(artifactID, projectUri, projectBarcode);
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.artifactID == null) ? 0 : this.artifactID.hashCode());
        result = prime * result + ((this.topObjectBarcode == null) ? 0 : this.topObjectBarcode.hashCode());
        result = prime * result + ((this.topObjectUri == null) ? 0 : this.topObjectUri.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(final Object obj)
    {
        if(this == obj)
        {
            return true;
        }
        if(obj == null)
        {
            return false;
        }
        if(!(obj instanceof PoddArtifact))
        {
            return false;
        }
        final PoddArtifact other = (PoddArtifact)obj;
        if(this.artifactID == null)
        {
            if(other.artifactID != null)
            {
                return false;
            }
        }
        else if(!this.artifactID.equals(other.artifactID))
        {
            return false;
        }
        if(this.topObjectBarcode == null)
        {
            if(other.topObjectBarcode != null)
            {
                return false;
            }
        }
        else if(!this.topObjectBarcode.equals(other.topObjectBarcode))
        {
            return false;
        }
        if(this.topObjectUri == null)
        {
            if(other.topObjectUri != null)
            {
                return false;
            }
        }
        else if(!this.topObjectUri.equals(other.topObjectUri))
        {
            return false;
        }
        return true;
    }
}
