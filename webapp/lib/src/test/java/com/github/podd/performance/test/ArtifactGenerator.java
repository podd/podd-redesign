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
 * Utility class to generate PODD artifacts for test purposes.
 * 
 * FIXME: in progress
 * 
 * @author kutila
 */
public class ArtifactGenerator
{
    
    public static final ValueFactory VF = ValueFactoryImpl.getInstance();
    
    public static final URI XSD_STRING = ArtifactGenerator.VF.createURI("http://www.w3.org/2001/XMLSchema#string");
    public static final URI XSD_DATE = ArtifactGenerator.VF.createURI("http://www.w3.org/2001/XMLSchema#date");
    
    private static final String filePath = "/home/kutila/";
    
    public static enum FILE_TYPES
    {
        RDFXML, TURTLE
    };
    
    public static String[] SCHEMA_ONTOLOGIES = { "http://purl.org/podd/ns/dcTerms", "http://purl.org/podd/ns/foaf",
            "http://purl.org/podd/ns/poddUser", "http://purl.org/podd/ns/poddBase",
            "http://purl.org/podd/ns/poddScience", "http://purl.org/podd/ns/poddPlant", };
    
    public static void main(final String[] args) throws Exception
    {
        final FILE_TYPES fileType = FILE_TYPES.TURTLE;
        final String seed = "AbC39";
        
        final ArtifactGenerator generator = new ArtifactGenerator();
        final Model model = generator.createNewModel();
        generator.addProject(model, seed);
        
        // - add internal objects TODO
        
        // - persist to file
        String fileName = "testArtifact";
        RDFFormat format = null;
        if(fileType == FILE_TYPES.TURTLE)
        {
            format = RDFFormat.TURTLE;
            fileName = fileName + ".ttl";
        }
        else if(fileType == FILE_TYPES.RDFXML)
        {
            format = RDFFormat.RDFXML;
            fileName = fileName + ".rdf";
        }
        
        final FileOutputStream out = new FileOutputStream(ArtifactGenerator.filePath + fileName);
        Rio.write(model, out, format);
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
    
    protected void addProject(final Model model, final String seed)
    {
        final URI artifactUri = ArtifactGenerator.VF.createURI("urn:temp:uuid:artifact:" + seed);
        final URI topObject = this.getRandomObjectUri(seed);
        
        // artifact
        model.add(artifactUri, RDF.TYPE, OWL.ONTOLOGY);
        
        // import schema ontologies
        for(final String schemaOntology : ArtifactGenerator.SCHEMA_ONTOLOGIES)
        {
            model.add(artifactUri, OWL.IMPORTS, ArtifactGenerator.VF.createURI(schemaOntology));
        }
        model.add(artifactUri, PoddRdfConstants.PODD_BASE_HAS_TOP_OBJECT, topObject);
        
        // top object
        model.add(topObject, RDF.TYPE, ArtifactGenerator.VF.createURI(PoddRdfConstants.PODD_SCIENCE, "Project"));
        model.add(topObject, RDFS.LABEL, ArtifactGenerator.VF.createLiteral("Project " + seed));
        model.add(topObject, RDFS.COMMENT,
                ArtifactGenerator.VF.createLiteral("Description about Project " + seed, ArtifactGenerator.XSD_STRING));
        model.add(topObject, ArtifactGenerator.VF.createURI(PoddRdfConstants.PODD_BASE, "hasLeadInstitution"),
                ArtifactGenerator.VF.createLiteral("CSIRO HRPPC " + seed, ArtifactGenerator.XSD_STRING));
        model.add(topObject, ArtifactGenerator.VF.createURI(PoddRdfConstants.PODD_SCIENCE, "hasANZSRC"),
                ArtifactGenerator.VF.createURI(PoddRdfConstants.PODD_PLANT, "ANZSRC-NotApplicable"));
        model.add(topObject, ArtifactGenerator.VF.createURI(PoddRdfConstants.PODD_BASE, "hasPrincipalInvestigator"),
                ArtifactGenerator.VF.createURI("mailto:xavier.sirault@csiro.au"));
        model.add(topObject, ArtifactGenerator.VF.createURI(PoddRdfConstants.PODD_BASE, "hasPublicationStatus"),
                ArtifactGenerator.VF.createURI(PoddRdfConstants.PODD_BASE, "NotPublished"));
        model.add(topObject, ArtifactGenerator.VF.createURI(PoddRdfConstants.PODD_BASE, "hasStartDate"),
                ArtifactGenerator.VF.createLiteral("2013-01-01", ArtifactGenerator.XSD_DATE));
    }
    
    protected URI getRandomObjectUri(final String seed)
    {
        final URI objectUri =
                ArtifactGenerator.VF.createURI("urn:temp:uuid:object:" + seed + ":" + UUID.randomUUID().toString());
        return objectUri;
    }
    
}
