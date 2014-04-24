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

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.podd.utils.PoddRoles;

/**
 * @author kutila
 *
 */
public class PoddRolesTest
{
    @Test
    public void testGetRepositoryRoles() throws Exception
    {
        final Set<RestletUtilRole> repositoryRoles = PoddRoles.getRepositoryRoles();
        Assert.assertEquals("Expected 2 Repository Roles", 2, repositoryRoles.size());

        Assert.assertTrue("Missing Admin role", repositoryRoles.contains(PoddRoles.ADMIN));
        Assert.assertTrue("Missing Project_Creator role", repositoryRoles.contains(PoddRoles.PROJECT_CREATOR));
    }

    @Test
    public void testGetRoleByName() throws Exception
    {
        final String testRoleName = PoddRoles.ADMIN.getName();

        final RestletUtilRole roleByName = PoddRoles.getRoleByName(testRoleName);
        Assert.assertEquals("Role has incorrect URI", PoddRoles.ADMIN.getURI(), roleByName.getURI());
    }
}
