/**
 * 
 */
package com.github.podd.restlet.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.restlet.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.restlet.PoddRoles;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;

/**
 * @author kutila
 *
 */
public class PoddSesameRealmTest
{
    private static final URI userMgtContext = PoddRdfConstants.VALUE_FACTORY.createURI("urn:context:usermanagement:graph");

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Repository testRepository;
    private PoddSesameRealm testRealm;
    
    private String testUserId1 = "john@example.com";
    private URI userUri = PoddRdfConstants.VALUE_FACTORY.createURI("urn:oas:user:" + testUserId1 + ":" + UUID.randomUUID().toString());
    
    @Before
    public void setUp() throws Exception
    {
        this.testRepository = new SailRepository(new MemoryStore());
        this.testRepository.initialize();
        
        testRealm = new PoddSesameRealm(this.testRepository, userMgtContext);
    }

    @After
    public void tearDown() throws Exception
    {
        if (testRepository != null)
        {
            this.testRepository.shutDown();
        }
    }
    
    /**
     * Test some basic functionality provided by the super-class RestletUtilSesameRealm.java
     * @throws Exception
     */
    @Test
    public void testRestletUtilSesameRealmFunctionalities() throws Exception
    {
        PoddUser testUser = new PoddUser(testUserId1, "secret".toCharArray(), "John", "Doe", testUserId1, 
                PoddUserStatus.ACTIVE, userUri, "Example Research LLC", "SOME_ORCID_ID");
    
        // - add a test user
        testRealm.addUser(testUser);
        Assert.assertEquals("Returned user different to original", testUser, testRealm.findUser(testUserId1));
        
        // - map ADMIN and PROJECT_ADMIN Roles to the test user
        testRealm.map(testUser, PoddRoles.ADMIN.getRole());
        testRealm.map(testUser, PoddRoles.PROJECT_ADMIN.getRole());
        
        Set<Role> rolesOfNextUser = testRealm.findRoles(testUser);
        Assert.assertEquals(2, rolesOfNextUser.size());
        Assert.assertTrue(rolesOfNextUser.contains(PoddRoles.ADMIN.getRole()));
        
        // - unmap the ADMIN Role
        testRealm.unmap(testUser, PoddRoles.ADMIN.getRole());
        Assert.assertEquals(1, testRealm.findRoles(testUser).size());
        
    }
    
    @Test
    public void testCommonRolesForObjects() throws Exception
    {
        PoddUser testUser = new PoddUser(testUserId1, "secret".toCharArray(), "John", "Doe", testUserId1, 
                PoddUserStatus.ACTIVE, userUri, "Example Research LLC", "SOME_ORCID_ID");
        
        testRealm.addUser(testUser);
        testRealm.map(testUser, PoddRoles.ADMIN.getRole());
        
        URI object1URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:"+ UUID.randomUUID().toString());
        URI object2URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:"+ UUID.randomUUID().toString());

        Collection<URI> optionalObjectUris = new HashSet<URI>(Arrays.asList(object1URI, object2URI));
        
        testRealm.map(testUser, PoddRoles.PROJECT_MEMBER.getRole(), optionalObjectUris);
        
        
        Collection<Role> commonRolesForOjects = testRealm.getCommonRolesForObjects(testUser, Collections.singleton(object1URI));
        
        // DEBUG print content
        RdfUtils.printContents(testRepository, userMgtContext);
    }
    
    
    
}
