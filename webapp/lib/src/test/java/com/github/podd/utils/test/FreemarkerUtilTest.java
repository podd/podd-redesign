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
                ValueFactoryImpl.getInstance().createLiteral(new Date()), };
        final String[] expectedOutputs = { "xsd:boolean", "xsd:int", "xsd:float", "xsd:double", "xsd:dateTime", };

        for(int i = 0; i < values.length; i++)
        {
            final String result = this.fmUtil.getDatatype(values[i]);
            Assert.assertEquals(expectedOutputs[i], result);
        }
    }
}
