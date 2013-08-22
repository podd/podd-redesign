<#-- @ftlvariable name="URIUtil" type="freemarker.template.TemplateHashModel" -->
<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="institutionList" type="java.util.ArrayList<java.lang.String>" -->
<#-- @ftlvariable name="statusList" type="java.util.ArrayList<java.lang.String>" -->
<#-- @ftlvariable name="roleObjectList" type="java.util.ArrayList<podd.model.user.RepositoryRole>" -->
<#-- @ftlvariable name="userObjectList" type="java.util.ArrayList<podd.model.user.User>" -->
<#-- @ftlvariable name="selectedInstitution" type="java.lang.String" -->
<#-- @ftlvariable name="selectedRole" type="java.lang.String" -->
<#-- @ftlvariable name="selectedStatus" type="java.lang.String" -->

<div id="title_pane">
    <h3>List Users</h3>
</div>

<div id="content_pane">
<#include "admin_aux.html.ftl"/>

<div id="main">

<p>
<h4 class="errorMsg">${errorMessage!""}</h4>

<div id="filter">
	<form name="list_user" action="${baseUrl}/admin/user/list" method="POST" onsubmit="">
		<div id="userFilter" class="fieldset">
			<div class="legend">Filter <a href="javascript:animatedcollapse.toggle('filterContent')" title="View Filter"><img src="${baseUrl}/resources/images/toggle.png" alt=""></a></div>
			<div id="filterContent">
			<ol>
				<li class="bold">Institution:</li>
				<li>
					<select name="institution">
						<#if selectedInstitution?? && selectedInstitution == "no_selection">
							<option value="no_selection" selected> -- ALL -- </option>
						<#else>
							<option value="no_selection"> -- ALL -- </option>
						</#if>
						<#if  institutionList??>
                        <#list institutionList as institution>
							<#if selectedInstitution?? && selectedInstitution == institution>
								<option value="${institution}" selected>${institution}</option>
							<#else>
								<option value="${institution}">${institution}</option>
							</#if>
						</#list>
                        </#if>
					</select>
				</li>
			</ol>
			<ol>
				<li class="bold">Role:</li>
				<li>
					<select name="role">
						<#if selectedRole?? && selectedRole == "no_selection">
							<option value="no_selection" selected> -- ALL -- </option>
						<#else>
							<option value="no_selection"> -- ALL -- </option>
						</#if>
						<#if roleObjectList??>
                        <#list roleObjectList as role>
							<#if selectedRole?? && selectedRole == role.name!"">
								<option value="${role.URI!""}" selected>${role.getName()!""}</option>
							<#else>
								<option value="${role.URI!""}">${role.getName()!""}</option>
							</#if>
						</#list>
                        </#if>
					</select>
				</li>
			</ol>
			<ol>
				<li class="bold">Status:</li>
				<li>
					<select name="status">
						<#if selectedStatus?? && selectedStatus == "no_selection">
							<option value="no_selection" selected> -- ALL -- </option>
						<#else>
							<option value="no_selection"> -- ALL -- </option>
						</#if>
                        <#if statusList??>
						<#list statusList as status>
							<#if selectedStatus?? && selectedStatus == status>
								<option value="${status.URI}" selected>${status.label}</option>
							<#else>
								<option value="${status.URI}">${status.label}</option>
							</#if>
						</#list>
                        </#if>
					</select>
				</li>
			</ol>
			<ol>
				<li>
					<button type="submit">Filter</button>
				</li>
			</ol>
			</div> <!-- filter content -->
		</div>
	</form>
</div> <!-- filter -->
	
<div id="listUsers">
	<table id='table' class="tablesorter {sortlist: [[0,0]]}" cellspacing="0"> 
		<thead> 
			<tr> 
			    <th>UserName</th> 
			    <th>Full Name</th> 
			    <th>Organization</th> 
			    <th>Status</th> 
			    <th>Last Access</th>
			</tr> 
		</thead>
		<tfoot>
			<tr id="pager">
				<td colspan="6">
                    <img class="first" src="${baseUrl}/resources/images/btn_first.png" alt="first" title="First">
                    <img class="prev" src="${baseUrl}/resources/images/btn_prev.png" alt="previous" title="Previous">
                    <label class="pagedisplay"></label>
                    <img class="next" src="${baseUrl}/resources/images/btn_next.png" alt="next" title="Next">
                    <img class="last" src="${baseUrl}/resources/images/btn_last.png" alt="last" title="Last">
					<select class="pagesize"> 
						<option value="10" selected>10</option>
						<option value="20">20</option> 
						<option value="30">30</option> 
						<option value="40">40</option> 
					</select> 
				</td>
			</tr>
		</tfoot>
		<tbody>
			<#list userObjectList as user>
			<tr>
				<td><a href="${baseUrl}/user/${user.identifier!"unknown-username"}">${user.identifier!""}</a></td>
				<td>${user.firstName!""} ${user.lastName!""}</td>
				<td>${user.organization!""}</td>
				<td>${user.userStatus!""}</td>
				<#if user.lastLoginTimeAsDate??>
					<td>${user.lastLoginTimeAsDate?datetime?string.medium_short}</td>
				<#else>
					<td></td>
				</#if>
			</tr>
			</#list>
		</tbody> 
	</table> 
</div>  <!-- list users -->

</div>  <!-- main -->

</div>  <!-- content pane -->

<script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.tablesorter.js"></script>
<script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.tablesorter.pager.js"></script>
<script type="text/javascript">
	$(document).ready(function() { 
		$("#table")
		.tablesorter({
            widthFixed: true,
            sortMultiSortKey: 'ctrlKey'
        })
		.tablesorterPager({container: $("#pager"), positionFixed: false}); 
	});
</script>

<script type="text/javascript" src="${baseUrl}/resources/scripts/animatedcollapse.js">
/***********************************************
* Animated Collapsible DIV v2.4- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
***********************************************/
</script>

<script type="text/javascript">
	animatedcollapse.addDiv('filterContent', 'fade=1,hide=0')

	animatedcollapse.ontoggle=function($, divobj, state){ 
		//fires each time a DIV is expanded/contracted
		//$: Access to jQuery
		//divobj: DOM reference to DIV being expanded/ collapsed. Use "divobj.id" to get its ID
		//state: "block" or "none", depending on state
	}
	animatedcollapse.init()
	
	// fix to make empty cells appear to have a border in IE
	$(document).ready(function() {
      $("td:empty").html("&nbsp");
    });
</script>