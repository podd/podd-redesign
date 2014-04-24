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

import java.util.List;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * This class contains static methods to help debug contents of an RDF repository
 *
 * @author kutila
 *
 */
public class DebugUtils
{

    public static void printContents(final Model model)
    {
        System.out.println("==================================================");
        System.out.println("Model Contents: ");
        System.out.println();
        final Statement[] allStatements = model.toArray(new Statement[0]);

        for(final Statement stmt : allStatements)
        {
            if(stmt.getContext() == null)
            {
                System.out.println("   {" + stmt.getSubject() + "}   <" + stmt.getPredicate() + ">  {"
                        + stmt.getObject() + "}");
            }
            else
            {
                System.out.println("   {" + stmt.getSubject() + "}   <" + stmt.getPredicate() + ">  {"
                        + stmt.getObject() + "} [" + stmt.getContext() + "]");
            }
        }
        System.out.println("==================================================");
    }

    /**
     * Helper method prints the contents of the given context of a Repository
     */
    public static void printContents(final RepositoryConnection conn, final URI... contexts) throws RepositoryException
    {
        OpenRDFUtil.verifyContextNotNull(contexts);
        for(final URI context : contexts)
        {
            System.out.println("==================================================");
            System.out.println("Graph = " + context);
            System.out.println();
            final List<Statement> repoResults = Iterations.asList(conn.getStatements(null, null, null, false, context));
            for(final Statement stmt : repoResults)
            {
                System.out.println("   {" + stmt.getSubject() + "}   <" + stmt.getPredicate() + ">  {"
                        + stmt.getObject() + "}");
            }
            System.out.println("==================================================");
        }
    }

    /**
     * Helper method prints the contents of the given context of a Model
     */
    public static void printContexts(final Model model) throws RepositoryException
    {
        System.out.println("==================================================");
        System.out.println("Contexts in Model:  ");
        for(final Resource context : model.contexts())
        {
            System.out.println(context);
        }
        System.out.println("==================================================");
    }

    /**
     * Helper method prints the contents of the given context of a Repository
     */
    public static void printContexts(final RepositoryConnection conn) throws RepositoryException
    {
        System.out.println("==================================================");
        System.out.println("Contexts in Repository:  ");
        for(final Resource context : Iterations.asSet(conn.getContextIDs()))
        {
            System.out.println(context);
        }
        System.out.println("==================================================");
    }

}
