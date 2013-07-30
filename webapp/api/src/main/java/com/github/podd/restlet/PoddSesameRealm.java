package com.github.podd.restlet;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;
import org.restlet.security.Role;
import org.restlet.security.User;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.RestletUtilSesameRealm;
import com.github.podd.utils.PoddUser;

/**
 * Abstract class which customises RestletUtilSesameRealm.java to use PODDUsers and PoddRoles.
 * 
 */
public abstract class PoddSesameRealm extends RestletUtilSesameRealm
{
    protected static final String PARAM_USER_URI = "userUri";
    protected static final String PARAM_USER_SECRET = "userSecret";
    protected static final String PARAM_USER_FIRSTNAME = "userFirstName";
    protected static final String PARAM_USER_LASTNAME = "userLastName";
    protected static final String PARAM_USER_EMAIL = "userEmail";
    protected static final String PARAM_USER_STATUS = "userStatus";
    protected static final String PARAM_USER_HOMEPAGE = "userHomePage";
    protected static final String PARAM_USER_IDENTIFIER = "userIdentifier";
    protected static final String PARAM_USER_ORCID = "userOrcid";
    protected static final String PARAM_USER_ORGANIZATION = "userOrganization";
    protected static final String PARAM_USER_TITLE = "userTitle";
    protected static final String PARAM_USER_PHONE = "userPhone";
    protected static final String PARAM_USER_ADDRESS = "userAddress";
    protected static final String PARAM_USER_POSITION = "userPosition";
    protected static final String PARAM_ROLE = "role";
    protected static final String PARAM_OBJECT_URI = "objectUri";
    
    public PoddSesameRealm(final Repository repository, final URI... contexts)
    {
        super(repository, contexts);
    }
    
    /**
     * This method adds a User entry to the Realm and underlying Sesame Repository including
     * PODD-specific user parameters.
     * 
     * @param nextUser
     * @return
     */
    public URI addUser(final PoddUser nextUser)
    {
        return super.addUser(nextUser);
    }
    
    protected abstract Entry<Role, URI> buildMapEntryFromSparqlResult(BindingSet bindingSet);
    
    /**
     * Overridden to build a PoddUser from the data retrieved in a SPARQL result.
     * 
     * @param userIdentifier
     *            The unique identifier of the User.
     * @param bindingSet
     *            Results of a single user from SPARQL.
     * @return A RestletUtilUser account.
     * 
     */
    @Override
    protected abstract PoddUser buildRestletUserFromSparqlResult(final String userIdentifier,
            final BindingSet bindingSet);
    
    /**
     * Retrieve a Restlet Role from the values retrieved via SPARQL
     * 
     * @param bindingSet
     * @return
     */
    protected abstract Role buildRoleFromSparqlResult(final BindingSet bindingSet);
    
    /**
     * Build a SPARQL query which returns Roles mapped to a given user and any 
     * optional object URIs included in the mapping.
     * 
     * @param userIdentifier
     * @return
     */
    protected abstract String buildSparqlQueryForRolesWithObjects(String userIdentifier);
    
    /**
     * Build a SPARQL query which returns Roles common to a given user and object URI
     * 
     * @param userIdentifier
     * @param objectUris
     * @return
     */
    protected abstract String buildSparqlQueryForObjectRoles(final String userIdentifier, final URI objectUri);
    
    /**
     * Overridden to build a SPARQL query to retrieve details of a PoddUser.
     * 
     * NOTE: For finding users, only User Identifier and secret are Mandatory fields. This is not
     * indicative of mandatory parameters when creating new users.
     * 
     * @param userIdentifier
     *            The unique identifier of the User to search for.
     * @return A String representation of the SPARQL Select query
     */
    @Override
    protected abstract String buildSparqlQueryToFindUser(final String userIdentifier, boolean findAllUsers);
    
    /**
     * For a given User and object URI, this method finds Role Mappings between them.
     * 
     * @param user
     * @param objectUri
     * @return A Collection of Roles between given User and object
     */
    public abstract Collection<Role> getRolesForObject(User user, URI objectUri);
    
    /**
     * Retrieve Roles that a User is mapped to together with any optional object URIs. 
     * 
     * @param user
     * @return A Map of <Role, object URI> pairs
     */
    public abstract Collection<Entry<Role,URI>> getRolesWithObjectMappings(User user);
    
    /**
     * @param name
     */
    @Override
    protected abstract RestletUtilRole getRoleByName(final String name);
    
    /**
     * @param uri
     */
    @Override
    protected abstract RestletUtilRole getRoleByUri(final URI uri);
    
    /**
     * This method maps a User to a Role with an optional URI.
     * 
     * Example 1: John (User) is a PROJECT_MEMBER (Role) of the "Water Stress" project (URI).
     * 
     * Example 2: Bob (User) is an ADMIN (Role) for the whole repository (no URI).
     * 
     * @param user
     * @param role
     * @param optionalObjectUri
     */
    public abstract void map(User user, Role role, URI optionalObjectUri);
    
}