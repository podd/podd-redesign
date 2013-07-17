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

import com.github.podd.utils.PoddRdfConstants;

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
        String filePath = "/home/user/path/";
        String fileName = "project-00100";
        RDFFormat format = RDFFormat.RDFXML;
        int objectCount = 100;
        String seed = "cent";
        
        // - generate top object
        final ArtifactGenerator generator = new ArtifactGenerator();
        final Model model = generator.createNewModel();
        URI topObject = generator.addProject(model, seed);
        
        URI parentOfInvestigation = topObject;
        
        // - add internal objects
        while (objectCount > 0)
        {
            parentOfInvestigation = generator.addInvestigation(model, parentOfInvestigation, seed + objectCount);
            
            URI platform1 = generator.addPlatform(model, topObject, seed + objectCount);
            URI platform2 = generator.addPlatform(model, topObject, seed + objectCount);
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
                this.addBasics(model, seed, VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Analysis"),
                        "Analysis " + seed, "Description about Analysis " + seed);
        
        // refers To
        for (URI referredPlatform : platformRefs)
        {
            model.add(thisObject, VF.createURI(PoddRdfConstants.PODD_SCIENCE, "refersToPlatform"), referredPlatform);
        }
        
        // connect to parent
        model.add(parentUri, VF.createURI(PoddRdfConstants.PODD_SCIENCE, "hasAnalysis"), thisObject);
    }
    
    /**
     * Create a temporary URI for the object and add TYPE, Title and Description. 
     */
    protected URI addBasics(final Model model, final String seed, final URI type, final String title,
            final String description)
    {
        final URI thisObject = this.getRandomObjectUri(seed);
        
        model.add(thisObject, RDF.TYPE, type);
        model.add(thisObject, RDF.TYPE, VF.createURI(OWL.NAMESPACE, "NamedIndividual"));
        model.add(thisObject, RDFS.LABEL, VF.createLiteral(title));
        model.add(thisObject, RDFS.COMMENT, VF.createLiteral(description, ArtifactGenerator.XSD_STRING));
        
        return thisObject;
    }

    /**
     * Add a new Genotype object.
     */
    protected URI addGenotype(final Model model, final URI parentUri, final String seed)
    {
        final URI thisObject =
                this.addBasics(model, seed, VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Genotype"),
                        "Genotype " + seed, "Description about Genotype " + seed);

        // mandatory attribute
        model.add(thisObject, VF.createURI(PoddRdfConstants.PODD_SCIENCE, "hasGenusSpecies"),
                VF.createLiteral("Genus or species " + seed, ArtifactGenerator.XSD_STRING));
        model.add(thisObject, VF.createURI(PoddRdfConstants.PODD_SCIENCE, "hasWildType"),
                VF.createURI(PoddRdfConstants.PODD_SCIENCE, "WildType_Yes"));
        
        // connect to parent
        model.add(parentUri, VF.createURI(PoddRdfConstants.PODD_SCIENCE, "hasGenotype"), thisObject);
        
        return thisObject;
    }
    
    /**
     * Add a new Investigation object.
     */
    protected URI addInvestigation(final Model model, final URI parentUri, final String seed)
    {
        final URI thisObject =
                this.addBasics(model, seed, VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Investigation"),
                        "Investigation " + seed, "Description about Investigation " + seed);

        // mandatory attribute
        model.add(thisObject, VF.createURI(PoddRdfConstants.PODD_BASE, "hasStartDateTime"),
                VF.createLiteral("2013-01-01T09:00:00", ArtifactGenerator.XSD_DATETIME));
        
        // connect to parent
        model.add(parentUri, VF.createURI(PoddRdfConstants.PODD_SCIENCE, "hasInvestigation"), thisObject);
        
        return thisObject;
    }

    /**
     * Add a new Platform object.
     */
    protected URI addPlatform(final Model model, final URI parentUri, final String seed)
    {
        final URI thisObject =
                this.addBasics(model, seed, VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Platform"),
                        "Platform " + seed, "Description about Platform " + seed);

        // mandatory attribute
        model.add(thisObject, VF.createURI(PoddRdfConstants.PODD_SCIENCE, "hasPlatformType"),
                VF.createURI(PoddRdfConstants.PODD_SCIENCE, "PlatformType_HardwareSoftware"));
        
        // connect to parent
        model.add(parentUri, VF.createURI(PoddRdfConstants.PODD_SCIENCE, "hasPlatform"), thisObject);
        
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
            model.add(artifactUri, OWL.IMPORTS, VF.createURI(schemaOntology));
        }
        
        // top object
        final URI topObject =
                this.addBasics(model, seed, VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Project"), "Project " + seed,
                        "Description about Project " + seed);
        model.add(topObject, VF.createURI(PoddRdfConstants.PODD_BASE, "hasLeadInstitution"),
                VF.createLiteral("CSIRO HRPPC " + seed, ArtifactGenerator.XSD_STRING));
        model.add(topObject, VF.createURI(PoddRdfConstants.PODD_SCIENCE, "hasANZSRC"),
                VF.createURI(PoddRdfConstants.PODD_PLANT, "ANZSRC06-Biological-Sciences"));
        model.add(topObject, VF.createURI(PoddRdfConstants.PODD_BASE, "hasPrincipalInvestigator"),
                VF.createURI("mailto:xavier.sirault@csiro.au"));
        model.add(topObject, VF.createURI(PoddRdfConstants.PODD_BASE, "hasPublicationStatus"),
                VF.createURI(PoddRdfConstants.PODD_BASE, "NotPublished"));
        model.add(topObject, VF.createURI(PoddRdfConstants.PODD_BASE, "hasStartDate"),
                VF.createLiteral("2013-01-01", ArtifactGenerator.XSD_DATE));
        
        model.add(artifactUri, PoddRdfConstants.PODD_BASE_HAS_TOP_OBJECT, topObject);

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
        model.setNamespace("dcTerms", PoddRdfConstants.PODD_DCTERMS);
        model.setNamespace("foaf", PoddRdfConstants.PODD_FOAF);
        model.setNamespace("poddUser", PoddRdfConstants.PODD_USER);
        model.setNamespace("poddBase", PoddRdfConstants.PODD_BASE);
        model.setNamespace("poddScience", PoddRdfConstants.PODD_SCIENCE);
        model.setNamespace("poddPlant", PoddRdfConstants.PODD_PLANT);
        
        return model;
    }
    
    protected URI getRandomObjectUri(final String seed)
    {
        final URI objectUri = VF.createURI("urn:temp:uuid:object:" + seed + ":" + UUID.randomUUID().toString());
        return objectUri;
    }
    
}
