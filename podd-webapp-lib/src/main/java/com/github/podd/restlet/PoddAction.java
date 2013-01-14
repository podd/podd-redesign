/**
 * 
 */
package com.github.podd.restlet;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.restlet.security.Role;

import com.github.ansell.restletutils.RestletUtilRoles;

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
     * An action by an administrator asking to edit the roles for a user.
     * 
     * By default only admin users are allowed to edit the roles for users.
     */
    ROLE_EDIT("true", "Action Failed: error message should come here", Collections.singleton(RestletUtilRoles.ADMIN
            .getRole())),
    
    ;
    
    private final boolean authRequired;
    private final String errorMessage;
    private final Set<Role> roles;
    
    PoddAction(final String authenticationRequired, final String errorMessage, final Set<Role> roles)
    {
        this.authRequired = Boolean.valueOf(authenticationRequired);
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
