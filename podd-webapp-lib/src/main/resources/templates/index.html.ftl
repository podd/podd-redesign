<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="user" type="podd.model.user.User" -->

<div id="title_pane">
    <p></p>
</div>

<div id="content_pane">
<#if user??>
    <p>Welcome, ${user.firstName!""}  ${user.lastName!""}.</p>
    <p>Places to go to: <a href="${baseUrl}/user/${user.userName!"unknown-username"}">User page</a></p>
<#else>
    <p>Welcome to PODD, please <a href="${baseUrl}/login">login</a>.</p>
</#if>
</div>  <!-- content pane -->