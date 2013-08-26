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
	
		// add Handlers for the Fields
		podd.addProjectRoleHandlers($('#pi'), $('#pi_hidden'), podd.artifactIri, '${piUri!""}');
		podd.addProjectRoleHandlers($('#admin'), $('#admin_hidden'), podd.artifactIri, '${adminUri!""}');
		podd.addProjectRoleHandlers($('#member'), $('#member_hidden'), podd.artifactIri, '${memberUri!""}');
		podd.addProjectRoleHandlers($('#observer'), $('#observer_hidden'), podd.artifactIri, '${observerUri!""}');
		
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
		                <br>
		            </li>
		            <li>
		                <label for="admin" class="bold">Project Administrators:
		                	<span icon="addField" class="clonable"></span>
		                </label>
		            </li>
		            <li>
		                <input autocomplete="off" class="wide ac_input" id="admin" name="admin" value="${admin!""}">
		                <input type="hidden" id="admin_hidden" name="admin_hidden" value="">
		                <br>Project Administrators will have Create, Read, Update and Delete access to all project objects.
		                <h6 class="errorMsg">${adminError!""}</h6>
		                <br>
		            </li>
		            <li>
		                <label for="member" class="bold">Project Members: 
		                	<span icon="addField" class="clonable"></span>
		                </label>
		            </li>
		            <li>
		                <input autocomplete="off" class="wide ac_input" id="member" name="member" value="${member!""}">
			            <input type="hidden" id="member_hidden" name="member_hidden" value="">
		                <br>Project Members will have Create, Read and Update access to all project objects.
		                <h6 class="errorMsg">${memberError!""}</h6>
		                <br>
		            </li>
		            <li>
		                <label for="observer" class="bold">Project Observers: 
		                	<span icon="addField" class="clonable"></span>
		                </label>
		            </li>
		            <li>
		                <input autocomplete="off" class="wide ac_input" id="observer" name="observer" value="${observer!""}">
			            <input type="hidden" id="observer_hidden" name="observer_hidden" value="">
		                <br>Project Observer will have Read only access to all project objects.
		                <h6 class="errorMsg">${observerError!""}</h6>
		                <br>
		            </li>
		       </ol>
		    </div>
		</div>  <!-- Collapsable div -->
	    
		<h3 class="underlined_heading"> </h3> <!-- just want the line -->
		<div id="buttonwrapper">
	        <button type="button" id="btnCancel" name="cancelEdit" value="cancelEdit">Return to Project</button>
	    </div>
	</form>
