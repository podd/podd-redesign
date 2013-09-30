
<#include "parent_details.html.ftl"/>

<h3 class="underlined_heading" id="startAttachDataReference">Attach Data Reference</h3>

<div id="attach_dataReference">  
	<div id="dataReferenceRepositoryList">
	</div>
	<div id="dataReferenceRepositoryDetails">
	</div>
	<div>
		<a id="verifyDataReferenceDetails" href="#">Verify details</a>
		<a id="saveDataReferenceDetails" href="#">Save</a>
	</div>
</div>  <!-- file_upload_div - Collapsible div -->

<script type="text/javascript">
	$(document).ready(function() {
		podd.getDataRepositories(function(databank) {
			podd.dataRepositoryDatabank = databank;
			var select = podd.createDataRepositoriesList(podd.dataRepositoryDatabank, undefined, undefined, true)
			$("#dataReferenceRepositoryList").append(select);
			// Update details when selection changes
			podd.addDataRepositoryHandler(select, $("#dataReferenceRepositoryDetails"), $("#verifyDataReferenceDetails"), $("saveDataReferenceDetails"), podd.dataRepositoryDatabank);
		});
		
	});
</script>