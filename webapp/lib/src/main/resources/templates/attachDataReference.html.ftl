
<#include "parent_details.html.ftl"/>

<div id="content_pane">
<h3 class="underlined_heading" id="startAttachDataReference">Attach Data Reference</h3>

<h4 id="errorMsgHeader" class="errorMsg">${errorMessage!""}</h4>

<#-- add general error messages -->
<ol id="errorMsgList">
	<#if generalErrorList?? && generalErrorList?has_content>
	    <#list generalErrorList as errorMsg>
	    <li class="errorMsg">${errorMsg}</li>
	    </#list>
	</#if>
</ol>

<div id="attach_dataReference">  
	<div id="dataReferenceRepositoryList">
	</div>
	<div id="dataReferenceRepositoryDetails">
	</div>
</div>  <!-- file_upload_div - Collapsible div -->

</div>

<script type="text/javascript">
	$(document).ready(function() {
		podd.objectUri = '${parentObject.uri!"undefined"}';
		podd.artifactIri = '${artifactIri!"undefined"}';
		podd.versionIri = '${versionIri!"undefined"}';
		
		podd.getDataRepositories(function(databank) {
			podd.dataRepositoryDatabank = databank;
			var select = podd.createDataRepositoriesList(podd.dataRepositoryDatabank, undefined, undefined, true)
			$("#dataReferenceRepositoryList").append(select);
			// Update details when selection changes
			podd.addDataRepositoryHandler(select, $("#dataReferenceRepositoryDetails"), podd.dataRepositoryDatabank);
		});
	});
</script>