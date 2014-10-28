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
package com.github.podd.api.data;

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
