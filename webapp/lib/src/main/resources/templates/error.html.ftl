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

		var errorDetailsInJson = ${message_details!"{}"};

		podd.displayDetailedErrors(errorDetailsInJson);

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