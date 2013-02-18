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

    public boolean isUri(Value value)
    {
        return value instanceof URI;
    }

    
}
