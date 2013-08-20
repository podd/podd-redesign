/*
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
    
    /** The user's title */
    private volatile String title;

    /** The user's telephone */
    private volatile String phone;
    
    /** The user's postal address */
    private volatile String address;
    
    /** The user's job position */
    private volatile String position;
    
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
    
    public PoddUser(final String identifier, final char[] secret, final String firstName, final String lastName,
            final String email, final PoddUserStatus userStatus, final URI homePage, final String organization,
            final String orcid, final String title, final String phone, final String address, final String position)
    {
        super(identifier, secret, firstName, lastName, email);
        this.userStatus = userStatus;
        this.homePage = homePage;
        this.organization = organization;
        this.orcid = orcid;
        this.title = title;
        this.phone = phone;
        this.address = address;
        this.position = position;
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
    
    public String getTitle()
    {
        return this.title;
    }
    
    public String getPhone()
    {
        return phone;
    }

    public String getAddress()
    {
        return address;
    }

    public String getPosition()
    {
        return position;
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
    
    public void setTitle(final String title)
    {
        this.title = title;
    }
    
    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public void setPosition(String position)
    {
        this.position = position;
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
        b.append(":");
        b.append(this.getTitle());
        b.append(":");
        b.append(this.getPhone());
        b.append(":");
        b.append(this.getAddress());
        b.append(":");
        b.append(this.getPosition());
        
        return b.toString();
    }
    
}
