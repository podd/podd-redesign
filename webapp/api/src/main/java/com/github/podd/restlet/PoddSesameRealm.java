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
package com.github.podd.restlet;

import info.aduna.iteration.Iterations;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.queryrender.RenderUtils;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.data.Status;
import org.restlet.engine.security.RoleMapping;
import org.restlet.resource.ResourceException;
import org.restlet.security.Enroler;
import org.restlet.security.Group;
import org.restlet.security.LocalVerifier;
import org.restlet.security.Realm;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;

/**
 * Abstract class which customises RestletUtilSesameRealm.java to use PODDUsers and PoddRoles.
 * 
 */
public abstract class PoddSesameRealm extends Realm
{
    // ======================= begin inner classes ==========================
    
    /**
     * Enroler class based on the default security model.
     * 
     * NOTE: 2013/01/22 - this class uses PoddRoles
     */
    private class DefaultPoddSesameRealmEnroler implements Enroler
    {
        
        @Override
        public void enrole(final ClientInfo clientInfo)
        {
            // casting is safe here as buildRestletUserFromSparqlResult() creates a PoddUser
            final PoddUser user = PoddSesameRealm.this.findUser(clientInfo.getUser().getIdentifier());
            
            if(user != null)
            {
                // Add roles specific to this user
                final Set<Role> userRoles = PoddSesameRealm.this.findRoles(user);
                
                for(final Role role : userRoles)
                {
                    clientInfo.getRoles().add(role);
                }
                
                // FIXME: When we support groups, reenable this section
                // Find all the inherited groups of this user
                // final Set<Group> userGroups = PoddSesameRealm.this.findGroups(user);
                
                // Add roles common to group members
                // final Set<Role> groupRoles = PoddSesameRealm.this.findRoles(userGroups);
                
                // for(final Role role : groupRoles)
                // {
                // clientInfo.getRoles().add(role);
                // }
            }
        }
    }
    
    /**
     * Verifier class based on the default security model. It looks up users in the mapped
     * organizations.
     * 
     * NOTE: 2013/01/22 - this class is identical to the DefaultOasSesameRealmVerifier.java
     */
    private class DefaultPoddSesameRealmVerifier extends LocalVerifier
    {
        @Override
        protected User createUser(final String identifier, final Request request, final Response response)
        {
            final PoddUser checkUser = PoddSesameRealm.this.findUser(identifier);
            
            if(checkUser == null)
            {
                PoddSesameRealm.this.log.error("Cannot create a user for the given identifier: {}", identifier);
                throw new IllegalArgumentException("Cannot create a user for the given identifier");
            }
            
            final PoddUser result =
                    new PoddUser(identifier, (char[])null, checkUser.getFirstName(), checkUser.getLastName(),
                            checkUser.getEmail(), checkUser.getUserStatus(), checkUser.getHomePage(),
                            checkUser.getOrganization(), checkUser.getOrcid(), checkUser.getTitle(),
                            checkUser.getPhone(), checkUser.getAddress(), checkUser.getPosition());
            
            return result;
        }
        
        @Override
        public char[] getLocalSecret(final String identifier)
        {
            char[] result = null;
            final User user = PoddSesameRealm.this.findUser(identifier);
            
            if(user != null)
            {
                result = user.getSecret();
            }
            
            return result;
        }
        
        /**
         * FIXME: Replace this with an implementation that hashes the given secret and compares the
         * hash value rather than comparing the secret directly, so we don't need to store the
         * password in the database.
         */
        @Override
        public int verify(String identifier, char[] secret)
        {
            return compare(secret, getLocalSecret(identifier)) ? RESULT_VALID : RESULT_INVALID;
        }
    }
    
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
    protected static final String PARAM_SEARCH_TERM = "searchTerm";
    /**
     * The Sesame Repository to use to get access to user information.
     */
    private Repository repository;
    
    private URI[] userManagerContexts;
    
    protected ValueFactory vf;
    
    /** The currently cached list of root groups. */
    private volatile List<Group> cachedRootGroups;
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Constructor.
     */
    public PoddSesameRealm(final Repository repository, final URI... contexts)
    {
        OpenRDFUtil.verifyContextNotNull(contexts);
        this.setRepository(repository);
        this.setContexts(contexts);
        // set PODD-specific Enroler and Verifier
        this.setEnroler(new DefaultPoddSesameRealmEnroler());
        this.setVerifier(new DefaultPoddSesameRealmVerifier());
        // this.cachedRootGroups = new CopyOnWriteArrayList<Group>();
        // this.rootGroups = new CopyOnWriteArrayList<Group>();
        // this.roleMappings = new CopyOnWriteArrayList<RoleMapping>();
        // this.users = new CopyOnWriteArrayList<User>();
    }
    
    /**
     * Recursively adds groups where a given user is a member.
     * 
     * @param user
     *            The member user.
     * @param userGroups
     *            The set of user groups to update.
     * @param currentGroup
     *            The current group to inspect.
     * @param stack
     *            The stack of ancestor groups.
     * @param inheritOnly
     *            Indicates if only the ancestors groups that have their "inheritRoles" property
     *            enabled should be added.
     */
    private void addGroupsForUser(final PoddUser user, final Set<Group> userGroups, final Group currentGroup,
            final Set<Group> stack, final boolean inheritOnly)
    {
        if((currentGroup != null) && !stack.contains(currentGroup))
        {
            stack.add(currentGroup);
            
            if(currentGroup.getMemberUsers().contains(user))
            {
                userGroups.add(currentGroup);
                
                // Add the ancestor groups as well
                boolean inherit = !inheritOnly || currentGroup.isInheritingRoles();
                
                if(inherit)
                {
                    for(final Group group : stack)
                    {
                        userGroups.add(group);
                        inherit = !inheritOnly || group.isInheritingRoles();
                    }
                }
            }
            
            for(final Group group : currentGroup.getMemberGroups())
            {
                this.addGroupsForUser(user, userGroups, group, stack, inheritOnly);
            }
        }
    }
    
    private void addRoleMapping(final RoleMapping nextMapping) throws RepositoryException
    {
        RepositoryConnection conn = null;
        try
        {
            conn = this.repository.getConnection();
            conn.begin();
            
            final URI nextRoleMappingUUID = this.vf.createURI("urn:oas:rolemapping:", UUID.randomUUID().toString());
            
            conn.add(this.vf.createStatement(nextRoleMappingUUID, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING),
                    this.getContexts());
            
            conn.add(this.vf.createStatement(nextRoleMappingUUID, SesameRealmConstants.OAS_ROLEMAPPEDROLE, this
                    .getRoleByName(nextMapping.getTarget().getName()).getURI()), this.getContexts());
            
            if(nextMapping.getSource() instanceof Group)
            {
                conn.add(
                        this.vf.createStatement(nextRoleMappingUUID, SesameRealmConstants.OAS_ROLEMAPPEDGROUP,
                                this.vf.createLiteral(((Group)nextMapping.getSource()).getName())), this.getContexts());
            }
            else if(nextMapping.getSource() instanceof User)
            {
                conn.add(
                        this.vf.createStatement(nextRoleMappingUUID, SesameRealmConstants.OAS_ROLEMAPPEDUSER,
                                this.vf.createLiteral(((User)nextMapping.getSource()).getIdentifier())),
                        this.getContexts());
            }
            else
            {
                conn.rollback();
                throw new RuntimeException("Could not map role for unknown source type: "
                        + nextMapping.getSource().getClass().getName());
            }
            
            conn.commit();
        }
        catch(final RepositoryException e)
        {
            this.log.error("Found exception while adding role mapping", e);
            if(conn != null)
            {
                conn.rollback();
            }
            throw e;
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Found exception closing repository connection", e);
                }
            }
        }
    }
    
    /**
     * Adds a fully populated root group to the underlying repository, including a statement
     * indicating that this group is a root group.
     * 
     * @param nextRootGroup
     */
    public void addRootGroup(final Group nextRootGroup)
    {
        this.getRootGroups().add(nextRootGroup);
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repository.getConnection();
            conn.begin();
            
            this.storeGroup(nextRootGroup, conn, true);
            
            conn.commit();
        }
        catch(final RepositoryException e)
        {
            this.log.error("Found exception while storing root group", e);
            if(conn != null)
            {
                try
                {
                    conn.rollback();
                }
                catch(final RepositoryException e1)
                {
                    this.log.error("Found exception while trying to roll back connection", e1);
                }
            }
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Found exception closing repository connection", e);
                }
            }
        }
    }
    
    public final URI addUser(final PoddUser nextUser)
    {
        URI nextUserUUID =
                this.vf.createURI("urn:oas:user:", nextUser.getIdentifier() + ":" + UUID.randomUUID().toString());
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repository.getConnection();
            conn.begin();
            
            final List<Statement> userIdentifierStatements =
                    Iterations.asList(conn.getStatements(null, SesameRealmConstants.OAS_USERIDENTIFIER,
                            this.vf.createLiteral(nextUser.getIdentifier()), true, this.getContexts()));
            
            // FIXME: Is it safe to overwrite old users like this...
            if(!userIdentifierStatements.isEmpty())
            {
                for(final Statement nextUserIdentifierStatement : userIdentifierStatements)
                {
                    if(nextUserIdentifierStatement.getSubject() instanceof URI)
                    {
                        // retrieve the user URI to persist it with the new statements
                        // does not matter if this is overwritten multiple times if there were
                        // multiple users with this identifier in the database
                        nextUserUUID = (URI)nextUserIdentifierStatement.getSubject();
                    }
                    
                    final List<Statement> currentUserStatements =
                            Iterations.asList(conn.getStatements(nextUserIdentifierStatement.getSubject(), null, null,
                                    true, this.getContexts()));
                    
                    // remove all of the previously known statements
                    conn.remove(currentUserStatements, this.getContexts());
                }
            }
            
            conn.add(nextUserUUID, RDF.TYPE, SesameRealmConstants.OAS_USER, this.getContexts());
            
            conn.add(nextUserUUID, SesameRealmConstants.OAS_USERIDENTIFIER,
                    this.vf.createLiteral(nextUser.getIdentifier()), this.getContexts());
            
            if(nextUser.getSecret() != null)
            {
                // TODO: Hash this
                conn.add(nextUserUUID, SesameRealmConstants.OAS_USERSECRET,
                        this.vf.createLiteral(new String(nextUser.getSecret())), this.getContexts());
            }
            
            if(nextUser.getFirstName() != null)
            {
                conn.add(nextUserUUID, SesameRealmConstants.OAS_USERFIRSTNAME,
                        this.vf.createLiteral(nextUser.getFirstName()), this.getContexts());
            }
            
            if(nextUser.getLastName() != null)
            {
                conn.add(nextUserUUID, SesameRealmConstants.OAS_USERLASTNAME,
                        this.vf.createLiteral(nextUser.getLastName()), this.getContexts());
            }
            
            if(nextUser.getEmail() != null)
            {
                conn.add(nextUserUUID, SesameRealmConstants.OAS_USEREMAIL, this.vf.createLiteral(nextUser.getEmail()),
                        this.getContexts());
            }
            
            conn.commit();
        }
        catch(final RepositoryException e)
        {
            this.log.error("Found repository exception while adding user", e);
            try
            {
                conn.rollback();
            }
            catch(final RepositoryException e1)
            {
                this.log.error("Found unexpected exception while rolling back repository connection after exception");
            }
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Found unexpected repository exception", e);
                }
            }
        }
        
        return nextUserUUID;
    }
    
    protected URI addUser(final PoddUser nextUser, final boolean isNew)
    {
        final PoddUser oldUser = this.findUser(nextUser.getIdentifier());
        if(isNew && oldUser != null)
        {
            throw new RuntimeException("User already exists");
        }
        else if(!isNew && oldUser == null)
        {
            throw new RuntimeException("Could not modify User (does not exist)");
        }
        
        final URI nextUserUUID = this.addUser(nextUser);
        
        this.log.debug("adding PODD specific parameters");
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.getRepository().getConnection();
            conn.begin();
            
            if(nextUser.getOrganization() != null)
            {
                conn.add(nextUserUUID, PoddRdfConstants.PODD_USER_ORGANIZATION,
                        this.vf.createLiteral(nextUser.getOrganization()), this.getContexts());
            }
            
            if(nextUser.getOrcid() != null)
            {
                conn.add(nextUserUUID, PoddRdfConstants.PODD_USER_ORCID, this.vf.createLiteral(nextUser.getOrcid()),
                        this.getContexts());
            }
            
            if(nextUser.getHomePage() != null)
            {
                conn.add(nextUserUUID, PoddRdfConstants.PODD_USER_HOMEPAGE, nextUser.getHomePage(), this.getContexts());
            }
            
            if(nextUser.getTitle() != null)
            {
                conn.add(nextUserUUID, PoddRdfConstants.PODD_USER_TITLE, this.vf.createLiteral(nextUser.getTitle()),
                        this.getContexts());
            }
            
            if(nextUser.getPhone() != null)
            {
                conn.add(nextUserUUID, PoddRdfConstants.PODD_USER_PHONE, this.vf.createLiteral(nextUser.getPhone()),
                        this.getContexts());
            }
            
            if(nextUser.getAddress() != null)
            {
                conn.add(nextUserUUID, PoddRdfConstants.PODD_USER_ADDRESS,
                        this.vf.createLiteral(nextUser.getAddress()), this.getContexts());
            }
            
            if(nextUser.getPosition() != null)
            {
                conn.add(nextUserUUID, PoddRdfConstants.PODD_USER_POSITION,
                        this.vf.createLiteral(nextUser.getPosition()), this.getContexts());
            }
            
            PoddUserStatus status = PoddUserStatus.INACTIVE;
            if(nextUser.getUserStatus() != null)
            {
                status = nextUser.getUserStatus();
            }
            conn.add(nextUserUUID, PoddRdfConstants.PODD_USER_STATUS, status.getURI(), this.getContexts());
            
            conn.commit();
        }
        catch(final RepositoryException e)
        {
            this.log.error("Found repository exception while adding user", e);
            if(conn != null)
            {
                try
                {
                    conn.rollback();
                }
                catch(final RepositoryException e1)
                {
                    this.log.error("Found unexpected exception while rolling back repository connection after exception");
                }
            }
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Found unexpected repository exception", e);
                }
            }
        }
        
        return nextUserUUID;
    }
    
    protected Entry<Role, URI> buildMapEntryFromSparqlResult(final BindingSet bindingSet)
    {
        final URI roleUri = (URI)bindingSet.getValue(PoddSesameRealm.PARAM_ROLE);
        final Role role = PoddRoles.getRoleByUri(roleUri).getRole();
        
        URI objectUri = null;
        if(bindingSet.getValue(PoddSesameRealm.PARAM_OBJECT_URI) != null)
        {
            objectUri = (URI)bindingSet.getValue(PoddSesameRealm.PARAM_OBJECT_URI);
        }
        
        this.log.debug("Building map entry: {}, <{}>", role.getName(), objectUri);
        
        return new AbstractMap.SimpleEntry<Role, URI>(role, objectUri);
    }
    
    protected PoddUser buildRestletUserFromSparqlResult(final String userIdentifier, final BindingSet bindingSet)
    {
        this.log.debug("Building PoddUser from SPARQL results");
        
        char[] secret = null;
        
        if(bindingSet.hasBinding(PoddSesameRealm.PARAM_USER_SECRET))
        {
            secret = bindingSet.getValue(PoddSesameRealm.PARAM_USER_SECRET).stringValue().toCharArray();
        }
        
        final PoddUser result =
                new PoddUser(userIdentifier, secret, bindingSet.getValue(PoddSesameRealm.PARAM_USER_FIRSTNAME)
                        .stringValue(), bindingSet.getValue(PoddSesameRealm.PARAM_USER_LASTNAME).stringValue(),
                        bindingSet.getValue(PoddSesameRealm.PARAM_USER_EMAIL).stringValue(), PoddUserStatus.INACTIVE);
        
        PoddUserStatus userStatus = PoddUserStatus.INACTIVE;
        final Value statusVal = bindingSet.getValue(PoddSesameRealm.PARAM_USER_STATUS);
        if(statusVal != null && statusVal instanceof URI)
        {
            userStatus = PoddUserStatus.getUserStatusByUri((URI)statusVal);
        }
        
        // Do not allow users without secrets to perform actions
        if(secret == null)
        {
            userStatus = PoddUserStatus.INACTIVE;
        }
        
        result.setUserStatus(userStatus);
        
        final Value organizationVal = bindingSet.getValue(PoddSesameRealm.PARAM_USER_ORGANIZATION);
        if(organizationVal != null)
        {
            result.setOrganization(organizationVal.stringValue());
        }
        
        final Value orcidVal = bindingSet.getValue(PoddSesameRealm.PARAM_USER_ORCID);
        if(orcidVal != null)
        {
            result.setOrcid(orcidVal.stringValue());
        }
        
        final Value homePageVal = bindingSet.getValue(PoddSesameRealm.PARAM_USER_HOMEPAGE);
        if(homePageVal != null)
        {
            result.setHomePage((URI)homePageVal);
        }
        
        final Value uriVal = bindingSet.getValue(PoddSesameRealm.PARAM_USER_URI);
        if(uriVal != null)
        {
            result.setUri((URI)uriVal);
        }
        
        final Value titleVal = bindingSet.getValue(PoddSesameRealm.PARAM_USER_TITLE);
        if(titleVal != null)
        {
            result.setTitle(titleVal.stringValue());
        }
        
        final Value phoneVal = bindingSet.getValue(PoddSesameRealm.PARAM_USER_PHONE);
        if(phoneVal != null)
        {
            result.setPhone(phoneVal.stringValue());
        }
        
        final Value addressVal = bindingSet.getValue(PoddSesameRealm.PARAM_USER_ADDRESS);
        if(addressVal != null)
        {
            result.setAddress(addressVal.stringValue());
        }
        
        final Value positionVal = bindingSet.getValue(PoddSesameRealm.PARAM_USER_POSITION);
        if(positionVal != null)
        {
            result.setPosition(positionVal.stringValue());
        }
        
        return result;
    }
    
    protected Role buildRoleFromSparqlResult(final BindingSet bindingSet)
    {
        final URI roleUri = (URI)bindingSet.getValue(PoddSesameRealm.PARAM_ROLE);
        return PoddRoles.getRoleByUri(roleUri).getRole();
    }
    
    protected String buildSparqlQueryForObjectRoles(final String userIdentifier, final URI objectUri)
    {
        this.log.debug("Building SPARQL query for Roles between User and object URI");
        
        final StringBuilder query = new StringBuilder();
        
        query.append(" SELECT DISTINCT ?");
        query.append(PoddSesameRealm.PARAM_ROLE);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_IDENTIFIER);
        
        query.append(" WHERE ");
        query.append(" { ");
        
        final String roleMappingVar = " ?mapping ";
        
        query.append(roleMappingVar);
        query.append(RenderUtils.getSPARQLQueryString(SesameRealmConstants.OAS_ROLEMAPPEDUSER));
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_IDENTIFIER);
        query.append(" . ");
        
        query.append(roleMappingVar);
        query.append(RenderUtils.getSPARQLQueryString(SesameRealmConstants.OAS_ROLEMAPPEDROLE));
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_ROLE);
        query.append(" . ");
        
        query.append(roleMappingVar);
        query.append(RenderUtils.getSPARQLQueryString(PoddRdfConstants.PODD_ROLEMAPPEDOBJECT));
        query.append(" ?object . ");
        
        if(userIdentifier != null)
        {
            query.append(" FILTER ( ?userIdentifier IN (");
            query.append("\"" + RenderUtils.escape(userIdentifier) + "\"");
            query.append(") ) ");
        }
        
        query.append(" FILTER ( ?object IN (");
        query.append(RenderUtils.getSPARQLQueryString(objectUri));
        query.append(") ) ");
        
        query.append(" } ");
        
        this.log.debug("roles query: {}", query);
        
        return query.toString();
    }
    
    protected String buildSparqlQueryForRolesWithObjects(final String userIdentifier)
    {
        this.log.debug("Building SPARQL query for Roles and object URIs of a User");
        
        final StringBuilder query = new StringBuilder();
        
        query.append(" SELECT ?");
        query.append(PoddSesameRealm.PARAM_ROLE);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_OBJECT_URI);
        query.append(" WHERE ");
        query.append(" { ");
        
        final String roleMappingVar = " ?mapping ";
        
        query.append(roleMappingVar);
        query.append(" <" + SesameRealmConstants.OAS_ROLEMAPPEDUSER + "> ");
        query.append(" \"");
        query.append(RenderUtils.escape(userIdentifier));
        query.append("\" . ");
        
        query.append(roleMappingVar);
        query.append(" <" + SesameRealmConstants.OAS_ROLEMAPPEDROLE + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_ROLE);
        query.append(" . ");
        
        query.append(" OPTIONAL{ ");
        query.append(roleMappingVar);
        query.append(" <" + PoddRdfConstants.PODD_ROLEMAPPEDOBJECT + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_OBJECT_URI);
        query.append(" . } ");
        
        query.append(" } ");
        
        this.log.debug("roles query: {}", query);
        
        return query.toString();
    }
    
    protected String buildSparqlQueryToFindUser(final String userIdentifier, final boolean findAllUsers)
    {
        this.log.debug("Building SPARQL query");
        
        final StringBuilder query = new StringBuilder();
        
        query.append(" SELECT ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_IDENTIFIER);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_SECRET);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_FIRSTNAME);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_LASTNAME);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_EMAIL);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_STATUS);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ORGANIZATION);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ORCID);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_HOMEPAGE);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_TITLE);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_PHONE);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ADDRESS);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_POSITION);
        
        query.append(" WHERE ");
        query.append(" { ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" a <" + SesameRealmConstants.OAS_USER + "> . ");
        
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERIDENTIFIER + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_IDENTIFIER);
        query.append(" . ");
        
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_STATUS + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_STATUS);
        query.append(" . ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERSECRET + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_SECRET);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_ORCID + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ORCID);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_HOMEPAGE + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_HOMEPAGE);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_ORGANIZATION + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ORGANIZATION);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERFIRSTNAME + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_FIRSTNAME);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERLASTNAME + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_LASTNAME);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USEREMAIL + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_EMAIL);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_TITLE + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_TITLE);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_PHONE + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_PHONE);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_ADDRESS + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ADDRESS);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_POSITION + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_POSITION);
        query.append(" . } ");
        
        if(!findAllUsers)
        {
            query.append("   FILTER(str(?userIdentifier) = \"" + RenderUtils.escape(userIdentifier) + "\") ");
        }
        
        query.append(" } ");
        
        final String queryString = query.toString();
        
        this.log.debug("buildSparqlQueryToFindUser: query={}", queryString);
        
        return queryString;
    }
    
    protected String buildSparqlQueryToGetUserByStatus(final PoddUserStatus status, final String orderByField,
            final boolean isDescending, final int limit, final int offset)
    {
        this.log.debug("Building SPARQL query");
        
        final StringBuilder query = new StringBuilder();
        
        query.append(" SELECT ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_IDENTIFIER);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_SECRET);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_FIRSTNAME);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_LASTNAME);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_EMAIL);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_STATUS);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ORGANIZATION);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ORCID);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_HOMEPAGE);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_TITLE);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_PHONE);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ADDRESS);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_POSITION);
        
        query.append(" WHERE ");
        query.append(" { ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" a <" + SesameRealmConstants.OAS_USER + "> . ");
        
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERIDENTIFIER + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_IDENTIFIER);
        query.append(" . ");
        
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERSECRET + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_SECRET);
        query.append(" . ");
        
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_STATUS + "> ");
        query.append(" <" + status.getURI() + "> ");
        query.append(" . ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_HOMEPAGE + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_HOMEPAGE);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_ORGANIZATION + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ORGANIZATION);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERFIRSTNAME + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_FIRSTNAME);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERLASTNAME + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_LASTNAME);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USEREMAIL + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_EMAIL);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_TITLE + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_TITLE);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_PHONE + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_PHONE);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_ADDRESS + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ADDRESS);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_POSITION + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_POSITION);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_ORCID + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ORCID);
        query.append(" . } ");
        
        query.append(" } ");
        
        if(isDescending)
        {
            query.append(" ORDER BY DESC(" + orderByField + ") ");
        }
        else
        {
            query.append(" ORDER BY " + orderByField);
        }
        query.append(" LIMIT " + limit);
        query.append(" OFFSET " + offset);
        
        return query.toString();
    }
    
    protected String buildSparqlQueryToSearchUsers(final PoddUserStatus status, final String orderByField,
            final boolean isDescending, final int limit, final int offset)
    {
        this.log.debug("Building SPARQL query");
        
        final StringBuilder query = new StringBuilder();
        
        query.append(" SELECT ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_IDENTIFIER);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_SECRET);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_FIRSTNAME);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_LASTNAME);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_EMAIL);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_STATUS);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ORGANIZATION);
        
        query.append(" WHERE ");
        query.append(" { ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" a <" + SesameRealmConstants.OAS_USER + "> . ");
        
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERIDENTIFIER + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_IDENTIFIER);
        query.append(" . ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_ORGANIZATION + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ORGANIZATION);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERFIRSTNAME + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_FIRSTNAME);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERLASTNAME + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_LASTNAME);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USEREMAIL + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_EMAIL);
        query.append(" . } ");
        
        // return a dummy password
        query.append(" VALUES ?" + PoddSesameRealm.PARAM_USER_SECRET + " { \"not_available\" } . ");
        
        // filter by Status if provided
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_STATUS + "> ");
        if(status == null)
        {
            query.append(" ?");
            query.append(PoddSesameRealm.PARAM_USER_STATUS);
        }
        else
        {
            query.append(" <" + status.getURI() + "> ");
        }
        query.append(" . ");
        
        // concatenate identifier, first and last names for searching
        query.append(" BIND(CONCAT(");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_FIRSTNAME);
        query.append(" , \" \" , ?");
        query.append(PoddSesameRealm.PARAM_USER_LASTNAME);
        query.append(" , \" \" , ?");
        query.append(PoddSesameRealm.PARAM_USER_IDENTIFIER);
        query.append(" ) AS ?name)");
        
        // filter for "searchTerm" in ?name
        query.append(" FILTER( ");
        query.append(" CONTAINS( LCASE(?name) , LCASE(?" + PoddSesameRealm.PARAM_SEARCH_TERM + ") ) ");
        query.append(") ");
        
        query.append(" } ");
        
        if(isDescending)
        {
            query.append(" ORDER BY DESC(" + orderByField + ") ");
        }
        else
        {
            query.append(" ORDER BY " + orderByField);
        }
        
        if(limit > -1)
        {
            query.append(" LIMIT " + limit);
        }
        
        if(offset > 0)
        {
            query.append(" OFFSET " + offset);
        }
        
        return query.toString();
    }
    
    private Group createGroupForStatements(final List<Statement> nextGroupStatements)
    {
        final Group nextGroup = new Group();
        
        for(final Statement nextStatement : nextGroupStatements)
        {
            if(nextStatement.getPredicate().equals(SesameRealmConstants.OAS_GROUPNAME))
            {
                nextGroup.setName(nextStatement.getObject().stringValue());
            }
            else if(nextStatement.getPredicate().equals(SesameRealmConstants.OAS_GROUPDESCRIPTION))
            {
                nextGroup.setDescription(nextStatement.getObject().stringValue());
            }
            else if(nextStatement.getPredicate().equals(SesameRealmConstants.OAS_GROUPINHERITINGROLES))
            {
                nextGroup.setInheritingRoles(((Literal)nextStatement.getObject()).booleanValue());
            }
            else if(nextStatement.getPredicate().equals(SesameRealmConstants.OAS_GROUPMEMBERUSER))
            {
                nextGroup.getMemberUsers().add(this.findUser(nextStatement.getObject().stringValue()));
            }
            else if(nextStatement.getPredicate().equals(RDF.TYPE)
                    && (nextStatement.getObject().equals(SesameRealmConstants.OAS_GROUP) || nextStatement.getObject()
                            .equals(SesameRealmConstants.OAS_ROOTGROUP)))
            {
                this.log.trace("Found rdf type statement for group: {}", nextStatement);
            }
            else
            {
                this.log.debug("Found unrecognised statement parsing group: {}", nextStatement);
            }
        }
        
        return nextGroup;
    }
    
    private Group createGroupHierarchy(final Group parentGroup, final RepositoryConnection conn, final URI nextGroupUri)
    {
        try
        {
            // get the statements for the nextGroupUri
            final List<Statement> nextRootGroupStatements =
                    Iterations.asList(conn.getStatements(nextGroupUri, null, null, true, this.getContexts()));
            // create the group
            final Group newGroup = this.createGroupForStatements(nextRootGroupStatements);
            
            if(parentGroup != null)
            {
                // add the group as a member group for the parent group
                parentGroup.getMemberGroups().add(newGroup);
            }
            
            // check if there are any member groups for this item
            if(conn.hasStatement(nextGroupUri, SesameRealmConstants.OAS_GROUPMEMBERGROUP, null, true,
                    this.getContexts()))
            {
                final List<Statement> nextMemberGroupStatements =
                        Iterations.asList(conn.getStatements(nextGroupUri, SesameRealmConstants.OAS_GROUPMEMBERGROUP,
                                null, true, this.getContexts()));
                
                for(final Statement nextMemberGroupStatement : nextMemberGroupStatements)
                {
                    if(nextMemberGroupStatement.getObject() instanceof URI)
                    {
                        // FIXME: Need to do cycle checking here to avoid infinite loops
                        
                        // recursively call addGroup to add children to newGroup
                        this.createGroupHierarchy(newGroup, conn, (URI)nextMemberGroupStatement.getObject());
                    }
                    else
                    {
                        this.log.error("Found member group reference that was not a URI: {}", nextMemberGroupStatement);
                    }
                }
            }
            
            return newGroup;
        }
        catch(final RepositoryException e)
        {
            this.log.error("Found error trying to examine member groups", e);
            
            throw new RuntimeException(e);
        }
        
    }
    
    public URI deleteUser(final User nextUser)
    {
        URI nextUserUUID = null;
        
        final PoddUser findUser = this.findUser(nextUser.getIdentifier());
        
        if(findUser == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No such user found");
        }
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repository.getConnection();
            conn.begin();
            
            final List<Statement> userIdentifierStatements =
                    Iterations.asList(conn.getStatements(null, SesameRealmConstants.OAS_USERIDENTIFIER,
                            this.vf.createLiteral(nextUser.getIdentifier()), true, this.getContexts()));
            
            if(!userIdentifierStatements.isEmpty())
            {
                for(final Statement nextUserIdentifierStatement : userIdentifierStatements)
                {
                    if(nextUserIdentifierStatement.getSubject() instanceof URI)
                    {
                        // retrieve the user URI to persist it with the new statements
                        // does not matter if this is overwritten multiple times if there were
                        // multiple users with this identifier in the database
                        nextUserUUID = (URI)nextUserIdentifierStatement.getSubject();
                    }
                    
                    final List<Statement> currentUserStatements =
                            Iterations.asList(conn.getStatements(nextUserIdentifierStatement.getSubject(), null, null,
                                    true, this.getContexts()));
                    
                    // remove all of the previously known statements
                    conn.remove(currentUserStatements, this.getContexts());
                }
            }
            
            conn.commit();
        }
        catch(final RepositoryException e)
        {
            this.log.error("Found repository exception while adding user", e);
            try
            {
                conn.rollback();
            }
            catch(final RepositoryException e1)
            {
                this.log.error("Found unexpected exception while rolling back repository connection after exception");
            }
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Found unexpected repository exception", e);
                }
            }
        }
        
        return nextUserUUID;
    }
    
    /**
     * Finds the set of groups where a given user is a member. Note that inheritable ancestors
     * groups are also returned.
     * 
     * @param user
     *            The member user.
     * @return The set of groups.
     */
    public Set<Group> findGroups(final PoddUser user)
    {
        return this.findGroups(user, true);
    }
    
    /**
     * Finds the set of groups where a given user is a member.
     * 
     * @param user
     *            The member user.
     * @param inheritOnly
     *            Indicates if only the ancestors groups that have their "inheritRoles" property
     *            enabled should be added.
     * @return The set of groups.
     */
    public Set<Group> findGroups(final PoddUser user, final boolean inheritOnly)
    {
        final Set<Group> result = new HashSet<Group>();
        Set<Group> stack;
        
        // Recursively find user groups
        for(final Group group : this.getRootGroups())
        {
            stack = new LinkedHashSet<Group>();
            this.addGroupsForUser(user, result, group, stack, inheritOnly);
        }
        
        return result;
    }
    
    /**
     * Finds the roles mapped to given user group.
     * 
     * @param userGroup
     *            The user group.
     * @return The roles found.
     */
    public Set<Role> findRoles(final Group userGroup)
    {
        final Set<Role> result = new HashSet<Role>();
        
        for(final RoleMapping mapping : this.getRoleMappings())
        {
            final Object source = mapping.getSource();
            
            if((userGroup != null) && userGroup.equals(source))
            {
                result.add(mapping.getTarget());
            }
        }
        
        return result;
    }
    
    /**
     * Finds the roles mapped to given user groups.
     * 
     * @param userGroups
     *            The user groups.
     * @return The roles found.
     */
    public Set<Role> findRoles(final Set<Group> userGroups)
    {
        final Set<Role> result = new HashSet<Role>();
        
        for(final RoleMapping mapping : this.getRoleMappings())
        {
            final Object source = mapping.getSource();
            
            if((userGroups != null) && userGroups.contains(source))
            {
                result.add(mapping.getTarget());
            }
        }
        
        return result;
    }
    
    /**
     * Finds the roles mapped to a given user.
     * 
     * @param user
     *            The user.
     * @return The roles found.
     */
    public Set<Role> findRoles(final User user)
    {
        final Set<Role> result = new HashSet<Role>();
        
        for(final RoleMapping mapping : this.getRoleMappings())
        {
            final Object source = mapping.getSource();
            
            if((user != null) && user.equals(source))
            {
                // TODO: Fix this hardcoding when Restlet implements equals for Role objects again
                final RestletUtilRole standardRole = this.getRoleByName(mapping.getTarget().getName());
                if(standardRole != null)
                {
                    result.add(standardRole.getRole());
                }
                else
                {
                    result.add(mapping.getTarget());
                }
            }
        }
        
        return result;
    }
    
    /**
     * Finds a user in the organization based on its identifier.
     * 
     * @param userIdentifier
     *            The identifier to match.
     * @return The matched user or null.
     */
    public PoddUser findUser(final String userIdentifier)
    {
        if(userIdentifier == null)
        {
            throw new NullPointerException("User identifier was null");
        }
        
        PoddUser result = null;
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repository.getConnection();
            
            final String query = this.buildSparqlQueryToFindUser(userIdentifier, false);
            
            this.log.debug("findUser: query={}", query);
            
            final TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            
            final TupleQueryResult queryResult = tupleQuery.evaluate();
            
            try
            {
                if(queryResult.hasNext())
                {
                    final BindingSet bindingSet = queryResult.next();
                    
                    result = this.buildRestletUserFromSparqlResult(userIdentifier, bindingSet);
                }
                else
                {
                    this.log.info("Could not find user with identifier, returning null: {}", userIdentifier);
                }
            }
            finally
            {
                queryResult.close();
            }
            
        }
        catch(final RepositoryException e)
        {
            throw new RuntimeException("Failure finding user in repository", e);
        }
        catch(final MalformedQueryException e)
        {
            throw new RuntimeException("Failure finding user in repository", e);
        }
        catch(final QueryEvaluationException e)
        {
            throw new RuntimeException("Failure finding user in repository", e);
        }
        finally
        {
            try
            {
                conn.close();
            }
            catch(final RepositoryException e)
            {
                this.log.error("Failure to close connection", e);
            }
        }
        
        return result;
    }
    
    public URI[] getContexts()
    {
        return this.userManagerContexts;
    }
    
    public Repository getRepository()
    {
        return this.repository;
    }
    
    /**
     * @param role
     * @return
     */
    protected RestletUtilRole getRoleByName(final String name)
    {
        final RestletUtilRole oasRole = PoddRoles.getRoleByName(name);
        return oasRole;
    }
    
    /**
     * @param uri
     * @return
     */
    protected RestletUtilRole getRoleByUri(final URI uri)
    {
        final RestletUtilRole nextStandardRole = PoddRoles.getRoleByUri(uri);
        return nextStandardRole;
    }
    
    private List<RoleMapping> getRoleMappings()
    {
        final List<RoleMapping> results = new ArrayList<RoleMapping>();
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repository.getConnection();
            
            final RepositoryResult<Statement> typeStatements =
                    conn.getStatements(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING, true, this.getContexts());
            
            try
            {
                // We iterate through this gradually to reduce the load as the size of this
                // collection will grow with users
                while(typeStatements.hasNext())
                {
                    final Statement next = typeStatements.next();
                    
                    if(next.getSubject() instanceof URI)
                    {
                        final URI nextRoleMappingUri = (URI)next.getSubject();
                        
                        final RoleMapping nextRoleMapping = new RoleMapping();
                        
                        // dump all of these statements into a list as the size will be relatively
                        // constant and small for all scenarios
                        final List<Statement> nextRoleMappingStatements =
                                Iterations.asList(conn.getStatements(nextRoleMappingUri, null, null, true,
                                        this.getContexts()));
                        
                        for(final Statement nextRoleMappingStatement : nextRoleMappingStatements)
                        {
                            if(nextRoleMappingStatement.getPredicate().equals(SesameRealmConstants.OAS_ROLEMAPPEDROLE))
                            {
                                if(nextRoleMappingStatement.getObject() instanceof URI)
                                {
                                    // XXX: When Restlet allows custom .equals for Role, switch to
                                    // avoid only using
                                    // StandardOASRoles here, until then we have no easy way of
                                    // matching roles out of the
                                    // repository to objects
                                    
                                    final RestletUtilRole nextStandardRole =
                                            this.getRoleByUri((URI)nextRoleMappingStatement.getObject());
                                    
                                    if(nextStandardRole == null)
                                    {
                                        this.log.warn(
                                                "Failed to find an in-memory role for the given role mapped role: {}",
                                                nextRoleMappingStatement);
                                    }
                                    else
                                    {
                                        nextRoleMapping.setTarget(nextStandardRole.getRole());
                                    }
                                }
                                else
                                {
                                    this.log.warn("Found a non-URI as the target for a role mapped role statement: {}",
                                            nextRoleMappingStatement);
                                }
                            }
                            else if(nextRoleMappingStatement.getPredicate().equals(
                                    SesameRealmConstants.OAS_ROLEMAPPEDGROUP))
                            {
                                if(nextRoleMappingStatement.getObject() instanceof Literal)
                                {
                                    final String nextGroupName =
                                            ((Literal)nextRoleMappingStatement.getObject()).stringValue();
                                    
                                    // TODO: Support nested groups here
                                    
                                    final List<Group> rootGroups = this.getRootGroups();
                                    
                                    for(final Group nextRootGroup : rootGroups)
                                    {
                                        if(nextRootGroup.getName().equals(nextGroupName))
                                        {
                                            nextRoleMapping.setSource(nextRootGroup);
                                        }
                                        else
                                        {
                                            // TODO: need to check further for nested groups
                                        }
                                    }
                                }
                                else
                                {
                                    this.log.warn(
                                            "Found a non-Literal as the target for a role mapped group statement: {}",
                                            nextRoleMappingStatement);
                                }
                            }
                            else if(nextRoleMappingStatement.getPredicate().equals(
                                    SesameRealmConstants.OAS_ROLEMAPPEDUSER))
                            {
                                if(nextRoleMappingStatement.getObject() instanceof Literal)
                                {
                                    final String nextUserIdentifier =
                                            ((Literal)nextRoleMappingStatement.getObject()).stringValue();
                                    
                                    final PoddUser nextUser = this.findUser(nextUserIdentifier);
                                    
                                    if(nextUser != null)
                                    {
                                        nextRoleMapping.setSource(nextUser);
                                    }
                                    else
                                    {
                                        this.log.info(
                                                "Failed to find a role mapped user internally for the given user identifier: {}",
                                                nextRoleMappingStatement);
                                    }
                                }
                                else
                                {
                                    this.log.warn(
                                            "Found a non-Literal as the target for a role mapped group statement: {}",
                                            nextRoleMappingStatement);
                                }
                            }
                            else if(nextRoleMappingStatement.getPredicate().equals(RDF.TYPE))
                            {
                                this.log.trace("Found rdf:type statement for role mapping: {}",
                                        nextRoleMappingStatement);
                            }
                            else
                            {
                                this.log.debug("Found unknown statement for role mapping: {}", nextRoleMappingStatement);
                            }
                        }
                        
                        // verify that the source and target were both setup before adding this
                        // mapping to results
                        if(nextRoleMapping.getSource() != null && nextRoleMapping.getTarget() != null)
                        {
                            results.add(nextRoleMapping);
                        }
                        else
                        {
                            this.log.info("Not adding incomplete role mapping to results: uri={}, partialMapping={}",
                                    nextRoleMappingUri, nextRoleMapping);
                        }
                    }
                    else
                    {
                        this.log.info("Found non-URI for role mapping, ignoring this role mapping: {}", next);
                    }
                }
            }
            finally
            {
                typeStatements.close();
            }
        }
        catch(final RepositoryException e)
        {
            this.log.error("Found exception while retrieving role mappings", e);
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Found exception closing repository connection", e);
                }
            }
        }
        
        return results;
    }
    
    public Collection<Role> getRolesForObject(final User user, final URI objectUri)
    {
        final Set<Role> results = new HashSet<Role>();
        
        final Collection<Collection<Role>> allResults =
                this.getRolesForObjectAlternate(user.getIdentifier(), objectUri).values();
        
        for(final Collection<Role> nextResult : allResults)
        {
            results.addAll(nextResult);
        }
        
        return results;
    }
    
    public Map<String, Collection<Role>> getRolesForObjectAlternate(final String userIdentifier, final URI objectUri)
    {
        final ConcurrentMap<String, Collection<Role>> roleCollection =
                new ConcurrentHashMap<String, Collection<Role>>();
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.getRepository().getConnection();
            
            final String query = this.buildSparqlQueryForObjectRoles(userIdentifier, objectUri);
            
            if(this.log.isDebugEnabled())
            {
                this.log.debug("getCommonRolesForObjects: query={}", query);
            }
            
            final TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            
            final TupleQueryResult queryResult = tupleQuery.evaluate();
            
            try
            {
                if(!queryResult.hasNext())
                {
                    this.log.warn("Could not find role with mappings for user: {}", userIdentifier);
                }
                
                while(queryResult.hasNext())
                {
                    final BindingSet bindingSet = queryResult.next();
                    
                    final Role role = this.buildRoleFromSparqlResult(bindingSet);
                    
                    if(!bindingSet.hasBinding(PoddSesameRealm.PARAM_USER_IDENTIFIER))
                    {
                        throw new RuntimeException("Query did not bind a user to the role : " + bindingSet.toString());
                    }
                    
                    Collection<Role> nextRoles = new HashSet<Role>();
                    final Collection<Role> putIfAbsent =
                            roleCollection.putIfAbsent(bindingSet.getBinding(PoddSesameRealm.PARAM_USER_IDENTIFIER)
                                    .getValue().stringValue(), nextRoles);
                    if(putIfAbsent != null)
                    {
                        nextRoles = putIfAbsent;
                    }
                    nextRoles.add(role);
                }
            }
            finally
            {
                queryResult.close();
            }
            
        }
        catch(final RepositoryException | MalformedQueryException | QueryEvaluationException e)
        {
            throw new RuntimeException("Failure finding user in repository", e);
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Failure to close connection", e);
                }
            }
        }
        
        return roleCollection;
    }
    
    public Collection<Entry<Role, URI>> getRolesWithObjectMappings(final User user)
    {
        if(user == null)
        {
            throw new NullPointerException("User was null");
        }
        
        // final Map<Role, URI> roleMap = new HashMap<Role, URI>();
        final Collection<Entry<Role, URI>> roleCollection = new HashSet<Entry<Role, URI>>();
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.getRepository().getConnection();
            
            final String query = this.buildSparqlQueryForRolesWithObjects(user.getIdentifier());
            
            if(this.log.isDebugEnabled())
            {
                this.log.debug("getRolesAndObjectsForUser: query={}", query);
            }
            
            final TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            
            final TupleQueryResult queryResult = tupleQuery.evaluate();
            
            try
            {
                if(!queryResult.hasNext())
                {
                    this.log.warn("Could not find role with mappings for user: {}", user.getIdentifier());
                }
                
                while(queryResult.hasNext())
                {
                    final Entry<Role, URI> roleEntry = this.buildMapEntryFromSparqlResult(queryResult.next());
                    // roleMap.put(roleEntry.getKey(), roleEntry.getValue());
                    roleCollection.add(roleEntry);
                }
            }
            finally
            {
                queryResult.close();
            }
            
        }
        catch(final RepositoryException | MalformedQueryException | QueryEvaluationException e)
        {
            throw new RuntimeException("Failure finding user in repository", e);
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Failure to close connection", e);
                }
            }
        }
        
        return roleCollection;
    }
    
    /**
     * Returns the modifiable list of root groups.
     * 
     * @return The modifiable list of root groups.
     */
    public List<Group> getRootGroups()
    {
        List<Group> results = this.cachedRootGroups;
        
        if(results == null)
        {
            synchronized(this)
            {
                results = this.cachedRootGroups;
                if(results == null)
                {
                    results = new ArrayList<Group>();
                    
                    RepositoryConnection conn = null;
                    
                    try
                    {
                        conn = this.getRepository().getConnection();
                        
                        final RepositoryResult<Statement> rootGroupStatements =
                                conn.getStatements(null, RDF.TYPE, SesameRealmConstants.OAS_ROOTGROUP, true,
                                        this.getContexts());
                        
                        try
                        {
                            while(rootGroupStatements.hasNext())
                            {
                                final Statement nextRootGroupStatement = rootGroupStatements.next();
                                
                                if(nextRootGroupStatement.getSubject() instanceof URI)
                                {
                                    final URI nextRootGroupUri = (URI)nextRootGroupStatement.getSubject();
                                    // add the group recursively to enable member groups to be added
                                    // recursively
                                    results.add(this.createGroupHierarchy(null, conn, nextRootGroupUri));
                                }
                                else
                                {
                                    this.log.warn("Not including root group as it did not have a URI identifier: {}",
                                            nextRootGroupStatement);
                                }
                            }
                        }
                        finally
                        {
                            rootGroupStatements.close();
                        }
                    }
                    catch(final RepositoryException e)
                    {
                        this.log.error("Found exception while trying to get root groups", e);
                    }
                    finally
                    {
                        try
                        {
                            if(conn != null)
                            {
                                conn.close();
                            }
                        }
                        catch(final RepositoryException e)
                        {
                            this.log.error("Found unexpected exception while closing repository connection", e);
                        }
                    }
                    
                    this.cachedRootGroups = results;
                }
            }
        }
        
        return results;
        
        // throw new RuntimeException(
        // "TODO: Implement code not to rely on getting a complete list of groups where possible");
        // return this.rootGroups;
    }
    
    private Dataset getSesameDataset()
    {
        final DatasetImpl result = new DatasetImpl();
        
        result.setDefaultInsertGraph(this.getContexts()[0]);
        
        for(final URI nextContext : this.getContexts())
        {
            result.addDefaultGraph(nextContext);
            result.addDefaultRemoveGraph(nextContext);
            result.addNamedGraph(nextContext);
        }
        
        return result;
    }
    
    public List<PoddUser> getUserByStatus(final PoddUserStatus status, final boolean isDescending, final int limit,
            final int offset)
    {
        final List<PoddUser> result = new ArrayList<PoddUser>();
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.getRepository().getConnection();
            
            final String orderBy = "?" + PoddSesameRealm.PARAM_USER_IDENTIFIER;
            final String query = this.buildSparqlQueryToGetUserByStatus(status, orderBy, isDescending, limit, offset);
            
            this.log.debug("getUserByStatus: query={}", query);
            
            final TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            
            final TupleQueryResult queryResult = tupleQuery.evaluate();
            
            try
            {
                while(queryResult.hasNext())
                {
                    final BindingSet bindingSet = queryResult.next();
                    final Binding binding = bindingSet.getBinding("userIdentifier");
                    
                    result.add(this.buildRestletUserFromSparqlResult(binding.getValue().stringValue(), bindingSet));
                }
            }
            finally
            {
                queryResult.close();
            }
            
        }
        catch(final RepositoryException e)
        {
            throw new RuntimeException("Failure finding user in repository", e);
        }
        catch(final MalformedQueryException e)
        {
            throw new RuntimeException("Failure finding user in repository", e);
        }
        catch(final QueryEvaluationException e)
        {
            throw new RuntimeException("Failure finding user in repository", e);
        }
        finally
        {
            try
            {
                conn.close();
            }
            catch(final RepositoryException e)
            {
                this.log.error("Failure to close connection", e);
            }
        }
        
        return result;
    }
    
    public String getUsername(final URI userURI) throws RepositoryException
    {
        RepositoryConnection conn = null;
        try
        {
            conn = this.repository.getConnection();
            
            final Model result =
                    new LinkedHashModel(Iterations.asList(conn.getStatements(userURI,
                            SesameRealmConstants.OAS_USERIDENTIFIER, null, true, this.getContexts())));
            
            for(final Value nextUsername : result.objects())
            {
                if(nextUsername instanceof Literal)
                {
                    return ((Literal)nextUsername).getLabel();
                }
            }
            
            return null;
        }
        catch(final RepositoryException e)
        {
            this.log.error("Found repository exception while adding user", e);
            try
            {
                conn.rollback();
            }
            catch(final RepositoryException e1)
            {
                this.log.error("Found unexpected exception while rolling back repository connection after exception");
            }
            
            throw new RuntimeException(e);
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Found unexpected repository exception", e);
                    throw new RuntimeException(e);
                }
            }
        }
        
    }
    
    /**
     * Returns an unmodifiable list of users.
     * 
     * @return An unmodifiable list of users.
     */
    public List<PoddUser> getUsers()
    {
        final List<PoddUser> result = new ArrayList<PoddUser>();
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.repository.getConnection();
            
            final String query = this.buildSparqlQueryToFindUser(null, true);
            
            this.log.debug("findUser: query={}", query);
            
            final TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            
            final TupleQueryResult queryResult = tupleQuery.evaluate();
            
            try
            {
                while(queryResult.hasNext())
                {
                    final BindingSet bindingSet = queryResult.next();
                    final Binding binding = bindingSet.getBinding("userIdentifier");
                    
                    result.add(this.buildRestletUserFromSparqlResult(binding.getValue().stringValue(), bindingSet));
                }
            }
            finally
            {
                queryResult.close();
            }
            
        }
        catch(final RepositoryException e)
        {
            throw new RuntimeException("Failure finding user in repository", e);
        }
        catch(final MalformedQueryException e)
        {
            throw new RuntimeException("Failure finding user in repository", e);
        }
        catch(final QueryEvaluationException e)
        {
            throw new RuntimeException("Failure finding user in repository", e);
        }
        finally
        {
            try
            {
                conn.close();
            }
            catch(final RepositoryException e)
            {
                this.log.error("Failure to close connection", e);
            }
        }
        
        return Collections.unmodifiableList(result);
    }
    
    public Map<String, PoddUser> getUsersMapByIdentifier()
    {
        final ConcurrentMap<String, PoddUser> result = new ConcurrentHashMap<String, PoddUser>();
        
        for(final PoddUser nextUser : this.getUsers())
        {
            final PoddUser putIfAbsent = result.putIfAbsent(nextUser.getIdentifier(), nextUser);
            
            if(putIfAbsent != null)
            {
                this.log.error("Found duplicate user identifier for different users: {} {}", putIfAbsent, nextUser);
            }
        }
        
        return result;
    }
    
    public Map<URI, PoddUser> getUsersMapByURI()
    {
        final ConcurrentMap<URI, PoddUser> result = new ConcurrentHashMap<URI, PoddUser>();
        
        for(final PoddUser nextUser : this.getUsers())
        {
            final PoddUser putIfAbsent = result.putIfAbsent(nextUser.getUri(), nextUser);
            
            if(putIfAbsent != null)
            {
                this.log.error("Found duplicate user URI for different users: {} {}", putIfAbsent, nextUser);
            }
        }
        
        return result;
    }
    
    public URI getUserUri(final String userIdentifier) throws RepositoryException
    {
        RepositoryConnection conn = null;
        try
        {
            conn = this.repository.getConnection();
            
            final Model result =
                    new LinkedHashModel(Iterations.asList(conn.getStatements(null,
                            SesameRealmConstants.OAS_USERIDENTIFIER, this.vf.createLiteral(userIdentifier), true,
                            this.getContexts())));
            
            for(final Resource nextUri : result.subjects())
            {
                if(nextUri instanceof URI)
                {
                    return (URI)nextUri;
                }
            }
            
            return null;
        }
        catch(final RepositoryException e)
        {
            this.log.error("Found repository exception while adding user", e);
            try
            {
                conn.rollback();
            }
            catch(final RepositoryException e1)
            {
                this.log.error("Found unexpected exception while rolling back repository connection after exception");
            }
            
            throw new RuntimeException(e);
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Found unexpected repository exception", e);
                    throw new RuntimeException(e);
                }
            }
        }
        
    }
    
    /**
     * Maps a group defined in a component to a role defined in the application.
     * 
     * @param group
     *            The source group.
     * @param role
     *            The target role.
     */
    public void map(final Group group, final Role role)
    {
        try
        {
            this.addRoleMapping(new RoleMapping(group, role));
        }
        catch(final RepositoryException e)
        {
            throw new RuntimeException("Found unexpected exception while adding role mapping", e);
        }
    }
    
    /**
     * Maps a user defined in a component to a role defined in the application.
     * 
     * @param user
     *            The source user.
     * @param role
     *            The target role.
     */
    public void map(final PoddUser user, final Role role)
    {
        try
        {
            this.addRoleMapping(new RoleMapping(user, role));
        }
        catch(final RepositoryException e)
        {
            throw new RuntimeException("Found unexpected exception while adding role mapping", e);
        }
    }
    
    public void map(final User user, final Role role, final URI optionalObjectUri)
    {
        RepositoryConnection conn = null;
        try
        {
            conn = this.getRepository().getConnection();
            conn.begin();
            
            final URI nextRoleMappingUUID = this.vf.createURI("urn:oas:rolemapping:", UUID.randomUUID().toString());
            
            conn.add(this.vf.createStatement(nextRoleMappingUUID, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING),
                    this.getContexts());
            
            conn.add(this.vf.createStatement(nextRoleMappingUUID, SesameRealmConstants.OAS_ROLEMAPPEDROLE, this
                    .getRoleByName(role.getName()).getURI()), this.getContexts());
            
            conn.add(
                    this.vf.createStatement(nextRoleMappingUUID, SesameRealmConstants.OAS_ROLEMAPPEDUSER,
                            this.vf.createLiteral(user.getIdentifier())), this.getContexts());
            
            if(optionalObjectUri != null)
            {
                conn.add(this.vf.createStatement(nextRoleMappingUUID, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT,
                        optionalObjectUri), this.getContexts());
            }
            
            conn.commit();
        }
        catch(final RepositoryException e)
        {
            this.log.error("Found exception while adding role mapping", e);
            if(conn != null)
            {
                try
                {
                    conn.rollback();
                }
                catch(final RepositoryException e1)
                {
                    // throw a RuntimeException to be consistent with the behaviour of
                    // super.map(user, role)
                    throw new RuntimeException("Found unexpected exception while adding role mapping", e);
                }
            }
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Found exception closing repository connection", e);
                }
            }
        }
    }
    
    public List<PoddUser> searchUser(String searchTerm, final PoddUserStatus status, final boolean isDescending,
            final int limit, final int offset)
    {
        final List<PoddUser> result = new ArrayList<PoddUser>();
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.getRepository().getConnection();
            
            final String orderBy = "?" + PoddSesameRealm.PARAM_USER_IDENTIFIER;
            final String query = this.buildSparqlQueryToSearchUsers(status, orderBy, isDescending, limit, offset);
            
            this.log.debug("searchUser: query={}", query);
            
            final TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            
            if(searchTerm == null)
            {
                // could this lead to an inefficient sparql query?
                searchTerm = "";
            }
            tupleQuery.setBinding(PoddSesameRealm.PARAM_SEARCH_TERM, PoddRdfConstants.VF.createLiteral(searchTerm));
            
            final TupleQueryResult queryResult = tupleQuery.evaluate();
            
            try
            {
                while(queryResult.hasNext())
                {
                    final BindingSet bindingSet = queryResult.next();
                    final Binding binding = bindingSet.getBinding("userIdentifier");
                    
                    result.add(this.buildRestletUserFromSparqlResult(binding.getValue().stringValue(), bindingSet));
                }
            }
            finally
            {
                queryResult.close();
            }
        }
        catch(final RepositoryException | MalformedQueryException | QueryEvaluationException e)
        {
            throw new RuntimeException("Failure searching users in repository", e);
        }
        finally
        {
            try
            {
                conn.close();
            }
            catch(final RepositoryException e)
            {
                this.log.error("Failure to close connection", e);
            }
        }
        return result;
    }
    
    public void setContexts(final URI... contexts)
    {
        if(contexts.length == 0)
        {
            // for security and usability we insist that a named graph is provided
            throw new IllegalArgumentException(
                    "Cannot create an OasSesameRealm without specifying the contexts that are used to manage user data.");
        }
        this.userManagerContexts = contexts;
    }
    
    public void setRepository(final Repository repository)
    {
        this.repository = repository;
        if(this.repository != null)
        {
            this.vf = this.repository.getValueFactory();
        }
        else
        {
            this.vf = ValueFactoryImpl.getInstance();
        }
    }
    
    /**
     * Sets the modifiable list of root groups. This method clears the current list and adds all
     * entries in the parameter list.
     * 
     * @param rootGroups
     *            A list of root groups.
     */
    @Deprecated
    public void setRootGroups(final List<Group> rootGroups)
    {
        throw new RuntimeException("TODO: Implement me if necessary, or convert to add and remove methods");
        // synchronized(this.getRootGroups())
        // {
        // if(rootGroups != this.getRootGroups())
        // {
        // this.getRootGroups().clear();
        //
        // if(rootGroups != null)
        // {
        // this.getRootGroups().addAll(rootGroups);
        // }
        // }
        // }
    }
    
    /**
     * Stores the group, including a root group statement if rootGroup is true.
     * 
     * @param nextGroup
     * @param isRootGroup
     * @throws RepositoryException
     */
    private void storeGroup(final Group nextGroup, final RepositoryConnection conn, final boolean isRootGroup)
        throws RepositoryException
    {
        if(conn.hasStatement(null, SesameRealmConstants.OAS_GROUPNAME, this.vf.createLiteral(nextGroup.getName()),
                true, this.getContexts()))
        {
            // TODO: Create an update method
            throw new RuntimeException(
                    "A user with the given identifier already exists. Cannot add a new user with that identifier.");
        }
        
        final URI nextGroupUUID =
                this.vf.createURI("urn:oas:group:", nextGroup.getName() + ":" + UUID.randomUUID().toString());
        
        conn.add(this.vf.createStatement(nextGroupUUID, RDF.TYPE, SesameRealmConstants.OAS_GROUP), this.getContexts());
        
        if(isRootGroup)
        {
            conn.add(this.vf.createStatement(nextGroupUUID, RDF.TYPE, SesameRealmConstants.OAS_ROOTGROUP),
                    this.getContexts());
        }
        
        conn.add(
                this.vf.createStatement(nextGroupUUID, SesameRealmConstants.OAS_GROUPNAME,
                        this.vf.createLiteral(nextGroup.getName())), this.getContexts());
        conn.add(
                this.vf.createStatement(nextGroupUUID, SesameRealmConstants.OAS_GROUPDESCRIPTION,
                        this.vf.createLiteral(nextGroup.getDescription())), this.getContexts());
        conn.add(
                this.vf.createStatement(nextGroupUUID, SesameRealmConstants.OAS_GROUPINHERITINGROLES,
                        this.vf.createLiteral(nextGroup.isInheritingRoles())), this.getContexts());
        
        // only store users who cannot be found based on their identifier
        for(final User nextUser : nextGroup.getMemberUsers())
        {
            if(this.findUser(nextUser.getIdentifier()) == null)
            {
                final URI nextUserUri = this.addUser((PoddUser)nextUser);
            }
        }
        
        if(!nextGroup.getMemberGroups().isEmpty())
        {
            for(final Group nextMemberGroup : nextGroup.getMemberGroups())
            {
                // always set rootGroup parameter to false when recursing into member groups
                this.storeGroup(nextMemberGroup, conn, false);
            }
        }
        
    }
    
    /**
     * Unmaps a group defined in a component from a role defined in the application.
     * 
     * @param group
     *            The source group.
     * @param role
     *            The target role.
     */
    public void unmap(final Group group, final Role role)
    {
        this.unmap(role, SesameRealmConstants.OAS_ROLEMAPPEDGROUP, group.getName());
    }
    
    /**
     * Unmaps a user defined in a component from a role defined in the application.
     * 
     * @param user
     *            The source user.
     * @param role
     *            The target role.
     */
    public void unmap(final PoddUser user, final Role role)
    {
        this.unmap(role, SesameRealmConstants.OAS_ROLEMAPPEDUSER, user.getIdentifier());
    }
    
    public void unmap(final Role role, final URI mappingUri, final String identifier)
    {
        RepositoryConnection conn = null;
        try
        {
            conn = this.repository.getConnection();
            conn.begin();
            final StringBuilder query = new StringBuilder();
            
            final RestletUtilRole oasRole = this.getRoleByName(role.getName());
            
            if(oasRole == null)
            {
                throw new IllegalArgumentException("Did not recognise role as a standard OAS role" + role.getName());
            }
            
            query.append(" SELECT ?roleMappingUri ");
            query.append(" WHERE ");
            query.append(" { ");
            query.append("   ?roleMappingUri a <" + SesameRealmConstants.OAS_ROLEMAPPING + "> . ");
            query.append("   ?roleMappingUri <" + mappingUri + "> ?identifier . ");
            query.append("   ?roleMappingUri <" + SesameRealmConstants.OAS_ROLEMAPPEDROLE + "> ?roleUri . ");
            query.append("   FILTER(str(?identifier) = \"" + NTriplesUtil.escapeString(identifier) + "\") ");
            query.append("   FILTER(?roleUri = <" + oasRole.getURI() + "> ) ");
            query.append(" } ");
            
            if(this.log.isDebugEnabled())
            {
                this.log.debug("findUser: query={}", query.toString());
            }
            
            final TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query.toString());
            tupleQuery.setDataset(this.getSesameDataset());
            
            final TupleQueryResult queryResult = tupleQuery.evaluate();
            
            try
            {
                if(!queryResult.hasNext())
                {
                    this.log.info("Could not find any role mappings to remove for this role: {} and this target: {}",
                            role, identifier);
                }
                
                while(queryResult.hasNext())
                {
                    final BindingSet bindingSet = queryResult.next();
                    
                    if(queryResult.hasNext())
                    {
                        this.log.warn(
                                "Found duplicate roleMapping, will remove all mappings for this role: {} and this target: {}",
                                role, identifier);
                    }
                    
                    final Value roleMappingUri = bindingSet.getValue("roleMappingUri");
                    
                    if(roleMappingUri instanceof Resource)
                    {
                        conn.remove((Resource)roleMappingUri, null, null, this.getContexts());
                    }
                    else
                    {
                        this.log.warn("This should not happen while RDF only allows URIs and blank nodes in the subject position of triples");
                    }
                }
            }
            finally
            {
                queryResult.close();
            }
            conn.commit();
        }
        catch(final RepositoryException e)
        {
            try
            {
                conn.rollback();
            }
            catch(final RepositoryException e1)
            {
                this.log.error("Repository Exception while rolling back connection");
            }
            throw new RuntimeException("Failure finding user in repository", e);
        }
        catch(final MalformedQueryException e)
        {
            try
            {
                conn.rollback();
            }
            catch(final RepositoryException e1)
            {
                this.log.error("Repository Exception while rolling back connection");
            }
            throw new RuntimeException("Failure finding user in repository", e);
        }
        catch(final QueryEvaluationException e)
        {
            try
            {
                conn.rollback();
            }
            catch(final RepositoryException e1)
            {
                this.log.error("Repository Exception while rolling back connection");
            }
            throw new RuntimeException("Failure finding user in repository", e);
        }
        finally
        {
            try
            {
                conn.close();
            }
            catch(final RepositoryException e)
            {
                this.log.error("Failure to close connection", e);
            }
        }
        
    }
    
    public void unmap(final User user, final Role role, final URI optionalObjectUri)
    {
        // If they only want to unmap a role without an object, then use super implementation.
        if(optionalObjectUri == null)
        {
            this.unmap(role, SesameRealmConstants.OAS_ROLEMAPPEDUSER, user.getIdentifier());
        }
        // Do our object reliant code instead
        else
        {
            RepositoryConnection conn = null;
            try
            {
                conn = this.getRepository().getConnection();
                conn.begin();
                final StringBuilder query = new StringBuilder();
                
                final RestletUtilRole oasRole = this.getRoleByName(role.getName());
                
                if(oasRole == null)
                {
                    throw new IllegalArgumentException("Did not recognise role as a standard OAS role" + role.getName());
                }
                
                query.append(" SELECT ?roleMappingUri ");
                query.append(" WHERE ");
                query.append(" { ");
                query.append("   ?roleMappingUri a <" + SesameRealmConstants.OAS_ROLEMAPPING + "> . ");
                query.append("   ?roleMappingUri <" + SesameRealmConstants.OAS_ROLEMAPPEDUSER + "> ?identifier . ");
                query.append("   ?roleMappingUri <" + SesameRealmConstants.OAS_ROLEMAPPEDROLE + "> ?role . ");
                query.append("   ?roleMappingUri <" + PoddRdfConstants.PODD_ROLEMAPPEDOBJECT + "> ?object . ");
                query.append("   FILTER(str(?identifier) = \"" + NTriplesUtil.escapeString(user.getIdentifier())
                        + "\") ");
                query.append("   FILTER(?role = <" + oasRole.getURI() + "> ) ");
                query.append("   FILTER(?object = <" + optionalObjectUri + "> ) ");
                query.append(" } ");
                
                final String queryString = query.toString();
                
                this.log.debug("findUser: query={}", queryString);
                
                final TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                tupleQuery.setDataset(this.getSesameDataset());
                
                final TupleQueryResult queryResult = tupleQuery.evaluate();
                
                try
                {
                    if(!queryResult.hasNext())
                    {
                        this.log.info(
                                "Could not find any role mappings to remove for this role: {}, object <{}>, and this target: {}",
                                role, optionalObjectUri, user.getIdentifier());
                    }
                    
                    while(queryResult.hasNext())
                    {
                        final BindingSet bindingSet = queryResult.next();
                        
                        if(queryResult.hasNext())
                        {
                            this.log.warn(
                                    "Found duplicate roleMapping, will remove all mappings for this role: {}, object <{}>, and this target: {}",
                                    role, optionalObjectUri, user.getIdentifier());
                        }
                        
                        final Value roleMappingUri = bindingSet.getValue("roleMappingUri");
                        
                        if(roleMappingUri instanceof Resource)
                        {
                            conn.remove((Resource)roleMappingUri, null, null, this.getContexts());
                        }
                        else
                        {
                            this.log.warn("This should not happen while RDF only allows URIs and blank nodes in the subject position of triples");
                        }
                    }
                }
                finally
                {
                    queryResult.close();
                }
                conn.commit();
            }
            catch(final RepositoryException e)
            {
                try
                {
                    conn.rollback();
                }
                catch(final RepositoryException e1)
                {
                    this.log.error("Repository Exception while rolling back connection");
                }
                throw new RuntimeException("Failure finding user in repository", e);
            }
            catch(final MalformedQueryException e)
            {
                try
                {
                    conn.rollback();
                }
                catch(final RepositoryException e1)
                {
                    this.log.error("Repository Exception while rolling back connection");
                }
                throw new RuntimeException("Failure finding user in repository", e);
            }
            catch(final QueryEvaluationException e)
            {
                try
                {
                    conn.rollback();
                }
                catch(final RepositoryException e1)
                {
                    this.log.error("Repository Exception while rolling back connection");
                }
                throw new RuntimeException("Failure finding user in repository", e);
            }
            finally
            {
                try
                {
                    conn.close();
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Failure to close connection", e);
                }
            }
        }
    }
    
    public URI updateUser(final PoddUser nextUser)
    {
        return this.addUser(nextUser, false);
    }
    
}