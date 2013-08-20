/*
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
package com.github.podd.prototype.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

/**
 * A simple SSH Service for testing. This is based on the unit tests in the sshj project.
 * 
 * @author kutila
 * @created 20/11/2012
 */
public class SSHService
{
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    private SshServer server;
    private boolean serverRunning = false;
    
    private final String hostkey = "/test/hostkey.pem";
    
    /**
     * Start an SSH service on the specified port. If the port is below 1024, a random available
     * port is used instead of it and returned.
     * 
     * @param port
     * @return
     * @throws Exception
     */
    public int startTestSSHServer(int port, final File tempDirectory) throws Exception
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
        final Path tempFile = Files.createTempFile(tempDirectory.toPath(), "podd-test-ssh-hostkey-", ".pem");
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
    public void stopTestSSHServer() throws Exception
    {
        this.log.info("Entering stopTestSSHServer()");
        if(this.server != null && this.serverRunning)
        {
            this.log.info("Stop SSHD server");
            this.server.stop();
            this.serverRunning = false;
        }
        this.log.info("Exiting stopTestSSHServer()");
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
    
}
