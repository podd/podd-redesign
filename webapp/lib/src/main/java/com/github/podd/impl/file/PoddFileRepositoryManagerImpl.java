/**
 * 
 */
package com.github.podd.impl.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
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
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
import org.semanticweb.owlapi.formats.RioRDFOntologyFormatFactory;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileRegistry;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
import org.semanticweb.owlapi.rio.RioParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.api.file.PoddFileRepositoryManager;
import com.github.podd.exception.FileReferenceInvalidException;
import com.github.podd.exception.FileReferenceVerificationFailureException;
import com.github.podd.exception.FileRepositoryException;
import com.github.podd.exception.FileRepositoryIncompleteException;
import com.github.podd.exception.FileRepositoryMappingExistsException;
import com.github.podd.exception.FileRepositoryMappingNotFoundException;
import com.github.podd.exception.PoddException;
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
    
    private PoddOWLManager owlManager;
    
    /**
     * 
     */
    public PoddFileRepositoryManagerImpl()
    {
    }
    
    @Override
    public void init(final String pathDefaultAliases, final RDFFormat rdfFormat) throws OpenRDFException,
        FileRepositoryException, IOException
    {
        if(this.repositoryManager == null)
        {
            throw new NullPointerException("A RepositoryManager should be set before calling init()");
        }
        
        // load poddFileRepository.owl into OWLAPI
        final Model schemaModel = new LinkedHashModel();
        try
        {
            final InputStream inputA = this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_FILE_REPOSITORY);
            final RDFParser rdfParserA = Rio.createParser(RDFFormat.RDFXML);
            final StatementCollector collectorA = new StatementCollector(schemaModel);
            rdfParserA.setRDFHandler(collectorA);
            rdfParserA.parse(inputA, "");
            
            this.verifyFileRepositoryModelWithOWL(schemaModel, "application/rdf+xml");
        }
        catch(final Exception e)
        {
            throw new FileRepositoryIncompleteException(schemaModel, "Could not load poddFileRepository.owl");
        }
        
        if(this.getAllAliases().size() == 0)
        {
            this.log.info("File Repository Graph is empty. Loading default configurations...");
            final InputStream inputStream = this.getClass().getResourceAsStream(pathDefaultAliases);
            
            final RDFParser rdfParser = Rio.createParser(rdfFormat);
            final Model modelFromFile = new LinkedHashModel();
            final StatementCollector collector = new StatementCollector(modelFromFile);
            rdfParser.setRDFHandler(collector);
            rdfParser.parse(inputStream, "");
            
            final Model allAliases = modelFromFile.filter(null, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, null);
            
            // validate the default alias file against the File Repository configuration schema
            this.verifyFileRepositoryModelWithOWL(modelFromFile, "");
            
            this.log.info("Found {} default aliases to add", allAliases.size());
            
            for(final Statement stmt : allAliases)
            {
                final String alias = stmt.getObject().stringValue();
                final Model model = new LinkedHashModel();
                model.addAll(modelFromFile.filter(stmt.getSubject(), null, null));
                
                final PoddFileRepository<?> fileRepository =
                        PoddFileRepositoryFactory.createFileRepository(alias, model);
                
                this.addRepositoryMapping(alias, fileRepository, false);
            }
        }
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
        if(repositoryConfiguration == null || alias == null)
        {
            throw new NullPointerException("Cannot add NULL as a File Repository mapping");
        }
        
        final String aliasInLowerCase = alias.toLowerCase();
        
        // - check if a mapping with this alias already exists
        if(this.getRepository(aliasInLowerCase) != null)
        {
            if(overwrite)
            {
                this.removeRepositoryMapping(aliasInLowerCase);
            }
            else
            {
                throw new FileRepositoryMappingExistsException(aliasInLowerCase,
                        "File Repository mapping with this alias already exists");
            }
        }
        
        boolean repositoryConfigurationExistsInGraph = true;
        if(this.getRepositoryAliases(repositoryConfiguration).isEmpty())
        {
            // adding a new repository configuration
            repositoryConfigurationExistsInGraph = false;
        }
        
        final URI context = this.repositoryManager.getFileRepositoryManagementGraph();
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            if(repositoryConfigurationExistsInGraph)
            {
                final Set<Resource> subjectUris =
                        repositoryConfiguration.getAsModel()
                                .filter(null, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, null).subjects();
                
                this.log.info("Found {} subject URIs", subjectUris.size()); // should be only 1 here
                for(final Resource subjectUri : subjectUris)
                {
                    conn.add(subjectUri, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance()
                            .createLiteral(aliasInLowerCase), context);
                    this.log.info("Added alias '{}' triple with subject <{}>", aliasInLowerCase,
                            subjectUri.stringValue());
                }
            }
            else
            {
                final Model model = repositoryConfiguration.getAsModel();
                if(model == null || model.isEmpty())
                {
                    throw new FileRepositoryIncompleteException(model,
                            "Incomplete File Repository since Model is empty");
                }
                
                // check that the subject URIs used in the repository configuration are not already
                // used in the file repository management graph
                final Set<Resource> subjectUris =
                        model.filter(null, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, null).subjects();
                for(final Resource subjectUri : subjectUris)
                {
                    final RepositoryResult<Statement> statements =
                            conn.getStatements(subjectUri, null, null, false, context);
                    if(statements.hasNext())
                    {
                        throw new FileRepositoryIncompleteException(model,
                                "Subject URIs used in Model already exist in Management Graph");
                    }
                }
                
                conn.add(model, context);
            }
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
    public void downloadFileReference(final FileReference nextFileReference, final OutputStream outputStream)
        throws PoddException, IOException
    {
        // TODO
        throw new RuntimeException("TODO: Implement me");
    }
    
    @Override
    public List<String> getAllAliases() throws FileRepositoryException, OpenRDFException
    {
        final List<String> results = new ArrayList<String>();
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            final URI context = this.repositoryManager.getFileRepositoryManagementGraph();
            
            final StringBuilder sb = new StringBuilder();
            
            sb.append("SELECT ?alias WHERE { ");
            sb.append(" ?aliasUri <" + PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS.stringValue() + "> ?alias .");
            sb.append(" } ");
            
            this.log.info("Created SPARQL {} ", sb.toString());
            
            final TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
            
            final QueryResultCollector queryResults = this.executeSparqlQuery(query, context);
            for(final BindingSet binding : queryResults.getBindingSets())
            {
                final Value member = binding.getValue("alias");
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
    public PoddFileRepository<?> getRepository(final String alias) throws FileRepositoryException, OpenRDFException
    {
        if(alias == null)
        {
            return null;
        }
        
        // Could use LCASE function in the SPARQL query instead of converting to lower case here
        // (http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-lcase)
        final String aliasInLowerCase = alias.toLowerCase();
        
        final boolean multipleAliasesExist = this.getEquivalentAliases(aliasInLowerCase).size() > 1;
        
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
            
            // filter to exclude other aliases
            if(multipleAliasesExist)
            {
                sb.append(" ?aliasUri <" + PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS.stringValue()
                        + "> ?otherAlias . ");
                sb.append(" FILTER ( str(?object) != str(?otherAlias) && str(?otherAlias) != str(?alias) )  . ");
            }
            sb.append(" } ");
            
            this.log.info("Created SPARQL {} with alias bound to '{}'", sb.toString(), aliasInLowerCase);
            
            final GraphQuery query = conn.prepareGraphQuery(QueryLanguage.SPARQL, sb.toString());
            query.setBinding("alias", ValueFactoryImpl.getInstance().createLiteral(aliasInLowerCase));
            
            final Model queryResults = this.executeGraphQuery(query, context);
            
            if(queryResults.isEmpty())
            {
                return null;
            }
            
            return PoddFileRepositoryFactory.createFileRepository(aliasInLowerCase, queryResults);
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
        return this.getEquivalentAliases(repositoryConfiguration.getAlias());
    }
    
    @Override
    public List<String> getEquivalentAliases(final String alias) throws FileRepositoryException, OpenRDFException
    {
        final List<String> results = new ArrayList<String>();
        final String aliasInLowerCase = alias.toLowerCase();
        
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
            
            this.log.info("Created SPARQL {} with alias bound to '{}'", sb.toString(), aliasInLowerCase);
            
            final TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
            query.setBinding("alias", ValueFactoryImpl.getInstance().createLiteral(aliasInLowerCase));
            
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
    public PoddOWLManager getOWLManager()
    {
        return this.owlManager;
    }
    
    @Override
    public PoddRepositoryManager getRepositoryManager()
    {
        return this.repositoryManager;
    }
    
    @Override
    public PoddFileRepository<?> removeRepositoryMapping(final String alias) throws FileRepositoryException,
        OpenRDFException
    {
        final String aliasInLowerCase = alias.toLowerCase();
        
        final PoddFileRepository<?> repositoryToRemove = this.getRepository(aliasInLowerCase);
        if(repositoryToRemove == null)
        {
            throw new FileRepositoryMappingNotFoundException(aliasInLowerCase,
                    "No File Repository mapped to this alias");
        }
        
        // retrieved early simply to avoid having multiple RepositoryConnections open simultaneously
        final int aliasCount = this.getRepositoryAliases(repositoryToRemove).size();
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getRepository().getConnection();
            conn.begin();
            
            final URI context = this.repositoryManager.getFileRepositoryManagementGraph();
            
            if(aliasCount > 1)
            {
                // several aliases map to this repository. only remove the statement which maps this
                // alias
                conn.remove(null, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS, ValueFactoryImpl.getInstance()
                        .createLiteral(aliasInLowerCase), context);
                this.log.info("Removed ONLY the mapping for alias '{}'", aliasInLowerCase);
            }
            else
            {
                // only one mapping exists. delete the repository configuration
                final Set<Resource> subjectUris =
                        repositoryToRemove
                                .getAsModel()
                                .filter(null, PoddRdfConstants.PODD_FILE_REPOSITORY_ALIAS,
                                        ValueFactoryImpl.getInstance().createLiteral(aliasInLowerCase)).subjects();
                
                this.log.info("Need to remove {} triples", subjectUris.size()); // DEBUG output
                for(final Resource subjectUri : subjectUris)
                {
                    conn.remove(subjectUri, null, null, context);
                    this.log.info("Removed ALL triples for alias '{}' with URI <{}>", aliasInLowerCase,
                            subjectUri.stringValue());
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
    public void setOWLManager(final PoddOWLManager owlManager)
    {
        this.owlManager = owlManager;
    }
    
    @Override
    public void setRepositoryManager(final PoddRepositoryManager repositoryManager)
    {
        this.repositoryManager = repositoryManager;
    }
    
    @Override
    public void verifyFileReferences(final Set<FileReference> fileReferenceResults) throws OpenRDFException,
        FileRepositoryException, FileReferenceVerificationFailureException
    {
        final Map<FileReference, Throwable> errors = new HashMap<FileReference, Throwable>();
        
        for(final FileReference fileReference : fileReferenceResults)
        {
            final String alias = fileReference.getRepositoryAlias();
            final PoddFileRepository<FileReference> repository =
                    (PoddFileRepository<FileReference>)this.getRepository(alias);
            if(repository == null)
            {
                errors.put(fileReference, new FileRepositoryMappingNotFoundException(alias,
                        "Could not find a File Repository configuration mapped to this alias"));
            }
            else
            {
                try
                {
                    if(!repository.validate(fileReference))
                    {
                        errors.put(fileReference, new FileReferenceInvalidException(fileReference,
                                "Remote File Repository says this File Reference is invalid"));
                    }
                }
                catch(final Exception e)
                {
                    errors.put(fileReference, e);
                }
            }
        }
        
        if(!errors.isEmpty())
        {
            throw new FileReferenceVerificationFailureException(errors,
                    "File Reference validation resulted in failures");
        }
    }
    
    /**
     * Helper method to verify a given Model represents a FileRepository configuration complying
     * with poddFileRepository OWL Ontology.
     * <br>
     * Any ontologies imported by the one to be validated must be already loaded into the
     * OWLOntologyManager's memory.
     * <br>
     * NOTE: When this method returns, the ontology built from the input Model remains in the 
     * OWLManager's memory.
     * 
     * @param model
     *            A Model which should contain an Ontology with File Repository configurations
     * @param mimeType
     * @throws FileRepositoryException
     */
    private void verifyFileRepositoryModelWithOWL(final Model model, final String mimeType)
        throws FileRepositoryException
    {
        
        final RioRDFOntologyFormatFactory ontologyFormatFactory =
                (RioRDFOntologyFormatFactory)OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(mimeType);
        final RioParserImpl owlParser = new RioParserImpl(ontologyFormatFactory);
        
        OWLOntology nextOntology = null;
        
        try
        {
            nextOntology = this.getOWLManager().getOWLOntologyManager().createOntology();
            final RioMemoryTripleSource owlSource = new RioMemoryTripleSource(model.iterator());
            
            owlParser.parse(owlSource, nextOntology);
        }
        catch(OWLOntologyCreationException | OWLParserException | IOException e)
        {
            throw new FileRepositoryIncompleteException(model, "Error creating ontology for validation");
        }
        // verify: ontology is not empty
        if(nextOntology.isEmpty())
        {
            throw new FileRepositoryIncompleteException(model, "Loaded ontology was empty");
        }
        
        // hard-coded - all our ontologies are expected to be in OWL-DL
        final OWLProfile nextProfile = OWLProfileRegistry.getInstance().getProfile(OWLProfile.OWL2_DL);
        
        // verify: check ontology in profile
        final OWLProfileReport profileReport = nextProfile.checkOntology(nextOntology);
        if(!profileReport.isInProfile())
        {
            for(final OWLProfileViolation v : profileReport.getViolations())
            {
                System.out.println(" Profile violation = " + v.toString());
            }
            throw new FileRepositoryIncompleteException(model, "Ontology not in profile");
        }
        
        // verify: check consistency
        final OWLReasonerFactory reasonerFactory =
                OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet");
        final OWLReasoner reasoner = reasonerFactory.createReasoner(nextOntology);
        
        if(!reasoner.isConsistent())
        {
            this.getOWLManager().getOWLOntologyManager().removeOntology(nextOntology);
            throw new FileRepositoryIncompleteException(model, "Ontology is inconsistent");
        }
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
