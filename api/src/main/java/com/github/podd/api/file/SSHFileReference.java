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
package com.github.podd.api.file;

/**
 * Encapsulates SSH File References that are tracked inside of PODD Artifacts.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface SSHFileReference extends DataReference
{
    
    /**
     * @return The "filename" component which is needed to identify and locate this SSH file
     *         reference.
     */
    String getFilename();
    
    /**
     * @return The "path" component which is needed to identify and locate this SSH file reference.
     */
    String getPath();
    
    /**
     * @param filename
     *            The "filename" component which is needed to identify and locate this SSH file
     *            reference.
     */
    void setFilename(final String filename);
    
    /**
     * @param path
     *            The "path" component which is needed to identify and locate this SSH file
     *            reference.
     */
    void setPath(final String path);
}
