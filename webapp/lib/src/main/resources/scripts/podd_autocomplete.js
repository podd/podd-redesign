/**
 * PODD : Uses jquery-RDF to analyse the current document with respect to the
 * RDFa information it contains and populate the rdfadebug element with the
 * resulting information
 */

if (typeof oas === 'undefined') {
	oas = {};
}

if (typeof oas['autocomplete'] == 'undefined') {
	oas.autocomplete = {};
}

$.widget("custom.catcomplete", $.ui.autocomplete, {
	_renderMenu : function(ul, items) {
		var self = this, currentCategory = "";
		$.each(items, function(index, item) {
			if (item.category != currentCategory) {
				ul.append("<li class='ui-autocomplete-category'>"
						+ item.category + "</li>");
				currentCategory = item.category;
			}
			self._renderItem(ul, item);
		});
	}
});

oas.autocomplete.constructAutocomplete = function() {
	// used to prevent race conditions between autocomplete requests
	var requestIndex = 0;

	// TODO: Load this list dynamically
	var ontologies = [ {
		value : "po",
		label : "Plant Ontology",
		desc : "Ontology about plants",
		icon : "plant_ontology.gif"
	}, {
		value : "go",
		label : "Gene Ontology",
		desc : "Ontology about genes",
		icon : "GO-head.gif"
	}, {
		value : "obi",
		label : "Ontology for Biomedical Investigations",
		desc : "Ontology used to describe biomedical investigations",
		icon : "obi-blue-236x65.png"
	} ];

	// TODO: remove hardcoded element ID's here
	$("#ontology").autocomplete({
		serviceUrl : oas.baseUrl + 'search/ontologylabels/?searchterm=',
		minChars : 3,
		width : 300,
		delimiter : /(,|;)\s*/,
		deferRequestBy : 500, // miliseconds
		focus : function(event, ui) {
			$("#ontology").val(ui.item.label);
			// $( "#ontology-icon" ).attr( "src",
			// oas.baseUrl+"resources/static/images/" + ui.item.icon );
			return false;
		},
		source : function(request, response) {
			// console.debug("in source function");
			// console.debug(this.options.serviceUrl);
			// console.debug(request);
			if (self.xhr) {
				self.xhr.abort();
			}
			self.xhr = $.ajax({
				url : this.options.serviceUrl + request.term,
				// data: request,
				dataType : "json",
				autocompleteRequest : ++requestIndex,
				success : function(data, status) {
					// console.debug("found success");
					// console.debug(data);
					// console.debug(status);
					if (this.autocompleteRequest === requestIndex) {
						// console.debug("response function");
						// console.debug(response);
						// TODO: make sure that the data here is filtered as
						// specified in
						// http://api.jqueryui.com/autocomplete/#option-source
						// Understanding the response function may also be
						// useful in understanding why nothing is happening here
						// http://api.jqueryui.com/autocomplete/#event-response
						response(oas.rdf.parsesearchresults(this.url, data));
					}
					// else
					// {
					// console.debug("ignored success callback because
					// autocompleteRequest=("+this.autocompleteRequest+") did
					// not equal requestIndex=("+requestIndex+")");
					// }
				},
				error : function(xhr, ajaxOptions, thrownError) {
					// console.debug("found error");
					// console.debug(xhr);
					// console.debug(ajaxOptions);
					// console.debug(thrownError);
					// console.debug(xhr.getResponseHeader());
					if (this.autocompleteRequest === requestIndex) {
						response([]);
					}
				}
			});
		},
		select : function(event, ui) {
			$("#ontology").val(ui.item.label);
			$("#ontology-id").val(ui.item.value.toString());
			// $( "#ontology-description" ).html( ui.item.desc );
			// $( "#ontology-icon" ).attr( "src", "images/" + ui.item.icon );
			return false;
		}
	});

	$("#ontology").data("autocomplete")._renderItem = function(ul, item) {
		return $("<li></li>").data("item.autocomplete", item).append(
				"<a>" + item.label + "<br />" + item.desc + "</a>")
				.appendTo(ul);
	};

	$('#ontologytermlabel').autocomplete({
		serviceUrl : oas.baseUrl + 'search/ontologies/?searchterm=',
		minChars : 3,
		width : 300,
		delimiter : /(,|;)\s*/,
		deferRequestBy : 500, // miliseconds
		// search: function( event, ui ){
		// var originalUrl = this.url;
		// console.debug("search event triggered");
		// / console.debug(event);
		// console.debug(ui);
		// console.debug(event.target.value);
		// url = serviceUrl+event.target.value;
		// },
		source : function(request, response) {
			// console.debug("in source function");
			// console.debug(this.options.serviceUrl);
			// console.debug(request);
			if (self.xhr) {
				self.xhr.abort();
			}

			// console.debug("current ontology id");
			// console.debug($( "#ontology-id" ));
			// console.debug($( "#ontology-id" ).val());

			var ontologyUri = $.trim($("#ontology-id").val());

			var requestUrl = this.options.serviceUrl + request.term;

			if (ontologyUri.length > 0) {
				requestUrl = requestUrl + "&ontologyUri=" + ontologyUri;
			}

			self.xhr = $.ajax({
				url : requestUrl,
				// data: request,
				dataType : "json",
				autocompleteRequest : ++requestIndex,
				success : function(data, status) {
					// console.debug("found success");
					// console.debug(data);
					// console.debug(status);
					if (this.autocompleteRequest === requestIndex) {
						// console.debug("response function");
						// console.debug(response);
						// NOTE: make sure that the data here is filtered as
						// specified in
						// http://api.jqueryui.com/autocomplete/#option-source
						// Understanding the response function may also be
						// useful in understanding why nothing is happening here
						// http://api.jqueryui.com/autocomplete/#event-response
						response(oas.rdf.parsesearchresults(this.url, data));
					}
					// else
					// {
					// console.debug("ignored success callback because
					// autocompleteRequest=("+this.autocompleteRequest+") did
					// not equal requestIndex=("+requestIndex+")");
					// }
				},
				error : function(xhr, ajaxOptions, thrownError) {
					// console.debug("found error");
					// console.debug(xhr);
					// console.debug(ajaxOptions);
					// console.debug(thrownError);
					// console.debug(xhr.getResponseHeader());
					if (this.autocompleteRequest === requestIndex) {
						response([]);
					}
				}
			});
		},
		select : function(event, ui) {
			console.debug("Option selected");
			console.debug(event);
			console.debug(ui);
			$("#ontologytermlabel").val(ui.item.label);
			$("#ontologytermuri").val(ui.item.value.toString());
			return false;
		}
	});

};

// --------------------------------

/* Manually created fragment for submission into edit artifact service */
var nextDatabank = $.rdf.databank();
//		.base('http://purl.org/podd/basic-2-20130206/artifact:1')
//		.prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#')
//		.prefix('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')
//		.prefix('owl', 'http://www.w3.org/2002/07/owl#')
//		.prefix('poddScience', 'http://purl.org/podd/ns/poddScience#')
//		.prefix('poddBase', 'http://purl.org/podd/ns/poddBase#')
//		.add('<genotype33> rdf:type poddScience:Genotype .')
//		.add('<genotype33> rdf:type owl:NamedIndividual .')
//		.add('<genotype33> rdfs:label "Genotype 33" .')
//		.add(
//				'<http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Material> poddScience:hasGenotype <genotype33> .')



function removeTriple() {
	
	var myDatabank = $.rdf.databank()
	.base('http://purl.org/podd/basic-2-20130206/artifact:1/')
	.prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#')
	.prefix('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')
	.prefix('owl', 'http://www.w3.org/2002/07/owl#')
	.prefix('poddScience', 'http://purl.org/podd/ns/poddScience#')
	.prefix('poddBase', 'http://purl.org/podd/ns/poddBase#')
	.add('<myTopObject> poddBase:hasGenotype <genotype33> .')
	.add('<genotype33> rdf:type poddScience:Genotype .')
	.add('<genotype33> rdf:type owl:NamedIndividual .')
	.add('<genotype33> rdfs:label "Genotype 33" .')
	.add(
			'<http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Material> poddScience:hasGenotype <genotype33> .')
	
	debugPrintDatabank(myDatabank, '[DEBUG] initial');
	
	myDatabank.remove('<genotype33> rdfs:label "Genotype 33"');
	debugPrintDatabank(myDatabank, '[DEBUG] removed');
	
	myDatabank.add('<genotype33> rdfs:comment "This is a simple comment" ');
	debugPrintDatabank(myDatabank, '[DEBUG] added');
	
	console.debug('-------------');
	$.rdf({
		databank : myDatabank
	})
	  .where('<http://purl.org/podd/basic-2-20130206/artifact:1/genotype33> rdfs:comment ?object')
	  .sources()
	  .each(function () {
	    console.debug('The triple = ' + this[0]); 
	    
	    try{
	    	myDatabank.remove(this[0]);
	    }catch (err) {
	    	console.debug(err);
	    }
	  });
	console.debug('-------------');
	
	debugPrintDatabank(myDatabank, '[DEBUG] removed');
	
}


function debugPrintDatabank(databank, message) {
	var triples = $.toJSON(
			databank.dump({format : 'application/json'})
			);
	console.debug(message + ': (' + databank.size() + ') ' + triples);
}


var theMessageBox = '#message1';

/* Display a Message */
function displayMessage(event) {
	var messageBox = event.data.param1;
	var message = event.data.param2;
	console.debug(message);
	console.debug(messageBox);
	$(messageBox).html('<i>' + message + '</i>');
}

/* Display a message on leaving text field */
function doBlur() {
	$(theMessageBox).html(
			'The value of field "' + $(this).attr('id') + '" was set to: "<b>'
					+ $(this).val() + '</b>".');
}

/* Retrieve the current version of an artifact and populate the databank */
function getArtifact(artifactUri) {
	var requestUrl = podd.baseUrl + '/artifact/base?artifacturi=' + encodeURIComponent(artifactUri);

	console.debug('[getArtifact] Request to: ' + requestUrl);
	$.ajax({
		url : requestUrl,
		type : 'GET',
		//dataType : 'application/rdf+xml', // what is expected back
		success : function(resultData, status, xhr) {
			nextDatabank = $.rdf.databank();
			nextDatabank = nextDatabank.load(resultData);
			console.debug('[getArtifact] ### SUCCESS ### loaded databank with size ' + nextDatabank.size());

			// update version IRI on page
			var artifactId = getOntologyID(nextDatabank);
			console.debug('[getArtifact] Version IRI : ' + artifactId[0].versionIri);
			$('#podd_artifact_version').val(artifactId[0].versionIri);
			
			// update project title on page
			var titles = getProjectTitle(nextDatabank);
			$('#in1').val(titles[0].title);
			$('#in1Hidden').val(titles[0].title);
			
			$(theMessageBox).html(
					'<i>Successfully retrieved artifact version: ' + artifactId[0].versionIri + '</i><br>');
		},
		error : function(xhr, status, error) {
			console.debug(status + '[getArtifact] $$$ ERROR $$$ ' + error);
			//console.debug(xhr.statusText);
		}
	});
	
}

/* Invoke the Edit PODD Artifact Service with the modified RDF triples */
function updateArtifact(isNew, property, newValue, oldValue) {
	console.debug('[updateArtifact] start');
	
	requestUrl = podd.baseUrl + '/artifact/edit';

	var artifactUri = $('#podd_artifact').val();
	var versionUri = $('#podd_artifact_version').val();
	console.debug('[updateArtifact] artifact details: "' + artifactUri + '" version: "'	+ versionUri + '" .');
	
	debugPrintDatabank(nextDatabank, '[updateArtifact] Databank at start:');

	// - find the topObject URI
	var topObject = ''; //'<http://purl.org/podd/basic-1-20130206/object:2966>';
	
	var myQuery = $.rdf({
		databank : nextDatabank
	})
	.where('?artifact poddBase:artifactHasTopObject ?topObject')
	var bindings = myQuery.select();

	$.each(bindings, function(index, value) {
		topObject = value.topObject.value;
	});

	
	if (!isNew) {
		//deleteProjectTitle4(nextDatabank);
	    deleteTriples(nextDatabank, '<' + topObject + '>', 'rdfs:label');
		debugPrintDatabank(nextDatabank,
				'[updateArtifact] Databank after removing: ' + nextDatabank.size());
	}
	
	triple = '<' + topObject + '>' + ' ' + property + ' ' + newValue;
	nextDatabank.add(triple);
	
	debugPrintDatabank(nextDatabank, '[updateArtifact] Databank after adding new triple:');
	
	var modifiedTriples = $.toJSON(nextDatabank.dump({
		format : 'application/json'
	}));

	// set query parameters in the URI as setting them under data failed, mostly
	// leading to a 415 error
	requestUrl = requestUrl + '?artifacturi=' + encodeURIComponent(artifactUri)
			+ '&versionuri=' + encodeURIComponent(versionUri) + '&isforce=true';
	console.debug('[updateArtifact] Request (POST):  ' + requestUrl);

	$.ajax({
		url : requestUrl,
		type : 'POST',
		data : modifiedTriples,
		contentType : 'application/rdf+json', // what we're sending
		dataType : 'json', // what is expected back
		success : function(resultData, status, xhr) {
			console.debug('[updateArtifact] ### SUCCESS ### ' + resultData);
			console.debug('[updateArtifact] ' + xhr.responseText);
			$(theMessageBox).html(
					'<i>Successfully edited artifact.</i><br><p>'
							+ xhr.responseText + '</p><br>');
			
			getArtifact(artifactUri);
		},
		error : function(xhr, status, error) {
			console.debug('[updateArtifact] $$$ ERROR $$$ ' + error);
			console.debug(xhr.statusText);
		}
	});

}

/*
 * Call Search Ontology Resource Service using AJAX, convert the RDF response to
 * a JSON array and set to the array as autocomplete data.
 */
function autoCompleteCallback(/* object with 'search term' */request, /* function */
		response) {

	requestUrl = podd.baseUrl + '/search';

	var artifactUri = $('#podd_artifact').val();
	var searchTypes = $('#podd_type').attr('value');

	console.debug('Searching artifact: "' + artifactUri
			+ '" for searchTypes: "' + searchTypes + '" for search term: "'
			+ request.term + '".');

	queryParams = {
		searchterm : request.term,
		artifacturi : artifactUri,
		searchtypes : searchTypes
	};

	// console.debug('Request: ' + requestUrl);

	$.get(requestUrl, queryParams, function(data) {
		console.debug('Response: ' + data.toString());
		var formattedData = parsesearchresults(requestUrl, data);
		// console.debug(formattedData);
		response(formattedData);
	}, 'json');
}

/*
 * Parse the RDF received from the server and create a JSON array. Modified Query
 * from oas.rdf.parsesearchresults()
 */
function parsesearchresults(/* string */searchURL, /* rdf/json */data) {
	// console.debug("Parsing search results");
	var nextDatabank = $.rdf.databank();

	var rdfSearchResults = nextDatabank.load(data);

	// console.debug("About to create query");
	var myQuery = $.rdf({
		databank : nextDatabank
	}).where('?pUri <http://www.w3.org/2000/01/rdf-schema#label> ?pLabel');
	var bindings = myQuery.select();

	var nodeChildren = [];
	$.each(bindings, function(index, value) {
		var nextChild = {};
		nextChild.label = value.pLabel.value;
		nextChild.value = value.pUri.value;

		nodeChildren.push(nextChild);
	});
	// TODO: Sort based on weights for properties

	return nodeChildren;
};

/* 
 * Parse the given Databank and extract the artifact IRI and version IRI
 * of the ontology/artifact contained within. 
 */
function getOntologyID(nextDatabank) {
	//console.debug("[getVersion] start");

	var myQuery = $.rdf({
		databank : nextDatabank
	}).where('?artifactIri <http://www.w3.org/2002/07/owl#versionIRI> ?versionIri');
	var bindings = myQuery.select();

	var nodeChildren = [];
	$.each(bindings, function(index, value) {
		var nextChild = {};
		nextChild.artifactIri = value.artifactIri.value;
		nextChild.versionIri = value.versionIri.value;

		nodeChildren.push(nextChild);
		//console.debug('[getVersion] Found version: ' + nextChild.versionIri + ' and artifact ID: ' + nextChild.artifactIri);
	});

	if (nodeChildren.length > 1){
		console.debug('[getVersion] ERROR - More than 1 version IRI statement found!!!');
	}
	
	return nodeChildren;
};

/* 
 * Parse the given Databank and extract the Project title
 * of the artifact contained within. 
 */
function getProjectTitle(nextDatabank) {
	console.debug("[getProjectTitle] start");

	var myQuery = $.rdf({
		databank : nextDatabank
	})
	.prefix('poddBase', 'http://purl.org/podd/ns/poddBase#')
	.prefix('rdfs',	'http://www.w3.org/2000/01/rdf-schema#')
	.where('?artifact poddBase:artifactHasTopObject ?topObject')
	.where('?topObject rdfs:label ?projectTitle');
	var bindings = myQuery.select();

	var nodeChildren = [];
	$.each(bindings, function(index, value) {
		var nextChild = {};
		nextChild.title = value.projectTitle.value;

		nodeChildren.push(nextChild);
		console.debug('[getProjectTitle] Found title: ' + nextChild.title);
	});

	if (nodeChildren.length > 1){
		console.debug('[getProjectTitle] ERROR - More than 1 Project Title found!!!');
	}
	
	return nodeChildren;
};

/* 
 * Remove the "project title" from the databank.
 */
function deleteProjectTitle4(nextDatabank) {
	console.debug('[deleteProjectTitle4] start');
	
	var myQuery = $.rdf({
		databank : nextDatabank
	})
	.where('?artifact poddBase:artifactHasTopObject ?topObject')
	.where('?topObject rdfs:label ?projectTitle');
	var bindings = myQuery.select();

	$.each(bindings, function(index, value) {
		var topObjectUri = value.topObject.value;
	    console.debug('[deleteProjectTitle4] The topObject = ' + topObjectUri); 
	    
	    deleteTriples(nextDatabank, '<' + topObjectUri + '>', 'rdfs:label');
	});
	
	console.debug('[deleteProjectTitle4] end');
}


/* 
 * Removes triples in the given databank that match the specified subject and property.
 * All parameters are mandatory.  
 */
function deleteTriples(nextDatabank, subject, property) {
	$.rdf({
		databank : nextDatabank
	})
 	  .where(subject + ' ' + property + ' ?object')
	  .sources()
	  .each(
			function(index, tripleArray) {
				console.debug('[deleteTriple] object to delete = '
						+ tripleArray[0]);
				nextDatabank.remove(tripleArray[0]);
			});
}





// --------------------------------
// everything needs to come in here
// --------------------------------
$(document).ready(
		function() {

			// auto complete 1
			$(".autocomplete").autocomplete(
					{
						delay : 500, // milliseconds
						minLength : 2, // minimum length to trigger
										// autocomplete
						source : autoCompleteCallback,
						
						focus : function(event, ui) {
							// hack to prevent ui.item.value from appearing in the textbox
							$('#in4').val(ui.item.label);
							return false;
						},
						
						select : function(event, ui) {
							console.debug('Option selected "' + ui.item.label
									+ '" with value "' + ui.item.value + '".');
							$('#in3').val(ui.item.value);
							$('#in4').val(ui.item.label);
							$('#message1').html('Selected : ' + ui.item.value);
							return false;
						}
			});

			// update the project title
			$(".short_text").blur(function(){
				var isNewTriple = false;
				var parent = '';
				var property = '<' + $(this).attr('property') + '>';
				var oldValueFormatted = '"' + $('#in1Hidden').val() + '"'; 
				var newValueFormatted = '"' + $(this).val() + '"';
				console.debug('Change property: ' + property + ' from ' + oldValueFormatted + ' to ' + newValueFormatted + '.');
				updateArtifact(isNewTriple, property, newValueFormatted, oldValueFormatted);
			});
			
			// add a new Platform
			$(".autocomplete").blur(function(){
				var parent = '';
				var isNewTriple = true;
				var property = '<' + $(this).attr('property') + '>';
				var newValue = '<' + $('#in3').val() + '>';
				console.debug('Add new property: <' + property + '> <' + newValue + '>');
				updateArtifact(isNewTriple, property, newValue);
			});

			// retrieve artifact and load it to databank
			$('#btn2').click(function(){
				var artifactUri = $('#podd_artifact').val();
				getArtifact(artifactUri);
				//removeTriple();
			});

			// test that the Message Box works
			$('#btn1').click({
				param1 : theMessageBox,
				param2 : 'Test Button clicked'
			}, displayMessage);

			// clear the Message Box
			$('#btn4').click({
				param1 : theMessageBox,
				param2 : '[Message should come here]'
			}, displayMessage);

		});
