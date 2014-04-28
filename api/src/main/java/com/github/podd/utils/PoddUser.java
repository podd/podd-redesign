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

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.RestletUtilUser;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.api.purl.PoddPurlProcessorPrefixes;

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
     * Creates a PoddUser object from statements in the given {@link Model}, without setting the
     * password.
     *
     * @param model
     * @return A PoddUser object created from the given model.
     * @throws ResourceException
     *             If there are errors in the process.
     */
    public static final PoddUser fromModel(final Model model)
    {
        return PoddUser.fromModel(model, false, false, false);
    }
    
    /**
     * Creates a PoddUser object from statements in the given {@link Model}.
     *
     * @param model
     * @param allowSecret
     *            If true, the password will be searched for.
     * @param failIfSecretFound
     *            If true, and if allowSecret is true, will fail if secret is found.
     * @param requireSecret
     *            If true, and if allowSecret is true, will fail if the secret is not found.
     * @return A PoddUser object created from the given model.
     * @throws ResourceException
     *             If there are errors in the process.
     */
    public static final PoddUser fromModel(final Model model, final boolean allowSecret,
            final boolean failIfSecretFound, final boolean requireSecret)
    {
        String identifier = model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString();
        if(identifier == null || identifier.trim().length() == 0)
        {
            // FIXME: Convert this to a PoddException
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User Identifier cannot be empty");
        }
        identifier = identifier.trim();
        for(char nextChar : identifier.toCharArray())
        {
            if(Character.isWhitespace(nextChar))
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User Identifier cannot contain a space");
            }
        }
        char[] secret = null;
        if(allowSecret)
        {
            final String password = model.filter(null, SesameRealmConstants.OAS_USERSECRET, null).objectString();
            if(failIfSecretFound && password != null)
            {
                // FIXME: Convert this to a PoddException
                throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, "User Password must not be present");
            }
            if(requireSecret && (password == null || password.trim().length() == 0))
            {
                // FIXME: Convert this to a PoddException
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User Password cannot be empty");
            }
            if(password != null)
            {
                secret = password.toCharArray();
            }
        }
        
        final String firstName = model.filter(null, SesameRealmConstants.OAS_USERFIRSTNAME, null).objectString();
        final String lastName = model.filter(null, SesameRealmConstants.OAS_USERLASTNAME, null).objectString();
        // PODD-specific requirement. First/Last names are mandatory.
        if(firstName == null || lastName == null)
        {
            // FIXME: Convert this to a PoddException
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User First/Last name cannot be empty");
        }
        
        // PODD-specific requirement. Email has to be present and equal to the user Identifier.
        final String email = model.filter(null, SesameRealmConstants.OAS_USEREMAIL, null).objectString();
        if(email == null)
        {
            // FIXME: Convert this to a PoddException
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User Email cannot be empty");
        }
        
        PoddUserStatus status = PoddUserStatus.INACTIVE;
        final URI statusUri = model.filter(null, PODD.PODD_USER_STATUS, null).objectURI();
        if(statusUri != null)
        {
            status = PoddUserStatus.getUserStatusByUri(statusUri);
        }
        
        final URI homePage = model.filter(null, PODD.PODD_USER_HOMEPAGE, null).objectURI();
        final String organization = model.filter(null, PODD.PODD_USER_ORGANIZATION, null).objectString();
        final String orcidID = model.filter(null, PODD.PODD_USER_ORCID, null).objectString();
        final String title = model.filter(null, PODD.PODD_USER_TITLE, null).objectString();
        final String phone = model.filter(null, PODD.PODD_USER_PHONE, null).objectString();
        final String address = model.filter(null, PODD.PODD_USER_ADDRESS, null).objectString();
        final String position = model.filter(null, PODD.PODD_USER_POSITION, null).objectString();
        
        final PoddUser user =
                new PoddUser(identifier, secret, firstName, lastName, email, status, homePage, organization, orcidID,
                        title, phone, address, position);
        
        return user;
    }
    
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
    
    public String getAddress()
    {
        return this.address;
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
    
    public String getPhone()
    {
        return this.phone;
    }
    
    public String getPosition()
    {
        return this.position;
    }
    
    public String getTitle()
    {
        return this.title;
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
    
    /**
     * Get a String identifying the human User and his/her affiliation.
     *
     * @return
     */
    public String getUserLabel()
    {
        return this.getFirstName() + " " + this.getLastName() + ", " + this.getOrganization();
    }
    
    public PoddUserStatus getUserStatus()
    {
        return this.userStatus;
    }
    
    public void setAddress(final String address)
    {
        this.address = address;
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
    
    public void setPhone(final String phone)
    {
        this.phone = phone;
    }
    
    public void setPosition(final String position)
    {
        this.position = position;
    }
    
    public void setTitle(final String title)
    {
        this.title = title;
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
    
    public void toModel(final Model model, final boolean includeSecret)
    {
        URI userUri = this.getUri();
        
        if(userUri == null)
        {
            userUri = PODD.VF.createURI(PoddPurlProcessorPrefixes.UUID.getTemporaryPrefix() + this.getIdentifier());
        }
        
        model.add(userUri, SesameRealmConstants.OAS_USERIDENTIFIER, PODD.VF.createLiteral(this.getIdentifier()));
        
        // Password should not be sent back in RDF to users!
        if(includeSecret && this.getSecret() != null)
        {
            model.add(userUri, SesameRealmConstants.OAS_USERSECRET, PODD.VF.createLiteral(new String(this.getSecret())));
        }
        
        model.add(userUri, SesameRealmConstants.OAS_USERFIRSTNAME, PODD.VF.createLiteral(this.getFirstName()));
        model.add(userUri, SesameRealmConstants.OAS_USERLASTNAME, PODD.VF.createLiteral(this.getLastName()));
        model.add(userUri, SesameRealmConstants.OAS_USEREMAIL, PODD.VF.createLiteral(this.getEmail()));
        
        if(this.getHomePage() != null)
        {
            model.add(userUri, PODD.PODD_USER_HOMEPAGE, this.getHomePage());
        }
        
        if(this.getOrganization() != null)
        {
            model.add(userUri, PODD.PODD_USER_ORGANIZATION, PODD.VF.createLiteral(this.getOrganization()));
        }
        
        if(this.getOrcid() != null)
        {
            model.add(userUri, PODD.PODD_USER_ORCID, PODD.VF.createLiteral(this.getOrcid()));
        }
        
        if(this.getTitle() != null)
        {
            model.add(userUri, PODD.PODD_USER_TITLE, PODD.VF.createLiteral(this.getTitle()));
        }
        
        if(this.getPhone() != null)
        {
            model.add(userUri, PODD.PODD_USER_PHONE, PODD.VF.createLiteral(this.getPhone()));
        }
        
        if(this.getAddress() != null)
        {
            model.add(userUri, PODD.PODD_USER_ADDRESS, PODD.VF.createLiteral(this.getAddress()));
        }
        
        if(this.getPosition() != null)
        {
            model.add(userUri, PODD.PODD_USER_POSITION, PODD.VF.createLiteral(this.getPosition()));
        }
        
        if(this.getUserStatus() != null)
        {
            model.add(userUri, PODD.PODD_USER_STATUS, this.getUserStatus().getURI());
        }
        else
        {
            // INACTIVE by default
            model.add(userUri, PODD.PODD_USER_STATUS, PoddUserStatus.INACTIVE.getURI());
        }
        
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
