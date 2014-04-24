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

import com.github.podd.api.data.SPARQLDataReference;
import com.github.podd.utils.PODD;

/**
 * A simple implementation of a SPARQL Data Reference object for use within PODD.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class SPARQLDataReferenceImpl extends AbstractDataReferenceImpl implements SPARQLDataReference
{
    private String graph;

    /**
     * Constructor
     */
    public SPARQLDataReferenceImpl()
    {
        super();
    }

    @Override
    public String getGraph()
    {
        return this.graph;
    }

    @Override
    public void setGraph(final String filename)
    {
        this.graph = filename;
    }

    @Override
    public Model toRDF()
    {
        final Model result = super.toRDF();

        result.add(this.getObjectIri().toOpenRDFURI(), RDF.TYPE, PODD.PODD_BASE_DATA_REFERENCE_TYPE_SPARQL);

        if(this.getGraph() != null)
        {
            result.add(this.getObjectIri().toOpenRDFURI(), PODD.PODD_BASE_HAS_SPARQL_GRAPH,
                    PODD.VF.createLiteral(this.getGraph()));
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
        b.append(this.getGraph());
        b.append(" , ");
        b.append(this.getRepositoryAlias());
        b.append("]");

        return b.toString();
    }

}
