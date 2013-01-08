<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="requestedPid" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->

<div id="title_pane">
    <#if errorMessage??>
    <h3 class="errorMsg">Error Generating PURLs for Project</h3>
    <#else>
    <h3>Generating Persistent URLs for Project</h3>
    </#if>
</div>


<div id="content_pane">
    <#if errorMessage??>
        <p>An error has occurred while attempting to generate Persistent URLs for your project: ${requestedPid}.</p>
        <p>${errorMessage!""}</p>
    <#else>
        <p>Persistent URLs are being generated for your project.</p>
        <p>You will receive and email containing the PURL for the project when one has been allocated from ANDS.</p>
    </#if>

    <div id="buttonwrapper">
    <#if requestedPid??>
    <a href="${baseUrl}/object/${requestedPid}">Back to Project</a>
    </#if>
</div>

</div>  <!-- content pane -->