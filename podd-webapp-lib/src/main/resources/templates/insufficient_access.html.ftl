<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="user" type="podd.model.user.User" -->

<div id="title_pane">
    <h3>Insufficient Access</h3>
</div>

<div id="content_pane">
<#if user??>
    <p>You have insufficient privileges to access the requested page.</p>
    <p>If you wish to continue you will need to <a href="${baseUrl}/j_spring_security_logout">logout</a>
    and login with a higher access level.</p>
<#else>
    <p>You must login to access the requested page.</p>
    <p><a href="${baseUrl}/login">login</a></p>
</#if>
</div>  <!-- content pane -->