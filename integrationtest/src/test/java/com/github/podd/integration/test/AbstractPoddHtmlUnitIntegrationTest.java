/**
 * PODD is an OWL ontology database used for scientific project management
 * 
 * Copyright (C) 2009-2013 The University Of Queensland
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.podd.integration.test;

import net.sourceforge.jwebunit.junit.WebTester;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.podd.utils.PoddWebConstants;

/**
 * Abstracts the WebTester setup and tear down actions from test implementations so they will always
 * have a new tester and not have to deal with opening and closing issues.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public abstract class AbstractPoddHtmlUnitIntegrationTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPoddHtmlUnitIntegrationTest.class);
    
    private WebTester tester;
    
    /**
     * Override this to return the http accept header for this test class.
     * 
     * @return The test HTTP Accept header to use for all requests through this class, or null to
     *         use the default Content Negotiation mechanism, which may produce random results.
     */
    protected abstract String getTestAcceptHeader();
    
    /**
     * 
     * @return The WebTester object for the current test
     */
    protected WebTester getWebTester()
    {
        return this.tester;
    }
    
    protected void login(final String username, final char[] password)
    {
        this.login(username, new String(password));
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
    
    @Before
    public void setUp() throws Exception
    {
        this.setWebTester(new WebTester());
        this.getWebTester().setBaseUrl("http://localhost:9090/podd-test/");
        this.setupTestAcceptHeader();
    }
    
    /**
     * This method is called from tests where the accept header needs to be setup.
     * 
     * HTML browser tests should set all of the normal HTML/CSS/Javascript types, while RDF tests
     * should only ever set RDF types.
     * 
     */
    protected final void setupTestAcceptHeader()
    {
        final String acceptHeader = this.getTestAcceptHeader();
        if(acceptHeader != null)
        {
            this.getWebTester().getTestContext().addRequestHeader("Accept", acceptHeader);
        }
    }
    
    /**
     * 
     * @param tester
     *            The new WebTester instance for the next test about to be executed
     */
    private void setWebTester(final WebTester tester)
    {
        this.tester = tester;
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        Exception e = null;
        
        try
        {
            this.logout();
        }
        catch(final Exception ex)
        {
            AbstractPoddHtmlUnitIntegrationTest.LOGGER.error("Found exception in logout after test", ex);
            e = ex;
        }
        
        try
        {
            // Reset using the maven build configured reset key from oas.properties
            this.getWebTester().gotoPage(
                    "/reset/" + new PropertyUtil("podd").get(PoddWebConstants.PROPERTY_TEST_WEBSERVICE_RESET_KEY, ""));
        }
        catch(final Exception ex)
        {
            AbstractPoddHtmlUnitIntegrationTest.LOGGER.error("Found exception resetting application after test", ex);
            e = ex;
        }
        
        try
        {
            this.getWebTester().closeBrowser();
        }
        catch(final Exception ex)
        {
            AbstractPoddHtmlUnitIntegrationTest.LOGGER.error("Found exception closing browser after test", ex);
            e = ex;
        }
        
        this.setWebTester(null);
        
        if(e != null)
        {
            throw e;
        }
    }
}
