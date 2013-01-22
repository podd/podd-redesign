/**
 * 
 */
package com.github.podd.restlet;

import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.Repository;
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
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;

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
     * Verifier class based on the default security model. It looks up users in the mapped organizations.
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

    protected static final String PARAM_USER_ORCID = "userOrcid";
    protected static final String PARAM_USER_ORGANIZATION = "userOrganization";

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Constructor
     * 
     * @param repository
     * @param contexts
     */
    public PoddSesameRealm(Repository repository, URI... contexts)
    {
        super(repository, contexts);
        
        //set PODD-specific Enroler and Verifier
        this.setEnroler(new DefaultPoddSesameRealmEnroler());
        this.setVerifier(new DefaultPoddSesameRealmVerifier());
    }
    
    
    /**
     * Overridden to build a SPARQL query to retrieve details of a PoddUser.
     * 
     * @param userIdentifier
     *            The unique identifier of the User to search for.
     * @return A String representation of the SPARQL Select query
     */
    @Override
    protected String buildSparqlQueryToFindUser(final String userIdentifier)
    {
        this.log.info("Building SPARQL query");
        
        final StringBuilder query = new StringBuilder();
        
        query.append(" SELECT ");
        query.append(" ?");
        query.append(PARAM_USER_URI);
        query.append(" ?");
        query.append(PARAM_USER_SECRET);
        query.append(" ?");
        query.append(PARAM_USER_FIRSTNAME);
        query.append(" ?");
        query.append(PARAM_USER_LASTNAME);
        query.append(" ?");
        query.append(PARAM_USER_EMAIL);
        query.append(" ?");
        query.append(PARAM_USER_STATUS);
        query.append(" ?");
        query.append(PARAM_USER_ORGANIZATION);
        query.append(" ?");
        query.append(PARAM_USER_ORCID);
        
        query.append(" WHERE ");
        query.append(" { ");
        query.append(" ?");
        query.append(PARAM_USER_URI);
        query.append(" a <" + SesameRealmConstants.OAS_USER + "> . ");

        query.append(" ?");
        query.append(PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERIDENTIFIER + "> ");
        query.append(" ?");
        query.append(PARAM_USER_IDENTIFIER);
        query.append(" . ");
       
        query.append(" ?");
        query.append(PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERSECRET + "> ");
        query.append(" ?");
        query.append(PARAM_USER_SECRET);
        query.append(" . ");

        query.append(" OPTIONAL{ ?");
        query.append(PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERFIRSTNAME + "> ");
        query.append(" ?");
        query.append(PARAM_USER_FIRSTNAME);
        query.append(" . } ");
        
        query.append(" OPTIONAL{ ?");
        query.append(PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USERLASTNAME + "> ");
        query.append(" ?");
        query.append(PARAM_USER_LASTNAME);
        query.append(" . } ");

        query.append(" OPTIONAL{ ?");
        query.append(PARAM_USER_URI);
        query.append(" <" + SesameRealmConstants.OAS_USEREMAIL + "> ");
        query.append(" ?");
        query.append(PARAM_USER_EMAIL);
        query.append(" . } ");

        //TODO: firstname, lastname, email are mandatory. add optional parameters: ORCID, Organization
        
        query.append("   FILTER(str(?userIdentifier) = \"" + NTriplesUtil.escapeString(userIdentifier) + "\") ");
        query.append(" } ");
        return query.toString();
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
        
        PoddUser result;
        result =
                new PoddUser(userIdentifier, 
                        bindingSet.getValue(PARAM_USER_SECRET).stringValue().toCharArray(),
                        bindingSet.getValue(PARAM_USER_FIRSTNAME).stringValue(), 
                        bindingSet.getValue(PARAM_USER_LASTNAME).stringValue(),
                        bindingSet.getValue(PARAM_USER_EMAIL).stringValue(),
                        PoddUserStatus.ACTIVE);
        return result;
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
        final RestletUtilRole nextStandardRole =
                PoddRoles.getRoleByUri(uri);
        return nextStandardRole;
    }
    
    
}

