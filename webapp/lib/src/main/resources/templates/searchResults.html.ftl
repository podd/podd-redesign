<#-- @ftlvariable name="baseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="searchBaseUrl" type="java.lang.String" -->
<#-- @ftlvariable name="searchResponse" type="podd.search.SearchResponse" -->
<#-- @ftlvariable name="filterError" type="java.lang.String" -->

<#if (searchResponse)?? || filterError?? >
    <h3 class="underlined_heading" id="startSearchResults">Search Results
		<a href="javascript:animatedcollapse.toggle('searchResults')" icon="toggle" title="View Search Results"></a>
	</h3>

    <div>
    <div id="searchResults" class="fieldset minorPadding">
        <#if (filterError?? || searchResponse.numFound <= 0)>
            <#if filterError??>
            <h5 class="errorMsg">${filterError}</h5><br>
            </#if>
            No search results found.
        <#else>
            <!-- Range of results -->
            <div>
                Results <span class="bold">${(searchResponse.start)!"0"} - ${(searchResponse.end)!"0"}</span> of <span class="bold">${(searchResponse.numFound)!"0"}</span>
            </div>

            <!-- Page links -->
            <div class="center minorPadding">
                <#if (searchResponse.currentPage > 0)>
                    <span>
                        <a href='${searchBaseUrl}&start=${(searchResponse.currentPage-1) * searchResponse.pageSize}'>Previous</a>
                    </span>
                </#if>
                <#list 0..4 as page>
                    <#if page < searchResponse.totalNumberOfPages>
                        <#if page != searchResponse.currentPage>
                            <a href='${searchBaseUrl}&start=${ page * searchResponse.pageSize }'>${page + 1}</a>
                        <#else>
                            <span class="bold">${page + 1}</span>
                        </#if>
                    </#if>
                </#list>
                <#if (searchResponse.numFound > (searchResponse.start + searchResponse.pageSize))>
                    <span>
                            <a href='${searchBaseUrl}&start=${ (searchResponse.currentPage+1) * searchResponse.pageSize }' >Next</a>
                    </span>
                </#if>
            </div>

            <!-- Search results -->
            <#list searchResponse.results as doc>
            <div>
                <#if (doc.projectId)?? && projectMap?? && (projectMap[doc.projectId])?? >
                    <#assign project=(projectMap[doc.projectId])>
                    <span class="searchHeader">Project: </span>
                    <a href="${baseUrl}/object/${doc.projectId}">${(project.getLocalName())!""}</a>,&nbsp;&nbsp;&nbsp;
                    <span class="searchHeader">Principal Investigator: </span> ${(project.getPrincipalInvestigator().getFirstName())!""}&nbsp;${(project.getPrincipalInvestigator().getLastName())!""},&nbsp;&nbsp;&nbsp;
                    <span class="searchHeader">Lead Institution: </span> ${(project.getLeadInstitution())!""}
                    <br/>
                </#if>
                <span class="searchHeader">Object: </span>${doc.objectType!"Unavailable"}
                <#if (highlights[doc.id])??>
                    <!-- Display the title from the highlights, if it already exists, otherwise just display the title -->
                    ,&nbsp;&nbsp;&nbsp;
                    <span class="searchHeader">Title: </span>
                    <#if (highlights[doc.id]['title_t'])??>
                            <#list highlights[doc.id]['title_t'] as fieldHighlight>
                                ${fieldHighlight}<#if (fieldHighlight?length >= searchResponse.highlightFragSize) > ...</#if>
                            </#list>
                    <#else>
                        ${(doc.title_t)!"Unavailable"}
                    </#if>

                    <#list highlights[doc.id]?keys as field>
                        <!-- Never display the title in the list of attributes -->
                        <#if field != 'title_t'>
                            <#assign displayField=field>

                            <#-- Strip the ending _t -->
                            <#if field?ends_with("_t")>
                                <#assign displayField=field?substring(0,field?length-2)>
                            </#if>

                            <#-- Strip the object name delimited by underscore -->
                            <#if displayField?contains("_")>
                                <#assign fieldSplit=displayField?split("_")>
                                <#if (fieldSplit?size > 1)>
                                    <#assign displayField=fieldSplit[1]>
                                </#if>
                            </#if>

                            <#-- Insert space where capital letters are found -->
                            <#assign capLetters="ABCDEFGHIJKLMNOPQRSTUVWXYZ">
                            <#if (displayField?length > 0)>
                                <#assign stringBuffer="">
                                <#list 0..(displayField?length-1) as i>
                                    <#assign character=displayField[i]>
                                    <#if capLetters?contains(character)>
                                        <#assign stringBuffer=stringBuffer+" ">
                                    </#if>
                                    <#assign stringBuffer=stringBuffer+character>
                                </#list>
                                <#assign displayField=stringBuffer>
                            </#if>

                            <#-- Strip the leading 'has' if it exists -->
                            <#if displayField?starts_with("has")>
                                <#assign displayField=displayField?substring(3)>
                            </#if>
                            <#assign displayField=displayField?cap_first>
                            <br/><span class="objectField">${displayField}: </span>
                            <#list highlights[doc.id][field] as fieldHighlight>
                                ${fieldHighlight}<#if (fieldHighlight?length >= searchResponse.highlightFragSize) > ...</#if>
                            </#list>
                        </#if>
                    </#list>
                </#if>
                <br/><a href='${baseUrl}/object/${doc.id}'>${baseUrl}/object/${doc.id}</a>
            </div> <!-- searchResponse.result -->
            <br>
            </#list>
        </#if>
    </div>  <!-- search results -->
    </div>
</#if>