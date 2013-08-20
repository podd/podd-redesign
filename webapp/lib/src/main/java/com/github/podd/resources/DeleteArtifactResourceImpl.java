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
package com.github.podd.resources;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.exception.PoddException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * TODO: Empty class with logic not implemented
 * 
 * Delete an artifact from PODD.
 * 
 * @author kutila
 * 
 */
public class DeleteArtifactResourceImpl extends AbstractPoddResourceImpl
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Delete
    public void deleteArtifact(final Representation entity) throws ResourceException
    {
        boolean result;
        try
        {
            final String artifactUri = this.getQueryValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
            
            if(artifactUri == null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "Did not find an artifacturi parameter in the request");
            }
            
            this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_DELETE, PoddRdfConstants.VF.createURI(artifactUri));
            
            final InferredOWLOntologyID currentVersion =
                    this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
            
            result = this.getPoddApplication().getPoddArtifactManager().deleteArtifact(currentVersion);
            
            if(result)
            {
                this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
            }
            else
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not delete artifact");
            }
        }
        catch(final PoddException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "Could not delete artifact due to an internal error", e);
        }
        
    }
    
    @Get
    public Representation deleteArtifactPageHtml(final Representation entity) throws ResourceException
    {
        // check mandatory parameter: artifact IRI
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
        if(artifactUri == null)
        {
            this.log.error("Artifact ID not submitted");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact IRI not submitted");
        }
        
        this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_DELETE, PoddRdfConstants.VF.createURI(artifactUri));
        
        this.log.info("deleteArtifactHtml");
        final User user = this.getRequest().getClientInfo().getUser();
        
        this.log.info("authenticated user: {}", user);
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "index.html.ftl");
        dataModel.put("pageTitle", "TODO: Delete Artifact");
        
        final Map<String, Object> artifactDataMap = this.getRequestedArtifact();
        dataModel.put("requestedArtifact", artifactDataMap);
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    // FIXME: populating dummy info for test
    private Map<String, Object> getRequestedArtifact()
    {
        final Map<String, Object> testArtifactMap = new HashMap<String, Object>();
        testArtifactMap.put("TODO: ", "Implement DeleteArtifactResourceImpl");
        
        final Map<String, String> roleMap = new HashMap<String, String>();
        roleMap.put("description", "A dummy user account for testing");
        testArtifactMap.put("repositoryRole", roleMap);
        
        return testArtifactMap;
    }
    
}
