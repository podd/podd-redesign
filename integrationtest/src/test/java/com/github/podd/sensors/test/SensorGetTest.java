/**
 * 
 */
package com.github.podd.sensors.test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
        final ClientResource clientResource = new ClientResource("http://152.83.198.37/TC_LOG00.CSV");
        
        final Representation representation = clientResource.get();
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        representation.write(outputStream);

        
    }
    
}
