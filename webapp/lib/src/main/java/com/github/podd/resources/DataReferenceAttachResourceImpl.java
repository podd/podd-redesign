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
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
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
import org.semanticweb.owlapi.model.OWLException;

import com.github.podd.api.DataReferenceVerificationPolicy;
import com.github.podd.api.PoddArtifactManager;
import com.github.podd.exception.FileReferenceVerificationFailureException;
import com.github.podd.exception.OntologyNotInProfileException;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedArtifactVersionException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * Attach a data reference to a PODD artifact
 * 
 * @author kutila
 * 
 */
public class DataReferenceAttachResourceImpl extends AbstractPoddResourceImpl
{
    /**
     * @param entity
     * @param artifactUri
     * @param versionUri
     * @param verificationPolicy
     * @return
     * @throws ResourceException
     * @throws IOException
     * @throws UnsupportedRDFormatException
     * @throws RDFParseException
     * @throws UnmanagedArtifactIRIException
     */
    private InferredOWLOntologyID attachDataReference(final Representation entity, final String artifactUriString,
            final String versionUriString, final DataReferenceVerificationPolicy verificationPolicy)
        throws ResourceException, RDFParseException, UnsupportedRDFormatException, IOException,
        UnmanagedArtifactIRIException
    {
        // get input stream containing RDF statements
        InputStream inputStream = null;
        try
        {
            inputStream = entity.getStream();
        }
        catch(final IOException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
        }
        final RDFFormat inputFormat = Rio.getParserFormatForMIMEType(entity.getMediaType().getName(), RDFFormat.RDFXML);
        
        final Model model = Rio.parse(inputStream, "", inputFormat);
        
        final URI artifactUri = PoddRdfConstants.VF.createURI(artifactUriString);
        final URI versionUri = PoddRdfConstants.VF.createURI(versionUriString);
        
        final PoddArtifactManager artifactManager = this.getPoddArtifactManager();
        
        InferredOWLOntologyID artifact;
        try
        {
            artifact = artifactManager.getArtifact(IRI.create(artifactUri), IRI.create(versionUri));
        }
        catch(final UnmanagedArtifactVersionException e1)
        {
            artifact = artifactManager.getArtifact(IRI.create(artifactUri));
        }
        
        InferredOWLOntologyID artifactMap = null;
        try
        {
            artifactMap = this.getPoddArtifactManager().attachDataReferences(artifact, model, verificationPolicy);
        }
        catch(final FileReferenceVerificationFailureException e)
        {
            this.log.error("File reference validation errors: {}", e.getValidationFailures());
            throw new ResourceException(Status.SERVER_ERROR_BAD_GATEWAY, "File reference(s) failed verification", e);
        }
        catch(final OntologyNotInProfileException e)
        {
            this.log.error("The ontology was not suitable for our reasoner after the changes: {}", e.getProfileReport());
            throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Ontology was not consistent after the changes. Was the parent object correct before the submission.",
                    e);
        }
        catch(OpenRDFException | PoddException | IOException | OWLException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not attach file references", e);
        }
        
        this.log.info("Successfully attached file reference to artifact {}", artifactMap);
        return artifactMap;
    }
    
    @Get
    public Representation attachDataReferencePageHtml(final Representation entity) throws ResourceException
    {
        // check mandatory parameter: artifact IRI
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
        if(artifactUri == null)
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
        
        this.checkAuthentication(PoddAction.ARTIFACT_EDIT, PoddRdfConstants.VF.createURI(artifactUri));
        
        InferredOWLOntologyID artifact;
        
        try
        {
            artifact = this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            this.log.error("Artifact IRI not recognised");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact IRI not recognised");
        }
        
        this.log.info("attachFileRefHtml");
        final User user = this.getRequest().getClientInfo().getUser();
        
        this.log.info("authenticated user: {}", user);
        
        PoddObjectLabel parentDetails;
        try
        {
            parentDetails = RestletUtils.getParentDetails(this.getPoddArtifactManager(), artifact, objectUri);
        }
        catch(final OpenRDFException e)
        {
            this.log.error("Could not find parent details", e);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find parent details", e);
        }
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "attachDataReference.html.ftl");
        dataModel.put("pageTitle", "Attach Data Reference");
        dataModel.put("artifactIri", artifact.getOntologyIRI().toString());
        dataModel.put("versionIri", artifact.getVersionIRI().toString());
        dataModel.put("parentObject", parentDetails);
        dataModel.put("objectUri", objectUri);
        
        // Output the base template, with contentTemplate from the dataModel
        // defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    @Post(":rdf|rj|ttl")
    public Representation attachDataReferenceRdf(final Representation entity, final Variant variant)
        throws ResourceException
    {
        // check mandatory parameter: artifact IRI
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
        if(artifactUri == null)
        {
            this.log.error("Artifact ID not submitted");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact IRI not submitted");
        }
        
        this.checkAuthentication(PoddAction.ARTIFACT_EDIT, PoddRdfConstants.VF.createURI(artifactUri));
        
        // check mandatory parameter: artifact version IRI
        final String versionUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, true);
        if(versionUri == null)
        {
            this.log.error("Artifact Version IRI not submitted");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact Version IRI not submitted");
        }
        
        // check optional parameter: whether file references should be verified.
        // Defaults to NO
        final String verifyFileRefs = this.getQuery().getFirstValue(PoddWebConstants.KEY_VERIFICATION_POLICY, true);
        DataReferenceVerificationPolicy verificationPolicy = DataReferenceVerificationPolicy.DO_NOT_VERIFY;
        if(verifyFileRefs != null && Boolean.valueOf(verifyFileRefs))
        {
            verificationPolicy = DataReferenceVerificationPolicy.VERIFY;
        }
        
        this.log.info("@Post attachFileReference ({})", entity.getMediaType().getName());
        
        InferredOWLOntologyID artifactMap;
        try
        {
            artifactMap = this.attachDataReference(entity, artifactUri, versionUri, verificationPolicy);
        }
        catch(final UnmanagedArtifactIRIException e1)
        {
            this.log.error("Artifact IRI not managed");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact IRI not managed");
        }
        catch(final UnsupportedRDFormatException e1)
        {
            this.log.error("Unsupported format: " + entity.getMediaType().getName());
            throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, "Cannot parse the given format: "
                    + entity.getMediaType().getName());
        }
        catch(final RDFParseException e1)
        {
            this.log.error("Artifact not parsed correctly");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact not parsed correctly");
        }
        catch(final IOException e1)
        {
            this.log.error("Artifact not parsed due to IO exception");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact not parsed due to IO exception");
        }
        
        // prepare output: Artifact ID, object URI, file reference URI
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        
        final RDFWriter writer =
                Rio.createWriter(Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML),
                        output);
        try
        {
            writer.startRDF();
            OntologyUtils.ontologyIDsToHandler(Arrays.asList(artifactMap), writer);
            writer.endRDF();
        }
        catch(final RDFHandlerException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(writer.getRDFFormat()
                .getDefaultMIMEType()));
    }
    
}
