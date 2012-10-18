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
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
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
    
    private static final String PODD_SCIENCE = "http://purl.org/podd/ns/poddScience#";
    private static final String PODD_BASE = "http://purl.org/podd/ns/poddBase#";
    
    private static final String poddBasePath = "/ontologies/poddBase.owl";
    private static final String poddSciencePath = "/ontologies/poddScience.owl";
    private static final String poddAnimalPath = "/ontologies/poddAnimal.owl";
    
    private static final URI contains = IRI.create(ArtifactGenerator.PODD_BASE, "contains").toOpenRDFURI();
    
    /**
     * @param args
     */
    public static void main(final String[] args) throws Exception
    {
        final String filePath = "/home/kutila/gitrepos/podd-redesign/podd-prototype/src/test/resources/test/artifacts/";
        
        final ArtifactGenerator generator = new ArtifactGenerator();
        
        final String filePrefix = "largeProject-";
        for(int i = 5; i < 6; i++)
        {
            generator.createPoddArtifact(filePath, filePrefix, i);
            System.out.println("Created " + filePrefix + i + ".rdf");
        }
    }
    
    /**
     * Create a PODD artifact object and saves it in RDF/XML format.
     * 
     * @param filePath
     *            Location where file should be created
     * @param filePrefix
     *            File name prefix
     * @param index
     *            Numeric end to the file name
     * @throws Exception
     */
    public final void createPoddArtifact(final String filePath, final String filePrefix, final int index)
        throws Exception
    {
        // create repository
        final Repository inMemoryRepository = new SailRepository(new MemoryStore());
        inMemoryRepository.initialize();
        final RepositoryConnection nextRepositoryConnection = inMemoryRepository.getConnection();
        nextRepositoryConnection.setAutoCommit(false);
        final ValueFactory valueFac = inMemoryRepository.getValueFactory();
        
        // set namespaces
        nextRepositoryConnection.setNamespace(OWL.PREFIX, OWL.NAMESPACE);
        nextRepositoryConnection.setNamespace(RDFS.PREFIX, RDFS.NAMESPACE);
        nextRepositoryConnection.setNamespace("poddBase", ArtifactGenerator.PODD_BASE);
        nextRepositoryConnection.setNamespace("poddScience", ArtifactGenerator.PODD_SCIENCE);
        
        // add ontology (i.e. artifact) object
        final URI artifactURI = valueFac.createURI("urn:temp:artifact:" + index);
        final URI versionIRI = valueFac.createURI("urn:temp:artifact:version:1");
        final URI owlVersionIRI = valueFac.createURI(OWL.NAMESPACE, "versionIRI");
        nextRepositoryConnection.add(artifactURI, RDF.TYPE, OWL.ONTOLOGY);
        nextRepositoryConnection.add(artifactURI, owlVersionIRI, versionIRI);
        
        // import PoddBase and PoddScience
        nextRepositoryConnection.add(artifactURI, OWL.IMPORTS, valueFac.createURI("http://purl.org/podd/ns/poddBase"));
        nextRepositoryConnection.add(artifactURI, OWL.IMPORTS,
                valueFac.createURI("http://purl.org/podd/ns/poddScience"));
        
        // add a Project as Top Object
        final URI projectURI = this.addPoddProject(index, nextRepositoryConnection, valueFac, artifactURI);
        
        final List<URI> resources = this.addPoddInternalObjects(nextRepositoryConnection, valueFac, 200, projectURI);
        nextRepositoryConnection.commit();
        
        // for(final URI u : resources)
        // {
        // nextRepositoryConnection.add(projectURI, contains, u);
        // }
        nextRepositoryConnection.commit();
        
        // this.printGraph(nextRepositoryConnection);
        
        // dump the RDF to a file
        final FileOutputStream fos = new FileOutputStream(filePath + filePrefix + index + ".rdf");
        final RDFHandler rdfxmlWriter = new RDFXMLWriter(fos);
        nextRepositoryConnection.export(rdfxmlWriter);
        nextRepositoryConnection.commit();
        nextRepositoryConnection.close();
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
                valueFac.createLiteral("Project007 Very very large science artifact"));
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
     * @return A List containing URIs of all the created objects
     * 
     * @throws Exception
     * @throws RepositoryException
     */
    private List<URI> addPoddInternalObjects(final RepositoryConnection nextRepositoryConnection,
            final ValueFactory valueFac, final int repeats, final URI projectURI) throws Exception, RepositoryException
    {
        final URI poddInternalObject = valueFac.createURI(ArtifactGenerator.PODD_BASE, "PoddInternalObject");
        final List<URI> resourceList = new ArrayList<URI>();
        final RepositoryConnection tempRepositoryConnection =
                ArtifactGenerator.getRepositoryConnection(ArtifactGenerator.poddSciencePath);
        final RepositoryResult<Statement> allResults =
                tempRepositoryConnection.getStatements(null, RDFS.SUBCLASSOF, poddInternalObject, false);
        
        URI parent = projectURI;
        while(allResults.hasNext())
        {
            final Statement stmt = allResults.next();
            final Resource subj = stmt.getSubject();
            final String subjStr = subj.stringValue();
            if(subjStr.lastIndexOf("/") > 0)
            {
                for(int i = 0; i < repeats; i++)
                {
                    final URI randomObj =
                            valueFac.createURI("urn:poddinternal:" + UUID.randomUUID().toString() + ":" + i);
                    resourceList.add(randomObj);
                    nextRepositoryConnection.add(randomObj, RDF.TYPE, subj);
                    nextRepositoryConnection.add(randomObj, RDFS.LABEL,
                            valueFac.createLiteral(subjStr.substring(subjStr.lastIndexOf("/") + 1) + " " + i));
                    nextRepositoryConnection.add(parent, ArtifactGenerator.contains, randomObj);
                    parent = randomObj;
                }
            }
        }
        return resourceList;
    }
    
    public final void createPlantArtifact(final String filePath, final String filePrefix, final int index)
        throws Exception
    {
        // create repository
        final Repository inMemoryRepository = new SailRepository(new MemoryStore());
        inMemoryRepository.initialize();
        final RepositoryConnection nextRepositoryConnection = inMemoryRepository.getConnection();
        nextRepositoryConnection.setAutoCommit(false);
        
        // set the namespaces
        nextRepositoryConnection.setNamespace(OWL.PREFIX, OWL.NAMESPACE);
        nextRepositoryConnection.setNamespace(RDFS.PREFIX, RDFS.NAMESPACE);
        
        final ValueFactory valueFac = inMemoryRepository.getValueFactory();
        
        // create ontology (i.e. artifact)
        final URI artifactURI = valueFac.createURI("urn:temp:artifact:" + index);
        final URI versionIRI = valueFac.createURI("urn:temp:artifact:version:1");
        nextRepositoryConnection.add(artifactURI, RDF.TYPE, OWL.ONTOLOGY);
        
        final URI owlVersionIRI = valueFac.createURI(OWL.NAMESPACE, "versionIRI");
        nextRepositoryConnection.add(artifactURI, owlVersionIRI, versionIRI);
        
        // import Plant ontology
        final URI plantOntology = valueFac.createURI("http://purl.obolibrary.org/obo/po.owl");
        nextRepositoryConnection.add(artifactURI, OWL.IMPORTS, plantOntology);
        
        // add objects
        final URI objPO_0025131 = valueFac.createURI("http://purl.obolibrary.org/obo/PO_0025131");
        final URI objNamedIndividual = valueFac.createURI("http://www.w3.org/2002/07/owl#NamedIndividual");
        
        for(int i = 0; i < 40000; i++)
        {
            final URI individual = valueFac.createURI("urn:temp:PlantAnatomicalEntity:" + i);
            nextRepositoryConnection.add(individual, RDF.TYPE, objPO_0025131);
            nextRepositoryConnection.add(individual, RDF.TYPE, objNamedIndividual);
            nextRepositoryConnection.add(individual, RDFS.LABEL,
                    valueFac.createLiteral("Plant " + index + " anatomical entity " + i));
        }
        
        // this.printGraph(nextRepositoryConnection);
        
        // dump the RDF to a file
        final FileOutputStream fos = new FileOutputStream(filePath + filePrefix + index + ".rdf");
        final RDFHandler rdfxmlWriter = new RDFXMLWriter(fos);
        nextRepositoryConnection.export(rdfxmlWriter);
        nextRepositoryConnection.commit();
        nextRepositoryConnection.close();
    }
    
    // ----- helper methods -----
    public static void printOntology(final String ontologyResource) throws Exception
    {
        final RepositoryConnection tempRepositoryConnection =
                ArtifactGenerator.getRepositoryConnection(ontologyResource);
        
        final RepositoryResult<Statement> allStatements =
                tempRepositoryConnection.getStatements(null, null, null, false);
        while(allStatements.hasNext())
        {
            final Statement stmt = allStatements.next();
            System.out.print(stmt.getSubject());
            System.out.print("  " + stmt.getPredicate());
            System.out.println("  " + stmt.getObject());
        }
        allStatements.close();
    }
    
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
    
    private void printGraph(final RepositoryConnection repositoryConnection)
    {
        org.openrdf.repository.RepositoryResult<Statement> results;
        try
        {
            results = repositoryConnection.getStatements(null, null, null, false);
            while(results.hasNext())
            {
                final Statement triple = results.next();
                final String tripleStr = java.net.URLDecoder.decode(triple.toString(), "UTF-8");
                System.out.println(" --- " + tripleStr);
            }
        }
        catch(final Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
