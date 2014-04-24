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
/**
 *
 */
package com.github.podd.api.data.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.podd.api.data.DataReference;
import com.github.podd.api.data.SSHFileReference;

/**
 * Simple abstract test class for SSHFileReference
 *
 * @author kutila
 */
public abstract class AbstractSSHFileReferenceTest extends AbstractDataReferenceTest
{
    protected SSHFileReference sshFileReference;

    @Override
    protected final DataReference getNewDataReference()
    {
        return this.getNewSSHFileReference();
    }

    /**
     *
     * @return A new SSHFileReference instance for use by the test
     */
    protected abstract SSHFileReference getNewSSHFileReference();

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        this.sshFileReference = this.getNewSSHFileReference();
    }

    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        this.sshFileReference = null;
    }

    @Test
    public void testGetFilename() throws Exception
    {
        this.sshFileReference.getFilename();
    }

    @Test
    public void testGetPath() throws Exception
    {
        this.sshFileReference.getPath();
    }

    @Test
    public void testSetFilename() throws Exception
    {
        this.sshFileReference.setFilename("plant-134.54-imageset-12343452.zip");
    }

    @Test
    public void testSetPath() throws Exception
    {
        this.sshFileReference.setPath("/path/to/file");
    }

}
