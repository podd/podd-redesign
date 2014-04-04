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

public class GetEventTypeResourceImpl extends AbstractPoddResourceImpl
{

	
	
	@Get("rdf|rj|json|ttl")
	public String getRdf(final Variant variant) throws ResourceException
	 {
        // - object Type (mandatory)
        this.log.info("Get Event Type query {}",this.getQuery());
       
        final String result;
        try
        {
            result = this.getJsonEventHierarchy().toString();
        }
        catch(PoddException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not generate event type", e);
        }
        this.log.debug("[GetEventTypeResourceImpl] result getRdf {}",result);
        return result;
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
		Set<URI> eventTopConcepts = new LinkedHashSet<>();
		try
		{
			eventTopConcepts = this.getPoddArtifactManager().getEventsTopConcepts(ontologyID);
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
		Datas.append( "[");
		Datas.append(this.getSubEventof(eventTopConcepts,ontologyID,Data,null));
		Datas.setLength(Datas.length() - 1);
		Datas.append("]");


		return Datas;
	}

	public String getSubEventof(Set<URI> topConcept,InferredOWLOntologyID ontologyID,StringBuffer Data,URI parent) throws ResourceException
	{
		//this.log.debug("getSubEventof::{}", Data.toString());
		//this.log.debug("Current Top Concept :{}", parent);
		Set<URI> eventSubConcepts = new LinkedHashSet<>();
		
		if(!topConcept.isEmpty()){
			URI tmp;
			Iterator<URI> it = topConcept.iterator();
			while(it.hasNext())
			{
				tmp = it.next();
				if(parent!=null){
					Data.append("{ \"id\" : \""+tmp.getLocalName()+"\", \"parent\" : \""+parent.getLocalName()+"\", \"text\" : \""+tmp.getLocalName()+"\" ,\"type\":\"concept\",\"uri\" : \""+tmp+"\"},");
				}else{
					Data.append("{ \"id\" : \""+tmp.getLocalName()+"\", \"parent\" : \"#\", \"text\" : \""+tmp.getLocalName()+"\" ,\"type\":\"concept\",\"uri\" : \""+tmp+"\"},");	 
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
	
	
	@Post("rdf|rj|json|ttl")
    public Representation addEventLinked(final Representation entity, final Variant variant) throws ResourceException
    {

        this.log.info("In addEventLinked");
        
       
        
        return null;
    }

}