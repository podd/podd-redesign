/**
 * 
 */
package com.github.podd.impl.file;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.file.PoddFileReference;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.api.file.PoddFileRepositoryManager;
import com.github.podd.exception.FileRepositoryMappingNotFoundException;
import com.github.podd.exception.PoddException;

/**
 * An implementation of FileRepositoryManager which uses an RDF repository graph as the backend
 * storage for maintaining information about Repository Configurations.
 * 
 * @author kutila
 */
public class PoddFileRepositoryManagerImpl implements PoddFileRepositoryManager
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private PoddRepositoryManager repositoryManager;

    public PoddFileRepositoryManagerImpl() 
    {
    }

    public void setRepositoryManager(PoddRepositoryManager repositoryManager)
    {
        this.repositoryManager = repositoryManager;
    }


    @Override
    public void addRepositoryMapping(String alias, PoddFileRepository repositoryConfiguration) throws OpenRDFException
    {
        // - TODO: validate FileRepository configuration
        if (repositoryConfiguration == null || alias == null)
        {
            throw new NullPointerException("Cannot add NULL as a File Repository mapping");
        }
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            URI context = this.repositoryManager.getFileRepositoryManagementGraph(); 
            // TODO add statements to file-repo-graph
        }
        finally
        {
            try
            {
                if(conn != null && conn.isActive())
                {
                    conn.rollback();
                }
                if(conn != null && conn.isOpen())
                {
                    conn.close();
                }
            }
            catch(RepositoryException e)
            {
                this.log.warn("Failed to close RepositoryConnection", e);
            }
        }
    }

    @Override
    public PoddFileRepository getRepository(String alias) throws FileRepositoryMappingNotFoundException, OpenRDFException
    {
        if(alias == null)
        {
            throw new FileRepositoryMappingNotFoundException(alias, "NULL is not a mapped File Repository");
        }
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            URI context = this.repositoryManager.getFileRepositoryManagementGraph(); 

            //TODO - retrieve mapping
            conn.getStatements(null, null, null, false, context);
            return null;
        }
        finally
        {
                if(conn != null && conn.isActive())
                {
                    conn.rollback();
                }
                if(conn != null && conn.isOpen())
                {
                    conn.close();
                }
        }
    }

    @Override
    public PoddFileRepository removeRepositoryMapping(String alias) throws FileRepositoryMappingNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getRepositoryAliases(PoddFileRepository repositoryConfiguration)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void verifyFileReferences(Set<PoddFileReference> fileReferenceResults) throws OpenRDFException,
        PoddException, FileRepositoryMappingNotFoundException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void downloadFileReference(PoddFileReference nextFileReference, OutputStream outputStream)
        throws PoddException, IOException
    {
        // TODO Auto-generated method stub
        
    }
    
}
