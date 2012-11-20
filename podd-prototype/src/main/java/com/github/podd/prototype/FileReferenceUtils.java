/**
 * 
 */
package com.github.podd.prototype;

import info.aduna.iteration.Iterations;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.SFTPClient;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains utility methods to construct FileReference objects, validate them etc.
 * 
 * @author kutila
 * @created 2012/11/05
 */
public class FileReferenceUtils
{
    public static final String KEY_FILE_REF_TYPE = "file_reference_type";
    public static final String KEY_OBJECT_URI = "object_uri";
    public static final String KEY_ARTIFACT_URI = "artifact_uri";
    public static final String KEY_FILE_DESCRIPTION = "file_description";
    public static final String KEY_FILE_NAME = "file_name";
    public static final String KEY_FILE_PATH = "file_path";
    public static final String KEY_FILE_SERVER_ALIAS = "file_server_alias";
    
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    private static FileReferenceUtils instance = new FileReferenceUtils();
    
    private Properties aliases = new Properties();
    private long aliasesLoadedAt;
    private String aliasFilePath;
    
    private FileReferenceUtils()
    {
    }
    
    public static FileReferenceUtils getInstance()
    {
        return FileReferenceUtils.instance;
    }
    
    public void initialize(final String aliasFilePath)
    {
        this.aliasFilePath = aliasFilePath;
    }
    
    public void clean()
    {
        this.aliasFilePath = null;
        this.aliases = new Properties();
        this.aliasesLoadedAt = -1;
    }
    
    /**
     * Constructs a FileReference object from the data available in the provided Map object.
     * 
     * Note: Currently works only for SSH and HTTP File References
     * 
     * @param requestMap
     * @return a populated FileReference object or NULL if an object could not be constructed with
     *         the provided information.
     */
    public FileReference constructFileReferenceFromMap(final Map<String, String[]> requestMap)
    {
        FileReference fileRef = null;
        
        try
        {
            final String fileReferenceType = requestMap.get(FileReferenceUtils.KEY_FILE_REF_TYPE)[0];
            if("SSH".equalsIgnoreCase(fileReferenceType))
            {
                final SshFileReference sshFileRef = new SshFileReference();
                sshFileRef.setPath(requestMap.get(FileReferenceUtils.KEY_FILE_PATH)[0]);
                sshFileRef.setFilename(requestMap.get(FileReferenceUtils.KEY_FILE_NAME)[0]);
                final String[] descriptions = requestMap.get(FileReferenceUtils.KEY_FILE_DESCRIPTION);
                if(descriptions != null && descriptions.length > 0)
                {
                    sshFileRef.setDescription(descriptions[0]);
                }
                fileRef = sshFileRef;
            }
            else if("HTTP".equalsIgnoreCase(fileReferenceType))
            {
                final HttpFileReference httpFileRef = new HttpFileReference();
                httpFileRef.setPath(requestMap.get(FileReferenceUtils.KEY_FILE_PATH)[0]);
                httpFileRef.setFilename(requestMap.get(FileReferenceUtils.KEY_FILE_NAME)[0]);
                final String[] descriptions = requestMap.get(FileReferenceUtils.KEY_FILE_DESCRIPTION);
                if(descriptions != null && descriptions.length > 0)
                {
                    httpFileRef.setDescription(descriptions[0]);
                }
                fileRef = httpFileRef;
            }
            else
            {
                this.log.error("Unsupported file reference type encountered. " + fileReferenceType);
                return null;
            }
            
            String artifactUri = requestMap.get(FileReferenceUtils.KEY_ARTIFACT_URI)[0];
            String objectUri = requestMap.get(FileReferenceUtils.KEY_OBJECT_URI)[0];
            try
            {
                artifactUri = PoddServletHelper.extractUri(artifactUri);
                objectUri = PoddServletHelper.extractUri(objectUri);
            }
            catch(URISyntaxException | UnsupportedEncodingException e)
            {
                this.log.error("Could not decode URI: " + e.toString());
                return null;
            }
            fileRef.setArtifactUri(artifactUri);
            fileRef.setObjectUri(objectUri);
            fileRef.setServerAlias(requestMap.get(FileReferenceUtils.KEY_FILE_SERVER_ALIAS)[0]);
            
        }
        catch(ArrayIndexOutOfBoundsException | NullPointerException e)
        {
            this.log.error("Expected parameter missing.");
            return null;
        }
        if(!fileRef.isFilled())
        {
            this.log.error("File Reference does not have all required fields filled." + fileRef.toString());
            return null;
        }
        
        return fileRef;
    }
    
    /**
     * This method attempts to validate that the referenced "File" is reachable.
     * 
     * @param fileReference
     * @throws IOException
     * @throws PoddException
     */
    public void checkFileExists(final FileReference fileReference) throws IOException, PoddException
    {
        if(fileReference instanceof SshFileReference)
        {
            this.checkFileExists((SshFileReference)fileReference);
        }
        else if(fileReference instanceof HttpFileReference)
        {
            this.checkFileExists((HttpFileReference)fileReference);
        }
        else
        {
            throw new PoddException("Unsupported File Reference format", null, -1);
        }
    }
    
    private void checkFileExists(final HttpFileReference httpFileRef) throws IOException, PoddException
    {
        this.loadAliases();
        
        final String host = this.aliases.getProperty(httpFileRef.getServerAlias() + ".host");
        final String protocol = this.aliases.getProperty(httpFileRef.getServerAlias() + ".protocol");
        // String username = aliases.getProperty(serverAlias + ".username");
        // String password = aliases.getProperty(serverAlias + ".password");
        
        if(host == null || protocol == null)
        {
            throw new PoddException("No entry for the alias: " + httpFileRef.getServerAlias(), null, -1);
        }
        
        final String urlString =
                protocol + "://" + host + "/" + httpFileRef.getPath() + "/" + httpFileRef.getFilename();
        
        this.log.info("Validating file reference: " + urlString);
        
        try
        {
            final URL url = new URL(urlString);
            final Object theResource = url.getContent();
            if(theResource == null)
            {
                throw new FileNotFoundException("Referenced file not found. " + urlString);
            }
        }
        catch(final IOException e)
        {
            throw e;
        }
    }
    
    private void checkFileExists(final SshFileReference sshFileRef) throws IOException, PoddException
    {
        this.loadAliases();
        final String host = this.aliases.getProperty(sshFileRef.getServerAlias() + ".host");
        final String port = this.aliases.getProperty(sshFileRef.getServerAlias() + ".port");
        final String fingerprint = this.aliases.getProperty(sshFileRef.getServerAlias() + ".fingerprint");
        final String username = this.aliases.getProperty(sshFileRef.getServerAlias() + ".username");
        final String secret = this.aliases.getProperty(sshFileRef.getServerAlias() + ".secret");
        
        if(host == null || port == null)
        {
            throw new PoddException("No entry for the alias: " + sshFileRef.getServerAlias(), null, -1);
        }
        
        int portNo = -1;
        try
        {
            portNo = Integer.parseInt(port);
        }
        catch(final NumberFormatException e)
        {
            throw new IOException("Port number could not be parsed correctly: " + port);
        }
        
        String fileName = sshFileRef.getFilename();
        if(sshFileRef.getPath() != null && sshFileRef.getPath().trim().length() > 0)
        {
            fileName = sshFileRef.getPath() + "/" + sshFileRef.getFilename();
        }
        
        this.log.info("Validating file reference: " + host + ":" + port + " " + fileName);
        
        final SSHClient sshClient = new SSHClient();
        sshClient.addHostKeyVerifier(fingerprint);
        sshClient.connect(host, portNo);
        
        try
        {
            sshClient.authPassword(username, secret);
            final SFTPClient sftp = sshClient.newSFTPClient();
            
            // check details of a remote file
            final FileAttributes attribs = sftp.lstat(fileName);
            if(attribs == null || attribs.getSize() <= 0)
            {
                throw new FileNotFoundException("Referenced file not found. " + fileName);
            }
        }
        finally
        {
            // close the SSH client without closing the SFTPClient
            sshClient.close();
        }
        
    }
    
    /**
     * Adds the content of the given File Reference object as RDF statements to the given
     * RepositoryConnection. Transaction handling should be taken care of by the caller.
     * 
     * @param repositoryConnection
     * @param fileReference
     * @param context
     *            The Context under which the Statements are added.
     * @return
     * @throws RepositoryException
     */
    public static URI addFileReferenceAsTriplesToRepository(final RepositoryConnection repositoryConnection,
            final FileReference fileReference, final URI context) throws RepositoryException
    {
        final URI objectToAttachTo = IRI.create(fileReference.getObjectUri()).toOpenRDFURI();
        
        // generate unique URI for file reference object
        final URI fileRefObject =
                IRI.create("http://example.org/permanenturl/fileref:" + UUID.randomUUID().toString()).toOpenRDFURI();
        
        final URI fileRefURI = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "FileReference").toOpenRDFURI();
        final URI propertyHasFileReference =
                IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasFileReference").toOpenRDFURI();
        repositoryConnection.add(fileRefObject, RDF.TYPE, fileRefURI, context);
        
        if(fileReference instanceof HttpFileReference)
        {
            FileReferenceUtils.addHttpSpecificStatements(repositoryConnection, (HttpFileReference)fileReference,
                    fileRefObject, context);
        }
        else if(fileReference instanceof SshFileReference)
        {
            FileReferenceUtils.addSshSpecificStatements(repositoryConnection, (SshFileReference)fileReference,
                    fileRefObject, context);
        }
        else
        {
            return null;
        }
        repositoryConnection.add(objectToAttachTo, propertyHasFileReference, fileRefObject, context);
        return fileRefObject;
    }
    
    private static void addSshSpecificStatements(final RepositoryConnection repositoryConnection,
            final SshFileReference sshFileRef, final URI fileRefObject, final URI context) throws RepositoryException
    {
        final URI propertyHasFileReferenceType =
                IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasFileReferenceType").toOpenRDFURI();
        final URI propertyHasFileName = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasFileName").toOpenRDFURI();
        final URI propertyHasAlias = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasAlias").toOpenRDFURI();
        final URI propertyHasPath = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasPath").toOpenRDFURI();
        final URI propertyHasDescription =
                IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasDescription").toOpenRDFURI();
        
        final ValueFactory f = repositoryConnection.getValueFactory();
        final Literal fileReferenceTypeLiteral = f.createLiteral("SSH");
        final Literal fileNameLiteral = f.createLiteral(sshFileRef.getFilename());
        final Literal filePathLiteral = f.createLiteral(sshFileRef.getPath());
        final Literal fileAliasLiteral = f.createLiteral(sshFileRef.getServerAlias());
        final Literal fileDescLiteral = f.createLiteral(sshFileRef.getDescription());
        
        repositoryConnection.add(fileRefObject, propertyHasFileReferenceType, fileReferenceTypeLiteral, context);
        repositoryConnection.add(fileRefObject, propertyHasFileName, fileNameLiteral, context);
        repositoryConnection.add(fileRefObject, propertyHasAlias, fileAliasLiteral, context);
        repositoryConnection.add(fileRefObject, propertyHasPath, filePathLiteral, context);
        repositoryConnection.add(fileRefObject, propertyHasDescription, fileDescLiteral, context);
    }
    
    private static void addHttpSpecificStatements(final RepositoryConnection repositoryConnection,
            final HttpFileReference httpFileRef, final URI fileRefObject, final URI context) throws RepositoryException
    {
        final URI propertyHasFileReferenceType =
                IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasFileReferenceType").toOpenRDFURI();
        final URI propertyHasFileName = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasFileName").toOpenRDFURI();
        final URI propertyHasAlias = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasAlias").toOpenRDFURI();
        final URI propertyHasPath = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasPath").toOpenRDFURI();
        final URI propertyHasDescription =
                IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasDescription").toOpenRDFURI();
        
        final ValueFactory f = repositoryConnection.getValueFactory();
        final Literal fileReferenceTypeLiteral = f.createLiteral("HTTP");
        final Literal fileNameLiteral = f.createLiteral(httpFileRef.getFilename());
        final Literal filePathLiteral = f.createLiteral(httpFileRef.getPath());
        final Literal fileAliasLiteral = f.createLiteral(httpFileRef.getServerAlias());
        final Literal fileDescLiteral = f.createLiteral(httpFileRef.getDescription());
        
        repositoryConnection.add(fileRefObject, propertyHasFileReferenceType, fileReferenceTypeLiteral, context);
        repositoryConnection.add(fileRefObject, propertyHasFileName, fileNameLiteral, context);
        repositoryConnection.add(fileRefObject, propertyHasAlias, fileAliasLiteral, context);
        repositoryConnection.add(fileRefObject, propertyHasPath, filePathLiteral, context);
        repositoryConnection.add(fileRefObject, propertyHasDescription, fileDescLiteral, context);
    }
    
    /**
     * Check RDF statements in the provided RepositoryConnection for File References and validate if
     * any exist. While this method does not make any changes to the content of the Repository,
     * committing or rolling back the Repository is the responsibility of the caller.
     * 
     * @param repositoryConnection
     * @param context
     * @throws RepositoryException
     * @throws IOException
     * @throws PoddException
     *             If invalid File References were found
     */
    public void checkFileReferencesInRDF(final RepositoryConnection repositoryConnection, final URI context)
        throws RepositoryException, IOException, PoddException
    {
        final List<String> errors = new ArrayList<String>();
        // how about making these constants?
        final URI fileRefURI = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "FileReference").toOpenRDFURI();
        final URI propertyHasFileName = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasFileName").toOpenRDFURI();
        final URI propertyHasAlias = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasAlias").toOpenRDFURI();
        final URI propertyHasPath = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasPath").toOpenRDFURI();
        final URI propertyHasFileReferenceType =
                IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasFileReferenceType").toOpenRDFURI();
        
        // search for statements identifying any resource as a File Reference
        final RepositoryResult<Statement> statements =
                repositoryConnection.getStatements(null, RDF.TYPE, fileRefURI, false, context);
        final List<Statement> statementList = Iterations.addAll(statements, new ArrayList<Statement>());
        for(final Statement statement : statementList)
        {
            final String fileRefObj = statement.getSubject().stringValue();
            final URI fileRefObjUri = IRI.create(fileRefObj).toOpenRDFURI();
            
            // identify the File Reference Type (i.e. HTTP or SSH)
            final RepositoryResult<Statement> st1 =
                    repositoryConnection.getStatements(fileRefObjUri, propertyHasFileReferenceType, null, false,
                            context);
            String fileRefType = null;
            if(st1.hasNext())
            {
                fileRefType = st1.next().getObject().stringValue();
            }
            else
            {
                errors.add("Missing File Reference Type");
                continue;
            }
            
            FileReference fileRef = null;
            if("HTTP".equalsIgnoreCase(fileRefType))
            {
                final HttpFileReference httpFileRef = new HttpFileReference();
                
                final RepositoryResult<Statement> st =
                        repositoryConnection.getStatements(fileRefObjUri, null, null, false, context);
                while(st.hasNext())
                {
                    final Statement nextStatement = st.next();
                    if(propertyHasFileName.equals(nextStatement.getPredicate()))
                    {
                        final String filename = nextStatement.getObject().stringValue();
                        httpFileRef.setFilename(filename);
                    }
                    else if(propertyHasAlias.equals(nextStatement.getPredicate()))
                    {
                        final String alias = nextStatement.getObject().stringValue();
                        httpFileRef.setServerAlias(alias);
                    }
                    else if(propertyHasPath.equals(nextStatement.getPredicate()))
                    {
                        final String path = nextStatement.getObject().stringValue();
                        httpFileRef.setPath(path);
                    }
                    
                }
                st.close();
                fileRef = httpFileRef;
            }
            else if("SSH".equalsIgnoreCase(fileRefType))
            {
                final SshFileReference sshFileRef = new SshFileReference();
                
                final RepositoryResult<Statement> st =
                        repositoryConnection.getStatements(fileRefObjUri, null, null, false, context);
                while(st.hasNext())
                {
                    final Statement nextStatement = st.next();
                    if(propertyHasFileName.equals(nextStatement.getPredicate()))
                    {
                        final String filename = nextStatement.getObject().stringValue();
                        sshFileRef.setFilename(filename);
                    }
                    else if(propertyHasAlias.equals(nextStatement.getPredicate()))
                    {
                        final String alias = nextStatement.getObject().stringValue();
                        sshFileRef.setServerAlias(alias);
                    }
                    else if(propertyHasPath.equals(nextStatement.getPredicate()))
                    {
                        final String path = nextStatement.getObject().stringValue();
                        sshFileRef.setPath(path);
                    }
                    
                }
                st.close();
                fileRef = sshFileRef;
            }
            else
            {
                errors.add("Unknown File Reference Type: " + fileRefType);
                continue;
            }
            
            try
            {
                this.checkFileExists(fileRef);
            }
            catch(IOException | PoddException e)
            {
                errors.add(e.getMessage());
            }
        }
        if(errors.size() > 0)
        {
            // FIXME: Create a subclass of PoddException for these errors
            throw new PoddException("Invalid File Reference(s) found.", errors, -1);
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
        catch(final IOException | NullPointerException e)
        {
            final String message = "Failed to load aliases from " + this.aliasFilePath;
            this.log.error(message, e);
            throw new IOException(message, e);
        }
        this.aliasesLoadedAt = System.currentTimeMillis();
    }
    
}
