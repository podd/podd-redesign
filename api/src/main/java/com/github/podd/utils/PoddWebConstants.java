/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.URI;

/**
 * @author kutila
 * 
 */
public interface PoddWebConstants
{
    public static final URI SCOPE_ARTIFACT = PoddRdfConstants.VALUE_FACTORY
            .createURI("http://purl.org/podd/poddBase#PoddArtifact");
    
    public static final URI SCOPE_REPOSITORY = PoddRdfConstants.VALUE_FACTORY
            .createURI("http://purl.org/podd/poddBase#PoddRepository");
    
    public static final URI PODD_ROLEMAPPEDOBJECT = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_USER, "roleMappedObject");
    
    public static final URI PODD_USER_ORGANIZATION = PoddRdfConstants.VALUE_FACTORY.createURI(
            PoddRdfConstants.PODD_USER, "organization");
    
    public static final URI PODD_USER_ORCID = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_USER,
            "orcid");
    
    public static final URI PODD_USER_HOMEPAGE = PoddRdfConstants.VALUE_FACTORY.createURI(PoddRdfConstants.PODD_USER,
            "uri");
    
    public static final String DEF_CHALLENGE_AUTH_METHOD = "digest";
    
    public static final String COOKIE_NAME = "PODDAuthentication";
    
    /**
     * Freemarker template used as the base for rendering all HTML pages
     */
    public static final String PROPERTY_TEMPLATE_BASE = "poddBase.html.ftl";
    
    /**
     * Path to locate resources
     */
    public static final String PATH_RESOURCES = "/resources/";
    
    /**
     * Path to login page
     */
    public static final String PATH_LOGIN_FORM = "loginpage";
    
    /**
     * Path to submit login details
     */
    public static final String PATH_LOGIN_SUBMIT = "login";
    
    /**
     * Path to logout from PODD
     */
    public static final String PATH_LOGOUT = "logout";
    
    /**
     * Path to redirect user on successful login
     */
    public static final String PATH_REDIRECT_LOGGED_IN = "index";
    
    /**
     * Path to "about" page
     */
    public static final String PATH_ABOUT = "about";
    
    /**
     * Path to "help" pages
     */
    public static final String PATH_HELP = "help";
    
    /**
     * Path to "index" page
     */
    public static final String PATH_INDEX = "index";
    
    /**
     * Path to "user details" page
     */
    public static final String PATH_USER_DETAILS = "user/";
    
    /**
     * Path to list artifacts
     */
    public static final String PATH_ARTIFACT_LIST = "artifacts";
    
    /**
     * Path to load a new artifact into PODD
     */
    public static final String PATH_ARTIFACT_UPLOAD = "artifact/new";
    
    /**
     * Path to get the base (asserted) statements of an artifact
     */
    public static final String PATH_ARTIFACT_GET_BASE = "artifact/base";
    
    /**
     * Path to get the inferred statements of an artifact
     */
    public static final String PATH_ARTIFACT_GET_INFERRED = "artifact/inferred";
    
    /**
     * Path to edit an artifact, merging it with existing statements
     */
    public static final String PATH_ARTIFACT_EDIT_MERGE = "artifact/edit/merge";
    
    /**
     * Path to edit an artifact, replacing previous statements about the edited objects
     */
    public static final String PATH_ARTIFACT_EDIT_REPLACE = "artifact/edit/replace";
    
    /**
     * Path to delete an artifact. This uses HTTP delete method
     */
    public static final String PATH_ARTIFACT_DELETE = "artifact";
    
    /**
     * Path to the file reference attachment service
     */
    public static final String PATH_ATTACH_FILE_REF = "attachref";
    
    /**
     * Path prefix of the reset service
     */
    public static final String PATH_RESET_PREFIX = "reset";
    
    /**
     * Key used to represent user identifier part of a URL
     */
    public static final String KEY_USER_IDENTIFIER = "identifier";
    
    /**
     * Key used to represent an artifact id as part of a request
     */
    public static final String KEY_ARTIFACT_IDENTIFIER = "artifacturi";
    
    /**
     * Key use to represent an object as part of a request
     */
    public static final String KEY_OBJECT_IDENTIFIER = "objecturi";
    
    /**
     * Key used to represent a specific help page as part of a request
     */
    public static final String KEY_HELP_PAGE_IDENTIFIER = "helppage";
    
    public static final String PROPERTY_CHALLENGE_AUTH_METHOD = "podd.webservice.auth.challenge.method";
    public static final String PROPERTY_TEST_WEBSERVICE_RESET_KEY = "podd.webservice.reset.key";
    
    public static final String PROPERTY_PURL_PREFIX = "podd.purl.prefix";
    
    /**
     * Key used to select published artifacts. Defaults to true.
     */
    public static final String KEY_PUBLISHED = "published";
    
    /**
     * Key used to select unpublished artifacts. Defaults to true for authenticated users.
     */
    public static final String KEY_UNPUBLISHED = "unpublished";
    
}
