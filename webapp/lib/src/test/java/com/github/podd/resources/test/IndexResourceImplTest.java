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
package com.github.podd.resources.test;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.github.podd.restlet.test.PoddRestletTestUtils;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class IndexResourceImplTest extends AbstractResourceImplTest
{
    /**
     * Test unauthenticated access to /index
     */
    @Test
    public void testGetIndexWithoutAuthentication() throws Exception
    {
        final ClientResource indexClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_INDEX));
        
        try
        {
            final Representation results =
                    PoddRestletTestUtils.doTestUnAuthenticatedRequest(indexClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK);
            
            final String body = this.getText(results);
            Assert.assertTrue(body.contains("Welcome to PODD, please"));
            this.assertFreemarker(body);
        }
        finally
        {
            this.releaseClient(indexClientResource);
        }
    }
    
}
