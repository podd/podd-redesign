<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="user" type="podd.model.user.User" -->
<#-- @ftlvariable name="projectStatuses" type="java.util.ArrayList<podd.model.entity.ProjectStatus>" -->
<#-- @ftlvariable name="searchProjectStatus" type="podd.model.entity.ProjectStatus" -->
<#-- @ftlvariable name="publicationStatuses" type="java.util.ArrayList<podd.model.project.Project.ProjectPublicationStatus>" -->
<#-- @ftlvariable name="searchPublicationStatus" type="podd.model.project.Project.ProjectPublicationStatus" -->

<div id="title_pane">
    <h3>Filter Projects</h3>
</div>

<div id="content_pane">

    <#include "search_aux.html.ftl"/>

    <div id="main">
        <form enctype="multipart/form-data" method="post" action="/podd/projectsfilter">

            <h3 class="underlined_heading">Filter Projects Query
                <a href="javascript:animatedcollapse.toggle('searchContent')" icon="toggle" title="View Filter Projects Query"></a>
            </h3>

            <#escape x as x?html>
            <div id="searchContent">
                <div id="searchTerms" class="fieldset">

                    <!-- filter scope -->
                    <ol>
                        <li><h3 class="underlined_heading">Project Types</h3></li>
                        <#if (user)??>
                        <#include "searchScope.html.ftl">
                        <#else>
                            <!-- Search for public records only for anonymous users (with hidden check box) -->
                            <input type="checkbox" class="narrow" name="scopePublicProjects" checked="checked" style="display:none" />
                        </#if>
                    </ol>

                    <!-- filter terms -->
                    <ol>
                        <li><h3 class="underlined_heading">Filter Terms</h3></li>
                        <#include "searchWords.html.ftl">
                    </ol>

                    <!-- Filter date -->
                    <ol>
                        <li><h3 class="underlined_heading">Filter Date</h3></li>
                        <#include "searchCreationDate.html.ftl">
                    </ol>

                    <!-- Filter status fields -->
                    <ol>
                        <li><h3 class="underlined_heading">Filter Status Fields</h3></li>

                        <li>
                            <label for="projectStatus" class="bold">Project Status:</label>
                            <select searchSelect='true' id="projectStatus" name="projectStatus">
                                <option value="">All</option>

                                <#list projectStatuses as projectStatus>
                                <option value="${projectStatus}"
                                    <#if searchProjectStatus?? && (searchProjectStatus == projectStatus) >
                                        selected="true"
                                    </#if>
                                >${projectStatus}</option>
                                </#list>
                            </select>
                        </li>

                        <li>
                            <label for="publicationStatus" class="bold">Publication Status:</label>
                            <select searchSelect='true' id="publicationStatus" name="publicationStatus">
                                <option value="">All</option>

                                <#list publicationStatuses as publicationStatus>
                                <option value="${publicationStatus}"
                                    <#if searchPublicationStatus?? && (searchPublicationStatus == publicationStatus) >
                                        selected="true"
                                    </#if>
                                >${publicationStatus}</option>
                                </#list>
                            </select>
                        </li>
                    </ol>

                    <!-- submit buttons -->
                    <div id="buttonwrapper">
                        <button type="submit">Filter</button>
                        <button type="button" onclick='resetSearchQuery();'>Reset</button>
                    </div>
                    <!-- if this is not here the buttons fall out of the fieldset?? -->
                    <p><br></p>
                </div>
            </div>
            </#escape>
        </form>
    </div> <!-- main -->
</div>  <!-- content pane -->


<script type="text/javascript" src="${baseUrl}/scripts/animatedcollapse.js">
/***********************************************
* Animated Collapsible DIV v2.4- (c) Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for this script and 100s more
***********************************************/
</script>
<script type="text/javascript">
	animatedcollapse.addDiv('searchContent', 'fade=1,hide=0')

	animatedcollapse.ontoggle=function($, divobj, state){
		//fires each time a DIV is expanded/contracted
		//$: Access to jQuery
		//divobj: DOM reference to DIV being expanded/ collapsed. Use "divobj.id" to get its ID
		//state: "block" or "none", depending on state
	}
	animatedcollapse.init()
</script>


<script type="text/javascript">
    function resetSearchQuery() {
        $("input[searchText='true']").each(function() {
            this.value = '';
        });

        $("input[searchSelect='true']").each(function() {
            this.selectedIndex = 0;
        });

        $("input[searchCheckbox='true']").each(function() {
            this.checked = true;
        });

        document.getElementById('projectStatus').value = "";
        document.getElementById('publicationStatus').value = "";
	}
</script>