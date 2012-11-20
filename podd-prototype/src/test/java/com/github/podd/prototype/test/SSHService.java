/**
 * 
 */
package com.github.podd.prototype.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.SFTPClient;

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

/**
 * A simple SSH Service for testing. This is based on the unit tests in the sshj project.
 * 
 * @author kutila
 * @created 20/11/2012
 */
public class SSHService
{
    
    private SshServer server;
    private boolean serverRunning = false;
    
    private final String hostkey = "src/test/resources/test/hostkey.pem";
    
    /**
     * Start an SSH service on the specified port. If the port is below 1024, a random available
     * port is used instead of it and returned.
     * 
     * @param port
     * @return
     * @throws Exception
     */
    public int startTestSSHServer(int port) throws Exception
    {
        if(port < 1024)
        {
            port = SSHService.getFreePort();
        }
        this.server = SshServer.setUpDefaultServer();
        this.server.setPort(port);
        
        this.server.setCommandFactory(new ScpCommandFactory());
        this.server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
        this.server.setKeyPairProvider(new FileKeyPairProvider(new String[] { this.hostkey }));
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
        
        this.server.start();
        this.serverRunning = true;
        System.out.println("started the SSHD server on port: " + port);
        return port;
    }
    
    /**
     * Stop the SSH service if it is running.
     * 
     * @throws Exception
     */
    public void stopTestSSHServer() throws Exception
    {
        if(this.server != null && this.serverRunning)
        {
            this.server.stop();
            this.serverRunning = false;
        }
        System.out.println("Stopped the SSHD server");
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
    
    public static void main(final String[] args) throws Exception
    {
        final SSHService sshd = new SSHService();
        sshd.startTestSSHServer(9856);
    }
    
    /**
     * Simply testing SSHJ based on their examples.SFTPDownload.java
     * 
     * @throws Exception
     */
    public void testSshjSPike() throws Exception
    {
        final SSHService sshd = new SSHService();
        sshd.startTestSSHServer(9856);
        final int port = sshd.startTestSSHServer(-1);
        
        final String username = "kutila";
        final String secret = "kutila";
        final String fingerprint = "ce:a7:c1:cf:17:3f:96:49:6a:53:1a:05:0b:ba:90:db";
        
        final SSHClient sshClient = new SSHClient();
        sshClient.addHostKeyVerifier(fingerprint);
        sshClient.connect("localhost", port);
        
        Assert.assertTrue(sshClient.isConnected());
        try
        {
            sshClient.authPassword(username, secret);
            Assert.assertTrue("Not authenticated", sshClient.isAuthenticated());
            
            final SFTPClient sftp = sshClient.newSFTPClient();
            Assert.assertNotNull("SFTPClient was null", sftp);
            
            // check details of a remote file
            final FileAttributes attribs = sftp.lstat("pom.xml");
            System.out.println("called lstat");
            Assert.assertNotNull(attribs);
            System.out.println("Result size = " + attribs.getSize());
            System.out.println("Result type = " + attribs.getType());
            System.out.println("Result String = " + attribs.toString());
        }
        finally
        {
            // close the SSH client without closing the SFTPClient. If both are closed,
            // the SSH server logs a "Connection reset by peer" error.
            sshClient.close();
            sshd.stopTestSSHServer();
        }
    }
    
}
