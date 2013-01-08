<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="requestedUser" type="podd.model.user.User" -->
<#-- @ftlvariable name="message" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->

<div id="title_pane">
    <h3>Delete User</h3>
</div>

<div id="content_pane">

<#include "admin_aux.html.ftl"/>

<div id="main">

<p>
<h4 class="errorMsg">${errorMessage!""}</h4>
</p>

    <#if requestedUser?? && requestedUser?has_content>
    <form name="delete_user" action="${baseUrl}/admin/user/${requestedUser.userName!"unknown-username"}/delete" method="POST">
	
	<div class="fieldset_without_border">
        <div class="legend_no_indent">Are you sure you want to delete the user?</div> 
		<ol>
			<li><span class="bold">User Name: </span>${requestedUser.userName!""}</li>
			<li><span class="bold">Full Name: </span>${requestedUser.firstName!""} ${requestedUser.lastName!""}</li>
		</ol>
		<ol>
			<li><h6>${message!""}</h6></li>
		</ol>
	</div>
	
	<div id="buttonwrapper">
		<button type="submit">Delete User</button>
		<a href="${baseUrl}/admin/user/${requestedUser.userName!"unknown-username"}">Cancel</a>
	</div>
    </form>
    </#if>
</div>  <!-- main -->
</div>  <!-- content pane -->