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
        String[] resourcePaths = {"/test/artifacts/basic-20130206.ttl", "/test/artifacts/basic-1-internal-object.rdf"};
        RDFFormat[] formats = {RDFFormat.TURTLE, RDFFormat.RDFXML};
        boolean[] expectedOutputs = {true, true};
        
        for (int i = 0; i < resourcePaths.length; i++)
        {
            final InputStream inputStream = this.getClass().getResourceAsStream(resourcePaths[i]);
            Assert.assertNotNull("Null resource", inputStream);
            
            boolean isConnected = RdfUtility.validateArtifactStructure(inputStream, formats[i]);
            Assert.assertEquals("Not the expected validity", expectedOutputs[i], isConnected);
        }
    }


}
