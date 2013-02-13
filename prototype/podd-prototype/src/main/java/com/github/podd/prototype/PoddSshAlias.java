/**
 * 
 */
package com.github.podd.prototype;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddSshAlias extends PoddAlias
{
    public PoddSshAlias(final String alias)
    {
        super(alias);
    }
    
    private String secret;
    private String username;
    private String fingerprint;
    
    public void setFingerprint(final String nextFingerprint)
    {
        this.fingerprint = nextFingerprint;
    }
    
    public String getFingerprint()
    {
        return this.fingerprint;
    }
    
    public void setUsername(final String nextUsername)
    {
        this.username = nextUsername;
    }
    
    public String getUsername()
    {
        return this.username;
    }
    
    public void setSecret(final String nextSecret)
    {
        this.secret = nextSecret;
    }
    
    public String getSecret()
    {
        return this.secret;
    }
    
}
