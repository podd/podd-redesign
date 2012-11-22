package com.github.podd.prototype;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes PODD at web-app startup and destroys when the web-app is shutting down.
 * 
 * @author kutila
 * @created 2012/11/02
 */
public class PoddServletContextListener implements ServletContextListener
{
    public static final String PODD_SERVLET_HELPER = "PODD_SERVLET_HELPER";
    
    public static final String PODD_PASSWORDS = "PODD_PASSWDS";
    public static final String PODD_ALIASES = "PODD_ALIASES";
    
    private static final String INIT_PODD_CONFIG_DIR = "podd_config_dir";
    private static final String INIT_PASSWORD_FILE = "passwdfile";
    private static final String INIT_ALIAS_FILE = "aliasfile";
    // private static final String INIT_SESAME_SERVER = "sesame-server";
    // private static final String INIT_SESAME_REPOSITORY = "sesame-repository-id";
    
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private String poddHomeDir;
    
    @Override
    public void contextInitialized(final ServletContextEvent sce)
    {
        this.log.info("\r\n" + " ======================================= \r\n"
                + " Initializing PODD Prototype Web Service \r\n" + " ======================================= ");
        
        final PoddServletHelper helper = new PoddServletHelper();
        try
        {
//            poddHomeDir = System.getProperty("podd.home");
            poddHomeDir = sce.getServletContext().getInitParameter(PoddServletContextListener.INIT_PODD_CONFIG_DIR);
            if(poddHomeDir == null || poddHomeDir.trim().length() <= 0)
            {
                throw new PoddException("PODD Home Directory not set.", null, -1);
            }
            System.out.println("********************************* podd.home is" + poddHomeDir);
            this.initializeAuthenticationService(sce);
            this.initializeFileRepositoryRegistry(sce);
            
            // final String sesameServer =
            // sce.getServletContext().getInitParameter(PoddServletContextListener.INIT_SESAME_SERVER);
            // final String repositoryID =
            // sce.getServletContext().getInitParameter(PoddServletContextListener.INIT_SESAME_REPOSITORY);
            
            
            final Repository nextRepository =
                    new SailRepository(
                            new NativeStore(new File(poddHomeDir + File.separatorChar + "native"),
                                    "spoc,posc,cspo,cpso,psoc,ospc,opsc,cops"));
            nextRepository.initialize();
            
            // FIXME: This is not very clean.
            // We're creating an instance of FileReferenceUtils and setting the aliases (which were
            // read above)
            // Then the FileReferenceUtils is being passed into the PoddServletHelper, which in turn
            // is kept in the servletContext for use by Servlets
            final FileReferenceUtils fileReferenceUtils = new FileReferenceUtils();
            final Properties aliases =
                    (Properties)sce.getServletContext().getAttribute(PoddServletContextListener.PODD_ALIASES);
            fileReferenceUtils.setAliases(aliases);
            
            helper.setUp(nextRepository);
            helper.loadSchemaOntologies();
            helper.setFileReferenceUtils(fileReferenceUtils);
            
            sce.getServletContext().setAttribute(PoddServletContextListener.PODD_SERVLET_HELPER, helper);
            this.log.info("\r\n ... initialization complete.");
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
            if(helper != null)
            {
                helper.tearDown();
            }
        }
        catch(final RepositoryException e)
        {
            this.log.error("Failed to clean up Servlet Helper", e);
            // TODO: check whether hiding this Exception leads to any issues
        }
        finally
        {
            sce.getServletContext().removeAttribute(PoddServletContextListener.PODD_SERVLET_HELPER);
            sce.getServletContext().removeAttribute(PoddServletContextListener.PODD_PASSWORDS);
        }
        this.log.info("\r\n ... termination complete.");
    }
    
    /**
     * Loads the passwords from a file and adds them to the ServletContext in a Properties object.
     * 
     * @param sce
     * @throws PoddException
     */
    private void initializeAuthenticationService(final ServletContextEvent sce) throws PoddException
    {
        String passwordFile = sce.getServletContext().getInitParameter(PoddServletContextListener.INIT_PASSWORD_FILE);
        if(passwordFile == null || passwordFile.trim().length() < 1)
        {
            throw new PoddException("Password file not specified.", null, -1);
        }
        
        // TODO: add support for Windows OS paths
        if(!passwordFile.startsWith("/"))
        {
            passwordFile = poddHomeDir + File.separatorChar + passwordFile;
        }
        final Properties passwords = new Properties();
        try
        {
            passwords.load(new FileInputStream(passwordFile));
        }
        catch(final IOException e)
        {
            throw new PoddException("Could not load passwords", e, -1);
        }
        sce.getServletContext().setAttribute(PoddServletContextListener.PODD_PASSWORDS, passwords);
        this.log.info("Loaded passwords from : " + passwordFile);
    }
    
    /**
     * Loads aliases containing information about external file repositories from a local file and
     * adds these details to the ServletContext in a Properties object.
     * <p/>
     * 
     * @param sce
     * @throws PoddException
     */
    private void initializeFileRepositoryRegistry(final ServletContextEvent sce) throws PoddException
    {
        String aliasFile = sce.getServletContext().getInitParameter(PoddServletContextListener.INIT_ALIAS_FILE);
        if(aliasFile == null || aliasFile.trim().length() < 1)
        {
            throw new PoddException("Alias file not specified.", null, -1);
        }
        
        // TODO: add support for Windows OS paths
        if(!aliasFile.startsWith("/"))
        {
            aliasFile = poddHomeDir + File.separatorChar + aliasFile;
        }
        final Properties aliasProps = new Properties();
        try
        {
            aliasProps.load(new FileInputStream(aliasFile));
        }
        catch(final IOException e)
        {
            throw new PoddException("Could not load aliases", e, -1);
        }
        sce.getServletContext().setAttribute(PoddServletContextListener.PODD_ALIASES, aliasProps);
        this.log.info("Loaded aliases from : " + aliasFile);
    }
    
}