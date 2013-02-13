package com.github.podd.prototype;

/**
 * This class extends <code>java.lang.Exception</code> to provide a PODD specific exception class.
 * 
 * @author kutila
 * 
 */
public class PoddException extends Exception
{
    
    /** Unsupported OWL Profile. */
    public static final int ERR_PROFILE_NOT_FOUND = 03000;
    
    /** Ontology not in required OWL Profile. */
    public static final int ERR_ONTOLOGY_NOT_IN_PROFILE = 03001;
    
    /** Ontology not consistent against OWL Profile. */
    public static final int ERR_INCONSISTENT_ONTOLOGY = 03002;
    
    /** Ontology not consistent against OWL Profile. */
    public static final int ERR_EMPTY_ONTOLOGY = 03003;
    
    private static final long serialVersionUID = 804321L;
    
    private final Object details;
    private final int code;
    
    /**
     * Create a new <code>PoddException</code> with details.
     * 
     * @param msg
     *            A message describing the Exception.
     * @param details
     *            An object containing details of the underlying cause.
     * @param code
     *            A numeric code describing the Exception.
     */
    public PoddException(final String msg, final Object details, final int code)
    {
        super(msg);
        this.details = details;
        this.code = code;
    }
    
    /**
     * Create a new <code>PoddException</code> with details.
     * 
     * @param msg
     *            A message describing the Exception.
     * @param details
     *            An object containing details of the underlying cause.
     * @param code
     *            A numeric code describing the Exception.
     */
    public PoddException(final String msg, final Throwable cause, final Object details, final int code)
    {
        super(msg, cause);
        this.details = details;
        this.code = code;
    }
    
    public Object getDetails()
    {
        return this.details;
    }
    
    public int getCode()
    {
        return this.code;
    }
    
}
