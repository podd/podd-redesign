/*
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
 /**
 * 
 */
package com.github.podd.impl.purl.test;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.github.podd.api.purl.PoddPurlProcessor;
import com.github.podd.api.purl.PoddPurlReference;
import com.github.podd.api.purl.test.AbstractPoddPurlProcessorTest;
import com.github.podd.impl.purl.UUIDPurlProcessorImpl;

/**
 * @author kutila
 * 
 */
public class UUIDPurlProcessorImplNonDefaultPrefixTest extends AbstractPoddPurlProcessorTest
{
    
    private final String purlPrefix = "urn:example:purl:";
    
    @Override
    protected PoddPurlProcessor getNewPoddPurlProcessor()
    {
        return new UUIDPurlProcessorImpl(this.purlPrefix);
    }
    
    @Override
    protected boolean isPurlGeneratedFromTemp(final URI purl, final URI tempUri)
    {
        // NOTE: This method will start giving incorrect results when
        // multiple PurlProcessors use the same prefix
        return purl.stringValue().startsWith(this.purlPrefix);
    }
    
    /**
     * Tests handleTranslation(inputUri, parentUri) with a valid Parent URI. Translation consists of
     * replacing the temporary URI prefix with the parent URI, which is a unique prefix.
     * 
     * @throws Exception
     */
    @Test
    public void testHandleTranslationWithParentUriSuccessful() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixUrnTemp);
        final URI tempUriUrnTemp = ValueFactoryImpl.getInstance().createURI(this.prefixUrnTemp + "artifact:1482");
        final URI parentUri = ValueFactoryImpl.getInstance().createURI(this.purlPrefix + "S0ME-UN1QUE-ID/");
        
        final PoddPurlReference purlReference = this.purlProcessor.handleTranslation(tempUriUrnTemp, parentUri);
        
        Assert.assertNotNull(purlReference);
        Assert.assertEquals(tempUriUrnTemp, purlReference.getTemporaryURI());
        Assert.assertTrue(this.isPurlGeneratedFromTemp(purlReference.getPurlURI(), tempUriUrnTemp));
        
        Assert.assertEquals("Not the expected Purl", this.purlPrefix + "S0ME-UN1QUE-ID/artifact:1482", purlReference
                .getPurlURI().stringValue());
    }
    
}
