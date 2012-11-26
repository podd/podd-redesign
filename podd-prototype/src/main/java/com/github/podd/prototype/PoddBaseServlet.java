package com.github.podd.prototype;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base servlet class for PODD web services. Declares <code>processRequest</code> method which
 * handles http GET/POST/DELETE methods.
 * 
 * @author kutila
 * @created 2012/10/26
 */
public abstract class PoddBaseServlet extends HttpServlet
{
    
    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";
    public static final String HTTP_DELETE = "DELETE";
    
    public static final String HEADER_APPLICATION_VERSION = "X-Application-Version";
    
    private static final String PODD_VERSION = "PODD/0.2_26.11.12";
    
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    public PoddBaseServlet()
    {
        super();
    }
    
    /**
     * A common method to handle GET/POST/DELETE requests
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException;
    
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
        IOException
    {
        response.setHeader(PoddBaseServlet.HEADER_APPLICATION_VERSION, PoddBaseServlet.PODD_VERSION);
        this.processRequest(request, response);
    }
    
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {
        response.setHeader(PoddBaseServlet.HEADER_APPLICATION_VERSION, PoddBaseServlet.PODD_VERSION);
        this.processRequest(request, response);
    }
    
    @Override
    protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {
        response.setHeader(PoddBaseServlet.HEADER_APPLICATION_VERSION, PoddBaseServlet.PODD_VERSION);
        this.processRequest(request, response);
    }
    
    /**
     * If the session already has a "user" attribute, we assume the user has successfully logged in.
     * 
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    protected boolean isValidSession(final HttpServletRequest request, final HttpServletResponse response)
        throws IOException
    {
        final HttpSession session = request.getSession(false);
        if(session == null || (session.getAttribute("user") == null))
        {
            this.log.info("No valid session for request {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login");
            return false;
        }
        this.log.debug("Validated session for user " + session.getAttribute("user"));
        return true;
    }
    
}