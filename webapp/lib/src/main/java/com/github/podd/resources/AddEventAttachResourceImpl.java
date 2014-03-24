package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.api.MetadataPolicy;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.SchemaManifestException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedArtifactVersionException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

public class AddEventAttachResourceImpl extends AbstractPoddResourceImpl
{

	@Post("rdf|rj|json|ttl")
    public Representation addEventLinked(final Representation entity, final Variant variant) throws ResourceException
    {
        // check authentication first
        this.checkAuthentication(PoddAction.USER_CREATE);
        
        this.log.info("In addUserRdf");
        
        final PoddSesameRealm nextRealm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        
        URI newUserUri = null;
        PoddUser newUser = null;
        try
        {
            // - get input stream with RDF content
            final InputStream inputStream = entity.getStream();
            final RDFFormat inputFormat =
                    Rio.getParserFormatForMIMEType(entity.getMediaType().getName(), RDFFormat.RDFXML);
            final Model newUserModel = Rio.parse(inputStream, "", inputFormat);
            
            this.log.info("About to create user from model");
            
            // - create new PoddUser and add to Realm
            newUser = PoddUser.fromModel(newUserModel, true, false, false);
            
            // If we didn't get a secret, then do not activate their login at
            // this stage
            if(newUser.getSecret() == null)
            {
                newUser.setUserStatus(PoddUserStatus.INACTIVE);
            }
            
            this.log.info("About to check if user already exists");
            
            if(nextRealm.findUser(newUser.getIdentifier()) != null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "User already exists");
            }
            newUserUri = nextRealm.addUser(newUser);
            
            this.log.info("Added new User <{}> <{}>", newUser.getIdentifier(), newUserUri);
            
            // - map Roles for the new User
            
            // - add Project Creator Role if nothing else has been specified
            if(!newUserModel.contains(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING))
            {
                nextRealm.map(newUser, PoddRoles.PROJECT_CREATOR.getRole());
            }
            
            for(final Resource mappingUri : newUserModel.filter(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING)
                    .subjects())
            {
                final URI roleUri =
                        newUserModel.filter(mappingUri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objectURI();
                final RestletUtilRole role = PoddRoles.getRoleByUri(roleUri);
                
                final URI mappedObject = newUserModel.filter(mappingUri, PODD.PODD_ROLEMAPPEDOBJECT, null).objectURI();
                
                this.log.debug("Mapping <{}> to Role <{}> with Optional Object <{}>", newUser.getIdentifier(),
                        role.getName(), mappedObject);
                if(mappedObject != null)
                {
                    nextRealm.map(newUser, role.getRole(), mappedObject);
                }
                else
                {
                    nextRealm.map(newUser, role.getRole());
                }
            }
            
            // - check the User was successfully added to the Realm
            final PoddUser findUser = nextRealm.findUser(newUser.getIdentifier());
            if(findUser == null)
            {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to add user");
            }
            
        }
        catch(final IOException | OpenRDFException e)
        {
            this.log.error("Error creating user", e);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
        }
        
        // - prepare response
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        final RDFFormat outputFormat =
                Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
        try
        {
            final Model model = new LinkedHashModel();
            model.add(newUserUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                    PODD.VF.createLiteral(newUser.getIdentifier()));
            
            Rio.write(model, output, outputFormat);
        }
        catch(final OpenRDFException e)
        {
            this.log.error("Error generating response entity", e);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat.getDefaultMIMEType()));
    }
	
	@Get("html")
	public Representation attachEventReferencePageHtml(final Representation entity) throws ResourceException, UnmanagedArtifactIRIException, UnmanagedSchemaIRIException
	{
		// check mandatory parameter: artifact IRI
		final String artifactUriString = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
		if(artifactUriString == null)
		{
			this.log.error("Artifact ID not submitted");
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact IRI not submitted");
		}

		// check mandatory parameter: object IRI
		final String objectUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_OBJECT_IDENTIFIER, true);
		if(objectUri == null)
		{
			this.log.error("Object IRI not submitted");
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Object IRI not submitted");
		}
		final URI artifactUri = PODD.VF.createURI(artifactUriString);
		this.checkAuthentication(PoddAction.ARTIFACT_EDIT, artifactUri);

		InferredOWLOntologyID artifact;

		try
		{
			artifact = this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
		}
		catch(final UnmanagedArtifactIRIException | UnmanagedSchemaIRIException e)
		{
			this.log.error("Artifact IRI not recognised");
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact IRI not recognised");
		}


		this.log.debug("artifactUriString :{}",artifactUriString);
		this.log.debug("objectUri :{}",objectUri);
		this.log.debug("artifactUri :{}",artifactUri);
		this.log.debug("artifact :{}",artifact);


		this.log.info("attachEvent");
		final User user = this.getRequest().getClientInfo().getUser();

		this.log.info("authenticated user: {}", user);

		Set<URI> eventType = new LinkedHashSet<>();
		InferredOWLOntologyID ontologyID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
		try
		{
			eventType = this.getPoddArtifactManager().getEventsType(ontologyID);
		}
		catch(final OpenRDFException | UnmanagedSchemaIRIException | SchemaManifestException
				| UnsupportedRDFormatException | IOException | UnmanagedArtifactIRIException
				| UnmanagedArtifactVersionException e)
		{
			this.log.error("Could not event type", e);
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find parent details", e);
		}



		final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
		dataModel.put("contentTemplate", this.getPoddApplication().getPropertyUtil()
				.get(PoddWebConstants.PROPERTY_TEMPLATE_EVENT, PoddWebConstants.DEFAULT_TEMPLATE_EVENT));
		dataModel.put("pageTitle", "Add Event");
		dataModel.put("artifactIri", artifact.getOntologyIRI().toString());
		dataModel.put("versionIri", artifact.getVersionIRI().toString());
		dataModel.put("eventList", eventType);
		dataModel.put("jsonEventHierarchy", this.getJsonEventHierarchy().toString());
		dataModel.put("objectUri", objectUri);

		// Output the base template, with contentTemplate from the dataModel
		// defining the
		// template to use for the content in the body of the page
		return RestletUtils.getHtmlRepresentation(
				this.getPoddApplication().getPropertyUtil()
				.get(PoddWebConstants.PROPERTY_TEMPLATE_BASE, PoddWebConstants.DEFAULT_TEMPLATE_BASE),
				dataModel, MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
	}


	public StringBuffer getJsonEventHierarchy() throws ResourceException, UnmanagedArtifactIRIException, UnmanagedSchemaIRIException
	{
		final String artifactUriString = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
		if(artifactUriString == null)
		{
			this.log.error("Artifact ID not submitted");
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact IRI not submitted");
		}
		final URI artifactUri = PODD.VF.createURI(artifactUriString);
		InferredOWLOntologyID ontologyID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
		Set<URI> eventTtopConcepts = new LinkedHashSet<>();
		try
		{
			eventTtopConcepts = this.getPoddArtifactManager().getEventsTopConcepts(ontologyID);
		}
		catch(final OpenRDFException | UnmanagedSchemaIRIException | SchemaManifestException
				| UnsupportedRDFormatException | IOException | UnmanagedArtifactIRIException
				| UnmanagedArtifactVersionException e)
		{
			this.log.error("Could not find event type", e);
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find parent details", e);
		}
		StringBuffer Data = new StringBuffer();
		StringBuffer Datas = new StringBuffer();
		Datas.append( " [ ");
		this.log.debug("getJsonEventHierarchy::before getSubEventof call :{}", Data.toString());
		Datas.append(this.getSubEventof(eventTtopConcepts,ontologyID,Data,null));

		Datas.append(" ] ");


		return Datas;
	}

	public String getSubEventof(Set<URI> topConcept,InferredOWLOntologyID ontologyID,StringBuffer Data,URI parent) throws ResourceException
	{
		this.log.debug("getSubEventof::{}", Data.toString());
		this.log.debug("Current Top Concept :{}", parent);
		Set<URI> eventSubConcepts = new LinkedHashSet<>();
		
		if(!topConcept.isEmpty()){
			URI tmp;
			Iterator<URI> it = topConcept.iterator();
			while(it.hasNext())
			{
				tmp = it.next();
				this.log.debug("Next value of the iterator :{}", tmp);
				if(parent!=null){
					Data.append("{ \"id\" : \""+tmp.getLocalName()+"\", \"parent\" : \""+parent.getLocalName()+"\", \"text\" : \""+tmp.getLocalName()+"\" ,\"type\":\"concept\"},");
				}else{
					Data.append("{ \"id\" : \""+tmp.getLocalName()+"\", \"parent\" : \"#\", \"text\" : \""+tmp.getLocalName()+"\" ,\"type\":\"concept\"},");	 
				}
				try
				{
					eventSubConcepts =  this.getPoddArtifactManager().getDirectSubClassOf(tmp,ontologyID);
				}
				catch(final OpenRDFException | UnmanagedSchemaIRIException | SchemaManifestException
						| UnsupportedRDFormatException | IOException | UnmanagedArtifactIRIException
						| UnmanagedArtifactVersionException e)
				{
					this.log.error("Could not event type", e);
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find parent details", e);
				}
				
				this.log.debug("eventSubConcepts:{}", eventSubConcepts);
				if(!eventSubConcepts.isEmpty()){
				this.log.debug("Value for Data after a While:{}", Data.toString());
				this.getSubEventof(eventSubConcepts,ontologyID,Data,tmp);
				
				}
			}
		}

		return Data.toString();
	}

}