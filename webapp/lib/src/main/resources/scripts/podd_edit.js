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
// constant not required for cardinality zero or one.

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
var OWL_NAMED_INDIVIDUAL = 'http://www.w3.org/2002/07/owl#NamedIndividual';
var XSD_DATETIME = 'http://www.w3.org/2001/XMLSchema#dateTime';

var DETAILS_LIST_Selector = '#details ol';


var PODD_CREATED_AT = '<http://purl.org/podd/ns/poddBase#createdAt>';
var PODD_LAST_MODIFIED = '<http://purl.org/podd/ns/poddBase#lastModified>';
var DUMMY_Datetime = '1970-01-01T00:00:00';
// --------------------------------

/**
 * Set this to have multiple 'searchtypes' query parameters sent to the
 * SearchOntologyService
 */
jQuery.ajaxSettings.traditional = true;

/**
 * @memberOf podd
 * 
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
 * @memberOf podd
 * 
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
    
    // add createdAt statement with default value
	nextDatabank.add(podd.buildTriple(objectUri, PODD_CREATED_AT,
			DUMMY_Datetime, DATATYPE_PROPERTY, XSD_DATETIME));
};

/**
 * @memberOf podd
 * 
 * Add triples to the given databank to initialise a new non-TopObject with the
 * given relationship to an existing parent.
 * 
 */
podd.initialiseNewObject = function(nextDatabank, artifactUri, objectUri, parentUri, parentPredicateUri) {
	
	podd.debug('Trying to build triple out of: ' + parentUri + ' ' + parentPredicateUri + ' and ' + objectUri);

    nextDatabank.add('<' + parentUri + '> <' + parentPredicateUri + '> ' + objectUri);
    
	// add createdAt statement with default value
	nextDatabank.add(podd.buildTriple(objectUri, PODD_CREATED_AT,
			DUMMY_Datetime, DATATYPE_PROPERTY, XSD_DATETIME));
};

/**
 * Resets the "lastModifiedAt" statement for the current object to the default
 * value.
 * 
 * @param objectUri
 *            The current object
 * @param nextDatabank
 *            Contains statements of current object
 */
podd.resetLastModifiedAt = function(objectUri, nextDatabank) {
	// delete lastModified statement if it exists
	podd.deleteTriples(nextDatabank, objectUri, PODD_LAST_MODIFIED);

	// add lastModified statement with dummy value
	nextDatabank.add(podd.buildTriple(objectUri, PODD_LAST_MODIFIED,
			DUMMY_Datetime, DATATYPE_PROPERTY, XSD_DATETIME));
}

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
 * Removes all triples in the given databank.
 */
podd.deleteAllTriples = function(nextDatabank) {
	var size = nextDatabank.size();
    $.rdf({
        databank : nextDatabank
    }).where('?subject ?property ?object').sources().each(function(index, tripleArray) {
        nextDatabank.remove(tripleArray[0]);
    });
    podd.debug('[deleteAllTriples] Cleaned databank by deleting ' + size + ' triples.')
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

/**
 * PODD specific URI encoding function where a given string is first trimmed of
 * surrounding angle brackets ('<' , '>') if they are present and then URI
 * encoded.
 */
podd.uriEncode = function(input) {

	if (typeof input === 'undefined') {
		return input;
	}

	var trimmedInput = $.trim(input);

	var len = trimmedInput.length;

	if ((len > 2) && (trimmedInput.substring(0, 1) === '<')
			&& (trimmedInput.substring(len - 1, len) === '>')) {

		trimmedInput = trimmedInput.substring(1, len - 1);
	}

	return encodeURIComponent(trimmedInput);
};

/**
 * Display a message on leaving text field
 * 
 * This is mostly used for DEBUG.
 */
podd.updateErrorMessageList = function(theMessage) {
    var li = $("<li>");
    li.addClass("errorMsg");
    li.html(theMessage);
    $("#errorMsgList").append(li);
};

/**
 * @memberOf podd
 * 
 * Clean all the error messages
 */
podd.emptyErrorMessages = function() {
	$("#errorMsgList").empty();
}

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
 * @param artifactUri -
 *            {string} The current artifact's URI. Maybe "undefined" if adding a
 *            new artifact.
 * @param objectType -
 *            {string} Type of Object to be added (e.g. Project, Publication)
 * @param successCallback -
 *            {function} where to send the results
 * @param nextSchemaDatabank -
 *            {databank} Databank where retrieved metadata is to be stored.
 * @param nextArtifactDatabank -
 *            {databank} Databank where artifact's triples are stored.
 * @memberOf podd
 */
podd.getObjectTypeMetadata = function(artifactUri, objectType, successCallback, nextSchemaDatabank,
		nextArtifactDatabank) {

    var requestUrl = podd.baseUrl + '/metadata';

    podd.debug('[getObjectTypeMetadata] Request (GET) (' + objectType + '): ' + requestUrl);

    $.ajax({
        url : requestUrl,
        type : 'GET',
        data : {
            objecttypeuri : objectType
        },
        dataType : 'json', // what is expected back
        success : function(resultData, status, xhr) {
            podd.debug('[getObjectTypeMetadata] ### SUCCESS ### ');
			podd.debug(resultData);

            nextSchemaDatabank.load(resultData);

            podd.debug('[getObjectTypeMetadata] Schema Databank size = ' + nextSchemaDatabank.size());

            successCallback(artifactUri, objectType, nextSchemaDatabank, nextArtifactDatabank);
        },
        error : function(xhr, status, error) {
            podd.debug('[getObjectTypeMetadata] $$$ ERROR $$$ ' + error);
            podd.debug(xhr.statusText);
        }
    });
};

/**
 * Continue updating interface after schemaDatabank is populated with metadata.
 * If an existing artifact is being updated, populates the artifactDatabank.
 * 
 * @param artifactUri -
 *            {string} The current artifact's URI. Maybe "undefined" if adding a
 *            new artifact.
 * @param objectType -
 *            {string} The type of Object to be added (e.g. Project,
 *            Publication)
 * @param nextSchemaDatabank -
 *            {databank} Databank where metadata is stored.
 * @param nextArtifactDatabank -
 *            {databank} Databank where artifact's triples are stored.
 * @memberOf podd
 */
podd.callbackFromGetMetadata = function(artifactUri, objectType, nextSchemaDatabank, nextArtifactDatabank) {
	
	podd.debug('[callbackFromGetMetadata] objectType=<' +objectType + '>, artifactUri=<' + artifactUri + '>');
	
	// TODO: are these two conditions sufficient to ensure it is a new artifact?
	if (typeof artifactUri !== 'undefined' && artifactUri !== 'undefined') {
		podd.debug('[callbackFromGetMetadata] artifact exists. retrieve it before update interface.');
		podd.getArtifact(artifactUri, nextSchemaDatabank, nextArtifactDatabank, false, podd.updateInterface, objectType);
	} else {
		podd.debug('[callbackFromGetMetadata] new artifact. invoke update interface');
		podd.updateInterface(objectType, nextSchemaDatabank, nextArtifactDatabank);
	}
}

/**
 * Callback function that redirects to the artifact when submitPoddObjectUpdate
 * is successful.
 * 
 * podd.artifactIri and podd.objectUri are used to determine the artifact/object
 * to redirect to. The incoming parameters are not used.
 */
podd.redirectToGetArtifact = function(objectType, nextSchemaDatabank, nextArtifactDatabank) {
	
	var redirectUri = podd.baseUrl + '/artifact/base?artifacturi=' + encodeURIComponent(podd.artifactIri);
	
	//TODO: podd.objectUri should be set before this redirect is called
	if (typeof podd.objectUri !== 'undefined' && podd.objectUri !== 'undefined'
			&& podd.objectUri.lastIndexOf('<urn:temp:uuid:', 0) !== 0) {

		podd.debug('[Redirect] to object: ' + podd.objectUri);
		redirectUri = redirectUri + '&objecturi=' + encodeURIComponent(podd.objectUri);
	}
	window.location.href = redirectUri;
};

/**
 * @memberOf podd
 * 
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

        
        // Avoid duplicates, which are occurring due to multiple ways of specifying ranges/etc., in OWL
        if($.inArray(nextChild.propertyUri, propertyUris) === -1)
       	{
            propertyUris.push(nextChild.propertyUri);
            propertyList.push(nextChild);
//                podd.debug("[" + nextChild.weight + "] propertyUri=<" + nextChild.propertyUri + "> label=\""
//                        + nextChild.propertyLabel + "\" displayType=<" + nextChild.displayType + "> card=<"
//                        + nextChild.cardinality + ">");
            
            //add cardinalities for use in validation at submit time. could be undefined
            var entry = {};
            entry.cardinality = nextChild.cardinality;
            entry.propertyUri = nextChild.propertyUri;
            entry.propertyLabel = nextChild.propertyLabel;
            podd.cardinalityList.push(entry);
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

		// If there are values for the property in the artifact databank, add them as an
        // array so that they can be displayed instead of showing a single new empty field
        if (nextArtifactBindings.length > 0) {
        	
        	var valuesArray = [];
            $.each(nextArtifactBindings, function(nextArtifactIndex, nextArtifactValue) {
            	var oneValue = {};
                // found existing value for property
                oneValue.displayValue = nextArtifactValue.propertyValue.value;
                podd.debug("Property <" + value.propertyUri + "> has value: " + oneValue.displayValue);

                // for URIs populate the valueUri property with the value so we
                // have the option to put a human readable label in displayValue
                if (nextArtifactValue.propertyValue.type === 'uri') {
                	oneValue.valueUri = nextArtifactValue.propertyValue.value;
                	
                    // look for a label in the schema databank
                    var labelQuery = $.rdf({
                        databank : nextSchemaDatabank
                    })
                    // 
                    .where('<' + oneValue.valueUri + '> <http://www.w3.org/2000/01/rdf-schema#label> ?uLabel');
                    var labelBindings = labelQuery.select();

                    $.each(labelBindings, function(index, nextBinding) {
                    	podd.debug('Found <' + oneValue.valueUri + '> has label: ' + nextBinding.uLabel.value);
                    	oneValue.displayValue = nextBinding.uLabel.value;
                    });
                }
                valuesArray.push(oneValue);
                
            });
            value.valuesArray = valuesArray;
            $(DETAILS_LIST_Selector).append(podd.createEditField(value, nextSchemaDatabank, nextArtifactDatabank, false));
            
            //TODO - invoke createEditField with this array of values rather than just one
            podd.debug('found ' + valuesArray.length + ' values for property ' + value.propertyUri);
        }
        else {
            podd.debug("Property <" + value.propertyUri + "> has NO value");

            var oneValue = {};
            oneValue.displayValue; //undefined to indicate there is NO value
            oneValue.valueUri = '';

            value.valuesArray = [];
            value.valuesArray.push(oneValue);
            
//            nextChild.displayValue; //undefined to indicate there is NO value
//            nextChild.valueUri = '';

            $(DETAILS_LIST_Selector).append(podd.createEditField(value, nextSchemaDatabank, nextArtifactDatabank, true));
        }
    });
};

/**
 * @memberOf podd
 * 
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
 * @return a list item (i.e. &lt;li&gt;) containing the HTML field
 */
podd.createEditField = function(nextField, nextSchemaDatabank, nextArtifactDatabank, isNew) {
	// podd.debug('[' + nextField.weight + '] <' + nextField.propertyUri
	// + '> "' + nextField.propertyLabel + '" <' +
	// nextField.displayType + '> <' + nextField.cardinality + '>');

	// <li> element to which the whole field is to be attached
    var li = $("<li>");

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

    // a list which will be useful if this field supports multi-values;
    var subList = $('<ul>');


    if (typeof nextField.valuesArray !== 'undefined' && nextField.valuesArray.length > 0) {
    	
    	$.each(nextField.valuesArray, function(index, aValue) {
    	    var li2 = $("<li>");
		    		
		    if (nextField.displayType == DISPLAY_LongText) {
		        var input = podd.addFieldTextArea(nextField, aValue, 30, 2, nextSchemaDatabank);
		        
		        // TODO: refactor so that there is one addHandler() inside which
				// blur handler is invoked for both the original and the cloned fields
				podd.addTextFieldBlurHandler(input, undefined, nextField.propertyUri, aValue.displayValue,
						nextField.propertyType, nextArtifactDatabank, isNew);
		        
		        if (index === 0) {
		        	//clone handler should only be added once
		        	podd.addCloneHandler(subList, link, input, nextField, nextArtifactDatabank);
		        }
		        
		        li2.append(input);
		    }
		    else if (nextField.displayType == DISPLAY_ShortText) {
		        var input = podd.addFieldInputText(nextField, aValue, 'text', nextSchemaDatabank);
		
		        // TODO: add support for date/time types other than xsd:date
		        if (typeof nextField.propertyRange !== 'undefined' &&
		        		nextField.propertyRange.toString() === 'http://www.w3.org/2001/XMLSchema#date') {
		
		        	// prevent user bypassing the datepicker widget and typing values in
		        	input.attr('readonly', 'readonly');
		        	input.attr('style', 'background:white');
		        	
		        	input.datepicker({
		        			dateFormat : "yy-mm-dd",
		        			changeYear : true,
		        			yearRange : "-5:+10",
		        			onSelect : function() {
		        				// blur handler does not work with datepicker as the blur event gets fired before
		        				// the selected value is set.
		        		        podd.handleDatePickerFieldChange(input, nextField.propertyUri, aValue.displayValue,
									nextField.propertyType, nextArtifactDatabank, isNew);
		        			} 
		        		});
		        } else {
				    podd.addTextFieldBlurHandler(input, undefined, nextField.propertyUri, aValue.displayValue,
				    	nextField.propertyType, nextArtifactDatabank, isNew);
		        }
		
		        li2.append(input);
		    }
		    else if (nextField.displayType == DISPLAY_DropDown) {
		        var input = podd.addFieldDropDownListNonAutoComplete(nextField, aValue, nextSchemaDatabank, isNew);
		        podd.addTextFieldBlurHandler(input, undefined, nextField.propertyUri, aValue.displayValue, 
		        		nextField.propertyType, nextArtifactDatabank, isNew);
		        li2.append(input);
		    }
		    else if (nextField.displayType == DISPLAY_CheckBox) {
		        var input = podd.addFieldInputText(nextField, aValue, 'checkbox', nextSchemaDatabank);
		        var label = '<label>' + aValue.displayValue + '</label>';
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
				var searchTypes = [ ];
				if (typeof nextField.propertyRange != 'undefined'
						&& nextField.propertyRange != 'Not Found') {
					searchTypes.push(nextField.propertyRange);
				} else {
					podd.debug('WARNING: Could not find search types for property: ' + nextField.propertyUri);
					searchTypes.push(OWL_NAMED_INDIVIDUAL); // attempt to limit the damage
				}
		
				// - set artifact URI
				var artifactUri;
				if (typeof podd.artifactIri != 'undefined'
						&& podd.artifactIri != 'undefined') {
					artifactUri = podd.artifactIri;
				}
		
				var input = podd.addFieldInputText(nextField, aValue, 'text',
						nextSchemaDatabank);
				var hiddenValueElement = podd.addFieldInputText(nextField, aValue, 'hidden',
						nextSchemaDatabank);
				podd.addAutoCompleteHandler(input, hiddenValueElement,
						nextArtifactDatabank, searchTypes, artifactUri, isNew);
				podd.addTextFieldBlurHandler(input, hiddenValueElement, nextField.propertyUri,
						aValue.valueUri, nextField.propertyType,
						nextArtifactDatabank, isNew);
		
				li2.append(input);
				li2.append(hiddenValueElement);
		    }
		    else { // default
		        podd.updateErrorMessageList("TODO: Support property : " + nextField.propertyUri + " (" + aValue.displayValue
		                + ")");
		    }
		    subList.append(li2);
		
    	}); //end $.each()
    }
    
    li.append(subList);
    return li;
};

/**
 * Add method which will clone the edit field on clicking of the (+) link. 
 * 
 * @param parentList
 * 			{object} an HTML element to which the cloned field should be appended
 * @param link
 * 			{object} an HTML anchor that gets clicked to trigger cloning
 * @param input
 * 			{object} The element to be cloned
 * @param nextField
 * 			{object} Contains details about the current field
 * @param nextArtifactDatabank
 * 			{databank} Contains artifact triples
 */
podd.addCloneHandler = function(parentList, link, input, nextField, nextArtifactDatabank) {
	
	var hiddenValueElement = undefined;
	
    if (typeof link !== 'undefined') {
    	
        link.click(function() {
        	podd.debug('Clicked (+) button to Clone');
            var clonedField = input.clone(false);
            podd.addTextFieldBlurHandler(clonedField, hiddenValueElement, nextField.propertyUri, undefined, nextField.propertyType, 
                    nextArtifactDatabank, true);

            var li = $("<li>");
            li.append(clonedField);
            parentList.append(li);
        });
    }
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
podd.addFieldInputText = function(nextField, nextFieldValue, inputType, nextDatabank) {

    // FIXME: id is useless here as it doesn't preserve the URI, and it will
    // never be unique for more than one element
    // var idString = 'id_' + nextField.propertyUri;
    // idString = idString.replace("#", "_");

    var input = $('<input>', {
        // id : idString,
        name : 'name_' + inputType + '_' + nextField.propertyLabel,
        type : inputType
    });

    if (inputType === 'checkbox' || inputType === 'hidden') {
        input.val(nextFieldValue.valueUri);
    } else {
    	input.val(nextFieldValue.displayValue);
    }

    input.attr('datatype', nextField.propertyRange);
    
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
podd.addFieldTextArea = function(nextField, nextFieldValue, noOfColumns, noOfRows, nextSchemaDatabank) {

    var textarea = $('<textarea>', {
        name : 'name_' + nextField.propertyLabel,
        cols : noOfColumns,
        rows : noOfRows
    });

    textarea.val(nextFieldValue.displayValue);
    textarea.attr('datatype', nextField.propertyRange);

    return textarea;
};


/**
 * Construct an HTML drop-down list for the given field without using
 * autocomplete or search services.
 * 
 * In these cases, the relevant options for this property must have been loaded
 * into the schema databank prior to calling this method.
 */
podd.addFieldDropDownListNonAutoComplete = function(nextField, nextFieldValue, nextSchemaDatabank, isNew) {
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

    // see if restriction exists with owl:onClass
    if (bindings.length === 0) {
        myQuery = $.rdf({
            databank : nextSchemaDatabank
        })
        // Find all display values for this property
        .where('?restriction owl:onProperty <' + nextField.propertyUri + '> ')
        //
        .where('?restriction owl:onClass ?class')
        // 
        .where('?pValue rdf:type ?class')
        //
        .optional('?pValue rdfs:label ?pDisplayValue');
        bindings = myQuery.select();
    }
    
    $.each(bindings, function(index, value) {

        var optionValue = value.pValue.value;

        var optionDisplayValue = value.pValue.value;
        if (typeof value.pDisplayValue != 'undefined') {
            optionDisplayValue = value.pDisplayValue.value;
        }

        var selectedVal = false;
        if (nextFieldValue.valueUri == optionValue) {
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

/**
 * @memberOf podd
 * 
 * This method identifies object URIs in the artifact databank that do not have
 * labels, retrieves them from the PODD service and stores them in the schema
 * databank (labels are stored in the schma databank as they are not
 * part of the artifact).
 * 
 * @param artifactUri -
 *            {string} URI for the current artifact
 * @param nextSchemaDatabank -
 *            {databank} Databank where metadata is stored
 * @param nextArtifactDatabank -
 *            {databank} Databank where artifact's triples are stored
 * @param callbackFunction -
 *            {function} Function to be invoked on completion of this request
 * @param callbackParam -
 *            {object} Parameter to be used when invoking the callback
 */
podd.loadMissingArtifactLabels = function(artifactUri, nextSchemaDatabank,
		nextArtifactDatabank, callbackFunction, callbackParam) {
    podd.debug('[loadArtifactLabels] started')
    
    // go through artifact databank and identify URIs without labels
    var uriList = [];
    var tempDatabank = podd.newDatabank();
    
    var myQuery = $.rdf({
        databank : nextArtifactDatabank
    })
    //for all statements
    .where('?subject ?predicate ?object')
    //optionally, if object has a label
    .optional('?object <http://www.w3.org/2000/01/rdf-schema#label> ?objectLabel')
    ;
    var bindings = myQuery.select();

    $.each(bindings, function(index, value) {
        if (value.object.type === 'uri' && value.objectLabel === undefined) {
            //podd.debug('[loadArtifactLabels] missing label for = ' + value.object.value)
            uriList.push(value.object.value);
            tempDatabank.add('<' + value.object.value + '> <http://www.w3.org/2000/01/rdf-schema#label> "?blank"');
        }
    });
    
    // retrieve labels for these and populate the schemadatabank with them
    var triplesToSendInJson = $.toJSON(tempDatabank.dump({
        format : 'application/json'
    }));    
    var requestUrl = podd.baseUrl + '/search?artifacturi=' + podd.uriEncode(artifactUri); 
    
    $.ajax({
        url : requestUrl,
        type : 'POST',
        
        data : {
            artifacturi : artifactUri
        },
        data : triplesToSendInJson,
        
        contentType : 'application/rdf+json', // what we're sending
        beforeSend : function(xhr) {
            xhr.setRequestHeader("Accept", "application/rdf+json");
        },
        success : function(resultData, status, xhr) {
            // add the results to schemadatabank
        	//podd.debug('[getUriLabel] response is = ' + resultData.toString());
        	var sizeBefore = nextSchemaDatabank.size();
            nextSchemaDatabank.load(resultData);
            podd.debug('[loadArtifactLabels] ### SUCCESS ### databank size changed from ' + sizeBefore + ' to ' + nextSchemaDatabank.size());
            
            // callback
            // The following may update the interface, redirect the user to
            // another page, or so anything it likes really
        	podd.debug('[loadArtifactLabels] invoke callback with 4 parameters')
        	callbackFunction(podd.objectTypeUri, nextSchemaDatabank, nextArtifactDatabank, callbackParam);
        },
        error : function(xhr, status, error) {
            podd.debug(status + '[loadArtifactLabels] $$$ ERROR $$$ ' + error);
            // podd.debug(xhr.statusText);
        }
    });
};

/**
 * Retrieve the current version of an artifact and populate the artifact
 * databank.
 * 
 * This method also sets podd.artifactIri and podd.versionIri to match the
 * retrieved artifact.
 * 
 * @param artifactUri
 *            The artifact to be retrieved.
 * @param nextSchemaDatabank
 *            To be passed into the callback method.
 * @param nextArtifactDatabank
 *            Databank to be populated with artifact triples.
 * @param cleanArtifactDatabank
 *            If true, the artifact databank is wiped clean before it is
 *            populated with newly retrieved content.
 * @param updateDisplayCallbackFunction
 *            The callback method to invoke on successful retrieval of the
 *            artifact.
 * @param callbackParam
 *            The 4th parameter of the callback method. Could be undefined.
 */
podd.getArtifact = function(artifactUri, nextSchemaDatabank,
		nextArtifactDatabank, cleanArtifactDatabank,
		updateDisplayCallbackFunction, callbackParam) {
    var requestUrl = podd.baseUrl + '/artifact/base?artifacturi=' + encodeURIComponent(artifactUri);

    podd.debug('[getArtifact] Request to: ' + requestUrl);
    $.ajax({
        url : requestUrl,
        type : 'GET',
        // dataType : 'application/rdf+xml', // what is expected back
        success : function(resultData, status, xhr) {
        	if (cleanArtifactDatabank) {
        		podd.deleteAllTriples(nextArtifactDatabank);
        	}
            nextArtifactDatabank.load(resultData);
            podd.debug('[getArtifact] ### SUCCESS ### loaded databank with size ' + nextArtifactDatabank.size());

            // update variables and page contents with retrieved artifact info
            var artifactId = podd.getOntologyID(nextArtifactDatabank);
            podd.artifactIri = artifactId[0].artifactIri;
            podd.versionIri = artifactId[0].versionIri;

            podd.updateErrorMessageList('<i>Successfully retrieved artifact version: ' + podd.versionIri + '</i><br>');

            podd.loadMissingArtifactLabels(artifactUri, nextSchemaDatabank, nextArtifactDatabank, updateDisplayCallbackFunction, callbackParam);
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

	podd.resetLastModifiedAt(objectUri, nextArtifactDatabank);
	
	podd.emptyErrorMessages();

	if (!podd.isValidArtifact(nextArtifactDatabank)) {
		return; // cannot continue submission
	}
	
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
        requestUrl = requestUrl + '?artifacturi=' + podd.uriEncode(artifactIri) + '&isforce=true';
        if (typeof versionIri !== "undefined") {
            podd.debug(' of artifact (' + versionIri + ').');
            // set query parameters in the URI as setting them under data
            // failed, mostly leading to a 415 error
            requestUrl = requestUrl + '&versionuri=' + podd.uriEncode(versionIri);
        }
        if (typeof objectUri !== 'undefined') {
            podd.debug(' on object (' + objectUri + ').');
            requestUrl = requestUrl + '&objectUri=' + podd.uriEncode(objectUri);
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
            
            podd.updateObjectUriWithPurl(tempDatabank);
            // Do we need to worry about parent URI?
            
            // After the update is complete we try to fetch the complete content
            // before calling updateCallback again, to make sure that all of the
            // temporary URIs in nextArtifactDatabank are replaced with their
            // PURL versions
            var emptyParam;
            podd.getArtifact(podd.artifactIri, nextSchemaDatabank, nextArtifactDatabank, true, updateCallback, emptyParam);
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
 * @param nextDatabank
 *            containing artifact statements
 * @return an array with a single element which contains the Ontology's artifact
 *         IRI and version IRI.
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

                nodeChildren.push(nextChild);
            });

    if (nodeChildren.length > 1) {
        podd.debug('[getVersion] ERROR - More than 1 version IRI statement found!!!');
        podd.debug(bindings);
    }

    return nodeChildren;
};

/**
 * NOTE: This function extracted from podd.getOntologyID() is not being used. Is
 * it useful any more?
 * 
 * Extracts and returns the parent URI and the current Object URI if they are
 * temporary URIs or unknown.
 * 
 * @param nextDatabank
 *            containing artifact statements
 * @return an object containing the parent URI and object URI if they have been
 *         set
 */
podd.getParentAndObjectUri = function(nextDatabank) {
    var nextChild = {};

    // If the current object URI is empty and the parent URI is
    // empty, then we bootstrap the current object URI based on
    // artifactHasTopObject as it is a top object
    if (typeof podd.parentUri === 'undefined' && podd.getCurrentObjectUri().lastIndexOf('<urn:temp:uuid:', 0) === 0) {
        var myQuery = $.rdf({
            databank : nextDatabank
        })
        .where('?artifactIri poddBase:artifactHasTopObject ?topObject');
        var innerBindings = myQuery.select();
        // TODO: validate only 1 binding exists
        $.each(innerBindings, function(index, value) {
            nextChild.parentUri = value.topObject.value;
            nextChild.objectUri = value.topObject.value;
        });

    } else {
        // NOTE: The results must contain only one triple linking
        // the known parent URI to the current object for this to
        // work.
        var myQuery = $.rdf({
            databank : nextDatabank
        })
        //
        .where('<' + podd.parentUri + '> ?property ?currentObject')
        //
        .where('?currentObject rdf:type <' + podd.objectTypeUri + '> ');
        
        var innerBindings = myQuery.select();

        $.each(innerBindings, function(index, value) {
            nextChild.objectUri = value.currentObject.value;
        });
    }
    return nextChild;
}

/**
 * If podd.objectUri is a temporary URI or unknown, this function attempts to
 * update to a PURL from data in the given databank.
 * 
 * @param nextDatabank
 *            A databank which may contain temporary URI to PURL mappings
 */
podd.updateObjectUriWithPurl = function(nextDatabank) {
	
	if (podd.getCurrentObjectUri().lastIndexOf('<urn:temp:uuid:', 0) === 0) {
		var myQuery = $.rdf({
			databank : nextDatabank
		}).where(podd.getCurrentObjectUri()	+ ' poddBase:hasPURL ?purl');
		
		var bindings = myQuery.select();
		$.each(bindings, function(index, value) {
			podd.objectUri = value.purl.value.toString();
			podd.debug('[updateObjectUri] podd.objectUri set to: ' + value.purl.value);
		});
	}
}

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
            $(this).val(ui.item.label);
			hiddenValueElement.val(ui.item.value);
            return false;
        },

        blur : function(event, ui) {
        	podd.debug('***ERROR*** - autocomplete blur event should be handled by textFieldBlurHandler');
        }
    });
};

/**
 * On leaving a text field (short/long), check if the contents of the field have
 * changed and if so, request the artifact databank to be updated.
 * 
 * @param textField
 *            reference to the text field that has been 'blurred'
 * @param hiddenValueElement
 * 			  TODO
 * @param propertyUri
 *            property/predicate representing this field
 * @param originalValue
 *            the original value that is recorded against this field. can be 'undefined'
 * @param propertyType
 * 			  TODO
 * @param nextArtifactDatabank
 *            {databank} databank containing artifact triples
 * @param isNew
 *            {boolean} boolean indicating whether this field did not previously have a
 *            value
 * 
 */
podd.addTextFieldBlurHandler = function(textField, hiddenValueElement, propertyUri, originalValue, propertyType,
		nextArtifactDatabank, isNew) {
	
    var nextOriginalValue = '' + originalValue;

    textField.blur(function(event) {
        var newValue = '' + $(this).val();
        podd.debug("[blur] triggered with new value: " + newValue);

        var objectUri = podd.getCurrentObjectUri();

        var changesets = [];

        if (typeof hiddenValueElement !== 'undefined') {
        	newValue = hiddenValueElement.val();
        	podd.debug('[blur] hidden field found with value: ' + newValue);
        }

        var propertyDatatype = $(this).attr('datatype');
        
        if (newValue !== nextOriginalValue) {
            var nextChangeset = {};
            nextChangeset.isNew = isNew;
            nextChangeset.objectUri = objectUri;
            nextChangeset.newTriples = [];
            nextChangeset.oldTriples = [];

            // add old triple ONLY if there originally was a value
            if (nextOriginalValue !== 'undefined') {
            	nextChangeset.oldTriples.push(podd.buildTriple(objectUri, propertyUri, nextOriginalValue, propertyType, propertyDatatype));
            }

            // add a new triple ONLY if the value is non-empty. enables deleting of previous entries. 
            if (newValue !== '') {
            	nextChangeset.newTriples.push(podd.buildTriple(objectUri, propertyUri, newValue, propertyType, propertyDatatype));
            }
            
            changesets.push(nextChangeset);

            podd.debug('Update property : ' + propertyUri + ' from ' + nextOriginalValue + ' to ' + newValue
                    + ' (isNew=' + isNew + ')');

            podd.updateDatabank(changesets, nextArtifactDatabank);

            // Unbind this handler and create a new one with the new value as
            // the original value
            $(this).unbind("blur");
            // NOTE: isNew is always false after the first time through this
            // method with a non-empty/non-default value
            podd.addTextFieldBlurHandler(textField, hiddenValueElement, propertyUri, newValue, propertyType, nextArtifactDatabank, false);
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
 * On change of a datepicker field, check if the contents of the field have
 * changed and if so, request the artifact databank to be updated.
 * 
 * @param textField
 *            {object} reference to the text field that has been 'blurred'
 * @param propertyUri 
 * 			  property/predicate representing this field
 * @param originalValue
 *            the original value that is recorded against this field. can be
 *            'undefined'
 * @param nextArtifactDatabank
 *            {databank} databank containing artifact triples
 * @param isNew
 *            {boolean} boolean indicating whether this field did not previously
 *            have a value
 */
podd.handleDatePickerFieldChange = function(textField, propertyUri, originalValue, propertyType, nextArtifactDatabank,
		isNew) {

	var nextOriginalValue = '' + originalValue;

	var newValue = '' + textField.val();
	podd.debug("[dateField] triggered with new value: " + newValue);

	var objectUri = podd.getCurrentObjectUri();

	var changesets = [];

	var propertyDatatype = textField.attr('datatype');

	if (newValue !== nextOriginalValue) {
		var nextChangeset = {};
		nextChangeset.isNew = isNew;
		nextChangeset.objectUri = objectUri;
		nextChangeset.newTriples = [];
		nextChangeset.oldTriples = [];

		// add old triple ONLY if there originally was a value
		if (nextOriginalValue !== 'undefined') {
			nextChangeset.oldTriples.push(podd.buildTriple(objectUri, propertyUri, nextOriginalValue, propertyType,
					propertyDatatype));
		}
		
        // add a new triple ONLY if the value is non-empty. enables deleting of previous entries. 
		if (newValue !== '') {
			nextChangeset.newTriples.push(podd
					.buildTriple(objectUri, propertyUri, newValue, propertyType, propertyDatatype));
		}
		
		changesets.push(nextChangeset);

		podd.debug('[dateField] Update property : ' + propertyUri + ' from ' + nextOriginalValue + ' to ' + newValue + ' (isNew='
				+ isNew + ')');

		podd.updateDatabank(changesets, nextArtifactDatabank);
	} else {
		podd.debug("[dateField] No change of value for property=" + propertyUri + " original=" + nextOriginalValue
				+ " newValue=" + newValue);
	}
};

/**
 * Retrieve metadata describing possible types of child objects and relationshps
 * for the given parent object type.
 * 
 * @param artifactUri -
 *            The current artifact's URI. Maybe "undefined" if adding a new
 *            artifact.
 * @param objectType -
 *            The type of Object (e.g. Project, Publication)
 * @param successCallback -
 *            where to send the results
 */
podd.getCreateChildMetadata = function(artifactUri, objectType, successCallback) {

    var requestUrl = podd.baseUrl + '/metadata';

    podd.debug('[getCreateChildMetadata] Request (GET) (' + objectType + '): ' + requestUrl);

    $.ajax({
        url : requestUrl,
        type : 'GET',
        data : {
        	artifacturi : artifactUri,
            objecttypeuri : objectType,
            includedndprops : false,
            metadatapolicy : 'containsonly'
        },
        dataType : 'json', // what is expected back
        success : function(resultData, status, xhr) {
            podd.debug('[getCreateChildMetadata] ### SUCCESS ### ');
			podd.debug(resultData);

		    var nextSchemaDatabank = podd.newDatabank();
			
            nextSchemaDatabank.load(resultData);

            successCallback(objectType, nextSchemaDatabank);
        },
        error : function(xhr, status, error) {
            podd.debug('[getCreateChildMetadata] $$$ ERROR $$$ ' + error);
            podd.debug(xhr.statusText);
        }
    });
};

/**
 * Display a Dialog where user can select the relationship to the child object
 * and the type of child object.
 * 
 * @param objectType
 *            The current object's type
 * @param nextSchemaDatabank
 *            Databank populated with necessary metadata
 */
podd.showAddChildDialog = function(objectType, nextSchemaDatabank) {
    podd.debug('[showAddChildDialog] Schema Databank size = ' + nextSchemaDatabank.size());

    var select = $('<select>', {
        name : 'name_child_relationship',
    });

    var defaultOption = $('<option>', {
        value : '',
        text : 'Please Select',
        targetobject : ''
    });
    select.append(defaultOption);
    
    var myQuery = $.rdf({
        databank : nextSchemaDatabank
    })
    // Find all possible child object details for this object type
    .where('<' +objectType + '> rdfs:subClassOf ?myRestriction')
    //
    .where('?myRestriction a owl:Restriction')
    //
    .where('?myRestriction owl:onProperty ?childRelationship')
    // 
    .where('?myRestriction owl:allValuesFrom ?childType')
    //
    .where('?childRelationship rdfs:label ?relationshipLabel')
    //
    .where('?childType rdfs:label ?childTypeLabel')
    ;
    var bindings = myQuery.select();

    var bindingsList = [];
    
    $.each(bindings, function(index, value) {
        var nextChild = {};
        nextChild.weight;
        nextChild.propertyUri = value.childRelationship.value;
        nextChild.propertyLabel = value.relationshipLabel.value;
        nextChild.objectType = value.childType.value;
    	nextChild.objectLabel = value.childTypeLabel.value;
    	
        podd.debug('[showAddChildDialog] child relationship: <' + nextChild.propertyUri + '> "' 
        		+ nextChild.propertyLabel + '"  and child type: ' + nextChild.objectType);
        
        bindingsList.push(nextChild);
    });

    // sort bindings list in ascending order of weight
    bindingsList.sort(function(a, b) {
        var aID = a.weight;
        var bID = b.weight;
        
        if (aID == bID) {
        	// on equal weights sort by property label
        	return (a.propertyLabel > b.propertyLabel) ? 1: -1;
        } else {
        	return (aID - bID);
        }
    });
    
    $.each(bindingsList, function(index, nextChild) {
        var text = nextChild.objectLabel + ' (' + nextChild.propertyLabel + ')'; 
        
        var option = $('<option>', {
            value : nextChild.propertyUri,
            text : text,
            targetobject : nextChild.objectType
        });
        
        select.append(option);
    });
    
    var hiddenChildType = $('<input>', {
    	name : 'name_child_type',
    	type : 'hidden'
    });
    
    var hiddenRelationship = $('<input>', {
    	name : 'name_child_relationship',
    	type : 'hidden'
    });
    
    var continueLink = $('<a>', {
        name : 'name_add_object_link',
        text : 'Continue', 
        class : 'button'
    });

    var div = $('<div>', {
        name : 'add_child',
    });

    podd.addChildObjectHandler(continueLink, select, hiddenChildType, hiddenRelationship);
   
    div.append('<p>Select Type of Child</p>')
    div.append(select);
    div.append('<br><br>');
    div.append(continueLink);
    div.append(hiddenChildType);
    div.append(hiddenRelationship);
    
	var dialog = $("#dialog").dialog({
		autoOpen : false,
		modal: true,
	    dialogClass: "dialog_class",
	    close: function () {
    		div.remove();
  		}  
	});
	dialog.append(div);
	dialog.dialog("open");
    
    podd.debug('[showAddChildDialog] finished');
};

/**
 * Add handlers for the Child Object Drop Down list and the "Continue" link.
 * 
 * @param theLink
 *            The link to click after child object type has been selected
 * @param dropDown
 *            Drop Down list to select the child object type and relationship
 * @param hiddenChildType
 *            Hidden input where the selected child object type is set
 * @param hiddenRelationsip
 *            Hidden input where the selected child relationship is set
 */
podd.addChildObjectHandler = function(theLink, dropDown, hiddenChildType,
		hiddenRelationship) {

	dropDown.change(function(event) {
		var option = $('option:selected', this);

		if (typeof option !== 'undefined') {
			var propertyUri = '' + option.val();
			var targetObjectType = '' + option.attr('targetobject');

			podd.debug('Selected ' + targetObjectType + ' and ' + propertyUri);

			hiddenRelationship.val(propertyUri);
			hiddenChildType.val(targetObjectType);
		} else {
			podd.debug('option was undefined');
		}
	});

	theLink.click(function(event) {
		
		$('#dialog').dialog('close');
		
		var propertyUri = hiddenRelationship.val();
		var targetObjectType = hiddenChildType.val();
		podd.debug('Clicked ' + $(this).attr('name') + ' Relationship = '
				+ propertyUri + ' and object type = ' + targetObjectType);

		var errors = [];
		if (typeof propertyUri === 'undefined' || propertyUri.length === 0) {
			errors.push('<p>A Parent-Child relationship should be selected</p>');
		}
		if (typeof targetObjectType === 'undefined'
				|| targetObjectType.length === 0) {
			errors.push('<p>A child type should be selected</p>');
		}

		if (errors.length > 0) {
			$.each(errors, function(index, value) {
				podd.updateErrorMessageList(value);
			});
		} else {
			var requestUrl = podd.baseUrl + '/artifact/addobject' +
			// artifact
			'?artifacturi=' + podd.uriEncode(podd.getCurrentArtifactIri()) +
			// parent object
			'&parenturi=' + podd.uriEncode(podd.getCurrentObjectUri()) +
			// type of child object
			'&objecttypeuri=' + podd.uriEncode(targetObjectType) +
			// relationship to child object
			'&parentpredicateuri=' + podd.uriEncode(propertyUri);

			// TODO: turn this into an AJAX call which listens for errors and then redirect
			window.location.href = requestUrl;
		}
	});
};

/**
 * @memberOf podd
 * 
 * Checks that the given artifact databank contains values for all mandatory
 * properties. Cardinality information is available from podd.cardinalityList.
 * 
 * @param nextDatabank
 *            {databank} Contains the artifact statements to be validated
 * @return {boolean} Indicating whether the artifact is valid or not
 */
podd.isValidArtifact = function(nextDatabank) {
	var valid = true;
	
	$.each(podd.cardinalityList, function(index, value) {
		
		if (typeof value.cardinality !== 'undefined' && (value.cardinality.toString() === CARD_ExactlyOne 
				|| value.cardinality.toString() === CARD_OneOrMany)) {
			
			 var myQuery = $.rdf({
			        databank : nextDatabank
			    }).where('?someObject <' +value.propertyUri + '> ?someValue');
		    var bindings = myQuery.select();
		    if (bindings.length === 0) {
				podd.updateErrorMessageList('Mandatory property ' + value.propertyLabel + ' is empty.');

				//TODO: display error next to the erroneous
				valid = false;
		    }
		}
	});
	return valid;
};
