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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
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
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * User Add resource
 * 
 * @author kutila
 * 
 */
public class UserAddResourceImpl extends AbstractUserResourceImpl
{
    /**
     * Handle an HTTP POST request submitting RDF data to create a new PoddUser. This method can
     * only add one user per request. On successful addition of a user, the new user's unique URI is
     * returned encapsulated in RDF.
     */
    @Post("rdf|rj|json|ttl")
    public Representation addUserRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        // check authentication first
        this.checkAuthentication(PoddAction.USER_CREATE);
        
        this.log.info("In addUserRdf");
        
        final PoddSesameRealm nextRealm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        
        URI newUserUri = null;
        PoddUser newUser = null;
        try
        {
            // - get input stream with RDF content
            final InputStream inputStream = entity.getStream();
            final RDFFormat inputFormat =
                    Rio.getParserFormatForMIMEType(entity.getMediaType().getName(), RDFFormat.RDFXML);
            final Model newUserModel = Rio.parse(inputStream, "", inputFormat);
            
            this.log.info("About to create user from model");
            
            // - create new PoddUser and add to Realm
            newUser = PoddUser.fromModel(newUserModel, true, false, false);
            
            // If we didn't get a secret, then do not activate their login at
            // this stage
            if(newUser.getSecret() == null)
            {
                newUser.setUserStatus(PoddUserStatus.INACTIVE);
            }
            
            this.log.info("About to check if user already exists");
            
            if(nextRealm.findUser(newUser.getIdentifier()) != null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "User already exists");
            }
            newUserUri = nextRealm.addUser(newUser);
            
            this.log.info("Added new User <{}> <{}>", newUser.getIdentifier(), newUserUri);
            
            // - map Roles for the new User
            
            // - add Project Creator Role if nothing else has been specified
            if(!newUserModel.contains(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING))
            {
                nextRealm.map(newUser, PoddRoles.PROJECT_CREATOR.getRole());
            }
            
            for(final Resource mappingUri : newUserModel.filter(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING)
                    .subjects())
            {
                final URI roleUri =
                        newUserModel.filter(mappingUri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objectURI();
                final RestletUtilRole role = PoddRoles.getRoleByUri(roleUri);
                
                final URI mappedObject = newUserModel.filter(mappingUri, PODD.PODD_ROLEMAPPEDOBJECT, null).objectURI();
                
                this.log.debug("Mapping <{}> to Role <{}> with Optional Object <{}>", newUser.getIdentifier(),
                        role.getName(), mappedObject);
                if(mappedObject != null)
                {
                    nextRealm.map(newUser, role.getRole(), mappedObject);
                }
                else
                {
                    nextRealm.map(newUser, role.getRole());
                }
            }
            
            // - check the User was successfully added to the Realm
            final PoddUser findUser = nextRealm.findUser(newUser.getIdentifier());
            if(findUser == null)
            {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to add user");
            }
            
        }
        catch(final IOException | OpenRDFException e)
        {
            this.log.error("Error creating user", e);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
        }
        
        // - prepare response
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        final RDFFormat outputFormat =
                Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
        try
        {
            final Model model = new LinkedHashModel();
            model.add(newUserUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                    PODD.VF.createLiteral(newUser.getIdentifier()));
            
            Rio.write(model, output, outputFormat);
        }
        catch(final OpenRDFException e)
        {
            this.log.error("Error generating response entity", e);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat.getDefaultMIMEType()));
    }
    
    @Get
    public Representation getUserAddPageHtml(final Representation entity) throws ResourceException
    {
        this.log.info("addUserHtml");
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        // identify needed Action
        this.checkAuthentication(PoddAction.USER_CREATE);
        
        // completed checking authorization
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "admin_createUser.html.ftl");
        dataModel.put("pageTitle", "Add PODD User Page");
        dataModel.put("title", "Create User");
        dataModel.put("authenticatedUsername", user.getIdentifier());
        
        final PoddUserStatus[] statuses = PoddUserStatus.values();
        dataModel.put("statusList", statuses);
        
        // Output the base template, with contentTemplate from the dataModel
        // defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(
                this.getPoddApplication().getPropertyUtil()
                        .get(PoddWebConstants.PROPERTY_TEMPLATE_BASE, PoddWebConstants.DEFAULT_TEMPLATE_BASE),
                dataModel, MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
}