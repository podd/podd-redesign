/**
 * 
 */
package com.github.podd.impl.file;

import org.openrdf.model.Model;

import com.github.podd.api.file.FileReference;
import com.github.podd.exception.IncompleteFileRepositoryException;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author kutila
 * 
 */
public class SSHFileRepositoryImpl<SSHFileReference> extends PoddFileRepositoryImpl<FileReference>
{
    public final static String PROTOCOL_SSH = "SSH";
    
    public SSHFileRepositoryImpl(final Model model) throws IncompleteFileRepositoryException
    {
        super(model);
        
        // check that the model contains values for protocol, host, port, fingerprint, username, and
        // secret
        final String protocol =
                model.filter(super.aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_PROTOCOL, null).objectString();
        final String host =
                model.filter(super.aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_HOST, null).objectString();
        final String port =
                model.filter(super.aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_PORT, null).objectString();
        final String fingerprint =
                model.filter(super.aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, null).objectString();
        final String username =
                model.filter(super.aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, null).objectString();
        final String secret =
                model.filter(super.aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, null).objectString();
        
        if(protocol == null || host == null || port == null || fingerprint == null || username == null
                || secret == null)
        {
            throw new IncompleteFileRepositoryException(model, "SSH repository configuration incomplete");
        }
        
        if(!SSHFileRepositoryImpl.PROTOCOL_SSH.equals(protocol))
        {
            throw new IncompleteFileRepositoryException(model, "Protocol needs to be SSH");
        }
    }
    
    @Override
    public boolean canHandle(final FileReference reference)
    {
        if(reference == null)
        {
            return false;
        }
        
        // unnecessary as Generics ensure only an SSHFileReference can be passed in
        if(!(reference instanceof com.github.podd.api.file.SSHFileReference))
        {
            return false;
        }
        
        final String aliasFromFileRef = reference.getRepositoryAlias();
        if(aliasFromFileRef == null || !this.alias.equals(aliasFromFileRef))
        {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean validate(final FileReference reference)
    {
        // TODO
        return false;
    }
    
}
