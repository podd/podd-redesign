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
import java.util.Timer;
import java.util.TimerTask;
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
    public int limit = 20;
    
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
	            .append("CONSTRUCT { ?object ?pred ?val . ?object ?predicate ?value . ?object ?p ?v .}")
                
        
	            .append(" WHERE { { ?object ?predicate ?value . FILTER(STRSTARTS(?value, \"" + keyword + "\"))} { ?object ?pred ?val .} }")
                    
                    //.append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}}")
                    /*.append(" OPTIONAL {?parent <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . }")
                    .append(" OPTIONAL {?parent <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm . }}")
                    .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                    .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                    .append(" OPTIONAL {?parent <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                    .append(" OPTIONAL {?parent <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . }")
                    .append(" OPTIONAL {?parent <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm . }}") */
	            .toString();
        
        
        Representation res = this.doSPARQL2(TEMPLATE, null);
        return res;
	}
    
    public Representation keywordSearchMes(String keyword) throws Exception {
		
    	final String TEMPLATE = new StringBuilder()
	            .append("CONSTRUCT { ?object ?pred ?val . ?object ?predicate ?value . ?object ?p ?v ."). append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .}")
        
	            .append(" WHERE { { ?parent ?p ?v .} {?parent <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype .} {?parent <http://purl.org/podd/ns/poddScience#hasMeasurement> ?object . }  { ?object ?predicate ?value . FILTER(STRSTARTS(?value, \"" + keyword + "\"))} { ?object ?pred ?val .} ")
                    .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . }")
                    .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . }} LIMIT 50000")
                    //.append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}}")
                    /*.append(" OPTIONAL {?parent <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . }")
                    .append(" OPTIONAL {?parent <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm . }}")
                    .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                    .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                    .append(" OPTIONAL {?parent <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                    .append(" OPTIONAL {?parent <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . }")
                    .append(" OPTIONAL {?parent <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm . }}") */
	            .toString();
    	String TEMPLAT = new StringBuilder()
                .append("CONSTRUCT { ?object a ?type . ?object ?predicate ?value . }")
                .append(" WHERE { ?object a ?type . ?object ?predicate ?value . }").append(" VALUES (?type) { ( <http://purl.org/podd/ns/poddScience#Material> ) }")
                .toString();
        String TEMPLATEF =
                new StringBuilder()
                        .append("CONSTRUCT { ?parent ?parentPredicate ?object . ?object a ?type . ?object ?predicate ?label . }")
                        .append(" WHERE { ?parent ?parentPredicate ?object . ?object a ?type . OPTIONAL { ?object ?predicate ?label . } }")
                        .append(" VALUES (?type ) { ( <http://purl.org/podd/ns/poddScience#Material>) }").toString();
        

        
        Representation res = this.doSPARQL2(TEMPLATEF, null);
        return res;
	}
public Representation keywordSearch2(String keyword) throws Exception {
		
    	final String TEMPLATE = new StringBuilder()
	            .append("CONSTRUCT { ?object ?predicate ?value}").append(" WHERE { {?parent <http://purl.org/podd/ns/poddScience#hasMeasurement> ?object . } {?object ?predicate ?value. FILTER(STRSTARTS(?predicate, \"" + keyword + "\"))}}")
	            .toString();
        
    	final String TEMPLAT = new StringBuilder()
                .append("CONSTRUCT { ?object a ?type . ?object ?predicate ?value . }")
                .append(" WHERE { ?object a ?type . ?object ?predicate ?value . }").append(" VALUES (?type) { ( <http://purl.org/podd/ns/poddScience#Genotype>  ) }").toString();
    	final String ne = "CONSTRUCT { ?s a ?o } WHERE { ?s a ?o }";
        Representation res = this.doSPARQL2(ne, null);
        return res;
	}
	public Representation listAllExperiments() throws Exception {
		
        final String TEMPLATE = new StringBuilder()
                .append("CONSTRUCT { ")
                .append("?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . ")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .}")
                .append(" WHERE { ?object a ?type . ")
                
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasContainer> ?tray . ?tray <http://purl.org/podd/ns/poddScience#hasContainer> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?mat . ?mat <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . }")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasContainer> ?tray . ?tray <http://purl.org/podd/ns/poddScience#hasContainer> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?mat . ?mat <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . }")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . } } LIMIT " + limit)
                .append(" VALUES (?type) { (<http://purl.org/podd/ns/poddScience#Experiment>) }").toString();
        
        final String TEMPLAT = new StringBuilder()
        		.append("CONSTRUCT { ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?pl . ")
        		
        		.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . }")
                
                
                .append(" WHERE {")
                .append(" { ?e <http://purl.org/podd/ns/poddScience#hasExperiment> ?o .}")
                .append(" { ?e <http://purl.org/podd/ns/poddScience#hasBarcode> ?pl . }")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . }")
                //.append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?object .}")
                //.append("{?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype .}")
                //.append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . }")
                .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . }")                
                //.append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}} LIMIT 1000").toString(); 
        Representation res = this.doSPARQL2(TEMPLAT, null);
        return res;
	}
	
	public Representation listAllgenotypes() throws Exception {
		
		final String TEMPLAT = new StringBuilder()
        		.append("CONSTRUCT { ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?pl . ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . ")
        		.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . }")
                
                .append(" WHERE {")
                
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . }")
               
                .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . }")                
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                .append(" OPTIONAL {?genotype <http://www.w3.org/2000/01/rdf-schema#label> ?label . }} LIMIT " + limit)
               
                .toString();
        
        
        Representation res = this.doSPARQL2(TEMPLAT, null);
        return res;
	}
	
	public Representation filterAllGenotypes(List<Filter> filter) throws Exception {
		
		String genus = "";
		String species = "";
		String barcode = "";
		if (!filter.isEmpty()) {
			for (int i = 0; i < filter.size(); ++i) {
				switch(filter.get(i).getField()) {
				case "genus": genus = filter.get(i).getValue(); break;
				case "species" : species = filter.get(i).getValue(); break;
				case "barcode" : barcode = filter.get(i).getValue(); break;
				}
			}	
		}
		
		final String TEMPLAT = new StringBuilder()
        		.append("CONSTRUCT { ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?pl . ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . ")
        		.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . }")
                
                .append(" WHERE {")
                
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
               
                .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")                
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                .append(" OPTIONAL {?genotype <http://www.w3.org/2000/01/rdf-schema#label> ?label . }} LIMIT " + limit).toString();
		
        final String TEMPLAT1 = new StringBuilder()
        		.append("CONSTRUCT { ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?pl . ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . ")
        		.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . }")
                
                .append(" WHERE {")
                .append("  {?e <http://purl.org/podd/ns/poddScience#hasExperiment> ?o .}")
                .append("  {?e <http://purl.org/podd/ns/poddScience#hasBarcode> ?pl . FILTER(STRSTARTS(?pl, \"" + barcode + "\"))}")
                .append("  {?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?object .}")
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
				case "barcode": barcode = filter.get(i).getValue(); break;
				case "genus": genus = filter.get(i).getValue(); break;
				case "species" : species = filter.get(i).getValue(); break;
				
				}
			}
		}
	    
        final String TEMPLAT = new StringBuilder()
        		.append("CONSTRUCT { ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?pl . ")
        		
        		.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . }")
                
                
                .append(" WHERE {")
                .append(" { ?e <http://purl.org/podd/ns/poddScience#hasExperiment> ?o .}")
                .append(" { ?e <http://purl.org/podd/ns/poddScience#hasBarcode> ?pl . FILTER(STRSTARTS(?pl, \"" + barcode + "\"))}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                
                .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")                
                //.append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}} LIMIT 1000").toString(); 
                  
                
                 
		
		final String TEMPLATE = new StringBuilder()
                .append("CONSTRUCT { ")
                .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . ")
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .}")
                .append(" WHERE { ")
                .append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . FILTER(STRSTARTS(?barcode, \"" + barcode + "\"))}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                //.append(" OPTIONAL { ?object <http://purl.org/podd/ns/poddScience#hasExperiment> ?exp . ?exp <http://purl.org/podd/ns/poddScience#hasEnvironment> ?env . ?env <http://www.w3.org/2000/01/rdf-schema#label> ?la . }")
                .append("  } LIMIT " + limit).toString();
		
		
		
		
		
		
		final String TEMPLAT4 = new StringBuilder()
                .append("CONSTRUCT { ")
                .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . ")
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .}")
                .append(" WHERE { ")
                .append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . FILTER(STRSTARTS(?barcode, \"" + barcode + "\"))} } LIMIT " + limit).toString();       
		final String TEMPLAT1 = new StringBuilder()
                .append("CONSTRUCT { ")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . ")
                .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . ")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . }")
                
                .append(" WHERE { ?object a ?type . ")
                .append(" OPTIONAL { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasContainer> ?tray . ?tray <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?mat . ?mat <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#hasContainer> ?tray . ?tray <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?mat . ?mat <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                .append(" OPTIONAL { ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . } } LIMIT " + limit)
                .append(" VALUES (?type) { (<http://purl.org/podd/ns/poddScience#Experiment>) } ").toString();
        Representation res = this.doSPARQL2(TEMPLAT, null);
        return res;
	}
	
public Representation countMeasurements(List<Filter> filter) throws Exception {
		System.out.println("perform counts");
		String barcode = "";
		String genus = "";
		String species = "";
		String type = "";
		String unit = "";
		String treatment = "";
		float greatr = 0;
		float lessthan = 0;
		if (!filter.isEmpty()) {
			for (int i = 0; i < filter.size(); ++i) {
				
				switch(filter.get(i).getField()) {
				case "barcode": barcode = filter.get(i).getValue(); break;
				case "genus": genus = filter.get(i).getValue(); break;
				case "species" : species = filter.get(i).getValue(); break;
				case "type" : type = filter.get(i).getValue(); break;
				case "unit" : unit = filter.get(i).getValue(); break;
				case "treatment" : treatment = filter.get(i).getValue(); break;
				case "greatr" : greatr = Float.parseFloat(filter.get(i).getValue()); break;
				case "lessthan" : lessthan = Float.parseFloat(filter.get(i).getValue()); break;
				}
			}
		}
	   
		
		final String TEMPLAT = new StringBuilder()
				.append("SELECT * WHERE {")
                .append("{ SELECT ")
                //.append("  ?object <http://www.w3.org/2000/01/rdf-schema#label> ?ti .")
                //.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
               // .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?pl .")
                .append(" (COUNT (DISTINCT ?m) AS  ?measurementCount)")
                //.append("(?pl AS ?Experiment)")
                 
              
                .append("")
                .append(" WHERE { ")
                .append(" { ?e <http://purl.org/podd/ns/poddScience#hasBarcode> ?pl .}")
                .append(" { ?e <http://purl.org/podd/ns/poddScience#hasExperiment> ?o .}")
                .append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?object .}")
                .append(" { ?o <http://www.w3.org/2000/01/rdf-schema#label> ?ti .}")
                //.append(" { ?e <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                //.append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
                //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
                //.append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                
                //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
                //.append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                //.append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                //.append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                //.append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                
                //.append(" {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . }")
                //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . } ")
                .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\")) }} LIMIT 1000} UNION")
                .append("{ SELECT ")
                //.append("  ?object <http://www.w3.org/2000/01/rdf-schema#label> ?ti .")
                //.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
                .append(" (?pl AS ?Experiment)")
                .append(" (?treatmenttype AS ?Treatment)")
                
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
                .append(" (?genus AS ?Genus) (?species AS ?Species) (?line AS ?Line)")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
                
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                
                
                
                
                
                //.append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?pl .")
                .append(" (?la AS ?MeasurementName)")
                .append(" (?val AS ?Value)")
               
                .append(" (?t AS ?Timestamp)")
                .append(" (?u AS ?Unit)")
                .append(" (?t2 AS ?Timestamp2)")
                .append(" (?u2 AS ?Unit2)")
                .append(" (?la2 AS ?MeasurementName2)")
                .append(" (?val2 AS ?Value2)")
                .append(" ")
                //.append(" ?m ?pred ?value .")
                //.append(" ?object ?p ?v .")
              
                .append("")
                .append(" WHERE { ")
                
                .append(" { ?e <http://purl.org/podd/ns/poddScience#hasExperiment> ?o .}")
                .append(" { ?e <http://purl.org/podd/ns/poddScience#hasBarcode> ?pl .}")
                .append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?object .}")
                //.append(" { ?o <http://www.w3.org/2000/01/rdf-schema#label> ?ti .}")
                .append(" { ?e <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
                //.append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                
                //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
                //.append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                //.append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                
                //.append(" {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
                .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\")) }")
                
                //.append(" {?m ?pred ?value .}}}")
                .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
                .append(" {?m2 <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t2 .}")
                .append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(((xsd:float(?val) > " + greatr + "))) }} }} LIMIT 10000")
                
                //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . }} ")
    			
				
				
				.toString();
		
		final String TEMPLA = new StringBuilder()
                .append(" SELECT {}")
                
                /*
                .append("  ?object <http://www.w3.org/2000/01/rdf-schema#label> ?ti .")
                .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                
                
                
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m .")
                .append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasTimestamp> ?t .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .")
                
                
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val2 .") */
                .append("")
                .append(" WHERE { ")
                .append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?object .}")
                //.append("{  ?o <http://www.w3.org/2000/01/rdf-schema#label> ?ti .}")
                //.append(" { ?e <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                //.append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                
                //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .}} ")
				.append( "} LIMIT 500000")
				
				
				.toString();
		String n = "SELECT DISTINCT ?s ?property ?o WHERE { ?s ?property ?o . }";
		final String TEMPLAT2 = new StringBuilder()
                .append("CONSTRUCT { ")
                .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m .")
                .append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasTimestamp> ?t .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .")
                
                
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .")
                
                .append("}")
                .append(" WHERE { ")
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
                .append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                
                //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                
                //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
                .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\"))}")
                .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
				.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) > " + greatr + ")}} LIMIT 50000")
				.toString();
		
		final String TEMPLAT3 = new StringBuilder()
                .append("CONSTRUCT { ")
                .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m .")
                .append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasTimestamp> ?t .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .")
                
                
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .")
                
                .append("}")
                .append(" WHERE { ")
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
                .append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                
                //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                
                //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
                .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\"))}")
                .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
				.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) > " + greatr + ")}")
				.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) < " + lessthan + ")}} LIMIT 50000").toString();
		
		final String TEMPLAT4 = new StringBuilder()
                .append("CONSTRUCT { ")
                .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m .")
                .append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasTimestamp> ?t .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .")
                
                
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .")
                
                .append("}")
                .append(" WHERE { ")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
                .append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                
                //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                
                //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
                .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\"))}")
                .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
				.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) > " + greatr + ")}")
				.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) < " + lessthan + ")}} LIMIT 50000").toString(); 
				
                //.append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .}}")
                //.append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .} }")
		Representation res;
		/*if (treatment.length() > 0 && lessthan > 0) {
        	res = this.doSPARQL2(TEMPLAT3, null);
        } else if (treatment.length() > 0){
        	res = this.doSPARQL2(TEMPLAT2, null);
        } else if (lessthan > 0) {
        	res = this.doSPARQL2(TEMPLAT4, null);
        } else { */
        res = this.doSPARQL3(TEMPLAT, null);
        //}
                      
		
        
        return res;
	}
	
public Representation filterMeasurements(List<Filter> filter) throws Exception {
		
		String barcode = "";
		String genus = "";
		String species = "";
		String type = "";
		String unit = "mg";
		String treatment = "";
		float greatr = 0;
		float lessthan = 0;
		if (!filter.isEmpty()) {
			for (int i = 0; i < filter.size(); ++i) {
				
				switch(filter.get(i).getField()) {
				case "barcode": barcode = filter.get(i).getValue(); break;
				case "genus": genus = filter.get(i).getValue(); break;
				case "species" : species = filter.get(i).getValue(); break;
				case "type" : type = filter.get(i).getValue(); break;
				case "unit" : unit = filter.get(i).getValue(); break;
				case "treatment" : treatment = filter.get(i).getValue(); break;
				case "greatr" : greatr = Float.parseFloat(filter.get(i).getValue()); break;
				case "lessthan" : lessthan = Float.parseFloat(filter.get(i).getValue()); break;
				}
			}
		}
	    
		genus = "";
		//treatment = "drought";
		final String TEMPLAT = new StringBuilder()
                .append("CONSTRUCT { ")
                //.append("  ?object <http://www.w3.org/2000/01/rdf-schema#label> ?ti .")
                //.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
                .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?pl .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                
                
                
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m .")
                //.append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?pl .")
                .append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasTimestamp> ?t .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .")
                //.append(" ?m ?pred ?value .")
                //.append(" ?object ?p ?v .")
              
                .append("}")
                .append(" WHERE { ")
                .append(" { ?e <http://www.w3.org/2000/01/rdf-schema#label> ?pl .}")
                .append(" { ?e <http://purl.org/podd/ns/poddScience#hasExperiment> ?o .}")
                .append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?object .}")
                .append(" { ?o <http://www.w3.org/2000/01/rdf-schema#label> ?ti .}")
                //.append(" { ?e <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                //.append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
                //.append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                
                //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                //.append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                //.append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                
                //.append(" {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
                
                .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\")) }")
                //.append(" {?m ?pred ?value .}}}")
                .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
                .append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(((xsd:float(?val) > " + greatr + "))) }} ")
                //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . }} ")
				.append("ORDER BY desc(?t) LIMIT 10")
				
				
				.toString();
		
		final String TEMPLA = new StringBuilder()
                .append(" SELECT {}")
                
                /*
                .append("  ?object <http://www.w3.org/2000/01/rdf-schema#label> ?ti .")
                .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                
                
                
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m .")
                .append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasTimestamp> ?t .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .")
                
                
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val2 .") */
                .append("")
                .append(" WHERE { ")
                .append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?object .}")
                //.append("{  ?o <http://www.w3.org/2000/01/rdf-schema#label> ?ti .}")
                //.append(" { ?e <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                //.append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                
                //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .}} ")
				.append( "} LIMIT 500000")
				
				
				.toString();
		String n = "SELECT DISTINCT ?s ?property ?o WHERE { ?s ?property ?o . }";
		final String TEMPLAT2 = new StringBuilder()
                .append("CONSTRUCT { ")
                .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m .")
                .append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasTimestamp> ?t .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .")
                
                
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .")
                
                .append("}")
                .append(" WHERE { ")
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
                .append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                
                //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                
                //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
                .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\"))}")
                .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
				.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) > " + greatr + ")}} LIMIT 50000")
				.toString();
		
		final String TEMPLAT3 = new StringBuilder()
                .append("CONSTRUCT { ")
                .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m .")
                .append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasTimestamp> ?t .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .")
                
                
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .")
                
                .append("}")
                .append(" WHERE { ")
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
                .append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                
                //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                
                //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
                .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\"))}")
                .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
				.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) > " + greatr + ")}")
				.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) < " + lessthan + ")}} LIMIT 50000").toString();
		
		final String TEMPLAT4 = new StringBuilder()
                .append("CONSTRUCT { ")
                .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
                //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m .")
                .append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasTimestamp> ?t .")
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .")
                
                
                .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .")
                
                .append("}")
                .append(" WHERE { ")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
                .append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                
                //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                
                //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
                .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\"))}")
                .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
				.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) > " + greatr + ")}")
				.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) < " + lessthan + ")}} LIMIT 50000").toString(); 
				
                //.append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .}}")
                //.append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .} }")
		Representation res;
		/*if (treatment.length() > 0 && lessthan > 0) {
        	res = this.doSPARQL2(TEMPLAT3, null);
        } else if (treatment.length() > 0){
        	res = this.doSPARQL2(TEMPLAT2, null);
        } else if (lessthan > 0) {
        	res = this.doSPARQL2(TEMPLAT4, null);
        } else { */
        res = this.doSPARQL2(TEMPLAT, null);
        //}
                      
		
        
        return res;
	}

public Representation filterMeasurements2(List<Filter> filter) throws Exception {
	
	String barcode = "";
	String genus = "";
	String species = "";
	String type = "";
	String type2 = "";
	String unit = "";
	String unit2 = "";
	String treatment = "";
	float greatr = 0;
	float greatr2 = 0;
	float lessthan = 0;
	float lessthan2 = 0;
	if (!filter.isEmpty()) {
		for (int i = 0; i < filter.size(); ++i) {
			
			switch(filter.get(i).getField()) {
			case "barcode": barcode = filter.get(i).getValue(); break;
			case "genus": genus = filter.get(i).getValue(); break;
			case "species" : species = filter.get(i).getValue(); break;
			case "type" : type = filter.get(i).getValue(); break;
			case "unit" : unit = filter.get(i).getValue(); break;
			case "unit2" : unit2 = filter.get(i).getValue(); break;
			case "treatment" : treatment = filter.get(i).getValue(); break;
			case "greatr" : greatr = Float.parseFloat(filter.get(i).getValue()); break;
			case "greatr2" : greatr2 = Float.parseFloat(filter.get(i).getValue()); break;
			case "type2" : type2 = (filter.get(i).getValue()); break;
			case "lessthan" : lessthan = Float.parseFloat(filter.get(i).getValue()); break;
			case "lessthan2" : lessthan2 = Float.parseFloat(filter.get(i).getValue()); break;
			}
		}
	}
    
	
	final String TEMPLAT = new StringBuilder()
            .append("SELECT ")
            //.append("  ?object <http://www.w3.org/2000/01/rdf-schema#label> ?ti .")
            //.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
            .append(" (?pl AS ?Experiment)")
            .append(" (?treatmenttype AS ?Treatment)")
            
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
            .append(" (?genus AS ?Genus) (?species AS ?Species) (?line AS ?Line)")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
            
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
            
            
            
            
            
            //.append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?pl .")
            .append(" (?la AS ?MeasurementName)")
            .append(" (?val AS ?Value)")
           
            .append(" (?t AS ?Timestamp)")
            .append(" (?u AS ?Unit)")
            .append(" (?t2 AS ?Timestamp2)")
            .append(" (?u2 AS ?Unit2)")
            .append(" (?la2 AS ?MeasurementName2)")
            .append(" (?val2 AS ?Value2)")
            .append(" ")
            //.append(" ?m ?pred ?value .")
            //.append(" ?object ?p ?v .")
          
            .append("")
            .append(" WHERE { ")
            
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasExperiment> ?o .}")
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasBarcode> ?pl .}")
            .append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?object .}")
            //.append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot2 . ?pot2 <http://purl.org/podd/ns/poddScience#hasMaterial> ?object2 .}")
            //.append(" { ?o <http://www.w3.org/2000/01/rdf-schema#label> ?ti .}")
   //.append(" { ?e <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            //.append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
          //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
            //.append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
            
            //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
            .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
            .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
            .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
            
            //.append(" {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
            
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m2 . ?m2 <http://www.w3.org/2000/01/rdf-schema#label> ?la2 . FILTER(STRSTARTS(?la2, \"" + type2 + "\"))}")
            .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\"))}")
            .append(" {?m2 <http://purl.org/podd/ns/poddScience#hasUnit> ?u2 . FILTER(STRSTARTS(?u2, \"" + unit2 + "\"))}")
            //.append(" {?m ?pred ?value .}}}")
            .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
            .append(" {?m2 <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t2 .}")
            .append("{?m2 <http://purl.org/podd/ns/poddScience#hasValue> ?val2 . FILTER(((xsd:float(?val2) > " + greatr2 + ")))}")
            //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(((xsd:float(?val)) > (xsd:float(?val2)) )) }")
            .append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(((xsd:float(?val) > " + greatr + "))) }}")
            //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . }} ")
			.append("LIMIT " + limit)
			
			
			.toString();
	 Representation res = this.doSPARQL3(TEMPLAT, null);
	 return res;
	 
}
public Representation filterMeasurements4(List<Filter> filter) throws Exception {
	
	String barcode = "";
	String genus = "";
	String species = "";
	String type = "";
	String type2 = "";
	String unit = "";
	String unit2 = "";
	String treatment = "";
	float greatr = -1;
	float greatr2 = 0;
	float lessthan = 0;
	float lessthan2 = 0;
	if (!filter.isEmpty()) {
		for (int i = 0; i < filter.size(); ++i) {
			
			switch(filter.get(i).getField()) {
			case "barcode": barcode = filter.get(i).getValue(); break;
			case "genus": genus = filter.get(i).getValue(); break;
			case "species" : species = filter.get(i).getValue(); break;
			case "type" : type = filter.get(i).getValue(); break;
			case "unit" : unit = filter.get(i).getValue(); break;
			case "unit2" : unit2 = filter.get(i).getValue(); break;
			case "treatment" : treatment = filter.get(i).getValue(); break;
			case "greatr" : greatr = Float.parseFloat(filter.get(i).getValue()); break;
			case "greatr2" : greatr2 = Float.parseFloat(filter.get(i).getValue()); break;
			case "type2" : type2 = (filter.get(i).getValue()); break;
			case "lessthan" : lessthan = Float.parseFloat(filter.get(i).getValue()); break;
			case "lessthan2" : lessthan2 = Float.parseFloat(filter.get(i).getValue()); break;
			}
		}
	}
    
	
	final String TEMPLAT = new StringBuilder()
            .append("SELECT ")
            //.append("  ?object <http://www.w3.org/2000/01/rdf-schema#label> ?ti .")
            //.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
            .append(" (?pl AS ?Experiment)")
            .append(" (?treatmenttype AS ?Treatment)")
            
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
            .append(" (?genus AS ?Genus) (?species AS ?Species) (?line AS ?Line)")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
            
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
            
            
            
            
            
            //.append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?pl .")
            .append(" (?la AS ?MeasurementName)")
            .append(" (?val AS ?Value)")
           
            .append(" (?t AS ?Timestamp)")
            .append(" (?u AS ?Unit)")
            .append(" (?t2 AS ?Timestamp2)")
            .append(" (?u2 AS ?Unit2)")
            .append(" (?la2 AS ?MeasurementName2)")
            .append(" (?val2 AS ?Value2)")
            .append(" ")
            //.append(" ?m ?pred ?value .")
            //.append(" ?object ?p ?v .")
          
            .append("")
            .append(" WHERE { ")
            
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasExperiment> ?o .}")
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasBarcode> ?pl . FILTER(STRSTARTS(?pl, \"" + barcode + "\"))}")
            .append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?object .}")
            //.append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot2 . ?pot2 <http://purl.org/podd/ns/poddScience#hasMaterial> ?object2 .}")
            //.append(" { ?o <http://www.w3.org/2000/01/rdf-schema#label> ?ti .}")
   //.append(" { ?e <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            //.append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
          //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
            //.append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
            
            //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
            .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
            .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
            .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
            
            //.append(" {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
            
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
            
            .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\"))}")
            
            //.append(" {?m ?pred ?value .}}}")
            .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
            
            
            //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(((xsd:float(?val)) > (xsd:float(?val2)) )) }")
            .append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(((xsd:float(?val) > " + greatr + "))) }}")
            //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . }} ")
			.append("LIMIT " + limit)
			
			
			.toString();
		Representation res = this.doSPARQL3(TEMPLAT, null);
		
	 
	 return res;
	 
}

public Representation filterPlants2(List<Filter> filter) throws Exception {
	
	String barcode = "";
	String genus = "";
	String species = "";
	String type = "";
	String type2 = "";
	String unit = "";
	String unit2 = "";
	String treatment = "";
	float greatr = 0;
	float greatr2 = 0;
	float lessthan = 0;
	float lessthan2 = 0;
	if (!filter.isEmpty()) {
		for (int i = 0; i < filter.size(); ++i) {
			
			switch(filter.get(i).getField()) {
			case "barcode": barcode = filter.get(i).getValue(); break;
			case "genus": genus = filter.get(i).getValue(); break;
			case "species" : species = filter.get(i).getValue(); break;
			case "type" : type = filter.get(i).getValue(); break;
			case "unit" : unit = filter.get(i).getValue(); break;
			case "unit2" : unit2 = filter.get(i).getValue(); break;
			case "treatment" : treatment = filter.get(i).getValue(); break;
			case "greatr" : greatr = Float.parseFloat(filter.get(i).getValue()); break;
			case "greatr2" : greatr2 = Float.parseFloat(filter.get(i).getValue()); break;
			case "type2" : type2 = (filter.get(i).getValue()); break;
			case "lessthan" : lessthan = Float.parseFloat(filter.get(i).getValue()); break;
			case "lessthan2" : lessthan2 = Float.parseFloat(filter.get(i).getValue()); break;
			}
		}
	}
    
	
	final String TEMPLAT = new StringBuilder()
            .append("SELECT ")
            //.append("  ?object <http://www.w3.org/2000/01/rdf-schema#label> ?ti .")
            //.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
            .append(" (?pl AS ?Experiment)")
            .append(" (?treatmenttype AS ?Treatment)")
            
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
            .append(" (?genus AS ?Genus) (?species AS ?Species) (?line AS ?Line)")
            .append(" (?control AS ?Control) (?plantid AS ?PlantId)")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
            
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
            
            
            
            
            
            //.append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?pl .")
            
            .append(" ")
            //.append(" ?m ?pred ?value .")
            //.append(" ?object ?p ?v .")
          
            .append("")
            .append(" WHERE { ")
            
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasExperiment> ?o .}")
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasBarcode> ?pl . FILTER(STRSTARTS(?pl, \"" + barcode + "\"))}")
            .append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?object .}")
            //.append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot2 . ?pot2 <http://purl.org/podd/ns/poddScience#hasMaterial> ?object2 .}")
            .append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?plantid .}")
   //.append(" { ?e <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            //.append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
          //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
            //.append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
            
            //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
            .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
            .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
            .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
            
            .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}}")
            
            
            //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . }} ")
			.append("LIMIT " + limit)
			
			
			.toString();
	 Representation res = this.doSPARQL3(TEMPLAT, null);
	 return res;
	 
}

public Representation filterExperiments2(List<Filter> filter) throws Exception {
	
	String barcode = "";
	String genus = "";
	String species = "";
	String type = "";
	String type2 = "";
	String unit = "";
	String unit2 = "";
	String treatment = "";
	float greatr = 0;
	float greatr2 = 0;
	float lessthan = 0;
	float lessthan2 = 0;
	if (!filter.isEmpty()) {
		for (int i = 0; i < filter.size(); ++i) {
			
			switch(filter.get(i).getField()) {
			case "barcode": barcode = filter.get(i).getValue(); break;
			case "genus": genus = filter.get(i).getValue(); break;
			case "species" : species = filter.get(i).getValue(); break;
			case "type" : type = filter.get(i).getValue(); break;
			case "unit" : unit = filter.get(i).getValue(); break;
			case "unit2" : unit2 = filter.get(i).getValue(); break;
			case "treatment" : treatment = filter.get(i).getValue(); break;
			case "greatr" : greatr = Float.parseFloat(filter.get(i).getValue()); break;
			case "greatr2" : greatr2 = Float.parseFloat(filter.get(i).getValue()); break;
			case "type2" : type2 = (filter.get(i).getValue()); break;
			case "lessthan" : lessthan = Float.parseFloat(filter.get(i).getValue()); break;
			case "lessthan2" : lessthan2 = Float.parseFloat(filter.get(i).getValue()); break;
			}
		}
	}
    
	
	final String TEMPLAT = new StringBuilder()
            .append("SELECT ")
            //.append("  ?object <http://www.w3.org/2000/01/rdf-schema#label> ?ti .")
            //.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
            .append(" (?pl AS ?Experiment)")
            
            
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
            .append(" (?genus AS ?Genus) (?species AS ?Species) (?line AS ?Line)")
            
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
            
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
            
            
            
            
            
            //.append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?pl .")
            
            .append(" ")
            //.append(" ?m ?pred ?value .")
            //.append(" ?object ?p ?v .")
          
            .append("")
            .append(" WHERE { ")
            
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasExperiment> ?o .}")
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasBarcode> ?pl . FILTER(STRSTARTS(?pl, \"" + barcode + "\"))}")
            
            //.append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot2 . ?pot2 <http://purl.org/podd/ns/poddScience#hasMaterial> ?object2 .}")
            
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}}")
          //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
           
            
            
            //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . }} ")
			.append("LIMIT " + limit)
			
			
			.toString();
	 Representation res = this.doSPARQL3(TEMPLAT, null);
	 return res;
	 
}

public Representation filterGenotypes2(List<Filter> filter) throws Exception {
	
	String barcode = "";
	String genus = "";
	String species = "";
	String type = "";
	String type2 = "";
	String unit = "";
	String unit2 = "";
	String treatment = "";
	float greatr = 0;
	float greatr2 = 0;
	float lessthan = 0;
	float lessthan2 = 0;
	if (!filter.isEmpty()) {
		for (int i = 0; i < filter.size(); ++i) {
			
			switch(filter.get(i).getField()) {
			case "barcode": barcode = filter.get(i).getValue(); break;
			case "genus": genus = filter.get(i).getValue(); break;
			case "species" : species = filter.get(i).getValue(); break;
			case "type" : type = filter.get(i).getValue(); break;
			case "unit" : unit = filter.get(i).getValue(); break;
			case "unit2" : unit2 = filter.get(i).getValue(); break;
			case "treatment" : treatment = filter.get(i).getValue(); break;
			case "greatr" : greatr = Float.parseFloat(filter.get(i).getValue()); break;
			case "greatr2" : greatr2 = Float.parseFloat(filter.get(i).getValue()); break;
			case "type2" : type2 = (filter.get(i).getValue()); break;
			case "lessthan" : lessthan = Float.parseFloat(filter.get(i).getValue()); break;
			case "lessthan2" : lessthan2 = Float.parseFloat(filter.get(i).getValue()); break;
			}
		}
	}
    
	
	final String TEMPLAT = new StringBuilder()
            .append("SELECT ")
            //.append("  ?object <http://www.w3.org/2000/01/rdf-schema#label> ?ti .")
            //.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
            .append(" (?pl AS ?Experiment)")
            
            
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
            .append(" (?genus AS ?Genus) (?species AS ?Species) (?line AS ?Line)")
            
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
            
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
            
            
            
            
            
            //.append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?pl .")
            
            .append(" ")
            //.append(" ?m ?pred ?value .")
            //.append(" ?object ?p ?v .")
          
            .append("")
            .append(" WHERE { ")
            
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasExperiment> ?o .}")
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasBarcode> ?pl . FILTER(STRSTARTS(?pl, \"" + barcode + "\"))}")
            
            //.append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot2 . ?pot2 <http://purl.org/podd/ns/poddScience#hasMaterial> ?object2 .}")
            
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}}")
          //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
           
            
            
            //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . }} ")
			.append("LIMIT " + limit)
			
			
			.toString();
	 Representation res = this.doSPARQL3(TEMPLAT, null);
	 return res;
	 
}

public Representation filterMeasurements5(List<Filter> filter) throws Exception {
	
	String barcode = "";
	String genus = "";
	String species = "";
	String type = "";
	String type2 = "";
	String unit = "";
	String unit2 = "";
	String treatment = "";
	float greatr = 0;
	float greatr2 = 0;
	float lessthan = 0;
	float lessthan2 = 0;
	if (!filter.isEmpty()) {
		for (int i = 0; i < filter.size(); ++i) {
			
			switch(filter.get(i).getField()) {
			case "barcode": barcode = filter.get(i).getValue(); break;
			case "genus": genus = filter.get(i).getValue(); break;
			case "species" : species = filter.get(i).getValue(); break;
			case "type" : type = filter.get(i).getValue(); break;
			case "unit" : unit = filter.get(i).getValue(); break;
			case "unit2" : unit2 = filter.get(i).getValue(); break;
			case "treatment" : treatment = filter.get(i).getValue(); break;
			case "greatr" : greatr = Float.parseFloat(filter.get(i).getValue()); break;
			case "greatr2" : greatr2 = Float.parseFloat(filter.get(i).getValue()); break;
			case "type2" : type2 = (filter.get(i).getValue()); break;
			case "lessthan" : lessthan = Float.parseFloat(filter.get(i).getValue()); break;
			case "lessthan2" : lessthan2 = Float.parseFloat(filter.get(i).getValue()); break;
			}
		}
	}
    
	
	final String TEMPLAT = new StringBuilder()
            .append("SELECT ")
            //.append("  ?object <http://www.w3.org/2000/01/rdf-schema#label> ?ti .")
            //.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
            .append(" (?pl AS ?Experiment)")
            .append(" (?treatmenttype AS ?Treatment)")
            
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
            .append(" (?genus AS ?Genus) (?species AS ?Species) (?line AS ?Line)")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
            
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
            
            
            
            
            
            //.append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?pl .")
            .append(" (?la AS ?MeasurementName)")
            .append(" (?val AS ?Value)")
           
            .append(" (?t AS ?Timestamp)")
            .append(" (?u AS ?Unit)")
            .append(" (?t2 AS ?Timestamp2)")
            .append(" (?u2 AS ?Unit2)")
            .append(" (?la2 AS ?MeasurementName2)")
            .append(" (?val2 AS ?Value2)")
            .append(" ")
            //.append(" ?m ?pred ?value .")
            //.append(" ?object ?p ?v .")
          
            .append("")
            .append(" WHERE { ")
            
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasExperiment> ?o .}")
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasBarcode> ?pl . FILTER(STRSTARTS(?pl, \"" + barcode + "\"))}")
            .append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?object .}")
            //.append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot2 . ?pot2 <http://purl.org/podd/ns/poddScience#hasMaterial> ?object2 .}")
            //.append(" { ?o <http://www.w3.org/2000/01/rdf-schema#label> ?ti .}")
   //.append(" { ?e <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            //.append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
          //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
            //.append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
            
            //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
            .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
            .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
            .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
            
            //.append(" {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
            
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
            
            .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\"))}")
            
            //.append(" {?m ?pred ?value .}}}")
            .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
            
            
            //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(((xsd:float(?val)) > (xsd:float(?val2)) )) }")
            .append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(((xsd:float(?val) > " + greatr + "))) }}")
            //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . }} ")
			.append("LIMIT " + limit)
			
			
			.toString();
	 Representation res = this.doSPARQL3(TEMPLAT, null);
	 return res;
	 
}
public Representation filterMeasurements3(List<Filter> filter) throws Exception {
	
	String barcode = "";
	String genus = "";
	String species = "";
	String type = "";
	String type2 = "";
	String unit = "";
	String unit2 = "";
	String treatment = "";
	float greatr = 0;
	float greatr2 = 0;
	float lessthan = 0;
	float lessthan2 = 0;
	if (!filter.isEmpty()) {
		for (int i = 0; i < filter.size(); ++i) {
			
			switch(filter.get(i).getField()) {
			case "barcode": barcode = filter.get(i).getValue(); break;
			case "genus": genus = filter.get(i).getValue(); break;
			case "species" : species = filter.get(i).getValue(); break;
			case "type" : type = filter.get(i).getValue(); break;
			case "unit" : unit = filter.get(i).getValue(); break;
			case "unit2" : unit2 = filter.get(i).getValue(); break;
			case "treatment" : treatment = filter.get(i).getValue(); break;
			case "greatr" : greatr = Float.parseFloat(filter.get(i).getValue()); break;
			case "greatr2" : greatr2 = Float.parseFloat(filter.get(i).getValue()); break;
			case "type2" : type2 = (filter.get(i).getValue()); break;
			case "lessthan" : lessthan = Float.parseFloat(filter.get(i).getValue()); break;
			case "lessthan2" : lessthan2 = Float.parseFloat(filter.get(i).getValue()); break;
			}
		}
	}
    
	
	final String TEMPLAT = new StringBuilder()
            
            .append("CONSTRUCT { ")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?pl .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
            
            
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m .")
            .append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la .")
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasTimestamp> ?t .")
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .")
            
            
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .")
            
            .append("}")
            //.append(" ?m ?pred ?value .")
            //.append(" ?object ?p ?v .")
          
            .append("")
            .append(" WHERE { ")
            
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasExperiment> ?o .}")
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasBarcode> ?pl . FILTER(STRSTARTS(?pl, \"" + barcode + "\"))}")
            .append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?object .}")
            //.append(" { ?o <http://www.w3.org/2000/01/rdf-schema#label> ?ti .}")
            .append(" { ?e <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
            //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
            //.append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
            
            //.append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
            //.append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            //.append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
            .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
            .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
            
            //.append(" {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
            
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
            //.append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m2 . ?m2 <http://www.w3.org/2000/01/rdf-schema#label> ?la2 . FILTER(STRSTARTS(?la2, \"" + type2 + "\"))}")
            .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\"))}")
            //.append(" {?m2 <http://purl.org/podd/ns/poddScience#hasUnit> ?u2 . FILTER(STRSTARTS(?u2, \"" + unit2 + "\")) }")
            //.append(" {?m ?pred ?value .}}}")
            .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
            //.append(" {?m2 <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t2 .}")
            .append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(((xsd:float(?val) > " + greatr + "))) }}")
            //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . }} ")
			.append("LIMIT 2000")
			
			
			.toString();
	
	final String TEMPLA = new StringBuilder()
            .append(" SELECT {}")
            
            /*
            .append("  ?object <http://www.w3.org/2000/01/rdf-schema#label> ?ti .")
            .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
            
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
            
            
            
            
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m .")
            .append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la .")
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasTimestamp> ?t .")
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .")
            
            
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .")
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val2 .") */
            .append("")
            .append(" WHERE { ")
            .append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?object .}")
            //.append("{  ?o <http://www.w3.org/2000/01/rdf-schema#label> ?ti .}")
            //.append(" { ?e <http://purl.org/podd/ns/poddScience#hasGenotypes> ?genotypes . ?genotypes <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype. ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            //.append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
            
            //.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .}} ")
			.append( "} LIMIT 500000")
			
			
			.toString();
	String n = "SELECT DISTINCT ?s ?property ?o WHERE { ?s ?property ?o . }";
	final String TEMPLAT2 = new StringBuilder()
            .append("CONSTRUCT { ")
            .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
            
            
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m .")
            .append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la .")
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasTimestamp> ?t .")
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .")
            
            
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .")
            
            .append("}")
            .append(" WHERE { ")
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
            .append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
            
            //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
            .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
            .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
            .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
            
            //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
            
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
            .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\"))}")
            .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
			.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) > " + greatr + ")}} LIMIT 50000")
			.toString();
	
	final String TEMPLAT3 = new StringBuilder()
            .append("CONSTRUCT { ")
            .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
            
            
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m .")
            .append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la .")
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasTimestamp> ?t .")
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .")
            
            
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .")
            
            .append("}")
            .append(" WHERE { ")
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
            .append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
            
            //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
            .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
            .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
            .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
            
            //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
            
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
            .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\"))}")
            .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
			.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) > " + greatr + ")}")
			.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) < " + lessthan + ")}} LIMIT 50000").toString();
	
	final String TEMPLAT4 = new StringBuilder()
            .append("CONSTRUCT { ")
            .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasGenotype> ?genotype .")
            //.append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
            .append(" ?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
            
            
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date .")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m .")
            .append(" ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la .")
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasTimestamp> ?t .")
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .")
            
            
            .append(" ?m <http://purl.org/podd/ns/poddScience#hasValue> ?val .")
            
            .append("}")
            .append(" WHERE { ")
            .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
            .append(" { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
            
            //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .}")
            .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
            .append(" { ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")
            .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
            .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
            
            //.append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
            
            .append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?m . ?m <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + type + "\"))}")
            .append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u . FILTER(STRSTARTS(?u, \"" + unit + "\"))}")
            .append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .}")
			.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) > " + greatr + ")}")
			.append("{?m <http://purl.org/podd/ns/poddScience#hasValue> ?val . FILTER(xsd:float(?val) < " + lessthan + ")}} LIMIT 50000").toString(); 
			
            //.append(" {?m <http://purl.org/podd/ns/poddScience#hasUnit> ?u .}}")
            //.append(" {?m <http://purl.org/podd/ns/poddBase#hasTimestamp> ?t .} }")
	Representation res;
	/*if (treatment.length() > 0 && lessthan > 0) {
    	res = this.doSPARQL2(TEMPLAT3, null);
    } else if (treatment.length() > 0){
    	res = this.doSPARQL2(TEMPLAT2, null);
    } else if (lessthan > 0) {
    	res = this.doSPARQL2(TEMPLAT4, null);
    } else { */
    res = this.doSPARQL2(TEMPLAT, null);
    //}
                  
	
    
    return res;
}
	public Representation listAllPlants() throws Exception {
		
		
		
        final String TEMPLATE = new StringBuilder()
        		.append("CONSTRUCT { ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . ")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?tm .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date . ")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date . ")
        		.append(" ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype .")
        		.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . }")
                .append(" WHERE {")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . }")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm}")
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
		String barcode = "";
		if (!filter.isEmpty()) {
			for (int i = 0; i < filter.size(); ++i) {
				switch(filter.get(i).getField()) {
				case "treatment": treatment = filter.get(i).getValue(); break;
				case "genus": genus = filter.get(i).getValue(); break;
				case "species" : species = filter.get(i).getValue(); break;
				case "barcode" : barcode = filter.get(i).getValue(); break;
				}
			}	
		}
		
        
        final String TEMPLAT = new StringBuilder()
        		.append("CONSTRUCT { ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?pl . ")
        		.append(" ?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .")
        		.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . ")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date .}")
                
                .append(" WHERE {")
                .append(" { ?e <http://purl.org/podd/ns/poddScience#hasExperiment> ?o .}")
                .append(" { ?e <http://purl.org/podd/ns/poddScience#hasBarcode> ?pl . FILTER(STRSTARTS(?pl, \"" + barcode + "\"))}")
                .append(" { ?o <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ?pot <http://purl.org/podd/ns/poddScience#hasMaterial> ?object .}")
                .append("{?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype .}")
                .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")                
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
                
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm}")
                
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date . }} LIMIT 1000")               
                .toString();
        
        final String TEMPLAT2 = new StringBuilder()
        		.append("CONSTRUCT { ")
        		.append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . ")
        		.append(" ?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .")
        		.append(" ?object <http://purl.org/podd/ns/poddScience#hasGenus> ?genus .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLine> ?line .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . ")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype .")
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm .")
                
                .append(" ?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date .}")
                
                .append(" } WHERE {")
                .append(" {?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                 .append("{ ?object <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype .}")
                .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasGenus> ?genus . FILTER(STRSTARTS(?genus, \"" + genus + "\"))}")
                .append(" { ?genotype <http://purl.org/podd/ns/poddScience#hasSpecies> ?species . FILTER(STRSTARTS(?species, \"" + species + "\"))}")                
                .append(" {?genotype <http://purl.org/podd/ns/poddScience#hasLine> ?line .}")
                .append(" OPTIONAL {?genotype <http://purl.org/podd/ns/poddScience#hasLineNumber> ?linenum .}")               
               
                .append(" {?object <http://purl.org/podd/ns/poddScience#hasControl> ?control .}")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentType> ?treatmenttype . FILTER(STRSTARTS(?treatmenttype, \"" + treatment + "\"))}")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasTreatment> ?treatment . ?treatment <http://purl.org/podd/ns/poddScience#hasTreatmentMaterial> ?tm . }")
                //.append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?mes . ?mes  <http://www.w3.org/2000/01/rdf-schema#label> ?la . FILTER(STRSTARTS(?la, \"" + "weight before" + "\"))}")
                
                //.append(" {?object <http://purl.org/podd/ns/poddScience#hasMeasurement> ?mes . }")
                .append(" OPTIONAL {?object <http://purl.org/podd/ns/poddScience#hasPlantingDate> ?date . }} LIMIT " + limit).toString();
        Representation res;
        
        res = this.doSPARQL2(TEMPLAT, null);
         
        
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