<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="target" type="podd.model.entity.PoddObject" -->
<#-- @ftlvariable name="targetPredicate" type="java.lang.String" -->
<#-- @ftlvariable name="cbItems" type="java.util.AbstractList<podd.util.copypaste.ClipBoardElement>" -->
<#-- @ftlvariable name="cbCount" type="java.lang.Integer" -->
<#-- @ftlvariable name="maxCbCount" type="java.lang.Integer" -->

<div id="title_pane">
<h3><#if !target??>Edit </#if>Clipboard</h3>
</div>

<div id="content_pane">

<#if errorMessage??>
<div id="error">
    <h4 class="errorMsg">${errorMessage!""}</h4>
    <br>
</div>
</#if>

<#if  target??>
<h3 class="underlined_heading">Target Details</h3>

<div id="parentInfo"class="fieldset">
    <div class="legend">Target Object Summary Information</div>
    <ol>
    <li><span class="bold">ID: </span><a href="${baseUrl}/object/${target.getPid()!""}">${target.getPid()!""}</a></li>
    <li><span class="bold">Type: </span>${target.getConcept().getConceptName()!""}</li>
    <li><span class="bold">Title: </span>${target.getLocalName()!""}</li>
    <#if targetPredicate??>
    <li><span class="bold">Target Relationship: </span>${targetPredicate}</li>
    </#if>
    </ol>
</div>
<br>
</#if>

<form name="clipboard" enctype="multipart/form-data" action="${baseUrl}/clipboard" method="POST" onsubmit="">
	<h3 class="underlined_heading">Clipboard</h3>
	<div id="details">
        <#if cbCount == 0>
        <br><strong>Your clipboard is empty. Try copy something in the project browser or object details pages.</strong>
        <#else>
            <div id="clipboard" class="fieldset_without_border">
                Displaying ${cbCount!0} Clipboard Object (Maximum ${maxCbCount!0})
                <@button_group/> <br>
                <@list_items/> <br>
                <@button_group/>
            </div>
        </#if>
    </div>
</form>
</div>  <!-- content - div -->

<#macro button_group>
<div id="buttonwrapper_right">
    <#if target??>
        <button type="submit" name="paste" value="paste">Paste</button>
    <#else>
        <button type="submit" name="deleteAll" value="deleteAll">Delete All</button>
        <button type="submit" name="delete" value="delete">Delete Selected</button>
    </#if>
</div>
</#macro>

<#macro list_items>
<#if cbItems??>
    <div id="listCboardElements">
        <table id='table' class="tablesorter {sortlist: [[0,0]]}" cellspacing="0">
            <thead>
                <tr>
                    <th>Select</th>
                    <th>Title</th>
                    <th>Type</th>
                    <th>Parent</th>
                    <th>Parent Type</th>
                    <#if target??>
                    <th>Depth</th>
                    <th>Relationship</th>
                    </#if>
                </tr>
            </thead>
            <tbody>
                <#list cbItems as cbElement>
                <tr>
                    <td>
                        <#if target??>
                            <#if cbElement.canPaste()>
                            <input id="${cbElement.getId()!""}" class="narrow" name="selected" type="checkbox" value="${cbElement.getId()!""}">
                            <#else>
                            &nbsp;&nbsp;&nbsp;&nbsp;
                            </#if>
                        <#else>
                        <!-- if target is not defined we are in the edit clipboard screen -->
                        <input id="${cbElement.getId()!""}" class="narrow" name="selected" type="checkbox" value="${cbElement.getId()!""}">
                        </#if>
                        <span icon="${cbElement.getType()!""}"></span>
                    </td>

                    <#if cbElement.getType() = "object">
                        <#if !cbElement.isDeleted()>
                        <td><a href="${baseUrl}/object/${cbElement.getPid()!""}">${cbElement.getTitle()!""}</a></td>
                        <#else>
                        <td>${cbElement.getTitle()} (<span class="errorMsg">Deleted</span>)</td>
                        </#if>
                    <#elseif cbElement.getType() = "predicate">
                        <#if !cbElement.isDeleted()>
                        <td>${cbElement.getTitle()} (Objects: ${cbElement.getChildCount()})</td>
                        <#else>
                        <td>${cbElement.getTitle()} (Objects: ${cbElement.getChildCount()}, <span class="errorMsg">Deleted ${cbElement.getDeletedChildCount()}</span>)</td>
                        </#if>
                    <#else>
                        <td>${cbElement.getTitle()}
                        <#if cbElement.isDeleted()>(<span class="errorMsg">Deleted</span>)</#if>
                        </td>
                    </#if>

                    <td>${cbElement.getDisplayType()!""}</td>

                    <#if !cbElement.isParentDeleted()>
                    <td><a href="${baseUrl}/object/${cbElement.getParentPid()!""}">${cbElement.getParentTitle()!""}</a></td>
                    <#else>
                    <td>${cbElement.getParentTitle()} (<span class="errorMsg">Deleted</span>)</td>
                    </#if>

                    <td>${cbElement.getParentType()!""}</td>

                    <#if target??>
                        <td>
                            <#if cbElement.getType() != "file" && cbElement.canPaste()>
                            <select id="${cbElement.getId()}:depth" name="${cbElement.getId()}:depth">
                                <option value="shallow" selected>Shallow</option>
                                <option value="deep">Deep</option>
                            </select>
                            </#if>
                        </td>

                        <td>
                            <#if cbElement.canSelectPredicate()>
                                <select id="${cbElement.getId()}:predicate" name="${cbElement.getId()}:predicate">
                                    <#list cbElement.getPredicateList() as predicate>
                                    <option value="${predicate}">${predicate}</option>
                                    </#list>
                                </select>
                            </#if>
                        </td>
                    </#if>
                </tr>
                </#list>
            </tbody>
        </table>
    </div>  <!-- list clip board element -->
</#if>
</#macro>