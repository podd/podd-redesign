/**
 * 
 */
package com.github.podd.api.file;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Model;

import com.github.ansell.abstractserviceloader.AbstractServiceLoader;
import com.github.podd.api.PoddProcessorStage;

/**
 * A registry containing dynamically loaded instances of {@link DataReferenceProcessorFactory}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddDataRepositoryRegistry extends AbstractServiceLoader<String, PoddDataRepositoryFactory>
{
    private static final PoddDataRepositoryRegistry instance = new PoddDataRepositoryRegistry();
    
    /**
     * @return A static instance of this registry.
     */
    public static PoddDataRepositoryRegistry getInstance()
    {
        return PoddDataRepositoryRegistry.instance;
    }
    
    public PoddDataRepositoryRegistry()
    {
        super(PoddDataRepositoryFactory.class);
    }
    
    @Override
    public final String getKey(final PoddDataRepositoryFactory nextFactory)
    {
        return nextFactory.getKey();
    }
    
    public PoddDataRepository<?> createDataRepository(Model model)
    {
        throw new RuntimeException("TODO: Implement me");
    }
    
}
