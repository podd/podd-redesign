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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PoddDigestUtils
{
    public static ConcurrentMap<Path, ConcurrentMap<String, String>> getDigests(List<Path> pathsToDigest)
        throws IOException, NoSuchAlgorithmException
    {
        ConcurrentMap<Path, ConcurrentMap<String, String>> result = new ConcurrentHashMap<>();
        
        for(Path nextPath : pathsToDigest)
        {
            try (final InputStream inputStream = Files.newInputStream(nextPath))
            {
                DigestInputStream shaStream = new DigestInputStream(inputStream, MessageDigest.getInstance("SHA-1"));
                DigestInputStream md5Stream = new DigestInputStream(shaStream, MessageDigest.getInstance("MD5"));
                byte[] shaDigest = shaStream.getMessageDigest().digest();
                byte[] md5Digest = md5Stream.getMessageDigest().digest();
                String shaDigestString = new BigInteger(1, shaDigest).toString(16);
                String md5DigestString = new BigInteger(1, md5Digest).toString(16);
                ConcurrentMap<String, String> nextMap = new ConcurrentHashMap<String, String>();
                ConcurrentMap<String, String> putIfAbsent = result.putIfAbsent(nextPath, nextMap);
                if(putIfAbsent != null)
                {
                    nextMap = putIfAbsent;
                }
                nextMap.putIfAbsent("MD5", md5DigestString);
                nextMap.putIfAbsent("SHA-1", shaDigestString);
            }
        }
        
        return result;
    }
    
}
