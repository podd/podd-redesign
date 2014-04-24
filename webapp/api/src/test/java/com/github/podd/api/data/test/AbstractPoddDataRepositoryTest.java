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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.github.podd.api.data.DataReference;
import com.github.podd.api.data.PoddDataRepository;
import com.github.podd.exception.DataReferenceNotSupportedException;
import com.github.podd.exception.DataRepositoryIncompleteException;

/**
 *
 * @author kutila
 */
public abstract class AbstractPoddDataRepositoryTest<T extends DataReference>
{
    private final String testAliasGood = "test_alias";
    private final String testAliasBad = "no_such_alias";

    protected PoddDataRepository<T> testDataRepository;

    protected final String getAliasGood()
    {
        return this.testAliasGood;
    }

    /**
     * @return A Collection of URIs representing the expected types of the test FileRepository
     *         instance
     */
    protected abstract Collection<URI> getExpectedTypes() throws Exception;

    /**
     * @return A Collection of Models that do not contain sufficient information to create a
     *         FileRepository object
     */
    protected abstract Map<Resource, Model> getIncompleteModels();

    /**
     * @return A new DataReference instance that must not be validated.
     */
    protected abstract T getNewNonValidatingDataReference() throws Exception;

    /**
     * @return A new {@link PoddDataRepository} instance for use by the test
     */
    protected abstract PoddDataRepository<T> getNewPoddDataRepository(final Resource nextDataRepository,
            final Model model) throws Exception;

    /**
     * @return A new {@link PoddDataRepository} instance for use by the test
     */
    protected abstract PoddDataRepository<T> getNewPoddDataRepository() throws Exception;

    /**
     * @return A new DataReference instance that will be validated by this repository.
     */
    protected abstract T getNewValidatingDataReference() throws Exception;

    @Before
    public void setUp() throws Exception
    {
        this.testDataRepository = this.getNewPoddDataRepository();
    }

    /**
     * Start a File Repository source for test
     */
    protected abstract void startRepositorySource() throws Exception;

    /**
     * Stop the test File Repository source
     */
    protected abstract void stopRepositorySource() throws Exception;

    @After
    public void tearDown() throws Exception
    {
        this.testDataRepository = null;
    }

    @Test
    public void testCanHandle() throws Exception
    {
        final T dataReference = this.getNewNonValidatingDataReference();
        dataReference.setRepositoryAlias(this.testAliasGood);

        Assert.assertTrue("Repository should be able to handle this file reference",
                this.testDataRepository.canHandle(dataReference));
    }

    @Test
    public void testCanHandleWithDifferentAliases() throws Exception
    {
        final T dataReference = this.getNewNonValidatingDataReference();
        dataReference.setRepositoryAlias(this.testAliasBad);

        Assert.assertFalse("Repository should not be able to handle this file reference",
                this.testDataRepository.canHandle(dataReference));
    }

    @Test
    public void testCanHandleWithNullReference() throws Exception
    {
        Assert.assertFalse("Repository should not be able to handle NULL file reference",
                this.testDataRepository.canHandle(null));
    }

    @Test
    public void testCreateFileRepositoryWithIncompleteModel() throws Exception
    {
        final Map<Resource, Model> incompleteModels = this.getIncompleteModels();

        for(final Resource nextDataRepository : incompleteModels.keySet())
        {
            try
            {
                this.getNewPoddDataRepository(nextDataRepository, incompleteModels.get(nextDataRepository));
                Assert.fail("Should have thrown an IncompleteFileRepositoryException");
            }
            catch(final DataRepositoryIncompleteException e)
            {
                Assert.assertTrue(e.getMessage().contains("repository configuration incomplete"));
            }
        }
    }

    @Test
    public void testGetAlias() throws Exception
    {
        final String alias = this.testDataRepository.getAlias();
        Assert.assertNotNull("NULL alias", alias);
    }

    @Test
    public void testGetTypes() throws Exception
    {
        final Set<URI> types = this.testDataRepository.getTypes();
        Assert.assertNotNull("NULL types", types);
        Assert.assertEquals("Expected 2 TYPEs", 2, types.size());
        final Collection<URI> expectedTypes = this.getExpectedTypes();
        for(final URI uri : expectedTypes)
        {
            Assert.assertTrue("Expected TYPE missing", types.contains(uri));
        }
    }

    /**
     * This test starts up an internal file repository source and therefore can be slow.
     */
    @Test
    public void testValidateSuccess() throws Exception
    {
        try
        {
            final T dataReference = this.getNewValidatingDataReference();
            dataReference.setRepositoryAlias(this.testAliasGood);
            this.startRepositorySource();
            Assert.assertTrue("File Reference should have been valid", this.testDataRepository.validate(dataReference));
        }
        finally
        {
            this.stopRepositorySource();
        }
    }

    /**
     * The internal file repository source is not started since this test fails before reaching a
     * stage where the remote repository is accessed.
     */
    @Test
    public void testValidateWithAliasMismatch() throws Exception
    {
        try
        {
            final T dataReference = this.getNewValidatingDataReference();
            dataReference.setRepositoryAlias(this.testAliasBad);
            this.testDataRepository.validate(dataReference);
            Assert.fail("Should have thrown a DataReferenceNotSupportedException");
        }
        catch(final DataReferenceNotSupportedException e)
        {
            Assert.assertEquals("cannot handle file reference for validation", e.getMessage());
        }
    }

    /**
     * This test starts up an internal file repository source and therefore can be slow.
     */
    @Test
    public void testValidateWithNonExistentFile() throws Exception
    {
        try
        {
            final T dataReference = this.getNewNonValidatingDataReference();
            dataReference.setRepositoryAlias(this.testAliasGood);
            this.startRepositorySource();
            Assert.assertFalse("File Reference should be invalid", this.testDataRepository.validate(dataReference));
        }
        finally
        {
            this.stopRepositorySource();
        }
    }

}
