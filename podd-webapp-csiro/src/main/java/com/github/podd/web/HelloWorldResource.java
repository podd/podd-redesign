package com.github.podd.web;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.restlet.RestletUtils;

import freemarker.template.Configuration;

/**
 * A very simple "Hello world" resource with a Freemarker HTML representation for the PODD web application.
 */
public class HelloWorldResource extends ServerResource
{
    
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Get("html")
    public Representation toHtml() 
    {
        log.info("Somebody's called me. :-)");
        
        Map<String, Object> dataModel = new HashMap<String, Object>();
        dataModel.put("pageTitle", "Welcome Page");
        Form form = this.getRequest().getResourceRef().getQueryAsForm();
        Parameter user = form.getFirst("user");
        if (user != null)
        {
            dataModel.put("user", user.getValue());
        }
        else
        {
            dataModel.put("user", "Anonymous");
        }

        Configuration freemarkerConfiguration = RestletUtils.getNewTemplateConfiguration(this.getContext());
        MediaType mediaType = MediaType.TEXT_HTML;
        String templateName = "simple.html.ftl";

        log.info("Sending HTML...");
        
        return RestletUtils.getHtmlRepresentation(templateName, dataModel, mediaType, freemarkerConfiguration);
    }
    
    
}