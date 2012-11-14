package com.github.podd.prototype;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.OpenRDFException;
import org.semanticweb.owlapi.model.OWLException;

/**
 * Servlet implementation class FileReferenceAttachServlet
 * 
 * @author kutila
 */
@SuppressWarnings("serial")
public class FileReferenceAttachServlet extends PoddBaseServlet
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
        final FileReferenceUtils utils = FileReferenceUtils.getInstance();
        
        final FileReference fileReference = utils.constructFileReferenceFromMap(request.getParameterMap());
        
        if(fileReference == null)
        {
            final String message = "Not enough information to create file reference";
            this.log.error(message);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
            return;
        }
        
        try
        {
            utils.checkFileExists(fileReference);
        }
        catch(IOException | PoddException e)
        {
            final String message = "Referenced file is not valid. ";
            this.log.error(message, e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, message + e.getMessage());
            return;
        }
        
        final PoddServletHelper helper =
                (PoddServletHelper)this.getServletContext()
                        .getAttribute(PoddServletContextListener.PODD_SERVLET_HELPER);
        try
        {
            helper.attachReference(fileReference, false);
        }
        catch(final RuntimeException e)
        {
            final String message = "REFERENCE attach failed. " + e.getMessage();
            this.log.error(message);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, message); // client error
            return;
        }
        catch(final PoddException | OWLException | OpenRDFException e)
        {
            // TODO: handle different failure cases
            final String message = "REFERENCE attach failed: " + e.getMessage();
            this.log.error(message);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
            return;
        }
        
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
    
}
