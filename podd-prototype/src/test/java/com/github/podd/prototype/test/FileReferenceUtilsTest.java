package com.github.podd.prototype.test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.prototype.FileReference;
import com.github.podd.prototype.FileReferenceUtils;
import com.github.podd.prototype.HttpFileReference;
import com.github.podd.prototype.PoddException;

public class FileReferenceUtilsTest
{
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    protected FileReferenceUtils utils = FileReferenceUtils.getInstance();
    
    @Before
    public void setUp() throws Exception
    {
        this.utils.initialize("src/test/resources/test/alias.txt");
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.utils.clean();
    }
    
    @Test
    public void testCheckFileExists_simpleFailures() throws Exception
    {
        // test with an unsupported FileReference format
        final FileReference emptyRef = new FileReference();
        try
        {
            this.utils.checkFileExists(emptyRef);
            Assert.fail("Should have thrown an exception during validate");
        }
        catch(final PoddException e)
        {
            Assert.assertNotNull(e);
        }
        
        // test with an empty FileReference
        final FileReference httpRef = new HttpFileReference();
        try
        {
            this.utils.checkFileExists(httpRef);
            Assert.fail("Should have thrown an exception during validate");
        }
        catch(final Exception e)
        {
            Assert.assertNotNull(e);
        }
        
        // test with an invalid server alias
        httpRef.setServerAlias("noSuchAlias");
        try
        {
            this.utils.checkFileExists(httpRef);
            Assert.fail("Should have thrown an exception during validate");
        }
        catch(final Exception e)
        {
            Assert.assertNotNull(e);
        }
        
        httpRef.setServerAlias("localhost");
        httpRef.setArtifactUri(null);
        httpRef.setObjectUri(null);
        ((HttpFileReference)httpRef).setFilename("something");
        ((HttpFileReference)httpRef).setPath("somepath");
        ((HttpFileReference)httpRef).setDescription(null);
        
        try
        {
            this.utils.checkFileExists(httpRef);
            Assert.fail("Should have thrown an exception during validate");
        }
        catch(final Exception e)
        {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testCheckFileExists_httpFileRef() throws Exception
    {
        // validating "http://www.w3.org/Protocols/rfc2616/rfc2616.html" exists
        final HttpFileReference httpRef = new HttpFileReference();
        httpRef.setServerAlias("w3");
        httpRef.setArtifactUri(null);
        httpRef.setObjectUri(null);
        httpRef.setFilename("rfc2616.html");
        httpRef.setPath("Protocols/rfc2616");
        httpRef.setDescription("HTTP RFC");
        
        this.utils.checkFileExists(httpRef);
    }
    
    @Test
    public void testConstructFileReferenceFromMap() throws Exception
    {
        final Map<String, String[]> map = new HashMap<String, String[]>();
        Assert.assertNull(this.utils.constructFileReferenceFromMap(map));
        
        map.put(FileReferenceUtils.KEY_ARTIFACT_URI, new String[] { "http://example.org/podd/artifact:12" });
        Assert.assertNull(this.utils.constructFileReferenceFromMap(map));
        map.put(FileReferenceUtils.KEY_OBJECT_URI, new String[] { "urn:poddinternal:permanent:object34" });
        Assert.assertNull(this.utils.constructFileReferenceFromMap(map));
        
        map.put(FileReferenceUtils.KEY_FILE_SERVER_ALIAS, new String[] { "some_alias" });
        Assert.assertNull(this.utils.constructFileReferenceFromMap(map));
        map.put(FileReferenceUtils.KEY_FILE_PATH, new String[] { "some_file_path" });
        Assert.assertNull(this.utils.constructFileReferenceFromMap(map));
        map.put(FileReferenceUtils.KEY_FILE_NAME, new String[] { "some_file_name" });
        // description is optional
        Assert.assertNotNull(this.utils.constructFileReferenceFromMap(map));
        
    }
    
    @Test
    public void testCheckFileReferenceInRDFWithNone() throws Exception
    {
        final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/fragment.rdf");
        Assert.assertNotNull("Resource was not found", in);
        
        Repository tempRepository = null;
        RepositoryConnection tempRepositoryConnection = null;
        
        // create a temporary in-memory repository
        tempRepository = new SailRepository(new MemoryStore());
        tempRepository.initialize();
        tempRepositoryConnection = tempRepository.getConnection();
        tempRepositoryConnection.setAutoCommit(false);
        
        try
        {
            // populate repository with incoming RDF statements
            final URI intContext = IRI.create("urn:intermediate:").toOpenRDFURI();
            tempRepositoryConnection.add(in, "", RDFFormat.RDFXML, intContext);
            
            this.utils.checkFileReferencesInRDF(tempRepositoryConnection, intContext);
        }
        finally
        {
            
            if(tempRepositoryConnection != null)
            {
                tempRepositoryConnection.rollback();
                tempRepositoryConnection.close();
            }
            if(tempRepository != null)
            {
                tempRepository.shutDown();
            }
        }
    }
    
    @Test
    public void testCheckFileReferenceInRDF() throws Exception
    {
        final InputStream in =
                this.getClass().getResourceAsStream("/test/artifacts/fragmentWithInvalidFileReference.rdf");
        Assert.assertNotNull("Resource was not found", in);
        
        Repository tempRepository = null;
        RepositoryConnection tempRepositoryConnection = null;
        
        // create a temporary in-memory repository
        tempRepository = new SailRepository(new MemoryStore());
        tempRepository.initialize();
        tempRepositoryConnection = tempRepository.getConnection();
        tempRepositoryConnection.setAutoCommit(false);
        
        try
        {
            // populate repository with incoming RDF statements
            final URI intContext = IRI.create("urn:intermediate:").toOpenRDFURI();
            tempRepositoryConnection.add(in, "", RDFFormat.RDFXML, intContext);
            tempRepositoryConnection.commit();
            
            this.utils.checkFileReferencesInRDF(tempRepositoryConnection, intContext);
            Assert.fail("Should have thrown exception from checkFileReferencesInRDF()");
        }
        catch(final PoddException e)
        {
            Assert.assertNotNull(e.getDetails());
            Assert.assertTrue(e.getDetails() instanceof List);
            final List errors = (List)e.getDetails();
            Assert.assertEquals(1, errors.size());
        }
        finally
        {
            
            if(tempRepositoryConnection != null)
            {
                tempRepositoryConnection.rollback();
                tempRepositoryConnection.close();
            }
            if(tempRepository != null)
            {
                tempRepository.shutDown();
            }
        }
    }
    
}
