/**
 * 
 */
package com.github.podd.impl.file;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
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
import com.github.podd.exception.FileRepositoryException;
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
    public void setRepositoryManager(final PoddRepositoryManager repositoryManager)
    {
        this.repositoryManager = repositoryManager;
    }
    
    @Override
    public PoddRepositoryManager getRepositoryManager()
    {
        return this.repositoryManager;
    }
    
    @Override
    public void addRepositoryMapping(final String alias, final PoddFileRepository<?> repositoryConfiguration)
        throws OpenRDFException, FileRepositoryException
    {
        this.addRepositoryMapping(alias, repositoryConfiguration, false);
    }
    
    @Override
    public void addRepositoryMapping(final String alias, final PoddFileRepository<?> repositoryConfiguration,
            final boolean overwrite) throws OpenRDFException, FileRepositoryException
    {
        // - TODO: validate FileRepository configuration
        if(repositoryConfiguration == null || alias == null)
        {
            throw new NullPointerException("Cannot add NULL as a File Repository mapping");
        }
        
        // - check if a mapping with this alias already exists
        if(this.getRepository(alias) != null)
        {
            if(overwrite)
            {
                this.removeRepositoryMapping(alias);
            }
            else
            {
                throw new FileRepositoryMappingExistsException(alias,
                        "File Repository mapping with this alias already exists");
            }
        }
        
        final URI context = this.repositoryManager.getFileRepositoryManagementGraph();
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            final Model model = repositoryConfiguration.getAsModel();
            conn.add(model, context);
            conn.commit();
        }
        catch(final Exception e)
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
            catch(final RepositoryException e)
            {
                this.log.warn("Failed to close RepositoryConnection", e);
            }
        }
    }
    
    @Override
    public PoddFileRepository<?> getRepository(final String alias) throws FileRepositoryException, OpenRDFException
    {
        if(alias == null)
        {
            return null;
        }
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            final URI context = this.repositoryManager.getFileRepositoryManagementGraph();
            
            final StringBuilder sb = new StringBuilder();
            
            sb.append("CONSTRUCT { ");
            sb.append(" ?aliasUri ?predicate ?object . ");
            
            sb.append(" } WHERE { ");
            
            sb.append(" ?aliasUri ?predicate ?object . ");
            sb.append(" ?aliasUri <" + RDF.TYPE.stringValue() + "> <"
                    + PoddRdfConstants.PODD_FILE_REPOSITORY.stringValue() + "> .");
            sb.append(" ?aliasUri <" + PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS.stringValue() + "> ?alias .");
            
            // TODO: filter out other aliases
            // sb.append(" ?aliasUri <" + PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS.stringValue()
            // + "> ?otherAlias . ");
            // sb.append("  ?otherAlias != ?alias . ");
            
            sb.append(" } ");
            
            this.log.info("Created SPARQL {} with alias bound to '{}'", sb.toString(), alias);
            
            final GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
            query.setBinding("alias", ValueFactoryImpl.getInstance().createLiteral(alias));
            
            final Model queryResults = this.executeGraphQuery(query, context);
            
            // DebugUtils.printContents(conn, context);
            DebugUtils.printContents(queryResults);
            
            if(queryResults.isEmpty())
            {
                return null;
            }
            
            return PoddFileRepositoryFactory.createFileRepository(alias, queryResults);
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
    public PoddFileRepository<?> removeRepositoryMapping(final String alias) throws FileRepositoryException,
        OpenRDFException
    {
        final PoddFileRepository<?> repositoryToRemove = this.getRepository(alias);
        if(repositoryToRemove == null)
        {
            throw new FileRepositoryMappingNotFoundException(alias, "No File Repository mapped to this alias");
        }
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            final URI context = this.repositoryManager.getFileRepositoryManagementGraph();
            
            if(this.getRepositoryAliases(repositoryToRemove).size() > 1)
            {
                // several aliases map to this repository. only remove the statement which maps this
                // alias
                conn.remove(null, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance()
                        .createLiteral(alias), context);
                this.log.info("Removed ONLY the mapping for alias {}", alias);
            }
            else
            {
                // only one mapping exists. delete the repository configuration
                final Set<Resource> subjectUris =
                        repositoryToRemove
                                .getAsModel()
                                .filter(null, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS,
                                        ValueFactoryImpl.getInstance().createLiteral(alias), context).subjects();
                for(final Resource subjectUri : subjectUris)
                {
                    conn.remove(subjectUri, null, null, context);
                    this.log.info("Removed ALL triples for alias with URI {}", alias, subjectUri.stringValue());
                }
            }
            
            conn.commit();
            return repositoryToRemove;
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
    public List<String> getRepositoryAliases(final PoddFileRepository<?> repositoryConfiguration)
        throws FileRepositoryException, OpenRDFException
    {
        final List<String> results = new ArrayList<String>();
        
        final String alias = repositoryConfiguration.getAlias();
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            final URI context = this.repositoryManager.getFileRepositoryManagementGraph();
            
            final StringBuilder sb = new StringBuilder();
            
            sb.append("SELECT ?otherAlias WHERE { ");
            sb.append(" ?aliasUri <" + PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS.stringValue() + "> ?otherAlias .");
            sb.append(" ?aliasUri <" + PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS.stringValue() + "> ?alias .");
            sb.append(" } ");
            
            this.log.info("Created SPARQL {} with alias bound to '{}'", sb.toString(), alias);
            
            final TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
            query.setBinding("alias", ValueFactoryImpl.getInstance().createLiteral(alias));
            
            final QueryResultCollector queryResults = this.executeSparqlQuery(query, context);
            for(final BindingSet binding : queryResults.getBindingSets())
            {
                final Value member = binding.getValue("otherAlias");
                results.add(member.stringValue());
            }
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
        
        return results;
    }
    
    @Override
    public void verifyFileReferences(final Set<FileReference> fileReferenceResults) throws OpenRDFException,
        PoddException, FileRepositoryMappingNotFoundException
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void downloadFileReference(final FileReference nextFileReference, final OutputStream outputStream)
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
        
        final QueryResultCollector results = new QueryResultCollector();
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
        final Model results = new LinkedHashModel();
        sparqlQuery.evaluate(new StatementCollector(results));
        
        return results;
    }
    
}
