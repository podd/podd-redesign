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

<div id="title_pane">
    <#if state??>
        <h3>View: ${objectType!"Object"} (<span class="descriptive">${state}</span>)</h3>
    <#else>
        <h3>View: ${objectType!"Object"}</h3>
    </#if>
</div>

<div id="content_pane">

<#include "parent_details.html.ftl"/>

<h3 class="underlined_heading">${objectType!"Object"} Details
    <a href="javascript:animatedcollapse.toggle('details')" icon="toggle" title="View Details"></a>
</h3>
    <div id="details">  <!-- Collapsible div -->
        <#if poddObject?? && poddObject.uri??>
            <div about="${poddObject.uri!"unknown-uri"}" id="${objectType!"object"}_details" class="fieldset">
                <ol>
                	<#-- object URI, Title and description -->
                    <li><span class="bold">URI: </span> <a href="${poddObject.uri}">${util.clipProtocol(poddObject.uri!"Unknown URI")}</a></li>
                    <li><span class="bold">Title: </span><span property="dcterms:title" datatype="xsd:string">${poddObject.title!""}</span></li>
                    <li><span class="bold">Description: </span><span property="dcterms:description" datatype="xsd:string">${poddObject.description!""}</span></li>
                    
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
    <br>
    <@refersToTable element=element/>
</#list>
</#if>

<#if referencedByList?? && referencedByList?has_content>
    <@referencedByTable/>
</#if>

<#if isProject?? && isProject>
<br />
<#include "projectParticipantDetails.html.ftl"/>
</#if>

<br />
<#include "attachedFilesDetails.html.ftl"/>

<br />
<!--#include "hierarchy.html.ftl"/-->

<br />    
<div id="buttonwrapper">
    <#if poddObject??>
	    <#if  canEditObject?? && canEditObject>
        <a href="${baseUrl}/artifact/edit/merge?artifacturi=${poddObject.uri!"unknown-pid"}/edit">Edit Object</a>
        </#if>
        <#if  canAddChildren?? && canAddChildren>
        <a href="${baseUrl}/object/${poddObject.uri!"unknown-pid"}/add">Add Child Object</a>
        </#if>
        <#if  canPublish?? && canPublish>
        <a href="${baseUrl}/artifact/publish?artifacturi=${poddObject.uri!"unknown-pid"}/publish?publish=true">Publish Project</a>
        </#if>
        <#if  canUnpublish?? && canUnpublish>
        <a href="${baseUrl}/artifact/updatepurls?artifacturi=${poddObject.uri!"unknown-pid"}">Update PURLs</a>
        <a href="${baseUrl}/artifact/unpublish?artifacturi=${poddObject.uri!"unknown-pid"}">Unpublish Project</a>
        </#if>
        <#if objectType?? && objectType == 'Investigation'>
        	<a href="${baseUrl}/services/getHierarchy?option=file&URI=http://www.podd.org/object%23${poddObject.uri!"unknown-pid"}">Download hierarchy attachments</a>
        </#if>        
        <#if canDelete?? && canDelete>
        <a href="${baseUrl}/artifact/delete?artifacturi=${poddObject.uri!"unknown-pid"}">Delete</a>
        </#if>
        <#if canUndelete?? && canUndelete>
        <a href="${baseUrl}/artifact/undelete?artifacturi=${poddObject.uri!"unknown-pid"}">Undelete</a>
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
		<#local label = completeModel.filter(propertyUri, rdfsLabelUri, null).objectString()!"Missing Label">
    	<span class="bold">${label}:</span>

		<#local objectList = completeModel.filter(poddObject.uri, propertyUri, null).objects()>
		<#if (objectList.size() > 1)>
			<#-- multiple values. create another HTML list -->
			<ol>
			<#list objectList as thisObject>
				<li>
					<#if util.isUri(thisObject)>
						<#local tempUri = util.getUri(thisObject)>
						<#if tempUri??>
							<#local valueLabel = completeModel.filter(thisObject, rdfsLabelUri, null).objectString()!thisObject.stringValue()>
							<span><a href="${thisObject}">${util.clipProtocol(valueLabel)}</a></span>
						<#else>
							<span><a href="${thisObject}">${util.clipProtocol(thisObject.stringValue())}</a></span>	
						</#if>
					<#else>
						<span property="${propertyUri}" datatype="${util.getDatatype(thisObject)}">${thisObject.stringValue()}</span>
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
						<#local valueLabel = completeModel.filter(thisObject, rdfsLabelUri, null).objectString()!thisObject.stringValue()>
						<span><a href="${thisObject}">${util.clipProtocol(valueLabel)}</a></span>
					<#else>
						<span><a href="${thisObject}">${util.clipProtocol(thisObject.stringValue())}</a></span>	
					</#if>
				<#else>
					<span property="${propertyUri}" datatype="${util.getDatatype(thisObject)}">${thisObject.stringValue()}</span>
				</#if>
			</#list>
		</#if>
    </li>
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

<script type="text/javascript" src="${baseUrl}/scripts/jquery.tablesorter.js"></script>

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

