/**
 * 
 */
package com.github.podd.api;

/**
 * 
 * @author kutila
 */
public enum UpdatePolicy
{
    /**
     * Replace previous statements about the subjects that are being updated
     */
    REPLACE_EXISTING,
    
    /**
     * Keep previous statements about the subjects that are being updated
     */
    MERGE_WITH_EXISTING
    
    ;
}
