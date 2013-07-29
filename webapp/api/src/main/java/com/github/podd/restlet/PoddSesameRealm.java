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
    protected static final String PARAM_USER_TITLE = "userTitle";
    protected static final String PARAM_USER_PHONE = "userPhone";
    protected static final String PARAM_USER_ADDRESS = "userAddress";
    protected static final String PARAM_USER_POSITION = "userPosition";
    protected static final String PARAM_ROLE = "role";
    
    public PoddSesameRealm(final Repository repository, final URI... contexts)
    {
        super(repository, contexts);
    }
    
    public URI addUser(final PoddUser nextUser)
    {
        return super.addUser(nextUser);
    }
    
    @Override
    protected abstract PoddUser buildRestletUserFromSparqlResult(final String userIdentifier,
            final BindingSet bindingSet);
    
    protected abstract Role buildRoleFromSparqlResult(final BindingSet bindingSet);
    
    protected abstract String buildSparqlQueryForCommonObjectRoles(final String userIdentifier,
            final URI objectUri);
    
    @Override
    protected abstract String buildSparqlQueryToFindUser(final String userIdentifier, boolean findAllUsers);
    
    public abstract Collection<Role> getRolesForObject(User user, URI objectUri);
    
    @Override
    protected abstract RestletUtilRole getRoleByName(final String name);
    
    @Override
    protected abstract RestletUtilRole getRoleByUri(final URI uri);
    
    public abstract void map(User user, Role role, URI optionalObjectUri);
    
}