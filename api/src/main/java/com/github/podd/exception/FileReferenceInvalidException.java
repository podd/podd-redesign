/**
 * 
 */
package com.github.podd.exception;

import com.github.podd.api.file.DataReference;

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
    
}
