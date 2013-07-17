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
    
    /**
     * 
     * @return The URL of the SPARQL Endpoint.
     */
    String getEndpointURL();
    
    void setGraph(final String graph);
    
    void setEndpointURL(final String endpointURL);
}
