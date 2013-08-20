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
package com.github.podd.api.purl;

import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.github.podd.exception.PurlProcessorNotHandledException;

/**
 * A manager object used to manage conversion of temporary URIs to Permanent URLs as required by
 * PODD.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface PoddPurlManager
{
    /**
     * Convert the temporary URIs from the RepositoryConnection in the given contexts to PURLs.
     * 
     * @param purlResults
     *            Set of PurlReferences containing temporary URIs and their corresponding PURLs
     * @param repositoryConnection
     *            The RepositoryConnection in which temporary URIs need to be replaced
     * @param contexts
     *            The contexts in the Repository to be considered
     * @throws RepositoryException
     * @throws UpdateExecutionException
     */
    void convertTemporaryUris(Set<PoddPurlReference> purlResults, RepositoryConnection repositoryConnection,
            URI... contexts) throws RepositoryException, UpdateExecutionException;
    
    /**
     * Identify temporary URIs from the RepositoryConnection (in the given contexts) and generate
     * PURLs for them.
     * 
     * @param repositoryConnection
     * @param contexts
     *            The contexts in the Repository to be considered
     * @return A set of PoddPurlReferences containing the extracted temporary URIs and the PURLs
     *         generated for them
     * @throws PurlProcessorNotHandledException
     * @throws RepositoryException
     */
    Set<PoddPurlReference> extractPurlReferences(RepositoryConnection repositoryConnection, URI... contexts)
        throws PurlProcessorNotHandledException, RepositoryException;
    
    /**
     * Identify temporary URIs from the RepositoryConnection (in the given contexts) and generate
     * PURLs for them.
     * 
     * @param repositoryConnection
     * @param parentUri
     *            An optional parent URI that the {@link PoddPurlProcessor} may use when creating
     *            Purls using {@link PoddPurlProcessor#handleTranslation(URI, URI)}.
     * @param contexts
     *            The contexts in the Repository to be considered
     * @return A set of PoddPurlReferences containing the extracted temporary URIs and the PURLs
     *         generated for them
     * @throws PurlProcessorNotHandledException
     * @throws RepositoryException
     */
    Set<PoddPurlReference> extractPurlReferences(URI parentUri, RepositoryConnection repositoryConnection,
            URI... contexts) throws PurlProcessorNotHandledException, RepositoryException;
    
    /**
     * Retrieve the <code>PodPurlProcessorFactoryRegistry</code> assigned to this Manager.
     * 
     * @return
     */
    PoddPurlProcessorFactoryRegistry getPurlProcessorFactoryRegistry();
    
    /**
     * Set the the <code>PodPurlProcessorFactoryRegistry</code> for this Manager.
     * 
     * @param purlProcessorFactoryRegistry
     */
    void setPurlProcessorFactoryRegistry(PoddPurlProcessorFactoryRegistry purlProcessorFactoryRegistry);
    
}
