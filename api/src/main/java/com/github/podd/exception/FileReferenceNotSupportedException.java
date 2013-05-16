/**
 * 
 */
package com.github.podd.exception;

import com.github.podd.api.file.FileReference;

/**
 * An exception that is thrown to indicate that validating a particular {@link FileReference} is not
 * supported by the FileRepository configuration which threw this Exception.
 * 
 * @author kutila
 */
public class FileReferenceNotSupportedException extends PoddException
{
    
    private static final long serialVersionUID = -4674378227045621468L;
    
    private FileReference fileReference;
    
    public FileReferenceNotSupportedException(final FileReference fileReference, final String msg)
    {
        super(msg);
        this.fileReference = fileReference;
    }
    
    public FileReferenceNotSupportedException(final FileReference fileReference, final String msg,
            final Throwable throwable)
    {
        super(msg, throwable);
        this.fileReference = fileReference;
    }
    
    public FileReferenceNotSupportedException(final FileReference fileReference, final Throwable throwable)
    {
        super(throwable);
        this.fileReference = fileReference;
    }
    
    public FileReference getFileReference()
    {
        return this.fileReference;
    }
    
}
