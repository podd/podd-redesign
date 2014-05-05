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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.exception.SchemaManifestException;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PODD;

/**
 * Test for OntologyUtils class that translates between RDF and InferredOWLOntologyID instances.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class OntologyUtilsTest
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ValueFactory vf = PODD.VF;
    
    private ConcurrentMap<URI, Set<URI>> importsMap;
    
    private void assertA1B2C3(final ConcurrentMap<URI, Set<URI>> nextImportsMap)
    {
        Assert.assertEquals(5, nextImportsMap.size());
        
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testVersionUriA1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testVersionUriB1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testVersionUriB2));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testVersionUriC1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testVersionUriC3));
        
        Assert.assertEquals(0, nextImportsMap.get(OntologyConstant.testVersionUriA1).size());
        Assert.assertEquals(1, nextImportsMap.get(OntologyConstant.testVersionUriB1).size());
        Assert.assertEquals(1, nextImportsMap.get(OntologyConstant.testVersionUriB2).size());
        Assert.assertEquals(2, nextImportsMap.get(OntologyConstant.testVersionUriC1).size());
        Assert.assertEquals(2, nextImportsMap.get(OntologyConstant.testVersionUriC3).size());
        
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testVersionUriB1).contains(
                OntologyConstant.testVersionUriA1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testVersionUriB2).contains(
                OntologyConstant.testVersionUriA1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testVersionUriC1).contains(
                OntologyConstant.testVersionUriA1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testVersionUriC1).contains(
                OntologyConstant.testVersionUriB1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testVersionUriC3).contains(
                OntologyConstant.testVersionUriA1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testVersionUriC3).contains(
                OntologyConstant.testVersionUriB2));
    }
    
    private void assertRealisticImportsMapV2(final ConcurrentMap<URI, Set<URI>> nextImportsMap)
    {
        Assert.assertEquals(12, nextImportsMap.size());
        
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddDcUriV1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddDcUriV2));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddFoafUriV1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddFoafUriV2));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddUserUriV1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddUserUriV2));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddBaseUriV1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddBaseUriV2));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddScienceUriV1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddScienceUriV2));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddPlantUriV1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddPlantUriV2));
        
        Assert.assertEquals(0, nextImportsMap.get(OntologyConstant.testPoddDcUriV1).size());
        Assert.assertEquals(0, nextImportsMap.get(OntologyConstant.testPoddDcUriV2).size());
        Assert.assertEquals(1, nextImportsMap.get(OntologyConstant.testPoddFoafUriV1).size());
        Assert.assertEquals(1, nextImportsMap.get(OntologyConstant.testPoddFoafUriV2).size());
        Assert.assertEquals(2, nextImportsMap.get(OntologyConstant.testPoddUserUriV1).size());
        Assert.assertEquals(2, nextImportsMap.get(OntologyConstant.testPoddUserUriV2).size());
        Assert.assertEquals(3, nextImportsMap.get(OntologyConstant.testPoddBaseUriV1).size());
        Assert.assertEquals(3, nextImportsMap.get(OntologyConstant.testPoddBaseUriV2).size());
        Assert.assertEquals(4, nextImportsMap.get(OntologyConstant.testPoddScienceUriV1).size());
        Assert.assertEquals(4, nextImportsMap.get(OntologyConstant.testPoddScienceUriV2).size());
        Assert.assertEquals(5, nextImportsMap.get(OntologyConstant.testPoddPlantUriV1).size());
        Assert.assertEquals(5, nextImportsMap.get(OntologyConstant.testPoddPlantUriV2).size());
        
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddFoafUriV1).contains(
                OntologyConstant.testPoddDcUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddFoafUriV2).contains(
                OntologyConstant.testPoddDcUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddUserUriV1).contains(
                OntologyConstant.testPoddDcUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddUserUriV2).contains(
                OntologyConstant.testPoddDcUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddUserUriV1).contains(
                OntologyConstant.testPoddFoafUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddUserUriV2).contains(
                OntologyConstant.testPoddFoafUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV1).contains(
                OntologyConstant.testPoddDcUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV2).contains(
                OntologyConstant.testPoddDcUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV1).contains(
                OntologyConstant.testPoddFoafUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV2).contains(
                OntologyConstant.testPoddFoafUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV1).contains(
                OntologyConstant.testPoddUserUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV2).contains(
                OntologyConstant.testPoddUserUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV1).contains(
                OntologyConstant.testPoddDcUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV2).contains(
                OntologyConstant.testPoddDcUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV1).contains(
                OntologyConstant.testPoddFoafUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV2).contains(
                OntologyConstant.testPoddFoafUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV1).contains(
                OntologyConstant.testPoddUserUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV2).contains(
                OntologyConstant.testPoddUserUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV1).contains(
                OntologyConstant.testPoddBaseUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV2).contains(
                OntologyConstant.testPoddBaseUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV1).contains(
                OntologyConstant.testPoddDcUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV2).contains(
                OntologyConstant.testPoddDcUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV1).contains(
                OntologyConstant.testPoddFoafUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV2).contains(
                OntologyConstant.testPoddFoafUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV1).contains(
                OntologyConstant.testPoddUserUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV2).contains(
                OntologyConstant.testPoddUserUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV1).contains(
                OntologyConstant.testPoddBaseUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV2).contains(
                OntologyConstant.testPoddBaseUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV1).contains(
                OntologyConstant.testPoddScienceUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV2).contains(
                OntologyConstant.testPoddScienceUriV2));
    }
    
    private void assertRealisticImportsMapV3(final ConcurrentMap<URI, Set<URI>> nextImportsMap)
    {
        Assert.assertEquals(22, nextImportsMap.size());
        
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddDcUriV1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddDcUriV2));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddDcUriV3));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddFoafUriV1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddFoafUriV2));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddFoafUriV3));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddUserUriV1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddUserUriV2));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddUserUriV3));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddBaseUriV1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddBaseUriV2));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddBaseUriV3));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddScienceUriV1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddScienceUriV2));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddScienceUriV3));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddPlantUriV1));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddPlantUriV2));
        Assert.assertTrue(nextImportsMap.containsKey(OntologyConstant.testPoddPlantUriV3));
        
        Assert.assertEquals(0, nextImportsMap.get(OntologyConstant.testPoddDcUriV1).size());
        Assert.assertEquals(0, nextImportsMap.get(OntologyConstant.testPoddDcUriV2).size());
        Assert.assertEquals(0, nextImportsMap.get(OntologyConstant.testPoddDcUriV3).size());
        Assert.assertEquals(1, nextImportsMap.get(OntologyConstant.testPoddFoafUriV1).size());
        Assert.assertEquals(1, nextImportsMap.get(OntologyConstant.testPoddFoafUriV2).size());
        Assert.assertEquals(1, nextImportsMap.get(OntologyConstant.testPoddFoafUriV3).size());
        Assert.assertEquals(2, nextImportsMap.get(OntologyConstant.testPoddUserUriV1).size());
        Assert.assertEquals(2, nextImportsMap.get(OntologyConstant.testPoddUserUriV2).size());
        Assert.assertEquals(2, nextImportsMap.get(OntologyConstant.testPoddUserUriV3).size());
        Assert.assertEquals(3, nextImportsMap.get(OntologyConstant.testPoddBaseUriV1).size());
        Assert.assertEquals(3, nextImportsMap.get(OntologyConstant.testPoddBaseUriV2).size());
        Assert.assertEquals(3, nextImportsMap.get(OntologyConstant.testPoddBaseUriV3).size());
        Assert.assertEquals(4, nextImportsMap.get(OntologyConstant.testPoddScienceUriV1).size());
        Assert.assertEquals(6, nextImportsMap.get(OntologyConstant.testPoddScienceUriV2).size());
        Assert.assertEquals(6, nextImportsMap.get(OntologyConstant.testPoddScienceUriV3).size());
        Assert.assertEquals(5, nextImportsMap.get(OntologyConstant.testPoddPlantUriV1).size());
        Assert.assertEquals(7, nextImportsMap.get(OntologyConstant.testPoddPlantUriV2).size());
        Assert.assertEquals(7, nextImportsMap.get(OntologyConstant.testPoddPlantUriV3).size());
        
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddFoafUriV1).contains(
                OntologyConstant.testPoddDcUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddFoafUriV2).contains(
                OntologyConstant.testPoddDcUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddFoafUriV3).contains(
                OntologyConstant.testPoddDcUriV3));
        
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddUserUriV1).contains(
                OntologyConstant.testPoddDcUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddUserUriV2).contains(
                OntologyConstant.testPoddDcUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddUserUriV3).contains(
                OntologyConstant.testPoddDcUriV3));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddUserUriV1).contains(
                OntologyConstant.testPoddFoafUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddUserUriV2).contains(
                OntologyConstant.testPoddFoafUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddUserUriV3).contains(
                OntologyConstant.testPoddFoafUriV3));
        
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV1).contains(
                OntologyConstant.testPoddDcUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV2).contains(
                OntologyConstant.testPoddDcUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV3).contains(
                OntologyConstant.testPoddDcUriV3));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV1).contains(
                OntologyConstant.testPoddFoafUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV2).contains(
                OntologyConstant.testPoddFoafUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV3).contains(
                OntologyConstant.testPoddFoafUriV3));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV1).contains(
                OntologyConstant.testPoddUserUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV2).contains(
                OntologyConstant.testPoddUserUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddBaseUriV3).contains(
                OntologyConstant.testPoddUserUriV3));
        
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV1).contains(
                OntologyConstant.testPoddDcUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV2).contains(
                OntologyConstant.testPoddDcUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV3).contains(
                OntologyConstant.testPoddDcUriV3));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV1).contains(
                OntologyConstant.testPoddFoafUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV2).contains(
                OntologyConstant.testPoddFoafUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV3).contains(
                OntologyConstant.testPoddFoafUriV3));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV1).contains(
                OntologyConstant.testPoddUserUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV2).contains(
                OntologyConstant.testPoddUserUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV3).contains(
                OntologyConstant.testPoddUserUriV3));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV1).contains(
                OntologyConstant.testPoddBaseUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV2).contains(
                OntologyConstant.testPoddBaseUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddScienceUriV3).contains(
                OntologyConstant.testPoddBaseUriV3));
        
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV1).contains(
                OntologyConstant.testPoddDcUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV2).contains(
                OntologyConstant.testPoddDcUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV3).contains(
                OntologyConstant.testPoddDcUriV3));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV1).contains(
                OntologyConstant.testPoddFoafUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV2).contains(
                OntologyConstant.testPoddFoafUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV3).contains(
                OntologyConstant.testPoddFoafUriV3));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV1).contains(
                OntologyConstant.testPoddUserUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV2).contains(
                OntologyConstant.testPoddUserUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV3).contains(
                OntologyConstant.testPoddUserUriV3));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV1).contains(
                OntologyConstant.testPoddBaseUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV2).contains(
                OntologyConstant.testPoddBaseUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV3).contains(
                OntologyConstant.testPoddBaseUriV3));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV1).contains(
                OntologyConstant.testPoddScienceUriV1));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV2).contains(
                OntologyConstant.testPoddScienceUriV2));
        Assert.assertTrue(nextImportsMap.get(OntologyConstant.testPoddPlantUriV3).contains(
                OntologyConstant.testPoddScienceUriV3));
    }
    
    @Before
    public void setUp() throws Exception
    {
        this.importsMap = new ConcurrentHashMap<>();
    }
    
    @Test
    public final void testGetArtifactImportsNoImports() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        
        final List<OWLOntologyID> imports = OntologyUtils.artifactImports(OntologyConstant.testOntologyID, model);
        
        Assert.assertEquals(0, imports.size());
        Assert.assertTrue(imports.isEmpty());
    }
    
    @Test
    public final void testGetArtifactImportsNonExistent() throws Exception
    {
        final Model model = new LinkedHashModel();
        try
        {
            OntologyUtils.artifactImports(OntologyConstant.testOntologyID, model);
            Assert.fail("Did not find expected exception");
        }
        catch(final SchemaManifestException e)
        {
            
        }
    }
    
    @Test
    public final void testGetArtifactImportsNullArtifact() throws Exception
    {
        final Model model = new LinkedHashModel();
        try
        {
            OntologyUtils.artifactImports(null, model);
            Assert.fail("Did not find expected exception");
        }
        catch(final NullPointerException e)
        {
            
        }
    }
    
    @Test
    public final void testGetArtifactImportsNullModel() throws Exception
    {
        try
        {
            OntologyUtils.artifactImports(OntologyConstant.testOntologyID, null);
            Assert.fail("Did not find expected exception");
        }
        catch(final NullPointerException e)
        {
            
        }
    }
    
    @Test
    public final void testGetArtifactImportsOneImport() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        model.add(OntologyConstant.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri1, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testOntologyUri1, OWL.IMPORTS, OntologyConstant.testImportOntologyUri1);
        
        // DebugUtils.printContents(model);
        
        final List<OWLOntologyID> imports = OntologyUtils.artifactImports(OntologyConstant.testOntologyID, model);
        
        Assert.assertEquals(1, imports.size());
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID1));
    }
    
    @Test
    public final void testGetArtifactImportsOneImportNonTransitiveSingle() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        model.add(OntologyConstant.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri1, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri2, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri2, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri2, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri2);
        model.add(OntologyConstant.testOntologyUri1, OWL.IMPORTS, OntologyConstant.testImportOntologyUri1);
        
        final List<OWLOntologyID> imports = OntologyUtils.artifactImports(OntologyConstant.testOntologyID, model);
        
        Assert.assertEquals(1, imports.size());
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID1));
    }
    
    @Test
    public final void testGetArtifactImportsOneImportTransitiveDouble() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        model.add(OntologyConstant.testOntologyUri1, OWL.IMPORTS, OntologyConstant.testImportOntologyUri1);
        model.add(OntologyConstant.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri1, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        
        model.add(OntologyConstant.testImportOntologyUri2, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri2, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri2);
        model.add(OntologyConstant.testImportVersionUri2, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri1, OWL.IMPORTS, OntologyConstant.testImportVersionUri2);
        
        model.add(OntologyConstant.testImportOntologyUri3, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri3, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri3);
        model.add(OntologyConstant.testImportVersionUri3, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri2, OWL.IMPORTS, OntologyConstant.testImportVersionUri3);
        
        final List<OWLOntologyID> imports = OntologyUtils.artifactImports(OntologyConstant.testOntologyID, model);
        
        Assert.assertEquals(3, imports.size());
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID1));
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID2));
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID3));
        Assert.assertEquals(OntologyConstant.testImportOntologyID3, imports.get(0));
        Assert.assertEquals(OntologyConstant.testImportOntologyID2, imports.get(1));
        Assert.assertEquals(OntologyConstant.testImportOntologyID1, imports.get(2));
    }
    
    @Test
    public final void testGetArtifactImportsOneImportTransitiveDoubleDouble() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        model.add(OntologyConstant.testOntologyUri1, OWL.IMPORTS, OntologyConstant.testImportOntologyUri1);
        model.add(OntologyConstant.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri1, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri1, OWL.IMPORTS, OntologyConstant.testImportVersionUri2);
        
        model.add(OntologyConstant.testImportOntologyUri2, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri2, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri2);
        model.add(OntologyConstant.testImportVersionUri2, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri2, OWL.IMPORTS, OntologyConstant.testImportVersionUri3);
        model.add(OntologyConstant.testImportVersionUri2, OWL.IMPORTS, OntologyConstant.testImportVersionUri4);
        
        model.add(OntologyConstant.testImportOntologyUri3, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri3, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri3);
        model.add(OntologyConstant.testImportVersionUri3, RDF.TYPE, OWL.ONTOLOGY);
        
        model.add(OntologyConstant.testImportOntologyUri4, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri4, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri4);
        model.add(OntologyConstant.testImportVersionUri4, RDF.TYPE, OWL.ONTOLOGY);
        
        final List<OWLOntologyID> imports = OntologyUtils.artifactImports(OntologyConstant.testOntologyID, model);
        
        Assert.assertEquals(4, imports.size());
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID1));
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID2));
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID3));
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID4));
        Assert.assertTrue(imports.indexOf(OntologyConstant.testImportOntologyID2) > imports
                .indexOf(OntologyConstant.testImportOntologyID4));
        Assert.assertTrue(imports.indexOf(OntologyConstant.testImportOntologyID1) > imports
                .indexOf(OntologyConstant.testImportOntologyID2));
        Assert.assertTrue(imports.indexOf(OntologyConstant.testImportOntologyID2) > imports
                .indexOf(OntologyConstant.testImportOntologyID3));
        // NOTE: First two positions are not consistent, so only testing the last two
        Assert.assertEquals(OntologyConstant.testImportOntologyID2, imports.get(2));
        Assert.assertEquals(OntologyConstant.testImportOntologyID1, imports.get(3));
    }
    
    @Test
    public final void testGetArtifactImportsOneImportTransitiveSingle() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        model.add(OntologyConstant.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri1, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri1, OWL.IMPORTS, OntologyConstant.testImportVersionUri2);
        model.add(OntologyConstant.testImportOntologyUri2, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri2, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri2, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri2);
        model.add(OntologyConstant.testOntologyUri1, OWL.IMPORTS, OntologyConstant.testImportOntologyUri1);
        
        final List<OWLOntologyID> imports = OntologyUtils.artifactImports(OntologyConstant.testOntologyID, model);
        
        Assert.assertEquals(2, imports.size());
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID1));
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID2));
        Assert.assertEquals(OntologyConstant.testImportOntologyID2, imports.get(0));
        Assert.assertEquals(OntologyConstant.testImportOntologyID1, imports.get(1));
    }
    
    @Test
    public final void testGetArtifactImportsOneImportVersion() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        model.add(OntologyConstant.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri1, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testVersionUri1, OWL.IMPORTS, OntologyConstant.testImportOntologyUri1);
        
        final List<OWLOntologyID> imports = OntologyUtils.artifactImports(OntologyConstant.testOntologyID, model);
        
        Assert.assertEquals(1, imports.size());
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID1));
    }
    
    @Test
    public final void testGetArtifactImportsOneImportVersionTransitiveDoubleDouble() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        model.add(OntologyConstant.testVersionUri1, OWL.IMPORTS, OntologyConstant.testImportOntologyUri1);
        model.add(OntologyConstant.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri1, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        
        model.add(OntologyConstant.testImportOntologyUri2, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri2, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri2);
        model.add(OntologyConstant.testImportVersionUri2, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri1, OWL.IMPORTS, OntologyConstant.testImportVersionUri2);
        
        model.add(OntologyConstant.testImportOntologyUri3, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri3, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri3);
        model.add(OntologyConstant.testImportVersionUri3, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri2, OWL.IMPORTS, OntologyConstant.testImportVersionUri3);
        
        model.add(OntologyConstant.testImportOntologyUri4, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri4, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri4);
        model.add(OntologyConstant.testImportVersionUri4, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri2, OWL.IMPORTS, OntologyConstant.testImportVersionUri4);
        
        final List<OWLOntologyID> imports = OntologyUtils.artifactImports(OntologyConstant.testOntologyID, model);
        
        Assert.assertEquals(4, imports.size());
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID1));
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID2));
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID3));
        Assert.assertTrue(imports.contains(OntologyConstant.testImportOntologyID4));
        // NOTE: First two positions are not consistent, so only testing the last two
        Assert.assertEquals(OntologyConstant.testImportOntologyID2, imports.get(2));
        Assert.assertEquals(OntologyConstant.testImportOntologyID1, imports.get(3));
    }
    
    @Test
    public void testGetArtifactImportsRealistic() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/artifacts/artifact-imports-test.nq"), "",
                        RDFFormat.NQUADS);
        
        model.addAll(Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                RDFFormat.TURTLE));
        
        // DebugUtils.printContents(model);
        
        final List<OWLOntologyID> imports = OntologyUtils.artifactImports(OntologyConstant.testOntologyID, model);
        
        // this.log.info("Imports: {}", imports);
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddScienceV1));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddDcV1));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddFoafV1));
        Assert.assertEquals(5, imports.size());
    }
    
    @Test
    public void testGetArtifactImportsRealisticInra() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/artifacts/artifact-imports-test.nq"), "",
                        RDFFormat.NQUADS);
        
        model.addAll(Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest-inra.ttl"), "",
                RDFFormat.TURTLE));
        
        // DebugUtils.printContents(model);
        
        final List<OWLOntologyID> imports = OntologyUtils.artifactImports(OntologyConstant.testOntologyID, model);
        
        // this.log.info("Imports: {}", imports);
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddScienceV1));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddDcV1));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddFoafV1));
        Assert.assertEquals(5, imports.size());
    }
    
    @Test
    public void testGetArtifactImportsRealisticInraV2() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/artifacts/artifact-imports-test-V2.nq"), "",
                        RDFFormat.NQUADS);
        
        model.addAll(Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest-inra.ttl"), "",
                RDFFormat.TURTLE));
        
        // DebugUtils.printContents(model);
        
        final List<OWLOntologyID> imports = OntologyUtils.artifactImports(OntologyConstant.testOntologyID, model);
        
        // this.log.info("Imports: {}", imports);
        Assert.assertTrue(imports.contains(OntologyConstant.testMisteaEventV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testMisteaObjectV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddScienceV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddDcV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddFoafV2));
        Assert.assertEquals(7, imports.size());
    }
    
    @Test
    public void testGetArtifactImportsRealisticInraVersion2() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/artifacts/artifact-imports-test-inra.nq"), "",
                        RDFFormat.NQUADS);
        
        model.addAll(Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest-inra.ttl"), "",
                RDFFormat.TURTLE));
        
        // DebugUtils.printContents(model);
        
        final List<OWLOntologyID> imports = OntologyUtils.artifactImports(OntologyConstant.testOntologyID, model);
        
        // this.log.info("Imports: {}", imports);
        Assert.assertTrue(imports.contains(OntologyConstant.testMisteaEventV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testMisteaObjectV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddScienceV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddDcV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddFoafV2));
        Assert.assertEquals(7, imports.size());
    }
    
    @Test
    public void testGetArtifactImportsRealisticNewProjectVersion2() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/artifacts/artifact-new-project-test.nq"), "",
                        RDFFormat.NQUADS);
        
        model.addAll(Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest-inra.ttl"), "",
                RDFFormat.TURTLE));
        
        // DebugUtils.printContents(model);
        
        final List<OWLOntologyID> imports = OntologyUtils.artifactImports(OntologyConstant.testOntologyID, model);
        
        // this.log.info("Imports: {}", imports);
        Assert.assertTrue(imports.contains(OntologyConstant.testMisteaEventV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testMisteaObjectV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddScienceV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddDcV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(imports.contains(OntologyConstant.testPoddPlantV2));
        Assert.assertEquals(8, imports.size());
    }
    
    @Test
    public void testImportsOrderFourLevels() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        model.add(OntologyConstant.testVersionUri1, OWL.IMPORTS, OntologyConstant.testImportOntologyUri1);
        model.add(OntologyConstant.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri1, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        
        model.add(OntologyConstant.testImportOntologyUri2, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri2, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri2);
        model.add(OntologyConstant.testImportVersionUri2, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri1, OWL.IMPORTS, OntologyConstant.testImportVersionUri2);
        
        model.add(OntologyConstant.testImportOntologyUri3, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri3, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri3);
        model.add(OntologyConstant.testImportVersionUri3, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri2, OWL.IMPORTS, OntologyConstant.testImportVersionUri3);
        
        model.add(OntologyConstant.testImportOntologyUri4, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri4, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri4);
        model.add(OntologyConstant.testImportVersionUri4, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri3, OWL.IMPORTS, OntologyConstant.testImportVersionUri4);
        
        final Set<URI> schemaOntologyUris = new LinkedHashSet<URI>();
        final Set<URI> schemaVersionUris = new LinkedHashSet<URI>();
        
        schemaOntologyUris.add(OntologyConstant.testOntologyUri1);
        schemaOntologyUris.add(OntologyConstant.testImportOntologyUri1);
        schemaOntologyUris.add(OntologyConstant.testImportOntologyUri2);
        schemaOntologyUris.add(OntologyConstant.testImportOntologyUri3);
        schemaOntologyUris.add(OntologyConstant.testImportOntologyUri4);
        
        schemaVersionUris.add(OntologyConstant.testVersionUri1);
        schemaVersionUris.add(OntologyConstant.testImportVersionUri1);
        schemaVersionUris.add(OntologyConstant.testImportVersionUri2);
        schemaVersionUris.add(OntologyConstant.testImportVersionUri3);
        schemaVersionUris.add(OntologyConstant.testImportVersionUri4);
        
        final ConcurrentMap<URI, Set<URI>> importsMap = new ConcurrentHashMap<URI, Set<URI>>();
        // Expected output solution from importsMap after calling orderImports
        // importsMap.put(testVersionUri1,
        // Collections.singleton(OntologyConstant.testImportVersionUri1));
        // importsMap.put(testImportVersionUri1,
        // Collections.singleton(OntologyConstant.testImportVersionUri2));
        // importsMap.put(testImportVersionUri2,
        // Collections.singleton(OntologyConstant.testImportVersionUri3));
        // importsMap.put(testImportVersionUri3, new
        // HashSet<URI>(Arrays.asList(OntologyConstant.testImportVersionUri4)));
        // importsMap.put(testImportVersionUri4, new HashSet<URI>());
        
        final List<URI> orderImports =
                OntologyUtils.orderImports(model, schemaOntologyUris, schemaVersionUris, importsMap, false);
        
        Assert.assertEquals(5, orderImports.size());
        Assert.assertEquals(OntologyConstant.testImportVersionUri4, orderImports.get(0));
        Assert.assertEquals(OntologyConstant.testImportVersionUri3, orderImports.get(1));
        Assert.assertEquals(OntologyConstant.testImportVersionUri2, orderImports.get(2));
        Assert.assertEquals(OntologyConstant.testImportVersionUri1, orderImports.get(3));
        Assert.assertEquals(OntologyConstant.testVersionUri1, orderImports.get(4));
        
        Assert.assertEquals(5, importsMap.size());
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testImportVersionUri4));
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testImportVersionUri3));
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testImportVersionUri2));
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testImportVersionUri1));
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testVersionUri1));
        
        final Set<URI> imports4 = importsMap.get(OntologyConstant.testImportVersionUri4);
        Assert.assertNotNull(imports4);
        Assert.assertEquals(0, imports4.size());
        
        final Set<URI> imports3 = importsMap.get(OntologyConstant.testImportVersionUri3);
        Assert.assertNotNull(imports3);
        Assert.assertEquals(1, imports3.size());
        Assert.assertTrue(imports3.contains(OntologyConstant.testImportVersionUri4));
        
        final Set<URI> imports2 = importsMap.get(OntologyConstant.testImportVersionUri2);
        Assert.assertNotNull(imports2);
        Assert.assertEquals(2, imports2.size());
        Assert.assertTrue(imports2.contains(OntologyConstant.testImportVersionUri3));
        Assert.assertTrue(imports2.contains(OntologyConstant.testImportVersionUri4));
        
        final Set<URI> imports1 = importsMap.get(OntologyConstant.testImportVersionUri1);
        Assert.assertNotNull(imports1);
        Assert.assertEquals(3, imports1.size());
        Assert.assertTrue(imports1.contains(OntologyConstant.testImportVersionUri2));
        Assert.assertTrue(imports1.contains(OntologyConstant.testImportVersionUri3));
        Assert.assertTrue(imports1.contains(OntologyConstant.testImportVersionUri4));
        
        final Set<URI> importsRoot = importsMap.get(OntologyConstant.testVersionUri1);
        Assert.assertNotNull(importsRoot);
        Assert.assertEquals(4, importsRoot.size());
        Assert.assertTrue(importsRoot.contains(OntologyConstant.testImportVersionUri1));
        Assert.assertTrue(importsRoot.contains(OntologyConstant.testImportVersionUri2));
        Assert.assertTrue(importsRoot.contains(OntologyConstant.testImportVersionUri3));
        Assert.assertTrue(importsRoot.contains(OntologyConstant.testImportVersionUri4));
    }
    
    @Test
    public void testImportsOrderFourLevelsOutOfOrder() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        model.add(OntologyConstant.testVersionUri1, OWL.IMPORTS, OntologyConstant.testImportOntologyUri1);
        model.add(OntologyConstant.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri1, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        
        model.add(OntologyConstant.testImportOntologyUri2, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri2, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri2);
        model.add(OntologyConstant.testImportVersionUri2, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri1, OWL.IMPORTS, OntologyConstant.testImportVersionUri2);
        
        model.add(OntologyConstant.testImportOntologyUri3, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri3, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri3);
        model.add(OntologyConstant.testImportVersionUri3, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri2, OWL.IMPORTS, OntologyConstant.testImportVersionUri3);
        
        model.add(OntologyConstant.testImportOntologyUri4, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri4, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri4);
        model.add(OntologyConstant.testImportVersionUri4, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportVersionUri3, OWL.IMPORTS, OntologyConstant.testImportVersionUri4);
        
        final Set<URI> schemaOntologyUris = new LinkedHashSet<URI>();
        final Set<URI> schemaVersionUris = new LinkedHashSet<URI>();
        
        schemaOntologyUris.add(OntologyConstant.testImportOntologyUri2);
        schemaOntologyUris.add(OntologyConstant.testImportOntologyUri4);
        schemaOntologyUris.add(OntologyConstant.testOntologyUri1);
        schemaOntologyUris.add(OntologyConstant.testImportOntologyUri3);
        schemaOntologyUris.add(OntologyConstant.testImportOntologyUri1);
        
        schemaVersionUris.add(OntologyConstant.testImportVersionUri2);
        schemaVersionUris.add(OntologyConstant.testImportVersionUri4);
        schemaVersionUris.add(OntologyConstant.testVersionUri1);
        schemaVersionUris.add(OntologyConstant.testImportVersionUri3);
        schemaVersionUris.add(OntologyConstant.testImportVersionUri1);
        
        final ConcurrentMap<URI, Set<URI>> importsMap = new ConcurrentHashMap<URI, Set<URI>>();
        
        final List<URI> orderImports =
                OntologyUtils.orderImports(model, schemaOntologyUris, schemaVersionUris, importsMap, false);
        
        Assert.assertEquals(5, orderImports.size());
        Assert.assertEquals(OntologyConstant.testImportVersionUri4, orderImports.get(0));
        Assert.assertEquals(OntologyConstant.testImportVersionUri3, orderImports.get(1));
        Assert.assertEquals(OntologyConstant.testImportVersionUri2, orderImports.get(2));
        Assert.assertEquals(OntologyConstant.testImportVersionUri1, orderImports.get(3));
        Assert.assertEquals(OntologyConstant.testVersionUri1, orderImports.get(4));
        
        Assert.assertEquals(5, importsMap.size());
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testImportVersionUri4));
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testImportVersionUri3));
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testImportVersionUri2));
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testImportVersionUri1));
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testVersionUri1));
        
        final Set<URI> imports4 = importsMap.get(OntologyConstant.testImportVersionUri4);
        Assert.assertNotNull(imports4);
        Assert.assertEquals(0, imports4.size());
        
        final Set<URI> imports3 = importsMap.get(OntologyConstant.testImportVersionUri3);
        Assert.assertNotNull(imports3);
        Assert.assertEquals(1, imports3.size());
        Assert.assertTrue(imports3.contains(OntologyConstant.testImportVersionUri4));
        
        final Set<URI> imports2 = importsMap.get(OntologyConstant.testImportVersionUri2);
        Assert.assertNotNull(imports2);
        Assert.assertEquals(2, imports2.size());
        Assert.assertTrue(imports2.contains(OntologyConstant.testImportVersionUri3));
        Assert.assertTrue(imports2.contains(OntologyConstant.testImportVersionUri4));
        
        final Set<URI> imports1 = importsMap.get(OntologyConstant.testImportVersionUri1);
        Assert.assertNotNull(imports1);
        Assert.assertEquals(3, imports1.size());
        Assert.assertTrue(imports1.contains(OntologyConstant.testImportVersionUri2));
        Assert.assertTrue(imports1.contains(OntologyConstant.testImportVersionUri3));
        Assert.assertTrue(imports1.contains(OntologyConstant.testImportVersionUri4));
        
        final Set<URI> importsRoot = importsMap.get(OntologyConstant.testVersionUri1);
        Assert.assertNotNull(importsRoot);
        Assert.assertEquals(4, importsRoot.size());
        Assert.assertTrue(importsRoot.contains(OntologyConstant.testImportVersionUri1));
        Assert.assertTrue(importsRoot.contains(OntologyConstant.testImportVersionUri2));
        Assert.assertTrue(importsRoot.contains(OntologyConstant.testImportVersionUri3));
        Assert.assertTrue(importsRoot.contains(OntologyConstant.testImportVersionUri4));
    }
    
    @Test
    public void testImportsOrderOneLevel() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        model.add(OntologyConstant.testVersionUri1, OWL.IMPORTS, OntologyConstant.testImportOntologyUri1);
        model.add(OntologyConstant.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri1, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        
        final Set<URI> schemaOntologyUris = new HashSet<URI>();
        final Set<URI> schemaVersionUris = new HashSet<URI>();
        
        schemaOntologyUris.add(OntologyConstant.testOntologyUri1);
        schemaOntologyUris.add(OntologyConstant.testImportOntologyUri1);
        
        schemaVersionUris.add(OntologyConstant.testVersionUri1);
        schemaVersionUris.add(OntologyConstant.testImportVersionUri1);
        
        final ConcurrentMap<URI, Set<URI>> importsMap = new ConcurrentHashMap<URI, Set<URI>>();
        // Expected output solution from importsMap after calling orderImports
        // importsMap.put(testVersionUri1,
        // Collections.singleton(OntologyConstant.testImportVersionUri1));
        // importsMap.put(testImportVersionUri1, new HashSet<URI>());
        
        final List<URI> orderImports =
                OntologyUtils.orderImports(model, schemaOntologyUris, schemaVersionUris, importsMap, false);
        
        Assert.assertEquals(2, orderImports.size());
        Assert.assertEquals(OntologyConstant.testImportVersionUri1, orderImports.get(0));
        Assert.assertEquals(OntologyConstant.testVersionUri1, orderImports.get(1));
        
        Assert.assertEquals(2, importsMap.size());
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testImportVersionUri1));
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testVersionUri1));
        
        final Set<URI> imports1 = importsMap.get(OntologyConstant.testImportVersionUri1);
        Assert.assertNotNull(imports1);
        Assert.assertEquals(0, imports1.size());
        
        final Set<URI> importsRoot = importsMap.get(OntologyConstant.testVersionUri1);
        Assert.assertNotNull(importsRoot);
        Assert.assertEquals(1, importsRoot.size());
        Assert.assertEquals(OntologyConstant.testImportVersionUri1, importsRoot.iterator().next());
    }
    
    @Test
    public void testImportsOrderOneLevelOutOfOrder() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        model.add(OntologyConstant.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri1, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testVersionUri1, OWL.IMPORTS, OntologyConstant.testImportOntologyUri1);
        
        final Set<URI> schemaOntologyUris = new LinkedHashSet<URI>();
        final Set<URI> schemaVersionUris = new LinkedHashSet<URI>();
        
        schemaOntologyUris.add(OntologyConstant.testImportOntologyUri1);
        schemaOntologyUris.add(OntologyConstant.testOntologyUri1);
        
        schemaVersionUris.add(OntologyConstant.testImportVersionUri1);
        schemaVersionUris.add(OntologyConstant.testVersionUri1);
        
        final ConcurrentMap<URI, Set<URI>> importsMap = new ConcurrentHashMap<URI, Set<URI>>();
        // Expected output solution from importsMap after calling orderImports
        // importsMap.put(testVersionUri1,
        // Collections.singleton(OntologyConstant.testImportVersionUri1));
        // importsMap.put(testImportVersionUri1, new HashSet<URI>());
        
        final List<URI> orderImports =
                OntologyUtils.orderImports(model, schemaOntologyUris, schemaVersionUris, importsMap, false);
        
        Assert.assertEquals(2, orderImports.size());
        Assert.assertEquals(OntologyConstant.testImportVersionUri1, orderImports.get(0));
        Assert.assertEquals(OntologyConstant.testVersionUri1, orderImports.get(1));
        
        Assert.assertEquals(2, importsMap.size());
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testImportVersionUri1));
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testVersionUri1));
        
        final Set<URI> imports1 = importsMap.get(OntologyConstant.testImportVersionUri1);
        Assert.assertNotNull(imports1);
        Assert.assertEquals(0, imports1.size());
        
        final Set<URI> importsRoot = importsMap.get(OntologyConstant.testVersionUri1);
        Assert.assertNotNull(importsRoot);
        Assert.assertEquals(1, importsRoot.size());
        Assert.assertEquals(OntologyConstant.testImportVersionUri1, importsRoot.iterator().next());
    }
    
    @Test
    public void testImportsOrderZeroLevels() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        
        final Set<URI> schemaOntologyUris = new HashSet<URI>();
        final Set<URI> schemaVersionUris = new HashSet<URI>();
        
        schemaOntologyUris.add(OntologyConstant.testOntologyUri1);
        
        schemaVersionUris.add(OntologyConstant.testVersionUri1);
        
        final ConcurrentMap<URI, Set<URI>> importsMap = new ConcurrentHashMap<URI, Set<URI>>();
        // Expected output solution from importsMap after calling orderImports
        // importsMap.put(testVersionUri1, new HashSet<URI>());
        
        final List<URI> orderImports =
                OntologyUtils.orderImports(model, schemaOntologyUris, schemaVersionUris, importsMap, false);
        
        Assert.assertEquals(1, orderImports.size());
        Assert.assertEquals(OntologyConstant.testVersionUri1, orderImports.get(0));
        
        Assert.assertEquals(1, importsMap.size());
        Assert.assertTrue(importsMap.containsKey(OntologyConstant.testVersionUri1));
        
        final Set<URI> importsRoot = importsMap.get(OntologyConstant.testVersionUri1);
        Assert.assertNotNull(importsRoot);
        Assert.assertEquals(0, importsRoot.size());
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
        input.add(this.vf.createStatement(OntologyConstant.testOntologyUri1, RDF.TYPE, OWL.ONTOLOGY));
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
        input.add(this.vf.createStatement(OntologyConstant.testOntologyUri1, RDF.TYPE, OWL.ONTOLOGY));
        input.add(this.vf.createStatement(OntologyConstant.testVersionUri1, RDF.TYPE, OWL.ONTOLOGY));
        input.add(this.vf.createStatement(OntologyConstant.testOntologyUri1, OWL.VERSIONIRI,
                OntologyConstant.testVersionUri1));
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
        
        OntologyUtils.ontologyIDsToHandler(Arrays.asList(OntologyConstant.owlid((IRI)null, null, null)),
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
        
        OntologyUtils.ontologyIDsToHandler(
                Arrays.asList(OntologyConstant.owlid(IRI.create("urn:test:ontology:iri:abc"),
                        IRI.create("urn:test:ontology:iri:abc:version:1"), null)), new StatementCollector(input));
        
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
                Arrays.asList(OntologyConstant.owlid(IRI.create("urn:test:ontology:iri:abc"), null, null)),
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
        
        OntologyUtils.ontologyIDsToHandler(
                Arrays.asList(OntologyConstant.owlid(IRI.create("urn:test:ontology:iri:abc"),
                        IRI.create("urn:test:ontology:iri:abc:version:1"),
                        IRI.create("urn:inferred:test:ontology:iri:abc:version:1:1"))), new StatementCollector(input));
        
        Assert.assertEquals(5, input.size());
        Assert.assertTrue(input.contains(null, RDF.TYPE, OWL.ONTOLOGY));
        Assert.assertTrue(input.contains(null, OWL.VERSIONIRI, null));
        Assert.assertTrue(input.contains(null, PODD.PODD_BASE_INFERRED_VERSION, null));
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
                OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.owlid((IRI)null, null, null)), input);
        
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
                        Arrays.asList(OntologyConstant.owlid(IRI.create("urn:test:ontology:iri:abc"),
                                IRI.create("urn:test:ontology:iri:abc:version:1"), null)), input);
        
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
                        Arrays.asList(OntologyConstant.owlid(IRI.create("urn:test:ontology:iri:abc"), null, null)),
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
                OntologyUtils.ontologyIDsToModel(
                        Arrays.asList(OntologyConstant.owlid(IRI.create("urn:test:ontology:iri:abc"),
                                IRI.create("urn:test:ontology:iri:abc:version:1"),
                                IRI.create("urn:inferred:test:ontology:iri:abc:version:1:1"))), input);
        
        Assert.assertNotNull(ontologyIDsToModel);
        Assert.assertEquals(input, ontologyIDsToModel);
        Assert.assertEquals(5, ontologyIDsToModel.size());
        Assert.assertTrue(ontologyIDsToModel.contains(null, RDF.TYPE, OWL.ONTOLOGY));
        Assert.assertTrue(ontologyIDsToModel.contains(null, OWL.VERSIONIRI, null));
        Assert.assertTrue(ontologyIDsToModel.contains(null, PODD.PODD_BASE_INFERRED_VERSION, null));
        Assert.assertEquals(3, ontologyIDsToModel.filter(null, RDF.TYPE, OWL.ONTOLOGY).size());
    }
    
    @Test
    public void testSchemaImportsA1() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/schema-manifest-a1b2c3.ttl"), "", RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(model, new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testA1)),
                        this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(1, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testA1));
        Assert.assertEquals(OntologyConstant.testA1, schemaManifestImports.get(0));
        
        this.assertA1B2C3(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsA1B1C1() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/schema-manifest-a1b2c3.ttl"), "", RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(
                        model,
                        new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testA1, OntologyConstant.testB1,
                                OntologyConstant.testC1)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(3, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testA1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testB1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testC1));
        Assert.assertEquals(OntologyConstant.testA1, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testB1, schemaManifestImports.get(1));
        Assert.assertEquals(OntologyConstant.testC1, schemaManifestImports.get(2));
        
        this.assertA1B2C3(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsA1B1C3() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/schema-manifest-a1b2c3.ttl"), "", RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(
                        model,
                        new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testA1, OntologyConstant.testB1,
                                OntologyConstant.testC3)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(4, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testA1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testB1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testB2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testC3));
        Assert.assertEquals(OntologyConstant.testA1, schemaManifestImports.get(0));
        // NOTE: The following three entries do not have a deterministic order in this case
        // If they start to break on another JVM, then comment them out
        // Assert.assertEquals(OntologyConstant.testB2, schemaManifestImports.get(1));
        // Assert.assertEquals(OntologyConstant.testC3, schemaManifestImports.get(2));
        // Assert.assertEquals(OntologyConstant.testB1, schemaManifestImports.get(3));
        // Check the general ordering of B2 and C3 are correct. B1 only depends on A1 so it can be
        // anywhere except the first position
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testB2) < schemaManifestImports
                .indexOf(OntologyConstant.testC3));
        
        this.assertA1B2C3(this.importsMap);
        
    }
    
    @Test
    public void testSchemaImportsA1B2C3() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/schema-manifest-a1b2c3.ttl"), "", RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(
                        model,
                        new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testA1, OntologyConstant.testB2,
                                OntologyConstant.testC3)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(3, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testA1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testB2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testC3));
        Assert.assertEquals(OntologyConstant.testA1, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testB2, schemaManifestImports.get(1));
        Assert.assertEquals(OntologyConstant.testC3, schemaManifestImports.get(2));
        
        this.assertA1B2C3(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsB1() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/schema-manifest-a1b2c3.ttl"), "", RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(model, new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testB1)),
                        this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(2, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testA1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testB1));
        Assert.assertEquals(OntologyConstant.testA1, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testB1, schemaManifestImports.get(1));
        
        this.assertA1B2C3(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsB2() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/schema-manifest-a1b2c3.ttl"), "", RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(model, new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testB2)),
                        this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(2, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testA1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testB2));
        Assert.assertEquals(OntologyConstant.testA1, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testB2, schemaManifestImports.get(1));
        
        this.assertA1B2C3(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsC1() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/schema-manifest-a1b2c3.ttl"), "", RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(model, new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testC1)),
                        this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(3, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testA1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testB1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testC1));
        Assert.assertEquals(OntologyConstant.testA1, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testB1, schemaManifestImports.get(1));
        Assert.assertEquals(OntologyConstant.testC1, schemaManifestImports.get(2));
        
        this.assertA1B2C3(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsC3() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/schema-manifest-a1b2c3.ttl"), "", RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(model, new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testC3)),
                        this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(3, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testA1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testB2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testC3));
        Assert.assertEquals(OntologyConstant.testA1, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testB2, schemaManifestImports.get(1));
        Assert.assertEquals(OntologyConstant.testC3, schemaManifestImports.get(2));
        
        this.assertA1B2C3(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsEmpty() throws Exception
    {
        final Model model = new LinkedHashModel();
        
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(model, Collections.<OWLOntologyID> emptySet(), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(0, schemaManifestImports.size());
        
        Assert.assertEquals(0, this.importsMap.size());
    }
    
    @Test
    public void testSchemaImportsOneLevel() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        model.add(OntologyConstant.testVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testVersionUri1, OWL.IMPORTS, OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri1, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        
        // DebugUtils.printContents(model);
        
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(
                        model,
                        new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testOntologyID,
                                OntologyConstant.testImportOntologyID1)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(2, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testOntologyID));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testImportOntologyID1));
        Assert.assertEquals(OntologyConstant.testImportOntologyID1, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testOntologyID, schemaManifestImports.get(1));
        
        Assert.assertEquals(2, this.importsMap.size());
        Assert.assertTrue(this.importsMap.containsKey(OntologyConstant.testImportVersionUri1));
        Assert.assertTrue(this.importsMap.containsKey(OntologyConstant.testVersionUri1));
        Assert.assertTrue(this.importsMap.get(OntologyConstant.testImportVersionUri1).isEmpty());
        Assert.assertEquals(1, this.importsMap.get(OntologyConstant.testVersionUri1).size());
        Assert.assertTrue(this.importsMap.get(OntologyConstant.testVersionUri1).contains(
                OntologyConstant.testImportVersionUri1));
    }
    
    @Test
    public void testSchemaImportsOneLevelCurrentVersion() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        model.add(OntologyConstant.testVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testVersionUri1, OWL.IMPORTS, OntologyConstant.testImportOntologyUri1);
        model.add(OntologyConstant.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
        model.add(OntologyConstant.testImportOntologyUri1, PODD.OMV_CURRENT_VERSION,
                OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testImportOntologyUri1, OWL.VERSIONIRI, OntologyConstant.testImportVersionUri1);
        model.add(OntologyConstant.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
        
        // DebugUtils.printContents(model);
        
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(
                        model,
                        new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testOntologyID,
                                OntologyConstant.testImportOntologyID1)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(2, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testOntologyID));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testImportOntologyID1));
        Assert.assertEquals(OntologyConstant.testImportOntologyID1, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testOntologyID, schemaManifestImports.get(1));
        
        Assert.assertEquals(2, this.importsMap.size());
        Assert.assertTrue(this.importsMap.containsKey(OntologyConstant.testVersionUri1));
        Assert.assertTrue(this.importsMap.containsKey(OntologyConstant.testImportVersionUri1));
        Assert.assertEquals(0, this.importsMap.get(OntologyConstant.testImportVersionUri1).size());
        Assert.assertEquals(1, this.importsMap.get(OntologyConstant.testVersionUri1).size());
        Assert.assertTrue(this.importsMap.get(OntologyConstant.testVersionUri1).contains(
                OntologyConstant.testImportVersionUri1));
    }
    
    @Test
    public void testSchemaImportsRealisticPoddV1Base() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                        RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(model,
                        new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testPoddBaseV1)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(4, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV1));
        Assert.assertEquals(OntologyConstant.testPoddDcV1, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testPoddFoafV1, schemaManifestImports.get(1));
        Assert.assertEquals(OntologyConstant.testPoddUserV1, schemaManifestImports.get(2));
        Assert.assertEquals(OntologyConstant.testPoddBaseV1, schemaManifestImports.get(3));
        
        this.assertRealisticImportsMapV2(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsRealisticPoddV1DcFoaf() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                        RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(
                        model,
                        new LinkedHashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testPoddDcV1,
                                OntologyConstant.testPoddFoafV1)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(2, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV1));
        Assert.assertEquals(OntologyConstant.testPoddDcV1, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testPoddFoafV1, schemaManifestImports.get(1));
        
        this.assertRealisticImportsMapV2(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsRealisticPoddV1Plant() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                        RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(model,
                        new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testPoddPlantV1)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(6, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddScienceV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddPlantV1));
        Assert.assertEquals(OntologyConstant.testPoddDcV1, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testPoddFoafV1, schemaManifestImports.get(1));
        Assert.assertEquals(OntologyConstant.testPoddUserV1, schemaManifestImports.get(2));
        Assert.assertEquals(OntologyConstant.testPoddBaseV1, schemaManifestImports.get(3));
        Assert.assertEquals(OntologyConstant.testPoddScienceV1, schemaManifestImports.get(4));
        Assert.assertEquals(OntologyConstant.testPoddPlantV1, schemaManifestImports.get(5));
        
        this.assertRealisticImportsMapV2(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsRealisticPoddV1V2AllToBaseInOrder() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                        RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(
                        model,
                        new LinkedHashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testPoddDcV1,
                                OntologyConstant.testPoddDcV2, OntologyConstant.testPoddFoafV1,
                                OntologyConstant.testPoddFoafV2, OntologyConstant.testPoddUserV1,
                                OntologyConstant.testPoddUserV2, OntologyConstant.testPoddBaseV1,
                                OntologyConstant.testPoddBaseV2)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(8, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV2));
        
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddFoafV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddUserV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddUserV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        
        this.assertRealisticImportsMapV2(this.importsMap);
    }
    
    /**
     * Randomised test, to fuzz test the algorithm.
     *
     * @throws Exception
     */
    @Test
    public void testSchemaImportsRealisticPoddV1V2AllToBaseRandomOrder() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                        RDFFormat.TURTLE);
        
        final List<OWLOntologyID> imports =
                new ArrayList<OWLOntologyID>(Arrays.asList(OntologyConstant.testPoddBaseV1,
                        OntologyConstant.testPoddUserV1, OntologyConstant.testPoddUserV2,
                        OntologyConstant.testPoddFoafV1, OntologyConstant.testPoddBaseV2,
                        OntologyConstant.testPoddFoafV2, OntologyConstant.testPoddDcV1, OntologyConstant.testPoddDcV2));
        
        Collections.shuffle(imports);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(model, new LinkedHashSet<OWLOntologyID>(imports), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(8, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV2));
        
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddFoafV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddUserV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddUserV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        
        this.assertRealisticImportsMapV2(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsRealisticPoddV1V2AllToBaseReverseOrder() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                        RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(
                        model,
                        new LinkedHashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testPoddBaseV1,
                                OntologyConstant.testPoddBaseV2, OntologyConstant.testPoddUserV1,
                                OntologyConstant.testPoddUserV2, OntologyConstant.testPoddFoafV1,
                                OntologyConstant.testPoddFoafV2, OntologyConstant.testPoddDcV1,
                                OntologyConstant.testPoddDcV2)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(8, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV2));
        
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddFoafV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddUserV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddUserV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        
        this.assertRealisticImportsMapV2(this.importsMap);
    }
    
    /**
     * Randomised test, to fuzz test the algorithm.
     *
     * @throws Exception
     */
    @Test
    public void testSchemaImportsRealisticPoddV1V2AllToPlantRandomOrder() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                        RDFFormat.TURTLE);
        
        final List<OWLOntologyID> imports =
                new ArrayList<OWLOntologyID>(Arrays.asList(OntologyConstant.testPoddBaseV1,
                        OntologyConstant.testPoddUserV1, OntologyConstant.testPoddUserV2,
                        OntologyConstant.testPoddFoafV1, OntologyConstant.testPoddPlantV1,
                        OntologyConstant.testPoddPlantV2, OntologyConstant.testPoddBaseV2,
                        OntologyConstant.testPoddFoafV2, OntologyConstant.testPoddDcV1, OntologyConstant.testPoddDcV2,
                        OntologyConstant.testPoddScienceV1, OntologyConstant.testPoddScienceV2));
        
        // Randomise the order to fuzz test the algorithm
        Collections.shuffle(imports);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> result =
                OntologyUtils.schemaImports(model, new LinkedHashSet<OWLOntologyID>(imports), this.importsMap);
        
        Assert.assertNotNull(result);
        Assert.assertEquals(12, result.size());
        Assert.assertTrue(result.contains(OntologyConstant.testPoddDcV1));
        Assert.assertTrue(result.contains(OntologyConstant.testPoddDcV2));
        Assert.assertTrue(result.contains(OntologyConstant.testPoddFoafV1));
        Assert.assertTrue(result.contains(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(result.contains(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(result.contains(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(result.contains(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(result.contains(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(result.contains(OntologyConstant.testPoddScienceV1));
        Assert.assertTrue(result.contains(OntologyConstant.testPoddScienceV2));
        Assert.assertTrue(result.contains(OntologyConstant.testPoddPlantV1));
        Assert.assertTrue(result.contains(OntologyConstant.testPoddPlantV2));
        
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddDcV1) < result
                .indexOf(OntologyConstant.testPoddFoafV1));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddDcV1) < result
                .indexOf(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddDcV1) < result
                .indexOf(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddDcV1) < result
                .indexOf(OntologyConstant.testPoddScienceV1));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddDcV1) < result
                .indexOf(OntologyConstant.testPoddPlantV1));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddFoafV1) < result
                .indexOf(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddFoafV1) < result
                .indexOf(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddFoafV1) < result
                .indexOf(OntologyConstant.testPoddScienceV1));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddFoafV1) < result
                .indexOf(OntologyConstant.testPoddPlantV1));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddUserV1) < result
                .indexOf(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddUserV1) < result
                .indexOf(OntologyConstant.testPoddScienceV1));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddUserV1) < result
                .indexOf(OntologyConstant.testPoddPlantV1));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddBaseV1) < result
                .indexOf(OntologyConstant.testPoddScienceV1));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddBaseV1) < result
                .indexOf(OntologyConstant.testPoddPlantV1));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddScienceV1) < result
                .indexOf(OntologyConstant.testPoddPlantV1));
        
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddDcV2) < result
                .indexOf(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddDcV2) < result
                .indexOf(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddDcV2) < result
                .indexOf(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddDcV2) < result
                .indexOf(OntologyConstant.testPoddScienceV2));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddDcV2) < result
                .indexOf(OntologyConstant.testPoddPlantV2));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddFoafV2) < result
                .indexOf(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddFoafV2) < result
                .indexOf(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddFoafV2) < result
                .indexOf(OntologyConstant.testPoddScienceV2));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddFoafV2) < result
                .indexOf(OntologyConstant.testPoddPlantV2));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddUserV2) < result
                .indexOf(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddUserV2) < result
                .indexOf(OntologyConstant.testPoddScienceV2));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddUserV2) < result
                .indexOf(OntologyConstant.testPoddPlantV2));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddBaseV2) < result
                .indexOf(OntologyConstant.testPoddScienceV2));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddBaseV2) < result
                .indexOf(OntologyConstant.testPoddPlantV2));
        Assert.assertTrue(result.indexOf(OntologyConstant.testPoddScienceV2) < result
                .indexOf(OntologyConstant.testPoddPlantV2));
        
        this.assertRealisticImportsMapV2(this.importsMap);
    }
    
    /**
     * Randomised test, to fuzz test the algorithm.
     *
     * @throws Exception
     */
    @Test
    public void testSchemaImportsRealisticPoddV1V2AllToScienceRandomOrder() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                        RDFFormat.TURTLE);
        
        final List<OWLOntologyID> imports =
                new ArrayList<OWLOntologyID>(Arrays.asList(OntologyConstant.testPoddBaseV1,
                        OntologyConstant.testPoddUserV1, OntologyConstant.testPoddUserV2,
                        OntologyConstant.testPoddFoafV1, OntologyConstant.testPoddBaseV2,
                        OntologyConstant.testPoddFoafV2, OntologyConstant.testPoddDcV1, OntologyConstant.testPoddDcV2,
                        OntologyConstant.testPoddScienceV1, OntologyConstant.testPoddScienceV2));
        
        Collections.shuffle(imports);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(model, new LinkedHashSet<OWLOntologyID>(imports), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(10, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddScienceV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddScienceV2));
        
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddFoafV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddScienceV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddScienceV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddUserV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddUserV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddScienceV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddBaseV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddScienceV1));
        
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddScienceV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddScienceV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddUserV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddUserV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddScienceV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddBaseV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddScienceV2));
        
        this.assertRealisticImportsMapV2(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsRealisticPoddV1V2Base() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                        RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(
                        model,
                        new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testPoddBaseV1,
                                OntologyConstant.testPoddBaseV2)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(8, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV2));
        
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddFoafV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddUserV1) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV1));
        
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddDcV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddFoafV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.indexOf(OntologyConstant.testPoddUserV2) < schemaManifestImports
                .indexOf(OntologyConstant.testPoddBaseV2));
        
        this.assertRealisticImportsMapV2(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsRealisticPoddV2All() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                        RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(
                        model,
                        new LinkedHashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testPoddDcV2,
                                OntologyConstant.testPoddFoafV2, OntologyConstant.testPoddUserV2,
                                OntologyConstant.testPoddBaseV2, OntologyConstant.testPoddScienceV2,
                                OntologyConstant.testPoddPlantV2)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(6, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddScienceV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddPlantV2));
        Assert.assertEquals(OntologyConstant.testPoddDcV2, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testPoddFoafV2, schemaManifestImports.get(1));
        Assert.assertEquals(OntologyConstant.testPoddUserV2, schemaManifestImports.get(2));
        Assert.assertEquals(OntologyConstant.testPoddBaseV2, schemaManifestImports.get(3));
        Assert.assertEquals(OntologyConstant.testPoddScienceV2, schemaManifestImports.get(4));
        Assert.assertEquals(OntologyConstant.testPoddPlantV2, schemaManifestImports.get(5));
        
        this.assertRealisticImportsMapV2(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsRealisticPoddV2DcFoaf() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                        RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(
                        model,
                        new LinkedHashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testPoddDcV2,
                                OntologyConstant.testPoddFoafV2)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(2, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV2));
        Assert.assertEquals(OntologyConstant.testPoddDcV2, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testPoddFoafV2, schemaManifestImports.get(1));
        
        this.assertRealisticImportsMapV2(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsRealisticPoddV2DcPlant() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                        RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(
                        model,
                        new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testPoddDcV2,
                                OntologyConstant.testPoddPlantV2)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(6, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddScienceV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddPlantV2));
        Assert.assertEquals(OntologyConstant.testPoddDcV2, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testPoddFoafV2, schemaManifestImports.get(1));
        Assert.assertEquals(OntologyConstant.testPoddUserV2, schemaManifestImports.get(2));
        Assert.assertEquals(OntologyConstant.testPoddBaseV2, schemaManifestImports.get(3));
        Assert.assertEquals(OntologyConstant.testPoddScienceV2, schemaManifestImports.get(4));
        Assert.assertEquals(OntologyConstant.testPoddPlantV2, schemaManifestImports.get(5));
        
        this.assertRealisticImportsMapV2(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsRealisticPoddV2Plant() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                        RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(model,
                        new HashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testPoddPlantV2)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(6, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddUserV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddBaseV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddScienceV2));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddPlantV2));
        Assert.assertEquals(OntologyConstant.testPoddDcV2, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testPoddFoafV2, schemaManifestImports.get(1));
        Assert.assertEquals(OntologyConstant.testPoddUserV2, schemaManifestImports.get(2));
        Assert.assertEquals(OntologyConstant.testPoddBaseV2, schemaManifestImports.get(3));
        Assert.assertEquals(OntologyConstant.testPoddScienceV2, schemaManifestImports.get(4));
        Assert.assertEquals(OntologyConstant.testPoddPlantV2, schemaManifestImports.get(5));
        
        this.assertRealisticImportsMapV2(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsRealisticV3PoddV2DcFoaf() throws Exception
    {
        final Model model =
                Rio.parse(this.getClass().getResourceAsStream("/default-podd-schema-manifest.ttl"), "",
                        RDFFormat.TURTLE);
        
        // DebugUtils.printContents(model);
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(
                        model,
                        new LinkedHashSet<OWLOntologyID>(Arrays.asList(OntologyConstant.testPoddDcV1,
                                OntologyConstant.testPoddFoafV1)), this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(2, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddDcV1));
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testPoddFoafV1));
        Assert.assertEquals(OntologyConstant.testPoddDcV1, schemaManifestImports.get(0));
        Assert.assertEquals(OntologyConstant.testPoddFoafV1, schemaManifestImports.get(1));
        
        this.assertRealisticImportsMapV3(this.importsMap);
    }
    
    @Test
    public void testSchemaImportsZeroLevels() throws Exception
    {
        final Model model = new LinkedHashModel();
        OntologyUtils.ontologyIDsToModel(Arrays.asList(OntologyConstant.testOntologyID), model);
        
        final List<OWLOntologyID> schemaManifestImports =
                OntologyUtils.schemaImports(model, Collections.singleton(OntologyConstant.testOntologyID),
                        this.importsMap);
        
        Assert.assertNotNull(schemaManifestImports);
        Assert.assertEquals(1, schemaManifestImports.size());
        Assert.assertTrue(schemaManifestImports.contains(OntologyConstant.testOntologyID));
        
        Assert.assertEquals(1, this.importsMap.size());
        Assert.assertTrue(this.importsMap.containsKey(OntologyConstant.testVersionUri1));
        Assert.assertEquals(0, this.importsMap.get(OntologyConstant.testVersionUri1).size());
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
