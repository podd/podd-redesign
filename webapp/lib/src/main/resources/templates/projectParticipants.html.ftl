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
	
		$(".deleteLinkStatic").click(function() {
	     	var identifier = $(this).closest('span').attr('value');
	     	var roleUri = $(this).attr('name');
	    	podd.debug('Remove: ' + identifier + ' as a <' + roleUri + '> for project ' + podd.artifactIri);
	    	podd.submitUserRoleDelete(identifier, roleUri, podd.artifactIri);
	     	
	     	var liToRemove = $(this).closest('li');
			podd.debug('Going to remove ' + liToRemove);	     	
	     	liToRemove.fadeOut(400, function(){
         		liToRemove.remove();
        	});
	     	
        	return false;
	    });
	
	
		// add Handlers for PI
		var piInput = $('#pi');
		var piHiddenValueElement =  $('#pi_hidden');
		podd.addAutoCompleteHandler(piInput, piHiddenValueElement, undefined, undefined, undefined, true);
		podd.addProjectRoleBlurHandler(piInput, piHiddenValueElement, podd.artifactIri, '${piUri!""}', '${piIdentifier!""}');
		
		
		// add Handlers for Project Admin
		var adminInput = $('#admin');
		var adminHiddenValueElement = $('#admin_hidden');
		var adminList = $('#admin_list');
		
		podd.addAutoCompleteHandler(adminInput, adminHiddenValueElement, undefined, undefined, undefined, true);
		podd.addProjectRoleBlurHandler2(adminInput, adminHiddenValueElement, adminList, podd.artifactIri, '${adminUri!""}');
		

		
		//podd.addProjectRoleHandlers($('#member'), $('#member_hidden'), podd.artifactIri, '${memberUri!""}');
		//podd.addProjectRoleHandlers($('#observer'), $('#observer_hidden'), podd.artifactIri, '${observerUri!""}');
		
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
		                <input autocomplete="off" class="wide ac_input" id="pi" name="pi" value="${piLabel!""}">
		                <input type="hidden" id="pi_hidden" name="pi_hidden" value="${piIdentifier!""}">
		                <h6 class="errorMsg">${piError!""}</h6>
		                <br>Only the Principal Investigator can publish a Project.
		                Principal Investigators have Project Administrator status by default.
		                <h6 class="errorMsg">${piError!""}</h6>
		                <br>
		            </li>
		            
		            <li>
		                <label for="admin" class="bold">Project Administrators:
	                	<a id="add_padmin" title="Add Project Administrator" icon="addField"></a>
		                </label>
		            </li>
		            
		            <li>
		            	<ul id="admin_list">
			            <#if admins?? && admins?has_content>
				            <#list admins as admin>
					            <li>
					            	<span value="${admin.identifier!""}">
					            		${admin.userLabel!""}
					            		<a name="${adminUri!""}" class="deleteLinkStatic" href="">delete</a>
					            	</span>
					            </li>
				            </#list>
			            </#if>
		            	</ul>
		            </li>
		            <li>
		                <input autocomplete="off" class="wide ac_input" id="admin" name="admin" value="">
		                <input type="hidden" id="admin_hidden" name="admin_hidden" value="">
	                	<a id="add_padmin" class="button" href="">Add</a>
		                <br>Project Administrators will have Create, Read, Update and Delete access to all project objects.
		                <h6 class="errorMsg">${adminError!""}</h6>
		                <br>
		            </li>

<!--		            
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
-->		            
		       </ol>
		    </div>
		</div>  <!-- Collapsable div -->
	    
		<h3 class="underlined_heading"> </h3> <!-- just want the line -->
		<div id="buttonwrapper">
	        <button type="button" id="btnCancel" name="cancelEdit" value="cancelEdit">Return to Project</button>
	    </div>
	</form>
