<#-- @ftlvariable name="canViewProjectParticipants" type="boolean" -->
<#-- @ftlvariable name="initialized" type="boolean" -->
<#-- @ftlvariable name="isAdmin" type="boolean" -->
<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="postUrl" type="java.lang.String" -->
<#-- @ftlvariable name="title" type="java.lang.String" -->
<#-- @ftlvariable name="objectPID" type="java.lang.String" -->
<#-- @ftlvariable name="typeName" type="java.lang.String" -->
<#-- @ftlvariable name="isProject" type="boolean" -->
<#-- @ftlvariable name="objectName" type="java.lang.String" -->
<#-- @ftlvariable name="objectDescription" type="java.lang.String" -->
<#-- @ftlvariable name="elementList" type="java.util.ArrayList<podd.template.content.HTMLElementTemplate>" -->
<#-- @ftlvariable name="refersList" type="java.util.ArrayList<podd.template.content.HTMLElementTemplate>" -->
<#-- @ftlvariable name="fileList" type="java.util.ArrayList<podd.resources.util.view.FileElement>" -->
<#-- @ftlvariable name="createdDate" type="java.lang.String" -->
<#-- @ftlvariable name="createdBy" type="java.lang.String" -->
<#-- @ftlvariable name="createdDate" type="java.lang.String" -->
<#-- @ftlvariable name="modifiedBy" type="java.lang.String" -->
<#-- @ftlvariable name="modifiedDate" type="java.lang.String" -->
<#-- @ftlvariable name="canComplete" type="boolean" -->
<#-- @ftlvariable name="aHREF" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="objectNameError" type="java.lang.String" -->
<#-- @ftlvariable name="objectDescriptionError" type="java.lang.String" -->
<#-- @ftlvariable name="generalErrorList" type="java.util.ArrayList<java.lang.String>" -->
<#-- @ftlvariable name="objectErrorList" type="java.util.ArrayList<java.lang.String>" -->

<script type="text/javascript" src="${baseUrl}/scripts/animatedcollapse.js">
    /* this needs to be placed at the top of the file so that we can add divs as they are created !!!! */
    /***********************************************
     * Animated Collapsible DIV v2.4- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
     * This notice MUST stay intact for legal use
     * Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
     ***********************************************/
</script>

<div id="title_pane">
    <h3>${title!""}</h3>
</div>

<div id="content_pane">
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

<br>
<#include "parent_details.html.ftl"/>
<form name="create_project" enctype="multipart/form-data" action="${postUrl}" method="POST" onsubmit="">
<#if initialized>
    <div id="buttonwrapper">
        <button type="submit" name="reinitialize" value="reinitialize">ReInitialize</button>
    </div>
<#else>

	<h3 class="underlined_heading">${typeName!""} Details
		<a href="javascript:animatedcollapse.toggle('details')" icon="toggle" title="View Details"></a>
	</h3>
	<div id="details">  <!-- Collapsible div -->

		<div id="project_details" class="fieldset">
			<!-- standard attributes -->
			<ol>
	        	<li>
	            <label for="object_name" class="bold">Title:
					<span icon="required"></span>
				</label>
                </li>
                <li>
                <input id="object_name" name="object_name" type="text" value="${objectName!""}">
                <h6 class="errorMsg">${objectNameError!""}</h6>
	            </li>
	            <li>
	            <label for="object_description" class="bold">Description:</label>
                </li>
                <li>
				<textarea id="object_description" name="object_description" cols="30" rows="2">${objectDescription!""}</textarea>
                <span id="object_desc_text_limit"></span>
                <h6 class="errorMsg">${objectDescriptionError!""}</h6>
				</li>

	            <!-- ontology attributes: data, object and field set properties -->
                <#if elementList?? && elementList?has_content>
	            <#list elementList as element>
                    <@displayItem element=element/>
	            </#list>
                </#if>

                <!-- creation infomation -->
                <#if  createdBy??>
                <li><span class="bold">Creator: </span>${createdBy}</li>
                </#if>
                <#if  createdDate??>
                <li><span class="bold">Creation date: </span>${createdDate!""}</li>
                </#if>
                <#if  modifiedBy??>
                <li><span class="bold">Last modified by: </span>${modifiedBy}</li>
                </#if>
                <#if  modifiedDate??>
                <li><span class="bold">Last modification date: </span>${modifiedDate!""}</li>
                </#if>
            </ol>
        </div>
    </div>  <!-- details - Collapsible div -->

    <#if isProject?? && isProject && canViewProjectParticipants>
        <br>
        <#include "projectParticipants.html.ftl"/>
    </#if>

    <!-- object referencing -->
    <#if refersList?? && refersList?has_content>
    <#list refersList as element>
        <br>
        <@displayTable element=element/>
    </#list>
    </#if>


    <#if  fileList?? && fileList?has_content>
    <br>
    <#include "attachedFilesDetails.html.ftl"/>
    </#if>

    <br>
    <#include "attachFile.html.ftl"/>

    <br>
    <h3 class="underlined_heading"> </h3> <!-- just want the line -->

    <div id="buttonwrapper">
    	<button type="submit" name="createObject" value="createObject">Submit</button>
    	<button type="reset" name="reset" value="reset">Reset</button>
        <button type="submit" name="cancel" value="cancel">Cancel</button>
        <button type="submit" name="reinitialize" value="reinitialize">ReInitialize</button>
    </div>
</#if>
</form>
</div>  <!-- content pane -->

<#macro displayTable element>
    <h3 class="underlined_heading">${element.getLabel()!""}
        <#if element.isRequired()>
        <span icon="required"></span>
        </#if>
		<a href="javascript:animatedcollapse.toggle('${element.getPropertyUriWithoutNamespace()}_details')" icon="toggle" title="View Details"></a>
	</h3>
	<div id="${element.getPropertyUriWithoutNamespace()}_details">  <!-- Collapsible div -->

    <#assign objectCount = 0>
    <#if isAdmin>
        <#assign objectCount = element.getAvailableObjects()?size>
    <#else>
        <#assign objectCount = 0>
        <#list element.getAvailableObjects() as object>
            <#if object.getState() == "A" || (object.isSelected() && isAdmin)>
            <#assign objectCount = objectCount + 1>
            </#if>
        </#list>
    </#if>

    <#if objectCount == 0>
    <p>No objects available for selection.</p>
    <#else>

    <table id='${element.getPropertyUriWithoutNamespace()}' class="tablesorter {sortlist: [[0,1], [1,0]]}" cellspacing="0">
		<thead>
			<tr>
			    <th>Select</th>
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
			<#list element.getAvailableObjects() as object>
            <!-- deleted objects can still be unchecked -->
            <#if object.getState() == "A" || isAdmin>
            <tr>
                <#if element.getCard()?? && element.getCard() = 1>
                    <#if object.isSelected()>
                    <td><input type="radio" class="narrow" name="${element.getPropertyURI()}" value="${object.getPid()!""}"
                        checked onchange="update${element.getPropertyUriWithoutNamespace()}Cache();"
                        <#if !element.isEditable()> disabled="true"</#if>
                    /></td>
                    <#else>
                    <td><input type="radio" class="narrow" name="${element.getPropertyURI()}" value="${object.getPid()!""}"
                        onchange="update${element.getPropertyUriWithoutNamespace()}Cache();"
                        <#if !element.isEditable() || (object.getState() != "A")> disabled="true"</#if>
                    /></td>
                    </#if>
                <#else>
                    <#if object.isSelected()>
                    <td><input type="checkbox" class="narrow" name="${element.getPropertyURI()}" value="${object.getPid()!""}"
                        checked onchange="update${element.getPropertyUriWithoutNamespace()}Cache();"
                        <#if !element.isEditable()> disabled="true"</#if>
                    /></td>
                    <#else>
                    <td><input type="checkbox" class="narrow" name="${element.getPropertyURI()}" value="${object.getPid()!""}"
                        onchange="update${element.getPropertyUriWithoutNamespace()}Cache();"
                        <#if !element.isEditable() || (object.getState() != "A")> disabled="true"</#if>
                    /></td>
                    </#if>
                </#if>
				<td>
                    <a href="${baseUrl}/object/${object.getPid()!""}">${object.getPid()!""}</a>
                <#if object.getState() != "A">(<span class="descriptive">Deleted</span>)</#if>
                </td>
				<td>${object.getTitle()!""}</td>
				<td>
                    <script type="text/javascript">writeAbstractWholeWords("${object.getDescription()!" - "}", 200);</script>
				</td>
			</tr>
            </#if>
			</#list>
		</tbody>
    </table>
    </#if>
    </div>

    <script type="text/javascript">
	    animatedcollapse.addDiv('${element.getPropertyUriWithoutNamespace()}_details', 'fade=1,hide=0');
    </script>
    <script type="text/javascript">
        function update${element.getPropertyUriWithoutNamespace()}Cache() {
            $("#${element.getPropertyUriWithoutNamespace()}").trigger("update");
            $("#${element.getPropertyUriWithoutNamespace()}").trigger("appendCache");
        }
    </script>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#${element.getPropertyUriWithoutNamespace()}")
            .tablesorter({
                headers: {
                    <#if element.getCard()?? && element.getCard() = 1>
                    0: { sorter: 'radio'}
                    <#else>
                    0: { sorter: 'checkbox'}
                    </#if>
                },
                sortMultiSortKey: 'ctrlKey'
            });
        });
    </script>
</#macro>

<#macro addFieldset element>
    <div id="${element.getPropertyURI()}_fieldset" class="fieldset inner">
        <div class="legend"><a name="${element.getGroupId()}">${element.getLabel()}</a>
            <#if element.isRequired()>
            <span icon="required"></span>
            </#if>
            <#if !element.getCard()?? && !element.getMaxCard()??>
            <button type="submit" icon="addField" name="addFieldset" value="${element.getId()!""}"></button>
            </#if>
        </div>
        <#if element.getFieldsetElementList()?? && element.getFieldsetElementList()?has_content>
             <!-- add the items fron each of the lists (data, object, fieldset and refersTo properties) to the field set -->
             <#list element.getFieldsetElementList() as elementList>
                <#if elementList?? && elementList?has_content>
                <ol>
                <#list elementList as element>
                    <@displayItem element=element/>
                </#list>
                </ol>
                </#if>
            </#list>
         </#if>
    </div>
</#macro>

<#macro displayItem element>
    <#if element.getType() == enums["podd.template.content.HTMLElementType"].FIELD_SET>
    <@addFieldset element=element/>
    <#else>
        <!-- work around as only some users have access to change project status -->
        <#if element.getPropertyURI() == "http://www.podd.org/poddModel#hasProjectStatus">
            <#if canComplete>
            <@addItem element=element/>
            </#if>
        <#else>
        <@addItem element=element/>
        </#if>
    </#if>
</#macro>

<#macro addItem element>
    <li>
    <label for="${element.getId()}" class="bold">${element.getLabel()}:
        <#if element.isRequired()>
        <span icon="required"></span>
        </#if>
        <#-- add button to add extra input fields if it is required -->
        <#if !element.getCard()?? && !element.getMaxCard()??>
            <#assign type="unknown">
            <#if element.getType() == enums["podd.template.content.HTMLElementType"].TEXT_AREA>
            <#assign type="textarea">
            <#elseif element.getType() == enums["podd.template.content.HTMLElementType"].INPUT>
            <#assign type="input">
            </#if>
            <a href="javascript:addField('${element.getId()}', '${type}')" icon="addField" title="add ${element.getLabel()}"></a>
        </#if>
    </label>
    </li>
    <#if element.getType() == enums["podd.template.content.HTMLElementType"].TEXT_AREA ||
         element.getType() == enums["podd.template.content.HTMLElementType"].INPUT>
        <@addField element=element/>
    <#elseif element.getType() == enums["podd.template.content.HTMLElementType"].DROP_DOWN_LIST>
        <@addDropDown element=element/>
    </#if>
    <#if element.errorMessages?has_content>
        <li>
        <h6 class="errorMsg">
        <#list element.errorMessages as errorMessage>
        ${errorMessage}<br>
        </#list>
        </h6>
        </li>
    </#if>
</#macro>

<#macro addField element>
    <#assign count=1>
    <#if element.getCard()??>
    <#assign count=element.getCard()>
    </#if>
    <#if element.getMaxCard()??>
    <#assign count=element.getMaxCard()>
    </#if>

    <#if element.getEnteredValues()?has_content>
    <#list element.getEnteredValues() as value>
        <#if element.getType() == enums["podd.template.content.HTMLElementType"].TEXT_AREA>
        <li><textarea id="${element.getId()}" name="${element.getId()}" cols="30" rows="2"
        <#if !element.isEditable()> disabled="true"</#if>
        >${value}</textarea></li>
        <#else>
        <li><input id="${element.getId()}" name="${element.getId()}" type="text" value="${value}"
        <#if !element.isEditable()> disabled="true"</#if>
        ></li>
        </#if>
    </#list>
    <#else>
    <#list 1..count as x>
        <#if element.getType() == enums["podd.template.content.HTMLElementType"].TEXT_AREA>
        <li><textarea id="${element.getId()}" name="${element.getId()}" cols="30" rows="2"
        <#if !element.isEditable()> disabled="true"</#if>
        >${element.getDefaultValue()}</textarea></li>
        <#else>
        <li><input id="${element.getId()}" name="${element.getId()}" type="text" value="${element.getDefaultValue()}"
        <#if !element.isEditable()> disabled="true"</#if>
        ></li>
        </#if>
    </#list>
    </#if>
    <!-- holder for additional fields -->
    <#if !element.getCard()?? && !element.getMaxCard()??>
    <span id="add_${element.getId()}"></span>
    </#if>
</#macro>

<#macro addDropDown element>
    <#if element.getEnteredValues()?has_content>
    <#list element.getEnteredValues() as selectedValue>
    <li>
    <select id="${element.getId()}" name="${element.getId()}"
    <#if !element.isEditable()> disabled="true"</#if>
    >
        <#list element.getPossibleValues() as value>
        <#if value == selectedValue>
        <option value="${value}" selected>${value}</option>
        <#else>
        <option value="${value}">${value}</option>
        </#if>
        </#list>
    </select>
    </li>
    </#list>
    <#else>
    <li>
    <select id="${element.getId()}" name="${element.getId()}"
    <#if !element.isEditable()> disabled="true"</#if>
    >
        <#list element.getPossibleValues() as value>
        <option value="${value}">${value}</option>
        </#list>
    </select>
    </li>
    </#if>
</#macro>

<script type="text/javascript" src="${baseUrl}/scripts/jquery.tablesorter.js"></script>
<script type="text/javascript">
    $.tablesorter.addParser( {
        id: "checkbox",
        is: function(s) {
            return false;
        },
        format: function(s, tabe, cell) {
            // format your data for normalization
            var checked = $(cell).children(":checkbox").get(0).checked;
            return  checked  ? 1 : 0;
        },
        type: "numeric"
    });
    $.tablesorter.addParser( {
        id: "radio",
        is: function(s) {
            return false;
        },
        format: function(s, tabe, cell) {
            // format your data for normalization
            var checked = $(cell).children(":radio").get(0).checked;
            return  checked  ? 1 : 0;
        },
        type: "numeric"
    });
</script>

<script type="text/javascript">
	animatedcollapse.addDiv('parent_details', 'fade=1,hide=0');
    animatedcollapse.addDiv('details', 'fade=1,hide=0');
    animatedcollapse.addDiv('project_participants', 'fade=1,hide=0');
	animatedcollapse.addDiv('files', 'fade=1,hide=0');
    animatedcollapse.addDiv('attach_file', 'fade=1,hide=1');
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
    function addField(id, type) {
        var element = document.createElement(type);
        element.setAttribute('id', id);
        element.setAttribute('name', id);
        var li = document.createElement("li");
        li.appendChild(element);
        document.getElementById('add_' + id).appendChild(li);
    }
</script>

<script type="text/javascript">
    $(document).ready(function() {
        // scroll to the section we have just added
        <#if aHREF??>
        window.location.hash="${aHREF}";
        </#if>
    });
</script>

<script type="text/javascript">
    $(document).ready(function() {
        // limit the number of characters in the object decription to 255
        $('#object_description').inputlimiter({
            limit: 1024,
            boxId: 'object_desc_text_limit',
	        boxAttach: false
        });
    });
</script>

<script type="text/javascript" src="${baseUrl}/scripts/jquery.autocomplete.js"></script>
<script type="text/javascript">
    $(document).ready(function(){

        $("#hasLeadInstitution").autocomplete("${baseUrl}/services/user/list", {
            extraParams: {format: "institution"},
            max: 0,
            multiple: false,
            mustMatch: false,
            matchSubset: false,
            dataType: "json",
            parse: function(data) {
                return parseData(data);
            },
            formatItem: function(row, i, max, term) {
                return formatItem(row);
            },
            formatResult: function(row) {
                return formatResult(row);
            }
        });

        function parseData(data) {
            return $.map(data, function(row) {
                return {
                    data: row,
                    value: row.institution,
                    result: row.institution
                }
            });
        }

        function formatItem(row) {
            return row.institution;
        }

        function formatResult(row) {
            return row.institution;
        }
    });
</script>