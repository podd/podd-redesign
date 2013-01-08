<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="requestedPid" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->

<div id="title_pane">
    <#if errorMessage??>
    <h3 class="errorMsg">Error Unpublishing Project</h3>
    <#else>
    <h3>Project Unpublished</h3>
    </#if>
</div>


<div id="content_pane">
    <#if errorMessage??>
        <p>An error has occurred while attempting to unpublish your project: ${requestedPid}.</p>
        <p>${errorMessage!""}</p>
    <#else>
        <p>Your project has been successfully unpublished.</p>
    </#if>

    <div id="buttonwrapper">
    <#if requestedPid??>
    <a href="${baseUrl}/object/${requestedPid}">Back to Project</a>
    </#if>
</div>


</div>  <!-- content pane -->