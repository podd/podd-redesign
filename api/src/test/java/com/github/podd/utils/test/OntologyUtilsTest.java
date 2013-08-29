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
/**
 * 
 */
package com.github.podd.utils.test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PoddRdfConstants;

/**
 * Test for OntologyUtils class that translates between RDF and InferredOWLOntologyID instances.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class OntologyUtilsTest
{
    private URI testOntologyUri1;
    private URI testVersionUri1;
    private URI testInferredUri1;
    private ValueFactory vf;
    
    @Before
    public void setUp() throws Exception
    {
        this.vf = ValueFactoryImpl.getInstance();
        this.testOntologyUri1 = this.vf.createURI("urn:test:ontology:uri:1");
        this.testVersionUri1 = this.vf.createURI("urn:test:ontology:uri:1:version:1");
        this.testInferredUri1 = this.vf.createURI("urn:inferred:test:ontology:uri:1:version:1");
        
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.vf = null;
        this.testOntologyUri1 = null;
        this.testVersionUri1 = null;
        this.testInferredUri1 = null;
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#modelToOntologyIDs(org.openrdf.model.Model)}.
     */
    @Test
    public final void testModelToOntologyIDsEmpty()
    {
        final Model input = new LinkedHashModel();
        final Collection<InferredOWLOntologyID> modelToOntologyIDs = OntologyUtils.modelToOntologyIDs(input);
        
        // No statements, should return an empty collection
        Assert.assertEquals(0, modelToOntologyIDs.size());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#modelToOntologyIDs(org.openrdf.model.Model)}.
     */
    @Test
    public final void testModelToOntologyIDsNoVersion()
    {
        final Model input = new LinkedHashModel();
        input.add(this.vf.createStatement(this.testOntologyUri1, RDF.TYPE, OWL.ONTOLOGY));
        final Collection<InferredOWLOntologyID> modelToOntologyIDs = OntologyUtils.modelToOntologyIDs(input);
        
        // Must have a version to be returned
        Assert.assertEquals(0, modelToOntologyIDs.size());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#modelToOntologyIDs(org.openrdf.model.Model)}.
     */
    @Test
    public final void testModelToOntologyIDsOneVersion()
    {
        final Model input = new LinkedHashModel();
        input.add(this.vf.createStatement(this.testOntologyUri1, RDF.TYPE, OWL.ONTOLOGY));
        input.add(this.vf.createStatement(this.testVersionUri1, RDF.TYPE, OWL.ONTOLOGY));
        input.add(this.vf.createStatement(this.testOntologyUri1, OWL.VERSIONIRI, this.testVersionUri1));
        final Collection<InferredOWLOntologyID> modelToOntologyIDs = OntologyUtils.modelToOntologyIDs(input);
        
        // 1 ontology returned
        Assert.assertEquals(1, modelToOntologyIDs.size());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToHandler(java.util.Collection, org.openrdf.rio.RDFHandler)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public final void testOntologyIDsToHandlerAnonymousOntology() throws Exception
    {
        final Model input = new LinkedHashModel();
        
        OntologyUtils.ontologyIDsToHandler(Arrays.asList(new InferredOWLOntologyID((IRI)null, null, null)),
                new StatementCollector(input));
        
        Assert.assertTrue(input.isEmpty());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToHandler(java.util.Collection, org.openrdf.rio.RDFHandler)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public final void testOntologyIDsToHandlerEmptyNotNull() throws Exception
    {
        final Model input = new LinkedHashModel();
        
        OntologyUtils.ontologyIDsToHandler(Collections.<InferredOWLOntologyID> emptyList(), new StatementCollector(
                input));
        
        Assert.assertTrue(input.isEmpty());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToHandler(java.util.Collection, org.openrdf.rio.RDFHandler)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public final void testOntologyIDsToHandlerEmptyNull() throws Exception
    {
        OntologyUtils.ontologyIDsToHandler(Collections.<InferredOWLOntologyID> emptyList(), (RDFHandler)null);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToHandler(java.util.Collection, org.openrdf.rio.RDFHandler)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public final void testOntologyIDsToHandlerNoInferredIRI() throws Exception
    {
        final Model input = new LinkedHashModel();
        
        OntologyUtils.ontologyIDsToHandler(Arrays.asList(new InferredOWLOntologyID(IRI
                .create("urn:test:ontology:iri:abc"), IRI.create("urn:test:ontology:iri:abc:version:1"), null)),
                new StatementCollector(input));
        
        Assert.assertEquals(3, input.size());
        Assert.assertTrue(input.contains(null, RDF.TYPE, OWL.ONTOLOGY));
        Assert.assertTrue(input.contains(null, OWL.VERSIONIRI, null));
        Assert.assertEquals(2, input.filter(null, RDF.TYPE, OWL.ONTOLOGY).size());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToHandler(java.util.Collection, org.openrdf.rio.RDFHandler)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public final void testOntologyIDsToHandlerNoVersionIRI() throws Exception
    {
        final Model input = new LinkedHashModel();
        
        OntologyUtils.ontologyIDsToHandler(
                Arrays.asList(new InferredOWLOntologyID(IRI.create("urn:test:ontology:iri:abc"), null, null)),
                new StatementCollector(input));
        
        Assert.assertEquals(1, input.size());
        Assert.assertTrue(input.contains(null, RDF.TYPE, OWL.ONTOLOGY));
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToHandler(java.util.Collection, org.openrdf.rio.RDFHandler)}
     * .
     * 
     * @throws Exception
     */
    @Test
    public final void testOntologyIDsToHandlerWithInferredIRI() throws Exception
    {
        final Model input = new LinkedHashModel();
        
        OntologyUtils.ontologyIDsToHandler(Arrays.asList(new InferredOWLOntologyID(IRI
                .create("urn:test:ontology:iri:abc"), IRI.create("urn:test:ontology:iri:abc:version:1"), IRI
                .create("urn:inferred:test:ontology:iri:abc:version:1:1"))), new StatementCollector(input));
        
        Assert.assertEquals(5, input.size());
        Assert.assertTrue(input.contains(null, RDF.TYPE, OWL.ONTOLOGY));
        Assert.assertTrue(input.contains(null, OWL.VERSIONIRI, null));
        Assert.assertTrue(input.contains(null, PoddRdfConstants.PODD_BASE_INFERRED_VERSION, null));
        Assert.assertEquals(3, input.filter(null, RDF.TYPE, OWL.ONTOLOGY).size());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToModel(java.util.Collection, org.openrdf.model.Model)}
     * .
     */
    @Test
    public final void testOntologyIDsToModelAnonymousOntology() throws Exception
    {
        final Model input = new LinkedHashModel();
        
        final Model ontologyIDsToModel =
                OntologyUtils
                        .ontologyIDsToModel(Arrays.asList(new InferredOWLOntologyID((IRI)null, null, null)), input);
        
        Assert.assertNotNull(ontologyIDsToModel);
        Assert.assertEquals(input, ontologyIDsToModel);
        Assert.assertTrue(ontologyIDsToModel.isEmpty());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToModel(java.util.Collection, org.openrdf.model.Model)}
     * .
     */
    @Test
    public final void testOntologyIDsToModelEmptyNotNull()
    {
        final Model input = new LinkedHashModel();
        
        final Model ontologyIDsToModel =
                OntologyUtils.ontologyIDsToModel(Collections.<InferredOWLOntologyID> emptyList(), input);
        
        Assert.assertNotNull(ontologyIDsToModel);
        Assert.assertEquals(input, ontologyIDsToModel);
        Assert.assertTrue(ontologyIDsToModel.isEmpty());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToModel(java.util.Collection, org.openrdf.model.Model)}
     * .
     */
    @Test
    public final void testOntologyIDsToModelEmptyNull()
    {
        final Model ontologyIDsToModel =
                OntologyUtils.ontologyIDsToModel(Collections.<InferredOWLOntologyID> emptyList(), (Model)null);
        
        Assert.assertNotNull(ontologyIDsToModel);
        Assert.assertTrue(ontologyIDsToModel.isEmpty());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToModel(java.util.Collection, org.openrdf.model.Model)}
     * .
     */
    @Test
    public final void testOntologyIDsToModelNoInferredIRI()
    {
        final Model input = new LinkedHashModel();
        
        final Model ontologyIDsToModel =
                OntologyUtils.ontologyIDsToModel(
                        Arrays.asList(new InferredOWLOntologyID(IRI.create("urn:test:ontology:iri:abc"), IRI
                                .create("urn:test:ontology:iri:abc:version:1"), null)), input);
        
        Assert.assertNotNull(ontologyIDsToModel);
        Assert.assertEquals(input, ontologyIDsToModel);
        Assert.assertEquals(3, ontologyIDsToModel.size());
        Assert.assertTrue(ontologyIDsToModel.contains(null, RDF.TYPE, OWL.ONTOLOGY));
        Assert.assertTrue(ontologyIDsToModel.contains(null, OWL.VERSIONIRI, null));
        Assert.assertEquals(2, ontologyIDsToModel.filter(null, RDF.TYPE, OWL.ONTOLOGY).size());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToModel(java.util.Collection, org.openrdf.model.Model)}
     * .
     */
    @Test
    public final void testOntologyIDsToModelNoVersionIRI()
    {
        final Model input = new LinkedHashModel();
        
        final Model ontologyIDsToModel =
                OntologyUtils.ontologyIDsToModel(
                        Arrays.asList(new InferredOWLOntologyID(IRI.create("urn:test:ontology:iri:abc"), null, null)),
                        input);
        
        Assert.assertNotNull(ontologyIDsToModel);
        Assert.assertEquals(input, ontologyIDsToModel);
        Assert.assertEquals(1, ontologyIDsToModel.size());
        Assert.assertTrue(ontologyIDsToModel.contains(null, RDF.TYPE, OWL.ONTOLOGY));
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToModel(java.util.Collection, org.openrdf.model.Model)}
     * .
     */
    @Test
    public final void testOntologyIDsToModelWithInferredIRI()
    {
        final Model input = new LinkedHashModel();
        
        final Model ontologyIDsToModel =
                OntologyUtils.ontologyIDsToModel(Arrays.asList(new InferredOWLOntologyID(IRI
                        .create("urn:test:ontology:iri:abc"), IRI.create("urn:test:ontology:iri:abc:version:1"), IRI
                        .create("urn:inferred:test:ontology:iri:abc:version:1:1"))), input);
        
        Assert.assertNotNull(ontologyIDsToModel);
        Assert.assertEquals(input, ontologyIDsToModel);
        Assert.assertEquals(5, ontologyIDsToModel.size());
        Assert.assertTrue(ontologyIDsToModel.contains(null, RDF.TYPE, OWL.ONTOLOGY));
        Assert.assertTrue(ontologyIDsToModel.contains(null, OWL.VERSIONIRI, null));
        Assert.assertTrue(ontologyIDsToModel.contains(null, PoddRdfConstants.PODD_BASE_INFERRED_VERSION, null));
        Assert.assertEquals(3, ontologyIDsToModel.filter(null, RDF.TYPE, OWL.ONTOLOGY).size());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#stringToOntologyID(String, RDFFormat)}.
     */
    @Test
    public final void testStringToOntologyIDsInRdfXml() throws Exception
    {
        final InputStream resourceStream = this.getClass().getResourceAsStream("/test/test-ontologyid-2.rdf");
        final String rdfString = IOUtils.toString(resourceStream);
        
        final Collection<InferredOWLOntologyID> ontologyIDs =
                OntologyUtils.stringToOntologyID(rdfString, RDFFormat.RDFXML);
        
        Assert.assertEquals("Did not find any Ontology IDs", 1, ontologyIDs.size());
        Assert.assertEquals("Version IRI did not match",
                "http://example.org/purl/c54fbaa5-0767-4f78-88b1-2509ff428f60/artifact:1:version:1", ontologyIDs
                        .iterator().next().getVersionIRI().toString());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#stringToOntologyID(String, RDFFormat)}.
     */
    @Test
    public final void testStringToOntologyIDsInTurtle() throws Exception
    {
        final InputStream resourceStream = this.getClass().getResourceAsStream("/test/test-ontologyid-1.ttl");
        final String rdfString = IOUtils.toString(resourceStream);
        
        final Collection<InferredOWLOntologyID> ontologyIDs =
                OntologyUtils.stringToOntologyID(rdfString, RDFFormat.TURTLE);
        
        Assert.assertEquals("Did not find any Ontology IDs", 1, ontologyIDs.size());
        Assert.assertEquals("Version IRI did not match",
                "http://example.org/purl/91bb7bff-acd6-4b2e-abf7-ce74d3d91061/artifact:1:version:1", ontologyIDs
                        .iterator().next().getVersionIRI().toString());
    }
    
}
