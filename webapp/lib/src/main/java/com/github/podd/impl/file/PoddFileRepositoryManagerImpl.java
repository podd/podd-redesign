/**
 * 
 */
package com.github.podd.impl.file;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.resultio.helpers.QueryResultCollector;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.api.file.PoddFileRepositoryManager;
import com.github.podd.exception.FileRepositoryMappingExistsException;
import com.github.podd.exception.FileRepositoryMappingNotFoundException;
import com.github.podd.exception.PoddException;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.PoddRdfConstants;

/**
 * An implementation of FileRepositoryManager which uses an RDF repository graph as the back-end
 * storage for maintaining information about Repository Configurations.
 * 
 * @author kutila
 */
public class PoddFileRepositoryManagerImpl implements PoddFileRepositoryManager
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private PoddRepositoryManager repositoryManager;

    /**
     * 
     */
    public PoddFileRepositoryManagerImpl() 
    {
    }

    @Override
    public void setRepositoryManager(PoddRepositoryManager repositoryManager)
    {
        this.repositoryManager = repositoryManager;
    }

    @Override
    public PoddRepositoryManager getRepositoryManager()
    {
        return this.repositoryManager;
    }
    
    @Override
    public void addRepositoryMapping(String alias, PoddFileRepository<?> repositoryConfiguration)
        throws OpenRDFException, FileRepositoryMappingExistsException
    {
        this.addRepositoryMapping(alias, repositoryConfiguration, false);
    }    
    
    @Override
    public void addRepositoryMapping(String alias, PoddFileRepository<?> repositoryConfiguration, boolean overwrite)
        throws OpenRDFException, FileRepositoryMappingExistsException
    {
        // - TODO: validate FileRepository configuration
        if (repositoryConfiguration == null || alias == null)
        {
            throw new NullPointerException("Cannot add NULL as a File Repository mapping");
        }

        // - check if a mapping with this alias already exists
        try
        {
            if (this.getRepository(alias) != null)
            {
                if (overwrite)
                {
                    this.removeRepositoryMapping(alias);
                }
                else
                {
                    throw new FileRepositoryMappingExistsException(alias, "File Repository mapping with this alias already exists");
                }
            }
        }
        catch (FileRepositoryMappingNotFoundException e)
        {
            // mapping doesn't exist, we can proceed to add a new one.
        }
        
        URI context = this.repositoryManager.getFileRepositoryManagementGraph(); 
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();

            Model model = repositoryConfiguration.getAsModel();
            conn.add(model, context);
            conn.commit();
        }
        catch (Exception e)
        {
            conn.rollback();
            throw e;
        }
        finally
        {
            try
            {
                if(conn != null && conn.isActive())
                {
                    conn.rollback();
                }
                if(conn != null && conn.isOpen())
                {
                    conn.close();
                }
            }
            catch(RepositoryException e)
            {
                this.log.warn("Failed to close RepositoryConnection", e);
            }
        }
    }

    @Override
    public PoddFileRepository<?> getRepository(String alias) throws FileRepositoryMappingNotFoundException, OpenRDFException
    {
        if(alias == null)
        {
            throw new FileRepositoryMappingNotFoundException(alias, "NULL is not a mapped File Repository");
        }
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            URI context = this.repositoryManager.getFileRepositoryManagementGraph(); 

            final StringBuilder sb = new StringBuilder();
            
            sb.append("CONSTRUCT { ");
            sb.append(" ?aliasUri ?predicate ?object . ");
            
            sb.append(" } WHERE { ");
            
            sb.append(" ?aliasUri ?predicate ?object . ");
            sb.append(" ?aliasUri <" + RDF.TYPE.stringValue() + "> <" + PoddRdfConstants.PODD_FILE_REPOSITORY.stringValue() + "> .");
            sb.append(" ?aliasUri <" + PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS.stringValue() + "> ?alias .");
            
            // TODO: filter out other aliases
            // sb.append(" ?aliasUri <" + PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS.stringValue() + "> ?otherAlias . ");
            // sb.append("  ?otherAlias != ?alias . ");

            sb.append(" } ");
            
            this.log.info("Created SPARQL {} with alias bound to {}", sb.toString(), alias);
            
            final GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
            query.setBinding("alias", ValueFactoryImpl.getInstance().createLiteral(alias));
            
            final Model queryResults = this.executeGraphQuery(query, context);            
            
            DebugUtils.printContents(conn, context);
            DebugUtils.printContents(queryResults);
            
            // TODO - how to construct a FileRepository object from this Model. Use a FileRepositoryFactory or reuse FileReferenceProcessors?
            return null;
        }
        finally
        {
                if(conn != null && conn.isActive())
                {
                    conn.rollback();
                }
                if(conn != null && conn.isOpen())
                {
                    conn.close();
                }
        }
    }

    @Override
    public PoddFileRepository<?> removeRepositoryMapping(String alias) throws FileRepositoryMappingNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getRepositoryAliases(PoddFileRepository<?> repositoryConfiguration)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void verifyFileReferences(Set<FileReference> fileReferenceResults) throws OpenRDFException,
        PoddException, FileRepositoryMappingNotFoundException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void downloadFileReference(FileReference nextFileReference, OutputStream outputStream)
        throws PoddException, IOException
    {
        // TODO Auto-generated method stub
        
    }
    
    
    /**
     * Helper method to execute a given SPARQL Tuple query, which may have had bindings attached.
     * 
     * @param sparqlQuery
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    private QueryResultCollector executeSparqlQuery(final TupleQuery sparqlQuery, final URI... contexts)
        throws OpenRDFException
    {
        final DatasetImpl dataset = new DatasetImpl();
        for(final URI uri : contexts)
        {
            dataset.addDefaultGraph(uri);
        }
        sparqlQuery.setDataset(dataset);
        
        QueryResultCollector results = new QueryResultCollector();
        QueryResults.report(sparqlQuery.evaluate(), results);
        
        return results;
    }
    
    /**
     * Helper method to execute a given SPARQL Graph query.
     * 
     * @param sparqlQuery
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    private Model executeGraphQuery(final GraphQuery sparqlQuery, final URI... contexts) throws OpenRDFException
    {
        final DatasetImpl dataset = new DatasetImpl();
        for(final URI uri : contexts)
        {
            dataset.addDefaultGraph(uri);
        }
        sparqlQuery.setDataset(dataset);
        Model results = new LinkedHashModel();
        sparqlQuery.evaluate(new StatementCollector(results));
        
        return results;
    }
    
}
