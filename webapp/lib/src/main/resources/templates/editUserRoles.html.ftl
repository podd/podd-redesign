<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="authenticatedUser" type="podd.model.user.User" -->
<#-- @ftlvariable name="requestedUser" type="podd.model.user.User" -->
<#-- @ftlvariable name="isAdmin" type="boolean" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="errorOldPassword" type="java.lang.String" -->

<script type="text/javascript">
	$(document).ready(function() {
        podd.debug('-------------------');
        podd.debug('initializing Edit User Roles page...');
        podd.debug('-------------------');

		podd.userName = '${requestedUser.identifier!""}';

		podd.roles = [];
		<#if repositoryRolesList?? && repositoryRolesList?has_content>
		    <#list repositoryRolesList as role>
		    	var aRole = {};
		    	aRole.uri = '${role.URI!'unknown'}';
		    	aRole.name = '${role.getName()!'unknown'}';
		    	aRole.description = '${role.description!'no description'}';
		    	podd.debug('The Role = ' + aRole.uri + ', ' + aRole.name + ', ' + aRole.description);
		    	podd.roles.push(aRole);
		    </#list>
		</#if>
		podd.debug('All Roles size = ' + podd.roles.length);
		
		// Add handler for deleting Roles populated at page load time
	    $(".deleteLinkStatic").click(function() {
	     	var tr = $(this).closest('tr');
	     	podd.showDeleteRoleConfirmDialog(podd.userName, tr);
        	return false;
	    });

		// Add new Row to Roles table
		$("#btnAddRole").click(function(event) {
			event.preventDefault();
			podd.showAddRoleDialog();
			
		});
	
		$("#btnCancel").click(function(event) {
			event.preventDefault();
			window.location.href = podd.baseUrl + '/user/${requestedUser.identifier}';
			return false;
		});
	
        podd.debug('### initialization complete ###');
	});
</script>

<div id="add_role_dialog" title="Add Role"></div>
<div id="delete_role_dialog" title="Delete Role"></div>

<div id="title_pane">
    <h3>Change User Roles</h3>
</div>

<div id="content_pane">

<#if isAdmin?? && isAdmin>
	<#include "admin_aux.html.ftl"/>

<div id="main">
</#if>

	<p>
    <h4 class="errorMsg">${errorMessage!""}</h4>

	<#-- add general error messages -->
	<ol id="errorMsgList">
		<#if generalErrorList?? && generalErrorList?has_content>
		    <#list generalErrorList as errorMsg>
		    <li class="errorMsg">${errorMsg}</li>
		    </#list>
		</#if>
	</ol>

	<form name="edit_user_roles" id="editUserRolesForm">

    <#if requestedUser?? && requestedUser?has_content>
		<div id="admin_left_pane" class="fieldset_without_border">
			<ol>
				<li><span class="bold">User Name: </span>${requestedUser.identifier!""}</li>
			</ol>
				
	    <table id="roleTable" class="tablesorter {sortlist: [[0,0]]}" cellspacing="0">
			<thead>
				<tr>
				    <th>Role</th>
				    <th>Mapped Object</th>
					<th>Action</th>
				</tr>
			</thead>
	        <tfoot>
	        <!-- empty table row, so that the footer appears and table looks complete -->
	            <tr>
	                <td></td>
	                <td></td>
	                <td></td>
	            </tr>
	        </tfoot>
	        <tbody>

			<#if userRoleList?? && userRoleList?has_content>
				<#list userRoleList as role>
					<tr>
			    		<td>
			    			<span class="role_span" value="${role.key.URI!""}">${role.key.getName()!""}</span>
			    		</td>
			    		<td>
			    			<#if role.value?? >
			    				<span><a href="${role.value.objectURI}">${role.value.label}</a></span>
			    			<#else>
			    				<span>Repository wide role</span>
			    		 	</#if>
			    		</td>
            			<td>
                			<a class="deleteLinkStatic" href="">delete</a>
            			</td>			    		
		    		</tr>
		    	</#list>
		    </#if>
			    	
			</tbody>
	    </table>
		</div>
		
		<div id="buttonwrapper">
			<button type="button" id="btnAddRole" >Add New Role</button>
			<button type="button" id="btnCancel" >Return to User Details</button>
		</div>
	</#if>
	</form>
	
<#if isAdmin?? && isAdmin>
	</div>
</#if>

</div>  <!-- content pane -->