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
package au.org.plantphenomics.podd.test;

import java.io.InputStream;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import au.org.plantphenomics.podd.HrppcPoddClient;

import com.github.podd.client.api.test.AbstractPoddClientTest;
import com.github.podd.client.impl.restlet.test.RestletPoddClientImplIntegrationTest;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class HrppcPoddClientTest extends RestletPoddClientImplIntegrationTest
{
    @Override
    protected HrppcPoddClient getNewPoddClientInstance()
    {
        return new HrppcPoddClient();
    }
    
    @Test
    public final void testRegexProject() throws Exception
    {
        final Matcher matcher = HrppcPoddClient.REGEX_PROJECT.matcher("Project#2014-0001");
        Assert.assertTrue(matcher.matches());
        
    }
    
    @Test
    public final void testRegexPosition() throws Exception
    {
        final Matcher matcher1 = HrppcPoddClient.REGEX_POSITION.matcher("B2");
        Assert.assertTrue(matcher1.matches());
        
        final Matcher matcher2 = HrppcPoddClient.REGEX_POSITION.matcher("AB23454");
        Assert.assertTrue(matcher2.matches());
    }
    
    @Test
    public final void testRegexTray() throws Exception
    {
        final Matcher matcher =
                HrppcPoddClient.REGEX_TRAY
                        .matcher("Project#2014-0001_Experiment#0001_IArabidopsis.thaliana_Tray#00009");
        Assert.assertTrue(matcher.matches());
        
    }
    
    /**
     * Test method for
     * {@link au.org.plantphenomics.podd.HrppcPoddClient#uploadPlantScanList(java.io.InputStream)}.
     * 
     * @throws Exception
     * @throws
     */
    @Test
    public final void testUploadPlantScanList() throws Exception
    {
        final HrppcPoddClient poddClient = this.getNewPoddClientInstance();
        poddClient.setPoddServerUrl(this.getTestPoddServerUrl());
        poddClient.login(AbstractPoddClientTest.TEST_ADMIN_USER, AbstractPoddClientTest.TEST_ADMIN_PASSWORD);

        final InputStream input = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
        Assert.assertNotNull("Test resource missing", input);
        
        final InferredOWLOntologyID newArtifact = poddClient.uploadNewArtifact(input, RDFFormat.RDFXML);
        Assert.assertNotNull(newArtifact);
        Assert.assertNotNull(newArtifact.getOntologyIRI());
        Assert.assertNotNull(newArtifact.getVersionIRI());
        
        poddClient.uploadPlantScanList(this.getClass().getResourceAsStream("/test/hrppc/PlantScan-Template.csv"));
    }
}