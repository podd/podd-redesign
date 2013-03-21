package au.org.plantphenomics.podd;

/**
 * 
 */

import org.openrdf.OpenRDFException;
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
import com.github.podd.utils.PoddWebConstants;

/**
 * Restlet Component used by the PODD web application.
 * 
 * Copied from OAS project (https://github.com/ansell/oas)
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class PoddCSIRORestletComponent extends Component
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
    public PoddCSIRORestletComponent()
    {
        super();
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param arg0
     */
    public PoddCSIRORestletComponent(final Reference arg0)
    {
        super(arg0);
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param xmlConfigRepresentation
     */
    public PoddCSIRORestletComponent(final Representation xmlConfigRepresentation)
    {
        super(xmlConfigRepresentation);
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param xmlConfigurationRef
     */
    public PoddCSIRORestletComponent(final String xmlConfigurationRef)
    {
        super(xmlConfigurationRef);
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
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
            
            // attach the web services application
            this.getDefaultHost().attach("/", nextApplication);
            
            // setup the application after attaching it, as it requires Application.getContext() to
            // not be null during the setup process
            ApplicationUtils.setupApplication(nextApplication, nextApplication.getContext());
        }
        catch(final OpenRDFException e)
        {
            throw new RuntimeException("Could not setup application", e);
        }
        
        this.log.info("routes={}", this.getDefaultHost().getRoutes().toString());
    }
    
}
