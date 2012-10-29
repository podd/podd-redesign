package com.github.podd.prototype;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base servlet class for PODD web services.
 * Declares <code>processRequest</code> method which handles http
 * GET/POST/DELETE methods.
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
    protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(request, response);
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(request, response);
    }
    
}