/**
 * 
 */
package com.github.podd.exception;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;

import com.github.podd.api.file.DataReference;
import com.github.podd.utils.PoddRdfConstants;

/**
 * An exception that is thrown to indicate that a {@link DataReference} was deemed invalid by its
 * hosting File Repository.
 * 
 * @author kutila
 */
public class FileReferenceInvalidException extends PoddException
{
    
    private static final long serialVersionUID = -9203219163144536259L;
    
    private DataReference dataReference;
    
    public FileReferenceInvalidException(final DataReference dataReference, final String msg)
    {
        super(msg);
        this.dataReference = dataReference;
    }
    
    public FileReferenceInvalidException(final DataReference dataReference, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.dataReference = dataReference;
    }
    
    public FileReferenceInvalidException(final DataReference dataReference, final Throwable throwable)
    {
        super(throwable);
        this.dataReference = dataReference;
    }
    
    public DataReference getFileReference()
    {
        return this.dataReference;
    }
    
    @Override
    public Model getDetailsAsModel(final Resource errorResource) 
    {
        final Model model = super.getDetailsAsModel(errorResource);
        
        //FIXME - untested and incomplete
        final URI fileRefUri = this.getFileReference().getObjectIri().toOpenRDFURI();
        model.add(errorResource, PoddRdfConstants.ERR_SOURCE, fileRefUri);
        model.add(fileRefUri, RDFS.LABEL, PoddRdfConstants.VF.createLiteral(this.getFileReference().getLabel()));
    
        
        return model;
    }
    
}
