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
    /**
     * Default value is urn:podd:default:usermanagementgraph:
     */
    public static final URI DEF_USER_MANAGEMENT_GRAPH = PoddRdfConstants.VALUE_FACTORY
            .createURI("urn:podd:default:usermanagementgraph:");

    
    public static final String PROPERTY_TEMPLATE_BASE = "poddBase.html.ftl";
    public static final String PROPERTY_TEMPLATE_LOGIN = "login.html.ftl";
    
    public static final String PATH_ABOUT = "/about";
    public static final String PATH_INDEX = "/index";
    public static final String PATH_USER_DETAILS = "/userdetails";
    public static final String PATH_LOGIN_FORM = "/login";
    public static final String PATH_LOGIN_SUBMIT = "/submitlogininfo";
    public static final String PATH_LOGOUT = "/deauthenticate";
    public static final String PATH_REDIRECT_LOGGED_IN = "/about";
    

    public static final String COOKIE_NAME = "PODDAuthentication";
    
    
    
    public static final String PATH_PODD_PLANT = "/ontologies/poddPlant.owl";
    public static final String PATH_PODD_SCIENCE = "/ontologies/poddScience.owl";
    public static final String PATH_PODD_BASE = "/ontologies/poddBase.owl";
    
    public static final String URI_PODD_PLANT = "http://purl.org/podd/ns/poddPlant";
    public static final String URI_PODD_SCIENCE = "http://purl.org/podd/ns/poddScience";
    public static final String URI_PODD_BASE = "http://purl.org/podd/ns/poddBase";







    
    
}
