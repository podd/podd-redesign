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
package com.github.podd.utils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PoddDigestUtils
{
    /**
     * An enumeration linking the MessageDigest algorithm identifier to the file extension used to
     * store it.
     * 
     * @author Peter Ansell p_ansell@yahoo.com
     */
    public static enum Algorithm
    {
        SHA1("SHA-1", ".sha1"),
        
        MD5("MD5", ".md5");
        
        private final String name;
        private final String extension;
        
        Algorithm(final String name, final String extension)
        {
            this.name = name;
            this.extension = extension;
        }
        
        public String getExtension()
        {
            return this.extension;
        }
        
        public String getName()
        {
            return this.name;
        }
    }
    
    public static ConcurrentMap<Path, ConcurrentMap<Algorithm, String>> getDigests(final Collection<Path> pathsToDigest)
        throws IOException, NoSuchAlgorithmException
    {
        final ConcurrentMap<Path, ConcurrentMap<Algorithm, String>> result = new ConcurrentHashMap<>();
        
        for(final Path nextPath : pathsToDigest)
        {
            try (final InputStream inputStream = Files.newInputStream(nextPath))
            {
                final DigestInputStream shaStream =
                        new DigestInputStream(inputStream, MessageDigest.getInstance(Algorithm.SHA1.getName()));
                final DigestInputStream md5Stream =
                        new DigestInputStream(shaStream, MessageDigest.getInstance(Algorithm.MD5.getName()));
                int b;
                while((b = md5Stream.read()) != -1)
                {
                    // No processing needed
                }
                final byte[] shaDigest = shaStream.getMessageDigest().digest();
                final byte[] md5Digest = md5Stream.getMessageDigest().digest();
                final String shaDigestString = new BigInteger(1, shaDigest).toString(16);
                final String md5DigestString = new BigInteger(1, md5Digest).toString(16);
                ConcurrentMap<Algorithm, String> nextMap = new ConcurrentHashMap<Algorithm, String>();
                final ConcurrentMap<Algorithm, String> putIfAbsent = result.putIfAbsent(nextPath, nextMap);
                if(putIfAbsent != null)
                {
                    nextMap = putIfAbsent;
                }
                nextMap.putIfAbsent(Algorithm.MD5, md5DigestString);
                nextMap.putIfAbsent(Algorithm.SHA1, shaDigestString);
            }
        }
        
        return result;
    }
    
}
