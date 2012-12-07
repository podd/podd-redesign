/**
 * 
 */
package com.github.podd.impl;

import java.io.InputStream;

import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.file.PoddFileReferenceManager;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Implementation of the PODD Artifact Manager API, to manage the lifecycle for PODD Artifacts.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddArtifactManagerImpl implements PoddArtifactManager
{
    
    private PoddFileReferenceManager fileReferenceManager;
    private PoddOWLManager owlManager;
    private PoddPurlManager purlManager;

    /**
     * 
     */
    public PoddArtifactManagerImpl()
    {
        // TODO Auto-generated constructor stub
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#getFileReferenceManager()
     */
    @Override
    public PoddFileReferenceManager getFileReferenceManager()
    {
        return this.fileReferenceManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#getOWLManager()
     */
    @Override
    public PoddOWLManager getOWLManager()
    {
        return this.owlManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#getPurlManager()
     */
    @Override
    public PoddPurlManager getPurlManager()
    {
        return this.purlManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream,
     * org.openrdf.rio.RDFFormat)
     */
    @Override
    public InferredOWLOntologyID loadArtifact(InputStream inputStream, RDFFormat format)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#publishArtifact(org.semanticweb.owlapi.model.
     * OWLOntologyID)
     */
    @Override
    public InferredOWLOntologyID publishArtifact(OWLOntologyID ontologyId)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.PoddArtifactManager#setFileReferenceManager(com.github.podd.api.file.
     * PoddFileReferenceManager)
     */
    @Override
    public void setFileReferenceManager(PoddFileReferenceManager fileManager)
    {
        this.fileReferenceManager = fileManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.PoddArtifactManager#setOwlManager(com.github.podd.api.PoddOWLManager)
     */
    @Override
    public void setOwlManager(PoddOWLManager owlManager)
    {
        this.owlManager = owlManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.PoddArtifactManager#setPurlManager(com.github.podd.api.purl.PoddPurlManager
     * )
     */
    @Override
    public void setPurlManager(PoddPurlManager purlManager)
    {
        this.purlManager = purlManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddArtifactManager#updateSchemaImport(org.semanticweb.owlapi.model.
     * OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID)
     */
    @Override
    public void updateSchemaImport(OWLOntologyID artifactId, OWLOntologyID schemaOntologyId)
    {
        // TODO Auto-generated method stub
        
    }
    
}
