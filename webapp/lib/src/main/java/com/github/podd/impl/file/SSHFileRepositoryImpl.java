/**
 * 
 */
package com.github.podd.impl.file;

import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.URI;

import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.exception.IncompleteFileRepositoryException;

/**
 * @author kutila
 * 
 */
public class SSHFileRepositoryImpl<SSHFileReference> implements PoddFileRepository<FileReference>
{
    
    
    public SSHFileRepositoryImpl(Model model) throws IncompleteFileRepositoryException
    {
        //super(model);
        // TODO 
    }

    @Override
    public boolean canHandle(FileReference reference)
    {
        if (reference == null)
        {
            return false;
        }
        
        String aliasFromFileRef = reference.getRepositoryAlias();
        if (aliasFromFileRef == null)// || !alias.equals(aliasFromFileRef))
        {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean validate(FileReference reference)
    {
        // TODO 
        return false;
    }

    @Override
    public String getAlias()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<URI> getTypes()
    {
        // TODO Auto-generated method stub
        return null;
    }

    
}
