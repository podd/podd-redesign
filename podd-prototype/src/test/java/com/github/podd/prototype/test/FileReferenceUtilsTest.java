package com.github.podd.prototype.test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
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
import com.github.podd.prototype.PoddServletHelper;
import com.github.podd.prototype.SshFileReference;

public class FileReferenceUtilsTest
{
    @Rule
    public TemporaryFolder tempDirectory = new TemporaryFolder();
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    protected FileReferenceUtils utils;
    
    @Before
    public void setUp() throws Exception
    {
        final InputStream inputStream = this.getClass().getResourceAsStream("/test/alias.ttl");
        Assert.assertNotNull("Could not find alias file", inputStream);
        
        this.utils = new FileReferenceUtils();
        this.log.info("About to set aliases");
        this.utils.setAliases(inputStream, RDFFormat.TURTLE);
        this.log.info("Finished setting aliases");
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.utils = null;
    }
    
    @Test
    public void testCheckFileExists_simpleFailures() throws Exception
    {
        // test with an unsupported FileReference format
        final FileReference emptyRef = new FileReference()
            {
                @Override
                public boolean isFilled()
                {
                    return true;
                }
            };
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
    
    /**
     * This test starts up an internal SSH server and therefore is somewhat an integration test. If
     * the specified port is unavailable, the test will fail.
     * 
     * @throws Exception
     */
    @Test
    public void testCheckFileExists_sshFileRef() throws Exception
    {
        final SSHService sshd = new SSHService();
        try
        {
            sshd.startTestSSHServer(9856, this.tempDirectory.newFolder());
            
            final SshFileReference sshFileRef = new SshFileReference();
            sshFileRef.setArtifactUri(null);
            sshFileRef.setObjectUri(null);
            sshFileRef.setServerAlias("localssh");
            sshFileRef.setFilename("basicProject-1.rdf");
            sshFileRef.setPath("src/test/resources/test/artifacts");
            sshFileRef.setDescription("Refers to one of the test artifacts, to be accessed through an ssh server");
            
            this.utils.checkFileExists(sshFileRef);
        }
        finally
        {
            sshd.stopTestSSHServer();
        }
    }
    
    @Test
    public void testConstructFileReferenceFromMap() throws Exception
    {
        final Map<String, String[]> map = new HashMap<String, String[]>();
        Assert.assertNull(this.utils.constructFileReferenceFromMap(map));
        
        map.put(FileReferenceUtils.KEY_FILE_REF_TYPE, new String[] { "http" });
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
    public void testAddFileReferenceAsTriplesToRepository_HTTP() throws Exception
    {
        final HttpFileReference httpRef = new HttpFileReference();
        httpRef.setServerAlias("w3");
        httpRef.setArtifactUri("urn:artifact:01:ac");
        httpRef.setObjectUri("urn:someobject");
        httpRef.setFilename("rfc2616.html");
        httpRef.setPath("Protocols/rfc2616");
        httpRef.setDescription("HTTP RFC");
        
        this.internalTestAddFileReferenceAsTriplesToRepository(httpRef, "HTTP");
    }
    
    @Test
    public void testAddFileReferenceAsTriplesToRepository_SSH() throws Exception
    {
        final SshFileReference sshRef = new SshFileReference();
        sshRef.setServerAlias("w3");
        sshRef.setArtifactUri("urn:artifact:01:ac");
        sshRef.setObjectUri("urn:someobject");
        sshRef.setFilename("rfc2616.html");
        sshRef.setPath("Protocols/rfc2616");
        sshRef.setDescription("HTTP RFC");
        
        this.internalTestAddFileReferenceAsTriplesToRepository(sshRef, "SSH");
    }
    
    protected void internalTestAddFileReferenceAsTriplesToRepository(final FileReference fileRef,
            final String fileReferenceType) throws Exception
    {
        // create a temporary in-memory repository
        final Repository tempRepository = new SailRepository(new MemoryStore());
        tempRepository.initialize();
        final RepositoryConnection tempRepositoryConnection = tempRepository.getConnection();
        tempRepositoryConnection.setAutoCommit(false);
        
        try
        {
            final URI intContext = IRI.create("urn:intermediate:").toOpenRDFURI();
            
            FileReferenceUtils.addFileReferenceAsTriplesToRepository(tempRepositoryConnection, fileRef, intContext);
            
            // verify the added statements
            Assert.assertEquals(7, tempRepositoryConnection.size(intContext));
            
            final URI hasFileReferenceType =
                    IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasFileReferenceType").toOpenRDFURI();
            final RepositoryResult<Statement> results =
                    tempRepositoryConnection.getStatements(null, hasFileReferenceType, null, false, intContext);
            Assert.assertNotNull(results);
            final List<Statement> list = results.asList();
            Assert.assertEquals(1, list.size());
            Assert.assertEquals(fileReferenceType, list.get(0).getObject().stringValue());
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
    public void testCheckFileReferencesInRDF_None() throws Exception
    {
        this.internalTestCheckFileReferencesInRDF("/test/artifacts/fragment.rdf", false);
    }
    
    @Test
    public void testCheckFileReferencesInRDF_Correct() throws Exception
    {
        this.internalTestCheckFileReferencesInRDF("/test/artifacts/fragment-1-file-reference.rdf", false);
    }
    
    @Test
    public void testCheckFileReferencesInRDF_2With1Invalid() throws Exception
    {
        final List<String> errors =
                this.internalTestCheckFileReferencesInRDF("/test/artifacts/fragment-invalid-file-reference.rdf", true);
        Assert.assertEquals(1, errors.size());
    }
    
    @Test
    public void testCheckFileReferencesInRDF_MissingFileReferenceType() throws Exception
    {
        final List<String> errors =
                this.internalTestCheckFileReferencesInRDF("/test/artifacts/fragment-missing-file-reference-type.rdf",
                        true);
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.get(0).contains("Missing File Reference Type"));
    }
    
    @Test
    public void testCheckFileReferencesInRDF_UnknownFileReferenceType() throws Exception
    {
        final List<String> errors =
                this.internalTestCheckFileReferencesInRDF("/test/artifacts/fragment-unknown-file-reference-type.rdf",
                        true);
        Assert.assertEquals(1, errors.size());
        Assert.assertTrue(errors.get(0).contains("Unknown File Reference Type"));
    }
    
    protected List<String> internalTestCheckFileReferencesInRDF(final String rdfPath, final boolean expectErrors)
        throws Exception
    {
        final InputStream in = this.getClass().getResourceAsStream(rdfPath);
        Assert.assertNotNull("Resource was not found", in);
        
        // create a temporary in-memory repository
        final Repository tempRepository = new SailRepository(new MemoryStore());
        tempRepository.initialize();
        final RepositoryConnection tempRepositoryConnection = tempRepository.getConnection();
        tempRepositoryConnection.setAutoCommit(false);
        
        try
        {
            // populate repository with incoming RDF statements
            final URI intContext = IRI.create("urn:intermediate:").toOpenRDFURI();
            tempRepositoryConnection.add(in, "", RDFFormat.RDFXML, intContext);
            tempRepositoryConnection.commit();
            
            this.utils.checkFileReferencesInRDF(tempRepositoryConnection, intContext);
            if(expectErrors)
            {
                Assert.fail("Should have thrown exception from checkFileReferencesInRDF()");
            }
        }
        catch(final PoddException e)
        {
            if(!expectErrors)
            {
                Assert.fail("Unexpected Exception thrown" + e.getMessage());
            }
            Assert.assertNotNull(e.getDetails());
            Assert.assertTrue(e.getDetails() instanceof List<?>);
            return (List<String>)e.getDetails();
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
        return null; // to make the Compiler happy!
    }
    
}
