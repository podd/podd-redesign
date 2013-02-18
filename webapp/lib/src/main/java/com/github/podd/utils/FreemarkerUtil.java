/**
 * 
 */
package com.github.podd.utils;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * @author kutila
 * 
 */
public class FreemarkerUtil
{
    
    public boolean isUri(final Value value)
    {
        return value instanceof URI;
    }
    
    public URI getUri(final Value value)
    {
        if(this.isUri(value))
        {
            return (URI)value;
        }
        return null;
    }
    
}
