/**
 * PODD : Uses jquery-RDF to analyse the current document with respect to the
 * RDFa information it contains and populate the rdfadebug element with the
 * resulting information
 */

// --------------------------------
// invoked when page is "ready"
// --------------------------------
// --------------------- Constants ----------------------------
// var artifactUri = 'http://purl.org/podd/basic-2-20130206/artifact:1';
// var objectUri = 'http://purl.org/podd/basic-1-20130206/object:2966';
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

podd.getCurrentObjectUri = function() {
    var nextObjectUri;

    if (typeof podd.objectUri === 'undefined') {
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

podd.getCurrentArtifactIri = function() {
    var nextArtifactIri;

    if (typeof podd.artifactIri === 'undefined') {
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
nextSchemaDatabank, /* object */nextArtifactDatabank) {
    if (typeof console !== "undefined" && console.debug) {
        console.debug('[getMetadata]  "' + objectTypeUri + '" .');
    }

    var requestUrl = podd.baseUrl + '/metadata';

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
            successCallback(resultData, status, xhr, objectTypeUri, nextSchemaDatabank, nextArtifactDatabank);
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
podd.callbackForGetMetadata = function(resultData, status, xhr, objectType, nextSchemaDatabank, nextArtifactDatabank) {
    if (typeof console !== "undefined" && console.debug) {
        console.debug('[getMetadata] ### SUCCESS ### ');
        console.debug(resultData);
    }

    nextSchemaDatabank.load(resultData);

    if (typeof console !== "undefined" && console.debug) {
        console.debug('Schema Databank size = ' + nextSchemaDatabank.size());
    }

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
        // TODO: Search through nextArtifactDatabank to find if there are any
        // values in it for this property and display them instead of displaying
        // an empty box
        $("#details ol").append(podd.createEditField(index, value, nextSchemaDatabank, nextArtifactDatabank, true));
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

    var requestUrl = podd.baseUrl + '/artifact/edit';

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
 * Display the given field on page
 */
podd.createEditField = function(index, nextField, nextSchemaDatabank, nextArtifactDatabank, isNew) {
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

    var link = undefined;

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
    // li2.attr('id', 'id_li_' + nextField.propertyUri);

    if (nextField.displayType == DISPLAY_LongText) {
        var input = podd.addFieldInputText(nextField, 'textarea', nextSchemaDatabank);
        podd.addShortTextBlurHandler(input, nextField.propertyUri, nextField.displayValue, nextArtifactDatabank, isNew);
        if (typeof link !== 'undefined') {
            link.click(function() {
                var clonedField = input.clone(true);
                podd.addShortTextBlurHandler(clonedField, nextField.propertyUri, nextField.displayValue,
                        nextArtifactDatabank, true);
                // FIXME: Determine correct place to append this to
                li.append(clonedField);
            });
        }
        li2.append(input);
    }
    else if (nextField.displayType == DISPLAY_ShortText) {
        var input = podd.addFieldInputText(nextField, 'text', nextSchemaDatabank);
        podd.addShortTextBlurHandler(input, nextField.propertyUri, nextField.displayValue, nextArtifactDatabank, isNew);
        li2.append(input);
    }
    else if (nextField.displayType == DISPLAY_DropDown) {
        var input = podd.addFieldDropDownList(nextField, nextSchemaDatabank, isNew);
        li2.append(input);
    }
    else if (nextField.displayType == DISPLAY_CheckBox) {
        var input = podd.addFieldInputText(nextField, 'checkbox', nextSchemaDatabank);
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
        podd.updateErrorMessageList("TODO: Support property : " + nextField.propertyUri + " (" + nextField.displayValue
                + ")");
        // var input = podd.addFieldInputText(nextField, 'text',
        // nextSchemaDatabank);
        // podd.addShortTextBlurHandler(input, nextField.propertyUri,
        // nextField.displayValue, nextArtifactDatabank, isNew);
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
podd.broken_AddNewEmptyField = function(id) {
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

    // add handler to process changes to this field
    // - handler should have property URI
    // - detect if value actually changed
    // - if changed, update the "instance" databank and set dirty flag

    return input;
};

/*
 * Construct an HTML drop-down list for the given field.
 */
podd.addFieldDropDownList = function(nextField, nextDatabank, isNew) {
    var select = $('<select>', {
        // id : 'id_' + nextField.propertyUri,
        name : 'name_' + encodeURIComponent(nextField.propertyLabel),
    });

    var myQuery = $.rdf({
        databank : nextDatabank
    }).where('<' + nextField.propertyUri + '> poddBase:hasAllowedValue ?pValue').optional(
            '?pValue rdfs:label ?pDisplayValue');
    var bindings = myQuery.select();
    console.debug('Found ' + bindings.length + ' bindings for query');
    console.debug(bindings);
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

    var requestUrl = podd.baseUrl + '/search';

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

// --------------------------------
/* Manually created fragment for submission into edit artifact service */
// var nextDatabank = $.rdf.databank();
// .base('http://purl.org/podd/basic-2-20130206/artifact:1')
// .prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#')
// .prefix('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')
// .prefix('owl', 'http://www.w3.org/2002/07/owl#')
// .prefix('poddScience', 'http://purl.org/podd/ns/poddScience#')
// .prefix('poddBase', 'http://purl.org/podd/ns/poddBase#')
// .add('<genotype33> rdf:type poddScience:Genotype .')
// .add('<genotype33> rdf:type owl:NamedIndividual .')
// .add('<genotype33> rdfs:label "Genotype 33" .')
// .add(
// '<http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Material>
// poddScience:hasGenotype <genotype33> .')
/* Display a Message */
podd.displayMessage = function(event) {
    var messageBox = event.data.param1;
    var message = event.data.param2;
    console.debug(message);
    console.debug(messageBox);
    $(messageBox).html('<i>' + message + '</i>');
}

/**
 * DEBUG-ONLY : retrieve artifact and load it to databank
 */
podd.debugAddDownloadArtifactHandler = function(/* string */buttonPath, /* string */inputPath) {
    // $('#btn2')
    $(path).click(function() {
        // var artifactUri = $('#podd_artifact').val();
        var artifactUri = $(inputPath).val();
        podd.getArtifact(artifactUri);
        // removeTriple();
    });
};

/* Display a message on leaving text field */
podd.updateErrorMessageList = function(theMessage) {
    var li = $("<li>")
    li.html(theMessage);
    $("#errorMsgList").append(li);
};

/* Retrieve the current version of an artifact and populate the databank */
podd.getArtifact = function(artifactUri, nextArtifactDatabank) {
    var requestUrl = podd.baseUrl + '/artifact/base?artifacturi=' + encodeURIComponent(artifactUri);

    console.debug('[getArtifact] Request to: ' + requestUrl);
    $.ajax({
        url : requestUrl,
        type : 'GET',
        // dataType : 'application/rdf+xml', // what is expected back
        success : function(resultData, status, xhr) {
            nextArtifactDatabank.load(resultData);
            console.debug('[getArtifact] ### SUCCESS ### loaded databank with size ' + nextArtifactDatabank.size());

            // update variables and page contents with retrieved artifact info
            var artifactId = podd.getOntologyID(nextArtifactDatabank);
            podd.artifactIri = artifactId[0].artifactIri;
            podd.versionIri = artifactId[0].versionIri;

            podd.updateErrorMessageList('<i>Successfully retrieved artifact version: ' + podd.versionIri + '</i><br>');
        },
        error : function(xhr, status, error) {
            console.debug(status + '[getArtifact] $$$ ERROR $$$ ' + error);
            // console.debug(xhr.statusText);
        }
    });

};

/*
 * Invoke the Edit Artifact Service to update the artifact with changed object
 * attributes. { isNew: boolean, property: String value of predicate URI
 * surrounded by angle brackets, newValue: String, (Should be surrounded by
 * angle brackets if a URI, or double quotes if a String literal) oldValue:
 * String, (Should be surrounded by angle brackets if a URI, or double quotes if
 * a String literal) }
 */
podd.submitPoddObjectUpdate = function(
/* String */artifactIri,
/* String */objectUri,
/* object */nextArtifactDatabank) {

    var requestUrl = podd.baseUrl + '/artifact/edit';

    var modifiedTriples = $.toJSON(nextArtifactDatabank.dump({
        format : 'application/json'
    }));

    console.debug('[updatePoddObject]  "' + objectUri);
    if (typeof artifactIri === 'undefined') {
        artifactIri = '<urn:temp:uuid:artifact>';
    }

    if (typeof artifactIri !== "undefined" && artifactIri.lastIndexOf('<urn:temp:uuid:', 0) === 0) {
        // Create a new object if it wasn't defined
        // To succeed this will require the object to be a valid PoddTopObject
        requestUrl = podd.baseUrl + '/artifact/new';
    }
    else {
        // FIXME: Why is the parameter isForce hardcoded to true?
        requestUrl = requestUrl + '?artifacturi=' + encodeURIComponent(artifactIri) + '&isforce=true';
        if (typeof versionIri !== "undefined") {
            console.debug(' of artifact (' + versionIri + ').');
            // set query parameters in the URI as setting them under data
            // failed, mostly leading to a 415 error
            +'&versionuri=' + encodeURIComponent(versionIri);
        }
        if (typeof objectUri !== 'undefined') {
            console.debug(' on object (' + objectUri + ').');
            requestUrl = requestUrl + '&objectUri=' + encodeURIComponent(objectUri);
        }
    }
    console.debug('[updatePoddObject] Request (POST):  ' + requestUrl);

    $.ajax({
        url : requestUrl,
        type : 'POST',
        data : modifiedTriples,
        contentType : 'application/rdf+json', // what we're sending
        beforeSend : function(xhr) {
            xhr.setRequestHeader("Accept", "application/rdf+json");
        },
        success : function(resultData, status, xhr) {
            console.debug('[updatePoddObject] ### SUCCESS ### ' + resultData);
            // console.debug('[updatePoddObject] ' + xhr.responseText);
            var message = '<div>Successfully edited artifact.<pre>' + xhr.responseText + '</pre></div>';
            podd.updateErrorMessageList(message);

            var tempDatabank = podd.newDatabank();
            tempDatabank.load(resultData);
            var artifactId = podd.getOntologyID(tempDatabank);
            podd.artifactIri = artifactId[0].artifactIri;
            podd.versionIri = artifactId[0].versionIri;

            // Wipe out databank so it contains the most current copy
            podd.deleteTriples(nextArtifactDatabank, "?subject", "?predicate");
            podd.getArtifact(podd.artifactIri, nextArtifactDatabank);
        },
        error : function(xhr, status, error) {
            console.debug('[updatePoddObject] $$$ ERROR $$$ ' + error);
            console.debug(xhr.statusText);
        }
    });
};

/*
 * Call Search Ontology Resource Service using AJAX, convert the RDF response to
 * a JSON array and set to the array as autocomplete data.
 */
podd.autoCompleteCallback = function(/* object with 'search term' */request, /* function */response) {

    var requestUrl = podd.baseUrl + '/search';

    var searchTypes = $('#podd_type').attr('value');

    console.debug('Searching artifact: "' + artifactIri.toString() + '" in searchTypes: "' + searchTypes
            + '" for terms matching "' + request.term + '".');

    queryParams = {
        searchterm : request.term,
        artifacturi : artifactIri.toString(),
        searchtypes : searchTypes
    };

    $.get(requestUrl, queryParams, function(data) {
        console.debug('Response: ' + data.toString());
        var formattedData = podd.parsesearchresults(requestUrl, data);
        // console.debug(formattedData);
        response(formattedData);
    }, 'json');
};

/*
 * Parse the RDF received from the server and create a JSON array. Modified
 * Query from oas.rdf.parsesearchresults()
 */
podd.parsesearchresults = function(/* string */searchURL, /* rdf/json */data) {
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
 * Parse the given Databank and extract the artifact IRI and version IRI of the
 * ontology/artifact contained within.
 */
podd.getOntologyID = function(nextDatabank) {

    var myQuery = $.rdf({
        databank : nextDatabank
    }).where('?artifactIri owl:versionIRI ?versionIri');
    var bindings = myQuery.select();

    var nodeChildren = [];
    $.each(bindings, function(index, value) {
        var nextChild = {};
        nextChild.artifactIri = value.artifactIri.value;
        nextChild.versionIri = value.versionIri.value;

        nodeChildren.push(nextChild);
    });

    if (nodeChildren.length > 1) {
        console.debug('[getVersion] ERROR - More than 1 version IRI statement found!!!');
    }

    return nodeChildren;
};

/*
 * Parse the given Databank and extract the rdfs:label of the top object of the
 * artifact contained within contained within.
 */
podd.getProjectTitle = function(nextDatabank) {
    console.debug("[getProjectTitle] start");

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
        console.debug('[getProjectTitle] ERROR - More than 1 Project Title found!!!');
    }

    return nodeChildren[0];
};

/*
 * Removes triples in the given databank that match the specified subject and
 * property. All parameters are mandatory.
 */
podd.deleteTriples = function(nextDatabank, subject, property) {
    $.rdf({
        databank : nextDatabank
    }).where(subject + ' ' + property + ' ?object').sources().each(function(index, tripleArray) {
        console.debug('[deleteTriple] object to delete = ' + tripleArray[0]);
        nextDatabank.remove(tripleArray[0]);
    });
};

// Add autocompleteHandlers
podd.addAutoCompleteHandler = function(/* object */autoComplete) {
    // $(".autocomplete")
    autoComplete.autocomplete({
        delay : 500, // milliseconds
        minLength : 2, // minimum length to trigger
        // autocomplete
        source : podd.autoCompleteCallback,

        focus : function(event, ui) {
            // prevent ui.item.value from appearing in the textbox
            // FIXME: Remove hardcoded path here
            $('#in4').val(ui.item.label);
            return false;
        },

        select : function(event, ui) {
            console.debug('Option selected "' + ui.item.label + '" with value "' + ui.item.value + '".');
            // FIXME: Remove hardcoded path here
            $('#in4Hidden').val(ui.item.value);
            // FIXME: Remove hardcoded path here
            $('#in4').val(ui.item.label);
            // FIXME: Remove hardcoded path here
            $('#message1').html('Selected : ' + ui.item.value);
            return false;
        }
    });
};

// Update for short text
podd.addShortTextBlurHandler = function(/* object */shortText, /* object */propertyUri, /* object */originalValue, /* object */
nextArtifactDatabank, /* boolean */isNew) {
    var nextOriginalValue = '"' + originalValue + '"';

    // $(".short_text")
    shortText.blur(function(event) {
        console.debug("shorttext blur event");
        console.debug(event);

        var objectUri = podd.getCurrentObjectUri();

        var attributes = [];
        var nextAttribute = {};
        nextAttribute.isNew = isNew;
        nextAttribute.objectUri = objectUri;
        nextAttribute.property = '<' + propertyUri + '>';
        nextAttribute.newValue = '"' + $(this).val() + '"';
        nextAttribute.oldValue = nextOriginalValue;

        attributes.push(nextAttribute);

        console.debug('Update property : ' + propertyUri + ' from ' + nextAttribute.oldValue + ' to '
                + nextAttribute.newValue + ' (isNew=' + isNew + ')');

        podd.updateArtifactDatabank(attributes, nextArtifactDatabank, isNew);

        // Unbind this handler and create a new one with the new value as the
        // original value
        $(this).unbind("blur");
        var newValue = '"' + $(this).val() + '"';
        // NOTE: isNew is always false after the first time through this method
        podd.addShortTextBlurHandler(shortText, propertyUri, newValue, nextArtifactDatabank, false);

        // FIXME: Cannot call update to the server after each edit, as some
        // fields may have invalid values at this point.
        // podd.updatePoddObject(objectUri, attributes, nextArtifactDatabank);
    });
};

// Update for autocomplete
podd.addAutoCompleteBlurHandler = function(/* object */autoComplete, /* object */nextArtifactDatabank, /* boolean */
isNew) {
    // $(".autocomplete")
    autoComplete.blur(function(event) {
        console.debug("autocomplete blur event");
        console.debug(event);
        var objectUri = podd.getCurrentObjectUri();

        var attributes = [];
        var nextAttribute = {};
        nextAttribute.isNew = isNew;
        nextAttribute.objectUri = objectUri;
        nextAttribute.property = '<' + $(this).attr('property') + '>';
        nextAttribute.newValue = '<' + $('#' + $(this).attr('id') + 'Hidden').val() + '>';
        attributes.push(nextAttribute);

        console.debug('Add new autocomplete property: <' + nextAttribute.property + '> <' + nextAttribute.newValue
                + '>');

        podd.updateArtifactDatabank(attributes, nextArtifactDatabank, isNew);
        // FIXME: Cannot call update to the server after each edit, as some
        // fields may have invalid values at this point.
        // podd.updatePoddObject(objectUri, attributes, nextArtifactDatabank);
    });
};

podd.updateArtifactDatabank = function(/* object */attributes, /* object */nextArtifactDatabank, /* boolean */isNew) {
    $.each(attributes, function(index, attribute) {
        console.debug('[updatePoddObject] handling property: ' + attribute.property);
        if (!attribute.isNew) {
            podd.deleteTriples(nextArtifactDatabank, attribute.objectUri, attribute.property);
        }
        nextArtifactDatabank.add(attribute.objectUri + ' ' + attribute.property + ' ' + attribute.newValue);
    });
};