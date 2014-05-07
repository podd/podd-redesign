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
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.api.data.DataReference;
import com.github.podd.ontologies.PODDBASE;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PODD;

public abstract class AbstractDataReferenceImpl implements DataReference
{
    
    private OWLOntologyID artifactID;
    private String label;
    private IRI objectIri;
    private IRI parentIri;
    private String repositoryAlias;
    /**
     * Defaults to http://purl.org/podd/ns/poddBase#hasDataReference
     */
    private IRI parentPredicateIRI = IRI.create(PODDBASE.HAS_DATA_REFERENCE);
    
    public AbstractDataReferenceImpl()
    {
        super();
    }
    
    @Override
    public OWLOntologyID getArtifactID()
    {
        return this.artifactID;
    }
    
    @Override
    public String getLabel()
    {
        return this.label;
    }
    
    @Override
    public IRI getObjectIri()
    {
        return this.objectIri;
    }
    
    @Override
    public IRI getParentIri()
    {
        return this.parentIri;
    }
    
    @Override
    public IRI getParentPredicateIRI()
    {
        return this.parentPredicateIRI;
    }
    
    @Override
    public String getRepositoryAlias()
    {
        return this.repositoryAlias;
    }
    
    @Override
    public void setArtifactID(final OWLOntologyID artifactID)
    {
        if(artifactID instanceof InferredOWLOntologyID)
        {
            this.artifactID = ((InferredOWLOntologyID)artifactID).getBaseOWLOntologyID();
        }
        else
        {
            this.artifactID = artifactID;
        }
    }
    
    @Override
    public void setLabel(final String label)
    {
        this.label = label;
    }
    
    @Override
    public void setObjectIri(final IRI objectIri)
    {
        this.objectIri = objectIri;
    }
    
    @Override
    public void setParentIri(final IRI parentIri)
    {
        this.parentIri = parentIri;
    }
    
    @Override
    public void setParentPredicateIRI(final IRI parentPredicateIRI)
    {
        this.parentPredicateIRI = parentPredicateIRI;
    }
    
    @Override
    public void setRepositoryAlias(final String repositoryAlias)
    {
        this.repositoryAlias = repositoryAlias;
    }
    
    @Override
    public final Model toRDF()
    {
        final Model model = new LinkedHashModel();
        return toRDF(model);
    }
    
    @Override
    public Model toRDF(Model model)
    {
        if(this.getParentIri() == null)
        {
            throw new IllegalStateException("Parent IRI must not be null for a data reference");
        }
        
        if(this.getRepositoryAlias() == null)
        {
            throw new IllegalStateException("Repository alias must not be null for a data reference");
        }
        
        // Setup a temporary URI if it is not setup explicitly
        if(this.getObjectIri() == null)
        {
            this.setObjectIri(IRI.create("urn:temp:uuid:datareference:" + this.getClass().getSimpleName() + ":"));
        }
        
        if(this.getParentIri().equals(this.getObjectIri()))
        {
            throw new IllegalStateException("The parent IRI must be distinct from the data reference object IRI");
        }
        
        // Override users who set the parent predicate IRI to null using the default
        if(this.getParentPredicateIRI() == null)
        {
            this.setParentPredicateIRI(IRI.create(PODDBASE.HAS_DATA_REFERENCE));
        }
        
        model.add(this.getParentIri().toOpenRDFURI(), this.getParentPredicateIRI().toOpenRDFURI(), this.getObjectIri()
                .toOpenRDFURI());
        model.add(this.getObjectIri().toOpenRDFURI(), RDF.TYPE, PODD.PODD_BASE_DATA_REFERENCE_TYPE);
        model.add(this.getObjectIri().toOpenRDFURI(), PODD.PODD_BASE_HAS_ALIAS,
                PODD.VF.createLiteral(this.getRepositoryAlias()));
        
        if(this.getArtifactID() != null)
        {
            OntologyUtils.ontologyIDToRDF(this.getArtifactID(), model, false);
        }
        
        if(this.getLabel() != null)
        {
            model.add(this.getObjectIri().toOpenRDFURI(), RDFS.LABEL, PODD.VF.createLiteral(this.getLabel()));
        }
        
        return model;
    }
}