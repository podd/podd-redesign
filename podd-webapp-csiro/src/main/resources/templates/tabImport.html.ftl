<#-- @ftlvariable name="fileList" type="java.util.ArrayList<podd.resources.util.view.FileElement>" -->

<style type="text/css">

.tabIcon {
	display:inline-block;
	position:relative;	
	height:16px;
	width:16px;	
}

.iconCheckmark {
	clip:rect(144px, 80px, 160px, 64px); /* (top, right, bottom, left)*/
	top: -144px;
	left: -64px;
}

.tabIcon img{
	position:absolute;
}

</style>
<link type="text/css" href='${baseUrl}/styles/jquery-ui-1.7.3.custom.css' rel="stylesheet" />
<script type="text/javascript" src="${baseUrl}/scripts/jquery-ui-1.7.3.custom/development-bundle/ui/ui.core.js"></script>
<script type="text/javascript" src="${baseUrl}/scripts/jquery-ui-1.7.3.custom/development-bundle/ui/ui.progressbar.js"></script>
<script type="text/javascript">

	function doSubmit(action) {
		var formEl = document.forms.create_project;
		
		var inputEl = document.createElement('input');
		inputEl.type="hidden";
		inputEl.name="action";
		inputEl.value=action;
		
		formEl.appendChild(inputEl);
		formEl.submit();
	}
	
	var intervalId;
	function checkStatus() {
	
		intervalId = setInterval(retrieveStatus, 3000);	
	
	}

	function retrieveStatus() {

        $.ajax({
            type: "GET",
            url: "${baseUrl}/tabImport",
            data: "action=status",            
            success: updateStatus,
            error: function(xhr, status, error) {
                alert('Error retrieving status: ' + status);
            }
        });		

	}

	function updateStatus(json) {		
		
		var parsedJson =  eval('(' + json + ')');
		
		var numObjectsCompleted = parsedJson.numObjectsCompleted;
		var totalNumObjects = parsedJson.totalNumObjects;	
		var error = parsedJson.error;
		
		var objectCountEl = document.getElementById('objectCount')
		if (error != null && error.length > 0) {			
			// Redirect to display the error
			//window.location="${baseUrl}/tabImport";
			$("#loading").hide();			
			objectCountEl.innerHTML="ERROR: "+error;
			objectCountEl.style.color='red';
			objectCountEl.style.fontWeight='bold';
			clearInterval(intervalId);
		} else {
			var progress = parseInt(numObjectsCompleted)/parseInt(totalNumObjects) * 100;
			//alert("progress="+progress);
			
			
			document.getElementById('loadingMsg').innerHTML="Working ...";
			//$("#progressbar").progressbar({ value: progress });
	
			// Update the object count
			objectCountEl.innerHTML='Number of objects completed: '+numObjectsCompleted+ ' out of '+totalNumObjects;		
	
			// Stop the timeout
			if (progress >= 100) {			
				clearInterval(intervalId);
				$("#loading").hide();
				$("#completed").show();
				
				// Redirect to completed page
				window.location="${baseUrl}/tabImport?action=completed";			
			}
		}
		
	}

	<#if submitted??>
	$(document).ready(function() {
		document.getElementById('buttons').style.display="none";
		document.getElementById('status').style.display='block';
		checkStatus();
	});
	</#if>

</script>

<div id="content_pane">
	<h1>TAB file import</h1>	
	
	<#if errorMessage?? && errorMessage != "">
	<h4 class="errorMsg">${errorMessage!""}</h4>
	</#if>
	
	<#-- add general error messages -->
	<#if generalErrorList?? && generalErrorList?has_content>
	<ol>
	    <#list generalErrorList as errorMsg>
	    <li class="errorMsg">${errorMsg}</li>
	    </#list>
	</ol>
	</#if>
	
	<#-- add object error messages -->
	<#if objectErrorList?? && objectErrorList?has_content>
	<br>
	<h4 class="errorMsg">Object Validation Errors - Must be corrected before the Project can be set to Completed</h4>
	<ol>
	    <#list objectErrorList as errorMsg>
	    <li class="errorMsg">${errorMsg}</li>
	    </#list>
	</ol>
	</#if>


	<h4 id="ajaxErrorMsg" class="errorMsg">${error!""}</h4>
		
	<form name="create_project" enctype="multipart/form-data" method="post" action="/podd/tabImport">
	
		<div style="margin: 20px;" >	
			<span style="margin: 4em; font-weight:bold;"><#if (uploadedTabFile)??>Uploaded </#if>TAB file:</span> 
			
			<#if (uploadedTabFile)??>
				${uploadedTabFile}  
				<span style="margin-left: 4em; color: green; font-weight:bold;">
					<div class="tabIcon">
						<img class="iconCheckmark" src="images/ui-icons_4eb305_256x240.png" />
					</div>
					Uploaded Successfully
				</span>
			</#if>
			<input type="file" name="tabFile" onchange="doSubmit('cacheTabFile');" <#if (uploadedTabFile)?? >disabled="true" style="display:none"</#if> />
		</div>
	
	
	    <#if  fileList?? && fileList?has_content>
	    <br>
	    <#include "attachedFilesDetails.html.ftl"/>
	    </#if>
	
	    <br>
	
		<#assign hideDescription=true >
		<#include "attachFile.html.ftl"/>

		<div id="buttons">
			<button type="reset" name="reset" value="reset" onclick="doSubmit('reset');">Reset</button>
			<button type="submit" value="submit" onclick="doSubmit('submit');" >Submit</button>
		</div>

		<div id="status" style="display:none">
			<div id="loading"><img src='${baseUrl}/images/indicator.gif' />&nbsp;<span id="loadingMsg">Please wait ...</span></div>
			<div id="completed" style="display:none;">Completed Successfully</div>
			<div>
				<div id="progressbar" />
			</div>
			<div id="objectCount" />
		</div>
	
	</form>
</div>