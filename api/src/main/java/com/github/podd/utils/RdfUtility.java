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
import org.openrdf.model.Resource;
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
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
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
        final long before = System.currentTimeMillis();
        graphQuery.evaluate(new StatementCollector(results));
        final long total = System.currentTimeMillis() - before;
        RdfUtility.log.debug("graph query took {}", Long.toString(total));
        if(total > 50 && RdfUtility.log.isDebugEnabled())
        {
            new Throwable().printStackTrace();
        }
        else if(total > 30 && RdfUtility.log.isTraceEnabled())
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
        final long before = System.currentTimeMillis();
        QueryResults.report(tupleQuery.evaluate(), results);
        final long total = System.currentTimeMillis() - before;
        RdfUtility.log.debug("tuple query took {}", Long.toString(total));
        if(total > 50 && RdfUtility.log.isDebugEnabled())
        {
            new Throwable().printStackTrace();
        }
        else if(total > 30 && RdfUtility.log.isTraceEnabled())
        {
            new Throwable().printStackTrace();
        }

        return results;
    }

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
                Arrays.asList(root, OWL.THING, OWL.ONTOLOGY, OWL.INDIVIDUAL,
                        ValueFactoryImpl.getInstance().createURI("http://www.w3.org/2002/07/owl#NamedIndividual"));

        final List<URI> propertyExclusions = Arrays.asList(OWL.IMPORTS, OWL.VERSIONIRI);

        // - identify nodes that should be connected to the root
        final Set<URI> nodesToCheck = new HashSet<URI>();

        final List<Statement> allStatements =
                Iterations.asList(connection.getStatements(null, null, null, false, context));
        for(final Statement s : allStatements)
        {
            final URI predicateValue = s.getPredicate();
            if(propertyExclusions.contains(predicateValue))
            {
                continue;
            }

            final Value objectValue = s.getObject();
            if(objectValue instanceof URI && !exclusions.contains(objectValue))
            {
                nodesToCheck.add((URI)objectValue);
            }

            final Resource subjectValue = s.getSubject();
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
        return Rio.parse(resourceStream, "", format);
    }

}
