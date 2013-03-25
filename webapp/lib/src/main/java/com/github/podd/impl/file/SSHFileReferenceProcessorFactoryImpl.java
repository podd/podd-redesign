package com.github.podd.impl.file;

import java.util.Collections;
import java.util.Set;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddProcessorStage;
import com.github.podd.api.file.PoddFileReferenceProcessor;
import com.github.podd.api.file.PoddFileReferenceProcessorFactory;
import com.github.podd.api.file.PoddSSHFileReferenceProcessorFactory;

@MetaInfServices(PoddFileReferenceProcessorFactory.class)
public class SSHFileReferenceProcessorFactoryImpl implements PoddSSHFileReferenceProcessorFactory
{

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /* The fixed set of stages supported by this Factory */
    private static final Set<PoddProcessorStage> STAGES = Collections.singleton(PoddProcessorStage.RDF_PARSING);
    
    @Override
    public boolean canHandleStage(final PoddProcessorStage stage)
    {
        if(stage == null)
        {
            throw new NullPointerException("Cannot handle NULL stage");
        }
        return SSHFileReferenceProcessorFactoryImpl.STAGES.contains(stage);
    }
    
    @Override
    public String getKey()
    {
        return this.getClass().getName();
    }
    
    
    @Override
    public String getSPARQLConstructBGP()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSPARQLConstructWhere()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSPARQLGroupBy()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSPARQLVariable()
    {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public PoddFileReferenceProcessor getProcessor()
    {
        // TODO - configure processor
        return new SSHFileReferenceProcessorImpl();
    }

    @Override
    public Set<PoddProcessorStage> getStages()
    {
        return SSHFileReferenceProcessorFactoryImpl.STAGES;
    }
    
}
