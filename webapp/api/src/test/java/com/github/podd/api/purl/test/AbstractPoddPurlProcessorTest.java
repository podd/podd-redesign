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
package com.github.podd.api.purl.test;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.github.podd.api.purl.PoddPurlProcessor;
import com.github.podd.api.purl.PoddPurlReference;
import com.github.podd.exception.PurlProcessorNotHandledException;

/**
 * Abstract class to test PoddPurlProcessor.
 *
 * @author kutila
 *
 */
public abstract class AbstractPoddPurlProcessorTest
{

    protected PoddPurlProcessor purlProcessor;

    protected String prefixUrnTemp = null;
    protected String prefixExampleUrl = null;
    protected String prefixPurl = null;

    /**
     * @return A new PoddPurlProcessor instance for use by the test
     */
    protected abstract PoddPurlProcessor getNewPoddPurlProcessor();

    /**
     * Checks whether the given purl could have been generated from the given temporary URI.
     * <p/>
     * Note: Since purl generation may involve some randomness, this method may only be able to make
     * a partial comparison to see if it could have originated as the given temporary URI.
     *
     * @param purl
     * @param tempUri
     * @return True if the Purl could have been generated from the tempUri. False otherwise
     */
    protected abstract boolean isPurlGeneratedFromTemp(URI purl, URI tempUri);

    @Before
    public void setUp() throws Exception
    {
        this.purlProcessor = this.getNewPoddPurlProcessor();
        Assert.assertNotNull("Null implementation of test processor", this.purlProcessor);

        this.prefixUrnTemp = "urn:temp:";
        this.prefixExampleUrl = "http://example.org/";
        this.prefixPurl = "http://purl.org/";
    }

    @After
    public void tearDown() throws Exception
    {
        this.purlProcessor = null;

        this.prefixUrnTemp = null;
        this.prefixExampleUrl = null;
        this.prefixPurl = null;
    }

    /**
     * Simple test to check that addTemporaryUriHandler() can be invoked
     *
     * @throws Exception
     */
    @Test
    public void testAddTemporaryUriHandler() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixPurl);
    }

    /**
     * Tests purlProcessor.addTemporaryUriHandler(null)
     *
     * @throws Exception
     */
    @Test
    public void testAddTemporaryUriHandlerWithNull() throws Exception
    {
        try
        {
            this.purlProcessor.addTemporaryUriHandler(null);
            Assert.fail("Adding a NULL temporary URI prefix should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertNotNull(e);
        }
    }

    /**
     * Test a PoddPurlProcessor that has not been assigned any temporary URI prefixes
     *
     * @throws Exception
     */
    @Test
    public void testCanHandleWithNoPrefixes() throws Exception
    {
        final URI tempUriUnsupported = ValueFactoryImpl.getInstance().createURI("urn:unsupported:temporary/uri");
        Assert.assertFalse(this.purlProcessor.canHandle(tempUriUnsupported));
    }

    /**
     * Tests the behaviour when a null value is given to the canHandle() method
     */
    @Test
    public void testCanHandleWithNull() throws Exception
    {
        Assert.assertFalse(this.purlProcessor.canHandle(null));
    }

    @Test
    public void testCanHandleWithOnePrefix() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixUrnTemp);

        final URI tempUriUrnTemp = ValueFactoryImpl.getInstance().createURI(this.prefixUrnTemp + "some/path");
        Assert.assertTrue(this.purlProcessor.canHandle(tempUriUrnTemp));
    }

    @Test
    public void testCanHandleWithTwoPrefixes() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixUrnTemp);
        this.purlProcessor.addTemporaryUriHandler(this.prefixExampleUrl);

        final URI tempUriUrnTemp = ValueFactoryImpl.getInstance().createURI(this.prefixUrnTemp + "some/path");
        Assert.assertTrue(this.purlProcessor.canHandle(tempUriUrnTemp));

        final URI tempUriExampleOrg =
                ValueFactoryImpl.getInstance().createURI(this.prefixExampleUrl + "some/other/path");
        Assert.assertTrue(this.purlProcessor.canHandle(tempUriExampleOrg));
    }

    /**
     * Test a PoddPurlProcessor that has not been assigned any temporary URI prefixes
     *
     * @throws Exception
     */
    @Test
    public void testGetTemporaryUriHandlersWithNoPrefixes() throws Exception
    {
        final List<String> prefixList = this.purlProcessor.getTemporaryUriHandlers();
        Assert.assertNotNull(prefixList);
        Assert.assertEquals(0, prefixList.size());
    }

    @Test
    public void testGetTemporaryUriHandlersWithTwoPrefixes() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixUrnTemp);
        this.purlProcessor.addTemporaryUriHandler(this.prefixExampleUrl);

        final List<String> prefixList = this.purlProcessor.getTemporaryUriHandlers();
        Assert.assertNotNull(prefixList);
        Assert.assertEquals(2, prefixList.size());
        Assert.assertTrue(prefixList.contains(this.prefixUrnTemp));
        Assert.assertTrue(prefixList.contains(this.prefixExampleUrl));

        // this unregistered prefix is not in the list
        Assert.assertFalse(prefixList.contains(this.prefixPurl));
    }

    @Test
    public void testHandleTranslationSuccessful() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixUrnTemp);
        final URI tempUriUrnTemp = ValueFactoryImpl.getInstance().createURI(this.prefixUrnTemp + "artifact:1482");

        final PoddPurlReference purlReference = this.purlProcessor.handleTranslation(tempUriUrnTemp);

        Assert.assertNotNull(purlReference);
        Assert.assertEquals(tempUriUrnTemp, purlReference.getTemporaryURI());
        Assert.assertTrue(this.isPurlGeneratedFromTemp(purlReference.getPurlURI(), tempUriUrnTemp));
    }

    @Test
    public void testHandleTranslationUnSupported() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixUrnTemp);
        final URI tempUriUrnTemp = ValueFactoryImpl.getInstance().createURI(this.prefixExampleUrl + "artifact:8275");

        try
        {
            this.purlProcessor.handleTranslation(tempUriUrnTemp);
            Assert.fail("Expected PurlGeneratorNotHandledException was not thrown");
        }
        catch(final PurlProcessorNotHandledException e)
        {
            Assert.assertNotNull(e);
        }
    }

    /**
     * Tests the behaviour when a null value is given to the handleTranslation() method
     */
    @Test
    public void testHandleTranslationWithNull() throws Exception
    {
        try
        {
            this.purlProcessor.handleTranslation(null);
            Assert.fail("Expected NullPointerException was not thrown");
        }
        catch(final NullPointerException e)
        {
            // fine as this is an expected exception
        }
    }

    /**
     * Tests handleTranslation(inputUri, parentUri). Passing NULL to parentUri is equivalent to
     * calling handleTranslation(inputUri).
     *
     * @throws Exception
     */
    @Test
    public void testHandleTranslationWithParentUriNull() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixUrnTemp);
        final URI tempUriUrnTemp = ValueFactoryImpl.getInstance().createURI(this.prefixUrnTemp + "artifact:1482");

        final PoddPurlReference purlReference = this.purlProcessor.handleTranslation(tempUriUrnTemp, null);

        Assert.assertNotNull(purlReference);
        Assert.assertEquals(tempUriUrnTemp, purlReference.getTemporaryURI());
        Assert.assertTrue(this.isPurlGeneratedFromTemp(purlReference.getPurlURI(), tempUriUrnTemp));
    }

    @Test
    public void testRemoveTemporaryUriHandler() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixUrnTemp);
        this.purlProcessor.addTemporaryUriHandler(this.prefixExampleUrl);

        final URI tempUriUrnTemp = ValueFactoryImpl.getInstance().createURI(this.prefixUrnTemp + "some/path");
        Assert.assertTrue(this.purlProcessor.canHandle(tempUriUrnTemp));

        this.purlProcessor.removeTemporaryUriHandler(this.prefixUrnTemp);

        // tempUriUrnTemp is no longer supported
        Assert.assertFalse(this.purlProcessor.canHandle(tempUriUrnTemp));

        // prefixExampleUrl is still supported
        final URI tempUriExampleOrg =
                ValueFactoryImpl.getInstance().createURI(this.prefixExampleUrl + "some/other/path");
        Assert.assertTrue(this.purlProcessor.canHandle(tempUriExampleOrg));
    }

}
