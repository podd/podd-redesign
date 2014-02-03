/**
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
package com.github.podd.impl.data;

import java.io.IOException;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.data.DataReference;
import com.github.podd.api.data.PoddDataRepository;
import com.github.podd.api.data.SPARQLDataReference;
import com.github.podd.exception.DataReferenceNotSupportedException;
import com.github.podd.exception.DataRepositoryIncompleteException;
import com.github.podd.utils.PODD;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class SPARQLDataRepositoryImpl extends PoddFileRepositoryImpl<SPARQLDataReference>
{
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public SPARQLDataRepositoryImpl(final Resource nextDataRepository, final Model model)
        throws DataRepositoryIncompleteException
    {
        super(nextDataRepository, model);
        
        // check that the model contains values for protocol, host, port,
        // fingerprint, username, and
        // secret
        final String protocol = model.filter(super.aliasUri, PODD.PODD_DATA_REPOSITORY_PROTOCOL, null).objectString();
        final String host = model.filter(super.aliasUri, PODD.PODD_DATA_REPOSITORY_HOST, null).objectString();
        final String port = model.filter(super.aliasUri, PODD.PODD_DATA_REPOSITORY_PORT, null).objectString();
        final String path = model.filter(super.aliasUri, PODD.PODD_DATA_REPOSITORY_PATH, null).objectString();
        
        if(protocol == null || host == null || port == null || path == null)
        {
            throw new DataRepositoryIncompleteException("SPARQL repository configuration incomplete");
        }
        
        if(!PoddDataRepository.PROTOCOL_HTTP.equalsIgnoreCase(protocol))
        {
            throw new DataRepositoryIncompleteException("Protocol needs to be HTTP for SPARQL Repository");
        }
    }
    
    @Override
    public boolean canHandle(final SPARQLDataReference reference)
    {
        if(reference == null)
        {
            return false;
        }
        
        // unnecessary as Generics ensure only an SPARQLDataReference can be
        // passed in
        if(!(reference instanceof SPARQLDataReference))
        {
            return false;
        }
        
        final String aliasFromFileRef = reference.getRepositoryAlias();
        if(aliasFromFileRef == null || !this.alias.equalsIgnoreCase(aliasFromFileRef))
        {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean validate(final SPARQLDataReference dataReference) throws DataReferenceNotSupportedException, IOException
    {
        if(!this.canHandle(dataReference))
        {
            throw new DataReferenceNotSupportedException(dataReference, "cannot handle file reference for validation");
        }
        
        final String host = this.model.filter(super.aliasUri, PODD.PODD_DATA_REPOSITORY_HOST, null).objectString();
        final String port = this.model.filter(super.aliasUri, PODD.PODD_DATA_REPOSITORY_PORT, null).objectString();
        
        int portNo = -1;
        try
        {
            portNo = Integer.parseInt(port);
        }
        catch(final NumberFormatException e)
        {
            throw new IOException("Port number could not be parsed correctly: " + port);
        }
        
        final String graph = ((SPARQLDataReference)dataReference).getGraph();
        
        this.log.info("Validating file reference: " + host + ":" + port + " GRAPH<" + graph + ">");
        
        // FIXME: Implement validation that the graph exists
        
        return true;
    }
    
}
