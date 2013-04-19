/**
 * 
 */
package com.github.podd.api.file;

import java.util.ArrayList;
import java.util.List;

import com.github.ansell.abstractserviceloader.AbstractServiceLoader;
import com.github.podd.api.PoddProcessorStage;

/**
 * A registry containing dynamically loaded instances of {@link FileReferenceProcessorFactory}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class FileReferenceProcessorFactoryRegistry extends
        AbstractServiceLoader<String, FileReferenceProcessorFactory>
{
    private static final FileReferenceProcessorFactoryRegistry instance =
            new FileReferenceProcessorFactoryRegistry();
    
    /**
     * @return A static instance of this registry.
     */
    public static FileReferenceProcessorFactoryRegistry getInstance()
    {
        return FileReferenceProcessorFactoryRegistry.instance;
    }
    
    public FileReferenceProcessorFactoryRegistry()
    {
        super(FileReferenceProcessorFactory.class);
    }
    
    public final List<FileReferenceProcessorFactory> getByStage(final PoddProcessorStage nextStage)
    {
        final List<FileReferenceProcessorFactory> result = new ArrayList<FileReferenceProcessorFactory>();
        for(final FileReferenceProcessorFactory nextProcessor : this.getAll())
        {
            if(nextProcessor.canHandleStage(nextStage))
            {
                result.add(nextProcessor);
            }
        }
        
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.semanticweb.owlapi.util.AbstractServiceLoader#getKey(java.lang.Object)
     */
    @Override
    public final String getKey(final FileReferenceProcessorFactory nextFactory)
    {
        return nextFactory.getKey();
    }
    
}
