<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="auditLogTypes" type="java.util.ArrayList<java.lang.String>" -->
<#-- @ftlvariable name="auditLogList" type="java.util.ArrayList<podd.model.audit.AuditLog>" -->
<#-- @ftlvariable name="selectedAuditLogType" type="java.lang.String" -->
<#-- @ftlvariable name="enteredFromDate" type="java.lang.String" -->
<#-- @ftlvariable name="fromDateError" type="java.lang.String" -->
<#-- @ftlvariable name="enteredToDate" type="java.lang.String" -->
<#-- @ftlvariable name="toDateError" type="java.lang.String" -->

<div id="title_pane">
    <h3>Audit Log</h3>
</div>

<div id="content_pane">
<#include "admin_aux.html.ftl"/>

<div id="main">

<p>
<h4 class="errorMsg">${errorMessage!""}</h4>

<div id="auditLog_filter">
	<form name="load_audit_log" action="${baseUrl}/admin/auditLog" method="POST" onsubmit="">
		<div id="typeFilter" class="fieldset">
			<div class="legend">Filter <a href="javascript:animatedcollapse.toggle('filterContent')" title="View Filter"><img src="${baseUrl}/images/toggle.png" alt=""></a></div>
			<div id="filterContent">
			<ol>
				<li class="bold">Log Type:</li>
				<li>
					<select name="log_type">
						<#if selectedAuditLogType?? && selectedAuditLogType == "no_selection">
							<option value="no_selection" selected> -- ALL -- </option>
						<#else>
							<option value="no_selection"> -- ALL -- </option>
						</#if>
						<#if  auditLogTypes??>
                        <#list auditLogTypes as log_type>
							<#if selectedAuditLogType?? && selectedAuditLogType == log_type>
								<option value="${log_type}" selected>${log_type}</option>
							<#else>
								<option value="${log_type}">${log_type}</option>
							</#if>
						</#list>
                        </#if>
					</select>
				</li>
			</ol>
            <ol>
                <li class="bold">From Date (DD/MM/YYYY hh:mm:ss):</li>
                <li><input id="fromDate" name="fromDate" type="text" value="${enteredFromDate!""}"></li>
                <h6 class="errorMsg" id='errorFromDate'>${fromDateError!""}</h6>
            </ol>
            <ol>
                <li class="bold">To Date (DD/MM/YYYY hh:mm:ss):</li>
                <li><input id="toDate" name="toDate" type="text" value="${enteredToDate!""}"></li>
				<h6 class="errorMsg" id='errorToDate'>${toDateError!""}</h6>
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

<div id="listAuditLog">
	<table id='table' class="tablesorter {sortlist: [[1,1]]}" cellspacing="0">
		<thead>
			<tr>
			    <th>Type</th>
			    <th>Date</th>
			    <th>General Message</th>
                <th>User</th>
			</tr>
		</thead>
		<tfoot>
			<tr id="pager">
				<td colspan="4">
                    <img class="first" src="${baseUrl}/images/btn_first.png" alt="first" title="First">
                    <img class="prev" src="${baseUrl}/images/btn_prev.png" alt="previous" title="Previous">
                    <label class="pagedisplay"></label>
                    <img class="next" src="${baseUrl}/images/btn_next.png" alt="next" title="Next">
                    <img class="last" src="${baseUrl}/images/btn_last.png" alt="last" title="Last">
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
			<#list auditLogList as audit_log>
			<tr>
				<td>${audit_log.getType()!""}</td>
				<td>${audit_log.getDateTimeForDisplay()!""}</td>
                <td><a href="${baseUrl}/admin/auditLogDetails/${audit_log.getId()}">
                    <script type="text/javascript">writeAbstractWholeWords("${audit_log.getGeneralMsg()!" - "}", 60);</script></a>
                </td>
                <td>${audit_log.getUsername()!""}</td>
			</tr>
			</#list>
		</tbody>
	</table>
</div>  <!-- list Audit Log -->

</div>  <!-- main -->

</div>  <!-- content pane -->

<script type="text/javascript" src="${baseUrl}/scripts/jquery.tablesorter.js"></script>
<script type="text/javascript" src="${baseUrl}/scripts/jquery.tablesorter.pager.js"></script>
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

<script type="text/javascript" src="${baseUrl}/scripts/animatedcollapse.js">
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