<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="stopRefreshKey" type="java.lang.String" -->

<#-- -- values that don't have to be set -- -->
<#-- @ftlvariable name="fileDescription" type="java.lang.String" -->
<#-- @ftlvariable name="fileErrorMessage" type="java.lang.String" -->
<#-- @ftlvariable name="fileDescriptionError" type="java.lang.String" -->
<#-- @ftlvariable name="attachedFileList" type="java.util.List<String>" -->

<h3 class="underlined_heading" id="startAttachFiles">Attach Files
    <a href="javascript:animatedcollapse.toggle('attach_file')" icon="toggle" title="Attach Files" name="attach_files"></a>
</h3>

<div id="attach_file">  <!-- Collapsible div -->
    <div id="file_upload" class="fieldset_without_border">
        <ol>
            <li <#if (hideDescription)?? && hideDescription >style="display:none"</#if>>
                <label for="file_description" class="bold"">File Description:</label>
                <textarea id="file_description" name="file_description" cols="30"
                          rows="3">${fileDescription!""}</textarea>
                <span id="file_desc_text_limit"></span>
                <h6 class="errorMsg">${fileDescriptionError!""}</h6>
            </li>
            <li>
                <label class="bold">Attach new File:</label>
                <script language="JavaScript" type="text/javascript"><!--
                var _info = navigator.userAgent;
                var _ns = false;
                var _ns6 = false;
                var _ie = (_info.indexOf("MSIE") > 0 && _info.indexOf("Win") > 0 && _info.indexOf("Windows 3.1") < 0);
                //--></script>
                <comment>
                    <script language="JavaScript" type="text/javascript"><!--
                    var _ns = (navigator.appName.indexOf("Netscape") >= 0 && ((_info.indexOf("Win") > 0 && _info.indexOf("Win16") < 0 && java.lang.System.getProperty("os.version").indexOf("3.5") < 0) || (_info.indexOf("Sun") > 0) || (_info.indexOf("Linux") > 0) || (_info.indexOf("AIX") > 0) || (_info.indexOf("OS/2") > 0) || (_info.indexOf("IRIX") > 0)));
                    var _ns6 = ((_ns == true) && (_info.indexOf("Mozilla/5") >= 0));
                    //--></script>
                </comment>

                <script language="JavaScript" type="text/javascript">
                if (_ie == true) {
                    document.write('<object ',
                                   'classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"',
                                   'width="720"',
                                   'height="360"',
                                   'codebase="http://java.sun.com/update/1.5.0/jinstall-1_5-windows-i586.cab#Version=5,0,0,3">',
                                   '<param name="code" value="wjhk.jupload2.JUploadApplet.class">',
                                   '<param name="archive" value="${baseUrl}/fileupload/wjhk.jupload.jar">',
                                    '<param name="scriptable" value="false">',
                                    '<param name="formdata" value="create_project">',
                                    '<param name="stringUploadSuccess" value="">',
                                    '<param name="lookAndFeel " value="system">',
                                    '<param name="showLogWindow" value="false">',
                                    '<param name="stringUploadSuccess" value="">',
                                    '<param name="afterUploadURL" value="javascript:refresh();">',
                                   '</object>');
                } else {
                    document.write('<applet ',
                            'width="720"',
                            'height="360"',
                            'code="wjhk.jupload2.JUploadApplet.class" archive="${baseUrl}/fileupload/wjhk.jupload.jar">',
                            'name="JUploadApplet">',
                            '<param name="code" value="wjhk.jupload2.JUploadApplet.class">',
                            '<param name="archive" value="${baseUrl}/fileupload/wjhk.jupload.jar">',
                            '<param name="scriptable" value="false">',
                            '<param name="formdata" value="create_project">',
                            '<param name="stringUploadSuccess" value="">',
                            '<param name="lookAndFeel " value="system">',
                            '<param name="showLogWindow" value="false">',
                            '<param name="stringUploadSuccess" value="">',
                            '<param name="afterUploadURL" value="javascript:refresh();">',
                            '</applet>');
                }
                </script>
                <h6 class="errorMsg">${fileErrorMessage!""}</h6>
            </li>
        <#if attachedFileList?has_content>
            <li>
                <a href="" style="visibility:hidden;" name="file_list" id="file_list"></a>
                <span class="bold">Attached Files: </span>
                <input id="fileToRemove" name="fileToRemove" value="" style="visibility:hidden;">
                <ol>
                    <#list attachedFileList as file>
                        <li>
                            <button type="submit" class="remove_file_btn" name="removeFile"
                            value="${file!""}" onclick=setFileToRemove("${file}")></button>
                            <script language="JavaScript" type="text/javascript">
                                document.write(decodeURI("${file}"));
                            </script>
                        </li>
                    </#list>
                </ol>
            </li>
            <script language="JavaScript" type="text/javascript">
                function setFileToRemove(filename) {
                    document.getElementById('fileToRemove').value = filename;
                }
            </script>
        </#if>
        </ol>
    </div>
</div>  <!-- file_upload_div - Collapsible div -->

<script type="text/javascript">
    $(document).ready(function() {
        // limit the number of characters in the file decription text area
        $('#file_description').inputlimiter({
            limit: 255,
            boxId: 'file_desc_text_limit',
            boxAttach: false
        });
    });
</script>
<script type="text/javascript">
    function refresh() {
        var refresh_pat = /${stopRefreshKey}=/;
        var toggle_pat = /expanddiv=/;
        var href_pat = /#attach_files/;
        var ie_href_pat = /#file_list/;
        var url = window.location.href;
        if (null == url.match(refresh_pat)) {
            if (null != url.match(/\?/)) {
                url = url + '&${stopRefreshKey}=true';
            } else {
                url = url + '?${stopRefreshKey}=true';
            }
        }
        if (null == url.match(toggle_pat)) {
            url = url + '&expanddiv=attach_file';
        }
        if (_ie != true) {
            if (null == url.match(href_pat)) {
                url = url + '#attach_files';
            }
            window.location.reload();
            window.location = url;
        } else {
            var reload = true;
            if (null == url.match(ie_href_pat)) {
                reload = false;
                url = url + '#file_list';
            }
            window.location = url;
            animatedcollapse.show('attach_file');
            window.location.href = '#file_list';
            if (reload) {
                // it seems like we have the choice when adding another file,
                // either we show the newly added file or we can scroll to the attach file section
                // i have chosen to show the newly added file and make the user scroll down the page themselves
                window.location.reload();
            }
        }
    }
</script>