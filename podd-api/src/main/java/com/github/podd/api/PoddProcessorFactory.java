/**
 * 
 */
package com.github.podd.api;

/**
 * Creates PoddProcessor instances to handle events in the PODD Artifact handling lifecycle.
 * 
 * The same processor object may be returned for multiple events if it is threadsafe.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 * @param <T> The types of Processor that this PoddProcessorFactory creates.
 * @param <E> The types of Events that this PoddProcessorFactory is designed to receive.
 * @param <I>
 * @param <O>
 */
public interface PoddProcessorFactory<T extends PoddProcessor<I, O>, E extends PoddProcessorEvent<I, O>, I, O>
{
    /**
     * Tests whether this processor factory can create objects that are able to handle the given
     * stage.
     * 
     * @param stage
     *            The stage that is being queried.
     * @return True if the processors created by this factory can handle the given stage.
     */
    boolean canHandleStage(PoddProcessorStage stage);
    
    T getProcessor(E event);
}
