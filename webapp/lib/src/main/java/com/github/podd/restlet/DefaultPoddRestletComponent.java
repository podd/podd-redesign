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
package com.github.podd.restlet;

/**
 * 
 */

import java.io.IOException;

import org.openrdf.OpenRDFException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.restlet.Component;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.routing.Router;
import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.github.ansell.restletutils.ClassLoaderDirectory;
import com.github.ansell.restletutils.CompositeClassLoader;
import com.github.podd.exception.PoddException;
import com.github.podd.utils.PoddWebConstants;

/**
 * Restlet Component used by the PODD web application.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DefaultPoddRestletComponent extends Component
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
    
    private static final Logger log = LoggerFactory.getLogger(DefaultPoddRestletComponent.class);
    
    /**
     * 
     */
    public DefaultPoddRestletComponent()
    {
        super();
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param arg0
     */
    public DefaultPoddRestletComponent(final Reference arg0)
    {
        super(arg0);
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param xmlConfigRepresentation
     */
    public DefaultPoddRestletComponent(final Representation xmlConfigRepresentation)
    {
        super(xmlConfigRepresentation);
        
        this.getClients().add(Protocol.CLAP);
        this.getClients().add(Protocol.HTTP);
        this.initialise();
    }
    
    /**
     * @param xmlConfigurationRef
     */
    public DefaultPoddRestletComponent(final String xmlConfigurationRef)
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
        
        DefaultPoddRestletComponent.log.info("attaching resource handler to path={}", resourcesPath);
        
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
        catch(final OpenRDFException | UnsupportedRDFormatException | IOException | OWLException | PoddException e)
        {
            throw new RuntimeException("Could not setup application", e);
        }
        
        DefaultPoddRestletComponent.log.info("routes={}", this.getDefaultHost().getRoutes().toString());
    }
    
}
