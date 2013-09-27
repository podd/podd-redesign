<#-- @ftlvariable name="isAdmin" type="boolean" -->
<#-- @ftlvariable name="forbidden" type="boolean" -->
<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="pid" type="java.lang.String" -->
<#-- @ftlvariable name="state" type="java.lang.String" -->
<#-- @ftlvariable name="objectType" type="java.lang.String" -->
<#-- @ftlvariable name="poddObject" type="podd.model.entity.PoddObject" -->
<#-- @ftlvariable name="isProject" type="boolean" -->
<#-- @ftlvariable name="creationDate" type="java.lang.String" -->
<#-- @ftlvariable name="modifiedDate" type="java.lang.String" -->
<#-- @ftlvariable name="elementList" type="java.util.ArrayList<podd.template.content.HTMLElementTemplate>" -->
<#-- @ftlvariable name="refersToList" type="java.util.ArrayList<podd.template.content.HTMLElementTemplate>" -->
<#-- @ftlvariable name="referencedByCount" type="java.lang.Integer" -->
<#-- @ftlvariable name="referencedByList" type="java.util.ArrayList<podd.model.entity.PoddObject>" -->
<#-- @ftlvariable name="canEditObject" type="boolean" -->
<#-- @ftlvariable name="canCopyObject" type="boolean" -->
<#-- @ftlvariable name="canAddChildren" type="boolean" -->
<#-- @ftlvariable name="canPublish" type="boolean" -->
<#-- @ftlvariable name="canUnpublish" type="boolean" -->
<#-- @ftlvariable name="canDelete" type="boolean" -->
<#-- @ftlvariable name="canUndelete" type="boolean" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->

<script type="text/javascript" src="${baseUrl}/resources/scripts/animatedcollapse.js">
    /* this needs to be placed at the top of the file so that we can add divs as they are created !!!! */
    /***********************************************
    * Animated Collapsible DIV v2.4- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
    * This notice MUST stay intact for legal use
    * Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
    ***********************************************/
</script>

<script type="text/javascript">
	$(document).ready(function() {
        podd.debug('-------------------');
        podd.debug('initializing...');
        podd.debug('-------------------');

		podd.objectUri = '${poddObject.objectURI!"Not Found"}';
		podd.objectTypeUri = '${objectType.objectURI!"undefined"}';
		podd.artifactIri = '${artifactUri!"undefined"}';
		podd.versionIri = '${versionIri!"undefined"}';
		
		// Add child object clicked
		$("#createChildObject").click(function(event) {
			event.preventDefault();
			podd.debug("Clicked add child object");

			podd.getCreateChildMetadata(podd.artifactIri, podd.objectTypeUri, podd.showAddChildDialog);
		});
	
		// Delete object clicked
		$("#deleteObject").click(function(event) {
			event.preventDefault();
			var objectName = '${poddObject.label!artifactUri}';
			var childCount = ${childCount};
			
			
			var redirectUrl = '${baseUrl}/artifact/base?artifacturi=${artifactUri?url}';
			
			<#if parentObject?? && parentObject.uri??>
				var parentUri = '${parentObject.uri?url}';
				redirectUrl += '&objecturi=' + parentUri;
			</#if>
			
			podd.showDeleteObjectConfirmDialog(podd.artifactIri, podd.versionIri, podd.objectUri, objectName, childCount, redirectUrl);
		});
	
		// Delete project clicked
		$("#deleteProject").click(function(event) {
			event.preventDefault();
			podd.debug("Clicked Delete Project");

			var objectName = '${poddObject.label!artifactUri}';
			var childCount = ${childCount};
			var redirectUrl = '${baseUrl}/artifacts';
			
			podd.showDeleteObjectConfirmDialog(podd.artifactIri, podd.versionIri, 'undefined', objectName, childCount, redirectUrl);
		});

	
        podd.debug('### initialization complete ###');
	});
</script>

<div id="dialog" title="Add Child"></div>
<div id="delete_object_dialog" title="Delete Object"></div>

<div id="title_pane">
    <#if state??>
        <h3>View: ${objectType.label!"Object"} (<span class="descriptive">${state}</span>)</h3>
    <#else>
        <h3>View: ${objectType.label!"Object"}</h3>
    </#if>
</div>

<div id="content_pane">
<h4 id="errorMsgHeader" class="errorMsg">${errorMessage!""}</h4>

<#-- add general error messages -->
<ol id="errorMsgList">
</ol>

<#include "parent_details.html.ftl"/>

<h3 class="underlined_heading">${objectType.label!"Object"} Details
    <a href="javascript:animatedcollapse.toggle('details')" icon="toggle" title="View Details"></a>
</h3>
    <div id="details">  <!-- Collapsible div -->
        <#if poddObject?? && poddObject.objectURI??>
            <div about="${poddObject.objectURI!"unknown-uri"}" id="${objectType.label!"object"}_details" class="fieldset">
                <ol>
                	<#-- object URI, Title and description -->
                    <li><span class="bold">URI: </span> <a href="${baseUrl}/artifact/base?artifacturi=${artifactUri?url}&amp;objecturi=${poddObject.objectURI?url}">${util.clipProtocol(poddObject.objectURI!"Unknown URI")}</a></li>
                    <li><span class="bold">Title: </span><span property="dcterms:title" datatype="xsd:string"><@formatText textValue=poddObject.label!""/></span></li>
                    <li><span class="bold">Description: </span><span property="dcterms:description" datatype="xsd:string"><@formatText textValue=poddObject.description!""/></span></li>
                    
                    <#-- all other attributes (data/object properties) -->
                    <#if propertyList??>
                       <#list propertyList  as propertyUri>
                       	<@displayField propertyUri=propertyUri/>
                       </#list>
                    </#if>
                    
                </ol>
            </div>
        </#if>
    </div>  <!-- details - Collapsable div -->

<#if refersToList?? && refersToList?has_content>
<#list refersToList as element>
    <br />
    <@refersToTable element=element/>
</#list>
</#if>

<#if referencedByList?? && referencedByList?has_content>
    <@referencedByTable/>
</#if>

<#if isProject?? && isProject>
<br />
<!--#include "projectParticipantDetails.html.ftl"/-->
</#if>

<br />
<#include "attachedFilesDetails.html.ftl"/>

<br />
<!--#include "hierarchy.html.ftl"/-->

<br />    
<div id="buttonwrapper">
    <#if poddObject??>
	    <#if  canEditObject?? && canEditObject>
        <a href="${baseUrl}/artifact/edit?artifacturi=${artifactUri?url!"unknown-artifacturi"}&amp;objecturi=${poddObject.objectURI?url!"unknown-objecturi"}">Edit Object</a>
        </#if>
		<#if isProject?? && isProject && canEditRoles?? && canEditRoles>
	        <a href="${baseUrl}/artifact/roles?artifacturi=${artifactUri?url!"unknown-artifacturi"}">Edit Participants</a>
		</#if>
        <#if  canAddChildren?? && canAddChildren>
	    	<a id="createChildObject" value="createChildObject">Add Child Object</a>
            <a href="${baseUrl}/artifact/attachdataref?artifacturi=${artifactUri?url!"unknown-artifacturi"}&amp;objecturi=${poddObject.objectURI?url!"unknown-objecturi"}">Attach data reference</a>
        </#if>
        <#if  canPublish?? && canPublish>
        <a href="${baseUrl}/artifact/publish?artifacturi=${poddObject.objectURI!"unknown-pid"}/publish?publish=true">Publish Project</a>
        </#if>
        <#if  canUnpublish?? && canUnpublish>
        <a href="${baseUrl}/artifact/unpublish?artifacturi=${poddObject.objectURI!"unknown-pid"}">Unpublish Project</a>
        </#if>
        <#if objectType?? && objectType.label == 'Investigation'>
        	<a href="${baseUrl}/services/getHierarchy?option=file&URI=http://www.podd.org/object%23${poddObject.objectURI!"unknown-pid"}">Download hierarchy attachments</a>
        </#if>        
        <#if canDelete?? && canDelete>
        	<#if isProject?? && isProject>
        		<#if canDeleteProject?? && canDeleteProject>
        			<a id="deleteProject" >Delete Project</a>
        		</#if>
        	<#else>
        		<a id="deleteObject" >Delete</a>
        	</#if>
        </#if>
    <#else>
    <!-- TODO: Remove me. -->
    <a href="${baseUrl}/project">Cancel</a>    
    </#if>
</div>
</div>  <!-- content pane -->

<#-- 
Macro to display information about the PODD object being viewed
  -->
<#macro displayField propertyUri>
    <li>
		<#local label = completeModel.filter(propertyUri, RDFS_LABEL, null).objectString()!"Missing Label">
    	<span class="bold">${label}:</span>

		<#local objectList = completeModel.filter(poddObject.objectURI, propertyUri, null).objects()>
		<#if (objectList.size() > 1)>
			<#-- multiple values. create another HTML list -->
			<ol>
			<#list objectList as thisObject>
				<li>
					<#if util.isUri(thisObject)>
						<#local tempUri = util.getUri(thisObject)>
						<#if tempUri??>
							<#local valueLabel = completeModel.filter(thisObject, RDFS_LABEL, null).objectString()!thisObject.stringValue()>
							<span><a property="${propertyUri}" href="${baseUrl}/artifact/base?artifacturi=${artifactUri?url}&amp;objecturi=${thisObject?url}">${util.clipProtocol(valueLabel)}</a></span>
						<#else>
							<span><a property="${propertyUri}" href="${baseUrl}/artifact/base?artifacturi=${artifactUri?url}&amp;objecturi=${thisObject?url}">${util.clipProtocol(thisObject.stringValue())}</a></span>	
						</#if>
					<#else>
						<span property="${propertyUri}" datatype="${util.getDatatype(thisObject)}"><@formatText textValue=thisObject.stringValue()!""/></span>
					</#if>
				</li>
			</#list>
			</ol>
		<#else>
			<#-- single value. no need to create another HTML list -->
			<#list objectList as thisObject>
				<#if util.isUri(thisObject)>
					<#local tempUri = util.getUri(thisObject)>
					<#if tempUri??>
						<#local valueLabel = completeModel.filter(thisObject, RDFS_LABEL, null).objectString()!thisObject.stringValue()>
						<span><a property="${propertyUri}" href="${baseUrl}/artifact/base?artifacturi=${artifactUri?url}&amp;objecturi=${thisObject?url}">${util.clipProtocol(valueLabel)}</a></span>
					<#else>
						<span><a property="${propertyUri}" href="${baseUrl}/artifact/base?artifacturi=${artifactUri?url}&amp;objecturi=${thisObject?url}">${util.clipProtocol(thisObject.stringValue())}</a></span>	
					</#if>
				<#else>
					<span property="${propertyUri}" datatype="${util.getDatatype(thisObject)}"><@formatText textValue=thisObject.stringValue()!""/></span>
				</#if>
			</#list>
		</#if>
    </li>
</#macro>

<#-- 
Macro to format a given text field for display by HTML encoding it and replacing newline characters with HTML <br> elements.
-->
<#macro formatText textValue>
	${textValue?html?replace("\n", "<br>")}
</#macro>


<#macro refersToTable element>
    <h3 class="underlined_heading">${element.label!""}
        <a href="javascript:animatedcollapse.toggle('${element.propertyUriWithoutNamespace}_details')" icon="toggle" title="View Details"></a>
    </h3>
    
	<div id="${element.propertyUriWithoutNamespace?html}_details">
    <table id='${element.propertyUriWithoutNamespace?html}' class="tablesorter" cellspacing="0">
		<thead>
			<tr>
			    <th>ID</th>
			    <th>Title</th>
			    <th>Description</th>
			</tr>
		</thead>
        <tbody>
			<#list element.availableObjects as object>
			<tr>
				<!-- TODO: Fix url and property -->
                <td><a href="${baseUrl}/object/${object.uri}">${object.uri!""}</a>
                </td>
                <td>${object.title!""}</td>
                <td>
                    <script type="text/javascript">writeAbstractWholeWords("${object.description!" - "}", 200);</script>
                </td>
			</tr>
			</#list>
		</tbody>
        <tfoot>
        <!-- empty table row, so that the footer appears and table looks complete -->
            <tr>
                <td></td>
                <td></td>
                <td></td>
            </tr>
        </tfoot>
    </table>
    </div>

    <script type="text/javascript">
	    animatedcollapse.addDiv('${element.propertyUriWithoutNamespace}_details', 'fade=1,hide=0');
    </script>
    <#if element.areSelectedObjects && selectedObjectCount != 0>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#${element.propertyUriWithoutNamespace}")
            .tablesorter({
                headers: {
                    0: { sorter: 'checkbox'}
                },
                sortMultiSortKey: 'ctrlKey'
            });
        });
    </script>
    </#if>
</#macro>

<!-- Use refersTo macro instead of referencedByTable for maintainability -->
<#macro referencedByTable>
</#macro>

<script type="text/javascript" src="${baseUrl}/resources/scripts/jquery.tablesorter.js"></script>

<script type="text/javascript">
    animatedcollapse.addDiv('parent_details', 'fade=1,hide=0');
    animatedcollapse.addDiv('details', 'fade=1,hide=0');
	animatedcollapse.addDiv('participants', 'fade=1,hide=0');
	animatedcollapse.addDiv('files', 'fade=1,hide=0');
	animatedcollapse.addDiv('hierarchy', 'fade=1,hide=0');

	animatedcollapse.ontoggle=function($, divobj, state){ 
		//fires each time a DIV is expanded/contracted
		//$: Access to jQuery
		//divobj: DOM reference to DIV being expanded/ collapsed. Use "divobj.id" to get its ID
		//state: "block" or "none", depending on state
	};
	animatedcollapse.init();
</script>

