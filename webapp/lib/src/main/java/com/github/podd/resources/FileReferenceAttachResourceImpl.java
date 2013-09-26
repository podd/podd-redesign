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
import java.util.HashMap;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.DataReferenceVerificationPolicy;
import com.github.podd.exception.FileReferenceVerificationFailureException;
import com.github.podd.exception.OntologyNotInProfileException;
import com.github.podd.exception.PoddException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * Attach a file reference to a PODD artifact
 * 
 * @author kutila
 * 
 */
public class FileReferenceAttachResourceImpl extends AbstractPoddResourceImpl
{
    @Get
    public Representation attachFileReferencePageHtml(final Representation entity) throws ResourceException
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
        
        this.log.info("attachFileRefHtml");
        final User user = this.getRequest().getClientInfo().getUser();
        
        this.log.info("authenticated user: {}", user);
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "attachdatareference.html.ftl");
        dataModel.put("pageTitle", "TODO: Attach Data Reference");
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    @Post(":rdf|rj|ttl")
    public Representation attachFileReferenceRdf(final Representation entity, final Variant variant)
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
        
        // check optional parameter: whether file references should be verified. Defaults to NO
        final String verifyFileRefs = this.getQuery().getFirstValue(PoddWebConstants.KEY_VERIFICATION_POLICY, true);
        DataReferenceVerificationPolicy verificationPolicy = DataReferenceVerificationPolicy.DO_NOT_VERIFY;
        if(verifyFileRefs != null && Boolean.valueOf(verifyFileRefs))
        {
            verificationPolicy = DataReferenceVerificationPolicy.VERIFY;
        }
        
        this.log.info("@Post attachFileReference ({})", entity.getMediaType().getName());
        
        InferredOWLOntologyID artifactMap = attachDataReference(entity, artifactUri, versionUri, verificationPolicy);
        
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
    
    /**
     * @param entity
     * @param artifactUri
     * @param versionUri
     * @param verificationPolicy
     * @return
     * @throws ResourceException
     */
    private InferredOWLOntologyID attachDataReference(final Representation entity, final String artifactUri,
            final String versionUri, DataReferenceVerificationPolicy verificationPolicy) throws ResourceException
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
        
        InferredOWLOntologyID artifactMap = null;
        try
        {
            artifactMap =
                    this.getPoddArtifactManager().attachDataReferences(
                            ValueFactoryImpl.getInstance().createURI(artifactUri),
                            ValueFactoryImpl.getInstance().createURI(versionUri), inputStream, inputFormat,
                            verificationPolicy);
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
    
}
