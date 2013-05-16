/**
 * 
 */
package com.github.podd.exception;

import java.util.Map;

import com.github.podd.api.file.FileReference;

/**
 * An exception that is thrown to indicate that validating a set of {@link FileReference}s resulted
 * in failures. <br>
 * The {@link Map} <code>errors</code> contains FileReferences and Exceptions describing the reasons
 * for their validation failure.
 * 
 * @author kutila
 */
public class FileReferenceVerificationFailureException extends PoddException
{
    
    private static final long serialVersionUID = 570735415625742494L;
    
    private Map<FileReference, Throwable> validationFailures;
    
    public FileReferenceVerificationFailureException(final Map<FileReference, Throwable> validationFailures,
            final String msg)
    {
        super(msg);
        this.validationFailures = validationFailures;
    }
    
    public FileReferenceVerificationFailureException(final Map<FileReference, Throwable> validationFailures,
            final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.validationFailures = validationFailures;
    }
    
    public FileReferenceVerificationFailureException(final Map<FileReference, Throwable> validationFailures,
            final Throwable throwable)
    {
        super(throwable);
        this.validationFailures = validationFailures;
    }
    
    public Map<FileReference, Throwable> getValidationFailures()
    {
        return this.validationFailures;
    }
    
}
