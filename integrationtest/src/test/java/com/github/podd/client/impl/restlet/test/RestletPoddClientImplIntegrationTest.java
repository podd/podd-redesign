/**
 * 
 */
package com.github.podd.client.impl.restlet.test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.github.podd.client.api.test.AbstractPoddClientTest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.resource.ClientResource;

import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.FileReferenceConstants;
import com.github.podd.client.api.PoddClient;
import com.github.podd.client.impl.restlet.RestletPoddClientImpl;
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
    private Path tempFile;
    
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
    protected FileReference deployFileReference(String label)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected void startFileRepositoryTest() throws Exception
    {
        tempFolder = this.tempDirectory.newFolder().toPath();
        
        sshd = new SSHService();
        
        // Put a file into the server to ensure that it is not empty.
        final InputStream testUploadedFile =
                this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
        
        tempFile = Files.createTempFile(tempFolder, "basicProject-1", ".rdf");
        Files.copy(testUploadedFile, tempFile, StandardCopyOption.REPLACE_EXISTING);
        
        sshd.startTestSSHServer(tempFolder);
    }
    
    @Override
    protected void endFileRepositoryTest() throws Exception
    {
        sshd.stopTestSSHServer(tempFolder);
    }
    
}
