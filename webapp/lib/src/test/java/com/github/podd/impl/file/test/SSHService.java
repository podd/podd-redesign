/**
 * 
 */
package com.github.podd.impl.file.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.file.SSHFileReference;
import com.github.podd.api.test.TestConstants;
import com.github.podd.impl.file.SSHFileReferenceImpl;

/**
 * A simple SSH Service for testing. This is based on the unit tests in the sshj project.
 * 
 * @author kutila
 * @created 20/11/2012
 */
public class SSHService
{
    public static final String TEST_SSH_HOST = "localhost";
    public static final String TEST_SSH_SERVICE_PORT = "9856";
    public static final String TEST_SSH_FINGERPRINT = "ce:a7:c1:cf:17:3f:96:49:6a:53:1a:05:0b:ba:90:db";
    public static final String TEST_SSH_USERNAME = "salt";
    public static final String TEST_SSH_SECRET = "salt";
    
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    private SshServer server;
    private boolean serverRunning = false;
    
    private final String hostkey = "/test/hostkey.pem";
    
    /**
     * Start an SSH service on the specified port. If the port is below 1024, a random available
     * port is used instead of it and returned.
     * 
     * @param port
     *            The port on which the SSH service is to start. If this value is below 1024, a
     *            random available port is used.
     * @param tempDirectory
     *            The temporary directory in which a copy of the host key file is maintained by the
     *            server.
     * @return The port on which the SSH service was started.
     * @throws Exception
     */
    public int startTestSSHServer(int port, final Path tempDirectory) throws Exception
    {
        if(port < 1024)
        {
            port = SSHService.getFreePort();
        }
        this.server = SshServer.setUpDefaultServer();
        this.server.setPort(port);
        
        final InputStream input = this.getClass().getResourceAsStream(this.hostkey);
        
        if(input == null)
        {
            throw new IllegalArgumentException("Test SSH Server Host key was not found");
        }
        // Copy the host key file out to a temporary directory so that the server can lock it as
        // required and cannot modify the test resource
        final Path tempFile = Files.createTempFile(tempDirectory, "podd-test-ssh-hostkey-", ".pem");
        Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
        
        this.server.setCommandFactory(new ScpCommandFactory());
        this.server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
        this.server.setKeyPairProvider(new FileKeyPairProvider(new String[] { tempFile.toString() }));
        this.server.setPasswordAuthenticator(new PasswordAuthenticator()
            {
                @Override
                public boolean authenticate(final String username, final String password, final ServerSession s)
                {
                    return username.equals(password);
                }
            });
        
        this.server.setCommandFactory(new ScpCommandFactory(new CommandFactory()
            {
                @Override
                public org.apache.sshd.server.Command createCommand(final String command)
                {
                    return new ProcessShellFactory(command.split(" ")).create();
                }
            }));
        
        final List<NamedFactory<org.apache.sshd.server.Command>> namedFactoryList =
                new ArrayList<NamedFactory<org.apache.sshd.server.Command>>();
        namedFactoryList.add(new SftpSubsystem.Factory());
        this.server.setSubsystemFactories(namedFactoryList);
        
        this.log.info("about to start the SSHD server on port: " + port);
        this.server.start();
        this.serverRunning = true;
        this.log.info("started the SSHD server on port: " + port);
        return port;
    }
    
    /**
     * Stop the SSH service if it is running.
     * 
     * @throws Exception
     */
    public void stopTestSSHServer(final Path tempDirectory) throws Exception
    {
        this.log.info("Entering stopTestSSHServer()");
        if(this.server != null && this.serverRunning)
        {
            this.log.info("Stop SSHD server");
            this.server.stop();
            this.serverRunning = false;
            this.log.info("Stopped SSHD server");
        }
        
        this.deleteDirectory(tempDirectory);
        
        this.log.info("Exiting stopTestSSHServer()");
    }
    
    private void deleteDirectory(Path dir) throws IOException
    {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    log.trace("Deleting file: {}", file);
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
                {
                    log.trace("Deleting dir: {}", dir);
                    if(exc == null)
                    {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                    else
                    {
                        throw exc;
                    }
                }
                
            });
    }
    
    /**
     * Copied from sshj net.schmizz.sshj.util.BasicFixture.java
     * 
     * @return
     */
    private static int getFreePort()
    {
        try
        {
            ServerSocket s = null;
            try
            {
                s = new ServerSocket(0);
                s.setReuseAddress(true);
                return s.getLocalPort();
            }
            finally
            {
                if(s != null)
                {
                    s.close();
                }
            }
        }
        catch(final IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 
     * @param alias
     *            The alias to be assigned to the created FileReference.
     * @param fileIdentifier
     *            If this parameter is not null, its value will be set as the file Identifier
     * @return A new FileReference instance for use by tests
     */
    public static SSHFileReference getNewFileReference(String alias, String fileIdentifier)
    {
        // prepare: create the FileReference to be validated
        final SSHFileReference fileReference = new SSHFileReferenceImpl();
        fileReference.setRepositoryAlias(alias);
        
        // prepare: get the name and path of File to be validated
        /*
         * NOTE: The TEST_FILE should be accessible on the file system as a file. If it is accessed
         * as a resource made available from a different module, it will not be accessible to the
         * SSH service.
         */
        
        // NOTE: This is very very bad practice with Maven. The resource should be copied out to a
        // temporary directory first
        final String testFile = SSHService.class.getResource(TestConstants.TEST_FILE).getFile();
        String fileName = testFile;
        String path = SSHService.class.getResource(TestConstants.TEST_FILE).getPath();
        
        final int lastSlashPosition = testFile.lastIndexOf(File.separatorChar);
        if(lastSlashPosition != -1)
        {
            fileName = testFile.substring(lastSlashPosition + 1);
            path = testFile.substring(0, lastSlashPosition);
        }
        
        if(fileIdentifier != null)
        {
            fileReference.setFilename(fileIdentifier);
        }
        else
        {
            fileReference.setFilename(fileName);
        }
        fileReference.setPath(path);
        
        return fileReference;
    }
    
}
