/**
 * 
 */
package com.github.podd.api.file;

/**
 * Encapsulates SPARQL Data References that are tracked inside of PODD Artifacts.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface SPARQLDataReference extends DataReference
{
    
    /**
     * @return The SPARQL Graph containing the data reference.
     */
    String getGraph();
    
    void setGraph(final String graph);
}
