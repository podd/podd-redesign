/**
 * 
 */
package com.github.podd.impl.file;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.api.file.PoddFileRepositoryManager;
import com.github.podd.exception.FileRepositoryMappingExistsException;
import com.github.podd.exception.FileRepositoryMappingNotFoundException;
import com.github.podd.exception.PoddException;
import com.github.podd.utils.PoddRdfConstants;

/**
 * An implementation of FileRepositoryManager which uses an RDF repository graph as the back-end
 * storage for maintaining information about Repository Configurations.
 * 
 * @author kutila
 */
public class PoddFileRepositoryManagerImpl implements PoddFileRepositoryManager<FileReference>
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
    public void addRepositoryMapping(String alias, PoddFileRepository<FileReference> repositoryConfiguration)
        throws OpenRDFException, FileRepositoryMappingExistsException
    {
        this.addRepositoryMapping(alias, repositoryConfiguration, false);
    }    
    
    @Override
    public void addRepositoryMapping(String alias, PoddFileRepository<FileReference> repositoryConfiguration, boolean overwrite)
        throws OpenRDFException, FileRepositoryMappingExistsException
    {
        // - TODO: validate FileRepository configuration
        if (repositoryConfiguration == null || alias == null)
        {
            throw new NullPointerException("Cannot add NULL as a File Repository mapping");
        }

        // - check if a mapping with this alias already exists
        try
        {
            if (this.getRepository(alias) != null)
            {
                if (overwrite)
                {
                    this.removeRepositoryMapping(alias);
                }
                else
                {
                    throw new FileRepositoryMappingExistsException(alias, "File Repository mapping with this alias already exists");
                }
            }
        }
        catch (FileRepositoryMappingNotFoundException e)
        {
            // mapping doesn't exist, we can proceed to add a new one.
        }
        
        URI context = this.repositoryManager.getFileRepositoryManagementGraph(); 
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();

            Model model = repositoryConfiguration.getAsModel();
            conn.add(model, context);
            conn.commit();
        }
        catch (Exception e)
        {
            conn.rollback();
            throw e;
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
    public PoddFileRepository<FileReference> getRepository(String alias) throws FileRepositoryMappingNotFoundException, OpenRDFException
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
            conn.getStatements(null, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, new LiteralImpl(alias), false, context);
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
    public PoddFileRepository<FileReference> removeRepositoryMapping(String alias) throws FileRepositoryMappingNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getRepositoryAliases(PoddFileRepository<FileReference> repositoryConfiguration)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void verifyFileReferences(Set<FileReference> fileReferenceResults) throws OpenRDFException,
        PoddException, FileRepositoryMappingNotFoundException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void downloadFileReference(FileReference nextFileReference, OutputStream outputStream)
        throws PoddException, IOException
    {
        // TODO Auto-generated method stub
        
    }
    
}
