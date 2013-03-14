/**
 * 
 */
package com.github.podd.utils;

import info.aduna.iteration.Iterations;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kutila
 * 
 */
public class RdfUtility
{
    
    private final static Logger log = LoggerFactory.getLogger(RdfUtility.class);
    
    /**
     * Given an artifact, this method attempts to validate that all objects are connected in a
     * hierarchy to the top object.
     * 
     * @param inputStream
     *            Input stream containing the artifact statements
     * @param format
     *            The RDF format in which the statements are provided
     * @return True if the artifact is structurally valid, false otherwise
     */
    public static boolean isConnectedStructure(final InputStream inputStream, RDFFormat format)
    {
        if(inputStream == null)
        {
            throw new NullPointerException("Input stream must not be null");
        }
        
        if(format == null)
        {
            format = RDFFormat.RDFXML;
        }
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:concrete:random");
        
        Repository tempRepository = null;
        RepositoryConnection connection = null;
        
        try
        {
            // create a temporary in-memory repository
            tempRepository = new SailRepository(new MemoryStore());
            tempRepository.initialize();
            connection = tempRepository.getConnection();
            connection.begin();
            
            // load artifact statements into repository
            connection.add(inputStream, "", format, context);
            // DebugUtils.printContents(connection, context);
            
            // - find artifact and top object URIs
            final List<Statement> topObjects =
                    Iterations.asList(connection.getStatements(null, PoddRdfConstants.PODDBASE_HAS_TOP_OBJECT, null,
                            false, context));
            
            if(topObjects.size() != 1)
            {
                RdfUtility.log.info("Artifact should have exactly 1 Top Object");
                return false;
            }
            
            final URI topObject = (URI)topObjects.get(0).getObject();
            final URI artifactUri = (URI)topObjects.get(0).getSubject();
            
            final List<URI> exclusions =
                    Arrays.asList(new URI[] { 
                            artifactUri, 
                            topObject, 
                            OWL.THING, 
                            OWL.ONTOLOGY, 
                            OWL.INDIVIDUAL,
                            ValueFactoryImpl.getInstance().createURI("http://www.w3.org/2002/07/owl#NamedIndividual"),
                            });
            
            
            // - identify the potential PODD objects to check for connectivity
            final Set<URI> potentialPoddObjects = new HashSet<URI>();

            List<Statement> allStatements = Iterations.asList(connection.getStatements(null, null, null, false, context));
            for (Statement s: allStatements)
            {
                final Value objectValue = s.getObject();
                if(objectValue instanceof URI && !exclusions.contains(objectValue))
                {
                    potentialPoddObjects.add((URI)objectValue);
                }
                
                final Value subjectValue = s.getSubject();
                if(subjectValue instanceof URI && !exclusions.contains(subjectValue))
                {
                    potentialPoddObjects.add((URI)subjectValue);
                }
            }
            
            RdfUtility.log.info("{} Objects to check for connectivity.", potentialPoddObjects.size());
            for(final URI u : potentialPoddObjects)
            {
                System.out.println("    " + u);
            }
            
            // - check for connectivity
            final Queue<URI> queue = new LinkedList<URI>();
            queue.add(artifactUri);
            
            while(!queue.isEmpty())
            {
                final URI currentNode = queue.remove();
                
                final List<URI> children = RdfUtility.getChildren(currentNode, connection, context);
                for(final URI child : children)
                {
                    // visit child node
                    if(potentialPoddObjects.contains(child))
                    {
                        potentialPoddObjects.remove(child);
                        if(potentialPoddObjects.isEmpty())
                        {
                            // all potential PoddObjects are connected.
                            return true;
                        }
                    }
                    queue.add(child);
                }
            }
            RdfUtility.log.info("{} unconnected object(s). {}", potentialPoddObjects.size(), potentialPoddObjects);
            return false;
        }
        catch(final Exception e)
        {
            // better to throw an exception containing error details
            RdfUtility.log.error("An exception in validateArtifactConnectedness() ", e);
            return false;
        }
        finally
        {
            try
            {
                if(connection != null && connection.isOpen())
                {
                    connection.rollback();
                    connection.close();
                }
                tempRepository.shutDown();
            }
            catch(final Exception e)
            {
                RdfUtility.log.error("Exception while releasing resources", e);
            }
        }
    }
    
    private static List<URI> getChildren(final URI node, final RepositoryConnection connection, final URI... context)
        throws Exception
    {
        final List<URI> children = new ArrayList<URI>();
        final List<Statement> childStatements =
                Iterations.asList(connection.getStatements(node, null, null, false, context));
        for(final Statement s : childStatements)
        {
            if(s.getObject() instanceof URI)
            {
                children.add((URI)s.getObject());
            }
        }
        return children;
    }
    
}
