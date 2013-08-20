/**
 * 
 */
package com.github.podd.restlet.integrationtest;

import java.io.IOException;

import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
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

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.ansell.restletutils.ClassLoaderDirectory;
import com.github.ansell.restletutils.CompositeClassLoader;
import com.github.podd.restlet.ApplicationUtils;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.PoddWebServiceApplicationImpl;
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
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private String resetKey;
    
    static
    {
        // add the ability to view java.util.logging messages using SLF4J
        SLF4JBridgeHandler.install();
    }
    
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
        
        this.log.info("attaching resource handler to path={}", resourcesPath);
        
        // attach the resources first
        this.getDefaultHost().attach(resourcesPath, directory);
        
        PoddWebServiceApplication nextApplication;
        try
        {
            nextApplication = new PoddWebServiceApplicationImpl();
            
            // Add a route for the reset service.
            final String resetPath =
                    "/reset/" + PropertyUtil.get(PoddWebConstants.PROPERTY_TEST_WEBSERVICE_RESET_KEY, "");
            this.log.info("attaching reset service to path={}", resetPath);
            final TestResetResourceImpl reset = new TestResetResourceImpl(nextApplication);
            this.setResetKey(PropertyUtil.get(PoddWebConstants.PROPERTY_TEST_WEBSERVICE_RESET_KEY, ""));
            
            this.getDefaultHost().attach(resetPath, reset);
            
            // attach the web services application
            this.getDefaultHost().attach("/", nextApplication);
            
            nextApplication.setAliasesConfiguration(Rio.parse(this.getClass().getResourceAsStream("/test-alias.ttl"),
                    "", RDFFormat.TURTLE));
            
            // setup the application after attaching it, as it requires Application.getContext() to
            // not be null during the setup process
            ApplicationUtils.setupApplication(nextApplication, nextApplication.getContext());
            ApplicationUtils.setupTestUser(nextApplication);
        }
        catch(final OpenRDFException | UnsupportedRDFormatException | IOException e)
        {
            throw new RuntimeException("Could not setup application", e);
        }
        
        this.log.info("routes={}", this.getDefaultHost().getRoutes().toString());
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
