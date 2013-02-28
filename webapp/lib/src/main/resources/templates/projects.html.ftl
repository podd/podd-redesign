<#-- @ftlvariable name="canFilter" type="boolean" -->
<#-- @ftlvariable name="hasFilter" type="boolean" -->
<#-- @ftlvariable name="userCanCreate" type="boolean" -->
<#-- @ftlvariable name="maxResults" type="int" -->
<#-- @ftlvariable name="myFirstRecord" type="int" -->
<#-- @ftlvariable name="myArtifactCount" type="int" -->
<#-- @ftlvariable name="myArtifactsList" type="java.util.List<com.github.podd.utils.PoddObjectLabel>" -->
<#-- @ftlvariable name="publicFirstRecord" type="int" -->
<#-- @ftlvariable name="publicArtifactCount" type="int" -->
<#-- @ftlvariable name="publicArtifactsList" type="java.util.List<com.github.podd.utils.PoddObjectLabel>" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->

<div id="title_pane">
    <h3>Artifacts Listing</h3>
</div>

<div id="content_pane">

<div id="error">
    <h4 class="errorMsg">${errorMessage!""}</h4>
</div>

<div id="buttonwrapper_right">
	<#if userCanCreate?? && userCanCreate>
		<a href="${baseUrl}/artifact/new">Upload a new Artifact</a>
	</#if>
	<form id="removeFilterForm" method="POST" action="/podd/removeartifactsfilter" style="display:none">		
	</form>
	<#if hasFilter?? && hasFilter>
	<a href="javascript:void(0);" onclick="removeFilter()">Remove Filter</a>
    </#if>
    <#if canFilter?? && canFilter>
    <a href="${baseUrl}/artifactsfilter/">Filter Artifacts</a>
    </#if>
</div>

<p><br></p>

<#if myArtifactsList??>
	<#-- if the user is authenticated show the list of artifacts they have access to -->
	<h3 class="underlined_heading">My Artifacts 
		<a href="javascript:animatedcollapse.toggle('myArtifacts')" icon="toggle" title="View My Artifacts"></a>
	</h3>
    <div id="myArtifacts">
        <#list myArtifactsList as artifact>
		    <@addArtifactDetails aArtifact=artifact/>
		</#list>
        <#if myArtifactCount?? && myFirstRecord?? && maxResults??>
        <@addPaging firstLabel="myFirst" firstRecord=myFirstRecord recordCount=myArtifactCount otherRecords="&publicFirst=${publicFirstRecord!0}"/>
        </#if>
	</div>
	<p></p>
</#if>

<#if publicArtifactsList??>
<h3 class="underlined_heading">Public Artifacts
	<a href="javascript:animatedcollapse.toggle('publicArtifacts')" icon="toggle" title="View Public Artifacts"></a>
</h3>
<div id="publicArtifacts">
	<#list publicArtifactsList as artifact>
        <@addArtifactDetails aArtifact=artifact/>
	</#list>
    <#if publicArtifactCount?? && publicFirstRecord?? && maxResults??>
    <@addPaging firstLabel="publicFirst" firstRecord=publicFirstRecord recordCount=publicArtifactCount otherRecords="&myFirst=${myFirstRecord!0}"/>
    </#if>
</div>
<p></p>
</#if>

<#if ! (allArtifactsList)?? && ! (publicArtifactsList)?? && ! myArtifactsList??>
No artifacts found
</#if>

</div>  <!-- content pane -->


<#macro addArtifactDetails aArtifact>
    <p>
    <a href="${baseUrl}/artifact/base?artifacturi=${(aArtifact.getUri())!"unknown-pid"}"> ${aArtifact.getTitle()!" - "}</a>
<#-- 
	These should be uncommented and displayed.
	
        <#if "D" == aArtifact.getArtifact().state>
            (<span class="descriptive">deleted</span>)
        <#elseif "I" == aArtifact.getArtifact().state>
            (<span class="descriptive">inactive</span>)
        </#if>
        <#if aArtifact.authenticatedUserRole != "">
         (${aArtifact.authenticatedUserRole}).
        <#else>
         .
        </#if>
    Principal Investigator: ${(aArtifact.getPrincipalInvestigator().getFirstName())!" - "}
        ${(aArtifact.getPrincipalInvestigator().getLastName())!" - "},
    Lead Institution: ${(aArtifact.getLeadInstitution())!" - "} <br>
-->
    <script type="text/javascript">
        writeAbstractWholeWords("${(aArtifact.getDescription())!" - "}", 200);
    </script>
    <a href="${baseUrl}/artifact/base?artifacturi=${(aArtifact.getUri())!"unknown-pid"}">Browse Artifact</a>
    </p>
</#macro>

<#macro addPaging firstLabel firstRecord recordCount otherRecords>
    <#assign lastRecord = firstRecord + maxResults>
    <#if recordCount < lastRecord>
    <#assign lastRecord = recordCount>
    </#if>
    <#assign prevRecord = firstRecord - maxResults>
    <#if prevRecord < 0>
    <#assign prevRecord = 0>
    </#if>
    <#if recordCount != 0>
    <br>
    Showing artifact ${firstRecord + 1} to ${lastRecord} of ${recordCount} records.&nbsp;&nbsp;&nbsp;&nbsp;
    <#if firstRecord != 0>
    <a href="${baseUrl}/artifacts?${firstLabel}=${prevRecord}${otherRecords!""}">Prev</a>&nbsp;&nbsp;
    </#if>
    <#if lastRecord < recordCount>
    <a href="${baseUrl}/artifacts?${firstLabel}=${lastRecord}${otherRecords!""}">Next</a>
    </#if>
    </#if>
</#macro>


<script type="text/javascript" src="${baseUrl}/scripts/animatedcollapse.js">
/***********************************************
* Animated Collapsible DIV v2.4- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
***********************************************/
</script>

<script type="text/javascript">
	animatedcollapse.addDiv('myArtifacts', 'fade=1,hide=0')
	animatedcollapse.addDiv('publicArtifacts', 'fade=1,hide=0')
    animatedcollapse.addDiv('allArtifacts', 'fade=1,hide=0')

	animatedcollapse.ontoggle=function($, divobj, state){ 
		//fires each time a DIV is expanded/contracted
		//$: Access to jQuery
		//divobj: DOM reference to DIV being expanded/ collapsed. Use "divobj.id" to get its ID
		//state: "block" or "none", depending on state
	}
	animatedcollapse.init()
</script>