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
	.optional('?pValue <http://www.w3.org/2000/01/rdf-schema#label> ?pValueLabel')
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
		
		if (typeof value.pValueLabel != 'undefined') {
			nextChild.displayValue = value.pValueLabel.value;
			nextChild.valueUri = value.pValue.value;
		} else {
			nextChild.displayValue = value.pValue.value;
			nextChild.valueUri = value.pValue.value;
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

// these can be loaded via freemarker
var CARD_ExactlyOne = 'http://purl.org/podd/ns/poddBase#Cardinality_Exactly_One';
var CARD_OneOrMany = 'http://purl.org/podd/ns/poddBase#Cardinality_One_Or_Many';
var CARD_ZeroOrMany = 'http://purl.org/podd/ns/poddBase#Cardinality_Zero_Or_Many'

var DISPLAY_LongText = 'http://purl.org/podd/ns/poddBase#DisplayType_LongText';
var DISPLAY_ShortText = 'http://purl.org/podd/ns/poddBase#DisplayType_ShortText';
var DISPLAY_CheckBox = 'http://purl.org/podd/ns/poddBase#DisplayType_CheckBox';
var DISPLAY_DropDown = 'http://purl.org/podd/ns/poddBase#DisplayType_DropDownList'
var DISPLAY_AutoComplete = 'http://purl.org/podd/ns/poddBase#DisplayType_AutoComplete';
var DISPLAY_Table = 'http://purl.org/podd/ns/poddBase#DisplayType_Table';
	
/*
 * Display the given field on page
 */
function displayEditField(index, nextField) {
	// console.debug('[' + nextField.weight + '] <' + nextField.propertyUri + '> "' + nextField.propertyLabel + '" <' +
	//		nextField.displayType + '> <' + nextField.cardinality + '>');

	// field name
    var span = $('<span>');
    span.attr('class', 'bold');
    span.attr('property', nextField.propertyUri);
    span.html(nextField.propertyLabel);

    var li = $("<li>")
	li.append(span);
	
	// required icon
	if (nextField.cardinality == CARD_ExactlyOne || nextField.cardinality == CARD_OneOrMany) {
		spanRequired = $('<span>');
		spanRequired.attr('icon', 'required');
		li.append(spanRequired);
	}
	
	// display (+) icon to add extra values
	if (nextField.cardinality == CARD_ZeroOrMany || nextField.cardinality == CARD_OneOrMany) {
		link = $('<a>');
		link.attr('href', 'javascript:addNewEmptyField("' + nextField.propertyLabel + '")');
		link.attr('icon', 'addField');
		link.attr('title', 'add ' + nextField.propertyLabel);
		li.append(link);
	}
	
	var li2 = $("<li>")
	
	if (nextField.displayType == DISPLAY_LongText) {
		li2.append(addFieldInputText(input, 'textarea'));
		
	} else if (nextField.displayType == DISPLAY_ShortText) {
		li2.append(addFieldInputText(nextField, 'text'));
		
	} else if (nextField.displayType == DISPLAY_DropDown) {
		li2.append(addFieldDropDownList(nextField));
		
	} else if (nextField.displayType == DISPLAY_CheckBox) {
		checkBox = $('<p>');
		checkBox.text('CheckBox here');
		
		li2.append(checkBox);
		
	} else if (nextField.displayType == DISPLAY_Table) {
		checkBox = $('<p>');
		checkBox.text('Table here please');

		li2.append(checkBox);
		
	} else if (nextField.displayType == DISPLAY_AutoComplete) {
		checkBox = $('<p>');
		checkBox.text('Auto complete please');

		li2.append(checkBox);
		
	} else { // default
		li2.append(addFieldInputText(nextField, 'text'));
	}

	li.append(li2);
	$("#details ol").append(li);
}

/*
 * Construct an HTML input field of a given type.
 */
function addFieldInputText(nextField, inputType) {
	var input = $('<input>', {
		id: 'prop_' + nextField.propertyLabel,
		name: 'prop_' + nextField.propertyLabel,
	    type: inputType,
	    value: nextField.displayValue
	});
	return input;
}

/*
 * Construct an HTML drop-down list for the given field. 
 */
function addFieldDropDownList(nextField) {
	var select = $('<select>', {
		id: 'prop_' + nextField.propertyLabel,
		name: 'prop_' + nextField.propertyLabel,
	});
	
	var myQuery = $.rdf({
		databank : nextDatabank
	})
	.prefix('poddBase', 'http://purl.org/podd/ns/poddBase#')
	.prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#')
	.where('<' + nextField.propertyUri + '> poddBase:hasAllowedValue ?pValue')
	.optional('?pValue rdfs:label ?pDisplayValue')
	;
	var bindings = myQuery.select();
	// console.debug('Found ' + bindings.length + ' bindings for query');
	$.each(bindings, function(index, value) {
		
		var optionValue = value.pValue.value;
		
		var optionDisplayValue = value.pValue.value;
		if (value.pDisplayValue != 'undefined') {
			optionDisplayValue = value.pDisplayValue.value;
		}
		
		var selectedVal = false;
		if (nextField.valueUri == optionValue)	{
			console.debug('SELECTED option = ' + optionValue);
			selectedVal = true;
		}
		
		var option = $('<option>', {
			value: optionValue,
			text: optionDisplayValue,
			selected: selectedVal
		});
		
		select.append(option);
	});
	return select;
}

//simply copied from PODD-1, does not work
function addNewEmptyField(id) {
	console.debug('Add new Empty field for "' + id + '"');
//    var element = document.createElement(type);
//    element.setAttribute('id', id);
//    element.setAttribute('name', id);
//    var li = document.createElement("li");
//    li.appendChild(element);
//    document.getElementById('add_' + id).appendChild(li);
}

/*
 * Search Ontology Resource Service using AJAX, convert the RDF response to
 * a JSON array and invoke the callback function.
 */
function searchOntologyService(
		/* object with 'search term' */ request, 
		/* function */ callbackFunction
		) {

	requestUrl = podd.baseUrl + '/search';

	console.debug('Searching artifact: "' + artifactUri.toString() + '" in searchTypes: "' + request.searchTypes 
			+ '" for terms matching "'	+ request.term + '".');

	queryParams = {
		searchterm : request.term,
		artifacturi : artifactUri.toString(),
		searchtypes : request.searchTypes
	};

	$.get(requestUrl, queryParams, function(data) {
		console.debug('Response: ' + data.toString());
		var formattedData = parsesearchresults(requestUrl, data);
		console.debug('No. of search results = ' + formattedData.length);
		//callbackFunction(formattedData);
	}, 'json');
}



