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
package com.github.podd.prototype.test;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.IRI;

/**
 * Simple utility class to generate OWL ontologies/artifacts for test purposes.
 * 
 * @author kutila
 * @since 2012/10/17
 */
public class ArtifactGenerator
{
    
    private static final String PLANT_PREFIX = "http://purl.obolibrary.org/obo/";
    private static final String PODD_SCIENCE = "http://purl.org/podd/ns/poddScience#";
    private static final String PODD_BASE = "http://purl.org/podd/ns/poddBase#";
    
    private static final String poddBasePath = "/ontologies/poddBase.owl";
    private static final String poddSciencePath = "/ontologies/poddScience.owl";
    private static final String poddAnimalPath = "/ontologies/poddAnimal.owl";
    
    private static final String filePath = "src/test/resources/test/artifacts/";
    
    private static final URI contains = IRI.create(ArtifactGenerator.PODD_BASE, "contains").toOpenRDFURI();
    private static final URI containedBy = IRI.create(ArtifactGenerator.PODD_BASE, "containedBy").toOpenRDFURI();
    
    private static final URI adjacentTo = IRI.create(ArtifactGenerator.PLANT_PREFIX, "po#adjacent_to").toOpenRDFURI();
    
    private static enum FILE_TYPES
    {
        RDFXML, NTRIPLES
    };
    
    private final RepositoryConnection repositoryConnection;
    private final ValueFactory valueFactory;
    
    /**
     * @param args
     */
    public static void main(final String[] args) throws Exception
    {
        final String filePrefix = "science-";
        final int maxResources = 1000;
        final boolean isDeep = false; // only for Science
        final boolean isPlant = false;
        final int i = 4;
        final FILE_TYPES fileType = FILE_TYPES.NTRIPLES;
        
        // create repository
        final Repository inMemoryRepository = new SailRepository(new MemoryStore());
        inMemoryRepository.initialize();
        final RepositoryConnection nextRepositoryConnection = inMemoryRepository.getConnection();
        nextRepositoryConnection.setAutoCommit(false);
        final ValueFactory valueFac = inMemoryRepository.getValueFactory();
        
        // generate artifact and write to file
        final ArtifactGenerator generator = new ArtifactGenerator(nextRepositoryConnection, valueFac);
        generator.createArtifact(isPlant, filePrefix, maxResources, i, isDeep, fileType);
        
        // clean up
        nextRepositoryConnection.close();
        inMemoryRepository.shutDown();
    }
    
    public ArtifactGenerator(final RepositoryConnection nextRepositoryConnection, final ValueFactory nextValueFactory)
    {
        this.repositoryConnection = nextRepositoryConnection;
        this.valueFactory = nextValueFactory;
    }
    
    public final void createArtifact(final boolean isPlant, final String filePrefix, final int maxResources,
            final int index, final boolean isDeep, final FILE_TYPES fileType) throws Exception
    {
        // set the namespaces
        // this.repositoryConnection.setNamespace(OWL.PREFIX, OWL.NAMESPACE);
        // this.repositoryConnection.setNamespace(RDFS.PREFIX, RDFS.NAMESPACE);
        this.repositoryConnection.setNamespace("rdf", RDF.NAMESPACE);
        this.repositoryConnection.setNamespace("rdfs", RDFS.NAMESPACE);
        this.repositoryConnection.setNamespace("owl", OWL.NAMESPACE);
        
        // create ontology (i.e. artifact)
        final URI artifactURI = this.valueFactory.createURI("urn:temp:artifact:" + index);
        final URI versionIRI = this.valueFactory.createURI("urn:temp:artifact:version:1");
        this.repositoryConnection.add(artifactURI, RDF.TYPE, OWL.ONTOLOGY);
        
        final URI owlVersionIRI = this.valueFactory.createURI(OWL.NAMESPACE, "versionIRI");
        this.repositoryConnection.add(artifactURI, owlVersionIRI, versionIRI);
        
        long objectCount = 0;
        if(isPlant)
        {
            objectCount = this.createPlantArtifact(maxResources, index, artifactURI);
        }
        else
        // science
        {
            // additional namespaces
            this.repositoryConnection.setNamespace("poddBase", ArtifactGenerator.PODD_BASE);
            this.repositoryConnection.setNamespace("poddScience", ArtifactGenerator.PODD_SCIENCE);
            
            // import PoddBase and PoddScience
            this.repositoryConnection.add(artifactURI, OWL.IMPORTS,
                    this.valueFactory.createURI("http://purl.org/podd/ns/poddBase"));
            this.repositoryConnection.add(artifactURI, OWL.IMPORTS,
                    this.valueFactory.createURI("http://purl.org/podd/ns/poddScience"));
            
            objectCount = this.createScienceArtifact(artifactURI, maxResources, isDeep, index);
        }
        
        // dump the RDF to a file
        String filename = null;
        FileOutputStream fos = null;
        RDFHandler rdfWriter = null;
        if(fileType == FILE_TYPES.RDFXML)
        {
            filename = filePrefix + index + ".rdf";
            fos = new FileOutputStream(ArtifactGenerator.filePath + filename);
            rdfWriter = new RDFXMLWriter(fos);
        }
        else if(fileType == FILE_TYPES.NTRIPLES)
        {
            filename = filePrefix + index + ".nt";
            fos = new FileOutputStream(ArtifactGenerator.filePath + filename);
            rdfWriter = new NTriplesWriter(fos);
        }
        else
        {
            System.out.println("Unsupported file type: " + fileType);
        }
        this.repositoryConnection.export(rdfWriter);
        this.repositoryConnection.commit();
        
        System.out.println("Created " + filename);
        System.out.println("  No. of triples = " + this.repositoryConnection.size());
        System.out.println("  No. of objects = " + objectCount);
    }
    
    private long createPlantArtifact(final int maxResources, final int index, final URI artifactURI)
        throws RepositoryException
    {
        // import Plant ontology
        final URI plantOntology = this.valueFactory.createURI(ArtifactGenerator.PLANT_PREFIX + "po.owl");
        this.repositoryConnection.add(artifactURI, OWL.IMPORTS, plantOntology);
        
        // add objects
        final URI objPO_0025131 = this.valueFactory.createURI(ArtifactGenerator.PLANT_PREFIX + "PO_0025131");
        
        URI previous = objPO_0025131;
        
        for(int i = 0; i < maxResources; i++)
        {
            final URI individual = this.valueFactory.createURI("urn:temp:PlantAnatomicalEntity:" + i);
            this.repositoryConnection.add(individual, RDF.TYPE, objPO_0025131);
            this.repositoryConnection.add(individual, ArtifactGenerator.adjacentTo, previous);
            this.repositoryConnection.add(individual, RDFS.LABEL,
                    this.valueFactory.createLiteral("Plant " + index + " anatomical entity " + i));
            previous = individual;
        }
        this.repositoryConnection.commit();
        return maxResources;
    }
    
    /**
     * Create a PODD Science artifact object and saves it in RDF/XML format.
     * 
     * @param filePath
     *            Location where file should be created
     * @param filePrefix
     *            File name prefix
     * @param index
     *            Numeric end to the file name
     * @return number of objects created
     * @throws Exception
     */
    public final long createScienceArtifact(final URI artifactURI, final int maxResources, final boolean isDeep,
            final int index) throws Exception
    {
        // add a Project as Top Object
        final URI projectURI = this.addPoddProject(index, this.repositoryConnection, this.valueFactory, artifactURI);
        
        final List<URI> resourceList =
                this.addPoddInternalObjects(this.repositoryConnection, this.valueFactory, 200, projectURI,
                        maxResources, isDeep);
        
        if(!isDeep)
        {
            for(final URI resourceURI : resourceList)
            {
                this.repositoryConnection.add(projectURI, ArtifactGenerator.contains, resourceURI);
            }
        }
        
        this.repositoryConnection.commit();
        
        return resourceList.size();
    }
    
    /**
     * Adds a new Project object as the "Top Object"
     * 
     * @param index
     * @param nextRepositoryConnection
     * @param valueFac
     * @param artifactURI
     *            URI of the ontology/artifact
     * @return URI of the new Project
     * @throws RepositoryException
     */
    private URI addPoddProject(final int index, final RepositoryConnection nextRepositoryConnection,
            final ValueFactory valueFac, final URI artifactURI) throws RepositoryException
    {
        final URI topObject = valueFac.createURI("urn:temp:object:" + (2222 + index));
        final URI poddProject = valueFac.createURI(ArtifactGenerator.PODD_SCIENCE, "Project");
        
        final URI artifactHasTopObject = valueFac.createURI(ArtifactGenerator.PODD_BASE + "artifactHasTopObject");
        nextRepositoryConnection.add(artifactURI, artifactHasTopObject, topObject);
        
        nextRepositoryConnection.add(topObject, RDF.TYPE, poddProject);
        nextRepositoryConnection.add(topObject, RDF.TYPE, OWL.THING);
        nextRepositoryConnection.add(topObject, RDFS.LABEL,
                valueFac.createLiteral("Project007 very large science artifact"));
        final URI hasLeadInstitution = valueFac.createURI(ArtifactGenerator.PODD_BASE + "hasLeadInstitution");
        nextRepositoryConnection.add(topObject, hasLeadInstitution, valueFac.createLiteral("UQ", XMLSchema.STRING));
        
        // NOTE: Principal Investigator, Publication Status, Top Object status etc. is not being
        // added.
        
        return topObject;
    }
    
    /**
     * Create objects for all sub-classes of PoddInternalObject that are found in PoddScience.
     * 
     * @param nextRepositoryConnection
     * @param valueFac
     * @param repeats
     *            How many objects of each class to create
     * @param maxResources
     *            Make up to this number of new resources
     * @return A List containing URIs of all the created objects
     * 
     * @throws Exception
     * @throws RepositoryException
     */
    private List<URI> addPoddInternalObjects(final RepositoryConnection nextRepositoryConnection,
            final ValueFactory valueFac, final int repeats, final URI projectURI, final int maxResources,
            final boolean isDeep) throws Exception, RepositoryException
    {
        final URI poddInternalObject = valueFac.createURI(ArtifactGenerator.PODD_BASE, "PoddInternalObject");
        final RepositoryConnection tempRepositoryConnection =
                ArtifactGenerator.getRepositoryConnection(ArtifactGenerator.poddSciencePath);
        
        final List<Statement> poddInternalSubTypes =
                tempRepositoryConnection.getStatements(null, RDFS.SUBCLASSOF, poddInternalObject, false).asList();
        final int noOfSubTypes = poddInternalSubTypes.size();
        URI parent = projectURI;
        
        int j = 0;
        final List<URI> resourceList = new ArrayList<URI>();
        while(resourceList.size() < maxResources)
        {
            final Resource subType = poddInternalSubTypes.get(j % noOfSubTypes).getSubject();
            final String subTypeName = subType.stringValue();
            if(subTypeName.lastIndexOf("/") > 0)
            {
                final URI randomObj = valueFac.createURI("urn:poddinternal:" + UUID.randomUUID().toString() + ":" + j);
                resourceList.add(randomObj);
                nextRepositoryConnection.add(randomObj, RDF.TYPE, subType);
                nextRepositoryConnection.add(randomObj, RDFS.LABEL,
                        valueFac.createLiteral(subTypeName.substring(subTypeName.lastIndexOf("/") + 1) + " " + j));
                if(isDeep)
                {
                    nextRepositoryConnection.add(randomObj, ArtifactGenerator.containedBy, parent);
                }
                parent = randomObj;
                if(resourceList.size() >= maxResources)
                {
                    return resourceList;
                }
            }
            j++;
        }
        
        return resourceList;
    }
    
    // ----- helper methods -----
    
    public static RepositoryConnection getRepositoryConnection(final String ontologyResource) throws Exception
    {
        // create repository
        final Repository inMemoryRepository = new SailRepository(new MemoryStore());
        inMemoryRepository.initialize();
        final RepositoryConnection tempRepositoryConnection = inMemoryRepository.getConnection();
        tempRepositoryConnection.setAutoCommit(false);
        if(ontologyResource != null)
        {
            tempRepositoryConnection.add(new Object().getClass().getResourceAsStream(ontologyResource), "",
                    RDFFormat.RDFXML);
        }
        
        return tempRepositoryConnection;
    }
}
