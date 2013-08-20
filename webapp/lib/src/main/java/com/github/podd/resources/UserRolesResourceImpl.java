/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.RestletUtilUser;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddRoles;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * @author kutila
 */
public class UserRolesResourceImpl extends AbstractPoddResourceImpl
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Display the HTML page for User Role Management
     */
    @Get(":html")
    public Representation getRoleManagementPageHtml(final Representation entity) throws ResourceException
    {
        this.log.info("getRoleManagementHtml");
        
        final String requestedUserIdentifier =
                (String)this.getRequest().getAttributes().get(PoddWebConstants.KEY_USER_IDENTIFIER);
        this.log.info("requesting role management of user: {}", requestedUserIdentifier);
        
        if(requestedUserIdentifier == null)
        {
            // no identifier specified.
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not specify user");
        }
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        // identify needed Action
        PoddAction action = PoddAction.OTHER_USER_EDIT;
        if(user != null && requestedUserIdentifier.equals(user.getIdentifier()))
        {
            action = PoddAction.CURRENT_USER_EDIT;
        }
        
        this.checkAuthentication(action);
        
        // completed checking authorization
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "editUserRoles.html.ftl");
        dataModel.put("pageTitle", "User Role Management");
        dataModel.put("authenticatedUserIdentifier", user.getIdentifier());
        
        final PoddSesameRealm realm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        final PoddUser poddUser = (PoddUser)realm.findUser(requestedUserIdentifier);
        
        if(poddUser == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "User not found.");
        }
        else
        {
            dataModel.put("requestedUser", poddUser);
            
            // - include all available PoddRoles
            dataModel.put("repositoryRolesList", PoddRoles.getRepositoryRoles());

            // - include user's current Roles and optional mapped objects
            final List<Entry<RestletUtilRole, PoddObjectLabel>> roleList = this.getUsersRoles(realm, poddUser);
            
            dataModel.put("userRoleList", roleList);
        }
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }

    /**
     * Retrieve the Roles that are mapped to this User, together with details of any optional mapped
     * objects.
     * 
     * TODO: Retrieve Repository Roles and Project Roles separately so that they can be displayed
     * separately in the Role Management screen. Reason behind this is that it is better to make
     * Project Roles read-only from this view.
     * 
     * @param realm
     * @param poddUser
     *            The PODD User whose Roles are requested
     */
    private List<Entry<RestletUtilRole, PoddObjectLabel>> getUsersRoles(final PoddSesameRealm realm,
            final PoddUser poddUser)
    {
        final List<Entry<RestletUtilRole, PoddObjectLabel>> roleList = new LinkedList<Entry<RestletUtilRole, PoddObjectLabel>>();
        
        final Collection<Entry<Role,URI>> rolesWithObjectMappings = realm.getRolesWithObjectMappings(poddUser);
        for(Iterator<Entry<Role, URI>> iterator = rolesWithObjectMappings.iterator(); iterator.hasNext();)
        {
            final Entry<Role, URI> entry = iterator.next();
                final RestletUtilRole roleByName = PoddRoles.getRoleByName(entry.getKey().getName());
                PoddObjectLabel poddObjectLabel = null;
                
            final URI artifactUri = entry.getValue();
            if(artifactUri != null)
            {
                try
                {
                    final InferredOWLOntologyID artifact =
                            this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
                    final List<PoddObjectLabel> topObjectLabels =
                            this.getPoddArtifactManager().getTopObjectLabels(Arrays.asList(artifact));
                    if(!topObjectLabels.isEmpty())
                    {
                        poddObjectLabel = topObjectLabels.get(0);
                    }
                }
                catch(OpenRDFException | UnmanagedArtifactIRIException e)
                {
                    // either the artifact mapped to this Role does not exist, or a Label for it
                    // could not be retrieved
                    this.log.warn("Failed to retrieve Role Mapped Object [{}]", artifactUri);
                }
            }
            roleList.add(new AbstractMap.SimpleEntry<RestletUtilRole, PoddObjectLabel>(roleByName, poddObjectLabel));
        }
        return roleList;
    }
    
    
    /**
     * Handle an HTTP POST request submitting RDF data to update (i.e. map/unmap) a PoddUser's
     * Roles. <br>
     * <br>
     * User authorization is checked for each Role to modify. The service proceeds to modify Role
     * mappings in the Realm only if the current user has sufficient privileges carry out ALL the
     * modifications.
     */
    @Post("rdf|rj|json|ttl")
    public Representation editUserRolesRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        this.log.info("editUserRolesRdf");
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);

        
        final String userIdentifier =
                (String)this.getRequest().getAttributes().get(PoddWebConstants.KEY_USER_IDENTIFIER);
        this.log.info("editing Roles of user: {}", userIdentifier);

        final PoddSesameRealm nextRealm = ((PoddWebServiceApplication)this.getApplication()).getRealm();

        // - validate User whose Roles are to be edited
        if(userIdentifier == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not specify user to edit Roles");
        }
        final RestletUtilUser restletUserToUpdate = nextRealm.findUser(userIdentifier);
        if (restletUserToUpdate == null || !(restletUserToUpdate instanceof PoddUser))
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User not found");
        }
        final PoddUser poddUser = (PoddUser) restletUserToUpdate;

        // - retrieve 'delete' parameter
        boolean isDelete = false;
        final String deleteQueryParam = this.getQuery().getFirstValue(PoddWebConstants.KEY_DELETE);
        if (deleteQueryParam != null)
        {
            isDelete = Boolean.valueOf(deleteQueryParam);
        }
        this.log.info(" edit Roles is a 'delete' = {}", isDelete);
        
        final List<Entry<Role, URI>> rolesToEdit = this.extractRoleMappingsFromRequestBody(entity);
        
        // - check authorization for each Role mapping
        for (Entry<Role, URI> entry : rolesToEdit)
        {
            final Role role = entry.getKey();
            final URI mappedUri = entry.getValue();
            
            PoddAction action = PoddAction.PROJECT_ROLE_EDIT;
            if (PoddRoles.getRepositoryRoles().contains(role))
            {
                action = PoddAction.REPOSITORY_ROLE_EDIT;
                if(mappedUri != null)
                {
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                            "Unwanted optional Object URI found");
                }
            }
            this.checkAuthentication(action, mappedUri); 
        }
            
        // - do the mapping/unmapping of Roles
        for (Entry<Role, URI> entry : rolesToEdit)
        {
            final Role role = entry.getKey();
            final URI mappedUri = entry.getValue();
            
            if (isDelete)
            {
                nextRealm.unmap(poddUser, role, mappedUri);
                this.log.info(" User [{}] unmapped from Role [{}]", poddUser.getIdentifier(), role.getName());
            }
            else
            {
                nextRealm.map(poddUser, role, mappedUri);
                this.log.info(" User [{}] mapped to Role [{}], [{}]", poddUser.getIdentifier(), role.getName(), mappedUri);
            }
        }
        
        // - prepare response
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        final RDFFormat outputFormat =
                Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
        try
        {
            final Model model = new LinkedHashModel();
            model.add(poddUser.getUri(), SesameRealmConstants.OAS_USERIDENTIFIER,
                    PoddRdfConstants.VF.createLiteral(poddUser.getIdentifier()));
            Rio.write(model, output, outputFormat);
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat.getDefaultMIMEType()));
    }

    /**
     * 
     * @param entity
     * @return
     */
    private List<Entry<Role, URI>>  extractRoleMappingsFromRequestBody(final Representation entity)
    {
        final List<Entry<Role, URI>> roles = new LinkedList<Entry<Role, URI>>();
        
        try
        {
            // parse input content to a Model
            final InputStream inputStream = entity.getStream();
            final RDFFormat inputFormat =
                    Rio.getParserFormatForMIMEType(entity.getMediaType().getName(), RDFFormat.RDFXML);
            final Model model = Rio.parse(inputStream, "", inputFormat);

            // extract Role Mapping info (User details are ignored as multiple users are not supported)
            final Collection<Resource> roleMappingUris = model.filter(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING).subjects();
            for(Iterator<Resource> iterator = roleMappingUris.iterator(); iterator.hasNext();)
            {
                final Resource mappingUri = iterator.next();
                
                final URI roleUri =
                        model.filter(mappingUri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objectURI();
                final RestletUtilRole role = PoddRoles.getRoleByUri(roleUri);
                
                final URI mappedObject =
                        model.filter(mappingUri, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, null).objectURI();
                
                this.log.debug("Extracted Role <{}> with Optional Object <{}>", role.getName(), mappedObject);
                roles.add(new AbstractMap.SimpleEntry<Role, URI>(role.getRole(), mappedObject));
            }

            return roles;
        }
        catch(final IOException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
        }
    }

}
