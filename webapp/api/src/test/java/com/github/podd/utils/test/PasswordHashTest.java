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
package com.github.podd.utils.test;

import org.junit.Assert;
import org.junit.Test;

import com.github.podd.utils.PasswordHash;

public class PasswordHashTest
{
    @Test
    public final void testCreateHashString() throws Exception
    {
        for(int i = 0; i < 10; i++)
        {
            final String password = Integer.toHexString(i);
            final String hash = PasswordHash.createHash(password);
            final String secondHash = PasswordHash.createHash(password);

            Assert.assertNotNull(hash);
            Assert.assertNotNull(secondHash);

            Assert.assertNotEquals(hash, secondHash);
        }
    }

    @Test
    public final void testCreateHashCharArray() throws Exception
    {
        for(int i = 0; i < 10; i++)
        {
            final String password = Integer.toHexString(i);
            final String hash = PasswordHash.createHash(password.toCharArray());
            final String secondHash = PasswordHash.createHash(password.toCharArray());

            Assert.assertNotNull(hash);
            Assert.assertNotNull(secondHash);

            Assert.assertNotEquals(hash, secondHash);
        }
    }

    @Test
    public final void testValidatePasswordStringString() throws Exception
    {
        for(int i = 0; i < 10; i++)
        {
            final String password = Integer.toHexString(i);
            final String hash = PasswordHash.createHash(password);

            Assert.assertNotNull(hash);

            Assert.assertTrue(PasswordHash.validatePassword(password, hash));
            Assert.assertFalse(PasswordHash.validatePassword(Integer.toHexString(i + 1), hash));
        }
    }

    @Test
    public final void testValidatePasswordCharArrayString() throws Exception
    {
        for(int i = 0; i < 10; i++)
        {
            final String password = Integer.toHexString(i);
            final String hash = PasswordHash.createHash(password.toCharArray());

            Assert.assertNotNull(hash);

            Assert.assertTrue(PasswordHash.validatePassword(password.toCharArray(), hash));

            Assert.assertFalse(PasswordHash.validatePassword(Integer.toHexString(i + 1).toCharArray(), hash));
        }
    }

}
