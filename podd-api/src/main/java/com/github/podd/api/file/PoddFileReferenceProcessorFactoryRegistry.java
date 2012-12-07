/**
 * 
 */
package com.github.podd.api.file;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.util.AbstractServiceLoader;

import com.github.podd.api.PoddProcessorStage;

/**
 * A registry containing dynamically loaded instances of {@link PoddFileReferenceProcessorFactory}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddFileReferenceProcessorFactoryRegistry extends
        AbstractServiceLoader<String, PoddFileReferenceProcessorFactory>
{
    private static final PoddFileReferenceProcessorFactoryRegistry instance =
            new PoddFileReferenceProcessorFactoryRegistry();
    
    /**
     * @return A static instance of this registry.
     */
    public static PoddFileReferenceProcessorFactoryRegistry getInstance()
    {
        return PoddFileReferenceProcessorFactoryRegistry.instance;
    }
    
    public PoddFileReferenceProcessorFactoryRegistry()
    {
        super(PoddFileReferenceProcessorFactory.class);
    }
    
    public final List<PoddFileReferenceProcessorFactory> getByStage(final PoddProcessorStage nextStage)
    {
        final List<PoddFileReferenceProcessorFactory> result = new ArrayList<PoddFileReferenceProcessorFactory>();
        for(final PoddFileReferenceProcessorFactory nextProcessor : this.getAll())
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
    protected final String getKey(final PoddFileReferenceProcessorFactory nextFactory)
    {
        return nextFactory.getKey();
    }
    
}
