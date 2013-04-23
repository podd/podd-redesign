/**
 * 
 */
package com.github.podd.utils.test;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.github.podd.utils.FreemarkerUtil;

/**
 * @author kutila
 * 
 */
public class FreemarkerUtilTest
{
    private FreemarkerUtil fmUtil;
    
    @Before
    public void setUp() throws Exception
    {
        this.fmUtil = new FreemarkerUtil();
    }
    
    @Test
    public void testClipProtocol() throws Exception
    {
        final Object[] testInputs =
                { "http://abc.net.au", "http://www.uq.edu.au", null, "mailto:x.sirault@csiro.au",
                        "https://www.google.com", "www.uq.edu.au", "mailto.com", "http.example.com",
                        ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddBase"),
                        ValueFactoryImpl.getInstance().createLiteral(44), };
        final String[] expectedOutputs =
                { "abc.net.au", "www.uq.edu.au", null, "x.sirault@csiro.au", "www.google.com", "www.uq.edu.au",
                        "mailto.com", "http.example.com", "purl.org/podd/ns/poddBase", "44", };
        
        for(int i = 0; i < testInputs.length; i++)
        {
            final String result = this.fmUtil.clipProtocol(testInputs[i]);
            Assert.assertEquals(expectedOutputs[i], result);
        }
    }
    
    @Test
    public void testGetDatatype() throws Exception
    {
        final Value[] values =
                { ValueFactoryImpl.getInstance().createLiteral(false),
                        ValueFactoryImpl.getInstance().createLiteral(55),
                        ValueFactoryImpl.getInstance().createLiteral(55f),
                        ValueFactoryImpl.getInstance().createLiteral(55.5),
                        ValueFactoryImpl.getInstance().createLiteral(new Date()),
                };
        final String[] expectedOutputs = { 
                "xsd:boolean", 
                "xsd:int",
                "xsd:float",
                "xsd:double", 
                "xsd:dateTime", 
                };
        
        for(int i = 0; i < values.length; i++)
        {
            final String result = this.fmUtil.getDatatype(values[i]);
            Assert.assertEquals(expectedOutputs[i], result);
        }
    }
}
