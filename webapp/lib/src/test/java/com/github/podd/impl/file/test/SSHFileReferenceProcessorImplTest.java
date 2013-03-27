/**
 * 
 */
package com.github.podd.impl.file.test;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;

import com.github.podd.api.file.PoddFileReferenceProcessor;
import com.github.podd.api.file.PoddSSHFileReference;
import com.github.podd.api.file.PoddSSHFileReferenceProcessor;
import com.github.podd.api.file.test.AbstractPoddFileReferenceProcessorTest;
import com.github.podd.api.file.test.AbstractPoddSSHFileReferenceProcessorTest;
import com.github.podd.api.test.TestConstants;
import com.github.podd.impl.file.SSHFileReferenceProcessorImpl;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.RdfUtility;

/**
 * 
 * @author kutila
 */
public class SSHFileReferenceProcessorImplTest extends AbstractPoddSSHFileReferenceProcessorTest
{
    
    @Override
    protected PoddSSHFileReferenceProcessor getNewPoddSSHFileReferenceProcessor()
    {
        return new SSHFileReferenceProcessorImpl();
    }
    
}
