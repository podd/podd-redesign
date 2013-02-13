<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="title" type="java.lang.String" -->
<#-- @ftlvariable name="selectedStatus" type="java.lang.String" -->
<#-- @ftlvariable name="statusList" type="java.util.ArrayList<java.lang.String>" -->
<#-- @ftlvariable name="selectedRepositoryRole" type="podd.model.user.RepositoryRole" -->
<#-- @ftlvariable name="roleObjectList" type="java.util.ArrayList<podd.model.user.RepositoryRole>" -->
<#-- @ftlvariable name="usernameValue" type="java.lang.String" -->
<#-- @ftlvariable name="emailValue" type="java.lang.String" -->
<#-- @ftlvariable name="titleValue" type="java.lang.String" -->
<#-- @ftlvariable name="firstNameValue" type="java.lang.String" -->
<#-- @ftlvariable name="lastNameValue" type="java.lang.String" -->
<#-- @ftlvariable name="organisationValue" type="java.lang.String" -->
<#-- @ftlvariable name="positionValue" type="java.lang.String" -->
<#-- @ftlvariable name="phoneValue" type="java.lang.String" -->
<#-- @ftlvariable name="addressValue" type="java.lang.String" -->
<#-- @ftlvariable name="urlValue" type="java.lang.String" -->
<#-- @ftlvariable name="sharedToken" type="java.lang.String" -->
<#-- @ftlvariable name="targetID" type="java.lang.String" -->
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
<#-- @ftlvariable name="loginError" type="java.lang.String" -->

<div id="title_pane">
    <h3>${title}</h3>
</div>

<div id="content_pane">
<p>
<h4 class="errorMsg">${errorMessage!""}</h4>

<script type="text/javascript">
    var RecaptchaOptions = {
        theme : 'white'
    };
</script>

<h3 class="underlined_heading">Merge Accounts</h3>
<div>
	<br />
	If you have an existing PODD Username and wish to use your institutional login<br/> 
	instead, you may merge these accounts by supplying your PODD Username and<br /> 
	Password. You can subsequently login using either credentials.
</div>
<form name="create_user" enctype="multipart/form-data" action="${baseUrl}/aaf?merge" method="POST">
	<div class="fieldset_without_border">
	
	<label style="width: 75px">Username:</label>
	<input id="user" name="username" type="text" value="">
	<br />
	<br />
	<label style="width: 75px">Password:</label>
	<input id="password" name="password" type="password">
	<br />
	<br />
	<button type="submit">Merge</button>
	<h6 class="errorMsg" id='loginError'>${loginError!""}</h6>
	<br />
	<br />
	</div>
</form>
<form name="create_user" enctype="multipart/form-data" action="${baseUrl}/aaf?create" method="POST" onsubmit="return validateUserInfo()">
	<h3 class="underlined_heading">AAF Registration</h3>
	<div>
		<br />
		New users accessing PODD for the first time via the AAF are required to provide user<br /> 
		registration information. Please confirm or update theses details.<br />
	</div>
	<div id="admin_left_pane" class="fieldset_without_border">
		<div class="legend_no_indent">Account Details</div>
		<ol>
			<li>
				<label for="email" class="bold">Email Address:
					<span icon="required"></span>
				</label>
				<input id="email" name="email" type="text" value="${emailValue!""}">
				<h6 class="errorMsg" id='errorEmail'>${emailError!""}</h6>
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
		</ol>
		<input type="hidden" name="sharedToken" value="${sharedToken!""}" />
		<input type="hidden" name="targetID" value="${targetID!""}" />
	</div>
	
	<div id="buttonwrapper">
		<button type="submit">Confirm Details</button>
	</div>
</form>

</div>  <!-- content pane -->