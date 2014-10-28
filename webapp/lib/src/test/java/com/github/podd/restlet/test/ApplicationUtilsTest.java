/**
 *
 */
package com.github.podd.restlet.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.restlet.Context;

import com.github.podd.restlet.ApplicationUtils;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.PoddWebServiceApplicationImpl;

/**
 * @author ans025
 *
 */
public class ApplicationUtilsTest
{
    
    private PoddWebServiceApplication application;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.application = new PoddWebServiceApplicationImpl();
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        if(this.application != null)
        {
            this.application.stop();
        }
        this.application = null;
    }
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.ApplicationUtils#getNewAuthenticator(org.restlet.security.Realm, org.restlet.Context, com.github.ansell.propertyutil.PropertyUtil)}
     * .
     */
    @Ignore
    @Test
    public final void testGetNewAuthenticator()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.ApplicationUtils#getNewManagementRepository(com.github.ansell.propertyutil.PropertyUtil)}
     * .
     */
    @Ignore
    @Test
    public final void testGetNewManagementRepository()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.ApplicationUtils#getNewTemplateConfiguration(org.restlet.Context)}
     * .
     */
    @Ignore
    @Test
    public final void testGetNewTemplateConfiguration()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.ApplicationUtils#setupApplication(com.github.podd.restlet.PoddWebServiceApplication, org.restlet.Context)}
     * .
     */
    @Test
    public final void testSetupApplication() throws Exception
    {
        ApplicationUtils.setupApplication(this.application, new Context());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.ApplicationUtils#setupSchemas(com.github.podd.restlet.PoddWebServiceApplication)}
     * .
     */
    @Ignore("Cannot easily test setupSchemas at this point without running most of setupApplication")
    @Test
    public final void testSetupSchemas() throws Exception
    {
        ApplicationUtils.setupSchemas(this.application);
    }
    
}
