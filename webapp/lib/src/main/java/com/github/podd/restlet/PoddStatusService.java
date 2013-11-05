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
package com.github.podd.restlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Model;
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
    
    private String convertModelToString(final Model model, final MediaType preferredMediaType)
    {
        final StringWriter out = new StringWriter();
        try
        {
            Rio.write(model, out, Rio.getWriterFormatForMIMEType(preferredMediaType.getName(), RDFFormat.RDFJSON));
        }
        catch(final RDFHandlerException e)
        {
            this.log.error("Error writing RDF content in status service", e);
            // We're already trying to send back an error. So ignore this?
            // FIXME: The error may mean that the error message is not
            // syntactically valid at this
            // point, so may need to overwrite it with a hardcoded string
        }
        return out.toString();
    }
    
    private Model getErrorAsModel(final Status status)
    {
        final Model model = new LinkedHashModel();
        
        final BNode topNode = PoddRdfConstants.VF.createBNode("error");
        model.add(topNode, RDF.TYPE, PoddRdfConstants.ERR_TYPE_TOP_ERROR);
        model.add(topNode, PoddRdfConstants.HTTP_STATUS_CODE_VALUE, PoddRdfConstants.VF.createLiteral(status.getCode()));
        model.add(topNode, PoddRdfConstants.HTTP_REASON_PHRASE,
                PoddRdfConstants.VF.createLiteral(status.getReasonPhrase()));
        
        final String errorDescription = status.getDescription();
        model.add(topNode, RDFS.COMMENT, PoddRdfConstants.VF.createLiteral(errorDescription));
        
        final BNode errorNode = PoddRdfConstants.VF.createBNode("cause");
        model.add(topNode, PoddRdfConstants.ERR_CONTAINS, errorNode);
        
        final Throwable throwable = status.getThrowable();
        if(throwable != null && throwable instanceof PoddException)
        {
            model.addAll(((PoddException)throwable).getDetailsAsModel(errorNode));
        }
        else
        {
            model.add(errorNode, RDF.TYPE, PoddRdfConstants.ERR_TYPE_ERROR);
            
            if(throwable != null)
            {
                model.add(errorNode, PoddRdfConstants.ERR_EXCEPTION_CLASS,
                        PoddRdfConstants.VF.createLiteral(throwable.getClass().getName()));
                
                if(throwable.getMessage() != null)
                {
                    model.add(errorNode, RDFS.LABEL, PoddRdfConstants.VF.createLiteral(throwable.getMessage()));
                }
                
                final StringWriter sw = new StringWriter();
                throwable.printStackTrace(new PrintWriter(sw));
                model.add(errorNode, RDFS.COMMENT, PoddRdfConstants.VF.createLiteral(sw.toString()));
            }
            else
            {
                model.add(errorNode, RDFS.COMMENT, PoddRdfConstants.VF.createLiteral("Error details unavailable"));
            }
        }
        return model;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.restlet.service.StatusService#getRepresentation(org.restlet.data. Status,
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
        
        final Model model = this.getErrorAsModel(status);
        
        final String errorModelAsString = this.convertModelToString(model, MediaType.APPLICATION_JSON);
        dataModel.put("message_details", errorModelAsString);
        
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
            final MediaType preferredMediaType)
    {
        final Model model = this.getErrorAsModel(status);
        
        // get a String representation of the statements in the Model
        final String modelAsString = this.convertModelToString(model, preferredMediaType);
        
        return new AppendableRepresentation(modelAsString, MediaType.APPLICATION_RDF_XML, Language.DEFAULT,
                CharacterSet.UTF_8);
    }
    
}
