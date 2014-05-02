/**
 * 
 */
package com.github.podd.restlet.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.restlet.Context;
import org.semanticweb.owlapi.model.OWLException;

import com.github.podd.exception.PoddException;
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
        application = new PoddWebServiceApplicationImpl();
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        application.stop();
        application = null;
    }
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.ApplicationUtils#getNewAuthenticator(org.restlet.security.Realm, org.restlet.Context, com.github.ansell.propertyutil.PropertyUtil)}
     * .
     */
    @Test
    public final void testGetNewAuthenticator()
    {
        fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.ApplicationUtils#getNewManagementRepository(com.github.ansell.propertyutil.PropertyUtil)}
     * .
     */
    @Test
    public final void testGetNewManagementRepository()
    {
        fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.ApplicationUtils#getNewTemplateConfiguration(org.restlet.Context)}
     * .
     */
    @Test
    public final void testGetNewTemplateConfiguration()
    {
        fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.ApplicationUtils#setupApplication(com.github.podd.restlet.PoddWebServiceApplication, org.restlet.Context)}
     * .
     */
    @Test
    public final void testSetupApplication() throws Exception
    {
        ApplicationUtils.setupApplication(application, new Context());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.ApplicationUtils#setupSchemas(com.github.podd.restlet.PoddWebServiceApplication)}
     * .
     */
    @Test
    public final void testSetupSchemas() throws Exception
    {
        ApplicationUtils.setupSchemas(application);
    }
    
}
