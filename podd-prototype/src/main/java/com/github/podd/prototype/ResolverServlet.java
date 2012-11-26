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
