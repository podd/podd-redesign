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
package com.github.podd.api.data.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.openrdf.model.URI;

import com.github.podd.api.data.DataReferenceProcessor;
import com.github.podd.api.data.SPARQLDataReference;
import com.github.podd.api.data.SPARQLDataReferenceProcessor;
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.PODD;

/**
 * @author kutila
 * 
 */
public abstract class AbstractSPARQLDataReferenceProcessorTest extends
        AbstractDataReferenceProcessorTest<SPARQLDataReference>
{
    @Override
    protected Set<URI> getExpectedDataReferenceTypes()
    {
        return Collections.singleton(PODD.PODD_BASE_DATA_REFERENCE_TYPE_SPARQL);
    }
    
    @Override
    protected final DataReferenceProcessor<SPARQLDataReference> getNewDataReferenceProcessor()
    {
        return this.getNewSPARQLDataReferenceProcessor();
    }
    
    protected abstract SPARQLDataReferenceProcessor getNewSPARQLDataReferenceProcessor();
    
    @Override
    protected String getPathToResourceWith2DataReferences()
    {
        return TestConstants.TEST_ARTIFACT_PURLS_2_SPARQL_DATA_REFS;
    }
    
    @Override
    protected void verify2DataReferences(final Collection<SPARQLDataReference> fileReferences)
    {
        Assert.assertNotNull("NULL collection of file references", fileReferences);
        Assert.assertEquals("Expected 2 file references to verify", 2, fileReferences.size());
        
        final List<String> objectIriList =
                Arrays.asList("http://purl.org/podd-test/130326f/object-rice-scan-34343-a",
                        "http://purl.org/podd-test/130326f/object-rice-scan-34343-b");
        
        final List<String> labelList = Arrays.asList("Rice tree scan 003454-98", "Rice tree scan 003454-99");
        final List<String> graphList =
                Arrays.asList("urn:test:sparqldatareference:rice-scan:a", "urn:test:sparqldatareference:rice-scan:b");
        
        for(final SPARQLDataReference sshFileReference : fileReferences)
        {
            Assert.assertNull("Artifact ID should be NULL", sshFileReference.getArtifactID());
            Assert.assertEquals("http://purl.org/podd-test/130326f/objA24#SqueekeeMaterial", sshFileReference
                    .getParentIri().toString());
            Assert.assertTrue("File Reference URI is not an expected one",
                    objectIriList.contains(sshFileReference.getObjectIri().toString()));
            Assert.assertTrue("Label is not an expected one", labelList.contains(sshFileReference.getLabel()));
            Assert.assertTrue("Graph is not an expected one", graphList.contains(sshFileReference.getGraph()));
        }
    }
    
}
