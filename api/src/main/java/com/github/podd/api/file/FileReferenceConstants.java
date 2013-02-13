/**
 * 
 */
package com.github.podd.api.file;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * This class contains utility methods to construct FileReference objects, validate them etc.
 * 
 * @author kutila
 * @created 2012/11/05
 */
public class FileReferenceConstants
{
    public static final URI PODD_FILE_REPOSITORY = ValueFactoryImpl.getInstance().createURI(
            "http://purl.org/podd/ns/poddBase#", "FileRepository");
    public static final URI PODD_HTTP_FILE_REPOSITORY = ValueFactoryImpl.getInstance().createURI(
            "http://purl.org/podd/ns/poddBase#", "HttpFileRepository");
    public static final URI PODD_SSH_FILE_REPOSITORY = ValueFactoryImpl.getInstance().createURI(
            "http://purl.org/podd/ns/poddBase#", "SshFileRepository");
    
    public static final URI PODD_FILE_REPOSITORY_ALIAS = ValueFactoryImpl.getInstance().createURI(
            "http://purl.org/podd/ns/poddBase#", "hasFileRepositoryAlias");
    public static final URI PODD_FILE_REPOSITORY_PROTOCOL = ValueFactoryImpl.getInstance().createURI(
            "http://purl.org/podd/ns/poddBase#", "hasFileRepositoryProtocol");
    public static final URI PODD_FILE_REPOSITORY_HOST = ValueFactoryImpl.getInstance().createURI(
            "http://purl.org/podd/ns/poddBase#", "hasFileRepositoryHost");
    public static final URI PODD_FILE_REPOSITORY_PORT = ValueFactoryImpl.getInstance().createURI(
            "http://purl.org/podd/ns/poddBase#", "hasFileRepositoryPort");
    public static final URI PODD_FILE_REPOSITORY_FINGERPRINT = ValueFactoryImpl.getInstance().createURI(
            "http://purl.org/podd/ns/poddBase#", "hasFileRepositoryFingerprint");
    public static final URI PODD_FILE_REPOSITORY_USERNAME = ValueFactoryImpl.getInstance().createURI(
            "http://purl.org/podd/ns/poddBase#", "hasFileRepositoryUsername");
    public static final URI PODD_FILE_REPOSITORY_SECRET = ValueFactoryImpl.getInstance().createURI(
            "http://purl.org/podd/ns/poddBase#", "hasFileRepositorySecret");
    
    public static final String KEY_FILE_REF_TYPE = "file_reference_type";
    public static final String KEY_OBJECT_URI = "object_uri";
    public static final String KEY_ARTIFACT_URI = "artifact_uri";
    public static final String KEY_FILE_DESCRIPTION = "file_description";
    public static final String KEY_FILE_NAME = "file_name";
    public static final String KEY_FILE_PATH = "file_path";
    public static final String KEY_FILE_SERVER_ALIAS = "file_server_alias";
    
}
