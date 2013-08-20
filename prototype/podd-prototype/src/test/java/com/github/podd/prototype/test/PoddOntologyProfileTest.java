/*
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
package com.github.podd.prototype.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.semanticweb.owlapi.formats.RDFXMLOntologyFormatFactory;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileRegistry;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This test verifies the OWL Profiles for each of the PODD ontologies using a profile report.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
@RunWith(Parameterized.class)
public class PoddOntologyProfileTest
{
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private OWLOntologyManager manager;
    
    private String ontologyResourcePath;
    
    private List<IRI> inProfiles;
    private List<IRI> outProfiles;
    
    private String[] prerequisitePaths;
    
    private static final String poddBasePath = "/ontologies/poddBase.owl";
    private static final String poddSciencePath = "/ontologies/poddScience.owl";
    private static final String poddAnimalPath = "/ontologies/poddAnimal.owl";
    private static final String poddPlantPath = "/ontologies/poddPlant.owl";
    private static final String poddUserPath = "/ontologies/poddUser.owl";
    
    public PoddOntologyProfileTest(final String[] prerequisitePaths, final String ontologyResourcePath,
            final IRI[] inProfiles, final IRI[] outProfiles)
    {
        this.prerequisitePaths = prerequisitePaths;
        this.ontologyResourcePath = ontologyResourcePath;
        this.inProfiles = Arrays.asList(inProfiles);
        this.outProfiles = Arrays.asList(outProfiles);
    }
    
    @Before
    public void setUp() throws Exception
    {
        // create the manager to use for the test
        this.manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        Assert.assertNotNull("Could not create a manager", this.manager);
        
        // All ontologies should be in at least one profile
        Assert.assertFalse("Ontology was not thought to be in any profiles: " + this.ontologyResourcePath,
                this.inProfiles.isEmpty());
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.manager = null;
    }
    
    @Parameters
    public static Collection<Object[]> getData()
    {
        return Arrays.asList(new Object[][] {
                { new String[] {}, PoddOntologyProfileTest.poddBasePath, new IRI[] { OWLProfile.OWL2_DL },
                        new IRI[] { OWLProfile.OWL2_EL, OWLProfile.OWL2_RL, OWLProfile.OWL2_QL } },
                { new String[] { PoddOntologyProfileTest.poddBasePath }, PoddOntologyProfileTest.poddSciencePath,
                        new IRI[] { OWLProfile.OWL2_DL },
                        new IRI[] { OWLProfile.OWL2_EL, OWLProfile.OWL2_RL, OWLProfile.OWL2_QL } },
                { new String[] { PoddOntologyProfileTest.poddBasePath, PoddOntologyProfileTest.poddSciencePath },
                        PoddOntologyProfileTest.poddAnimalPath, new IRI[] { OWLProfile.OWL2_DL },
                        new IRI[] { OWLProfile.OWL2_EL, OWLProfile.OWL2_RL, OWLProfile.OWL2_QL } },
                { new String[] { PoddOntologyProfileTest.poddBasePath, PoddOntologyProfileTest.poddSciencePath },
                        PoddOntologyProfileTest.poddPlantPath, new IRI[] { OWLProfile.OWL2_DL },
                        new IRI[] { OWLProfile.OWL2_EL, OWLProfile.OWL2_RL, OWLProfile.OWL2_QL } },
                { new String[] { PoddOntologyProfileTest.poddBasePath }, PoddOntologyProfileTest.poddUserPath,
                        new IRI[] { OWLProfile.OWL2_DL },
                        new IRI[] { OWLProfile.OWL2_EL, OWLProfile.OWL2_RL, OWLProfile.OWL2_QL } }, });
    }
    
    /**
     * Tests that base ontology is in inProfiles.
     * 
     * @throws Exception
     */
    @Test
    public final void testValidOWLProfiles() throws Exception
    {
        for(final IRI nextInProfile : this.inProfiles)
        {
            OWLOntology parsedOntology = null;
            try
            {
                for(final String nextPrerequisite : this.prerequisitePaths)
                {
                    this.manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(this.getClass()
                            .getResourceAsStream(nextPrerequisite), new RDFXMLOntologyFormatFactory()));
                }
                
                parsedOntology =
                        this.manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(this.getClass()
                                .getResourceAsStream(this.ontologyResourcePath), new RDFXMLOntologyFormatFactory()));
                Assert.assertTrue("Profile that failed: " + nextInProfile.toQuotedString(),
                        this.runConsistencyCheck(nextInProfile, parsedOntology));
            }
            finally
            {
                for(final OWLOntology nextOntology : this.manager.getOntologies())
                {
                    this.manager.removeOntology(nextOntology);
                }
            }
        }
    }
    
    /**
     * Tests that base ontology is not in outProfiles.
     * 
     * @throws Exception
     */
    @Test
    public final void testInvalidOWLProfiles() throws Exception
    {
        for(final IRI nextOutProfile : this.outProfiles)
        {
            OWLOntology parsedOntology = null;
            try
            {
                for(final String nextPrerequisite : this.prerequisitePaths)
                {
                    this.manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(this.getClass()
                            .getResourceAsStream(nextPrerequisite), new RDFXMLOntologyFormatFactory()));
                }
                
                parsedOntology =
                        this.manager.loadOntologyFromOntologyDocument(new StreamDocumentSource(this.getClass()
                                .getResourceAsStream(this.ontologyResourcePath), new RDFXMLOntologyFormatFactory()));
                Assert.assertFalse(this.runConsistencyCheck(nextOutProfile, parsedOntology));
            }
            finally
            {
                for(final OWLOntology nextOntology : this.manager.getOntologies())
                {
                    this.manager.removeOntology(nextOntology);
                }
            }
        }
    }
    
    /**
     * Helper method to check consistency based on different OWL profiles
     * 
     * @param owlProfile
     * @param ontologyResourcePath
     *            TODO
     * @param format
     * @return True if the consistency check passed and false otherwise.
     * @throws Exception
     */
    private boolean runConsistencyCheck(final IRI owlProfile, final OWLOntology parsedOntology) throws Exception
    {
        final OWLProfile profile = OWLProfileRegistry.getInstance().getProfile(owlProfile);
        final OWLProfileReport report = profile.checkOntology(parsedOntology);
        this.printOWLProfileReport(report);
        
        return report.isInProfile();
    }
    
    private void printOWLProfileReport(final OWLProfileReport report)
    {
        this.log.debug("==== Profile Report ======");
        this.log.debug(" Profile:" + report.getProfile());
        this.log.debug(" Is in Profile: " + report.isInProfile());
        this.log.debug(" No. of violations: " + report.getViolations().size());
        
        if(this.log.isTraceEnabled())
        {
            final ArrayList<String> violations = new ArrayList<String>();
            
            for(final OWLProfileViolation violation : report.getViolations())
            {
                violations.add(violation.toString());
            }
            
            Collections.sort(violations);
            
            for(final String nextViolation : violations)
            {
                this.log.trace(nextViolation);
            }
        }
    }
    
}
