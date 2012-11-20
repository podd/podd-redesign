/**
 * 
 */
package com.github.podd.api;

import java.util.Set;

/**
 * Creates PoddProcessor instances to handle events in the PODD Artifact handling lifecycle.
 * 
 * The same processor object may be returned for multiple calls if it is threadsafe.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 * @param <T>
 *            The types of Processor that this PoddProcessorFactory creates.
 * @param <I>
 *            The type of the Input for this type of PoddProcessorFactory.
 */
public interface PoddProcessorFactory<T extends PoddProcessor<I>, I>
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
    
    /**
     * 
     * @return A string that is unique to this processor implementation.
     */
    String getKey();
    
    /**
     * 
     * NOTE: If instances of the processor are not threadsafe, then new instances must be returned
     * by this method for each call.
     * 
     * @return An instance of PoddProcessor.
     */
    T getProcessor();
    
    /**
     * 
     * @return A set of stages that this processor factory is relevant to.
     */
    Set<PoddProcessorStage> getStages();
}
