/**
 * 
 */
package com.github.podd.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.exception.EmptyOntologyException;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.UnmanagedSchemaException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.exception.UnmanagedSchemaOntologyIDException;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddSchemaManagerImpl implements PoddSchemaManager
{
    
    private PoddRepositoryManager repositoryManager;
    private PoddOWLManager owlManager;
    private URI schemaManagementContext;
    
    /**
     * 
     */
    public PoddSchemaManagerImpl()
    {
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void downloadSchemaOntology(final OWLOntologyID schemaOntologyID, final OutputStream outputStream,
            final RDFFormat format, final boolean includeInferences) throws UnmanagedSchemaException
    {
        throw new RuntimeException("TODO: Implement downloadSchemaOntology");
    }
    
    @Override
    public InferredOWLOntologyID getCurrentSchemaOntologyVersion(final IRI schemaOntologyIRI)
        throws UnmanagedSchemaIRIException
    {
        throw new RuntimeException("TODO: Implement getCurrentSchemaOntologyVersion");
    }
    
    @Override
    public OWLOntology getSchemaOntology(final IRI schemaOntologyIRI) throws UnmanagedSchemaIRIException
    {
        throw new RuntimeException("TODO: Implement getSchemaOntology(IRI)");
    }
    
    @Override
    public OWLOntology getSchemaOntology(final OWLOntologyID schemaOntologyID)
        throws UnmanagedSchemaOntologyIDException
    {
        throw new RuntimeException("TODO: Implement getSchemaOntology(OWLOntologyID)");
    }
    
    @Override
    public void setCurrentSchemaOntologyVersion(final OWLOntologyID schemaOntologyID)
        throws UnmanagedSchemaOntologyIDException, IllegalArgumentException
    {
        throw new RuntimeException("TODO: Implement setCurrentSchemaOntologyVersion");
    }
    
    @Override
    public void setOwlManager(final PoddOWLManager owlManager)
    {
        this.owlManager = owlManager;
    }
    
    @Override
    public void setRepositoryManager(final PoddRepositoryManager repositoryManager)
    {
        this.repositoryManager = repositoryManager;
    }
    
    @Override
    public void setSchemaManagementContext(final URI context)
    {
        this.schemaManagementContext = context;
    }
    
    @Override
    public InferredOWLOntologyID uploadSchemaOntology(final InputStream inputStream, final RDFFormat fileFormat)
        throws OpenRDFException, IOException, OWLException, PoddException
    {
        return this.uploadSchemaOntology(null, inputStream, fileFormat);
    }
    
    @Override
    public InferredOWLOntologyID uploadSchemaOntology(final OWLOntologyID schemaOntologyID,
            final InputStream inputStream, final RDFFormat fileFormat) throws OpenRDFException, IOException,
        OWLException, PoddException
    {
        if(inputStream == null)
        {
            throw new NullPointerException("Schema Ontology input stream was null");
        }
        
        OWLOntologyDocumentSource owlSource = new StreamDocumentSource(inputStream, fileFormat.getDefaultMIMEType());
        OWLOntology ontology = this.owlManager.loadOntology(owlSource);
        
        if(ontology.isEmpty())
        {
            throw new EmptyOntologyException(ontology, "Schema Ontology contained no axioms");
        }
        
        RepositoryConnection conn = null;
        
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            
            this.owlManager.dumpOntologyToRepository(ontology, conn);
            
            
        }
        catch(OpenRDFException | IOException e)
        {
            if(conn != null && conn.isActive())
            {
                conn.rollback();
            }
        }
        finally
        {
            if(conn != null && conn.isOpen())
            {
                conn.close();
            }
        }
        
        throw new RuntimeException("TODO: Implement uploadSchemaOntology(OWLOntologyID,InputStream,RDFFormat)");
    }
}
