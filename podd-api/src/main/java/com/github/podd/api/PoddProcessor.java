/**
 * 
 */
package com.github.podd.api;

/**
 * An interface to accommodate an event driven architecture for processing PODD Artifacts at various
 * stages throughout the lifecycle.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface PoddProcessor<I, O>
{
    /**
     * Called if this processor is being used as a handler for the stage that is about to be started.
     * 
     * @param stage The stage that is about to be started.
     */
    void handleBefore(PoddProcessorEvent<I, O> event);
    
    /**
     * Called if this processor is being used as a handler for the stage that was just completed.
     * 
     * @param stage The stage that was just completed.
     */
    void handleAfter(PoddProcessorEvent<I, O> event);
}
