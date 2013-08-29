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
package com.github.podd.prototype;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.nativerdf.NativeStore;
import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes PODD at web-app startup and destroys when the web-app is shutting down.
 * 
 * @author kutila
 * @created 2012/11/02
 */
public class PoddServletContextListener implements ServletContextListener
{
    public static final String PATH_PODD_PLANT = "/ontologies/poddPlant.owl";
    public static final String PATH_PODD_SCIENCE = "/ontologies/poddScience.owl";
    public static final String PATH_PODD_BASE = "/ontologies/poddBase.owl";
    
    public static final String URI_PODD_PLANT = "http://purl.org/podd/ns/poddPlant";
    public static final String URI_PODD_SCIENCE = "http://purl.org/podd/ns/poddScience";
    public static final String URI_PODD_BASE = "http://purl.org/podd/ns/poddBase";
    
    public static final String PODD_SERVLET_HELPER = "PODD_SERVLET_HELPER";
    
    public static final String PODD_PASSWORDS = "PODD_PASSWDS";
    public static final String PODD_ALIASES = "PODD_ALIASES";
    
    private static final String INIT_PASSWORD_FILE = "passwdfile";
    private static final String INIT_ALIAS_FILE = "aliasfile";
    private static final String INIT_SESAME_SERVER = "sesame-server";
    private static final String INIT_SESAME_REPOSITORY = "sesame-repository-id";
    
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    // private String poddHomeDir;
    
    @Override
    public void contextInitialized(final ServletContextEvent sce)
    {
        this.log.info("\r\n" + " ======================================= \r\n"
                + " Initializing PODD Prototype Web Service \r\n" + " ======================================= ");
        
        final PoddServletHelper helper = new PoddServletHelper();
        try
        {
            this.initializeAuthenticationService(sce);
            
            final String sesameServer =
                    sce.getServletContext().getInitParameter(PoddServletContextListener.INIT_SESAME_SERVER);
            final String repositoryID =
                    sce.getServletContext().getInitParameter(PoddServletContextListener.INIT_SESAME_REPOSITORY);
            
            Repository nextRepository = null;
            if(sesameServer != null && repositoryID != null && sesameServer.trim().length() > 0
                    && repositoryID.trim().length() > 0)
            {
                nextRepository = new HTTPRepository(sesameServer, repositoryID);
                this.log.info("Using remote RDF Repository: " + sesameServer + ", " + repositoryID);
            }
            else
            {
                final String nativeStoreDir =
                        sce.getServletContext().getAttribute("javax.servlet.context.tempdir").toString()
                                + File.separatorChar + "native";
                this.log.warn("No remote RDF Repository details. Setting up a native store in {}", nativeStoreDir);
                nextRepository =
                        new SailRepository(new NativeStore(new File(nativeStoreDir),
                                "spoc,posc,cspo,cpso,psoc,ospc,opsc,cops"));
            }
            nextRepository.initialize();
            helper.setUp(nextRepository);
            
            // populate Helper with a list of Schema Ontologies to manage
            final List<Entry<URI, String>> schemaOntologyList = new ArrayList<>();
            schemaOntologyList.add(new SimpleEntry<URI, String>(nextRepository.getValueFactory().createURI(
                    PoddServletContextListener.URI_PODD_BASE), PoddServletContextListener.PATH_PODD_BASE));
            schemaOntologyList.add(new SimpleEntry<URI, String>(nextRepository.getValueFactory().createURI(
                    PoddServletContextListener.URI_PODD_SCIENCE), PoddServletContextListener.PATH_PODD_SCIENCE));
            schemaOntologyList.add(new SimpleEntry<URI, String>(nextRepository.getValueFactory().createURI(
                    PoddServletContextListener.URI_PODD_PLANT), PoddServletContextListener.PATH_PODD_PLANT));
            helper.setSchemaOntologyList(schemaOntologyList);
            helper.loadSchemaOntologies();
            
            helper.setFileReferenceUtils(this.setupFileRepositoryAliases(sce));
            
            sce.getServletContext().setAttribute(PoddServletContextListener.PODD_SERVLET_HELPER, helper);
            this.log.info("\r\n ... initialization complete.");
        }
        catch(OWLException | OpenRDFException | IOException | PoddException e)
        {
            final String message = "Failed to setup PODD web service";
            this.log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }
    
    @Override
    public void contextDestroyed(final ServletContextEvent sce)
    {
        this.log.info("\r\n" + " ====================================== \r\n"
                + " TERMINATING PODD Prototype Web Service \r\n" + " ====================================== ");
        try
        {
            final PoddServletHelper helper =
                    (PoddServletHelper)sce.getServletContext().getAttribute(
                            PoddServletContextListener.PODD_SERVLET_HELPER);
            if(helper != null)
            {
                helper.tearDown();
            }
        }
        catch(final RepositoryException e)
        {
            this.log.error("Failed to clean up Servlet Helper", e);
            // TODO: check whether hiding this Exception leads to any issues
        }
        finally
        {
            sce.getServletContext().removeAttribute(PoddServletContextListener.PODD_SERVLET_HELPER);
            sce.getServletContext().removeAttribute(PoddServletContextListener.PODD_PASSWORDS);
        }
        this.log.info("\r\n ... termination complete.");
    }
    
    /**
     * Loads the passwords from a file and adds them to the ServletContext in a Properties object.
     * 
     * @param sce
     * @throws PoddException
     */
    private void initializeAuthenticationService(final ServletContextEvent sce) throws PoddException
    {
        final String passwordFile =
                sce.getServletContext().getInitParameter(PoddServletContextListener.INIT_PASSWORD_FILE);
        if(passwordFile == null || passwordFile.trim().length() < 1)
        {
            throw new PoddException("Password file not specified.", null, -1);
        }
        
        final Properties passwords = new Properties();
        try
        {
            passwords.load(this.getClass().getResourceAsStream(passwordFile));
        }
        catch(final IOException e)
        {
            throw new PoddException("Could not load passwords", e, -1);
        }
        sce.getServletContext().setAttribute(PoddServletContextListener.PODD_PASSWORDS, passwords);
        this.log.info("Loaded passwords from : " + passwordFile);
    }
    
    /**
     * Loads aliases containing information about external file repositories from a local file and
     * adds these details to the ServletContext in a Properties object.
     * <p/>
     * 
     * @param sce
     * @throws PoddException
     */
    private FileReferenceUtils setupFileRepositoryAliases(final ServletContextEvent sce) throws PoddException
    {
        final String aliasFile = sce.getServletContext().getInitParameter(PoddServletContextListener.INIT_ALIAS_FILE);
        if(aliasFile == null || aliasFile.trim().length() < 1)
        {
            throw new PoddException("Alias file not specified.", null, -1);
        }
        
        final FileReferenceUtils fileReferenceUtils = new FileReferenceUtils();
        final InputStream stream = this.getClass().getResourceAsStream(aliasFile);
        
        if(stream == null)
        {
            this.log.warn("File Repository Aliases were not found, File Reference Attachments will not be functional!");
        }
        else
        {
            try
            {
                fileReferenceUtils.setAliases(stream, RDFFormat.TURTLE);
                this.log.info("Loaded aliases from : {}", aliasFile);
            }
            catch(RepositoryException | RDFParseException | IOException e1)
            {
                this.log.error("Failed to load aliases from : {}", aliasFile);
                throw new PoddException("Could not set File Repository Aliases due to an exception", e1, null, -1);
            }
        }
        
        return fileReferenceUtils;
    }
}