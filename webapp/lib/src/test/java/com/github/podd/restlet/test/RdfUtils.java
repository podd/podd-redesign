/**
 * 
 */
package com.github.podd.restlet.test;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

/**
 * This class contains static methods to help debug contents of an RDF repository
 * 
 * @author kutila
 *
 */
public class RdfUtils
{
    
    /**
     * Helper method prints the contents of the given context of a Repository
     */
    public static void printContents(Repository repository, final URI context) throws Exception
    {
        final RepositoryConnection conn = repository.getConnection();
        conn.begin();
        
        System.out.println("==================================================");
        System.out.println("Graph = " + context);
        System.out.println();
        final org.openrdf.repository.RepositoryResult<Statement> repoResults =
                conn.getStatements(null, null, null, false, context);
        while(repoResults.hasNext())
        {
            final Statement stmt = repoResults.next();
            System.out.println("   {" + stmt.getSubject() + "}   <" + stmt.getPredicate() + ">  {" + stmt.getObject()
                    + "}");
        }
        System.out.println("==================================================");
        
        conn.rollback();
        conn.close();
    }
    
    /**
     * Helper method prints the contents of the given context of a Repository
     */
    public static void printContexts(Repository repository) throws Exception
    {
        final java.util.HashSet<String> contextSet = new java.util.HashSet<String>();
        
        final RepositoryConnection conn = repository.getConnection();
        conn.begin();
        final org.openrdf.repository.RepositoryResult<Statement> repoResults =
                conn.getStatements(null, null, null, true);
        while(repoResults.hasNext())
        {
            final Statement stmt = repoResults.next();
            contextSet.add(stmt.getContext().stringValue());
        }
        
        System.out.println("==================================================");
        System.out.println("Contexts in Repository:  ");
        for(final String context : contextSet)
        {
            System.out.println(context);
        }
        System.out.println("==================================================");
        
        conn.rollback();
        conn.close();
    }
    
    
}
