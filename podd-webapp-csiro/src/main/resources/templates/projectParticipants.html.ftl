<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="pi" type="java.lang.String" -->
<#-- @ftlvariable name="piError" type="java.lang.String" -->
<#-- @ftlvariable name="admin" type="java.lang.String" -->
<#-- @ftlvariable name="adminError" type="java.lang.String" -->
<#-- @ftlvariable name="member" type="java.lang.String" -->
<#-- @ftlvariable name="memberError" type="java.lang.String" -->
<#-- @ftlvariable name="observer" type="java.lang.String" -->
<#-- @ftlvariable name="observerError" type="java.lang.String" -->

<h3 class="underlined_heading">Project Participants
	<a href="javascript:animatedcollapse.toggle('project_participants')" icon="toggle" title="View Project Participants"></a>
</h3>

<div id="project_participants">
    <div id="participants" class="fieldset">
        <ol>
            <li>
                <label for="pi" class="bold">Principal Investigator:
                    <span icon="required"></span>
                </label>
            </li>
            <li>
                <input class="wide" id="pi" name="pi" value="${pi!""}">
                <br>Only one Principal Investigator is allowed.
                Principal Investigators have Project Administrator status by default.
                <h6 class="errorMsg">${piError!""}</h6>
            </li>
            <li>
                <label for="admin" class="bold">Project Administrators: </label>
            </li>
            <li>
                <textarea class="high" id="admin" name="admin">${admin!""}</textarea>
                <br>Project Administrators will have Create, Read, Update and Delete access to all project objects.
                <h6 class="errorMsg">${adminError!""}</h6>
            </li>
            <li>
                <label for="member" class="bold">Project Members: </label>
            </li>
            <li>
                <textarea class="high" id="member" name="member">${member!""}</textarea><br>
                Project Members will have Create, Read and Update access to all project objects.
                <h6 class="errorMsg">${memberError!""}</h6>
            </li>
            <li>
                <label for="observer" class="bold">Project Observers: </label>
            </li>
            <li>
                <textarea class="high" id="observer" name="observer">${observer!""}</textarea>
                <br>Project Observer will have Read only access to all project objects.
                <h6 class="errorMsg">${observerError!""}</h6>
            </li>
       </ol>
    </div>
</div>  <!-- Collapsable div -->

<script type="text/javascript" src="${baseUrl}/scripts/jquery.autocomplete.js"></script>
<script type="text/javascript">
    $(document).ready(function(){

        $("#pi").autocomplete("${baseUrl}/services/user/list", {
            extraParams: {format: "participants"},
            max: 0,
            multiple: false,
            mustMatch: false,
            matchSubset: false,
            dataType: "json",
            parse: function(data) {
                return parseData(data);
            },
            formatItem: function(row, i, max) {
                return formatItem(row);
            },
            formatResult: function(row) {
                return formatResult(row);
            }
        });
        $("#admin").autocomplete("${baseUrl}/services/user/list", {
            extraParams: {format: "participants"},
            max: 0,
            multiple: true,
            multipleSeparator: "; ",
            mustMatch: false,
            matchSubset: false,
            dataType: "json",
            parse: function(data) {
                return parseData(data);
            },
            formatItem: function(row, i, max) {
                return formatItem(row);
            },
            formatResult: function(row) {
                return formatResult(row);
            }
        });
        $("#member").autocomplete("${baseUrl}/services/user/list", {
            extraParams: {format: "participants"},
            max: 0,
            multiple: true,
            multipleSeparator: "; ",
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
        $("#observer").autocomplete("${baseUrl}/services/user/list", {
            extraParams: {format: "participants"},
            max: 0,
            multiple: true,
            multipleSeparator: "; ",
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
                    value: row.username,
                    result: row.name + ", " + row.institution + ", " + row.email
                }
            });
        }

        function formatItem(row) {
            return row.name + ", " + row.institution + ", " + row.email;
        }

        function formatResult(row) {
            return row.name + ", " + row.institution + ", " + row.email;
        }
    });
</script>