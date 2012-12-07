/**
 * 
 */
package com.github.podd.impl.purl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddProcessorStage;
import com.github.podd.api.purl.PoddPurlProcessor;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.purl.PoddPurlProcessorPrefixes;

/**
 * A Simple Processor Factory that creates <code>SimpleUUIDPurlProcessor</code> instances.
 * 
 * 
 * @author kutila
 * 
 */
public class SimpleUUIDPurlProcessorFactory implements PoddPurlProcessorFactory
{
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private String prefix;
    
    private final List<String> temporaryUriArray = Collections.unmodifiableList(Arrays.asList(PoddPurlProcessorPrefixes.UUID.getTemporaryPrefix()));
    
    /* The fixed set of stages supported by this Factory */
    private static final Set<PoddProcessorStage> stages = new HashSet<PoddProcessorStage>(
            Arrays.asList(new PoddProcessorStage[] { PoddProcessorStage.RDF_PARSING }));
    
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
        if(!this.temporaryUriArray.isEmpty())
        {
            builder.append("FILTER ( ");
            final int startLength = builder.length();
            
            for(final String tempUri : this.temporaryUriArray)
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
    
    /**
     * Only retrieves RDF triples containing the given Subject. Note that any occurrences of the
     * given URI as a predicate/object are ignored.
     * 
     * <p>
     * --------------------------------------------------------------------------------------
     * <p>
     * TODO: This method should simply throw an exception as calling it does not make sense in
     * identifying temporary URIs for the purpose of generating PURLs. The current implementation
     * should be moved to a FileReferenceProcessor class.
     * <p>
     * --------------------------------------------------------------------------------------
     * <p>
     * 
     * @param subject
     *            The URI of a specific object to fetch results for.
     * @return A String which makes up the WHERE clause of a SPARQL construct query
     * 
     * @see com.github.podd.api.PoddRdfProcessorFactory#getSPARQLConstructWhere(URI)
     */
    @Override
    public String getSPARQLVariable()
    {
        return "subject";
    }
    
    @Override
    public String getSPARQLGroupBy()
    {
        // an empty GROUP BY clause
        return "";
    }
    
    @Override
    public boolean canHandleStage(final PoddProcessorStage stage)
    {
        if(stage == null)
        {
            throw new NullPointerException("Cannot handle NULL stage");
        }
        return SimpleUUIDPurlProcessorFactory.stages.contains(stage);
    }
    
    @Override
    public String getKey()
    {
        return this.getClass().getName();
    }
    
    @Override
    public PoddPurlProcessor getProcessor()
    {
        if(this.temporaryUriArray.isEmpty())
        {
            // NOTE: Could throw a custom exception, possibly extending a PoddRuntimeException ?
            throw new RuntimeException("Not enough data (temporary URIs) to create SimplePoddPurlProcessor");
        }
        
        SimpleUUIDPurlProcessor processor = null;
        if(this.prefix != null)
        {
            processor = new SimpleUUIDPurlProcessor(this.prefix);
        }
        else
        {
            processor = new SimpleUUIDPurlProcessor();
        }
        
        for(final String tempUri : this.temporaryUriArray)
        {
            processor.addTemporaryUriHandler(tempUri);
        }
        
        return processor;
    }
    
    @Override
    public Set<PoddProcessorStage> getStages()
    {
        return SimpleUUIDPurlProcessorFactory.stages;
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
