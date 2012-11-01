package com.github.podd.prototype;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoddServletContextListener implements ServletContextListener
{
    public static final String PODD_SERVLET_HELPER = "PODD_SERVLET_HELPER";
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void contextInitialized(final ServletContextEvent sce)
    {
        this.log.info("\r\n" +
        		" ======================================= \r\n" +
        		" Initializing PODD Prototype Web Service \r\n" +
        		" ======================================= ");
        final PoddServletHelper helper = new PoddServletHelper();
        try
        {
            helper.setUp();
            helper.loadSchemaOntologies();
            
            sce.getServletContext().setAttribute(PoddServletContextListener.PODD_SERVLET_HELPER, helper);
        }
        catch(final Exception e)
        {
            this.log.error("Failed to setup Servlet Helper", e);
            e.printStackTrace();
        }
    }
    
    @Override
    public void contextDestroyed(final ServletContextEvent sce)
    {
        this.log.info("\r\n" +
        		" ====================================== \r\n" +
        		" TERMINATING PODD Prototype Web Service \r\n" +
        		" ====================================== ");
        try
        {
            final PoddServletHelper helper =
                    (PoddServletHelper)sce.getServletContext().getAttribute(
                            PoddServletContextListener.PODD_SERVLET_HELPER);
            helper.tearDown();
        }
        catch(final Exception e)
        {
            this.log.error("Failed to clean up Servlet Helper", e);
            e.printStackTrace();
        }
        finally
        {
            sce.getServletContext().removeAttribute(PoddServletContextListener.PODD_SERVLET_HELPER);
        }
    }
    
}