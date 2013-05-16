/**
 * 
 */
package com.github.podd.restlet.test;

import org.junit.Assert;
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
import org.restlet.resource.ResourceException;

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
            final MediaType requestMediaType, final String artifactUri) throws Exception
    {
        final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SEARCH));
        
        searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCHTERM, searchTerm);
        if(artifactUri != null)
        {
            searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
        }
        
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
    
    @Test
    public void testErrorSearchRdfWithInvalidArtifactID() throws Exception
    {
        // prepare:
        final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SEARCH));
        
        searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCHTERM, "Scan");
        searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, "http://no.such.artifact");
        searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCH_TYPES,
                "http://purl.org/podd/ns/poddScience#Platform");
        
        // there is no need to authenticate or have a test artifact as the artifact ID is checked
        // for first
        try
        {
            searchClientResource.get(MediaType.APPLICATION_RDF_XML);
            Assert.fail("Should have thrown a ResourceException");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
        }
    }
    
    @Test
    public void testErrorSearchRdfWithoutAuthentication() throws Exception
    {
        // prepare:
        final InferredOWLOntologyID testArtifact =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SEARCH));
        
        searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCHTERM, "Scan");
        searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, testArtifact.getOntologyIRI()
                .toString());
        searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCH_TYPES,
                "http://purl.org/podd/ns/poddScience#Platform");
        
        // request without authentication
        try
        {
            searchClientResource.get(MediaType.APPLICATION_RDF_XML);
            Assert.fail("Should have thrown a ResourceException");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
    
    @Test
    public void testErrorSearchRdfWithoutSearchTerm() throws Exception
    {
        // prepare:
        final InferredOWLOntologyID testArtifact =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SEARCH));
        
        // no search term!
        searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, testArtifact.getOntologyIRI()
                .toString());
        searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCH_TYPES,
                "http://purl.org/podd/ns/poddScience#Platform");
        
        // there is no need to authenticate or have a test artifact as the search term is checked
        // for first
        try
        {
            searchClientResource.get(MediaType.APPLICATION_RDF_XML);
            Assert.fail("Should have thrown a ResourceException");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
        }
    }
    
    /**
     * Test successful search for a Platform in JSON format
     */
    @Test
    public void testSearchJson() throws Exception
    {
        final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#Platform", OWL.THING.stringValue() };
        final MediaType requestMediaType = MediaType.APPLICATION_JSON;
        
        final Model resultModel = this.internalTestSearchRdf("Scan", searchTypes, requestMediaType, null);
        
        Assert.assertEquals("Not the expected number of results", 5, resultModel.size());
        System.out.println(resultModel.toString());
        Assert.assertEquals("Expected Platform CabScan not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("CabScan")).size());
        Assert.assertEquals("Expected Platform PlantScan not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("PlantScan")).size());
    }
    
    /**
     * Test successful search for a FOR Codes in RDF/XML
     */
    @Test
    public void testSearchRdfForANZSRCAssertion() throws Exception
    {
        final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#ANZSRCAssertion" };
        final MediaType requestMediaType = MediaType.APPLICATION_RDF_XML;
        
        final Model resultModel = this.internalTestSearchRdf("", searchTypes, requestMediaType, null);
        
        // verify:
        Assert.assertEquals("Not the expected number of results", 4, resultModel.size());
        Assert.assertEquals("Expected Assertion 'Yes' not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("Yes")).size());
        Assert.assertEquals("Expected Assertion 'Unknown' not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("Unknown")).size());
        Assert.assertEquals("Expected Assertion 'Not Applicable' not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("Not Applicable")).size());
    }
    
    /**
     * Test successful search for a Platform in RDF/XML
     */
    @Test
    public void testSearchRdfForPlatforms() throws Exception
    {
        final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#Platform", OWL.THING.stringValue() };
        final MediaType requestMediaType = MediaType.APPLICATION_RDF_XML;
        
        final Model resultModel = this.internalTestSearchRdf("me", searchTypes, requestMediaType, null);
        
        // verify:
        Assert.assertEquals("Not the expected number of results", 9, resultModel.size());
        Assert.assertEquals("Expected Platform SPAD Meter not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("SPAD Meter")).size());
        Assert.assertEquals("Expected Platform Pyrometer not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("Pyrometer")).size());
        Assert.assertEquals("Expected Platform SC1 Porometer not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("SC1 Porometer")).size());
    }
    
    /**
     * Test successful search for PoddScience:Sex values in RDF/XML
     */
    @Test
    public void testSearchRdfForSex() throws Exception
    {
        // prepare: add an artifact
        final InferredOWLOntologyID testArtifact =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#Sex" };
        final MediaType requestMediaType = MediaType.APPLICATION_RDF_XML;
        
        final Model resultModel =
                this.internalTestSearchRdf("", searchTypes, requestMediaType, testArtifact.getOntologyIRI().toString());
        
        Assert.assertEquals("Not the expected number of results", 5, resultModel.size());
        Assert.assertEquals("Value Hermaphrodite not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("Hermaphrodite")).size());
        Assert.assertEquals("Value Male not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("Male")).size());
    }
    
    /**
     * Test successful search for a Platform in Turtle
     */
    @Test
    public void testSearchTurtle() throws Exception
    {
        final String[] searchTypes = { "http://purl.org/podd/ns/poddScience#Platform", OWL.THING.stringValue() };
        final MediaType requestMediaType = MediaType.APPLICATION_RDF_TURTLE;
        
        final Model resultModel = this.internalTestSearchRdf("Scan", searchTypes, requestMediaType, null);
        
        Assert.assertEquals("Not the expected number of results", 5, resultModel.size());
        System.out.println(resultModel.toString());
        Assert.assertEquals("Expected Platform CabScan not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("CabScan")).size());
        Assert.assertEquals("Expected Platform PlantScan not found", 1,
                resultModel.filter(null, null, PoddRdfConstants.VF.createLiteral("PlantScan")).size());
    }
    
}
