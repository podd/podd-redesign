/*
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
 /**
 * 
 */
package com.github.podd.impl.purl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.purl.PoddPurlProcessor;
import com.github.podd.api.purl.PoddPurlReference;
import com.github.podd.exception.PurlProcessorNotHandledException;

/**
 * A simple permanent URI generator using UUIDs.
 * <p/>
 * The conversion process replaces the temporary URI prefix with a prefix of the form
 * <code>{new_prefix}{unique-id}</code> where
 * 
 * {new_prefix} is a prefix assigned during Processor creation or the default prefix
 * <code>http://purl.org/podd/</code>
 * 
 * {unique-id} is a random universally unique ID internally generated based on
 * {@link java.util.UUID#randomUUID()}.
 * 
 * @author kutila
 * 
 */
public class UUIDPurlProcessorImpl implements PoddPurlProcessor
{
    public static final String DEFAULT_PREFIX = "http://example.org/purl/";
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private Set<String> supportedTemporaryUriPrefixes = new HashSet<String>();
    
    /**
     * Prefix to use with generated Purls
     */
    private final String prefix;
    
    public UUIDPurlProcessorImpl()
    {
        this(UUIDPurlProcessorImpl.DEFAULT_PREFIX);
    }
    
    public UUIDPurlProcessorImpl(final String prefix)
    {
        this.prefix = prefix;
    }
    
    @Override
    public void addTemporaryUriHandler(final String temporaryUriPrefix)
    {
        if(temporaryUriPrefix == null)
        {
            throw new NullPointerException("Temporary URI prefix cannot be NULL");
        }
        this.supportedTemporaryUriPrefixes.add(temporaryUriPrefix);
    }
    
    @Override
    public boolean canHandle(final URI inputUri)
    {
        // since this Purl generator cannot create new PURLs from scratch
        if(inputUri == null)
        {
            return false;
        }
        
        final String inputStr = inputUri.stringValue();
        for(final String tempPrefix : this.supportedTemporaryUriPrefixes)
        {
            if(inputStr.startsWith(tempPrefix))
            {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<String> getTemporaryUriHandlers()
    {
        return new ArrayList<String>(this.supportedTemporaryUriPrefixes);
    }
    
    @Override
    public PoddPurlReference handleTranslation(final URI inputUri) throws PurlProcessorNotHandledException
    {
        return this.handleTranslation(inputUri, null);
    }
    
    @Override
    public PoddPurlReference handleTranslation(final URI inputUri, final URI parentUri)
        throws PurlProcessorNotHandledException
    {
        if(inputUri == null)
        {
            throw new NullPointerException("NULL URI cannot be handled by this Purl Processor");
        }
        
        // find which of the supported temporary URI prefixes is used in this inputUri
        String theTemporaryPrefix = null;
        final String inputStr = inputUri.stringValue();
        for(final String aTemporaryPrefix : this.supportedTemporaryUriPrefixes)
        {
            if(inputStr.startsWith(aTemporaryPrefix))
            {
                theTemporaryPrefix = aTemporaryPrefix;
                break;
            }
        }
        if(theTemporaryPrefix == null)
        {
            throw new PurlProcessorNotHandledException(this, inputUri,
                    "The input URI cannot be handled by this Purl Processor");
        }
        
        // generate the PURL
        final StringBuilder b = new StringBuilder();
        if(parentUri != null && parentUri.stringValue().startsWith(this.prefix))
        {
            b.append(this.prefix);
            
            // get the first slash after the prefix
            final int index = parentUri.stringValue().indexOf('/', this.prefix.length());
            // get the UUID from after the end of the prefix (until the next slash)
            if(index != -1)
            {
                b.append(parentUri.stringValue().substring(this.prefix.length(), index));
            }
            else
            {
                b.append(parentUri.stringValue().substring(this.prefix.length()));
            }
        }
        else
        {
            b.append(this.prefix);
            b.append(UUID.randomUUID().toString());
        }
        b.append("/");
        b.append(inputStr.substring(theTemporaryPrefix.length()));
        
        final URI purl = ValueFactoryImpl.getInstance().createURI(b.toString());
        
        this.log.debug("Generated PURL {}", purl);
        
        return new SimplePoddPurlReference(inputUri, purl);
    }
    
    @Override
    public void removeTemporaryUriHandler(final String temporaryUriPrefix)
    {
        this.supportedTemporaryUriPrefixes.remove(temporaryUriPrefix);
    }
    
}
