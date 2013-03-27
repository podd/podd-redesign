package com.github.podd.impl.file;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.github.podd.api.file.PoddFileReferenceProcessor;
import com.github.podd.api.file.PoddSSHFileReference;
import com.github.podd.utils.PoddRdfConstants;

/**
 * Processor for File References of type <i>http://purl.org/podd/ns/poddBase#SSHFileReference</i>.
 *   
 * @author kutila
 */
public class SSHFileReferenceProcessorImpl implements PoddFileReferenceProcessor<PoddSSHFileReference>
{
    
    private final URI FILE_TYPE = PoddRdfConstants.PODDBASE_FILE_REFERENCE_TYPE_SSH;
    
    @Override
    public boolean canHandle(Model rdfStatements)
    {
        if (rdfStatements == null || rdfStatements.isEmpty())
        {
            return false;
        }
        
        Model matchingModels = rdfStatements.filter((Resource)null, null, this.FILE_TYPE);
        if (!matchingModels.isEmpty())
        {
            return true;
        }
        
        return false;
    }
    
    @Override
    public Collection<PoddSSHFileReference> createReferences(Model rdfStatements)
    {
        if (rdfStatements == null || rdfStatements.isEmpty())
        {
            return null;
        }
        
        // TODO 
        return null;
    }
    
    @Override
    public Set<URI> getTypes()
    {
        return Collections.singleton(this.FILE_TYPE);
    }
    
}
