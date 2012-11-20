/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.URI;

/**
 * This interface represents a PODD User.
 * 
 * An email address is used as the "login name" for a user and needs to be unique. The URI is also a
 * unique identifier of a user.
 * 
 * @author kutila
 * 
 */
public interface PoddUser
{
    
    public abstract String getEmail();
    
    public abstract String getFirstName();
    
    public abstract String getLastName();
    
    public abstract String getOrcid();
    
    public abstract String getOrganization();
    
    public abstract char[] getSecret();
    
    public abstract PoddUserStatus getStatus();
    
    public abstract URI getUri();
    
    public abstract void setEmail(String email);
    
    public abstract void setFirstName(String firstName);
    
    public abstract void setLastName(String lastName);
    
    public abstract void setOrcid(String orcid);
    
    public abstract void setOrganization(String organization);
    
    public abstract void setSecret(char[] secret);
    
    public abstract void setStatus(PoddUserStatus status);
    
    public abstract void setUri(URI uri);
    
}