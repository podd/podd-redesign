package com.github.podd.integration.test;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.junit.Assert;

import org.junit.After;
import org.junit.Before;
import org.restlet.Client;
import org.restlet.data.Protocol;

public abstract class AbstractPoddRestletClientIntegrationTest extends AbstractSesameTest
{
    
    protected static String TEST_USERNAME = null;
    protected static String TEST_PASSWORD = null;
    
    protected String BASE_URL = null;
    
    protected Client client = null;
    
    /**
     * Get a Restlet Client instance with which web service requests can be made.
     * 
     * @return
     */
    protected Client getClient()
    {
        return this.client;
    }
    
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
     * Resets the web service so that any artifacts in the RDF store are wiped out.
     */
    protected abstract void resetWebService();
    
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        this.BASE_URL = "http://localhost:9090/podd-test";
        this.client = new Client(Protocol.HTTP);
        
        final InputStream passwdStream = this.getClass().getResourceAsStream("/integrationtest/passwd");
        Assert.assertNotNull("Test password file was not found", passwdStream);
        final Properties passwords = new Properties();
        passwords.load(passwdStream);
        Assert.assertTrue("Could not read test user details", passwords.size() > 0);
        final Enumeration<Object> keys = passwords.keys();
        if(keys.hasMoreElements())
        {
            AbstractPoddRestletClientIntegrationTest.TEST_USERNAME = (String)keys.nextElement();
            AbstractPoddRestletClientIntegrationTest.TEST_PASSWORD =
                    passwords.getProperty(AbstractPoddRestletClientIntegrationTest.TEST_USERNAME);
        }
        
        // Assert alias file exists
        final InputStream aliasStream = this.getClass().getResourceAsStream("/integrationtest/alias.ttl");
        Assert.assertNotNull("test alias file was not found", aliasStream);
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
    }
}
