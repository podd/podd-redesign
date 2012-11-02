package com.github.podd.prototype;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;
import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoddServletContextListener implements ServletContextListener
{
    public static final String PODD_SERVLET_HELPER = "PODD_SERVLET_HELPER";
    public static final String PODD_HOME = "podd.home";
    public static final String PODD_PASSWORD_FILE = "PODD_PASSWDS";
    
    private static final String INIT_PASSWORD_FILE = "passwdfile";
    private static final String INIT_SESAME_SERVER = "sesame-server";
    private static final String INIT_SESAME_REPOSITORY = "sesame-repository-id";
    
    
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void contextInitialized(final ServletContextEvent sce)
    {
        this.log.info("\r\n" + " ======================================= \r\n"
                + " Initializing PODD Prototype Web Service \r\n" + " ======================================= ");
        
        final PoddServletHelper helper = new PoddServletHelper();
        try
        {
            this.initializeAuthenticationService(sce);
            
            String sesameServer = sce.getServletContext().getInitParameter(INIT_SESAME_SERVER);
            String repositoryID =sce.getServletContext().getInitParameter(INIT_SESAME_REPOSITORY); 

            Repository nextRepository = new SailRepository(new NativeStore(new File(System.getProperty(PODD_HOME), "spoc,cspo,cpso,psoc,ospc,opsc,cops")));
            nextRepository.initialize();
            
            helper.setUp(nextRepository);
            helper.loadSchemaOntologies();
            
            sce.getServletContext().setAttribute(PoddServletContextListener.PODD_SERVLET_HELPER, helper);
        }
        catch(OWLException | OpenRDFException | IOException | PoddException e)
        {
            final String message = "Failed to setup PODD web service";
            this.log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }
    
    @Override
    public void contextDestroyed(final ServletContextEvent sce)
    {
        this.log.info("\r\n" + " ====================================== \r\n"
                + " TERMINATING PODD Prototype Web Service \r\n" + " ====================================== ");
        try
        {
            final PoddServletHelper helper =
                    (PoddServletHelper)sce.getServletContext().getAttribute(
                            PoddServletContextListener.PODD_SERVLET_HELPER);
            helper.tearDown();
        }
        catch(final RepositoryException e)
        {
            this.log.error("Failed to clean up Servlet Helper", e);
            // TODO: check whether hiding this Exception leads to any issues
        }
        finally
        {
            sce.getServletContext().removeAttribute(PoddServletContextListener.PODD_SERVLET_HELPER);
            sce.getServletContext().removeAttribute(PoddServletContextListener.PODD_PASSWORD_FILE);
        }
    }
    
    /**
     * Figures out the password file location and adds it as an attribute to the ServletContext.
     * 
     * @param sce
     * @throws PoddException
     */
    private void initializeAuthenticationService(final ServletContextEvent sce) throws PoddException
    {
        String passwordFile = sce.getServletContext().getInitParameter(INIT_PASSWORD_FILE);
        if(passwordFile == null || passwordFile.trim().length() < 1)
        {
            throw new PoddException("Password file not specified.", null, -1);
        }
        
        // TODO: add support for Windows OS paths
        if(!passwordFile.startsWith("/"))
        {
            final String poddHomeDir = System.getProperty(PoddServletContextListener.PODD_HOME);
            if(poddHomeDir == null || poddHomeDir.trim().length() < 1)
            {
                throw new PoddException("PODD Home Directory not set.", null, -1);
            }
            passwordFile = poddHomeDir + "/" + passwordFile;
        }
        sce.getServletContext().setAttribute(PoddServletContextListener.PODD_PASSWORD_FILE, passwordFile);
        this.log.info("The PODD password file is located at : " + passwordFile);
    }
    
}