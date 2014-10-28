/**
 * PODD is an OWL ontology database used for scientific project management
 *
 * Copyright (C) 2009-2013 The University Of Queensland
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
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
    
    public static Representation doTestUnauthenticatedRequest(final ClientResource clientResource,
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
