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
package com.github.podd.resources;

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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
//import org.apache.commons.io.IOUtils;
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
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.ansell.restletutils.RestletUtilMediaType;
import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.api.DanglingObjectPolicy;
import com.github.podd.api.DataReferenceVerificationPolicy;
import com.github.podd.api.data.DataReference;
import com.github.podd.api.data.DataReferenceConstants;
import com.github.podd.exception.PoddException;
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
    
    public static final String DEFAULT_PODD_SERVER_URL = null;// "http://localhost:8080/podd/";
    
    public static final String PROP_PODD_USERNAME = "podd.username";
    
    public static final String PROP_PODD_PASSWORD = "podd.password";
    
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
        throws PoddException
    {
        this.log.debug("cookies: {}", this.currentCookies);
        
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
            try {
				throw new IOException("Could not parse results due to an IOException", e);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
    }
    
    @Override
    public InferredOWLOntologyID appendArtifact(final InferredOWLOntologyID ontologyIRI,
            final InputStream partialInputStream, final RDFFormat format) throws PoddException
    {
        return this.appendArtifact(ontologyIRI, partialInputStream, format, DanglingObjectPolicy.REPORT,
                DataReferenceVerificationPolicy.DO_NOT_VERIFY);
    }
    
    @Override
    public InferredOWLOntologyID appendArtifact(final InferredOWLOntologyID artifactID,
            final InputStream partialInputStream, final RDFFormat format,
            final DanglingObjectPolicy danglingObjectPolicy,
            final DataReferenceVerificationPolicy dataReferenceVerificationPolicy) throws PoddException
    {
        final InputRepresentation rep =
                new InputRepresentation(partialInputStream, MediaType.valueOf(format.getDefaultMIMEType()));
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT));
        resource.getCookies().addAll(this.currentCookies);
        
        this.log.debug("cookies: {}", this.currentCookies);
        
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
            
            throw new Exception("Failed to verify that the artifact was uploaded correctly.");
        }
        catch(final Exception e)
        {
        	System.out.println(e.toString());
        }
        return null;
    }
    
    @Override
    public Map<InferredOWLOntologyID, InferredOWLOntologyID> appendArtifacts(
            final Map<InferredOWLOntologyID, Model> uploadQueue) throws PoddException
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
    public boolean autologin() throws PoddException
    {
        final String username = this.getProps().get(RestletPoddClientImpl.PROP_PODD_USERNAME, null);
        final String password = this.getProps().get(RestletPoddClientImpl.PROP_PODD_PASSWORD, null);
        
        Objects.requireNonNull(username,
                "Cannot automatically login as username was not defined in poddclient.properties");
        Objects.requireNonNull(password,
                "Cannot automatically login as password was not defined in poddclient.properties");
        
        return this.login(username, password);
    }
    
    @Override
    public PoddUser createUser(final PoddUser user) throws PoddException
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
            
            this.log.debug("cookies: {}", this.currentCookies);
            
            resource.addQueryParameter("format", RDFFormat.RDFJSON.getDefaultMIMEType());
            
            // Request the results in Turtle to reduce the bandwidth
            final Representation post = resource.post(rep, MediaType.APPLICATION_RDF_TURTLE);
            
            final Model parsedStatements = this.parseRdf(post);
            
            final PoddUser result = PoddUser.fromModel(model);
            
            return result;
        }
        catch(final IOException | RDFHandlerException | ResourceException e)
        {
        	System.out.println(e.toString());
        }
        return null;
    }
    
    @Override
    public boolean deleteArtifact(final InferredOWLOntologyID artifactId) throws PoddException
    {
        this.log.debug("cookies: {}", this.currentCookies);
        
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
            try {
				throw new Exception("Failed to successfully delete artifact: "
				        + artifactId.getOntologyIRI().toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        return false;
    }
    
    public Representation doSPARQL2(final String queryString, final Collection<InferredOWLOntologyID> artifactIds)
        throws PoddException
    {
        this.log.debug("cookies: {}", this.currentCookies);
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SPARQL));
        resource.getCookies().addAll(this.currentCookies);
        
        final Form form = new Form();
        form.add(PoddWebConstants.KEY_SPARQLQUERY, queryString);
        
        // TODO: Parse query to make sure it is syntactically valid before sending query
        resource.addQueryParameter(PoddWebConstants.KEY_SPARQLQUERY, queryString);
        
        try
        {
        	final Representation get = resource.get(MediaType.APPLICATION_ALL_XML);
        	
        	 try {        		
        		DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
        	                             .newDocumentBuilder();
        		InputStream in = IOUtils.toInputStream(get.getText(), "UTF-8");
        		Document doc = dBuilder.parse(in);
        		System.out.println("\r\n" + "============================== \r\n" + " PODD Query Results \r\n"
                        + "==============================");
        		

        		if (doc.hasChildNodes()) {
        			printNote(doc.getChildNodes());
        		}

        	 } catch (Exception e) {
        		 System.out.println(e.getMessage());
        	 }
        	 
            return get;
            
        }
        catch(final ResourceException e)
        {
            if(e.getStatus().equals(Status.CLIENT_ERROR_PRECONDITION_FAILED))
            {
                // Precondition failed indicates that they do not have access to any artifacts, so
                // return empty results set
                return null;
            }
            else
            {
               
            }
            System.out.println(e.toString());
        }
        catch(final UnsupportedRDFormatException e)
        {
            // Attempt to retry the request once to avoid random restlet failures stopping the
            // entire process
            try
            {
                final Representation get =
                        resource.post(form.getWebRepresentation(CharacterSet.UTF_8),
                                RestletUtilMediaType.APPLICATION_RDF_JSON);
                
                // Pass the desired format to the get method of the ClientResource
                // final Representation get =
                // resource.get(RestletUtilMediaType.APPLICATION_RDF_JSON);
                
                final StringWriter writer = new StringWriter(4096);
                
                get.write(writer);
                return null;
            }
            catch(final ResourceException e1)
            {
                if(e1.getStatus().equals(Status.CLIENT_ERROR_PRECONDITION_FAILED))
                {
                    // Precondition failed indicates that they do not have access to any artifacts,
                    // so
                    // return empty results set
                    return null;
                }
                else
                {
                	System.out.println(e.toString());
                }
            }
            catch(final IOException | UnsupportedRDFormatException e1)
            {
            	System.out.println(e.toString());
            }
        }
        return null;
    }
    private static void printNote(NodeList nodeList) {

    	for (int count = 0; count < nodeList.getLength(); count++) {

    		Node tempNode = nodeList.item(count);

    		// make sure it's element node.
    		if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

    			// get node name and value
    			if (!tempNode.getNodeName().startsWith("rdf:") && tempNode.getTextContent().length() > 0) {
    				if (tempNode.getNodeName().startsWith("rdfs:label")) {
    					System.out.println("");
    					System.out.println("Name" + ": " + tempNode.getTextContent());
    				} else if (tempNode.getNodeName().startsWith("rdfs:comment")) {
    					System.out.println("Description" + ": " + tempNode.getTextContent());
    				} else {
    					System.out.println(tempNode.getNodeName() + ": " + tempNode.getTextContent());
    				}
    			}

    			if (tempNode.hasChildNodes()) {
    				// loop again if has child node
    				
    				printNote(tempNode.getChildNodes());
    				System.out.println("");
    				
    			}
    			
    		} 
    		
    	}
    }

    @Override
    public Model downloadArtifact(final InferredOWLOntologyID artifactId) throws PoddException
    {
        try
        {
            final Path tempFile = Files.createTempFile("downloadartifact-", ".rj");
            try (final OutputStream output = Files.newOutputStream(tempFile);)
            {
                this.downloadArtifact(artifactId, output, RDFFormat.RDFJSON);
            }
            try (final InputStream input = Files.newInputStream(tempFile);)
            {
                return Rio.parse(input, "", RDFFormat.RDFJSON);
            }
        }
        catch(RDFParseException | UnsupportedRDFormatException | IOException e)
        {
        	System.out.println(e.toString());
        }
        return null;
    }
    
    @Override
    public void downloadArtifact(final InferredOWLOntologyID artifactId, final OutputStream outputStream,
            final RDFFormat format) throws PoddException
    {
        Objects.requireNonNull(artifactId);
        Objects.requireNonNull(outputStream);
        Objects.requireNonNull(format);
        
        this.log.debug("cookies: {}", this.currentCookies);
        
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
            
        }
    }
    
    @Override
    public Model getObjectByURI(final URI objectURI, final Collection<InferredOWLOntologyID> artifacts)
        throws PoddException
    {
        final String queryString =
                String.format(PoddClient.TEMPLATE_SPARQL_BY_URI, RenderUtils.getSPARQLQueryString(objectURI));
        this.log.debug("queryString={}", queryString);
        return this.doSPARQL(queryString, artifacts);
    }
    
    @Override
    public Model getObjectsByType(final URI type, final Collection<InferredOWLOntologyID> artifacts)
        throws PoddException
    {
        final String queryString =
                String.format(PoddClient.TEMPLATE_SPARQL_BY_TYPE_WITH_LABEL, RenderUtils.getSPARQLQueryString(type));
        this.log.debug("queryString={}", queryString);
        return this.doSPARQL(queryString, artifacts);
    }
    
    @Override
    public Model getObjectsByTypeAndPrefix(final URI type, final String labelPrefix,
            final Collection<InferredOWLOntologyID> artifacts) throws PoddException
    {
        final String queryString =
                String.format(PoddClient.TEMPLATE_SPARQL_BY_TYPE_LABEL_STRSTARTS, RenderUtils.escape(labelPrefix),
                        RenderUtils.getSPARQLQueryString(type));
        this.log.debug("queryString={}", queryString);
        return this.doSPARQL(queryString, artifacts);
    }
    
    @Override
    public Model getObjectsByPredicate(final URI predicate, final Collection<InferredOWLOntologyID> artifacts)
        throws PoddException
    {
        final String predicateString = RenderUtils.getSPARQLQueryString(predicate);
        // NOTE: predicateString must be both the second and third arguments sent into String.format
        // as it is used twice, once for the Construct and once for the Where
        // Hypothetically the second could be different to the third for mapping predicates, but
        // that would cause confusion if not obvious
        final String queryString =
                String.format(PoddClient.TEMPLATE_SPARQL_BY_PREDICATE, predicateString, predicateString);
        this.log.debug("queryString={}", queryString);
        return this.doSPARQL(queryString, artifacts);
    }
    
    @Override
    public Model getObjectsByTypePredicateAndPrefix(final URI type, final URI predicate, final String labelPrefix,
            final Collection<InferredOWLOntologyID> artifacts) throws PoddException
    {
        final String predicateString = RenderUtils.getSPARQLQueryString(predicate);
        // NOTE: predicateString must be both the second and third arguments sent into String.format
        // as it is used twice, once for the Construct and once for the Where
        // Hypothetically the second could be different to the third for mapping predicates, but
        // that would cause confusion if not obvious
        final String queryString =
                String.format(PoddClient.TEMPLATE_SPARQL_BY_TYPE_LABEL_STRSTARTS_PREDICATE, predicateString,
                        predicateString, RenderUtils.escape(labelPrefix), RenderUtils.getSPARQLQueryString(type));
        this.log.debug("queryString={}", queryString);
        return this.doSPARQL(queryString, artifacts);
    }
    
    @Override
    public Model getObjectsByTypeAndBarcode(final URI type, final String barcode,
            final Collection<InferredOWLOntologyID> artifacts) throws PoddException
    {
        final String queryString =
                String.format(PoddClient.TEMPLATE_SPARQL_BY_BARCODE_STRSTARTS, RenderUtils.escape(barcode),
                        RenderUtils.getSPARQLQueryString(type));
        this.log.debug("queryString={}", queryString);
        return this.doSPARQL(queryString, artifacts);
    }
    
    @Override
    public Model getAllBarcodes(final Collection<InferredOWLOntologyID> artifacts)
    {
        try {
			return this.doSPARQL(PoddClient.TEMPLATE_SPARQL_BY_BARCODE_MATCH_NO_TYPE_NO_BARCODE, artifacts);
		} catch (PoddException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }
    
    @Override
    public Model getObjectsByBarcode(final String barcode, final Collection<InferredOWLOntologyID> artifacts)
        throws PoddException
    {
        final String queryString =
                String.format(PoddClient.TEMPLATE_SPARQL_BY_BARCODE_MATCH_NO_TYPE, RenderUtils.escape(barcode));
        this.log.debug("queryString={}", queryString);
        return this.doSPARQL(queryString, artifacts);
    }
    
    @Override
    public Model getObjectsByTypeAndParent(final URI parent, final URI parentPredicate, final URI type,
            final Collection<InferredOWLOntologyID> artifacts) throws PoddException
    {
        final String queryString =
                String.format(PoddClient.TEMPLATE_SPARQL_BY_TYPE_AND_PARENT_ALL_PROPERTIES,
                        RenderUtils.getSPARQLQueryString(parent), RenderUtils.getSPARQLQueryString(parentPredicate),
                        RenderUtils.getSPARQLQueryString(type));
        this.log.debug("queryString={}", queryString);
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
    
    public URI getTempURI(final String tempUriString)
    {
        return RestletPoddClientImpl.vf.createURI(RestletPoddClientImpl.TEMP_UUID_PREFIX + tempUriString
                + UUID.randomUUID().toString());
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
        if(this.getPoddServerUrl() == null)
        {
            throw new IllegalStateException("PODD Server URL has not been set for this client");
        }
        
        if(path == null || path.isEmpty())
        {
            throw new NullPointerException("Path cannot be null or empty");
        }
        
        String actualPath = path;
        
        final boolean serverUrlEndsWithSlash = this.serverUrl.endsWith("/");
        
        // Avoid double slashes
        if(actualPath.startsWith("/") && serverUrlEndsWithSlash)
        {
            actualPath = actualPath.substring(1);
        }
        else if(!serverUrlEndsWithSlash)
        {
            actualPath = "/" + actualPath;
        }
        
        final String result = this.serverUrl + actualPath;
        
        this.log.debug("getURL={}", result);
        
        return result;
    }
    

    
    @Override
    public boolean isLoggedIn()
    {
        return !this.currentCookies.isEmpty();
    }
   
    

    
    @Override
    public Map<RestletUtilRole, Collection<String>> listRoles(final InferredOWLOntologyID artifactId)
        throws PoddException
    {
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_ROLES));
        resource.getCookies().addAll(this.currentCookies);
        resource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactId.getOntologyIRI().toString());
        
        this.log.debug("cookies: {}", this.currentCookies);
        
        final Representation get = resource.get(MediaType.APPLICATION_RDF_TURTLE);
        
        try
        {
            return PoddRoles.extractRoleMappingsArtifact(this.parseRdf(get));
        }
        catch(final IOException e)
        {
            System.out.println(e.toString());
        }
        return null;
    }
    
    @Override
    public Map<RestletUtilRole, Collection<URI>> listRoles(final String userIdentifier) throws PoddException
    {
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ROLES));
        resource.getCookies().addAll(this.currentCookies);
        if(userIdentifier != null)
        {
            resource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, userIdentifier);
        }
        
        this.log.debug("cookies: {}", this.currentCookies);
        
        final Representation get = resource.get(MediaType.APPLICATION_RDF_TURTLE);
        
        try
        {
            return PoddRoles.extractRoleMappingsUser(this.parseRdf(get));
        }
        catch(final IOException e)
        {
        	System.out.println(e.toString());
        }
        return null;
    }
    
    
    @Override
    public List<PoddUser> listUsers() throws PoddException
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
    public boolean login(final String username, final String password) throws PoddException
    {
    	
        //final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.DEF_PATH_LOGIN_SUBMIT));
    	final ClientResource resource = new ClientResource("https://poddtest.plantphenomics.org.au/podd/login");
    	
        resource.getCookies().addAll(this.currentCookies);
        
        // TODO: when Cookies natively supported by Client Resource, or another method remove this
        // Until then, this is necessary to manually attach the cookies after login to the
        // redirected address.
        // GitHub issue for this: https://github.com/restlet/restlet-framework-java/issues/21
        resource.setFollowingRedirects(false);
        
        final Form form = new Form();
        form.add("username", username);
        form.add("password", password);

        try
        {
            final Representation rep = resource.post(form.getWebRepresentation(CharacterSet.UTF_8));
            
            this.log.debug("login result status: {}", resource.getStatus());
            if(rep != null)
            {
                // FIXME: Representation.getText may be implemented badly, so avoid calling it
                // this.log.debug("login result: {}", rep.getText());
            }
            else
            {
                this.log.debug("login result was null");
            }
            
            // HACK
            if(resource.getStatus().equals(Status.REDIRECTION_SEE_OTHER) || resource.getStatus().isSuccess())
            {
                this.currentCookies = resource.getCookieSettings();
            }
            
            this.log.debug("cookies: {}", this.currentCookies);
            
            return !this.currentCookies.isEmpty();
        }
        catch(final Throwable e)
        {
            this.currentCookies.clear();
            this.log.warn("Error with request", e);
            
        }
        return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.client.api.PoddClient#logout()
     */
    @Override
    public boolean logout() throws PoddException
    {
        this.log.debug("cookies: {}", this.currentCookies);
        
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
            
        }
        return false;
    }
    
    private Model parseRdf(final Representation rep) throws PoddException, IOException
    {
        final RDFFormat format = Rio.getParserFormatForMIMEType(rep.getMediaType().getName());
        
        /* if(format == null)
        {
            throw new PoddException("Did not understand the format for the RDF response: "
                    + rep.getMediaType().getName());
        } */
        
        try
        {
            return Rio.parse(rep.getStream(), "", format);
        }
        catch(RDFParseException | UnsupportedRDFormatException e)
        {
        	System.out.println(e.toString());
        }
        return null;
    }
    
    /**
     * @param resource
     * @param rdf
     * @return
     * @throws PoddException
     * @throws ResourceException
     */
    private Representation postRdf(final ClientResource resource, final Model rdf) throws PoddException,
        ResourceException
    {
        final StringWriter writer = new StringWriter();
        
        try
        {
            Rio.write(rdf, writer, RDFFormat.RDFJSON);
        }
        catch(final RDFHandlerException e)
        {
            
        }
        
        final Representation rep =
                new ReaderRepresentation(new StringReader(writer.toString()), RestletUtilMediaType.APPLICATION_RDF_JSON);
        
        final Representation post = resource.post(rep, RestletUtilMediaType.APPLICATION_RDF_JSON);
        return post;
    }
    
    @Override
    public InferredOWLOntologyID publishArtifact(final InferredOWLOntologyID ontologyIRI) throws PoddException
    {
        throw new RuntimeException("TODO: Implement publishArtifact!");
    }
    
    @Override
    public void removeRole(final String userIdentifier, final RestletUtilRole role, final InferredOWLOntologyID artifact)
        throws PoddException
    {
        this.log.debug("cookies: {}", this.currentCookies);
        
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
    public InferredOWLOntologyID unpublishArtifact(final InferredOWLOntologyID ontologyIRI) throws PoddException
    {
        throw new RuntimeException("TODO: Implement unpublishArtifact");
    }
    
    @Override
    public InferredOWLOntologyID updateArtifact(final InferredOWLOntologyID ontologyIRI,
            final InputStream fullInputStream, final RDFFormat format) throws PoddException
    {
        throw new RuntimeException("TODO: Implement updateArtifact");
    }
    
    @Override
    public InferredOWLOntologyID uploadNewArtifact(final Model model) throws PoddException
    {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try
        {
            Rio.write(model, output, RDFFormat.RDFJSON);
        }
        catch(final RDFHandlerException e)
        {
            
        }
        
        return this.uploadNewArtifact(new ByteArrayInputStream(output.toByteArray()), RDFFormat.RDFJSON,
                DanglingObjectPolicy.REPORT, DataReferenceVerificationPolicy.DO_NOT_VERIFY);
    }
    
    @Override
    public InferredOWLOntologyID uploadNewArtifact(final InputStream input, final RDFFormat format)
        throws PoddException
    {
        return this.uploadNewArtifact(input, format, DanglingObjectPolicy.REPORT,
                DataReferenceVerificationPolicy.DO_NOT_VERIFY);
    }
    
    @Override
    public InferredOWLOntologyID uploadNewArtifact(final InputStream input, final RDFFormat format,
            final DanglingObjectPolicy danglingObjectPolicy,
            final DataReferenceVerificationPolicy dataReferenceVerificationPolicy) throws PoddException
    {
        final InputRepresentation rep = new InputRepresentation(input, MediaType.valueOf(format.getDefaultMIMEType()));
        
        final ClientResource resource = new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
        resource.getCookies().addAll(this.currentCookies);
        
        this.log.debug("cookies: {}", this.currentCookies);
        
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
            
            
        }
        catch(final IOException e)
        {
            System.out.println(e.toString());
        }
        return null;
    }

	@Override
	public Model doSPARQL(String queryString, Collection<InferredOWLOntologyID> artifacts) throws PoddException {
		// TODO Auto-generated method stub
		return null;
	}
    
}
