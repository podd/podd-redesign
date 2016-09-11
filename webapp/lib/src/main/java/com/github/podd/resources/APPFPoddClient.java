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
package com.github.podd.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;


import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.github.podd.exception.PoddException;
import com.github.podd.ontologies.PODDBASE;
import com.github.podd.ontologies.PODDSCIENCE;
import com.github.podd.resources.Filter;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddWebConstants;

/**
 * Provides operations in relation to queries and other user requests in PODD.
 *
 * @author Vidya Bala
 */
public class APPFPoddClient extends RestletPoddClientImpl
{
    public int limit = 100;
    
    public APPFPoddClient()
    {
        super();
    }
    
    public APPFPoddClient(final String poddServerUrl)
    {
        super(poddServerUrl);
        
        try {
        	final String username = this.getProps().get(RestletPoddClientImpl.PROP_PODD_USERNAME, null);
            final String password = this.getProps().get(RestletPoddClientImpl.PROP_PODD_PASSWORD, null);
			boolean var = this.login(username, password);
		} catch (PoddException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void setLimit(int lim) {
    	limit = lim;
    }
    
    public Representation keywordSearch(String keyword) throws Exception {
		
    	final String TEMPLATE = new StringBuilder()
	            .append("CONSTRUCT { ?object ?predicate ?value . ?object ?pred ?val . }").append(" WHERE { {?object ?predicate ?value . FILTER(STRSTARTS(?value, \"" + keyword + "\"))} { ?object ?pred ?val .}}")
	            .toString();
        
        
        Representation res = this.doSPARQL2(TEMPLATE, null);
        return res;
	}
    
	public Representation listAllExperiments() throws Exception {
		
        final String TEMPLATE = new StringBuilder()
                .append("CONSTRUCT { ")
                .append("?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . ")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . } ")
                .append(" WHERE { ?object a ?type . ")
                .append(" OPTIONAL { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . } } LIMIT " + limit)
                .append(" VALUES (?type) { (<http://purl.org/podd/ns/poddScience#Experiment>) }").toString();
        
        
        Representation res = this.doSPARQL2(TEMPLATE, null);
        return res;
	}
	
	public Representation listAllgenotypes() throws Exception {
		
		final String TEMPLATE = new StringBuilder()
        		.append("CONSTRUCT { ")
        		.append(" ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . }")
                .append(" WHERE {?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype .")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .} } LIMIT " + limit)
               
                .toString();
        
        
        Representation res = this.doSPARQL2(TEMPLATE, null);
        return res;
	}
	
	public Representation filterAllGenotypes(List<Filter> filter) throws Exception {
		
		String genus = "";
		String species = "";
		
		if (!filter.isEmpty()) {
			for (int i = 0; i < filter.size(); ++i) {
				switch(filter.get(i).getField()) {
				case "genus": genus = filter.get(i).getValue();
				case "species" : species = filter.get(i).getValue();
				
				}
			}	
		}
		
        
        final String TEMPLAT = new StringBuilder()
        		.append("CONSTRUCT { ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . ")
        		.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . }")
                
                .append(" WHERE {")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")                
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                .append(" {?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }} LIMIT " + limit)
               
                .toString();
        
        Representation res = this.doSPARQL2(TEMPLAT, null);
        return res;
	}
	
	public Representation filterExperiments(List<Filter> filter) throws Exception {
		
		String barcode = "";
		String genus = "";
		String species = "";
		
		if (!filter.isEmpty()) {
			for (int i = 0; i < filter.size(); ++i) {
				switch(filter.get(i).getField()) {
				case "barcode": barcode = filter.get(i).getValue();
				case "genus": genus = filter.get(i).getValue();
				case "species" : species = filter.get(i).getValue();
				
				}
			}
		}
	    
	    
		final String TEMPLATE = new StringBuilder()
                .append("CONSTRUCT { ")
                .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . ")
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .}")
                .append(" WHERE { ")
                .append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . FILTER(STRSTARTS(?barcode, \"" + barcode + "\"))} } LIMIT " + limit).toString();
               
        
        Representation res = this.doSPARQL2(TEMPLATE, null);
        return res;
	}
	
	public Representation listAllPlants() throws Exception {
		
		
        final String TEMPLATE = new StringBuilder()
        		.append("CONSTRUCT { ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . ")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatment .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date . ")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date . ")
        		.append(" ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype .")
        		.append(" ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . }")
                .append(" WHERE {")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatment .}")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date . }")
                .append(" {?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                .append(" {?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype .}")
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .}")
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .}} LIMIT " + limit)               
                .toString();
        
        
        Representation res = this.doSPARQL2(TEMPLATE, null);
        return res;
	}
	
	public Representation filterAllPlants(List<Filter> filter) throws Exception {
		
		String treatment = "";
		String genus = "";
		String species = "";
		
		if (!filter.isEmpty()) {
			for (int i = 0; i < filter.size(); ++i) {
				switch(filter.get(i).getField()) {
				case "treatment": treatment = filter.get(i).getValue();
				case "genus": genus = filter.get(i).getValue();
				case "species" : species = filter.get(i).getValue();
				
				}
			}	
		}
		
        
        final String TEMPLAT = new StringBuilder()
        		.append("CONSTRUCT { ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . ")
        		.append(" ?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .")
        		.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . ")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatment .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date .}")
                
                .append(" WHERE {?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype .")
                .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")                
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                .append(" {?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatment . FILTER(STRSTARTS(?treatment, \"" + treatment + "\"))}")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date . }} LIMIT " + limit)
               
                .toString();
        
        Representation res = this.doSPARQL2(TEMPLAT, null);
        return res;
	}
    /**
     * Get a unique object based on the given parent, parentPredicate, type, and barcode
     *
     * @param uploadModel
     *            The model to add the new URI to if it is created.
     * @param parent
     * @param parentPredicate
     * @param type
     * @param tempUriString
     * @param artifactId
     * @param barcode
     * @return
     * @throws PoddClientException
     * @throws GraphUtilException
     */
    public synchronized URI getOrCreateByBarcode(final Model uploadModel, final URI parent, final URI parentPredicate,
            final URI type, final String tempUriString, final InferredOWLOntologyID artifactId, final String barcode)
        throws PoddException
    {
        // Attempt to find one remotely
        final Model existingNode = this.getObjectsByTypeAndBarcode(type, barcode, Arrays.asList(artifactId));
        
        URI dataUri = null;
        
        if(existingNode.isEmpty())
        {
            // If a remote instance was not available attempt to find one in the set of RDF
            // statements about to be uploaded
            final Set<Value> objects = uploadModel.filter(parent, parentPredicate, null).objects();
            for(final Value nextObject : objects)
            {
                if(nextObject instanceof URI)
                {
                    final Literal barcodeLiteral =
                            uploadModel.filter((URI)nextObject, PODDSCIENCE.HAS_BARCODE, null).objectLiteral();
                    if(barcodeLiteral.getLabel().equals(barcode))
                    {
                        dataUri = (URI)nextObject;
                    }
                }
            }
            
            // If one was not found locally, create a stub and return the URI so the user can attach
            // more information to it
            if(dataUri == null)
            {
                dataUri = this.getTempURI(tempUriString);
                uploadModel.add(parent, parentPredicate, dataUri);
                uploadModel.add(dataUri, RDF.TYPE, type);
                uploadModel.add(dataUri, PODDSCIENCE.HAS_BARCODE,
                        RestletPoddClientImpl.vf.createLiteral(barcode, XMLSchema.STRING));
            }
        }
        else
        {
            try
            {
                dataUri = GraphUtil.getUniqueSubjectURI(existingNode, RDF.TYPE, type);
            }
            catch(final GraphUtilException e)
            {
                System.out.println(e.toString());
            }
            if(dataUri == null)
            {
                try {
					throw new Exception(
					        "Failed to find or create a new barcoded object for: parent=" + parent + " predicate="
					                + parentPredicate + " type=" + type + " barcode=" + barcode);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
        
        // HACK: Cannot seem to inline this or the command line javac doesn't necessarily like
        // it, although Eclipse Java Compiler doesn't mind either way
        return dataUri;
    }
    
    
}