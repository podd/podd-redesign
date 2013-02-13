/**
 * 
 */
package com.github.podd.utils.test;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.github.podd.api.PoddRdfProcessor;
import com.github.podd.api.PoddRdfProcessorFactory;
import com.github.podd.utils.PoddRdfUtils;

/**
 * @author kutila
 * 
 */
public class PoddRdfUtilsTest
{
    private String[] testBGPArray = { "?s ?p ?o", "[BGP-String]" };
    private String[] testWhereArray = { "?s ?p ?o", "[WHERE-String]" };
    private String[] testGroupByArray = { "", "[GROUPBY-String]" };
    
    /**
     * Tests Building a SPARQL construct query using a mock PODD RDF Processor Factory
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSparqlConstructQuery() throws Exception
    {
        
        for(int i = 0; i < 2; i++)
        {
            final PoddRdfProcessorFactory<PoddRdfProcessor> mockFactory = Mockito.mock(PoddRdfProcessorFactory.class);
            Mockito.when(mockFactory.getSPARQLConstructBGP()).thenReturn(this.testBGPArray[i]);
            Mockito.when(mockFactory.getSPARQLConstructWhere()).thenReturn(this.testWhereArray[i]);
            Mockito.when(mockFactory.getSPARQLGroupBy()).thenReturn(this.testGroupByArray[i]);
            
            final String sparql = PoddRdfUtils.buildSparqlConstructQuery(mockFactory);
            Assert.assertNotNull(sparql);
            Assert.assertFalse(sparql.isEmpty());
            
            String expectedSparql =
                    "CONSTRUCT { " + this.testBGPArray[i] + " } WHERE { " + this.testWhereArray[i] + " }";
            if(!this.testGroupByArray[i].isEmpty())
            {
                expectedSparql = expectedSparql + " GROUP BY " + this.testGroupByArray[i];
            }
            
            Assert.assertEquals("SPARQL Query was not as expected", expectedSparql, sparql);
        }
    }
    
    /**
     * Tests Building a SPARQL construct query using a mock PODD RDF Processor Factory and a subject
     * URI
     * 
     * @throws Exception
     */
    @Test
    public void testBuildSparqlConstructQueryUsingSubject() throws Exception
    {
        final URI[] subjects =
                { ValueFactoryImpl.getInstance().createURI("http://p.org/sub1"),
                        ValueFactoryImpl.getInstance().createURI("http://p.org/sub2") };
        
        for(int i = 0; i < 2; i++)
        {
            final PoddRdfProcessorFactory<PoddRdfProcessor> mockFactory = Mockito.mock(PoddRdfProcessorFactory.class);
            Mockito.when(mockFactory.getSPARQLConstructBGP()).thenReturn(this.testBGPArray[i]);
            Mockito.when(mockFactory.getSPARQLConstructWhere()).thenReturn(this.testWhereArray[i]);
            Mockito.when(mockFactory.getSPARQLVariable()).thenReturn("s");
            Mockito.when(mockFactory.getSPARQLGroupBy()).thenReturn(this.testGroupByArray[i]);
            
            final String sparql = PoddRdfUtils.buildSparqlConstructQuery(mockFactory, subjects[i]);
            Assert.assertNotNull(sparql);
            Assert.assertFalse(sparql.isEmpty());
            
            String expectedSparql =
                    "CONSTRUCT { " + this.testBGPArray[i] + " } WHERE { " + this.testWhereArray[i]
                            + " } VALUES (?s) { (<" + subjects[i] + "> ) }";
            if(!this.testGroupByArray[i].isEmpty())
            {
                expectedSparql = expectedSparql + " GROUP BY " + this.testGroupByArray[i];
            }
            
            Assert.assertEquals("SPARQL Query was not as expected", expectedSparql, sparql);
        }
    }
    
}
