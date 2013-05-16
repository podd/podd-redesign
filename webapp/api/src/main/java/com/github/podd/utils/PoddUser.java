/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.URI;

import com.github.ansell.restletutils.RestletUtilUser;

/**
 * This class represents a PODD user.
 * 
 * In PODD, the unique "identifier" of a user is also the email address. The URI is also a unique
 * identifier of a user.
 * 
 * Equality between two users is computed in {@link RestletUtilUser} using only the email,
 * identifier, firstName and lastName fields.
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
    
    /** The Home Page of this user */
    private volatile URI homePage;
    
    /** The unique URI for this user */
    private volatile URI uri;
    
    // TODO - include title, phone, address
    
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
     * @param homePage
     *            The Home page of the user.
     * @param organization
     *            The organization.
     * @param orcid
     *            The ORCID identifier.
     */
    public PoddUser(final String identifier, final char[] secret, final String firstName, final String lastName,
            final String email, final PoddUserStatus userStatus, final URI homePage, final String organization,
            final String orcid)
    {
        super(identifier, secret, firstName, lastName, email);
        this.userStatus = userStatus;
        this.homePage = homePage;
        this.organization = organization;
        this.orcid = orcid;
    }
    
    /**
     * Get the URL of a home page containing details about the User. This value is set by the user.
     * 
     * @return
     */
    public URI getHomePage()
    {
        return this.homePage;
    }
    
    public String getOrcid()
    {
        return this.orcid;
    }
    
    public String getOrganization()
    {
        return this.organization;
    }
    
    /**
     * Get the Unique URI allocated to each user. This value is usually generated and set by PODD.
     * 
     * @return
     */
    public URI getUri()
    {
        return this.uri;
    }
    
    public PoddUserStatus getUserStatus()
    {
        return this.userStatus;
    }
    
    /**
     * Set the URL of a home page containing details about the User. This value is usually set by
     * the user.
     * 
     * @param homePage
     */
    public void setHomePage(final URI homePage)
    {
        this.homePage = homePage;
    }
    
    public void setOrcid(final String orcid)
    {
        this.orcid = orcid;
    }
    
    public void setOrganization(final String organization)
    {
        this.organization = organization;
    }
    
    /**
     * Set the Unique URI allocated to each user. This value is usually generated and set by PODD.
     * 
     * @param uri
     */
    public void setUri(final URI uri)
    {
        this.uri = uri;
    }
    
    public void setUserStatus(final PoddUserStatus userStatus)
    {
        this.userStatus = userStatus;
    }
    
    @Override
    public String toString()
    {
        final StringBuilder b = new StringBuilder();
        b.append(super.toString());
        b.append(":");
        b.append(this.getUserStatus());
        b.append(":");
        b.append(this.getOrganization());
        b.append(":");
        b.append(this.getOrcid());
        b.append(":");
        b.append(this.getHomePage());
        
        return b.toString();
    }
    
}
