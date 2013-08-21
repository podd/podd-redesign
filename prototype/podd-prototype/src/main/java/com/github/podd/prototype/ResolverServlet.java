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
 package com.github.podd.prototype;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to resolve PODD schema ontologies. Authentication is not required to invoke this
 * operation.
 * 
 * E.g. http://purl.org/podd/ns/poddBase
 * 
 * TODO: support getting a specific version of the ontology that is managed by PODD
 * 
 * @author kutila
 * @created 2012/11/26
 * 
 */
@SuppressWarnings("serial")
public class ResolverServlet extends PoddBaseServlet
{
    /** The Base URL of schema ontologies managed by PODD. */
    public static final String PODD_BASE_URL = "http://purl.org/podd/ns";
    
    @Override
    protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {
        final PoddServletHelper helper =
                (PoddServletHelper)this.getServletContext()
                        .getAttribute(PoddServletContextListener.PODD_SERVLET_HELPER);
        
        final OutputStream out = response.getOutputStream();
        final String httpMethod = request.getMethod();
        
        if(PoddBaseServlet.HTTP_GET.equals(httpMethod))
        {
            final String pathInfo = request.getPathInfo();
            
            this.log.info("GET schema ontology: " + pathInfo);
            
            try
            {
                final String ontologyUri = ResolverServlet.PODD_BASE_URL + pathInfo.trim();
                
                response.setContentType(PoddServlet.MIME_TYPE_RDF_XML);
                helper.getSchemaOntology(ontologyUri, PoddServlet.MIME_TYPE_RDF_XML, out);
            }
            catch(final Exception e)
            {
                this.log.error("Failed to retrieve ontology due to: ", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve ontology due to: "
                        + e.getMessage());
            }
        }
        else
        {
            this.log.info("Unsupported service request: " + httpMethod);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request not supported");
        }
        out.flush();
        out.close();
    }
    
}
