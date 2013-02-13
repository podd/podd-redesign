<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="requestedId" type="java.lang.String" -->
<#-- @ftlvariable name="auditLogDetails" type="podd.model.audit.AuditLog" -->

<div id="title_pane">
    <h3>Audit Log Details</h3>
</div>

<div id="content_pane">
    <#include "admin_aux.html.ftl"/>

    <div id="main">
        <#if  errorMessage??>
        <p>
        <h4 class="errorMsg">${errorMessage!""}</h4>
        </#if>

        <div id="audit_log_details" class="fieldset">
            <div class="legend">Audit Log Details</div>
            <ol>
            <#if  auditLogDetails??>
                <li><span class="bold">Type: </span>${auditLogDetails.getType()!""}</li>
                <li><span class="bold">Date: </span>${auditLogDetails.getDateTimeForDisplay()!""}</li>
                <li><span class="bold">General Message: </span>${auditLogDetails.getGeneralMsg()!""}</li>
                <li><span class="bold">Detailed Message: </span>${auditLogDetails.getDetailedMsg()!""}</li>
                <li><span class="bold">User: </span>${auditLogDetails.getUsername()!""}</li>
                <li><span class="bold">IP Address: </span>${auditLogDetails.getIp()!""}</li>
            <#else>
                <li><h4 class="errorMsg">Audit Log: ${requestedId} could not be found</h4></li>
            </#if>
            </ol>
        </div>
        <div id="buttonwrapper">
            <a href="${baseUrl}/admin/auditLog">Back</a>
        </div>
    </div>  <!-- main -->
</div>  <!-- content pane -->