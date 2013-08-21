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

import java.util.Iterator;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import com.github.podd.api.file.DataReference;
import com.github.podd.utils.PoddRdfConstants;

/**
 * An exception that is thrown to indicate that validating a set of {@link DataReference}s resulted
 * in failures. <br>
 * The {@link Map} <code>errors</code> contains FileReferences and Exceptions describing the reasons
 * for their validation failure.
 * 
 * @author kutila
 */
public class FileReferenceVerificationFailureException extends PoddException
{
    
    private static final long serialVersionUID = 570735415625742494L;
    
    private Map<DataReference, Throwable> validationFailures;
    
    public FileReferenceVerificationFailureException(final Map<DataReference, Throwable> validationFailures,
            final String msg)
    {
        super(msg);
        this.validationFailures = validationFailures;
    }
    
    public FileReferenceVerificationFailureException(final Map<DataReference, Throwable> validationFailures,
            final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.validationFailures = validationFailures;
    }
    
    public FileReferenceVerificationFailureException(final Map<DataReference, Throwable> validationFailures,
            final Throwable throwable)
    {
        super(throwable);
        this.validationFailures = validationFailures;
    }
    
    public Map<DataReference, Throwable> getValidationFailures()
    {
        return this.validationFailures;
    }
 
    @Override
    public Model getDetailsAsModel(final Resource errorResource) 
    {
        final Model model = super.getDetailsAsModel(errorResource);
        
        //FIXME - untested and incomplete
        final Map<DataReference, Throwable> validationFailures = this.getValidationFailures();
        Iterator<DataReference> iterator = validationFailures.keySet().iterator();
        while (iterator.hasNext())
        {
            final DataReference dataReference = iterator.next();
            Throwable throwable = validationFailures.get(dataReference);
            //TODO
            model.add(dataReference.getObjectIri().toOpenRDFURI(), RDFS.LABEL, PoddRdfConstants.VF.createLiteral(throwable.getMessage()));
            dataReference.getLabel();
            
            final BNode v = PoddRdfConstants.VF.createBNode();
            model.add(errorResource, PoddRdfConstants.ERR_CONTAINS, v);
            model.add(v, RDF.TYPE, PoddRdfConstants.ERR_TYPE_ERROR);
            
            final URI dataRefUri = dataReference.getObjectIri().toOpenRDFURI();
            model.add(v, PoddRdfConstants.ERR_SOURCE, dataRefUri);
            model.add(dataRefUri, RDFS.LABEL, PoddRdfConstants.VF.createLiteral(dataReference.getLabel()));
            model.add(dataRefUri, RDFS.COMMENT, PoddRdfConstants.VF.createLiteral(throwable.getMessage()));
        }
        
        return model;
    }
    
}
