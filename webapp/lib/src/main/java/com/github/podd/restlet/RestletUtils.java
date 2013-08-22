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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
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
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.AppendableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.utils.PoddRdfConstants;

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
            
            RestletUtils.log.info("currentUser: {}", currentUser);
            RestletUtils.log.info("currentUser.getFirstName: {}", currentUser.getFirstName());
            RestletUtils.log.info("currentUser.getLastName: {}", currentUser.getLastName());
            RestletUtils.log.info("currentUser.getName: {}", currentUser.getName());
            RestletUtils.log.info("currentUser.getIdentifier: {}", currentUser.getIdentifier());
        }
        else
        {
            RestletUtils.log.info("No currentUser logged in");
        }
        
        return dataModel;
    }
    
    /**
     * Extracts role mappings, optionally to object URIs, from the given RDF statements.
     * 
     * @param model
     *            A set of RDF statements defining role mappings
     * @return A map of roles to collections of optional object URIs. If the collection contains a
     *         null element, then the role was not mapped to an object URI in at least one case.
     */
    public static Map<RestletUtilRole, Collection<URI>> extractRoleMappings(Model model, URI... contexts)
    {
        final ConcurrentMap<RestletUtilRole, Collection<URI>> results = new ConcurrentHashMap<>();
        
        // extract Role Mapping info (User details are ignored as multiple users are not
        // supported)
        for(Resource mappingUri : model.filter(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING, contexts)
                .subjects())
        {
            final URI roleUri =
                    model.filter(mappingUri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null, contexts).objectURI();
            final RestletUtilRole role = PoddRoles.getRoleByUri(roleUri);
            
            final URI mappedObject =
                    model.filter(mappingUri, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, null, contexts).objectURI();
            
            // this.log.debug("Extracted Role <{}> with Optional Object <{}>", role.getName(),
            // mappedObject);
            Collection<URI> nextObjectUris = new HashSet<>();
            Collection<URI> putIfAbsent = results.putIfAbsent(role, nextObjectUris);
            if(putIfAbsent != null)
            {
                nextObjectUris = putIfAbsent;
            }
            nextObjectUris.add(mappedObject);
        }
        
        return results;
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
    
}