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
package com.github.podd.impl.data;

import info.aduna.iteration.Iterations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.resultio.helpers.QueryResultCollector;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.data.DataReference;
import com.github.podd.api.data.PoddDataRepository;
import com.github.podd.api.data.PoddDataRepositoryManager;
import com.github.podd.api.data.PoddDataRepositoryRegistry;
import com.github.podd.exception.DataReferenceInvalidException;
import com.github.podd.exception.DataReferenceVerificationException;
import com.github.podd.exception.DataRepositoryException;
import com.github.podd.exception.DataRepositoryIncompleteException;
import com.github.podd.exception.DataRepositoryMappingExistsException;
import com.github.podd.exception.DataRepositoryMappingNotFoundException;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.PoddRuntimeException;
import com.github.podd.utils.PODD;
import com.github.podd.utils.RdfUtility;

/**
 * An implementation of FileRepositoryManager which uses an RDF repository graph as the back-end
 * storage for maintaining information about Repository Configurations.
 *
 * @author kutila
 */
public class PoddDataRepositoryManagerImpl implements PoddDataRepositoryManager
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private PoddRepositoryManager repositoryManager;

    private PoddOWLManager owlManager;

    private final Model dataRepositorySchema;

    /**
     *
     */
    public PoddDataRepositoryManagerImpl()
    {
        try (final InputStream inputA = this.getClass().getResourceAsStream(PODD.PATH_PODD_DATA_REPOSITORY_V3);)
        {
            Objects.requireNonNull(inputA, "could not find data repository ontology");
            // load poddDataRepository.owl into a Model
            this.dataRepositorySchema = Rio.parse(inputA, "", RDFFormat.RDFXML);
        }
        catch(IOException | RDFParseException | UnsupportedRDFormatException e)
        {
            throw new PoddRuntimeException(
                    "Could not initialise data repository manager due to an RDF or IO exception", e);
        }
    }

    @Override
    public void addRepositoryMapping(final String alias, final PoddDataRepository<?> repositoryConfiguration)
            throws OpenRDFException, DataRepositoryException
    {
        this.addRepositoryMapping(alias, repositoryConfiguration, false);
    }

    @Override
    public void addRepositoryMapping(final String alias, final PoddDataRepository<?> repositoryConfiguration,
            final boolean overwrite) throws OpenRDFException, DataRepositoryException
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
                throw new DataRepositoryMappingExistsException(aliasInLowerCase,
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
            conn = this.repositoryManager.getManagementRepositoryConnection();
            conn.begin();

            if(repositoryConfigurationExistsInGraph)
            {
                final Set<Resource> subjectUris =
                        repositoryConfiguration.getAsModel().filter(null, PODD.PODD_DATA_REPOSITORY_ALIAS, null)
                        .subjects();

                this.log.debug("Found {} subject URIs", subjectUris.size()); // should
                // be
                // only
                // 1
                // here
                for(final Resource subjectUri : subjectUris)
                {
                    conn.add(subjectUri, PODD.PODD_DATA_REPOSITORY_ALIAS,
                            ValueFactoryImpl.getInstance().createLiteral(aliasInLowerCase), context);
                    this.log.debug("Added alias '{}' triple with subject <{}>", aliasInLowerCase, subjectUri);
                }
            }
            else
            {
                final Model model = repositoryConfiguration.getAsModel();
                if(model == null || model.isEmpty())
                {
                    throw new DataRepositoryIncompleteException("Incomplete File Repository since Model is empty");
                }

                // check that the subject URIs used in the repository
                // configuration are not already
                // used in the file repository management graph
                final Set<Resource> subjectUris = model.filter(null, PODD.PODD_DATA_REPOSITORY_ALIAS, null).subjects();
                for(final Resource subjectUri : subjectUris)
                {
                    if(conn.hasStatement(subjectUri, null, null, false, context))
                    {
                        throw new DataRepositoryIncompleteException(
                                "Subject URIs used in Model already exist in Management Graph: uri=" + subjectUri);
                    }
                }

                conn.add(model, context);
            }
            conn.commit();
        }
        catch(final Throwable e)
        {
            if(conn != null)
            {
                conn.rollback();
            }
            throw e;
        }
        finally
        {
            try
            {
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
    public void downloadFileReference(final DataReference nextFileReference, final OutputStream outputStream)
            throws PoddException, IOException
    {
        // TODO
        throw new RuntimeException("TODO: Implement me");
    }

    @Override
    public List<String> getAllAliases() throws DataRepositoryException, OpenRDFException
    {
        final List<String> results = new ArrayList<String>();

        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getManagementRepositoryConnection();
            conn.begin();

            final URI context = this.repositoryManager.getFileRepositoryManagementGraph();

            final StringBuilder sb = new StringBuilder();

            sb.append("SELECT ?alias WHERE { ");
            sb.append(" ?aliasUri <" + PODD.PODD_DATA_REPOSITORY_ALIAS.stringValue() + "> ?alias .");
            sb.append(" } ");

            this.log.debug("Created SPARQL {} ", sb);

            final TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());

            final QueryResultCollector queryResults = RdfUtility.executeTupleQuery(query, context);
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
    public List<String> getEquivalentAliases(final String alias) throws DataRepositoryException, OpenRDFException
    {
        final List<String> results = new ArrayList<String>();
        final String aliasInLowerCase = alias.toLowerCase();

        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getManagementRepositoryConnection();
            conn.begin();

            final URI context = this.repositoryManager.getFileRepositoryManagementGraph();

            final StringBuilder sb = new StringBuilder();

            sb.append("SELECT ?otherAlias WHERE { ");
            sb.append(" ?aliasUri <" + PODD.PODD_DATA_REPOSITORY_ALIAS.stringValue() + "> ?otherAlias .");
            sb.append(" ?aliasUri <" + PODD.PODD_DATA_REPOSITORY_ALIAS.stringValue() + "> ?alias .");
            sb.append(" } ");

            this.log.debug("Created SPARQL {} with alias bound to '{}'", sb, aliasInLowerCase);

            final TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sb.toString());
            query.setBinding("alias", ValueFactoryImpl.getInstance().createLiteral(aliasInLowerCase));

            final QueryResultCollector queryResults = RdfUtility.executeTupleQuery(query, context);
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
    public PoddDataRepository<? extends DataReference> getRepository(final String alias)
            throws DataRepositoryException, OpenRDFException
            {
        if(alias == null)
        {
            this.log.warn("Could not find a repository with a null alias");
            throw new IllegalArgumentException("Could not find a repository with a null alias");
        }

        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getManagementRepositoryConnection();

            final URI context = this.repositoryManager.getFileRepositoryManagementGraph();
            final Model repositories = new LinkedHashModel();
            // Fetch the entire configuration into memory, as it should never be
            // more than a trivial
            // size. If this hampers efficiency could switch back to on demand
            // querying
            Iterations.addAll(conn.getStatements(null, null, null, true, context), repositories);
            final Set<Resource> matchingRepositories = new HashSet<Resource>();
            for(final Resource nextRepository : repositories.filter(null, RDF.TYPE, PODD.PODD_DATA_REPOSITORY)
                    .subjects())
            {
                for(final Value nextAlias : repositories.filter(nextRepository, PODD.PODD_DATA_REPOSITORY_ALIAS, null)
                        .objects())
                {
                    if(nextAlias instanceof Literal && ((Literal)nextAlias).getLabel().equalsIgnoreCase(alias))
                    {
                        matchingRepositories.add(nextRepository);
                        break;
                    }
                }
            }

            for(final Resource nextMatchingRepository : matchingRepositories)
            {
                final PoddDataRepository<? extends DataReference> repository =
                        PoddDataRepositoryRegistry.getInstance().createDataRepository(nextMatchingRepository,
                                repositories);

                if(repository != null)
                {
                    return repository;
                }
            }
        }
        finally
        {
            if(conn != null && conn.isOpen())
            {
                conn.close();
            }
        }

        // log.warn("Could not find a repository with alias: {}", alias);
        // throw new DataRepositoryMappingNotFoundException(alias,
        // "Could not find a repository with this alias");
        return null;
            }

    @Override
    public List<String> getRepositoryAliases(final PoddDataRepository<?> repositoryConfiguration)
            throws DataRepositoryException, OpenRDFException
            {
        return this.getEquivalentAliases(repositoryConfiguration.getAlias());
            }

    @Override
    public PoddRepositoryManager getRepositoryManager()
    {
        return this.repositoryManager;
    }

    @Override
    public void initialise(final Model defaultAliasConfiguration) throws OpenRDFException, PoddException, IOException
    {
        if(this.repositoryManager == null)
        {
            throw new NullPointerException("A RepositoryManager should be set before calling initialise()");
        }

        if(this.getAllAliases().size() == 0)
        {
            this.log.info("File Repository Graph is empty. Loading default configurations...");

            // validate the default alias file against the File Repository
            // configuration schema
            this.getOWLManager().verifyAgainstSchema(defaultAliasConfiguration, this.dataRepositorySchema);

            final Model allAliases = defaultAliasConfiguration.filter(null, PODD.PODD_DATA_REPOSITORY_ALIAS, null);

            this.log.info("Found {} default aliases to add", allAliases.size());

            for(final Statement stmt : allAliases)
            {
                final String alias = stmt.getObject().stringValue();

                try
                {
                    final PoddDataRepository<?> dataRepository =
                            PoddDataRepositoryRegistry.getInstance().createDataRepository(stmt.getSubject(),
                                    defaultAliasConfiguration);

                    if(dataRepository != null)
                    {
                        this.addRepositoryMapping(alias, dataRepository, false);
                    }
                }
                catch(final DataRepositoryException dre)
                {
                    this.log.error("Found error attempting to create repository for alias", dre);
                }

            }
        }
    }

    @Override
    public PoddDataRepository<?> removeRepositoryMapping(final String alias) throws DataRepositoryException,
    OpenRDFException
    {
        final String aliasInLowerCase = alias.toLowerCase();

        final PoddDataRepository<?> repositoryToRemove = this.getRepository(aliasInLowerCase);
        if(repositoryToRemove == null)
        {
            throw new DataRepositoryMappingNotFoundException(aliasInLowerCase,
                    "No File Repository mapped to this alias");
        }

        // retrieved early simply to avoid having multiple RepositoryConnections
        // open simultaneously
        final int aliasCount = this.getRepositoryAliases(repositoryToRemove).size();

        RepositoryConnection conn = null;
        try
        {
            conn = this.repositoryManager.getManagementRepositoryConnection();
            conn.begin();

            final URI context = this.repositoryManager.getFileRepositoryManagementGraph();

            if(aliasCount > 1)
            {
                // several aliases map to this repository. only remove the
                // statement which maps this
                // alias
                conn.remove(null, PODD.PODD_DATA_REPOSITORY_ALIAS,
                        ValueFactoryImpl.getInstance().createLiteral(aliasInLowerCase), context);
                this.log.debug("Removed ONLY the mapping for alias '{}'", aliasInLowerCase);
            }
            else
            {
                // only one mapping exists. delete the repository configuration
                final Set<Resource> subjectUris =
                        repositoryToRemove
                        .getAsModel()
                        .filter(null, PODD.PODD_DATA_REPOSITORY_ALIAS,
                                ValueFactoryImpl.getInstance().createLiteral(aliasInLowerCase)).subjects();

                this.log.debug("Need to remove {} triples", subjectUris.size()); // DEBUG
                // output
                for(final Resource subjectUri : subjectUris)
                {
                    conn.remove(subjectUri, null, null, context);
                    this.log.debug("Removed ALL triples for alias '{}' with URI <{}>", aliasInLowerCase, subjectUri);
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
    public void verifyDataReferences(final Set<DataReference> fileReferenceResults) throws OpenRDFException,
    DataRepositoryException, DataReferenceVerificationException
    {
        final Map<DataReference, Throwable> errors = new HashMap<DataReference, Throwable>();

        for(final DataReference dataReference : fileReferenceResults)
        {
            final String alias = dataReference.getRepositoryAlias();
            final PoddDataRepository<DataReference> repository =
                    (PoddDataRepository<DataReference>)this.getRepository(alias);
            if(repository == null)
            {
                errors.put(dataReference, new DataRepositoryMappingNotFoundException(alias,
                        "Could not find a File Repository configuration mapped to this alias"));
            }
            else
            {
                try
                {
                    if(!repository.validate(dataReference))
                    {
                        errors.put(dataReference, new DataReferenceInvalidException(dataReference,
                                "Remote File Repository says this File Reference is invalid"));
                    }
                }
                catch(final Exception e)
                {
                    errors.put(dataReference, e);
                }
            }
        }

        if(!errors.isEmpty())
        {
            throw new DataReferenceVerificationException(errors, "File Reference validation resulted in failures");
        }
    }

}
