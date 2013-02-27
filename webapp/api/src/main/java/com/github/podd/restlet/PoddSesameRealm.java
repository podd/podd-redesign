package com.github.podd.restlet;

import java.util.Collection;

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;
import org.restlet.security.Role;
import org.restlet.security.User;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.RestletUtilSesameRealm;
import com.github.podd.utils.PoddUser;

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
    protected static final String PARAM_ROLE = "role";
    
    public PoddSesameRealm(Repository repository, URI... contexts)
    {
        super(repository, contexts);
    }
    
    public abstract Collection<Role> getCommonRolesForObjects(User user, Collection<URI> objectUris);
    
    public abstract void map(User user, Role role, URI optionalObjectUri);
    
    protected abstract RestletUtilRole getRoleByUri(final URI uri);
    
    protected abstract RestletUtilRole getRoleByName(final String name);
    
    protected abstract String buildSparqlQueryToFindUser(final String userIdentifier, boolean findAllUsers);
    
    protected abstract String buildSparqlQueryForCommonObjectRoles(final String userIdentifier,
            final Collection<URI> objectUris);
    
    protected abstract Role buildRoleFromSparqlResult(final BindingSet bindingSet);
    
    protected abstract PoddUser buildRestletUserFromSparqlResult(final String userIdentifier,
            final BindingSet bindingSet);
    
    public URI addUser(final PoddUser nextUser)
    {
        return super.addUser(nextUser);
    }
    
}