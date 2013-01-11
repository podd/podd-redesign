package com.github.podd.web;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * A Restlet Application for PODD.
 * 
 * @author kutila
 * 
 */
public class PoddRestletApplication extends Application
{
 
    static
    {
        // add the ability to view java.util.logging messages using SLF4J
        SLF4JBridgeHandler.install();
        
        //NOTE: this doesn't seem to have any effect right now. Logs are printed on the console though,
        // so fine for the moment.
    }
    
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    public PoddRestletApplication()
    {
        super();
        this.getConnectorService().getClientProtocols().add(Protocol.HTTP);
        this.getConnectorService().getClientProtocols().add(Protocol.CLAP);
        
        this.log.info("Created PoddRestletApplication");
    }
    
    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createInboundRoot()
    {
        // Create a router Restlet that routes each call to a new instance of HelloWorldResource.
        Context context = this.getContext();
        final Router router = new Router(context);
        
        // Defines only one route
        router.attach("/", HelloWorldResource.class);
        router.attach("/hello.html", HelloWorldResource.class);
        
        this.log.info("Router attached");
        return router;
    }
    
}