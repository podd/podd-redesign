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
package com.github.podd.client.impl.restlet.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.restlet.Client;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.github.podd.api.data.DataReference;
import com.github.podd.api.data.SSHFileReference;
import com.github.podd.client.api.PoddClient;
import com.github.podd.client.api.test.AbstractPoddClientTest;
import com.github.podd.client.impl.restlet.RestletPoddClientImpl;
import com.github.podd.impl.data.SSHFileReferenceImpl;
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
    protected DataReference deployFileReference(final String label) throws Exception
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
    protected void resetTestServers() throws IOException
    {
        // Reset server after each test so that assertions are not dependent on the order of the
        // tests, which is unpredictable
        // HACK: This presumes that this reset service will exist and that it has this URL
        final ClientResource clientResource = new ClientResource(this.getTestPoddServerUrl() + "/reset/r3set");
        try
        {
            final Representation representation = clientResource.get();
            try
            {
                representation.exhaust();
            }
            finally
            {
                representation.release();
            }
        }
        catch(final Throwable e)
        {
            this.log.error("FAILURE: Could not reset PODD server after test complete", e);
        }
        finally
        {
            if(clientResource.getNext() != null && clientResource.getNext() instanceof Client)
            {
                final Client c = (Client)clientResource.getNext();
                try
                {
                    c.stop();
                }
                catch(final Throwable e)
                {
                    this.log.error("FAILURE: Could not reset PODD server after test complete", e);
                }
            }
            
        }
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
