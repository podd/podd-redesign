/**
 * 
 */
package com.github.podd.impl.file.test;

import com.github.podd.api.file.SPARQLDataReference;
import com.github.podd.api.file.test.AbstractSPARQLDataReferenceTest;
import com.github.podd.impl.file.SPARQLDataReferenceImpl;

/**
 * @author kutila
 * 
 */
public class SPARQLDataReferenceImplTest extends AbstractSPARQLDataReferenceTest
{
    
    @Override
    protected SPARQLDataReference getNewSPARQLDataReference()
    {
        final SPARQLDataReference ref = new SPARQLDataReferenceImpl();
        return ref;
    }
    
}
