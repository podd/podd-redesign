package com.github.podd.resources;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.exception.PoddException;
import com.github.podd.exception.SchemaManifestException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedArtifactVersionException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddWebConstants;

public class GetEventTypeResourceImpl extends AbstractPoddResourceImpl
{

    @Get("rdf|rj|json|ttl")
    public String getRdf(final Variant variant) throws ResourceException
    {
        // - object Type (mandatory)
        this.log.info("Get Event Type query {}", this.getQuery());

        final String result;

        try
        {
            result = this.getJsonEventHierarchy().toString();

        }
        catch(final PoddException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not generate event type", e);
        }
        this.log.debug("[GetEventTypeResourceImpl] result getRdf {}", result);
        return result;
    }

    public StringBuffer getJsonEventHierarchy() throws ResourceException, UnmanagedArtifactIRIException,
    UnmanagedSchemaIRIException
    {
        final String artifactUriString = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
        if(artifactUriString == null)
        {
            this.log.error("Artifact ID not submitted");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact IRI not submitted");
        }
        final URI artifactUri = PODD.VF.createURI(artifactUriString);
        final InferredOWLOntologyID ontologyID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
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
        final StringBuffer Datas = new StringBuffer();
        Datas.append("[");
        Datas.append(this.getChildof(eventTopConcepts, ontologyID, true));
        Datas.setLength(Datas.length() - 1);
        Datas.append("]");
        this.log.debug("Final Datas {}", Datas.toString());

        return Datas;
    }

    /**
     * Request to get the direct child of a set of concepts
     *
     * @param Concepts
     *            the set of concepts
     * @param ontologyID
     * @return
     * @throws ResourceException
     */

    public Model getchildOfList(final Set<URI> Concepts, final InferredOWLOntologyID ontologyID)
            throws ResourceException
    {

        Model subConcepts;
        try
        {
            subConcepts = this.getPoddArtifactManager().childOfList(Concepts, ontologyID);
        }
        catch(final OpenRDFException | UnmanagedSchemaIRIException | SchemaManifestException
                | UnsupportedRDFormatException | IOException | UnmanagedArtifactIRIException
                | UnmanagedArtifactVersionException e)
        {
            this.log.error("Could not find child of list ", e);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find child of list ", e);
        }
        this.log.debug("Result to array", subConcepts.toArray());

        return subConcepts;
    }

    /**
     * Call the function getchildOfList and construct the JSON for the jsTree plugin
     *
     * @param Set
     *            of currant concept. Contains top concept the first time
     * @param ontologyID
     * @param TopConcept
     *            Boolean indicate if it's the first call of the function
     * @return
     * @throws ResourceException
     */

    public StringBuffer getChildof(final Set<URI> Concepts, final InferredOWLOntologyID ontologyID,
            final boolean TopConcept) throws ResourceException
    {

        Model ResultRequest;
        final StringBuffer Data = new StringBuffer();
        ResultRequest = this.getchildOfList(Concepts, ontologyID);
        final Set<URI> SubConcepts = new LinkedHashSet<>();

        if(!ResultRequest.isEmpty())
        {

            for(final Value object : ResultRequest.objects())
            {
                if(TopConcept)
                {
                    Data.append("{ \"id\" : \"" + ((URI)object).getLocalName()
                            + "\", \"parent\" : \"#\", \"text\" : \"" + ((URI)object).getLocalName()
                            + "\" ,\"type\":\"concept\",\"uri\" : \"" + object + "\"},");
                }
                final Model SubConcept =
                        ResultRequest.filter(null,
                                PODD.VF.createURI("http://www.w3.org/2000/01/rdf-schema#subClassOf"), object);

                for(final Value subject : SubConcept.subjects())
                {

                    Data.append("{ \"id\" : \"" + ((URI)subject).getLocalName() + "\", \"parent\" : \""
                            + ((URI)object).getLocalName() + "\", \"text\" : \"" + ((URI)subject).getLocalName()
                            + "\" ,\"type\":\"concept\",\"uri\" : \"" + subject + "\"},");
                    SubConcepts.add((URI)subject);
                }
            }

            return Data.append(this.getChildof(SubConcepts, ontologyID, false));
        }
        else
        {
            return Data;
        }

    }

    @Post("rdf|rj|json|ttl")
    public Representation addEventLinked(final Representation entity, final Variant variant) throws ResourceException
    {

        this.log.info("In addEventLinked");

        return null;
    }

}