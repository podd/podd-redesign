/**
 * PODD is an OWL ontology database used for scientific project management
 * 
 * Copyright (C) 2009-2013 The University Of Queensland
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.podd.impl.data;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;

import com.github.podd.api.data.DataReference;
import com.github.podd.api.file.PoddDataRepository;
import com.github.podd.exception.DataRepositoryIncompleteException;
import com.github.podd.utils.PODD;

/**
 * An abstract implementation of {@link PoddDataRepository} which maintains the <i>alias</i> and
 * <i>types</i>. All internal attributes required to construct a repository configuration are stored
 * in a {@link Model} object and should be validated by sub-classes.
 * 
 * @author kutila
 */
abstract class PoddFileRepositoryImpl<T extends DataReference> implements PoddDataRepository<DataReference>
{
    protected Model model;
    
    protected String alias;
    
    protected final Set<URI> types = Collections.newSetFromMap(new ConcurrentHashMap<URI, Boolean>());
    
    protected Resource aliasUri;
    
    /**
     * Sub-classes should first invoke this from their constructors and subsequently validate
     * sub-class specific attributes exist in the {@link Model}.
     * 
     * @param nextDataRepository
     *            The {@link Resource} identifying the data repository.
     * @param model
     *            A {@link Model} containing data to construct a File Repository configuration.
     * @throws DataRepositoryIncompleteException
     */
    protected PoddFileRepositoryImpl(final Resource nextDataRepository, final Model model)
        throws DataRepositoryIncompleteException
    {
        this.aliasUri = nextDataRepository;
        this.model = new LinkedHashModel(model.filter(nextDataRepository, null, null));
        
        // check that the model contains an "alias" and at least one "type"
        final Model aliasModel = this.model.filter(nextDataRepository, PODD.PODD_DATA_REPOSITORY_ALIAS, null);
        
        if(aliasModel.isEmpty())
        {
            throw new DataRepositoryIncompleteException("Model did not contain any aliases");
        }
        
        // alias
        this.alias = ((Literal)aliasModel.objects().iterator().next()).getLabel();
        if(this.alias.trim().isEmpty())
        {
            throw new DataRepositoryIncompleteException("File Repository Alias cannot be empty");
        }
        
        // types
        final Set<Value> typeValues = this.model.filter(this.aliasUri, RDF.TYPE, null).objects();
        for(final Value value : typeValues)
        {
            if(value instanceof URI)
            {
                this.types.add((URI)value);
            }
        }
        if(this.types.isEmpty())
        {
            throw new DataRepositoryIncompleteException("No File Repository type information found: alias="
                    + this.alias + " aliasUri=" + this.aliasUri);
        }
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
