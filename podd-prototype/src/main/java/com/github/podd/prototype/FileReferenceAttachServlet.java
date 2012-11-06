package com.github.podd.prototype;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryException;
import org.semanticweb.owlapi.model.OWLException;

/**
 * Servlet implementation class FileReferenceAttachServlet
 * 
 * @author kutila
 */
@SuppressWarnings("serial")
public class FileReferenceAttachServlet extends PoddBaseServlet implements Servlet
{
    
    @Override
    protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {
        if(!this.isValidSession(request, response))
        {
            return;
        }
        
        final String httpMethod = request.getMethod();
        if(!PoddBaseServlet.HTTP_POST.equals(httpMethod))
        {
            this.log.info("Unsupported service request method: " + httpMethod);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request Method not supported");
            return;
        }
        
        this.log.info("REFERENCE attach");
        
        final String artifactUri = request.getParameter("artifact_uri");
        final String objectUri = request.getParameter("object_uri");
        final String serverAlias = request.getParameter("file_server_alias");
        final String path = request.getParameter("file_path");
        final String filename = request.getParameter("file_name");
        final String description = request.getParameter("file_description");
        
        if(artifactUri == null || objectUri == null || serverAlias == null | path == null || filename == null)
        {
            this.log.error("REFERENCE attach failed. Insufficient information");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing information");
            return;
        }
        
        final FileReferenceValidator validator = FileReferenceValidator.getInstance();
        try
        {
            validator.validate(serverAlias, path, filename);
        }
        catch(IOException | NullPointerException e)
        {
            this.log.error("REFERENCE attach failed. File reference invalid");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Referenced file is not valid: " + e.getMessage());
            return;
        }
        
        final PoddServletHelper helper =
                (PoddServletHelper)request.getServletContext().getAttribute(
                        PoddServletContextListener.PODD_SERVLET_HELPER);
        
        String resultJson = null;
        try
        {
            resultJson = helper.attachReference(artifactUri, objectUri, serverAlias, path, filename, description);
        }
        catch(final RepositoryException e)
        {
            // TODO: handle different failure cases
            this.log.error("REFERENCE attach failed. Generic");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "REFERENCE attach failed: " + e.getMessage());
            return;
        }
        catch(PoddException e)
        {
            // TODO Auto-generated catch block
            this.log.error("REFERENCE attach failed. Generic");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "REFERENCE attach failed: " + e.getMessage());
            return;
        }
        catch(OpenRDFException e)
        {
            // TODO Auto-generated catch block
            this.log.error("REFERENCE attach failed. Generic");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "REFERENCE attach failed: " + e.getMessage());
            return;
        }
        catch(OWLException e)
        {
            // TODO Auto-generated catch block
            this.log.error("REFERENCE attach failed. Generic");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "REFERENCE attach failed: " + e.getMessage());
            return;
        }
        
        response.setContentType(PoddServlet.MIME_TYPE_JSON);
        final PrintWriter out = response.getWriter();
        out.write(resultJson); // should be in JSON
        out.write("\r\n");
        out.flush();
        out.close();
    }
    
}
