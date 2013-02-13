<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="objectType" type="java.lang.String" -->
<#-- @ftlvariable name="poddObject" type="podd.model.entity.PoddObject" -->
<#-- @ftlvariable name="file" type="podd.resources.util.view.FileDisplayHelper.FileElement" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->

<div id="title_pane">
<h3>View: File Details</h3>
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

<h3 class="underlined_heading">File Details
    <a href="javascript:animatedcollapse.toggle('file_details')" icon="toggle" title="View Details"></a>
</h3>

<div id="file_details">  <!-- Collapsible div -->
<div id="details" class="fieldset">
    <ol>
        <#if file??>
        <li><span class="bold">Title: </span>${file.getFileName()!""}</li>
        <li><span class="bold">Description: </span>${file.getDescription()!""}</li>
        <li><span class="bold">Size (kb): </span>${file.getSize()!""}</li>
        <li><span class="bold">Type: </span>${file.getMimeType()!""}</li>
        <li><span class="bold">Format: </span>${file.getFormat()!""}</li>
        <#if file.getCreator()??>
        <li><span class="bold">Creator: </span>${file.getCreator().getFirstName()!""} ${file.getCreator().getLastName()!""}</li>
        </#if>
        <li><span class="bold">Upload Date: </span>${file.getUploadDate()!""}</li>
        </#if>
    </ol>
</div>
</div>  <!-- file_details - Collapsable div -->

<br>
<div id="buttonwrapper">
    <#if file??>
    <a href="${baseUrl}/${file.getUrl()!"unknown-url"}/download">Download</a>
    <a href="${baseUrl}/${file.getUrl()!"unknown-url"}/edit">Edit</a>
    <a href="${baseUrl}/${file.getUrl()!"unknown-url"}/delete">Delete</a>
    </#if>
    <#if poddObject??>
    <a href="${baseUrl}/object/${poddObject.getPid()!"unknown-url"}">Back</a>
    <#else>
    <a href="${baseUrl}/project">Back</a>
    </#if>
</div>
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
    animatedcollapse.addDiv('file_details', 'fade=1,hide=0');

	animatedcollapse.ontoggle=function($, divobj, state){
		//fires each time a DIV is expanded/contracted
		//$: Access to jQuery
		//divobj: DOM reference to DIV being expanded/ collapsed. Use "divobj.id" to get its ID
		//state: "block" or "none", depending on state
	};
	animatedcollapse.init();
</script>
