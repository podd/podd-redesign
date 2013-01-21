/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.URI;

import com.github.ansell.restletutils.RestletUtilUser;

/**
 * This class represents a PODD user.
 * 
 * In PODD, the unique "identifier" of a user is also the email address.  
 * The URI is also a unique identifier of a user.
 * 
 * Equality between two users is computed in {@link RestletUtilUser} using
 * only the email, identifier, firstName and lastName fields.
 * 
 * @author kutila
 */
public class PoddUser extends RestletUtilUser
{
    /** 
     * The ORCID (see {@link http://orcid.org}) identifier of the user. 
     */
    private volatile String orcid;
    
    /** The organization */
    private volatile String organization;
    
    /** The status of this user */
    private volatile PoddUserStatus userStatus;
    
    /** The unique URI for this user */
    private volatile URI uri;
    
    /**
     * Constructor.
     * 
     * @param identifier
     *            The identifier (login).
     * @param secret
     *            The identification secret.
     * @param firstName
     *            The first name.
     * @param lastName
     *            The last name.
     * @param email
     *            The email.
     * @param userStatus
     *            The user status.
     */
    public PoddUser(final String identifier, final char[] secret, final String firstName, final String lastName,
            final String email, final PoddUserStatus userStatus)
    {
        super(identifier, secret, firstName, lastName, email);
        this.userStatus = userStatus;
    }
    
    /**
     * Constructor.
     * 
     * @param identifier
     *            The identifier (login).
     * @param secret
     *            The identification secret.
     * @param firstName
     *            The first name.
     * @param lastName
     *            The last name.
     * @param email
     *            The email.
     * @param userStatus
     *            The user status.
     * @param uri
     *            The URI of the user.
     * @param organization
     *            The organization.
     * @param orcid
     *            The ORCID identifier.
     */
    public PoddUser(final String identifier, final char[] secret, final String firstName, final String lastName,
            final String email, final PoddUserStatus userStatus, final URI uri, final String organization, final String orcid)
    {
        super(identifier, secret, firstName, lastName, email);
        this.userStatus = userStatus;
        this.uri = uri;
        this.organization = organization;
        this.orcid = orcid;
    }
    
    
    public String getOrcid()
    {
        return orcid;
    }
    
    public String getOrganization()
    {
        return organization;
    }
    
    public PoddUserStatus getUserStatus()
    {
        return userStatus;
    }
    
    public URI getUri()
    {
        return uri;
    }
    
    public void setOrcid(String orcid)
    {
        this.orcid = orcid;
    }
    
    public void setOrganization(String organization)
    {
        this.organization = organization;
    }
    
    public void setUserStatus(PoddUserStatus userStatus)
    {
        this.userStatus = userStatus;
    }
    
    public void setUri(URI uri)
    {
        this.uri = uri;
    }
    
}
