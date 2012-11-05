/**
 * 
 */
package com.github.podd.prototype;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class checks whether a given file reference is accurate.
 * 
 * NOTE: This class is currently a singleton. When adding support for multiple file reference
 * formats, this can be modified to have multiple "FileReferenceTypeXValidator" implementations.
 * 
 * @author kutila
 * @created 2012/11/05
 */
public class FileReferenceValidator
{
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    private static FileReferenceValidator instance = new FileReferenceValidator();
    
    private Properties aliases = new Properties();
    private long aliasesLoadedAt;
    private String aliasFilePath;
    
    private FileReferenceValidator()
    {
    }
    
    public static FileReferenceValidator getInstance()
    {
        return FileReferenceValidator.instance;
    }
    
    public void initialize(final String aliasFilePath)
    {
        this.aliasFilePath = aliasFilePath;
    }
    
    /**
     * This method quietly returns if the referenced file could be validated. An IOException is
     * thrown if validation failed.
     * 
     * @param serverAlias
     * @param path
     * @param filename
     * @throws IOException
     */
    public void validate(final String serverAlias, final String path, final String filename) throws IOException
    {
        this.loadAliases();
        
        final String host = this.aliases.getProperty(serverAlias + ".host");
        final String protocol = this.aliases.getProperty(serverAlias + ".protocol");
        // String username = aliases.getProperty(serverAlias + ".username");
        // String password = aliases.getProperty(serverAlias + ".password");
        
        if(host == null || protocol == null)
        {
            throw new NullPointerException("No entry for the alias: " + serverAlias);
        }
        
        final String urlString = protocol + "://" + host + "/" + path + "/" + filename;
        
        this.log.info("Validating file reference: " + urlString);
        
        try
        {
            final URL url = new URL(urlString);
            final Object theFile = url.getContent();
            if(theFile == null)
            {
                throw new FileNotFoundException("Referenced file not found. " + urlString);
            }
        }
        catch(final IOException e)
        {
            throw e;
        }
    }
    
    private void loadAliases() throws IOException
    {
        if((System.currentTimeMillis() - this.aliasesLoadedAt) < 60000)
        {
            return;
        }
        
        try
        {
            this.aliases.load(new FileInputStream(this.aliasFilePath));
        }
        catch(final IOException e)
        {
            this.log.error("Failed to load aliases", e);
            throw new IOException("Failed to load aliases", e);
        }
        this.aliasesLoadedAt = System.currentTimeMillis();
    }
    
}
