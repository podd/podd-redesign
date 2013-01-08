<div id="content_pane">
	<h1>Imported Successfully</h1>
		
	<p>
	<div>The following objects were processed: </div>
	</p>		
	<br/>	
	<#list tabObjects as object>
		<div style="padding: 2px; padding-left: 10px;" >	
			<a href="${baseUrl}/object/${object.fragment}" >${object.fragment}</a>		
		</div>
	</#list>	
	
</div>