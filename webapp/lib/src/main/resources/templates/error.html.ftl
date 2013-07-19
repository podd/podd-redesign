<!-- Podd Error template -->

<script type="text/javascript">
	$(document).ready(function() {
        podd.debug('-------------------');
        podd.debug('initializing...');
        podd.debug('-------------------');

		var errorModelAsString = ${message_details!""}
		podd.debug(errorModelAsString);


	    var nextDatabank = podd.newDatabank();
        nextDatabank.load(errorModelAsString);
		
		var query = $.rdf({
        	databank : nextDatabank
    	}).where('?s ?p ?o');

    	var bindings = query.select();
		
	
		$.each(bindings, function(index, binding) {
			var subject = binding.s.value;
			var predicate = binding.p.value;
			var object = binding.o.value;
			
			podd.debug(subject + ' : ' + predicate + ' : ' + object);
			podd.updateErrorMessageList(subject + ' : ' + predicate + ' : ' + object);
		});
	
        podd.debug('### initialization complete ###');
	});
</script>




<div id="content_pane">
    <h4 class="errorMsg">ERROR: ${error_code!"Error Code Unknown"}</h4>
    
    <p>${message!""}</p>
    <br>

<!--
	FIXME
	<h3 class="underlined_heading">Error Details 
		<a href="javascript:animatedcollapse.toggle('details')" icon="toggle" title="Error Details"></a>
	</h3>	
	<div id='details'>

		<ol id="errorMsgList">
			<#if generalErrorList?? && generalErrorList?has_content>
			    <#list generalErrorList as errorMsg>
			    <li class="errorMsg">${errorMsg}</li>
			    </#list>
			</#if>
		</ol>
	</div>
-->
	<br>
<!--    <p>${message_details!""}</p> -->
    
</div>  <!-- content pane -->