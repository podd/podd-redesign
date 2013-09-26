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
package com.github.podd.utils;

import info.aduna.iteration.Iterations;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.resultio.helpers.QueryResultCollector;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
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
     * Given a set of RDF Statements, and a Root node, this method finds any nodes that are not
     * connected to the Root node.
     * 
     * A <b>Node</b> is a Value that is of type URI (i.e. Literals are ignored).
     * 
     * A direct connection between two nodes exist if there is a Statement with the two nodes as the
     * Subject and the Object.
     * 
     * @param root
     *            The Root of the Graph, from which connectedness is calculated.
     * @param connection
     *            A RepositoryConnection
     * @param context
     *            The Graph containing statements.
     * @return A <code>Set</code> containing any URIs that are not connected to the Root.
     * @throws RepositoryException
     */
    public static Set<URI> findDisconnectedNodes(final URI root, final RepositoryConnection connection,
            final URI... context) throws RepositoryException
    {
        final List<URI> exclusions =
                Arrays.asList(new URI[] { root, OWL.THING, OWL.ONTOLOGY, OWL.INDIVIDUAL,
                        ValueFactoryImpl.getInstance().createURI("http://www.w3.org/2002/07/owl#NamedIndividual"), });
        
        // - identify nodes that should be connected to the root
        final Set<URI> nodesToCheck = new HashSet<URI>();
        
        final List<Statement> allStatements =
                Iterations.asList(connection.getStatements(null, null, null, false, context));
        for(final Statement s : allStatements)
        {
            final Value objectValue = s.getObject();
            if(objectValue instanceof URI && !exclusions.contains(objectValue))
            {
                nodesToCheck.add((URI)objectValue);
            }
            
            final Value subjectValue = s.getSubject();
            if(subjectValue instanceof URI && !exclusions.contains(subjectValue))
            {
                nodesToCheck.add((URI)subjectValue);
            }
        }
        
        // RdfUtility.log.info("{} nodes to check for connectivity.", nodesToCheck.size());
        // for(final URI u : objectsToCheck)
        // {
        // System.out.println("    " + u);
        // }
        
        // - check for connectivity
        final Queue<URI> queue = new LinkedList<URI>();
        final Set<URI> visitedNodes = new HashSet<URI>(); // to handle cycles
        queue.add(root);
        visitedNodes.add(root);
        
        while(!queue.isEmpty())
        {
            final URI currentNode = queue.remove();
            
            final List<URI> children = RdfUtility.getImmediateChildren(currentNode, connection, context);
            for(final URI child : children)
            {
                // visit child node
                if(nodesToCheck.contains(child))
                {
                    nodesToCheck.remove(child);
                    if(nodesToCheck.isEmpty())
                    {
                        // all identified nodes are connected.
                        return nodesToCheck;
                    }
                }
                if(!visitedNodes.contains(child))
                {
                    queue.add(child);
                    visitedNodes.add(child);
                }
            }
        }
        RdfUtility.log.debug("{} unconnected node(s). {}", nodesToCheck.size(), nodesToCheck);
        return nodesToCheck;
    }
    
    /**
     * Internal helper method to retrieve the direct child objects of a given object.
     * 
     * @param node
     * @param connection
     * @param context
     * @return
     * @throws RepositoryException
     */
    private static List<URI> getImmediateChildren(final URI node, final RepositoryConnection connection,
            final URI... context) throws RepositoryException
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
    
    /**
     * Helper method to load an {@link InputStream} into an {@link Model}.
     * 
     * @param resourceStream
     *            The input stream with RDF statements
     * @param format
     *            Format found in the input RDF data
     * @return an {@link Model} populated with the statements from the input stream.
     * 
     * @throws OpenRDFException
     * @throws IOException
     */
    public static Model inputStreamToModel(final InputStream resourceStream, final RDFFormat format)
        throws OpenRDFException, IOException
    {
        if(resourceStream == null)
        {
            throw new IOException("Inputstream was null");
        }
        
        final Model concreteModel = new LinkedHashModel();
        final RDFParser parser = Rio.createParser(format);
        parser.setRDFHandler(new StatementCollector(concreteModel));
        parser.parse(resourceStream, "");
        
        return concreteModel;
    }
    
    /**
     * Given an artifact, this method evaluates whether all Objects within the artifact are
     * connected to the Top Object.
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
            
            return RdfUtility.isConnectedStructure(connection, context);
            
        }
        catch(final Exception e)
        {
            // better to throw an exception containing error details
            RdfUtility.log.error("An exception in checking connectedness of artifact", e);
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
    
    /**
     * Given an artifact, this method evaluates whether all Objects within the artifact are
     * connected to the Top Object.
     * 
     * @param connection
     *            The RepositoryConnection
     * @param context
     *            The Context within the RepositoryConnection.
     * @return True if all internal objects are connected to the top object, false otherwise.
     * @throws RepositoryException
     */
    public static boolean isConnectedStructure(final RepositoryConnection connection, final URI... context)
        throws RepositoryException
    {
        // - find artifact and top object URIs
        final List<Statement> topObjects =
                Iterations.asList(connection.getStatements(null, PoddRdfConstants.PODD_BASE_HAS_TOP_OBJECT, null,
                        false, context));
        
        if(topObjects.size() != 1)
        {
            RdfUtility.log.info("Artifact should have exactly 1 Top Object");
            return false;
        }
        
        final URI artifactUri = (URI)topObjects.get(0).getSubject();
        
        final Set<URI> disconnectedNodes = RdfUtility.findDisconnectedNodes(artifactUri, connection, context);
        if(disconnectedNodes == null || disconnectedNodes.isEmpty())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Helper method to execute a given SPARQL Graph query.
     * 
     * @param graphQuery
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    public static Model executeGraphQuery(final GraphQuery graphQuery, final URI... contexts) throws OpenRDFException
    {
        final DatasetImpl dataset = new DatasetImpl();
        for(final URI uri : contexts)
        {
            dataset.addDefaultGraph(uri);
        }
        graphQuery.setDataset(dataset);
        final Model results = new LinkedHashModel();
        long before = System.currentTimeMillis();
        graphQuery.evaluate(new StatementCollector(results));
        long total = System.currentTimeMillis() - before;
        log.debug("graph query took {}", Long.toString(total));
        if(total > 50 && log.isDebugEnabled())
        {
            new Throwable().printStackTrace();
        }
        else if(total > 30 && log.isTraceEnabled())
        {
            new Throwable().printStackTrace();
        }
        
        return results;
    }
    
    /**
     * Helper method to execute a given SPARQL Tuple query, which may have had bindings attached.
     * 
     * @param tupleQuery
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    public static QueryResultCollector executeTupleQuery(final TupleQuery tupleQuery, final URI... contexts)
        throws OpenRDFException
    {
        final DatasetImpl dataset = new DatasetImpl();
        for(final URI uri : contexts)
        {
            dataset.addDefaultGraph(uri);
        }
        tupleQuery.setDataset(dataset);
        
        final QueryResultCollector results = new QueryResultCollector();
        long before = System.currentTimeMillis();
        QueryResults.report(tupleQuery.evaluate(), results);
        long total = System.currentTimeMillis() - before;
        log.debug("tuple query took {}", Long.toString(total));
        if(total > 50 && log.isDebugEnabled())
        {
            new Throwable().printStackTrace();
        }
        else if(total > 30 && log.isTraceEnabled())
        {
            new Throwable().printStackTrace();
        }
        
        return results;
    }
    
}
