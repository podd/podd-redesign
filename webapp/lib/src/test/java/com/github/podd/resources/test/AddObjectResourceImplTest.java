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
package com.github.podd.resources.test;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class AddObjectResourceImplTest extends AbstractResourceImplTest
{
    
    /**
     * Test viewing the edit HTML page for creating a new Publication object.
     */
    @Test
    public void testGetAddPublicationObjectHtml() throws Exception
    {
        // prepare: add an artifact
        final InferredOWLOntologyID ontologyID =
                this.loadTestArtifact("/test/artifacts/basic-2.rdf", MediaType.APPLICATION_RDF_XML);
        final String parentUri = "http://purl.org/podd/ns/some-random-top-object-uri";
        
        final ClientResource addObjectClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_OBJECT_ADD));
        
        try
        {
            // mandatory parameter object-type
            addObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER,
                    "http://purl.org/podd/ns/poddScience#Publication");
            
            addObjectClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, ontologyID
                    .getOntologyIRI().toString());
            
            addObjectClientResource.addQueryParameter(PoddWebConstants.KEY_PARENT_IDENTIFIER, parentUri);
            
            addObjectClientResource.addQueryParameter(PoddWebConstants.KEY_PARENT_PREDICATE_IDENTIFIER,
                    "http://purl.org/podd/ns/poddScience#hasPublication");
            
            final Representation results =
                    this.doTestAuthenticatedRequest(addObjectClientResource, Method.GET, null, MediaType.TEXT_HTML,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final String body = this.getText(results);
            
            // verify:
            // System.out.println(body);
            Assert.assertTrue("Page title not as expected", body.contains("Add new Publication"));
            Assert.assertTrue("object type not set",
                    body.contains("podd.objectTypeUri = 'http://purl.org/podd/ns/poddScience#Publication'"));
            Assert.assertTrue("parent URI not set",
                    body.contains("podd.parentUri = 'http://purl.org/podd/ns/some-random-top-object-uri'"));
            Assert.assertTrue("parent predicate not set",
                    body.contains("podd.parentPredicateUri = 'http://purl.org/podd/ns/poddScience#hasPublication'"));
            
            this.assertFreemarker(body);
        }
        finally
        {
            this.releaseClient(addObjectClientResource);
        }
    }
    
    /**
     * Test viewing the edit HTML page for creating a new Project.
     */
    @Test
    public void testGetAddTopObjectHtml() throws Exception
    {
        final ClientResource addObjectClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_OBJECT_ADD));
        
        try
        {
            // mandatory parameter object-type
            addObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER,
                    "http://purl.org/podd/ns/poddScience#Project");
            
            final Representation results =
                    this.doTestAuthenticatedRequest(addObjectClientResource, Method.GET, null, MediaType.TEXT_HTML,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final String body = this.getText(results);
            
            // this.log.trace(body);
            
            // verify:
            // System.out.println(body);
            Assert.assertTrue("Page title not as expected", body.contains("Add new Project"));
            Assert.assertTrue("object type not set in script",
                    body.contains("podd.objectTypeUri = 'http://purl.org/podd/ns/poddScience#Project'"));
            
            this.assertFreemarker(body);
        }
        finally
        {
            this.releaseClient(addObjectClientResource);
        }
    }
    
}
