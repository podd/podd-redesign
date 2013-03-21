/**
 * 
 */
package com.github.podd.client.impl.restlet.test;

import org.github.podd.client.api.test.AbstractPoddClientTest;
import org.restlet.resource.ClientResource;

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
    
    @Override
    protected void resetTestPoddServer()
    {
        // Reset server after each test so that assertions are not dependent on the order of the
        // tests, which is unpredictable
        // HACK: This presumes that this reset service will exist and that it has this URL
        final ClientResource resource = new ClientResource(this.getTestPoddServerUrl() + "/reset/r3set");
        resource.get();
    }
    
}
