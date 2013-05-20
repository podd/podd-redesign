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
		// FIXME: Insert the parent URI using freemarker
		podd.parentUri = undefined;
		// The object URI is always undefined for a new object initially,
		// until the first valid save event to the server
		podd.objectUri = undefined;
	
	    podd.artifactDatabank = podd.newDatabank();
	    podd.schemaDatabank = podd.newDatabank();
	
		// TODO: Preload artifactDatabank before this point in general (do not fail for new object URIs) and only load the schemaDatabank after that is 
		// loaded so that we can immediately add the fields and handlers when we receive the schema data
	
	    // getPoddObjectForEdit(artifactUri, objectUri);
	    // Get Metadata and create fields for either new data or data that exists in artifactDatabank at this point
	    podd.getObjectTypeMetadata(podd.objectTypeUri, podd.callbackForGetMetadata, podd.schemaDatabank, podd.artifactDatabank);
	
	    // use delegation for dynamically added .clonable anchors
	    // FIXME: This doesn't seem to be the right strategy, although it may work for short-text
	    //$("#details").delegate(".clonable", "click", podd.cloneEmptyField);
	
	    if (typeof console !== "undefined" && console.debug) {
	        console.debug('### initialization complete ###');
	    }
	});
</script>


<div id="title_pane">
    <h3>${title!""}</h3>
</div>

<div id="content_pane">
<#if errorMessage?? && errorMessage != "">
<h4 id="errorMsgHeader" class="errorMsg">${errorMessage!""}</h4>
</#if>

<#-- add general error messages -->
<#if generalErrorList?? && generalErrorList?has_content>
<ol id="errorMsgList">
    <#list generalErrorList as errorMsg>
    <li class="errorMsg">${errorMsg}</li>
    </#list>
</ol>
</#if>
	<h3 class="underlined_heading">${objectType.label!""} Details 
		<a href="javascript:animatedcollapse.toggle('details')" icon="toggle" title="View Details"></a>
	</h3>	
	<div id='details'>  <!-- Collapsible div -->
			<!-- TODO: Generate these using javascript -->
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
			<!--  other attributes -->

            </ol>

	    </div>  <!-- details - Collapsible div -->


		
		<div>
		<ul id='list_attributes'>
		</ul>
		</div>
		
    <script type="text/javascript">
	    animatedcollapse.addDiv('details', 'fade=1,hide=0');
    </script>
		