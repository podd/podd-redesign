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
     * Note: Currently works only for HTTP File References
     * 
     * @param requestMap
     * @return a populated FileReference object or NULL if an object could not be constructed with
     *         the provided information.
     */
    public FileReference constructFileReferenceFromMap(final Map<String, String[]> requestMap)
    {
        final HttpFileReference fileRef = new HttpFileReference();
        
        try
        {
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
            
            // these will vary depending on file reference type (e.g. SSH, HTTP)
            fileRef.setPath(requestMap.get(FileReferenceUtils.KEY_FILE_PATH)[0]);
            fileRef.setFilename(requestMap.get(FileReferenceUtils.KEY_FILE_NAME)[0]);
            final String[] descriptions = requestMap.get(FileReferenceUtils.KEY_FILE_DESCRIPTION);
            if(descriptions != null && descriptions.length > 0)
            {
                fileRef.setDescription(descriptions[0]);
            }
        }
        catch(ArrayIndexOutOfBoundsException | NullPointerException e)
        {
            this.log.error("Expected parameter missing.");
            return null;
        }
        if(fileRef.getArtifactUri() == null || fileRef.getObjectUri() == null || fileRef.getServerAlias() == null
                || fileRef.getFilename() == null || fileRef.getPath() == null)
        {
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
        if(!(fileReference instanceof HttpFileReference))
        {
            throw new PoddException("Unsupported File Reference format", null, -1);
        }
        final HttpFileReference httpFileRef = (HttpFileReference)fileReference;
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
    
    public static URI addFileReferenceAsTriplesToRepository(final RepositoryConnection tempRepositoryConnection,
            final FileReference fileReference, final URI context) throws RepositoryException
    {
        // handles HTTP file reference
        final HttpFileReference httpRef = (HttpFileReference)fileReference;
        
        final URI objectToAttachTo = IRI.create(fileReference.getObjectUri()).toOpenRDFURI();
        
        // generate unique URI for file reference object
        final URI fileRefObject =
                IRI.create("http://example.org/permanenturl/fileref:" + UUID.randomUUID().toString()).toOpenRDFURI();
        
        final URI fileRefURI = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "FileReference").toOpenRDFURI();
        final URI propertyHasFileReference =
                IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasFileReference").toOpenRDFURI();
        final URI propertyHasFileName = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasFileName").toOpenRDFURI();
        final URI propertyHasAlias = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasAlias").toOpenRDFURI();
        final URI propertyHasPath = IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasPath").toOpenRDFURI();
        final URI propertyHasDescription =
                IRI.create(PoddServletHelper.PODD_BASE_NAMESPACE, "hasDescription").toOpenRDFURI();
        
        final ValueFactory f = tempRepositoryConnection.getValueFactory();
        final Literal fileNameLiteral = f.createLiteral(httpRef.getFilename());
        final Literal filePathLiteral = f.createLiteral(httpRef.getPath());
        final Literal fileAliasLiteral = f.createLiteral(httpRef.getServerAlias());
        final Literal fileDescLiteral = f.createLiteral(httpRef.getDescription());
        
        tempRepositoryConnection.add(fileRefObject, RDF.TYPE, fileRefURI, context);
        tempRepositoryConnection.add(fileRefObject, propertyHasFileName, fileNameLiteral, context);
        tempRepositoryConnection.add(fileRefObject, propertyHasAlias, fileAliasLiteral, context);
        tempRepositoryConnection.add(fileRefObject, propertyHasPath, filePathLiteral, context);
        tempRepositoryConnection.add(fileRefObject, propertyHasDescription, fileDescLiteral, context);
        
        tempRepositoryConnection.add(objectToAttachTo, propertyHasFileReference, fileRefObject, context);
        return fileRefObject;
    }
    
    /**
     * Check RDF statements in the provided RepositoryConnection for File References and validate if
     * any exist. While this method does not make any changes to the content of the Repository, any
     * uncommitted operations are committed.
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
        
        // search statements identifying any resource as a File Reference
        final RepositoryResult<Statement> statements =
                repositoryConnection.getStatements(null, RDF.TYPE, fileRefURI, false, context);
        final List<Statement> statementList = Iterations.addAll(statements, new ArrayList<Statement>());
        for(final Statement statement : statementList)
        {
            final String fileRefObj = statement.getSubject().stringValue();
            final HttpFileReference httpFileRef = new HttpFileReference();
            
            final RepositoryResult<Statement> st =
                    repositoryConnection.getStatements(IRI.create(fileRefObj).toOpenRDFURI(), null, null, false,
                            context);
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
            try
            {
                this.checkFileExists(httpFileRef);
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
            // this.aliases.load(this.getClass().getResourceAsStream(this.aliasFilePath));
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
