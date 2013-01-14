<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="target" type="podd.model.entity.PoddObject" -->
<#-- @ftlvariable name="results" type=java.util.List<org.json.JSONObject>" -->

<div id="title_pane">
<h3>Copy & Paste Results</h3>
</div>

<div id="content_pane">

<#if errorMessage??>
<div id="error">
    <h4 class="errorMsg">${errorMessage!""}</h4>
    <br>
</div>
</#if>

<#include "parent_details.html.ftl"/>

<h3 class="underlined_heading">Clipboard Objects Paste Status
    <a href="javascript:animatedcollapse.toggle('details')" icon="toggle" title="View Details"></a>
</h3>

<div id="details">
    <table id='table' class="tablesorter {sortlist: [[0,0]]}" cellspacing="0">
        <thead>
            <tr>
                <th>Title</th>
                <th>Type</th>
                <th>Depth</th>
                <th>Relationship</th>
                <th>Paste Status</th>
            </tr>
        </thead>
        <tbody>
            <#if results?? && results?has_content>
            <#list results as result>
                <tr>
                    <#if result.get("type") = "predicate">
                        <#if result.get("deleted")>
                        <td>${result.get("title")} (Objects: ${result.get("childCount")}, <span class="errorMsg">Deleted ${result.get("deletedCount")}</span>)</td>
                        <#else>
                        <td>${result.get("title")} (Objects: ${result.get("childCount")})</td>
                        </#if>
                    <#else>
                        <#if result.get("deleted")>
                        <td>${result.get("title")} (<span class="errorMsg">Deleted</span>)</td>
                        <#else>
                        <td>${result.get("title")}</td>
                        </#if>
                    </#if>
                    <td>${result.get("displayType")}</td>
                    <td>${result.get("depth")}</td>
                    <td>${result.get("predicate")}</td>
                    <#if result.get("status")>
                        <td>${result.get("statusMsg")}</td>
                    <#else>
                        <td><span class="errorMsg">${result.get("statusMsg")}</span></td>
                    </#if>
                </tr>
            </#list>
            </#if>
        </tbody>
    </table>
</div>      <!-- details - Collapsable div -->

<br>
<#include "hierarchy.html.ftl"/>

<script type="text/javascript" src="${baseUrl}/scripts/animatedcollapse.js">
    /* this needs to be placed at the top of the file so that we can add divs as they are created !!!! */
    /***********************************************
    * Animated Collapsible DIV v2.4- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
    * This notice MUST stay intact for legal use
    * Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
    ***********************************************/
</script>    
<script type="text/javascript">
    animatedcollapse.addDiv('parent_details', 'fade=1,hide=0');
    animatedcollapse.addDiv('details', 'fade=1,hide=0');
	animatedcollapse.addDiv('hierarchy', 'fade=1,hide=0');

	animatedcollapse.ontoggle=function($, divobj, state){
		//fires each time a DIV is expanded/contracted
		//$: Access to jQuery
		//divobj: DOM reference to DIV being expanded/ collapsed. Use "divobj.id" to get its ID
		//state: "block" or "none", depending on state
	};
	animatedcollapse.init();
</script>