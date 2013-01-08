<#-- @ftlvariable name="springRedirect" type="java.lang.String" -->
<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="selfRegistrationEnabled" type="java.lang.Boolean" -->
<#-- @ftlvariable name="shibbolethEnabled" type="java.lang.Boolean" -->
<#-- @ftlvariable name="invalidDomain" type="java.lang.Boolean" -->
<#-- @ftlvariable name="message" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="detailedMessage" type="java.lang.String" -->

<div id="title_pane">
    <h3>Login</h3>
</div>

<div id="content_pane">
	<#if shibbolethEnabled>
	<table style="width: 100%; margin:10px 10px 15px">
		<tr>
			<td style="width: 400px">
				<div>
					PODD user are required to authenticate via an<br/>
					Australian Access Federation Identity Provider<br />
					organisation <br /> 
				</div>
			</td>
			<td>

			</td>
			<td style="width: 400px">
				<div>
					If your identity provider is not listed you may login<br />
					using a PODD username and password<br />
				</div>
			</td>	
		</tr>
		<tr>
			<td style="width: 400px">
			    <form name="aaf" action="${baseUrl}/aaf" method="POST">
				    <div class="fieldset" id="login" style="width: 340px; height: 180px; text-align: center">
						<div class="legend" style="text-align: left">Login with AAF username and password</div>
						<br />
						<a href="${baseUrl}/aaf" style="outline: 0"><img id="AAFLOGINLOGO"  src="${baseUrl}/images/AAF.png" align="middle" style="vertical-align: middle; border-style: solid; border-width: 1px"></a>
				  	</div>
			  	</form>
			</td>
			<td>
			</td>
			<td style="width: 400px">
			    <form name="f" action="${baseUrl}/j_spring_security_check" method="POST">
				    <div class="fieldset" id="login" style="width: 327px; height: 180px; text-align: centre">
						<div class="legend">Login with PODD username and password</div>
						<ol> 
							<li> 
								<label for="user" class="bold">User: </label>
								<input id="user" class="medium" name="j_username" type="text" value=""> 
							</li> 
							<li> 
								<label for="password" class="bold">Password:</label>
								<input id="password" class="medium" name="j_password" type="password">
							</li>
							<li> 
								<div class="radioGroup">
								    <label id="rememberMeLabel" for="_spring_security_remember_me">Remember me on this computer.</label>
								    <input id="_spring_security_remember_me" class="narrow" name="_spring_security_remember_me" type="checkbox" value="true">
								</div>
					        </li>
					        <#if  springRedirect?? && springRedirect !="">
				                <li>
			                        <input type="hidden" name="spring-security-redirect" value="${springRedirect}"/>
				                </li>
			                </#if>
			            </ol>
			            <div id="buttonwrapper">
							<button type="submit">login</button>
			            	<#if selfRegistrationEnabled?? && selfRegistrationEnabled>
			            		<a href="${baseUrl}/admin/user/create?init=true">Register</a>
			            	</#if>
						</div>
						<br />
						<br />
						<br />
			        </div>
			    </form>
			</td>
		</tr>
		<tr>
			<td style="width: 400px">
				<div>
					If you do not remember your identity provider username or<br/>
					password, please contact your identity provider's IT support<br />
					for assistance<br />
					<br />
				<#if invalidDomain>
					<h4 class="errorMsg">
						Your Identity Provider has not provided sufficient information
						to PODD for you to be registered as a PODD user or log in to your 
						existing account. If you wish to register for a PODD login, or wish 
						to report an error in the login process, please contact PODD support. </h4>
            	</#if>
            	</div>
			</td>
			<td>
			
			</td>
			<td style="width: 400px" valign="top">
				<div>
					If you do not remember your PODD username or password <br />
					or wish to register for a new login please contact <a href="${baseUrl}/supportDesk" >Support</a><br />
				</div>
				<br />
				<br />
				<ol >
            	<#if errorMessage?? && errorMessage?has_content>
                	<li style="list-style: none"><h4 class="errorMsg">${errorMessage}</h4></li>
            	</#if>
            	<#if message?? && message?has_content>
            	    <li style="list-style: none"><h4>${message!""}</h4></li>
            	</#if>
            	<#if detailedMessage?? && detailedMessage?has_content>
            	    <li style="list-style: none"><p>${detailedMessage}</p></li>
            	</#if>
				</ol>
			</td>	
		</tr>
	</table>
	<#else>
    <form name="f" action="${baseUrl}/j_spring_security_check" method="POST">
    <div class="fieldset" id="login">
		<div class="legend">Login with PODD username and password</div>
		<ol> 
			<li> 
				<label for="user">User: </label> 
				<input id="user" class="medium" name="j_username" type="text" value=""> 
			</li> 
			<li> 
				<label for="password">Password:</label> 
				<input id="password" class="medium" name="j_password" type="password">
			</li>
            <#if errorMessage?? && errorMessage?has_content>
                <li><h4 class="errorMsg">${errorMessage}</h4></li>
            </#if>
            <#if message?? && message?has_content>
                <li><h4>${message!""}</h4></li>
            </#if>
            <#if detailedMessage?? && detailedMessage?has_content>
                <li><p>${detailedMessage}</p></li>
            </#if>
			<li> 
				<div class="radioGroup">
				    <label id="rememberMeLabel" for="_spring_security_remember_me">Remember me on this computer.</label>
				    <input id="_spring_security_remember_me" class="narrow" name="_spring_security_remember_me" type="checkbox" value="true">
				</div>
	        </li>
            <li>
                <#if  springRedirect?? && springRedirect !="">
                    <input type="hidden" name="spring-security-redirect" value="${springRedirect}"/>
                </#if>
            </li>
        </ol>
    </div>
	
	<div id="buttonwrapper">
		<button type="submit">login</button>
        <#if selfRegistrationEnabled?? && selfRegistrationEnabled>
        <a href="${baseUrl}/admin/user/create?init=true">Register</a>
        </#if>
	</div>
    </form>	
	</#if>
</div>  <!-- content pane -->