/**
 * 
 */
package com.github.podd.sensors.test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * @author ans025
 *
 */
public class SensorGetTest
{
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
    }
    
    @Test
    public void testGet() throws Exception
    {
        final ClientResource clientResource = new ClientResource("http://152.83.198.37/");
        
        final Representation representation = clientResource.get();
        
        System.out.println(representation.getMediaType().toString());
        System.out.println(clientResource.getStatus().getCode());
        StringWriter writer = new StringWriter();
        
        representation.write(writer);
        
        String index = writer.toString();
        
        System.out.println(index);
        
        Matcher fileMatcher = Pattern.compile("href=\"(.+)\"").matcher(index);
        while(fileMatcher.find())
        {
            System.out.println(fileMatcher.group(1));
        }

        Matcher dateMatcher = Pattern.compile("</a> (.+) (.+) ").matcher(index);
        while(dateMatcher.find())
        {
            System.out.println(dateMatcher.group(1));
            System.out.println(dateMatcher.group(2));
        }

        Matcher sizeMatcher = Pattern.compile("(.+) (.+)</li>").matcher(index);
        while(sizeMatcher.find())
        {
            System.out.println(sizeMatcher.group(2));
        }
    }
    
}
