/*
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
package com.github.podd.impl.purl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translates between two URI prefixes for a given set of triples.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class URITranslator
{
    private final static Logger LOGGER = LoggerFactory.getLogger(URITranslator.class);
    
    /**
     * Maps URIs for all triples in the given contexts in the given repository, between the input
     * URI prefix and the output URI prefix.
     * 
     * The mapping predicates are used to define extra triples to link the input and output URIs.
     * 
     * NOTE: The results for queries with deleteTranslatedTriples set to false may not be consistent
     * with what you expect.
     * 
     * @param repositoryConnection
     *            The repository containing the input triples, and which will contain the output
     *            triples
     * @param inputUriPrefix
     *            The string defining the start of any URIs to look for.
     * @param outputUriPrefix
     *            The string defining the start of the URIs which matched the inputUriPrefix, after
     *            the translation is complete.
     * @param nextSubjectMappingPredicates
     * @param translateSubjectUris
     * @param nextPredicateMappingPredicates
     * @param translatePredicateUris
     * @param nextObjectMappingPredicates
     * @param translateObjectUris
     * @param deleteTranslatedTriples
     *            If this is true, then any triples which contained translated URIs will be deleted.
     *            Mapping triples will still exist if any mapping predicates were utilised.
     * @param contexts
     *            The contexts in the repository that are relevant to the mapping
     * @param exactMatchRequired
     * @throws RepositoryException
     *             If the repository threw an exception during the course of the method.
     * @throws MalformedQueryException
     *             If any of the translation queries could not be executed due to an error in the
     *             queries or a lack of understanding of the query by the repository.
     * @throws UpdateExecutionException
     *             If the SPARQL Update queries used by this method were not able to be successfully
     *             executed on the given repository for some reason.
     */
    public static void doTranslation(final RepositoryConnection repositoryConnection, final String inputUriPrefix,
            final String outputUriPrefix, final Collection<URI> nextSubjectMappingPredicates,
            final boolean translateSubjectUris, final boolean exactSubjectMatchRequired,
            final Collection<URI> nextPredicateMappingPredicates, final boolean translatePredicateUris,
            final boolean exactPredicateMatchRequired, final Collection<URI> nextObjectMappingPredicates,
            final boolean translateObjectUris, final boolean exactObjectMatchRequired,
            final boolean deleteTranslatedTriples, final Resource... contexts) throws RepositoryException,
        MalformedQueryException, UpdateExecutionException
    {
        try
        {
            final List<String> withClauses = new ArrayList<String>();
            final List<String> allQueries = new ArrayList<String>();
            
            if(contexts != null)
            {
                for(final Resource nextResource : contexts)
                {
                    if(nextResource != null && nextResource instanceof URI)
                    {
                        withClauses.add(" WITH <" + nextResource.stringValue() + "> ");
                    }
                    else
                    {
                        URITranslator.LOGGER.error("Did not recognise (and ignoring) the context: " + nextResource);
                    }
                }
            }
            
            // add a single empty with clause if they didn't include any URI resources as contexts
            // to make the rest of the code simpler
            if(withClauses.size() == 0)
            {
                withClauses.add("");
            }
            
            if(translateObjectUris)
            {
                for(final String nextWithClause : withClauses)
                {
                    final StringBuilder objectConstructBuilder =
                            new StringBuilder(nextObjectMappingPredicates.size() * 120);
                    
                    for(final URI nextMappingPredicate : nextObjectMappingPredicates)
                    {
                        objectConstructBuilder.append(" ?normalisedObjectUri <" + nextMappingPredicate.stringValue()
                                + "> ?objectUri . ");
                    }
                    
                    final StringBuilder objectTemplateWhereBuilder = new StringBuilder();
                    
                    objectTemplateWhereBuilder.append(" ?subjectUri ?predicateUri ?objectUri . ");
                    
                    if(!exactObjectMatchRequired)
                    {
                        objectTemplateWhereBuilder.append("filter(isIRI(?objectUri) && strStarts(str(?objectUri), \""
                                + inputUriPrefix + "\")");
                        objectTemplateWhereBuilder.append(") . ");
                        objectTemplateWhereBuilder.append("bind(iri(concat(\"");
                        objectTemplateWhereBuilder.append(outputUriPrefix);
                        objectTemplateWhereBuilder.append("\", encode_for_uri(substr(str(?objectUri), ");
                        objectTemplateWhereBuilder.append((inputUriPrefix.length() + 1));
                        objectTemplateWhereBuilder.append(")))) AS ?normalisedObjectUri) ");
                    }
                    else
                    {
                        // the following should be more efficient on large queries for exact
                        // matching, as it contains constants that can be compiled down to IRIs
                        // In addition, the branch above will work with exact matching, but is prone
                        // to collisions if the IRI is used as the base of a longer IRI
                        objectTemplateWhereBuilder.append("filter(isIRI(?objectUri) && sameTerm(?objectUri, IRI(\""
                                + inputUriPrefix + "\"))). bind(iri(\"" + outputUriPrefix
                                + "\") AS ?normalisedObjectUri) . ");
                    }
                    
                    String deleteObjectTemplate;
                    
                    if(deleteTranslatedTriples)
                    {
                        deleteObjectTemplate = " DELETE { ?subjectUri ?predicateUri ?objectUri . } ";
                    }
                    else
                    {
                        deleteObjectTemplate = "";
                    }
                    
                    final String objectTemplate =
                            nextWithClause + " " + deleteObjectTemplate
                                    + " INSERT { ?subjectUri ?predicateUri ?normalisedObjectUri . "
                                    + objectConstructBuilder.toString() + " } " + " WHERE { "
                                    + objectTemplateWhereBuilder.toString() + " } ; ";
                    
                    URITranslator.LOGGER.debug("objectTemplate=" + objectTemplate);
                    
                    // allQueries.add(objectTemplate);
                    
                    URITranslator.executeSparqlUpdateQueries(repositoryConnection, objectTemplate);
                }
                
                // FIXME: Sesame seems to need this, or the following queries do not work correctly
                repositoryConnection.commit();
                
            }
            
            if(translateSubjectUris)
            {
                for(final String nextWithClause : withClauses)
                {
                    final StringBuilder subjectConstructBuilder =
                            new StringBuilder(nextSubjectMappingPredicates.size() * 120);
                    
                    for(final URI nextMappingPredicate : nextSubjectMappingPredicates)
                    {
                        subjectConstructBuilder.append(" ?normalisedSubjectUri <" + nextMappingPredicate.stringValue()
                                + "> ?subjectUri . ");
                    }
                    
                    final StringBuilder subjectTemplateWhereBuilder = new StringBuilder();
                    
                    subjectTemplateWhereBuilder.append(" ?subjectUri ?predicateUri ?objectUri . ");
                    
                    if(!exactObjectMatchRequired)
                    {
                        subjectTemplateWhereBuilder
                                .append("filter(isIRI(?subjectUri) && strStarts(str(?subjectUri), \"" + inputUriPrefix
                                        + "\")");
                        subjectTemplateWhereBuilder.append(") . ");
                        subjectTemplateWhereBuilder.append("bind(iri(concat(\"");
                        subjectTemplateWhereBuilder.append(outputUriPrefix);
                        subjectTemplateWhereBuilder.append("\", encode_for_uri(substr(str(?subjectUri), ");
                        subjectTemplateWhereBuilder.append((inputUriPrefix.length() + 1));
                        subjectTemplateWhereBuilder.append(")))) AS ?normalisedSubjectUri) ");
                    }
                    else
                    {
                        // the following should be more efficient on large queries for exact
                        // matching, as it contains constants that can be compiled down to IRIs
                        // In addition, the branch above will work with exact matching, but is prone
                        // to collisions if the IRI is used as the base of a longer IRI
                        subjectTemplateWhereBuilder.append("filter(isIRI(?subjectUri) && sameTerm(?subjectUri, IRI(\""
                                + inputUriPrefix + "\"))). bind(iri(\"" + outputUriPrefix
                                + "\") AS ?normalisedSubjectUri) . ");
                    }
                    
                    String deleteSubjectTemplate;
                    
                    if(deleteTranslatedTriples)
                    {
                        deleteSubjectTemplate = " DELETE { ?subjectUri ?predicateUri ?objectUri . } ";
                    }
                    else
                    {
                        deleteSubjectTemplate = "";
                    }
                    
                    final String subjectTemplate =
                            nextWithClause + " " + deleteSubjectTemplate
                                    + " INSERT { ?normalisedSubjectUri ?predicateUri ?objectUri . "
                                    + subjectConstructBuilder.toString() + " } " + " WHERE { "
                                    + subjectTemplateWhereBuilder.toString() + " } ; ";
                    
                    // allQueries.add(subjectTemplate);
                    
                    URITranslator.executeSparqlUpdateQueries(repositoryConnection, subjectTemplate);
                }
                
                // FIXME: Sesame seems to need this, or the following queries do not work correctly
                repositoryConnection.commit();
            }
            
            if(translatePredicateUris)
            {
                for(final String nextWithClause : withClauses)
                {
                    final StringBuilder predicateConstructBuilder =
                            new StringBuilder(nextPredicateMappingPredicates.size() * 120);
                    
                    for(final URI nextMappingPredicate : nextPredicateMappingPredicates)
                    {
                        predicateConstructBuilder.append(" ?normalisedPredicateUri <"
                                + nextMappingPredicate.stringValue() + "> ?predicateUri . ");
                    }
                    
                    final StringBuilder predicateTemplateWhereBuilder = new StringBuilder();
                    
                    predicateTemplateWhereBuilder.append(" ?subjectUri ?predicateUri ?objectUri . ");
                    
                    if(!exactObjectMatchRequired)
                    {
                        predicateTemplateWhereBuilder
                                .append("filter(isIRI(?predicateUri) && strStarts(str(?predicateUri), \""
                                        + inputUriPrefix + "\")");
                        predicateTemplateWhereBuilder.append(") . ");
                        predicateTemplateWhereBuilder.append("bind(iri(concat(\"");
                        predicateTemplateWhereBuilder.append(outputUriPrefix);
                        predicateTemplateWhereBuilder.append("\", encode_for_uri(substr(str(?predicateUri), ");
                        predicateTemplateWhereBuilder.append((inputUriPrefix.length() + 1));
                        predicateTemplateWhereBuilder.append(")))) AS ?normalisedPredicateUri) ");
                    }
                    else
                    {
                        // the following should be more efficient on large queries for exact
                        // matching, as it contains constants that can be compiled down to IRIs
                        // In addition, the branch above will work with exact matching, but is prone
                        // to collisions if the IRI is used as the base of a longer IRI
                        predicateTemplateWhereBuilder
                                .append("filter(isIRI(?predicateUri) && sameTerm(?predicateUri, IRI(\""
                                        + inputUriPrefix + "\"))). bind(iri(\"" + outputUriPrefix
                                        + "\") AS ?normalisedPredicateUri) . ");
                    }
                    
                    String deletePredicateTemplate;
                    
                    if(deleteTranslatedTriples)
                    {
                        deletePredicateTemplate = " DELETE { ?subjectUri ?predicateUri ?objectUri . } ";
                    }
                    else
                    {
                        deletePredicateTemplate = "";
                    }
                    
                    final String predicateTemplate =
                            nextWithClause + deletePredicateTemplate
                                    + " INSERT { ?subjectUri ?normalisedPredicateUri ?objectUri . "
                                    + predicateConstructBuilder.toString() + " } " + " WHERE { "
                                    + predicateTemplateWhereBuilder.toString() + " } ; ";
                    
                    // allQueries.add(predicateTemplate);
                    
                    URITranslator.executeSparqlUpdateQueries(repositoryConnection, predicateTemplate);
                }
                
                // executeSparqlUpdateQueries(repositoryConnection, allQueries);
                
                repositoryConnection.commit();
            }
        }
        catch(final RepositoryException rex)
        {
            // rollback the connection and then throw the resulting exception
            // TODO: Will this get called before the repositoryConnection.close() in the finally
            // block?
            repositoryConnection.rollback();
            throw rex;
        }
        catch(final MalformedQueryException mqe)
        {
            // rollback the connection and then throw the resulting exception
            // TODO: Will this get called before the repositoryConnection.close() in the finally
            // block?
            repositoryConnection.rollback();
            throw mqe;
        }
        catch(final UpdateExecutionException uee)
        {
            // rollback the connection and then throw the resulting exception
            // TODO: Will this get called before the repositoryConnection.close() in the finally
            // block?
            repositoryConnection.rollback();
            throw uee;
        }
    }
    
    /**
     * Maps URIs for all triples in the given contexts in the given repository, between the input
     * URI prefix and the output URI prefix.
     * 
     * The mapping predicates are used to define extra triples to link the input and output URIs.
     * 
     * NOTE: The results for queries with deleteTranslatedTriples set to false may not be consistent
     * with what you expect.
     * 
     * @param repositoryConnection
     *            The repository containing the input triples, and which will contain the output
     *            triples
     * @param inputUriPrefix
     *            The string defining the start of any URIs to look for.
     * @param outputUriPrefix
     *            The string defining the start of the URIs which matched the inputUriPrefix, after
     *            the translation is complete.
     * @param nextSubjectMappingPredicates
     * @param nextPredicateMappingPredicates
     * @param nextObjectMappingPredicates
     * @param deleteTranslatedTriples
     *            If this is true, then any triples which contained translated URIs will be deleted.
     *            Mapping triples will still exist if any mapping predicates were utilised.
     * @param contexts
     *            The contexts in the repository that are relevant to the mapping
     * @throws RepositoryException
     *             If the repository threw an exception during the course of the method.
     * @throws MalformedQueryException
     *             If any of the translation queries could not be executed due to an error in the
     *             queries or a lack of understanding of the query by the repository.
     * @throws UpdateExecutionException
     *             If the SPARQL Update queries used by this method were not able to be successfully
     *             executed on the given repository for some reason.
     */
    public static void doTranslation(final RepositoryConnection repositoryConnection, final String inputUriPrefix,
            final String outputUriPrefix, final Collection<URI> nextSubjectMappingPredicates,
            final Collection<URI> nextPredicateMappingPredicates, final Collection<URI> nextObjectMappingPredicates,
            final boolean deleteTranslatedTriples, final Resource... contexts) throws RepositoryException,
        MalformedQueryException, UpdateExecutionException
    {
        URITranslator.doTranslation(repositoryConnection, inputUriPrefix, outputUriPrefix,
                nextSubjectMappingPredicates, true, false, nextPredicateMappingPredicates, true, false,
                nextObjectMappingPredicates, true, false, deleteTranslatedTriples, contexts);
    }
    
    /**
     * Maps URIs for all triples in the given contexts in the given repository, between the input
     * URI prefix and the output URI prefix.
     * 
     * @param repositoryConnection
     *            The repository containing the input triples, and which will contain the output
     *            triples
     * @param inputUriPrefix
     *            The string defining the start of any URIs to look for.
     * @param outputUriPrefix
     *            The string defining the start of the URIs which matched the inputUriPrefix, after
     *            the translation is complete.
     * @param contexts
     *            The contexts in the repository that are relevant to the mapping
     * @throws RepositoryException
     *             If the repository threw an exception during the course of the method.
     * @throws MalformedQueryException
     *             If any of the translation queries could not be executed due to an error in the
     *             queries or a lack of understanding of the query by the repository.
     * @throws UpdateExecutionException
     *             If the SPARQL Update queries used by this method were not able to be successfully
     *             executed on the given repository for some reason.
     */
    public static void doTranslation(final RepositoryConnection repositoryConnection, final String inputUriPrefix,
            final String outputUriPrefix, final Resource... contexts) throws RepositoryException,
        MalformedQueryException, UpdateExecutionException
    {
        final Collection<URI> subjectMappingPredicates = Collections.emptyList();
        final Collection<URI> predicateMappingPredicates = Collections.emptyList();
        final Collection<URI> objectMappingPredicates = Collections.emptyList();
        
        URITranslator.doTranslation(repositoryConnection, inputUriPrefix, outputUriPrefix, subjectMappingPredicates,
                predicateMappingPredicates, objectMappingPredicates, true, contexts);
    }
    
    /**
     * Executes the given SPARQL Update queries against the given repository.
     * 
     * @param repositoryConnection
     * @param nextQueries
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws UpdateExecutionException
     */
    private static void executeSparqlUpdateQueries(final RepositoryConnection repositoryConnection,
            final List<String> nextQueries) throws RepositoryException, MalformedQueryException,
        UpdateExecutionException
    {
        for(final String nextQuery : nextQueries)
        {
            URITranslator.LOGGER.debug("nextQuery=" + nextQuery);
            
            final Update preparedUpdate = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, nextQuery);
            
            preparedUpdate.execute();
        }
    }
    
    /**
     * Executes the given SPARQL Update query against the given repository.
     * 
     * @param repositoryConnection
     * @param nextQuery
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws UpdateExecutionException
     */
    private static void executeSparqlUpdateQueries(final RepositoryConnection repositoryConnection,
            final String nextQuery) throws RepositoryException, MalformedQueryException, UpdateExecutionException
    {
        URITranslator.executeSparqlUpdateQueries(repositoryConnection, Collections.singletonList(nextQuery));
    }
    
}
