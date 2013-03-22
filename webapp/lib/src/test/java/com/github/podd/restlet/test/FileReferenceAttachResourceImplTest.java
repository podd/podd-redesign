/**
 * 
 */
package com.github.podd.restlet.test;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.restlet.resource.ClientResource;

import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class FileReferenceAttachResourceImplTest extends AbstractResourceImplTest
{
    
    /**
     * Test successful attach of a file reference in Turtle
     */
    @Ignore
    @Test
    public void testErrorAttachInvalidFileReference() throws Exception
    {
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
        
        Assert.fail("TODO: implement me");
    }

    /**
     * Test successful attach of a file reference in Turtle
     */
    @Ignore
    @Test
    public void testAttachFileReferenceBasicRdf() throws Exception
    {
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
        
        Assert.fail("TODO: implement me");
    }
    
    
    /**
     * Test successful attach of a file reference in Turtle
     */
    @Ignore
    @Test
    public void testAttachFileReferenceBasicTurtle() throws Exception
    {
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
        
        Assert.fail("TODO: implement me");
    }
    
}
