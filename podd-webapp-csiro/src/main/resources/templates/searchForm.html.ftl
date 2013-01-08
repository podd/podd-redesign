<#-- @ftlvariable name="user" type="podd.model.user.User" -->
<#-- @ftlvariable name="searchCriteriaWeb" type="podd.search.web.SearchCriteriaWeb" -->
<#-- @ftlvariable name="searchResponse" type="podd.search.SearchResponse" -->
<#-- @ftlvariable name="hasAtLeastOneObjectType" type="java.lang.Boolean" -->
<#-- @ftlvariable name="objectTypeMap" type="java.util.Map<java.lang.String, java.lang.Boolean>" -->
<#-- @ftlvariable name="objectClasses" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="allTheseWordsError" type="java.lang.String" -->
<#-- @ftlvariable name="anyTheseWordsError" type="java.lang.String" -->
<#-- @ftlvariable name="phraseError" type="java.lang.String" -->
<#-- @ftlvariable name="excludeError" type="java.lang.String" -->
<#-- @ftlvariable name="startDateCreatedError" type="java.lang.String" -->
<#-- @ftlvariable name="endDateCreatedError" type="java.lang.String" -->

<form enctype="multipart/form-data" method="post" action="/podd/search">

    <h3 class="underlined_heading">Advanced Search Query
		<a href="javascript:animatedcollapse.toggle('searchContent')" icon="toggle" title="View Search Query"></a>
	</h3>

    <#escape x as x?html>
    <div id="searchContent">
        <div id="searchTerms" class="fieldset">

            <!-- search terms -->
            <ol>
                <li><h3 class="underlined_heading">Search Terms</h3></li>
                <#include "searchWords.html.ftl">
            </ol>

            <!-- scope -->
            <#if (user)??>
            <ol>
                <li><h3 class="underlined_heading">Scope</h3></li>
                <#include "searchScope.html.ftl">
            </ol>
            </#if>

            <!-- creation date -->
            <ol>
                <li><h3 class="underlined_heading">Creation Date</h3></li>
                <#include "searchCreationDate.html.ftl">
            </ol>

            <!-- object types -->
            <ol>
                <li><h3 class="underlined_heading">Object Types</h3></li>
                <li>
                    <label for="objectTypes" class="bold">Select object type:</label>
                    <select id="objectTypes" name="objectTypes" multiple size="8" searchSelect='true'>
                        <option value=""
                            <#if (!( hasAtLeastOneObjectType)??) || !hasAtLeastOneObjectType >selected="true"</#if>
                        >All</option>

                        <#list objectClasses?keys as typeURI>
                            <option value="${typeURI}"
                                <#if objectTypeMap?? && (objectTypeMap[typeURI]) >
                                    selected="true"
                                </#if>
                            >${objectClasses[typeURI]}</option>
                        </#list>
                    </select>
                </li>
            </ol>

            <!-- submit buttons -->
            <div id="buttonwrapper">
                <button type="submit">Search</button>
                <button type="button" onclick='resetSearchQuery();'>Reset</button>
            </div>
            <!-- if this is not here the buttons fall out of the fieldset?? -->
            <p><br></p>
        </div>
    </div>
    </#escape>
</form>