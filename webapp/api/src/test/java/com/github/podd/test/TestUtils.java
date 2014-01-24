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
package com.github.podd.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.Assert;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.util.Namespaces;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.rio.RioMemoryTripleSource;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.test.AbstractPoddOWLManagerTest;
import com.github.podd.exception.PoddException;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class TestUtils
{
    /**
     * Adds a Test User to the PODD Realm.
     * 
     * @param application
     */
    public static void setupTestUser(final PoddWebServiceApplication application)
    {
        final PoddSesameRealm nextRealm = application.getRealm();
        
        final URI testUserHomePage = PODD.VF.createURI("http://www.example.com/testUser");
        final PoddUser testUser =
                new PoddUser(RestletTestUtils.TEST_USERNAME, RestletTestUtils.TEST_PASSWORD, "Test", "User",
                        "test.user@example.com", PoddUserStatus.ACTIVE, testUserHomePage, "CSIRO", "Orcid-Test-User");
        final URI testUserUri = nextRealm.addUser(testUser);
        nextRealm.map(testUser, PoddRoles.PROJECT_CREATOR.getRole());
        nextRealm.map(testUser, PoddRoles.PROJECT_ADMIN.getRole(), PODD.TEST_ARTIFACT);
        
        // ApplicationUtils.log.debug("Added Test User to PODD: {} <{}>", testUser.getIdentifier(),
        // testUserUri);
    }
    
    public static List<InferredOWLOntologyID> loadSchemaOntologies(final String schemaManifest,
            final int expectedSchemaOntologies, final PoddSchemaManager schemaManager) throws OpenRDFException,
        IOException, OWLException, PoddException
    {
        Model model = null;
        try (final InputStream schemaManifestStream = TestUtils.class.getResourceAsStream(schemaManifest);)
        {
            final RDFFormat format = Rio.getParserFormatForFileName(schemaManifest, RDFFormat.RDFXML);
            model = Rio.parse(schemaManifestStream, "", format);
        }
        
        Assert.assertNotNull("Manifest was not loaded", model);
        Assert.assertFalse("Manifest was not loaded correctly", model.isEmpty());
        
        final List<InferredOWLOntologyID> schemaOntologies = schemaManager.uploadSchemaOntologies(model);
        
        Assert.assertEquals("Unexpected number of schema ontologies loaded", expectedSchemaOntologies,
                schemaOntologies.size());
        
        for(final InferredOWLOntologyID nextSchema : schemaOntologies)
        {
            Assert.assertNotNull("Ontology IRI was null for schema", nextSchema.getOntologyIRI());
            Assert.assertNotNull("Version IRI was null for schema: " + nextSchema, nextSchema.getVersionIRI());
            Assert.assertNotNull("Inferred IRI was null for schema: " + nextSchema, nextSchema.getInferredOntologyIRI());
        }
        
        final ConcurrentMap<URI, URI> currentVersionsMap = new ConcurrentHashMap<>(schemaOntologies.size());
        
        // Find current version for each schema ontology
        for(final InferredOWLOntologyID nextSchemaOntologyId : schemaOntologies)
        {
            OntologyUtils.mapCurrentVersion(model, currentVersionsMap, nextSchemaOntologyId.getOntologyIRI()
                    .toOpenRDFURI());
        }
        
        for(final Entry<URI, URI> nextEntry : currentVersionsMap.entrySet())
        {
            for(final InferredOWLOntologyID nextSchema : schemaOntologies)
            {
                if(nextSchema.getOntologyIRI().toOpenRDFURI().equals(nextEntry.getKey())
                        && nextSchema.getVersionIRI().toOpenRDFURI().equals(nextEntry.getValue()))
                {
                    schemaManager.setCurrentSchemaOntologyVersion(nextSchema);
                }
            }
        }
        
        return schemaOntologies;
    }
    
    /**
     * This internal method loads the default schema ontologies to PODD. Should be used as a setUp()
     * mechanism where needed.
     */
    public static List<InferredOWLOntologyID> loadDefaultSchemaOntologies(final PoddSchemaManager schemaManager)
        throws Exception
    {
        // NOTE: Update the number 12 here when updates are made to the schema manifest used by this
        // test
        return TestUtils.loadSchemaOntologies(PODD.PATH_DEFAULT_SCHEMAS, 12, schemaManager);
    }
    
    /**
     * Private default constructor
     */
    private TestUtils()
    {
    }
    
    public static RioMemoryTripleSource getRioTripleSource(final String classpath) throws RDFParseException,
        UnsupportedRDFormatException, IOException
    {
        final InputStream inputStream = AbstractPoddOWLManagerTest.class.getResourceAsStream(classpath);
        final Model statements =
                Rio.parse(inputStream, "", Rio.getParserFormatForFileName(classpath, RDFFormat.RDFXML));
        
        final RioMemoryTripleSource owlSource =
                new RioMemoryTripleSource(statements.iterator(), Namespaces.asMap(statements.getNamespaces()));
        
        return owlSource;
    }
}
