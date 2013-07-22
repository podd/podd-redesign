<!-- Podd Error template -->

<script type="text/javascript" src="${baseUrl}/resources/scripts/animatedcollapse.js">
    /* this needs to be placed at the top of the file so that we can add divs as they are created !!!! */
    /***********************************************
    * Animated Collapsible DIV v2.4- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
    * This notice MUST stay intact for legal use
    * Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
    ***********************************************/
</script>


<script type="text/javascript">
	$(document).ready(function() {
        podd.debug('-------------------');
        podd.debug('initializing...');
        podd.debug('-------------------');

		var errorModelAsString = ${message_details!"{}"};

		var errorDetailsCount = 0;

	    var nextDatabank = podd.newDatabank();
        nextDatabank.load(errorModelAsString, {format: 'application/json'});

		// Display top level error details
		// NOTE: This query does not capture errors which only have partial information.
		// Should be improved as a separate function in podd_edit.js
		var queryDetails = $.rdf({
        	databank : nextDatabank
    	})
    	.where('?x rdfs:comment ?stacktrace')
    	.where('?x <http://purl.org/podd/ns/err#exceptionClass> ?exceptionclass')
    	.where('?x <http://purl.org/podd/ns/err#source> ?source')
    	;
    	var bindings1 = queryDetails.select();
	
		$.each(bindings1, function(index, binding) {
			var stackTrace = '<PRE>' + binding.stacktrace.value + '</PRE>';
			var exceptionClass = '<PRE>' + binding.exceptionclass.value + '</PRE>';
			var source = '<PRE>' + binding.source.value + '</PRE>';
			
			podd.updateErrorTable('Source of Error', source);
			podd.updateErrorTable('Exception Class', exceptionClass);
			podd.updateErrorTable('Stack Trace', stackTrace);
			
			errorDetailsCount = errorDetailsCount + 1;
		});
		
		// Display any sub-details		
		var querySub = $.rdf({
        	databank : nextDatabank
    	})
    	.where('?top <http://purl.org/podd/ns/err#contains> ?x')
    	.where('?x rdfs:comment ?details')
    	.where('?x <http://purl.org/podd/ns/err#source> ?source')
    	;
    	var bindings2 = querySub.select();
	
		$.each(bindings2, function(index, binding) {
			var details = '<PRE>' + binding.details.value + '</PRE>';
			var source = '<PRE>' + binding.source.value + '</PRE>';
			
			podd.updateErrorTable(' Secondary Source', source);
			podd.updateErrorTable(' Secondary Details', '<PRE>' + details + '</PRE>');
			
			errorDetailsCount = errorDetailsCount + 1;
		});

		if (errorDetailsCount == 0) {
			podd.updateErrorTable('', 'No error details available');
		};

        podd.debug('### initialization complete ###');
	});
</script>


<div id="content_pane">
    <h4 class="errorMsg">ERROR: ${error_code!"Error Code Unknown"}</h4>
    
    <p>${message!""}</p>
    <br>

	<h3 class="underlined_heading">Error Details 
		<a href="javascript:animatedcollapse.toggle('details')" icon="toggle" title="Error Details"></a>
	</h3>	
	<div id='details'>
		<table id="errorTable" class="table"></table>
	</div>
	<br>
</div>  <!-- content pane -->

<script type="text/javascript">
    animatedcollapse.addDiv('details', 'fade=1,hide=1');

	animatedcollapse.ontoggle=function($, divobj, state){ 
		//fires each time a DIV is expanded/contracted
		//$: Access to jQuery
		//divobj: DOM reference to DIV being expanded/ collapsed. Use "divobj.id" to get its ID
		//state: "block" or "none", depending on state
	};
	animatedcollapse.init();
</script>