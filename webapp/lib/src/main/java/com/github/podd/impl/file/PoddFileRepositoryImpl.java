/**
 * 
 */
package com.github.podd.impl.file;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.exception.FileRepositoryIncompleteException;
import com.github.podd.utils.PoddRdfConstants;

/**
 * An abstract implementation of {@link PoddFileRepository} which maintains the <i>alias</i> and
 * <i>types</i>. All internal attributes required to construct a repository configuration are stored
 * in a {@link Model} object and should be validated by sub-classes.
 * 
 * @author kutila
 */
public abstract class PoddFileRepositoryImpl<T extends FileReference> implements PoddFileRepository<FileReference>
{
    protected Model model;
    
    protected String alias;
    
    protected final Set<URI> types = Collections.newSetFromMap(new ConcurrentHashMap<URI, Boolean>());
    
    protected Resource aliasUri;
    
    /**
     * Sub-classes should first invoke this from their constructors and subsequently validate
     * sub-class specific attributes exist in the {@link Model}.
     * 
     * @param model
     *            A {@link Model} containing data to construct a File Repository configuration.
     * @throws FileRepositoryIncompleteException
     */
    public PoddFileRepositoryImpl(final Model model) throws FileRepositoryIncompleteException
    {
        // check that the model contains an "alias" and at least one "type"
        final Model aliasModel = model.filter(null, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, null);
        
        if(aliasModel.size() != 1)
        {
            throw new FileRepositoryIncompleteException(model, "Model should have exactly 1 alias");
        }
        
        // alias
        this.alias = aliasModel.objectString();
        if(this.alias == null || this.alias.trim().length() < 1)
        {
            throw new FileRepositoryIncompleteException(model, "File Repository Alias cannot be NULL/empty");
        }
        
        this.aliasUri = aliasModel.subjects().iterator().next();
        
        // types
        final Set<Value> typeValues = model.filter(this.aliasUri, RDF.TYPE, null).objects();
        for(Value value : typeValues)
        {
            if(value instanceof URI)
            {
                this.types.add((URI)value);
            }
        }
        if(this.types.isEmpty())
        {
            throw new FileRepositoryIncompleteException(model, "No FileRepsitoryType information found");
        }
        
        this.model = model;
    }
    
    @Override
    public String getAlias()
    {
        return this.alias;
    }
    
    @Override
    public Model getAsModel()
    {
        return this.model;
    }
    
    @Override
    public Set<URI> getTypes()
    {
        return this.types;
    }
    
}
