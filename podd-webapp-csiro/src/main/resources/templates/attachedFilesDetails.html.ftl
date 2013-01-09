<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="postUrl" type="java.lang.String" -->
<#-- @ftlvariable name="attachedFilesDownloadable" type="boolean" -->
<#-- @ftlvariable name="fileList" type="java.util.ArrayList<podd.resources.util.view.FileDisplayHelper.FileElement>" -->
<#-- @ftlvariable name="poddObject" type="podd.model.entity.PoddObject" -->

<#-- -- values that don't have to be set -- -->
<#-- @ftlvariable name="fileErrorMessage" type="java.lang.String" -->

<h3 class="underlined_heading">File Attachments
	<a href="javascript:animatedcollapse.toggle('files')" icon="toggle" title="View File Attachments"></a>
</h3>
<div id="files">  <!-- Collapsible div -->
    <#if fileErrorMessage??>
    <h6 class="errorMsg">${fileErrorMessage!""}</h6>
    </#if>

    <#if  fileList?? && fileList?has_content>
    <#if  poddObject?? && attachedFilesDownloadable?? && attachedFilesDownloadable>
        <form name="attached_files" action="${baseUrl}/object/${poddObject.getPid()!""}/bulk-download" method="POST" onsubmit="return checkSelected()">
    </#if>
    <table id="table" class="tablesorter {sortlist: []}" cellspacing="0">
        <thead>
            <tr>
                <#if attachedFilesDownloadable?? && attachedFilesDownloadable>
                <th></th>
                </#if>
                <th>File Name</th>
                <th>Size (kb)</th>
                <th>Type</th>
                <th>Format</th>
                <th>Description</th>
            </tr>
        </thead>
        <tfoot>
        <!-- empty table row, so that the footer appears and table looks complete -->
            <tr>
                <#if attachedFilesDownloadable?? && attachedFilesDownloadable>
                <td></td>
                </#if>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
            </tr>
        </tfoot>
        <tbody>
            <#list fileList as fileElement>
			<tr>
                <#if attachedFilesDownloadable?? && attachedFilesDownloadable>
                <td><input type="checkbox" class="narrow" id="${fileElement.getFileName()!""}" name="${fileElement.getFileName()!""}" value="checked"></td>
                </#if>
                <td><a href="${baseUrl}/${fileElement.getUrl()}">${fileElement.getFileName()!""}</a></td>
                <td>${fileElement.getSize()!""}</td>
                <td>${fileElement.getMimeType()!""}</td>
                <td>${fileElement.getFormat()!""}</td>
                <td><script type="text/javascript">writeAbstractWholeWords("${fileElement.getDescription()!""}", 160)</script></td>
			</tr>
			</#list>
		</tbody>
    </table>
    <#if attachedFilesDownloadable?? && attachedFilesDownloadable>
    <div id="buttonwrapper">
        <button type="submit" name="downloadSelected" value="downloadSelected" onclick="selectAll=false">Download Selected</button>
        <button type="submit" name="downloadAll" value="downloadAll" onclick="selectAll=true">Download All</button>
    </div>
    <#if  poddObject?? && attachedFilesDownloadable?? && attachedFilesDownloadable>
    </form>
    </#if>
    <br /><br /><br />
    </#if>
    <#else>
    <p>No files are attached to this object.</p>
    </#if>

</div>  <!-- files - Collapsable div -->

<#if  attachedFilesDownloadable?? && attachedFilesDownloadable>
<script type="text/javascript">
    var selectAll;

    function checkSelected() {
        var anyChecked = selectAll;
        if (!selectAll) {
            <#if  fileList??>
            <#list fileList as fileElement>
                if(document.getElementById('${fileElement.getFileName()}').checked){
                    anyChecked = true;
                }
            </#list>
            </#if>
        }
        if (!anyChecked) {
            alert("You must select at least one file to download\n or click 'Download All'.");
        }
        return anyChecked;
    }
</script>
</#if>