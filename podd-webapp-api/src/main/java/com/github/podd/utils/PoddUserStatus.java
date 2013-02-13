package com.github.podd.utils;

/**
 * Represents the valid states that a <code>PoddUser</code> can be in.
 * 
 * @author kutila
 * 
 */
public enum PoddUserStatus
{
    /**
     * An ACTIVE user has the ability to access the PODD system.
     */
    ACTIVE,
    
    /**
     * An inactive user can be referenced from within PODD artifacts. Such a user does not have the
     * ability to access the PODD system.
     */
    INACTIVE
}