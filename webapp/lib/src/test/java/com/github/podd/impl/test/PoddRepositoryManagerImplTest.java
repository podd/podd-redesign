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
package com.github.podd.impl.test;

import java.nio.file.Path;

import org.junit.Assert;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryImplConfigBase;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.test.AbstractPoddRepositoryManagerTest;
import com.github.podd.impl.PoddRepositoryManagerImpl;

/**
 * @author kutila
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class PoddRepositoryManagerImplTest extends AbstractPoddRepositoryManagerTest
{
    
    @Override
    protected PoddRepositoryManager getNewPoddRepositoryManagerInstance(final Repository managementRepository,
            final Path tempDirPath) throws Exception
    {
        final Model graph =
                Rio.parse(this.getClass().getResourceAsStream("/memorystoreconfig.ttl"), "", RDFFormat.TURTLE);
        final Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RepositoryConfigSchema.REPOSITORYTYPE, null);
        final RepositoryImplConfig repositoryImplConfig = RepositoryImplConfigBase.create(graph, repositoryNode);
        Assert.assertNotNull(repositoryImplConfig);
        Assert.assertNotNull(repositoryImplConfig.getType());
        return new PoddRepositoryManagerImpl(managementRepository, repositoryImplConfig, "", tempDirPath,
                new PropertyUtil("podd"));
    }
}
