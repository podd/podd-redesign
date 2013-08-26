/**
 * 
 */
package com.github.podd.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * @author kutila
 */
public class ArtifactRolesResourceImpl extends AbstractPoddResourceImpl
{
 
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * View the Edit Project Participants page in HTML
     */
    @Get("html")
    public Representation getEditArtifactParticipantsHtml(final Representation entity) throws ResourceException
    {
        this.log.info("getArtifactRolesHtml");
        
        // the artifact in which editing is requested
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
        if(artifactUri == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact ID not submitted");
        }
        
        this.log.info("requesting to edit Artifact Participants (HTML): {}", artifactUri);
        
        this.checkAuthentication(PoddAction.PROJECT_ROLE_EDIT, PoddRdfConstants.VF.createURI(artifactUri));
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        // validate artifact exists
        InferredOWLOntologyID ontologyID;
        try
        {
            ontologyID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact", e);
        }
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        
        dataModel.put("contentTemplate", "projectParticipants.html.ftl");
        dataModel.put("pageTitle", "Edit Project Participants");
        
        try
        {
            dataModel.put("piUri", PoddRoles.PROJECT_PRINCIPAL_INVESTIGATOR.getURI());
            dataModel.put("adminUri", PoddRoles.PROJECT_ADMIN.getURI());
            dataModel.put("memberUri", PoddRoles.PROJECT_MEMBER.getURI());
            dataModel.put("observerUri", PoddRoles.PROJECT_OBSERVER.getURI());
            
            Map<PoddRoles, List<PoddUser>> roleUserMap =
                    this.getUsersForRole(Arrays.asList(PoddRoles.PROJECT_PRINCIPAL_INVESTIGATOR,
                            PoddRoles.PROJECT_ADMIN, PoddRoles.PROJECT_MEMBER, PoddRoles.PROJECT_OBSERVER), artifactUri);
            
            // TODO - add user labels/identifiers
            List<PoddUser> piList = roleUserMap.get(PoddRoles.PROJECT_PRINCIPAL_INVESTIGATOR);
            if (piList != null && piList.size() == 1)
            {
                dataModel.put("pi", piList.get(0).getUserLabel());
            }
            
            List<PoddUser> adminList = roleUserMap.get(PoddRoles.PROJECT_ADMIN);
            if (adminList != null && !adminList.isEmpty())
            {
                dataModel.put("admin", adminList.get(0).getUserLabel());
            }
//            dataModel.put("member", roleUserMap.get(PoddRoles.PROJECT_MEMBER));
//            dataModel.put("observer", roleUserMap.get(PoddRoles.PROJECT_OBSERVER));
            
        }
        catch(final Exception e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to populate data model");
        }
        
        dataModel.put("artifactIri", ontologyID.getOntologyIRI().toString());
        //dataModel.put("versionIri", ontologyID.getVersionIRI().toString());
        
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }

    
    private Map<PoddRoles, List<PoddUser>> getUsersForRole(final List<PoddRoles> rolesOfInterest, final String artifactUri)
    {
        final ConcurrentMap<PoddRoles, List<PoddUser>> userList = new ConcurrentHashMap<PoddRoles, List<PoddUser>>();
        
        final PoddSesameRealm nextRealm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        final Map<User, Collection<Role>> participantMap =
                nextRealm.getRolesForObjectAlternate(null, PoddRdfConstants.VF.createURI(artifactUri));
        
        final Collection<User> keySet = participantMap.keySet();
        for(Iterator<User> iterator = keySet.iterator(); iterator.hasNext();)
        {
            User user = iterator.next();
            Collection<Role> rolesOfUser = participantMap.get(user);
            
            for (PoddRoles roleOfInterest : rolesOfInterest)
            {
                if (rolesOfUser.contains(roleOfInterest.getRole()))
                {
                    this.log.info("User {} has Role {} ", user.getIdentifier(), roleOfInterest.getName());
                    
                    List<PoddUser> nextRoles = new ArrayList<PoddUser>();
                    List<PoddUser> putIfAbsent = userList.putIfAbsent(roleOfInterest, nextRoles);
                    if(putIfAbsent != null)
                    {
                        nextRoles = putIfAbsent;
                    }
                    nextRoles.add((PoddUser)nextRealm.findUser(user.getIdentifier()));
                }
            }
        }
        
        return userList;
    }
    
}
