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
package com.github.podd.restlet.integrationtest;

import org.openrdf.OpenRDFException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.restlet.Component;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.github.ansell.restletutils.ClassLoaderDirectory;
import com.github.ansell.restletutils.CompositeClassLoader;
import com.github.podd.restlet.ApplicationUtils;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.PoddWebServiceApplicationImpl;
import com.github.podd.test.TestUtils;
import com.github.podd.utils.PoddWebConstants;

/**
 * Restlet Component used by the PODD web application.
 * 
 * Copied from OAS project (https://github.com/ansell/oas)
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class PoddRestletIntegrationTestComponent extends Component
{
    static
    {
        System.setProperty("org.restlet.engine.loggerFacadeClass", "org.restlet.ext.slf4j.Slf4jLoggerFacade");
        
        // Optionally remove existing handlers attached to j.u.l root logger
        SLF4JBridgeHandler.removeHandlersForRootLogger(); // (since SLF4J 1.6.5)
        
        // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
        // the initialization phase of your application
        SLF4JBridgeHandler.install();
    }
    
    private static final Logger log = LoggerFactory.getLogger(PoddRestletIntegrationTestComponent.class);
    
    private String resetKey;
    
    /**
     * 
     */
    public PoddRestletIntegrationTestComponent()
    {
        super();
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param arg0
     */
    public PoddRestletIntegrationTestComponent(final Reference arg0)
    {
        super(arg0);
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param xmlConfigRepresentation
     */
    public PoddRestletIntegrationTestComponent(final Representation xmlConfigRepresentation)
    {
        super(xmlConfigRepresentation);
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param xmlConfigurationRef
     */
    public PoddRestletIntegrationTestComponent(final String xmlConfigurationRef)
    {
        super(xmlConfigurationRef);
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @return the resetKey
     */
    protected String getResetKey()
    {
        return this.resetKey;
    }
    
    public void initialise()
    {
        // FIXME: Make this configurable
        final LocalReference localReference = LocalReference.createClapReference(LocalReference.CLAP_THREAD, "static/");
        
        final CompositeClassLoader customClassLoader = new CompositeClassLoader();
        customClassLoader.addClassLoader(Thread.currentThread().getContextClassLoader());
        customClassLoader.addClassLoader(Router.class.getClassLoader());
        
        final ClassLoaderDirectory directory =
                new ClassLoaderDirectory(this.getContext().createChildContext(), localReference, customClassLoader);
        
        directory.setListingAllowed(true);
        
        final String resourcesPath = PoddWebConstants.PATH_RESOURCES;
        
        PoddRestletIntegrationTestComponent.log.info("attaching resource handler to path={}", resourcesPath);
        
        // attach the resources first
        this.getDefaultHost().attach(resourcesPath, directory);
        
        PoddWebServiceApplication nextApplication;
        try
        {
            nextApplication = new PoddWebServiceApplicationImpl();
            
            final String resetKey =
                    nextApplication.getPropertyUtil().get(PoddWebConstants.PROPERTY_TEST_WEBSERVICE_RESET_KEY, "");
            // Add a route for the reset service.
            final String resetPath = "/reset/" + resetKey;
            PoddRestletIntegrationTestComponent.log.info("attaching reset service to path={}", resetPath);
            final TestResetResourceImpl reset = new TestResetResourceImpl(nextApplication);
            this.setResetKey(resetKey);
            
            this.getDefaultHost().attach(resetPath, reset);
            
            // attach the web services application
            this.getDefaultHost().attach("/", nextApplication);
            
            // nextApplication.setAliasesConfiguration(Rio.parse(this.getClass().getResourceAsStream("/test-alias.ttl"),
            // "", RDFFormat.TURTLE));
            
            // setup the application after attaching it, as it requires Application.getContext() to
            // not be null during the setup process
            ApplicationUtils.setupApplication(nextApplication, nextApplication.getContext());
            TestUtils.setupTestUser(nextApplication);
        }
        catch(final OpenRDFException | UnsupportedRDFormatException e)
        {
            throw new RuntimeException("Could not setup application", e);
        }
        
        PoddRestletIntegrationTestComponent.log.info("routes={}", this.getDefaultHost().getRoutes().toString());
    }
    
    /**
     * This field is used in testing to enable the resetting of the internal elements of the website
     * after each test.
     * 
     * It is protected by a simple runtime generated key to prevent this function leaking out if
     * people directly deploy this TEST website component.
     * 
     * @param key
     *            A simple key shared with us by the test running environment.
     */
    protected void setResetKey(final String key)
    {
        this.resetKey = key;
    }
    
}
