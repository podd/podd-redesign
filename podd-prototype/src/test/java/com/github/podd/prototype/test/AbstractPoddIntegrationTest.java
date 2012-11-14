package com.github.podd.prototype.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.restlet.Client;
import org.restlet.data.Protocol;

import com.github.podd.prototype.PoddServletContextListener;

public abstract class AbstractPoddIntegrationTest extends AbstractSesameTest
{
    
    protected static final String TEST_USERNAME = "john";
    protected static final String TEST_PASSWORD = "wayne";
    
    protected String BASE_URL = null;
    
    protected Client client = null;
    
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
    
    /**
     * Resets the web service so that the RDF store is wiped clean.
     */
    protected abstract void resetWebService();
    
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        this.BASE_URL = "http://localhost:9090/podd-test";
        this.client = new Client(Protocol.HTTP);
        
        // -- generate password file
        final String poddHome = System.getProperty(PoddServletContextListener.PODD_HOME);
        final Properties passwords = new Properties();
        passwords.setProperty(AbstractPoddIntegrationTest.TEST_USERNAME, AbstractPoddIntegrationTest.TEST_PASSWORD);
        this.writeFile(passwords, poddHome + "/passwd");
        
        // -- generate alias file
        final Properties aliases = new Properties();
        aliases.setProperty("localhost.protocol", "http");
        aliases.setProperty("localhost.host", "localhost");
        aliases.setProperty("w3.protocol", "http");
        aliases.setProperty("w3.host", "www.w3.org");
        this.writeFile(aliases, poddHome + "/alias");
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        try
        {
            this.resetWebService();
            this.logout();
            this.client.stop();
        }
        catch(final Exception ex)
        {
            this.log.error("Found exception in tearDown after test", ex);
        }
        
        try
        {
            final String poddHome = System.getProperty(PoddServletContextListener.PODD_HOME);
            this.deleteFile(poddHome + "/passwd");
            this.deleteFile(poddHome + "/alias");
        }
        catch(final Exception ex)
        {
            this.log.error("Found exception resetting application after test", ex);
        }
    }
    
    /**
     * Get a Restlet Client instance with which web service requests can be made.
     * 
     * @return
     */
    protected Client getClient()
    {
        return this.client;
    }

    private void writeFile(final Properties props, final String filename)
    {
        try
        {
            props.store(new FileOutputStream(filename), "");
            this.log.debug("Created file: " + filename);
        }
        catch(final IOException e)
        {
            this.log.error("Failed to create file: " + filename, e);
        }
    }
    
    private void deleteFile(final String filename)
    {
        boolean deleted = false;
        final File f = new File(filename);
        if(f.exists() && !f.isDirectory())
        {
            deleted = f.delete();
        }
        if(deleted)
        {
            this.log.debug("Deleted file: " + filename);
        }
        else
        {
            this.log.debug("Could not delete file: " + filename);
        }
    }
}
