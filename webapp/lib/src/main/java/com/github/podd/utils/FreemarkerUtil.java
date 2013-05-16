/**
 * 
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
        }
        return "TODO";
    }
    
    public URI getUri(final Value value)
    {
        if(this.isUri(value))
        {
            return (URI)value;
        }
        return null;
    }
    
    public boolean isUri(final Value value)
    {
        return value instanceof URI;
    }
    
}
