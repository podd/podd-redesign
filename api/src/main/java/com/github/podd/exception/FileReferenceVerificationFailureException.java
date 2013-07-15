/**
 * 
 */
package com.github.podd.exception;

import java.util.Map;

import com.github.podd.api.file.DataReference;

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
    
}
