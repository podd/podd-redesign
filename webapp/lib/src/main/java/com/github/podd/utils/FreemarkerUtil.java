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

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * @author kutila
 * 
 */
public class FreemarkerUtil
{
    
    /**
     * If the given object contains a URI with a protocol (scheme), clip it off.
     * 
     * @param object
     * @return
     */
    public String clipProtocol(final Object object)
    {
        if(object == null)
        {
            return null;
        }
        
        // get String representation of input
        String result;
        if(object instanceof Value)
        {
            final Value v = (Value)object;
            result = v.stringValue();
        }
        else
        {
            result = object.toString();
        }
        
        // clip the protocol part
        if(result.startsWith("mailto:") || result.startsWith("http://"))
        {
            return result.substring(7);
        }
        else if(result.startsWith("https://"))
        {
            return result.substring(8);
        }
        
        return result;
    }
    
    /**
     * Rather hacky attempt to retrieve the datatype of a given Value object. TODO: Incomplete and
     * needs to be fixed.
     * 
     * @param value
     * @return
     */
    public String getDatatype(final Value value)
    {
        if(value instanceof Literal)
        {
            final URI dataType = ((Literal)value).getDatatype();
            if(dataType != null)
            {
                if(dataType.getNamespace().contains("http://www.w3.org/2001/XMLSchema#"))
                {
                    return "xsd:" + dataType.getLocalName();
                }
                
                return dataType.stringValue();
            }
            // Default to xsd:string datatype if any value happens to get
            // through without a datatype
            // at this point
            return "xsd:string";
        }
        return "NotALiteral:" + value.getClass().getName();
    }
    
    public URI getUri(final Value value)
    {
        if(this.isUri(value))
        {
            return (URI)value;
        }
        return null;
    }
    
    public boolean isLiteral(final Value value)
    {
        return value instanceof Literal;
    }
    
    public boolean isUri(final Value value)
    {
        return value instanceof URI;
    }
    
}
