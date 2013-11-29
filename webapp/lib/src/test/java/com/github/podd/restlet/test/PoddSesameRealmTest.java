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
package com.github.podd.restlet.test;

import info.aduna.iteration.Iterations;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.restlet.security.Role;

import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;

/**
 * @author kutila
 * 
 */
public class PoddSesameRealmTest
{
    private static final URI userMgtContext = PODD.VF.createURI("urn:context:usermanagement:graph");
    
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
        
        final URI testUserHomePage = PODD.VF.createURI("http://example.org/" + userId);
        final PoddUser testUser =
                new PoddUser(userId, "secret".toCharArray(), "First", "Last", userId, PoddUserStatus.ACTIVE,
                        testUserHomePage, "Some Organization", "SOME_ORCID_ID");
        
        // final URI testUserUri =
        // PoddRdfConstants.VF.createURI("urn:oas:user:" + userId + ":" +
        // UUID.randomUUID().toString());
        // testUser.setUri(testUserUri);
        final URI addUserUri = this.testRealm.addUser(testUser);
        Assert.assertNotNull("Test user was not added correctly", addUserUri);
        return testUser;
    }
    
    /**
     * Wrapper to get statements from the Repository
     */
    protected Model getStatementList(final URI subject, final URI predicate, final Value object) throws Exception
    {
        RepositoryConnection conn = null;
        try
        {
            conn = this.testRepository.getConnection();
            conn.begin();
            
            return new LinkedHashModel(Iterations.asList(conn.getStatements(subject, predicate, object, true,
                    PoddSesameRealmTest.userMgtContext)));
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
    
    /**
     * Test that a User can be added with PODD-specific attributes (Organization, ORCID, HomePage).
     */
    @Test
    public void testAddUserSimple() throws Exception
    {
        final String testUserId1 = "john@example.com";
        final PoddUser testUser = this.addTestUser(testUserId1);
        Assert.assertNotNull("Test user was null", testUser);
        
        DebugUtils.printContents(this.getStatementList(null, null, null));
        
        final PoddUser retrievedUser = this.testRealm.findUser(testUserId1);
        Assert.assertNotNull("Retrieved user was null", retrievedUser);
        Assert.assertEquals("Returned user different to original", testUser, retrievedUser);
        Assert.assertTrue("Returned user is not a PoddUser", retrievedUser instanceof PoddUser);
        
        final PoddUser recvdPoddUser = retrievedUser;
        Assert.assertEquals("Returned user ORCID different to original", "SOME_ORCID_ID", recvdPoddUser.getOrcid());
        Assert.assertEquals("Returned user URI different to original", testUser.getHomePage(),
                recvdPoddUser.getHomePage());
        Assert.assertEquals("Returned user Organization different to original", "Some Organization",
                recvdPoddUser.getOrganization());
        Assert.assertEquals("Returned user has incorrect status", PoddUserStatus.ACTIVE, recvdPoddUser.getUserStatus());
    }
    
    @Test
    public void testAddUserWithInactiveStatus() throws Exception
    {
        final String testIdentifier = "xTest@example.com";
        final PoddUser testUser =
                new PoddUser(testIdentifier, "secret".toCharArray(), "First", "Last", testIdentifier,
                        PoddUserStatus.INACTIVE, PODD.VF.createURI("http://example.org/" + testIdentifier),
                        "Some Organization", "SOME_ORCID_ID");
        this.testRealm.addUser(testUser);
        
        final PoddUser retrievedUser = this.testRealm.findUser(testIdentifier);
        Assert.assertEquals("Returned user different to original", testUser, retrievedUser);
        Assert.assertTrue("Returned user is not a PoddUser", retrievedUser instanceof PoddUser);
        
        final PoddUser recvdPoddUser = retrievedUser;
        Assert.assertEquals("Returned user has incorrect status", PoddUserStatus.INACTIVE,
                recvdPoddUser.getUserStatus());
    }
    
    @Test
    public void testAddUserWithNoSecret() throws Exception
    {
        final String testIdentifier = "xTest@example.com";
        final PoddUser testUser =
                new PoddUser(testIdentifier, null, "First", "Last", testIdentifier, PoddUserStatus.INACTIVE,
                        PODD.VF.createURI("http://example.org/" + testIdentifier), "Some Organization", "SOME_ORCID_ID");
        this.testRealm.addUser(testUser);
        
        final PoddUser retrievedUser = this.testRealm.findUser(testIdentifier);
        Assert.assertEquals("Returned user different to original", testUser, retrievedUser);
        Assert.assertTrue("Returned user is not a PoddUser", retrievedUser instanceof PoddUser);
        
        final PoddUser recvdPoddUser = retrievedUser;
        Assert.assertEquals("Returned user has incorrect status", PoddUserStatus.INACTIVE,
                recvdPoddUser.getUserStatus());
    }
    
    @Test
    public void testAddUserWithNoSecretActive() throws Exception
    {
        final String testIdentifier = "xTest@example.com";
        final PoddUser testUser =
                new PoddUser(testIdentifier, null, "First", "Last", testIdentifier, PoddUserStatus.ACTIVE,
                        PODD.VF.createURI("http://example.org/" + testIdentifier), "Some Organization", "SOME_ORCID_ID");
        this.testRealm.addUser(testUser);
        
        final PoddUser retrievedUser = this.testRealm.findUser(testIdentifier);
        Assert.assertEquals("Returned user different to original", testUser, retrievedUser);
        Assert.assertTrue("Returned user is not a PoddUser", retrievedUser instanceof PoddUser);
        
        final PoddUser recvdPoddUser = retrievedUser;
        Assert.assertEquals("Returned user has incorrect status", PoddUserStatus.INACTIVE,
                recvdPoddUser.getUserStatus());
    }
    
    @Test
    public void testGetRolesForObjectWithMiscCombinations() throws Exception
    {
        // -prepare: users
        final PoddUser user1 = this.addTestUser("john@example.com");
        final PoddUser user2 = this.addTestUser("bob@hope.com");
        
        // -prepare: test objects
        final URI object1URI = PODD.VF.createURI("urn:podd:artifact:1");
        final URI object2URI = PODD.VF.createURI("urn:podd:artifact:2");
        final URI object3URI = PODD.VF.createURI("urn:podd:artifact:3");
        final URI object4URI = PODD.VF.createURI("urn:podd:artifact:4");
        
        // -prepare: map Users - Roles and Objects together
        this.testRealm.map(user1, PoddRoles.ADMIN.getRole());
        
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object2URI);
        
        this.testRealm.map(user1, PoddRoles.PROJECT_OBSERVER.getRole(), object3URI);
        this.testRealm.map(user1, PoddRoles.PROJECT_ADMIN.getRole(), object3URI);
        
        this.testRealm.map(user1, PoddRoles.PROJECT_ADMIN.getRole(), object4URI);
        this.testRealm.map(user1, PoddRoles.PROJECT_OBSERVER.getRole(), object4URI);
        
        this.testRealm.map(user2, PoddRoles.ADMIN.getRole());
        this.testRealm.map(user2, PoddRoles.PROJECT_OBSERVER.getRole(), object1URI);
        this.testRealm.map(user2, PoddRoles.PROJECT_OBSERVER.getRole(), object2URI);
        
        // -verify: common Role for 1 Object
        final Collection<Role> rolesForObject1 = this.testRealm.getRolesForObject(user1, object1URI);
        Assert.assertEquals("Should be only 1 role", 1, rolesForObject1.size());
        Assert.assertTrue("Project_Member role missing", rolesForObject1.contains(PoddRoles.PROJECT_MEMBER.getRole()));
        
        // -verify: common Role for 1 Object
        final Collection<Role> rolesForObject3 = this.testRealm.getRolesForObject(user1, object3URI);
        Assert.assertEquals("Should be 2 roles", 2, rolesForObject3.size());
        Assert.assertTrue("Project_Observer role missing",
                rolesForObject3.contains(PoddRoles.PROJECT_OBSERVER.getRole()));
        Assert.assertTrue("Project_Admin role missing", rolesForObject3.contains(PoddRoles.PROJECT_ADMIN.getRole()));
    }
    
    /**
     * Test that when there are no object URIs mapped for Roles, nothing is returned.
     */
    @Test
    public void testGetRolesForObjectWithNoObjectUriMappings() throws Exception
    {
        // -prepare: users
        final PoddUser user1 = this.addTestUser("john@example.com");
        
        // -prepare: test objects
        final URI object1URI = PODD.VF.createURI("urn:podd:artifact:1");
        
        // -prepare: map Users - Roles and Objects together
        this.testRealm.map(user1, PoddRoles.ADMIN.getRole());
        
        // -verify: common Role for Object 1
        final Collection<Role> rolesForObject1 = this.testRealm.getRolesForObject(user1, object1URI);
        Assert.assertEquals("Should be 0 Roles", 0, rolesForObject1.size());
    }
    
    /**
     * Test common roles for a user when 2 objects are mapped with roles.
     */
    @Test
    public void testGetRolesForObjectWithTwoObjects() throws Exception
    {
        // -prepare: users
        final PoddUser user1 = this.addTestUser("john@example.com");
        final PoddUser user2 = this.addTestUser("bob@hope.com");
        
        // -prepare: test objects
        final URI object1URI = PODD.VF.createURI("urn:podd:artifact:1");
        final URI object2URI = PODD.VF.createURI("urn:podd:artifact:2");
        
        // -prepare: map Users - Roles and Objects together
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object2URI);
        this.testRealm.map(user1, PoddRoles.ADMIN.getRole(), null);
        
        this.testRealm.map(user2, PoddRoles.PROJECT_OBSERVER.getRole(), object1URI);
        this.testRealm.map(user2, PoddRoles.PROJECT_OBSERVER.getRole(), object2URI);
        
        // -verify: common Role for 1 Object
        final Collection<Role> rolesForObject1 = this.testRealm.getRolesForObject(user1, object1URI);
        Assert.assertEquals("Should be only 1 role", 1, rolesForObject1.size());
        Assert.assertTrue("Project_Member role missing", rolesForObject1.contains(PoddRoles.PROJECT_MEMBER.getRole()));
    }
    
    @Test
    public void testGetRolesWithObjectMappings() throws Exception
    {
        // -prepare: users
        final PoddUser user1 = this.addTestUser("john@example.com");
        final PoddUser user2 = this.addTestUser("bob@hope.com");
        
        // -prepare: test objects
        final URI object1URI = PODD.VF.createURI("urn:podd:artifact:1");
        final URI object2URI = PODD.VF.createURI("urn:podd:artifact:2");
        
        // -prepare: map Users - Roles and Objects together
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object2URI);
        this.testRealm.map(user1, PoddRoles.ADMIN.getRole(), null);
        
        this.testRealm.map(user2, PoddRoles.PROJECT_OBSERVER.getRole(), object1URI);
        this.testRealm.map(user2, PoddRoles.PROJECT_OBSERVER.getRole(), object2URI);
        
        final Collection<Entry<Role, URI>> rolesForUser1 = this.testRealm.getRolesWithObjectMappings(user1);
        Assert.assertEquals("Should be 3 role mappings", 3, rolesForUser1.size());
        
        final Collection<Entry<Role, URI>> rolesForUser2 = this.testRealm.getRolesWithObjectMappings(user2);
        Assert.assertEquals("Should be 2 role mappings", 2, rolesForUser2.size());
    }
    
    @Test
    public void testGetUserByStatus() throws Exception
    {
        // -prepare: users
        this.addTestUser("albert@hope.com");
        this.addTestUser("bob@hope.com");
        this.addTestUser("charles@hope.com");
        this.addTestUser("david@hope.com");
        this.addTestUser("elmo@hope.com");
        
        // - ACTIVE users
        final List<PoddUser> activeUsers = this.testRealm.getUserByStatus(PoddUserStatus.ACTIVE, false, 10, 0);
        Assert.assertEquals("Not the expected number of active Users", 5, activeUsers.size());
        Assert.assertEquals("Results not in ascending order", "albert@hope.com", activeUsers.get(0).getIdentifier());
        
        // - INACTIVE users
        final List<PoddUser> inactiveUsers = this.testRealm.getUserByStatus(PoddUserStatus.INACTIVE, false, 10, 0);
        Assert.assertEquals("Not the expected number of inactive Users", 0, inactiveUsers.size());
        
        // - order by DESC(identifier) and limit of 3
        final List<PoddUser> filteredUsers = this.testRealm.getUserByStatus(PoddUserStatus.ACTIVE, true, 3, 0);
        Assert.assertEquals("Not the expected number of Users after filtering", 3, filteredUsers.size());
        Assert.assertEquals("Results not in descending order", "elmo@hope.com", filteredUsers.get(0).getIdentifier());
        
        // - order by identifier and offset of 2
        final List<PoddUser> offsetUsers = this.testRealm.getUserByStatus(PoddUserStatus.ACTIVE, false, 10, 2);
        Assert.assertEquals("Not the expected number of Users after offsetting", 3, offsetUsers.size());
        Assert.assertEquals("Results not in ascending order", "charles@hope.com", offsetUsers.get(0).getIdentifier());
    }
    
    @Test
    public void testGetUserRoles() throws Exception
    {
        // -prepare: users
        final PoddUser user1 = this.addTestUser("john@example.com");
        final PoddUser user2 = this.addTestUser("bob@hope.com");
        
        // -prepare: test objects
        final URI object1URI = PODD.VF.createURI("urn:podd:artifact:1");
        final URI object2URI = PODD.VF.createURI("urn:podd:artifact:2");
        
        // -prepare: map Users - Roles and Objects together
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object2URI);
        this.testRealm.map(user1, PoddRoles.ADMIN.getRole(), null);
        
        this.testRealm.map(user2, PoddRoles.PROJECT_OBSERVER.getRole(), object1URI);
        this.testRealm.map(user2, PoddRoles.PROJECT_OBSERVER.getRole(), object2URI);
        
        // -get Roles of user1
        final Set<Role> user1Roles = this.testRealm.findRoles(user1);
        Assert.assertNotNull("Null set for user roles", user1Roles);
        Assert.assertFalse("No roles allocated to user 1", user1Roles.isEmpty());
        Assert.assertTrue("Admin role wasn't allocated to user 1", user1Roles.contains(PoddRoles.ADMIN.getRole()));
        Assert.assertTrue("Project Member role wasn't allocated to user 1",
                user1Roles.contains(PoddRoles.PROJECT_MEMBER.getRole()));
        
        final Set<Role> user2Roles = this.testRealm.findRoles(user2);
        Assert.assertTrue("Project Observer role wasn't allocated to user 2",
                user2Roles.contains(PoddRoles.PROJECT_OBSERVER.getRole()));
    }
    
    @Test
    public void testGetUsers() throws Exception
    {
        // -prepare: users
        final PoddUser user1 = this.addTestUser("john@example.com");
        final PoddUser user2 = this.addTestUser("bob@hope.com");
        
        // -prepare: test objects
        final URI object1URI = PODD.VF.createURI("urn:podd:artifact:1");
        final URI object2URI = PODD.VF.createURI("urn:podd:artifact:2");
        
        // -prepare: map Users - Roles and Objects together
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        
        this.testRealm.map(user2, PoddRoles.PROJECT_OBSERVER.getRole(), object2URI);
        
        // - get User List
        final List<PoddUser> users = this.testRealm.getUsers();
        Assert.assertNotNull("NULL returned for user list", users);
        Assert.assertEquals("Incorrect number of Users in list", 2, users.size());
        Assert.assertTrue("User list did not contain user1", users.contains(user1));
        Assert.assertTrue("User list did not contain user2", users.contains(user2));
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
        final URI object1URI = PODD.VF.createURI("urn:podd:artifact:1");
        final URI object2URI = PODD.VF.createURI("urn:podd:artifact:2");
        
        // -map Users - Roles and Objects together
        this.testRealm.map(user1, PoddRoles.ADMIN.getRole());
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        
        // -verify
        final Model list1 =
                this.getStatementList(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.ADMIN.getURI());
        Assert.assertFalse(list1.isEmpty());
        Assert.assertEquals(1, list1.size());
        
        // verify: a PROJECT_MEMBER role mapping exists
        final Model list2 =
                this.getStatementList(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_MEMBER.getURI());
        Assert.assertFalse(list2.isEmpty());
        Assert.assertEquals(1, list2.size());
        
        // -add another PROJECT_MEMBER mapping
        final PoddUser user2 = this.addTestUser("bob@hope.com");
        this.testRealm.map(user2, PoddRoles.PROJECT_MEMBER.getRole(), object2URI);
        
        // verify: 2 PROJECT_MEMBER role mapping exists
        final Model list3 =
                this.getStatementList(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_MEMBER.getURI());
        Assert.assertFalse(list3.isEmpty());
        Assert.assertEquals(2, list3.size());
        
        // verify: 2 RoleMappedObject statements exist in the repository
        final Model list4 = this.getStatementList(null, PODD.PODD_ROLEMAPPEDOBJECT, null);
        Assert.assertFalse(list4.isEmpty());
        Assert.assertEquals(2, list4.size());
    }
    
    @Test
    public void testSearchUser() throws Exception
    {
        // -prepare: users
        this.addTestUser("albert@hope.com");
        this.addTestUser("bob@hope.com");
        this.addTestUser("charles@hope.com");
        this.addTestUser("david@hope.com");
        this.addTestUser("elmo@hope.com");
        
        // - users matching search term 'b'
        final List<PoddUser> activeUsers = this.testRealm.searchUser("b", null, false, 10, 0);
        Assert.assertEquals("Not the expected number of Users", 2, activeUsers.size());
        Assert.assertEquals("Results not in ascending order", "albert@hope.com", activeUsers.get(0).getIdentifier());
        Assert.assertEquals("Results not in ascending order", "bob@hope.com", activeUsers.get(1).getIdentifier());
        
        // - INACTIVE users matching search term 'b'
        final List<PoddUser> inactiveUsers = this.testRealm.searchUser("b", PoddUserStatus.INACTIVE, false, 10, 0);
        Assert.assertEquals("Not the expected number of Users", 0, inactiveUsers.size());
        
        // - users matching search term "dav" and ACTIVE
        final List<PoddUser> filteredUsers = this.testRealm.searchUser("dav", PoddUserStatus.ACTIVE, true, -1, 0);
        Assert.assertEquals("Not the expected number of matching Users", 1, filteredUsers.size());
        Assert.assertEquals("Not the expected result", "david@hope.com", filteredUsers.get(0).getIdentifier());
        
        // - all users with NULL
        final List<PoddUser> allUsers = this.testRealm.searchUser(null, null, false, -1, 0);
        Assert.assertEquals("Not the expected number of total Users", 5, allUsers.size());
        
        // - all users with empty searchTerm
        final List<PoddUser> allUsers2 = this.testRealm.searchUser("", null, false, -1, 0);
        Assert.assertEquals("Not the expected number of total Users", 5, allUsers2.size());
    }
    
    @Test
    public void testSearchUserByFirstname() throws Exception
    {
        // -prepare: users
        final PoddUser testUser1 =
                new PoddUser("ks1985", "secret".toCharArray(), "Kamal", "Silva", "kamal@silva.com",
                        PoddUserStatus.ACTIVE, PODD.VF.createURI("http://example.org/kamal"),
                        "University of Queensland", "SOME_ORCID_ID");
        final PoddUser testUser2 =
                new PoddUser("ns1983", "secret".toCharArray(), "Nimal", "Silva", "Nimal@silva.com",
                        PoddUserStatus.ACTIVE, PODD.VF.createURI("http://example.org/nimal"), "CSIRO", "SOME_ORCID_ID");
        this.testRealm.addUser(testUser1);
        this.testRealm.addUser(testUser2);
        
        // - search giving First Name
        final List<PoddUser> activeUsers = this.testRealm.searchUser("kamal", null, false, 10, 0);
        Assert.assertEquals("Not the expected number of Users", 1, activeUsers.size());
        Assert.assertEquals("Results not in ascending order", "ks1985", activeUsers.get(0).getIdentifier());
    }
    
    @Test
    public void testSearchUserByLastname() throws Exception
    {
        // -prepare: users
        final PoddUser testUser1 =
                new PoddUser("ks1985", "secret".toCharArray(), "Kamal", "Silva", "kamal@silva.com",
                        PoddUserStatus.ACTIVE, PODD.VF.createURI("http://example.org/kamal"),
                        "University of Queensland", "SOME_ORCID_ID");
        final PoddUser testUser2 =
                new PoddUser("ns1983", "secret".toCharArray(), "Nimal", "Silva", "Nimal@silva.com",
                        PoddUserStatus.ACTIVE, PODD.VF.createURI("http://example.org/nimal"), "CSIRO", "SOME_ORCID_ID");
        this.testRealm.addUser(testUser1);
        this.testRealm.addUser(testUser2);
        
        // - search giving Last Name
        final List<PoddUser> activeUsers = this.testRealm.searchUser("Silva", null, false, 10, 0);
        Assert.assertEquals("Not the expected number of Users", 2, activeUsers.size());
        Assert.assertEquals("Results not in ascending order", "ks1985", activeUsers.get(0).getIdentifier());
        Assert.assertEquals("Results not in ascending order", "ns1983", activeUsers.get(1).getIdentifier());
    }
    
    /**
     * Test some of the functionality provided by the super class RestletUtilSesameRealm.java
     */
    @Test
    public void testSuperClassFunctions() throws Exception
    {
        final String testUserId1 = "john@example.com";
        final PoddUser testUser = this.addTestUser(testUserId1);
        
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
    
    /**
     * Test that mappings between a User, a Role and an optional Object URI can be removed.
     */
    @Test
    public void testUnmapSimple() throws Exception
    {
        // -prepare: user and test object
        final PoddUser user1 = this.addTestUser("john@example.com");
        final URI object1URI = PODD.VF.createURI("urn:podd:artifact:1");
        
        // -map Users with Roles and Objects together
        this.testRealm.map(user1, PoddRoles.ADMIN.getRole());
        this.testRealm.map(user1, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        
        // -verify: ADMIN role mapping
        final Model list1 =
                this.getStatementList(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.ADMIN.getURI());
        Assert.assertFalse(list1.isEmpty());
        Assert.assertEquals(1, list1.size());
        
        // verify: PROJECT_MEMBER role mapping
        final Model list2 =
                this.getStatementList(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_MEMBER.getURI());
        Assert.assertFalse(list2.isEmpty());
        Assert.assertEquals(1, list2.size());
        
        // unmap Project_Member Role
        this.testRealm.unmap(user1, PoddRoles.PROJECT_MEMBER.getRole(), object1URI);
        
        // verify: no PROJECT_MEMBER role mapping exists
        final Model list3 =
                this.getStatementList(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_MEMBER.getURI());
        Assert.assertTrue(list3.isEmpty());
        
        // verify: Admin Role Mapping exist in the repository
        final Model list4 =
                this.getStatementList(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.ADMIN.getURI());
        Assert.assertFalse(list4.isEmpty());
        Assert.assertEquals(1, list4.size());
        
        this.testRealm.unmap(user1, PoddRoles.PROJECT_ADMIN.getRole(), object1URI);
    }
    
    @Test
    public void testUpdateUser() throws Exception
    {
        final String testUserId = "john@example.com";
        
        // create test user
        final URI testUser1HomePage = PODD.VF.createURI("http://example.org/john");
        final PoddUser testUser1 =
                new PoddUser(testUserId, "secret".toCharArray(), "First", "Last", testUserId, PoddUserStatus.ACTIVE,
                        testUser1HomePage, "UQ", "john_ORCID_111");
        this.testRealm.addUser(testUser1);
        
        // second test user
        final URI testUser2HomePage = PODD.VF.createURI("http://example.org/john.cloned");
        final String testUser2FirstName = "Jason";
        final String testUser2LastName = "Bourne";
        final PoddUser testUser2 =
                new PoddUser(testUserId, "secret".toCharArray(), testUser2FirstName, "Bourne", testUserId,
                        PoddUserStatus.INACTIVE, testUser2HomePage, "CSIRO", "john_ORCID_cloned22");
        
        // try to add another user with same identifier
        try
        {
            this.testRealm.addUser(testUser2);
            Assert.fail("Should have thrown an Exception as User identifier already exists");
        }
        catch(final IllegalStateException e)
        {
            Assert.assertTrue("Not the expected Exception", e.getMessage().contains("User already exists"));
        }
        
        // modify the existing User
        this.testRealm.updateUser(testUser2);
        
        final PoddUser userFromRealm = this.testRealm.findUser(testUserId);
        Assert.assertEquals("First name was not overwritten", testUser2FirstName, userFromRealm.getFirstName());
        Assert.assertEquals("Last name was not overwritten", testUser2LastName, userFromRealm.getLastName());
        Assert.assertEquals("Home Page was not overwritten", testUser2HomePage, userFromRealm.getHomePage());
        Assert.assertEquals("Status was not overwritten", PoddUserStatus.INACTIVE, userFromRealm.getUserStatus());
    }
    
}
