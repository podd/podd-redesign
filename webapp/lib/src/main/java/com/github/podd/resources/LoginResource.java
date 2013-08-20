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

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * Static login page resource which accepts HTTP GET to return a form if the user is not
 * authenticated, or redirect them if they are authenticated.
 * 
 * HTTP GET returns a static login form
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface LoginResource
{
    /**
     * Fetch a form that can be used in an HTML application to login.
     * 
     * @param entity
     * @return
     * @throws ResourceException
     */
    @Get("html")
    Representation getLoginPageHtml(Representation entity) throws ResourceException;
    
}