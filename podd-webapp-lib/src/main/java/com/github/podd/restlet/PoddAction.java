/**
 * 
 */
package com.github.podd.restlet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.restlet.security.Role;

import com.github.podd.utils.PoddWebConstants;

/**
 * Provides constants to use in the authentication interface, including whether authentication is
 * required for a particular action and what the error message should be if a request fails.
 * 
 * FIXME: Roles are currently hardcoded into each action.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public enum PoddAction
{
    /**
     * An action by a user asking to create a new artifact, or update an existing artifact.
     * 
     * By default superuser, project admin and project editor users are allowed to create/update artifacts.
     */
    ARTIFACT_CREATE(
            true, 
            "Could not create/edit artifact.", 
            new HashSet<Role>(Arrays.asList(
                    PoddRoles.ADMIN.getRole(),
                    PoddRoles.AUTHENTICATED.getRole(),
                    PoddRoles.PROJECT_ADMIN.getRole()
                    )), 
            new HashSet<URI>(Arrays.asList(PoddWebConstants.SCOPE_ARTIFACT))
            ),
    
    /**
     * An action by a user asking to delete an artifact.
     * 
     * By default superuser, project admin and project editor users are allowed to delete artifacts.
     */
    ARTIFACT_DELETE(
            true,
            "Could not delete artifact",
            new HashSet<Role>(Arrays.asList(
                    PoddRoles.AUTHENTICATED.getRole(),
                    PoddRoles.ADMIN.getRole())),
            new HashSet<URI>(Arrays.asList(PoddWebConstants.SCOPE_ARTIFACT))
            ),
    
    /**
     * An action by a user asking to read an artifact.
     * 
     * By default all unauthenticated users are allowed to read artifacts.
     */
    ARTIFACT_READ(
            false, 
            "Failed to read artifact", 
            Collections.<Role> emptySet(),
            new HashSet<URI>(Arrays.asList(PoddWebConstants.SCOPE_ARTIFACT))
            ),
    
    /**
     * An action by a user asking to publish an artifact.
     * 
     * By default only the superuser and project admins are allowed to publish projects.
     */
    ARTIFACT_PUBLISH(
            true, 
            "Could not publish artifact",
            new HashSet<Role>(Arrays.asList(
                    PoddRoles.AUTHENTICATED.getRole(),
                    PoddRoles.ADMIN.getRole())),
            new HashSet<URI>(Arrays.asList(PoddWebConstants.SCOPE_ARTIFACT))
            ),
            
    /**
     * An action by an administrator asking to create a new user, or update an existing user.
     * 
     * By default only the superuser is allowed to create new users.
     */
    USER_CREATE(
            true, 
            "Could not create/update user.", 
            new HashSet<Role>(Arrays.asList(
                    PoddRoles.AUTHENTICATED.getRole(),
                    PoddRoles.ADMIN.getRole())),
            new HashSet<URI>(Arrays.asList(PoddWebConstants.SCOPE_REPOSITORY))
            ),
    
    /**
     * An action by an administrator asking to delete an existing user.
     * 
     * By default only admin users are allowed to delete existing users.
     */
    USER_DELETE(
            true, 
            "Could not delete user", 
            Collections.singleton(PoddRoles.ADMIN.getRole()),
            new HashSet<URI>(Arrays.asList(PoddWebConstants.SCOPE_REPOSITORY))
            ),
    
    /**
     * An action by a user asking to fetch a user
     * 
     * By default all authenticated users can request user details, although if they are not
     * admins, they will not be able to see information about other users.
     */
    USER_READ(
            true, 
            "Could not retrieve user details", 
            new HashSet<Role>(Arrays.asList(
                    PoddRoles.AUTHENTICATED.getRole(),
                    PoddRoles.ADMIN.getRole())),
            new HashSet<URI>(Arrays.asList(PoddWebConstants.SCOPE_REPOSITORY))
            ),
    ;
    
    private final boolean authRequired;
    private final String errorMessage;
    private final Set<Role> roles;
    
    PoddAction(final boolean authenticationRequired, final String errorMessage, final Set<Role> roles, final Set<URI> scopeTypes)
    {
        this.authRequired = authenticationRequired;
        this.errorMessage = errorMessage;
        this.roles = roles;
    }
    
    public String getErrorMessage()
    {
        return this.errorMessage;
    }
    
    /**
     * @return the authRequired
     */
    public boolean isAuthRequired()
    {
        return this.authRequired;
    }
    
    public boolean isRoleRequired()
    {
        return !this.roles.isEmpty();
    }
    
    /**
     * Returns true if isRoleRequired() returns true and any of the roles in authenticatedRoles are
     * in the set of roles for this action.
     * 
     * @param authenticatedRoles
     *            The set of roles that the user currently has.
     * @return True if this action requires a role and the collection of authenticated roles matches
     *         one of the roles for this action.
     */
    public boolean matchesForRoles(final Collection<Role> authenticatedRoles)
    {
        if(!this.isRoleRequired())
        {
            return true;
        }
        
        for(final Role nextAuthenticatedRole : authenticatedRoles)
        {
            if(this.roles.contains(nextAuthenticatedRole))
            {
                return true;
            }
        }
        
        return false;
    }
}
