/**
 * 
 */
package com.github.podd.impl.file;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.FileReferenceConstants;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.exception.IncompleteFileRepositoryException;

/**
 * @author kutila
 *
 */
public abstract class PoddFileRepositoryImpl<T extends FileReference> implements PoddFileRepository<FileReference>
{
    protected Model model;

    protected String alias;
    
    protected final Set<URI> types = Collections.newSetFromMap(new ConcurrentHashMap<URI, Boolean>());
    
    /**
     * Sub-classes should first invoke this from their constructors and subsequently validate
     * sub-class specific attributes exist in the {@link Model}.
     * 
     * @param model
     *            A {@link Model} containing data to construct a File Repository configuration.
     * @throws IncompleteFileRepositoryException
     */
    public PoddFileRepositoryImpl(Model model) throws IncompleteFileRepositoryException
    {
        // validate all required properties for this Repository type are present
        try
        {   
            // alias
            this.alias = model.filter(null, FileReferenceConstants.PODD_FILE_REPOSITORY_ALIAS, null).objectString();
            if (this.alias == null || this.alias.trim().length() < 1)
            {
                throw new IncompleteFileRepositoryException(model, "File Repository Alias cannot be NULL/empty");
            }
            
            // types
            Set<Value> typeValues = model.filter(null, RDF.TYPE, null).objects();
            for(Iterator<Value> iterator = typeValues.iterator(); iterator.hasNext();)
            {
                Value value = iterator.next();
                if (value instanceof URI)
                {
                   types.add((URI)value); 
                }
            }
            if (types.isEmpty())
            {
                throw new IncompleteFileRepositoryException(model, "No FileRepsitoryType information found");
            }
            
            this.model = model;
        }
        catch (Exception e)
        {
            throw new IncompleteFileRepositoryException(model, "Could not construct a valid FileRepository configuration", e);
        }
    }
    
    
    @Override
    public String getAlias()
    {
        return this.alias;
    }
    
    @Override
    public Set<URI> getTypes()
    {
        return this.types;
    }

}
