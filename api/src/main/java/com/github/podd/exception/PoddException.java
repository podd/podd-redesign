/*
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
 package com.github.podd.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import com.github.podd.utils.PoddRdfConstants;

/**
 * This class extends <code>java.lang.Exception</code> to provide a PODD specific checked exception
 * base class.
 * 
 * This exception class is abstract and cannot be directly instantiated.
 * 
 * @author kutila
 * @author Peter Ansell p_ansell@yahoo.com
 */
public abstract class PoddException extends Exception
{
    private static final long serialVersionUID = -6240755031638346731L;
    
    public PoddException(final String msg)
    {
        super(msg);
    }
    
    public PoddException(final String msg, final Throwable throwable)
    {
        super(msg, throwable);
    }
    
    public PoddException(final Throwable throwable)
    {
        super(throwable);
    }
    
    /**
     * Retrieve details about this Exception instance as a {@link Model}. This method should be
     * overridden by sub-classes to provide more specific details.
     * 
     * @param errorResource 
     * 
     * @return A non-empty {@link Model} containing details about this Exception
     */
    public Model getDetailsAsModel(final Resource errorResource)
    {
        final Model model = new LinkedHashModel();

        model.add(errorResource, RDF.TYPE, PoddRdfConstants.ERR_TYPE_ERROR);
        model.add(errorResource, PoddRdfConstants.ERR_EXCEPTION_CLASS,
                PoddRdfConstants.VF.createLiteral(this.getClass().getName()));
        
        if(this.getMessage() != null)
        {
            model.add(errorResource, RDFS.LABEL, PoddRdfConstants.VF.createLiteral(this.getMessage()));
        }
        
        final StringWriter sw = new StringWriter();
        this.printStackTrace(new PrintWriter(sw));
        model.add(errorResource, RDFS.COMMENT, PoddRdfConstants.VF.createLiteral(sw.toString()));
        
        return model;
    }
}
