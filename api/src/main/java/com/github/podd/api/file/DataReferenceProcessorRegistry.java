/**
 * 
 */
package com.github.podd.api.file;

import java.util.ArrayList;
import java.util.List;

import com.github.ansell.abstractserviceloader.AbstractServiceLoader;
import com.github.podd.api.PoddProcessorStage;

/**
 * A registry containing dynamically loaded instances of {@link DataReferenceProcessorFactory}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class DataReferenceProcessorRegistry extends AbstractServiceLoader<String, DataReferenceProcessorFactory>
{
    private static final DataReferenceProcessorRegistry instance = new DataReferenceProcessorRegistry();
    
    /**
     * @return A static instance of this registry.
     */
    public static DataReferenceProcessorRegistry getInstance()
    {
        return DataReferenceProcessorRegistry.instance;
    }
    
    public DataReferenceProcessorRegistry()
    {
        super(DataReferenceProcessorFactory.class);
    }
    
    public final List<DataReferenceProcessorFactory> getByStage(final PoddProcessorStage nextStage)
    {
        final List<DataReferenceProcessorFactory> result = new ArrayList<DataReferenceProcessorFactory>();
        for(final DataReferenceProcessorFactory nextProcessor : this.getAll())
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
    public final String getKey(final DataReferenceProcessorFactory nextFactory)
    {
        return nextFactory.getKey();
    }
    
}
