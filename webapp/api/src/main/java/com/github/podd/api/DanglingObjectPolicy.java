/**
 * 
 */
package com.github.podd.api;

/**
 * Possible options for handling Dangling Objects.
 * 
 * @author kutila
 */
public enum DanglingObjectPolicy
{
    /**
     * Remove any dangling objects found without informing user.
     */
    FORCE_CLEAN,
    
    /**
     * Notify caller of any dangling objects found.
     */
    REPORT
    
    ;
}
