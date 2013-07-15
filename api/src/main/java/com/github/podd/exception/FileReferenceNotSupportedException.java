/**
 * 
 */
package com.github.podd.exception;

import com.github.podd.api.file.DataReference;

/**
 * An exception that is thrown to indicate that validating a particular {@link DataReference} is not
 * supported by the FileRepository configuration which threw this Exception.
 * 
 * @author kutila
 */
public class FileReferenceNotSupportedException extends PoddException
{
    
    private static final long serialVersionUID = -4674378227045621468L;
    
    private DataReference dataReference;
    
    public FileReferenceNotSupportedException(final DataReference dataReference, final String msg)
    {
        super(msg);
        this.dataReference = dataReference;
    }
    
    public FileReferenceNotSupportedException(final DataReference dataReference, final String msg,
            final Throwable throwable)
    {
        super(msg, throwable);
        this.dataReference = dataReference;
    }
    
    public FileReferenceNotSupportedException(final DataReference dataReference, final Throwable throwable)
    {
        super(throwable);
        this.dataReference = dataReference;
    }
    
    public DataReference getFileReference()
    {
        return this.dataReference;
    }
    
}
