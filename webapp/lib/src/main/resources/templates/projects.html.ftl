<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="canFilter" type="boolean" -->
<#-- @ftlvariable name="hasFilter" type="boolean" -->
<#-- @ftlvariable name="userCanCreate" type="boolean" -->
<#-- @ftlvariable name="maxResults" type="int" -->
<#-- @ftlvariable name="myFirstRecord" type="int" -->
<#-- @ftlvariable name="myProjectCount" type="int" -->
<#-- @ftlvariable name="myProjectsList" type="java.util.ArrayList<podd.resources.util.view.ProjectListPopulator.FreemarkerProjectHelper>" -->
<#-- @ftlvariable name="publicFirstRecord" type="int" -->
<#-- @ftlvariable name="publicProjectCount" type="int" -->
<#-- @ftlvariable name="publicProjectsList" type="java.util.ArrayList<podd.resources.util.view.ProjectListPopulator.FreemarkerProjectHelper>" -->
<#-- @ftlvariable name="allFirstRecord" type="int" -->
<#-- @ftlvariable name="allProjectsCount" type="int" -->
<#-- @ftlvariable name="allProjectsList" type="java.util.ArrayList<podd.resources.util.view.ProjectListPopulator.FreemarkerProjectHelper>" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->

<div id="title_pane">
    <h3>Projects Listing</h3>
</div>

<div id="content_pane">

<div id="error">
    <h4 class="errorMsg">${errorMessage!""}</h4>
</div>

<div id="buttonwrapper_right">
	<#if userCanCreate?? && userCanCreate>
		<a href="${baseUrl}/object/new?type=Project">Create Project</a>
	</#if>
	<form id="removeFilterForm" method="POST" action="/podd/removeprojectsfilter" style="display:none">		
	</form>
	<#if hasFilter?? && hasFilter>
	<a href="javascript:void(0);" onclick="removeFilter()">Remove Filter</a>
    </#if>
    <#if canFilter?? && canFilter>
    <a href="${baseUrl}/projectsfilter/">Filter Projects</a>
    </#if>
</div>

<p><br></p>

<#if myProjectsList??>
	<#-- if the user is authenicated show the list of projects they have access to -->
	<h3 class="underlined_heading">My Projects 
		<a href="javascript:animatedcollapse.toggle('myProjects')" icon="toggle" title="View My Projects"></a>
	</h3>
    <div id="myProjects">
        <#list myProjectsList as project>
		    <@addProjectDetails aProject=project/>
		</#list>
        <#if myProjectCount?? && myFirstRecord?? && maxResults??>
        <@addPaging firstLabel="myFirst" firstRecord=myFirstRecord recordCount=myProjectCount otherRecords="&publicFirst=${publicFirstRecord!0}"/>
        </#if>
	</div>
	<p></p>
</#if>

<#if publicProjectsList??>
<h3 class="underlined_heading">Public Projects
	<a href="javascript:animatedcollapse.toggle('publicProjects')" icon="toggle" title="View Public Projects"></a>
</h3>
<div id="publicProjects">
	<#list publicProjectsList as project>
        <@addProjectDetails aProject=project/>
	</#list>
    <#if publicProjectCount?? && publicFirstRecord?? && maxResults??>
    <@addPaging firstLabel="publicFirst" firstRecord=publicFirstRecord recordCount=publicProjectCount otherRecords="&myFirst=${myFirstRecord!0}"/>
    </#if>
</div>
<p></p>
</#if>

<#if allProjectsList??>
<h3 class="underlined_heading">All Projects
	<a href="javascript:animatedcollapse.toggle('allProjects')" icon="toggle" title="View All Projects"></a>
</h3>
<div id="allProjects">
	<#list allProjectsList as project>
        <@addProjectDetails aProject=project/>
	</#list>
    <#if allProjectsCount?? && allFirstRecord?? && maxResults??>
    <@addPaging firstLabel="first" firstRecord=allFirstRecord recordCount=allProjectsCount otherRecords=""/>
    </#if>
</div>
<p></p>
</#if>


<#if ! (allProjectsList)?? && ! (publicProjectsList)?? && ! myProjectsList??>
No projects found
</#if>

</div>  <!-- content pane -->


<#macro addProjectDetails aProject>
    <p>
    <a href="${baseUrl}/artifact/base?artifacturi=${(aProject.getUri())!"unknown-pid"}"> ${aProject.getTitle()!" - "}</a>
<#-- 
	These should be uncommented and displayed.
	
        <#if "D" == aProject.getProject().state>
            (<span class="descriptive">deleted</span>)
        <#elseif "I" == aProject.getProject().state>
            (<span class="descriptive">inactive</span>)
        </#if>
        <#if aProject.authenticatedUserRole != "">
         (${aProject.authenticatedUserRole}).
        <#else>
         .
        </#if>
    Principal Investigator: ${(aProject.getPrincipalInvestigator().getFirstName())!" - "}
        ${(aProject.getPrincipalInvestigator().getLastName())!" - "},
    Lead Institution: ${(aProject.getLeadInstitution())!" - "} <br>
-->
    <script type="text/javascript">
        writeAbstractWholeWords("${(aProject.getDescription())!" - "}", 200);
    </script>
    <a href="${baseUrl}/artifact/base?artifacturi=${(aProject.getUri())!"unknown-pid"}">Browse Project</a>
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
    Showing project ${firstRecord + 1} to ${lastRecord} of ${recordCount} records.&nbsp;&nbsp;&nbsp;&nbsp;
    <#if firstRecord != 0>
    <a href="${baseUrl}/projects?${firstLabel}=${prevRecord}${otherRecords!""}">Prev</a>&nbsp;&nbsp;
    </#if>
    <#if lastRecord < recordCount>
    <a href="${baseUrl}/projects?${firstLabel}=${lastRecord}${otherRecords!""}">Next</a>
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
	animatedcollapse.addDiv('myProjects', 'fade=1,hide=0')
	animatedcollapse.addDiv('publicProjects', 'fade=1,hide=0')
    animatedcollapse.addDiv('allProjects', 'fade=1,hide=0')

	animatedcollapse.ontoggle=function($, divobj, state){ 
		//fires each time a DIV is expanded/contracted
		//$: Access to jQuery
		//divobj: DOM reference to DIV being expanded/ collapsed. Use "divobj.id" to get its ID
		//state: "block" or "none", depending on state
	}
	animatedcollapse.init()
</script>