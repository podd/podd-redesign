<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="forbidden" type="boolean" -->
<#-- @ftlvariable name="pid" type="java.lang.String" -->
<#-- @ftlvariable name="objectType" type="java.lang.String" -->
<#-- @ftlvariable name="poddObject" type="podd.model.entity.PoddObject" -->
<#-- @ftlvariable name="referencedByCount" type="java.lang.Integer" -->
<#-- @ftlvariable name="referencedByList" type="java.util.ArrayList<podd.model.entity.PoddObject>" -->
<#-- @ftlvariable name="referencedByStartIndex" type="java.lang.Integer" -->
<#-- @ftlvariable name="referencedByEndIndex" type="java.lang.Integer" -->
<#-- @ftlvariable name="referencedByIndexIncrement" type="java.lang.Integer" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->

<div id="title_pane">
<h3>View Referers</h3>
</div>

<div id="content_pane">

<#if errorMessage?? && errorMessage != "">
<p>
<h4 class="errorMsg">${errorMessage!""}</h4>
</p>
<#else>
<br />
</#if>

<h3 class="underlined_heading">${objectType!"Object"} Details</h3>
    <div id="object_details" class="fieldset">
        <#if forbidden?? && forbidden>
            <h4 class="errorMsg">Object: ${pid} is deleted or inactive. Please contact reposity administrator
            if you believe it has been deleted in error.</h4>
        <#elseif poddObject??>
            <div about="${baseUrl}/object/${poddObject.getPid()!"unknown-pid"}" id="${objectType!"object"}_details" class="fieldset">
            <ol>
                <li><span class="bold">ID: </span><span property="podd:hasID" datatype="xsd:string">${poddObject.getPid()!""}</span></li>
                <li><span class="bold">Title: </span><span property="dcterms:title" datatype="xsd:string">${poddObject.getLocalName()!""}</span></li>
                <li><span class="bold">Description: </span><span property="dcterms:description" datatype="xsd:string">${poddObject.getLabel()!""}</span></li>
                <br />
                <li><a href="${baseUrl}/object/${poddObject.getPid()!"unknown"}" id="view_object_btn" class="button">View Object</a></li>
            </ol>
            </div>
        </#if>
    </div>
    <br />
    <div>
        <h3 class="underlined_heading">Referenced By</h3>

        <div id="referencedBy_details">
            <#if  poddObject??>
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
                    <tr about="${baseUrl}/object/${object.getPid()!"unknown-pid"}" >
                        <!-- TODO: extract rdf:type's from PoddConcept -->
                        <td>${object.getConcept().getConceptName()!"-"}</td>
                        <td><span property="podd:hasID" datatype="xsd:string"><a href="${baseUrl}/object/${object.getPid()}">${object.getPid()!""}</a></span>
                            <#if object.getState() != "A">
                             (<span class="descriptive">Deleted</span>)
                            </#if>
                        </td>
                        <td><span property="dcterms:title" datatype="xsd:string">${object.getLocalName()!""}</span></td>
                        <td>
                            <span style="display:none;" property="dcterms:description" datatype="xsd:string">${object.getLabel()!""}</span>
                            <script type="text/javascript">writeAbstractWholeWords("${object.getLabel()!" - "}", 200);</script>
                        </td>
                    </tr>
                    </#list>
                </tbody>
            </table>
            <#if 0 < referencedByStartIndex - 1>
                <#assign prevIndex = referencedByStartIndex - referencedByIndexIncrement - 1>
                <#if prevIndex < 0><#assign prevIndex = 0></#if>
                <a href="${baseUrl}/allReferers/${poddObject.getPid()}?index=${prevIndex}" class="previous_btn"><span>PREV</span></a> &nbsp;&nbsp;
            </#if>
            ${referencedByStartIndex} - ${referencedByEndIndex} of ${referencedByCount} &nbsp;&nbsp;
            <#if referencedByEndIndex < referencedByCount>
                <a href="${baseUrl}/allReferers/${poddObject.getPid()}?index=${referencedByEndIndex}" class="next_btn"><span>NEXT</span></a>
            </#if>
            <br>
            </#if>
        </div>
    </div>
</div> <!-- content pane -->

<script type="text/javascript" src="${baseUrl}/scripts/jquery.tablesorter.js"></script>
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