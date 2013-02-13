/**
 * 
 */
package com.github.podd.prototype;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddAlias
{
    private String host;
    private String port;
    private String protocol;
    private String alias;
    
    public PoddAlias(final String alias)
    {
        this.alias = alias;
    }
    
    public void setProtocol(final String nextProtocol)
    {
        this.protocol = nextProtocol;
    }
    
    public String getProtocol()
    {
        return this.protocol;
    }
    
    public void setHost(final String nextHost)
    {
        this.host = nextHost;
    }
    
    public String getHost()
    {
        return this.host;
    }
    
    public void setPort(final String nextPort)
    {
        this.port = nextPort;
    }
    
    public String getPort()
    {
        return this.port;
    }
    
}
