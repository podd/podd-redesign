/**
 * 
 */
package com.github.podd.restlet;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.restlet.security.Role;

import com.github.ansell.restletutils.RestletUtilRole;

/**
 * The Roles available for PODD users.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 *         Copied from http://github.com/ansell/restlet-utils
 */
public enum PoddRoles implements RestletUtilRole
{
    ADMIN("Administrator", "A repository administrator of the PODD System",
            "http://purl.org/podd/ns/poddUser#RoleAdministrator", true),
    
    PROJECT_CREATOR("Project Creator", "A User who can create new projects",
            "http://purl.org/podd/ns/poddUser#RoleProjectCreator", true),
            
    PROJECT_MEMBER("Project Member", "A user who is a member of a particular project",
            "http://purl.org/podd/ns/poddUser#RoleProjectMember", true),
    
    PROJECT_OBSERVER("Project Observer", "A user who is an observer of a particular project",
            "http://purl.org/podd/ns/poddUser#RoleProjectObserver", true),
    
    PROJECT_ADMIN("Project Administrator", "A user who is an administrator of a particular project",
            "http://purl.org/podd/ns/poddUser#RoleProjectAdministrator", true),
    
    ;
    
    public static RestletUtilRole getRoleByName(final String name)
    {
        for(final RestletUtilRole nextRole : PoddRoles.values())
        {
            if(nextRole.getName().equals(name))
            {
                return nextRole;
            }
        }
        
        return null;
    }
    
    public static RestletUtilRole getRoleByUri(final URI nextUri)
    {
        for(final RestletUtilRole nextRole : PoddRoles.values())
        {
            if(nextRole.getURI().equals(nextUri))
            {
                return nextRole;
            }
        }
        
        return null;
    }
    
    public static List<Role> getRoles()
    {
        final List<Role> result = new ArrayList<Role>(PoddRoles.values().length);
        
        for(final RestletUtilRole nextRole : PoddRoles.values())
        {
            // WARNING: After Restlet-2.1RC5 Roles will only be considered equal if they are the
            // same java object, so this must not create a new Role each time
            result.add(nextRole.getRole());
        }
        
        return result;
    }
    
    private final Role role;
    
    private final URI uri;
    
    private final boolean isAssignable;
    
    /**
     * Constructor
     * 
     * @param roleName
     * @param description
     * @param uriString
     */
    PoddRoles(final String roleName, final String description, final String uriString, final boolean isAssignable)
    {
        this.role = new Role(roleName, description);
        this.uri = ValueFactoryImpl.getInstance().createURI(uriString);
        this.isAssignable = isAssignable;
    }
    
    /**
     * @return the description
     */
    @Override
    public String getDescription()
    {
        return this.role.getDescription();
    }
    
    /**
     * @return the name
     */
    @Override
    public String getName()
    {
        return this.role.getName();
    }
    
    @Override
    public Role getRole()
    {
        return this.role;
    }
    
    @Override
    public URI getURI()
    {
        return this.uri;
    }
    
    @Override
    public boolean isAssignable()
    {
        return this.isAssignable;
    }
    
}
