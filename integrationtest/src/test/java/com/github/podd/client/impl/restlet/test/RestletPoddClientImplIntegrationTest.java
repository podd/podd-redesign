/**
 * 
 */
package com.github.podd.client.impl.restlet.test;

import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.restlet.resource.ClientResource;

import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.SSHFileReference;
import com.github.podd.client.api.PoddClient;
import com.github.podd.client.api.test.AbstractPoddClientTest;
import com.github.podd.client.impl.restlet.RestletPoddClientImpl;
import com.github.podd.impl.file.SSHFileReferenceImpl;
import com.github.podd.impl.file.test.SSHService;

/**
 * Integration test for the Restlet PODD Client API implementation.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class RestletPoddClientImplIntegrationTest extends AbstractPoddClientTest
{
    @Rule
    public TemporaryFolder tempDirectory = new TemporaryFolder();
    
    private SSHService sshd;
    
    private Path tempFolder;
    private Map<String, Path> tempFiles = new ConcurrentHashMap<String, Path>();
    
    @Override
    protected FileReference deployFileReference(final String label) throws Exception
    {
        Path nextTempFile;
        
        if(this.tempFiles.containsKey(label))
        {
            nextTempFile = this.tempFiles.get(label);
        }
        else
        {
            nextTempFile = this.tempFolder.resolve("file-" + label.hashCode() + ".data");
        }
        
        if(!Files.exists(nextTempFile))
        {
            // Put a file into the server for this file reference to ensure that it validates to a
            // file
            try (final InputStream testUploadedFile =
                    this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");)
            {
                Files.createFile(nextTempFile);
                
                this.tempFiles.put(label, nextTempFile);
                Files.copy(testUploadedFile, nextTempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            catch(final FileAlreadyExistsException e)
            {
                // Ignore, the file may have been created by another thread since we entered this
                // block.
            }
        }
        
        final SSHFileReference nextFileReference = new SSHFileReferenceImpl();
        
        nextFileReference.setPath(this.tempFolder.toAbsolutePath().toString());
        nextFileReference.setFilename(nextTempFile.getFileName().toString());
        nextFileReference.setRepositoryAlias("localssh");
        nextFileReference.setLabel(label);
        
        return nextFileReference;
    }
    
    @Override
    protected void endFileRepositoryTest() throws Exception
    {
        this.sshd.stopTestSSHServer(this.tempFolder);
    }
    
    @Override
    protected PoddClient getNewPoddClientInstance()
    {
        return new RestletPoddClientImpl();
    }
    
    @Override
    protected String getTestPoddServerUrl()
    {
        return "http://localhost:9090/podd-test";
    }
    
    @Override
    protected void resetTestPoddServer()
    {
        // Reset server after each test so that assertions are not dependent on the order of the
        // tests, which is unpredictable
        // HACK: This presumes that this reset service will exist and that it has this URL
        final ClientResource resource = new ClientResource(this.getTestPoddServerUrl() + "/reset/r3set");
        resource.get();
    }
    
    @Override
    protected void startFileRepositoryTest() throws Exception
    {
        this.tempFolder = this.tempDirectory.newFolder().toPath();
        
        this.sshd = new SSHService();
        // This is setup to match the "localssh" repository alias defined in
        // src/main/resources/test-alias.ttl
        this.sshd.TEST_SSH_SERVICE_PORT = 9856;
        
        this.sshd.startTestSSHServer(this.tempFolder);
    }
    
}
