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
package com.github.podd.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.restlet.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.SesameRealmConstants;

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
            "http://purl.org/podd/ns/poddUser#RoleTopObjectCreator", true),
    
    PROJECT_MEMBER("Project Member", "A user who is a member of a particular project",
            "http://purl.org/podd/ns/poddUser#RoleTopObjectMember", true),
    
    PROJECT_OBSERVER("Project Observer", "A user who is an observer of a particular project",
            "http://purl.org/podd/ns/poddUser#RoleTopObjectObserver", true),
    
    PROJECT_ADMIN("Project Administrator", "A user who is an administrator of a particular project",
            "http://purl.org/podd/ns/poddUser#RoleTopObjectAdministrator", true),
    
    PROJECT_PRINCIPAL_INVESTIGATOR("Principal Investigator",
            "A user who is the lead of a particular project and has the ability to publish the project",
            "http://purl.org/podd/ns/poddUser#RoleTopObjectPrincipalInvestigator", true),
    
    ;
    
    private final static Logger log = LoggerFactory.getLogger(PoddRoles.class);
    
    private final static Set<RestletUtilRole> INTERNAL_REPOSITORY_ROLES;
    private final static Set<Role> INTERNAL_RESTLET_ROLES;
    
    static
    {
        final Set<RestletUtilRole> tempRepositoryRoles = new HashSet<>();
        tempRepositoryRoles.add(ADMIN);
        tempRepositoryRoles.add(PROJECT_CREATOR);
        
        INTERNAL_REPOSITORY_ROLES = Collections.unmodifiableSet(tempRepositoryRoles);
        
        final Set<Role> tempRoles = Collections.newSetFromMap(new IdentityHashMap<Role, Boolean>());
        
        for(final RestletUtilRole nextRole : PoddRoles.values())
        {
            // WARNING: After Restlet-2.1RC5 Roles are only considered equal if they are the
            // same java object, so this must not create a new Role each time
            tempRoles.add(nextRole.getRole());
        }
        
        INTERNAL_RESTLET_ROLES = Collections.unmodifiableSet(tempRoles);
    }
    
    /**
     * Dumps the role mappings from the given map to the given model, optionally into the given
     * contexts.
     */
    public static void dumpRoleMappingsArtifact(final Map<RestletUtilRole, Collection<String>> mappings,
            final Model model, final URI... contexts)
    {
        for(final Entry<RestletUtilRole, Collection<String>> nextEntry : mappings.entrySet())
        {
            final RestletUtilRole nextRole = nextEntry.getKey();
            for(final String nextUserIdentifier : nextEntry.getValue())
            {
                final BNode mappingUri = PODD.VF.createBNode();
                
                model.add(mappingUri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING, contexts);
                model.add(mappingUri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, nextRole.getURI(), contexts);
                model.add(mappingUri, SesameRealmConstants.OAS_ROLEMAPPEDUSER,
                        PODD.VF.createLiteral(nextUserIdentifier), contexts);
            }
        }
    }
    
    /**
     * Dumps the role mappings from the given map to the given model, optionally into the given
     * contexts.
     */
    public static void dumpRoleMappingsUser(final Map<RestletUtilRole, Collection<URI>> mappings, final Model model,
            final URI... contexts)
    {
        for(final Entry<RestletUtilRole, Collection<URI>> nextEntry : mappings.entrySet())
        {
            final RestletUtilRole nextRole = nextEntry.getKey();
            for(final URI nextObjectUri : nextEntry.getValue())
            {
                final BNode mappingUri = PODD.VF.createBNode();
                
                model.add(mappingUri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING, contexts);
                model.add(mappingUri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, nextRole.getURI(), contexts);
                
                if(nextObjectUri != null)
                {
                    model.add(mappingUri, PODD.PODD_ROLEMAPPEDOBJECT, nextObjectUri, contexts);
                }
            }
        }
    }
    
    /**
     * Extracts role mappings for an object, to users who have the role, from the given RDF
     * statements.
     * 
     * @param model
     *            A set of RDF statements defining role mappings
     * @return A map of roles to collections of users who have the role.
     */
    public static Map<RestletUtilRole, Collection<String>> extractRoleMappingsArtifact(final Model model,
            final URI... contexts)
    {
        final ConcurrentMap<RestletUtilRole, Collection<String>> results = new ConcurrentHashMap<>();
        
        // extract Role Mapping info (User details are ignored as multiple users are not
        // supported)
        for(final Resource mappingUri : model.filter(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING, contexts)
                .subjects())
        {
            final URI roleUri =
                    model.filter(mappingUri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null, contexts).objectURI();
            final RestletUtilRole role = PoddRoles.getRoleByUri(roleUri);
            
            final Literal mappedObject =
                    model.filter(mappingUri, SesameRealmConstants.OAS_ROLEMAPPEDUSER, null, contexts).objectLiteral();
            
            PoddRoles.log.debug("Extracted Role <{}> with Optional Object <{}>", role.getName(), mappedObject);
            Collection<String> nextObjectUris = new HashSet<>();
            final Collection<String> putIfAbsent = results.putIfAbsent(role, nextObjectUris);
            if(putIfAbsent != null)
            {
                nextObjectUris = putIfAbsent;
            }
            nextObjectUris.add(mappedObject.stringValue());
        }
        
        return results;
    }
    
    /**
     * Extracts role mappings, optionally to object URIs, from the given RDF statements.
     * <p>
     * NOTE: This method does not fail if Repository roles contain object URIs or if Project Roles
     * do not contain object URIs. The user must determine when and how to fail in these cases.
     * 
     * @param model
     *            A set of RDF statements defining role mappings
     * @return A map of roles to collections of optional object URIs. If the collection contains a
     *         null element, then the role was not mapped to an object URI in at least one case.
     */
    public static Map<RestletUtilRole, Collection<URI>> extractRoleMappingsUser(final Model model,
            final URI... contexts)
    {
        final ConcurrentMap<RestletUtilRole, Collection<URI>> results = new ConcurrentHashMap<>();
        
        // extract Role Mapping info (User details are ignored as multiple users are not
        // supported)
        for(final Resource mappingUri : model.filter(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING, contexts)
                .subjects())
        {
            final URI roleUri =
                    model.filter(mappingUri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null, contexts).objectURI();
            final RestletUtilRole role = PoddRoles.getRoleByUri(roleUri);
            
            final URI mappedObject = model.filter(mappingUri, PODD.PODD_ROLEMAPPEDOBJECT, null, contexts).objectURI();
            
            PoddRoles.log.debug("Extracted Role <{}> with Optional Object <{}>", role.getName(), mappedObject);
            Collection<URI> nextObjectUris = new HashSet<>();
            final Collection<URI> putIfAbsent = results.putIfAbsent(role, nextObjectUris);
            if(putIfAbsent != null)
            {
                nextObjectUris = putIfAbsent;
            }
            nextObjectUris.add(mappedObject);
        }
        
        return results;
    }
    
    /**
     * @return Retrieve PoddRoles that are Repository-wide (e.g. Administrator Role)
     */
    public static Set<RestletUtilRole> getRepositoryRoles()
    {
        return PoddRoles.INTERNAL_REPOSITORY_ROLES;
    }
    
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
    
    public static Set<Role> getRoles()
    {
        return PoddRoles.INTERNAL_RESTLET_ROLES;
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
