/*
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

import org.openrdf.model.URI;

import com.github.podd.api.purl.PoddPurlProcessor;

/**
 * This exception indicates that a Purl Generator was not able to handle the generation of a
 * Permanent URL.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PurlProcessorNotHandledException extends PoddException
{
    private static final long serialVersionUID = -8569720976338731517L;
    
    private final PoddPurlProcessor generator;
    private final URI inputUri;
    
    /**
     * 
     * @param generator
     *            The PURL Generator that was not able to handle the given input URI.
     * @param inputUri
     *            The input URI that failed to be handled by the given PURL Generator.
     * @param msg
     *            The message for this exception.
     */
    public PurlProcessorNotHandledException(final PoddPurlProcessor generator, final URI inputUri, final String msg)
    {
        super(msg);
        this.generator = generator;
        this.inputUri = inputUri;
    }
    
    /**
     * @param generator
     *            The PURL Generator that was not able to handle the given input URI.
     * @param inputUri
     *            The input URI that failed to be handled by the given PURL Generator.
     * @param msg
     *            The message for this exception.
     * @param throwable
     *            The cause for this exception.
     */
    public PurlProcessorNotHandledException(final PoddPurlProcessor generator, final URI inputUri, final String msg,
            final Throwable throwable)
    {
        super(msg, throwable);
        this.generator = generator;
        this.inputUri = inputUri;
    }
    
    /**
     * @param generator
     *            The PURL Generator that was not able to handle the given input URI.
     * @param inputUri
     *            The input URI that failed to be handled by the given PURL Generator.
     * @param throwable
     *            The cause for this exception.
     */
    public PurlProcessorNotHandledException(final PoddPurlProcessor generator, final URI inputUri,
            final Throwable throwable)
    {
        super(throwable);
        this.generator = generator;
        this.inputUri = inputUri;
    }
    
    /**
     * @return The PURL Generator that was not able to handle the given input URI.
     */
    public PoddPurlProcessor getGenerator()
    {
        return this.generator;
    }
    
    /**
     * @return The input URI that failed to be handled by the given PURL Generator.
     */
    public URI getInputUri()
    {
        return this.inputUri;
    }
    
}
