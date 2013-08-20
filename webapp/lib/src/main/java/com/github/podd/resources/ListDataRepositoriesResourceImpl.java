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
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.file.PoddDataRepository;
import com.github.podd.api.file.PoddDataRepositoryManager;
import com.github.podd.exception.DataRepositoryException;
import com.github.podd.exception.PoddException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * Resource which lists the current data repositories in PODD, along with their aliases and types.
 * 
 * @author kutila
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class ListDataRepositoriesResourceImpl extends AbstractPoddResourceImpl
{
    public static final String LIST_PAGE_TITLE_TEXT = "PODD Data Repository Listing";
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Handle http GET request to serve the list data repositories page.
     */
    @Get(":html")
    public Representation getListDataRepositoriesPage(final Representation entity) throws ResourceException
    {
        this.log.info("@Get list data repositories Page");
        
        this.checkAuthentication(PoddAction.DATA_REPOSITORY_READ);
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "datarepositories.html.ftl");
        dataModel.put("pageTitle", ListDataRepositoriesResourceImpl.LIST_PAGE_TITLE_TEXT);
        
        try
        {
            dataModel.put("dataRepositoriesList", this.getPoddApplication().getPoddDataRepositoryManager()
                    .getAllAliases());
        }
        catch(OpenRDFException | PoddException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "Could not find definitions for data repositories", e);
        }
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    @Get(":rdf|rj|json|ttl")
    public Representation getListDataRepositoriesRdf(final Representation entity, final Variant variant)
        throws ResourceException
    {
        this.log.info("@Get list data repositories RDF");
        
        this.checkAuthentication(PoddAction.DATA_REPOSITORY_READ);
        
        final RDFFormat resultFormat = Rio.getWriterFormatForMIMEType(variant.getMediaType().getName());
        
        if(resultFormat == null)
        {
            this.log.error("Could not find an RDF serialiser matching the requested mime-type: "
                    + variant.getMediaType().getName());
            throw new ResourceException(Status.CLIENT_ERROR_NOT_ACCEPTABLE,
                    "Could not find an RDF serialiser matching the requested mime-type: "
                            + variant.getMediaType().getName());
        }
        
        final MediaType resultMediaType = MediaType.valueOf(resultFormat.getDefaultMIMEType());
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream(8096);
        
        final Model results = new LinkedHashModel();
        
        PoddDataRepositoryManager poddDataRepositoryManager = this.getPoddApplication().getPoddDataRepositoryManager();
        
        try
        {
            for(String nextAlias : poddDataRepositoryManager.getAllAliases())
            {
                BNode resourceNode = PoddRdfConstants.VF.createBNode();
                results.add(resourceNode, PoddRdfConstants.PODD_BASE_HAS_ALIAS,
                        PoddRdfConstants.VF.createLiteral(nextAlias));
                
                PoddDataRepository<?> dataRepository = poddDataRepositoryManager.getRepository(nextAlias);
                
                for(URI nextType : dataRepository.getTypes())
                {
                    results.add(resourceNode, RDF.TYPE, nextType);
                }
            }
            
            Rio.write(results, out, resultFormat);
        }
        catch(OpenRDFException | PoddException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "Could not find definitions for data repositories", e);
        }
        
        final ByteArrayRepresentation result = new ByteArrayRepresentation(out.toByteArray(), resultMediaType);
        
        return result;
    }
    
}
