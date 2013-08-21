/**
 * PODD is an OWL ontology database used for scientific project management
 * 
 * Copyright (C) 2009-2013 The University Of Queensland
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
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
     * @throws NullPointerException
     *             If a NULL value is passed in as the stage.
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
     * @throws RuntimeException
     *             If an exceptional situation prevents this method from returning a PoddProcessor
     *             instance.
     */
    T getProcessor();
    
    /**
     * 
     * @return A set of stages that this processor factory is relevant to. An empty set is returned
     *         if, for some reason this factory does not support any stages.
     */
    Set<PoddProcessorStage> getStages();
}
