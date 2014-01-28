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
package com.github.podd.exception;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;

import com.github.podd.api.data.DataReference;
import com.github.podd.utils.PODD;

/**
 * An exception that is thrown to indicate that a {@link DataReference} was deemed invalid by its
 * hosting File Repository.
 * 
 * @author kutila
 */
public class DataReferenceInvalidException extends PoddException
{
    
    private static final long serialVersionUID = -9203219163144536259L;
    
    private DataReference dataReference;
    
    public DataReferenceInvalidException(final DataReference dataReference, final String msg)
    {
        super(msg);
        this.dataReference = dataReference;
    }
    
    public DataReferenceInvalidException(final DataReference dataReference, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.dataReference = dataReference;
    }
    
    public DataReferenceInvalidException(final DataReference dataReference, final Throwable throwable)
    {
        super(throwable);
        this.dataReference = dataReference;
    }
    
    @Override
    public Model getDetailsAsModel(final Resource errorResource)
    {
        final Model model = super.getDetailsAsModel(errorResource);
        
        // FIXME - untested and incomplete
        final URI fileRefUri = this.getFileReference().getObjectIri().toOpenRDFURI();
        model.add(errorResource, PODD.ERR_SOURCE, fileRefUri);
        model.add(fileRefUri, RDFS.LABEL, PODD.VF.createLiteral(this.getFileReference().getLabel()));
        
        return model;
    }
    
    public DataReference getFileReference()
    {
        return this.dataReference;
    }
    
}
