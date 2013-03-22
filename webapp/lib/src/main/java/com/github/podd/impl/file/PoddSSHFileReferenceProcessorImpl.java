package com.github.podd.impl.file;

import java.util.Collection;

import org.openrdf.model.Graph;

import com.github.podd.api.file.PoddFileReference;
import com.github.podd.api.file.PoddSSHFileReferenceProcessor;

public class PoddSSHFileReferenceProcessorImpl implements PoddSSHFileReferenceProcessor 
{

    @Override
    public boolean canHandle(Graph rdfStatements)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Collection<PoddFileReference> createReferences(Graph rdfStatements)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
}
