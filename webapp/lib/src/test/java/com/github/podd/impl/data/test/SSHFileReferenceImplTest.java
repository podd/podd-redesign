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
package com.github.podd.impl.data.test;

import com.github.podd.api.data.SSHFileReference;
import com.github.podd.api.data.test.AbstractSSHFileReferenceTest;
import com.github.podd.impl.data.SSHFileReferenceImpl;

/**
 * @author kutila
 *
 */
public class SSHFileReferenceImplTest extends AbstractSSHFileReferenceTest
{

    @Override
    protected SSHFileReference getNewSSHFileReference()
    {
        final SSHFileReference ref = new SSHFileReferenceImpl();
        return ref;
    }

}
