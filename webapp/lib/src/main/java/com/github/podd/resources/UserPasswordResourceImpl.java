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
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;
import org.restlet.security.SecretVerifier;
import org.restlet.security.User;
import org.restlet.security.Verifier;

import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddWebConstants;

/**
 * This resource handles User password change.
 *
 * @author kutila
 */
public class UserPasswordResourceImpl extends AbstractUserResourceImpl
{
    private URI changePassword(final PoddSesameRealm nextRealm, final Model model, final PoddUser changePwdUser,
            final boolean changeOwnPassword)
    {
        final String identifierInModel =
                model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString();

        // verify user identifier in Model is same as that from the request
        if(!changePwdUser.getIdentifier().equals(identifierInModel))
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "Problem with input: user identifiers don't match");
        }

        // changing own password, verify old password.
        if(changeOwnPassword)
        {
            final String oldPassword = model.filter(null, PODD.PODD_USER_OLDSECRET, null).objectString();

            final Verifier verifier = nextRealm.getVerifier();
            if(verifier == null)
            {
                this.log.warn("Could not access Verifier to check old password");
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to access Verifier");
            }

            if(verifier instanceof SecretVerifier)
            {
                final int verifyResult =
                        ((SecretVerifier)verifier).verify(identifierInModel, oldPassword.toCharArray());

                if(verifyResult != Verifier.RESULT_VALID)
                {
                    throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, "Old password is invalid.");
                }
            }
            else
            {
                this.log.warn("Could not access Verifier to check old password");
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to access Verifier");
            }

        }

        // update sesame Realm with new password
        final String newPassword = model.filter(null, SesameRealmConstants.OAS_USERSECRET, null).objectString();
        // this.log.info("[DEBUG] new password is [{}]", newPassword);
        changePwdUser.setSecret(newPassword.toCharArray());

        return nextRealm.updateUser(changePwdUser);
    }

    /**
     * Handle password change requests
     */
    @Post("rdf|rj|ttl")
    public Representation editPasswordRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        this.log.info("changePasswordRdf");

        final String requestedUserIdentifier = this.getUserParameter();
        final PoddAction action =
                this.getAction(requestedUserIdentifier, PoddAction.OTHER_USER_EDIT, PoddAction.CURRENT_USER_EDIT);

        final boolean changeOwnPassword = (action == PoddAction.CURRENT_USER_EDIT);

        this.log.info("requesting change password of user: {}", requestedUserIdentifier);

        if(requestedUserIdentifier == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not specify user");
        }

        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);

        this.checkAuthentication(action);

        final PoddSesameRealm nextRealm = ((PoddWebServiceApplication)this.getApplication()).getRealm();

        final PoddUser poddUser = nextRealm.findUser(requestedUserIdentifier);
        if(poddUser == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User not found");
        }

        URI userUri = null;
        try
        {
            // - get input stream with RDF content
            final InputStream inputStream = entity.getStream();
            final RDFFormat inputFormat =
                    Rio.getParserFormatForMIMEType(entity.getMediaType().getName(), RDFFormat.RDFXML);
            final Model modifiedUserModel = Rio.parse(inputStream, "", inputFormat);

            userUri = this.changePassword(nextRealm, modifiedUserModel, poddUser, changeOwnPassword);
        }
        catch(IOException | OpenRDFException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
        }

        // - set Credentials Cookie to expire so that User has to login again
        if(changeOwnPassword)
        {
            final String cookieName =
                    this.getPoddApplication().getPropertyUtil()
                    .get(PoddWebConstants.PROPERTY_COOKIE_NAME, PoddWebConstants.DEF_COOKIE_NAME);
            final CookieSetting credentialsCookie = this.getResponse().getCookieSettings().getFirst(cookieName);
            if(credentialsCookie != null)
            {
                credentialsCookie.setMaxAge(0);
                this.getResponse().getCookieSettings().add(credentialsCookie);
                this.log.debug("Set max Age of Credentials Cookie to expire");
            }
        }

        // - prepare response
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        final RDFFormat outputFormat =
                Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
        try
        {
            final Model model = new LinkedHashModel();
            model.add(userUri, SesameRealmConstants.OAS_USERIDENTIFIER, PODD.VF.createLiteral(poddUser.getIdentifier()));
            Rio.write(model, output, outputFormat);
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
        }

        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat.getDefaultMIMEType()));
    }

    /**
     * Display the HTML page for password change
     */
    @Get(":html")
    public Representation getUserPasswordPageHtml(final Representation entity) throws ResourceException
    {
        this.log.info("getUserPasswordHtml");

        final String requestedUserIdentifier = this.getUserParameter();
        final PoddAction action =
                this.getAction(requestedUserIdentifier, PoddAction.OTHER_USER_EDIT, PoddAction.CURRENT_USER_EDIT);

        final boolean changeOwnPassword = (action == PoddAction.CURRENT_USER_EDIT);

        this.log.info("requesting change password of user: {}", requestedUserIdentifier);

        if(requestedUserIdentifier == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not specify user");
        }

        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);

        // identify needed Action
        this.checkAuthentication(action);

        // completed checking authorization

        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "editUserPwd.html.ftl");
        dataModel.put("pageTitle", "Edit Password");
        dataModel.put("authenticatedUserIdentifier", user.getIdentifier());

        final PoddSesameRealm realm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        final PoddUser poddUser = realm.findUser(requestedUserIdentifier);

        if(poddUser == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "User not found.");
        }
        else
        {
            dataModel.put("requestedUser", poddUser);

            final Set<Role> roles = realm.findRoles(poddUser);
            dataModel.put("repositoryRoleList", roles);
        }

        // Output the base template, with contentTemplate from the dataModel defining the template
        // to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(
                this.getPoddApplication().getPropertyUtil()
                .get(PoddWebConstants.PROPERTY_TEMPLATE_BASE, PoddWebConstants.DEFAULT_TEMPLATE_BASE),
                dataModel, MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }

}