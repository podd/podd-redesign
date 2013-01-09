<#-- @ftlvariable name="user" type="podd.model.user.User" -->
<#-- @ftlvariable name="searchResponse" type="podd.search.SearchResponse" -->
<#-- @ftlvariable name="searchCriteriaWeb" type="podd.search.web.SearchCriteriaWeb" -->

<li>
    <div class="radioGroup">
    <input id="scopeMyProjects" name="scopeMyProjects" type="checkbox" value="true" class="narrow" searchCheckbox='true'
        <#if !searchResponse?? || !searchCriteriaWeb?? || searchCriteriaWeb.getScopeMyProjects()>
           checked="checked"
        </#if>
    >
    <label for="scopeMyProjects" class="inline, bold">My Projects</label>
    &nbsp;&nbsp;
    <input id="scopePublicProjects" name="scopePublicProjects" type="checkbox" value="true" class="narrow" searchCheckbox='true'
        <#if !searchResponse?? || !searchCriteriaWeb?? || searchCriteriaWeb.getScopePublicProjects()>
           checked="checked"
        </#if>
    >
    <label for="scopePublicProjects" class="inline, bold">Public Projects</label>
    </div>
</li>