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
package com.github.podd.restlet;

import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.AppendableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.podd.api.PoddArtifactManager;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddObjectLabelImpl;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUser;

import freemarker.template.Configuration;

/**
 * Wraps up calls to methods that relate to Restlet items, such as Representations.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 *         Copied from https://github.com/ansell/oas
 */
public final class RestletUtils
{
    private static final Logger log = LoggerFactory.getLogger(RestletUtils.class);
    
    public static Map<String, Object> getBaseDataModel(final Request nextRequest)
    {
        final ClientInfo nextClientInfo = nextRequest.getClientInfo();
        final Map<String, Object> dataModel = new TreeMap<String, Object>();
        dataModel.put("resourceRef", nextRequest.getResourceRef());
        dataModel.put("rootRef", nextRequest.getRootRef());
        dataModel.put("keywords", "podd, ontology, phenomics");
        
        String baseUrl = nextRequest.getRootRef().toString();
        if(baseUrl.endsWith("/"))
        {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        dataModel.put("baseUrl", baseUrl);
        
        dataModel.put("clientInfo", nextClientInfo);
        dataModel.put("isAuthenticated", nextClientInfo.isAuthenticated());
        final List<Role> roles = nextClientInfo.getRoles();
        final boolean isAdmin = roles.contains(PoddRoles.ADMIN.getRole());
        dataModel.put("isAdmin", isAdmin);
        dataModel.put("user", nextClientInfo.getUser());
        
        final User currentUser = nextClientInfo.getUser();
        if(currentUser != null)
        {
            dataModel.put("currentUserName", currentUser.getName());
            
            RestletUtils.log.debug("currentUser: {}", currentUser);
            RestletUtils.log.trace("currentUser.getFirstName: {}", currentUser.getFirstName());
            RestletUtils.log.trace("currentUser.getLastName: {}", currentUser.getLastName());
            RestletUtils.log.trace("currentUser.getName: {}", currentUser.getName());
            RestletUtils.log.trace("currentUser.getIdentifier: {}", currentUser.getIdentifier());
        }
        else
        {
            RestletUtils.log.info("No currentUser logged in");
        }
        
        return dataModel;
    }
    
    /**
     * Tests the parameter against a list of known true parameter values, before testing it against
     * a list of known false values.
     * 
     * @param nextParameter
     *            A parameter to test for its boolean value.
     * @return True if the parameter looks like a true value and false otherwise.
     */
    public static boolean getBooleanFromParameter(final Parameter nextParameter)
    {
        if(nextParameter == null)
        {
            throw new IllegalArgumentException("Cannot get a boolean from a null parameter");
        }
        
        final String paramValue = nextParameter.getValue();
        
        if(paramValue == null)
        {
            return false;
        }
        
        boolean result = false;
        
        // If that was true, return true
        if(Boolean.valueOf(paramValue))
        {
            result = true;
        }
        else if(paramValue.equalsIgnoreCase("false"))
        {
            result = false;
        }
        else if(paramValue.equalsIgnoreCase("yes"))
        {
            result = true;
        }
        else if(paramValue.equalsIgnoreCase("no"))
        {
            result = false;
        }
        else if(paramValue.equalsIgnoreCase("y"))
        {
            result = true;
        }
        else if(paramValue.equalsIgnoreCase("n"))
        {
            result = false;
        }
        
        return result;
    }
    
    /**
     * Returns a templated representation dedicated to HTML content.
     * 
     * @param templateName
     *            The name of the template.
     * @param dataModel
     *            The collection of data processed by the template engine.
     * @param mediaType
     *            The media type of the representation.
     * @param freemarkerConfiguration
     *            The FreeMarker template configuration
     * @return The representation.
     */
    public static Representation getHtmlRepresentation(final String templateName, final Map<String, Object> dataModel,
            final MediaType mediaType, final Configuration freemarkerConfiguration) throws ResourceException
    {
        // The template representation is based on Freemarker.
        return new TemplateRepresentation(templateName, freemarkerConfiguration, dataModel, mediaType);
    }
    
    public static Map<RestletUtilRole, Collection<URI>> getUsersRoles(final PoddSesameRealm realm,
            final PoddUser poddUser)
    {
        final ConcurrentMap<RestletUtilRole, Collection<URI>> results = new ConcurrentHashMap<>();
        
        // extract Role Mapping info (User details are ignored as multiple users are not
        // supported)
        final Collection<Entry<Role, URI>> rolesWithObjectMappings = realm.getRolesWithObjectMappings(poddUser);
        for(final Entry<Role, URI> entry : rolesWithObjectMappings)
        {
            final RestletUtilRole role = PoddRoles.getRoleByName(entry.getKey().getName());
            final URI artifactUri = entry.getValue();
            
            Collection<URI> nextObjectUris = new HashSet<>();
            final Collection<URI> putIfAbsent = results.putIfAbsent(role, nextObjectUris);
            if(putIfAbsent != null)
            {
                nextObjectUris = putIfAbsent;
            }
            nextObjectUris.add(artifactUri);
        }
        
        return results;
    }
    
    /**
     * Retrieve the Roles that are mapped to this User, together with details of any optional mapped
     * objects.
     * 
     * @param realm
     * @param poddUser
     *            The PODD User whose Roles are requested
     */
    public static List<Entry<RestletUtilRole, PoddObjectLabel>> getUsersRoles(final PoddSesameRealm realm,
            final PoddUser poddUser, final PoddArtifactManager artifactManager)
    {
        final Map<RestletUtilRole, Collection<URI>> roles = RestletUtils.getUsersRoles(realm, poddUser);
        
        final List<Entry<RestletUtilRole, PoddObjectLabel>> results =
                new LinkedList<Entry<RestletUtilRole, PoddObjectLabel>>();
        
        for(final RestletUtilRole role : roles.keySet())
        {
            for(final URI artifactUri : roles.get(role))
            {
                PoddObjectLabel poddObjectLabel = null;
                
                if(artifactUri != null)
                {
                    try
                    {
                        final InferredOWLOntologyID artifact = artifactManager.getArtifact(IRI.create(artifactUri));
                        final List<PoddObjectLabel> topObjectLabels =
                                artifactManager.getTopObjectLabels(Arrays.asList(artifact));
                        if(!topObjectLabels.isEmpty())
                        {
                            poddObjectLabel = topObjectLabels.get(0);
                        }
                        else
                        {
                            RestletUtils.log.error("Failed to find label for artifact, returning URI as label");
                            poddObjectLabel = new PoddObjectLabelImpl(artifact, artifactUri, artifactUri.stringValue());
                        }
                    }
                    catch(OpenRDFException | UnmanagedArtifactIRIException e)
                    {
                        // either the artifact mapped to this Role does not exist, or a Label for it
                        // could not be retrieved
                        RestletUtils.log.warn("Failed to retrieve Role Mapped Object [{}]", artifactUri);
                    }
                }
                results.add(new AbstractMap.SimpleEntry<RestletUtilRole, PoddObjectLabel>(role, poddObjectLabel));
            }
        }
        return results;
    }
    
    /**
     * Serialises part or all of a repository into RDF, depending on which contexts are provided.
     * 
     * @param mimeType
     *            The MIME type of the serialised RDF statements.
     * @param myRepository
     *            The repository containing the RDF statements to serialise.
     * @param contexts
     *            0 or more Resources identifying contexts in the repository to serialise.
     * @return A Restlet Representation containing the
     * @throws RepositoryException
     * @throws RDFHandlerException
     */
    public static Representation toRDFSerialisation(final String mimeType, final Repository myRepository,
            final Resource... contexts) throws RepositoryException, RDFHandlerException
    {
        if(myRepository == null)
        {
            throw new IllegalArgumentException("Repository cannot be null");
        }
        
        // Attempt to find a writer format based on their requested mime type, or if that fails,
        // give them RDF/XML that every RDF library can process.
        final RDFFormat outputFormat = Rio.getWriterFormatForMIMEType(mimeType, RDFFormat.RDFXML);
        
        final StringWriter writer = new StringWriter();
        
        RepositoryConnection conn = null;
        
        conn = myRepository.getConnection();
        
        final RDFHandler output = Rio.createWriter(outputFormat, writer);
        
        conn.export(output, contexts);
        
        // TODO: find a subclass of Representation that accepts a writer directly, without having to
        // serialise it to a string, to improve performance for large results sets.
        final Representation result =
                new AppendableRepresentation(writer.toString(), MediaType.valueOf(outputFormat.getDefaultMIMEType()),
                        Language.DEFAULT, CharacterSet.UTF_8);
        
        return result;
        
    }
    
    /**
     * Private default constructor
     */
    private RestletUtils()
    {
    }
    
    /**
     * Populate the data model with info about the parent of the current object. If the given object
     * does not have a parent (i.e. is a Top Object) the data model remains unchanged.
     * 
     * TODO: This method uses multiple API methods resulting in several SPARQL queries. Efficiency
     * could be improved by either adding a new API method or modifying getParentDetails() to supply
     * most of the required information.
     * 
     * @param ontologyID
     * @param objectUri
     *            The object whose parent details are required
     * @param dataModel
     * @throws OpenRDFException
     */
    public static void populateParentDetails(final PoddArtifactManager artifactManager,
            final InferredOWLOntologyID ontologyID, final URI objectUri, final Map<String, Object> dataModel)
        throws OpenRDFException
    {
        
        final Model parentDetails = artifactManager.getParentDetails(ontologyID, objectUri);
        if(parentDetails.size() == 1)
        {
            final Statement statement = parentDetails.iterator().next();
            
            final Map<String, String> parentMap = new HashMap<>();
            
            final String parentUriString = statement.getSubject().stringValue();
            parentMap.put("uri", parentUriString);
            
            final URI parentUri = PoddRdfConstants.VF.createURI(parentUriString);
            final URI parentPredicateUri = PoddRdfConstants.VF.createURI(statement.getPredicate().stringValue());
            
            // - parent's Title
            String parentLabel = "Missing Title";
            final PoddObjectLabel objectLabel = artifactManager.getObjectLabel(ontologyID, parentUri);
            if(objectLabel != null)
            {
                parentLabel = objectLabel.getLabel();
            }
            parentMap.put("label", parentLabel);
            
            // - parent relationship Label
            String predicateLabel = "Missing parent relationship";
            final PoddObjectLabel predicateLabelModel = artifactManager.getObjectLabel(ontologyID, parentPredicateUri);
            if(predicateLabelModel != null)
            {
                predicateLabel = predicateLabelModel.getLabel();
            }
            parentMap.put("relationship", predicateLabel);
            
            // - parent's Type
            String parentType = "Unknown Type";
            final List<PoddObjectLabel> objectTypes = artifactManager.getObjectTypes(ontologyID, parentUri);
            if(objectTypes.size() > 0)
            {
                parentType = objectTypes.get(0).getLabel();
            }
            parentMap.put("type", parentType);
            
            dataModel.put("parentObject", parentMap);
        }
    }
    
    /**
     * Finds the parent details given an object, which may be null, and the artifact that it
     * expected to be found in.
     * 
     * @param artifactManager
     *            The artifact manager.
     * @param ontologyID
     *            The details of the artifact.
     * @param objectToView
     *            If this is null, the top object will be found, otherwise, the details for this
     *            object will be found if possible.
     * @return The details for the parent object, including a label if possible and its URI.
     * @throws OpenRDFException
     *             If there are errors getting the labels
     * @throws ResourceException
     *             If there is more than one top object.
     */
    public static PoddObjectLabel getParentDetails(final PoddArtifactManager artifactManager,
            final InferredOWLOntologyID ontologyID, final String objectToView) throws OpenRDFException,
        ResourceException
    {
        PoddObjectLabel theObject = null;
        
        if(objectToView != null && !objectToView.trim().isEmpty())
        {
            theObject = artifactManager.getObjectLabel(ontologyID, PoddRdfConstants.VF.createURI(objectToView));
        }
        else
        {
            // find and set top-object of this artifact as the object to display
            final List<PoddObjectLabel> topObjectLabels = artifactManager.getTopObjectLabels(Arrays.asList(ontologyID));
            if(topObjectLabels == null || topObjectLabels.size() != 1)
            {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "There should be only 1 top object");
            }
            
            theObject = topObjectLabels.get(0);
        }
        
        return theObject;
    }
    
}