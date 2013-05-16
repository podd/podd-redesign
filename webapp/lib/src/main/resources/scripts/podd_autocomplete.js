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


var artifactIri;

var versionIri;

var theMessageBox = '#message1';

function debugPrintDatabank(databank, message) {
	var triples = $.toJSON(
			databank.dump({format : 'application/json'})
			);
	console.debug(message + ': (' + databank.size() + ') ' + triples);
}


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

			// update variables and page contents with retrieved artifact info
			var artifactId = getOntologyID(nextDatabank);
			artifactIri = artifactId[0].artifactIri;
			versionIri = artifactId[0].versionIri;
			
			$('#podd_artifact').val(artifactIri);
			$('#podd_artifact_version').val(versionIri);
			
			// update project title on page
			var title = getProjectTitle(nextDatabank);
			$('#in1').val(title.value);
			$('#in1Hidden').val(title.value);
			
			$(theMessageBox).html(
					'<i>Successfully retrieved artifact version: ' + versionIri + '</i><br>');
		},
		error : function(xhr, status, error) {
			console.debug(status + '[getArtifact] $$$ ERROR $$$ ' + error);
			//console.debug(xhr.statusText);
		}
	});
	
}

/* 
 * Invoke the Edit Artifact Service to update the artifact with changed object attributes.
 * 
 * {
 * 	isNew: boolean,
 * 	property: String value of predicate URI surrounded by angle brackets,
 * 	newValue: String, (Should be surrounded by angle brackets if a URI, or double quotes if a String literal)
 * 	oldValue: String, (Should be surrounded by angle brackets if a URI, or double quotes if a String literal)
 * } 
 */
function updatePoddObject(
		/* String */ objectUri, 
		/* array of objects */ attributes) {
	
	requestUrl = podd.baseUrl + '/artifact/edit';

	console.debug('[updatePoddObject]  "' + objectUri + '" of artifact ('	+ versionIri + ') .');
	
	$.each(attributes, function(index, attribute) {
		console.debug('[updatePoddObject] handling property: ' + attribute. property);
		if (!attribute.isNew) {
		    deleteTriples(nextDatabank, objectUri, attribute.property);
		}
		nextDatabank.add(objectUri + ' ' + attribute.property + ' ' + attribute.newValue);
	});
	
	var modifiedTriples = $.toJSON(nextDatabank.dump({
		format : 'application/json'
	}));

	// set query parameters in the URI as setting them under data failed, mostly
	// leading to a 415 error
	requestUrl = requestUrl + '?artifacturi=' + encodeURIComponent(artifactIri)
			+ '&versionuri=' + encodeURIComponent(versionIri) + '&isforce=true';
	console.debug('[updatePoddObject] Request (POST):  ' + requestUrl);

	$.ajax({
		url : requestUrl,
		type : 'POST',
		data : modifiedTriples,
		contentType : 'application/rdf+json', // what we're sending
		dataType : 'json', // what is expected back
		success : function(resultData, status, xhr) {
			console.debug('[updatePoddObject] ### SUCCESS ### ' + resultData);
			// console.debug('[updatePoddObject] ' + xhr.responseText);
			$(theMessageBox).html(
					'<i>Successfully edited artifact.</i><br><p>'
							+ xhr.responseText + '</p><br>');
			
			getArtifact(artifactIri);
		},
		error : function(xhr, status, error) {
			console.debug('[updatePoddObject] $$$ ERROR $$$ ' + error);
			console.debug(xhr.statusText);
		}
	});
}


/*
 * Call Search Ontology Resource Service using AJAX, convert the RDF response to
 * a JSON array and set to the array as autocomplete data.
 */
function autoCompleteCallback(/* object with 'search term' */ request, /* function */ response) {

	requestUrl = podd.baseUrl + '/search';

	var searchTypes = $('#podd_type').attr('value');

	console.debug('Searching artifact: "' + artifactIri.toString() + '" in searchTypes: "' + searchTypes 
			+ '" for terms matching "'	+ request.term + '".');

	queryParams = {
		searchterm : request.term,
		artifacturi : artifactIri.toString(),
		searchtypes : searchTypes
	};

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
		nextChild.value = value.projectTitle.value;

		nodeChildren.push(nextChild);
	});

	if (nodeChildren.length > 1){
		console.debug('[getProjectTitle] ERROR - More than 1 Project Title found!!!');
	}
	
	return nodeChildren[0];
};


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
							// prevent ui.item.value from appearing in the textbox
							$('#in4').val(ui.item.label);
							return false;
						},
						
						select : function(event, ui) {
							console.debug('Option selected "' + ui.item.label
									+ '" with value "' + ui.item.value + '".');
							$('#in4Hidden').val(ui.item.value);
							$('#in4').val(ui.item.label);
							$('#message1').html('Selected : ' + ui.item.value);
							return false;
						}
			});

			// update the project title
			$(".short_text").blur(function(){
				var objectUri = '<' + $('#podd_object').val() + '>';
				
				var attributes = [];
				var nextAttribute = {};
				nextAttribute.isNew = false;
				nextAttribute.property = '<' + $(this).attr('property') + '>';
				nextAttribute.newValue = '"' + $(this).val() + '"';
				nextAttribute.oldValue = '"' + $('#in1Hidden').val() + '"';

				attributes.push(nextAttribute);
				
				console.debug('Change property: ' + nextAttribute.property + ' from ' + 
						nextAttribute.oldValue + ' to ' + nextAttribute.newValue + '.');

				updatePoddObject(objectUri, attributes);
			});
			
			// add a new Platform
			$(".autocomplete").blur(function(){
				var objectUri = '<' + $('#podd_object').val() + '>';

				var attributes = [];
				var nextAttribute = {};
				nextAttribute.isNew = true;
				nextAttribute.property = '<' + $(this).attr('property') + '>';
				nextAttribute.newValue = '<' + $('#' + $(this).attr('id') + 'Hidden').val() + '>';
				attributes.push(nextAttribute);
				
				console.debug('Add new property: <' + nextAttribute.property + '> <' + nextAttribute.newValue + '>');
				
				updatePoddObject(objectUri, attributes);
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
