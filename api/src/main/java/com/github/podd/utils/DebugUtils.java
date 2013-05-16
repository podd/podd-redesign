/**
 * 
 */
package com.github.podd.utils;

import info.aduna.iteration.Iterations;

import java.util.List;

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
            System.out.println("   {" + stmt.getSubject() + "}   <" + stmt.getPredicate() + ">  {" + stmt.getObject()
                    + "}");
        }
        System.out.println("==================================================");
    }
    
    /**
     * Helper method prints the contents of the given context of a Repository
     */
    public static void printContents(final RepositoryConnection conn, final URI context) throws RepositoryException
    {
        System.out.println("==================================================");
        System.out.println("Graph = " + context);
        System.out.println();
        final List<Statement> repoResults = Iterations.asList(conn.getStatements(null, null, null, false, context));
        for(final Statement stmt : repoResults)
        {
            System.out.println("   {" + stmt.getSubject() + "}   <" + stmt.getPredicate() + ">  {" + stmt.getObject()
                    + "}");
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
