/**
 * 
 */
package com.github.podd.api;

/**
 * Possible options when requesting object type metadata.
 * 
 * @author kutila
 */
public enum MetadataPolicy
{
    /**
     * Include all types of properties
     */
    INCLUDE_ALL,
    
    /**
     * Include only poddBase:contains and its sub-properties
     */
    ONLY_CONTAINS,
    
    /**
     * Exclude poddBase:contains and its sub-properties
     */
    EXCLUDE_CONTAINS;
    
}
