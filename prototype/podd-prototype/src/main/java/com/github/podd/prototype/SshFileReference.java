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
 /**
 * 
 */
package com.github.podd.prototype;

/**
 * This class represents a PODD File Reference to a resource accessible via SSH.
 * 
 * @author kutila
 * @created 19/11/2012
 */
public class SshFileReference extends FileReference
{
    
    private String filename;
    private String path;
    private String description;
    
    public String getFilename()
    {
        return this.filename;
    }
    
    public void setFilename(final String filename)
    {
        this.filename = filename;
    }
    
    public String getPath()
    {
        return this.path;
    }
    
    public void setPath(final String path)
    {
        this.path = path;
    }
    
    public String getDescription()
    {
        return this.description;
    }
    
    public void setDescription(final String description)
    {
        this.description = description;
    }
    
    @Override
    public boolean isFilled()
    {
        if(this.getArtifactUri() == null || this.getObjectUri() == null || this.getServerAlias() == null
                || this.getFilename() == null || this.getPath() == null)
        {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString()
    {
        final StringBuilder b = new StringBuilder();
        b.append(super.toString());
        b.append(" path=");
        b.append(this.path);
        b.append(" filename=");
        b.append(this.filename);
        return b.toString();
    }
    
}
