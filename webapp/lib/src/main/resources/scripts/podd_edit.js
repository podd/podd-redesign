/**
 * PODD : Uses jquery-RDF to analyse the current document with respect to the
 * RDFa information it contains and populate the rdfadebug element with the
 * resulting information
 */

// --------------------------------
// invoked when page is "ready"
// --------------------------------
$(document).ready(
	function() {
		console.debug('-------------------');
		console.debug('initializing...');
		console.debug('-------------------');

		getPoddObjectForEdit(artifactUri, objectUri);

		console.debug('### initialization complete ###');
});

var nextDatabank = $.rdf.databank();

var artifactUri = 'http://purl.org/podd/basic-2-20130206/artifact:1';

var objectUri = 'http://purl.org/podd/basic-1-20130206/object:2966';

// --------------------------------


/* 
 * Retrieve an RDF containing necessary data and meta-data to populate the Edit Artifact
 * page.
 */
function getPoddObjectForEdit(
		/* String */ artifactUri,
		/* String */ objectUri) {
	
	console.debug('[getPoddObjectForEdit]  "' + objectUri + '" of artifact ('	+ artifactUri + ') .');

	requestUrl = podd.baseUrl + '/artifact/edit';
	console.debug('[getPoddObjectForEdit] Request (GET):  ' + requestUrl);

	$.ajax({
		url : requestUrl,
		type : 'GET',
		data : {artifacturi : artifactUri, objecturi : objectUri},
		dataType : 'json', // what is expected back
		success : loadEditDataCallback,
		error : function(xhr, status, error) {
			console.debug('[getPoddObjectForEdit] $$$ ERROR $$$ ' + error);
			console.debug(xhr.statusText);
		}
	});
}

/*
 * Callback function when RDF containing Edit data is available
 */
function loadEditDataCallback(resultData, status, xhr) {
	console.debug('[getPoddObjectForEdit] ### SUCCESS ### ' + resultData);
	
	nextDatabank = nextDatabank.load(resultData);
	console.debug('Databank size = ' + nextDatabank.size());
	
	// retrieve weighted property list
	var myQuery = $.rdf({
		databank : nextDatabank
	})
	.prefix('poddBase', 'http://purl.org/podd/ns/poddBase#')
	.where('?objectUri ?propertyUri ?pValue')
	.where('?propertyUri poddBase:weight ?weight')
	.optional('?propertyUri <http://www.w3.org/2000/01/rdf-schema#label> ?pLabel')
	.optional('?propertyUri poddBase:hasDisplayType ?displayType')
	.optional('?propertyUri poddBase:hasCardinality ?cardinality')
	;
	var bindings = myQuery.select();

	var propertyList = [];
	$.each(bindings, function(index, value) {
		var nextChild = {};
		nextChild.weight;
		nextChild.propertyUri = value.propertyUri.value;
		nextChild.propertyLabel;
		nextChild.displayType;
		nextChild.cardinality;
		nextChild.value = value.pValue.value;
		
		if (typeof value.pLabel != 'undefined') {
			nextChild.propertyLabel = value.pLabel.value;
		}
		if (typeof value.displayType != 'undefined') {
			nextChild.displayType = value.displayType.value;
		}
		if (typeof value.weight != 'undefined') {
			nextChild.weight = value.weight.value;	
		}
		
		if (typeof value.cardinality != 'undefined') {
			nextChild.cardinality = value.cardinality.value;
		}
		
		propertyList.push(nextChild);
		//console.debug(nextChild.weight + '] <' + nextChild.propertyUri + '> "' + nextChild.propertyLabel + '" <' +
		//		nextChild.displayType + '> <' + nextChild.cardinality + '>');
	});

	
	// sort property list
	propertyList.sort(function(a, b) {
		   var aID = a.weight;
		   var bID = b.weight;
		   return (aID == bID) ? 0 : (aID > bID) ? 1 : -1;
		});
	
	$.each(propertyList, displayEditField);
}

/*
 * Display the given field on page
 */
function displayEditField(index, nextField) {
	// console.debug('[' + nextField.weight + '] <' + nextField.propertyUri + '> "' + nextField.propertyLabel + '" <' +
	//		nextField.displayType + '> <' + nextField.cardinality + '>');
	
	var html = '<li> <span class="bold">:' + nextField.propertyLabel + '</span>';
	
	if (nextField.cardinality == 'http://purl.org/podd/ns/poddBase#Cardinality_Exactly_One' ||
			nextField.cardinality == 'http://purl.org/podd/ns/poddBase#Cardinality_One_Or_Many') {
		
		html = html + '<span icon="required"></span>';
	}
	
	if (nextField.cardinality == 'http://purl.org/podd/ns/poddBase#Cardinality_Zero_Or_Many' ||
			nextField.cardinality == 'http://purl.org/podd/ns/poddBase#Cardinality_One_Or_Many') {
		
		html = html + '<a href="javascript:addField(TODO)" icon="addField" title="add ' + nextField.propertyLabel 
		+ '"></a>';
	}
	
	
	if (nextField.displayType == 'http://purl.org/podd/ns/poddBase#DisplayType_LongText') {
		html = html + '<input property="' + nextField.propertyUri + '"  id="prop_' + nextField.propertyLabel +
		'" name="' + '" value="' + nextField.value + '" type="textarea">';
		
	} else if (nextField.displayType == 'http://purl.org/podd/ns/poddBase#DisplayType_DropDownList') {
		html = html + '<p>Drop Down List</p>';
		
	} else if (nextField.displayType == 'http://purl.org/podd/ns/poddBase#DisplayType_CheckBox') {
		html = html + '<input type="checkbox" name="' + nextField.propertyLabel + '" value="' + nextField.value + '">' + nextField.value;
		
	} else if (nextField.displayType == 'http://purl.org/podd/ns/poddBase#DisplayType_Table') {
		html = html + '<p>Table</p>';
		
	} else if (nextField.displayType == 'http://purl.org/podd/ns/poddBase#DisplayType_AutoComplete') {
		html = html + '<p>auto complete</p>';
		
	} else { // default, short_text
	
		html = html + '<input property="' + nextField.propertyUri + '"  id="prop_' + nextField.propertyLabel +
			'" name="' + '" value="' + nextField.value + '" type="text">';
	}
	html = html + '</li>';
	
	$("#details ol").append(html);
	
	
	
	
}
