/**
 * 
 */
package com.github.podd.api;

/**
 * This interface is the general interface used to surround all processing events.
 * 
 * Subinterfaces are used for each stage to identify the important details that the processor needs
 * to determine whether it needs to handle this event and if it does, what processing it needs to do
 * to handle the event.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 * @param <I>
 *            The input for this PoddProcessorEvent.
 * @param <O>
 *            The output for this PoddProcessorEvent.
 */
public interface PoddProcessorEvent<I>
{
    I getInput();
    
    PoddProcessorStage getStage();
    
    boolean isAfterStage();
    
    boolean isBeforeStage();
    
}
