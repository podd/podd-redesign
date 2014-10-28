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
package com.github.podd.api.data.test;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.data.DataReference;
import com.github.podd.api.data.PoddDataRepository;
import com.github.podd.api.data.PoddDataRepositoryManager;
import com.github.podd.api.test.TestConstants;
import com.github.podd.exception.DataReferenceInvalidException;
import com.github.podd.exception.DataReferenceVerificationException;
import com.github.podd.exception.DataRepositoryException;
import com.github.podd.exception.DataRepositoryIncompleteException;
import com.github.podd.exception.DataRepositoryMappingNotFoundException;
import com.github.podd.utils.PODD;

/**
 * Abstract test to verify that the PoddDataRepositoryManager API contract is followed by
 * implementations.
 *
 * @author kutila
 */
public abstract class AbstractPoddDataRepositoryManagerTest
{
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public static final String TEST_ALIAS_1A = "alias-1-alpha";
    public static final String TEST_ALIAS_1B = "alias-1-beta";
    public static final String TEST_ALIAS_2A = "alias-2-alpha";
    public static final String TEST_ALIAS_2B = "alias-2-beta";
    
    protected PoddDataRepositoryManager testDataRepositoryManager;
    protected PoddRepositoryManager testRepositoryManager;
    
    /**
     * Build a File Repository object in memory with the given alias and Model. ALl other method
     * implementations may be empty.
     *
     * @param alias
     *            The alias to be assigned to this object
     * @param model
     *            A Model containing implementation-specific configurations for this object
     * @return A valid PoddDataRepository instance
     */
    protected abstract PoddDataRepository<?> buildDataRepositoryInstance(final String alias, final Model model);
    
    /**
     * Create a {@link Model} that can be used to construct a File Repository. Implementation
     * specific default values should be set by this method such that the returned {@link Model} is
     * complete.
     *
     * @param aliasUri
     *            The URI to use as the subject in the Model
     * @param aliases
     *            The aliases which should be set in the Model
     * @return
     */
    protected abstract Model buildModelForDataRepository(final URI aliasUri, final String... aliases);
    
    protected abstract DataReference getNewInvalidDataReference() throws Exception;
    
    /**
     * {@link PoddDataRepositoryManager} is the object under test in this class.
     *
     * @return A new {@link PoddDataRepositoryManager} instance for use by the test
     * @throws Exception
     */
    protected abstract PoddDataRepositoryManager getNewPoddDataRepositoryManager() throws Exception;
    
    protected abstract DataReference getNewValidDataReference() throws Exception;
    
    @Before
    public void setUp() throws Exception
    {
        this.testDataRepositoryManager = this.getNewPoddDataRepositoryManager();
        this.testRepositoryManager = this.testDataRepositoryManager.getRepositoryManager();
        
        final RepositoryConnection conn = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            conn.begin();
            
            final URI context = this.testRepositoryManager.getFileRepositoryManagementGraph();
            
            // repository configuration with 1 mapped alias
            final URI alias1Uri = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/alias/1-alpha");
            final Model model1 =
                    this.buildModelForDataRepository(alias1Uri, AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_1A);
            conn.add(model1, context);
            
            // repository configuration with 2 mapped aliases
            final URI alias2Uri = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/alias/2-beta");
            final Model model2 =
                    this.buildModelForDataRepository(alias2Uri, AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A,
                            AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2B);
            conn.add(model2, context);
            
            conn.commit();
        }
        finally
        {
            conn.close();
        }
    }
    
    protected abstract void startRepositorySource() throws Exception;
    
    protected abstract void stopRepositorySource() throws Exception;
    
    @After
    public void tearDown() throws Exception
    {
        // final Repository repository =
        // this.testRepositoryManager.getManagementRepositoryConnection();
        // repository.shutDown();
        
        this.testDataRepositoryManager = null;
        this.testRepositoryManager = null;
    }
    
    @Test
    public void testAddRepositoryMappingToExistingRepositoryConfiguration() throws Exception
    {
        // prepare:
        final String alias = "alias-1-gamma";
        final PoddDataRepository<?> existingFileRepository =
                this.testDataRepositoryManager.getRepository(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_1A);
        Assert.assertNotNull("FileRepository was NULL", existingFileRepository);
        
        this.testDataRepositoryManager.addRepositoryMapping(alias, existingFileRepository);
        
        // verify:
        final PoddDataRepository<?> mappedRepository = this.testDataRepositoryManager.getRepository(alias);
        Assert.assertNotNull("Alias mapping returns NULL", mappedRepository);
    }
    
    /**
     * Tests addRepositoryMapping() when the input FileRepository's internal Model is NULL.
     */
    @Test
    public void testAddRepositoryMappingWithIncompleteRepositoryConfiguration1() throws Exception
    {
        // prepare:
        final String alias = "alias-1-gamma";
        final PoddDataRepository<?> dataRepository = this.buildDataRepositoryInstance(alias, null);
        
        Assert.assertNotNull("FileRepository was NULL", dataRepository);
        
        try
        {
            this.testDataRepositoryManager.addRepositoryMapping(alias, dataRepository);
            Assert.fail("Should have thrown a DataRepositoryIncompleteException");
        }
        catch(final DataRepositoryIncompleteException e)
        {
            Assert.assertEquals("Not the expected error message", "Incomplete File Repository since Model is empty",
                    e.getMessage());
        }
    }
    
    /**
     * Tests addRepositoryMapping() when the input FileRepository's internal Model uses a URI that
     * already exists in the management graph.
     */
    @Test
    public void testAddRepositoryMappingWithIncompleteRepositoryConfiguration2() throws Exception
    {
        // prepare:
        final String alias = "alias-1-gamma";
        final URI duplicateAliasUri = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/alias/1-alpha");
        final PoddDataRepository<?> dataRepository =
                this.buildDataRepositoryInstance(alias, this.buildModelForDataRepository(duplicateAliasUri, alias));
        
        Assert.assertNotNull("FileRepository was NULL", dataRepository);
        
        try
        {
            this.testDataRepositoryManager.addRepositoryMapping(alias, dataRepository);
            Assert.fail("Should have thrown a DataRepositoryIncompleteException");
        }
        catch(final DataRepositoryIncompleteException e)
        {
            Assert.assertTrue("Not the expected error message",
                    e.getMessage().contains("Subject URIs used in Model already exist in Management Graph"));
        }
    }
    
    @Test
    public void testAddRepositoryMappingWithNewRepositoryConfiguration() throws Exception
    {
        // prepare:
        final String alias = "alias-1-gamma";
        final URI aliasUri = ValueFactoryImpl.getInstance().createURI("http://purl.org/alias-1-gamma");
        final PoddDataRepository<?> dataRepository =
                this.buildDataRepositoryInstance(alias, this.buildModelForDataRepository(aliasUri, alias));
        
        Assert.assertNotNull("FileRepository was NULL", dataRepository);
        
        this.testDataRepositoryManager.addRepositoryMapping(alias, dataRepository);
        
        // verify:
        final PoddDataRepository<?> mappedRepository = this.testDataRepositoryManager.getRepository(alias);
        Assert.assertNotNull("Alias mapping returns NULL", mappedRepository);
    }
    
    @Test
    public void testAddRepositoryMappingWithNullAlias() throws Exception
    {
        final PoddDataRepository<?> dataRepository =
                this.buildDataRepositoryInstance(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_1A, null);
        try
        {
            this.testDataRepositoryManager.addRepositoryMapping(null, dataRepository);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertEquals("Not the expected error message", "Cannot add NULL as a File Repository mapping",
                    e.getMessage());
        }
    }
    
    @Test
    public void testAddRepositoryMappingWithNullRepository() throws Exception
    {
        final PoddDataRepository<?> dataRepository = null;
        try
        {
            this.testDataRepositoryManager.addRepositoryMapping(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A,
                    dataRepository);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertEquals("Not the expected error message", "Cannot add NULL as a File Repository mapping",
                    e.getMessage());
        }
    }
    
    @Ignore
    @Test
    public void testDownloadFileReference() throws Exception
    {
        // TODO - implement me
    }
    
    @Test
    public void testGetAllAliasesSuccess() throws Exception
    {
        final List<String> aliases = this.testDataRepositoryManager.getAllAliases();
        
        // verify:
        Assert.assertNotNull("NULL list of aliases", aliases);
        Assert.assertEquals("Not the expected number of aliases", 3, aliases.size());
        Assert.assertTrue("Expected alias missing",
                aliases.contains(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_1A));
        Assert.assertTrue("Expected alias missing",
                aliases.contains(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A));
        Assert.assertTrue("Expected alias missing",
                aliases.contains(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2B));
    }
    
    @Test
    public void testGetAllAliasesWhenEmpty() throws Exception
    {
        // prepare: clean up any aliases that already exist
        final RepositoryConnection connection = this.testRepositoryManager.getManagementRepositoryConnection();
        final URI dataRepositoryManagementGraph = this.testRepositoryManager.getFileRepositoryManagementGraph();
        connection.clear(dataRepositoryManagementGraph);
        connection.close();
        
        final List<String> aliases = this.testDataRepositoryManager.getAllAliases();
        
        // verify:
        Assert.assertNotNull("NULL list of aliases", aliases);
        Assert.assertEquals("Not the expected number of aliases", 0, aliases.size());
    }
    
    @Test
    public void testGetEquivalentAliasesSuccess() throws Exception
    {
        final List<String> aliases =
                this.testDataRepositoryManager
                        .getEquivalentAliases(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A);
        
        // verify:
        Assert.assertNotNull("NULL list of aliases", aliases);
        Assert.assertEquals("Not the expected number of aliases", 2, aliases.size());
        Assert.assertTrue("Expected alias missing",
                aliases.contains(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A));
        Assert.assertTrue("Expected alias missing",
                aliases.contains(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2B));
    }
    
    @Test
    public void testGetEquivalentAliasesWithNonExistentAlias() throws Exception
    {
        final List<String> aliases = this.testDataRepositoryManager.getEquivalentAliases("no_such_alias");
        
        // verify:
        Assert.assertNotNull("NULL list of aliases", aliases);
        Assert.assertEquals("Expected no aliases", 0, aliases.size());
    }
    
    @Test
    public void testGetEquivalentAliasesWithNullAlias() throws Exception
    {
        try
        {
            this.testDataRepositoryManager.getEquivalentAliases(null);
            Assert.fail("Should have thrown a NULLPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testGetRepositoryAliasesSuccess() throws Exception
    {
        final PoddDataRepository<?> dataRepository =
                this.buildDataRepositoryInstance(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A, null);
        
        final List<String> aliases = this.testDataRepositoryManager.getRepositoryAliases(dataRepository);
        
        // verify:
        Assert.assertNotNull("NULL list of aliases", aliases);
        Assert.assertEquals("Not the expected number of aliases", 2, aliases.size());
        Assert.assertTrue("Expected alias missing",
                aliases.contains(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A));
        Assert.assertTrue("Expected alias missing",
                aliases.contains(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2B));
    }
    
    @Test
    public void testGetRepositoryAliasesWithNonExistentRepository() throws Exception
    {
        final PoddDataRepository<?> dataRepository = this.buildDataRepositoryInstance("no_such_alias", null);
        
        final List<String> aliases = this.testDataRepositoryManager.getRepositoryAliases(dataRepository);
        
        // verify:
        Assert.assertNotNull("NULL list of aliases", aliases);
        Assert.assertEquals("Expected no aliases", 0, aliases.size());
    }
    
    @Test
    public void testGetRepositoryAliasesWithNullFileRepository() throws Exception
    {
        try
        {
            this.testDataRepositoryManager.getRepositoryAliases((PoddDataRepository<?>)null);
            Assert.fail("Should have thrown a NULLPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testGetRepositoryHavingMultipleAliases() throws Exception
    {
        final PoddDataRepository<?> repository =
                this.testDataRepositoryManager.getRepository(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A);
        
        Assert.assertNotNull("FileRepository was NULL", repository);
        Assert.assertEquals("Not the expected alias", AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A,
                repository.getAlias());
        Assert.assertNotNull("FileRepository has NULL types", repository.getTypes());
        Assert.assertEquals("Not the expected no. of types", 2, repository.getTypes().size());
    }
    
    @Test
    public void testGetRepositoryHavingSingleAlias() throws Exception
    {
        final PoddDataRepository<?> repository =
                this.testDataRepositoryManager.getRepository(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_1A);
        
        Assert.assertNotNull("FileRepository was NULL", repository);
        Assert.assertEquals("Not the expected alias", AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_1A,
                repository.getAlias());
        Assert.assertNotNull("FileRepository has NULL types", repository.getTypes());
        Assert.assertEquals("Not the expected no. of types", 2, repository.getTypes().size());
    }
    
    @Test
    public void testGetRepositoryWithNonExistentAlias() throws Exception
    {
        final PoddDataRepository<?> repository = this.testDataRepositoryManager.getRepository("no_such_alias");
        Assert.assertNull("FileRepository should be NULL", repository);
    }
    
    @Ignore("No reason to be attempting to find a repository using a null alias without having an exception thrown")
    @Test
    public void testGetRepositoryWithNullAlias() throws Exception
    {
        final PoddDataRepository<?> repository = this.testDataRepositoryManager.getRepository(null);
        Assert.assertNull("FileRepository should be NULL", repository);
    }
    
    @Test
    public void testInitSuccessDefault() throws Exception
    {
        // prepare: clean up any aliases that already exist
        final RepositoryConnection connection = this.testRepositoryManager.getManagementRepositoryConnection();
        final URI dataRepositoryManagementGraph = this.testRepositoryManager.getFileRepositoryManagementGraph();
        connection.clear(dataRepositoryManagementGraph);
        connection.close();
        Assert.assertEquals("File Repository Graph was not cleaned properly", 0, this.testDataRepositoryManager
                .getAllAliases().size());
        
        try (final InputStream input = this.getClass().getResourceAsStream(PODD.PATH_DEFAULT_ALIASES_FILE))
        {
            this.testDataRepositoryManager.initialise(Rio.parse(input, "", RDFFormat.TURTLE));
        }
        
        // verify:
        final List<String> allAliases = this.testDataRepositoryManager.getAllAliases();
        Assert.assertEquals("Expected 0 alias", 0, allAliases.size());
    }
    
    @Test
    public void testInitSuccessTest() throws Exception
    {
        // prepare: clean up any aliases that already exist
        final RepositoryConnection connection = this.testRepositoryManager.getManagementRepositoryConnection();
        final URI dataRepositoryManagementGraph = this.testRepositoryManager.getFileRepositoryManagementGraph();
        connection.clear(dataRepositoryManagementGraph);
        connection.close();
        Assert.assertEquals("File Repository Graph was not cleaned properly", 0, this.testDataRepositoryManager
                .getAllAliases().size());
        
        try (final InputStream input = this.getClass().getResourceAsStream("/test/test-alias.ttl"))
        {
            this.testDataRepositoryManager.initialise(Rio.parse(input, "", RDFFormat.TURTLE));
        }
        
        // verify:
        final List<String> allAliases = this.testDataRepositoryManager.getAllAliases();
        Assert.assertEquals("Expected 1 alias", 1, allAliases.size());
        Assert.assertEquals("alias_local_ssh", allAliases.get(0));
    }
    
    @Test
    public void testInitWithInvalidAliasFile() throws Exception
    {
        // prepare: clean up any aliases that already exist
        final RepositoryConnection connection = this.testRepositoryManager.getManagementRepositoryConnection();
        final URI dataRepositoryManagementGraph = this.testRepositoryManager.getFileRepositoryManagementGraph();
        connection.begin();
        connection.clear(dataRepositoryManagementGraph);
        connection.commit();
        connection.close();
        Assert.assertEquals("File Repository Graph was not cleaned properly", 0, this.testDataRepositoryManager
                .getAllAliases().size());
        
        try (final InputStream input = this.getClass().getResourceAsStream(TestConstants.TEST_ALIAS_BAD))
        {
            Assert.assertNotNull("Could not find test resource", input);
            // initializing with a Turtle file which is an inconsistent "alias" file
            this.testDataRepositoryManager.initialise(Rio.parse(input, "", RDFFormat.TURTLE));
        }
        
        // verify:
        final List<String> allAliases = this.testDataRepositoryManager.getAllAliases();
        Assert.assertEquals("Expected 0 aliases", 0, allAliases.size());
    }
    
    @Test
    public void testInitWithNonExistentAliasFile() throws Exception
    {
        // prepare: clean up any aliases that already exist
        final RepositoryConnection connection = this.testRepositoryManager.getManagementRepositoryConnection();
        final URI dataRepositoryManagementGraph = this.testRepositoryManager.getFileRepositoryManagementGraph();
        connection.begin();
        connection.clear(dataRepositoryManagementGraph);
        connection.commit();
        connection.close();
        Assert.assertEquals("File Repository Graph was not cleaned properly", 0, this.testDataRepositoryManager
                .getAllAliases().size());
        
        try (final InputStream input = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206 + "aa"))
        {
            this.testDataRepositoryManager.initialise(Rio.parse(input, "", RDFFormat.TURTLE));
            Assert.fail("Should have thrown an Exception here");
        }
        catch(final Exception e)
        {
            Assert.assertTrue(e.getMessage().contains("Input stream"));
        }
    }
    
    @Test
    public void testRemoveRepositoryMappingOnlyAlias() throws Exception
    {
        final PoddDataRepository<?> removedRepositoryMapping =
                this.testDataRepositoryManager
                        .removeRepositoryMapping(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_1A);
        
        // verify:
        Assert.assertNotNull("Removed Mapping is NULL", removedRepositoryMapping);
        Assert.assertEquals("Removed alias is incorrect", AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_1A,
                removedRepositoryMapping.getAlias());
        Assert.assertNull("A mapping still exists for this Alias",
                this.testDataRepositoryManager.getRepository(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_1A));
    }
    
    @Test
    public void testRemoveRepositoryMappingWhenMultipleAliasesExist() throws Exception
    {
        // prepare:
        final List<String> existingAliases =
                this.testDataRepositoryManager
                        .getEquivalentAliases(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A);
        Assert.assertEquals("Test setup should have 2 aliases mapped", 2, existingAliases.size());
        existingAliases.remove(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A);
        
        final PoddDataRepository<?> removedRepositoryMapping =
                this.testDataRepositoryManager
                        .removeRepositoryMapping(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A);
        
        // verify:
        Assert.assertNotNull("Removed Mapping is NULL", removedRepositoryMapping);
        Assert.assertEquals("Removed alias is incorrect", AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A,
                removedRepositoryMapping.getAlias());
        Assert.assertNull("A mapping still exists for this Alias",
                this.testDataRepositoryManager.getRepository(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A));
        
        for(final String existingAlias : existingAliases)
        {
            Assert.assertNotNull("The other alias should still be mapped",
                    this.testDataRepositoryManager.getRepository(existingAlias));
        }
    }
    
    @Test
    public void testRemoveRepositoryMappingWithNullAlias() throws Exception
    {
        try
        {
            this.testDataRepositoryManager.removeRepositoryMapping(null);
            Assert.fail("Should have thrown a NULLPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testRemoveRepositoryWithNonExistentAlias() throws Exception
    {
        try
        {
            this.testDataRepositoryManager.removeRepositoryMapping("no_such_alias");
            Assert.fail("Should have thrown a NULLPointerException");
        }
        catch(final DataRepositoryException e)
        {
            Assert.assertTrue(e instanceof DataRepositoryMappingNotFoundException);
        }
    }
    
    @Test
    public void testVerifyFileReferencesWithEmptyFileReferenceSet() throws Exception
    {
        final Set<DataReference> dataReferences = new HashSet<DataReference>();
        this.testDataRepositoryManager.verifyDataReferences(dataReferences);
    }
    
    /**
     * This test starts up an internal file repository source and therefore can be slow.
     */
    @Test
    public void testVerifyFileReferencesWithNoFailures() throws Exception
    {
        try
        {
            this.startRepositorySource();
            // prepare: create FileReferences to test
            final Set<DataReference> dataReferences = new HashSet<DataReference>();
            final DataReference fileRefWithAlias1A = this.getNewValidDataReference();
            fileRefWithAlias1A.setRepositoryAlias(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_1A);
            dataReferences.add(fileRefWithAlias1A);
            
            final DataReference fileRefWithAlias2A = this.getNewValidDataReference();
            fileRefWithAlias2A.setRepositoryAlias(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_2A);
            dataReferences.add(fileRefWithAlias2A);
            
            this.testDataRepositoryManager.verifyDataReferences(dataReferences);
        }
        finally
        {
            this.stopRepositorySource();
        }
    }
    
    @Test
    public void testVerifyFileReferencesWithNullFileReferences() throws Exception
    {
        try
        {
            this.testDataRepositoryManager.verifyDataReferences(null);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final RuntimeException e)
        {
            Assert.assertTrue(e instanceof NullPointerException);
        }
    }
    
    /**
     * This test starts up an internal file repository source and therefore can be slow.
     */
    @Test
    public void testVerifyFileReferencesWithOneFailure() throws Exception
    {
        try
        {
            this.startRepositorySource();
            
            // prepare: create FileReferences to test
            final Set<DataReference> dataReferences = new HashSet<DataReference>();
            final DataReference fileRefWithAlias1A = this.getNewValidDataReference();
            fileRefWithAlias1A.setRepositoryAlias(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_1A);
            dataReferences.add(fileRefWithAlias1A);
            
            final DataReference fileRefWithNoSuchFile = this.getNewInvalidDataReference();
            fileRefWithNoSuchFile.setRepositoryAlias(AbstractPoddDataRepositoryManagerTest.TEST_ALIAS_1A);
            dataReferences.add(fileRefWithNoSuchFile);
            
            try
            {
                this.testDataRepositoryManager.verifyDataReferences(dataReferences);
                Assert.fail("Verify should have thrown an Exception containing errors");
            }
            catch(final DataReferenceVerificationException e)
            {
                Assert.assertEquals("Expected 1 validation failure", 1, e.getValidationFailures().size());
                Assert.assertNotNull(fileRefWithNoSuchFile);
                final Throwable throwable = e.getValidationFailures().get(fileRefWithNoSuchFile);
                Assert.assertTrue("Not the expected cause of validation failure",
                        throwable instanceof DataReferenceInvalidException);
                Assert.assertEquals("Not the expected error message",
                        "Remote File Repository says this File Reference is invalid", throwable.getMessage());
            }
        }
        finally
        {
            this.stopRepositorySource();
        }
    }
    
}
