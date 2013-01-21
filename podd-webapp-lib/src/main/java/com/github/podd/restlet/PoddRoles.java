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
 * Copied from http://github.com/ansell/restlet-utils
 */
public enum PoddRoles implements RestletUtilRole
{
    SUPERUSER("Super User", "An administrator (super user) of the Podd System",
            "http://purl.org/podd/roles/superuser"),
    
    PROJECT_ADMIN("Project Administrator", "An administrator of a PODD project",
            "http://purl.org/podd/roles/project_admin"),

    PROJECT_EDITOR("Project Editor", "A PODD user who can edit projects",
            "http://purl.org/podd/roles/project_editor"),
    
    PROJECT_READER("Project Reader", "A PODD user who can only read projects",
            "http://purl.org/podd/roles/project_reader");
    
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
    
    private Role role;
    
    private URI uri;
    
    PoddRoles(final String roleName, final String description, final String uriString)
    {
        this.role = new Role(roleName, description);
        this.uri = ValueFactoryImpl.getInstance().createURI(uriString);
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
    
}
