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
