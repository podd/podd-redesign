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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.security.Enroler;
import org.restlet.security.Group;
import org.restlet.security.LocalVerifier;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.RestletUtilUser;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;

/**
 * Customises RestletUtilSesameRealm.java to use PODDUsers and PoddRoles.
 * 
 * @author kutila
 * 
 */
public class PoddSesameRealmImpl extends PoddSesameRealm
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
            final PoddUser user = (PoddUser)PoddSesameRealmImpl.this.findUser(clientInfo.getUser().getIdentifier());
            
            if(user != null)
            {
                // Find all the inherited groups of this user
                final Set<Group> userGroups = PoddSesameRealmImpl.this.findGroups(user);
                
                // Add roles specific to this user
                final Set<Role> userRoles = PoddSesameRealmImpl.this.findRoles(user);
                
                for(final Role role : userRoles)
                {
                    clientInfo.getRoles().add(role);
                }
                
                // Add roles common to group members
                final Set<Role> groupRoles = PoddSesameRealmImpl.this.findRoles(userGroups);
                
                for(final Role role : groupRoles)
                {
                    clientInfo.getRoles().add(role);
                }
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
            // casting is safe here as buildRestletUserFromSparqlResult() creates a PoddUser
            final PoddUser checkUser = (PoddUser)PoddSesameRealmImpl.this.findUser(identifier);
            
            if(checkUser == null)
            {
                PoddSesameRealmImpl.this.log.error("Cannot create a user for the given identifier: {}", identifier);
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
            final User user = PoddSesameRealmImpl.this.findUser(identifier);
            
            if(user != null)
            {
                result = user.getSecret();
            }
            
            return result;
        }
    }
    
    // ======================= end inner classes ==========================
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Constructor
     * 
     * @param repository
     * @param contexts
     */
    public PoddSesameRealmImpl(final Repository repository, final URI... contexts)
    {
        super(repository, contexts);
        
        // set PODD-specific Enroler and Verifier
        this.setEnroler(new DefaultPoddSesameRealmEnroler());
        this.setVerifier(new DefaultPoddSesameRealmVerifier());
    }
    
    @Override
    public URI addUser(final PoddUser nextUser)
    {
        return this.addUser(nextUser, true);
    }
    
    protected URI addUser(final PoddUser nextUser, final boolean isNew)
    {
        final RestletUtilUser oldUser = this.findUser(nextUser.getIdentifier());
        if(isNew && oldUser != null)
        {
            throw new RuntimeException("User already exists");
        }
        else if(!isNew && oldUser == null)
        {
            throw new RuntimeException("Could not modify User (does not exist)");
        }
        
        final URI nextUserUUID = super.addUser(nextUser);
        
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
    
    @Override
    protected Entry<Role, URI> buildMapEntryFromSparqlResult(final BindingSet bindingSet)
    {
        final URI roleUri = this.vf.createURI(bindingSet.getValue(PoddSesameRealm.PARAM_ROLE).stringValue());
        final Role role = PoddRoles.getRoleByUri(roleUri).getRole();
        
        URI objectUri = null;
        if(bindingSet.getValue(PoddSesameRealm.PARAM_OBJECT_URI) != null)
        {
            objectUri = this.vf.createURI(bindingSet.getValue(PoddSesameRealm.PARAM_OBJECT_URI).stringValue());
        }
        
        this.log.info("Building map entry: {}, <{}>", role.getName(), objectUri);
        
        return new AbstractMap.SimpleEntry<Role, URI>(role, objectUri);
    }
    
    @Override
    protected PoddUser buildRestletUserFromSparqlResult(final String userIdentifier, final BindingSet bindingSet)
    {
        this.log.debug("Building RestletUtilUser from SPARQL results");
        
        final PoddUser result =
                new PoddUser(userIdentifier, bindingSet.getValue(PoddSesameRealm.PARAM_USER_SECRET).stringValue()
                        .toCharArray(), bindingSet.getValue(PoddSesameRealm.PARAM_USER_FIRSTNAME).stringValue(),
                        bindingSet.getValue(PoddSesameRealm.PARAM_USER_LASTNAME).stringValue(), bindingSet.getValue(
                                PoddSesameRealm.PARAM_USER_EMAIL).stringValue(), PoddUserStatus.INACTIVE);
        
        PoddUserStatus userStatus = PoddUserStatus.INACTIVE;
        final Value statusVal = bindingSet.getValue(PoddSesameRealm.PARAM_USER_STATUS);
        if(statusVal != null && statusVal instanceof URI)
        {
            userStatus = PoddUserStatus.getUserStatusByUri((URI)statusVal);
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
            result.setHomePage(PoddRdfConstants.VF.createURI(homePageVal.stringValue()));
        }
        
        final Value uriVal = bindingSet.getValue(PoddSesameRealm.PARAM_USER_URI);
        if(uriVal != null)
        {
            result.setUri(PoddRdfConstants.VF.createURI(uriVal.stringValue()));
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
    
    @Override
    protected Role buildRoleFromSparqlResult(final BindingSet bindingSet)
    {
        final URI roleUri = this.vf.createURI(bindingSet.getValue(PoddSesameRealm.PARAM_ROLE).stringValue());
        return PoddRoles.getRoleByUri(roleUri).getRole();
    }
    
    @Override
    protected String buildSparqlQueryForObjectRoles(final String userIdentifier, final URI objectUri)
    {
        this.log.debug("Building SPARQL query for Roles between User and object URI");
        
        final StringBuilder query = new StringBuilder();
        
        query.append(" SELECT DISTINCT ?");
        query.append(PoddSesameRealm.PARAM_ROLE);
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        
        query.append(" WHERE ");
        query.append(" { ");
        
        final String roleMappingVar = " ?mapping ";
        
        query.append(roleMappingVar);
        query.append(" <" + SesameRealmConstants.OAS_ROLEMAPPEDUSER + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" . ");
        
        query.append(roleMappingVar);
        query.append(" <" + SesameRealmConstants.OAS_ROLEMAPPEDROLE + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_ROLE);
        query.append(" . ");
        
        query.append(roleMappingVar);
        query.append(" <" + PoddRdfConstants.PODD_ROLEMAPPEDOBJECT + "> ");
        query.append(" ?object . ");
        
        if(userIdentifier != null)
        {
            query.append(" FILTER ( ?userUri IN (");
            query.append("\"" + NTriplesUtil.escapeString(userIdentifier) + "\"");
            query.append(") ) ");
        }
        
        query.append(" FILTER ( ?object IN (");
        query.append("<" + objectUri.stringValue() + ">");
        query.append(") ) ");
        
        query.append(" } ");
        
        this.log.debug(query.toString());
        
        return query.toString();
    }
    
    @Override
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
        query.append(NTriplesUtil.escapeString(userIdentifier));
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
        
        this.log.debug(query.toString());
        
        return query.toString();
    }
    
    @Override
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
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_ORCID + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ORCID);
        query.append(" . } ");
        
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERSECRET + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_SECRET);
        query.append(" . ");
        
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_STATUS + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_STATUS);
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
        
        if(!findAllUsers)
        {
            query.append("   FILTER(str(?userIdentifier) = \"" + NTriplesUtil.escapeString(userIdentifier) + "\") ");
        }
        
        query.append(" } ");
        return query.toString();
    }
    
    @Override
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
        
        query.append(" OPTIONAL{ ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddRdfConstants.PODD_USER_ORCID + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ORCID);
        query.append(" . } ");
        
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
    
    /**
     * @param role
     * @return
     */
    @Override
    protected RestletUtilRole getRoleByName(final String name)
    {
        final RestletUtilRole oasRole = PoddRoles.getRoleByName(name);
        return oasRole;
    }
    
    /**
     * @param uri
     * @return
     */
    @Override
    protected RestletUtilRole getRoleByUri(final URI uri)
    {
        final RestletUtilRole nextStandardRole = PoddRoles.getRoleByUri(uri);
        return nextStandardRole;
    }
    
    @Override
    public Collection<Role> getRolesForObject(final User user, final URI objectUri)
    {
        final Set<Role> results = new HashSet<Role>();
        
        final Collection<Collection<Role>> allResults = this.getRolesForObjectAlternate(user.getIdentifier(), objectUri).values();
        
        for(final Collection<Role> nextResult : allResults)
        {
            results.addAll(nextResult);
        }
        
        return results;
    }
    
    @Override
    public Map<String, Collection<Role>> getRolesForObjectAlternate(final String userIdentifier, final URI objectUri)
    {
        final ConcurrentMap<String, Collection<Role>> roleCollection = new ConcurrentHashMap<String, Collection<Role>>();
        
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
                    
                    if(!bindingSet.hasBinding(PoddSesameRealm.PARAM_USER_URI))
                    {
                        throw new RuntimeException("Query did not bind a user to the role");
                    }
                    
                    final RestletUtilUser nextUser =
                            this.findUser(bindingSet.getBinding(PoddSesameRealm.PARAM_USER_URI).getValue()
                                    .stringValue());
                    
                    Collection<Role> nextRoles = new HashSet<Role>();
                    final Collection<Role> putIfAbsent = roleCollection.putIfAbsent(nextUser.getIdentifier(), nextRoles);
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
    
    @Override
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
     * Copied from RestletUtilSesameRealm.java
     */
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
    
    @Override
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
    
    @Override
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
    
    // TODO: search in first/last name as well as identifier
    @Override
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
    
    @Override
    public void unmap(final User user, final Role role, final URI optionalObjectUri)
    {
        if(optionalObjectUri == null)
        {
            super.unmap(role, SesameRealmConstants.OAS_ROLEMAPPEDUSER, user.getIdentifier());
            return;
        }
        
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
            query.append("   FILTER(str(?identifier) = \"" + NTriplesUtil.escapeString(user.getIdentifier()) + "\") ");
            query.append("   FILTER(?role = <" + oasRole.getURI() + "> ) ");
            query.append("   FILTER(?object = <" + optionalObjectUri + "> ) ");
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
    
    @Override
    public URI updateUser(final PoddUser nextUser)
    {
        return this.addUser(nextUser, false);
    }
    
}
