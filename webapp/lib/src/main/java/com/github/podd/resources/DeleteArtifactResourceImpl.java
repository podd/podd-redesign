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
package com.github.podd.resources;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.exception.PoddException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PODD;
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
    @Delete
    public void deleteArtifact(final Representation entity) throws ResourceException
    {
        boolean result;
        try
        {
            final String artifactUriString = this.getQueryValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
            
            if(artifactUriString == null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "Did not find an artifacturi parameter in the request");
            }
            
            final URI artifactUri = PODD.VF.createURI(artifactUriString);
            
            this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_DELETE, artifactUri);
            
            final InferredOWLOntologyID currentVersion =
                    this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
            
            result = this.getPoddApplication().getPoddArtifactManager().deleteArtifact(currentVersion);
            
            if(result)
            {
                final PoddSesameRealm realm = this.getPoddApplication().getRealm();
                final Map<String, Collection<Role>> roleMap = realm.getRolesForObjectAlternate(null, artifactUri);
                
                for(final Entry<String, Collection<Role>> nextEntry : roleMap.entrySet())
                {
                    final String userIdentifier = nextEntry.getKey();
                    for(final Role nextRole : nextEntry.getValue())
                    {
                        try
                        {
                            realm.unmap(nextRole, artifactUri, userIdentifier);
                        }
                        catch(final RuntimeException e)
                        {
                            // Ignore errors during this process for now
                        }
                    }
                }
                
                this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
            }
            else
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not delete artifact");
            }
        }
        catch(final PoddException | UnsupportedRDFormatException | OpenRDFException | IOException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "Could not delete artifact due to an internal error", e);
        }
        
    }
    
    @Get
    public Representation deleteArtifactPageHtml(final Representation entity) throws ResourceException
    {
        // check mandatory parameter: artifact IRI
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
        if(artifactUri == null)
        {
            this.log.error("Artifact ID not submitted");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact IRI not submitted");
        }
        
        this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_DELETE, PODD.VF.createURI(artifactUri));
        
        this.log.info("deleteArtifactHtml");
        final User user = this.getRequest().getClientInfo().getUser();
        
        this.log.info("authenticated user: {}", user);
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "index.html.ftl");
        dataModel.put("pageTitle", "TODO: Delete Artifact");
        
        final Map<String, Object> artifactDataMap = this.getRequestedArtifact();
        dataModel.put("requestedArtifact", artifactDataMap);
        
        // Output the base template, with contentTemplate from the dataModel
        // defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(
                this.getPoddApplication().getPropertyUtil()
                        .get(PoddWebConstants.PROPERTY_TEMPLATE_BASE, PoddWebConstants.DEFAULT_TEMPLATE_BASE),
                dataModel, MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
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
