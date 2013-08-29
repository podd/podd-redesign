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

import org.openrdf.model.URI;

/**
 * An immutable object designed solely to provide a wrapper for labels and descriptions that are
 * going to be immediately displayed.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddObjectLabel
{
    /**
     * @return the description
     */
    String getDescription();
    
    /**
     * @return the label
     */
    String getLabel();
    
    /**
     * @return the objectID
     */
    URI getObjectURI();
    
    /**
     * @return the parentArtifactID
     */
    InferredOWLOntologyID getOntologyID();
}