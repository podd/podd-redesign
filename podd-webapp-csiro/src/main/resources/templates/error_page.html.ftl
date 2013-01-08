<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="title" type="java.lang.String" -->
<#-- @ftlvariable name="error_code" type="java.lang.String" -->
<#-- @ftlvariable name="message" type="java.lang.String" -->
<div id="title_pane">
    <h3>ERROR: ${error_code!""}</h3>
</div>

<div id="content_pane">
    <h4 class=errorMsg>ERROR: ${error_code!""}</h4>

    <p>${message!""}
</div>  <!-- content pane -->