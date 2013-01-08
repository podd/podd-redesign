<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="postUrl" type="java.lang.String" -->
<#-- @ftlvariable name="objectType" type="java.lang.String" -->
<#-- @ftlvariable name="poddObject" type="podd.model.entity.PoddObject" -->
<#-- @ftlvariable name="file" type="podd.resources.util.view.FileDisplayHelper.FileElement" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->

<div id="title_pane">
    <h3>Delete: File</h3>
</div>

<div id="content_pane">

<#if errorMessage?? && errorMessage != "">
<p>
<h4 class="errorMsg">${errorMessage!""}</h4>
</#if>
<br>

<h3 class="underlined_heading">${objectType!"Object"} Details
    <a href="javascript:animatedcollapse.toggle('object_details')" icon="toggle" title="View ${objectType!"Object"} Details"></a>
</h3>

<div id="object_details">  <!-- Collapsible div -->
    <div id="parentInfo"class="fieldset">
        <div class="legend">${objectType!"Object"} Summary Information</div>
        <ol>
        <#if poddObject??>
            <li><span class="bold">ID: </span><a href="${baseUrl}/object/${poddObject.getPid()!""}">${poddObject.getPid()!""}</a></li>
            <li><span class="bold">Title: </span>${poddObject.getLocalName()!""}</li>
            <li><span class="bold">Type: </span>${objectType!"unknown"}</li>
        </#if>
        </ol>
    </div>
</div>  <!-- object_details - Collapsable div -->
<br>
    
<h3 class="underlined_heading">Are you sure you want to delete the file?</h3>    
<form name="delete_file" action="${postUrl}" method="POST">
	<div class="fieldset">
		<ol>
            <#if  file??>
			<li><span class="bold">Title: </span>${file.getFileName()!""}</li>
            <li><span class="bold">Description: </span>${file.getDescription()!""}</li>
            <li><span class="bold">Size: </span>${file.getSize()!""}</li>
            </#if>
		</ol>
	</div>

	<div id="buttonwrapper">
        <#if poddObject?? && file??>
            <button type="submit">Delete File</button>
            <a href="${baseUrl}/${file.getUrl()!"unknown-url"}">Cancel</a>
        <#else>
            <#if poddObject??>
            <a href="${baseUrl}/object/${poddObject.getPid()!"unknown-url"}">Cancel</a>
            <#else>
            <a href="${baseUrl}/project">Cancel</a>
            </#if>
        </#if>
	</div>
</form>
</div>  <!-- content pane -->

<script type="text/javascript" src="${baseUrl}/scripts/animatedcollapse.js">
/***********************************************
* Animated Collapsible DIV v2.4- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
***********************************************/
</script>

<script type="text/javascript">
    animatedcollapse.addDiv('object_details', 'fade=1,hide=0');

	animatedcollapse.ontoggle=function($, divobj, state){
		//fires each time a DIV is expanded/contracted
		//$: Access to jQuery
		//divobj: DOM reference to DIV being expanded/ collapsed. Use "divobj.id" to get its ID
		//state: "block" or "none", depending on state
	};
	animatedcollapse.init();
</script>