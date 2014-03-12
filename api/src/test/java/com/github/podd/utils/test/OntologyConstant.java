package com.github.podd.utils.test;

import org.openrdf.model.URI;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PODD;

public class OntologyConstant {

	  public static final InferredOWLOntologyID testA1;
	    public static final InferredOWLOntologyID testB1;
	    public static final InferredOWLOntologyID testB2;
	    public static final InferredOWLOntologyID testC1;
	    public static final InferredOWLOntologyID testC3;
	    public static final InferredOWLOntologyID testImportOntologyID1;
	    public static final InferredOWLOntologyID testImportOntologyID2;
	    public static final InferredOWLOntologyID testImportOntologyID3;
	    public static final InferredOWLOntologyID testImportOntologyID4;
	    public static final URI testImportOntologyUri1;
	    public static final URI testImportOntologyUri2;
	    public static final URI testImportOntologyUri3;
	    public static final URI testImportOntologyUri4;
	    public static final URI testImportVersionUri1;
	    public static final URI testImportVersionUri2;
	    public static final URI testImportVersionUri3;
	    public static final URI testImportVersionUri4;
	    
	    public static final InferredOWLOntologyID testOntologyID;
	    public static final URI testOntologyUri1;
	    public static final URI testInferredUri1;
	    
	    public static final URI testOntologyUriA;
	    public static final URI testOntologyUriB;
	    public static final URI testOntologyUriC;
	    
	    public static final URI testPoddBaseUri;
	    public static final URI testPoddBaseUriV1;
	    public static final URI testPoddBaseUriV2;
	    public static final InferredOWLOntologyID testPoddBaseV1;
	    public static final InferredOWLOntologyID testPoddBaseV2;
	    
	    public static final URI testPoddDcUri;
	    public static final URI testPoddDcUriV1;
	    public static final URI testPoddDcUriV2;
	    public static final InferredOWLOntologyID testPoddDcV1;
	    public static final InferredOWLOntologyID testPoddDcV2;
	    
	    public static final URI testPoddFoafUri;
	    public static final URI testPoddFoafUriV1;
	    public static final URI testPoddFoafUriV2;
	    public static final InferredOWLOntologyID testPoddFoafV1;
	    public static final InferredOWLOntologyID testPoddFoafV2;
	    
	    public static final URI testPoddPlantUri;
	    public static final URI testPoddPlantUriV1;
	    public static final URI testPoddPlantUriV2;
	    public static final InferredOWLOntologyID testPoddPlantV1;
	    public static final InferredOWLOntologyID testPoddPlantV2;
	    
	    public static final URI testPoddScienceUri;
	    public static final URI testPoddScienceUriV1;
	    public static final URI testPoddScienceUriV2;
	    public static final InferredOWLOntologyID testPoddScienceV1;
	    public static final InferredOWLOntologyID testPoddScienceV2;
	    
	    public static final URI testPoddUserUri;
	    public static final URI testPoddUserUriV1;
	    public static final URI testPoddUserUriV2;
	    public static final InferredOWLOntologyID testPoddUserV1;
	    public static final InferredOWLOntologyID testPoddUserV2;
	    
	    public static final URI testVersionUri1;
	    public static final URI testVersionUriA1;
	    public static final URI testVersionUriB1;
	    public static final URI testVersionUriB2;
	    public static final URI testVersionUriC1;
	    public static final URI testVersionUriC3;
		public static final URI testMisteaEventUri;
		public static final URI testMisteaEventUriV2;
		public static final InferredOWLOntologyID testMisteaEventV2;
		public static final URI testMisteaObjectUri;
		public static final URI testMisteaObjectUriV2;
		public static final InferredOWLOntologyID testMisteaObjectV2;
  
		
		
	    public static final InferredOWLOntologyID owlid(final IRI ontologyUri, final IRI versionUri, final IRI inferredUri)
	    {
	        return new InferredOWLOntologyID(ontologyUri, versionUri, inferredUri);
	    }
	    
	    public static final InferredOWLOntologyID owlid(final URI ontologyUri, final URI versionUri)
	    {
	        return owlid(ontologyUri, versionUri, null);
	    }
	    
	    public static final InferredOWLOntologyID owlid(final URI ontologyUri, final URI versionUri, final URI inferredUri)
	    {
	        return new InferredOWLOntologyID(ontologyUri, versionUri, inferredUri);
	    }	
		
	    public static final URI uri(final String uri)
	    {
	        return PODD.VF.createURI(uri);
	    }
	    
static {
	
	 testPoddDcUri = uri("http://purl.org/podd/ns/dcTerms");
   testPoddDcUriV1 = uri("http://purl.org/podd/ns/version/dcTerms/1");
   testPoddDcUriV2 = uri("http://purl.org/podd/ns/version/dcTerms/2");
   testPoddDcV1 = owlid(testPoddDcUri, testPoddDcUriV1);
   testPoddDcV2 = owlid(testPoddDcUri, testPoddDcUriV2);
   
   testPoddFoafUri = uri("http://purl.org/podd/ns/foaf");
   testPoddFoafUriV1 = uri("http://purl.org/podd/ns/version/foaf/1");
   testPoddFoafUriV2 = uri("http://purl.org/podd/ns/version/foaf/2");
   testPoddFoafV1 = owlid(testPoddFoafUri, testPoddFoafUriV1);
   testPoddFoafV2 = owlid(testPoddFoafUri, testPoddFoafUriV2);
   
   testPoddUserUri = uri("http://purl.org/podd/ns/poddUser");
   testPoddUserUriV1 = uri("http://purl.org/podd/ns/version/poddUser/1");
   testPoddUserUriV2 = uri("http://purl.org/podd/ns/version/poddUser/2");
   testPoddUserV1 = owlid(testPoddUserUri, testPoddUserUriV1);
   testPoddUserV2 = owlid(testPoddUserUri, testPoddUserUriV2);
   
   testPoddBaseUri = uri("http://purl.org/podd/ns/poddBase");
   testPoddBaseUriV1 = uri("http://purl.org/podd/ns/version/poddBase/1");
   testPoddBaseUriV2 = uri("http://purl.org/podd/ns/version/poddBase/2");
   testPoddBaseV1 = owlid(testPoddBaseUri, testPoddBaseUriV1);
   testPoddBaseV2 = owlid(testPoddBaseUri, testPoddBaseUriV2);
   
   testPoddScienceUri = uri("http://purl.org/podd/ns/poddScience");
   testPoddScienceUriV1 = uri("http://purl.org/podd/ns/version/poddScience/1");
   testPoddScienceUriV2 = uri("http://purl.org/podd/ns/version/poddScience/2");
   testPoddScienceV1 = owlid(testPoddScienceUri, testPoddScienceUriV1);
   testPoddScienceV2 = owlid(testPoddScienceUri, testPoddScienceUriV2);
   
   testPoddPlantUri = uri("http://purl.org/podd/ns/poddPlant");
   testPoddPlantUriV1 = uri("http://purl.org/podd/ns/version/poddPlant/1");
   testPoddPlantUriV2 = uri("http://purl.org/podd/ns/version/poddPlant/2");
   testPoddPlantV1 = owlid(testPoddPlantUri, testPoddPlantUriV1);
   testPoddPlantV2 = owlid(testPoddPlantUri, testPoddPlantUriV2);
   
   testMisteaEventUri = uri("http://www.mistea.supagro.inra.fr/event");
   testMisteaEventUriV2 = uri("http://www.mistea.supagro.inra.fr/event/2");
   testMisteaEventV2 = owlid(testMisteaEventUri, testMisteaEventUriV2);
   
   testMisteaObjectUri = uri("http://www.mistea.supagro.inra.fr/object");
   testMisteaObjectUriV2 = uri("http://www.mistea.supagro.inra.fr/object/2");
   testMisteaObjectV2 = owlid(testMisteaObjectUri, testMisteaObjectUriV2);
  
   
   testOntologyUri1 = uri("urn:test:ontology:uri:1");
   testVersionUri1 = uri("urn:test:ontology:uri:1:version:1");
   testInferredUri1 = uri("urn:inferred:test:ontology:uri:1:version:1");
   testOntologyID = owlid(testOntologyUri1, testVersionUri1, testInferredUri1);
   
   testImportOntologyUri1 = uri("urn:test:import:ontology:uri:1");
   testImportVersionUri1 = uri("urn:test:import:ontology:uri:1:version:1");
   testImportOntologyID1 = owlid(testImportOntologyUri1, testImportVersionUri1);
   
   testImportOntologyUri2 = uri("urn:test:import:ontology:uri:2");
   testImportVersionUri2 = uri("urn:test:import:ontology:uri:2:version:1");
   testImportOntologyID2 = owlid(testImportOntologyUri2, testImportVersionUri2);
   
   testImportOntologyUri3 = uri("urn:test:import:ontology:uri:3");
   testImportVersionUri3 = uri("urn:test:import:ontology:uri:3:version:1");
   testImportOntologyID3 = owlid(testImportOntologyUri3, testImportVersionUri3);
   
   testImportOntologyUri4 = uri("urn:test:import:ontology:uri:4");
   testImportVersionUri4 = uri("urn:test:import:ontology:uri:4:version:1");
   testImportOntologyID4 = owlid(testImportOntologyUri4, testImportVersionUri4);
   
   testOntologyUriA = uri("http://example.org/podd/ns/poddA");
   testVersionUriA1 = uri("http://example.org/podd/ns/version/poddA/1");
   testA1 = owlid(testOntologyUriA, testVersionUriA1);
   
   testOntologyUriB = uri("http://example.org/podd/ns/poddB");
   testVersionUriB1 = uri("http://example.org/podd/ns/version/poddB/1");
   testB1 = owlid(testOntologyUriB, testVersionUriB1);
   testVersionUriB2 = uri("http://example.org/podd/ns/version/poddB/2");
   testB2 = owlid(testOntologyUriB, testVersionUriB2);
   
   testOntologyUriC = uri("http://example.org/podd/ns/poddC");
   testVersionUriC1 = uri("http://example.org/podd/ns/version/poddC/1");
   testC1 = owlid(testOntologyUriC, testVersionUriC1);
   testVersionUriC3 = uri("http://example.org/podd/ns/version/poddC/3");
   testC3 = owlid(testOntologyUriC, testVersionUriC3);
}
	
}
