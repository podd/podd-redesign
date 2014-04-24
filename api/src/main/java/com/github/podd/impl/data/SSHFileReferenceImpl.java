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
package com.github.podd.impl.data;

import org.openrdf.model.Model;
import org.openrdf.model.vocabulary.RDF;

import com.github.podd.api.data.SSHFileReference;
import com.github.podd.utils.PODD;

/**
 * A simple implementation of an SSH File Reference object for use within PODD.
 *
 * @author kutila
 */
public class SSHFileReferenceImpl extends AbstractDataReferenceImpl implements SSHFileReference
{

    private String filename;
    private String path;

    /**
     * Constructor
     */
    public SSHFileReferenceImpl()
    {
        super();
    }

    @Override
    public String getFilename()
    {
        return this.filename;
    }

    @Override
    public String getPath()
    {
        return this.path;
    }

    @Override
    public void setFilename(final String filename)
    {
        this.filename = filename;
    }

    @Override
    public void setPath(final String path)
    {
        this.path = path;
    }

    @Override
    public Model toRDF()
    {
        final Model result = super.toRDF();

        result.add(this.getObjectIri().toOpenRDFURI(), RDF.TYPE, PODD.PODD_BASE_FILE_REFERENCE_TYPE_SSH);

        if(this.getFilename() != null)
        {
            result.add(this.getObjectIri().toOpenRDFURI(), PODD.PODD_BASE_HAS_FILENAME,
                    PODD.VF.createLiteral(this.getFilename()));
        }

        if(this.getPath() != null)
        {
            result.add(this.getObjectIri().toOpenRDFURI(), PODD.PODD_BASE_HAS_FILE_PATH,
                    PODD.VF.createLiteral(this.getPath()));
        }

        return result;
    }

    @Override
    public String toString()
    {
        final StringBuilder b = new StringBuilder();
        b.append("[");
        b.append(this.getArtifactID());
        b.append(" , ");
        b.append(this.getParentIri());
        b.append(" , ");
        b.append(this.getObjectIri());
        b.append(" , ");
        b.append(this.getLabel());
        b.append(" , ");
        b.append(this.filename);
        b.append(" , ");
        b.append(this.path);
        b.append(" , ");
        b.append(this.getRepositoryAlias());
        b.append("]");

        return b.toString();
    }

}
