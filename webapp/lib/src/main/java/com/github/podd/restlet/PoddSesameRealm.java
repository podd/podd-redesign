/**
 * 
 */
package com.github.podd.restlet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
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
import com.github.ansell.restletutils.RestletUtilSesameRealm;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

/**
 * Customises RestletUtilSesameRealm.java to use PODDUsers and PoddRoles.
 * 
 * @author kutila
 * 
 */
public class PoddSesameRealm extends RestletUtilSesameRealm
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
            final PoddUser user = (PoddUser)PoddSesameRealm.this.findUser(clientInfo.getUser().getIdentifier());
            
            if(user != null)
            {
                // Find all the inherited groups of this user
                final Set<Group> userGroups = PoddSesameRealm.this.findGroups(user);
                
                // Add roles specific to this user
                final Set<Role> userRoles = PoddSesameRealm.this.findRoles(user);
                
                for(final Role role : userRoles)
                {
                    clientInfo.getRoles().add(role);
                }
                
                if(clientInfo.isAuthenticated())
                {
                    clientInfo.getRoles().add(PoddRoles.AUTHENTICATED.getRole());
                }
                
                // Add roles common to group members
                final Set<Role> groupRoles = PoddSesameRealm.this.findRoles(userGroups);
                
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
            final PoddUser checkUser = (PoddUser)PoddSesameRealm.this.findUser(identifier);
            
            if(checkUser == null)
            {
                PoddSesameRealm.this.log.error("Cannot create a user for the given identifier: {}", identifier);
                throw new IllegalArgumentException("Cannot create a user for the given identifier");
            }
            
            final PoddUser result =
                    new PoddUser(identifier, (char[])null, checkUser.getFirstName(), checkUser.getLastName(),
                            checkUser.getEmail(), PoddUserStatus.ACTIVE);
            
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
    }
    
    // ======================= end inner classes ==========================
    
    protected static final String PARAM_USER_IDENTIFIER = "userIdentifier";
    protected static final String PARAM_USER_URI = "userUri";
    protected static final String PARAM_USER_SECRET = "userSecret";
    protected static final String PARAM_USER_FIRSTNAME = "userFirstName";
    protected static final String PARAM_USER_LASTNAME = "userLastName";
    protected static final String PARAM_USER_EMAIL = "userEmail";
    protected static final String PARAM_USER_STATUS = "userStatus";
    protected static final String PARAM_USER_HOMEPAGE = "userHomePage";
    
    protected static final String PARAM_USER_ORCID = "userOrcid";
    protected static final String PARAM_USER_ORGANIZATION = "userOrganization";
    
    protected static final String PARAM_ROLE = "role";
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private final ValueFactory vf = PoddRdfConstants.VALUE_FACTORY;
    
    /**
     * Constructor
     * 
     * @param repository
     * @param contexts
     */
    public PoddSesameRealm(Repository repository, URI... contexts)
    {
        super(repository, contexts);
        
        // set PODD-specific Enroler and Verifier
        this.setEnroler(new DefaultPoddSesameRealmEnroler());
        this.setVerifier(new DefaultPoddSesameRealmVerifier());
    }
    
    /**
     * This method adds a User entry to the Realm and underlying Sesame Repository
     * including PODD-specific user parameters.
     *  
     * @param nextUser
     * @return
     */
    public URI addUser(final PoddUser nextUser)
    {
        URI nextUserUUID = super.addUser(nextUser);
        
        this.log.info("adding org, orcid, uri");
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.getRepository().getConnection();
            conn.begin();
            
            if(nextUser.getOrganization() != null)
            {
                conn.add(nextUserUUID, PoddWebConstants.PODD_USER_ORGANIZATION,
                        this.vf.createLiteral(nextUser.getOrganization()), this.getContexts());
            }
            
            if(nextUser.getOrcid() != null)
            {
                conn.add(nextUserUUID, PoddWebConstants.PODD_USER_ORCID,
                        this.vf.createLiteral(nextUser.getOrcid()), this.getContexts());
            }
            
            if(nextUser.getHomePage() != null)
            {
                conn.add(nextUserUUID, PoddWebConstants.PODD_USER_HOMEPAGE, nextUser.getHomePage(),
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
    protected PoddUser buildRestletUserFromSparqlResult(final String userIdentifier, final BindingSet bindingSet)
    {
        this.log.info("Building RestletUtilUser from SPARQL results");
        
        final PoddUser result =
                new PoddUser(userIdentifier, 
                        bindingSet.getValue(PARAM_USER_SECRET).stringValue().toCharArray(),
                        bindingSet.getValue(PARAM_USER_FIRSTNAME).stringValue(), 
                        bindingSet.getValue(PARAM_USER_LASTNAME).stringValue(),
                        bindingSet.getValue(PARAM_USER_EMAIL).stringValue(),
                        PoddUserStatus.ACTIVE);
        
        Value organizationVal = bindingSet.getValue(PARAM_USER_ORGANIZATION);
        if (organizationVal != null)
        {
            result.setOrganization(organizationVal.stringValue());
        }
        
        Value orcidVal = bindingSet.getValue(PARAM_USER_ORCID);
        if (orcidVal != null)
        {
            result.setOrcid(orcidVal.stringValue());
        }
        
        Value homePageVal = bindingSet.getValue(PARAM_USER_HOMEPAGE);
        if (homePageVal != null)
        {
            result.setHomePage(PoddRdfConstants.VALUE_FACTORY.createURI(homePageVal.stringValue()));
        }

        Value uriVal = bindingSet.getValue(PARAM_USER_URI);
        if (uriVal != null)
        {
            result.setUri(PoddRdfConstants.VALUE_FACTORY.createURI(uriVal.stringValue()));
        }
        
        return result;
    }
    
    /**
     * Retrieve a Restlet Role from the values retrieved via SPARQL
     * 
     * @param bindingSet
     * @return
     */
    protected Role buildRoleFromSparqlResult(final BindingSet bindingSet)
    {
        final URI roleUri = this.vf.createURI(bindingSet.getValue(PoddSesameRealm.PARAM_ROLE).stringValue());
        return PoddRoles.getRoleByUri(roleUri).getRole();
    }
    
    /**
     * Build a SPARQL query which returns Roles common to a given user across all given object URIs
     * 
     * @param userIdentifier
     * @param objectUris
     * @return
     */
    protected String buildSparqlQueryForCommonObjectRoles(final String userIdentifier, final Collection<URI> objectUris)
    {
        this.log.info("Building SPARQL query for common Roles across Objects");
        
        final StringBuilder query = new StringBuilder();

/*        
             SELECT ?role WHERE {
             
              ?_anyMapping0 <roleMappedUser> :userIdentifier .
              ?_anyMapping0 <roleMappedRole> ?role .
              ?_anyMapping0 <roleMappedObject> <objectUri_0> .

              ?_anyMapping1 <roleMappedUser> :userIdentifier .
              ?_anyMapping1 <roleMappedRole> ?role .
              ?_anyMapping1 <roleMappedObject> <objectUri_1> .
          
              ...
          } 
*/
        
        query.append(" SELECT ?");
        query.append(PoddSesameRealm.PARAM_ROLE);
        query.append(" WHERE ");
        query.append(" { ");

        int i = 0;
        for (URI objectUri : objectUris)
        {
            String roleMappingVar = " ?_anyMapping" + i;
            i++;
            
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
            
            query.append(roleMappingVar);
            query.append(" <" + PoddWebConstants.PODD_ROLEMAPPEDOBJECT + "> ");
            query.append(" <");
            query.append(objectUri.stringValue());
            query.append("> . ");
        }
        
        query.append(" } ");
        return query.toString();
    }
    
    /**
     * Overridden to build a SPARQL query to retrieve details of a PoddUser.
     * 
     * @param userIdentifier
     *            The unique identifier of the User to search for.
     * @return A String representation of the SPARQL Select query
     */
    @Override
    protected String buildSparqlQueryToFindUser(final String userIdentifier, boolean findAllUsers)
    {
        this.log.info("Building SPARQL query");
        
        final StringBuilder query = new StringBuilder();

        query.append(" SELECT ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
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
        query.append(" <" + PoddWebConstants.PODD_USER_ORCID + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ORCID);
        query.append(" . ");
       
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERSECRET + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_SECRET);
        query.append(" . ");

        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddWebConstants.PODD_USER_HOMEPAGE + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_HOMEPAGE);
        query.append(" . ");

        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_URI);
        query.append(" <" + PoddWebConstants.PODD_USER_ORGANIZATION + "> ");
        query.append(" ?");
        query.append(PoddSesameRealm.PARAM_USER_ORGANIZATION);
        query.append(" . ");

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

        //TODO: firstname, lastname, email are mandatory. add optional parameters: ORCID, Organization
        if(!findAllUsers)
        {
            query.append("   FILTER(str(?userIdentifier) = \"" + NTriplesUtil.escapeString(userIdentifier) + "\") ");
        }
        
        query.append(" } ");
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
     * @param nextRoleMappingStatement
     * @return
     */
    @Override
    protected RestletUtilRole getRoleByUri(final URI uri)
    {
        final RestletUtilRole nextStandardRole = PoddRoles.getRoleByUri(uri);
        return nextStandardRole;
    }
    
    /**
     * This method maps a User to a Role with an optional URIs.
     * 
     * Example 1: John (User) is a PROJECT_MEMBER (Role) of the "Water Stress" project (URI).
     * 
     * Example 2: Bob (User) is an ADMIN (Role) for the whole repository (no URI).
     * 
     * @param user
     * @param role
     * @param optionalObjectUri
     */
    public void map(User user, Role role, URI optionalObjectUri)
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
                conn.add(this.vf.createStatement(nextRoleMappingUUID, PoddWebConstants.PODD_ROLEMAPPEDOBJECT,
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
                catch(RepositoryException e1)
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
    
    /**
     * For a given User, this method finds Role Mappings common across ALL the given object URIs.
     * 
     * @param user
     * @param objectUris
     * @return A Collection of Roles that are common for ALL given object URIs
     */
    public Collection<Role> getCommonRolesForObjects(User user, Collection<URI> objectUris)
    {
        if(user == null)
        {
            throw new NullPointerException("User was null");
        }
        
        Collection<Role> roleCollection = new HashSet<Role>();
        
        RepositoryConnection conn = null;
        try
        {
            conn = this.getRepository().getConnection();
            
            final String query = this.buildSparqlQueryForCommonObjectRoles(user.getIdentifier(), objectUris);
            
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
                    this.log.info("Could not find role with mappings for user: {}", user.getIdentifier());
                }
                
                while(queryResult.hasNext())
                {
                    Role role = this.buildRoleFromSparqlResult(queryResult.next());
                    roleCollection.add(role);
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
            try
            {
                conn.close();
            }
            catch(final RepositoryException e)
            {
                this.log.error("Failure to close connection", e);
            }
        }
        
        return roleCollection;
    }
    
}
