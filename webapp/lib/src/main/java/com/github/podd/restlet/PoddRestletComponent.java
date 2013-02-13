/**
 * 
 */
package com.github.podd.restlet;

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
import com.github.podd.utils.PoddWebConstants;

/**
 * Restlet Component used by the PODD web application.
 * 
 * Copied from OAS project (https://github.com/ansell/oas)
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class PoddRestletComponent extends Component
{
    private final Logger log = LoggerFactory.getLogger(PoddRestletComponent.class);
    
    private String resetKey;
    
    static
    {
        // add the ability to view java.util.logging messages using SLF4J
        SLF4JBridgeHandler.install();
    }
    
    /**
     * 
     */
    public PoddRestletComponent()
    {
        super();
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param arg0
     */
    public PoddRestletComponent(final Reference arg0)
    {
        super(arg0);
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param xmlConfigRepresentation
     */
    public PoddRestletComponent(final Representation xmlConfigRepresentation)
    {
        super(xmlConfigRepresentation);
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param xmlConfigurationRef
     */
    public PoddRestletComponent(final String xmlConfigurationRef)
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
        
        // NOTE: This needs to be Impl as Restlet is not designed using interfaces, so we cannot
        // easily include our interface in the process.
        final PoddWebServiceApplication nextApplication = new PoddWebServiceApplicationImpl();
        
        // Create a resource to use in integration tests to reset the application to a fresh state
        // on demand, without adding this functionality to OasWebServiceApplicationImpl
//        final TestResetResource reset = new TestResetResource(nextApplication);
//        this.setResetKey(PropertyUtil.getProperty("oas.test.website.reset.key", ""));
//        final String resetPath = "/reset/" + this.getResetKey();
//        this.getDefaultHost().attach(resetPath, reset);
        
        // attach the resources first
        this.getDefaultHost().attach(resourcesPath, directory);
        
        // attach the web services application
        this.getDefaultHost().attach("/", nextApplication);
        
        // setup the application after attaching it, as it requires Application.getContext() to not
        // be null during the setup process
        ApplicationUtils.setupApplication(nextApplication, nextApplication.getContext());
        
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
