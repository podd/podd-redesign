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
package com.github.podd.performance.test;

import java.io.FileOutputStream;
import java.util.UUID;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

import com.github.podd.utils.PODD;

/**
 * Utility class to generate PODD artifacts for test purposes. To generate a PODD artifact set the
 * parameters inside the main method and run as a stand alone application. The parameters that can
 * be set are:
 * 
 * - Location where file should be generated
 * 
 * - Name of the file to generate
 * 
 * - Format of file to generate
 * 
 * - Number of internal PODD objects the generated artifact should have. This maybe exceeded by 4.
 * 
 * - Seed, a string value appended to generated names and descriptions for the PODD objects.
 * 
 * The internal PODD object generation happens in runs, where each run generates an Investigation, 2
 * Platforms, a Genotype and an Analysis object. Successive Investigations are created as children
 * of parent Investigations. All other objects are created as direct children of the Project object.
 * 
 * @author kutila
 */
public class ArtifactGenerator
{
    
    public static final String[] SCHEMA_ONTOLOGIES = { "http://purl.org/podd/ns/dcTerms",
            "http://purl.org/podd/ns/foaf", "http://purl.org/podd/ns/poddUser", "http://purl.org/podd/ns/poddBase",
            "http://purl.org/podd/ns/poddScience", "http://purl.org/podd/ns/poddPlant", };
    
    public static final ValueFactory VF = ValueFactoryImpl.getInstance();
    
    public static final URI XSD_DATE = ArtifactGenerator.VF.createURI("http://www.w3.org/2001/XMLSchema#date");
    
    public static final URI XSD_DATETIME = ArtifactGenerator.VF.createURI("http://www.w3.org/2001/XMLSchema#dateTime");
    
    public static final URI XSD_STRING = ArtifactGenerator.VF.createURI("http://www.w3.org/2001/XMLSchema#string");
    
    /**
     * Main method
     */
    public static void main(final String[] args) throws Exception
    {
        // - parameters need to be manually set
        final String filePath = "/home/user/path/";
        String fileName = "project-00100";
        final RDFFormat format = RDFFormat.RDFXML;
        int objectCount = 100;
        final String seed = "cent";
        
        // - generate top object
        final ArtifactGenerator generator = new ArtifactGenerator();
        final Model model = generator.createNewModel();
        final URI topObject = generator.addProject(model, seed);
        
        URI parentOfInvestigation = topObject;
        
        // - add internal objects
        while(objectCount > 0)
        {
            parentOfInvestigation = generator.addInvestigation(model, parentOfInvestigation, seed + objectCount);
            
            final URI platform1 = generator.addPlatform(model, topObject, seed + objectCount);
            final URI platform2 = generator.addPlatform(model, topObject, seed + objectCount);
            generator.addAnalysis(model, topObject, seed + objectCount, platform1, platform2);
            
            generator.addGenotype(model, topObject, seed + objectCount);
            objectCount = objectCount - 5;
        }
        
        // - persist to file
        fileName = filePath + fileName + '.' + format.getDefaultFileExtension();
        final FileOutputStream out = new FileOutputStream(fileName);
        Rio.write(model, out, format);
        System.out.println("Wrote to " + fileName);
    }
    
    /**
     * Add a new Analysis object.
     */
    protected void addAnalysis(final Model model, final URI parentUri, final String seed, final URI... platformRefs)
    {
        final URI thisObject =
                this.addBasics(model, seed, ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "Analysis"), "Analysis "
                        + seed, "Description about Analysis " + seed);
        
        // refers To
        for(final URI referredPlatform : platformRefs)
        {
            model.add(thisObject, ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "refersToPlatform"),
                    referredPlatform);
        }
        
        // connect to parent
        model.add(parentUri, ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "hasAnalysis"), thisObject);
    }
    
    /**
     * Create a temporary URI for the object and add TYPE, Title and Description.
     */
    protected URI addBasics(final Model model, final String seed, final URI type, final String title,
            final String description)
    {
        final URI thisObject = this.getRandomObjectUri(seed);
        
        model.add(thisObject, RDF.TYPE, type);
        model.add(thisObject, RDF.TYPE, ArtifactGenerator.VF.createURI(OWL.NAMESPACE, "NamedIndividual"));
        model.add(thisObject, RDFS.LABEL, ArtifactGenerator.VF.createLiteral(title));
        model.add(thisObject, RDFS.COMMENT,
                ArtifactGenerator.VF.createLiteral(description, ArtifactGenerator.XSD_STRING));
        
        return thisObject;
    }
    
    /**
     * Add a new Genotype object.
     */
    protected URI addGenotype(final Model model, final URI parentUri, final String seed)
    {
        final URI thisObject =
                this.addBasics(model, seed, ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "Genotype"), "Genotype "
                        + seed, "Description about Genotype " + seed);
        
        // mandatory attribute
        model.add(thisObject, ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "hasGenusSpecies"),
                ArtifactGenerator.VF.createLiteral("Genus or species " + seed, ArtifactGenerator.XSD_STRING));
        model.add(thisObject, ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "hasWildType"),
                ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "WildType_Yes"));
        
        // connect to parent
        model.add(parentUri, ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "hasGenotype"), thisObject);
        
        return thisObject;
    }
    
    /**
     * Add a new Investigation object.
     */
    protected URI addInvestigation(final Model model, final URI parentUri, final String seed)
    {
        final URI thisObject =
                this.addBasics(model, seed, ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "Investigation"),
                        "Investigation " + seed, "Description about Investigation " + seed);
        
        // mandatory attribute
        model.add(thisObject, ArtifactGenerator.VF.createURI(PODD.PODD_BASE, "hasStartDateTime"),
                ArtifactGenerator.VF.createLiteral("2013-01-01T09:00:00", ArtifactGenerator.XSD_DATETIME));
        
        // connect to parent
        model.add(parentUri, ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "hasInvestigation"), thisObject);
        
        return thisObject;
    }
    
    /**
     * Add a new Platform object.
     */
    protected URI addPlatform(final Model model, final URI parentUri, final String seed)
    {
        final URI thisObject =
                this.addBasics(model, seed, ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "Platform"), "Platform "
                        + seed, "Description about Platform " + seed);
        
        // mandatory attribute
        model.add(thisObject, ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "hasPlatformType"),
                ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "PlatformType_HardwareSoftware"));
        
        // connect to parent
        model.add(parentUri, ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "hasPlatform"), thisObject);
        
        return thisObject;
    }
    
    /**
     * Add a Project object to the given Model.
     */
    protected URI addProject(final Model model, final String seed)
    {
        final URI artifactUri = ArtifactGenerator.VF.createURI("urn:temp:uuid:artifact:" + seed);
        model.add(artifactUri, RDF.TYPE, OWL.ONTOLOGY);
        
        // import schema ontologies
        for(final String schemaOntology : ArtifactGenerator.SCHEMA_ONTOLOGIES)
        {
            model.add(artifactUri, OWL.IMPORTS, ArtifactGenerator.VF.createURI(schemaOntology));
        }
        
        // top object
        final URI topObject =
                this.addBasics(model, seed, ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "Project"), "Project "
                        + seed, "Description about Project " + seed);
        model.add(topObject, ArtifactGenerator.VF.createURI(PODD.PODD_BASE, "hasLeadInstitution"),
                ArtifactGenerator.VF.createLiteral("CSIRO HRPPC " + seed, ArtifactGenerator.XSD_STRING));
        model.add(topObject, ArtifactGenerator.VF.createURI(PODD.PODD_SCIENCE, "hasANZSRC"),
                ArtifactGenerator.VF.createURI(PODD.PODD_PLANT, "ANZSRC06-Biological-Sciences"));
        model.add(topObject, ArtifactGenerator.VF.createURI(PODD.PODD_BASE, "hasPrincipalInvestigator"),
                ArtifactGenerator.VF.createURI("mailto:xavier.sirault@csiro.au"));
        model.add(topObject, ArtifactGenerator.VF.createURI(PODD.PODD_BASE, "hasPublicationStatus"),
                ArtifactGenerator.VF.createURI(PODD.PODD_BASE, "NotPublished"));
        model.add(topObject, ArtifactGenerator.VF.createURI(PODD.PODD_BASE, "hasStartDate"),
                ArtifactGenerator.VF.createLiteral("2013-01-01", ArtifactGenerator.XSD_DATE));
        
        model.add(artifactUri, PODD.PODD_BASE_HAS_TOP_OBJECT, topObject);
        
        return topObject;
    }
    
    protected Model createNewModel()
    {
        final Model model = new LinkedHashModel();
        model.setNamespace("rdf", RDF.NAMESPACE);
        model.setNamespace("rdfs", RDFS.NAMESPACE);
        model.setNamespace("owl", OWL.NAMESPACE);
        
        model.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNamespace("dcterms", "http://purl.org/dc/terms/");
        model.setNamespace("dcTerms", PODD.PODD_DCTERMS);
        model.setNamespace("foaf", PODD.PODD_FOAF);
        model.setNamespace("poddUser", PODD.PODD_USER);
        model.setNamespace("poddBase", PODD.PODD_BASE);
        model.setNamespace("poddScience", PODD.PODD_SCIENCE);
        model.setNamespace("poddPlant", PODD.PODD_PLANT);
        
        return model;
    }
    
    protected URI getRandomObjectUri(final String seed)
    {
        final URI objectUri =
                ArtifactGenerator.VF.createURI("urn:temp:uuid:object:" + seed + ":" + UUID.randomUUID().toString());
        // final URI objectUri =
        // VF.createURI("http://example.com/podd-performance:" + seed + ":" +
        // UUID.randomUUID().toString());
        return objectUri;
    }
    
}
