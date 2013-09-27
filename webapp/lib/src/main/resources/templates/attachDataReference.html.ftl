
<#include "parent_details.html.ftl"/>

<h3 class="underlined_heading" id="startAttachDataReference">Attach Data Reference</h3>

<div id="attach_dataReference">  <!-- Collapsible div -->
	
</div>  <!-- file_upload_div - Collapsible div -->

<script type="text/javascript">
	$(document).ready(function() {
		podd.getDataRepositories(function(databank) {
			$("attach_dataReference").append(podd.createDataRepositoriesList(databank));
		);
	});
</script>