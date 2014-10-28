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
package com.github.podd.utils.test;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.github.podd.api.PoddRdfProcessor;
import com.github.podd.api.PoddRdfProcessorFactory;
import com.github.podd.utils.PoddRdfProcessorUtils;

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
            
            final String sparql = PoddRdfProcessorUtils.buildSparqlConstructQuery(mockFactory);
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
            
            final String sparql = PoddRdfProcessorUtils.buildSparqlConstructQuery(mockFactory, subjects[i]);
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
