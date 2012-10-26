package com.github.podd.prototype;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * PODD prototype servlet.
 * Currently supports operations on artifacts only.
 * 
 * @author kutila
 * @created 2012/10/25
 * 
 */
@SuppressWarnings("serial")
public class PoddServlet extends PoddBaseServlet
{
    static PoddServletHelper helper = null;
    
    @Override
    public void init()
    {
        helper = new PoddServletHelper();
        try
        {
            helper.setUp();
            helper.loadSchemaOntologies();
        }
        catch(Exception e)
        {
            log.error("Failed to setup Servlet Helper", e);
            e.printStackTrace();
        }
    }
    
    /**
     * A common method to handle GET/POST/DELETE requests
     * 
     * TODO: - check content-type and accept headers - send OK only after ensuring consistency of
     * operation
     * 
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException
    {
        if (!isValidSession(request, response))
        {
            return;
        }
        PrintWriter out = response.getWriter();
        
        String pathInfo = request.getPathInfo();
        String httpMethod = request.getMethod();
        
        
        // ---- handle the PODD operations ----
        
        if(HTTP_POST.equals(httpMethod) && pathInfo.startsWith("/artifact/new"))
        {
            log.info("ADD new artifact");
            InputStream in = request.getInputStream();
            try
            {
                String loadedArtifactContent = helper.loadPoddArtifactPublic(in);
                response.setContentType("application/rdf+xml");
                out.write(loadedArtifactContent);
            }
            catch(Exception e)
            {
                log.error("Failed to add artifact due to: ", e);
                // TODO: status code will change depending on cause of failure
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Failed to add artifact due to: " + e.getMessage());
            }
        }
        
        else if(HTTP_POST.equals(httpMethod) && pathInfo.startsWith("/artifact/edit/"))
        {
            log.info("EDIT artifact");
            try
            {
                String artifactURI = extractUri(pathInfo.substring(14));
                InputStream in = request.getInputStream();
                String editedURI = helper.editArtifact(artifactURI, in);
                response.setContentType("application/json");
                out.write(editedURI); // should be in JSON
            }
            catch(Exception e)
            {
                log.error("EDIT Artifact failed with: " + e.toString());
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Failed to EDIT artifact due to: " + e.getMessage());
            }
        }
        
        else if(HTTP_GET.equals(httpMethod) && pathInfo.startsWith("/artifact/"))
        {
            log.info("GET artifact");
            try
            {
                String artifactURI = extractUri(pathInfo.substring(10));
                String rdfContent = helper.getArtifact(artifactURI);
                response.setContentType("application/rdf+xml");
                out.write(rdfContent);
            }
            catch(Exception e)
            {
                log.error("GET Artifact failed with: " + e.toString());
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Failed to GET artifact due to: " + e.getMessage());
            }
        }
        
        else if(HTTP_DELETE.equals(httpMethod) && pathInfo.startsWith("/artifact/"))
        {
            log.info("DELETE artifact");
            try
            {
                String artifactURI = extractUri(pathInfo.substring(10));
                helper.deleteArtifact(artifactURI);
            }
            catch(Exception e)
            {
                log.error("DELETE Artifact failed with: " + e.toString());
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Failed to DELETE artifact due to: " + e.getMessage());
            }
        }
        
        else
        {
            // unsupported operation
            log.info("Unsupported service request: " + httpMethod + ":" + pathInfo);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request not supported");
        }
        out.write("\r\n");
        out.flush();
        out.close();
    }

    /**
     * If the session already has a "user" attribute, we assume the user has successfuly logged in.
     * 
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    private boolean isValidSession(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        HttpSession session = request.getSession(false);
        if(session == null || (session.getAttribute("user") == null))
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login");
            return false;
        }
        log.info("Validated session for user " + session.getAttribute("user"));
        return true;
    }
    
    
    /**
     * Converts the incoming (URL-encoded) URI string back to a usable one by adding the missing
     * ":/" after the protocol string. Currently supports http and https only.
     * 
     * @param uriPath
     *            URL encoded string of the form "http/host.com/..."
     * @return The converted URI or an error message
     */
    private String extractUri(String uriPath)
    {
        if(uriPath == null || uriPath.length() < 6)
        {
            return "TOO_SHORT_URI:<" + uriPath + ">";
        }
        try
        {
            uriPath = URLDecoder.decode(uriPath, "UTF-8");
        }
        catch(UnsupportedEncodingException e)
        {
            return "FAILED_TO_EXTRACT_URI:<" + uriPath + ">";
        }
        
        if(uriPath.startsWith("http/"))
        {
            uriPath = uriPath.replaceFirst("http/", "http://");
        }
        else if(uriPath.startsWith("https/"))
        {
            uriPath = uriPath.replaceFirst("https/", "https://");
        }
        
        return uriPath;
    }
    
}
