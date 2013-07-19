/**
 * 
 */
package com.github.podd.restlet;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Status;
import org.restlet.representation.AppendableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilMediaType;
import com.github.podd.api.file.DataReference;
import com.github.podd.exception.FileReferenceInvalidException;
import com.github.podd.exception.FileReferenceVerificationFailureException;
import com.github.podd.exception.PoddException;
import com.github.podd.utils.PoddRdfConstants;

import freemarker.template.Configuration;

/**
 * This status service is based on the standard Restlet guide for returning custom error pages.
 * 
 * Set this to Application via application.setStatusService().
 * 
 * TODO: create the templates used (i.e. PropertyUtils.PROPERTY_TEMPLATE...)
 * 
 * @author Peter Ansell p_ansell@yahoo.com copied from the OAS project
 *         (https://github.com/ansell/oas)
 */
public class PoddStatusService extends StatusService
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private Configuration freemarkerConfiguration;
    
    /**
     * 
     */
    public PoddStatusService(final Configuration freemarkerConfiguration)
    {
        super();
        this.freemarkerConfiguration = freemarkerConfiguration;
    }
    
    /**
     * @param enabled
     */
    public PoddStatusService(final Configuration freemarkerConfiguration, final boolean enabled)
    {
        super(enabled);
        this.freemarkerConfiguration = freemarkerConfiguration;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.service.StatusService#getRepresentation(org.restlet.data.Status,
     * org.restlet.Request, org.restlet.Response)
     */
    @Override
    public Representation getRepresentation(final Status status, final Request request, final Response response)
    {
        // identify Media Type to send response in
        MediaType preferredMediaType = MediaType.TEXT_PLAIN;
        float maxQuality = 0;
        final List<Preference<MediaType>> acceptedMediaTypes = request.getClientInfo().getAcceptedMediaTypes();
        
        for(final Preference<MediaType> pref : acceptedMediaTypes)
        {
            final float quality = pref.getQuality();
            if(quality > maxQuality)
            {
                preferredMediaType = pref.getMetadata();
                maxQuality = quality;
            }
        }
        
        Representation representation = null;
        if(MediaType.APPLICATION_RDF_XML.equals(preferredMediaType)
                || RestletUtilMediaType.APPLICATION_RDF_JSON.equals(preferredMediaType)
                || MediaType.APPLICATION_JSON.equals(preferredMediaType))
        {
            representation = this.getRepresentationRdf(status, request, response, preferredMediaType);
        }
        else
        // if (MediaType.TEXT_HTML.equals(preferredMediaType))
        {
            representation = this.getRepresentationHtml(status, request, response);
        }
        
        return representation;
    }
    
    /**
     * Returns an Error page representation in text/html.
     * 
     */
    private Representation getRepresentationHtml(final Status status, final Request request, final Response response)
    {
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(request);
        
        dataModel.put("contentTemplate", "error.html.ftl");
        dataModel.put("pageTitle", "An error occurred : HTTP " + status.getCode());
        dataModel.put("error_code", Integer.toString(status.getCode()));
        
        final StringBuilder message = new StringBuilder();
        if(status.getDescription() != null)
        {
            message.append(status.getDescription());
        }
        if(status.getThrowable() != null && status.getThrowable().getMessage() != null)
        {
            message.append(" (");
            message.append(status.getThrowable().getMessage());
            message.append(")");
        }
        dataModel.put("message", message.toString());
        
        return RestletUtils.getHtmlRepresentation("poddBase.html.ftl", dataModel, MediaType.TEXT_HTML,
                this.freemarkerConfiguration);
    }
    
    /**
     * Returns an Error page representation in RDF/XML.
     * 
     * @param preferredMediaType
     * 
     */
    private Representation getRepresentationRdf(final Status status, final Request request, final Response response,
            MediaType preferredMediaType)
    {
        final Model model = new LinkedHashModel();
        
        final URI errorUri = PoddRdfConstants.ERR_TYPE_ERROR;
        model.add(errorUri, PoddRdfConstants.HTTP_STATUS_CODE_VALUE, PoddRdfConstants.VF.createLiteral(status.getCode()));
        model.add(errorUri, PoddRdfConstants.HTTP_REASON_PHRASE, PoddRdfConstants.VF.createLiteral(status.getReasonPhrase()));
        
        String errorDescription = status.getDescription();
        model.add(errorUri, RDFS.COMMENT, PoddRdfConstants.VF.createLiteral(errorDescription));
        
        if(status.getThrowable() != null)
        {
            
            if(status.getThrowable() instanceof PoddException)
            {
                final Model errorModel = ((PoddException)status.getThrowable()).getDetailsAsModel();
                model.addAll(errorModel);
            }

            // TODO: Handle Ontology Not Consistent exceptions here and map the reasons specifically
            // so that web interface and client can describe to users why the ontology save failed
            
            // FIXME: move to FileReferenceVerificationFailureException.getDetailsAsModel()
            else if(status.getThrowable() instanceof FileReferenceVerificationFailureException)
            {
                FileReferenceVerificationFailureException fre =(FileReferenceVerificationFailureException)status.getThrowable();
                model.add(errorUri, PoddRdfConstants.ERR_EXCEPTION_CLASS,
                        PoddRdfConstants.VF.createLiteral(fre.getClass().getName()));
                
                final Map<DataReference, Throwable> validationFailures = fre.getValidationFailures();
                Iterator<DataReference> iterator = validationFailures.keySet().iterator();
                while (iterator.hasNext())
                {
                    final DataReference dataReference = iterator.next();
                    Throwable throwable = validationFailures.get(dataReference);
                    //TODO
                    model.add(dataReference.getObjectIri().toOpenRDFURI(), RDFS.LABEL, PoddRdfConstants.VF.createLiteral(throwable.getMessage()));
                    dataReference.getLabel();
                    
                    final BNode v = PoddRdfConstants.VF.createBNode();
                    model.add(errorUri, PoddRdfConstants.ERR_CONTAINS, v);
                    model.add(v, RDF.TYPE, PoddRdfConstants.ERR_TYPE_ERROR);
                    
                    final URI dataRefUri = dataReference.getObjectIri().toOpenRDFURI();
                    model.add(v, PoddRdfConstants.ERR_SOURCE, dataRefUri);
                    model.add(dataRefUri, RDFS.LABEL, PoddRdfConstants.VF.createLiteral(dataReference.getLabel()));
                    model.add(dataRefUri, RDFS.COMMENT, PoddRdfConstants.VF.createLiteral(throwable.getMessage()));
                }
                
            }
            // FIXME: move to FileReferenceVerificationFailureException.getDetailsAsModel()
            else if(status.getThrowable() instanceof FileReferenceInvalidException)
            {
                FileReferenceInvalidException fre =(FileReferenceInvalidException)status.getThrowable();
                model.add(errorUri, PoddRdfConstants.ERR_EXCEPTION_CLASS,
                        PoddRdfConstants.VF.createLiteral(fre.getClass().getName()));
                
                final URI fileRefUri = fre.getFileReference().getObjectIri().toOpenRDFURI();
                model.add(errorUri, PoddRdfConstants.ERR_SOURCE, fileRefUri);
                model.add(fileRefUri, RDFS.LABEL, PoddRdfConstants.VF.createLiteral(fre.getFileReference().getLabel()));
                
            }
        }
        
        // get a String representation of the statements in the Model
        final StringWriter out = new StringWriter();
        try
        {
            Rio.write(model, out, Rio.getWriterFormatForMIMEType(preferredMediaType.getName(), RDFFormat.RDFJSON));
        }
        catch(RDFHandlerException e)
        {
            this.log.error("Error writing RDF content in status service", e);
            // We're already trying to send back an error. So ignore this?
            // FIXME: The error may mean that the error message is not syntactically valid at this
            // point, so may need to overwrite it with a hardcoded string
        }
        
        return new AppendableRepresentation(out.toString(), MediaType.APPLICATION_RDF_XML, Language.DEFAULT,
                CharacterSet.UTF_8);
    }
}
