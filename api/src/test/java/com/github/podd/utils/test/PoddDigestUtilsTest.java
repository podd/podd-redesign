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

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.podd.utils.PoddDigestUtils;

public class PoddDigestUtilsTest
{
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    
    private Path testDir;
    
    @Before
    public void setUp() throws Exception
    {
        testDir = tempDir.newFolder("podddigestutils").toPath();
    }
    
    @After
    public void tearDown() throws Exception
    {
        testDir = null;
    }
    
    @Test
    public final void testGetDigests() throws Exception
    {
        Path emptyFile = testDir.resolve("emptyfile.txt");
        Files.createFile(emptyFile);
        
        Assert.assertTrue(Files.exists(emptyFile));
        Assert.assertEquals(0, Files.size(emptyFile));
        
        ConcurrentMap<Path, ConcurrentMap<String, String>> digests =
                PoddDigestUtils.getDigests(Arrays.asList(emptyFile));
        
        Assert.assertEquals(1, digests.size());
        
        Assert.assertTrue(digests.containsKey(emptyFile));
        
        ConcurrentMap<String, String> emptyFileDigests = digests.get(emptyFile);
        
        Assert.assertTrue(emptyFileDigests.containsKey("MD5"));
        Assert.assertTrue(emptyFileDigests.containsKey("SHA-1"));
        
        Assert.assertEquals("d41d8cd98f00b204e9800998ecf8427e", emptyFileDigests.get("MD5"));
        Assert.assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", emptyFileDigests.get("SHA-1"));
    }
    
}
