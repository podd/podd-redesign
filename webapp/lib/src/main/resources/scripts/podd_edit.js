/**
 * PODD : Uses jquery-RDF to analyse the current document with respect to the
 * RDFa information it contains and populate the rdfadebug element with the
 * resulting information
 */

// --------------------- Constants ----------------------------
// These are used to define the expected cardinalities, so that the server knows
// whether to offer or accept properties with a given number of values.
var CARD_ExactlyOne = 'http://purl.org/podd/ns/poddBase#Cardinality_Exactly_One';
var CARD_OneOrMany = 'http://purl.org/podd/ns/poddBase#Cardinality_One_Or_Many';
var CARD_ZeroOrMany = 'http://purl.org/podd/ns/poddBase#Cardinality_Zero_Or_Many'

// These are used to define the expected input control types, so that the client
// knows which method to use in each case.
var DISPLAY_LongText = 'http://purl.org/podd/ns/poddBase#DisplayType_LongText';
var DISPLAY_ShortText = 'http://purl.org/podd/ns/poddBase#DisplayType_ShortText';
var DISPLAY_CheckBox = 'http://purl.org/podd/ns/poddBase#DisplayType_CheckBox';
var DISPLAY_DropDown = 'http://purl.org/podd/ns/poddBase#DisplayType_DropDownList'
var DISPLAY_AutoComplete = 'http://purl.org/podd/ns/poddBase#DisplayType_AutoComplete';
var DISPLAY_Table = 'http://purl.org/podd/ns/poddBase#DisplayType_Table';

var OBJECT_PROPERTY = 'http://www.w3.org/2002/07/owl#ObjectProperty';
var DATATYPE_PROPERTY = 'http://www.w3.org/2002/07/owl#DatatypeProperty';
var OWL_THING = 'http://www.w3.org/2002/07/owl#Thing';
var XSD_DATETIME = 'http://www.w3.org/2001/XMLSchema#dateTime';

var DETAILS_LIST_Selector = '#details ol';

var DUMMY_Datetime = '1970-01-01T00:00:00';
// --------------------------------

/**
 * Set this to have multiple 'searchtypes' query parameters sent to the
 * SearchOntologyService
 */
jQuery.ajaxSettings.traditional = true;

/**
 * Creates a new rdfquery.js databank, adds common prefixes to it, and then
 * returns the databank to the caller.
 */
podd.newDatabank = function() {
    var nextDatabank = $.rdf.databank();
    // TODO: Is base useful to us?
    // nextDatabank.base("http://www.example.org/")
    nextDatabank.prefix("dcterms", "http://purl.org/dc/terms/");
    nextDatabank.prefix('poddBase', 'http://purl.org/podd/ns/poddBase#');
    nextDatabank.prefix('poddUser', 'http://purl.org/podd/ns/poddUser#');
    nextDatabank.prefix('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#');
    nextDatabank.prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#');
    nextDatabank.prefix('owl', 'http://www.w3.org/2002/07/owl#');
    nextDatabank.prefix("foaf", "http://xmlns.com/foaf/0.1/");
    nextDatabank.prefix("moat", "http://moat-project.org/ns#");
    nextDatabank.prefix("tagging", "http://www.holygoat.co.uk/owl/redwood/0.1/tags/");

    return nextDatabank;
};

/**
 * Add triples to the given databank to initialise the top object using the PODD
 * Plant ontologies.
 * 
 * TODO: At some stage in the future make the list of ontologies configurable.
 * 
 * IMPORTANT: When the versions change, this method must be updated to the
 * current versions.
 */
podd.initialiseNewTopObject = function(nextDatabank, artifactUri, objectUri) {
    nextDatabank.add(artifactUri + ' rdf:type owl:Ontology ');
    nextDatabank.add(artifactUri + ' poddBase:artifactHasTopObject ' + objectUri);
    nextDatabank.add(artifactUri + ' owl:imports <http://purl.org/podd/ns/version/dcTerms/1>');
    nextDatabank.add(artifactUri + ' owl:imports <http://purl.org/podd/ns/version/foaf/1>');
    nextDatabank.add(artifactUri + ' owl:imports <http://purl.org/podd/ns/version/poddUser/1>');
    nextDatabank.add(artifactUri + ' owl:imports <http://purl.org/podd/ns/version/poddBase/1>');
    nextDatabank.add(artifactUri + ' owl:imports <http://purl.org/podd/ns/version/poddScience/1>');
    nextDatabank.add(artifactUri + ' owl:imports <http://purl.org/podd/ns/version/poddPlant/1>');
    
    // add createdAt statement with dummy value
	nextDatabank.add(
		podd.buildTriple(objectUri,	'<http://purl.org/podd/ns/poddBase#createdAt>',
			DUMMY_Datetime, DATATYPE_PROPERTY, XSD_DATETIME)
	);

    // add lastModified statement with dummy value
	nextDatabank.add(
		podd.buildTriple(objectUri,	'<http://purl.org/podd/ns/poddBase#lastModified>',
			DUMMY_Datetime, DATATYPE_PROPERTY, XSD_DATETIME)
	);
};

/**
 * Add triples to the given databank to initialise a new non-TopObject with the
 * given relationship to an existing parent.
 * 
 */
podd.initialiseNewObject = function(nextDatabank, artifactUri, objectUri, parentUri, parentPredicateUri) {
    nextDatabank.add(parentUri + ' ' + parentPredicateUri + ' ' + objectUri);
    
    // add createdAt statement with default value
	nextDatabank.add(
		podd.buildTriple(objectUri,	'<http://purl.org/podd/ns/poddBase#createdAt>',
			DUMMY_Datetime, DATATYPE_PROPERTY, XSD_DATETIME)
	);

    // add lastModified statement with dummy value
	nextDatabank.add(
		podd.buildTriple(objectUri,	'<http://purl.org/podd/ns/poddBase#lastModified>',
			DUMMY_Datetime, DATATYPE_PROPERTY, XSD_DATETIME)
	);
};

/**
 * If podd.objectUri exists, it returns that, wrapped in braces to look like a
 * URI ready for rdfquery.js.
 * 
 * Otherwise, it returns a fixed temporary URI that is recognised by the server
 * as such, and replaced with a PURL when it is first submitted.
 */
podd.getCurrentObjectUri = function() {
    var nextObjectUri;

    if (typeof podd.objectUri === 'undefined' || podd.objectUri === 'undefined') {
        // hardcoded blank node for new objects
        // this will be replaced after the first valid submission of the
        // object to the server
        nextObjectUri = "<urn:temp:uuid:object>";
    }
    else {
        nextObjectUri = '<' + podd.objectUri + '>';
    }

    return nextObjectUri;
};

/**
 * If artifactIri exists, it returns that, wrapped in braces to look like a URI
 * ready for rdfquery.js.
 * 
 * Otherwise, it returns a fixed temporary URI that is recognised by the server
 * as such, and replaced with a PURL when it is first submitted.
 */
podd.getCurrentArtifactIri = function() {
    var nextArtifactIri;

    if (typeof podd.artifactIri === 'undefined' || podd.artifactIri === 'undefined') {
        // hardcoded blank node for new artifacts
        // this will be replaced after the first valid submission of the
        // artifact to the server
        nextArtifactIri = "<urn:temp:uuid:artifact>";
    }
    else {
        nextArtifactIri = '<' + podd.artifactIri + '>';
    }

    return nextArtifactIri;
};

/**
 * Getter method for Version IRI.
 * 
 * @return the Version IRI or 'undefined' if it is not defined.
 */
podd.getCurrentVersionIri = function() {
	return podd.versionIri;
};

/**
 * Updates the given databank using the given changesets to identify old and new
 * triples.
 * 
 * The changesets are each checked to see if they are new, and if so, whether
 * they contain any oldTriples, and if so those triples are all deleted.
 * 
 * Then we back through the changesets to see if they contain any newTriples,
 * which are then added into the databank.
 */
podd.updateDatabank = function(/* object */changesets, /* object */nextDatabank) {
    $.each(changesets, function(index, changeset) {
        if (!changeset.isNew) {
            $.each(changeset.oldTriples, function(nextOldTripleIndex, nextOldTriple) {
                podd.debug('[updateDatabank] remove oldTriple: ' + nextOldTriple);
                nextDatabank.remove(nextOldTriple);
            });
        }
    });
    $.each(changesets, function(index, changeset) {
        $.each(changeset.newTriples, function(nextNewTripleIndex, nextNewTriple) {
            podd.debug('[updateDatabank] add newTriple: ' + nextNewTriple);
            nextDatabank.add(nextNewTriple);
        });
    });
};

/**
 * Removes triples in the given databank that match the specified subject and
 * property. All parameters are mandatory.
 */
podd.deleteTriples = function(nextDatabank, subject, property) {
    $.rdf({
        databank : nextDatabank
    }).where(subject + ' ' + property + ' ?object').sources().each(function(index, tripleArray) {
        podd.debug('[deleteTriples] object to delete = ' + tripleArray[0]);
        nextDatabank.remove(tripleArray[0]);
    });
};

/**
 * Display a message on leaving text field
 * 
 * This is mostly used for DEBUG.
 */
podd.updateErrorMessageList = function(theMessage) {
    var li = $("<li>")
    li.html(theMessage);
    $("#errorMsgList").append(li);
};

/**
 * DEBUG-ONLY : Prints the contents of the given databank to the console
 */
podd.debugPrintDatabank = function(databank, message) {
    var triples = $.toJSON(databank.dump({
        format : 'application/json'
    }));
    podd.debug(message + ': (' + databank.size() + ') ' + triples);
};

/**
 * Print debug message to console.
 */
podd.debug = function(message) {
    if (typeof console !== "undefined" && console.debug) {
        console.debug('[DEBUG] ' + message);
    }
};

/**
 * Retrieve metadata to render the fields to add a new object of the given type.
 * 
 * @param objectTypeUri -
 *            the type of Object to be added
 * @param successCallback -
 *            where to send the results
 * @param nextSchemaDatabank -
 * @param nextArtifactDatabank -
 */
podd.getObjectTypeMetadata = function(/* String */objectTypeUri, /* function */successCallback, /* object */
nextSchemaDatabank, /* object */nextArtifactDatabank) {

	podd.debug('[getMetadata]  "' + objectTypeUri + '" .');

    var requestUrl = podd.baseUrl + '/metadata';

    podd.debug('[getMetadata] Request (GET):  ' + requestUrl);

    $.ajax({
        url : requestUrl,
        type : 'GET',
        data : {
            objecttypeuri : objectTypeUri
        },
        dataType : 'json', // what is expected back
        success : function(resultData, status, xhr) {
            podd.debug('[getObjectTypeMetadata] ### SUCCESS ### ');
			podd.debug(resultData);

            nextSchemaDatabank.load(resultData);

            podd.debug('Schema Databank size = ' + nextSchemaDatabank.size());

            successCallback(objectTypeUri, nextSchemaDatabank, nextArtifactDatabank);
        },
        error : function(xhr, status, error) {
            podd.debug('[getMetadata] $$$ ERROR $$$ ' + error);
            podd.debug(xhr.statusText);
        }
    });
};

/**
 * Callback function that redirects to the artifact when submitPoddObjectUpdate
 * is successful.
 */
podd.redirectToGetArtifact = function(objectType, nextSchemaDatabank, nextArtifactDatabank) {
    window.location.href = podd.baseUrl + '/artifact/base?artifacturi=' + encodeURIComponent(podd.artifactIri);
};

/**
 * 
 */
podd.emptyUpdateDisplayCallback = function(objectType, nextSchemaDatabank, nextArtifactDatabank) {
    podd.debug('Empty Callback');
};


/**
 * Callback function when RDF containing metadata is available
 * 
 * FIXME: Any properties without weights should have them added, just as any
 * properties without labels should have them added.
 */
podd.updateInterface = function(objectType, nextSchemaDatabank, nextArtifactDatabank) {
    // retrieve weighted property list
    var myQuery = $.rdf({
        databank : nextSchemaDatabank
    })
    // Desired type a OWL:Class
    .where('<' + objectType + '> rdf:type owl:Class')
    // Desired type has rdfs:subClassOf
    .where('<' + objectType + '> rdfs:subClassOf ?x')
    // Subclass is an owl:Restriction
    .where('?x rdf:type owl:Restriction')
    // Restriction has a linked property, which is the minimum we require
    .where('?x owl:onProperty ?propertyUri')
    // Optional, one way of specifying Range
    .optional('?x owl:allValuesFrom ?allValuesClass')
    // Optional, one way of specifying Range
    .optional('?x owl:onDataRange ?rangeClass')
    // Optional, one way of specifying Range
    .optional('?x owl:onClass ?onClass')
    // Optional, though recommended, rdfs:label annotation on property
    .optional('?propertyUri rdfs:label ?propertyLabel')
    // Optional, though recommended, display type to customise the HTML
    // interface for this property
    .optional('?propertyUri poddBase:hasDisplayType ?displayType')
    // Optional, though recommended, cardinality to specify how many of this
    // property can be linked to this class
    .optional('?propertyUri poddBase:hasCardinality ?cardinality')
    // Optional, to figure out if values are Literals or Resources
    .optional('?propertyUri rdf:type ?propertyType')
    // Optional, weight given for property when used with this class to order
    // the interface consistently
    .optional('?propertyUri poddBase:weight ?weight');

    var bindings = myQuery.select();

    var propertyList = [];
    var propertyUris = [];
    $.each(bindings, function(index, nextBinding) {
        var nextChild = {};
        nextChild.weight;
        nextChild.propertyUri = nextBinding.propertyUri.value;
        nextChild.propertyLabel;
        nextChild.displayType;
        nextChild.cardinality;
        nextChild.propertyRange;
        nextChild.propertyType;

        if (typeof nextBinding.propertyLabel != 'undefined') {
            nextChild.propertyLabel = nextBinding.propertyLabel.value;
        }
        else {
            podd.debug("Did not find a label for property: " + nextBinding.propertyUri.value);
            nextChild.propertyLabel = nextBinding.propertyUri.value;
        }

        if (typeof nextBinding.displayType != 'undefined') {
            nextChild.displayType = nextBinding.displayType.value;
        }

        if (typeof nextBinding.weight != 'undefined') {
            nextChild.weight = nextBinding.weight.value;
        }
        else {
            nextChild.weight = 99;
        }

        if (typeof nextBinding.cardinality != 'undefined') {
            nextChild.cardinality = nextBinding.cardinality.value;
        }

        // set Range of the property
        if (typeof nextBinding.allValuesClass !== 'undefined') {
    		nextChild.propertyRange = nextBinding.allValuesClass.value;
    	} else if (typeof nextBinding.rangeClass !== 'undefined') {
    		nextChild.propertyRange = nextBinding.rangeClass.value;
    	} else if (typeof nextBinding.onClass !== 'undefined') {
    		nextChild.propertyRange = nextBinding.onClass.value;
    	} else {
    		nextChild.propertyRange = 'Not Found';
    	}
        
        if (typeof nextBinding.propertyType != 'undefined') {
            nextChild.propertyType = nextBinding.propertyType.value;
        }

        
        nextChild.displayValue; //undefined to indicate there is NO value
        nextChild.valueUri = '';

        // Avoid duplicates, which are occurring due to multiple ways of specifying ranges/etc., in OWL
        if($.inArray(nextChild.propertyUri, propertyUris) === -1)
       	{
            propertyUris.push(nextChild.propertyUri);
            propertyList.push(nextChild);
//                podd.debug("[" + nextChild.weight + "] propertyUri=<" + nextChild.propertyUri + "> label=\""
//                        + nextChild.propertyLabel + "\" displayType=<" + nextChild.displayType + "> card=<"
//                        + nextChild.cardinality + ">");
       	}
        else
        {
            podd.debug("Duplicate property found: "+nextChild.propertyUri);
        }
    });

    // sort property list in ascending order of weight
    propertyList.sort(function(a, b) {
        var aID = a.weight;
        var bID = b.weight;
        
        if (aID == bID) {
        	// on equal weights sort by property label
        	return (a.propertyLabel > b.propertyLabel) ? 1: -1;
        } else {
        	return (aID - bID);
        }
    });
    
    // Reset the details list
    $(DETAILS_LIST_Selector).empty();

    $.each(propertyList, function(index, value) {
        var nextArtifactQuery = $.rdf({
            databank : nextArtifactDatabank
        })
        // Desired object a objectType
        .where(podd.getCurrentObjectUri() + ' rdf:type <' + objectType + '>')
        // Restriction has a linked property, which is the minimum we
        // require
        .where(podd.getCurrentObjectUri() + ' <' + value.propertyUri + '> ?propertyValue ');

        var nextArtifactBindings = nextArtifactQuery.select();

        // If there are values for the property in the artifact
        // databank, display them instead of showing a single new empty field
        if (nextArtifactBindings.length > 0) {
            podd.debug("Found existing values for " + podd.getCurrentObjectUri() + " property <" + value.propertyUri
                    + "> value=" + nextArtifactBindings);
            $.each(nextArtifactBindings, function(nextArtifactIndex, nextArtifactValue) {
                // found existing value for property

                // for URIs populate the valueUri property with the value so we
                // have the option to put a human readable label in displayValue
                if (nextArtifactValue.propertyValue.type === 'uri') {
                    // TODO: Fetch label here for the URI
                    value.valueUri = nextArtifactValue.propertyValue.value;
                }
                value.displayValue = nextArtifactValue.propertyValue.value;
                $(DETAILS_LIST_Selector).append(podd.createEditField(value, nextSchemaDatabank, nextArtifactDatabank, false));
            });
        }
        else {
            podd.debug("Found no existing values for " + podd.getCurrentObjectUri() + " property <"
                    + value.propertyUri + "> value=" + nextArtifactBindings);
            $(DETAILS_LIST_Selector).append(podd.createEditField(value, nextSchemaDatabank, nextArtifactDatabank, true));
        }
    });
};

/**
 * Display the given field on page
 * 
 * @param nextField
 *            Object containing details of field to be displayed
 * @param nextSchemaDatabank
 *            Databank containing schema triples
 * @param nextArtifactDatabank
 *            Databank containing artifact triples
 * @param isNew
 *            Boolean value indicating whether this field is new or a value
 *            exists
 */
podd.createEditField = function(nextField, nextSchemaDatabank, nextArtifactDatabank, isNew) {
	// podd.debug('[' + nextField.weight + '] <' + nextField.propertyUri
	// + '> "' + nextField.propertyLabel + '" <' +
	// nextField.displayType + '> <' + nextField.cardinality + '>');

	// podd.debug('<<<<< [' + nextField.propertyUri + '] has range [' + nextField.propertyRange + '] >>>>>');
	
    // field name
    var span = $('<span>');
    span.attr('class', 'bold');
    span.attr('property', nextField.propertyUri.toString());
    span.attr('title', nextField.propertyUri.toString());
    // Make sure that in the case that the label was not found that we give it
    // the URI as a last resort label
    if (typeof nextField.propertyLabel !== "undefined") {
        span.html(nextField.propertyLabel);
    }
    else {
        span.html(nextField.propertyUri.toString());
    }

    var li = $("<li>")
    li.append(span);

    // required icon
    if (nextField.cardinality == CARD_ExactlyOne || nextField.cardinality == CARD_OneOrMany) {
        spanRequired = $('<span>');
        spanRequired.attr('icon', 'required');
        li.append(spanRequired);
    }

    var link = undefined;

    // display (+) icon to add extra values
    if (nextField.cardinality == CARD_ZeroOrMany || nextField.cardinality == CARD_OneOrMany) {
        link = $('<a>');
        // link.attr('href', '#');
        link.attr('icon', 'addField');
        link.attr('title', 'Add ' + nextField.propertyLabel);
        link.attr('class', 'clonable');
        link.attr('property', nextField.propertyUri);
        li.append(link);
    }

    var li2 = $("<li>");

    if (nextField.displayType == DISPLAY_LongText) {
        var input = podd.addFieldTextArea(nextField, 30, 2, nextSchemaDatabank);
        podd.addTextFieldBlurHandler(input, nextField.propertyUri, nextField.displayValue, nextField.propertyType, nextArtifactDatabank, isNew);
        if (typeof link !== 'undefined') {
            link.click(function() {
                var clonedField = input.clone(true);
                podd.addTextFieldBlurHandler(clonedField, nextField.propertyUri, nextField.displayValue, nextField.propertyType, 
                        nextArtifactDatabank, true);
                // FIXME: Determine correct place to append this to
                li.append(clonedField);
            });
        }
        li2.append(input);
    }
    else if (nextField.displayType == DISPLAY_ShortText) {
        var input = podd.addFieldInputText(nextField, 'text', nextSchemaDatabank);
        podd.addTextFieldBlurHandler(input, nextField.propertyUri, nextField.displayValue, nextField.propertyType, nextArtifactDatabank, isNew);
        li2.append(input);
    }
    else if (nextField.displayType == DISPLAY_DropDown) {
        var input = podd.addFieldDropDownListNonAutoComplete(nextField, nextSchemaDatabank, isNew);
        podd.addTextFieldBlurHandler(input, nextField.propertyUri, nextField.displayValue, nextField.propertyType, nextArtifactDatabank, isNew);
        li2.append(input);
    }
    else if (nextField.displayType == DISPLAY_CheckBox) {
        var input = podd.addFieldInputText(nextField, 'checkbox', nextSchemaDatabank);
        var label = '<label>' + nextField.displayValue + '</label>';
        // TODO: add blur handler
        li2.append(input.after(label));
    }
    else if (nextField.displayType == DISPLAY_Table) {
        var checkBox = $('<p>');
        checkBox.text('Table here please');

        li2.append(checkBox);
    }
    else if (nextField.displayType == DISPLAY_AutoComplete) {

		// - set search Types
		var searchTypes = [ OWL_THING ];
		if (typeof nextField.propertyRange != 'undefined'
				&& nextField.propertyRange != 'Not Found') {
			searchTypes.push(nextField.propertyRange);
		}

		// - set artifact URI
		var artifactUri;
		if (typeof podd.artifactIri != 'undefined'
				&& podd.artifactIri != 'undefined') {
			artifactUri = podd.artifactIri;
		}

		var input = podd.addFieldInputText(nextField, 'text',
				nextSchemaDatabank);
		var hiddenValueElement = podd.addFieldInputText(nextField, 'hidden',
				nextSchemaDatabank);
		podd.addAutoCompleteHandler(input, hiddenValueElement,
				nextArtifactDatabank, searchTypes, artifactUri, isNew);
		podd.addTextFieldBlurHandler(input, nextField.propertyUri,
				nextField.displayValue, nextField.propertyType,
				nextArtifactDatabank, isNew);

		li2.append(input);
		li2.append(hiddenValueElement);
    }
    else { // default
        podd.updateErrorMessageList("TODO: Support property : " + nextField.propertyUri + " (" + nextField.displayValue
                + ")");
        // var input = podd.addFieldInputText(nextField, 'text',
        // nextSchemaDatabank);
        // podd.addTextFieldBlurHandler(input, nextField.propertyUri,
        // nextField.displayValue, nextField.propertyType, nextArtifactDatabank, isNew);
        // li2.append(input);
    }

    var subList = $('<ul>').append(li2);
    li.append(subList);
    return li;
};

/**
 * FIXME: Convert the following to a range of functions that work for each
 * object type.
 * 
 * This function must be able to reset the value consistently and attach
 * handlers that know the field has not been saved before this point.
 */
podd.cloneEmptyField = function() {
    // var thisProperty = $(this).attr('property');
    // if (typeof console !== "undefined" && console.debug) {
    // console.debug('Clicked clonable: ' + thisProperty);
    // }

    // var idToClone = '#tLbl1'; //
    // '#id_http://purl.org/podd/ns/poddScience_hasANZSRC';
    // //'#id_' + $(this).attr('property');
    // console.debug('Requested cloning ' + idToClone);

    var clonedField = $(this).clone(true);
    // clonedField.attr('id', 'LABEL_cloned');
    // console.debug('Cloned: ' + clonedField.attr('id'));
    // $(this).append(clonedField);
    // if (typeof console !== "undefined" && console.debug) {
    // console.debug('appended cloned field');
    // }

    // var newObject = jQuery.extend(true, {}, $(idToClone));
    // console.debug('## SO clone: ' + JSON.stringify(newObject, null, 4));
    // $(this).append(newObject);

    // debug - cloning
    // var p1Id = '#p1';
    // var cloned = $(p1Id).clone()
    // var oldId = cloned.attr('id');
    // cloned.attr('id', oldId + '_v1');
    // $(this).append(cloned);

};

/**
 * Construct an HTML input field of a type text or checkbox.
 */
podd.addFieldInputText = function(nextField, inputType, nextDatabank) {

    var displayValue = nextField.displayValue;
    if (inputType == 'checkbox') {
        displayValue = nextField.valueUri;
    }

    // FIXME: id is useless here as it doesn't preserve the URI, and it will
    // never be unique for more than one element
    // var idString = 'id_' + nextField.propertyUri;
    // idString = idString.replace("#", "_");

    var input = $('<input>', {
        // id : idString,
        name : 'name_' + nextField.propertyLabel,
        type : inputType,
        value : displayValue
    });

    input.attr('datatype', nextField.propertyRange);
    
    // add handler to process changes to this field
    // - handler should have property URI
    // - detect if value actually changed
    // - if changed, update the "instance" databank and set dirty flag

    return input;
};

/**
 * Construct an HTML TextArea element.
 * 
 * @param nextField
 * 			Object containing values and meta-data of the current property
 * @param noOfColumns
 * 			number of columns in the TextArea
 * @param noOfRows
 * 			number of rows in the TextArea
 * @param nextSchemaDatabank
 * 			Databank containing all meta-data
 */
podd.addFieldTextArea = function(nextField, noOfColumns, noOfRows, nextSchemaDatabank) {

    var textarea = $('<textarea>', {
        name : 'name_' + nextField.propertyLabel,
        cols : noOfColumns,
        rows : noOfRows,
        value : nextField.displayValue
    });

    textarea.attr('datatype', nextField.propertyRange);

    // add handler to process changes to this field
    // - handler should have property URI
    // - detect if value actually changed
    // - if changed, update the "instance" databank and set dirty flag

    return textarea;
};


/**
 * Construct an HTML drop-down list for the given field without using
 * autocomplete or search services.
 * 
 * In these cases, the relevant options for this property must have been loaded
 * into the schema databank prior to calling this method.
 */
podd.addFieldDropDownListNonAutoComplete = function(nextField, nextSchemaDatabank, isNew) {
    podd.debug("addFieldDropDownListNonAutoComplete");
    var select = $('<select>', {
        // id : 'id_' + nextField.propertyUri,
        name : 'name_' + encodeURIComponent(nextField.propertyLabel),
    });

    select.attr('datatype', nextField.propertyRange);
    
    var myQuery = $.rdf({
        databank : nextSchemaDatabank
    })
    // Find all display values for this property
    .where('?restriction owl:onProperty <' + nextField.propertyUri + '> ')
    //
    .where('?restriction owl:allValuesFrom ?class')
    // 
    .where('?pValue rdf:type ?class')
    //
    .optional('?pValue rdfs:label ?pDisplayValue');
    var bindings = myQuery.select();

    $.each(bindings, function(index, value) {

        var optionValue = value.pValue.value;

        var optionDisplayValue = value.pValue.value;
        if (typeof value.pDisplayValue != 'undefined') {
            optionDisplayValue = value.pDisplayValue.value;
        }

        var selectedVal = false;
        if (nextField.valueUri == optionValue) {
            podd.debug('SELECTED option = ' + optionValue);
            selectedVal = true;
        }

        var option = $('<option>', {
            value : optionValue,
            text : optionDisplayValue,
            // TODO: Does the following need to be "selected: selected"?
            selected : selectedVal
        });

        select.append(option);
    });
    return select;
};

/**
 * Call Search Ontology Resource Service using AJAX, convert the RDF response to
 * a JSON array and invoke the specified callback function.
 * 
 * @param request
 *            Object containing the 'search term', 'search types' and artifact
 *            URI to search in
 * @param callbackFunction
 *            Function to be invoked on completion of the search request
 */
podd.searchOntologyService = function(
/* object with 'search term' */request,
/* function */callbackFunction) {

    var requestUrl = podd.baseUrl + '/search';

    podd.debug('Searching artifact: "' + request.artifactUri + '" in searchTypes: "' + request.searchTypes
            + '" for terms matching "' + request.term + '".');

    queryParams = {
        searchterm : request.term,
        artifacturi : request.artifactUri,
        searchtypes : request.searchTypes
    };

    $.get(requestUrl, queryParams, function(data) {
        podd.debug('Response: ' + data.toString());
        var formattedData = podd.parseSearchResults(requestUrl, data);
        podd.debug('No. of search results = ' + formattedData.length);
        callbackFunction(formattedData);
    }, 'json');
};

/* Retrieve the current version of an artifact and populate the databank */
podd.getArtifact = function(artifactUri, nextSchemaDatabank, nextArtifactDatabank, updateDisplayCallbackFunction) {
    var requestUrl = podd.baseUrl + '/artifact/base?artifacturi=' + encodeURIComponent(artifactUri);

    podd.debug('[getArtifact] Request to: ' + requestUrl);
    $.ajax({
        url : requestUrl,
        type : 'GET',
        // dataType : 'application/rdf+xml', // what is expected back
        success : function(resultData, status, xhr) {
            // Wipe out databank so it contains the most current copy after this
            // point, since we succeeded in our quest to get the current version
        	// FIXME: have a separate method which deletes everything
            podd.deleteTriples(nextArtifactDatabank, "?subject", "?predicate");
            nextArtifactDatabank.load(resultData);
            podd.debug('[getArtifact] ### SUCCESS ### loaded databank with size ' + nextArtifactDatabank.size());

            // update variables and page contents with retrieved artifact info
            var artifactId = podd.getOntologyID(nextArtifactDatabank);
            podd.artifactIri = artifactId[0].artifactIri;
            podd.versionIri = artifactId[0].versionIri;
            // FIXME: May not always want to do this
            podd.parentUri = artifactId[0].parentUri;
            podd.objectUri = artifactId[0].objectUri;

            podd.updateErrorMessageList('<i>Successfully retrieved artifact version: ' + podd.versionIri + '</i><br>');
            // The following may update the interface, redirect the user to
            // another page, or so anything it likes really
            updateDisplayCallbackFunction(podd.objectTypeUri, nextSchemaDatabank, nextArtifactDatabank);
        },
        error : function(xhr, status, error) {
            podd.debug(status + '[getArtifact] $$$ ERROR $$$ ' + error);
            // podd.debug(xhr.statusText);
        }
    });

};

/**
 * Invoke the Edit Artifact Service to update the artifact with changed object
 * attributes. { isNew: boolean, property: String value of predicate URI
 * surrounded by angle brackets, newValue: String, (Should be surrounded by
 * angle brackets if a URI, or double quotes if a String literal) oldValue:
 * String, (Should be surrounded by angle brackets if a URI, or double quotes if
 * a String literal) }
 */
podd.submitPoddObjectUpdate = function(
/* String */artifactIri,
/* String */versionIri,
/* String */objectUri,
/* object */nextSchemaDatabank,
/* object */nextArtifactDatabank,
/* function */updateCallback) {

    var requestUrl;

    var modifiedTriples = $.toJSON(nextArtifactDatabank.dump({
        format : 'application/json'
    }));

    podd.debug('[updatePoddObject]  "' + objectUri);
    if (typeof artifactIri === 'undefined') {
        artifactIri = '<urn:temp:uuid:artifact>';
    }

    if (typeof artifactIri !== "undefined" && artifactIri.lastIndexOf('<urn:temp:uuid:', 0) === 0) {
        // Create a new object if it wasn't defined
        // To succeed this will require the object to be a valid PoddTopObject
        requestUrl = podd.baseUrl + '/artifact/new';
    }
    else {
    	requestUrl = podd.baseUrl + '/artifact/edit'
    	// FIXME: Why is the parameter isForce hardcoded to true?
        requestUrl = requestUrl + '?artifacturi=' + encodeURIComponent(artifactIri) + '&isforce=true';
        if (typeof versionIri !== "undefined") {
            podd.debug(' of artifact (' + versionIri + ').');
            // set query parameters in the URI as setting them under data
            // failed, mostly leading to a 415 error
            requestUrl = requestUrl + '&versionuri=' + encodeURIComponent(versionIri);
        }
        if (typeof objectUri !== 'undefined') {
            podd.debug(' on object (' + objectUri + ').');
            requestUrl = requestUrl + '&objectUri=' + encodeURIComponent(objectUri);
        }
    }
    podd.debug('[updatePoddObject] Request (POST):  ' + requestUrl);

    $.ajax({
        url : requestUrl,
        type : 'POST',
        data : modifiedTriples,
        contentType : 'application/rdf+json', // what we're sending
        beforeSend : function(xhr) {
            xhr.setRequestHeader("Accept", "application/rdf+json");
        },
        success : function(resultData, status, xhr) {
            podd.debug('[updatePoddObject] ### SUCCESS ### ' + resultData);
            // podd.debug('[updatePoddObject] ' + xhr.responseText);
            var message = '<div>Successfully edited artifact.<pre>' + xhr.responseText + '</pre></div>';
            podd.updateErrorMessageList(message);

            // The results of an update query are minimal
            var tempDatabank = podd.newDatabank();
            tempDatabank.load(resultData);
            var artifactId = podd.getOntologyID(tempDatabank);
            // Reset the artifact and version URIs based on what came back
            podd.artifactIri = artifactId[0].artifactIri;
            podd.versionIri = artifactId[0].versionIri;
            // Also setup the parent URI and object URI as they may be empty
            // before this point or may have changed as part of the update
            // FIXME: May not always want to do this
            podd.parentUri = artifactId[0].parentUri;
            podd.objectUri = artifactId[0].objectUri;

            // After the update is complete we try to fetch the complete content
            // before calling updateCallback again, to make sure that all of the
            // temporary URIs in nextArtifactDatabank are replaced with their
            // PURL versions
            podd.getArtifact(podd.artifactIri, nextSchemaDatabank, nextArtifactDatabank, updateCallback);
        },
        error : function(xhr, status, error) {
            podd.debug('[updatePoddObject] $$$ ERROR $$$ ' + error);
            podd.debug(xhr.statusText);
            var message = '<div>Failed to store artifact.<pre>' + xhr.responseText + '</pre></div>';
            podd.updateErrorMessageList(message);
        }
    });
};

/**
 * Parse the RDF received from the server and create a JSON array.
 */
podd.parseSearchResults = function(/* string */searchURL, /* rdf/json */data) {
    // podd.debug("Parsing search results");
    var nextDatabank = $.rdf.databank();

    var rdfSearchResults = nextDatabank.load(data);

    // podd.debug("About to create query");
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

/**
 * Parse the given temporary Databank and extract the artifact IRI and version
 * IRI of the ontology/artifact contained within.
 * 
 * Also extracts and updates the parent URI and the current Object URI if they
 * are temporary URIs or unknown.
 */
podd.getOntologyID = function(nextDatabank) {

    var myQuery = $.rdf({
        databank : nextDatabank
    }).where('?artifactIri owl:versionIRI ?versionIri');
    var bindings = myQuery.select();

    var nodeChildren = [];
    $.each(bindings,
            function(index, value) {
                var nextChild = {};
                nextChild.artifactIri = value.artifactIri.value;
                nextChild.versionIri = value.versionIri.value;

                // If the current object URI is empty and the parent URI is
                // empty, then we bootstrap the current object URI based on
                // artifactHasTopObject as it is a top object
                if (typeof podd.parentUri === 'undefined'
                        && podd.getCurrentObjectUri().lastIndexOf('<urn:temp:uuid:', 0) === 0) {
                    var myQuery = $.rdf({
                        databank : nextDatabank
                    }).where('?artifactIri poddBase:artifactHasTopObject ?topObject');
                    var innerBindings = myQuery.select();

                    $.each(innerBindings, function(index, value) {
                        nextChild.parentUri = value.topObject.value;
                        nextChild.objectUri = value.topObject.value;
                    });

                    if (nodeChildren.length > 1) {
                        podd.debug('[getVersion] ERROR - More than 1 artifact top object statement found!!!');
                        podd.debug(bindings);
                    }
                }
                else {
                    // NOTE: The results must contain only one triple linking
                    // the known parent URI to the current object for this to
                    // work.
                    var myQuery = $.rdf({
                        databank : nextDatabank
                    }).where('<' + podd.parentUri + '> ?property ?currentObject').where(
                            '?currentObject rdf:type <' + podd.objectTypeUri + '> ');
                    var innerBindings = myQuery.select();

                    $.each(innerBindings, function(index, value) {
                        nextChild.objectUri = value.currentObject.value;
                    });

                    if (nodeChildren.length > 1) {
                        podd.debug('[getVersion] ERROR - More than 1 artifact top object statement found!!!');
                        podd.debug(bindings);
                    }
                }
                nodeChildren.push(nextChild);
            });

    if (nodeChildren.length > 1) {
        podd.debug('[getVersion] ERROR - More than 1 version IRI statement found!!!');
        podd.debug(bindings);
    }

    return nodeChildren;
};

/**
 * DEBUG-ONLY : Could be more generic, but right now only used for debugging.
 * 
 * Parse the given Databank and extract the rdfs:label of the top object of the
 * artifact contained within contained within.
 */
podd.getProjectTitle = function(nextDatabank) {
    podd.debug("[getProjectTitle] start");

    var myQuery = $.rdf({
        databank : nextDatabank
    }).where('?artifact poddBase:artifactHasTopObject ?topObject').where('?topObject rdfs:label ?projectTitle');
    var bindings = myQuery.select();

    var nodeChildren = [];
    $.each(bindings, function(index, value) {
        var nextChild = {};
        nextChild.value = value.projectTitle.value;

        nodeChildren.push(nextChild);
    });

    if (nodeChildren.length > 1) {
        podd.debug('[getProjectTitle] ERROR - More than 1 Project Title found!!!');
    }

    return nodeChildren[0];
};

/**
 * Add autocompleteHandlers
 * 
 * @param autoComplete
 *            object to have auto completion
 * @param hiddenValueElement
 *            a hidden element where the URI value of the auto completion object
 *            is to be saved
 * @param nextArtifactDatabank
 *            databank
 * @param searchTypes
 * @param artifactUri
 * @param isNew
 *            boolean
 */
podd.addAutoCompleteHandler = function(
/* object */autoComplete,
/* object */hiddenValueElement,
/* object */nextArtifactDatabank,
/* object array */searchTypes,
/* object */artifactUri,
/* boolean */isNew) {

	autoComplete.autocomplete({
        delay : 500, // milliseconds
        minLength : 2, // min length to trigger
        
        source : function(request, callbackFunction) {
            request.searchTypes = searchTypes;
            request.artifactUri = artifactUri;
            podd.searchOntologyService(request, callbackFunction);
        },

        focus : function(event, ui) {
            // prevent ui.item.value from appearing in the textbox
            $(this).val(ui.item.label);
            return false;
        },

        select : function(event, ui) {
            podd.debug('Option selected "' + ui.item.label + '" with value "' + ui.item.value + '".');
            hiddenValueElement.val(ui.item.value);
            $(this).val(ui.item.label);
            // $('#message1').html('Selected : ' + ui.item.value);
            return false;
        },

        blur : function(event, ui) {
            podd.debug("autocomplete blur event");
            podd.debug(event);
            podd.debug(ui);
            var objectUri = podd.getCurrentObjectUri();

            var attributes = [];
            var nextAttribute = {};
            nextAttribute.isNew = isNew;
            nextAttribute.objectUri = objectUri;
            nextAttribute.property = '<' + $(this).attr('property') + '>';
            nextAttribute.newValue = '<' + hiddenValueElement.val() + '>';
            attributes.push(nextAttribute);

            podd.debug('Add new autocomplete property: <' + nextAttribute.property + '> <' + nextAttribute.newValue
                    + '>');

            podd.updateDatabank(attributes, nextArtifactDatabank);
            // NOTE: Cannot call update to the server after each edit, as some
            // fields may have invalid values at this point.
        }
    });
};

/**
 * On leaving a text field (short/long), check if the contents of the field have
 * changed and if so, request the artifact databank to be updated.
 * 
 * @param textField
 *            reference to the text field that has been 'blurred'
 * @param propertyUri
 *            property/predicate representing this field
 * @param originalValue
 *            the original value that is recorded against this field. can be 'undefined'
 * @param nextArtifactDatabank
 *            databank containing artifact triples
 * @param isNew
 *            boolean indicating whether this field did not previously have a
 *            value
 * 
 */
podd.addTextFieldBlurHandler = function(/* object */textField, /* object */
		propertyUri, /* object */originalValue, /* object */propertyType, /* object */
		nextArtifactDatabank, /* boolean */isNew) {
	
    var nextOriginalValue = '' + originalValue;

    textField.blur(function(event) {
        podd.debug("text field blur event");

        var objectUri = podd.getCurrentObjectUri();

        var changesets = [];

        var newValue = '' + $(this).val();

        var propertyDatatype = $(this).attr('datatype');
        
        if (newValue !== nextOriginalValue) {
            var nextChangeset = {};
            nextChangeset.isNew = isNew;
            nextChangeset.objectUri = objectUri;
            nextChangeset.newTriples = [];
            nextChangeset.oldTriples = [];

            // add old triple ONLY if there originally was a value
            if (typeof originalValue !== 'undefined') {
            	nextChangeset.oldTriples.push(podd.buildTriple(objectUri, propertyUri, nextOriginalValue, propertyType, propertyDatatype));
            }
            nextChangeset.newTriples.push(podd.buildTriple(objectUri, propertyUri, newValue, propertyType, propertyDatatype));
            
            changesets.push(nextChangeset);

            podd.debug('Update property : ' + propertyUri + ' from ' + nextOriginalValue + ' to ' + newValue
                    + ' (isNew=' + isNew + ')');

            podd.updateDatabank(changesets, nextArtifactDatabank);

            // Unbind this handler and create a new one with the new value as
            // the original value
            $(this).unbind("blur");
            // NOTE: isNew is always false after the first time through this
            // method with a non-empty/non-default value
            podd.addTextFieldBlurHandler(textField, propertyUri, newValue, propertyType, nextArtifactDatabank, false);
        }
        else {
            podd.debug("No change on blur for value for property=" + propertyUri + " original=" + nextOriginalValue
                    + " newValue=" + newValue);
        }
        // NOTE: Cannot call update to the server after each edit, as some
        // fields may have incomplete/invalid values at this point.
    });
};

/**
 * Build an RDF triple from the given subject, property and object.
 * 
 * @param subjectUri
 * @param propertyUri
 * @param objectValue
 * @param propertyType
 *            Identifies the type of this property (e.g. object property, data
 *            property)
 * @param objectDatatype
 *            Specifies the datatype of the object
 * @return The constructed triple
 */
podd.buildTriple = function(subjectUri, propertyUri, objectValue, propertyType,
		objectDatatype) {

	podd.debug('buildTriple(' + subjectUri + ', ' + propertyUri + ', '
			+ objectValue + ' [' + objectDatatype + ']');

	var objectPart;

	if (typeof objectDatatype !== 'undefined') {

		// figure out if the object is a Resource or a Literal
		if (typeof propertyType !== 'undefined'
				&& propertyType.toString() === OBJECT_PROPERTY) {

			objectPart = $.rdf.resource('<' + objectValue + '>');
		} else {

			objectPart = $.rdf.literal(objectValue, {
				datatype : objectDatatype
			});
		}
	} else {
		objectPart = $.rdf.literal(objectValue);
	}

	return $.rdf.triple(subjectUri, $.rdf.resource(propertyUri), objectPart);
};
