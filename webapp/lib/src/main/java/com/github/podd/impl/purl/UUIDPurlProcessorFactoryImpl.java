/**
 * 
 */
package com.github.podd.impl.purl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddProcessorStage;
import com.github.podd.api.purl.PoddPurlProcessor;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.purl.PoddPurlProcessorPrefixes;
import com.github.podd.exception.PoddRuntimeException;

/**
 * A Purl Processor Factory that creates <code>UUIDPurlProcessorImpl</code> instances.
 * 
 * 
 * @author kutila
 * 
 */
@MetaInfServices(PoddPurlProcessorFactory.class)
public class UUIDPurlProcessorFactoryImpl implements PoddPurlProcessorFactory
{
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private String prefix;
    
    private final List<String> temporaryUris = Collections.unmodifiableList(Arrays
            .asList(PoddPurlProcessorPrefixes.UUID.getTemporaryPrefix()));
    
    /* The fixed set of stages supported by this Factory */
    private static final Set<PoddProcessorStage> STAGES = Collections.singleton(PoddProcessorStage.RDF_PARSING);
    
    @Override
    public boolean canHandleStage(final PoddProcessorStage stage)
    {
        if(stage == null)
        {
            throw new NullPointerException("Cannot handle NULL stage");
        }
        return UUIDPurlProcessorFactoryImpl.STAGES.contains(stage);
    }
    
    @Override
    public String getKey()
    {
        return this.getClass().getName();
    }
    
    @Override
    public PoddPurlProcessor getProcessor()
    {
        if(this.temporaryUris.isEmpty())
        {
            throw new PoddRuntimeException("Not enough data (temporary URIs) to create SimplePoddPurlProcessor");
        }
        
        UUIDPurlProcessorImpl processor = null;
        if(this.prefix != null)
        {
            processor = new UUIDPurlProcessorImpl(this.prefix);
        }
        else
        {
            processor = new UUIDPurlProcessorImpl();
        }
        
        for(final String tempUri : this.temporaryUris)
        {
            processor.addTemporaryUriHandler(tempUri);
        }
        
        return processor;
    }
    
    @Override
    public String getSPARQLConstructBGP()
    {
        return "?subject ?predicate ?object";
    }
    
    @Override
    public String getSPARQLConstructWhere()
    {
        final StringBuilder builder = new StringBuilder();
        
        // match all triples
        builder.append(" ?subject ?predicate ?object ");
        
        // get a focused SPARQL by checking for the temporary URI patterns
        if(!this.temporaryUris.isEmpty())
        {
            builder.append("FILTER ( ");
            final int startLength = builder.length();
            
            for(final String tempUri : this.temporaryUris)
            {
                if(builder.length() > startLength)
                {
                    builder.append(" || ");
                }
                builder.append(" strstarts(STR(?subject), ");
                builder.append("\"");
                builder.append(tempUri);
                builder.append("\")");
                builder.append(" || ");
                builder.append(" strstarts(STR(?object), ");
                builder.append("\"");
                builder.append(tempUri);
                builder.append("\")");
            }
            builder.append(" ) ");
            
        }
        return builder.toString();
    }
    
    @Override
    public String getSPARQLGroupBy()
    {
        // an empty GROUP BY clause
        return "";
    }
    
    /**
     * Returns the variable assigned to the "subject" of the SPARQL graph. This variable could then
     * be used to retrieve RDF triples containing the given Subject. Note that any occurrences of
     * the given URI as a predicate/object are ignored.
     * 
     * @return Variable name assigned to "subjects" in the SPARQL construct query
     * 
     * @see com.github.podd.api.PoddRdfProcessorFactory#getSPARQLVariable()
     */
    @Override
    public String getSPARQLVariable()
    {
        return "subject";
    }
    
    @Override
    public Set<PoddProcessorStage> getStages()
    {
        return UUIDPurlProcessorFactoryImpl.STAGES;
    }
    
    /**
     * 
     * @param prefix
     *            The Prefix used by the PurlProcessor's created by this factory
     */
    public void setPrefix(final String prefix)
    {
        this.prefix = prefix;
    }
    
}
