package com.github.podd.web;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * A very simple "Hello world" resource with a String representation for the PODD web application.
 */
public class HelloWorldResource extends ServerResource
{
    
    @Get
    public String represent()
    {
        return "hello, world";
    }
    
}