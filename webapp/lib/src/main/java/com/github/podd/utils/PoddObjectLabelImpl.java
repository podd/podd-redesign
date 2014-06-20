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
package com.github.podd.utils;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.semanticweb.owlapi.model.IRI;

/**
 * Encapsulates the basic label and description metadata about an object into a single object.
 *
 * This class must only ever be used to present precompiled sets of results to users. In other
 * cases, lists of {@link InferredOWLOntologyID}, {@link IRI} or {@link URI} are the correct way to
 * process information.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class PoddObjectLabelImpl implements PoddObjectLabel
{
    private InferredOWLOntologyID ontologyID;
    
    private URI objectID;
    
    // TODO: Migrate this to be Literal to preserve datatype and language where
    // necessary
    private String label;
    
    // TODO: Migrate this to be Literal to preserve datatype and language where
    // necessary
    private String description;
    
    private String barcode;
    
    /**
     * Creates a label object without a description, and without an object, meaning that the label
     * applies to the ontology itself.
     *
     * @param ontologyID
     *            The base artifact that this label is applied to.
     * @param label
     *            The label for this ontology.
     */
    public PoddObjectLabelImpl(final InferredOWLOntologyID ontologyID, final String label)
    {
        this.ontologyID = ontologyID;
        this.label = label;
    }
    
    /**
     * Creates a label object without a description, and without an object, meaning that the label
     * applies to the ontology itself.
     *
     * @param ontologyID
     *            The base artifact that this label is applied to.
     * @param label
     *            The label for this ontology.
     */
    public PoddObjectLabelImpl(final InferredOWLOntologyID ontologyID, final String label, final String description)
    {
        this(ontologyID, label);
        this.description = description;
    }
    
    /**
     * Creates a label object without a description
     *
     * @param parent
     *            The base artifact that this label is applied to.
     * @param object
     *            The object inside of the given artifact that this label is for.
     * @param label
     *            The label for this object.
     */
    public PoddObjectLabelImpl(final InferredOWLOntologyID ontologyID, final URI object, final String label)
    {
        this(ontologyID, label);
        this.objectID = object;
    }
    
    /**
     * Creates a label object with a description
     *
     * @param parent
     *            The base artifact that this label is applied to.
     * @param object
     *            The object inside of the given artifact that this label is for.
     * @param label
     *            The label for this object.
     * @param description
     *            The description for this object.
     */
    public PoddObjectLabelImpl(final InferredOWLOntologyID parent, final URI object, final String label,
            final String description)
    {
        this(parent, object, label);
        this.description = description;
    }
    
    /**
     * Creates a label object with a description
     *
     * @param parent
     *            The base artifact that this label is applied to.
     * @param object
     *            The object inside of the given artifact that this label is for.
     * @param label
     *            The label for this object.
     * @param description
     *            The description for this object.
     * @param barcode
     *            The barcode for this object.
     */
    public PoddObjectLabelImpl(final InferredOWLOntologyID parent, final URI object, final String label,
            final String description, final String barcode)
    {
        this(parent, object, label, description);
        this.barcode = barcode;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.utils.PoddObjectLabel#getDescription()
     */
    @Override
    public String getDescription()
    {
        return this.description;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.utils.PoddObjectLabel#getLabel()
     */
    @Override
    public String getLabel()
    {
        return this.label;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.utils.PoddObjectLabel#getObjectID()
     */
    @Override
    public URI getObjectURI()
    {
        return this.objectID;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.utils.PoddObjectLabel#getParentArtifactID()
     */
    @Override
    public InferredOWLOntologyID getOntologyID()
    {
        return this.ontologyID;
    }
    
    @Override
    public String toString()
    {
        final StringBuilder b = new StringBuilder("[");
        b.append(" objectURI=");
        b.append(this.objectID.stringValue());
        b.append(" label=");
        b.append(this.label);
        b.append(" description=");
        b.append(this.description);
        b.append("]");
        return b.toString();
    }
    
    @Override
    public Literal getDescriptionLiteral()
    {
        return PODD.VF.createLiteral(this.description);
    }
    
    @Override
    public Literal getLabelLiteral()
    {
        return PODD.VF.createLiteral(this.label);
    }
    
    @Override
    public String getBarcode()
    {
        return barcode;
    }
    
    @Override
    public Literal getBarcodeLiteral()
    {
        return PODD.VF.createLiteral(this.barcode);
    }
}
