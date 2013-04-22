/**
 * 
 */
package com.github.podd.restlet.test;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class SearchOntologyResourceImplTest extends AbstractResourceImplTest
{
    
    private Model internalTestSearchRdf(final String searchTerm, final String[] searchTypes,
            final MediaType requestMediaType) throws Exception
    {
        // prepare: add an artifact
        final InferredOWLOntologyID testArtifact =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SEARCH));
        
        searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCHTERM, searchTerm);
        searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, testArtifact.getOntologyIRI()
                .toString());
        
        for(final String searchType : searchTypes)
        {
            searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCH_TYPES, searchType);
        }
        
        // invoke the search resource
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(searchClientResource, Method.GET, null, requestMediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // construct a Model out of the result
        final Model resultModel = new LinkedHashModel();
        final RDFParser parser =
                Rio.createParser(Rio.getWriterFormatForMIMEType(requestMediaType.getName(), RDFFormat.RDFXML));
        parser.setRDFHandler(new StatementCollector(resultModel));
        parser.parse(results.getStream(), "");
        
        return resultModel;
    }
    
    /**
     * Test successful search for a Platform in RDF
     */
    @Test
    public void testSearchRdfForPlatforms() throws Exception
    {
        final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#Platform", OWL.THING.stringValue() };
        final MediaType requestMediaType = MediaType.APPLICATION_RDF_XML;
        
        final Model resultModel = this.internalTestSearchRdf("me", searchTypes, requestMediaType);
        
        // verify:
        Assert.assertEquals("Not the expected number of results", 9, resultModel.size());
        Assert.assertEquals("Expected Platform SPAD Meter not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VALUE_FACTORY.createLiteral("SPAD Meter")).size());
        Assert.assertEquals("Expected Platform Pyrometer not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VALUE_FACTORY.createLiteral("Pyrometer")).size());
        Assert.assertEquals("Expected Platform SC1 Porometer not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VALUE_FACTORY.createLiteral("SC1 Porometer")).size());
    }
    
    /**
     * Test successful search for PoddScience:Sex values in RDF
     */
    @Test
    public void testSearchRdfForSex() throws Exception
    {
        final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#Sex" };
        final MediaType requestMediaType = MediaType.APPLICATION_RDF_XML;
        
        final Model resultModel = this.internalTestSearchRdf("", searchTypes, requestMediaType);
        
        Assert.assertEquals("Not the expected number of results", 5, resultModel.size());
        Assert.assertEquals("Value Hermaphrodite not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VALUE_FACTORY.createLiteral("Hermaphrodite")).size());
        Assert.assertEquals("Value Male not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VALUE_FACTORY.createLiteral("Male")).size());
    }
    
    /**
     * 
     * TODO: still identifies as RDF/XML at server resource
     * 
     */
    @Ignore
    @Test
    public void testSearchTurtle() throws Exception
    {
        final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#Sex" };
        final MediaType requestMediaType = MediaType.APPLICATION_RDF_TURTLE;
        
        final Model resultModel = this.internalTestSearchRdf("", searchTypes, requestMediaType);
        
        Assert.assertEquals("Not the expected number of results", 5, resultModel.size());
        Assert.assertEquals("Value Hermaphrodite not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VALUE_FACTORY.createLiteral("Hermaphrodite")).size());
        Assert.assertEquals("Value Male not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VALUE_FACTORY.createLiteral("Male")).size());
    }
    
    /**
     */
    @Ignore
    @Test
    public void testErrorSearchRdf_Cause1() throws Exception
    {
    }
    
    @Ignore
    @Test
    public void testErrorSearchRdf_Cause2() throws Exception
    {
    }
    
}
