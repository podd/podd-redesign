package com.github.podd.web;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * A Restlet Application for PODD.
 * 
 * @author kutila
 * 
 */
public class PoddRestletApplication extends Application
{
    
    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createInboundRoot()
    {
        // Create a router Restlet that routes each call to a new instance of HelloWorldResource.
        final Router router = new Router(this.getContext());
        
        // Defines only one route
        router.attach("/", HelloWorldResource.class);
        
        return router;
    }
    
}