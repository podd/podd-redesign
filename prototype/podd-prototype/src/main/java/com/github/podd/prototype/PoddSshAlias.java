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
