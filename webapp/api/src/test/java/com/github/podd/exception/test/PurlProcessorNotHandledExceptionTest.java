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
package com.github.podd.exception.test;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.openrdf.model.URI;

import com.github.podd.api.purl.PoddPurlProcessor;
import com.github.podd.exception.PurlProcessorNotHandledException;

/**
 * Tests that the PurlProcessorNotHandledException correctly manages its internal content.
 *
 * @author kutila
 */
public class PurlProcessorNotHandledExceptionTest
{

    @Test
    public void testConstructorWithProcessorUriMessage() throws Exception
    {
        final PoddPurlProcessor thePurlProcessor = Mockito.mock(PoddPurlProcessor.class);
        final URI failedUri = Mockito.mock(URI.class);
        final String message = "Exception with mocked up internals";

        final PurlProcessorNotHandledException theException =
                new PurlProcessorNotHandledException(thePurlProcessor, failedUri, message);

        Assert.assertNotNull(theException.getGenerator());
        Assert.assertEquals(failedUri, theException.getInputUri());
        Assert.assertEquals(message, theException.getMessage());
    }

    @Test
    public void testConstructorWithProcessorUriMessageThrowable() throws Exception
    {
        final PoddPurlProcessor thePurlProcessor = Mockito.mock(PoddPurlProcessor.class);
        final URI failedUri = Mockito.mock(URI.class);
        final String message = "Exception with mocked up internals";
        final Exception rootCause = new Exception("root cause exception");

        final PurlProcessorNotHandledException theException =
                new PurlProcessorNotHandledException(thePurlProcessor, failedUri, message, rootCause);

        Assert.assertNotNull(theException.getGenerator());
        Assert.assertEquals(failedUri, theException.getInputUri());
        Assert.assertEquals(message, theException.getMessage());
        Assert.assertEquals(rootCause, theException.getCause());
    }

    @Test
    public void testConstructorWithProcessorUriThrowable() throws Exception
    {
        final PoddPurlProcessor thePurlProcessor = Mockito.mock(PoddPurlProcessor.class);
        final URI failedUri = Mockito.mock(URI.class);
        final Exception rootCause = new Exception("root cause of this exception");

        final PurlProcessorNotHandledException theException =
                new PurlProcessorNotHandledException(thePurlProcessor, failedUri, rootCause);

        Assert.assertNotNull(theException.getGenerator());
        Assert.assertEquals(failedUri, theException.getInputUri());
        Assert.assertEquals(rootCause, theException.getCause());
    }

}
