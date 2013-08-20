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
 /**
 * 
 */
package com.github.podd.impl.file;

import java.io.IOException;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.SFTPClient;

import org.openrdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.file.DataReference;
import com.github.podd.api.file.PoddDataRepository;
import com.github.podd.api.file.SSHFileReference;
import com.github.podd.exception.FileReferenceNotSupportedException;
import com.github.podd.exception.FileRepositoryIncompleteException;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author kutila
 * 
 */
public class SSHFileRepositoryImpl extends PoddFileRepositoryImpl<SSHFileReference>
{
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public SSHFileRepositoryImpl(final Model model) throws FileRepositoryIncompleteException
    {
        super(model);
        
        // check that the model contains values for protocol, host, port, fingerprint, username, and
        // secret
        final String protocol =
                model.filter(super.aliasUri, PoddRdfConstants.PODD_DATA_REPOSITORY_PROTOCOL, null).objectString();
        final String host =
                model.filter(super.aliasUri, PoddRdfConstants.PODD_DATA_REPOSITORY_HOST, null).objectString();
        final String port =
                model.filter(super.aliasUri, PoddRdfConstants.PODD_DATA_REPOSITORY_PORT, null).objectString();
        final String fingerprint =
                model.filter(super.aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, null).objectString();
        final String username =
                model.filter(super.aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, null).objectString();
        final String secret =
                model.filter(super.aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, null).objectString();
        
        if(protocol == null || host == null || port == null || fingerprint == null || username == null
                || secret == null)
        {
            throw new FileRepositoryIncompleteException(model, "SSH repository configuration incomplete");
        }
        
        if(!PoddDataRepository.PROTOCOL_SSH.equalsIgnoreCase(protocol))
        {
            throw new FileRepositoryIncompleteException(model, "Protocol needs to be SSH");
        }
    }
    
    @Override
    public boolean canHandle(final DataReference reference)
    {
        if(reference == null)
        {
            return false;
        }
        
        // unnecessary as Generics ensure only an SSHFileReference can be passed in
        if(!(reference instanceof SSHFileReference))
        {
            return false;
        }
        
        final String aliasFromFileRef = reference.getRepositoryAlias();
        if(aliasFromFileRef == null || !this.alias.equalsIgnoreCase(aliasFromFileRef))
        {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean validate(final DataReference dataReference) throws FileReferenceNotSupportedException, IOException
    {
        if(!this.canHandle(dataReference))
        {
            throw new FileReferenceNotSupportedException(dataReference, "cannot handle file reference for validation");
        }
        
        final String host =
                this.model.filter(super.aliasUri, PoddRdfConstants.PODD_DATA_REPOSITORY_HOST, null).objectString();
        final String port =
                this.model.filter(super.aliasUri, PoddRdfConstants.PODD_DATA_REPOSITORY_PORT, null).objectString();
        final String fingerprint =
                this.model.filter(super.aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_FINGERPRINT, null)
                        .objectString();
        final String username =
                this.model.filter(super.aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_USERNAME, null).objectString();
        final String secret =
                this.model.filter(super.aliasUri, PoddRdfConstants.PODD_FILE_REPOSITORY_SECRET, null).objectString();
        
        int portNo = -1;
        try
        {
            portNo = Integer.parseInt(port);
        }
        catch(final NumberFormatException e)
        {
            throw new IOException("Port number could not be parsed correctly: " + port);
        }
        
        String fileName = ((SSHFileReference)dataReference).getFilename();
        final String path = ((SSHFileReference)dataReference).getPath();
        if(path != null && path.trim().length() > 0)
        {
            fileName = path + "/" + fileName;
        }
        
        this.log.info("Validating file reference: " + host + ":" + port + " " + fileName);
        
        try (SSHClient sshClient = new SSHClient();)
        {
            sshClient.addHostKeyVerifier(fingerprint);
            sshClient.connect(host, portNo);
            
            sshClient.authPassword(username, secret);
            
            try (SFTPClient sftp = sshClient.newSFTPClient();)
            {
                // check details of a remote file
                final FileAttributes attribs = sftp.lstat(fileName);
                if(attribs == null || attribs.getSize() <= 0)
                {
                    return false;
                }
            }
            catch(final IOException e)
            {
                // lstat() throws an IOException if the file does not exist
                return false;
            }
        }
        return true;
    }
    
}
