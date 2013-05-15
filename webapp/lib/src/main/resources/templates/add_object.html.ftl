<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="objectType" type="java.lang.String" -->

<link rel="stylesheet" href="${baseUrl}/resources/styles/jquery-ui-smoothness.css" media="screen" type="text/css" />
		
    	<script type="text/javascript" src="${baseUrl}/resources/scripts/jquery-1.6.2.js"></script>

 	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.ui.core.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.ui.widget.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.effects.core.js"></script>

		<!-- Dependencies for rdfquery -->
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.json.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.rdfquery.rules-1.1-SNAPSHOT.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.rdf.turtle.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.effects.bounce.js"></script>
		
		<!-- Dependencies for autocomplete -->
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.ui.mouse.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.ui.position.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.ui.selectable.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.ui.resizable.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.ui.sortable.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.ui.button.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.ui.dialog.js"></script>
	    
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.ui.autocomplete.js"></script>

		<!-- Dependencies for jstree -->
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.hotkeys.js"></script>
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.cookie.js"></script>
	    
	    <script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.jstree.js"></script>
 
 		<!-- Dependencies for PODD -->
		<script type="text/javascript" src="${baseUrl}/resources/scripts/podd_autocomplete.js"></script>
		<script type="text/javascript" src="${baseUrl}/resources/scripts/podd_edit.js"></script>

		<script type="text/javascript" src="${baseUrl}/resources/scripts/animatedcollapse.js">
		    /* this needs to be placed at the top of the file so that we can add divs as they are created !!!! */
		    /***********************************************
		     * Animated Collapsible DIV v2.4- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
		     * This notice MUST stay intact for legal use
		     * Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
		     ***********************************************/
		</script>

<div id="title_pane">
    <h3>${title!""}</h3>
</div>

<div id="content_pane">
<#if errorMessage?? && errorMessage != "">
<h4 class="errorMsg">${errorMessage!""}</h4>
</#if>

<#-- add general error messages -->
<#if generalErrorList?? && generalErrorList?has_content>
<ol>
    <#list generalErrorList as errorMsg>
    <li class="errorMsg">${errorMsg}</li>
    </#list>
</ol>
</#if>

	<h3 class="underlined_heading">${objectType.label!""} Details
		<a href="javascript:animatedcollapse.toggle('${objectType.label}_details')" icon="toggle" title="View Details"></a>
	</h3>	
	<div id='${objectType.label}_details'>  <!-- Collapsible div -->
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

			<br>
			<label id="tLbl1">Hello there</label><br>
			<p id="p1">Message</p><br>
			<br>
			
			<button id='btn9' class='clonable'>Test</button>

		<p>Artifact URI: ${artifactUri!""}</p>
		<br>

		<div>
			Project (Artifact) IRI: <input class='noaction' id='podd_artifact' cols='60' value='http://purl.org/podd/basic-2-20130206/artifact:1'><br>
		</div>
	<br><br>
	

    </div>  <!-- details - Collapsible div -->


		
		<div id="header">
		<ul id='list_attributes'>
		</ul>
		</div>
		
		<br><br>
		
    <script type="text/javascript">
	    animatedcollapse.addDiv('${objectType.label}_details', 'fade=1,hide=0');
    </script>
		