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

<#if errorMessage?? && errorMessage != "">
<p>
<h4 class="errorMsg">${errorMessage!""}</h4>
</p>
<#else>
<br />
</#if>

<#include "parent_details.html.ftl"/>

<h3 class="underlined_heading">${objectType!"Object"} Details
    <a href="javascript:animatedcollapse.toggle('details')" icon="toggle" title="View Details"></a>
</h3>
    <div id="details">  <!-- Collapsible div -->
        <!-- standard_attributes -->
        <#if forbidden?? && forbidden>
            <h4 class="errorMsg">Object: [${pid}] is deleted or inactive. Please contact repository administrator
            if you believe it has been deleted in error.</h4>
        <#elseif poddObject??>
            <div about="${baseUrl}/object/${poddObject.pid!"unknown-pid"}" id="${objectType!"object"}_details" class="fieldset">
                <ol>
                    <li><span class="bold">ID: </span><span property="podd:hasID" datatype="xsd:string">${poddObject.pid!""}</span></li>
                    <li><span class="bold">Title: </span><span property="dcterms:title" datatype="xsd:string">${poddObject.localName!""}</span></li>
                    <li><span class="bold">Description: </span><span property="dcterms:description" datatype="xsd:string">${poddObject.label!""}</span></li>
                    <!-- data, object and field set attributes -->
                    <#if elementList??>
                       <#list elementList  as field>
                        <@addField anField=field/>
                        </#list>
                    </#if>
                    <!-- creation infomation -->
                    <li><span class="bold">Creator: </span><span property="dcterms:creator" datatype="xsd:string">${poddObject.creator.firstName!""} ${poddObject.creator.lastName!""}</span></li>
                    <!-- TODO: change to xsd:dateTime when the dates can be generated or converted to ISO8601 compliant dates -->
                    <li><span class="bold">Creation Date: </span><span property="dcterms:date" datatype="xsd:string">${creationDate!""}</span></li>
                    <li><span class="bold">Last modified by: </span><span property="dcterms:creator" datatype="xsd:string">${poddObject.lastModifier.firstName!""} ${poddObject.lastModifier.lastName!""}</span></li>
                    <!-- TODO: change to xsd:dateTime when the dates can be generated or converted to ISO8601 compliant dates -->
                    <li><span class="bold">Last modification date: </span><span property="dcterms:modified" datatype="xsd:string">${modifiedDate!""}</span></li>
                </ol>
            </div>
        <#else>
            <h4 class="errorMsg">Object: ${pid} could not be found</h4>
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
<#--include "hierarchy.html.ftl"/-->

<br />    
<div id="buttonwrapper">
    <#if poddObject??>
	    <#if  canEditObject?? && canEditObject>
        <a href="${baseUrl}/object/${poddObject.pid!"unknown-pid"}/edit">Edit Object</a>
        </#if>
        <#if  canEditObject?? && canEditObject>
        <a href="${baseUrl}/clipboard?type=object&mp;target=${poddObject.pid}">Paste</a>
        </#if>
        <#if  canAddChildren?? && canAddChildren>
        <a href="${baseUrl}/object/${poddObject.pid!"unknown-pid"}/add">Add Child Object</a>
        </#if>
        <#if  canPublish?? && canPublish>
        <a href="${baseUrl}/object/${poddObject.pid!"unknown-pid"}/publish?publish=true">Publish Project</a>
        </#if>
        <#if  canUnpublish?? && canUnpublish>
        <a href="${baseUrl}/object/${poddObject.pid!"unknown-pid"}/generatePURLs">Update PURLs</a>
        <a href="${baseUrl}/object/${poddObject.pid!"unknown-pid"}/publish?publish=false">Unpublish Project</a>
        </#if>
        <#if objectType?? && objectType == 'Investigation'>
        	<a href="${baseUrl}/services/getHierarchy?option=file&URI=http://www.podd.org/object%23${poddObject.pid!"unknown-pid"}">Download hierarchy attachments</a>
        </#if>        
        <#if canDelete?? && canDelete>
        <a href="${baseUrl}/object/${poddObject.pid!"unknown-pid"}/delete">Delete</a>
        </#if>
        <#if canUndelete?? && canUndelete>
        <a href="${baseUrl}/object/${poddObject.pid!"unknown-pid"}/delete">Undelete</a>
        </#if>
    <#else>
    <a href="${baseUrl}/project">Cancel</a>    
    </#if>
</div>
</div>  <!-- content pane -->

<#macro addField anField>
    <#if anField.getType() == enums["podd.template.content.HTMLElementType"].FIELD_SET>
        <@addFieldset element=anField/>
    <#else>
    <li><span class="bold">${anField.label}: </span>
    <#if anField.enteredValues?has_content>
        <#if anField.enteredValues[1]??>
        <ol>
        <#list anField.enteredValues as value>
            <li>${value}</li>
        </#list>
        </ol>
        <#else>
        ${anField.enteredValues[0]!""}
        </#if>
    </#if>
    </li>
    </#if>
</#macro>

<#macro addFieldset element>
    <div id="${element.getPropertyURI()}_fieldset" class="fieldset inner">
        <div class="legend">${element.getLabel()}</div>
         <#if element.getFieldsetElementList()?? && element.getFieldsetElementList()?has_content>
             <!-- add the items fron each of the lists (data, object, fieldset and refersTo properties) to the field set -->
             <#list element.getFieldsetElementList() as elementList>
                <#if elementList?? && elementList?has_content>
                <ol>
                <#list elementList as element>
                    <@addField anField=element/>
                </#list>
                </ol>
                </#if>
            </#list>
         </#if>
    </div>
</#macro>

<#macro refersToTable element>
    <h3 class="underlined_heading">${element.label!""}
        <a href="javascript:animatedcollapse.toggle('${element.propertyUriWithoutNamespace}_details')" icon="toggle" title="View Details"></a>
    </h3>
	<div id="${element.propertyUriWithoutNamespace}_details">

    <#assign selectedObjectCount = 0>
    <#if isAdmin>
        <#assign selectedObjectCount = element.availableObjects?size>
    <#else>
        <#assign selectedObjectCount = 0>
        <#list element.availableObjects as object>
            <#if object.isSelected && object.state == "A">
            <#assign selectedObjectCount = selectedObjectCount + 1>
            </#if>
        </#list>
    </#if>
    
    <#if element.areSelectedObjects && selectedObjectCount != 0>
    <table id='${element.propertyUriWithoutNamespace}' class="tablesorter {sortlist: [[0,0]]}" cellspacing="0">
		<thead>
			<tr>
			    <th>ID</th>
			    <th>Title</th>
			    <th>Description</th>
			</tr>
		</thead>
        <tfoot>
        <!-- empty table row, so that the footer appears and table looks complete -->
            <tr>
                <td></td>
                <td></td>
                <td></td>
            </tr>
        </tfoot>
        <tbody>
			<#list element.availableObjects as object>
            <#if object.isSelected && (object.state == "A" || isAdmin)>
            <tr>
                <#if object.type == "error">
                    <td class="errorMsg"><a href="${baseUrl}/object/${object.pid}">${object.pid!""}</a></td>
                    <td class="errorMsg">${object.title!""}</td>
                    <td class="errorMsg">${object.description!" - "}</td>
                <#else>
                    <td><a href="${baseUrl}/object/${object.pid}">${object.pid!""}</a>
                        <#if object.state != "A">
                         (<span class="descriptive">Deleted</span>)
                        </#if>
                    </td>
                    <td>${object.title!""}</td>
                    <td>
                        <script type="text/javascript">writeAbstractWholeWords("${object.description!" - "}", 200);</script>
                    </td>
                </#if>
			</tr>
            </#if>
			</#list>
		</tbody>
    </table>
    <#else>
    <p>No objects selected.</p>
    </#if>
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

<#macro referencedByTable>
    <h3 class="underlined_heading">Referenced By
        <a href="javascript:animatedcollapse.toggle('referencedBy_details')" icon="toggle" title="View Details"></a>
    </h3>
	<div id="referencedBy_details">

    <table id='referencedBy_table' class="tablesorter {sortlist: [[0,0],[2,0]]}" cellspacing="0">
		<thead>
			<tr>
			    <th>Type</th>
                <th>ID</th>
			    <th>Title</th>
			    <th>Description</th>
			</tr>
		</thead>
        <tfoot>
        <!-- empty table row, so that the footer appears and table looks complete -->
            <tr>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
            </tr>
        </tfoot>
        <tbody>
            <#list referencedByList as object>
            <tr>
				<td>${object.getConcept().getConceptName()!"-"}</td>
                <td><a href="${baseUrl}/object/${object.pid}">${object.pid!""}</a>
                    <#if object.getState() != "A">
                     (<span class="descriptive">Deleted</span>)
                    </#if>
                </td>
				<td>${object.getLocalName()!""}</td>
				<td>
                    <script type="text/javascript">writeAbstractWholeWords("${object.getLabel()!" - "}", 200);</script>
                </td>
			</tr>
            </#list>
		</tbody>
    </table>
    <#if referencedByList?size < referencedByCount>
        ${referencedByList?size} of ${referencedByCount} referencing objects displayed. &nbsp;&nbsp;
        <a href="${baseUrl}/allReferers/${poddObject.pid}" id="referee_btn" class="button">View All</a>
        <br>
    </#if>
    </div>

    <script type="text/javascript">
	    animatedcollapse.addDiv('referencedBy_details', 'fade=1,hide=0');
    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#referencedBy_table")
            .tablesorter({
                headers: {
                    0: { sorter: 'checkbox'}
                },
                sortMultiSortKey: 'ctrlKey'
            });
        });
    </script>
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

<script type="text/javascript">
    $(document).ready(function() {
        $("#copy_btn").click(function() {
            $.ajax({
                type: "POST",
                url: "${baseUrl}/services/manageClipboard?type=object&amp;pid=${pid}",
                success: function(data) {
                    var responseObject = eval('(' + data + ')');
                    alert(responseObject.message);
                },
                error: function(xhr, status, error) {
                    alert(error.message);
                }
            });
            return false;
        });
    });
</script>
