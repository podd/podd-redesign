/**
 * 
 */
package com.github.podd.impl.file;

import java.util.Set;

import org.openrdf.model.URI;

import com.github.podd.api.file.PoddFileReference;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.api.file.PoddSSHFileReference;

/**
 * @author kutila
 * 
 */
public class SSHFileRepositoryImpl implements PoddFileRepository<PoddSSHFileReference>
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
    public boolean validate(PoddSSHFileReference reference)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public boolean canHandle(PoddSSHFileReference reference)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
}
