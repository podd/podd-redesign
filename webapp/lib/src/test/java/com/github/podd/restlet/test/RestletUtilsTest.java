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

import java.util.Collection;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.podd.restlet.RestletUtils;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class RestletUtilsTest
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
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.RestletUtils#extractRoleMappings(org.openrdf.model.Model)}.
     */
    @Test
    public void testExtractRoleMappingsEmpty()
    {
        final Map<RestletUtilRole, Collection<URI>> roleMappings =
                RestletUtils.extractRoleMappings(new LinkedHashModel());
        
        Assert.assertTrue(roleMappings.isEmpty());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.RestletUtils#getBaseDataModel(org.restlet.Request)}.
     */
    @Ignore
    @Test
    public void testGetBaseDataModel()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.RestletUtils#getBooleanFromParameter(org.restlet.data.Parameter)}
     * .
     */
    @Ignore
    @Test
    public void testGetBooleanFromParameter()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.RestletUtils#getHtmlRepresentation(java.lang.String, java.util.Map, org.restlet.data.MediaType, freemarker.template.Configuration)}
     * .
     */
    @Ignore
    @Test
    public void testGetHtmlRepresentation()
    {
        Assert.fail("Not yet implemented");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.restlet.RestletUtils#toRDFSerialisation(java.lang.String, org.openrdf.repository.Repository, org.openrdf.model.Resource[])}
     * .
     */
    @Ignore
    @Test
    public void testToRDFSerialisation()
    {
        Assert.fail("Not yet implemented");
    }
    
}
