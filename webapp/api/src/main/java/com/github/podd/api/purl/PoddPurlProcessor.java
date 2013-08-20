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
 /**
 * 
 */
package com.github.podd.api.purl;

import java.util.List;

import org.openrdf.model.URI;

import com.github.podd.api.PoddRdfProcessor;
import com.github.podd.exception.PurlProcessorNotHandledException;

/**
 * An interface that can be subclassed to generate different types of Permanent URLs, or more
 * generally Permanent URIs.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddPurlProcessor extends PoddRdfProcessor
{
    /**
     * Signals to this Purl Processor that it should handle permanent URL generation for URIs that
     * start with the given prefix.
     * 
     * @param temporaryUriPrefix
     *            A string that matches against the start of a temporary URI to identify it as being
     *            relevant to this Purl Processor.
     * @throws NullPointerException
     *             If a null String is passed
     */
    void addTemporaryUriHandler(String temporaryUriPrefix);
    
    /**
     * Decides whether the given input URI is compatible with this Purl Processor.
     * 
     * @param inputUri
     *            A temporary URI that needs to be translated into a Permanent URL.
     * @return True if this Purl Processor should be able to handle the conversion of this temporary
     *         URI to a Purl, and false if it is not known whether this will be possible, or if a
     *         NULL value is passed in.
     */
    boolean canHandle(URI inputUri);
    
    /**
     * 
     * @return A list of temporary URI handler prefixes that have been registered for this
     *         PoddPurlProcessor.
     */
    List<String> getTemporaryUriHandlers();
    
    /**
     * Handles the translation of URIs using this Purl Processor
     * 
     * @param inputUri
     *            A temporary URI that needs to be translated into a Permanent URL.
     * @return A translated URI that was generated using a mechanism which attempts to guarantee
     *         both permanence and uniqueness of the resulting URI.
     * @throws PurlProcessorNotHandledException
     *             If the URI was not able to be handled by this Purl Processor for any reason. To
     *             avoid this exception in normal circumstances, check first using canHandle(URI).
     * @throws NullPointerException
     *             If a null URI is passed
     * 
     */
    PoddPurlReference handleTranslation(URI inputUri) throws PurlProcessorNotHandledException;
    
    /**
     * Handles the translation of URIs using this Purl Processor
     * 
     * @param inputUri
     *            A temporary URI that needs to be translated into a Permanent URL.
     * @param parentUri
     *            A known URI for a parent of the temporary URI that may be used to create Purls.
     * @return A translated URI that was generated using a mechanism which attempts to guarantee
     *         both permanence and uniqueness of the resulting URI.
     * @throws PurlProcessorNotHandledException
     *             If the URI was not able to be handled by this Purl Processor for any reason. To
     *             avoid this exception in normal circumstances, check first using canHandle(URI).
     * @throws NullPointerException
     *             If a null URI is passed
     * 
     */
    PoddPurlReference handleTranslation(URI inputUri, URI parentUri) throws PurlProcessorNotHandledException;
    
    /**
     * Signals to this Purl Processor that it should no longer handle permanent URL generation for
     * URIs that start with the given prefix.
     * 
     * @param temporaryUriPrefix
     *            A string that matches against the start of a temporary URI to identify it as being
     *            relevant to this Purl Processor.
     */
    void removeTemporaryUriHandler(String temporaryUriPrefix);
}
