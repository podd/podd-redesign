/**
 * 
 */
package com.github.podd.oas;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.representation.Representation;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.ansell.restletutils.ClassLoaderDirectory;
import com.github.ansell.restletutils.CompositeClassLoader;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;

/**
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class PoddTestWebsiteComponent extends Component
{
    private static final Logger log = LoggerFactory.getLogger(PoddTestWebsiteComponent.class);
    private String resetKey;
    
    static
    {
        // add the ability to view java.util.logging messages using SLF4J
        SLF4JBridgeHandler.install();
    }
    
    /**
     * 
     */
    public PoddTestWebsiteComponent()
    {
        super();
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param arg0
     */
    public PoddTestWebsiteComponent(final Reference arg0)
    {
        super(arg0);
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param xmlConfigRepresentation
     */
    public PoddTestWebsiteComponent(final Representation xmlConfigRepresentation)
    {
        super(xmlConfigRepresentation);
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param xmlConfigurationRef
     */
    public PoddTestWebsiteComponent(final String xmlConfigurationRef)
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
        
        final String resourcesPath = "/resources/";
        
        PoddTestWebsiteComponent.log.info("attaching resource handler to path={}", resourcesPath);
        
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
        
        PoddTestWebsiteComponent.log.info("routes={}", this.getDefaultHost().getRoutes().toString());
        
    }
    
    
    public void kinitialise()
    {
        // FIXME: Make this configurable
        final LocalReference localReference = LocalReference.createClapReference(LocalReference.CLAP_THREAD, "static/");

        final String resourcesPath = "/resources/";
        
        PoddTestWebsiteComponent.log.info("attaching resource handler to path={}", resourcesPath);
        
        // NOTE: This needs to be Impl as Restlet is not designed using interfaces, so we cannot
        // easily include our interface in the process.
        final PoddWebServiceApplication nextApplication = new PoddWebServiceApplicationImpl();
        
        
        // Oas attaches a CompositeClassLoader to the router
        // Oas attaches a reset resource to the router - for use in integration tests
        
        // attach the web services application
        this.getDefaultHost().attach("/", nextApplication);
        
        // setup the application after attaching it, as it requires Application.getContext() to not
        // be null during the setup process
        this.setupApplication(nextApplication, nextApplication.getContext());
        
        PoddTestWebsiteComponent.log.info("routes={}", this.getDefaultHost().getRoutes().toString());
        
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
    
    private void setupApplication(PoddWebServiceApplication application, Context applicationContext)
    {
        // final Context templateChildContext = applicationContext.createChildContext();
        final Configuration newTemplateConfiguration = PoddTestWebsiteComponent.getNewTemplateConfiguration(applicationContext);
        application.setTemplateConfiguration(newTemplateConfiguration);
        
        // create a custom error handler using our overridden PoddStatusService together with the
        // freemarker configuration
        final PoddStatusService statusService = new PoddStatusService(newTemplateConfiguration);
        application.setStatusService(statusService);
    }
    
    public static Configuration getNewTemplateConfiguration(final Context newChildContext)
    {
        final Configuration result = new Configuration();
        // FIXME: Make this configurable
        result.setTemplateLoader(new ContextTemplateLoader(newChildContext, "clap://class/templates"));
        
        final BeansWrapper myWrapper = new BeansWrapper();
        myWrapper.setSimpleMapWrapper(true);
        result.setObjectWrapper(myWrapper);
        
        return result;
    }
    
    
    
}
