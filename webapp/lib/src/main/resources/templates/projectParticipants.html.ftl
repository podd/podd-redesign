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

		podd.artifactIri = '${artifactUri!"undefined"}';
		
		podd.debug("artifact IRI: " + podd.artifactIri);
		
		podd.roledata = {};
		podd.roledata['${adminUri}'] = [];
	    <#if admins?? && admins?has_content>
	        <#list admins as admin>
	        	podd.roledata['${adminUri}'].push('${admin.identifier}');
	        </#list>
	    </#if>
		
		podd.roledata['${memberUri}'] = [];
	    <#if members?? && members?has_content>
	        <#list members as member>
	        	podd.roledata['${memberUri}'].push('${member.identifier}');
	        </#list>
	    </#if>

		podd.roledata['${observerUri}'] = [];
	    <#if observers?? && observers?has_content>
	        <#list observers as observer>
	        	podd.roledata['${observerUri}'].push('${observer.identifier}');
	        </#list>
	    </#if>
		
		$("#btnCancel").click(function(event) {
			event.preventDefault();
			podd.redirectToGetArtifact(undefined, undefined, undefined);
			return false;
		});
	
		$("#pi_input_div").hide();
	
		$("#pi_label_div span").mouseover(function () {
			$("#pi_label_div").hide();
			$("#pi_input_div").show();
			$("#pi_input_div input").focus();
		});
	
		var deleteLink = $(".deleteLinkStatic");
		podd.addListItemDeleteHandler(deleteLink);
	
		// add Handlers for PI
		var piInput = $('#pi');
		var piHiddenValueElement =  $('#pi_hidden');
		podd.addAutoCompleteHandler(piInput, piHiddenValueElement, undefined, undefined, undefined, true);
		podd.addPiBlurHandler(piInput, piHiddenValueElement, podd.artifactIri, '${piUri!""}', '${piIdentifier!""}');
		
		
		// - add Handlers for Project Admins
		var adminInput = $('#admin');
		var adminHiddenValueElement = $('#admin_hidden');
		var adminList = $('#admin_list');
		podd.addAutoCompleteHandler(adminInput, adminHiddenValueElement, undefined, undefined, undefined, true);
		podd.addProjectRoleBlurHandler(adminInput, adminHiddenValueElement, adminList, podd.artifactIri, '${adminUri!""}');
		
		// - add Handlers for Project Members
		var memberInput = $('#member');
		var memberHiddenValueElement = $('#member_hidden');
		var memberList = $('#member_list');
		podd.addAutoCompleteHandler(memberInput, memberHiddenValueElement, undefined, undefined, undefined, true);
		podd.addProjectRoleBlurHandler(memberInput, memberHiddenValueElement, memberList, podd.artifactIri, '${memberUri!""}');
		
		// - add Handlers for Project Observers
		var observerInput = $('#observer');
		var observerHiddenValueElement = $('#observer_hidden');
		var observerList = $('#observer_list');
		podd.addAutoCompleteHandler(observerInput, observerHiddenValueElement, undefined, undefined, undefined, true);
		podd.addProjectRoleBlurHandler(observerInput, observerHiddenValueElement, observerList, podd.artifactIri, '${observerUri!""}');

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

	<div id="project_details">  <!-- Collapsible div -->
	    <div id="projectInfo" class="fieldset">
	        <div class="legend">Project Summary Information</div>
	        <ol>
	        <li><span class="bold">ID:</span>
	        	<a href="${baseUrl}/artifact/base?artifacturi=${artifactUri?url}&amp;objecturi=${projectObject.objectURI?url}">${projectObject.objectURI!""}</a>
	        </li>
	        <li><span class="bold">Title: </span>${projectObject.label!""}</li>
	        </ol>
	    </div>
	</div>  <!-- projectDetails - Collapsable div -->
	<br>


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
		            
		                <div id="pi_input_div">
		                	<input autocomplete="off" class="wide ac_input" id="pi" name="pi" value="${piLabel!""}">
		                </div>
		                <div id="pi_label_div">
		                	<span>${piLabel!""}&nbsp;</span>
		                </div>
		                
		                <input type="hidden" id="pi_hidden" name="pi_hidden" value="${piIdentifier!""}">
		                <h6 class="errorMsg">${piError!""}</h6>
		                <br>Only the Principal Investigator can publish a Project.
		                Principal Investigators have Project Administrator status by default.
		                <h6 class="errorMsg">${piError!""}</h6>
		                <br>
		            </li>
		            
	<!-- Project Admins -->
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
	                	<a id="add_padmin" class="button" href="javascript:void(0);">Add</a>
		                <br>Project Administrators will have Create, Read, Update and Delete access to all project objects.
		                <h6 class="errorMsg">${adminError!""}</h6>
		                <br>
		            </li>

	<!-- Project Members -->
		            <li>
		                <label for="member" class="bold">Project Members: 
		                	<span icon="addField" class="clonable"></span>
		                </label>
		            </li>
		            <li>
		            	<ul id="member_list">
			            <#if members?? && members?has_content>
				            <#list members as member>
					            <li>
					            	<span value="${member.identifier!""}">
					            		${member.userLabel!""}
					            		<a name="${memberUri!""}" class="deleteLinkStatic" href="">delete</a>
					            	</span>
					            </li>
				            </#list>
			            </#if>
		            	</ul>
		            </li>
		            <li>
		                <input autocomplete="off" class="wide ac_input" id="member" name="member" value="">
			            <input type="hidden" id="member_hidden" name="member_hidden" value="">
	                	<a id="add_pmember" class="button" href="javascript:void(0);">Add</a>
		                <br>Project Members will have Create, Read and Update access to all project objects.
		                <h6 class="errorMsg">${memberError!""}</h6>
		                <br>
		            </li>

	<!-- Project Observers -->
		            
		            <li>
		                <label for="observer" class="bold">Project Observers: 
		                	<span icon="addField" class="clonable"></span>
		                </label>
		            </li>
		            <li>
		            	<ul id="observer_list">
			            <#if observers?? && observers?has_content>
				            <#list observers as observer>
					            <li>
					            	<span value="${observer.identifier!""}">
					            		${observer.userLabel!""}
					            		<a name="${observerUri!""}" class="deleteLinkStatic" href="">delete</a>
					            	</span>
					            </li>
				            </#list>
			            </#if>
		            	</ul>
		            </li>
		            <li>
		                <input autocomplete="off" class="wide ac_input" id="observer" name="observer" value="${observer!""}">
			            <input type="hidden" id="observer_hidden" name="observer_hidden" value="">
	                	<a id="add_pobserver" class="button" href="javascript:void(0);">Add</a>
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
