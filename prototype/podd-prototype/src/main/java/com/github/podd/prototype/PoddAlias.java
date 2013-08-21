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
