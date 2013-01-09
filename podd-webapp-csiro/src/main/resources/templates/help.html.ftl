<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="content" type="java.lang.String" -->

<div id="title_pane">
    <h3>Help</h3>
</div>

<div id="content_pane">
<#include "help_aux.html.ftl"/>

        <div id="main">
        <#if content??>
            <#include "help_${content}.html">
        <#else>
        <#--<#include "help_overview.html">-->
        </#if>
    </div>
</div>      <!-- content pane -->