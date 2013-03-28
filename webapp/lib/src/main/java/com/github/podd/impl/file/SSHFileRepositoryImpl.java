/**
 * 
 */
package com.github.podd.impl.file;

import java.util.Set;

import org.openrdf.model.URI;

import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.api.file.SSHFileReference;

/**
 * @author kutila
 * 
 */
public class SSHFileRepositoryImpl implements PoddFileRepository<SSHFileReference>
{
    
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
    
    @Override
    public boolean validate(SSHFileReference reference)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public boolean canHandle(SSHFileReference reference)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
}
