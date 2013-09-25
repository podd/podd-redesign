

<h3 class="underlined_heading" id="startAttachDataReference">Attach Data Reference
    <a href="javascript:animatedcollapse.toggle('attach_dataReference')" icon="toggle" title="Attach Data References" name="attach_dataReferences"></a>
</h3>

<div id="attach_dataReference">  <!-- Collapsible div -->
	
</div>  <!-- file_upload_div - Collapsible div -->

<script type="text/javascript">
	$(document).ready(function() {
		
			
		podd.getDataRepositories(function(databank) {
			$("attach_dataReference").append(podd.populateDataRepositoriesList(databank));
		);
	});
</script>