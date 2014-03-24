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
package com.github.podd.restlet.integrationtest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.exception.PoddException;
import com.github.podd.restlet.ApplicationUtils;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.test.TestUtils;

/**
 * Resets an application using ApplicationUtils.setupApplication.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class TestResetResourceImpl extends Restlet
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private PoddWebServiceApplication application;
    
    /**
     */
    public TestResetResourceImpl()
    {
        super();
    }
    
    public TestResetResourceImpl(final PoddWebServiceApplication nextApplication)
    {
        this.application = nextApplication;
    }
    
    @Override
    public void handle(final Request request, final Response response)
    {
        super.handle(request, response);
        
        this.log.info("========== Reset called ==========");
        try
        {
            // Reset the aliases configuration
            this.application.setDataRepositoryConfig(Rio.parse(this.getClass().getResourceAsStream("/test-alias.ttl"),
                    "", RDFFormat.TURTLE));
            ApplicationUtils.setupApplication(this.application, this.application.getContext());
            TestUtils.setupTestUser(this.application);
        }
        catch(final OpenRDFException | IOException | OWLException | PoddException | ExecutionException
                | InterruptedException e)
        {
            this.log.error("Could not reset application", e);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not reset application", e);
        }
        this.log.info("========== Reset complete ==========");
        
        response.setStatus(Status.SUCCESS_NO_CONTENT);
    }
    
}
