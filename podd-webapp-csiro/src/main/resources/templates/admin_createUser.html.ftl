<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="title" type="java.lang.String" -->
<#-- @ftlvariable name="isAdmin" type="boolean" -->
<#-- @ftlvariable name="selectedStatus" type="java.lang.String" -->
<#-- @ftlvariable name="statusList" type="java.util.ArrayList<java.lang.String>" -->
<#-- @ftlvariable name="selectedRepositoryRole" type="podd.model.user.RepositoryRole" -->
<#-- @ftlvariable name="roleObjectList" type="java.util.ArrayList<podd.model.user.RepositoryRole>" -->
<#-- @ftlvariable name="userNameValue" type="java.lang.String" -->
<#-- @ftlvariable name="emailValue" type="java.lang.String" -->
<#-- @ftlvariable name="titleValue" type="java.lang.String" -->
<#-- @ftlvariable name="firstNameValue" type="java.lang.String" -->
<#-- @ftlvariable name="lastNameValue" type="java.lang.String" -->
<#-- @ftlvariable name="organisationValue" type="java.lang.String" -->
<#-- @ftlvariable name="positionValue" type="java.lang.String" -->
<#-- @ftlvariable name="phoneValue" type="java.lang.String" -->
<#-- @ftlvariable name="addressValue" type="java.lang.String" -->
<#-- @ftlvariable name="urlValue" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="userNameError" type="java.lang.String" -->
<#-- @ftlvariable name="emailError" type="java.lang.String" -->
<#-- @ftlvariable name="passwordError" type="java.lang.String" -->
<#-- @ftlvariable name="titleError" type="java.lang.String" -->
<#-- @ftlvariable name="firstNameError" type="java.lang.String" -->
<#-- @ftlvariable name="lastNameError" type="java.lang.String" -->
<#-- @ftlvariable name="organisationError" type="java.lang.String" -->
<#-- @ftlvariable name="positionError" type="java.lang.String" -->
<#-- @ftlvariable name="phoneError" type="java.lang.String" -->
<#-- @ftlvariable name="addressError" type="java.lang.String" -->
<#-- @ftlvariable name="urlError" type="java.lang.String" -->
<#-- @ftlvariable name="captchaPublicKey" type="java.lang.String" -->
<#-- @ftlvariable name="captchaError" type="java.lang.String" -->

<div id="title_pane">
    <h3>${title}</h3>
</div>

<div id="content_pane">

<#if isAdmin>
<#include "admin_aux.html.ftl"/>

<div id="main">
</#if>

<p>
<h4 class="errorMsg">${errorMessage!""}</h4>

<script type="text/javascript">
    var RecaptchaOptions = {
        theme : 'white'
    };
</script>

<form name="create_user" enctype="multipart/form-data" action="${baseUrl}/admin/user/create" method="POST" onsubmit="return createUserFormValidator()">

	<div id="admin_left_pane" class="fieldset_without_border">
		<div class="legend_no_indent">Account Details</div>
		<ol>
			<li>
				<label for="userName" class="bold">User Name:
					<span icon="required"></span>
				</label>
				<input id="userName" name="userName" type="text" value="${userNameValue!""}">
				<h6 class="errorMsg" id='errorUserName'>${userNameError!""}</h6>
			</li>
			<li>
				<label for="email" class="bold">Email Address:
					<span icon="required"></span>
				</label>
				<input id="email" name="email" type="text" value="${emailValue!""}">
				<h6 class="errorMsg" id='errorEmail'>${emailError!""}</h6>
			</li>
			<li>
				<label for="password" class="bold">Password:
					<span icon="required"></span>
				</label>
				<input id="password" name="password" type="password">
				<h6 class="errorMsg" id='errorPassword'>${passwordError!""}</h6>
			</li>
			<li>
				<label for="confirmPassword" class="bold">Confirm Password:
					<span icon="required"></span>
				</label>
				<input id="confirmPassword" name="confirmPassword" type="password">
				<h6 class="errorMsg" id='errorConfirmPassword'></h6>
			</li>
			<li>
				<div class="fieldset_without_border radioGroup">
					<div class="legend_no_indent radioGroup">Status:</div>
					<ol>
						<#if statusList??>
                        <#list statusList as status>
							<li>
								<#if selectedStatus?? && selectedStatus == status>

									<input id="${status}" class="narrow" name="status" type="radio" value="${status}" checked
                                    <#if !isAdmin>disabled="true"</#if>
                                    >
								<#else>
									<input id="${status}" class="narrow" name="status" type="radio" value="${status}"
                                    <#if !isAdmin>disabled="true"</#if>
                                    >
								</#if>       
								<label for="${status}" class="bold">${status}</label>
							</li>
						</#list>
                        </#if>
					</ol>
                    </div>
			</li>
			<li>
				<div class="fieldset_without_border radioGroup">
					<div class="legend_no_indent radioGroup">Roles:</div>
					<ol>
						<#if roleObjectList??>
                        <#list roleObjectList as role>
							<li>
								<#if selectedRepositoryRole?? && selectedRepositoryRole == role>
									<input id=${role.name!""} class="narrow" name="role" type="radio" value=${role.name!""} checked
                                    <#if !isAdmin>disabled="true"</#if>
                                    >
								<#else>
									<input id=${role.name!""} class="narrow" name="role" type="radio" value=${role.name!""}
                                    <#if !isAdmin>disabled="true"</#if>
                                    >
								</#if>
								<label for=${role.name!""} class="bold">${role.description!""}</label>
							</li>
						</#list>
                        </#if>
					</ol>
                    </div>
			</li>
		</ol>
	</div>
	
	<div id="admin_right_pane" class="fieldset_without_border">
		<div class="legend_no_indent">Contact Details</div>
		<ol>
			<li>
				<label for="title" class="bold">Title:</label>
				<input id="title" name="title" type="text" value="${titleValue!""}">
                <h6 class="errorMsg" id='titleError'>${titleError!""}</h6>
			</li>
			<li>
				<label for="firstName" class="bold">First Name:
					<span icon="required"></span>
				</label>
				<input id="firstName" name="firstName" type="text" value="${firstNameValue!""}">
				<h6 class="errorMsg" id='errorFirstName'>${firstNameError!""}</h6>
			</li>
			<li>
				<label for="lastName" class="bold">Last Name:
					<span icon="required"></span>
				</label>
				<input id="lastName" name="lastName" type="text" value="${lastNameValue!""}">
				<h6 class="errorMsg" id='errorLastName'>${lastNameError!""}</h6>
			</li>
			<li>
				<label for="organisation" class="bold">Organisation/Institution:
					<span icon="required"></span>
				</label>
				<input id="organisation" name="organisation" type="text" value="${organisationValue!""}">
				<h6 class="errorMsg" id='errorOrganisation'>${organisationError!""}</h6>
			</li>
			<li>
				<label for="position" class="bold">Professional Position:</label>
				<input id="position" name="position" type="text" size="40" value="${positionValue!""}">
                <h6 class="errorMsg" id='positionError'>${positionError!""}</h6>
			</li>
			<li>
				<label for="phone" class="bold">Phone Number:
					<span icon="required"></span>
				</label>
				<input id="phone" name="phone" type="text" value="${phoneValue!""}">
				<h6 class="errorMsg" id='errorPhone'>${phoneError!""}</h6>
			</li>
			<li>
				<label for="address" class="bold">Mailing Address:
					<span icon="required"></span>
				</label>
				<textarea id="address" name="address" cols="30" rows="4">${addressValue!""}</textarea>
				<h6 class="errorMsg" id='errorAddress'>${addressError!""}</h6>
			</li>
			<li>
				<label for="url" class="bold">URL:</label>
				<input id="url" name="url" type="text" value="${urlValue!""}">
                <h6 class="errorMsg" id='urlError'>${urlError!""}</h6>
			</li>

            <#if !isAdmin>
                <!-- display reCAPTCHA -->
                <li>
                <script type="text/javascript" src="http://www.google.com/recaptcha/api/challenge?k=${captchaPublicKey}&error=${captchaError!""}"></script>
                <noscript>
                    <iframe src="http://www.google.com/recaptcha/api/noscript?k=${captchaPublicKey}&error=${captchaError!""}" height="300" width="500" frameborder="0"></iframe><br>
                    <textarea name="recaptcha_challenge_field" rows="3" cols="40"></textarea>
                    <input type="hidden" name="recaptcha_response_field" value="manual_challenge">
                </noscript>
                </li>
            </#if>

		</ol>
	</div>
	
	<div id="buttonwrapper">
		<button type="submit">Create New User</button>
        <#if isAdmin>
        <a href="${baseUrl}/admin/user/list">Cancel</a>
        <#else>
        <a href="${baseUrl}/login">Cancel</a>
        </#if>
	</div>
</form>

<#if isAdmin>
</div>
</#if>

</div>  <!-- content pane -->