package com.github.podd.prototype.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.prototype.PoddServletContextListener;

public abstract class AbstractPoddIntegrationTest
{
    
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    protected String BASE_URL = null;
    
    /**
     * Override this to return the http accept header for this test class.
     * 
     * @return The test HTTP Accept header to use for all requests through this class, or null to
     *         use the default Content Negotiation mechanism, which may produce random results.
     */
    protected abstract String getTestAcceptHeader();
    
    /**
     * Logs in the user with the given username and password using whatever method matches the
     * integration test implementing this method.
     * 
     * @param username
     *            The username of the user to login.
     * @param password
     *            The password of the user to login.
     */
    protected abstract void login(String username, String password);
    
    /**
     * Logs out any user that is currently logged in.
     */
    protected abstract void logout();
    
    @Before
    public void setUp() throws Exception
    {
        this.BASE_URL = "http://localhost:9090/podd-test";

        String poddHome = System.getProperty(PoddServletContextListener.PODD_HOME);
        Properties passwords = new Properties();
        passwords.setProperty("john", "wayne");
        this.writeFile(passwords, poddHome + "/passwd");
        
        Properties aliases = new Properties();
        aliases.setProperty("localhost.protocol", "http");
        aliases.setProperty("localhost.host", "localhost");
        this.writeFile(aliases, poddHome + "/alias");
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        try
        {
            // TODO: Reset the data store
            this.logout();
        }
        catch(final Exception ex)
        {
            this.log.error("Found exception in tearDown after test", ex);
        }
        
        try
        {
            String poddHome = System.getProperty(PoddServletContextListener.PODD_HOME);
            this.deleteFile(poddHome + "/passwd");
            this.deleteFile(poddHome + "/alias");
        }
        catch(final Exception ex)
        {
            this.log.error("Found exception resetting application after test", ex);
        }
        
    }

    private void writeFile(Properties props, String filename)
    {
        try
        {
            props.store(new FileOutputStream(filename), "");
            this.log.info("Created file: " + filename);
        }
        catch(IOException e)
        {
            this.log.error("Failed to create file: " + filename, e);
        }
    }

    private void deleteFile(String filename)
    {
        boolean deleted = false;
        File f = new File(filename);
        if (f.exists() && !f.isDirectory())
        {
            deleted = f.delete();
        }
        if (deleted)
        {
            this.log.info("Deleted file: " + filename);
        }
        else
        {
            this.log.info("Could not delete file: " + filename);
        }
    }
    
}
