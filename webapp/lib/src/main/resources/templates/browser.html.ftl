<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="project" type="podd.model.project.Project" -->
<#-- @ftlvariable name="errorMessage" type="java.lang.String" -->

<div id="title_pane">
    <h3>Browser Pane</h3>
</div>

<div id="content_pane">

<#if errorMessage?? && errorMessage != "">
<p>
<h4 class="errorMsg">${errorMessage!""}</h4>
<#else>
<br>
</#if>

<#if !project?? || !project?has_content>
    <p>No project selected. <br>
       Please select a project in the <a href="${baseUrl}/projects">Projects pane</a>.</p>
<#else>

    <div id="browser_tree">
    <ul class="treeview" id="tree"></ul>
    </div>

    <script type="text/javascript" src="${baseUrl}/scripts/jquery.treeview.js"></script>
    <script type="text/javascript" src="${baseUrl}/scripts/jquery.treeview.async.js"></script>
    <script type="text/javascript">
        $(jQuery(document).ready(function() {
            $("#tree").treeview({
                collapsed: true,
                serviceURL: "${baseUrl}/services/browser",
                redirectURL: "${baseUrl}/object/",
                copyServiceURL: "${baseUrl}/services/manageClipboard",
                pasteURL: "${baseUrl}/clipboard",
                root: "${project.getPid()}"
            });
        }));
    </script>

    <script type="text/javascript">
        function copyClicked(href) {
            $.ajax({
                type: "POST",
                url: href,
                data: href,
                success: function(data) {
                    var responseObject = eval('(' + data + ')');
                    alert(responseObject.message.replace(/\+/g, " "));
                    //  this works correctly but has been commented out (at Gavin's request) as it is not part of the usecase  
                    //document.getElementById(responseObject.id + '_copy').firstChild.data = 'Copied';
                    //var removedElement = document.getElementById(responseObject.removedId + '_copy');
                    //if (removedElement) {
                    //    removedElement.firstChild.data = 'Copy';
                    //}
                },
                error: function(xhr, status, error) {
                    alert('error copying object: ' + status);
                }
            });
            return false;
        }
    </script>
</#if>

</div> <!-- content pane -->