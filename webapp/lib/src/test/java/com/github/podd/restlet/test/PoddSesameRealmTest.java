/**
 * 
 */
package com.github.podd.restlet.test;

import info.aduna.iteration.Iterations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.restlet.security.Role;

import com.github.ansell.restletutils.RestletUtilUser;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.restlet.PoddRoles;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class PoddSesameRealmTest
{
    private static final URI userMgtContext = PoddRdfConstants.VALUE_FACTORY
            .createURI("urn:context:usermanagement:graph");
    
    private Repository testRepository;
    private PoddSesameRealm testRealm;
    
    /**
     * Helper method to create a test User and add it to the SesameRealm.
     * 
     * @param userId
     *            A unique identifier for test user
     * @return The created PoddUser object
     */
    protected PoddUser addTestUser(final String userId)
    {
        
        final URI testUserHomePage = PoddRdfConstants.VALUE_FACTORY.createURI("http://example.org/" + userId);
        final PoddUser testUser =
                new PoddUser(userId, "secret".toCharArray(), "First", "Last", userId, PoddUserStatus.ACTIVE,
                        testUserHomePage, "Some Organization", "SOME_ORCID_ID");
        
        // final URI testUserUri =
        // PoddRdfConstants.VALUE_FACTORY.createURI("urn:oas:user:" + userId + ":" +
        // UUID.randomUUID().toString());
        // testUser.setUri(testUserUri);
        this.testRealm.addUser(testUser);
        return testUser;
    }
    
    /**
     * Wrapper to get statements from the Repository
     */
    protected List<Statement> getStatementList(final URI subject, final URI predicate, final Value object)
        throws Exception
    {
        RepositoryConnection conn = null;
        try
        {
            conn = this.testRepository.getConnection();
            conn.begin();
            
            return Iterations.asList(conn.getStatements(subject, predicate, object, true,
                    PoddSesameRealmTest.userMgtContext));
        }
        finally
        {
            if(conn != null)
            {
                conn.rollback();
                conn.close();
            }
        }
    }
    
    @Before
    public void setUp() throws Exception
    {
        this.testRepository = new SailRepository(new MemoryStore());
        this.testRepository.initialize();
        
        this.testRealm = new PoddSesameRealm(this.testRepository, PoddSesameRealmTest.userMgtContext);
    }
    
    @After
    public void tearDown() throws Exception
    {
        if(this.testRepository != null)
        {
            this.testRepository.shutDown();
        }
    }
    
    @Test
    public void testGetCommonRolesForObjectsWithMiscCombinations() throws Exception
    {
        // -prepare: users
        final PoddUser user1 = this.addTestUser("john@example.com");
        final PoddUser user2 = this.addTestUser("bob@hope.com");
        
        // -prepare: test objects
        final URI object1URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:1");
        final URI object2URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:2");
        final URI object3URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:3");
        final URI object4URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:4");
        
        // -prepare: map Users - Roles and Objects together
        this.testRealm.map(user1, PoddRoles.ADMIN.getRole());
        this.testRealm.map(user1, PoddRoles.AUTHENTICATED.getRole());
        
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object2URI);
        
        this.testRealm.map(user1, PoddRoles.PROJECT_OBSERVER.getRole(), object3URI);
        this.testRealm.map(user1, PoddRoles.PROJECT_ADMIN.getRole(), object3URI);
        
        this.testRealm.map(user1, PoddRoles.PROJECT_ADMIN.getRole(), object4URI);
        this.testRealm.map(user1, PoddRoles.PROJECT_OBSERVER.getRole(), object4URI);
        
        this.testRealm.map(user2, PoddRoles.ADMIN.getRole());
        this.testRealm.map(user2, PoddRoles.ROLE_A.getRole(), object1URI);
        this.testRealm.map(user2, PoddRoles.ROLE_A.getRole(), object2URI);
        
        // -verify: common Role for 1 Object
        final Collection<Role> commonRolesForObject1 =
                this.testRealm.getCommonRolesForObjects(user1, Collections.singleton(object1URI));
        Assert.assertEquals("Should be only 1 role", 1, commonRolesForObject1.size());
        Assert.assertTrue("Project_Member role missing",
                commonRolesForObject1.contains(PoddRoles.PROJECT_MEMBER.getRole()));
        
        // -verify: common Roles between Objects 1 and 2
        final Collection<Role> commonRolesForObjects1And2 =
                this.testRealm.getCommonRolesForObjects(user1, Arrays.asList(object1URI, object2URI));
        Assert.assertEquals("Should be 1 common role", 1, commonRolesForObjects1And2.size());
        Assert.assertTrue("Project_Member role missing",
                commonRolesForObjects1And2.contains(PoddRoles.PROJECT_MEMBER.getRole()));
        
        // -verify: common Roles between Objects 1 and 3
        final Collection<Role> commonRolesForObjects1And3 =
                this.testRealm.getCommonRolesForObjects(user1, Arrays.asList(object1URI, object3URI));
        Assert.assertEquals("Should be no common role", 0, commonRolesForObjects1And3.size());
        
        // -verify: common Roles between Objects 1,2,3
        final Collection<Role> commonRolesForObjects123 =
                this.testRealm.getCommonRolesForObjects(user1, Arrays.asList(object1URI, object2URI, object3URI));
        Assert.assertEquals("Should be no common role", 0, commonRolesForObjects123.size());
        
        // -verify: common Roles between Objects 3 and 4
        final Collection<Role> commonRolesForObjects3And4 =
                this.testRealm.getCommonRolesForObjects(user1, Arrays.asList(object3URI, object4URI));
        Assert.assertEquals("Should be 1 common role", 2, commonRolesForObjects3And4.size());
        Assert.assertTrue("Project_Admin role missing",
                commonRolesForObjects3And4.contains(PoddRoles.PROJECT_ADMIN.getRole()));
        Assert.assertTrue("Project_Observer role missing",
                commonRolesForObjects3And4.contains(PoddRoles.PROJECT_OBSERVER.getRole()));
        
        // -verify: common Roles between Objects 1,2,4
        final Collection<Role> commonRolesForObjects124 =
                this.testRealm.getCommonRolesForObjects(user1, Arrays.asList(object1URI, object2URI, object4URI));
        Assert.assertEquals("Should be 0 common roles", 0, commonRolesForObjects124.size());
        
        // -verify: common Roles between Objects 1 and 2 for User 2
        final Collection<Role> user2CommonRolesForObjects1And2 =
                this.testRealm.getCommonRolesForObjects(user2, Arrays.asList(object1URI, object2URI));
        Assert.assertEquals("Should be 1 common role", 1, user2CommonRolesForObjects1And2.size());
        Assert.assertTrue("Role_A role missing", user2CommonRolesForObjects1And2.contains(PoddRoles.ROLE_A.getRole()));
    }
    
    /**
     * Test that common Roles between DIFFERENT users for the requires set of Objects are not
     * considered together.
     */
    @Test
    public void testGetCommonRolesForObjectsWithMultipleUsers() throws Exception
    {
        // -prepare: users
        final PoddUser user1 = this.addTestUser("john@example.com");
        final PoddUser user2 = this.addTestUser("bob@hope.com");
        final PoddUser user3 = this.addTestUser("tim@hope.com");
        
        // -prepare: test objects
        final URI object1URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:1");
        final URI object2URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:2");
        final URI object3URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:3");
        
        // -prepare: map Users - Roles and Objects together
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        this.testRealm.map(user1, PoddRoles.PROJECT_ADMIN.getRole(), object2URI);
        this.testRealm.map(user1, PoddRoles.PROJECT_OBSERVER.getRole(), object3URI);
        
        this.testRealm.map(user2, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        this.testRealm.map(user2, PoddRoles.PROJECT_MEMBER.getRole(), object2URI);
        this.testRealm.map(user2, PoddRoles.PROJECT_MEMBER.getRole(), object3URI);
        
        this.testRealm.map(user3, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        this.testRealm.map(user3, PoddRoles.PROJECT_MEMBER.getRole(), object2URI);
        this.testRealm.map(user3, PoddRoles.PROJECT_MEMBER.getRole(), object3URI);
        
        // -verify: user 1 common Roles for Objects
        final Collection<Role> commonRolesForObject1 =
                this.testRealm.getCommonRolesForObjects(user1, Arrays.asList(object1URI, object2URI, object3URI));
        Assert.assertEquals("Should 0 common Roles", 0, commonRolesForObject1.size());
    }
    
    /**
     * Test that when there are no object URIs mapped for Roles, nothing is returned.
     */
    @Test
    public void testGetCommonRolesForObjectsWithNoObjectUriMappings() throws Exception
    {
        // -prepare: users
        final PoddUser user1 = this.addTestUser("john@example.com");
        
        // -prepare: test objects
        final URI object1URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:1");
        
        // -prepare: map Users - Roles and Objects together
        this.testRealm.map(user1, PoddRoles.ADMIN.getRole());
        this.testRealm.map(user1, PoddRoles.AUTHENTICATED.getRole());
        
        // -verify: common Role for Object 1
        final Collection<Role> commonRolesForObject1 =
                this.testRealm.getCommonRolesForObjects(user1, Collections.singleton(object1URI));
        Assert.assertEquals("Should be 0 Roles", 0, commonRolesForObject1.size());
        
        // RdfUtils.printContents(this.testRepository, PoddSesameRealmTest.userMgtContext);
    }
    
    /**
     * Test common roles for a user when 2 objects are mapped with roles.
     */
    @Test
    public void testGetCommonRolesForObjectsWithTwoObjects() throws Exception
    {
        // -prepare: users
        final PoddUser user1 = this.addTestUser("john@example.com");
        final PoddUser user2 = this.addTestUser("bob@hope.com");
        
        // -prepare: test objects
        final URI object1URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:1");
        final URI object2URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:2");
        
        // -prepare: map Users - Roles and Objects together
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object2URI);
        this.testRealm.map(user1, PoddRoles.PROJECT_OBSERVER.getRole(), object2URI);
        
        this.testRealm.map(user2, PoddRoles.ROLE_A.getRole(), object1URI);
        this.testRealm.map(user2, PoddRoles.ROLE_A.getRole(), object2URI);
        
        // -verify: common Role for 1 Object
        final Collection<Role> commonRolesForObject1 =
                this.testRealm.getCommonRolesForObjects(user1, Collections.singleton(object1URI));
        Assert.assertEquals("Should be only 1 role", 1, commonRolesForObject1.size());
        Assert.assertTrue("Project_Member role missing",
                commonRolesForObject1.contains(PoddRoles.PROJECT_MEMBER.getRole()));
        
        // -verify: common Roles between Objects 1 and 2
        final Collection<Role> commonRolesForObjects1And2 =
                this.testRealm.getCommonRolesForObjects(user1, Arrays.asList(object1URI, object2URI));
        Assert.assertEquals("Should be 1 common role", 1, commonRolesForObjects1And2.size());
        Assert.assertTrue("Project_Member role missing",
                commonRolesForObjects1And2.contains(PoddRoles.PROJECT_MEMBER.getRole()));
        
        // -verify: common Roles between Objects 1 and 2 for User 2
        final Collection<Role> user2CommonRolesForObjects1And2 =
                this.testRealm.getCommonRolesForObjects(user2, Arrays.asList(object1URI, object2URI));
        Assert.assertEquals("Should be 1 common role", 1, user2CommonRolesForObjects1And2.size());
        Assert.assertTrue("Role_A role missing", user2CommonRolesForObjects1And2.contains(PoddRoles.ROLE_A.getRole()));
    }
    
    @Test
    public void testGetUserRoles() throws Exception
    {
        // -prepare: users
        final PoddUser user1 = this.addTestUser("john@example.com");
        final PoddUser user2 = this.addTestUser("bob@hope.com");
        
        // -prepare: test objects
        final URI object1URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:1");
        final URI object2URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:2");
        
        // -prepare: map Users - Roles and Objects together
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object2URI);
        this.testRealm.map(user1, PoddRoles.ADMIN.getRole(), null);
        
        this.testRealm.map(user2, PoddRoles.ROLE_A.getRole(), object1URI);
        this.testRealm.map(user2, PoddRoles.ROLE_A.getRole(), object2URI);
        
        // -get Roles of user1
        final Set<Role> user1Roles = this.testRealm.findRoles(user1);
        Assert.assertNotNull("Null set for user roles", user1Roles);
        Assert.assertFalse("No roles allocated to user 1", user1Roles.isEmpty());
        Assert.assertTrue("Admin role wasn't allocated to user 1", user1Roles.contains(PoddRoles.ADMIN.getRole()));
        Assert.assertTrue("PROJECT_MEMBER role wasn't allocated to user 1",
                user1Roles.contains(PoddRoles.PROJECT_MEMBER.getRole()));
        
        final Set<Role> user2Roles = this.testRealm.findRoles(user2);
        Assert.assertTrue("ROLE_A role wasn't allocated to user 2", user2Roles.contains(PoddRoles.ROLE_A.getRole()));
    }
    
    /**
     * Test that mappings between a User, a Role and an optional Object URI can be added.
     */
    @Test
    public void testMapSimple() throws Exception
    {
        // -prepare: users
        final PoddUser user1 = this.addTestUser("john@example.com");
        
        // -prepare: test objects
        final URI object1URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:1");
        final URI object2URI = PoddRdfConstants.VALUE_FACTORY.createURI("urn:podd:artifact:2");
        
        // -map Users - Roles and Objects together
        this.testRealm.map(user1, PoddRoles.ADMIN.getRole());
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        
        // -verify
        final List<Statement> list1 =
                this.getStatementList(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.ADMIN.getURI());
        Assert.assertFalse(list1.isEmpty());
        Assert.assertEquals(1, list1.size());
        
        // verify: a PROJECT_MEMBER role mapping exists
        final List<Statement> list2 =
                this.getStatementList(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_MEMBER.getURI());
        Assert.assertFalse(list2.isEmpty());
        Assert.assertEquals(1, list2.size());
        
        // -add another PROJECT_MEMBER mapping
        final PoddUser user2 = this.addTestUser("bob@hope.com");
        this.testRealm.map(user2, PoddRoles.PROJECT_MEMBER.getRole(), object2URI);
        
        // verify: 2 PROJECT_MEMBER role mapping exists
        final List<Statement> list3 =
                this.getStatementList(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_MEMBER.getURI());
        Assert.assertFalse(list3.isEmpty());
        Assert.assertEquals(2, list3.size());
        
        // verify: 2 RoleMappedObject statements exist in the repository
        final List<Statement> list4 = this.getStatementList(null, PoddWebConstants.PODD_ROLEMAPPEDOBJECT, null);
        Assert.assertFalse(list4.isEmpty());
        Assert.assertEquals(2, list4.size());
    }
    
    /**
     * Test that a User can be added with PODD-specific attributes (Organization, ORCID, HomePage).
     */
    @Test
    public void testAddUserSimple() throws Exception
    {
        final String testUserId1 = "john@example.com";
        final PoddUser testUser = this.addTestUser(testUserId1);
        
        // - add a test user
        this.testRealm.addUser(testUser);
        
        final RestletUtilUser retrievedUser = this.testRealm.findUser(testUserId1);
        Assert.assertEquals("Returned user different to original", testUser, retrievedUser);
        Assert.assertTrue("Returned user is not a PoddUser", retrievedUser instanceof PoddUser);
        
        final PoddUser recvdPoddUser = (PoddUser)retrievedUser;
        Assert.assertEquals("Returned user ORCID different to original", "SOME_ORCID_ID", recvdPoddUser.getOrcid());
        Assert.assertEquals("Returned user URI different to original", testUser.getHomePage(),
                recvdPoddUser.getHomePage());
        Assert.assertEquals("Returned user Organization different to original", "Some Organization",
                recvdPoddUser.getOrganization());
    }
    
    /**
     * Test some of the functionality provided by the super class RestletUtilSesameRealm.java
     */
    @Test
    public void testSuperClassFunctions() throws Exception
    {
        final String testUserId1 = "john@example.com";
        final PoddUser testUser = this.addTestUser(testUserId1);
        
        // - add a test user
        this.testRealm.addUser(testUser);
        Assert.assertEquals("Returned user different to original", testUser, this.testRealm.findUser(testUserId1));
        
        // - map ADMIN and PROJECT_ADMIN Roles to the test user
        this.testRealm.map(testUser, PoddRoles.ADMIN.getRole());
        this.testRealm.map(testUser, PoddRoles.PROJECT_ADMIN.getRole());
        
        final Set<Role> rolesOfNextUser = this.testRealm.findRoles(testUser);
        Assert.assertEquals("Should have 2 roles mapped", 2, rolesOfNextUser.size());
        
        Assert.assertTrue("Admin role not mapped", rolesOfNextUser.contains(PoddRoles.ADMIN.getRole()));
        
        // - unmap the ADMIN Role
        this.testRealm.unmap(testUser, PoddRoles.ADMIN.getRole());
        Assert.assertEquals(1, this.testRealm.findRoles(testUser).size());
    }
    
}
