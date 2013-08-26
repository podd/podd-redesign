<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="pi" type="java.lang.String" -->
<#-- @ftlvariable name="piError" type="java.lang.String" -->
<#-- @ftlvariable name="admin" type="java.lang.String" -->
<#-- @ftlvariable name="adminError" type="java.lang.String" -->
<#-- @ftlvariable name="member" type="java.lang.String" -->
<#-- @ftlvariable name="memberError" type="java.lang.String" -->
<#-- @ftlvariable name="observer" type="java.lang.String" -->
<#-- @ftlvariable name="observerError" type="java.lang.String" -->

<script type="text/javascript">
	$(document).ready(function() {
        podd.debug('-------------------');
        podd.debug('initializing...');
        podd.debug('-------------------');

		podd.artifactIri = '${artifactIri!"undefined"}';
		
		podd.debug("artifact IRI: " + podd.artifactIri);
		
		$("#btnCancel").click(function(event) {
			event.preventDefault();
			podd.redirectToGetArtifact(undefined, undefined, undefined);
			return false;
		});
	
		// autocomplete for Principal Investigator
		var input = $('#pi');
		var hiddenValueElement = $('#pi_hidden');
		
		podd.addAutoCompleteHandler(input, hiddenValueElement, undefined, undefined, undefined, true);
		podd.debug('added autocomplete handler to #pi');
		
		// TODO
		// autocomplete for Project Administrators
		// autocomplete for Project Members
		// autocomplete for Project Observers
			
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
		                <input autocomplete="off" class="wide ac_input" id="pi" name="pi" value="${pi!""}">
		                <input type="hidden" id="pi_hidden" name="pi_hidden" value="">
		                <br>Only the Principal Investigator can publish a Project.
		                Principal Investigators have Project Administrator status by default.
		                <h6 class="errorMsg">${piError!""}</h6>
		            </li>
		            <li>
		                <label for="admin" class="bold">Project Administrators: </label>
		            </li>
		            <li>
		                <textarea autocomplete="off" class="high ac_input" id="admin" name="admin">${admin!""}</textarea>
		                <br>Project Administrators will have Create, Read, Update and Delete access to all project objects.
		                <h6 class="errorMsg">${adminError!""}</h6>
		            </li>
		            <li>
		                <label for="member" class="bold">Project Members: </label>
		            </li>
		            <li>
		                <textarea autocomplete="off" class="high ac_input" id="member" name="member">${member!""}</textarea><br>
		                Project Members will have Create, Read and Update access to all project objects.
		                <h6 class="errorMsg">${memberError!""}</h6>
		            </li>
		            <li>
		                <label for="observer" class="bold">Project Observers: </label>
		            </li>
		            <li>
		                <textarea autocomplete="off" class="high ac_input" id="observer" name="observer">${observer!""}</textarea>
		                <br>Project Observer will have Read only access to all project objects.
		                <h6 class="errorMsg">${observerError!""}</h6>
		            </li>
		       </ol>
		    </div>
		</div>  <!-- Collapsable div -->
	    
		<h3 class="underlined_heading"> </h3> <!-- just want the line -->
		<div id="buttonwrapper">
	        <button type="button" id="btnCancel" name="cancelEdit" value="cancelEdit">Return to Project</button>
	    </div>
	</form>
