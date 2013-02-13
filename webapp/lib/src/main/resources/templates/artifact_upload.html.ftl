<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="user" type="podd.model.user.User" -->

<div id="title_pane">
    <p></p>
</div>

<div id="content_pane">
<#if user??>
    <#if artifact??>
    	<h4>Artifact successfully uploaded.</h4>
	    <p><a href="${baseUrl}/artifact/base?artifacturi=${artifact.iri}" class="padded">View project.</a></p>
	    <div class="fieldset" id="upload">
			<div class="legend">Artifact Details</div>
			<ol> 
				<li><span class="bold">Ontology IRI: </span>${artifact.iri}</li>
				<li><span class="bold">Version IRI: </span>${artifact.versionIri}</li>
				<li><span class="bold">Inferred IRI: </span>${artifact.inferredIri}</li>
			</div>
		</div>
	<#else>    
    	<p>Upload a new Artifact.</p>
	    <form name="f" enctype="multipart/form-data" action="${baseUrl}/artifact/new" method="POST">
	    <div class="fieldset" id="upload">
			<div class="legend">Upload new artifact</div>
			<ol> 
				<li> 
					<label for="user">Select file: </label> 
					<input id="user" class="medium" name="artifact_file" type="file" value=""> 
				</li> 
				<li> 
					<label for="user">Artifact details: </label> 
					<input id="user" class="medium" name="artifact_details" type="text" value=""> 
				</li> 
	            <#if errorMessage?? && errorMessage?has_content>
	                <li><h4 class="errorMsg">${errorMessage}</h4></li>
	            </#if>
	            <#if message?? && message?has_content>
	                <li><h4>${message!""}</h4></li>
	            </#if>
	            <#if detailedMessage?? && detailedMessage?has_content>
	                <li><p>${detailedMessage}</p></li>
	            </#if>
	        </ol>
	    </div>
		
		<div id="buttonwrapper">
			<button type="submit">upload</button>
		</div>
	    </form>	
   	</#if>
    
 
    
<#else>
    <p>Welcome to PODD, please <a href="${baseUrl}/loginpage">login</a>.</p>
</#if>
</div>  <!-- content pane -->