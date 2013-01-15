/**
 * 
 */
package com.github.podd.restlet.test;


import org.junit.Assert;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.test.RestletTestUtils;

/**
 * Test utility methods specific to Podd should be located in this class.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class PoddRestletTestUtils extends RestletTestUtils
{
    private static final Logger log = LoggerFactory.getLogger(PoddRestletTestUtils.class);

    public static Representation doTestUnAuthenticatedRequest(final ClientResource clientResource,
            final Method requestMethod, final Object inputRepresentation, final MediaType requestMediaType,
            final Status expectedResponseStatus)
    {
        Representation result = null;
        
        if(requestMethod.equals(Method.DELETE))
        {
            result = clientResource.delete(requestMediaType);
        }
        else if(requestMethod.equals(Method.PUT))
        {
            result = clientResource.put(inputRepresentation, requestMediaType);
        }
        else if(requestMethod.equals(Method.GET))
        {
            result = clientResource.get(requestMediaType);
        }
        else if(requestMethod.equals(Method.POST))
        {
            result = clientResource.post(inputRepresentation, requestMediaType);
        }
        else
        {
            throw new RuntimeException("Did not recognise request method: " + requestMethod.toString());
        }
        
        Assert.assertEquals(expectedResponseStatus.getCode(), clientResource.getResponse().getStatus().getCode());
        
        return result;
    }
    

}
