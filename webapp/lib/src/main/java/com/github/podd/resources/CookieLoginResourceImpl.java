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

import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;

import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class CookieLoginResourceImpl extends AbstractPoddResourceImpl implements LoginResource
{
    @Override
    @Get("html")
    public Representation getLoginPageHtml(final Representation entity) throws ResourceException
    {
        final User user = this.getRequest().getClientInfo().getUser();

        this.log.info("authenticated user: {}", user);

        if(user == null)
        {
            this.log.info("In getLoginPageHtml");
            final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
            dataModel.put(
                    "contentTemplate",
                    this.getPropertyUtil().get(PoddWebConstants.PROPERTY_TEMPLATE_LOGIN,
                            PoddWebConstants.DEFAULT_TEMPLATE_LOGIN));

            dataModel.put("shibbolethEnabled", Boolean.valueOf("false"));
            dataModel.put("invalidDomain", Boolean.valueOf("false"));

            dataModel.put("pageTitle", "PODD Login Page");

            // FIXME: By default use the referrer to populate the redirectTo
            // field internally for
            // use after a successful login
            dataModel.put("referrerRef", this.getRequest().getReferrerRef());
            this.log.info("referrerRef={}", this.getRequest().getReferrerRef());

            // Output the base template, with contentTemplate from the dataModel
            // defining the
            // template to use for the content in the body of the page
            return RestletUtils.getHtmlRepresentation(
                    this.getPropertyUtil().get(PoddWebConstants.PROPERTY_TEMPLATE_BASE,
                            PoddWebConstants.DEFAULT_TEMPLATE_BASE), dataModel, MediaType.TEXT_HTML, this
                            .getPoddApplication().getTemplateConfiguration());
        }
        else
        {
            final Reference referrerRef = this.getRequest().getReferrerRef();

            this.log.info("referrerRef={}", referrerRef);

            if(referrerRef != null && !referrerRef.equals(this.getRequest().getResourceRef()))
            {
                this.getResponse().redirectSeeOther(referrerRef);
            }
            else
            {
                this.getResponse().redirectSeeOther("location:to:redirect:to:already:logged:in");
            }
            return null;
        }
    }

}
