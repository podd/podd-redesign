<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="authenticatedUser" type="podd.model.user.User" -->
<#-- @ftlvariable name="requestedUser" type="podd.model.user.User" -->
<#-- @ftlvariable name="isAdmin" type="boolean" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="errorOldPassword" type="java.lang.String" -->

<script type="text/javascript">
	$(document).ready(function() {
        podd.debug('-------------------');
        podd.debug('initializing Change Password page...');
        podd.debug('-------------------');

	
		// Add form submission handler
		$("#btnSubmit").click(function(event) {
			event.preventDefault();
			podd.debug("Attempting to update password");
			podd.emptyErrorMessages();
			var validInput = validateUserPassword();
			if (validInput) {
				podd.submitUserPassword();
			}
			return false;
		});
	
		$("#btnCancel").click(function(event) {
			event.preventDefault();
			window.location.href = podd.baseUrl + '/user/${requestedUser.identifier}';
			return false;
		});
	
        podd.debug('### initialization complete ###');
	});
</script>


<div id="title_pane">
    <h3>Change User Password</h3>
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

	<form name="edit_user_pwd" id="editUserPwdForm">

    <#if requestedUser?? && requestedUser?has_content>
		<div id="admin_left_pane" class="fieldset_without_border">
			<ol>
				<li><span class="bold">User Name: </span>${requestedUser.identifier!""}</li>
				<#if !isAdmin?? || !isAdmin || authenticatedUserIdentifier == requestedUser.identifier>
                <li>
					<label for="oldPassword" class="bold">Old Password:
						<span icon="required"></span>
					</label>
					<input id="oldPassword" name="oldPassword" type="password">
					<h6 class="errorMsg" id='errorOldPassword'>${errorOldPassword!""}</h6>
				</li>
                </#if>
                <li>
					<label for="password" class="bold">New Password:
						<span icon="required"></span>
					</label>
					<input id="userName" name="userName" type="hidden" value="${requestedUser.identifier!""}">
					<input id="password" name="password" type="password">
					<h6 class="errorMsg" id='errorPassword'></h6>
				</li>
				<li>
					<label for="confirmPassword" class="bold">Confirm New Password:
						<span icon="required"></span>
					</label>
					<input id="confirmPassword" name="confirmPassword" type="password">
					<h6 class="errorMsg" id='errorConfirmPassword'></h6>
				</li>
			</ol>
		</div>
		
		<div id="buttonwrapper">
			<button type="button" id="btnSubmit" >Save Password</button>
			<button type="button" id="btnCancel" >Cancel</button>
		</div>
	</#if>
	</form>
	
<#if isAdmin?? && isAdmin>
	</div>
</#if>

</div>  <!-- content pane -->