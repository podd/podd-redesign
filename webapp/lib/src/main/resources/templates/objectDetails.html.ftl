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
                    <li><span class="bold">ID (URI): </span>${poddObject.uri!""}</li>
                    <li><span class="bold">Title: </span><span property="dcterms:title" datatype="xsd:string">${poddObject.title!""}</span></li>
                    <li><span class="bold">Description: </span><span property="dcterms:description" datatype="xsd:string">${poddObject.description!""}</span></li>
                    
                    <!-- data, object attributes -->
                    <#if elementList??>
                       <#list elementList  as field>
                        <@addField anField=field/>
                        </#list>
                    </#if>
                    
                    <#if objectType?? && objectType.contains("TopObject")>
	                    <!-- creation infomation -->
	                    <li><span class="bold">Creator: </span><span property="dcterms:creator" resource="${poddTopObjectCreator.uri}">${poddTopObjectCreator.firstName!""} ${poddTopObjectCreator.lastName!""} (${poddTopObjectCreator.email!""})</span></li>
	                    <!-- TODO: change to xsd:dateTime when the dates can be generated or converted to ISO8601 compliant dates -->
	                    <li><span class="bold">Creation Date: </span><span property="dcterms:date" datatype="xsd:string">${topObject["http://purl.org/podd/ns/poddBase#createdAt"]!""}</span></li>
                    </#if>

					                    
                    <#if objectModel??>
                       <#list objectModel  as statement>
                       	<@displayField statement=statement/>
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

<#macro addField anField>
    <li><span class="bold">${anField.label}: </span>
    <#if anField.enteredValues?has_content>
        <#if anField.enteredValues[1]??>
	        <ol>
	        <#list anField.enteredValues as value>
	        	<#if value.isUri>
		        	<!-- TODO: Determine if this is a URI or a literal and render it as a link here if necessary -->
		            <li property="${value.propertyUri}" resource="${value.datatype}"><a href="${value.uri}>${value.label}</a></li>
	        	<#else>
		            <li property="${value.propertyUri}" datatype="${value.datatype}">${value.label}</li>
	            </#if>
	        </#list>
	        </ol>
        <#else>
	    	<#if anField.enteredValues[0].isUri>
		    	<!-- TODO: Determine if this is a URI or a literal and render it as a link here if necessary -->
		        <li property="${anField.enteredValues[0].propertyUri}" resource="${anField.enteredValues[0].datatype}"><a href="${anField.enteredValues[0].uri}>${anField.enteredValues[0].label}</a></li>
	    	<#else>
		        <!-- FIXME: Move these renderings into a macro -->
		        <li property="${anField.enteredValues[0].propertyUri}" datatype="${anField.enteredValues[0].datatype}">${anField.enteredValues[0].label}</li>
	        </#if>
	    </#if>
    </#if>
    </li>
</#macro>

<#-- 
Macro to display information contained in a statement
Should eventually replace macro addField
added [2013-02-18] 
  -->
<#macro displayField statement>
    <li>
    	<span class="bold">
    		${completeModel.filter(statement.getPredicate(), rdfsLabelUri, null).objectString()}: </span>
		<span property="${statement.getPredicate()}" datatype="xsd:string"> 
			<span>${statement.getObject().stringValue()}</span>
		</span>
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

