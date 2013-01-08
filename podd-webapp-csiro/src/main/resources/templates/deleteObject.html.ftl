<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="doDelete" type="boolean" -->
<#-- @ftlvariable name="doUndelete" type="boolean" -->
<#-- @ftlvariable name="canCancel" type="boolean" -->
<#-- @ftlvariable name="title" type="java.lang.String" -->
<#-- @ftlvariable name="subHeading" type="java.lang.String" -->
<#-- @ftlvariable name="pid" type="java.lang.String" -->
<#-- @ftlvariable name="objectType" type="java.lang.String" -->
<#-- @ftlvariable name="poddObject" type="podd.model.entity.PoddObject" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->

<div id="title_pane">
    <h3>${title!"Delete Object"}</h3>
</div>

<div id="content_pane">

<#if errorMessage?? && errorMessage != "">
<p><h4 class="errorMsg">${errorMessage!""}</h4></p>
<#else>
<br>
</#if>

<#include "parent_details.html.ftl"/>

<div id="details">  <!-- Collapsible div -->

    <#if  poddObject??>
        <h3 class="underlined_heading">${subHeading!""}</h3>
        <div class="fieldset">
            <ol>
                <li><span class="bold">ID: </span><a href="${baseUrl}/object/${poddObject.getPid()!""}">${poddObject.getPid()!""}</a></li>
                <li><span class="bold">Type: </span>${poddObject.getConcept().getConceptName()!""}</li>
                <li><span class="bold">Title: </span>${poddObject.getLocalName()!""}</li>
                <li><span class="bold">Description: </span>${poddObject.getLabel()!""}</li>
            </ol>
        </div>
    </#if>

    <div id="buttonwrapper">
        <#if poddObject??>
            <#if canCancel?? && canCancel>
                <form name="delete_object" action="${baseUrl}/object/${poddObject.getPid()}/delete" method="POST">
                <#if doDelete?? && doDelete>
                    <button type="submit" name="delete" value="delete">Delete Object</button>
                </#if>
                <#if doUndelete?? && doUndelete>
                    <button type="submit" name="undelete" value="undelete">Restore Object</button>
                </#if>
                </form>
                <a href="${baseUrl}/object/${poddObject.getPid()!""}">Cancel</a>
            <#else>
                <#if doDelete?? && doDelete>
                    <a href="${baseUrl}/object/${poddObject.getPid()!"unknown-pid"}/delete">Delete</a>
                </#if>
                <#if doUndelete?? && doUndelete>
                    <a href="${baseUrl}/object/${poddObject.getPid()!"unknown-pid"}/delete">Undelete</a>
                </#if>
            </#if>
        </#if>
    </div>
</div>  <!-- details -->
</div>  <!-- content pane -->

<script type="text/javascript" src="${baseUrl}/scripts/animatedcollapse.js">
/***********************************************
* Animated Collapsible DIV v2.4- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
***********************************************/
</script>

<script type="text/javascript">
    animatedcollapse.addDiv('parent_details', 'fade=1,hide=0');
    animatedcollapse.addDiv('object_details', 'fade=1,hide=0');

	animatedcollapse.ontoggle=function($, divobj, state){
		//fires each time a DIV is expanded/contracted
		//$: Access to jQuery
		//divobj: DOM reference to DIV being expanded/ collapsed. Use "divobj.id" to get its ID
		//state: "block" or "none", depending on state
	};
	animatedcollapse.init();
</script>