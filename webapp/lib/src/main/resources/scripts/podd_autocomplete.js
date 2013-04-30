/**
 * PODD :
 * Uses jquery-RDF to analyse the current document with respect to the RDFa information 
 * it contains and populate the rdfadebug element with the resulting information
 */



if(typeof oas  === 'undefined')
{
	oas = {};
}

if(typeof oas['autocomplete']  == 'undefined')
{
	oas.autocomplete = {};
}

$.widget("custom.catcomplete", $.ui.autocomplete, {
    _renderMenu: function(ul, items) {
        var self = this,
            currentCategory = "";
        $.each(items, function(index, item) {
            if (item.category != currentCategory) {
                ul.append("<li class='ui-autocomplete-category'>" + item.category + "</li>");
                currentCategory = item.category;
            }
            self._renderItem(ul, item);
        });
    }
});

oas.autocomplete.constructAutocomplete = function()
{
	// used to prevent race conditions between autocomplete requests
	var requestIndex = 0;

	// TODO: Load this list dynamically
	var ontologies = [
		{
			value: "po",
			label: "Plant Ontology",
			desc: "Ontology about plants",
			icon: "plant_ontology.gif"
		},
		{
			value: "go",
			label: "Gene Ontology",
			desc: "Ontology about genes",
			icon: "GO-head.gif"
		},
		{
			value: "obi",
			label: "Ontology for Biomedical Investigations",
			desc: "Ontology used to describe biomedical investigations",
			icon: "obi-blue-236x65.png"
		}
	];

	// TODO: remove hardcoded element ID's here
	$( "#ontology" ).autocomplete({
        serviceUrl: oas.baseUrl+'search/ontologylabels/?searchterm=',
        minChars:3,
        width: 300,
        delimiter: /(,|;)\s*/,
        deferRequestBy: 500, //miliseconds
		focus: function( event, ui ) {
			$( "#ontology" ).val( ui.item.label );
			//$( "#ontology-icon" ).attr( "src", oas.baseUrl+"resources/static/images/" + ui.item.icon );
			return false;
		},
        source: function( request, response ) {
            //console.debug("in source function");
            //console.debug(this.options.serviceUrl);
            //console.debug(request);
            if ( self.xhr ) {
                self.xhr.abort();
            }
            self.xhr = $.ajax({
                url: this.options.serviceUrl+request.term,
//    					data: request,
                dataType: "json",
                autocompleteRequest: ++requestIndex,
                success: function( data, status ) {
                    //console.debug("found success");
                    //console.debug(data);
                    //console.debug(status);
                    if ( this.autocompleteRequest === requestIndex ) {
                        //console.debug("response function");
                        //console.debug(response);
                        // TODO: make sure that the data here is filtered as specified in http://api.jqueryui.com/autocomplete/#option-source
                        // Understanding the response function may also be useful in understanding why nothing is happening here http://api.jqueryui.com/autocomplete/#event-response
                        response( oas.rdf.parsesearchresults(this.url, data) );
                    }
                    //else
                    //{
                        //console.debug("ignored success callback because autocompleteRequest=("+this.autocompleteRequest+") did not equal requestIndex=("+requestIndex+")");
                    //}
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    //console.debug("found error");
                    //console.debug(xhr);
                    //console.debug(ajaxOptions);
                    //console.debug(thrownError);
                    //console.debug(xhr.getResponseHeader());
                    if ( this.autocompleteRequest === requestIndex ) {
                        response( [] );
                    }
                }
            });
        },
		select: function( event, ui ) {
			$( "#ontology" ).val( ui.item.label );
			$( "#ontology-id" ).val( ui.item.value.toString() );
			//$( "#ontology-description" ).html( ui.item.desc );
			//$( "#ontology-icon" ).attr( "src", "images/" + ui.item.icon );
			return false;
		}
	});
	
	$("#ontology").data( "autocomplete" )._renderItem = function( ul, item ) {
		return $( "<li></li>" ).
			data( "item.autocomplete", item ).
			append( "<a>" + item.label + "<br />" + item.desc + "</a>" ).
			appendTo( ul );
	};

    $('#ontologytermlabel').autocomplete({
        serviceUrl: oas.baseUrl+'search/ontologies/?searchterm=',
        minChars:3,
        width: 300,
        delimiter: /(,|;)\s*/,
        deferRequestBy: 500, //miliseconds
//	            search: function( event, ui ){ 
//	            	var originalUrl = this.url;
//	            	console.debug("search event triggered");
///	            	console.debug(event);
//	            	console.debug(ui);
//	            	console.debug(event.target.value);
//	            	url = serviceUrl+event.target.value;
//            	},
        source: function( request, response ) {
            //console.debug("in source function");
            //console.debug(this.options.serviceUrl);
            //console.debug(request);
            if ( self.xhr ) {
                self.xhr.abort();
            }
            
            //console.debug("current ontology id");
            //console.debug($( "#ontology-id" ));
            //console.debug($( "#ontology-id" ).val());
            
            var ontologyUri = $.trim($( "#ontology-id" ).val());
            
            var requestUrl = this.options.serviceUrl+request.term;
            
            if(ontologyUri.length > 0)
            {
                requestUrl = requestUrl + "&ontologyUri=" + ontologyUri;
            }
            
            self.xhr = $.ajax({
                url: requestUrl,
//    					data: request,
                dataType: "json",
                autocompleteRequest: ++requestIndex,
                success: function( data, status ) {
                    //console.debug("found success");
                    //console.debug(data);
                    //console.debug(status);
                    if ( this.autocompleteRequest === requestIndex ) {
                        //console.debug("response function");
                        //console.debug(response);
                        // NOTE: make sure that the data here is filtered as specified in http://api.jqueryui.com/autocomplete/#option-source
                        // Understanding the response function may also be useful in understanding why nothing is happening here http://api.jqueryui.com/autocomplete/#event-response
                        response( oas.rdf.parsesearchresults(this.url, data) );
                    }
                    //else
                    //{
                        //console.debug("ignored success callback because autocompleteRequest=("+this.autocompleteRequest+") did not equal requestIndex=("+requestIndex+")");
                    //}
                },
                error: function (xhr, ajaxOptions, thrownError) {
                    //console.debug("found error");
                    //console.debug(xhr);
                    //console.debug(ajaxOptions);
                    //console.debug(thrownError);
                    //console.debug(xhr.getResponseHeader());
                    if ( this.autocompleteRequest === requestIndex ) {
                        response( [] );
                    }
                }
            });
        },
        select: function( event, ui ) {
            console.debug("Option selected");
            console.debug(event);
            console.debug(ui);
            $( "#ontologytermlabel" ).val( ui.item.label );
            $( "#ontologytermuri" ).val( ui.item.value.toString() );
            return false;
        }
      });

};

// --------------------------------

  /* Manually created fragment for submission into edit artifact service */
  var nextDatabank = $.rdf.databank()
	.base('http://purl.org/podd/basic-2-20130206/artifact:1')
	.prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#')
	.prefix('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#')
	.prefix('owl', 'http://www.w3.org/2002/07/owl#')
	.prefix('poddScience', 'http://purl.org/podd/ns/poddScience#')
	.prefix('poddBase', 'http://purl.org/podd/ns/poddBase#')
	.add('<genotype33> rdf:type poddScience:Genotype .')
	.add('<genotype33> rdf:type owl:NamedIndividual .')
	.add('<genotype33> rdfs:label "Genotype 33" .')
	.add('<http://purl.org/podd/basic-2-20130206/artifact:1#Demo_Material> poddScience:hasGenotype <genotype33> .')

  var theMessageBox = '#message1';
	
  /* Display a Message */
  function displayMessage(event){
    var messageBox = event.data.param1;
    var message = event.data.param2;
    console.debug(message);
    console.debug(messageBox);
    $(messageBox).html('<i>' + message + '</i>');
  }


  /* Display a message on leaving text field */
  function doBlur(){
    $('#message1').html('The value of field "' + $(this).attr('id') +  '" was set to: "<b>' + $(this).val() + '</b>".');
  }
  
  /* Invoke the Edit PODD Artifact Service with the modified RDF triples */
  function updateArtifact(){
	console.debug('inside doUpdate()');
	  
	requestUrl = podd.baseUrl + '/artifact/edit';  

	
	var artifactUri = $('#podd_artifact').val();
	var versionUri = $('#podd_artifact_version').val();
	var modifiedTriples = $.toJSON(nextDatabank.dump({format: 'application/json'})); 
	
	console.debug('Updating artifact: "' + artifactUri + '" version: "' + versionUri + '" .');
    console.debug('The content: ' + modifiedTriples);

    // set query parameters in the URI as setting them under data failed, mostly leading to a 415 error
	requestUrl = requestUrl + '?artifacturi=' + encodeURIComponent(artifactUri)
				+ '&versionuri='  + encodeURIComponent(versionUri)
				+ '&isforce=true';
	console.debug('Request (POST):  ' + requestUrl);
	
    $.ajax({
    	url: requestUrl,
    	type: 'POST',
    	data: modifiedTriples,
    	contentType: 'application/rdf+json', // what we're sending
    	dataType: 'json', // what is expected back
    	success: function(resultData, status, xhr){
    		console.debug('########## SUCCESS###### ' + resultData);
    		console.debug(xhr.responseText);
    		$(theMessageBox).html('<i>Successfully edited artifact.</i><br><p>' +
    				xhr.responseText + '</p><br>');
    	},
    	error: function(xhr,status,error){
    		console.debug('$$$$$ ERROR $$$$$ ' + error);
    		console.debug(xhr.statusText);
    	}
    });
    
    
  }

  /*
     Call Search Ontology Resource Service using AJAX, convert the RDF response to a JSON array 
     and set to the array as autocomplete data.
   */
  function autoCompleteCallback(/* object with 'search term' */ request, /* function */ response) {
    
	requestUrl = podd.baseUrl + '/search';  
	
	var artifactUri = $('#podd_artifact').val();
	var searchTypes = $('#podd_type').attr('value');
	
	console.debug('Searching artifact: "' + artifactUri + '" for searchTypes: "' + searchTypes + '" for search term: "'
			+ request.term + '".');

	queryParams = { 
			searchterm: request.term, 
			artifacturi: artifactUri,
			searchtypes: searchTypes 
	};
	
    // console.debug('Request: ' + requestUrl);
    
    $.get(requestUrl, queryParams,
    	function(data){
    	    console.debug('Response: ' + data.toString());
	        var formattedData = parsesearchresults(requestUrl, data);
	        //console.debug(formattedData);
	        response(formattedData);
    	},
    	'json'
    );
  }

  /*
     Parse the RDF received from the server and create a JSON array
     Modified Query from oas.rdf.parsesearchresults()
   */
  function parsesearchresults(/* string */ searchURL, /* rdf/json */ data) {
    // console.debug("Parsing search results");
    var nextDatabank = $.rdf.databank();

    var rdfSearchResults = nextDatabank.load(data);
	
    // console.debug("About to create query");
	var myQuery = $.rdf({ databank: nextDatabank })
	    .where('?pUri <http://www.w3.org/2000/01/rdf-schema#label> ?pLabel');
	var bindings = myQuery.select();	
	
    var nodeChildren = [];
    $.each(bindings, function(index, value)
        {
            var nextChild = {};
            nextChild.label = value.pLabel.value;
            nextChild.value = value.pUri.value;

            nodeChildren.push(nextChild);
        });
    // TODO: Sort based on weights for properties
    
    return nodeChildren;
  };


// --------------------------------
// everything needs to come in here
// --------------------------------
$(document).ready(function() {

  // auto complete 1
  $( ".autocomplete" ).autocomplete({ 
    delay: 500, //milliseconds
    minLength: 2, //minimum length to trigger autocomplete 
    source: autoCompleteCallback, 
    select: function( event, ui ) {
    	console.debug('Option selected "' + ui.item.label + '" with value "' + ui.item.value + '".');
    	$( '#in3' ).val( ui.item.value );
    	$( '#in4' ).val( ui.item.label );
    	$( '#message1' ).html( 'Selected : ' + ui.item.value );
        return false;
     }
  });

  $(".short_text").blur(updateArtifact);

  $('#btn1').click({param1: '#message1', param2: 'Button 1 clicked'}, displayMessage);

  // $('#btn2').click({param1: $("#in1").val() , param2: $("#in2Random").val()}, doProcess);

  $('#btn4').click({param1: '#message1', param2: '[Message]'}, displayMessage);

});

