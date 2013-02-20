/**
 * 
 */
package com.github.podd.client.impl.restlet.test;

import org.github.podd.client.api.test.AbstractPoddClientTest;

import com.github.podd.client.api.PoddClient;
import com.github.podd.client.impl.restlet.RestletPoddClientImpl;

/**
 * Integration test for the Restlet PODD Client API implementation.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class RestletPoddClientImplIntegrationTest extends AbstractPoddClientTest
{
    @Override
    protected PoddClient getNewPoddClientInstance()
    {
        return new RestletPoddClientImpl();
    }
    
    @Override
    protected String getTestPoddServerUrl()
    {
        return "http://localhost:9090/podd-test";
    }
    
}
