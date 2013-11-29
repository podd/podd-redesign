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
package com.github.podd.test;

import org.openrdf.model.URI;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class TestUtils
{
    /**
     * Adds a Test User to the PODD Realm.
     * 
     * @param application
     */
    public static void setupTestUser(final PoddWebServiceApplication application)
    {
        final PoddSesameRealm nextRealm = application.getRealm();
        
        final URI testUserHomePage = PODD.VF.createURI("http://www.example.com/testUser");
        final PoddUser testUser =
                new PoddUser(RestletTestUtils.TEST_USERNAME, RestletTestUtils.TEST_PASSWORD, "Test", "User",
                        "test.user@example.com", PoddUserStatus.ACTIVE, testUserHomePage, "CSIRO", "Orcid-Test-User");
        final URI testUserUri = nextRealm.addUser(testUser);
        nextRealm.map(testUser, PoddRoles.PROJECT_CREATOR.getRole());
        nextRealm.map(testUser, PoddRoles.PROJECT_ADMIN.getRole(), PODD.TEST_ARTIFACT);
        
        // ApplicationUtils.log.debug("Added Test User to PODD: {} <{}>", testUser.getIdentifier(),
        // testUserUri);
    }
    
    /**
     * Private default constructor
     */
    private TestUtils()
    {
    }
}
