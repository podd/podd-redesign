/**
 * 
 */
package com.github.podd.exception;

import org.openrdf.model.Model;

/**
 * An exception that is thrown to indicate that there was insufficient information to construct a
 * file repository configuration from given data.
 * 
 * @author kutila
 */
public class FileRepositoryIncompleteException extends FileRepositoryException
{
    private static final long serialVersionUID = -689252740293644258L;
    
    private final Model model;
    
    /**
     * 
     * @param model
     *            The {@link Model} that did not contain sufficient data to create a valid
     *            FileRepository configuration.
     * @param msg
     *            The message for this exception.
     */
    public FileRepositoryIncompleteException(final Model model, final String msg)
    {
        super(msg);
        this.model = model;
    }
    
    /**
     * @param model
     *            The {@link Model} that did not contain sufficient data to create a valid
     *            FileRepository configuration.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public FileRepositoryIncompleteException(final Model model, final String msg, final Throwable throwable)
    {
        super(msg, throwable);
        this.model = model;
    }
    
    /**
     * @param model
     *            The {@link Model} that did not contain sufficient data to create a valid
     *            FileRepository configuration.
     * @param throwable
     *            The cause for this exception.
     */
    public FileRepositoryIncompleteException(final Model model, final Throwable throwable)
    {
        super(throwable);
        this.model = model;
    }
    
    /**
     * @return The {@link Model} that did not contain sufficient data to create a valid
     *         FileRepository configuration.
     */
    public Model getModel()
    {
        return this.model;
    }
    
}
