/**
 * 
 */
package com.github.podd.exception;

import com.github.podd.api.file.FileReference;

/**
 * An exception that is thrown to indicate that a {@link FileReference} was deemed
 * invalid by its hosting File Repository. 
 * 
 * @author kutila
 */
public class FileReferenceInvalidException extends PoddException
{

    private static final long serialVersionUID = -9203219163144536259L;
    
    private FileReference fileReference;
    
    public FileReferenceInvalidException(final FileReference fileReference, final String msg)
    {
        super(msg);
        this.fileReference = fileReference;
    }

    public FileReferenceInvalidException(final FileReference fileReference, final Throwable throwable)
    {
        super(throwable);
        this.fileReference = fileReference;
    }
    
    public FileReferenceInvalidException(final FileReference fileReference, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.fileReference = fileReference;
    }

    
    public FileReference getFileReference()
    {
        return fileReference;
    }
    
}
