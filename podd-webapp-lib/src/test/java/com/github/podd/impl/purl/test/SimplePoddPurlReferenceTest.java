/**
 * 
 */
package com.github.podd.impl.purl.test;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.github.podd.api.purl.PoddPurlReference;
import com.github.podd.api.purl.test.AbstractPoddPurlReferenceTest;
import com.github.podd.impl.purl.SimplePoddPurlReference;

/**
 * @author kutila
 */
public class SimplePoddPurlReferenceTest extends AbstractPoddPurlReferenceTest
{

    @Override
    protected PoddPurlReference getNewPoddPurlReference()
    {
        URI tempUri = ValueFactoryImpl.getInstance().createURI("urn:temp:this/is/supposed/to/be/a:temporary:uri");
        URI purl = ValueFactoryImpl.getInstance().createURI("http://purl.org/this/is/supposed/to/be/a:purl");
        return new SimplePoddPurlReference(tempUri, purl);
    }
    
}
