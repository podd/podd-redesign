/**
 * 
 */
package com.github.podd.utils.test;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;

import com.github.podd.utils.RdfUtility;

/**
 * @author kutila
 *
 */
public class RdfUtilityTest
{
  
    @Test
    public void testValidateArtifactConnectedness() throws Exception
    {
        Object[][] testData = {
                {"/test/artifacts/3-topobjects.ttl", RDFFormat.TURTLE, false},
                {"/test/artifacts/basic-20130206.ttl", RDFFormat.TURTLE, true},
                {"/test/artifacts/basic-1-internal-object.rdf", RDFFormat.RDFXML, false},
        };
        
        for (int i = 0; i < testData.length; i++)
        {
            final InputStream inputStream = this.getClass().getResourceAsStream((String)testData[i][0]);
            Assert.assertNotNull("Null resource", inputStream);
            
            boolean isConnected = RdfUtility.isConnectedStructure(inputStream, (RDFFormat)testData[i][1]);
            Assert.assertEquals("Not the expected validity", testData[i][2], isConnected);
        }
    }


}
