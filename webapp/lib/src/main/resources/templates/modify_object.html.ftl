<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="objectType" type="java.lang.String" -->

<script type="text/javascript">
	$(document).ready(function() {
	    if (typeof console !== "undefined" && console.debug) {
	        console.debug('-------------------');
	        console.debug('initializing...');
	        console.debug('-------------------');
	    }
		podd.objectTypeUri = '${objectType.objectURI!"Not Found"}';
		podd.artifactIri = undefined;
		podd.versionIri = undefined;
		// FIXME: Insert the parent URI using freemarker
		podd.parentUri = '${parentUri!"undefined"}';
		podd.parentPredicateUri = '${parentPredicateUri!"undefined"}'; 
		// The object URI is undefined for a new object initially,
		// until the first valid save event to the server
		podd.objectUri = '${objectUri!"undefined"}';
	
	    podd.artifactDatabank = podd.newDatabank();
	    podd.schemaDatabank = podd.newDatabank();
		
		// FIXME: Are the following always necessary? Should they only be added for new objects?
		podd.artifactDatabank.add(podd.getCurrentObjectUri() + ' rdf:type <' + podd.objectTypeUri + '> ');
		<#if user?? && user.email??>
		podd.artifactDatabank.add(podd.getCurrentObjectUri() + ' dcterms:creator <mailto:${user.email!'unknown'}> ');
		podd.artifactDatabank.add('<mailto:${user.email!'unknown'}> rdf:type poddUser:User');
		</#if>
		
		console.debug("artifact IRI");
		console.debug(podd.getCurrentArtifactIri());
		console.debug("object IRI");
		console.debug(podd.getCurrentObjectUri());
		
		if(typeof podd.objectUri === 'undefined' || podd.objectUri === 'undefined') {
			if(typeof podd.parentUri === 'undefined' || podd.parentUri === 'undefined') {
				podd.initialiseNewTopObject(podd.artifactDatabank, podd.getCurrentArtifactIri(), podd.getCurrentObjectUri());
			}
			else {
				podd.initialiseNewObject(podd.artifactDatabank, podd.getCurrentArtifactIri(), podd.getCurrentObjectUri(), podd.parentUri, podd.parentPredicateUri);
			}
		}
				
	    // Get Metadata and create fields for either new data or data that exists in artifactDatabank at this point
	    podd.getObjectTypeMetadata(podd.objectTypeUri, podd.updateInterface, podd.schemaDatabank, podd.artifactDatabank);
	
	    // use delegation for dynamically added .clonable anchors
	    // FIXME: This doesn't seem to be the right strategy with respect to handlers, 
	    // as each handler contains the current value of the field to detect whether it has changed.
	    //$("#details").delegate(".clonable", "click", podd.cloneEmptyField);
	
		// Add form submission handler
		$("#editObjectForm").submit(function(event) {
			event.preventDefault();
			console.debug("Attempting to submit update query to server");
			podd.submitPoddObjectUpdate(podd.getCurrentArtifactIri(), podd.getCurrentObjectUri(), podd.schemaDatabank, podd.artifactDatabank, podd.redirectToGetArtifact);
			return false;
		});
	
	    if (typeof console !== "undefined" && console.debug) {
	        console.debug('### initialization complete ###');
	    }
	});
</script>


<div id="title_pane">
    <h3>${title!""}</h3>
</div>

<div id="content_pane">
<h4 id="errorMsgHeader" class="errorMsg">${errorMessage!""}</h4>

<#-- add general error messages -->
<ol id="errorMsgList">
	<#if generalErrorList?? && generalErrorList?has_content>
	    <#list generalErrorList as errorMsg>
	    <li class="errorMsg">${errorMsg}</li>
	    </#list>
	</#if>
</ol>
	<h3 class="underlined_heading">${objectType.label!""} Details 
		<a href="javascript:animatedcollapse.toggle('details')" icon="toggle" title="View Details"></a>
	</h3>	
	<form id="editObjectForm">
		<!-- FIXME: These are not populated or handled by Javascript yet. Ideally rdfs:label and rdfs:comment should be in the property list, but it appears they are not. -->
		<div id='basicdetails'>  <!-- Collapsible div -->
			<!-- standard attributes -->
			<ol>
				<!-- object name -->
	        	<li>
		            <label for="object_name" class="bold">Title:
						<span icon="required"></span>
					</label>
                </li>
                <li>
	                <input id="object_name" name="object_name" type="text" value="">
	                <h6 class="errorMsg">${objectNameError!""}</h6>
	            </li>
	            
	            <!-- object description -->
	            <li>
		            <label for="object_description" class="bold">Description:</label>
                </li>
                <li>
					<textarea id="object_description" name="object_description" cols="30" rows="2"></textarea>
	                <span id="object_desc_text_limit"></span>
	                <h6 class="errorMsg">${objectDescriptionError!""}</h6>
				</li>

            </ol>
		
		<!--  other attributes -->
		<div id='details'>  <!-- Collapsible div -->
			<ol>
            </ol>
	    </div>  <!-- details - Collapsible div -->
	    
	    <input type="submit">Save object</input>
	</form>


		
		
    <script type="text/javascript">
	    animatedcollapse.addDiv('details', 'fade=1,hide=0');
    </script>
		