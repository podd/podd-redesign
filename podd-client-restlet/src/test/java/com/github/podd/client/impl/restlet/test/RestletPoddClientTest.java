/**
 * 
 */
package com.github.podd.client.impl.restlet.test;

import org.github.podd.client.api.test.AbstractPoddClientTest;

import com.github.podd.client.api.PoddClient;
import com.github.podd.client.impl.restlet.RestletPoddClientImpl;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class RestletPoddClientTest extends AbstractPoddClientTest
{
    
    /*
     * (non-Javadoc)
     * 
     * @see org.github.podd.client.api.test.AbstractPoddClientTest#getNewPoddClientInstance()
     */
    @Override
    protected PoddClient getNewPoddClientInstance()
    {
        return new RestletPoddClientImpl();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.github.podd.client.api.test.AbstractPoddClientTest#getPoddServerUrl()
     */
    @Override
    protected String getPoddServerUrl()
    {
        return "";
    }
    
}
