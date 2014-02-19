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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PoddDigestUtils
{
    public ConcurrentMap<Path, ConcurrentMap<String, String>> getDigests(List<Path> pathsToDigest) throws IOException,
        NoSuchAlgorithmException
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
