/**
 * PODD : Uses jquery-RDF to analyse the current document with respect to the
 * RDFa information it contains and populate the rdfadebug element with the
 * resulting information
 */

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
podd.doBlur = function(theMessageBox) {
    $(theMessageBox)
            .html('The value of field "' + $(this).attr('id') + '" was set to: "<b>' + $(this).val() + '</b>".');
}

/* Retrieve the current version of an artifact and populate the databank */
podd.getArtifact = function(artifactUri, nextDatabank) {
    var requestUrl = podd.baseUrl + '/artifact/base?artifacturi=' + encodeURIComponent(artifactUri);

    console.debug('[getArtifact] Request to: ' + requestUrl);
    $.ajax({
        url : requestUrl,
        type : 'GET',
        // dataType : 'application/rdf+xml', // what is expected back
        success : function(resultData, status, xhr) {
            nextDatabank.load(resultData);
            console.debug('[getArtifact] ### SUCCESS ### loaded databank with size ' + nextDatabank.size());

            // update variables and page contents with retrieved artifact info
            var artifactId = podd.getOntologyID(nextDatabank);
            artifactIri = artifactId[0].artifactIri;
            versionIri = artifactId[0].versionIri;

            $('#podd_artifact').val(artifactIri);
            $('#podd_artifact_version').val(versionIri);

            // update project title on page
            var title = podd.getProjectTitle(nextDatabank);
            $('#in1').val(title.value);
            $('#in1Hidden').val(title.value);

            $(theMessageBox).html('<i>Successfully retrieved artifact version: ' + versionIri + '</i><br>');
        },
        error : function(xhr, status, error) {
            console.debug(status + '[getArtifact] $$$ ERROR $$$ ' + error);
            // console.debug(xhr.statusText);
        }
    });

}

/*
 * Invoke the Edit Artifact Service to update the artifact with changed object
 * attributes. { isNew: boolean, property: String value of predicate URI
 * surrounded by angle brackets, newValue: String, (Should be surrounded by
 * angle brackets if a URI, or double quotes if a String literal) oldValue:
 * String, (Should be surrounded by angle brackets if a URI, or double quotes if
 * a String literal) }
 */
podd.updatePoddObject = function(
/* String */objectUri,
/* array of objects */attributes,
/* object */nextDatabank) {

    requestUrl = podd.baseUrl + '/artifact/edit';

    console.debug('[updatePoddObject]  "' + objectUri + '" of artifact (' + versionIri + ') .');

    $.each(attributes, function(index, attribute) {
        console.debug('[updatePoddObject] handling property: ' + attribute.property);
        if (!attribute.isNew) {
            podd.deleteTriples(nextDatabank, objectUri, attribute.property);
        }
        nextDatabank.add(objectUri + ' ' + attribute.property + ' ' + attribute.newValue);
    });

    var modifiedTriples = $.toJSON(nextDatabank.dump({
        format : 'application/json'
    }));

    // set query parameters in the URI as setting them under data failed, mostly
    // leading to a 415 error
    requestUrl = requestUrl + '?artifacturi=' + encodeURIComponent(artifactIri) + '&versionuri='
            + encodeURIComponent(versionIri) + '&isforce=true';
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
            $(theMessageBox).html('<i>Successfully edited artifact.</i><br><p>' + xhr.responseText + '</p><br>');

            // FIXME: Should we be wiping out the databank before doing this?
            // FIXME: Should we be parsing resultData before doing this?
            podd.getArtifact(artifactIri, nextDatabank);
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
podd.autoCompleteCallback = function(/* object with 'search term' */request, /* function */response) {

    requestUrl = podd.baseUrl + '/search';

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
}

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
}

// Add autocompleteHandlers
podd.addAutoCompleteHandlers = function(/* string */autoCompletePath) {
    // $(".autocomplete")
    $(autoCompletePath).autocomplete({
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
podd.addShortTextBlurHandlers = function(/* string */shortTextPath) {
    // $(".short_text")
    $(shortTextPath).blur(
            function(event) {
                console.debug("shorttext blur event");
                console.debug(event);

                var objectUri = podd.getCurrentPoddObjectUri();

                var attributes = [];
                var nextAttribute = {};
                nextAttribute.isNew = false;
                nextAttribute.property = '<' + $(this).attr('property') + '>';
                nextAttribute.newValue = '"' + $(this).val() + '"';
                nextAttribute.oldValue = '"' + $('#in1Hidden').val() + '"';

                attributes.push(nextAttribute);

                console.debug('Change property: ' + nextAttribute.property + ' from ' + nextAttribute.oldValue + ' to '
                        + nextAttribute.newValue + '.');

                podd.updatePoddObject(objectUri, attributes, podd.artifactDatabank);
            });
};

// Update for autocomplete
podd.addAutoCompleteBlurHandlers = function(/* string */autoCompletePath) {
    // $(".autocomplete")
    $(autoCompletePath).blur(function(event) {
        console.debug("autocomplete blur event");
        console.debug(event);
        var objectUri = podd.getCurrentPoddObjectUri();

        var attributes = [];
        var nextAttribute = {};
        nextAttribute.isNew = true;
        nextAttribute.property = '<' + $(this).attr('property') + '>';
        nextAttribute.newValue = '<' + $('#' + $(this).attr('id') + 'Hidden').val() + '>';
        attributes.push(nextAttribute);

        console.debug('Add new property: <' + nextAttribute.property + '> <' + nextAttribute.newValue + '>');

        podd.updatePoddObject(objectUri, attributes, podd.artifactDatabank);
    });
};
