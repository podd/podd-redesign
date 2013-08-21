<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="title" type="java.lang.String" -->
<#-- @ftlvariable name="user" type="" -->
<#-- @ftlvariable name="objectType" type="com.github.podd.utils.PoddObjectLabel" -->
<#-- @ftlvariable name="parentUri" type="java.lang.String" -->
<#-- @ftlvariable name="parentPredicateUri" type="java.lang.String" -->
<#-- @ftlvariable name="objectUri" type="java.lang.String" -->
<#-- @ftlvariable name="artifactIri" type="java.lang.String" -->
<#-- @ftlvariable name="versionIri" type="java.lang.String" -->

<script type="text/javascript">
	$(document).ready(function() {
        podd.debug('-------------------');
        podd.debug('initializing...');
        podd.debug('-------------------');

		podd.objectTypeUri = '${objectType.objectURI!"Not Found"}';
		podd.artifactIri = '${artifactIri!"undefined"}';
		podd.versionIri = '${versionIri!"undefined"}';
		
		podd.parentUri = '${parentUri!"undefined"}';
		podd.parentPredicateUri = '${parentPredicateUri!"undefined"}'; 
		
		// The object URI is undefined for a new object initially,
		// until the first valid save event to the server
		podd.objectUri = '${objectUri!"undefined"}';
	
	    podd.artifactDatabank = podd.newDatabank();
	    podd.schemaDatabank = podd.newDatabank();
		
		// object to hold each property values for validation purposes
		podd.valuesTable = {};
		
		// an array to hold cardinality values
		podd.cardinalityList = [];
		
		
		// FIXME: Are the following always necessary? Should they only be added for new objects?
		podd.artifactDatabank.add(podd.getCurrentObjectUri() + ' rdf:type <' + podd.objectTypeUri + '> ');
		<#if user?? && user.email??>
		podd.artifactDatabank.add(podd.getCurrentObjectUri() + ' dcterms:creator <mailto:${user.email!'unknown'}> ');
		podd.artifactDatabank.add('<mailto:${user.email!'unknown'}> rdf:type poddUser:User');
		</#if>
		
		podd.debug("artifact IRI: " + podd.getCurrentArtifactIri());
		podd.debug("object IRI: " + podd.getCurrentObjectUri());
		
		if(typeof podd.objectUri === 'undefined' || podd.objectUri === 'undefined') {
			if(typeof podd.parentUri === 'undefined' || podd.parentUri === 'undefined') {
				podd.initialiseNewTopObject(podd.artifactDatabank, podd.getCurrentArtifactIri(), podd.getCurrentObjectUri());
			}
			else {
				podd.initialiseNewObject(podd.artifactDatabank, podd.getCurrentArtifactIri(), podd.getCurrentObjectUri(), podd.parentUri, podd.parentPredicateUri);
			}
		}
				
	    // Get Metadata and invoke the callback function which will continue to update the interface
	    podd.getObjectTypeMetadata(podd.artifactIri, podd.objectTypeUri, podd.callbackFromGetMetadata, podd.schemaDatabank, podd.artifactDatabank);
	
		// Add form submission handler
		$("#btnSubmit").click(function(event) {
			event.preventDefault();
			podd.debug("Attempting to submit update query to server");
			podd.submitPoddObjectUpdate(podd.getCurrentArtifactIri(), podd.getCurrentVersionIri(), podd.getCurrentObjectUri(),  
					podd.schemaDatabank, podd.artifactDatabank, podd.redirectToGetArtifact);
			return false;
		});
	
		$("#btnCancel").click(function(event) {
			event.preventDefault();
			podd.redirectToGetArtifact(undefined, undefined, undefined);
			return false;
		});
	
        podd.debug('### initialization complete ###');
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
	<form id="editObjectForm">
	<h3 class="underlined_heading">${objectType.label!""} Details 
		<a href="javascript:animatedcollapse.toggle('details')" icon="toggle" title="View Details"></a>
	</h3>	
		<!--  all attributes -->
		<div id='details'>  <!-- Collapsible div -->
			<div id="project_details" class="fieldset">
				<ol>
	            </ol>
	        </div>
	    </div>  <!-- details - Collapsible div -->
	 
	 <#if showParticipants??>
		 <!-- Project Participants -->
		<h3 class="underlined_heading">Project Participants
			<a href="javascript:animatedcollapse.toggle('project_participants')" icon="toggle" title="View Project Participants"></a>
		</h3>

		<div style="display: block;" fade="1" id="project_participants">
		    <div id="participants" class="fieldset">
		        <ol>
		            <li>
		                <label for="pi" class="bold">Principal Investigator:
		                    <span icon="required"></span>
		                </label>
		            </li>
		            <li>
		                <input autocomplete="off" class="wide ac_input" id="pi" name="pi" value="">
		                <br>Only one Principal Investigator is allowed.
		                Principal Investigators have Project Administrator status by default.
		                <h6 class="errorMsg"></h6>
		            </li>
		            <li>
		                <label for="admin" class="bold">Project Administrators: </label>
		            </li>
		            <li>
		                <textarea autocomplete="off" class="high ac_input" id="admin" name="admin"></textarea>
		                <br>Project Administrators will have Create, Read, Update and Delete access to all project objects.
		                <h6 class="errorMsg"></h6>
		            </li>
		            <li>
		                <label for="member" class="bold">Project Members: </label>
		            </li>
		            <li>
		                <textarea autocomplete="off" class="high ac_input" id="member" name="member"></textarea><br>
		                Project Members will have Create, Read and Update access to all project objects.
		                <h6 class="errorMsg"></h6>
		            </li>
		            <li>
		                <label for="observer" class="bold">Project Observers: </label>
		            </li>
		            <li>
		                <textarea autocomplete="off" class="high ac_input" id="observer" name="observer"></textarea>
		                <br>Project Observer will have Read only access to all project objects.
		                <h6 class="errorMsg"></h6>
		            </li>
		       </ol>
		    </div>
		</div>  <!-- Collapsable div -->
	</#if>
	    
		<h3 class="underlined_heading"> </h3> <!-- just want the line -->
		<div id="buttonwrapper">
	    	<button type="button" id="btnSubmit" name="createObject" value="createObject">Submit</button>
	        <button type="button" id="btnCancel" name="cancelEdit" value="cancelEdit">Cancel</button>
<#--	    	
	    	<button type="reset" name="reset" value="reset">Reset</button>
	        <button type="submit" name="reinitialize" value="reinitialize">ReInitialize</button>
-->
	    </div>
	</form>
		
		
    <script type="text/javascript">
	    animatedcollapse.addDiv('details', 'fade=1,hide=0');
    </script>
		