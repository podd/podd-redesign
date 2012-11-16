/**
 * 
 */
package com.github.podd.api;

import org.openrdf.model.Graph;

/**
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 * @param <T>
 *            The types of objects that this PoddRdfProcessor creates.
 */
public interface PoddRdfProcessor extends PoddProcessor<Graph>
{
    void processRdfGraph(Graph graph);
}
