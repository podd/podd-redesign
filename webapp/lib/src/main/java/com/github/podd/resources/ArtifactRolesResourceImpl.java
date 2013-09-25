/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilRole;
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
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
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
            
            final Collection<RestletUtilRole> nextRoles = new LinkedHashSet<>();
            nextRoles.addAll(Arrays.asList(PoddRoles.PROJECT_PRINCIPAL_INVESTIGATOR, PoddRoles.PROJECT_ADMIN,
                    PoddRoles.PROJECT_MEMBER, PoddRoles.PROJECT_OBSERVER));
            
            final Map<RestletUtilRole, Collection<PoddUser>> roleUserMap =
                    ((PoddWebServiceApplication)this.getApplication()).getRealm().getUsersForRole(nextRoles,
                            ontologyID.getOntologyIRI().toOpenRDFURI());
            
            // - add PI user label and identifier
            final Collection<PoddUser> piList = roleUserMap.get(PoddRoles.PROJECT_PRINCIPAL_INVESTIGATOR);
            if(piList != null && piList.size() == 1)
            {
                final PoddUser nextUser = piList.iterator().next();
                dataModel.put("piLabel", nextUser.getUserLabel());
                dataModel.put("piIdentifier", nextUser.getIdentifier());
            }
            else
            {
                this.log.warn("Did not find Principal Investigator for artifact: {}", artifactUri);
            }
            
            // - add other Project Role participants
            dataModel.put("admins", roleUserMap.get(PoddRoles.PROJECT_ADMIN));
            dataModel.put("members", roleUserMap.get(PoddRoles.PROJECT_MEMBER));
            dataModel.put("observers", roleUserMap.get(PoddRoles.PROJECT_OBSERVER));
        }
        catch(final Exception e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to populate data model", e);
        }
        
        dataModel.put("artifactUri", ontologyID.getOntologyIRI().toString());
        
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    /**
     * View the Edit Project Participants data in RDF.
     */
    @Get("rdf|rj|json|ttl")
    public Representation getEditArtifactParticipantsRdf(final Variant variant) throws ResourceException
    {
        this.log.info("getArtifactRolesHtml");
        
        // the artifact in which editing is requested
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
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
        
        final Collection<RestletUtilRole> nextRoles = new LinkedHashSet<>();
        nextRoles.addAll(Arrays.asList(PoddRoles.PROJECT_PRINCIPAL_INVESTIGATOR, PoddRoles.PROJECT_ADMIN,
                PoddRoles.PROJECT_MEMBER, PoddRoles.PROJECT_OBSERVER));
        
        final Map<RestletUtilRole, Collection<PoddUser>> roleUserMap =
                ((PoddWebServiceApplication)this.getApplication()).getRealm().getUsersForRole(nextRoles,
                        ontologyID.getOntologyIRI().toOpenRDFURI());
        final Map<RestletUtilRole, Collection<String>> mappings = this.translateMap(roleUserMap);
        
        final Model model = new LinkedHashModel();
        
        PoddRoles.dumpRoleMappingsArtifact(mappings, model);
        
        // - prepare response
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        final RDFFormat outputFormat =
                Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
        try
        {
            Rio.write(model, output, outputFormat);
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat.getDefaultMIMEType()));
    }
    
    /**
     * Helper method to retrieve display details for Project Top Object.
     * 
     * @param ontologyID
     * @return
     * @throws OpenRDFException
     */
    private PoddObjectLabel getProjectDetails(final InferredOWLOntologyID ontologyID) throws OpenRDFException
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
    
    private Map<RestletUtilRole, Collection<String>> translateMap(final Map<RestletUtilRole, Collection<PoddUser>> input)
    {
        final ConcurrentMap<RestletUtilRole, Collection<String>> userList = new ConcurrentHashMap<>();
        
        for(final RestletUtilRole nextRole : input.keySet())
        {
            Collection<String> nextRoles = new LinkedHashSet<String>();
            final Collection<String> putIfAbsent = userList.putIfAbsent(nextRole, nextRoles);
            if(putIfAbsent != null)
            {
                nextRoles = putIfAbsent;
            }
            
            for(final PoddUser nextUser : input.get(nextRole))
            {
                nextRoles.add(nextUser.getIdentifier());
            }
        }
        
        return userList;
    }
}
