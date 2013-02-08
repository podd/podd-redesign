/**
 * 
 */
package com.github.podd.client.impl.restlet;

import java.io.InputStream;
import java.util.List;

import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.client.api.PoddClient;

/**
 * Restlet based PODD Client implementation.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class RestletPoddClientImpl implements PoddClient
{
    private String serverUrl = null;
    
    public RestletPoddClientImpl()
    {
    }
    
    public RestletPoddClientImpl(String serverUrl)
    {
        this.serverUrl = serverUrl;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#appendArtifact(org.semanticweb.owlapi.model.IRI,
     * java.io.InputStream, org.openrdf.rio.RDFFormat)
     */
    @Override
    public OWLOntologyID appendArtifact(IRI ontologyIRI, InputStream partialInputStream, RDFFormat format)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.client.api.PoddClient#attachFileReference(org.semanticweb.owlapi.model.IRI,
     * org.semanticweb.owlapi.model.IRI, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public OWLOntologyID attachFileReference(IRI ontologyIRI, IRI objectIRI, String label, String repositoryAlias,
            String filePathInRepository)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#deleteArtifact(org.semanticweb.owlapi.model.IRI)
     */
    @Override
    public boolean deleteArtifact(IRI ontologyIRI)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#getPoddServerUrl()
     */
    @Override
    public String getPoddServerUrl()
    {
        return serverUrl;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#listFileReferenceRepositories()
     */
    @Override
    public List<String> listFileReferenceRepositories()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#listPublishedArtifacts()
     */
    @Override
    public List<OWLOntologyID> listPublishedArtifacts()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#listUnpublishedArtifacts()
     */
    @Override
    public List<OWLOntologyID> listUnpublishedArtifacts()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#login(java.lang.String, char[])
     */
    @Override
    public boolean login(String username, char[] password)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#logout()
     */
    @Override
    public boolean logout()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#publishArtifact(org.semanticweb.owlapi.model.IRI)
     */
    @Override
    public OWLOntologyID publishArtifact(IRI ontologyIRI)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#setPoddServerUrl(java.lang.String)
     */
    @Override
    public void setPoddServerUrl(String serverUrl)
    {
        this.serverUrl = serverUrl;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.client.api.PoddClient#unpublishArtifact(org.semanticweb.owlapi.model.IRI)
     */
    @Override
    public OWLOntologyID unpublishArtifact(IRI ontologyIRI)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#updateArtifact(org.semanticweb.owlapi.model.IRI,
     * java.io.InputStream, org.openrdf.rio.RDFFormat)
     */
    @Override
    public OWLOntologyID updateArtifact(IRI ontologyIRI, InputStream fullInputStream, RDFFormat format)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#uploadNewArtifact(java.io.InputStream,
     * org.openrdf.rio.RDFFormat)
     */
    @Override
    public OWLOntologyID uploadNewArtifact(InputStream input, RDFFormat format)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
}
