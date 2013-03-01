/**
 * 
 */
package com.github.podd.utils.test;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;

/**
 * Test for OntologyUtils class that translates between RDF and InferredOWLOntologyID instances.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class OntologyUtilsTest
{
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#modelToOntologyIDs(org.openrdf.model.Model)}.
     */
    @Ignore
    @Test
    public final void testModelToOntologyIDs()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToHandler(java.util.Collection, org.openrdf.rio.RDFHandler)}
     * .
     */
    @Ignore
    @Test
    public final void testOntologyIDsToHandler()
    {
        Assert.fail("Not yet implemented"); // TODO
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
    public final void testOntologyIDsToModelAnonymousOntology()
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
    }
    
}
