<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="statusList" type="java.util.ArrayList<java.lang.String>" -->
<#-- @ftlvariable name="roleObjectList" type="java.util.ArrayList<podd.model.user.RepositoryRole>" -->
<#-- @ftlvariable name="requestedUser" type="podd.model.user.User" -->
<#-- @ftlvariable name="isAdmin" type="boolean" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="userNameError" type="java.lang.String" -->
<#-- @ftlvariable name="emailError" type="java.lang.String" -->
<#-- @ftlvariable name="titleError" type="java.lang.String" -->
<#-- @ftlvariable name="firstNameError" type="java.lang.String" -->
<#-- @ftlvariable name="lastNameError" type="java.lang.String" -->
<#-- @ftlvariable name="organisationError" type="java.lang.String" -->
<#-- @ftlvariable name="positionError" type="java.lang.String" -->
<#-- @ftlvariable name="phoneError" type="java.lang.String" -->
<#-- @ftlvariable name="addressError" type="java.lang.String" -->
<#-- @ftlvariable name="urlError" type="java.lang.String" -->

<div id="title_pane">
    <h3>Edit User Details</h3>
</div>

<div id="content_pane">

<#if isAdmin?? && isAdmin>
	<#include "admin_aux.html.ftl"/>

<div id="main">
</#if>


    <p>
    <h4 class="errorMsg">${errorMessage!""}</h4>

    <#if requestedUser?? && requestedUser?has_content>

        <#if isAdmin?? && isAdmin>
        <form name="edit_user" enctype="multipart/form-data" action="${baseUrl}/admin/user/${requestedUser.identifier!"unknown-username"}/edit" method="POST" onsubmit="return validateUserInfo()">
            <#else>
        <form name="edit_user" enctype="multipart/form-data" action="${baseUrl}/user/${requestedUser.identifier!"unknown-username"}/edit" method="POST" onsubmit="return validateUserInfo()">
        </#if>

        <div id="admin_left_pane" class="fieldset_without_border">
			<div class="legend_no_indent">Account Details</div>
			<ol>
				<li><span class="bold">User Name: </span>${requestedUser.identifier!""}</li>
				<li>
					<label for="email" class="bold">Email Address:
						<span icon="required"></span>
					</label>
					<input id="email" name="email" type="text" value="${requestedUser.email!""}">
                    <h6 class="errorMsg" id='errorEmail'></h6>
					<h6 class="errorMsg" id='validationError'>${emailError!""}</h6>
				</li>
				<#if isAdmin?? && isAdmin>
					<li>
						<div class="fieldset_without_border radioGroup">
							<div class="legend_no_indent radioGroup">Status:</div>
							<ol>
								<#list statusList as status>
									<li>
										<#if requestedUser.userStatus?? && requestedUser.userStatus == status>
											<input id="${status.URI}" class="narrow" name="status" type="radio" value="${status.URI}" checked
											<#if !isAdmin> disabled="true"</#if>
											>       
										<#else>
											<input id="${status.URI}" class="narrow" name="status" type="radio" value="${status.URI}"
											<#if !isAdmin> disabled="true"</#if>
											>
										</#if>       
										<label for="${status.URI}" class="bold">${status.label}</label>
									</li>
								</#list>
							</ol>
						</div>
					</li>
				<#else>
					<li><span class="bold">Status: </span>${requestedUser.status!""}</li>
					<li><span class="bold">Role: </span>${requestedUser.repositoryRole.description!""}</li>
				</#if>
			</ol>
		</div>
		
		<div id="admin_right_pane" class="fieldset_without_border">
			<div class="legend_no_indent">Contact Details</div>
			<ol>
				<li>
					<label for="title" class="bold">Title:</label>
					<input id="title" name="title" type="text" value="${requestedUser.title!""}">
                    <h6 class="errorMsg" id='errorTitle'>${titleError!""}</h6>
				</li>
				<li>
					<label for="firstName" class="bold">First Name:
						<span icon="required"></span>
					</label>
					<input id="firstName" name="firstName" type="text" value="${requestedUser.firstName!""}">
					<h6 class="errorMsg" id='errorFirstName'>${firstNameError!""}</h6>
				</li>
				<li>
					<label for="lastName" class="bold">Last Name:
						<span icon="required"></span>
					</label>
					<input id="lastName" name="lastName" type="text" value="${requestedUser.lastName!""}">
					<h6 class="errorMsg" id='errorLastName'>${lastNameError!""}</h6>
				</li>
				<li>
					<label for="organisation" class="bold">Organisation/Institution:
						<span icon="required"></span>
					</label>
					<input id="organisation" name="organisation" type="text" value="${requestedUser.organization!""}">
					<h6 class="errorMsg" id='errorOrganisation'>${organisationError!""}</h6>
				</li>
				<li>
					<label for="position" class="bold">Professional Position:</label>
					<input id="position" name="position" type="text" size="40" value="${requestedUser.position!""}">
                    <h6 class="errorMsg" id='errorPostion'>${positionError!""}</h6>
				</li>
				<li>
					<label for="phone" class="bold">Phone Number:
						<span icon="required"></span>
					</label>
					<input id="phone" name="phone" type="text" value="${requestedUser.phone!""}">
					<h6 class="errorMsg" id='errorPhone'>${phoneError!""}</h6>
				</li>
				<li>
					<label for="address" class="bold">Mailing Address:
						<span icon="required"></span>
					</label>
					<textarea id="address" name="address" cols="30" rows="4">${requestedUser.address!""}</textarea>
					<h6 class="errorMsg" id='errorAddress'>${addressError!""}</h6>
				</li>
				<li>
					<label for="url" class="bold">URL:</label>
					<input id="url" name="url" type="text" value="${requestedUser.homePage!""}">
                    <h6 class="errorMsg" id='errorURL'>${urlError!""}</h6>
				</li>
				<li>
					<label for="orcid" class="bold">ORCID ID:</label>
					<input id="orcid" name="orcid" type="text" value="${requestedUser.orcid!""}">
                    <h6 class="errorMsg" id='errorOrcid'>${urlError!""}</h6>
				</li>
			</ol>
		</div>
		
		<div id="buttonwrapper">
			<button type="submit">Update Details</button>
	        <#if isAdmin?? && isAdmin>
	        <a href="${baseUrl}/admin/user/${requestedUser.identifier!"unknown-username"}">Cancel</a>
	        	<#else>
	        <a href="${baseUrl}/user/${requestedUser.identifier!"unknown-username"}">Cancel</a>
	        </#if>
		</div>
    </#if>
	</form>
	
<#if isAdmin?? && isAdmin>
	</div>
</#if>
</div>  <!-- content pane -->