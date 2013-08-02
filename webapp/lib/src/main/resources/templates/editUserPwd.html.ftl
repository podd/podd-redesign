<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="authenticatedUser" type="podd.model.user.User" -->
<#-- @ftlvariable name="requestedUser" type="podd.model.user.User" -->
<#-- @ftlvariable name="isAdmin" type="boolean" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="errorOldPassword" type="java.lang.String" -->

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

	<#if isAdmin?? && isAdmin>
		<form name="edit_user_pwd" enctype="multipart/form-data" action="${baseUrl}/admin/user/${requestedUser.identifier!"unknown-username"}/editpwd" method="POST" onsubmit="return validateUserPassword()">
	<#else>
		<form name="edit_user_pwd" enctype="multipart/form-data" action="${baseUrl}/user/${requestedUser.identifier!"unknown-username"}/editpwd" method="POST" onsubmit="return validateUserPassword()">
	</#if>

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
			<button type="submit">Save Password</button>
            <a href="${baseUrl}/admin/user/${requestedUser.identifier!"unknown-username"}">Cancel</a>
		</div>
	</#if>
	</form>
	
<#if isAdmin?? && isAdmin>
	</div>
</#if>

</div>  <!-- content pane -->