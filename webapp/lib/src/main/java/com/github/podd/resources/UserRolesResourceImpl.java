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
import java.util.Set;

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
            dataModel.put("allRolesList", PoddRoles.values());

            // - include user's current Roles and optional mapped objects
            final List<Entry<RestletUtilRole, PoddObjectLabel>> roleList = getUsersRoles(realm, poddUser);
            
            dataModel.put("repositoryRoleList", roleList);
        }
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }

    /**
     * Retrieve the Roles that are mapped to this User, together with details of any optional mapped objects.
     * 
     * TODO: Retrieve Repository Roles and Project Roles separately so that they can be displayed separately
     *  in the Role Management screen. Reason behind this is that it is better to make Project Roles read-only from
     *  this view.
     * 
     * @param realm
     * @param poddUser
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
     * Handle an HTTP POST request submitting RDF data to update (i.e. map/unmap) a PoddUser's Roles
     */
    @Post("rdf|rj|json|ttl")
    public Representation editUserRolesRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        this.log.info("editUserRolesRdf");
        
        final String userIdentifier =
                (String)this.getRequest().getAttributes().get(PoddWebConstants.KEY_USER_IDENTIFIER);
        this.log.info("updating Roles of user: {}", userIdentifier);
        
        if(userIdentifier == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not specify user to update Roles");
        }
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);

        // check authentication first
        PoddAction action = PoddAction.OTHER_USER_EDIT;
        if(user != null && userIdentifier.equals(user.getIdentifier()))
        {
            action = PoddAction.CURRENT_USER_EDIT;
        }
        this.checkAuthentication(action);        
        
        final PoddSesameRealm nextRealm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        
        final RestletUtilUser restletUserToUpdate = nextRealm.findUser(userIdentifier);
        if (restletUserToUpdate == null || !(restletUserToUpdate instanceof PoddUser))
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User not found");
        }
        final PoddUser poddUser = (PoddUser) restletUserToUpdate;
        
        try
        {
            // - construct Model from input RDF content
            final InputStream inputStream = entity.getStream();
            final RDFFormat inputFormat =
                    Rio.getParserFormatForMIMEType(entity.getMediaType().getName(), RDFFormat.RDFXML);
            final Model incomingRoles = Rio.parse(inputStream, "", inputFormat);

            // - remove old Role Mappings in Realm
            final Set<Role> oldRoles = nextRealm.findRoles(poddUser);
            for(Iterator<Role> iterator1 = oldRoles.iterator(); iterator1.hasNext();)
            {
                final Role role = iterator1.next();
                nextRealm.unmap(poddUser, role);
                this.log.debug(" User [{}] unmapped from Role [{}]", poddUser.getIdentifier(), role.getName());
            }
            
            // - map new Roles for the User
            for(Iterator<Resource> iterator2 =
                    incomingRoles.filter(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING).subjects().iterator(); iterator2
                    .hasNext();)
            {
                final Resource mappingUri = iterator2.next();
                
                final URI roleUri =
                        incomingRoles.filter(mappingUri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objectURI();
                final RestletUtilRole role = PoddRoles.getRoleByUri(roleUri);
                
                final URI mappedObject =
                        incomingRoles.filter(mappingUri, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, null).objectURI();
                
                this.log.debug("Mapping <{}> to Role <{}> with Optional Object <{}>", poddUser.getIdentifier(),
                        role.getName(), mappedObject);
                if(mappedObject != null)
                {
                    nextRealm.map(poddUser, role.getRole(), mappedObject);
                }
                else
                {
                    nextRealm.map(poddUser, role.getRole());
                }
            }
        }
        catch(final IOException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
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

}
