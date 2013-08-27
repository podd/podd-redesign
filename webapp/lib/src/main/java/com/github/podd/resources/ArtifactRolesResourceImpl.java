/**
 * 
 */
package com.github.podd.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.OpenRDFException;
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
import com.github.podd.utils.PoddObjectLabel;
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
            dataModel.put("projectObject", this.getProjectDetails(ontologyID));
            
            dataModel.put("piUri", PoddRoles.PROJECT_PRINCIPAL_INVESTIGATOR.getURI());
            dataModel.put("adminUri", PoddRoles.PROJECT_ADMIN.getURI());
            dataModel.put("memberUri", PoddRoles.PROJECT_MEMBER.getURI());
            dataModel.put("observerUri", PoddRoles.PROJECT_OBSERVER.getURI());
            
            Map<PoddRoles, List<PoddUser>> roleUserMap =
                    this.getUsersForRole(Arrays.asList(PoddRoles.PROJECT_PRINCIPAL_INVESTIGATOR,
                            PoddRoles.PROJECT_ADMIN, PoddRoles.PROJECT_MEMBER, PoddRoles.PROJECT_OBSERVER), artifactUri);
            
            // - add PI user label and identifier
            List<PoddUser> piList = roleUserMap.get(PoddRoles.PROJECT_PRINCIPAL_INVESTIGATOR);
            if (piList != null && piList.size() == 1)
            {
                dataModel.put("piLabel", piList.get(0).getUserLabel());
                dataModel.put("piIdentifier", piList.get(0).getIdentifier());
            }
            
            // - add Project Admin details
            List<PoddUser> adminList = roleUserMap.get(PoddRoles.PROJECT_ADMIN);
            if (adminList != null && !adminList.isEmpty())
            {
                dataModel.put("admins", adminList);
            }
            
            // TODO - add member and observer labels/identifiers
//            dataModel.put("member", roleUserMap.get(PoddRoles.PROJECT_MEMBER));
//            dataModel.put("observer", roleUserMap.get(PoddRoles.PROJECT_OBSERVER));
            
        }
        catch(final Exception e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to populate data model");
        }
        
        dataModel.put("artifactUri", ontologyID.getOntologyIRI().toString());
        
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }

    /**
     * Helper method to find PODD Users who are assigned the 'roles of interest' for the given
     * artifact.
     * 
     * For security purposes, the returned Users only have their Identifier, First name, Last name,
     * Status and Organization filled.
     * 
     * @param rolesOfInterest
     * @param artifactUri
     * @return
     */
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
                    
                    final PoddUser tempUser = (PoddUser)nextRealm.findUser(user.getIdentifier());
                    final PoddUser userToReturn =
                            new PoddUser(tempUser.getIdentifier(), null, tempUser.getFirstName(),
                                    tempUser.getLastName(), null, tempUser.getUserStatus(), null,
                                    tempUser.getOrganization(), null);
                    
                    nextRoles.add(userToReturn);
                }
            }
        }
        
        return userList;
    }
    
    /**
     * Helper method to retrieve display details for Project Top Object.
     * 
     * @param ontologyID
     * @return
     * @throws OpenRDFException
     */
    private PoddObjectLabel getProjectDetails(final InferredOWLOntologyID ontologyID)
    throws OpenRDFException
    {
        // find and set top-object of this artifact as the object to display
        final List<PoddObjectLabel> topObjectLabels =
                this.getPoddArtifactManager().getTopObjectLabels(Arrays.asList(ontologyID));
        if(topObjectLabels == null || topObjectLabels.size() != 1)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "There should be only 1 top object");
        }
        return topObjectLabels.get(0);
    }
}
