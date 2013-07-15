/**
 * 
 */
package com.github.podd.api.file;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * This class contains utility methods to construct DataReference objects, validate them etc.
 * 
 * @author kutila
 * @created 2012/11/05
 */
public class DataReferenceConstants
{
    private static final String PODD_DATA_PREFIX = "http://purl.org/podd/ns/dataRepository#";
    
    public static final URI PODD_DATA_REPOSITORY = ValueFactoryImpl.getInstance().createURI(
            DataReferenceConstants.PODD_DATA_PREFIX, "DataRepository");
    public static final URI PODD_HTTP_FILE_REPOSITORY = ValueFactoryImpl.getInstance().createURI(
            DataReferenceConstants.PODD_DATA_PREFIX, "HTTPFileRepository");
    public static final URI PODD_SSH_FILE_REPOSITORY = ValueFactoryImpl.getInstance().createURI(
            DataReferenceConstants.PODD_DATA_PREFIX, "SSHFileRepository");
    
    public static final URI PODD_FILE_REPOSITORY_ALIAS = ValueFactoryImpl.getInstance().createURI(
            DataReferenceConstants.PODD_DATA_PREFIX, "hasAlias");
    
    public static final URI PODD_FILE_REPOSITORY_PROTOCOL = ValueFactoryImpl.getInstance().createURI(
            DataReferenceConstants.PODD_DATA_PREFIX, "hasDataRepositoryProtocol");
    public static final URI PODD_FILE_REPOSITORY_HOST = ValueFactoryImpl.getInstance().createURI(
            DataReferenceConstants.PODD_DATA_PREFIX, "hasDataRepositoryHost");
    public static final URI PODD_FILE_REPOSITORY_PORT = ValueFactoryImpl.getInstance().createURI(
            DataReferenceConstants.PODD_DATA_PREFIX, "hasDataRepositoryPort");
    public static final URI PODD_FILE_REPOSITORY_FINGERPRINT = ValueFactoryImpl.getInstance().createURI(
            DataReferenceConstants.PODD_DATA_PREFIX, "hasDataRepositoryFingerprint");
    public static final URI PODD_FILE_REPOSITORY_USERNAME = ValueFactoryImpl.getInstance().createURI(
            DataReferenceConstants.PODD_DATA_PREFIX, "hasDataRepositoryUsername");
    public static final URI PODD_FILE_REPOSITORY_SECRET = ValueFactoryImpl.getInstance().createURI(
            DataReferenceConstants.PODD_DATA_PREFIX, "hasDataRepositorySecret");
    
    public static final String KEY_FILE_REF_TYPE = "file_reference_type";
    public static final String KEY_OBJECT_URI = "object_uri";
    public static final String KEY_FILE_DESCRIPTION = "file_description";
    public static final String KEY_FILE_NAME = "file_name";
    public static final String KEY_FILE_PATH = "file_path";
    public static final String KEY_FILE_SERVER_ALIAS = "file_server_alias";
    
}
