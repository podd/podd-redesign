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
package com.github.podd.client.impl.restlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.queryrender.RenderUtils;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.restlet.data.CharacterSet;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.ReaderRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.ansell.restletutils.RestletUtilMediaType;
import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.api.DanglingObjectPolicy;
import com.github.podd.api.DataReferenceVerificationPolicy;
import com.github.podd.api.data.DataReference;
import com.github.podd.api.data.DataReferenceConstants;
import com.github.podd.client.api.PoddClient;
import com.github.podd.client.api.PoddClientException;
import com.github.podd.ontologies.PODDBASE;
import com.github.podd.ontologies.PODDSCIENCE;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddWebConstants;

/**
 * Restlet based PODD Client implementation.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class RestletPoddClientImpl implements PoddClient
{
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public final static String DEFAULT_PROPERTY_BUNDLE = "poddclient";
    
    public static final String PROP_PODD_SERVER_URL = "podd.serverurl";
    
    public static final String DEFAULT_PODD_SERVER_URL = "http://localhost:8080/podd/";
    
    public final static String TEMP_UUID_PREFIX = "urn:temp:uuid:";
    
    private Series<CookieSetting> currentCookies = new Series<CookieSetting>(CookieSetting.class);
    
    private PropertyUtil props;
    
    private volatile String serverUrl = null;
    
    /**
     * Shortcut to {@link PODD#VF}
     */
    protected final static ValueFactory vf = PODD.VF;
    
    public RestletPoddClientImpl()
    {
        this.props = new PropertyUtil(RestletPoddClientImpl.DEFAULT_PROPERTY_BUNDLE);
    }
    
    public RestletPoddClientImpl(final String poddServerUrl)
    {
        this();
        this.serverUrl = poddServerUrl;
    }
    
    @Override
    public void addRole(final String userIdentifier, final RestletUtilRole role, final InferredOWLOntologyID artifact)
        throws PoddClientException
    {
        this.log.info("cookies: {}", this.currentCookies);
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ROLES));
        resource.getCookies().addAll(this.currentCookies);
        resource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, userIdentifier);
        
        final Map<RestletUtilRole, Collection<URI>> mappings = new HashMap<>();
        final Collection<URI> artifacts = Arrays.asList(artifact.getOntologyIRI().toOpenRDFURI());
        mappings.put(role, artifacts);
        
        final Model model = new LinkedHashModel();
        PoddRoles.dumpRoleMappingsUser(mappings, model);
        
        final Representation post = this.postRdf(resource, model);
        
        try
        {
            final Model parsedStatements = this.parseRdf(post);
            
            if(!parsedStatements.contains(null, SesameRealmConstants.OAS_USERIDENTIFIER,
                    PODD.VF.createLiteral(userIdentifier)))
            {
                this.log.warn("Role edit response did not seem to contain the user identifier");
            }
        }
        catch(final IOException e)
        {
            throw new PoddClientException("Could not parse results due to an IOException", e);
        }
    }
    
    @Override
    public InferredOWLOntologyID appendArtifact(final InferredOWLOntologyID ontologyIRI,
            final InputStream partialInputStream, final RDFFormat format) throws PoddClientException
    {
        return this.appendArtifact(ontologyIRI, partialInputStream, format, DanglingObjectPolicy.REPORT,
                DataReferenceVerificationPolicy.DO_NOT_VERIFY);
    }
    
    @Override
    public InferredOWLOntologyID appendArtifact(final InferredOWLOntologyID artifactID,
            final InputStream partialInputStream, final RDFFormat format,
            final DanglingObjectPolicy danglingObjectPolicy,
            final DataReferenceVerificationPolicy dataReferenceVerificationPolicy) throws PoddClientException
    {
        final InputRepresentation rep =
                new InputRepresentation(partialInputStream, MediaType.valueOf(format.getDefaultMIMEType()));
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT));
        resource.getCookies().addAll(this.currentCookies);
        
        this.log.info("cookies: {}", this.currentCookies);
        
        resource.addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_REPLACE, Boolean.toString(false));
        resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID.getOntologyIRI().toString());
        resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID.getVersionIRI()
                .toString());
        if(danglingObjectPolicy == DanglingObjectPolicy.FORCE_CLEAN)
        {
            resource.addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_FORCE, "true");
        }
        if(dataReferenceVerificationPolicy == DataReferenceVerificationPolicy.VERIFY)
        {
            resource.addQueryParameter(PoddWebConstants.KEY_EDIT_VERIFY_FILE_REFERENCES, "true");
        }
        resource.addQueryParameter("format", format.getDefaultMIMEType());
        
        // Request the results in Turtle to reduce the bandwidth
        final Representation post = resource.post(rep, MediaType.APPLICATION_RDF_TURTLE);
        
        try
        {
            final Model parsedStatements = this.parseRdf(post);
            
            final Collection<InferredOWLOntologyID> result =
                    OntologyUtils.modelToOntologyIDs(parsedStatements, true, false);
            
            if(!result.isEmpty())
            {
                return result.iterator().next();
            }
            
            throw new PoddClientException("Failed to verify that the artifact was uploaded correctly.");
        }
        catch(final IOException e)
        {
            throw new PoddClientException("Could not parse artifact details due to an IOException", e);
        }
    }
    
    @Override
    public Map<InferredOWLOntologyID, InferredOWLOntologyID> appendArtifacts(
            final Map<InferredOWLOntologyID, Model> uploadQueue) throws PoddClientException
    {
        final ConcurrentMap<InferredOWLOntologyID, InferredOWLOntologyID> resultMap = new ConcurrentHashMap<>();
        for(final Entry<InferredOWLOntologyID, Model> nextUpload : uploadQueue.entrySet())
        {
            try
            {
                final StringWriter writer = new StringWriter(4096);
                Rio.write(nextUpload.getValue(), writer, RDFFormat.RDFJSON);
                final InferredOWLOntologyID newID =
                        this.appendArtifact(nextUpload.getKey(),
                                new ByteArrayInputStream(writer.toString().getBytes(Charset.forName("UTF-8"))),
                                RDFFormat.RDFJSON);
                
                if(newID == null)
                {
                    this.log.error("Did not find a valid result from append artifact: {}", nextUpload.getKey());
                }
                else if(nextUpload.getKey().equals(newID))
                {
                    this.log.error("Result from append artifact was not changed, as expected. {} {}",
                            nextUpload.getKey(), newID);
                }
                else
                {
                    resultMap.putIfAbsent(nextUpload.getKey(), newID);
                }
            }
            catch(final RDFHandlerException e)
            {
                this.log.error("Found exception generating upload body: ", e);
            }
        }
        return resultMap;
    }
    
    @Override
    public InferredOWLOntologyID attachDataReference(final DataReference ref) throws PoddClientException
    {
        this.log.info("cookies: {}", this.currentCookies);
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_DATA_REF));
        resource.getCookies().addAll(this.currentCookies);
        resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, ref.getArtifactID().getOntologyIRI()
                .toString());
        resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, ref.getArtifactID()
                .getVersionIRI().toString());
        resource.addQueryParameter(DataReferenceConstants.KEY_OBJECT_URI, ref.getObjectIri().toString());
        
        final Model model = ref.toRDF();
        
        final Representation post = this.postRdf(resource, model);
        
        try
        {
            final Model parsedStatements = this.parseRdf(post);
            
            final Collection<InferredOWLOntologyID> result =
                    OntologyUtils.modelToOntologyIDs(parsedStatements, true, false);
            
            if(!result.isEmpty())
            {
                return result.iterator().next();
            }
            
            throw new PoddClientException("Failed to verify that the file reference was attached correctly.");
        }
        catch(final IOException e)
        {
            throw new PoddClientException("Could not parse artifact details due to an IOException", e);
        }
    }
    
    @Override
    public PoddUser createUser(final PoddUser user) throws PoddClientException
    {
        try
        {
            final Model model = new LinkedHashModel();
            user.toModel(model, true);
            final ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
            Rio.write(model, output, RDFFormat.RDFJSON);
            final InputRepresentation rep =
                    new InputRepresentation(new ByteArrayInputStream(output.toByteArray()),
                            MediaType.valueOf(RDFFormat.RDFJSON.getDefaultMIMEType()));
            
            final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ADD));
            resource.getCookies().addAll(this.currentCookies);
            
            this.log.info("cookies: {}", this.currentCookies);
            
            resource.addQueryParameter("format", RDFFormat.RDFJSON.getDefaultMIMEType());
            
            // Request the results in Turtle to reduce the bandwidth
            final Representation post = resource.post(rep, MediaType.APPLICATION_RDF_TURTLE);
            
            final Model parsedStatements = this.parseRdf(post);
            
            final PoddUser result = PoddUser.fromModel(model);
            
            return result;
        }
        catch(final IOException | RDFHandlerException | ResourceException e)
        {
            throw new PoddClientException("Could not parse artifact details due to an exception", e);
        }
    }
    
    @Override
    public boolean deleteArtifact(final InferredOWLOntologyID artifactId) throws PoddClientException
    {
        this.log.info("cookies: {}", this.currentCookies);
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_DELETE));
        resource.getCookies().addAll(this.currentCookies);
        
        resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactId.getOntologyIRI().toString());
        
        if(artifactId.getVersionIRI() != null)
        {
            // FIXME: Versions are not supported in general by PODD, but they are important for
            // verifying the state of the client to allow for early failure in cases where the
            // client is out of date.
            resource.addQueryParameter("versionUri", artifactId.getVersionIRI().toString());
        }
        
        resource.delete();
        
        if(resource.getStatus().isSuccess())
        {
            return true;
        }
        else
        {
            throw new PoddClientException("Failed to successfully delete artifact: "
                    + artifactId.getOntologyIRI().toString());
        }
    }
    
    @Override
    public Model doSPARQL(final String queryString, Collection<InferredOWLOntologyID> artifactIds)
        throws PoddClientException
    {
        this.log.info("cookies: {}", this.currentCookies);
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SPARQL));
        resource.getCookies().addAll(this.currentCookies);
        
        // TODO: Parse query to make sure it is syntactically valid before sending query
        resource.addQueryParameter(PoddWebConstants.KEY_SPARQLQUERY, queryString);
        
        for(InferredOWLOntologyID artifactId : artifactIds)
        {
            resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactId.getOntologyIRI().toString());
        }
        
        try
        {
            // Pass the desired format to the get method of the ClientResource
            final Representation get = resource.get(RestletUtilMediaType.APPLICATION_RDF_JSON);
            
            final StringWriter writer = new StringWriter(4096);
            
            get.write(writer);
            return Rio.parse(new StringReader(writer.toString()), "", RDFFormat.RDFJSON);
        }
        catch(ResourceException e)
        {
            if(e.getStatus().equals(Status.CLIENT_ERROR_PRECONDITION_FAILED))
            {
                // Precondition failed indicates that they do not have access to any artifacts, so
                // return empty results set
                return new LinkedHashModel();
            }
            else
            {
                throw new PoddClientException("Could not execute SPARQL query", e);
            }
        }
        catch(final IOException | RDFParseException | UnsupportedRDFormatException e)
        {
            throw new PoddClientException("Could not process SPARQL query results", e);
        }
    }
    
    @Override
    public Model downloadArtifact(final InferredOWLOntologyID artifactId) throws PoddClientException
    {
        try
        {
            Path tempFile = Files.createTempFile("downloadartifact-", ".rj");
            try (final OutputStream output = Files.newOutputStream(tempFile);)
            {
                downloadArtifact(artifactId, output, RDFFormat.RDFJSON);
            }
            try (final InputStream input = Files.newInputStream(tempFile);)
            {
                return Rio.parse(input, "", RDFFormat.RDFJSON);
            }
        }
        catch(RDFParseException | UnsupportedRDFormatException | IOException e)
        {
            throw new PoddClientException(e);
        }
    }
    
    @Override
    public void downloadArtifact(final InferredOWLOntologyID artifactId, final OutputStream outputStream,
            final RDFFormat format) throws PoddClientException
    {
        Objects.requireNonNull(artifactId);
        Objects.requireNonNull(outputStream);
        Objects.requireNonNull(format);
        
        this.log.info("cookies: {}", this.currentCookies);
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        resource.getCookies().addAll(this.currentCookies);
        
        resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactId.getOntologyIRI().toString());
        
        if(artifactId.getVersionIRI() != null)
        {
            // FIXME: Versions are not supported in general by PODD, but they are important for
            // verifying the state of the client to allow for early failure in cases where the
            // client is out of date.
            resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactId.getVersionIRI()
                    .toString());
        }
        
        // Pass the desired format to the get method of the ClientResource
        final Representation get = resource.get(MediaType.valueOf(format.getDefaultMIMEType()));
        
        try
        {
            get.write(outputStream);
        }
        catch(final IOException e)
        {
            throw new PoddClientException("Could not write downloaded artifact to output stream", e);
        }
    }
    
    @Override
    public Model getObjectsByType(final URI type, final Collection<InferredOWLOntologyID> artifacts)
        throws PoddClientException
    {
        String queryString =
                String.format(PoddClient.TEMPLATE_SPARQL_BY_TYPE_WITH_LABEL, RenderUtils.getSPARQLQueryString(type));
        this.log.info("queryString={}", queryString);
        return this.doSPARQL(queryString, artifacts);
    }
    
    @Override
    public Model getObjectsByTypeAndPrefix(final URI type, final String labelPrefix,
            final Collection<InferredOWLOntologyID> artifacts) throws PoddClientException
    {
        String queryString =
                String.format(PoddClient.TEMPLATE_SPARQL_BY_TYPE_LABEL_STRSTARTS, RenderUtils.escape(labelPrefix),
                        RenderUtils.getSPARQLQueryString(type));
        this.log.info("queryString={}", queryString);
        return this.doSPARQL(queryString, artifacts);
    }
    
    @Override
    public Model getObjectsByTypePredicateAndPrefix(final URI type, final URI predicate, final String labelPrefix,
            final Collection<InferredOWLOntologyID> artifacts) throws PoddClientException
    {
        String predicateString = RenderUtils.getSPARQLQueryString(predicate);
        // NOTE: predicateString must be both the second and third arguments sent into String.format
        // as it is used twice, once for the Construct and once for the Where
        // Hypothetically the second could be different to the third for mapping predicates, but
        // that would cause confusion if not obvious
        String queryString =
                String.format(PoddClient.TEMPLATE_SPARQL_BY_TYPE_LABEL_STRSTARTS_PREDICATE, predicateString,
                        predicateString, RenderUtils.escape(labelPrefix), RenderUtils.getSPARQLQueryString(type));
        this.log.info("queryString={}", queryString);
        return this.doSPARQL(queryString, artifacts);
    }
    
    @Override
    public Model getObjectsByTypeAndParent(final URI parent, final URI parentPredicate, final URI type,
            final Collection<InferredOWLOntologyID> artifacts) throws PoddClientException
    {
        String queryString =
                String.format(PoddClient.TEMPLATE_SPARQL_BY_TYPE_AND_PARENT_ALL_PROPERTIES,
                        RenderUtils.getSPARQLQueryString(parent), RenderUtils.getSPARQLQueryString(parentPredicate),
                        RenderUtils.getSPARQLQueryString(type));
        this.log.info("queryString={}", queryString);
        return this.doSPARQL(queryString, artifacts);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#getPoddServerUrl()
     */
    @Override
    public String getPoddServerUrl()
    {
        String result = this.serverUrl;
        if(result == null)
        {
            synchronized(this)
            {
                result = this.serverUrl;
                if(result == null)
                {
                    this.serverUrl =
                            this.props.get(RestletPoddClientImpl.PROP_PODD_SERVER_URL,
                                    RestletPoddClientImpl.DEFAULT_PODD_SERVER_URL);
                }
                result = this.serverUrl;
            }
        }
        return result;
    }
    
    public PropertyUtil getProps()
    {
        return this.props;
    }
    
    /**
     * Creates the URL for a given path using the current {@link #getPoddServerUrl()} result, or
     * throws an IllegalStateException if the server URL has not been set.
     *
     * @param path
     *            The path of the web service to get a full URL for.
     * @return The full URL to the given path.
     * @throws IllegalStateException
     *             If {@link #setPoddServerUrl(String)} has not been called with a valid URL before
     *             this point.
     */
    private String getUrl(final String path)
    {
        if(this.serverUrl == null)
        {
            throw new IllegalStateException("PODD Server URL has not been set for this client");
        }
        
        if(path == null)
        {
            throw new NullPointerException("Path cannot be null");
        }
        
        if(!path.startsWith("/"))
        {
            return this.serverUrl + "/" + path;
        }
        else
        {
            return this.serverUrl + path;
        }
    }
    
    @Override
    public PoddUser getUserDetails(final String userIdentifier) throws PoddClientException
    {
        this.log.info("cookies: {}", this.currentCookies);
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        resource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, userIdentifier);
        
        resource.getCookies().addAll(this.currentCookies);
        
        try
        {
            final Representation getResponse = resource.get(RestletUtilMediaType.APPLICATION_RDF_JSON);
            
            if(!resource.getStatus().equals(Status.SUCCESS_OK))
            {
                throw new PoddClientException("Server returned a non-success status code: "
                        + resource.getStatus().toString());
            }
            
            final InputStream stream = getResponse.getStream();
            
            if(stream == null)
            {
                throw new PoddClientException("Did not receive valid response from server");
            }
            
            final RDFFormat format =
                    Rio.getParserFormatForMIMEType(getResponse.getMediaType().getName(), RDFFormat.RDFXML);
            
            final Model model = Rio.parse(stream, "", format);
            
            final PoddUser poddUser = PoddUser.fromModel(model);
            
            return poddUser;
        }
        catch(final RDFParseException e)
        {
            throw new PoddClientException("Failed to parse RDF", e);
        }
        catch(final ResourceException e)
        {
            throw new PoddClientException("Failed to communicate with PODD Server", e);
        }
        catch(final IOException e)
        {
            throw new PoddClientException("Input output exception while parsing RDF", e);
        }
    }
    
    @Override
    public boolean isLoggedIn()
    {
        return !this.currentCookies.isEmpty();
    }
    
    @Override
    public Model listArtifacts(final boolean published, final boolean unpublished) throws PoddClientException
    {
        this.log.info("cookies: {}", this.currentCookies);
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_LIST));
        resource.getCookies().addAll(this.currentCookies);
        
        resource.addQueryParameter(PoddWebConstants.KEY_PUBLISHED, Boolean.toString(published));
        resource.addQueryParameter(PoddWebConstants.KEY_UNPUBLISHED, Boolean.toString(unpublished));
        
        try
        {
            final Representation getResponse = resource.get(RestletUtilMediaType.APPLICATION_RDF_JSON);
            
            if(!resource.getStatus().equals(Status.SUCCESS_OK))
            {
                throw new PoddClientException("Server returned a non-success status code: "
                        + resource.getStatus().toString());
            }
            
            final InputStream stream = getResponse.getStream();
            
            if(stream == null)
            {
                throw new PoddClientException("Did not receive valid response from server");
            }
            
            final RDFFormat format =
                    Rio.getParserFormatForMIMEType(getResponse.getMediaType().getName(), RDFFormat.RDFXML);
            
            return Rio.parse(stream, "", format);
        }
        catch(final RDFParseException e)
        {
            throw new PoddClientException("Failed to parse RDF", e);
        }
        catch(final ResourceException e)
        {
            throw new PoddClientException("Failed to communicate with PODD Server", e);
        }
        catch(final IOException e)
        {
            throw new PoddClientException("Input output exception while parsing RDF", e);
        }
    }
    
    private Map<InferredOWLOntologyID, String> listArtifactsInternal(final boolean published, final boolean unpublished)
        throws PoddClientException
    {
        Map<InferredOWLOntologyID, String> results = new ConcurrentHashMap<>();
        Model model = this.listArtifacts(published, unpublished);
        for(InferredOWLOntologyID ontologyID : OntologyUtils.modelToOntologyIDs(model, false, false))
        {
            String nextLabel =
                    model.filter(
                            model.filter(ontologyID.getOntologyIRI().toOpenRDFURI(), PODDBASE.ARTIFACT_HAS_TOP_OBJECT,
                                    null).objectURI(), RDFS.LABEL, null).objectLiteral().getLabel();
            results.put(ontologyID, nextLabel);
        }
        return results;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#listDataReferenceRepositories()
     */
    @Override
    public List<String> listDataReferenceRepositories() throws PoddClientException
    {
        this.log.info("cookies: {}", this.currentCookies);
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
        resource.getCookies().addAll(this.currentCookies);
        
        try
        {
            final Representation getResponse = resource.get(RestletUtilMediaType.APPLICATION_RDF_JSON);
            
            if(!resource.getStatus().equals(Status.SUCCESS_OK))
            {
                throw new PoddClientException("Server returned a non-success status code: "
                        + resource.getStatus().toString());
            }
            
            final InputStream stream = getResponse.getStream();
            
            if(stream == null)
            {
                throw new PoddClientException("Did not receive valid response from server");
            }
            
            final RDFFormat format =
                    Rio.getParserFormatForMIMEType(getResponse.getMediaType().getName(), RDFFormat.RDFXML);
            
            final Model model = Rio.parse(stream, "", format);
            
            // DebugUtils.printContents(model);
            
            final Set<Value> aliases = model.filter(null, PODD.PODD_BASE_HAS_ALIAS, null).objects();
            
            final List<String> aliasResults = new ArrayList<String>(aliases.size());
            for(final Value nextAlias : aliases)
            {
                aliasResults.add(((Literal)nextAlias).getLabel());
            }
            
            Collections.sort(aliasResults);
            
            return aliasResults;
        }
        catch(final RDFParseException e)
        {
            throw new PoddClientException("Failed to parse RDF", e);
        }
        catch(final ResourceException e)
        {
            throw new PoddClientException("Failed to communicate with PODD Server", e);
        }
        catch(final IOException e)
        {
            throw new PoddClientException("Input output exception while parsing RDF", e);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#listPublishedArtifacts()
     */
    @Override
    public Map<InferredOWLOntologyID, String> listPublishedArtifacts() throws PoddClientException
    {
        this.log.info("cookies: {}", this.currentCookies);
        
        return this.listArtifactsInternal(true, false);
    }
    
    @Override
    public Map<RestletUtilRole, Collection<String>> listRoles(final InferredOWLOntologyID artifactId)
        throws PoddClientException
    {
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_ROLES));
        resource.getCookies().addAll(this.currentCookies);
        resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactId.getOntologyIRI().toString());
        
        this.log.info("cookies: {}", this.currentCookies);
        
        final Representation get = resource.get(MediaType.APPLICATION_RDF_TURTLE);
        
        try
        {
            return PoddRoles.extractRoleMappingsArtifact(this.parseRdf(get));
        }
        catch(final IOException e)
        {
            throw new PoddClientException("Could not parse role details due to an IOException", e);
        }
        
    }
    
    @Override
    public Map<RestletUtilRole, Collection<URI>> listRoles(final String userIdentifier) throws PoddClientException
    {
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ROLES));
        resource.getCookies().addAll(this.currentCookies);
        if(userIdentifier != null)
        {
            resource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, userIdentifier);
        }
        
        this.log.info("cookies: {}", this.currentCookies);
        
        final Representation get = resource.get(MediaType.APPLICATION_RDF_TURTLE);
        
        try
        {
            return PoddRoles.extractRoleMappingsUser(this.parseRdf(get));
        }
        catch(final IOException e)
        {
            throw new PoddClientException("Could not parse role details due to an IOException", e);
        }
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#listUnpublishedArtifacts()
     */
    @Override
    public Map<InferredOWLOntologyID, String> listUnpublishedArtifacts() throws PoddClientException
    {
        this.log.info("cookies: {}", this.currentCookies);
        
        return this.listArtifactsInternal(false, true);
    }
    
    @Override
    public List<PoddUser> listUsers() throws PoddClientException
    {
        // Implement this when the service is available
        throw new RuntimeException("TODO: Implement listUsers!");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#login(java.lang.String, char[])
     */
    @Override
    public boolean login(final String username, final String password) throws PoddClientException
    {
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.DEF_PATH_LOGIN_SUBMIT));
        resource.getCookies().addAll(this.currentCookies);
        
        // TODO: when Cookies natively supported by Client Resource, or another method remove this
        // Until then, this is necessary to manually attach the cookies after login to the
        // redirected address.
        // GitHub issue for this: https://github.com/restlet/restlet-framework-java/issues/21
        resource.setFollowingRedirects(false);
        
        final Form form = new Form();
        form.add("username", username);
        form.add("password", password);
        
        final Representation rep = resource.post(form.getWebRepresentation(CharacterSet.UTF_8));
        
        try
        {
            this.log.info("login result status: {}", resource.getStatus());
            if(rep != null)
            {
                // FIXME: Representation.getText may be implemented badly, so avoid calling it
                // this.log.info("login result: {}", rep.getText());
            }
            else
            {
                this.log.info("login result was null");
            }
            
            // HACK
            if(resource.getStatus().equals(Status.REDIRECTION_SEE_OTHER) || resource.getStatus().isSuccess())
            {
                this.currentCookies = resource.getCookieSettings();
            }
            
            this.log.info("cookies: {}", this.currentCookies);
            
            return !this.currentCookies.isEmpty();
        }
        catch(final Throwable e)
        {
            this.currentCookies.clear();
            this.log.warn("Error with request", e);
            throw new PoddClientException(e);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#logout()
     */
    @Override
    public boolean logout() throws PoddClientException
    {
        this.log.info("cookies: {}", this.currentCookies);
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.DEF_PATH_LOGOUT));
        // add the cookie settings so that the server knows who to logout
        resource.getCookies().addAll(this.currentCookies);
        
        // TODO: when Cookies natively supported by Client Resource, or another method remove this
        // Until then, this is necessary to manually attach the cookies after login to the
        // redirected address.
        // GitHub issue for this: https://github.com/restlet/restlet-framework-java/issues/21
        resource.setFollowingRedirects(false);
        
        final Representation rep = resource.get();
        
        this.currentCookies = resource.getCookieSettings();
        
        try
        {
            this.log.info("logout result status: {}", resource.getStatus());
            
            if(rep != null)
            {
                // FIXME: Representation.getText may be implemented badly, so avoid calling it
                // this.log.info("logout result: {}", rep.getText());
            }
            else
            {
                this.log.info("logout result was null");
            }
            
            this.log.info("cookies: {}", this.currentCookies);
            
            this.currentCookies.clear();
            
            return true;
        }
        catch(final Throwable e)
        {
            this.log.warn("Error with request", e);
            throw new PoddClientException(e);
        }
    }
    
    private Model parseRdf(final Representation rep) throws PoddClientException, IOException
    {
        final RDFFormat format = Rio.getParserFormatForMIMEType(rep.getMediaType().getName());
        
        if(format == null)
        {
            throw new PoddClientException("Did not understand the format for the RDF response: "
                    + rep.getMediaType().getName());
        }
        
        try
        {
            return Rio.parse(rep.getStream(), "", format);
        }
        catch(RDFParseException | UnsupportedRDFormatException e)
        {
            throw new PoddClientException("There was an error parsing the artifact", e);
        }
    }
    
    /**
     * @param resource
     * @param rdf
     * @return
     * @throws PoddClientException
     * @throws ResourceException
     */
    private Representation postRdf(final ClientResource resource, final Model rdf) throws PoddClientException,
        ResourceException
    {
        final StringWriter writer = new StringWriter();
        
        try
        {
            Rio.write(rdf, writer, RDFFormat.RDFJSON);
        }
        catch(final RDFHandlerException e)
        {
            throw new PoddClientException("Could not generate request entity", e);
        }
        
        final Representation rep =
                new ReaderRepresentation(new StringReader(writer.toString()), RestletUtilMediaType.APPLICATION_RDF_JSON);
        
        final Representation post = resource.post(rep, RestletUtilMediaType.APPLICATION_RDF_JSON);
        return post;
    }
    
    @Override
    public InferredOWLOntologyID publishArtifact(final InferredOWLOntologyID ontologyIRI) throws PoddClientException
    {
        throw new RuntimeException("TODO: Implement publishArtifact!");
    }
    
    @Override
    public void removeRole(final String userIdentifier, final RestletUtilRole role, final InferredOWLOntologyID artifact)
        throws PoddClientException
    {
        this.log.info("cookies: {}", this.currentCookies);
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ROLES));
        resource.getCookies().addAll(this.currentCookies);
        resource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, userIdentifier);
        resource.addQueryParameter(PoddWebConstants.KEY_DELETE, "true");
        
        final Map<RestletUtilRole, Collection<URI>> mappings = new HashMap<>();
        final Collection<URI> artifacts = Arrays.asList(artifact.getOntologyIRI().toOpenRDFURI());
        mappings.put(role, artifacts);
        
        final Model model = new LinkedHashModel();
        PoddRoles.dumpRoleMappingsUser(mappings, model);
        
        final Representation post = this.postRdf(resource, model);
        
        try
        {
            final Model parsedStatements = this.parseRdf(post);
            
            if(!parsedStatements.contains(null, SesameRealmConstants.OAS_USERIDENTIFIER,
                    PODD.VF.createLiteral(userIdentifier)))
            {
                this.log.warn("Role edit response did not seem to contain the user identifier");
            }
        }
        catch(final IOException e)
        {
            throw new PoddClientException("Could not parse results due to an IOException", e);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#setPoddServerUrl(java.lang.String)
     */
    @Override
    public void setPoddServerUrl(final String serverUrl)
    {
        this.serverUrl = serverUrl;
    }
    
    public void setProps(final PropertyUtil props)
    {
        this.props = props;
    }
    
    @Override
    public InferredOWLOntologyID unpublishArtifact(final InferredOWLOntologyID ontologyIRI) throws PoddClientException
    {
        throw new RuntimeException("TODO: Implement unpublishArtifact");
    }
    
    @Override
    public InferredOWLOntologyID updateArtifact(final InferredOWLOntologyID ontologyIRI,
            final InputStream fullInputStream, final RDFFormat format) throws PoddClientException
    {
        throw new RuntimeException("TODO: Implement updateArtifact");
    }
    
    @Override
    public InferredOWLOntologyID uploadNewArtifact(final InputStream input, final RDFFormat format)
        throws PoddClientException
    {
        return this.uploadNewArtifact(input, format, DanglingObjectPolicy.REPORT,
                DataReferenceVerificationPolicy.DO_NOT_VERIFY);
    }
    
    @Override
    public InferredOWLOntologyID uploadNewArtifact(final InputStream input, final RDFFormat format,
            final DanglingObjectPolicy danglingObjectPolicy,
            final DataReferenceVerificationPolicy dataReferenceVerificationPolicy) throws PoddClientException
    {
        final InputRepresentation rep = new InputRepresentation(input, MediaType.valueOf(format.getDefaultMIMEType()));
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        resource.getCookies().addAll(this.currentCookies);
        
        this.log.info("cookies: {}", this.currentCookies);
        
        resource.addQueryParameter("format", format.getDefaultMIMEType());
        if(danglingObjectPolicy == DanglingObjectPolicy.FORCE_CLEAN)
        {
            resource.addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_FORCE, "true");
        }
        if(dataReferenceVerificationPolicy == DataReferenceVerificationPolicy.VERIFY)
        {
            resource.addQueryParameter(PoddWebConstants.KEY_EDIT_VERIFY_FILE_REFERENCES, "true");
        }
        
        // Request the results in Turtle to reduce the bandwidth
        final Representation post = resource.post(rep, MediaType.APPLICATION_RDF_TURTLE);
        
        try
        {
            final Model parsedStatements = this.parseRdf(post);
            
            final Collection<InferredOWLOntologyID> result =
                    OntologyUtils.modelToOntologyIDs(parsedStatements, true, false);
            
            if(!result.isEmpty())
            {
                return result.iterator().next();
            }
            
            throw new PoddClientException("Failed to verify that the artifact was uploaded correctly.");
        }
        catch(final IOException e)
        {
            throw new PoddClientException("Could not parse artifact details due to an IOException", e);
        }
    }
    
}
