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
    
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    protected static PoddServletHelper helper = null;
    
    public PoddBaseServlet()
    {
        super();
    }
    
    @Override
    public void init()
    {
        this.log.info("------------ Initializing PODD Prototype Web Service -------------");
        PoddBaseServlet.helper = new PoddServletHelper();
        try
        {
            PoddBaseServlet.helper.setUp();
            PoddBaseServlet.helper.loadSchemaOntologies();
        }
        catch(final Exception e)
        {
            this.log.error("Failed to setup Servlet Helper", e);
            e.printStackTrace();
        }
    }
    
    @Override
    public void destroy()
    {
        this.log.info("====== TERMINATING PODD .....");
        try
        {
            PoddBaseServlet.helper.tearDown();
        }
        catch(final Exception e)
        {
            this.log.error("Failed to clean up Servlet Helper", e);
            e.printStackTrace();
        }
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
        this.processRequest(request, response);
    }
    
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {
        this.processRequest(request, response);
    }
    
    @Override
    protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException
    {
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
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login");
            return false;
        }
        this.log.debug("Validated session for user " + session.getAttribute("user"));
        return true;
    }
    
}