/**
 * PODD : Uses jquery-RDF to analyse the current document with respect to the
 * RDFa information it contains and populate the rdfadebug element with the
 * resulting information
 */

// --------------------------------
// invoked when page is "ready"
// --------------------------------
$(document).ready(function() {
    if (typeof console !== "undefined" && console.debug) {
        console.debug('-------------------');
        console.debug('initializing...');
        console.debug('-------------------');
    }
    podd.artifactDatabank = podd.newDatabank();
    podd.schemaDatabank = podd.newDatabank();

    // getPoddObjectForEdit(artifactUri, objectUri);
    podd.getObjectTypeMetadata(podd.objectTypeUri, podd.callbackForGetMetadata, podd.schemaDatabank);

    // use delegation for dynamically added .clonable anchors
    $("#details").delegate(".clonable", "click", podd.cloneEmptyField);

    if (typeof console !== "undefined" && console.debug) {
        console.debug('### initialization complete ###');
    }
});

// --------------------- Constants ----------------------------

var artifactUri = 'http://purl.org/podd/basic-2-20130206/artifact:1';

var objectUri = 'http://purl.org/podd/basic-1-20130206/object:2966';

// TODO: these can be loaded via freemarker
var CARD_ExactlyOne = 'http://purl.org/podd/ns/poddBase#Cardinality_Exactly_One';
var CARD_OneOrMany = 'http://purl.org/podd/ns/poddBase#Cardinality_One_Or_Many';
var CARD_ZeroOrMany = 'http://purl.org/podd/ns/poddBase#Cardinality_Zero_Or_Many'

var DISPLAY_LongText = 'http://purl.org/podd/ns/poddBase#DisplayType_LongText';
var DISPLAY_ShortText = 'http://purl.org/podd/ns/poddBase#DisplayType_ShortText';
var DISPLAY_CheckBox = 'http://purl.org/podd/ns/poddBase#DisplayType_CheckBox';
var DISPLAY_DropDown = 'http://purl.org/podd/ns/poddBase#DisplayType_DropDownList'
var DISPLAY_AutoComplete = 'http://purl.org/podd/ns/poddBase#DisplayType_AutoComplete';
var DISPLAY_Table = 'http://purl.org/podd/ns/poddBase#DisplayType_Table';

// --------------------------------

/**
 * Creates a new rdfquery.js databank, adds common prefixes to it, and then
 * returns the databank to the caller.
 */
podd.newDatabank = function() {
    var nextDatabank = $.rdf.databank();
    // TODO: Is base useful to us?
    // nextDatabank.base("http://www.example.org/")
    nextDatabank.prefix("dcterms", "http://purl.org/dc/terms/");
    nextDatabank.prefix('poddBase', 'http://purl.org/podd/ns/poddBase#')
    nextDatabank.prefix('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')
    nextDatabank.prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#')
    nextDatabank.prefix('owl', 'http://www.w3.org/2002/07/owl#')
    nextDatabank.prefix("foaf", "http://xmlns.com/foaf/0.1/");
    nextDatabank.prefix("moat", "http://moat-project.org/ns#");
    nextDatabank.prefix("tagging", "http://www.holygoat.co.uk/owl/redwood/0.1/tags/");

    return nextDatabank;
};

podd.getCurrentObjectUri = function() {
    var objectUri = '<' + podd.objectUri + '>';

    if (typeof podd.objectUri === 'undefined') {
        // hardcoded blank node for new objects
        // this will be replaced after the first valid submission of the
        // object to the server
        objectUri = "_:a1";
    }

    return objectUri;
};

/**
 * DEBUG-ONLY : Prints the contents of the given databank to the console
 */
podd.debugPrintDatabank = function(databank, message) {
    var triples = $.toJSON(databank.dump({
        format : 'application/json'
    }));
    if (typeof console !== "undefined" && console.debug) {
        console.debug(message + ': (' + databank.size() + ') ' + triples);
    }
};

/*
 * Retrieve metadata to render the fields to add a new object of the given type.
 * 
 * @param objectTypeUri - the type of Object to be added @param successCallback -
 * where to send the results
 */
podd.getObjectTypeMetadata = function(/* String */objectTypeUri, /* function */successCallback, /* object */
nextDatabank) {
    if (typeof console !== "undefined" && console.debug) {
        console.debug('[getMetadata]  "' + objectTypeUri + '" .');
    }

    requestUrl = podd.baseUrl + '/metadata';

    if (typeof console !== "undefined" && console.debug) {
        console.debug('[getMetadata] Request (GET):  ' + requestUrl);
    }

    $.ajax({
        url : requestUrl,
        type : 'GET',
        data : {
            objecttypeuri : objectTypeUri
        },
        dataType : 'json', // what is expected back
        success : function(resultData, status, xhr) {
            successCallback(resultData, status, xhr, objectTypeUri, nextDatabank);
        },
        error : function(xhr, status, error) {
            if (typeof console !== "undefined" && console.debug) {
                console.debug('[getMetadata] $$$ ERROR $$$ ' + error);
                console.debug(xhr.statusText);
            }
        }
    });
};

/*
 * Callback function when RDF containing metadata is available
 * 
 * FIXME: Since the the metadata we get back does not contain Options for
 * drop-down type values, those have to be searched for using the Search
 * Ontology Service.
 * 
 * Also, this returns weights that are given in the schemas. Since there can be
 * properties without weights, sorting them only by weight is insufficient.
 * 
 */
podd.callbackForGetMetadata = function(resultData, status, xhr, objectType, nextDatabank) {
    if (typeof console !== "undefined" && console.debug) {
        console.debug('[getMetadata] ### SUCCESS ### ');
        console.debug(resultData);
    }

    nextDatabank.load(resultData);

    if (typeof console !== "undefined" && console.debug) {
        console.debug('Databank size = ' + nextDatabank.size());
    }

    // retrieve weighted property list
    var myQuery = $.rdf({
        databank : nextDatabank
    })
    // Desired type a OWL:Class
    .where('<' + objectType + '> rdf:type owl:Class')
    // Desired type has rdfs:subClassOf
    .where('<' + objectType + '> rdfs:subClassOf ?x')
    // Subclass is an owl:Restriction
    .where('?x rdf:type owl:Restriction')
    // Restriction has a linked property, which is the minimum we require
    .where('?x owl:onProperty ?propertyUri')
    // Optional, though recommended, rdfs:label annotation on property
    .optional('?propertyUri rdfs:label ?propertyLabel')
    // Optional, though recommended, display type to customise the HTML
    // interface for this property
    .optional('?propertyUri poddBase:hasDisplayType ?displayType')
    // Optional, though recommended, cardinality to specify how many of this
    // property can be linked to this class
    .optional('?propertyUri poddBase:hasCardinality ?cardinality')
    // Optional, weight given for property when used with this class to order
    // the interface consistently
    .optional('?propertyUri poddBase:weight ?weight');

    var bindings = myQuery.select();

    var propertyList = [];
    $.each(bindings, function(index, nextBinding) {
        var nextChild = {};
        nextChild.weight;
        nextChild.propertyUri = nextBinding.propertyUri.value;
        nextChild.propertyLabel;
        nextChild.displayType;
        nextChild.cardinality;

        if (typeof nextBinding.propertyLabel != 'undefined') {
            nextChild.propertyLabel = nextBinding.propertyLabel.value;
        }
        else {
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

        nextChild.displayValue = '';
        nextChild.valueUri = '';

        propertyList.push(nextChild);
        if (typeof console !== "undefined" && console.debug) {
            console.debug("[" + nextChild.weight + "] propertyUri=<" + nextChild.propertyUri + "> label=\""
                    + nextChild.propertyLabel + "\" displayType=<" + nextChild.displayType + "> card=<"
                    + nextChild.cardinality + ">");
        }
    });

    // sort property list
    propertyList.sort(function(a, b) {
        var aID = a.weight;
        var bID = b.weight;
        return (aID == bID) ? 0 : (aID > bID) ? 1 : -1;
    });

    $.each(propertyList, function(index, value) {
        podd.displayEditField(index, value, nextDatabank)
    });
};

/*
 * Retrieve an RDF containing necessary data and meta-data to populate the Edit
 * Artifact page.
 */
podd.getPoddObjectForEdit = function(
/* String */artifactUri,
/* String */objectUri) {

    if (typeof console !== "undefined" && console.debug) {
        console.debug('[getPoddObjectForEdit]  "' + objectUri + '" of artifact (' + artifactUri + ') .');
    }

    requestUrl = podd.baseUrl + '/artifact/edit';

    if (typeof console !== "undefined" && console.debug) {
        console.debug('[getPoddObjectForEdit] Request (GET):  ' + requestUrl);
    }

    $.ajax({
        url : requestUrl,
        type : 'GET',
        data : {
            artifacturi : artifactUri,
            objecturi : objectUri
        },
        dataType : 'json', // what is expected back
        success : loadEditDataCallback,
        error : function(xhr, status, error) {
            if (typeof console !== "undefined" && console.debug) {
                console.debug('[getPoddObjectForEdit] $$$ ERROR $$$ ' + error);
                console.debug(xhr.statusText);
            }
        }
    });
};

/*
 * Callback function when RDF containing Edit data is available
 */
podd.loadEditDataCallback = function(resultData, status, xhr, nextDatabank) {
    if (typeof console !== "undefined" && console.debug) {
        console.debug('[getPoddObjectForEdit] ### SUCCESS ### ' + resultData);
    }
    nextDatabank.load(resultData);
    if (typeof console !== "undefined" && console.debug) {
        console.debug('Databank size = ' + nextDatabank.size());
    }
    // retrieve weighted property list
    var myQuery = $.rdf({
        databank : nextDatabank
    })
    // FIXME: The following line is going to be very difficult to maintain in
    // the long run, so need to redesign the triple format
    .where('?objectUri ?propertyUri ?pValue').where('?propertyUri poddBase:weight ?weight').optional(
            '?propertyUri <http://www.w3.org/2000/01/rdf-schema#label> ?pLabel').optional(
            '?propertyUri poddBase:hasDisplayType ?displayType').optional(
            '?propertyUri poddBase:hasCardinality ?cardinality').optional(
            '?pValue <http://www.w3.org/2000/01/rdf-schema#label> ?pValueLabel');
    var bindings = myQuery.select();

    var propertyList = [];
    $.each(bindings, function(index, nextBinding) {
        var nextChild = {};
        nextChild.weight;
        nextChild.propertyUri = nextBinding.propertyUri.value;
        nextChild.propertyLabel;
        nextChild.displayType;
        nextChild.cardinality;

        if (typeof nextBinding.pLabel != 'undefined') {
            nextChild.propertyLabel = nextBinding.pLabel.value;
        }
        else {
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

        if (typeof nextBinding.pValueLabel != 'undefined') {
            nextChild.displayValue = nextBinding.pValueLabel.value;
            nextChild.valueUri = nextBinding.pValue.value;
        }
        else {
            nextChild.displayValue = nextBinding.pValue.value;
            nextChild.valueUri = nextBinding.pValue.value;
        }

        propertyList.push(nextChild);
        if (typeof console !== "undefined" && console.debug) {
            // console.debug(nextChild.weight + '] <' + nextChild.propertyUri +
            // '>
            // "' + nextChild.propertyLabel + '" <' +
            // nextChild.displayType + '> <' + nextChild.cardinality + '>');
        }
    });

    // sort property list
    propertyList.sort(function(a, b) {
        var aID = a.weight;
        var bID = b.weight;
        return (aID == bID) ? 0 : (aID > bID) ? 1 : -1;
    });

    $.each(propertyList, podd.displayEditField);
};

/*
 * Display the given field on page
 */
podd.displayEditField = function(index, nextField, nextDatabank) {
    if (typeof console !== "undefined" && console.debug) {
        // console.debug('[' + nextField.weight + '] <' + nextField.propertyUri
        // + '>
        // "' + nextField.propertyLabel + '" <' +
        // nextField.displayType + '> <' + nextField.cardinality + '>');
    }

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

    // display (+) icon to add extra values
    if (nextField.cardinality == CARD_ZeroOrMany || nextField.cardinality == CARD_OneOrMany) {
        link = $('<a>');
        // link.attr('href', '#');
        link.attr('icon', 'addField');
        link.attr('title', 'Add ' + nextField.propertyLabel);
        link.attr('class', 'clonable');
        // link.attr('id', 'cid_' + nextField.propertyUri);
        link.attr('property', nextField.propertyUri);
        li.append(link);
    }

    var li2 = $("<li>");
    li2.attr('id', 'id_li_' + nextField.propertyUri);

    if (nextField.displayType == DISPLAY_LongText) {
        li2.append(podd.addFieldInputText(nextField, 'textarea'));

    }
    else if (nextField.displayType == DISPLAY_ShortText) {
        li2.append(podd.addFieldInputText(nextField, 'text'));

    }
    else if (nextField.displayType == DISPLAY_DropDown) {
        li2.append(podd.addFieldDropDownList(nextField, nextDatabank));

    }
    else if (nextField.displayType == DISPLAY_CheckBox) {
        var input = podd.addFieldInputText(nextField, 'checkbox')
        var label = '<label>' + nextField.displayValue + '</label>';
        li2.append(input.after(label));

    }
    else if (nextField.displayType == DISPLAY_Table) {
        var checkBox = $('<p>');
        checkBox.text('Table here please');

        li2.append(checkBox);

    }
    else if (nextField.displayType == DISPLAY_AutoComplete) {
        var checkBox = $('<p>');
        checkBox.text('Auto complete please');

        li2.append(checkBox);

    }
    else { // default
        li2.append(podd.addFieldInputText(nextField, 'text'));
    }

    var subList = $('<ul>').append(li2);
    li.append(subList);
    $("#details ol").append(li);
};

podd.cloneEmptyField = function() {
    var thisId = $(this).attr('id');
    if (typeof console !== "undefined" && console.debug) {
        console.debug('Clicked clonable: ' + thisId);
    }

    var idToClone = '#tLbl1'; // '#id_http://purl.org/podd/ns/poddScience_hasANZSRC';
    // //'#id_' + $(this).attr('property');
    // console.debug('Requested cloning ' + idToClone);

    var clonedField = $(this).clone(true);
    // clonedField.attr('id', 'LABEL_cloned');
    // console.debug('Cloned: ' + clonedField.attr('id'));
    $(this).append(clonedField);
    if (typeof console !== "undefined" && console.debug) {
        console.debug('appended cloned field');
    }

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
 * function old() {
 * 
 * var toClone = $(idToClone); var clonedField = toClone.clone();
 * clonedField.attr('id', idToClone + '_some_random_val'); clonedField.val('');
 * 
 * toClone.append(clonedField);
 * 
 * console.debug('Cloning completed'); }
 */

// simply copied from PODD-1, does not work
podd.addNewEmptyField = function(id) {
    if (typeof console !== "undefined" && console.debug) {
        console.debug('Add new Empty field for "' + id + '"');
    }

    var parentId = 'id_li' + id;
    if (typeof console !== "undefined" && console.debug) {
        console.debug('Add after parent with id: ' + $('"#' + parentId + '"').attr('id'));
    }

    var clonableId = 'id_' + id;
    // var clonedField = $('"#' + clonableId + '"').clone();
    var clonedField = $('#p1').clone();
    if (typeof console !== "undefined" && console.debug) {
        console.debug('Cloned field of type: ' + clonedField.attr('type'));
    }
    // $(parentId).append(clonedField);
    $('#id_http://purl.org/podd/ns/poddScience#hasANZSRC').append(clonedField);

    // var cloned = $('#p1').clone()
    // var oldId = cloned.attr('id');
    // cloned.attr('id', oldId + '_v1');

    // $('#tLbl1').append(clonedField);
    $('#p1').html('button clicked');

    // var element = document.createElement(type);
    // element.setAttribute('id', id);
    // element.setAttribute('name', id);
    // var li = document.createElement("li");
    // li.appendChild(element);
    // document.getElementById('add_' + id).appendChild(li);
};

/*
 * Construct an HTML input field of a given type.
 */
podd.addFieldInputText = function(nextField, inputType) {

    var displayValue = nextField.displayValue;
    if (inputType == 'checkbox') {
        displayValue = nextField.valueUri;
    }

    var idString = 'id_' + nextField.propertyUri;
    idString = idString.replace("#", "_");

    var input = $('<input>', {
        id : idString,
        name : 'name_' + nextField.propertyLabel,
        type : inputType,
        value : displayValue
    });

    // add handler to process changes to this field
    // - handler should have property URI
    // - detect if value actually changed
    // - if changed, update the "instance" databank and set dirty flag

    return input;
};

/*
 * Construct an HTML drop-down list for the given field.
 */
podd.addFieldDropDownList = function(nextField, nextDatabank) {
    var select = $('<select>', {
        id : 'id_' + nextField.propertyUri,
        name : 'name_' + nextField.propertyLabel,
    });

    var myQuery = $.rdf({
        databank : nextDatabank
    }).where('<' + nextField.propertyUri + '> poddBase:hasAllowedValue ?pValue').optional(
            '?pValue rdfs:label ?pDisplayValue');
    var bindings = myQuery.select();
    // console.debug('Found ' + bindings.length + ' bindings for query');
    $.each(bindings, function(index, value) {

        var optionValue = value.pValue.value;

        var optionDisplayValue = value.pValue.value;
        if (value.pDisplayValue != 'undefined') {
            optionDisplayValue = value.pDisplayValue.value;
        }

        var selectedVal = false;
        if (nextField.valueUri == optionValue) {
            console.debug('SELECTED option = ' + optionValue);
            selectedVal = true;
        }

        var option = $('<option>', {
            value : optionValue,
            text : optionDisplayValue,
            selected : selectedVal
        });

        select.append(option);
    });
    return select;
};

/*
 * Search Ontology Resource Service using AJAX, convert the RDF response to a
 * JSON array and invoke the callback function.
 */
podd.searchOntologyService = function(
/* object with 'search term' */request,
/* function */callbackFunction) {

    requestUrl = podd.baseUrl + '/search';

    if (console && console.debug) {
        console.debug('Searching artifact: "' + artifactUri.toString() + '" in searchTypes: "' + request.searchTypes
                + '" for terms matching "' + request.term + '".');
    }

    queryParams = {
        searchterm : request.term,
        artifacturi : artifactUri.toString(),
        searchtypes : request.searchTypes
    };

    $.get(requestUrl, queryParams, function(data) {
        if (console && console.debug) {
            console.debug('Response: ' + data.toString());
        }
        var formattedData = parsesearchresults(requestUrl, data);
        if (console && console.debug) {
            console.debug('No. of search results = ' + formattedData.length);
        }
        callbackFunction(formattedData);
    }, 'json');
};
