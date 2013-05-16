/**
 * 
 */
package com.github.podd.impl.purl.test;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
import com.github.podd.api.purl.PoddPurlReference;
import com.github.podd.api.purl.test.AbstractPoddPurlManagerTest;
import com.github.podd.impl.purl.PoddPurlManagerImpl;
import com.github.podd.impl.purl.UUIDPurlProcessorFactoryImpl;

/**
 * @author kutila
 * 
 */
public class PoddPurlManagerImplTest extends AbstractPoddPurlManagerTest
{
    protected String purlPrefix = "http://purl.org/podd/";
    
    @Override
    public PoddPurlManager getNewPoddPurlManager()
    {
        return new PoddPurlManagerImpl();
    }
    
    @Override
    public PoddPurlProcessorFactoryRegistry getNewPoddPurlProcessorFactoryRegistry()
    {
        final PoddPurlProcessorFactoryRegistry registry = new PoddPurlProcessorFactoryRegistry();
        registry.clear();
        
        final UUIDPurlProcessorFactoryImpl uuidPurlProcessorFactory = new UUIDPurlProcessorFactoryImpl();
        uuidPurlProcessorFactory.setPrefix(this.purlPrefix);
        registry.add(uuidPurlProcessorFactory);
        
        return registry;
    }
    
    public void internalTestExtractPurlReferenceWithParentUri(final URI parentUri, final boolean useParentUri)
        throws Exception
    {
        final URI context = this.loadTestResources();
        
        final Set<PoddPurlReference> purlSet =
                this.testPurlManager.extractPurlReferences(parentUri, this.testRepositoryConnection, context);
        
        Assert.assertNotNull("Extracted Purl references were null", purlSet);
        Assert.assertFalse("Extracted Purl references were empty", purlSet.isEmpty());
        Assert.assertEquals("Incorrect number of Purl references extracted", 3, purlSet.size());
        
        for(final PoddPurlReference purl : purlSet)
        {
            Assert.assertNotNull("Purl has null temporary URI", purl.getTemporaryURI());
            Assert.assertNotNull("Purl has null permanent URI", purl.getPurlURI());
            
            Assert.assertFalse("Purl and Temporary URI were same", purl.getPurlURI().equals(purl.getTemporaryURI()));
            
            // check temporary URI is present in the original RDF statements as a subject or object
            final boolean tempUriExistsAsSubject =
                    this.testRepositoryConnection.getStatements(purl.getTemporaryURI(), null, null, false, context)
                            .hasNext();
            final boolean tempUriExistsAsObject =
                    this.testRepositoryConnection.getStatements(null, null, purl.getTemporaryURI(), false, context)
                            .hasNext();
            Assert.assertTrue("Temporary URI not found in original RDF statements", tempUriExistsAsSubject
                    || tempUriExistsAsObject);
            
            // The following assertion checks whether the generated Purls use the parent URI or not
            if(useParentUri)
            {
                Assert.assertTrue("Purl not using parent URI",
                        purl.getPurlURI().stringValue().startsWith(parentUri.stringValue()));
            }
            else
            {
                Assert.assertFalse("Purl should not parent URI",
                        purl.getPurlURI().stringValue().startsWith(parentUri.stringValue()));
            }
        }
    }
    
    /**
     * Tests extractPurlReferences(URI, RepositoryConnection, URI) passing in a parent URI different
     * to the prefix used in generated Purls.
     * 
     * This parent URI should be ignored and Purls generated using internally generated UUIDs.
     * 
     * @throws Exception
     */
    @Test
    public void testExtractPurlReferencesWithNonMatchingParentUri() throws Exception
    {
        final URI parentUri =
                ValueFactoryImpl.getInstance().createURI("http://purl.org/nodd/same-UUID-for-this-purl-set-11");
        this.internalTestExtractPurlReferenceWithParentUri(parentUri, false);
    }
    
    /**
     * Tests extractPurlReferences(URI, RepositoryConnection, URI) passing in a parent URI with the
     * same prefix as that used in generated Purls.
     * 
     * This parent URI should then be used in all Purls instead of generating UUIDs.
     * 
     * @throws Exception
     */
    @Test
    public void testExtractPurlReferencesWithParentUri() throws Exception
    {
        final URI parentUri =
                ValueFactoryImpl.getInstance().createURI(this.purlPrefix + "same-UUID-for-this-purl-set-11");
        this.internalTestExtractPurlReferenceWithParentUri(parentUri, true);
    }
    
}
